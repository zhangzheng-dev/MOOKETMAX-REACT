import React, {useMemo, useState} from 'react';
import {Pressable, StyleSheet, Text, View} from 'react-native';
import Svg, {Circle, Line, Polyline} from 'react-native-svg';
import {colors} from '../../theme/colors';
import type {DailyPrice} from '../../types/api';

type RangeKey = '7d' | '30d';

type Props = {
  history7Days?: DailyPrice[] | null;
  history30Days?: DailyPrice[] | null;
};

const chartWidth = 300;
const chartHeight = 118;
const chartPaddingX = 18;
const chartPaddingY = 16;

export function PriceTrendCard({history7Days, history30Days}: Props) {
  const [range, setRange] = useState<RangeKey>('7d');
  const points = useMemo(
    () => normalizePoints(range === '7d' ? history7Days : history30Days),
    [history30Days, history7Days, range],
  );

  if (points.length === 0) {
    return null;
  }

  const prices = points.map(item => item.price);
  const min = Math.min(...prices);
  const max = Math.max(...prices);
  const latest = points[points.length - 1];
  const pointString = points
    .map((item, index) => {
      const x =
        chartPaddingX +
        (points.length === 1 ? 0.5 : index / (points.length - 1)) * (chartWidth - chartPaddingX * 2);
      const ratio = max === min ? 0.5 : (item.price - min) / (max - min);
      const y = chartHeight - chartPaddingY - ratio * (chartHeight - chartPaddingY * 2);
      return `${x},${y}`;
    })
    .join(' ');

  return (
    <View style={styles.card}>
      <View style={styles.head}>
        <View>
          <Text style={styles.title}>价格走势</Text>
          <Text style={styles.subtitle}>最新 {formatPrice(latest.price)}</Text>
        </View>
        <View style={styles.rangeSwitch}>
          <RangeChip active={range === '7d'} label="7日" onPress={() => setRange('7d')} />
          <RangeChip active={range === '30d'} label="30日" onPress={() => setRange('30d')} />
        </View>
      </View>

      <View style={styles.chartBox}>
        <Svg width="100%" height={chartHeight} viewBox={`0 0 ${chartWidth} ${chartHeight}`}>
          <Line
            x1={chartPaddingX}
            y1={chartHeight - chartPaddingY}
            x2={chartWidth - chartPaddingX}
            y2={chartHeight - chartPaddingY}
            stroke={colors.border}
            strokeWidth={1}
          />
          <Line
            x1={chartPaddingX}
            y1={chartPaddingY}
            x2={chartWidth - chartPaddingX}
            y2={chartPaddingY}
            stroke={colors.border}
            strokeWidth={1}
            strokeDasharray="4 4"
          />
          <Polyline points={pointString} fill="none" stroke={colors.primary} strokeWidth={2.4} />
          {points.map((item, index) => {
            const x =
              chartPaddingX +
              (points.length === 1 ? 0.5 : index / (points.length - 1)) * (chartWidth - chartPaddingX * 2);
            const ratio = max === min ? 0.5 : (item.price - min) / (max - min);
            const y = chartHeight - chartPaddingY - ratio * (chartHeight - chartPaddingY * 2);
            const showPoint = points.length <= 10 || index === points.length - 1 || index === 0;
            return showPoint ? (
              <Circle key={`${item.date}-${index}`} cx={x} cy={y} r={3.2} fill={colors.primary} />
            ) : null;
          })}
        </Svg>
      </View>

      <View style={styles.stats}>
        <Text style={styles.statText}>最低 {formatPrice(min)}</Text>
        <Text style={styles.statText}>最高 {formatPrice(max)}</Text>
        <Text style={styles.statText}>{latest.date}</Text>
      </View>
    </View>
  );
}

function RangeChip({
  active,
  label,
  onPress,
}: {
  active: boolean;
  label: string;
  onPress: () => void;
}) {
  return (
    <Pressable onPress={onPress} style={[styles.rangeChip, active && styles.rangeChipActive]}>
      <Text style={[styles.rangeText, active && styles.rangeTextActive]}>{label}</Text>
    </Pressable>
  );
}

function normalizePoints(items?: DailyPrice[] | null) {
  return (items ?? [])
    .map(item => ({
      date: item.date || item.fullDate || '--',
      price: Number(item.avgPrice),
    }))
    .filter(item => Number.isFinite(item.price) && item.price > 0);
}

function formatPrice(value: number) {
  return `¥${value.toFixed(value >= 100 ? 0 : 1)}`;
}

const styles = StyleSheet.create({
  card: {
    gap: 10,
    borderRadius: 4,
    borderWidth: 1,
    borderColor: colors.border,
    backgroundColor: colors.surface,
    padding: 12,
  },
  head: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: 12,
  },
  title: {
    color: colors.text,
    fontSize: 15,
    fontWeight: '800',
  },
  subtitle: {
    marginTop: 3,
    color: colors.textMuted,
    fontSize: 11,
  },
  rangeSwitch: {
    flexDirection: 'row',
    gap: 6,
  },
  rangeChip: {
    minWidth: 44,
    height: 28,
    borderRadius: 4,
    alignItems: 'center',
    justifyContent: 'center',
    borderWidth: 1,
    borderColor: colors.border,
    backgroundColor: colors.surfaceMuted,
    paddingHorizontal: 8,
  },
  rangeChipActive: {
    borderColor: colors.primary,
    backgroundColor: colors.primaryLight,
  },
  rangeText: {
    color: colors.textMuted,
    fontSize: 11,
    fontWeight: '700',
  },
  rangeTextActive: {
    color: colors.primary,
  },
  chartBox: {
    overflow: 'hidden',
  },
  stats: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    gap: 8,
  },
  statText: {
    color: colors.textSecondary,
    fontSize: 10,
    fontWeight: '700',
  },
});
