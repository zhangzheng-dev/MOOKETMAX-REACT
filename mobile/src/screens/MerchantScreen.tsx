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
import type {MerchantDetail, OfferSummary} from '../types/api';
import {copyToClipboard, dialPhone} from '../utils/contact';
import type {OriginalTextPayload} from '../utils/originalText';
import {extractCity, splitTags} from '../utils/offer';

type Props = NativeStackScreenProps<RootStackParamList, 'Merchant'>;

const pageSize = 20;

export function MerchantScreen({navigation, route}: Props) {
  const {merchantId, category} = route.params;
  const [detail, setDetail] = useState<MerchantDetail | null>(null);
  const [offers, setOffers] = useState<OfferSummary[]>([]);
  const [inquiries, setInquiries] = useState<OfferSummary[]>([]);
  const [tab, setTab] = useState<OfferTab>('offer');
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
  const [country, setCountry] = useState<string | null>(null);
  const [factories, setFactories] = useState<Set<string>>(new Set());
  const [regions, setRegions] = useState<Set<string>>(new Set());
  const [products, setProducts] = useState<Set<string>>(new Set());
  const [goodsTypes, setGoodsTypes] = useState<Set<string>>(new Set());
  const [feedingMethods, setFeedingMethods] = useState<Set<string>>(new Set());
  const [tagFilters, setTagFilters] = useState<Set<string>>(new Set());

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await mooketApi.getMerchantDetail(merchantId, category);
      setDetail(data);
      // 初始加载用第一页数据（后端默认综合排序）
      setOffers(data.offers ?? []);
      setInquiries(data.inquiries ?? []);
      setPage({offer: 1, inquiry: 1});
      setHasMore({
        offer: (data.totalOffers ?? data.offers?.length ?? 0) > (data.offers?.length ?? 0),
        inquiry: (data.totalInquiries ?? data.inquiries?.length ?? 0) > (data.inquiries?.length ?? 0),
      });
    } catch (e) {
      setError(e instanceof Error ? e.message : '加载失败');
    } finally {
      setLoading(false);
    }
  }, [category, merchantId]);

  // 当排序或 tab 变化时重新加载第一页
  const reloadSorted = useCallback(async () => {
    setLoading(true);
    try {
      const data = await mooketApi.getMerchantProducts(merchantId, tab, category, 1, pageSize, sortParam);
      const incoming = data.products ?? [];
      if (tab === 'offer') setOffers(incoming);
      else setInquiries(incoming);
      setPage(p => ({...p, [tab]: 1}));
      setHasMore(m => ({...m, [tab]: 1 < (data.totalPages ?? 1)}));
    } catch {
      // 静默
    } finally {
      setLoading(false);
    }
  }, [category, merchantId, sortParam, tab]);

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
  }, [tab]);

  const currentList = tab === 'offer' ? offers : inquiries;

  const loadMore = useCallback(async () => {
    if (loadingMore || loading) return;
    if (!hasMore[tab]) return;
    const nextPage = page[tab] + 1;
    setLoadingMore(true);
    try {
      const data = await mooketApi.getMerchantProducts(merchantId, tab, category, nextPage, pageSize, sortParam);
      const incoming = data.products ?? [];
      const updater = (prev: OfferSummary[]) => mergeOffers(prev, incoming);
      if (tab === 'offer') setOffers(updater);
      else setInquiries(updater);
      setPage(p => ({...p, [tab]: nextPage}));
      setHasMore(m => ({...m, [tab]: nextPage < (data.totalPages ?? nextPage)}));
    } catch {
      // 静默
    } finally {
      setLoadingMore(false);
    }
  }, [category, hasMore, loading, loadingMore, merchantId, page, sortParam, tab]);

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

  const allCountries = useMemo(() => currentFilterOptions?.countries ?? [], [currentFilterOptions?.countries]);
  const allFactoryKeys = useMemo(
    () =>
      (currentFilterOptions?.countryFactories ?? []).filter(item =>
        country == null ? true : item.startsWith(country),
      ),
    [country, currentFilterOptions?.countryFactories],
  );
  const allRegions = useMemo(() => currentFilterOptions?.regions ?? [], [currentFilterOptions?.regions]);
  const allProductNames = useMemo(() => currentFilterOptions?.products ?? [], [currentFilterOptions?.products]);
  const allTags = useMemo(() => currentFilterOptions?.tags ?? [], [currentFilterOptions?.tags]);

  const filters = [
    {key: 'countryFactory' as const, label: '国家厂号', hasSelection: country != null || factories.size > 0},
    {key: 'region' as const, label: '地区', hasSelection: regions.size > 0},
    {key: 'product' as const, label: '产品', hasSelection: products.size > 0},
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
                  {
                    label: tab === 'offer' ? '近2日报盘' : '近2日求购',
                    value: tab === 'offer' ? detail.todayOfferCount : detail.todayInquiryCount,
                    onPress: () =>
                      navigation.navigate('OfferFeed', {
                        category,
                        initialTab: tab,
                        keyword: detail.merchantShortName || detail.merchantName,
                        merchantId,
                      }),
                  },
                  {label: '产品数', value: detail.todayProductCount},
                  {label: '工厂数', value: detail.todayFactoryCount},
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
  gap: {height: 12, backgroundColor: '#F4FBF8'},
  footer: {alignItems: 'center', paddingVertical: 8},
  footerText: {color: '#9DA4A3', fontSize: 11, lineHeight: 18},
  empty: {textAlign: 'center', paddingVertical: 48, color: '#9DA4A3', fontSize: 14},
  sectionLabel: {color: colors.textMuted, fontSize: 12, fontWeight: '600', marginBottom: 8},
  sectionLabelTop: {marginTop: 16},
  stickyHeader: {backgroundColor: '#FFFFFF', borderBottomWidth: 1, borderBottomColor: '#DEE4E1', zIndex: 10, elevation: 3},
});
