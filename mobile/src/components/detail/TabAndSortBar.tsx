import React from 'react';
import {
  Pressable,
  StyleSheet,
  Text,
  View,
} from 'react-native';
import {SvgXml} from 'react-native-svg';
import {colors} from '../../theme/colors';
import {sortArrowsAsc, sortArrowsDefault, sortArrowsDesc} from './productIcons';

export type OfferTab = 'offer' | 'inquiry';
export type SortOrder = 'none' | 'asc' | 'desc';
export type SortMode =
  | {kind: 'comprehensive'}
  | {kind: 'publishTime'}
  | {kind: 'price'; order: SortOrder};

type Props = {
  tab: OfferTab;
  onTabChange: (next: OfferTab) => void;
  sort: SortMode;
  onSortChange: (next: SortMode) => void;
  showRecommend?: boolean;
  showPublishTime?: boolean;
  offerLabel?: string;
  inquiryLabel?: string;
};

export function TabAndSortBar({
  tab,
  onTabChange,
  sort,
  onSortChange,
  showRecommend = true,
  showPublishTime = false,
  offerLabel = '报盘',
  inquiryLabel = '求购',
}: Props) {
  const priceOrder = sort.kind === 'price' ? sort.order : 'none';

  function togglePrice() {
    if (sort.kind !== 'price') {
      onSortChange({kind: 'price', order: 'asc'});
      return;
    }
    const next: SortOrder =
      sort.order === 'asc' ? 'desc' : sort.order === 'desc' ? 'none' : 'asc';
    onSortChange(next === 'none' ? {kind: 'comprehensive'} : {kind: 'price', order: next});
  }

  const arrowsXml =
    priceOrder === 'asc'
      ? sortArrowsAsc()
      : priceOrder === 'desc'
        ? sortArrowsDesc()
        : sortArrowsDefault();

  return (
    <View style={styles.bar}>
      <View style={styles.left}>
        <TabItem text={offerLabel} active={tab === 'offer'} onPress={() => onTabChange('offer')} />
        <TabItem
          text={inquiryLabel}
          active={tab === 'inquiry'}
          onPress={() => onTabChange('inquiry')}
        />
      </View>
      <View style={styles.right}>
        {showRecommend ? (
          <SortItem
            text="综合推荐"
            active={sort.kind === 'comprehensive'}
            onPress={() => onSortChange({kind: 'comprehensive'})}
          />
        ) : null}
        {showPublishTime ? (
          <SortItem
            text="发布时间"
            active={sort.kind === 'publishTime'}
            onPress={() => onSortChange({kind: 'publishTime'})}
          />
        ) : null}
        <SortItem
          text="价格"
          active={sort.kind === 'price' && priceOrder !== 'none'}
          onPress={togglePrice}
          rightSlot={<SvgXml xml={arrowsXml} width={6} height={12} />}
        />
      </View>
    </View>
  );
}

function TabItem({text, active, onPress}: {text: string; active: boolean; onPress: () => void}) {
  return (
    <Pressable onPress={onPress} style={styles.item}>
      <Text style={[styles.text, active && styles.textActive]}>{text}</Text>
      <View style={[styles.indicator, active && styles.indicatorActive]} />
    </Pressable>
  );
}

function SortItem({
  text,
  active,
  onPress,
  rightSlot,
}: {
  text: string;
  active: boolean;
  onPress: () => void;
  rightSlot?: React.ReactNode;
}) {
  return (
    <Pressable onPress={onPress} style={styles.item}>
      <View style={styles.labelRow}>
        <Text style={[styles.text, active && styles.textActive]}>{text}</Text>
        {rightSlot ? <View style={styles.iconWrap}>{rightSlot}</View> : null}
      </View>
      <View style={[styles.indicator, active && styles.indicatorActive]} />
    </Pressable>
  );
}

const styles = StyleSheet.create({
  bar: {
    minHeight: 44,
    backgroundColor: '#FFFFFF',
    paddingHorizontal: 16,
    paddingTop: 6,
    paddingBottom: 4,
    flexDirection: 'row',
    alignItems: 'flex-end',
    justifyContent: 'space-between',
    borderBottomWidth: 1,
    borderBottomColor: colors.border,
  },
  left: {
    flexDirection: 'row',
    alignItems: 'flex-end',
    gap: 20,
    flexShrink: 0,
  },
  right: {
    flexDirection: 'row',
    alignItems: 'flex-end',
    gap: 16,
    flexShrink: 1,
    justifyContent: 'flex-end',
  },
  item: {
    alignItems: 'center',
    justifyContent: 'flex-end',
    gap: 2,
  },
  labelRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 3,
  },
  iconWrap: {
    width: 12,
    height: 12,
    alignItems: 'center',
    justifyContent: 'center',
  },
  text: {
    color: '#3C4947',
    fontSize: 14,
    lineHeight: 20,
    fontWeight: '400',
    textAlign: 'center',
  },
  textActive: {
    color: colors.text,
    fontWeight: '600',
  },
  indicator: {
    width: 18,
    height: 3,
    backgroundColor: 'transparent',
  },
  indicatorActive: {
    backgroundColor: colors.primary,
  },
});
