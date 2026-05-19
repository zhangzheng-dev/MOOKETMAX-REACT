import React from 'react';
import {
  ActivityIndicator,
  FlatList,
  Pressable,
  RefreshControl,
  StyleSheet,
  Text,
  View,
} from 'react-native';
import {ErrorState} from '../common/ErrorState';
import {colors} from '../../theme/colors';

type Metric = {
  label: string;
  value: string | number | null | undefined;
};

type Row = {
  id: string;
  title: string;
  subtitle?: string;
  meta?: string;
  onPress?: () => void;
};

type Props = {
  title: string;
  subtitle?: string;
  metrics: Metric[];
  rows: Row[];
  loading: boolean;
  refreshing: boolean;
  onRefresh: () => void;
  error?: string | null;
  headerExtra?: React.ReactNode;
  onEndReached?: () => void;
  loadingMore?: boolean;
};

export type DetailSort = 'comprehensive' | 'price_asc' | 'price_desc';
export type DetailType = 'offer' | 'inquiry';

export function DetailScaffold({
  title,
  subtitle,
  metrics,
  rows,
  loading,
  refreshing,
  onRefresh,
  error,
  headerExtra,
  onEndReached,
  loadingMore,
}: Props) {
  return (
    <FlatList
      style={styles.container}
      contentContainerStyle={styles.content}
      data={rows}
      keyExtractor={item => item.id}
      refreshControl={<RefreshControl refreshing={refreshing} onRefresh={onRefresh} />}
      ListHeaderComponent={
        <View style={styles.headerPanel}>
          <Text style={styles.title}>{title}</Text>
          {subtitle ? <Text style={styles.subtitle}>{subtitle}</Text> : null}
          <View style={styles.metricGrid}>
            {metrics.map(item => (
              <View key={item.label} style={styles.metric}>
                <Text style={styles.metricValue} numberOfLines={1}>
                  {item.value ?? '--'}
                </Text>
                <Text style={styles.metricLabel}>{item.label}</Text>
              </View>
            ))}
          </View>
          {headerExtra ? <View style={styles.headerExtra}>{headerExtra}</View> : null}
        </View>
      }
      renderItem={({item}) => <DetailRow item={item} />}
      ListEmptyComponent={
        loading ? (
          <ActivityIndicator color={colors.primary} style={styles.loading} />
        ) : error ? (
          <ErrorState message={error} onRetry={onRefresh} />
        ) : (
          <View style={styles.emptyState}>
            <Text style={styles.emptyTitle}>暂无数据</Text>
            <Text style={styles.emptyMessage}>下拉刷新试试</Text>
          </View>
        )
      }
      onEndReached={onEndReached}
      onEndReachedThreshold={0.35}
      ListFooterComponent={
        loadingMore ? <ActivityIndicator color={colors.primary} style={styles.footerLoading} /> : null
      }
    />
  );
}

function DetailRow({item}: {item: Row}) {
  const content = (
    <>
      <View style={styles.rowText}>
        <Text style={styles.rowTitle} numberOfLines={1}>
          {item.title}
        </Text>
        {item.subtitle ? (
          <Text style={styles.rowSubtitle} numberOfLines={1}>
            {item.subtitle}
          </Text>
        ) : null}
        {item.meta ? (
          <Text style={styles.rowMeta} numberOfLines={1}>
            {item.meta}
          </Text>
        ) : null}
      </View>
      {item.onPress ? <Text style={styles.rowArrow}>›</Text> : null}
    </>
  );

  if (item.onPress) {
    return (
      <Pressable
        onPress={item.onPress}
        android_ripple={{color: colors.primaryLight}}
        style={({pressed}) => [styles.row, pressed && styles.rowPressed]}>
        {content}
      </Pressable>
    );
  }

  return <View style={styles.row}>{content}</View>;
}

export function DetailListControls({
  type,
  sort,
  onTypeChange,
  onSortChange,
}: {
  type: DetailType;
  sort: DetailSort;
  onTypeChange: (type: DetailType) => void;
  onSortChange: (sort: DetailSort) => void;
}) {
  const nextPriceSort =
    sort === 'price_asc' ? 'price_desc' : sort === 'price_desc' ? 'comprehensive' : 'price_asc';

  return (
    <View style={styles.controls}>
      <View style={styles.segmentGroup}>
        <ControlChip active={type === 'offer'} label="报盘" onPress={() => onTypeChange('offer')} />
        <ControlChip active={type === 'inquiry'} label="求购" onPress={() => onTypeChange('inquiry')} />
      </View>
      <View style={styles.segmentGroup}>
        <ControlChip active={sort === 'comprehensive'} label="综合" onPress={() => onSortChange('comprehensive')} />
        <ControlChip
          active={sort === 'price_asc' || sort === 'price_desc'}
          label={sort === 'price_asc' ? '价格↑' : sort === 'price_desc' ? '价格↓' : '价格'}
          onPress={() => onSortChange(nextPriceSort)}
        />
      </View>
    </View>
  );
}

function ControlChip({active, label, onPress}: {active: boolean; label: string; onPress: () => void}) {
  return (
    <Pressable onPress={onPress} style={[styles.controlChip, active && styles.controlChipActive]}>
      <Text style={[styles.controlText, active && styles.controlTextActive]}>{label}</Text>
    </Pressable>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: colors.background,
  },
  content: {
    padding: 16,
    paddingTop: 12,
    gap: 10,
  },
  headerPanel: {
    gap: 10,
    backgroundColor: '#FFFFFF',
    padding: 14,
    borderRadius: 4,
    borderWidth: 1,
    borderColor: colors.border,
  },
  title: {
    fontSize: 20,
    color: colors.text,
    fontWeight: '700',
  },
  subtitle: {
    fontSize: 12,
    color: colors.textSecondary,
  },
  metricGrid: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: 8,
  },
  metric: {
    width: '48%',
    borderRadius: 4,
    paddingHorizontal: 12,
    paddingVertical: 9,
    backgroundColor: '#FFFFFF',
    borderWidth: 1,
    borderColor: colors.border,
  },
  metricValue: {
    color: colors.text,
    fontSize: 15,
    fontWeight: '800',
  },
  metricLabel: {
    marginTop: 4,
    color: colors.textMuted,
    fontSize: 11,
  },
  headerExtra: {
    paddingTop: 2,
  },
  row: {
    borderRadius: 4,
    paddingHorizontal: 12,
    paddingVertical: 10,
    minHeight: 62,
    flexDirection: 'row',
    alignItems: 'center',
    gap: 10,
    backgroundColor: '#FFFFFF',
    borderWidth: 1,
    borderColor: colors.border,
  },
  rowPressed: {
    opacity: 0.86,
  },
  rowText: {
    flex: 1,
    minWidth: 0,
  },
  rowTitle: {
    color: colors.text,
    fontSize: 14,
    fontWeight: '600',
  },
  rowSubtitle: {
    marginTop: 4,
    color: colors.textSecondary,
    fontSize: 11,
  },
  rowMeta: {
    marginTop: 5,
    color: colors.primary,
    fontSize: 11,
    fontWeight: '700',
  },
  rowArrow: {
    color: colors.textMuted,
    fontSize: 18,
    lineHeight: 18,
  },
  loading: {
    marginTop: 32,
  },
  emptyState: {
    marginTop: 32,
    alignItems: 'center',
    gap: 6,
  },
  emptyTitle: {
    color: colors.textSecondary,
    fontSize: 14,
    fontWeight: '700',
  },
  emptyMessage: {
    color: colors.textMuted,
    fontSize: 11,
  },
  footerLoading: {
    paddingVertical: 16,
  },
  controls: {
    gap: 8,
  },
  segmentGroup: {
    flexDirection: 'row',
    gap: 8,
  },
  controlChip: {
    flex: 1,
    height: 34,
    borderRadius: 4,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: '#FFFFFF',
    borderWidth: 1,
    borderColor: colors.border,
  },
  controlChipActive: {
    borderColor: colors.primary,
    backgroundColor: colors.primaryLight,
  },
  controlText: {
    color: colors.textMuted,
    fontSize: 12,
    fontWeight: '600',
  },
  controlTextActive: {
    color: colors.primary,
  },
});
