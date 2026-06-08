import React, {useCallback, useEffect, useMemo, useState} from 'react';
import {
  ActivityIndicator,
  Keyboard,
  PressableStateCallbackType,
  Platform,
  Pressable,
  RefreshControl,
  ScrollView,
  StyleProp,
  StatusBar,
  StyleSheet,
  Text,
  TextInput,
  TextStyle,
  useWindowDimensions,
  View,
} from 'react-native';
import {SafeAreaView, useSafeAreaInsets} from 'react-native-safe-area-context';
import type {NativeStackScreenProps} from '@react-navigation/native-stack';
import Svg, {Path, Rect, SvgXml} from 'react-native-svg';
import {fetchInventoryDataset, InventoryDataset, InventoryItem} from '../api/inventory';
import {getMainTabBarHeight} from '../components/MainTabBar';
import type {RootStackParamList} from '../navigation/routes';
import PivotTable from '../components/PivotTable';
import {
  computePivot,
  formatMoneyWan,
  formatWeight,
  PivotProduct,
  PivotSummary,
  ResolvedInventoryRow,
  resolveInventoryRows,
} from '../utils/pivot';
import {colors} from '../theme/colors';
import {InventoryTheme, Mode, themes} from '../theme/inventoryTheme';

type Props = Partial<NativeStackScreenProps<RootStackParamList, 'Inventory'>> & {
  mode?: Mode;
  onModeChange?: (mode: Mode) => void;
};

type InventoryTopTab = 'pivot' | 'dynamic' | 'detail';
type DynamicGroupBy = 'product' | 'funder' | 'country' | 'status' | 'factory' | 'customGroup';
type DetailSortKey =
  | 'productionDate'
  | 'receivable'
  | 'profit'
  | 'dailyCost'
  | 'recoverableCash'
  | 'totalCost'
  | 'totalWeight';
type SortDirection = 'asc' | 'desc' | null;
type SummaryMetricCell = {
  label: string;
  value: {
    main: string;
    unit?: string;
  };
  color?: string;
  tone?: 'positive' | 'negative' | null;
};

const INVENTORY_TOP_TABS: Array<{key: InventoryTopTab; label: string}> = [
  {key: 'pivot', label: '标准透视'},
  {key: 'dynamic', label: '动态库存'},
  {key: 'detail', label: '库存明细'},
];

const DETAIL_SORT_OPTIONS: Array<{key: DetailSortKey; label: string; minWidth: number}> = [
  {key: 'productionDate', label: '生产日期', minWidth: 76},
  {key: 'receivable', label: '应收账款', minWidth: 76},
  {key: 'profit', label: '盈利', minWidth: 58},
  {key: 'dailyCost', label: '每日成本', minWidth: 76},
  {key: 'recoverableCash', label: '可回现金', minWidth: 76},
  {key: 'totalCost', label: '总成本', minWidth: 68},
  {key: 'totalWeight', label: '总重量', minWidth: 68},
];
const DETAIL_ROW_COLUMN_GAP = 32;
const DETAIL_HEADER_COLUMN_GAP = 8;
const DETAIL_FROZEN_MIN_WIDTH = 174;
const DETAIL_PRODUCTION_MIN_WIDTH = 112;

const ANDROID_TOP_CONTENT_SPACING = Platform.OS === 'android' ? 12 : 0;
const FIGMA_ACTIVE_GREEN = '#006A61';

function getDesktopAlignedValuationDate() {
  const next = new Date();
  next.setDate(next.getDate() + 1);
  next.setHours(0, 0, 0, 0);
  return next;
}


const RECEIPT_SEARCH_SVG = `<svg preserveAspectRatio="none" width="100%" height="100%" overflow="visible" style="display: block;" viewBox="0 0 14 14" fill="none" xmlns="http://www.w3.org/2000/svg"><g id="vuesax/linear/receipt-search"><path d="M11.9583 6.59166V4.10667C11.9583 1.75584 11.41 1.16667 9.205 1.16667H4.795C2.59 1.16667 2.04167 1.75584 2.04167 4.10667V10.675C2.04167 12.2267 2.89334 12.5942 3.92584 11.4858L3.93166 11.48C4.40999 10.9725 5.13916 11.0133 5.55333 11.5675L6.1425 12.355" stroke="__COLOR__" stroke-width="0.875" stroke-linecap="round" stroke-linejoin="round"/><g><path d="M10.6167 12.4833C11.6476 12.4833 12.4833 11.6476 12.4833 10.6167C12.4833 9.58574 11.6476 8.75 10.6167 8.75C9.58574 8.75 8.75 9.58574 8.75 10.6167C8.75 11.6476 9.58574 12.4833 10.6167 12.4833Z" stroke="__COLOR__" stroke-width="0.875" stroke-linecap="round" stroke-linejoin="round"/><path d="M12.8333 12.8333L12.25 12.25" stroke="__COLOR__" stroke-width="0.875" stroke-linecap="round" stroke-linejoin="round"/></g><path d="M4.66667 4.08333H9.33333" stroke="__COLOR__" stroke-width="0.875" stroke-linecap="round" stroke-linejoin="round"/><path d="M5.25 6.41667H8.75" stroke="__COLOR__" stroke-width="0.875" stroke-linecap="round" stroke-linejoin="round"/></g></svg>`;
const SETTING4_SVG = `<svg preserveAspectRatio="none" width="100%" height="100%" overflow="visible" style="display: block;" viewBox="0 0 14 14" fill="none" xmlns="http://www.w3.org/2000/svg"><g id="setting-4"><path d="M12.8333 3.79167H9.33333" stroke="__COLOR__" stroke-width="0.875" stroke-miterlimit="10" stroke-linecap="round" stroke-linejoin="round"/><path d="M3.5 3.79167H1.16667" stroke="__COLOR__" stroke-width="0.875" stroke-miterlimit="10" stroke-linecap="round" stroke-linejoin="round"/><path d="M5.83333 5.83333C6.96092 5.83333 7.875 4.91925 7.875 3.79167C7.875 2.66409 6.96092 1.75 5.83333 1.75C4.70575 1.75 3.79167 2.66409 3.79167 3.79167C3.79167 4.91925 4.70575 5.83333 5.83333 5.83333Z" stroke="__COLOR__" stroke-width="0.875" stroke-miterlimit="10" stroke-linecap="round" stroke-linejoin="round"/><path d="M12.8333 10.2083H10.5" stroke="__COLOR__" stroke-width="0.875" stroke-miterlimit="10" stroke-linecap="round" stroke-linejoin="round"/><path d="M4.66667 10.2083H1.16667" stroke="__COLOR__" stroke-width="0.875" stroke-miterlimit="10" stroke-linecap="round" stroke-linejoin="round"/><path d="M8.16667 12.25C9.29425 12.25 10.2083 11.3359 10.2083 10.2083C10.2083 9.08075 9.29425 8.16667 8.16667 8.16667C7.03909 8.16667 6.125 9.08075 6.125 10.2083C6.125 11.3359 7.03909 12.25 8.16667 12.25Z" stroke="__COLOR__" stroke-width="0.875" stroke-miterlimit="10" stroke-linecap="round" stroke-linejoin="round"/></g></svg>`;
const DOCUMENT_TEXT_SVG = `<svg preserveAspectRatio="none" width="100%" height="100%" overflow="visible" style="display: block;" viewBox="0 0 14 14" fill="none" xmlns="http://www.w3.org/2000/svg"><g id="document-text"><path d="M12.25 4.08333V9.91667C12.25 11.6667 11.375 12.8333 9.33333 12.8333H4.66667C2.625 12.8333 1.75 11.6667 1.75 9.91667V4.08333C1.75 2.33333 2.625 1.16667 4.66667 1.16667H9.33333C11.375 1.16667 12.25 2.33333 12.25 4.08333Z" stroke="__COLOR__" stroke-width="0.875" stroke-miterlimit="10" stroke-linecap="round" stroke-linejoin="round"/><path d="M8.45833 2.625V3.79167C8.45833 4.43333 8.98333 4.95833 9.625 4.95833H10.7917" stroke="__COLOR__" stroke-width="0.875" stroke-miterlimit="10" stroke-linecap="round" stroke-linejoin="round"/><path d="M4.66667 7.58333H7" stroke="__COLOR__" stroke-width="0.875" stroke-miterlimit="10" stroke-linecap="round" stroke-linejoin="round"/><path d="M4.66667 9.91667H9.33333" stroke="__COLOR__" stroke-width="0.875" stroke-miterlimit="10" stroke-linecap="round" stroke-linejoin="round"/></g></svg>`;

function figmaIconXml(template: string, color: string) {
  return template.replace(/__COLOR__/g, color);
}

function InventoryPageHeader({
  palette,
  refreshing,
  onBack,
  onRefresh,
}: {
  palette: Palette;
  refreshing: boolean;
  onBack: () => void;
  onRefresh: () => void;
}) {
  return (
    <View
      style={[
        styles.inventoryHeader,
        styles.inventoryHeaderSurface,
        {borderBottomColor: palette.border},
      ]}>
      <Pressable
        accessibilityRole="button"
        accessibilityLabel="返回"
        hitSlop={10}
        onPress={onBack}
        style={styles.inventoryHeaderButton}>
        <Svg width={24} height={24} viewBox="0 0 24 24" fill="none">
          <Path
            d="M15 18l-6-6 6-6"
            stroke={palette.text}
            strokeWidth={2}
            strokeLinecap="round"
            strokeLinejoin="round"
          />
        </Svg>
      </Pressable>
      <Text style={[styles.inventoryHeaderTitle, {color: palette.text}]}>库存</Text>
      <Pressable
        accessibilityRole="button"
        accessibilityLabel="刷新库存"
        hitSlop={10}
        disabled={refreshing}
        onPress={onRefresh}
        style={styles.inventoryHeaderButton}>
        {refreshing ? (
          <ActivityIndicator size="small" color={palette.accent} />
        ) : (
          <Svg width={22} height={22} viewBox="0 0 24 24" fill="none">
            <Path
              d="M20 12a8 8 0 0 1-13.66 5.66M4 12A8 8 0 0 1 17.66 6.34M17 3v4h-4M7 21v-4h4"
              stroke={palette.text}
              strokeWidth={1.9}
              strokeLinecap="round"
              strokeLinejoin="round"
            />
          </Svg>
        )}
      </Pressable>
    </View>
  );
}

export default function InventoryScreen({
  mode = 'light',
  onModeChange: _onModeChange = () => {},
  navigation,
}: Props) {
  const [items, setItems] = useState<InventoryItem[]>([]);
  const [dataset, setDataset] = useState<InventoryDataset | null>(null);
  const [products, setProducts] = useState<PivotProduct[]>([]);
  const [summary, setSummary] = useState<PivotSummary | null>(null);
  const [globalSummary, setGlobalSummary] = useState<PivotSummary | null>(null);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [pivotSearchQuery, setPivotSearchQuery] = useState('');
  const [detailSearchQuery, setDetailSearchQuery] = useState('');
  const [showKg, setShowKg] = useState(false);
  const [activeTopTab, setActiveTopTab] = useState<InventoryTopTab>('pivot');
  const [dynamicGroupBys, setDynamicGroupBys] = useState<DynamicGroupBy[]>(['product']);
  const [detailSortState, setDetailSortState] = useState<{
    key: DetailSortKey;
    direction: SortDirection;
  }>({key: 'productionDate', direction: 'desc'});
  const tableHeaderScrollRef = React.useRef<ScrollView>(null);
  const detailTableScrollRef = React.useRef<ScrollView>(null);
  const detailHeaderScrollRef = React.useRef<ScrollView>(null);
  const {width} = useWindowDimensions();
  const insets = useSafeAreaInsets();
  const theme = themes[mode];
  const palette = useMemo(() => getPalette(theme), [theme]);

  const contentWidth = width - 24;
  const pageBottomPadding = getMainTabBarHeight(insets.bottom) + 36;
  const loadData = useCallback(async (silent = false) => {
    if (!silent) setLoading(true);
    setError(null);
    try {
      const data = await fetchInventoryDataset();
      setDataset(data);
      setItems(data.items);
    } catch (e: any) {
      setError(e?.message ?? '加载失败');
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  }, []);

  useEffect(() => {
    loadData();
  }, [loadData]);

  useEffect(() => {
    const valuationDate = getDesktopAlignedValuationDate();
    const pivotContext = dataset ? {...dataset, valuationDate} : undefined;

    const {products: nextProducts, summary: filteredSummary} = computePivot(
      items,
      null,
      null,
      pivotSearchQuery,
      pivotContext,
    );
    const {summary: allSummary} = computePivot(items, null, null, undefined, pivotContext);
    setProducts(nextProducts);
    setSummary(filteredSummary);
    setGlobalSummary(allSummary);
  }, [items, dataset, pivotSearchQuery]);

  const detailRows = useMemo(() => {
    const valuationDate = getDesktopAlignedValuationDate();
    const pivotContext = dataset ? {...dataset, valuationDate} : undefined;
    return resolveInventoryRows(items, pivotContext);
  }, [items, dataset]);

  const filteredDetailRows = useMemo(() => {
    const query = detailSearchQuery.trim().toLowerCase();
    if (!query) return detailRows;
    return detailRows.filter(row =>
      [
        row.containerId,
        row.contractId,
        row.factoryCode,
        row.productName,
        row.skuCode,
      ]
        .filter(Boolean)
        .some(value => value.toLowerCase().includes(query)),
    );
  }, [detailRows, detailSearchQuery]);

  const sortedDetailRows = useMemo(() => {
    const direction = detailSortState.direction;
    if (direction == null) return filteredDetailRows;
    return [...filteredDetailRows].sort((left, right) =>
      compareDetailRows(left, right, detailSortState.key, direction),
    );
  }, [filteredDetailRows, detailSortState]);

  const dynamicFilteredRows = useMemo(() => detailRows, [detailRows]);

  const dynamicSummary = useMemo(() => {
    const totalWeight = dynamicFilteredRows.reduce((sum, row) => sum + row.weightKg, 0);
    const totalCost = dynamicFilteredRows.reduce((sum, row) => sum + row.costPerKg * row.weightKg, 0);
    const totalOccupiedCash = dynamicFilteredRows.reduce(
      (sum, row) => sum + row.occupiedCash,
      0,
    );
    const totalProfit = dynamicFilteredRows.reduce((sum, row) => sum + row.profit, 0);
    const groupCount = new Set(
      dynamicFilteredRows.map(row => dynamicGroupingKey(row, dynamicGroupBys)),
    ).size;
    const containerCount = new Set(dynamicFilteredRows.map(row => row.containerId)).size;

    return {
      groupCount,
      containerCount,
      totalWeight,
      averageCost: totalWeight > 0 ? totalCost / totalWeight : 0,
      totalProfit,
      totalOccupiedCash,
    };
  }, [dynamicFilteredRows, dynamicGroupBys]);

  const dynamicCards = useMemo(() => {
    const grouped = new Map<string, typeof dynamicFilteredRows>();
    dynamicFilteredRows.forEach(row => {
      const key = dynamicGroupingKey(row, dynamicGroupBys);
      const list = grouped.get(key) ?? [];
      list.push(row);
      grouped.set(key, list);
    });

    const cards = Array.from(grouped.entries()).map(([key, rows]) => {
        const firstRow = rows[0];
        const containerCount = new Set(rows.map(row => row.containerId)).size;
        const totalWeight = rows.reduce((sum, row) => sum + row.weightKg, 0);
        const totalCost = rows.reduce((sum, row) => sum + row.costPerKg * row.weightKg, 0);
        const occupiedCash = rows.reduce((sum, row) => sum + row.occupiedCash, 0);
        const profit = rows.reduce((sum, row) => sum + row.profit, 0);
        const averageCost = totalCost / Math.max(totalWeight, 1);
        const roi = occupiedCash > 0 ? (profit / occupiedCash) * 100 : 0;
        const containerProfitMap = new Map<string, number>();
        rows.forEach(row => {
          containerProfitMap.set(
            row.containerId,
            (containerProfitMap.get(row.containerId) ?? 0) + row.profit,
          );
        });
        const lossContainers = Array.from(containerProfitMap.values()).filter(value => value < 0).length;
        const profitContainers = Array.from(containerProfitMap.values()).filter(value => value >= 0).length;
        const riskCount = Array.from(containerProfitMap.values()).filter(value => value < 0).length;
        const ages = rows
          .map(row => productionAgeDays(row.productionDate))
          .filter((value): value is number => value != null);
        const avgAge = ages.length
          ? Math.round(ages.reduce((sum, value) => sum + value, 0) / ages.length)
          : null;
        const costs = rows.map(row => row.costPerKg).filter(cost => cost > 0);
        const minCost = costs.length ? Math.min(...costs) : 0;
        const maxCost = costs.length ? Math.max(...costs) : 0;
        const avgCost = averageCost;
        const ratio = dynamicSummary.totalOccupiedCash > 0 ? occupiedCash / dynamicSummary.totalOccupiedCash : 0;
        const trendPoints = costBandPoints(costs);
        const highestCostRow = rows.reduce<typeof rows[number] | null>(
          (current, row) =>
            !current || row.costPerKg > current.costPerKg ? row : current,
          null,
        );
        const spread = maxCost - minCost;
        const costCenter =
          spread > 0 ? (averageCost - minCost) / Math.max(spread, Number.EPSILON) : 0.5;
        const insight =
          spread < 0.8
            ? '持仓重心居中，成本分布均衡'
            : costCenter >= 0.66
              ? `持仓重心偏高，关注高价柜：${highestCostRow?.containerId ?? '--'}`
              : costCenter <= 0.34
                ? '持仓重心偏低，成本优势明显'
                : '持仓重心居中，成本分布均衡';

        return {
          key,
          titleLines: dynamicGroupBys.map(dimension => ({
            label: dynamicDimensionLabel(dimension),
            value: dynamicDimensionValue(firstRow, dimension),
          })),
          alertLabel:
            riskCount > 0 ? '盈亏预警' : avgAge != null && avgAge >= 300 ? '周转预警' : '周转平稳',
          containerCount,
          fundRatio: ratio,
          lossContainers,
          profitContainers,
          averageCost,
          occupiedCash,
          profit,
          avgAge,
          roi,
          insight,
          minCost,
          avgCost,
          maxCost,
          trendPoints,
        };
      });

    return cards.sort((a, b) => b.occupiedCash - a.occupiedCash);
  }, [dynamicFilteredRows, dynamicGroupBys, dynamicSummary.totalOccupiedCash]);

  const detailTableLayout = useMemo(
    () => buildDetailTableLayout(sortedDetailRows),
    [sortedDetailRows],
  );

  const onRefresh = useCallback(() => {
    setRefreshing(true);
    loadData(true);
  }, [loadData]);

  const onBack = useCallback(() => {
    if (navigation?.canGoBack?.()) {
      navigation.goBack();
      return;
    }
    navigation?.navigate?.('Home');
  }, [navigation]);

  if (loading) {
    return (
      <SafeAreaView
        style={[
          styles.centerContainer,
          {
            backgroundColor: palette.bg,
            paddingTop: ANDROID_TOP_CONTENT_SPACING,
          },
        ]}
        edges={['top']}>
        <StatusBar
          barStyle={mode === 'dark' ? 'light-content' : 'dark-content'}
          backgroundColor={palette.bg}
        />
        <InventoryPageHeader
          palette={palette}
          refreshing={refreshing}
          onBack={onBack}
          onRefresh={onRefresh}
        />
        <View style={styles.centerState}>
          <ActivityIndicator size="large" color={palette.accent} />
          <Text style={[styles.loadingText, {color: palette.muted}]}>正在同步库存数据...</Text>
        </View>
      </SafeAreaView>
    );
  }

  if (error) {
    return (
      <SafeAreaView
        style={[
          styles.centerContainer,
          {
            backgroundColor: palette.bg,
            paddingTop: ANDROID_TOP_CONTENT_SPACING,
          },
        ]}
        edges={['top']}>
        <StatusBar
          barStyle={mode === 'dark' ? 'light-content' : 'dark-content'}
          backgroundColor={palette.bg}
        />
        <InventoryPageHeader
          palette={palette}
          refreshing={refreshing}
          onBack={onBack}
          onRefresh={onRefresh}
        />
        <View style={styles.centerState}>
          <Text style={[styles.errorText, {color: palette.negative}]}>{error}</Text>
          <Pressable
            style={[styles.retryBtn, {borderColor: palette.border}]}
            onPress={() => loadData()}>
            <Text style={[styles.retryText, {color: palette.text}]}>重试</Text>
          </Pressable>
        </View>
      </SafeAreaView>
    );
  }

  if (!summary || !globalSummary) {
    return null;
  }

  return (
    <SafeAreaView
      style={[
        styles.container,
        {
          backgroundColor: palette.bg,
          paddingTop: ANDROID_TOP_CONTENT_SPACING,
        },
      ]}
      edges={['top']}>
      <StatusBar
        barStyle={mode === 'dark' ? 'light-content' : 'dark-content'}
        backgroundColor={palette.bg}
      />
      <InventoryPageHeader
        palette={palette}
        refreshing={refreshing}
        onBack={onBack}
        onRefresh={onRefresh}
      />
      <ScrollView
        style={styles.pageScroll}
        contentContainerStyle={[styles.pageContent, {paddingBottom: pageBottomPadding}]}
        keyboardShouldPersistTaps="handled"
        keyboardDismissMode="on-drag"
        nestedScrollEnabled={Platform.OS === 'android'}
        onScrollBeginDrag={Keyboard.dismiss}
        refreshControl={
          <RefreshControl
            refreshing={refreshing}
            onRefresh={onRefresh}
            tintColor={palette.accent}
          />
        }>
        <TopOverviewHeader width={contentWidth} palette={palette} summary={globalSummary} />

        <TopModuleTabs
          width={contentWidth}
          palette={palette}
          activeTab={activeTopTab}
          onChange={setActiveTopTab}
        />

        {activeTopTab === 'pivot' ? (
          <>
        <ModuleSectionTop
        width={contentWidth}
        palette={palette}
        title="标准透视表"
        subtitle="Standard Pivot"
        searchQuery={pivotSearchQuery}
        onSearchQueryChange={setPivotSearchQuery}
        showKg={showKg}
              onToggleShowKg={setShowKg}
              summary={summary}
            />

            <View
              style={[
                styles.tableSticky,
                {width: contentWidth, backgroundColor: palette.panel, borderColor: palette.border},
              ]}>
              <View style={styles.tableHeaderCard}>
                <PivotTable.Header
                  products={products}
                  showKg={showKg}
                  palette={palette}
                  scrollRef={tableHeaderScrollRef}
                />
              </View>
            </View>

            <View
              style={[
                styles.tableRowsCard,
                {width: contentWidth, backgroundColor: palette.panel, borderColor: palette.border},
              ]}>
              <PivotTable.Rows
                products={products}
                showKg={showKg}
                palette={palette}
                initialScrollKey={`${showKg}-${products.length}`}
                onHorizontalScroll={x =>
                  tableHeaderScrollRef.current?.scrollTo({x, animated: false})
                }
              />
            </View>
          </>
        ) : null}

        {activeTopTab === 'dynamic' ? (
          <>
          <DynamicInventoryStickySection
            width={contentWidth}
            palette={palette}
            summary={dynamicSummary}
            groupBys={dynamicGroupBys}
            onGroupByToggle={value =>
              setDynamicGroupBys(current => {
                if (current.includes(value)) {
                  return current.length === 1 ? current : current.filter(item => item !== value);
                }
                return [...current, value];
              })
            }
          />
          <DynamicInventoryCardsGrid width={contentWidth} palette={palette} cards={dynamicCards} />
          </>
        ) : null}

        {activeTopTab === 'detail' ? (
          <>
            <InventoryDetailSearchSection
              width={contentWidth}
              palette={palette}
              searchQuery={detailSearchQuery}
              onSearchQueryChange={setDetailSearchQuery}
            />
            <InventoryDetailStickyHeader
              width={contentWidth}
              palette={palette}
              layout={detailTableLayout}
              sortState={detailSortState}
              onSortChange={nextState => {
                setDetailSortState(nextState);
                const x = detailColumnScrollOffset(nextState.key, detailTableLayout.scrollColumns);
                detailHeaderScrollRef.current?.scrollTo({x, animated: true});
                detailTableScrollRef.current?.scrollTo({x, animated: true});
              }}
              scrollRef={detailHeaderScrollRef}
            />
            <InventoryDetailRows
              width={contentWidth}
              layout={detailTableLayout}
              rows={sortedDetailRows}
              scrollRef={detailTableScrollRef}
              headerScrollRef={detailHeaderScrollRef}
            />
          </>
        ) : null}
      </ScrollView>
    </SafeAreaView>
  );
}

function TopModuleTabs({
  width,
  palette,
  activeTab,
  onChange,
}: {
  width: number;
  palette: Palette;
  activeTab: InventoryTopTab;
  onChange: (tab: InventoryTopTab) => void;
}) {
  return (
    <View style={[styles.topTabsRow, {width}]}>
      {INVENTORY_TOP_TABS.map(tab => (
        <View key={tab.key} style={styles.topTabItem}>
          <Pressable
            onPress={() => onChange(tab.key)}
            style={({pressed}: PressableStateCallbackType) => [
              styles.topTabChip,
              {
                backgroundColor: activeTab === tab.key ? FIGMA_ACTIVE_GREEN : palette.panel,
                borderColor: activeTab === tab.key ? FIGMA_ACTIVE_GREEN : palette.border,
                opacity: pressed ? 0.9 : 1,
              },
              activeTab === tab.key ? styles.topTabChipActive : null,
            ]}>
            <View style={styles.topTabInner}>
              <TopTabIcon tab={tab.key} color={activeTab === tab.key ? palette.panel : palette.label} />
              <Text
                style={[
                  styles.topTabText,
                  activeTab === tab.key ? styles.topTabTextActive : styles.topTabTextInactive,
                  {
                    color: activeTab === tab.key ? palette.panel : palette.label,
                  },
                ]}
                numberOfLines={1}>
                {tab.label}
              </Text>
            </View>
          </Pressable>
          <View
            style={[
              styles.topTabIndicator,
              activeTab === tab.key ? styles.topTabIndicatorActive : styles.topTabIndicatorInactive,
            ]}
          />
        </View>
      ))}
    </View>
  );
}

function TopTabIcon({
  tab,
  color,
}: {
  tab: InventoryTopTab;
  color: string;
}) {
  const xml =
    tab === 'pivot'
      ? figmaIconXml(RECEIPT_SEARCH_SVG, color)
      : tab === 'dynamic'
        ? figmaIconXml(SETTING4_SVG, color)
        : figmaIconXml(DOCUMENT_TEXT_SVG, color);
  return <SvgXml xml={xml} width={14} height={14} />;
}

function SelectedFilterBadge() {
  return (
    <View style={styles.selectedFilterBadge}>
      <Svg width={12} height={10} viewBox="0 0 12 10" fill="none">
        <Path d="M0 0H10C11.1046 0 12 0.895431 12 2V10H4C1.79086 10 0 8.20914 0 6V0Z" fill="#006A61" />
        <Path
          d="M3 5L4.99765 7L9 3"
          stroke="#FFFFFF"
          strokeLinecap="round"
          strokeLinejoin="round"
        />
      </Svg>
    </View>
  );
}

function ModuleSectionTop({
  width,
  palette,
  title,
  subtitle,
  searchQuery,
  onSearchQueryChange,
  showKg,
  onToggleShowKg,
  summary,
}: {
  width: number;
  palette: Palette;
  title: string;
  subtitle: string;
  searchQuery: string;
  onSearchQueryChange: (value: string) => void;
  showKg: boolean;
  onToggleShowKg: (value: boolean) => void;
  summary: PivotSummary;
}) {
  return (
    <View
      style={[
        styles.sectionCard,
        styles.sectionCardTop,
        {width, backgroundColor: palette.panel, borderColor: palette.border},
      ]}>
      <View style={styles.sectionHead}>
        <SectionTitleLockup palette={palette} title={title} subtitle={subtitle} />
        <UnitSwitch
          palette={palette}
          showKg={showKg}
          onToggleShowKg={onToggleShowKg}
        />
      </View>

      <ModuleSearchBar
        palette={palette}
        searchQuery={searchQuery}
        onSearchQueryChange={onSearchQueryChange}
      />

      <View style={styles.pivotSummaryWrap}>
        <SummaryBoard width={width - 20} palette={palette} summary={summary} showKg={showKg} />
      </View>
    </View>
  );
}

function SectionTitleLockup({
  palette,
  title,
  subtitle,
}: {
  palette: Palette;
  title: string;
  subtitle: string;
}) {
  return (
    <View style={styles.sectionTitleLockup}>
      <Text
        style={[styles.sectionTitlePrimary, {color: FIGMA_ACTIVE_GREEN}]}
        numberOfLines={1}>
        {title}
      </Text>
      <Text
        style={[styles.sectionTitleSecondary, {color: palette.text}]}
        numberOfLines={1}>
        {subtitle}
      </Text>
    </View>
  );
}

function UnitSwitch({
  palette,
  showKg,
  onToggleShowKg,
}: {
  palette: Palette;
  showKg: boolean;
  onToggleShowKg: (value: boolean) => void;
}) {
  return (
    <View style={styles.unitWrap}>
      <Text style={[styles.unitLabel, {color: palette.label}]}>单位</Text>
      <View
        style={[
          styles.unitSwitch,
          {backgroundColor: palette.searchBg, borderColor: palette.border},
        ]}>
        <Pressable
          style={[
            styles.unitSegment,
            showKg && styles.unitSegmentActive,
            showKg && {backgroundColor: palette.tabActiveBg},
          ]}
          onPress={() => onToggleShowKg(true)}>
          <Text
            style={[
              styles.unitSegmentText,
              showKg ? styles.unitSegmentTextActive : styles.unitSegmentTextInactive,
              {color: showKg ? palette.panel : palette.muted},
            ]}>
            KG
          </Text>
        </Pressable>
        <Pressable
          style={[
            styles.unitSegment,
            !showKg && styles.unitSegmentActive,
            !showKg && {backgroundColor: palette.tabActiveBg},
          ]}
          onPress={() => onToggleShowKg(false)}>
          <Text
            style={[
              styles.unitSegmentText,
              !showKg ? styles.unitSegmentTextActive : styles.unitSegmentTextInactive,
              {color: !showKg ? palette.panel : palette.muted},
            ]}>
            T
          </Text>
        </Pressable>
      </View>
    </View>
  );
}

function ModuleSearchBar({
  palette,
  searchQuery,
  onSearchQueryChange,
  backgroundColor,
}: {
  palette: Palette;
  searchQuery: string;
  onSearchQueryChange: (value: string) => void;
  backgroundColor?: string;
}) {
  return (
    <View
      style={[
        styles.searchBar,
        {backgroundColor: backgroundColor ?? palette.searchBg, borderColor: palette.border},
      ]}>
      <View style={styles.searchInputWrap}>
        <SearchFieldIcon color={palette.label} />
        <TextInput
          style={[styles.searchInput, {color: palette.text}]}
          value={searchQuery}
          onChangeText={onSearchQueryChange}
          placeholder="搜索厂号/品名"
          placeholderTextColor={palette.muted}
          returnKeyType="search"
          onSubmitEditing={() => Keyboard.dismiss()}
        />
      </View>
      <Pressable style={styles.searchAction} onPress={() => Keyboard.dismiss()}>
        <Text style={[styles.searchActionText, {color: palette.label}]}>搜索</Text>
      </Pressable>
    </View>
  );
}

function InventoryDetailSearchBar({
  palette,
  searchQuery,
  onSearchQueryChange,
}: {
  palette: Palette;
  searchQuery: string;
  onSearchQueryChange: (value: string) => void;
}) {
  return (
    <View
      style={[
        styles.searchBar,
        styles.detailSearchBar,
        styles.searchBarWhite,
        {borderColor: palette.border},
      ]}>
      <View style={styles.searchInputWrap}>
        <SearchFieldIcon color={palette.label} />
        <TextInput
          style={[styles.searchInput, {color: palette.text}]}
          value={searchQuery}
          onChangeText={onSearchQueryChange}
          placeholder="搜索柜号/厂号/品名"
          placeholderTextColor={palette.muted}
          returnKeyType="search"
          onSubmitEditing={() => Keyboard.dismiss()}
        />
      </View>
      {searchQuery ? (
        <Pressable style={styles.searchAction} onPress={() => onSearchQueryChange('')}>
          <Text style={[styles.searchActionText, {color: palette.label}]}>清除</Text>
        </Pressable>
      ) : (
        <Pressable style={styles.searchAction} onPress={() => Keyboard.dismiss()}>
          <Text style={[styles.searchActionText, {color: palette.label}]}>搜索</Text>
        </Pressable>
      )}
    </View>
  );
}

function DynamicInventoryStickySection({
  width,
  palette,
  summary,
  groupBys,
  onGroupByToggle,
}: {
  width: number;
  palette: Palette;
  summary: {
    groupCount: number;
    containerCount: number;
    totalWeight: number;
    averageCost: number;
    totalProfit: number;
    totalOccupiedCash: number;
  };
  groupBys: DynamicGroupBy[];
  onGroupByToggle: (value: DynamicGroupBy) => void;
}) {
  return (
    <View
      style={[
        styles.dynamicSummaryCard,
        styles.dynamicStickyWrap,
        {width, backgroundColor: palette.panel, borderColor: palette.border},
      ]}>
      <ScrollView
        horizontal
        showsHorizontalScrollIndicator={false}
        contentContainerStyle={[styles.filterChipRow, styles.dynamicFilterRow]}>
        {DYNAMIC_FILTER_DIMENSIONS.map(item => (
          (() => {
            const selected = groupBys.includes(item.key);
            return (
          <Pressable
            key={item.key}
            onPress={() => onGroupByToggle(item.key)}
            style={[
              styles.filterChip,
              selected ? styles.filterChipSelected : styles.filterChipUnselected,
              {width: item.width},
            ]}>
            <Text
              style={[
                styles.filterChipText,
                selected ? styles.filterChipTextSelected : styles.filterChipTextDefault,
                {
                  color: selected ? FIGMA_ACTIVE_GREEN : palette.label,
                },
              ]}
              numberOfLines={1}>
              {item.label}
            </Text>
            {selected ? <SelectedFilterBadge /> : null}
          </Pressable>
            );
          })()
        ))}
      </ScrollView>

      <DynamicSummaryBoard palette={palette} summary={summary} />
    </View>
  );
}

function DynamicInventoryCardsGrid({
  width,
  palette,
  cards,
}: {
  width: number;
  palette: Palette;
  cards: Array<{
    key: string;
    titleLines: Array<{label: string; value: string}>;
    alertLabel: string;
    containerCount: number;
    fundRatio: number;
    lossContainers: number;
    profitContainers: number;
    averageCost: number;
    occupiedCash: number;
    profit: number;
    avgAge: number | null;
    roi: number;
    insight: string;
    minCost: number;
    avgCost: number;
    maxCost: number;
    trendPoints: number[];
  }>;
}) {
  const cardWidth = (width - 6) / 2;
  return (
    <View style={[styles.dynamicCardsGrid, {width}]}>
      {cards.map(card => {
        const lossColor = card.lossContainers === 0 ? '#9DA4A3' : palette.negative;
        const profitColor = card.profitContainers === 0 ? '#9DA4A3' : palette.positive;

        return (
          <View
            key={card.key}
            style={[
              styles.dynamicProductCard,
              {width: cardWidth},
              {backgroundColor: palette.panel, borderColor: palette.border},
            ]}>
          <View style={styles.dynamicProductHead}>
            <View style={styles.dynamicProductTitleWrap}>
              {card.titleLines.map(line => (
                <Text key={`${card.key}-${line.label}`} style={styles.dynamicProductTitle}>
                  {line.label}:{line.value}
                </Text>
              ))}
            </View>
          </View>
          <View style={styles.dynamicHeadMetaRow}>
            <View
              style={[
                styles.dynamicAlertBadge,
                dynamicAlertBadgeStyle(card.alertLabel, palette),
              ]}>
              <Text
                style={[
                  styles.dynamicAlertBadgeText,
                  dynamicAlertTextStyle(card.alertLabel, palette),
                ]}>
                {card.alertLabel}
              </Text>
            </View>
            <View style={styles.dynamicCountWrap}>
              <Text style={[styles.dynamicCardCountValue, {color: palette.text}]}>
                {card.containerCount}
              </Text>
              <Text style={[styles.dynamicCardCountUnit, {color: palette.text}]}>柜</Text>
            </View>
          </View>
          <View style={styles.dynamicFundRow}>
            <Text style={[styles.dynamicFundLabel, {color: palette.label}]}>资金占用</Text>
            <Text style={[styles.dynamicFundValue, {color: palette.warning}]}>
              {(card.fundRatio * 100).toFixed(1)}%
            </Text>
          </View>
          <View style={[styles.dynamicProfitLossBand, {backgroundColor: palette.searchBg}]}>
            <View style={styles.dynamicProfitLossCell}>
              <Text
                style={[
                  styles.dynamicProfitLossValue,
                  {color: lossColor},
                ]}>
                {card.lossContainers}
              </Text>
              <Text style={[styles.dynamicProfitLossLabel, {color: palette.label}]}>亏损柜</Text>
            </View>
            <View style={[styles.dynamicProfitLossDividerLine, {backgroundColor: palette.border}]} />
            <View style={styles.dynamicProfitLossCell}>
              <Text
                style={[
                  styles.dynamicProfitLossValue,
                  {color: profitColor},
                ]}>
                {card.profitContainers}
              </Text>
              <Text style={[styles.dynamicProfitLossLabel, {color: palette.label}]}>盈利柜</Text>
            </View>
          </View>
          <View style={styles.dynamicMetricList}>
            <DynamicMetricRow label="持仓成本" value={`¥${safeFixed(card.averageCost, 1)}`} palette={palette} />
            <DynamicMetricRow label="资金投入" value={`¥${formatCardMoney(card.occupiedCash)}`} palette={palette} />
            <DynamicMetricRow
              label="总盈利"
              value={`${card.profit >= 0 ? '+' : '-'}¥${formatPreciseMoney(card.profit)}`}
              palette={palette}
              tone={moneyTone(card.profit) ?? undefined}
            />
            <DynamicMetricRow
              label="平均库龄"
              value={card.avgAge != null ? `${card.avgAge}天` : '--'}
              palette={palette}
              tone="warning"
            />
            <DynamicMetricRow
              label="ROI"
              value={`${card.roi >= 0 ? '+' : '-'}${safeFixed(Math.abs(card.roi), 1)}%`}
              palette={palette}
              tone={moneyTone(card.roi) ?? undefined}
            />
          </View>
          </View>
        );
      })}
    </View>
  );
}

function DynamicSummaryBoard({
  palette,
  summary,
}: {
  palette: Palette;
  summary: {
    groupCount: number;
    containerCount: number;
    totalWeight: number;
    averageCost: number;
    totalProfit: number;
    totalOccupiedCash: number;
  };
}) {
  const columns: SummaryMetricCell[][] = [
    [
      {label: '分组数', value: {main: summary.groupCount.toLocaleString()}},
      {
        label: '平均成本',
        value: {main: `¥${safeFixed(summary.averageCost, 1)}`},
      },
    ],
    [
      {label: '柜号数', value: {main: summary.containerCount.toLocaleString()}},
      {
        label: '总盈利',
        value: splitMoney(formatMoneyWan(Math.abs(summary.totalProfit))),
        color: moneyColor(summary.totalProfit, palette),
        tone: moneyTone(summary.totalProfit),
      },
    ],
    [
      {label: '总重量', value: splitWeight(formatWeight(summary.totalWeight, false))},
      {
        label: '总资金占用',
        value: splitMoney(formatMoneyWan(summary.totalOccupiedCash)),
        color: palette.text,
      },
    ],
  ];

  return (
    <View style={styles.summaryBoard}>
      {columns.map((column, columnIndex) => (
        <View
          key={`dynamic-summary-column-${columnIndex}`}
          style={[
            styles.summaryColumn,
            columnIndex < columns.length - 1 ? styles.summaryColumnDivider : null,
            columnIndex < columns.length - 1 ? {borderRightColor: palette.divider} : null,
          ]}>
          {column.map((row, rowIndex) => (
            <React.Fragment key={row.label}>
              <View style={styles.summaryMetric}>
                <View style={styles.summaryMetricContent}>
                  <Text style={[styles.summaryLabel, {color: palette.label}]}>{row.label}</Text>
                  <View style={styles.summaryValueRow}>
                    {row.tone ? <SignedValueIcon tone={row.tone} /> : null}
                    <InlineValue
                      main={row.value.main}
                      unit={row.value.unit}
                      valueStyle={[styles.summaryValue, {color: row.color ?? palette.text}]}
                      unitStyle={[styles.summaryUnit, {color: palette.text}]}
                    />
                  </View>
                </View>
              </View>
              {rowIndex === 0 ? (
                <View style={[styles.summaryDividerHorizontal, {backgroundColor: palette.divider}]} />
              ) : null}
            </React.Fragment>
          ))}
        </View>
      ))}
    </View>
  );
}

function InventoryDetailSearchSection({
  width,
  palette,
  searchQuery,
  onSearchQueryChange,
}: {
  width: number;
  palette: Palette;
  searchQuery: string;
  onSearchQueryChange: (value: string) => void;
}) {
  return (
    <View style={[styles.detailSearchWrap, {width}]}>
      <InventoryDetailSearchBar
        palette={palette}
        searchQuery={searchQuery}
        onSearchQueryChange={onSearchQueryChange}
      />
    </View>
  );
}

function estimateTextWidth(text: string, fontSize: number) {
  let units = 0;
  for (const char of text) {
    if (/[\u3400-\u9FFF\uF900-\uFAFF]/.test(char)) {
      units += 1;
    } else if (/[A-Z]/.test(char)) {
      units += 0.72;
    } else if (/[a-z]/.test(char)) {
      units += 0.6;
    } else if (/[0-9]/.test(char)) {
      units += 0.62;
    } else if (char === ' ') {
      units += 0.35;
    } else {
      units += 0.46;
    }
  }
  return Math.ceil(units * fontSize);
}

function buildDetailTableLayout(rows: ResolvedInventoryRow[]) {
  const frozenWidth = Math.max(
    DETAIL_FROZEN_MIN_WIDTH,
    estimateTextWidth(DETAIL_TABLE_COLUMNS[0].label, 10) + 24,
    ...rows.flatMap(row => [
      estimateTextWidth(row.containerId || '--', 13) + 24,
      estimateTextWidth(row.contractId || row.skuCode || row.id || '--', 12) + 24,
      estimateTextWidth(`${row.country || '--'}/${row.factoryCode || '--'}`, 13) + 24,
      estimateTextWidth(row.productName || '--', 12) + 24,
    ]),
  );

  const columns = DETAIL_TABLE_COLUMNS.map(column => {
    if (column.key === 'container') {
      return {...column, width: frozenWidth};
    }

    const headerWidth = estimateTextWidth(column.label, 10) + 4;
    const contentWidth = rows.reduce((max, row) => {
      let content = '--';
      switch (column.key) {
        case 'funder':
          return Math.max(
            max,
            estimateTextWidth(row.funder || '--', 13) + 2,
            estimateTextWidth(row.status || '--', 10) + 2,
          );
        case 'weight':
          content = formatPreciseValue(row.weightKg);
          break;
        case 'cost':
          content = `avg：${safeFixed(row.costPerKg, 2)}`;
          break;
        case 'selling':
          content = `avg：${row.sellingPricePerKg ? safeFixed(row.sellingPricePerKg, 2) : '--'}`;
          break;
        case 'totalCost':
          content = `¥${formatPreciseValue(row.costPerKg * row.weightKg)}`;
          break;
        case 'receivable':
          content = `¥${formatPreciseValue((row.sellingPricePerKg || 0) * row.weightKg)}`;
          break;
        case 'dailyCost':
          content = `¥${safeFixed(row.dailyCost, 2)}/天`;
          break;
        case 'recoverableCash':
          return Math.max(
            max,
            estimateTextWidth('¥0', 13) +
              estimateTextWidth('定金', 8) +
              estimateTextWidth(`¥${formatPreciseValue(Math.abs(row.recoverableCash))}`, 13) +
              16,
          );
        case 'profit':
          content = `${row.profit >= 0 ? '¥' : '-¥'}${formatPreciseValue(Math.abs(row.profit))}`;
          break;
        case 'productionDate':
          content = row.productionDate || '--';
          break;
        default:
          break;
      }

      const widthPadding =
        column.key === 'weight' || column.key === 'cost' || column.key === 'selling' ? 2 : 4;
      return Math.max(max, estimateTextWidth(content, 13) + widthPadding);
    }, 0);

    return {
      ...column,
      width: Math.max(column.minWidth, headerWidth, contentWidth),
    };
  });

  const scrollColumns = columns.slice(1);
  const scrollableWidth =
    24 +
    scrollColumns.reduce((sum, column) => sum + column.width, 0) +
    DETAIL_ROW_COLUMN_GAP * (scrollColumns.length - 1);

  return {
    frozenWidth,
    columns,
    scrollColumns,
    scrollableWidth,
  };
}

function InventoryDetailStickyHeader({
  width,
  palette,
  layout,
  sortState,
  onSortChange,
  scrollRef,
}: {
  width: number;
  palette: Palette;
  layout: ReturnType<typeof buildDetailTableLayout>;
  sortState: {key: DetailSortKey; direction: SortDirection};
  onSortChange: (value: {key: DetailSortKey; direction: SortDirection}) => void;
  scrollRef: React.RefObject<ScrollView | null>;
}) {
  const scrollColumns = layout.scrollColumns;
  return (
    <View style={[styles.detailStickyWrap, {width, backgroundColor: palette.bg}]}>
      <ScrollView
        horizontal
        showsHorizontalScrollIndicator={false}
        contentContainerStyle={[styles.filterChipRow, styles.detailFilterRow]}>
        {DETAIL_SORT_OPTIONS.map(option => {
          const direction = option.key === sortState.key ? sortState.direction : null;
          const selected = direction != null;
          return (
            <Pressable
              key={option.key}
              onPress={() =>
                onSortChange(nextDetailSortState(sortState, option.key))
              }
              style={[
                styles.detailSortChip,
                selected ? styles.detailSortChipSelected : styles.detailSortChipDefault,
                {
                  minWidth: option.minWidth,
                  borderColor: selected ? FIGMA_ACTIVE_GREEN : palette.border,
                },
              ]}>
              <Text
                style={[
                  styles.detailSortChipText,
                  {color: selected ? FIGMA_ACTIVE_GREEN : palette.label},
                ]}>
                {option.label}
              </Text>
              {selected ? (
                <View style={styles.detailSortStateBadge}>
                  <Text style={styles.detailSortStateBadgeText}>
                    {direction === 'asc' ? '升序' : '降序'}
                  </Text>
                </View>
              ) : null}
              <DetailSortArrowIcon direction={direction} selected={selected} />
            </Pressable>
          );
        })}
      </ScrollView>

      <View style={[styles.detailHeaderShell, {width}]}>
        <View style={[styles.detailTableSplit, styles.detailHeaderTableSplit]}>
          <View style={[styles.detailFrozenColumn, {width: layout.frozenWidth}]}>
            <View style={[styles.detailTableHeader, styles.detailFrozenHeader]}>
              <View style={[styles.detailHeaderCell, {width: layout.frozenWidth}]}>
                <Text style={styles.detailHeaderText}>{DETAIL_TABLE_COLUMNS[0].label}</Text>
              </View>
            </View>
          </View>

          <ScrollView
            ref={scrollRef}
            horizontal
            showsHorizontalScrollIndicator={false}
            scrollEnabled={false}
            contentContainerStyle={styles.detailTableContent}>
            <View style={[styles.detailScrollableTable, {width: layout.scrollableWidth}]}>
              <View style={styles.detailTableHeader}>
                {scrollColumns.map(column => (
                  <View
                    key={column.key}
                    style={[
                      styles.detailHeaderCell,
                      {
                        width:
                          column === scrollColumns[scrollColumns.length - 1]
                            ? column.width
                            : column.width + (DETAIL_ROW_COLUMN_GAP - DETAIL_HEADER_COLUMN_GAP),
                      },
                    ]}>
                    <Text style={styles.detailHeaderText}>{column.label}</Text>
                  </View>
                ))}
              </View>
            </View>
          </ScrollView>
        </View>
      </View>
    </View>
  );
}

function InventoryDetailRows({
  width,
  layout,
  rows,
  scrollRef,
  headerScrollRef,
}: {
  width: number;
  layout: ReturnType<typeof buildDetailTableLayout>;
  rows: ResolvedInventoryRow[];
  scrollRef: React.RefObject<ScrollView | null>;
  headerScrollRef: React.RefObject<ScrollView | null>;
}) {
  return (
    <View style={[styles.detailTableShell, {width}]}>
      <View style={[styles.detailTableSplit, styles.detailBodyTableSplit]}>
        <View style={[styles.detailFrozenColumn, {width: layout.frozenWidth}]}>
          {rows.map((row, index) => (
            <View
              key={`frozen-${row.id}`}
              style={[
                styles.detailTableRow,
                styles.detailFrozenRow,
                index % 2 === 0 ? styles.detailTableRowEven : styles.detailTableRowOdd,
              ]}>
              <View style={[styles.detailBodyCell, {width: layout.frozenWidth}]}>
                <Text
                  style={styles.detailPrimaryStrong}
                  numberOfLines={1}>
                  {row.containerId || '--'}
                </Text>
                <Text
                  style={styles.detailPrimaryText}
                  numberOfLines={1}>
                  {row.contractId || row.skuCode || row.id || '--'}
                </Text>
                <View style={styles.detailInlineMetaRow}>
                  <Text style={styles.detailPrimaryStrong} numberOfLines={1}>
                    {row.country}
                  </Text>
                  <View style={styles.detailInlineDivider} />
                  <Text style={styles.detailPrimaryStrong} numberOfLines={1}>
                    {row.factoryCode}
                  </Text>
                </View>
                <Text style={styles.detailSecondaryText} numberOfLines={1}>
                  {row.productName}
                </Text>
              </View>
            </View>
          ))}
        </View>

        <ScrollView
          ref={scrollRef}
          horizontal
          showsHorizontalScrollIndicator={false}
          contentContainerStyle={styles.detailTableContent}
          onScroll={event => {
            const x = event.nativeEvent.contentOffset.x;
            headerScrollRef.current?.scrollTo({x, animated: false});
          }}
          scrollEventThrottle={16}>
          <View style={[styles.detailScrollableTable, {width: layout.scrollableWidth}]}>
            {rows.map((row, index) => (
              <View
                key={`scroll-${row.id}`}
                style={[
                  styles.detailTableRow,
                  index % 2 === 0 ? styles.detailTableRowEven : styles.detailTableRowOdd,
                ]}>
              <View style={[styles.detailBodyCell, {width: layout.columns[1].width}]}>
                <Text style={styles.detailPrimaryStrong}>{row.funder}</Text>
                <Text style={[styles.detailStatusValue, {color: detailStatusColor(row.status)}]}>
                  {row.status}
                </Text>
              </View>

              <View style={[styles.detailBodyCell, styles.detailNumberCell, {width: layout.columns[2].width}]}>
                <Text
                  style={styles.detailMetricNumber}
                  numberOfLines={1}>
                  {formatPreciseValue(row.weightKg)}
                </Text>
              </View>

              <View style={[styles.detailBodyCell, styles.detailNumberCell, {width: layout.columns[3].width}]}>
                <Text style={styles.detailMetricNumber}>avg：{safeFixed(row.costPerKg, 2)}</Text>
              </View>

              <View style={[styles.detailBodyCell, styles.detailNumberCell, {width: layout.columns[4].width}]}>
                <Text style={styles.detailMetricNumber}>
                  avg：{row.sellingPricePerKg ? safeFixed(row.sellingPricePerKg, 2) : '--'}
                </Text>
              </View>

              <View style={[styles.detailBodyCell, styles.detailNumberCell, {width: layout.columns[5].width}]}>
                <Text style={styles.detailMoneyNumber}>¥{formatPreciseValue(row.costPerKg * row.weightKg)}</Text>
              </View>

              <View style={[styles.detailBodyCell, styles.detailNumberCell, {width: layout.columns[6].width}]}>
                <Text style={styles.detailMoneyNumber}>
                  ¥{formatPreciseValue((row.sellingPricePerKg || 0) * row.weightKg)}
                </Text>
              </View>

              <View style={[styles.detailBodyCell, styles.detailNumberCell, {width: layout.columns[7].width}]}>
                <View style={styles.detailMoneyInline}>
                  <Text style={[styles.detailMoneyNumber, styles.detailWarningNumber]}>
                    ¥{safeFixed(row.dailyCost, 2)}
                  </Text>
                  <Text style={styles.detailMoneyUnit}>/天</Text>
                </View>
              </View>

              <View style={[styles.detailBodyCell, styles.detailNumberCell, {width: layout.columns[8].width}]}>
                <View style={styles.detailRecoverableWrap}>
                  <View style={styles.detailMoneyInline}>
                    <Text style={styles.detailMoneyNumber}>¥0</Text>
                    <Text style={styles.detailMoneyUnit}>定金</Text>
                  </View>
                  <View style={styles.detailRecoverableDot} />
                  <Text
                    style={[
                      styles.detailMoneyNumber,
                      row.recoverableCash >= 0 ? styles.detailPositiveNumber : styles.detailNegativeNumber,
                    ]}>
                    ¥{formatPreciseValue(Math.abs(row.recoverableCash))}
                  </Text>
                </View>
              </View>

              <View style={[styles.detailBodyCell, styles.detailNumberCell, {width: layout.columns[9].width}]}>
                <Text
                  style={[
                    styles.detailMoneyNumber,
                    row.profit >= 0 ? styles.detailPositiveNumber : styles.detailNegativeNumber,
                  ]}>
                  {row.profit >= 0 ? '¥' : '-¥'}
                  {formatPreciseValue(Math.abs(row.profit))}
                </Text>
              </View>

              <View style={[styles.detailBodyCell, styles.detailNumberCell, {width: layout.columns[10].width}]}>
                <Text
                  style={styles.detailMetricNumber}
                  numberOfLines={1}>
                  {row.productionDate}
                </Text>
              </View>
            </View>
            ))}
          </View>
        </ScrollView>
      </View>
    </View>
  );
}

const DETAIL_TABLE_COLUMNS: Array<{key: string; label: string; minWidth: number}> = [
  {key: 'container', label: '柜号/合同 (Container)', minWidth: DETAIL_FROZEN_MIN_WIDTH},
  {key: 'funder', label: '资方/状态', minWidth: 56},
  {key: 'weight', label: '总重 (kg)', minWidth: 56},
  {key: 'cost', label: '成本 (¥/kg)', minWidth: 92},
  {key: 'selling', label: '卖价 (¥/kg)', minWidth: 72},
  {key: 'totalCost', label: '总成本', minWidth: 96},
  {key: 'receivable', label: '应收账款', minWidth: 96},
  {key: 'dailyCost', label: '日成本', minWidth: 86},
  {key: 'recoverableCash', label: '可回现金', minWidth: 136},
  {key: 'profit', label: '盈利', minWidth: 96},
  {key: 'productionDate', label: '生产日期', minWidth: DETAIL_PRODUCTION_MIN_WIDTH},
];

function DetailSortArrowIcon({
  direction,
  selected,
}: {
  direction: SortDirection;
  selected: boolean;
}) {
  const activeColor = FIGMA_ACTIVE_GREEN;
  const idleColor = '#9DA4A3';
  const upColor = direction === 'asc' ? activeColor : selected ? activeColor : idleColor;
  const downColor = direction === 'desc' ? activeColor : selected ? activeColor : idleColor;

  return (
    <Svg width={6} height={12} viewBox="0 0 6 12" fill="none" style={styles.detailSortArrowIcon}>
      <Path
        d="M1 4L3 2L5 4"
        stroke={upColor}
        strokeWidth={1}
        strokeLinecap="round"
        strokeLinejoin="round"
      />
      <Path
        d="M5 8L3 10L1 8"
        stroke={downColor}
        strokeWidth={1}
        strokeLinecap="round"
        strokeLinejoin="round"
      />
    </Svg>
  );
}

function nextDetailSortState(
  current: {key: DetailSortKey; direction: SortDirection},
  key: DetailSortKey,
): {key: DetailSortKey; direction: SortDirection} {
  if (current.key !== key) return {key, direction: 'desc'};
  if (current.direction === 'desc') return {key, direction: 'asc'};
  if (current.direction === 'asc') return {key, direction: null};
  return {key, direction: 'desc'};
}

function detailColumnKey(sortKey: DetailSortKey) {
  if (sortKey === 'productionDate') return 'productionDate';
  if (sortKey === 'receivable') return 'receivable';
  if (sortKey === 'profit') return 'profit';
  if (sortKey === 'dailyCost') return 'dailyCost';
  if (sortKey === 'recoverableCash') return 'recoverableCash';
  if (sortKey === 'totalCost') return 'totalCost';
  return 'weight';
}

function detailColumnScrollOffset(
  sortKey: DetailSortKey,
  scrollColumns: Array<{key: string; width: number}>,
) {
  const targetKey = detailColumnKey(sortKey);
  const targetIndex = scrollColumns.findIndex(column => column.key === targetKey);
  if (targetIndex <= 0) return 0;

  let offset = 0;
  for (let index = 0; index < targetIndex; index += 1) {
    offset += scrollColumns[index].width;
    if (index < targetIndex) offset += DETAIL_ROW_COLUMN_GAP;
  }
  return Math.max(0, offset);
}

function compareDetailRows(
  left: ResolvedInventoryRow,
  right: ResolvedInventoryRow,
  sortKey: DetailSortKey,
  direction: Exclude<SortDirection, null>,
) {
  const leftReceivable = (left.sellingPricePerKg || 0) * left.weightKg;
  const rightReceivable = (right.sellingPricePerKg || 0) * right.weightKg;
  const leftTotalCost = left.costPerKg * left.weightKg;
  const rightTotalCost = right.costPerKg * right.weightKg;

  let result = 0;
  switch (sortKey) {
    case 'productionDate':
      result = parseDateValue(right.productionDate) - parseDateValue(left.productionDate);
      break;
    case 'receivable':
      result = rightReceivable - leftReceivable;
      break;
    case 'profit':
      result = right.profit - left.profit;
      break;
    case 'dailyCost':
      result = right.dailyCost - left.dailyCost;
      break;
    case 'recoverableCash':
      result = right.recoverableCash - left.recoverableCash;
      break;
    case 'totalCost':
      result = rightTotalCost - leftTotalCost;
      break;
    case 'totalWeight':
    default:
      result = right.weightKg - left.weightKg;
      break;
  }
  return direction === 'desc' ? result : -result;
}

function parseDateValue(value: string) {
  const time = Date.parse(value);
  return Number.isNaN(time) ? 0 : time;
}

function formatPreciseValue(value: number) {
  return value.toLocaleString('en-US', {
    minimumFractionDigits: 3,
    maximumFractionDigits: 3,
  });
}

function detailStatusColor(status: string) {
  if (status.includes('现货') || status.includes('在库')) return '#006A61';
  if (status.includes('清关')) return '#4347BA';
  return '#3C4947';
}

function DynamicMetricRow({
  label,
  value,
  palette,
  tone,
}: {
  label: string;
  value: string;
  palette: Palette;
  tone?: 'positive' | 'negative' | 'warning';
}) {
  const color =
    tone === 'positive'
      ? palette.positive
      : tone === 'negative'
        ? palette.negative
        : tone === 'warning'
          ? palette.warning
          : palette.text;
  const valueColor = value.includes('¥') ? colors.price : color;
  const dayUnitMatch = label === '平均库龄' ? value.match(/^(.*?)(天)$/) : null;

  return (
    <View style={styles.dynamicMetricRow}>
      <Text style={[styles.dynamicMetricLabel, {color: palette.label}]}>{label}</Text>
      {dayUnitMatch ? (
        <Text style={[styles.dynamicMetricValue, {color: valueColor}]}>
          {dayUnitMatch[1]}
          <Text style={[styles.dynamicMetricUnit, {color: valueColor}]}>天</Text>
        </Text>
      ) : (
        <Text style={[styles.dynamicMetricValue, {color: valueColor}]}>{value}</Text>
      )}
    </View>
  );
}

function dynamicAlertBadgeStyle(
  label: string,
  _palette: Palette,
): {backgroundColor: string; borderColor: string; borderWidth: number} {
  if (label === '盈亏预警') {
    return {
      backgroundColor: '#FEEEEE',
      borderColor: 'rgba(242,70,70,0.4)',
      borderWidth: 0.5,
    };
  }
  if (label === '周转预警') {
    return {
      backgroundColor: '#FEFAF1',
      borderColor: 'rgba(179,130,33,0.4)',
      borderWidth: 0.5,
    };
  }
  return {
    backgroundColor: '#F3F3F3',
    borderColor: 'rgba(60, 73, 71, 0.40)',
    borderWidth: 0.5,
  };
}

function dynamicAlertTextStyle(label: string, palette: Palette): {color: string} {
  if (label === '盈亏预警') return {color: '#F24646'};
  if (label === '周转预警') return {color: '#B38221'};
  return {color: palette.label};
}

function safeFixed(value: number, digits: number) {
  if (!isFinite(value)) return '--';
  return value.toFixed(digits);
}

function productionAgeDays(productionDate: string) {
  if (!productionDate || productionDate === '--') return null;
  const date = new Date(productionDate);
  if (Number.isNaN(date.getTime())) return null;
  return Math.max(0, Math.floor((Date.now() - date.getTime()) / (1000 * 60 * 60 * 24)));
}

function dynamicDimensionValue(
  row: {
    productName: string;
    funder: string;
    country: string;
    status: string;
    factoryCode: string;
    customGroup?: string;
  },
  dimension: DynamicGroupBy,
) {
  if (dimension === 'product') return row.productName || '--';
  if (dimension === 'funder') return row.funder || '--';
  if (dimension === 'country') return row.country || '--';
  if (dimension === 'status') return row.status || '--';
  if (dimension === 'factory') return row.factoryCode || '--';
  return row.customGroup || '--';
}

function dynamicDimensionLabel(dimension: DynamicGroupBy) {
  const match = DYNAMIC_FILTER_DIMENSIONS.find(item => item.key === dimension);
  return match?.label ?? '';
}

function dynamicGroupingKey(
  row: {
    productName: string;
    funder: string;
    country: string;
    status: string;
    factoryCode: string;
    customGroup?: string;
  },
  dimensions: DynamicGroupBy[],
) {
  return dimensions
    .map(dimension => `${dimension}:${dynamicDimensionValue(row, dimension)}`)
    .join('||');
}

const DYNAMIC_FILTER_DIMENSIONS: Array<{key: DynamicGroupBy; label: string; width: number}> = [
  {key: 'product', label: '品名', width: 56},
  {key: 'funder', label: '资方', width: 56},
  {key: 'country', label: '国家', width: 56},
  {key: 'status', label: '物理状态', width: 84},
  {key: 'factory', label: '厂号', width: 56},
  {key: 'customGroup', label: '自定义分组', width: 96},
];

function costBandPoints(costs: number[]) {
  if (!costs.length) return [12, 28, 44, 72, 88];
  const sorted = [...costs].sort((a, b) => a - b);
  if (sorted.length === 1) return [50];
  const min = sorted[0];
  const max = sorted[sorted.length - 1];
  return sorted.slice(0, Math.min(sorted.length, 5)).map(cost => {
    if (max === min) return 50;
    return 6 + ((cost - min) / (max - min)) * 88;
  });
}

function formatCardMoney(value: number) {
  return Math.abs(value).toLocaleString('en-US', {
    minimumFractionDigits: 0,
    maximumFractionDigits: 0,
  });
}

function formatPreciseMoney(value: number) {
  return Math.abs(value).toLocaleString('en-US', {
    minimumFractionDigits: 3,
    maximumFractionDigits: 3,
  });
}

function SearchFieldIcon({color}: {color: string}) {
  return (
    <Svg width={15} height={15} viewBox="0 0 15 15" fill="none">
      <Path
        d="M6.875 12.5C9.9816 12.5 12.5 9.9816 12.5 6.875C12.5 3.7684 9.9816 1.25 6.875 1.25C3.7684 1.25 1.25 3.7684 1.25 6.875C1.25 9.9816 3.7684 12.5 6.875 12.5Z"
        stroke={color}
        strokeWidth={1.25}
        strokeLinecap="round"
        strokeLinejoin="round"
      />
      <Path
        d="M13.125 13.125L11.875 11.875"
        stroke={color}
        strokeWidth={1.25}
        strokeLinecap="round"
        strokeLinejoin="round"
      />
    </Svg>
  );
}

function TopOverviewHeader({
  width,
  palette,
  summary,
}: {
  width: number;
  palette: Palette;
  summary: PivotSummary;
}) {
  return (
    <View
      style={[
        styles.topOverviewCard,
        {
          width,
          borderColor: palette.border,
          backgroundColor: palette.panel,
        },
      ]}>
      <OverviewMetricBlock
        palette={palette}
        title="预计总盈利"
        value={summary.totalFloatingPnL}
        unit="万"
        subLabel="浮盈亏总额"
        footerLabel={`${summary.watchedProducts}/${summary.totalItems} 单已盯市`}
        footerIcon={<StatusTinyIcon color={palette.label} />}
        flexRatio={3}
      />
      <View style={[styles.topOverviewDivider, {backgroundColor: palette.border}]} />
      <OverviewMetricBlock
        palette={palette}
        title="每日资金燃烧"
        value={summary.totalDailyBurn}
        unit="/天"
        subLabel="利息+仓储/天"
        footerLabel="按估值计算"
        footerIcon={<FilterTinyIcon color={palette.label} />}
        emphasizeBurn
        flexRatio={3}
      />
      <View style={[styles.topOverviewDivider, {backgroundColor: palette.border}]} />
      <OverviewCashBlock
        palette={palette}
        beforeValue={summary.totalNetCashBefore}
        afterValue={summary.totalRecoverableCash}
        flexRatio={4}
      />
    </View>
  );
}

function OverviewMetricBlock({
  palette,
  title,
  value,
  unit,
  subLabel,
  footerLabel,
  footerIcon,
  emphasizeBurn = false,
  flexRatio = 1,
}: {
  palette: Palette;
  title: string;
  value: number;
  unit: string;
  subLabel: string;
  footerLabel: string;
  footerIcon: React.ReactNode;
  emphasizeBurn?: boolean;
  flexRatio?: number;
}) {
  return (
    <View style={[styles.topOverviewColumn, {flex: flexRatio}]}>
      <View style={styles.topOverviewMain}>
        <Text style={[styles.metricTitle, {color: palette.text}]} numberOfLines={1}>
          {title}
        </Text>
        <SignedMetricValue
          palette={palette}
          value={value}
          unit={unit}
          compact={false}
          emphasizeBurn={emphasizeBurn}
        />
        <Text style={[styles.metricSubLabel, {color: palette.muted}]} numberOfLines={1}>
          {subLabel}
        </Text>
      </View>
      <View style={[styles.metricFooter, {borderTopColor: palette.border}]}>
        <Text style={[styles.metricFooterText, {color: palette.label}]} numberOfLines={1}>
          {footerLabel}
        </Text>
        {footerIcon}
      </View>
    </View>
  );
}

function OverviewCashBlock({
  palette,
  beforeValue,
  afterValue,
  flexRatio = 1,
}: {
  palette: Palette;
  beforeValue: number;
  afterValue: number;
  flexRatio?: number;
}) {
  return (
    <View style={[styles.topOverviewCashColumn, {flex: flexRatio}]}>
      <View style={styles.topOverviewMain}>
        <Text style={[styles.metricTitle, {color: palette.text}]} numberOfLines={1}>
          预计可回现金
        </Text>
        <OverviewCashRow palette={palette} label="交割前" value={beforeValue} emptyDashWhenZero />
        <OverviewCashRow palette={palette} label="交割后" value={afterValue} />
      </View>
      <View style={[styles.metricFooter, {borderTopColor: palette.border}]}>
        <Text style={[styles.metricFooterText, {color: palette.label}]} numberOfLines={1}>
          基于当前库存
        </Text>
        <ActivityTinyIcon color={palette.label} />
      </View>
    </View>
  );
}

function OverviewCashRow({
  palette,
  label,
  value,
  emptyDashWhenZero = false,
}: {
  palette: Palette;
  label: string;
  value: number;
  emptyDashWhenZero?: boolean;
}) {
  return (
    <View style={styles.cashRow}>
      <Text style={[styles.cashLabel, {color: palette.muted}]}>{label}</Text>
      <SignedMetricValue
        palette={palette}
        value={value}
        unit="万"
        compact
        emptyDashWhenZero={emptyDashWhenZero}
      />
    </View>
  );
}

function SummaryBoard({
  width,
  palette,
  summary,
  showKg,
}: {
  width: number;
  palette: Palette;
  summary: PivotSummary;
  showKg: boolean;
}) {
  const columns: SummaryMetricCell[][] = [
    [
      {label: '总重量', value: splitWeight(formatWeight(summary.totalWeight, showKg))},
      {
        label: '占用资金',
        value: splitMoney(formatMoneyWan(summary.totalOccupiedCash)),
        color: palette.text,
      },
    ],
    [
      {label: '总件数', value: {main: summary.totalPieces.toLocaleString()}},
      {
        label: '浮盈亏',
        value: splitMoney(formatMoneyWan(Math.abs(summary.totalFloatingPnL))),
        color: moneyColor(summary.totalFloatingPnL, palette),
        tone: moneyTone(summary.totalFloatingPnL),
      },
    ],
    [
      {label: '品类', value: {main: summary.totalProducts.toLocaleString()}},
      {
        label: '可用现金',
        value: splitMoney(formatMoneyWan(Math.abs(summary.totalRecoverableCash))),
        color: moneyColor(summary.totalRecoverableCash, palette),
        tone: moneyTone(summary.totalRecoverableCash),
      },
    ],
  ];

  return (
    <View style={[styles.summaryBoard, {width}]}>
      {columns.map((column, columnIndex) => (
        <View
          key={`summary-column-${columnIndex}`}
          style={[
            styles.summaryColumn,
            columnIndex < columns.length - 1 ? styles.summaryColumnDivider : null,
            columnIndex < columns.length - 1 ? {borderRightColor: palette.divider} : null,
          ]}>
          {column.map((row, rowIndex) => (
            <React.Fragment key={row.label}>
              <View style={styles.summaryMetric}>
                <View style={styles.summaryMetricContent}>
                  <Text
                    style={[styles.summaryLabel, {color: palette.label}]}
                    numberOfLines={1}>
                    {row.label}
                  </Text>
                  <View style={styles.summaryValueRow}>
                    {row.tone ? <SignedValueIcon tone={row.tone} /> : null}
                    <InlineValue
                      main={row.value.main}
                      unit={row.value.unit}
                      valueStyle={[styles.summaryValue, {color: row.color ?? palette.text}]}
                      unitStyle={[styles.summaryUnit, {color: palette.text}]}
                    />
                  </View>
                </View>
              </View>
              {rowIndex === 0 ? (
                <View style={[styles.summaryDividerHorizontal, {backgroundColor: palette.divider}]} />
              ) : null}
            </React.Fragment>
          ))}
        </View>
      ))}
    </View>
  );
}

function InlineValue({
  main,
  unit,
  valueStyle,
  unitStyle,
}: {
  main: string;
  unit?: string;
  valueStyle: StyleProp<TextStyle>;
  unitStyle: StyleProp<TextStyle>;
}) {
  const mainStyle = main.includes('¥') ? [valueStyle, {color: colors.price}] : valueStyle;
  return (
    <Text style={mainStyle} numberOfLines={1} ellipsizeMode="tail">
      {main}
      {unit ? <Text style={unitStyle}>{unit}</Text> : null}
    </Text>
  );
}

function SignedMetricValue({
  palette,
  value,
  unit,
  compact,
  emptyDashWhenZero = false,
  emphasizeBurn = false,
}: {
  palette: Palette;
  value: number;
  unit: string;
  compact: boolean;
  emptyDashWhenZero?: boolean;
  emphasizeBurn?: boolean;
}) {
  if (emptyDashWhenZero && Math.abs(value) < 0.0001) {
    return (
      <View style={compact ? styles.cashValueRow : styles.metricValueRow}>
        <InlineValue
          main="-"
          unit={unit}
          valueStyle={[compact ? styles.cashValue : styles.metricValue, {color: palette.text}]}
          unitStyle={[compact ? styles.cashUnit : styles.metricUnit, {color: palette.text}]}
        />
      </View>
    );
  }

  const tone = emphasizeBurn ? 'negative' : moneyTone(value);
  const color = tone === 'positive' ? palette.positive : tone === 'negative' ? palette.negative : palette.text;
  const unitColor = emphasizeBurn ? '#171D1C' : palette.text;
  const main = emphasizeBurn
    ? dailyBurnMain(-Math.abs(value))
    : compact
      ? moneyMagnitudeMain(value)
      : metricMagnitudeMain(value, unit);

  return (
    <View style={compact ? styles.cashValueRow : styles.metricValueRow}>
      {tone ? <SignedValueIcon tone={tone} /> : null}
      <InlineValue
        main={main}
        unit={unit}
        valueStyle={[compact ? styles.cashValue : styles.metricValue, {color}]}
        unitStyle={[compact ? styles.cashUnit : styles.metricUnit, {color: unitColor}]}
      />
    </View>
  );
}

function SignedValueIcon({tone}: {tone: 'positive' | 'negative'}) {
  const color = tone === 'positive' ? '#E24B30' : '#229E6C';

  return (
    <Svg width={11} height={12} viewBox="0 0 11 12" fill="none">
      <Rect width={11} height={12} rx={1} fill={color} fillOpacity={0.15} />
      <Path d="M3.3 6H7.7" stroke={color} />
      {tone === 'positive' ? <Path d="M5.5 3.8V8.2" stroke={color} /> : null}
    </Svg>
  );
}

function StatusTinyIcon({color}: {color: string}) {
  return (
    <Svg width={12} height={12} viewBox="0 0 12 12" fill="none">
      <Path
        d="M5.245 1.115L2.75 2.055C2.175 2.27 1.705 2.95 1.705 3.56V7.275C1.705 7.865 2.095 8.64 2.57 8.995L4.72 10.6C5.425 11.13 6.585 11.13 7.29 10.6L9.44 8.995C9.915 8.64 10.305 7.865 10.305 7.275V3.56C10.305 2.945 9.835 2.265 9.26 2.05L6.765 1.115C6.34 0.96 5.66 0.96 5.245 1.115Z"
        stroke={color}
        strokeWidth={0.75}
        strokeLinecap="round"
        strokeLinejoin="round"
      />
      <Path
        d="M4.525 5.935L5.33 6.74L7.48 4.59"
        stroke={color}
        strokeWidth={0.75}
        strokeLinecap="round"
        strokeLinejoin="round"
      />
    </Svg>
  );
}

function ActivityTinyIcon({color}: {color: string}) {
  return (
    <Svg width={12} height={12} viewBox="0 0 12 12" fill="none">
      <Path
        d="M4.5 11H7.5C10 11 11 10 11 7.5V4.5C11 2 10 1 7.5 1H4.5C2 1 1 2 1 4.5V7.5C1 10 2 11 4.5 11Z"
        stroke={color}
        strokeWidth={0.75}
        strokeLinecap="round"
        strokeLinejoin="round"
      />
      <Path
        d="M3.66504 7.245L4.85504 5.7C5.02504 5.48 5.34004 5.44 5.56004 5.61L6.47504 6.33C6.69504 6.5 7.01004 6.46 7.18004 6.245L8.33504 4.755"
        stroke={color}
        strokeWidth={0.75}
        strokeLinecap="round"
        strokeLinejoin="round"
      />
    </Svg>
  );
}

function FilterTinyIcon({color}: {color: string}) {
  return (
    <Svg width={12} height={12} viewBox="0 0 12 12" fill="none">
      <Path
        d="M7 7.99998C7 8.88498 6.615 9.68498 6 10.23C5.47 10.71 4.77 11 4 11C2.345 11 1 9.65498 1 7.99998C1 6.61998 1.94 5.44998 3.21 5.10498C3.555 5.97498 4.295 6.64498 5.21 6.89498C5.46 6.96498 5.725 6.99998 6 6.99998C6.275 6.99998 6.54 6.96498 6.79 6.89498C6.925 7.23498 7 7.60998 7 7.99998Z"
        stroke={color}
        strokeWidth={0.75}
        strokeLinecap="round"
        strokeLinejoin="round"
      />
      <Path
        d="M9 4C9 4.39 8.925 4.765 8.79 5.105C8.445 5.975 7.705 6.645 6.79 6.895C6.54 6.965 6.275 7 6 7C5.725 7 5.46 6.965 5.21 6.895C4.295 6.645 3.555 5.975 3.21 5.105C3.075 4.765 3 4.39 3 4C3 2.345 4.345 1 6 1C7.655 1 9 2.345 9 4Z"
        stroke={color}
        strokeWidth={0.75}
        strokeLinecap="round"
        strokeLinejoin="round"
      />
      <Path
        d="M11 7.99998C11 9.65498 9.655 11 8 11C7.23 11 6.53 10.71 6 10.23C6.615 9.68498 7 8.88498 7 7.99998C7 7.60998 6.925 7.23498 6.79 6.89498C7.705 6.64498 8.445 5.97498 8.79 5.10498C10.06 5.44998 11 6.61998 11 7.99998Z"
        stroke={color}
        strokeWidth={0.75}
        strokeLinecap="round"
        strokeLinejoin="round"
      />
    </Svg>
  );
}

function moneyMagnitudeMain(value: number) {
  return splitMoney(formatMoneyWan(Math.abs(value))).main;
}

function splitMoney(value: string) {
  const match = value.match(/^(.*?)(万)$/);
  if (!match) return {main: value};
  return {main: match[1], unit: match[2]};
}

function splitWeight(value: string) {
  const match = value.match(/^(.*?)(kg|t)$/i);
  if (!match) return {main: value};
  return {main: match[1], unit: match[2].toUpperCase()};
}

function dailyBurnMain(value: number) {
  if (Math.abs(value) < 0.5) return '¥0';
  return `¥${Math.abs(value).toFixed(0)}`;
}

function metricMagnitudeMain(value: number, unit: string) {
  if (unit === '/天') {
    return Math.abs(value) < 0.5 ? '¥0' : `¥${Math.abs(value).toFixed(0)}`;
  }
  return moneyMagnitudeMain(value);
}

function moneyColor(value: number, palette: Palette) {
  if (value > 0) return palette.positive;
  if (value < 0) return palette.negative;
  return palette.text;
}

function moneyTone(value: number): 'positive' | 'negative' | null {
  if (value > 0) return 'positive';
  if (value < 0) return 'negative';
  return null;
}

function getPalette(theme: InventoryTheme) {
  return {
    mode: theme.mode,
    bg: theme.mode === 'light' ? '#F5F5F5' : theme.bg,
    panel: theme.mode === 'light' ? '#FFFFFF' : theme.panel,
    panelSoft: theme.mode === 'light' ? '#F3F6F5' : '#151C24',
    panelSoftAlt: theme.mode === 'light' ? '#F6FBFA' : '#10171E',
    searchBg: theme.mode === 'light' ? '#F3F4F4' : '#131A22',
    text: theme.mode === 'light' ? '#171D1C' : '#EEF2F6',
    label: theme.mode === 'light' ? '#3C4947' : '#B6BDC8',
    muted: theme.mode === 'light' ? '#9DA4A3' : '#8B939F',
    border: theme.mode === 'light' ? '#E3EAE7' : '#27313C',
    divider: theme.mode === 'light' ? '#EEF2F1' : '#1E2832',
    accent: '#09AE92',
    warning: '#E98B21',
    positive: '#E24B30',
    negative: '#229E6C',
    shadow: theme.mode === 'light' ? 'rgba(54,54,54,0.1)' : theme.shadow,
    tabActiveBg: theme.mode === 'light' ? '#3C4947' : '#2C3736',
    selectedBg: theme.mode === 'light' ? 'rgba(9,174,146,0.06)' : '#163A37',
    selectedBorder: '#09AE92',
  };
}

type Palette = ReturnType<typeof getPalette>;

const numericFontFamily = Platform.select({
  ios: 'Manrope-Bold',
  android: 'Manrope-Bold',
  default: undefined,
});

const cashNumericFontFamily = Platform.select({
  ios: 'Manrope-Bold',
  android: 'Manrope-Bold',
  default: undefined,
});

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  inventoryHeader: {
    height: 52,
    width: '100%',
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingHorizontal: 12,
    borderBottomWidth: StyleSheet.hairlineWidth,
    shadowColor: '#000000',
    shadowOpacity: 0.05,
    shadowRadius: 3,
    shadowOffset: {width: 0, height: 2},
    elevation: 2,
  },
  inventoryHeaderSurface: {
    backgroundColor: '#FFFFFF',
  },
  inventoryHeaderButton: {
    width: 40,
    height: 40,
    alignItems: 'center',
    justifyContent: 'center',
  },
  inventoryHeaderTitle: {
    position: 'absolute',
    left: 56,
    right: 56,
    textAlign: 'center',
    fontSize: 16,
    lineHeight: 21,
    fontWeight: '700',
  },
  pageScroll: {
    flex: 1,
  },
  pageContent: {
    alignItems: 'center',
    paddingTop: 10,
    paddingHorizontal: 12,
  },
  centerContainer: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'flex-start',
  },
  centerState: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    gap: 6,
  },
  loadingText: {
    fontSize: 14,
  },
  errorText: {
    fontSize: 14,
    fontWeight: '600',
  },
  retryBtn: {
    paddingHorizontal: 16,
    paddingVertical: 10,
    borderRadius: 6,
    borderWidth: 1,
  },
  retryText: {
    fontSize: 14,
    fontWeight: '600',
  },
  topCardsWrap: {
    alignItems: 'center',
  },
  topOverviewCard: {
    minHeight: 105,
    borderRadius: 4,
    borderWidth: 1,
    padding: 12,
    flexDirection: 'row',
    alignItems: 'stretch',
    justifyContent: 'space-between',
    gap: 8,
    backgroundColor: '#FFFFFF',
  },
  topOverviewColumn: {
    flex: 1,
    gap: 6,
    minWidth: 0,
  },
  topOverviewCashColumn: {
    flex: 1,
    gap: 6,
    minWidth: 0,
  },
  topOverviewMain: {
    flex: 1,
    gap: 2,
    minWidth: 0,
  },
  topOverviewDivider: {
    width: StyleSheet.hairlineWidth,
    height: 23,
    marginHorizontal: 4,
    alignSelf: 'center',
  },
  topTabsRow: {
    paddingTop: 6,
    paddingBottom: 0,
    gap: 6,
    flexDirection: 'row',
    alignItems: 'flex-start',
  },
  topTabItem: {
    flex: 1,
    gap: 3,
    minWidth: 0,
  },
  topTabChip: {
    minHeight: 33,
    paddingHorizontal: 8,
    paddingVertical: 8,
    borderRadius: 4,
    borderWidth: 1,
    alignItems: 'center',
    justifyContent: 'center',
    minWidth: 0,
  },
  topTabChipActive: {
    shadowColor: 'rgba(0,106,97,0.4)',
    shadowOffset: {width: 0, height: 2},
    shadowOpacity: 1,
    shadowRadius: 7.5,
    elevation: 4,
  },
  topTabInner: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    gap: 4,
    minWidth: 0,
    width: '100%',
  },
  topTabText: {
    fontSize: 13,
    lineHeight: 16,
    includeFontPadding: false,
    flexShrink: 1,
    minWidth: 0,
    textAlign: 'center',
  },
  topTabTextActive: {
    fontWeight: '600',
  },
  topTabTextInactive: {
    fontWeight: '400',
  },
  topTabIndicator: {
    width: 17,
    height: 3,
    borderRadius: 3,
    alignSelf: 'center',
  },
  topTabIndicatorActive: {
    backgroundColor: FIGMA_ACTIVE_GREEN,
  },
  topTabIndicatorInactive: {
    backgroundColor: 'transparent',
  },
  filterChipRow: {
    paddingBottom: 2,
    gap: 6,
  },
  filterChip: {
    height: 27,
    paddingHorizontal: 10,
    borderRadius: 4,
    borderWidth: 1,
    alignItems: 'center',
    justifyContent: 'center',
    position: 'relative',
  },
  filterChipText: {
    fontSize: 12,
    lineHeight: 17,
    includeFontPadding: false,
  },
  filterChipSelected: {
    backgroundColor: 'rgba(0,106,97,0.05)',
    borderColor: FIGMA_ACTIVE_GREEN,
  },
  filterChipUnselected: {
    backgroundColor: '#F3F4F4',
    borderColor: '#E2E7E6',
  },
  filterChipTextSelected: {
    fontWeight: '500',
  },
  filterChipTextDefault: {
    fontWeight: '400',
  },
  dynamicFilterRow: {
    paddingTop: 0,
  },
  dynamicSummaryCard: {
    marginTop: 6,
    borderRadius: 4,
    borderWidth: 1,
    padding: 12,
    gap: 6,
  },
  dynamicStickyWrap: {
    paddingBottom: 6,
  },
  thresholdChip: {
    minHeight: 22,
    paddingHorizontal: 10,
    paddingVertical: 5,
    borderRadius: 4,
    borderWidth: 1,
    alignItems: 'center',
    justifyContent: 'center',
    flexDirection: 'row',
    gap: 2,
  },
  thresholdChipText: {
    fontSize: 12,
    lineHeight: 17,
    fontWeight: '400',
    includeFontPadding: false,
  },
  metricTitle: {
    fontSize: 11,
    lineHeight: 16,
    fontWeight: '600',
    letterSpacing: 0,
    flexShrink: 1,
  },
  metricValue: {
    fontSize: 12,
    lineHeight: 16,
    fontStyle: 'normal',
    fontVariant: ['tabular-nums'],
    fontFamily: numericFontFamily,
  },
  metricUnit: {
    fontSize: 8,
    lineHeight: 12,
    fontStyle: 'normal',
    fontWeight: '400',
    fontFamily: 'PingFang SC',
  },
  metricSubLabel: {
    fontSize: 10,
    lineHeight: 14,
    flexShrink: 1,
  },
  metricValueRow: {
    minHeight: 20,
    flexDirection: 'row',
    alignItems: 'center',
    gap: 2,
    minWidth: 0,
  },
  metricFooter: {
    paddingTop: 6,
    borderTopWidth: 1,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: 4,
    minWidth: 0,
  },
  metricFooterText: {
    fontSize: 10,
    lineHeight: 14,
    flex: 1,
    minWidth: 0,
  },
  cashRow: {
    width: '100%',
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    minHeight: 19,
    gap: 4,
  },
  cashLabel: {
    fontSize: 10,
    lineHeight: 14,
  },
  cashValue: {
    fontSize: 13,
    lineHeight: 17,
    fontStyle: 'normal',
    fontWeight: '700',
    fontVariant: ['tabular-nums'],
    fontFamily: cashNumericFontFamily,
  },
  cashUnit: {
    fontSize: 8,
    lineHeight: 12,
    fontStyle: 'normal',
    fontWeight: '400',
    fontFamily: 'PingFang SC',
  },
  cashValueRow: {
    minHeight: 17,
    flexDirection: 'row',
    alignItems: 'center',
    gap: 2,
  },
  sectionCard: {
    marginTop: 6,
    borderRadius: 4,
    borderWidth: 1,
    paddingHorizontal: 10,
    paddingTop: 10,
    paddingBottom: 10,
  },
  sectionCardTop: {
    borderBottomWidth: 0,
    borderBottomLeftRadius: 0,
    borderBottomRightRadius: 0,
    paddingBottom: 0,
  },
  sectionHead: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: 8,
  },
  sectionTitleLockup: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 6,
    flex: 1,
    minWidth: 0,
  },
  sectionTitlePrimary: {
    fontSize: 15,
    lineHeight: 21,
    fontWeight: '600',
    flexShrink: 1,
  },
  sectionTitleSecondary: {
    fontSize: 12,
    lineHeight: 17,
    fontWeight: '400',
    flexShrink: 1,
  },
  unitWrap: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 6,
    flexShrink: 0,
  },
  unitLabel: {
    fontSize: 11,
    lineHeight: 16,
  },
  unitSwitch: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 2,
    padding: 3,
    borderRadius: 4,
    borderWidth: 1,
  },
  unitSegment: {
    width: 32,
    paddingVertical: 2,
    borderRadius: 4,
    alignItems: 'center',
    justifyContent: 'center',
  },
  unitSegmentActive: {
    borderRadius: 4,
  },
  unitSegmentText: {
    fontSize: 11,
    lineHeight: 16,
  },
  unitSegmentTextActive: {
    fontWeight: '500',
  },
  unitSegmentTextInactive: {
    fontWeight: '400',
  },
  searchBar: {
    marginTop: 6,
    height: 38,
    borderRadius: 4,
    borderWidth: 1,
    paddingHorizontal: 10,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
  },
  searchBarWhite: {
    backgroundColor: '#FFFFFF',
  },
  searchInputWrap: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 6,
    flex: 1,
    minWidth: 0,
  },
  searchInput: {
    flex: 1,
    height: 20,
    paddingTop: 0,
    paddingBottom: 0,
    marginTop: Platform.OS === 'ios' ? -1 : 0,
    fontSize: 13,
    lineHeight: 16,
    textAlignVertical: 'center',
  },
  searchAction: {
    minWidth: 28,
    alignItems: 'flex-end',
    justifyContent: 'center',
    marginLeft: 8,
    flexShrink: 0,
  },
  searchActionText: {
    fontSize: 12,
    lineHeight: 17,
  },
  detailSearchBar: {
    marginTop: 0,
  },
  detailStickyWrap: {
    paddingBottom: 0,
  },
  detailHeaderShell: {
    alignSelf: 'center',
    marginTop: 6,
  },
  summaryBoard: {
    minHeight: 102,
    alignSelf: 'center',
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    paddingVertical: 0,
  },
  summaryColumn: {
    flex: 1,
    height: 78,
    alignItems: 'center',
    justifyContent: 'center',
    gap: 6,
    paddingHorizontal: 6,
    minWidth: 0,
  },
  summaryColumnDivider: {
    borderRightWidth: 1,
  },
  summaryMetric: {
    height: 35,
    width: '100%',
    alignItems: 'center',
    justifyContent: 'center',
  },
  summaryMetricContent: {
    width: '100%',
    height: 35,
    alignItems: 'center',
    gap: 2,
    minWidth: 0,
  },
  summaryDividerHorizontal: {
    width: '100%',
    height: StyleSheet.hairlineWidth,
  },
  summaryLabel: {
    fontSize: 10,
    lineHeight: 13,
    textAlign: 'center',
  },
  summaryValueRow: {
    width: '100%',
    height: 19,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    gap: 2,
    minWidth: 0,
  },
  summaryValue: {
    fontSize: 12,
    lineHeight: 16,
    fontStyle: 'normal',
    fontVariant: ['tabular-nums'],
    fontFamily: numericFontFamily,
  },
  summaryUnit: {
    fontSize: 8,
    lineHeight: 12,
    fontStyle: 'normal',
    fontWeight: '400',
    fontFamily: 'PingFang SC',
  },
  pivotSummaryWrap: {
    marginTop: 0,
  },
  tableSticky: {
    alignSelf: 'center',
    borderLeftWidth: 1,
    borderRightWidth: 1,
    paddingTop: 12,
  },
  tableHeaderCard: {
    minHeight: 21,
    paddingHorizontal: 10,
  },
  tableRowsCard: {
    alignSelf: 'center',
    borderLeftWidth: 1,
    borderRightWidth: 1,
    borderBottomWidth: 1,
    borderBottomLeftRadius: 4,
    borderBottomRightRadius: 4,
    paddingHorizontal: 10,
    overflow: 'hidden',
  },
  moduleBodyCard: {
    marginTop: 0,
    alignSelf: 'center',
    borderLeftWidth: 1,
    borderRightWidth: 1,
    borderBottomWidth: 1,
    borderBottomLeftRadius: 4,
    borderBottomRightRadius: 4,
    paddingHorizontal: 10,
    paddingTop: 10,
    paddingBottom: 10,
    gap: 6,
  },
  dynamicCardsGrid: {
    marginTop: 6,
    flexDirection: 'row',
    flexWrap: 'wrap',
    rowGap: 6,
    columnGap: 6,
  },
  analysisBucketRow: {
    flexDirection: 'row',
    gap: 8,
  },
  analysisBucketCard: {
    flex: 1,
    minHeight: 84,
    borderWidth: 1,
    borderRadius: 4,
    paddingHorizontal: 10,
    paddingVertical: 10,
    justifyContent: 'space-between',
  },
  analysisBucketLabel: {
    fontSize: 11,
    lineHeight: 16,
  },
  analysisBucketValue: {
    fontSize: 13,
    lineHeight: 17,
    fontStyle: 'normal',
    fontFamily: numericFontFamily,
    fontVariant: ['tabular-nums'],
  },
  analysisBucketMeta: {
    fontSize: 10,
    lineHeight: 14,
  },
  analysisRankingCard: {
    borderWidth: 1,
    borderRadius: 4,
    paddingHorizontal: 10,
    paddingVertical: 10,
  },
  analysisCardTitle: {
    fontSize: 12,
    lineHeight: 17,
    fontWeight: '600',
    marginBottom: 4,
  },
  analysisRankingRow: {
    minHeight: 42,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingVertical: 8,
    gap: 10,
  },
  analysisRankingText: {
    flex: 1,
    minWidth: 0,
  },
  analysisRankingLabel: {
    fontSize: 11,
    lineHeight: 16,
    fontWeight: '500',
  },
  analysisRankingMeta: {
    marginTop: 1,
    fontSize: 9,
    lineHeight: 13,
  },
  analysisRankingValue: {
    fontSize: 13,
    lineHeight: 17,
    fontStyle: 'normal',
    fontFamily: numericFontFamily,
    fontVariant: ['tabular-nums'],
  },
  dynamicProductCard: {
    borderWidth: 1,
    borderRadius: 4,
    paddingHorizontal: 10,
    paddingVertical: 10,
    gap: 6,
  },
  dynamicProductHead: {
    width: '100%',
  },
  dynamicProductTitleWrap: {
    gap: 2,
  },
  dynamicProductTitle: {
    fontSize: 11,
    lineHeight: 16,
    fontWeight: '500',
    color: '#171D1C',
  },
  selectedFilterBadge: {
    position: 'absolute',
    top: -1,
    right: -1,
    width: 12,
    height: 10,
    alignItems: 'center',
    justifyContent: 'center',
  },
  dynamicHeadMetaRow: {
    width: '100%',
    minHeight: 19,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: 8,
  },
  dynamicCountWrap: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 2,
  },
  dynamicCardCountValue: {
    fontSize: 12,
    lineHeight: 16,
    fontStyle: 'normal',
    fontFamily: numericFontFamily,
    fontVariant: ['tabular-nums'],
  },
  dynamicCardCountUnit: {
    fontSize: 8,
    lineHeight: 12,
  },
  dynamicAlertBadge: {
    alignSelf: 'flex-start',
    minHeight: 17,
    paddingHorizontal: 4,
    paddingVertical: 2,
    borderRadius: 2,
    justifyContent: 'center',
    alignItems: 'center',
  },
  dynamicAlertBadgeText: {
    fontSize: 9,
    lineHeight: 12,
    fontWeight: '400',
  },
  dynamicFundRow: {
    minHeight: 19,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
  },
  dynamicFundLabel: {
    fontSize: 11,
    lineHeight: 16,
  },
  dynamicFundValue: {
    fontSize: 12,
    lineHeight: 16,
    fontStyle: 'normal',
    fontFamily: numericFontFamily,
    fontVariant: ['tabular-nums'],
  },
  dynamicProfitLossBand: {
    minHeight: 45,
    borderRadius: 4,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingHorizontal: 10,
    paddingVertical: 6,
  },
  dynamicProfitLossCell: {
    alignItems: 'center',
    gap: 2,
    width: 36,
  },
  dynamicProfitLossValue: {
    fontSize: 12,
    lineHeight: 16,
    fontStyle: 'normal',
    fontFamily: numericFontFamily,
    fontVariant: ['tabular-nums'],
  },
  dynamicProfitLossLabel: {
    fontSize: 9,
    lineHeight: 13,
  },
  dynamicProfitLossDividerLine: {
    width: StyleSheet.hairlineWidth,
    height: 16.5,
  },
  dynamicMetricList: {
    gap: 6,
  },
  dynamicMetricRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: 10,
  },
  dynamicMetricLabel: {
    fontSize: 11,
    lineHeight: 16,
  },
  dynamicMetricValue: {
    fontSize: 11,
    lineHeight: 16,
    fontStyle: 'normal',
    fontWeight: '700',
    fontFamily: numericFontFamily,
    fontVariant: ['tabular-nums'],
  },
  dynamicMetricUnit: {
    fontSize: 8,
    lineHeight: 12,
    fontStyle: 'normal',
    fontWeight: '400',
    fontFamily: 'PingFang SC',
  },
  dynamicCardDivider: {
    height: StyleSheet.hairlineWidth,
    width: '100%',
  },
  dynamicInsightText: {
    fontSize: 11,
    lineHeight: 16,
  },
  dynamicCostBar: {
    height: 28,
    borderRadius: 14,
    justifyContent: 'center',
    overflow: 'hidden',
    position: 'relative',
  },
  dynamicCostTrack: {
    height: 14,
    marginHorizontal: 4,
    borderRadius: 7,
  },
  dynamicCostLabels: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
  },
  dynamicCostDot: {
    position: 'absolute',
    top: 3,
    width: 22,
    height: 22,
    marginLeft: -11,
    borderRadius: 11,
    alignItems: 'center',
    justifyContent: 'center',
  },
  dynamicCostDotCool: {
    backgroundColor: '#22C38E',
  },
  dynamicCostDotHot: {
    backgroundColor: '#FF6B65',
    borderWidth: 2,
    borderColor: '#FFD5D3',
  },
  dynamicCostDotText: {
    fontSize: 13,
    lineHeight: 17,
    fontStyle: 'normal',
    color: '#FFFFFF',
    fontFamily: numericFontFamily,
  },
  dynamicCostAvgMarker: {
    position: 'absolute',
    left: '50%',
    top: 1,
    bottom: 1,
    marginLeft: -1,
    width: 2,
    alignItems: 'center',
  },
  dynamicCostAvgLine: {
    width: 2,
    height: '100%',
  },
  dynamicCostLabel: {
    fontSize: 10,
    lineHeight: 14,
  },
  dynamicCostLabelStrong: {
    fontSize: 10,
    lineHeight: 14,
    fontWeight: '600',
  },
  detailSearchWrap: {
    width: '100%',
    alignSelf: 'center',
    marginTop: 6,
  },
  detailFilterRow: {
    paddingTop: 6,
    paddingBottom: 0,
  },
  detailSortChip: {
    height: 32,
    paddingHorizontal: 6,
    borderRadius: 4,
    borderWidth: 1,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    gap: 4,
  },
  detailSortChipSelected: {
    backgroundColor: '#F2F7F7',
  },
  detailSortChipDefault: {
    backgroundColor: '#FFFFFF',
  },
  detailSortChipText: {
    fontSize: 12,
    lineHeight: 17,
    includeFontPadding: false,
  },
  detailSortStateBadge: {
    backgroundColor: FIGMA_ACTIVE_GREEN,
    borderRadius: 2,
    paddingHorizontal: 3,
    paddingVertical: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  detailSortStateBadgeText: {
    fontSize: 10,
    lineHeight: 13,
    color: '#FFFFFF',
    includeFontPadding: false,
  },
  detailSortArrowIcon: {
    width: 6,
    height: 12,
    resizeMode: 'contain',
  },
  detailTableShell: {
    alignSelf: 'center',
    marginTop: 0,
    overflow: 'hidden',
    borderBottomLeftRadius: 4,
    borderBottomRightRadius: 4,
  },
  detailTableSplit: {
    flexDirection: 'row',
    alignItems: 'stretch',
    borderWidth: 1,
    borderColor: '#E2E7E6',
    borderRadius: 4,
    overflow: 'hidden',
    backgroundColor: '#FFFFFF',
  },
  detailHeaderTableSplit: {
    borderBottomLeftRadius: 0,
    borderBottomRightRadius: 0,
  },
  detailBodyTableSplit: {
    borderTopWidth: 0,
    borderTopLeftRadius: 0,
    borderTopRightRadius: 0,
  },
  detailFrozenColumn: {
    width: DETAIL_FROZEN_MIN_WIDTH,
    borderRightWidth: 1,
    borderRightColor: '#E2E7E6',
    backgroundColor: '#FFFFFF',
  },
  detailFrozenHeader: {
    paddingHorizontal: 12,
    gap: 0,
  },
  detailFrozenRow: {
    paddingHorizontal: 12,
    gap: 0,
  },
  detailTableContent: {
    paddingBottom: 0,
  },
  detailScrollableTable: {
    width: 0,
  },
  detailTableHeader: {
    height: 26,
    backgroundColor: '#E0E0E0',
    paddingHorizontal: 12,
    paddingVertical: 0,
    flexDirection: 'row',
    alignItems: 'center',
    gap: DETAIL_HEADER_COLUMN_GAP,
  },
  detailHeaderCell: {
    height: 26,
    justifyContent: 'center',
    alignItems: 'flex-start',
  },
  detailHeaderText: {
    fontSize: 10,
    lineHeight: 13,
    color: '#3C4947',
  },
  detailTableRow: {
    height: 98,
    paddingHorizontal: 12,
    paddingVertical: 12,
    flexDirection: 'row',
    alignItems: 'center',
    gap: DETAIL_ROW_COLUMN_GAP,
  },
  detailTableRowEven: {
    backgroundColor: '#FFFFFF',
  },
  detailTableRowOdd: {
    backgroundColor: '#EEEEEE',
  },
  detailBodyCell: {
    minHeight: 74,
    justifyContent: 'center',
    gap: 2,
  },
  detailNumberCell: {
    alignItems: 'flex-start',
  },
  detailPrimaryStrong: {
    fontSize: 12,
    lineHeight: 16,
    fontStyle: 'normal',
    color: '#171D1C',
    fontFamily: numericFontFamily,
    fontVariant: ['tabular-nums'],
  },
  detailPrimaryText: {
    fontSize: 11,
    lineHeight: 16,
    color: '#171D1C',
  },
  detailSecondaryText: {
    fontSize: 11,
    lineHeight: 16,
    color: '#3C4947',
  },
  detailInlineMetaRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
  },
  detailInlineDivider: {
    width: StyleSheet.hairlineWidth,
    height: 8,
    backgroundColor: '#9DA4A3',
  },
  detailStatusValue: {
    fontSize: 9,
    lineHeight: 13,
  },
  detailMetricNumber: {
    fontSize: 12,
    lineHeight: 16,
    color: '#171D1C',
    fontStyle: 'normal',
    fontFamily: numericFontFamily,
    fontVariant: ['tabular-nums'],
  },
  detailMoneyNumber: {
    fontSize: 12,
    lineHeight: 16,
    color: colors.price,
    fontStyle: 'normal',
    fontFamily: numericFontFamily,
    fontVariant: ['tabular-nums'],
  },
  detailMoneyInline: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 2,
  },
  detailMoneyUnit: {
    fontSize: 8,
    lineHeight: 12,
    color: '#171D1C',
  },
  detailWarningNumber: {
    color: colors.price,
  },
  detailPositiveNumber: {
    color: colors.price,
  },
  detailNegativeNumber: {
    color: colors.price,
  },
  detailRecoverableWrap: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 4,
  },
  detailRecoverableDot: {
    width: 4,
    height: 4,
    borderRadius: 2,
    backgroundColor: '#9DA4A3',
  },
});
