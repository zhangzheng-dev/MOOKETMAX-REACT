export type OriginalTextPayload = {
  text: string;
  keywords: string[];
};

export type AnalyzeResult = {
  keywords: string[];
  segments: string[];
  bestSegmentIndex: number;
};

type BuildPayloadParams = {
  text?: string | null;
  country?: string | null;
  factoryNo?: string | null;
  productName?: string | null;
  price?: string | number | null;
  priceMax?: string | number | null;
  goodsType?: string | null;
  goodsLocation?: string | null;
  feedingType?: string | null;
  feedingMethod?: string | null;
  fatRatio?: string | null;
  cattleBreed?: string | null;
  tags?: string | null;
  remark?: string | null;
  publishTime?: string | null;
  userNickname?: string | null;
};

type AnchorTokens = {
  productName: string;
  factoryNo: string;
  country: string;
};

const MIN_CONFIDENT_SCORE = 8;
const ANCHOR_PRODUCT_PREFIX = '__anchor_product__:';
const ANCHOR_FACTORY_PREFIX = '__anchor_factory__:';
const ANCHOR_COUNTRY_PREFIX = '__anchor_country__:';

export function buildOriginalTextPayload(params: BuildPayloadParams): OriginalTextPayload {
  const text = (params.text ?? '').trim();
  const country = normalizeText(params.country);
  const factoryNo = normalizeText(params.factoryNo);
  const productName = normalizeText(params.productName);
  const goodsType = normalizeText(params.goodsType);
  const goodsLocation = normalizeLocation(params.goodsLocation);
  const feeding = normalizeText(params.feedingType) || normalizeText(params.feedingMethod);
  const fatRatio = normalizeText(params.fatRatio);
  const cattleBreed = normalizeText(params.cattleBreed);
  const remark = normalizeText(params.remark);
  const userNickname = normalizeText(params.userNickname);
  const publishTokens = buildPublishTimeTokens(params.publishTime);
  const priceTokens = buildPriceTokens(params.price, params.priceMax);
  const tags = splitTokens(params.tags);
  const remarkTokens = splitTokens(params.remark);

  const keywords = uniqueStrings([
    productName,
    factoryNo,
    country,
    goodsType,
    goodsLocation,
    feeding,
    fatRatio,
    cattleBreed,
    remark,
    userNickname,
    combineTokens(country, factoryNo),
    combineTokens(country, productName),
    combineTokens(factoryNo, productName),
    combineTokens(country, factoryNo, productName),
    combineCompact(country, factoryNo),
    combineCompact(country, productName),
    combineCompact(factoryNo, productName),
    combineCompact(country, factoryNo, productName),
    buildAnchorToken(ANCHOR_PRODUCT_PREFIX, productName),
    buildAnchorToken(ANCHOR_FACTORY_PREFIX, factoryNo),
    buildAnchorToken(ANCHOR_COUNTRY_PREFIX, country),
    ...priceTokens,
    ...publishTokens,
    ...tags,
    ...remarkTokens,
  ]).filter(item => item.length >= 2);

  return {text, keywords};
}

export function analyzeOriginalText(text: string, keywords: string[]): AnalyzeResult {
  const normalizedText = (text ?? '').trim();
  const parsed = parseKeywordPayload(keywords);
  const normalizedKeywords = parsed.keywords;
  const segments = splitSegments(normalizedText);

  if (!normalizedText || segments.length === 0) {
    return {
      keywords: normalizedKeywords,
      segments: normalizedText ? [normalizedText] : [],
      bestSegmentIndex: -1,
    };
  }

  let bestIndex = -1;
  let bestScore = 0;

  segments.forEach((segment, index) => {
    const score = scoreSegment(segment, normalizedKeywords, parsed.anchors);
    if (score > bestScore) {
      bestScore = score;
      bestIndex = index;
    }
  });

  return {
    keywords: normalizedKeywords,
    segments,
    bestSegmentIndex: bestScore >= MIN_CONFIDENT_SCORE ? bestIndex : -1,
  };
}

function parseKeywordPayload(keywords: string[]) {
  const anchors: AnchorTokens = {
    productName: '',
    factoryNo: '',
    country: '',
  };

  const result: string[] = [];

  uniqueStrings(keywords).forEach(keyword => {
    if (keyword.startsWith(ANCHOR_PRODUCT_PREFIX)) {
      anchors.productName = keyword.slice(ANCHOR_PRODUCT_PREFIX.length).trim();
      return;
    }
    if (keyword.startsWith(ANCHOR_FACTORY_PREFIX)) {
      anchors.factoryNo = keyword.slice(ANCHOR_FACTORY_PREFIX.length).trim();
      return;
    }
    if (keyword.startsWith(ANCHOR_COUNTRY_PREFIX)) {
      anchors.country = keyword.slice(ANCHOR_COUNTRY_PREFIX.length).trim();
      return;
    }
    if (keyword.length >= 2) {
      result.push(keyword);
    }
  });

  return {
    keywords: uniqueStrings(result),
    anchors,
  };
}

function splitSegments(text: string): string[] {
  const lines = text
    .split(/\r?\n+/)
    .map(item => item.trim())
    .filter(Boolean);

  const source = lines.length > 0 ? lines : [text];
  const result: string[] = [];

  for (const line of source) {
    const pieces = line
      .split(/(?<=[。！？；;!?,，、])/)
      .map(item => item.trim())
      .filter(Boolean);

    if (pieces.length > 0) {
      result.push(...pieces);
    } else if (line) {
      result.push(line);
    }
  }

  return uniqueStrings(result);
}

function scoreSegment(segment: string, keywords: string[], anchors: AnchorTokens): number {
  const target = normalizeComparable(segment);
  let score = 0;
  let hits = 0;

  for (const keyword of keywords) {
    const comparableKeyword = normalizeComparable(keyword);
    if (!comparableKeyword) continue;
    if (target.includes(comparableKeyword)) {
      hits += 1;
      score += keywordScore(keyword);
    }
  }

  score += scoreAnchorProximity(target, anchors);

  if (hits >= 2) score += 3;
  if (hits >= 3) score += 5;
  return score;
}

function scoreAnchorProximity(target: string, anchors: AnchorTokens): number {
  const product = normalizeComparable(anchors.productName);
  const factory = normalizeComparable(anchors.factoryNo);
  const country = normalizeComparable(anchors.country);

  const productPos = findPosition(target, product);
  const factoryPos = findPosition(target, factory);
  const countryPos = findPosition(target, country);

  let score = 0;

  if (productPos >= 0) {
    score += 10;
  } else {
    return score;
  }

  if (factoryPos >= 0) {
    score += 6;
  }
  if (countryPos >= 0) {
    score += 4;
  }

  if (productPos >= 0 && factoryPos >= 0) {
    score += proximityBonus(productPos, factoryPos, product.length, factory.length, 12, 10, 6);
  }
  if (productPos >= 0 && countryPos >= 0) {
    score += proximityBonus(productPos, countryPos, product.length, country.length, 16, 8, 4);
  }
  if (productPos >= 0 && factoryPos >= 0 && countryPos >= 0) {
    const windowStart = Math.min(productPos, factoryPos, countryPos);
    const windowEnd = Math.max(productPos + product.length, factoryPos + factory.length, countryPos + country.length);
    const span = windowEnd - windowStart;
    if (span <= 24) {
      score += 14;
    } else if (span <= 36) {
      score += 8;
    } else if (span <= 48) {
      score += 4;
    }
  }

  return score;
}

function proximityBonus(
  firstPos: number,
  secondPos: number,
  firstLength: number,
  secondLength: number,
  tightThreshold: number,
  mediumBonus: number,
  looseBonus: number,
) {
  const firstEnd = firstPos + firstLength;
  const secondEnd = secondPos + secondLength;
  const gap = Math.max(0, Math.max(firstPos, secondPos) - Math.min(firstEnd, secondEnd));

  if (gap <= tightThreshold) {
    return mediumBonus;
  }
  if (gap <= tightThreshold * 2) {
    return looseBonus;
  }
  return 0;
}

function findPosition(target: string, keyword: string) {
  if (!keyword) return -1;
  return target.indexOf(keyword);
}

function keywordScore(keyword: string) {
  if (/[0-9]/.test(keyword) && /[-/:]/.test(keyword)) return 5;
  if (/[0-9]/.test(keyword) && keyword.length >= 4) return 4;
  return Math.max(1, Math.min(keyword.length, 6));
}

function buildPriceTokens(min?: string | number | null, max?: string | number | null): string[] {
  const minText = normalizeNumberText(min);
  const maxText = normalizeNumberText(max);
  if (!minText) return [];

  const tokens = [`楼${minText}`, `¥${minText}`, `${minText}/kg`];

  if (!maxText || minText === maxText) {
    return tokens;
  }

  return tokens.concat([
    `楼${minText}-${maxText}`,
    `¥${minText}-${maxText}`,
    `${minText}-${maxText}/kg`,
    `楼${minText} - ${maxText}`,
    `¥${minText} - ${maxText}`,
  ]);
}

function buildPublishTimeTokens(value?: string | null): string[] {
  const text = normalizeText(value);
  if (!text) return [];

  const compact = text.replace(/\s+/g, '');
  const normalized = compact
    .replace(/\./g, '-')
    .replace(/\//g, '-')
    .replace(/年/g, '-')
    .replace(/月/g, '-')
    .replace(/日/g, '');

  const monthDay = extractMonthDayTokens(normalized);

  return uniqueStrings([text, compact, normalized, ...monthDay]);
}

function extractMonthDayTokens(value: string): string[] {
  const match = value.match(/(\d{1,2})-(\d{1,2})(?:\D|$)/);
  if (!match) return [];
  const month = match[1].padStart(2, '0');
  const day = match[2].padStart(2, '0');
  return [`${month}-${day}`, `${month}/${day}`, `${month}.${day}`];
}

function splitTokens(value?: string | null): string[] {
  if (!value) return [];
  return value
    .split(/[|,，、/]/)
    .map(item => item.trim())
    .filter(Boolean);
}

function combineTokens(...parts: Array<string | null | undefined>) {
  const values = parts.map(normalizeText).filter(Boolean);
  return values.join(' ');
}

function combineCompact(...parts: Array<string | null | undefined>) {
  const values = parts.map(normalizeText).filter(Boolean);
  return values.join('');
}

function buildAnchorToken(prefix: string, value: string) {
  return value ? `${prefix}${value}` : '';
}

function normalizeLocation(value?: string | null) {
  const text = normalizeText(value);
  if (!text) return '';
  return text.replace(/发货地/g, '').replace(/货物地/g, '').trim();
}

function normalizeNumberText(value?: string | number | null) {
  if (value == null || value === '') return '';
  return String(value).replace(/[^\d.]/g, '').trim();
}

function normalizeText(value?: string | null) {
  return (value ?? '').trim();
}

function normalizeComparable(value: string) {
  return value.toLowerCase().replace(/\s+/g, '').replace(/[|，。、]/g, '').trim();
}

function uniqueStrings(values: Array<string | null | undefined>): string[] {
  const seen = new Set<string>();
  const result: string[] = [];

  for (const value of values) {
    const text = (value ?? '').trim();
    if (!text || seen.has(text)) continue;
    seen.add(text);
    result.push(text);
  }

  return result;
}
