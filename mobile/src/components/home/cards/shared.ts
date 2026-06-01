import type {ViewStyle} from 'react-native';
import {StyleSheet} from 'react-native';
import {colors} from '../../../theme/colors';
import {fonts} from '../../../theme/typography';

/**
 * 与 Figma 一致：8px 圆角 + 1px 浅边框 + 浅阴影 + 12/8 内边距
 */
export const cardBaseStyle: ViewStyle = {
  borderRadius: 8,
  borderWidth: 1,
  borderColor: '#E3EAE7',
  backgroundColor: '#FFFFFF',
  paddingHorizontal: 12,
  paddingVertical: 8,
  shadowColor: '#000',
  shadowOpacity: 0.02,
  shadowOffset: {width: 0, height: 4},
  shadowRadius: 6,
  elevation: 1,
};

export const sharedStyles = StyleSheet.create({
  divider: {
    height: 1,
    backgroundColor: 'rgba(0,0,0,0.04)',
    marginVertical: 0,
  },
  smallLabel: {
    color: 'rgba(60,73,71,0.5)',
    fontSize: 10,
    lineHeight: 14,
  },
  bigStat: {
    fontFamily: fonts.manropeBold,
    color: colors.text,
    fontSize: 20,
    lineHeight: 26,
  },
  midStat: {
    fontFamily: fonts.manropeSemiBold,
    color: colors.text,
    fontSize: 16,
    lineHeight: 20,
  },
  title: {
    color: colors.text,
    fontSize: 16,
    fontWeight: '500',
    lineHeight: 28,
    flex: 1,
  },
  titleRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 6,
  },
  rowBetween: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
  },
  bottomRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
  },
  // 用 Manrope 显示价格 ¥xxx /kg
  priceMain: {
    fontFamily: fonts.manropeSemiBold,
    color: colors.price,
    fontSize: 14,
    lineHeight: 18,
  },
  priceSecondary: {
    fontFamily: fonts.manropeSemiBold,
    color: colors.text,
    fontSize: 14,
    lineHeight: 18,
  },
  priceUnit: {
    fontFamily: fonts.manropeRegular,
    color: colors.text,
    fontSize: 10,
    lineHeight: 15,
  },
});

export function formatThousand(value?: number | null): string {
  if (value == null) return '--';
  if (value >= 10000) {
    return `${(value / 1000).toFixed(1)}k`;
  }
  if (value >= 1000) {
    const major = Math.floor(value / 1000);
    const minor = Math.floor((value % 1000) / 100);
    return `${major}.${minor}k`;
  }
  return `${value}`;
}

export function formatPriceRange(min?: number | null, max?: number | null): string {
  if (min != null && max != null) {
    if (min === max) return `¥${formatPriceNum(min)}`;
    return `¥${formatPriceNum(min)}-${formatPriceNum(max)}`;
  }
  if (min != null) return `¥${formatPriceNum(min)}`;
  return '--';
}

export function formatPriceNum(value: number): string {
  return Number.isInteger(value) ? `${value}` : value.toFixed(1);
}

export function priceChangePalette(change?: number | null) {
  if (change == null || change === 0) {
    return {bg: 'rgba(227,234,231,0.1)', fg: '#9DA4A3'};
  }
  if (change > 0) {
    return {bg: 'rgba(230,127,90,0.12)', fg: '#A53321'};
  }
  return {bg: 'rgba(71,187,88,0.1)', fg: '#0E8D41'};
}

/**
 * Figma 排名色板：1 棕、2 蓝灰、3 棕黄
 */
export function rankPalette(rank?: number | null) {
  switch (rank) {
    case 1:
      return {bg: '#FFF9F0', fg: '#906134', borderInner: '#F0DFC0'};
    case 2:
      return {bg: '#F5F8FF', fg: '#4B5462', borderInner: '#D8DBE3'};
    case 3:
      return {bg: '#FFF9F8', fg: '#80521E', borderInner: '#F0D6BD'};
    default:
      return {bg: '#F3F6F5', fg: '#9DA4A3', borderInner: '#E3EAE7'};
  }
}
