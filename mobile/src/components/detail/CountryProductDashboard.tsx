import React, {useState} from 'react';
import {Pressable, StyleSheet, Text, View} from 'react-native';import Svg, {Path} from 'react-native-svg';
import {colors} from '../../theme/colors';
import {fonts} from '../../theme/typography';
import {MiniTrendChart} from '../home/MiniTrendChart';
import type {DailyPrice} from '../../types/api';

type Props = {
  /** 标题左部分（"巴西SIF1440"） */
  country: string;
  /** 标题右部分（产品名） */
  productName: string;
  /** 是否求购 tab */
  isInquiry?: boolean;
  priceMin?: number | null;
  priceMax?: number | null;
  priceChange?: number | null;
  priceChangeRate?: number | null;
  offerCount?: number | null;
  inquiryCount?: number | null;
  merchantCount?: number | null;
  history7Days?: DailyPrice[] | null;
  history30Days?: DailyPrice[] | null;
  /** 标题中是否避免重复显示产品名（品牌+产品页用） */
  hideProductTitle?: boolean;
};

/**
 * 国家+产品 / 国家+厂号+产品 详情页 Dashboard
 * 与 Figma 1421:5845 (158:1232) / 1421:4074 (158:634) 对齐：
 * 行 1: 标题"X · Y"
 * 行 2: 左 233w：近2日报盘价格区间（RMB） + 24px Manrope 大价格 + 涨跌徽章
 *       右 82w：7日报价走势 + 28dp 迷你图表
 * 行 3 (separator)
 * 行 4: 报盘数 / 求购数 / 商家数 + 展开/收起数据 按钮
 * 行 5 (展开后): 30 日大趋势图
 */
export function CountryProductDashboard({
  country,
  productName,
  isInquiry = false,
  priceMin,
  priceMax,
  priceChange,
  priceChangeRate,
  offerCount,
  inquiryCount,
  merchantCount,
  history7Days,
  history30Days,
  hideProductTitle = false,
}: Props) {
  const [expanded, setExpanded] = useState(false);

  const priceText = formatPrice(priceMin, priceMax);
  const trend7 = (history7Days ?? [])
    .map(d => Number(d.avgPrice))
    .filter(v => Number.isFinite(v) && v > 0) as number[];
  const trend30 = (history30Days ?? [])
    .map(d => Number(d.avgPrice))
    .filter(v => Number.isFinite(v) && v > 0) as number[];
  const hasValidChange =
    priceChange != null &&
    priceChangeRate != null;

  // Fallback: if backend returns null/0 priceChange, calculate from 7-day history
  let effectiveChange = priceChange;
  let effectiveRate = priceChangeRate;
  if ((!hasValidChange || (priceChange === 0 && priceChangeRate === 0)) && trend7.length >= 2) {
    const todayAvg = trend7[trend7.length - 1];
    const yesterdayAvg = trend7[trend7.length - 2];
    if (todayAvg > 0 && yesterdayAvg > 0) {
      effectiveChange = Number((todayAvg - yesterdayAvg).toFixed(2));
      effectiveRate = Number(((effectiveChange / yesterdayAvg) * 100).toFixed(2));
    }
  }
  const showChange = effectiveChange != null && effectiveRate != null;
  const hasDuplicate =
    productName && country && (country.includes(productName) || country.endsWith(productName));

  return (
    <View style={styles.container}>
      <View style={styles.titleRow}>
        <Text style={styles.titlePart} numberOfLines={1}>{country}</Text>
        {!hideProductTitle && productName && !hasDuplicate ? (
          <>
            <View style={styles.dot} />
            <Text style={styles.titlePart} numberOfLines={1}>{productName}</Text>
          </>
        ) : null}
      </View>

      <View style={styles.midRow}>
        <View style={styles.left}>
          <Text style={styles.smallLabel}>
            近2日{isInquiry ? '求购' : '报盘'}价格区间（RMB）
          </Text>
          <View style={styles.priceLine}>
            <Text style={styles.priceValue} numberOfLines={1} adjustsFontSizeToFit>
              {priceText.value}
            </Text>
            {priceText.unit ? <Text style={styles.priceUnit}>{priceText.unit}</Text> : null}
            {showChange ? (
              <PriceChange value={effectiveChange!} rate={effectiveRate!} />
            ) : null}
          </View>
        </View>

        <View style={styles.right}>
          <Text style={styles.trendLabel}>7日报价走势</Text>
          <View style={styles.trendChart}>
            {trend7.length > 1 ? (
              <MiniTrendChart data={trend7} width={82} height={28} />
            ) : (
              <View style={styles.chartPlaceholder} />
            )}
          </View>
        </View>
      </View>

      <View style={styles.statsFooter}>
        <View style={styles.statsRow}>
          <Stat label="报盘数" value={offerCount ?? '--'} />
          <Stat label="求购数" value={inquiryCount ?? '--'} />
          <Stat label="商家数" value={merchantCount ?? '--'} />
        </View>
        <Pressable onPress={() => setExpanded(prev => !prev)} style={styles.expandButton}>
          <Text style={styles.expandText}>{expanded ? '收起数据' : '展开数据'}</Text>
          <Svg width={12} height={12} viewBox="0 0 12 12">
            <Path
              d={expanded ? 'M3 7.5L6 4.5L9 7.5' : 'M3 4.5L6 7.5L9 4.5'}
              stroke="#171D1C"
              strokeWidth={1.4}
              strokeLinecap="round"
              strokeLinejoin="round"
              fill="none"
            />
          </Svg>
        </Pressable>
      </View>

      {expanded ? <ExpandedChart data={trend30} dates={(history30Days ?? []).map(d => d.date)} /> : null}
    </View>
  );
}

function ExpandedChart({data, dates}: {data: number[]; dates: string[]}) {
  const [selectedIdx, setSelectedIdx] = useState<number | null>(null);

  if (data.length < 2) {
    return <Text style={styles.empty}>暂无 30 日趋势数据</Text>;
  }
  const length = dates.length;
  const indices = length > 4
    ? [0, Math.floor(length / 4), Math.floor(length / 2), Math.floor((length * 3) / 4), length - 1]
    : dates.map((_, i) => i);
  const labels = indices.map(i => formatShort(dates[i]));

  function handlePress(event: {nativeEvent: {locationX: number}}) {
    const x = event.nativeEvent.locationX;
    const chartWidth = 343;
    const stepX = length > 1 ? chartWidth / (length - 1) : chartWidth;
    const idx = Math.round(x / stepX);
    const clamped = Math.max(0, Math.min(length - 1, idx));
    setSelectedIdx(prev => (prev === clamped ? null : clamped));
  }

  const tooltipDate = selectedIdx != null ? formatShort(dates[selectedIdx]) : null;
  const tooltipPrice = selectedIdx != null && data[selectedIdx] != null ? data[selectedIdx] : null;

  // 计算选中点在曲线上的位置
  const chartHeight = 88;
  const padY = 4;
  const min = Math.min(...data);
  const max = Math.max(...data);
  let dotTop = chartHeight / 2;
  if (selectedIdx != null && data[selectedIdx] != null) {
    const ratio = max === min ? 0.5 : (data[selectedIdx] - min) / (max - min);
    dotTop = chartHeight - padY - ratio * (chartHeight - padY * 2);
  }

  return (
    <View style={styles.expandedWrap}>
      <Pressable onPress={handlePress} style={styles.bigChart}>
        <MiniTrendChart data={data} width={343} height={88} />
        {selectedIdx != null ? (
          <>
            {/* 垂直虚线 */}
            <View
              style={[
                styles.chartVerticalLine,
                {left: (selectedIdx / (length - 1)) * 343},
              ]}
            />
            {/* 交点圆点 */}
            <View
              style={[
                styles.chartDot,
                {left: (selectedIdx / (length - 1)) * 343 - 4, top: dotTop - 4},
              ]}
            />
          </>
        ) : null}
      </Pressable>
      {tooltipDate ? (
        <View style={styles.tooltip}>
          <Text style={styles.tooltipDate}>{tooltipDate}</Text>
          <Text style={styles.tooltipPrice}>
            日均价 ¥{tooltipPrice != null ? tooltipPrice.toFixed(1) : '--'}/kg
          </Text>
        </View>
      ) : null}
      <View style={styles.dateRow}>
        {labels.map((label, i) => (
          <Text key={`${label}-${i}`} style={styles.dateText}>{label}</Text>
        ))}
      </View>
    </View>
  );
}

function Stat({label, value}: {label: string; value: number | string}) {
  return (
    <View style={styles.statItem}>
      <Text style={styles.smallLabel}>{label}</Text>
      <Text style={styles.statValue}>{value}</Text>
    </View>
  );
}

function PriceChange({value, rate}: {value: number; rate: number}) {
  const up = value >= 0;
  const fg = up ? '#A53321' : '#0E8D41';

  return (
    <View style={styles.changeRow}>
      <Svg width={10} height={6} viewBox="0 0 10 6">
        <Path
          d={up
            ? 'M0.7 6L0 5.3L3.7 1.575L5.7 3.575L8.3 1H7V0H10V3H9V1.7L5.7 5L3.7 3L0.7 6Z'
            : 'M0.7 0L0 0.7L3.7 4.425L5.7 2.425L8.3 5H7V6H10V3H9V4.3L5.7 1L3.7 3L0.7 0Z'}
          fill={fg}
        />
      </Svg>
      <Text style={[styles.changeText, {color: fg}]} numberOfLines={1}>
        {value > 0 ? '+' : ''}{value.toFixed(2)}  {Math.abs(rate).toFixed(2)}%
      </Text>
    </View>
  );
}

function formatPrice(min?: number | null, max?: number | null) {
  if (min != null && max != null && min > 0 && max >= min) {
    if (min === max) return {value: `¥${num(min)}`, unit: '/kg'};
    return {value: `¥${num(min)}-${num(max)}`, unit: '/kg'};
  }
  if (min != null && min > 0) return {value: `¥${num(min)}`, unit: '/kg'};
  return {value: '协商报价', unit: ''};
}

function num(value: number) {
  return Number.isInteger(value) ? `${value}` : value.toFixed(1);
}

function formatShort(date?: string | null) {
  if (!date) return '--';
  const m = date.match(/(\d{2,4})[-./](\d{1,2})[-./](\d{1,2})/);
  if (m) return `${m[2].padStart(2, '0')}.${m[3].padStart(2, '0')}`;
  return date;
}

const styles = StyleSheet.create({
  container: {
    backgroundColor: '#FFFFFF',
    paddingHorizontal: 16,
    paddingTop: 12,
    paddingBottom: 0,
    borderTopWidth: 1,
    borderTopColor: colors.border,
    gap: 4,
  },
  titleRow: {flexDirection: 'row', alignItems: 'center', gap: 6},
  titlePart: {color: colors.text, fontSize: 20, fontWeight: '500', lineHeight: 30, flexShrink: 1},
  dot: {width: 4, height: 4, borderRadius: 2, backgroundColor: '#171D1C'},

  midRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: 8,
  },
  left: {
    flex: 1,
    gap: 0,
  },
  smallLabel: {color: 'rgba(60,73,71,0.5)', fontSize: 10, lineHeight: 14},
  priceLine: {flexDirection: 'row', alignItems: 'baseline', gap: 8, flexWrap: 'nowrap'},
  priceValue: {
    fontFamily: fonts.manropeBold,
    color: colors.price,
    fontSize: 24,
    lineHeight: 32,
    flexShrink: 1,
  },
  priceUnit: {
    fontFamily: fonts.manropeRegular,
    color: colors.text,
    fontSize: 12,
    lineHeight: 32,
  },
  changeRow: {flexDirection: 'row', alignItems: 'center', gap: 2, flexShrink: 0},
  changeText: {
    fontFamily: fonts.manropeSemiBold,
    fontSize: 10,
    letterSpacing: -0.25,
    lineHeight: 15,
  },

  right: {
    width: 82,
    gap: 4,
    paddingTop: 0,
  },
  trendLabel: {color: '#9DA4A3', fontSize: 10, lineHeight: 14, textAlign: 'right'},
  trendChart: {height: 28, width: 82, overflow: 'hidden'},
  chartPlaceholder: {height: 28, width: 82, backgroundColor: '#F4FBF8'},

  statsFooter: {
    paddingTop: 12,
    paddingBottom: 12,
    borderTopWidth: 1,
    borderTopColor: colors.border,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
  },
  statsRow: {flexDirection: 'row', alignItems: 'center', gap: 16, flex: 1},
  statItem: {flexDirection: 'row', alignItems: 'center', gap: 8},
  statValue: {
    fontFamily: fonts.manropeSemiBold,
    color: colors.text,
    fontSize: 16,
    lineHeight: 20,
  },
  expandButton: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 2,
    paddingHorizontal: 8,
    paddingVertical: 4,
    borderRadius: 2,
    borderWidth: 0.5,
    borderColor: 'rgba(23,29,28,0.1)',
    backgroundColor: 'rgba(23,29,28,0.05)',
  },
  expandText: {color: colors.text, fontSize: 10, lineHeight: 14},

  expandedWrap: {paddingBottom: 12, gap: 6},
  bigChart: {height: 100, position: 'relative'},
  chartDot: {
    position: 'absolute',
    width: 8,
    height: 8,
    borderRadius: 4,
    backgroundColor: colors.primary,
    borderWidth: 1.5,
    borderColor: '#FFFFFF',
  },
  chartVerticalLine: {
    position: 'absolute',
    top: 0,
    bottom: 0,
    width: 1,
    borderLeftWidth: 1,
    borderLeftColor: '#9DA4A3',
    borderStyle: 'dashed',
  },
  tooltip: {
    alignSelf: 'center',
    backgroundColor: 'rgba(0,0,0,0.8)',
    paddingHorizontal: 8,
    paddingVertical: 4,
    borderRadius: 4,
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
  },
  tooltipDate: {color: '#FFFFFF', fontSize: 11, fontWeight: '600'},
  tooltipPrice: {color: '#FFFFFF', fontSize: 11},
  dateRow: {flexDirection: 'row', justifyContent: 'space-between'},
  dateText: {color: '#9DA4A3', fontSize: 10, lineHeight: 14},
  empty: {color: '#9DA4A3', fontSize: 12, paddingVertical: 16, textAlign: 'center'},
});
