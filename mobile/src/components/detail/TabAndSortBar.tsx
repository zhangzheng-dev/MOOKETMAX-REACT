import React from 'react';
import {Pressable, StyleSheet, Text, View} from 'react-native';
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
  /** 是否显示"综合推荐"按钮（产品页/商家页都需要） */
  showRecommend?: boolean;
  /** 是否显示"发布时间"按钮（详情页 1421:4191 显示） */
  showPublishTime?: boolean;
  /** 报盘 / 求购 文案，部分页面用"近2日报盘" */
  offerLabel?: string;
  inquiryLabel?: string;
};

/**
 * 顶部 Tab + 排序条（与 Figma node-id 1437:3442 对齐）：
 * - 高 40dp，下边线 1px
 * - 左：报盘 / 求购，激活态 18x3 主色指示条
 * - 右：综合推荐 + 价格（双箭头 6x12）
 */
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
    const next: SortOrder = sort.order === 'asc' ? 'desc' : sort.order === 'desc' ? 'none' : 'asc';
    onSortChange(next === 'none' ? {kind: 'comprehensive'} : {kind: 'price', order: next});
  }

  const arrowsXml =
    priceOrder === 'asc' ? sortArrowsAsc() : priceOrder === 'desc' ? sortArrowsDesc() : sortArrowsDefault();

  return (
    <View style={styles.bar}>
      <View style={styles.left}>
        <TabItem text={offerLabel} active={tab === 'offer'} onPress={() => onTabChange('offer')} />
        <TabItem text={inquiryLabel} active={tab === 'inquiry'} onPress={() => onTabChange('inquiry')} />
      </View>
      <View style={styles.right}>
        {showRecommend ? (
          <Pressable onPress={() => onSortChange({kind: 'comprehensive'})}>
            <Text style={[styles.sortText, sort.kind === 'comprehensive' && styles.sortTextActive]}>
              综合推荐
            </Text>
          </Pressable>
        ) : null}
        {showPublishTime ? (
          <Pressable onPress={() => onSortChange({kind: 'publishTime'})}>
            <Text style={[styles.sortText, sort.kind === 'publishTime' && styles.sortTextActive]}>
              发布时间
            </Text>
          </Pressable>
        ) : null}
        <Pressable onPress={togglePrice} style={styles.priceWrap}>
          <Text
            style={[
              styles.sortText,
              sort.kind === 'price' && priceOrder !== 'none' && styles.sortTextActive,
            ]}>
            价格
          </Text>
          <SvgXml xml={arrowsXml} width={6} height={12} />
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
  left: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 28,
  },
  right: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 28,
  },
  tabItem: {
    alignItems: 'center',
    gap: 2,
  },
  tabText: {
    color: '#3C4947',
    fontSize: 14,
    lineHeight: 20,
    fontWeight: '400',
    textAlign: 'center',
  },
  tabTextActive: {
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
  sortText: {
    color: '#3C4947',
    fontSize: 14,
    lineHeight: 20,
    fontWeight: '400',
    textAlign: 'center',
  },
  sortTextActive: {
    color: colors.text,
    fontWeight: '600',
  },
  priceWrap: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 3,
  },
});
