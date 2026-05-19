import React, {useCallback, useEffect, useMemo, useState} from 'react';
import {
  ActivityIndicator,
  SectionList,
  Pressable,
  RefreshControl,
  StyleSheet,
  Text,
  View,
} from 'react-native';
import Svg, {Path} from 'react-native-svg';
import type {NativeStackScreenProps} from '@react-navigation/native-stack';
import {mooketApi} from '../api/mooketApi';
import {CountryProductDashboard} from '../components/detail/CountryProductDashboard';
import {DetailTopBar} from '../components/detail/DetailTopBar';
import {FilterBar, type FilterKey} from '../components/detail/FilterBar';
import {FilterPanelSheet, MultiSelectChips} from '../components/detail/FilterPanelSheet';
import {MerchantOfferGroupCard} from '../components/detail/MerchantOfferGroupCard';
import {OriginalTextSheet} from '../components/detail/OriginalTextSheet';
import {TabAndSortBar, type OfferTab, type SortMode} from '../components/detail/TabAndSortBar';
import {ErrorState} from '../components/common/ErrorState';
import type {RootStackParamList} from '../navigation/routes';
import {colors} from '../theme/colors';
import type {CountryFactoryProductDetail, MerchantOfferGroup} from '../types/api';
import {extractCity, splitTags} from '../utils/offer';

type Props = NativeStackScreenProps<RootStackParamList, 'CountryFactoryProduct'>;

const pageSize = 20;
type LocalFilterKey = Exclude<FilterKey, 'countryFactory' | 'product' | 'famousMerchant'>;

export function CountryFactoryProductScreen({navigation, route}: Props) {
  const {country, factoryNo, productName, category} = route.params;
  const [data, setData] = useState<CountryFactoryProductDetail | null>(null);
  const [tab, setTab] = useState<OfferTab>('offer');
  const [sort, setSort] = useState<SortMode>({kind: 'comprehensive'});
  const [page, setPage] = useState(1);
  const [loading, setLoading] = useState(false);
  const [loadingMore, setLoadingMore] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [originalText, setOriginalText] = useState<string | null>(null);
  const [activeFilter, setActiveFilter] = useState<LocalFilterKey | null>(null);

  const [famousMerchant, setFamousMerchant] = useState(false);
  const [merchants, setMerchants] = useState<Set<string>>(new Set());
  const [regions, setRegions] = useState<Set<string>>(new Set());
  const [goodsTypes, setGoodsTypes] = useState<Set<string>>(new Set());
  const [feedingMethods, setFeedingMethods] = useState<Set<string>>(new Set());
  const [tags, setTags] = useState<Set<string>>(new Set());

  const loadFirst = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      setPage(1);
      const next = await mooketApi.getCountryFactoryProductDetail(
        country,
        factoryNo,
        productName,
        category,
        tab,
        sortToParam(sort),
        1,
        pageSize,
      );
      setData(next);
    } catch (e) {
      setError(e instanceof Error ? e.message : '加载失败');
    } finally {
      setLoading(false);
    }
  }, [category, country, factoryNo, productName, sort, tab]);

  useEffect(() => {
    loadFirst().catch(() => undefined);
  }, [loadFirst]);

  const loadMore = useCallback(async () => {
    if (loading || loadingMore || !data) return;
    if (page >= (data.totalPages ?? 1)) return;
    const next = page + 1;
    setLoadingMore(true);
    try {
      const more = await mooketApi.getCountryFactoryProductDetail(
        country,
        factoryNo,
        productName,
        category,
        tab,
        sortToParam(sort),
        next,
        pageSize,
      );
      setPage(next);
      setData(prev =>
        prev
          ? {
              ...more,
              merchantOffers: mergeMerchantOffers(prev.merchantOffers, more.merchantOffers),
            }
          : more,
      );
    } catch {
      // 静默
    } finally {
      setLoadingMore(false);
    }
  }, [category, country, data, factoryNo, loading, loadingMore, page, productName, sort, tab]);

  const filtered = useMemo(() => {
    const groups = data?.merchantOffers ?? [];
    return groups
      .filter(group => {
        if (famousMerchant && !group.isFamousMerchant) return false;
        if (merchants.size > 0) {
          const key = `${group.merchantId ?? group.merchantName ?? ''}`;
          if (!merchants.has(key)) return false;
        }
        return true;
      })
      .map(group => ({
        ...group,
        employeeOffers: (group.employeeOffers ?? []).filter(emp => {
          if (regions.size > 0) {
            const city = extractCity(emp.goodsLocation);
            if (!regions.has(city)) return false;
          }
          if (goodsTypes.size > 0) {
            if (!emp.goodsType || !goodsTypes.has(emp.goodsType)) return false;
          }
          if (feedingMethods.size > 0) {
            if (!emp.feedingType || !feedingMethods.has(emp.feedingType)) return false;
          }
          if (tags.size > 0) {
            const offerTags = splitTags(emp.tags, 8);
            const ok = offerTags.some(t => tags.has(t));
            if (!ok) return false;
          }
          return true;
        }),
      }))
      .filter(group => group.employeeOffers.length > 0);
  }, [data?.merchantOffers, famousMerchant, feedingMethods, goodsTypes, merchants, regions, tags]);

  const allRegions = useMemo(
    () =>
      unique(
        (data?.merchantOffers ?? []).flatMap(group =>
          (group.employeeOffers ?? []).map(emp => extractCity(emp.goodsLocation)),
        ),
      ),
    [data?.merchantOffers],
  );

  const allGoodsTypes = useMemo(
    () =>
      unique(
        (data?.merchantOffers ?? []).flatMap(group =>
          (group.employeeOffers ?? []).map(emp => emp.goodsType ?? ''),
        ),
      ),
    [data?.merchantOffers],
  );

  const allFeedings = useMemo(
    () =>
      unique(
        (data?.merchantOffers ?? []).flatMap(group =>
          (group.employeeOffers ?? []).map(emp => emp.feedingType ?? ''),
        ),
      ),
    [data?.merchantOffers],
  );

  const allTags = useMemo(
    () =>
      unique(
        (data?.merchantOffers ?? []).flatMap(group =>
          (group.employeeOffers ?? []).flatMap(emp => splitTags(emp.tags, 8)),
        ),
      ),
    [data?.merchantOffers],
  );

  const allMerchants = useMemo(
    () =>
      (data?.merchantOffers ?? []).map(group => ({
        key: `${group.merchantId ?? group.merchantName ?? ''}`,
        label: group.merchantName ?? `商家-${group.merchantId ?? ''}`,
      })),
    [data?.merchantOffers],
  );

  const filterDefs = [
    {key: 'famousMerchant' as const, label: '知名商家', hasSelection: famousMerchant, toggle: true},
    {key: 'merchant' as const, label: '商家筛选', hasSelection: merchants.size > 0},
    {key: 'region' as const, label: '地区', hasSelection: regions.size > 0},
    {key: 'priceRange' as const, label: '价格区间', hasSelection: false},
    {key: 'goodsType' as const, label: '货物类型', hasSelection: goodsTypes.size > 0},
    {key: 'feedingMethod' as const, label: '饲养方式', hasSelection: feedingMethods.size > 0},
    {key: 'tag' as const, label: '标签', hasSelection: tags.size > 0},
  ];

  function handleFilterPress(key: FilterKey) {
    if (key === 'famousMerchant') {
      setFamousMerchant(prev => !prev);
      return;
    }
    setActiveFilter(key as LocalFilterKey);
  }

  return (
    <View style={styles.container}>
      <DetailTopBar
        onBack={() => navigation.goBack()}
        tags={[
          {
            text: `${country}${factoryNo}`,
            onClose: () => {
              if (data?.productId) {
                navigation.navigate('Product', {
                  productId: data.productId,
                  category,
                  productName,
                });
              }
            },
          },
          {
            text: productName,
            onClose: () => navigation.navigate('Factory', {country, factoryNo, category}),
          },
        ]}
      />

      {loading && !data ? (
        <View style={styles.loading}>
          <ActivityIndicator color={colors.primary} />
        </View>
      ) : error && !data ? (
        <ErrorState message={error} onRetry={loadFirst} />
      ) : data ? (
        <>
          <SectionList
            sections={[{key: 'items', data: filtered}]}
            keyExtractor={(item, index) => `${item.merchantId ?? item.merchantName ?? index}`}
            stickySectionHeadersEnabled
            initialNumToRender={10}
            maxToRenderPerBatch={10}
            windowSize={5}
            ListHeaderComponent={
              <View>
                <CountryProductDashboard
                  country={`${data.country || country}${data.factoryNo || factoryNo}`}
                  productName={data.productName || productName}
                  isInquiry={tab === 'inquiry'}
                  priceMin={data.priceMin}
                  priceMax={data.priceMax}
                  priceChange={data.priceChange}
                  priceChangeRate={data.priceChangeRate}
                  offerCount={data.offerCount}
                  inquiryCount={data.inquiryCount}
                  merchantCount={data.merchantCount}
                  history7Days={data.priceHistory7Days}
                  history30Days={data.priceHistory30Days}
                />
                <View style={styles.gap} />
              </View>
            }
            renderSectionHeader={() => (
              <View style={styles.stickyHeader}>
                <TabAndSortBar
                  tab={tab}
                  onTabChange={setTab}
                  sort={sort}
                  onSortChange={setSort}
                  showPublishTime
                />
                <FilterBar
                  filters={filterDefs}
                  active={activeFilter as FilterKey | null}
                  onPress={handleFilterPress}
                />
              </View>
            )}
            renderItem={({item}) => (
              <MerchantOfferGroupCard
                group={item}
                isInquiry={tab === 'inquiry'}
                onCopyPhone={item.merchantPhone ?? undefined}
                onDial={item.merchantPhone ?? undefined}
                onViewOriginalText={value => setOriginalText(value)}
              />
            )}
            ItemSeparatorComponent={() => <View style={styles.itemDivider} />}
            refreshControl={<RefreshControl refreshing={loading} onRefresh={loadFirst} />}
            onEndReached={loadMore}
            onEndReachedThreshold={0.4}
            ListFooterComponent={
              <View style={styles.footer}>
                {loadingMore ? (
                  <Text style={styles.footerText}>加载中...</Text>
                ) : page >= (data.totalPages ?? 1) ? (
                  <Text style={styles.footerText}>没有更多了～</Text>
                ) : null}
              </View>
            }
            ListEmptyComponent={!loading ? <Text style={styles.empty}>暂无数据</Text> : null}
          />

          {data.hasSubstitute ? (
            <Pressable
              style={styles.substituteFab}
              onPress={() =>
                navigation.navigate('SubstituteProduct', {
                  country: data.country || country,
                  factoryNo: data.factoryNo || factoryNo,
                  productName: data.productName || productName,
                  category,
                })
              }>
              <View style={styles.substituteText}>
                <Text style={styles.substituteChar}>{'平'}</Text>
                <Text style={styles.substituteChar}>{'替'}</Text>
                <Text style={styles.substituteChar}>{'产'}</Text>
                <Text style={styles.substituteChar}>{'品'}</Text>
              </View>
              <Svg width={12} height={12} viewBox="0 0 12 12">
                <Path
                  d="M4 3l3 3-3 3"
                  stroke="#FFFFFF"
                  strokeWidth={1.4}
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  fill="none"
                />
              </Svg>
            </Pressable>
          ) : null}
        </>
      ) : null}

      <FilterPanelSheet
        visible={activeFilter === 'merchant'}
        title="商家筛选"
        onClose={() => setActiveFilter(null)}
        onReset={() => {
          setMerchants(new Set());
          setActiveFilter(null);
        }}
        onConfirm={() => setActiveFilter(null)}>
        <MultiSelectChips
          options={allMerchants.map(m => m.label)}
          selected={new Set(allMerchants.filter(m => merchants.has(m.key)).map(m => m.label))}
          onToggle={label => {
            const found = allMerchants.find(m => m.label === label);
            if (!found) return;
            setMerchants(prev => toggleSet(prev, found.key));
          }}
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
          onToggle={value => setRegions(prev => toggleSet(prev, value))}
        />
      </FilterPanelSheet>

      <FilterPanelSheet
        visible={activeFilter === 'priceRange'}
        title="价格区间"
        onClose={() => setActiveFilter(null)}
        onReset={() => setActiveFilter(null)}
        onConfirm={() => setActiveFilter(null)}>
        <Text style={styles.placeholder}>暂未实现价格区间筛选</Text>
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
          options={allGoodsTypes.length > 0 ? allGoodsTypes : ['现货', '半期货', '期货']}
          selected={goodsTypes}
          onToggle={value => setGoodsTypes(prev => toggleSet(prev, value))}
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
          options={allFeedings.length > 0 ? allFeedings : ['草饲', '谷饲']}
          selected={feedingMethods}
          onToggle={value => setFeedingMethods(prev => toggleSet(prev, value))}
        />
      </FilterPanelSheet>

      <FilterPanelSheet
        visible={activeFilter === 'tag'}
        title="标签"
        onClose={() => setActiveFilter(null)}
        onReset={() => {
          setTags(new Set());
          setActiveFilter(null);
        }}
        onConfirm={() => setActiveFilter(null)}>
        <MultiSelectChips
          options={allTags}
          selected={tags}
          onToggle={value => setTags(prev => toggleSet(prev, value))}
        />
      </FilterPanelSheet>

      <OriginalTextSheet
        visible={originalText !== null}
        text={originalText ?? ''}
        onClose={() => setOriginalText(null)}
      />
    </View>
  );
}

function sortToParam(sort: SortMode): string {
  if (sort.kind === 'comprehensive') return 'comprehensive';
  if (sort.kind === 'publishTime') return 'publish_time';
  return sort.order === 'asc' ? 'price_asc' : sort.order === 'desc' ? 'price_desc' : 'comprehensive';
}

function mergeMerchantOffers(prev: MerchantOfferGroup[], incoming: MerchantOfferGroup[]) {
  const map = new Map<string, MerchantOfferGroup>();
  prev.forEach((group, index) => {
    const key = `${group.merchantId ?? group.merchantName ?? index}`;
    map.set(key, group);
  });
  incoming.forEach((group, index) => {
    const key = `${group.merchantId ?? group.merchantName ?? prev.length + index}`;
    const existing = map.get(key);
    if (existing) {
      map.set(key, {
        ...group,
        employeeOffers: [...(existing.employeeOffers ?? []), ...(group.employeeOffers ?? [])],
      });
    } else {
      map.set(key, group);
    }
  });
  return Array.from(map.values());
}

function unique(values: string[]) {
  const set = new Set<string>();
  const out: string[] = [];
  for (const value of values) {
    const trimmed = value.trim();
    if (!trimmed) continue;
    if (set.has(trimmed)) continue;
    set.add(trimmed);
    out.push(trimmed);
  }
  return out;
}

function toggleSet<T>(set: Set<T>, value: T): Set<T> {
  const next = new Set(set);
  if (next.has(value)) next.delete(value);
  else next.add(value);
  return next;
}

const styles = StyleSheet.create({
  container: {flex: 1, backgroundColor: colors.background},
  loading: {paddingVertical: 48, alignItems: 'center'},
  gap: {height: 12, backgroundColor: '#F4FBF8'},
  itemDivider: {
    marginHorizontal: 16,
    height: 0,
  },
  footer: {alignItems: 'center', paddingVertical: 16},
  footerText: {color: '#9DA4A3', fontSize: 12},
  empty: {textAlign: 'center', paddingVertical: 48, color: '#9DA4A3', fontSize: 14},
  placeholder: {color: '#9DA4A3', fontSize: 12, padding: 16, textAlign: 'center'},
  stickyHeader: {backgroundColor: '#FFFFFF', borderBottomWidth: 1, borderBottomColor: '#DEE4E1', zIndex: 10, elevation: 3},

  substituteFab: {
    position: 'absolute',
    left: 0,
    bottom: 80,
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 10,
    paddingVertical: 6,
    borderTopRightRadius: 4,
    borderBottomRightRadius: 4,
    backgroundColor: 'rgba(23,29,28,0.8)',
    gap: 4,
  },
  substituteText: {
    flexDirection: 'column',
    alignItems: 'center',
    gap: 0,
  },
  substituteChar: {
    color: '#FFFFFF',
    fontSize: 12,
    lineHeight: 14,
    fontWeight: '500',
  },
});
