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
import type {OfferTab, SortOrder} from './TabAndSortBar';

export type MerchantSortKey = 'comprehensive' | 'publish_time' | 'price';
export type MerchantSortMode =
  | {kind: 'comprehensive'}
  | {kind: 'publish_time'}
  | {kind: 'price'; order: SortOrder};

type Props = {
  tab: OfferTab;
  onTabChange: (next: OfferTab) => void;
  sort: MerchantSortMode;
  onSortChange: (next: MerchantSortMode) => void;
  hideInquiry?: boolean;
  showTabs?: boolean;
};

export function MerchantSortBar({
  tab,
  onTabChange,
  sort,
  onSortChange,
  hideInquiry = false,
  showTabs = true,
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
      {showTabs ? (
        <View style={styles.left}>
          <TabItem text="报盘" active={tab === 'offer'} onPress={() => onTabChange('offer')} />
          {!hideInquiry ? (
            <TabItem text="求购" active={tab === 'inquiry'} onPress={() => onTabChange('inquiry')} />
          ) : null}
        </View>
      ) : null}
      <View style={[styles.right, !showTabs && styles.rightOnly]}>
        <Pressable onPress={() => onSortChange({kind: 'comprehensive'})}>
          <Text style={[styles.sortText, sort.kind === 'comprehensive' && styles.sortTextActive]}>
            综合推荐
          </Text>
        </Pressable>
        <Pressable onPress={() => onSortChange({kind: 'publish_time'})}>
          <Text style={[styles.sortText, sort.kind === 'publish_time' && styles.sortTextActive]}>
            发布时间
          </Text>
        </Pressable>
        <Pressable onPress={togglePrice} style={styles.priceWrap}>
          <Text
            style={[
              styles.sortText,
              sort.kind === 'price' && priceOrder !== 'none' && styles.sortTextActive,
            ]}>
            价格
          </Text>
          <View style={styles.iconWrap}>
            <SvgXml xml={arrowsXml} width={6} height={12} />
          </View>
        </Pressable>
      </View>
    </View>
  );
}

function TabItem({text, active, onPress}: {text: string; active: boolean; onPress: () => void}) {
  return (
    <Pressable onPress={onPress} style={styles.tabItem}>
      <Text style={[styles.tabText, active && styles.tabTextActive]}>{text}</Text>
      <View style={[styles.indicator, active && styles.indicatorActive]} />
    </Pressable>
  );
}

const styles = StyleSheet.create({
  bar: {
    minHeight: 40,
    backgroundColor: '#FFFFFF',
    paddingHorizontal: 16,
    paddingVertical: 6,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    borderBottomWidth: 1,
    borderBottomColor: colors.border,
  },
  left: {flexDirection: 'row', alignItems: 'center', gap: 28},
  right: {flexDirection: 'row', alignItems: 'center', gap: 28},
  rightOnly: {flex: 1, justifyContent: 'flex-end'},
  tabItem: {alignItems: 'center', gap: 2},
  tabText: {color: '#3C4947', fontSize: 14, lineHeight: 20, textAlign: 'center'},
  tabTextActive: {color: colors.text, fontWeight: '600'},
  indicator: {width: 18, height: 3, backgroundColor: 'transparent'},
  indicatorActive: {backgroundColor: colors.primary},
  sortText: {color: '#3C4947', fontSize: 14, lineHeight: 20, textAlign: 'center'},
  sortTextActive: {color: colors.text, fontWeight: '600'},
  priceWrap: {flexDirection: 'row', alignItems: 'center', gap: 3},
  iconWrap: {width: 12, height: 12, alignItems: 'center', justifyContent: 'center'},
});
