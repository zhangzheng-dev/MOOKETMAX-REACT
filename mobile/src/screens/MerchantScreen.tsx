import React, {useCallback, useEffect, useMemo, useRef, useState} from 'react';
import {ActivityIndicator, SectionList, RefreshControl, StyleSheet, Text, View} from 'react-native';
import type {NativeStackScreenProps} from '@react-navigation/native-stack';
import {mooketApi} from '../api/mooketApi';
import {DataDashboard} from '../components/detail/DataDashboard';
import {FilterBar, type FilterKey} from '../components/detail/FilterBar';
import {FilterPanelSheet, MultiSelectChips} from '../components/detail/FilterPanelSheet';
import {MerchantHeader} from '../components/detail/MerchantHeader';
import {SelfSelectButton, toHistoryMerchantId} from '../components/detail/SelfSelectButton';
import {MerchantSortBar, type MerchantSortMode} from '../components/detail/MerchantSortBar';
import {OfferCardCompact} from '../components/detail/OfferCardCompact';
import {OriginalTextSheet} from '../components/detail/OriginalTextSheet';
import {ErrorState} from '../components/common/ErrorState';
import type {OfferTab} from '../components/detail/TabAndSortBar';
import type {RootStackParamList} from '../navigation/routes';
import {colors} from '../theme/colors';
import type {EmployeeOffer, MerchantDetail, OfferFeedItem, OfferSummary} from '../types/api';
import {copyToClipboard, dialPhone} from '../utils/contact';
import type {OriginalTextPayload} from '../utils/originalText';
import {extractCity, splitTags} from '../utils/offer';
import {countUniqueFactories, countUniqueProducts, pickTabNumber} from '../utils/tabStats';

type Props = NativeStackScreenProps<RootStackParamList, 'Merchant'>;

const pageSize = 20;
const merchantCategoryOptions = ['牛', '猪'] as const;
type MerchantCategory = (typeof merchantCategoryOptions)[number];
type MerchantCategoryFilter = 'all' | MerchantCategory;

export function MerchantScreen({navigation, route}: Props) {
  const {
    merchantId,
    category,
    initialTab,
    initialCategory,
    initialCountry,
    initialFactoryNo,
    initialProductName,
  } = route.params;
  const [detail, setDetail] = useState<MerchantDetail | null>(null);
  const [offers, setOffers] = useState<OfferSummary[]>([]);
  const [inquiries, setInquiries] = useState<OfferSummary[]>([]);
  const [tab, setTab] = useState<OfferTab>(initialTab ?? 'offer');
  const [categoryFilter, setCategoryFilter] = useState<MerchantCategoryFilter>(initialCategory ?? 'all');
  const [sort, setSort] = useState<MerchantSortMode>({kind: 'comprehensive'});
  const [page, setPage] = useState({offer: 1, inquiry: 1});
  const [hasMore, setHasMore] = useState({offer: true, inquiry: true});
  const [loading, setLoading] = useState(false);
  const [loadingMore, setLoadingMore] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const sortParam = useMemo(() => {
    if (sort.kind === 'comprehensive') return 'comprehensive';
    if (sort.kind === 'publish_time') return 'publish_time';
    return sort.order === 'asc' ? 'price_asc' : sort.order === 'desc' ? 'price_desc' : 'comprehensive';
  }, [sort]);

  const [expanded, setExpanded] = useState<Set<string>>(new Set());
  const [originalText, setOriginalText] = useState<OriginalTextPayload | null>(null);
  const [activeFilter, setActiveFilter] = useState<FilterKey | null>(null);
  const [country, setCountry] = useState<string | null>(initialCountry ?? null);
  const [factories, setFactories] = useState<Set<string>>(
    () => new Set(initialFactoryNo ? [`${initialCountry ?? ''}${initialFactoryNo}`] : []),
  );
  const [regions, setRegions] = useState<Set<string>>(new Set());
  const [products, setProducts] = useState<Set<string>>(
    () => new Set(initialProductName ? [initialProductName] : []),
  );
  const [goodsTypes, setGoodsTypes] = useState<Set<string>>(new Set());
  const [feedingMethods, setFeedingMethods] = useState<Set<string>>(new Set());
  const [tagFilters, setTagFilters] = useState<Set<string>>(new Set());
  const preserveInitialFiltersRef = useRef(true);
  const hasInitialSearchFilters = Boolean(initialCountry || initialFactoryNo || initialProductName);
  const activeSearchProduct = products.size === 1 ? Array.from(products)[0] : undefined;
  const activeSearchFactory =
    factories.size === 1
      ? getFactoryNoFromFilterKey(Array.from(factories)[0], country)
      : undefined;

  const loadInitialSearchPage = useCallback(
    async (type: OfferTab, targetPage: number, targetSort: string) => {
      const categories = getMerchantCategories(categoryFilter, category);
      const results = await Promise.allSettled(
        categories.map(item =>
          mooketApi.getOfferFeed({
            category: item,
            type,
            merchantId,
            country: country ?? undefined,
            factoryNo: activeSearchFactory,
            productName: activeSearchProduct,
            page: targetPage,
            pageSize,
            sortBy: targetSort,
            skipCache: true,
          }),
        ),
      );
      const pages = results
        .filter(
          (item): item is PromiseFulfilledResult<Awaited<ReturnType<typeof mooketApi.getOfferFeed>>> =>
            item.status === 'fulfilled',
        )
        .map(item => item.value);

      return {
        products: groupFeedItemsForMerchant(pages.flatMap(item => item.items ?? [])),
        hasMore: pages.some(item => targetPage < (item.totalPages ?? targetPage)),
      };
    },
    [
      category,
      categoryFilter,
      activeSearchFactory,
      activeSearchProduct,
      country,
      merchantId,
    ],
  );

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const categories = getMerchantCategories(categoryFilter, category);
      const results = await Promise.allSettled(
        categories.map(item => mooketApi.getMerchantDetail(merchantId, item)),
      );
      const details = results
        .filter((item): item is PromiseFulfilledResult<MerchantDetail> => item.status === 'fulfilled')
        .map(item => item.value);
      if (details.length === 0) {
        throw new Error('加载失败');
      }
      const data = combineMerchantDetails(details);
      setDetail(data);
      if (hasInitialSearchFilters) {
        const [offerPage, inquiryPage] = await Promise.all([
          loadInitialSearchPage('offer', 1, 'comprehensive'),
          loadInitialSearchPage('inquiry', 1, 'comprehensive'),
        ]);
        setOffers(offerPage.products);
        setInquiries(inquiryPage.products);
        setHasMore({offer: offerPage.hasMore, inquiry: inquiryPage.hasMore});
      } else {
        // 普通商家详情仍沿用原有聚合接口。
        setOffers(data.offers ?? []);
        setInquiries(data.inquiries ?? []);
        setHasMore({
          offer: (data.totalOffers ?? data.offers?.length ?? 0) > (data.offers?.length ?? 0),
          inquiry: (data.totalInquiries ?? data.inquiries?.length ?? 0) > (data.inquiries?.length ?? 0),
        });
      }
      setPage({offer: 1, inquiry: 1});
    } catch (e) {
      setError(e instanceof Error ? e.message : '加载失败');
    } finally {
      setLoading(false);
    }
  }, [category, categoryFilter, hasInitialSearchFilters, loadInitialSearchPage, merchantId]);

  // 当排序或 tab 变化时重新加载第一页
  const reloadSorted = useCallback(async () => {
    setLoading(true);
    try {
      if (hasInitialSearchFilters) {
        const result = await loadInitialSearchPage(tab, 1, sortParam);
        if (tab === 'offer') setOffers(result.products);
        else setInquiries(result.products);
        setPage(p => ({...p, [tab]: 1}));
        setHasMore(m => ({...m, [tab]: result.hasMore}));
        return;
      }
      const categories = getMerchantCategories(categoryFilter, category);
      const results = await Promise.allSettled(
        categories.map(item => mooketApi.getMerchantProducts(merchantId, tab, item, 1, pageSize, sortParam)),
      );
      const pages = results
        .filter((item): item is PromiseFulfilledResult<Awaited<ReturnType<typeof mooketApi.getMerchantProducts>>> => item.status === 'fulfilled')
        .map(item => item.value);
      const incoming = pages.flatMap(item => item.products ?? []);
      if (tab === 'offer') setOffers(incoming);
      else setInquiries(incoming);
      setPage(p => ({...p, [tab]: 1}));
      setHasMore(m => ({...m, [tab]: pages.some(item => 1 < (item.totalPages ?? 1))}));
    } catch {
      // 静默
    } finally {
      setLoading(false);
    }
  }, [
    category,
    categoryFilter,
    hasInitialSearchFilters,
    loadInitialSearchPage,
    merchantId,
    sortParam,
    tab,
  ]);

  useEffect(() => {
    load().catch(() => undefined);
  }, [load]);

  // 排序变化时重新请求
  const isFirstRender = useRef(true);
  useEffect(() => {
    if (isFirstRender.current) {
      isFirstRender.current = false;
      return;
    }
    reloadSorted().catch(() => undefined);
  }, [sortParam, tab]);

  useEffect(() => {
    setExpanded(new Set());
  }, [categoryFilter, tab]);

  useEffect(() => {
    if (preserveInitialFiltersRef.current) {
      preserveInitialFiltersRef.current = false;
      return;
    }
    setCountry(null);
    setFactories(new Set());
    setRegions(new Set());
    setProducts(new Set());
    setGoodsTypes(new Set());
    setFeedingMethods(new Set());
    setTagFilters(new Set());
    setActiveFilter(null);
  }, [categoryFilter]);

  const currentList = tab === 'offer' ? offers : inquiries;

  const loadMore = useCallback(async () => {
    if (loadingMore || loading) return;
    if (!hasMore[tab]) return;
    const nextPage = page[tab] + 1;
    setLoadingMore(true);
    try {
      if (hasInitialSearchFilters) {
        const result = await loadInitialSearchPage(tab, nextPage, sortParam);
        const updater = (prev: OfferSummary[]) => mergeOffers(prev, result.products);
        if (tab === 'offer') setOffers(updater);
        else setInquiries(updater);
        setPage(p => ({...p, [tab]: nextPage}));
        setHasMore(m => ({...m, [tab]: result.hasMore}));
        return;
      }
      const categories = getMerchantCategories(categoryFilter, category);
      const results = await Promise.allSettled(
        categories.map(item => mooketApi.getMerchantProducts(merchantId, tab, item, nextPage, pageSize, sortParam)),
      );
      const pages = results
        .filter((item): item is PromiseFulfilledResult<Awaited<ReturnType<typeof mooketApi.getMerchantProducts>>> => item.status === 'fulfilled')
        .map(item => item.value);
      const incoming = pages.flatMap(item => item.products ?? []);
      const updater = (prev: OfferSummary[]) => mergeOffers(prev, incoming);
      if (tab === 'offer') setOffers(updater);
      else setInquiries(updater);
      setPage(p => ({...p, [tab]: nextPage}));
      setHasMore(m => ({...m, [tab]: pages.some(item => nextPage < (item.totalPages ?? nextPage))}));
    } catch {
      // 静默
    } finally {
      setLoadingMore(false);
    }
  }, [
    category,
    categoryFilter,
    hasInitialSearchFilters,
    hasMore,
    loadInitialSearchPage,
    loading,
    loadingMore,
    merchantId,
    page,
    sortParam,
    tab,
  ]);

  const filteredAndSorted = useMemo(() => {
    let list = currentList.slice();
    if (country) list = list.filter(item => item.country === country);
    if (factories.size > 0) {
      list = list.filter(item => factories.has(`${item.country ?? ''}${item.factoryNo ?? ''}`));
    }
    if (regions.size > 0) {
      list = list.filter(item => offerRegions(item).some(city => regions.has(city)));
    }
    if (products.size > 0) {
      list = list.filter(item => item.productName != null && products.has(item.productName));
    }
    if (goodsTypes.size > 0) {
      list = list.filter(item => offerGoodsTypes(item).some(type => goodsTypes.has(type)));
    }
    if (feedingMethods.size > 0) {
      list = list.filter(item => offerFeedingMethods(item).some(method => feedingMethods.has(method)));
    }
    if (tagFilters.size > 0) {
      list = list.filter(item => offerTags(item).some(tag => tagFilters.has(tag)));
    }
    // 排序由后端处理，前端只做筛选
    return list;
  }, [country, currentList, factories, feedingMethods, goodsTypes, products, regions, tagFilters]);

  const currentFilterOptions = useMemo(
    () => (tab === 'offer' ? detail?.offerFilterOptions : detail?.inquiryFilterOptions) ?? null,
    [detail?.inquiryFilterOptions, detail?.offerFilterOptions, tab],
  );
  const dashboardProductCount =
    pickTabNumber(detail, tab, {
      offer: ['offerProductCount', 'productOfferCount', 'todayOfferProductCount'],
      inquiry: ['inquiryProductCount', 'productInquiryCount', 'todayInquiryProductCount'],
    }) ??
    currentFilterOptions?.products?.length ??
    countUniqueProducts(currentList);
  const dashboardFactoryCount =
    pickTabNumber(detail, tab, {
      offer: ['offerFactoryCount', 'factoryOfferCount', 'todayOfferFactoryCount'],
      inquiry: ['inquiryFactoryCount', 'factoryInquiryCount', 'todayInquiryFactoryCount'],
    }) ??
    currentFilterOptions?.countryFactories?.length ??
    countUniqueFactories(currentList);

  const allCountries = useMemo(
    () => Array.from(new Set([...(currentFilterOptions?.countries ?? []), ...(country ? [country] : [])])),
    [country, currentFilterOptions?.countries],
  );
  const allFactoryKeys = useMemo(
    () =>
      Array.from(new Set([...(currentFilterOptions?.countryFactories ?? []), ...factories])).filter(item =>
        country == null ? true : item.startsWith(country),
      ),
    [country, currentFilterOptions?.countryFactories, factories],
  );
  const allRegions = useMemo(() => currentFilterOptions?.regions ?? [], [currentFilterOptions?.regions]);
  const allProductNames = useMemo(
    () => Array.from(new Set([...(currentFilterOptions?.products ?? []), ...products])),
    [currentFilterOptions?.products, products],
  );
  const allTags = useMemo(() => currentFilterOptions?.tags ?? [], [currentFilterOptions?.tags]);

  const filters = [
    {key: 'category' as const, label: getCategoryFilterLabel(categoryFilter), hasSelection: true},
    {key: 'product' as const, label: getSelectedFilterLabel(products, '产品'), hasSelection: products.size > 0},
    {
      key: 'countryFactory' as const,
      label: getCountryFactoryFilterLabel(country, factories),
      hasSelection: country != null || factories.size > 0,
    },
    {key: 'region' as const, label: '地区', hasSelection: regions.size > 0},
    {key: 'goodsType' as const, label: '货物类型', hasSelection: goodsTypes.size > 0},
    {key: 'feedingMethod' as const, label: '饲养方式', hasSelection: feedingMethods.size > 0},
    {key: 'tag' as const, label: '标签', hasSelection: tagFilters.size > 0},
  ];

  function toggleExpand(key: string) {
    setExpanded(prev => {
      const next = new Set(prev);
      if (next.has(key)) next.delete(key);
      else next.add(key);
      return next;
    });
  }

  const merchantDisplayName = detail?.merchantShortName || detail?.merchantName || null;
  const merchantCardId = detail?.merchantId ?? merchantId;
  const selfSelectCard = merchantCardId || merchantDisplayName
    ? {
        cardType: 'merchant',
        merchantId: merchantCardId,
        merchantName: detail?.merchantName ?? merchantDisplayName,
        merchantShortName: detail?.merchantShortName ?? null,
      }
    : null;
  const selfSelectPayload = merchantDisplayName
    ? {
        searchWord: merchantDisplayName,
        searchType: '\u5546\u5bb6',
        merchantId: toHistoryMerchantId(merchantCardId),
      }
    : null;

  return (
    <View style={styles.container}>
      <MerchantHeader
        merchant={detail}
        onBack={() => navigation.goBack()}
        rightAction={
          <SelfSelectButton category={category} card={selfSelectCard} payload={selfSelectPayload} />
        }
      />

      {loading && !detail ? (
        <View style={styles.loading}>
          <ActivityIndicator color={colors.primary} />
        </View>
      ) : error && !detail ? (
        <ErrorState message={error} onRetry={load} />
      ) : detail ? (
        <SectionList
          sections={[{key: 'items', data: filteredAndSorted}]}
          keyExtractor={(item, index) => offerKey(item, index)}
          stickySectionHeadersEnabled
            initialNumToRender={8}
            maxToRenderPerBatch={5}
            windowSize={3}
            removeClippedSubviews
          ListHeaderComponent={
            <View>
              <DataDashboard
                stats={[
                  {label: '近2日报盘', value: detail.todayOfferCount},
                  {label: '近2日求购', value: detail.todayInquiryCount},
                ]}
              />
              <View style={styles.gap} />
            </View>
          }
          renderSectionHeader={() => (
            <View style={styles.stickyHeader}>
              <MerchantSortBar tab={tab} onTabChange={setTab} sort={sort} onSortChange={setSort} />
              <FilterBar filters={filters} active={activeFilter} onPress={setActiveFilter} />
            </View>
          )}
          renderItem={({item, index}) => {
            const key = offerKey(item, index);
            return (
              <OfferCardCompact
                offer={item}
                expanded={expanded.has(key)}
                onToggle={() => toggleExpand(key)}
                merchantPhone={detail.contactPhone ?? null}
                plateType={tab === 'inquiry' ? 'inquiry' : 'offer'}
                merchantId={detail.merchantId}
                merchantName={detail.merchantShortName || detail.merchantName}
                onCopyPhone={() =>
                  copyToClipboard(detail.contactPhone ?? '', '已复制手机号').catch(() => undefined)
                }
                onDial={() => dialPhone(detail.contactPhone)}
                onViewOriginalText={value => setOriginalText(value)}
              />
            );
          }}
          refreshControl={<RefreshControl refreshing={loading} onRefresh={load} />}
          onEndReached={loadMore}
          onEndReachedThreshold={0.4}
          ListFooterComponent={
            <View style={styles.footer}>
              {loadingMore ? (
                <Text style={styles.footerText}>加载中...</Text>
              ) : !hasMore[tab] ? (
                <Text style={styles.footerText}>没有更多了～</Text>
              ) : null}
            </View>
          }
          ListEmptyComponent={!loading ? <Text style={styles.empty}>暂无数据</Text> : null}
        />
      ) : null}

      <FilterPanelSheet
        visible={activeFilter === 'category'}
        title="大类"
        onClose={() => setActiveFilter(null)}
        onReset={() => {
          setCategoryFilter('all');
          setActiveFilter(null);
        }}
        onConfirm={() => setActiveFilter(null)}>
        <MultiSelectChips
          options={['全部', '牛', '猪']}
          selected={new Set([getCategoryFilterLabel(categoryFilter)])}
          onToggle={value => {
            setCategoryFilter(value === '牛' || value === '猪' ? value : 'all');
            setActiveFilter(null);
          }}
        />
      </FilterPanelSheet>

      <FilterPanelSheet
        visible={activeFilter === 'countryFactory'}
        title="国家·厂号"
        onClose={() => setActiveFilter(null)}
        onReset={() => {
          setCountry(null);
          setFactories(new Set());
          setActiveFilter(null);
        }}
        onConfirm={() => setActiveFilter(null)}>
        <Text style={styles.sectionLabel}>国家</Text>
        <MultiSelectChips
          options={allCountries}
          selected={new Set(country ? [country] : [])}
          onToggle={value => setCountry(prev => (prev === value ? null : value))}
        />
        <Text style={[styles.sectionLabel, styles.sectionLabelTop]}>厂号</Text>
        <MultiSelectChips
          options={allFactoryKeys}
          selected={factories}
          onToggle={value => setFactories(prev => toggle(prev, value))}
        />
      </FilterPanelSheet>

      <FilterPanelSheet
        visible={activeFilter === 'region'}
        title="地区"
        onClose={() => setActiveFilter(null)}
        onReset={() => {
          setRegions(new Set());
          setActiveFilter(null);
        }}
        onConfirm={() => setActiveFilter(null)}>
        <MultiSelectChips
          options={allRegions}
          selected={regions}
          onToggle={value => setRegions(prev => toggle(prev, value))}
        />
      </FilterPanelSheet>

      <FilterPanelSheet
        visible={activeFilter === 'product'}
        title="产品"
        onClose={() => setActiveFilter(null)}
        onReset={() => {
          setProducts(new Set());
          setActiveFilter(null);
        }}
        onConfirm={() => setActiveFilter(null)}>
        <MultiSelectChips
          options={allProductNames}
          selected={products}
          onToggle={value => setProducts(prev => toggle(prev, value))}
        />
      </FilterPanelSheet>

      <FilterPanelSheet
        visible={activeFilter === 'goodsType'}
        title="货物类型"
        onClose={() => setActiveFilter(null)}
        onReset={() => {
          setGoodsTypes(new Set());
          setActiveFilter(null);
        }}
        onConfirm={() => setActiveFilter(null)}>
        <MultiSelectChips
          options={['现货', '半期货', '期货']}
          selected={goodsTypes}
          onToggle={value => setGoodsTypes(prev => toggle(prev, value))}
        />
      </FilterPanelSheet>

      <FilterPanelSheet
        visible={activeFilter === 'feedingMethod'}
        title="饲养方式"
        onClose={() => setActiveFilter(null)}
        onReset={() => {
          setFeedingMethods(new Set());
          setActiveFilter(null);
        }}
        onConfirm={() => setActiveFilter(null)}>
        <MultiSelectChips
          options={['草饲', '谷饲']}
          selected={feedingMethods}
          onToggle={value => setFeedingMethods(prev => toggle(prev, value))}
        />
      </FilterPanelSheet>

      <FilterPanelSheet
        visible={activeFilter === 'tag'}
        title="标签"
        onClose={() => setActiveFilter(null)}
        onReset={() => {
          setTagFilters(new Set());
          setActiveFilter(null);
        }}
        onConfirm={() => setActiveFilter(null)}>
        <MultiSelectChips
          options={allTags.length > 0 ? allTags : ['大日期', '可开票', '整柜', '一口价']}
          selected={tagFilters}
          onToggle={value => setTagFilters(prev => toggle(prev, value))}
        />
      </FilterPanelSheet>

      <OriginalTextSheet
        visible={originalText !== null}
        text={originalText?.text ?? ''}
        keywords={originalText?.keywords ?? []}
        onClose={() => setOriginalText(null)}
      />
    </View>
  );
}

function offerKey(offer: OfferSummary, index: number) {
  if (offer.offerId != null) return `${offer.offerId}`;
  return `${offer.country ?? ''}-${offer.factoryNo ?? ''}-${offer.productName ?? ''}-${index}`;
}

function getMerchantCategories(filter: MerchantCategoryFilter, fallbackCategory: string): MerchantCategory[] {
  if (filter === 'all') return [...merchantCategoryOptions];
  if (merchantCategoryOptions.includes(filter)) return [filter];
  return [fallbackCategory === '猪' ? '猪' : '牛'];
}

function getCategoryFilterLabel(filter: MerchantCategoryFilter) {
  return filter === 'all' ? '全部' : filter;
}

function getSelectedFilterLabel(values: Set<string>, fallback: string) {
  if (values.size === 1) return Array.from(values)[0];
  return values.size > 1 ? `${fallback}(${values.size})` : fallback;
}

function getCountryFactoryFilterLabel(country: string | null, factories: Set<string>) {
  if (factories.size === 1) return Array.from(factories)[0];
  if (factories.size > 1) return `国家厂号(${factories.size})`;
  return country || '国家厂号';
}

function getFactoryNoFromFilterKey(value: string, country: string | null) {
  if (country && value.startsWith(country)) {
    return value.slice(country.length);
  }
  return value;
}

function groupFeedItemsForMerchant(items: OfferFeedItem[]): OfferSummary[] {
  const groups = new Map<string, OfferSummary>();

  items.forEach(item => {
    const key = `${item.category ?? ''}|${item.country ?? ''}|${item.factoryNo ?? ''}|${item.productName ?? ''}`;
    const employee = feedItemToEmployeeOffer(item);
    const existing = groups.get(key);
    if (existing) {
      existing.employeeOffers = [...(existing.employeeOffers ?? []), employee];
      return;
    }

    groups.set(key, {
      offerId: item.offerId,
      productName: item.productName,
      country: item.country,
      factoryNo: item.factoryNo,
      price: item.price,
      priceMax: item.priceMax,
      goodsLocation: item.goodsLocation,
      tags: item.tags,
      goodsType: item.goodsType,
      feedingType: item.feedingType,
      publishTime: item.publishTime,
      employeeOffers: [employee],
    });
  });

  return Array.from(groups.values());
}

function feedItemToEmployeeOffer(item: OfferFeedItem): EmployeeOffer {
  return {
    offerId: item.offerId,
    userNickname: item.userNickname,
    price: item.price,
    priceMax: item.priceMax,
    weight: item.weight,
    goodsLocation: item.goodsLocation,
    tags: item.tags,
    goodsType: item.goodsType,
    feedingType: item.feedingType,
    fatRatio: item.fatRatio,
    cattleBreed: item.cattleBreed,
    remark: item.remark,
    offerOriginalText:
      item.offerOriginalText ??
      item.originalText ??
      item.originalContent ??
      item.sourceText ??
      item.rawText,
    publishTime: item.publishTime,
  };
}

function combineMerchantDetails(details: MerchantDetail[]): MerchantDetail {
  const primary = details[0];
  const offers = details.flatMap(item => item.offers ?? []);
  const inquiries = details.flatMap(item => item.inquiries ?? []);
  return {
    ...primary,
    todayOfferCount: sumNumbers(details.map(item => item.todayOfferCount)),
    todayInquiryCount: sumNumbers(details.map(item => item.todayInquiryCount)),
    todayProductCount: countUniqueProducts([...offers, ...inquiries]),
    todayFactoryCount: countUniqueFactories([...offers, ...inquiries]),
    offers,
    inquiries,
    offerFilterOptions: combineMerchantFilterOptions(details.map(item => item.offerFilterOptions ?? null)),
    inquiryFilterOptions: combineMerchantFilterOptions(details.map(item => item.inquiryFilterOptions ?? null)),
    totalOffers: sumNumbers(details.map(item => item.totalOffers ?? item.offers?.length ?? 0)),
    totalInquiries: sumNumbers(details.map(item => item.totalInquiries ?? item.inquiries?.length ?? 0)),
  };
}

function combineMerchantFilterOptions(options: Array<MerchantDetail['offerFilterOptions'] | null>) {
  return {
    countries: unique(options.flatMap(item => item?.countries ?? [])),
    countryFactories: unique(options.flatMap(item => item?.countryFactories ?? [])),
    regions: unique(options.flatMap(item => item?.regions ?? [])),
    products: unique(options.flatMap(item => item?.products ?? [])),
    goodsTypes: unique(options.flatMap(item => item?.goodsTypes ?? [])),
    feedingMethods: unique(options.flatMap(item => item?.feedingMethods ?? [])),
    tags: unique(options.flatMap(item => item?.tags ?? [])),
  };
}

function sumNumbers(values: Array<number | null | undefined>) {
  return values.reduce((total, value) => total + (Number(value) || 0), 0);
}

function offerRegions(offer: OfferSummary): string[] {
  return unique([
    extractCity(offer.goodsLocation),
    ...(offer.employeeOffers ?? []).map(emp => extractCity(emp.goodsLocation)),
  ]);
}

function offerGoodsTypes(offer: OfferSummary): string[] {
  return unique([
    offer.goodsType ?? '',
    ...(offer.employeeOffers ?? []).map(emp => emp.goodsType ?? ''),
  ]);
}

function offerFeedingMethods(offer: OfferSummary): string[] {
  return unique([
    offer.feedingType ?? '',
    ...(offer.employeeOffers ?? []).map(emp => emp.feedingMethod ?? ''),
  ]);
}

function offerTags(offer: OfferSummary): string[] {
  return unique([
    ...splitTags(offer.tags, 8),
    ...(offer.employeeOffers ?? []).flatMap(emp => splitTags(emp.tags, 8)),
  ]);
}

function mergeOffers(prev: OfferSummary[], incoming: OfferSummary[]): OfferSummary[] {
  const seen = new Set(prev.map((item, index) => offerKey(item, index)));
  const next = prev.slice();
  incoming.forEach((item, index) => {
    const key = offerKey(item, prev.length + index);
    if (!seen.has(key)) {
      seen.add(key);
      next.push(item);
    }
  });
  return next;
}

function unique(values: string[]): string[] {
  const result: string[] = [];
  const seen = new Set<string>();
  for (const value of values) {
    const trimmed = value.trim();
    if (!trimmed) continue;
    if (seen.has(trimmed)) continue;
    seen.add(trimmed);
    result.push(trimmed);
  }
  return result;
}

function toggle<T>(set: Set<T>, value: T): Set<T> {
  const next = new Set(set);
  if (next.has(value)) next.delete(value);
  else next.add(value);
  return next;
}

const styles = StyleSheet.create({
  container: {flex: 1, backgroundColor: colors.background},
  loading: {paddingVertical: 48, alignItems: 'center'},
  topTabs: {borderTopWidth: StyleSheet.hairlineWidth, borderTopColor: '#EFF5F3', borderBottomWidth: 1, borderBottomColor: colors.border},
  gap: {height: 12, backgroundColor: '#F4FBF8'},
  footer: {alignItems: 'center', paddingVertical: 8},
  footerText: {color: '#9DA4A3', fontSize: 11, lineHeight: 18},
  empty: {textAlign: 'center', paddingVertical: 48, color: '#9DA4A3', fontSize: 14},
  sectionLabel: {color: colors.textMuted, fontSize: 12, fontWeight: '600', marginBottom: 8},
  sectionLabelTop: {marginTop: 16},
  stickyHeader: {backgroundColor: '#FFFFFF', borderBottomWidth: 1, borderBottomColor: '#DEE4E1', zIndex: 10, elevation: 3},
});
