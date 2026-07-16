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
  merchantName?: string | null;
  merchantShortName?: string | null;
};

type AnchorTokens = {
  productName: string;
  factoryNo: string;
  factoryShort: string;
  country: string;
  signature: string;
  prices: string[];
  productAliases: string[];
};

const MIN_CONFIDENT_SCORE = 8;
const ANCHOR_PRODUCT_PREFIX = '__anchor_product__:';
const ANCHOR_FACTORY_PREFIX = '__anchor_factory__:';
const ANCHOR_FACTORY_SHORT_PREFIX = '__anchor_factory_short__:';
const ANCHOR_COUNTRY_PREFIX = '__anchor_country__:';
const ANCHOR_SIGNATURE_PREFIX = '__anchor_signature__:';
const ANCHOR_PRICE_PREFIX = '__anchor_price__:';
const ANCHOR_PRODUCT_ALIAS_PREFIX = '__anchor_product_alias__:';

export function buildOriginalTextPayload(params: BuildPayloadParams): OriginalTextPayload {
  const text = (params.text ?? '').trim();
  const country = normalizeText(params.country);
  const factoryNo = normalizeText(params.factoryNo);
  const factoryShort = normalizeFactoryShort(factoryNo);
  const productName = normalizeText(params.productName);
  const productAliases = buildProductAliases(productName);
  const goodsType = normalizeText(params.goodsType);
  const goodsLocation = normalizeLocation(params.goodsLocation);
  const feeding = normalizeText(params.feedingType) || normalizeText(params.feedingMethod);
  const fatRatio = normalizeText(params.fatRatio);
  const cattleBreed = normalizeText(params.cattleBreed);
  const remark = normalizeText(params.remark);
  const userNickname = normalizeText(params.userNickname);
  const merchantName = normalizeText(params.merchantShortName) || normalizeText(params.merchantName);
  const publishTokens = buildPublishTimeTokens(params.publishTime);
  const priceTokens = buildPriceTokens(params.price, params.priceMax);
  const tags = splitTokens(params.tags);
  const remarkTokens = splitTokens(params.remark);
  const signature = uniqueStrings([
    productName,
    ...productAliases,
    country,
    factoryNo,
    factoryShort,
    goodsType,
    goodsLocation,
    feeding,
    fatRatio,
    cattleBreed,
    remark,
    userNickname,
    merchantName,
    ...priceTokens,
    ...publishTokens,
    ...tags,
    ...remarkTokens,
  ]).join('');

  const keywords = uniqueStrings([
    productName,
    ...productAliases,
    factoryNo,
    factoryShort,
    country,
    goodsType,
    goodsLocation,
    feeding,
    fatRatio,
    cattleBreed,
    remark,
    userNickname,
    merchantName,
    combineTokens(country, factoryNo),
    combineTokens(country, productName),
    combineTokens(factoryNo, productName),
    combineTokens(country, factoryNo, productName),
    combineCompact(country, factoryNo),
    combineCompact(country, factoryShort),
    combineCompact(country, productName),
    combineCompact(factoryNo, productName),
    combineCompact(factoryShort, productName),
    combineCompact(country, factoryNo, productName),
    combineCompact(country, factoryShort, productName),
    buildAnchorToken(ANCHOR_PRODUCT_PREFIX, productName),
    buildAnchorToken(ANCHOR_FACTORY_PREFIX, factoryNo),
    buildAnchorToken(ANCHOR_FACTORY_SHORT_PREFIX, factoryShort),
    buildAnchorToken(ANCHOR_COUNTRY_PREFIX, country),
    buildAnchorToken(ANCHOR_SIGNATURE_PREFIX, signature),
    ...priceTokens.map(item => buildAnchorToken(ANCHOR_PRICE_PREFIX, item)),
    ...productAliases.map(item => buildAnchorToken(ANCHOR_PRODUCT_ALIAS_PREFIX, item)),
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
    factoryShort: '',
    country: '',
    signature: '',
    prices: [],
    productAliases: [],
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
    if (keyword.startsWith(ANCHOR_FACTORY_SHORT_PREFIX)) {
      anchors.factoryShort = keyword.slice(ANCHOR_FACTORY_SHORT_PREFIX.length).trim();
      return;
    }
    if (keyword.startsWith(ANCHOR_COUNTRY_PREFIX)) {
      anchors.country = keyword.slice(ANCHOR_COUNTRY_PREFIX.length).trim();
      return;
    }
    if (keyword.startsWith(ANCHOR_SIGNATURE_PREFIX)) {
      anchors.signature = keyword.slice(ANCHOR_SIGNATURE_PREFIX.length).trim();
      return;
    }
    if (keyword.startsWith(ANCHOR_PRICE_PREFIX)) {
      const price = normalizeNumberText(keyword.slice(ANCHOR_PRICE_PREFIX.length).trim());
      if (price) anchors.prices.push(price);
      return;
    }
    if (keyword.startsWith(ANCHOR_PRODUCT_ALIAS_PREFIX)) {
      const alias = keyword.slice(ANCHOR_PRODUCT_ALIAS_PREFIX.length).trim();
      if (alias) anchors.productAliases.push(alias);
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
      .split(/(?<=[。！？；;!?,，、])|[ \t]{2,}/)
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

  score += scorePreciseOfferEvidence(target, anchors);
  score += scoreAnchorProximity(target, anchors);
  score += scoreSignatureSimilarity(target, anchors.signature);

  if (hits >= 2) score += 3;
  if (hits >= 3) score += 5;
  return score;
}

function scorePreciseOfferEvidence(target: string, anchors: AnchorTokens): number {
  const factoryCandidates = uniqueStrings([
    normalizeComparable(anchors.factoryNo),
    normalizeComparable(anchors.factoryShort),
  ]);
  const prices = uniqueStrings(anchors.prices.map(normalizeNumberText));
  const aliases = uniqueStrings([
    normalizeComparable(anchors.productName),
    ...anchors.productAliases.map(normalizeComparable),
  ]).filter(item => item.length >= 2);

  const hasFactory = factoryCandidates.some(item => item && target.includes(item));
  const hasPrice = prices.some(price => price && targetIncludesPrice(target, price));
  const hasAlias = aliases.some(alias => alias && target.includes(alias));

  let score = 0;
  if (hasFactory && hasPrice) score += 52;
  if (hasFactory && hasAlias) score += 28;
  if (hasPrice && hasAlias) score += 18;
  if (hasFactory) score += 8;
  if (hasAlias) score += 6;
  return score;
}

function targetIncludesPrice(target: string, price: string): boolean {
  if (!price) return false;
  const escaped = price.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
  const pattern = new RegExp(`(^|[^\\d.])${escaped}([^\\d.]|$)`);
  return pattern.test(target);
}

function scoreSignatureSimilarity(target: string, signature: string): number {
  const signatureTokens = buildSimilarityTokens(signature);
  if (signatureTokens.size === 0) return 0;

  const targetTokens = buildSimilarityTokens(target);
  if (targetTokens.size === 0) return 0;

  let overlap = 0;
  signatureTokens.forEach(token => {
    if (targetTokens.has(token)) {
      overlap += 1;
    }
  });

  if (overlap === 0) return 0;

  const base = Math.round((overlap / Math.min(signatureTokens.size, 18)) * 16);
  const bonus = overlap >= 6 ? 8 : overlap >= 4 ? 5 : overlap >= 2 ? 2 : 0;
  return base + bonus;
}

function buildSimilarityTokens(value: string): Set<string> {
  const normalized = normalizeComparable(value);
  const tokens = new Set<string>();

  if (!normalized) return tokens;

  const wordMatches = normalized.match(/[a-z]+\d*[a-z0-9]*|\d+(?:\.\d+)?|[\u4e00-\u9fff]{2,}/g) ?? [];
  wordMatches.forEach(item => {
    if (item.length >= 2) {
      tokens.add(item);
    }
  });

  for (let index = 0; index < normalized.length - 1; index += 1) {
    tokens.add(normalized.slice(index, index + 2));
  }

  return tokens;
}

function scoreAnchorProximity(target: string, anchors: AnchorTokens): number {
  const product = normalizeComparable(anchors.productName);
  const factory = normalizeComparable(anchors.factoryNo);
  const factoryShort = normalizeComparable(anchors.factoryShort);
  const country = normalizeComparable(anchors.country);

  const productPos = findPosition(target, product);
  const factoryPos = Math.max(findPosition(target, factory), findPosition(target, factoryShort));
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

  const tokens = [minText, `楼${minText}`, `¥${minText}`, `${minText}/kg`];

  if (!maxText || minText === maxText) {
    return tokens;
  }

  return tokens.concat([
    `楼${minText}-${maxText}`,
    `¥${minText}-${maxText}`,
    `${minText}-${maxText}/kg`,
    `楼${minText} - ${maxText}`,
    `¥${minText} - ${maxText}`,
    maxText,
    `楼${maxText}`,
    `¥${maxText}`,
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

function buildProductAliases(productName: string): string[] {
  const text = normalizeText(productName);
  if (!text) return [];

  const aliases = new Set<string>();
  aliases.add(text);
  aliases.add(text.replace(/^[牛猪]/, ''));

  if (text.includes('件套')) {
    aliases.add('件套');
    if (text.includes('前八') || text.includes('八件套')) {
      [
        '前腱',
        '后腱',
        '板腱',
        '牛腩',
        '胸肉',
        '肋条',
        '保乐肩',
        '上脑心',
        '小米龙',
      ].forEach(item => aliases.add(item));
    }
  }

  return Array.from(aliases).filter(item => item.length >= 2);
}

function normalizeLocation(value?: string | null) {
  const text = normalizeText(value);
  if (!text) return '';
  return text.replace(/发货地/g, '').replace(/货物地/g, '').trim();
}

function normalizeFactoryShort(value?: string | null) {
  const text = normalizeText(value);
  if (!text) return '';
  const numeric = text.match(/\d+/g)?.join('');
  return numeric ?? '';
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
