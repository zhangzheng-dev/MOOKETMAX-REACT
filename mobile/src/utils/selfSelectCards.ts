import {mooketApi} from '../api/mooketApi';
import type {HomeCardItem, OfferFeedItem, SearchHistory} from '../types/api';
import {getHomeCardEntityKey} from './homeFallbackCards';

function parseLocalDateTime(value?: string | null) {
  if (!value) return 0;
  const match = value
    .trim()
    .match(/^(\d{4})-(\d{1,2})-(\d{1,2})(?:[ T](\d{1,2}):(\d{1,2})(?::(\d{1,2}))?)?/);
  if (match) {
    const [, y, m, d, h = '0', min = '0', s = '0'] = match;
    return new Date(
      Number(y),
      Number(m) - 1,
      Number(d),
      Number(h),
      Number(min),
      Number(s),
    ).getTime();
  }
  const parsed = Date.parse(value);
  return Number.isFinite(parsed) ? parsed : 0;
}

export function sortSelfSelectCardsByCreateTime(
  cards: HomeCardItem[],
  histories: SearchHistory[] = [],
) {
  const historyById = new Map(histories.map(item => [item.historyId, item]));

  return cards
    .map((card, index) => {
      const history =
        card.historyId == null ? undefined : historyById.get(card.historyId);
      const createTime = card.createTime ?? history?.createTime ?? null;
      return {
        card: createTime ? {...card, createTime} : card,
        index,
        createdAt: parseLocalDateTime(createTime),
      };
    })
    .sort((a, b) => {
      if (a.createdAt !== b.createdAt) return b.createdAt - a.createdAt;
      const aId = a.card.historyId ?? 0;
      const bId = b.card.historyId ?? 0;
      if (aId !== bId) return bId - aId;
      return a.index - b.index;
    })
    .map(item => item.card);
}

export function mergeSelfSelectCardsWithHistories(
  cards: HomeCardItem[],
  histories: SearchHistory[] = [],
) {
  const seen = new Set<string>();
  const result: HomeCardItem[] = [];
  const historyById = new Map(histories.map(history => [history.historyId, history]));

  cards.forEach(card => {
    const normalizedCard = normalizeSelfSelectCard(
      card,
      card.historyId == null ? null : historyById.get(card.historyId)?.searchWord,
    );
    collectCardKeys(normalizedCard).forEach(key => seen.add(key));
    result.push(normalizedCard);
  });

  histories.forEach(history => {
    if (history.isSelfSelect !== 1) return;
    const card = buildSelfSelectCardFromHistory(history);
    if (!card) return;

    const normalizedCard = normalizeSelfSelectCard(card);
    const keys = collectCardKeys(normalizedCard);
    if (keys.some(key => seen.has(key))) return;

    keys.forEach(key => seen.add(key));
    result.push(normalizedCard);
  });

  return result;
}

export async function enrichSelfSelectCards(
  category: string,
  cards: HomeCardItem[],
) {
  return Promise.all(
    cards.map(card =>
      needsMerchantOfferEnrichment(card)
        ? enrichMerchantSelfSelectCard(category, card).catch(() => card)
        : needsFactoryProductEnrichment(card)
          ? enrichFactoryProductSelfSelectCard(category, card).catch(() => card)
        : Promise.resolve(card),
    ),
  );
}

function collectCardKeys(card: HomeCardItem) {
  const keys = new Set<string>();
  if (card.historyId != null) keys.add(`history:${card.historyId}`);
  const entityKey = getHomeCardEntityKey(card);
  if (entityKey) keys.add(`entity:${normalizeText(entityKey)}`);

  switch (card.cardType) {
    case 'product':
      addCardKey(keys, ['product', card.productName]);
      break;
    case 'country':
      addCardKey(keys, ['country', card.country]);
      break;
    case 'brand':
      addCardKey(keys, ['brand', card.brandName]);
      break;
    case 'merchant':
      addCardKey(keys, ['merchant', card.merchantId == null ? card.merchantName : String(card.merchantId)]);
      addCardKey(keys, ['merchant-name', card.merchantName]);
      addCardKey(keys, ['merchant-name', card.merchantShortName]);
      break;
    case 'factory':
      addCardKey(keys, ['factory', card.country, card.factoryNo]);
      break;
    case 'countryProduct':
      addCardKey(keys, ['countryProduct', card.country, card.productName]);
      break;
    case 'factoryProduct':
      addCardKey(keys, ['factoryProduct', card.country, card.factoryNo, card.productName]);
      break;
    case 'brandProduct':
      addCardKey(keys, ['brandProduct', card.brandName, card.productName]);
      if (card.brandId != null) addCardKey(keys, ['brandProductId', String(card.brandId), card.productName]);
      break;
    default:
      break;
  }
  return Array.from(keys);
}

function addCardKey(keys: Set<string>, parts: Array<string | null | undefined>) {
  const normalized = parts.map(item => normalizeText(item ?? ''));
  if (normalized.every(Boolean)) keys.add(`semantic:${normalized.join(':')}`);
}

function buildSelfSelectCardFromHistory(history: SearchHistory): HomeCardItem | null {
  const searchType = history.searchType.trim();
  const base = {
    historyId: history.historyId,
    createTime: history.createTime,
  };

  switch (searchType) {
    case '产品': {
      const productName = history.productName || history.searchWord;
      if (!productName) return null;
      return {
        ...base,
        cardType: 'product',
        productId: history.productId ?? null,
        productName,
      };
    }
    case '国家':
      if (!history.country && !history.searchWord) return null;
      return {
        ...base,
        cardType: 'country',
        country: history.country || history.searchWord,
      };
    case '品牌':
      if (!history.searchWord) return null;
      return {
        ...base,
        cardType: 'brand',
        brandId: history.brandId ?? null,
        brandName: history.searchWord,
      };
    case '商家':
      if (!history.searchWord && history.merchantId == null) return null;
      return {
        ...base,
        cardType: 'merchant',
        merchantId: history.merchantId ?? null,
        merchantName: history.searchWord || null,
        merchantShortName: history.searchWord || null,
      };
    case '国家厂号':
      if (!history.country || !history.factoryNo) return null;
      return {
        ...base,
        cardType: 'factory',
        country: history.country,
        factoryNo: history.factoryNo,
      };
    case '国家产品':
      if (!history.country || !history.productName) return null;
      return {
        ...base,
        cardType: 'countryProduct',
        country: history.country,
        productName: history.productName,
      };
    case '国家厂号产品':
      if (!history.country || !history.factoryNo || !history.productName) return null;
      return normalizeSelfSelectCard({
        ...base,
        cardType: 'factoryProduct',
        country: history.country,
        factoryNo: history.factoryNo,
        productName: history.productName,
        searchWord: history.searchWord,
      } as HomeCardItem & {searchWord?: string | null});
    case '品牌产品': {
      if (!history.productName) return null;
      const brandName = inferBrandName(history.searchWord, history.productName);
      return {
        ...base,
        cardType: 'brandProduct',
        brandId: history.brandId ?? null,
        brandName,
        productName: history.productName,
      };
    }
    default:
      return null;
  }
}

function normalizeSelfSelectCard(card: HomeCardItem, searchWordOverride?: string | null) {
  if (card.cardType !== 'factoryProduct') return card;

  const country = card.country?.trim() || '';
  const searchWord = searchWordOverride?.trim() || getCardSearchWord(card);
  if (!country || !searchWord || !searchWord.startsWith(country)) {
    return card;
  }

  const parsed = parseFactoryProductFromSearchWord(country, searchWord);
  if (!parsed) return card;

  if (
    normalizeText(parsed.factoryNo) === normalizeText(card.factoryNo ?? '') &&
    normalizeText(parsed.productName) === normalizeText(card.productName ?? '')
  ) {
    return card;
  }

  return {
    ...card,
    factoryNo: parsed.factoryNo,
    productName: parsed.productName,
  };
}

function getCardSearchWord(card: HomeCardItem) {
  const explicit = (card as HomeCardItem & {searchWord?: string | null}).searchWord;
  if (explicit?.trim()) return explicit.trim();
  if (card.country && card.factoryNo && card.productName) {
    return `${card.country}${card.factoryNo}${card.productName}`;
  }
  return '';
}

function parseFactoryProductFromSearchWord(country: string, searchWord: string) {
  const tail = searchWord.slice(country.length).trim();
  if (!tail) return null;

  const match = tail.match(/^([A-Za-z0-9-]+)(?=[\u3400-\u9fff])/);
  if (!match) return null;

  const factoryNo = match[1].trim();
  const productName = tail.slice(factoryNo.length).trim();
  if (!factoryNo || !productName) return null;

  return {factoryNo, productName};
}

function needsMerchantOfferEnrichment(card: HomeCardItem) {
  if (card.cardType !== 'merchant') return false;
  const hasOfferPreview = Boolean(card.latestOffers?.length);
  const hasCount = card.todayOfferCount != null;
  const hasLookupKey =
    card.merchantId != null ||
    Boolean(card.merchantName?.trim()) ||
    Boolean(card.merchantShortName?.trim());
  return hasLookupKey && (!hasOfferPreview || !hasCount);
}

function needsFactoryProductEnrichment(card: HomeCardItem) {
  if (card.cardType !== 'factoryProduct') return false;
  if (!card.country?.trim() || !card.factoryNo?.trim() || !card.productName?.trim()) {
    return false;
  }
  return (
    card.priceMin == null ||
    card.priceMax == null ||
    card.todayOfferCount == null ||
    card.inquiryCount == null ||
    !card.hotMerchants?.length ||
    !card.trendPoints?.length
  );
}

async function enrichFactoryProductSelfSelectCard(
  category: string,
  card: HomeCardItem,
) {
  if (!card.country || !card.factoryNo || !card.productName) return card;

  const detail = await mooketApi.getCountryFactoryProductDetail(
    card.country,
    card.factoryNo,
    card.productName,
    category,
    'offer',
    'comprehensive',
    1,
    6,
  );

  return {
    ...card,
    productId: card.productId ?? detail.productId ?? null,
    country: detail.country || card.country,
    factoryNo: detail.factoryNo || card.factoryNo,
    productName: detail.productName || card.productName,
    priceMin: card.priceMin ?? detail.priceMin ?? null,
    priceMax: card.priceMax ?? detail.priceMax ?? null,
    priceChange: card.priceChange ?? detail.priceChange ?? null,
    priceChangeRate: card.priceChangeRate ?? detail.priceChangeRate ?? null,
    todayOfferCount: card.todayOfferCount ?? detail.offerCount ?? null,
    inquiryCount: card.inquiryCount ?? detail.inquiryCount ?? null,
    merchantCount: card.merchantCount ?? detail.merchantCount ?? null,
    hotMerchants: card.hotMerchants?.length
      ? card.hotMerchants
      : (detail.merchantOffers ?? []).slice(0, 3).map(group => {
          const priceRange = getEmployeeOfferPriceRange(group.employeeOffers ?? []);
          return {
            merchantId: group.merchantId ?? null,
            merchantName: group.merchantName ?? null,
            priceMin: priceRange.min,
            priceMax: priceRange.max,
          };
        }),
    trendPoints: card.trendPoints?.length
      ? card.trendPoints
      : (detail.priceHistory7Days ?? [])
          .filter(point => point.avgPrice != null)
          .map(point => ({
            date: point.date,
            fullDate: point.fullDate,
            avgPrice: point.avgPrice,
            offerCount: point.offerCount,
          })),
  };
}

function getEmployeeOfferPriceRange(
  offers: Array<{price?: string | number | null}>,
) {
  const prices = offers
    .map(offer => parseOfferPrice(offer.price))
    .filter((price): price is number => price != null);
  if (!prices.length) return {min: null, max: null};
  return {min: Math.min(...prices), max: Math.max(...prices)};
}

function parseOfferPrice(value?: string | number | null) {
  if (typeof value === 'number' && Number.isFinite(value) && value > 0) {
    return value;
  }
  if (typeof value !== 'string') return null;
  const match = value.match(/\d+(?:\.\d+)?/);
  if (!match) return null;
  const parsed = Number(match[0]);
  return Number.isFinite(parsed) && parsed > 0 ? parsed : null;
}

async function enrichMerchantSelfSelectCard(
  category: string,
  card: HomeCardItem,
) {
  const byId = await loadMerchantOffersById(category, card);
  const byKeyword = byId.items.length ? null : await loadMerchantOffersByKeyword(category, card);
  const page = byId.items.length ? byId : byKeyword;
  const items = page?.items ?? [];
  if (!items.length) return card;

  const first = items[0];
  return {
    ...card,
    merchantId: card.merchantId ?? first.merchantId ?? null,
    merchantName: card.merchantName ?? first.merchantName ?? null,
    merchantShortName:
      card.merchantShortName ?? first.merchantShortName ?? first.merchantName ?? null,
    latestOffers: card.latestOffers?.length
      ? card.latestOffers
      : items.slice(0, 2).map(toHomeLatestOffer),
    todayOfferCount: card.todayOfferCount ?? page?.totalCount ?? items.length,
  };
}

async function loadMerchantOffersById(category: string, card: HomeCardItem) {
  if (card.merchantId == null || !String(card.merchantId).trim()) {
    return {items: [], totalCount: 0};
  }

  return mooketApi.getOfferFeed({
    category,
    type: 'offer',
    merchantId: card.merchantId,
    page: 1,
    pageSize: 3,
    sortBy: 'publishTime',
  });
}

async function loadMerchantOffersByKeyword(category: string, card: HomeCardItem) {
  const keyword = card.merchantShortName?.trim() || card.merchantName?.trim();
  if (!keyword) return {items: [], totalCount: 0};

  const page = await mooketApi.getOfferFeed({
    category,
    type: 'offer',
    keyword,
    page: 1,
    pageSize: 20,
    sortBy: 'publishTime',
  });
  const items = (page.items ?? []).filter(item => merchantNameMatches(item, keyword));
  return {...page, items, totalCount: items.length || page.totalCount};
}

function merchantNameMatches(item: OfferFeedItem, keyword: string) {
  const target = normalizeText(keyword);
  if (!target) return false;
  const names = [item.merchantName, item.merchantShortName]
    .map(value => normalizeText(value ?? ''))
    .filter(Boolean);
  return names.some(name => name === target || name.includes(target) || target.includes(name));
}

function toHomeLatestOffer(item: OfferFeedItem): Record<string, unknown> {
  return {
    offerId: item.offerId ?? null,
    productName: item.productName ?? null,
    country: item.country ?? null,
    factoryNo: item.factoryNo ?? null,
    price: item.price ?? item.priceMax ?? null,
    priceMax: item.priceMax ?? null,
    weight: item.weight ?? null,
    publishTime: item.publishTime ?? null,
  };
}

function inferBrandName(searchWord: string, productName: string) {
  const trimmed = searchWord.trim();
  const product = productName.trim();
  if (!trimmed) return null;
  if (!product) return trimmed;

  const withoutProduct = trimmed.replace(product, '').trim();
  return withoutProduct || trimmed;
}

function normalizeText(value: string) {
  return value.trim().toLowerCase();
}
