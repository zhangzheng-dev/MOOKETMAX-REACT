import React, {useCallback, useMemo, useState} from 'react';
import {
  ActivityIndicator,
  Pressable,
  RefreshControl,
  ScrollView,
  StyleSheet,
  Text,
  View,
} from 'react-native';
import {useFocusEffect} from '@react-navigation/native';
import {useSafeAreaInsets} from 'react-native-safe-area-context';
import type {NativeStackScreenProps} from '@react-navigation/native-stack';
import Svg, {Circle, Line, Path, Polyline} from 'react-native-svg';
import {SvgXml} from 'react-native-svg';
import {mooketApi} from '../api/mooketApi';
import {ErrorState} from '../components/common/ErrorState';
import {backArrowXml} from '../components/detail/productIcons';
import type {RootStackParamList} from '../navigation/routes';
import {colors} from '../theme/colors';
import {fonts} from '../theme/typography';
import type {FactoryPriceComparison, FactoryTrendData, FactoryTrendPoint} from '../types/api';

type Props = NativeStackScreenProps<RootStackParamList, 'DataComparison'>;

const maxSelectedFactories = 6;
// Figma: 1427:2462 / 343 wide / 164 height chart area
const chartWidth = 311;
const chartHeight = 164;
const chartPaddingX = 8;
const chartPaddingY = 18;
// Figma 颜色：橙、品红、蓝、红、紫、绿
const lineColors = ['#FC9E39', '#E438AE', '#0C40DD', '#E24B30', '#7C4DFF', '#229E6C'];
const cardTints = [
  'rgba(252,158,57,0.05)',
  'rgba(228,56,174,0.05)',
  'rgba(12,64,221,0.05)',
  'rgba(226,75,48,0.05)',
  'rgba(124,77,255,0.05)',
  'rgba(34,158,108,0.05)',
];
// 14x12 ic_check selected indicator (Figma style)
const selectedTickXml = `<svg viewBox="0 0 14 12" xmlns="http://www.w3.org/2000/svg"><path d="M0 0 L14 0 L7 12 Z" fill="#006A61"/><path d="M4.5 4.5 L6.5 6.5 L9.5 3.5" stroke="white" stroke-width="1.2" stroke-linecap="round" stroke-linejoin="round" fill="none"/></svg>`;

export function DataComparisonScreen({navigation, route}: Props) {
  const insets = useSafeAreaInsets();
  const {country, factoryNos, productName, category, excludeFactoryNo} = route.params;
  const availableFactories = useMemo(
    () => uniqueFactories(factoryNos),
    [factoryNos],
  );
  void excludeFactoryNo;

  const [selectedFactories, setSelectedFactories] = useState<Set<string>>(
    () => new Set(availableFactories.slice(0, Math.min(2, maxSelectedFactories))),
  );
  const [data, setData] = useState<FactoryPriceComparison | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [selectedDateIndex, setSelectedDateIndex] = useState<number | null>(null);

  const selectedList = useMemo(() => Array.from(selectedFactories), [selectedFactories]);

  const load = useCallback(async () => {
    if (selectedList.length === 0) {
      setData(null);
      return;
    }
    setLoading(true);
    setError(null);
    try {
      const nextData = await mooketApi.getFactoryPriceComparison(country, selectedList, productName, category);
      setData(nextData);
      const lastIndex = nextData.factories[0]?.trend?.length ? nextData.factories[0].trend.length - 1 : null;
      setSelectedDateIndex(current => (current == null ? lastIndex : current));
    } catch (e) {
      setError(e instanceof Error ? e.message : '加载失败');
    } finally {
      setLoading(false);
    }
  }, [category, country, productName, selectedList]);

  useFocusEffect(
    useCallback(() => {
      load().catch(() => undefined);
    }, [load]),
  );

  // 维持厂号在 availableFactories 中的稳定顺序
  const orderedFactories = useMemo(() => {
    const trendByFactory = new Map((data?.factories ?? []).map(item => [item.factoryNo, item]));
    return availableFactories
      .filter(factoryNo => selectedFactories.has(factoryNo))
      .map(factoryNo => trendByFactory.get(factoryNo))
      .filter((item): item is FactoryTrendData => Boolean(item));
  }, [availableFactories, data?.factories, selectedFactories]);

  const visibleFactories = orderedFactories;
  const selectedIndex = selectedDateIndex ?? latestTrendIndex(visibleFactories);
  const selectedDate = visibleFactories[0]?.trend?.[selectedIndex]?.fullDate ?? visibleFactories[0]?.trend?.[selectedIndex]?.date;

  const toggleFactory = (factoryNo: string) => {
    setSelectedFactories(current => {
      const next = new Set(current);
      if (next.has(factoryNo)) {
        next.delete(factoryNo);
      } else if (next.size < maxSelectedFactories) {
        next.add(factoryNo);
      }
      return next;
    });
    setSelectedDateIndex(null);
  };

  return (
    <View style={styles.screen}>
      <View style={[styles.topBar, {paddingTop: insets.top + 12, minHeight: insets.top + 48}]}>
        <Pressable hitSlop={8} onPress={() => navigation.goBack()} style={styles.backButton}>
          <SvgXml xml={backArrowXml} width={24} height={24} />
        </Pressable>
        <Text style={styles.topBarTitle}>数据对比</Text>
        <View style={styles.topBarPlaceholder} />
      </View>

      <ScrollView
        style={styles.container}
        contentContainerStyle={styles.content}
        refreshControl={<RefreshControl refreshing={loading} onRefresh={load} />}>
        {/* 厂号选择器 */}
        <View style={styles.selectorBlock}>
          <View style={styles.selectorHead}>
            <Text style={styles.titleText} numberOfLines={1}>
              {country}{availableFactories[0] ?? ''} {productName}
            </Text>
            <Text style={styles.selectedCount}>
              <Text style={styles.selectedCountActive}>{selectedFactories.size}</Text>
              <Text>/</Text>
              <Text>{maxSelectedFactories}</Text>
            </Text>
          </View>
          <ScrollView
            horizontal
            showsHorizontalScrollIndicator={false}
            contentContainerStyle={styles.factoryRow}>
            {availableFactories.map(factoryNo => {
              const active = selectedFactories.has(factoryNo);
              return (
                <Pressable
                  key={factoryNo}
                  onPress={() => toggleFactory(factoryNo)}
                  style={[styles.factoryChip, active && styles.factoryChipActive]}>
                  <Text style={[styles.factoryChipText, active && styles.factoryChipTextActive]}>
                    {factoryNo}
                  </Text>
                  {active ? (
                    <View style={styles.factoryChipTick}>
                      <SvgXml xml={selectedTickXml} width={14} height={12} />
                    </View>
                  ) : null}
                </Pressable>
              );
            })}
          </ScrollView>
        </View>

        {loading && visibleFactories.length === 0 ? (
          <ActivityIndicator color={colors.primary} style={styles.loading} />
        ) : error && visibleFactories.length === 0 ? (
          <ErrorState message={error} onRetry={load} />
        ) : selectedFactories.size === 0 ? (
          <Text style={styles.empty}>请选择至少一个厂号</Text>
        ) : visibleFactories.length === 0 ? (
          <Text style={styles.empty}>暂无对比数据</Text>
        ) : (
          <>
            <View style={styles.titleRow}>
              <Text style={styles.dateBig}>{formatDate(selectedDate)}</Text>
              <Text style={styles.dateSmall}>报盘价格走势（RMB）</Text>
            </View>

            <PriceCardsGrid factories={visibleFactories} selectedIndex={selectedIndex} country={country} />

            <TrendComparisonChart
              factories={visibleFactories}
              selectedIndex={selectedIndex}
              onDateSelect={setSelectedDateIndex}
            />
          </>
        )}
      </ScrollView>
    </View>
  );
}

function PriceCardsGrid({
  factories,
  selectedIndex,
  country,
}: {
  factories: FactoryTrendData[];
  selectedIndex: number;
  country: string;
}) {
  return (
    <View style={styles.cardsGrid}>
      {factories.map((factory, index) => {
        const point = factory.trend?.[selectedIndex];
        const color = lineColors[index % lineColors.length];
        const tint = cardTints[index % cardTints.length];
        // 计算该厂号全部趋势的价格区间
        const allPrices = (factory.trend ?? [])
          .map(p => p.avgPrice)
          .filter((v): v is number => v != null && v > 0);
        const factoryPriceMin = allPrices.length ? Math.min(...allPrices) : null;
        const factoryPriceMax = allPrices.length ? Math.max(...allPrices) : null;
        return (
          <View
            key={factory.factoryNo}
            style={[styles.priceCard, {backgroundColor: tint, borderTopColor: color}]}>
            <Text style={styles.priceCardTitle} numberOfLines={1}>
              {country}{factory.factoryNo}
            </Text>
            <View style={styles.priceCardPriceLine}>
              <Text style={styles.priceCardPriceMain}>
                {formatPriceRange(factoryPriceMin, factoryPriceMax)}
              </Text>
              <Text style={styles.priceCardUnit}>/kg</Text>
            </View>
            <View style={styles.priceCardDivider} />
            <View style={styles.priceCardStats}>
              <View style={styles.priceCardStatCell}>
                <Text style={styles.priceCardSmall}>日均价</Text>
                <View style={styles.priceCardPriceLine}>
                  <Text style={styles.priceCardPriceSub}>
                    {point?.avgPrice != null ? `¥${num(point.avgPrice)}` : '--'}
                  </Text>
                  {point?.avgPrice != null ? (
                    <Text style={styles.priceCardUnit}>/kg</Text>
                  ) : null}
                </View>
              </View>
              <View style={styles.priceCardStatCell}>
                <Text style={styles.priceCardSmall}>报盘数</Text>
                <Text style={styles.priceCardCount}>
                  {point?.offerCount ?? '--'}
                </Text>
              </View>
            </View>
          </View>
        );
      })}
    </View>
  );
}

function TrendComparisonChart({
  factories,
  onDateSelect,
  selectedIndex,
}: {
  factories: FactoryTrendData[];
  selectedIndex: number;
  onDateSelect: (index: number) => void;
}) {
  const allPrices = factories.flatMap(factory =>
    (factory.trend ?? []).map(point => point.avgPrice).filter((value): value is number => value != null && value > 0),
  );
  const min = allPrices.length ? Math.min(...allPrices) : 0;
  const max = allPrices.length ? Math.max(...allPrices) : 1;
  const firstTrend = factories[0]?.trend ?? [];

  const dateLabels = useMemo(() => {
    if (firstTrend.length === 0) return [];
    const idxs = [0, Math.floor(firstTrend.length / 4), Math.floor(firstTrend.length / 2), Math.floor((firstTrend.length * 3) / 4), firstTrend.length - 1];
    return idxs.map(i => firstTrend[i]?.date ?? '');
  }, [firstTrend]);

  function handleChartPress(event: {nativeEvent: {locationX: number}}) {
    const x = event.nativeEvent.locationX;
    const total = firstTrend.length;
    if (total <= 0) return;
    const stepX = total > 1 ? chartWidth / (total - 1) : chartWidth;
    const index = Math.round(x / stepX);
    const clamped = Math.max(0, Math.min(total - 1, index));
    onDateSelect(clamped);
  }

  return (
    <View style={styles.chartCard}>
      <Pressable onPress={handleChartPress}>
        <Svg width={chartWidth} height={chartHeight} viewBox={`0 0 ${chartWidth} ${chartHeight}`}>
        {/* X 轴底线 */}
        <Line
          x1={chartPaddingX}
          y1={chartHeight - chartPaddingY}
          x2={chartWidth - chartPaddingX}
          y2={chartHeight - chartPaddingY}
          stroke="#DEE4E1"
          strokeWidth={0.5}
        />
        {/* 选中日期竖虚线 */}
        {firstTrend.length > 0 ? (
          <Line
            x1={xForIndex(selectedIndex, firstTrend.length)}
            y1={chartPaddingY}
            x2={xForIndex(selectedIndex, firstTrend.length)}
            y2={chartHeight - chartPaddingY}
            stroke="#9DA4A3"
            strokeWidth={0.5}
            strokeDasharray="3 3"
          />
        ) : null}
        {factories.map((factory, factoryIndex) => {
          const pathPoints = pointString(factory.trend ?? [], min, max);
          return pathPoints ? (
            <Polyline
              key={factory.factoryNo}
              points={pathPoints}
              fill="none"
              stroke={lineColors[factoryIndex % lineColors.length]}
              strokeWidth={1.5}
              strokeLinecap="round"
              strokeLinejoin="round"
            />
          ) : null;
        })}
        {/* 选中日期圆环 */}
        {factories.map((factory, factoryIndex) => {
          const point = factory.trend?.[selectedIndex];
          if (!point || point.avgPrice == null) return null;
          const x = xForIndex(selectedIndex, firstTrend.length);
          const ratio = max === min ? 0.5 : (point.avgPrice - min) / (max - min);
          const y = chartHeight - chartPaddingY - ratio * (chartHeight - chartPaddingY * 2);
          return (
            <Circle
              key={`dot-${factory.factoryNo}`}
              cx={x}
              cy={y}
              r={4}
              fill="#FFFFFF"
              stroke={lineColors[factoryIndex % lineColors.length]}
              strokeWidth={1.5}
            />
          );
        })}
        {/* 触摸热区圆点（透明） */}
        {firstTrend.map((point, index) => {
          const x = xForIndex(index, firstTrend.length);
          return (
            <Circle
              key={`hit-${point.fullDate}-${index}`}
              cx={x}
              cy={chartHeight - chartPaddingY}
              r={8}
              fill="transparent"
              onPress={() => onDateSelect(index)}
            />
          );
        })}
      </Svg>
      </Pressable>
      <View style={styles.dateRow}>
        {dateLabels.map((d, i) => (
          <Text key={`${d}-${i}`} style={styles.dateText}>{d || '--'}</Text>
        ))}
      </View>
    </View>
  );
}

function pointString(points: FactoryTrendPoint[], min: number, max: number) {
  const segments: string[] = [];
  let segment: string[] = [];
  points.forEach((point, index) => {
    if (point.avgPrice == null || point.avgPrice <= 0) {
      if (segment.length > 0) {
        segments.push(segment.join(' '));
        segment = [];
      }
      return;
    }
    const x = xForIndex(index, points.length);
    const ratio = max === min ? 0.5 : (point.avgPrice - min) / (max - min);
    const y = chartHeight - chartPaddingY - ratio * (chartHeight - chartPaddingY * 2);
    segment.push(`${x.toFixed(1)},${y.toFixed(1)}`);
  });
  if (segment.length > 0) segments.push(segment.join(' '));
  return segments.join('  ');
}

function xForIndex(index: number, total: number) {
  if (total <= 1) return chartWidth / 2;
  return chartPaddingX + (index / (total - 1)) * (chartWidth - chartPaddingX * 2);
}

function latestTrendIndex(factories: FactoryTrendData[]) {
  const length = factories[0]?.trend?.length ?? 0;
  return Math.max(length - 1, 0);
}

function uniqueFactories(factoryNos: string[]) {
  return Array.from(new Set(factoryNos.map(item => item.trim()).filter(Boolean)));
}

function formatPriceRange(min?: number | null, max?: number | null) {
  if (min != null && max != null && min > 0 && max >= min) {
    if (min === max) return `¥${num(min)}`;
    return `¥${num(min)}-${num(max)}`;
  }
  if (min != null && min > 0) return `¥${num(min)}`;
  return '--';
}

function num(value: number) {
  return Number.isInteger(value) ? `${value}` : value.toFixed(1);
}

function formatDate(text?: string | null) {
  if (!text) return '--';
  // 后端可能返回 "2026-04-07" 或 "2026.04.07"
  return text.replace(/-/g, '.').slice(0, 10);
}

const styles = StyleSheet.create({
  screen: {flex: 1, backgroundColor: '#FFFFFF'},
  topBar: {
    paddingHorizontal: 16,
    paddingBottom: 12,
    backgroundColor: '#FFFFFF',
    flexDirection: 'row',
    alignItems: 'center',
  },
  backButton: {width: 32, height: 32, alignItems: 'center', justifyContent: 'center'},
  topBarTitle: {
    flex: 1,
    textAlign: 'center',
    color: colors.text,
    fontSize: 16,
    fontWeight: '500',
  },
  topBarPlaceholder: {width: 32},
  container: {flex: 1, backgroundColor: '#FFFFFF'},
  content: {paddingBottom: 32},

  selectorBlock: {
    paddingTop: 12,
    paddingHorizontal: 16,
    paddingBottom: 12,
    borderTopWidth: 1,
    borderTopColor: '#DEE4E1',
    borderBottomWidth: 1,
    borderBottomColor: '#DEE4E1',
    gap: 8,
    backgroundColor: '#FFFFFF',
  },
  selectorHead: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: 12,
  },
  titleText: {
    flex: 1,
    color: colors.primary,
    fontSize: 14,
    fontWeight: '500',
    lineHeight: 20,
  },
  selectedCount: {
    fontFamily: fonts.manropeRegular,
    fontSize: 14,
    color: colors.text,
  },
  selectedCountActive: {
    color: colors.primary,
    fontWeight: '500',
  },
  factoryRow: {gap: 12, paddingRight: 4},
  factoryChip: {
    minWidth: 64,
    paddingHorizontal: 12,
    paddingVertical: 8,
    borderRadius: 2,
    borderWidth: 1,
    borderColor: 'rgba(60,73,71,0.15)',
    backgroundColor: 'rgba(60,73,71,0.05)',
    alignItems: 'center',
    justifyContent: 'center',
    position: 'relative',
  },
  factoryChipActive: {
    borderColor: colors.primary,
    backgroundColor: 'rgba(0,106,97,0.05)',
  },
  factoryChipText: {color: colors.textSecondary, fontSize: 14, lineHeight: 20},
  factoryChipTextActive: {color: colors.primary},
  factoryChipTick: {
    position: 'absolute',
    top: -1,
    right: 0,
  },

  titleRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
    paddingHorizontal: 16,
    paddingTop: 12,
  },
  dateBig: {
    fontFamily: fonts.manropeSemiBold,
    color: '#000000',
    fontSize: 16,
    lineHeight: 30,
  },
  dateSmall: {
    color: colors.text,
    fontSize: 12,
    lineHeight: 15,
  },

  cardsGrid: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    paddingHorizontal: 16,
    paddingTop: 8,
    gap: 12,
  },
  priceCard: {
    width: '47.5%',
    paddingHorizontal: 16,
    paddingVertical: 12,
    borderRadius: 2,
    borderTopWidth: 2,
    gap: 8,
  },
  priceCardTitle: {
    color: colors.text,
    fontSize: 15,
    lineHeight: 20,
  },
  priceCardPriceLine: {
    flexDirection: 'row',
    alignItems: 'baseline',
  },
  priceCardPriceMain: {
    fontFamily: fonts.manropeSemiBold,
    color: colors.text,
    fontSize: 14,
    lineHeight: 18,
  },
  priceCardPriceSub: {
    fontFamily: fonts.manropeSemiBold,
    color: colors.text,
    fontSize: 14,
    lineHeight: 18,
  },
  priceCardUnit: {
    fontFamily: fonts.manropeRegular,
    color: colors.text,
    fontSize: 10,
    lineHeight: 15,
  },
  priceCardDivider: {
    height: 0.5,
    backgroundColor: '#DEE4E1',
  },
  priceCardStats: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: 12,
  },
  priceCardStatCell: {
    gap: 4,
  },
  priceCardSmall: {
    color: 'rgba(60,73,71,0.5)',
    fontSize: 10,
    lineHeight: 14,
  },
  priceCardCount: {
    fontFamily: fonts.manropeSemiBold,
    color: colors.text,
    fontSize: 14,
    lineHeight: 18,
  },

  chartCard: {
    paddingHorizontal: 16,
    paddingTop: 16,
    paddingBottom: 4,
  },
  dateRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    paddingTop: 4,
  },
  dateText: {
    color: '#9DA4A3',
    fontSize: 10,
    lineHeight: 14,
  },

  loading: {marginTop: 32},
  empty: {marginTop: 32, textAlign: 'center', color: '#9DA4A3', fontSize: 12},
});
