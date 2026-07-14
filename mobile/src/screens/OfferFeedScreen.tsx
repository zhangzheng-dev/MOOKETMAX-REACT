import React, {useCallback, useEffect, useMemo, useState} from 'react';
import {
  ActivityIndicator,
  FlatList,
  Keyboard,
  Modal,
  Pressable,
  RefreshControl,
  ScrollView,
  StatusBar,
  StyleSheet,
  Text,
  TextInput,
  View,
} from 'react-native';
import type {NativeStackScreenProps} from '@react-navigation/native-stack';
import {useSafeAreaInsets} from 'react-native-safe-area-context';
import Svg, {Circle, Path} from 'react-native-svg';
import {mooketApi} from '../api/mooketApi';
import {DEFAULT_CATEGORY} from '../config/env';
import type {RootStackParamList} from '../navigation/routes';
import {colors} from '../theme/colors';
import {fonts} from '../theme/typography';
import type {OfferFeedFilterOptions, OfferFeedItem} from '../types/api';
import {parseWeight} from '../utils/offer';

type Props = NativeStackScreenProps<RootStackParamList, 'OfferFeed'>;
type OfferTab = 'offer' | 'inquiry';
type SortBy = 'comprehensive' | 'price_asc' | 'price_desc';
type FilterKey = 'country' | 'factoryNo' | 'goodsType' | 'region' | 'feedingType' | 'tag';

type FeedFilters = {
  country?: string | null;
  factoryNo?: string | null;
  goodsType?: string | null;
  region?: string | null;
  feedingType?: string | null;
  tag?: string | null;
};

const PAGE_SIZE = 20;
const categoryOptions = ['牛', '猪'];

const filterMeta: Record<FilterKey, {title: string; empty: string; field: keyof FeedFilters}> = {
  country: {title: '国家', empty: '国家', field: 'country'},
  factoryNo: {title: '规格', empty: '规格', field: 'factoryNo'},
  goodsType: {title: '货物类型', empty: '货物类型', field: 'goodsType'},
  region: {title: '地区', empty: '地区', field: 'region'},
  feedingType: {title: '饲养方式', empty: '饲养方式', field: 'feedingType'},
  tag: {title: '标签', empty: '标签', field: 'tag'},
};

export function OfferFeedScreen({navigation, route}: Props) {
  const insets = useSafeAreaInsets();
  const [tab, setTab] = useState<OfferTab>(route.params?.initialTab ?? 'offer');
  const [category, setCategory] = useState(route.params?.category ?? DEFAULT_CATEGORY);
  const [keywordInput, setKeywordInput] = useState('');
  const [keyword, setKeyword] = useState('');
  const [filters, setFilters] = useState<FeedFilters>({});
  const [sortBy, setSortBy] = useState<SortBy>('comprehensive');
  const [quotedOnly, setQuotedOnly] = useState(false);
  const [realNameOnly, setRealNameOnly] = useState(false);
  const [verifiedOnly, setVerifiedOnly] = useState(false);
  const [items, setItems] = useState<OfferFeedItem[]>([]);
  const [filterOptions, setFilterOptions] = useState<OfferFeedFilterOptions>({});
  const [totalCount, setTotalCount] = useState(0);
  const [page, setPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const [loading, setLoading] = useState(false);
  const [refreshing, setRefreshing] = useState(false);
  const [loadingMore, setLoadingMore] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [activeFilter, setActiveFilter] = useState<FilterKey | null>(null);

  const loadPage = useCallback(
    async (nextPage: number, mode: 'replace' | 'refresh' | 'more' = 'replace') => {
      if (mode === 'more') {
        setLoadingMore(true);
      } else if (mode === 'refresh') {
        setRefreshing(true);
      } else {
        setLoading(true);
      }

      try {
        const result = await mooketApi.getOfferFeed({
          category,
          type: tab,
          keyword,
          country: filters.country,
          factoryNo: filters.factoryNo,
          goodsType: filters.goodsType,
          region: filters.region,
          feedingType: filters.feedingType,
          tag: filters.tag,
          quotedOnly,
          realNameOnly,
          verifiedOnly,
          sortBy,
          page: nextPage,
          pageSize: PAGE_SIZE,
        });
        setItems(prev => (mode === 'more' ? prev.concat(result.items ?? []) : result.items ?? []));
        setFilterOptions(result.filterOptions ?? {});
        setTotalCount(result.totalCount ?? 0);
        setPage(result.page ?? nextPage);
        setTotalPages(result.totalPages ?? 1);
        setError(null);
      } catch (err) {
        setError(err instanceof Error ? err.message : '加载失败，请稍后重试');
        if (mode !== 'more') {
          setItems([]);
          setTotalCount(0);
        }
      } finally {
        setLoading(false);
        setRefreshing(false);
        setLoadingMore(false);
      }
    },
    [category, filters, keyword, quotedOnly, realNameOnly, sortBy, tab, verifiedOnly],
  );

  useEffect(() => {
    loadPage(1).catch(() => undefined);
  }, [loadPage]);

  const optionList = useMemo(() => {
    if (!activeFilter) return [];
    if (activeFilter === 'country') return filterOptions.countries ?? [];
    if (activeFilter === 'factoryNo') return filterOptions.factoryNos ?? [];
    if (activeFilter === 'goodsType') return filterOptions.goodsTypes ?? [];
    if (activeFilter === 'region') return filterOptions.regions ?? [];
    if (activeFilter === 'feedingType') return filterOptions.feedingTypes ?? [];
    return filterOptions.tags ?? [];
  }, [activeFilter, filterOptions]);

  const applyKeyword = useCallback(() => {
    Keyboard.dismiss();
    setKeyword(keywordInput.trim());
  }, [keywordInput]);

  const clearAllFilters = useCallback(() => {
    setFilters({});
    setQuotedOnly(false);
    setRealNameOnly(false);
    setVerifiedOnly(false);
    setSortBy('comprehensive');
  }, []);

  const toggleCategory = useCallback(() => {
    const currentIndex = categoryOptions.indexOf(category);
    const next = categoryOptions[(currentIndex + 1) % categoryOptions.length] ?? categoryOptions[0];
    setCategory(next);
  }, [category]);

  const togglePriceSort = useCallback(() => {
    setSortBy(prev => (prev === 'price_asc' ? 'price_desc' : prev === 'price_desc' ? 'comprehensive' : 'price_asc'));
  }, []);

  const hasActiveFilters =
    Object.values(filters).some(Boolean) || quotedOnly || realNameOnly || verifiedOnly || sortBy !== 'comprehensive';

  return (
    <View style={styles.container}>
      <StatusBar barStyle="dark-content" backgroundColor="#FFFFFF" translucent={false} />
      <View style={[styles.safeTop, {height: insets.top}]} />

      <View style={styles.header}>
        <Pressable onPress={() => navigation.goBack()} hitSlop={10} style={styles.backButton}>
          <BackIcon />
        </Pressable>
        <View style={styles.headerTabs}>
          <HeaderTab text="报盘" active={tab === 'offer'} onPress={() => setTab('offer')} />
          <HeaderTab text="求购" active={tab === 'inquiry'} onPress={() => setTab('inquiry')} />
        </View>
        <Pressable onPress={clearAllFilters} hitSlop={10} style={styles.clearButton}>
          <FilterIcon color={hasActiveFilters ? colors.primary : '#9DA4A3'} />
        </Pressable>
      </View>

      <View style={styles.searchArea}>
        <View style={styles.searchBox}>
          <SearchIcon />
          <TextInput
            value={keywordInput}
            onChangeText={setKeywordInput}
            onSubmitEditing={applyKeyword}
            returnKeyType="search"
            placeholder={`支持搜索产品/厂号，例如1440牛霖`}
            placeholderTextColor="rgba(108,122,119,0.55)"
            style={styles.searchInput}
          />
          {keywordInput ? (
            <Pressable onPress={() => { setKeywordInput(''); setKeyword(''); }} hitSlop={8}>
              <Text style={styles.clearSearch}>×</Text>
            </Pressable>
          ) : null}
        </View>
      </View>

      <View style={styles.filterBlock}>
        <ScrollView horizontal showsHorizontalScrollIndicator={false} contentContainerStyle={styles.filterRow}>
          <FilterChip text={category} active onPress={toggleCategory} rightSlot={<ChevronDownIcon />} />
          <FilterChip
            text={filters.goodsType ?? '货物类型'}
            active={Boolean(filters.goodsType)}
            onPress={() => setActiveFilter('goodsType')}
            rightSlot={<ChevronDownIcon />}
          />
          <FilterChip
            text={filters.factoryNo ?? '规格'}
            active={Boolean(filters.factoryNo)}
            onPress={() => setActiveFilter('factoryNo')}
            rightSlot={<ChevronDownIcon />}
          />
          <FilterChip
            text={filters.region ?? '地区'}
            active={Boolean(filters.region)}
            onPress={() => setActiveFilter('region')}
            rightSlot={<ChevronDownIcon />}
          />
          <FilterChip
            text={filters.country ?? '国家'}
            active={Boolean(filters.country)}
            onPress={() => setActiveFilter('country')}
            rightSlot={<ChevronDownIcon />}
          />
          <FilterChip text="筛选" active={Boolean(filters.feedingType || filters.tag)} onPress={() => setActiveFilter('feedingType')} rightSlot={<FilterIcon size={12} />} />
        </ScrollView>

        <ScrollView horizontal showsHorizontalScrollIndicator={false} contentContainerStyle={styles.quickRow}>
          <QuickChip text="综合排序" active={sortBy === 'comprehensive'} onPress={() => setSortBy('comprehensive')} rightSlot={<ChevronDownIcon />} />
          <QuickChip text="明盘" active={quotedOnly} onPress={() => setQuotedOnly(prev => !prev)} />
          <QuickChip text="牧集实名" active={realNameOnly} onPress={() => setRealNameOnly(prev => !prev)} />
          <QuickChip text="商家认证" active={verifiedOnly} onPress={() => setVerifiedOnly(prev => !prev)} />
          <QuickChip
            text={sortBy === 'price_asc' ? '价格升序' : sortBy === 'price_desc' ? '价格降序' : '价格'}
            active={sortBy === 'price_asc' || sortBy === 'price_desc'}
            onPress={togglePriceSort}
          />
        </ScrollView>
      </View>

      <View style={styles.countRow}>
        <Text style={styles.countText}>
          {tab === 'offer' ? '全量报盘' : '全量求购'}
          <Text style={styles.countNumber}> {totalCount}</Text>
        </Text>
        {keyword ? <Text style={styles.keywordText} numberOfLines={1}>搜索：{keyword}</Text> : null}
      </View>

      <FlatList
        data={items}
        keyExtractor={(item, index) => `${item.offerId ?? 'offer'}-${index}`}
        renderItem={({item}) => <OfferFeedCard item={item} tab={tab} onMerchantPress={() => {
          if (item.merchantId != null) {
            navigation.navigate('Merchant', {merchantId: item.merchantId, category});
          }
        }} />}
        contentContainerStyle={styles.listContent}
        refreshControl={<RefreshControl refreshing={refreshing} onRefresh={() => loadPage(1, 'refresh')} />}
        onEndReachedThreshold={0.25}
        onEndReached={() => {
          if (!loading && !loadingMore && page < totalPages) {
            loadPage(page + 1, 'more').catch(() => undefined);
          }
        }}
        ListEmptyComponent={
          loading ? (
            <ActivityIndicator color={colors.primary} style={styles.loading} />
          ) : error ? (
            <Pressable onPress={() => loadPage(1)} style={styles.errorState}>
              <Text style={styles.errorTitle}>加载失败</Text>
              <Text style={styles.errorMessage}>{error}</Text>
              <Text style={styles.errorRetry}>点击重试</Text>
            </Pressable>
          ) : (
            <Text style={styles.empty}>暂无匹配数据</Text>
          )
        }
        ListFooterComponent={loadingMore ? <ActivityIndicator color={colors.primary} style={styles.footerLoading} /> : null}
        showsVerticalScrollIndicator={false}
      />

      <FilterSheet
        visible={activeFilter != null}
        title={activeFilter ? filterMeta[activeFilter].title : ''}
        options={optionList}
        selected={activeFilter ? filters[filterMeta[activeFilter].field] ?? null : null}
        onClose={() => setActiveFilter(null)}
        onSelect={value => {
          if (!activeFilter) return;
          const field = filterMeta[activeFilter].field;
          setFilters(prev => ({...prev, [field]: value}));
          setActiveFilter(null);
        }}
      />
    </View>
  );
}

function HeaderTab({text, active, onPress}: {text: string; active: boolean; onPress: () => void}) {
  return (
    <Pressable onPress={onPress} style={styles.headerTab}>
      <Text style={[styles.headerTabText, active && styles.headerTabTextActive]}>{text}</Text>
      <View style={[styles.headerTabLine, active && styles.headerTabLineActive]} />
    </Pressable>
  );
}

function FilterChip({
  text,
  active,
  rightSlot,
  onPress,
}: {
  text: string;
  active?: boolean;
  rightSlot?: React.ReactNode;
  onPress: () => void;
}) {
  return (
    <Pressable onPress={onPress} style={[styles.filterChip, active && styles.filterChipActive]}>
      <Text style={[styles.filterChipText, active && styles.filterChipTextActive]} numberOfLines={1}>
        {text}
      </Text>
      {rightSlot ? <View style={styles.chipIcon}>{rightSlot}</View> : null}
    </Pressable>
  );
}

function QuickChip({
  text,
  active,
  rightSlot,
  onPress,
}: {
  text: string;
  active?: boolean;
  rightSlot?: React.ReactNode;
  onPress: () => void;
}) {
  return (
    <Pressable onPress={onPress} style={[styles.quickChip, active && styles.quickChipActive]}>
      <Text style={[styles.quickChipText, active && styles.quickChipTextActive]}>{text}</Text>
      {rightSlot ? <View style={styles.chipIcon}>{rightSlot}</View> : null}
    </Pressable>
  );
}

function OfferFeedCard({item, tab, onMerchantPress}: {item: OfferFeedItem; tab: OfferTab; onMerchantPress: () => void}) {
  const title = buildTitle(item);
  const price = formatPrice(item.price, item.priceMax);
  const weight = formatWeight(item.weight);
  const time = formatCardTime(item.publishTime);
  const merchantName = item.merchantShortName || item.merchantName || item.userNickname || '未知商家';
  const mainTags = [
    item.goodsType,
    item.goodsLocation,
    item.feedingType,
    item.fatRatio,
    item.cattleBreed,
    ...splitTags(item.tags).slice(0, 2),
  ].filter(Boolean) as string[];

  return (
    <View style={styles.card}>
      <View style={styles.cardHeader}>
        <Text style={styles.cardTitle} numberOfLines={2}>{title}</Text>
        <Text style={styles.cardTime}>{time}</Text>
      </View>

      <View style={styles.metaRow}>
        {item.goodsType ? <Text style={styles.metaText}>{item.goodsType}</Text> : null}
        {item.region ? <Text style={styles.metaText}>{item.region}</Text> : null}
        {tab === 'offer' ? <Text style={styles.metaText}>可开票</Text> : null}
      </View>

      <View style={styles.tradeRow}>
        <View style={styles.priceLine}>
          <Text style={[styles.priceValue, !price.amount && styles.negotiateText]}>{price.amount || '协商报价'}</Text>
          {price.unit ? <Text style={styles.priceUnit}>{price.unit}</Text> : null}
        </View>
        {weight ? (
          <View style={styles.weightLine}>
            <Text style={styles.weightValue}>{weight.value}</Text>
            <Text style={styles.weightUnit}>{weight.unit}</Text>
          </View>
        ) : null}
      </View>

      {item.remark ? (
        <View style={styles.remarkStrip}>
          <Text style={styles.remarkText} numberOfLines={2}>{item.remark}</Text>
        </View>
      ) : null}

      {mainTags.length > 0 ? (
        <View style={styles.tagRow}>
          {mainTags.slice(0, 5).map(tag => <SmallTag key={tag} text={tag} />)}
        </View>
      ) : null}

      <Pressable onPress={onMerchantPress} style={styles.merchantRow}>
        <View style={styles.merchantTags}>
          {hasRealName(item.merchantTags) ? <Text style={styles.certTag}>牧集实名</Text> : null}
          {hasVerified(item.merchantTags) ? <Text style={styles.certTag}>商家认证</Text> : null}
        </View>
        <Text style={styles.merchantName} numberOfLines={1}>{merchantName}</Text>
        <ChevronRightIcon />
      </Pressable>
    </View>
  );
}

function SmallTag({text}: {text: string}) {
  return (
    <View style={styles.smallTag}>
      <Text style={styles.smallTagText} numberOfLines={1}>{text}</Text>
    </View>
  );
}

function FilterSheet({
  visible,
  title,
  options,
  selected,
  onClose,
  onSelect,
}: {
  visible: boolean;
  title: string;
  options: string[];
  selected: string | null;
  onClose: () => void;
  onSelect: (value: string | null) => void;
}) {
  return (
    <Modal visible={visible} transparent animationType="fade" onRequestClose={onClose}>
      <Pressable style={styles.modalBackdrop} onPress={onClose}>
        <Pressable style={styles.sheet} onPress={() => undefined}>
          <View style={styles.sheetHeader}>
            <Text style={styles.sheetTitle}>{title}</Text>
            <Pressable onPress={onClose} hitSlop={8}>
              <Text style={styles.sheetClose}>×</Text>
            </Pressable>
          </View>
          <ScrollView contentContainerStyle={styles.sheetOptions}>
            <Pressable onPress={() => onSelect(null)} style={[styles.sheetOption, selected == null && styles.sheetOptionActive]}>
              <Text style={[styles.sheetOptionText, selected == null && styles.sheetOptionTextActive]}>全部</Text>
            </Pressable>
            {options.map(option => (
              <Pressable
                key={option}
                onPress={() => onSelect(option)}
                style={[styles.sheetOption, selected === option && styles.sheetOptionActive]}>
                <Text style={[styles.sheetOptionText, selected === option && styles.sheetOptionTextActive]}>{option}</Text>
              </Pressable>
            ))}
          </ScrollView>
        </Pressable>
      </Pressable>
    </Modal>
  );
}

function buildTitle(item: OfferFeedItem) {
  const product = item.productName ?? '未知产品';
  const countryFactory = `${item.country ?? ''}${item.factoryNo ?? ''}`.trim();
  return countryFactory ? `${product} ${countryFactory}` : product;
}

function formatPrice(price?: number | null, priceMax?: number | null): {amount: string | null; unit: string | null} {
  const min = typeof price === 'number' && Number.isFinite(price) && price > 0 ? price : null;
  const max = typeof priceMax === 'number' && Number.isFinite(priceMax) && priceMax > 0 ? priceMax : null;
  if (min != null && max != null && min !== max) {
    return {amount: `¥${formatNumber(min)}-${formatNumber(max)}`, unit: '/kg'};
  }
  if (min != null) {
    return {amount: `¥${formatNumber(min)}`, unit: '/kg'};
  }
  return {amount: null, unit: null};
}

function formatNumber(value: number) {
  const rounded = Math.round(value * 10) / 10;
  return Number.isInteger(rounded) ? `${rounded}` : rounded.toFixed(1);
}

function formatWeight(weight?: string | null): {value: string; unit: string} | null {
  const [value, unit] = parseWeight(weight);
  if (!value) return null;
  return {value, unit: unit || '吨'};
}

function formatCardTime(time?: string | null) {
  if (!time) return '';
  const match = time.match(/(\d{4})-(\d{2})-(\d{2})[T ](\d{2}):(\d{2})/);
  if (!match) return time;
  return `${match[4]}:${match[5]}`;
}

function splitTags(tags?: string | null) {
  if (!tags) return [];
  return tags.split(/[,，、\s]+/).map(item => item.trim()).filter(Boolean);
}

function hasRealName(tags?: string | null) {
  return Boolean(tags?.includes('实名'));
}

function hasVerified(tags?: string | null) {
  return Boolean(tags?.includes('认证') || tags?.includes('实名'));
}

function BackIcon() {
  return (
    <Svg width={24} height={24} viewBox="0 0 24 24" fill="none">
      <Path d="M15 18L9 12L15 6" stroke="#171D1C" strokeWidth={2} strokeLinecap="round" strokeLinejoin="round" />
    </Svg>
  );
}

function SearchIcon() {
  return (
    <Svg width={18} height={18} viewBox="0 0 24 24" fill="none">
      <Circle cx={11} cy={11} r={7} stroke="#9DA4A3" strokeWidth={1.8} />
      <Path d="M20 20L16.2 16.2" stroke="#9DA4A3" strokeWidth={1.8} strokeLinecap="round" />
    </Svg>
  );
}

function ChevronDownIcon() {
  return (
    <Svg width={12} height={12} viewBox="0 0 12 12" fill="none">
      <Path d="M3 4.5L6 7.5L9 4.5" stroke="#6C7A77" strokeWidth={1.4} strokeLinecap="round" strokeLinejoin="round" />
    </Svg>
  );
}

function ChevronRightIcon() {
  return (
    <Svg width={12} height={12} viewBox="0 0 12 12" fill="none">
      <Path d="M4.5 3L7.5 6L4.5 9" stroke="#6C7A77" strokeWidth={1.4} strokeLinecap="round" strokeLinejoin="round" />
    </Svg>
  );
}

function FilterIcon({size = 16, color = '#6C7A77'}: {size?: number; color?: string}) {
  return (
    <Svg width={size} height={size} viewBox="0 0 16 16" fill="none">
      <Path d="M2 4H14" stroke={color} strokeWidth={1.4} strokeLinecap="round" />
      <Path d="M4.5 8H11.5" stroke={color} strokeWidth={1.4} strokeLinecap="round" />
      <Path d="M6.5 12H9.5" stroke={color} strokeWidth={1.4} strokeLinecap="round" />
    </Svg>
  );
}

const styles = StyleSheet.create({
  container: {flex: 1, backgroundColor: '#F4F7F6'},
  safeTop: {backgroundColor: '#FFFFFF'},
  header: {
    height: 56,
    backgroundColor: '#FFFFFF',
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingHorizontal: 12,
  },
  backButton: {width: 36, height: 36, alignItems: 'center', justifyContent: 'center'},
  clearButton: {width: 36, height: 36, alignItems: 'center', justifyContent: 'center'},
  headerTabs: {flexDirection: 'row', alignItems: 'center', gap: 34},
  headerTab: {alignItems: 'center', justifyContent: 'center', paddingTop: 6},
  headerTabText: {fontSize: 22, lineHeight: 28, color: colors.text, fontWeight: '400'},
  headerTabTextActive: {color: '#00A99A', fontWeight: '700'},
  headerTabLine: {marginTop: 2, width: 28, height: 3, borderRadius: 3, backgroundColor: 'transparent'},
  headerTabLineActive: {backgroundColor: '#00A99A'},
  searchArea: {backgroundColor: '#FFFFFF', paddingHorizontal: 12, paddingBottom: 10},
  searchBox: {
    height: 42,
    borderRadius: 22,
    borderWidth: 1.5,
    borderColor: '#00A99A',
    backgroundColor: '#FFFFFF',
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 14,
    gap: 8,
  },
  searchInput: {flex: 1, color: colors.text, fontSize: 14, paddingVertical: 0},
  clearSearch: {fontSize: 22, color: '#9DA4A3', lineHeight: 22},
  filterBlock: {backgroundColor: '#FFFFFF', borderBottomWidth: 1, borderBottomColor: colors.border},
  filterRow: {paddingHorizontal: 12, paddingBottom: 8, gap: 8, alignItems: 'center'},
  quickRow: {paddingHorizontal: 12, paddingBottom: 12, gap: 8, alignItems: 'center'},
  filterChip: {
    height: 32,
    paddingHorizontal: 10,
    borderRadius: 4,
    backgroundColor: '#F4F6F5',
    flexDirection: 'row',
    alignItems: 'center',
    gap: 4,
    maxWidth: 130,
  },
  filterChipActive: {backgroundColor: '#E8F5F3'},
  filterChipText: {fontSize: 13, color: colors.textSecondary, lineHeight: 18},
  filterChipTextActive: {color: colors.primary, fontWeight: '600'},
  quickChip: {
    height: 32,
    paddingHorizontal: 12,
    borderRadius: 4,
    backgroundColor: '#F4F6F5',
    flexDirection: 'row',
    alignItems: 'center',
    gap: 4,
  },
  quickChipActive: {backgroundColor: '#E8F5F3'},
  quickChipText: {fontSize: 13, color: colors.textSecondary},
  quickChipTextActive: {color: colors.primary, fontWeight: '600'},
  chipIcon: {width: 12, height: 12, alignItems: 'center', justifyContent: 'center'},
  countRow: {
    minHeight: 34,
    paddingHorizontal: 12,
    backgroundColor: '#F4F7F6',
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
  },
  countText: {fontSize: 12, color: colors.textMuted},
  countNumber: {fontFamily: fonts.manropeBold, color: colors.primary},
  keywordText: {fontSize: 12, color: colors.textMuted, maxWidth: 180},
  listContent: {paddingHorizontal: 8, paddingBottom: 28},
  loading: {marginTop: 40},
  footerLoading: {paddingVertical: 16},
  empty: {marginTop: 48, textAlign: 'center', color: '#9DA4A3', fontSize: 14},
  errorState: {marginTop: 48, paddingHorizontal: 24, alignItems: 'center'},
  errorTitle: {color: colors.text, fontSize: 15, fontWeight: '700'},
  errorMessage: {marginTop: 6, color: colors.textMuted, fontSize: 12, lineHeight: 18, textAlign: 'center'},
  errorRetry: {marginTop: 10, color: colors.primary, fontSize: 13, fontWeight: '600'},
  card: {
    marginBottom: 12,
    paddingHorizontal: 16,
    paddingVertical: 14,
    backgroundColor: '#FFFFFF',
    borderRadius: 8,
  },
  cardHeader: {flexDirection: 'row', alignItems: 'flex-start', gap: 12},
  cardTitle: {flex: 1, color: colors.text, fontSize: 18, lineHeight: 25, fontWeight: '700'},
  cardTime: {color: colors.textMuted, fontSize: 13, lineHeight: 20},
  metaRow: {marginTop: 4, flexDirection: 'row', alignItems: 'center', gap: 6},
  metaText: {color: colors.textMuted, fontSize: 13, lineHeight: 18},
  tradeRow: {marginTop: 6, flexDirection: 'row', alignItems: 'baseline', gap: 18},
  priceLine: {flexDirection: 'row', alignItems: 'baseline'},
  priceValue: {fontFamily: fonts.manropeBold, color: colors.price, fontSize: 22, lineHeight: 28},
  priceUnit: {color: colors.price, fontSize: 12, lineHeight: 18, marginLeft: 2},
  negotiateText: {fontFamily: undefined, color: '#E56B2F', fontSize: 16, fontWeight: '600'},
  weightLine: {flexDirection: 'row', alignItems: 'baseline'},
  weightValue: {fontFamily: fonts.manropeRegular, color: colors.textSecondary, fontSize: 19, lineHeight: 26},
  weightUnit: {color: colors.textSecondary, fontSize: 12, marginLeft: 3},
  remarkStrip: {marginTop: 8, paddingHorizontal: 10, paddingVertical: 6, backgroundColor: '#EAF8FA', borderRadius: 2},
  remarkText: {color: '#31727B', fontSize: 13, lineHeight: 18},
  tagRow: {marginTop: 9, flexDirection: 'row', flexWrap: 'wrap', gap: 6},
  smallTag: {height: 20, paddingHorizontal: 6, borderRadius: 2, backgroundColor: '#F3F6F5', justifyContent: 'center'},
  smallTagText: {fontSize: 11, color: colors.textSecondary, maxWidth: 110},
  merchantRow: {marginTop: 10, flexDirection: 'row', alignItems: 'center', gap: 6},
  merchantTags: {flexDirection: 'row', gap: 4},
  certTag: {
    borderWidth: 1,
    borderColor: '#D7B978',
    color: '#8B6118',
    fontSize: 11,
    lineHeight: 16,
    paddingHorizontal: 3,
  },
  merchantName: {flex: 1, color: colors.textMuted, fontSize: 13, lineHeight: 18},
  modalBackdrop: {
    flex: 1,
    backgroundColor: 'rgba(0,0,0,0.28)',
    justifyContent: 'flex-end',
  },
  sheet: {
    maxHeight: '68%',
    backgroundColor: '#FFFFFF',
    borderTopLeftRadius: 10,
    borderTopRightRadius: 10,
    paddingBottom: 12,
  },
  sheetHeader: {
    height: 52,
    paddingHorizontal: 18,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    borderBottomWidth: 1,
    borderBottomColor: colors.border,
  },
  sheetTitle: {fontSize: 17, color: colors.text, fontWeight: '700'},
  sheetClose: {fontSize: 24, color: colors.textMuted, lineHeight: 24},
  sheetOptions: {padding: 12, flexDirection: 'row', flexWrap: 'wrap', gap: 8},
  sheetOption: {
    minHeight: 34,
    paddingHorizontal: 12,
    borderRadius: 4,
    backgroundColor: '#F4F6F5',
    alignItems: 'center',
    justifyContent: 'center',
  },
  sheetOptionActive: {backgroundColor: '#E8F5F3'},
  sheetOptionText: {fontSize: 13, color: colors.textSecondary},
  sheetOptionTextActive: {color: colors.primary, fontWeight: '700'},
});
