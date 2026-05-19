import React from 'react';
import {Pressable, ScrollView, StyleSheet, Text, View} from 'react-native';
import Svg, {Path} from 'react-native-svg';
import {colors} from '../../theme/colors';

export type FilterKey =
  | 'famousMerchant'
  | 'merchant'
  | 'countryFactory'
  | 'region'
  | 'product'
  | 'priceRange'
  | 'goodsType'
  | 'feedingMethod'
  | 'tag';

export type FilterDef = {
  key: FilterKey;
  label: string;
  hasSelection: boolean;
  /** "famousMerchant" 是切换型 chip，无下拉箭头 */
  toggle?: boolean;
};

type Props = {
  filters: FilterDef[];
  active: FilterKey | null;
  onPress: (key: FilterKey) => void;
};

/**
 * 筛选条（与 Figma 1421:4211 对齐）
 * 7 个 chip 横向滚动：
 * 知名商家(toggle) / 商家筛选 / 地区 / 价格区间 / 货物类型 / 饲养方式 / 标签
 */
export function FilterBar({filters, active, onPress}: Props) {
  return (
    <View style={styles.wrap}>
      <ScrollView
        horizontal
        showsHorizontalScrollIndicator={false}
        contentContainerStyle={styles.row}>
        {filters.map(item => (
          <FilterChip
            key={item.key}
            label={item.label}
            selected={item.hasSelection}
            active={active === item.key}
            toggle={item.toggle}
            famous={item.key === 'famousMerchant'}
            onPress={() => onPress(item.key)}
          />
        ))}
      </ScrollView>
    </View>
  );
}

function FilterChip({
  label,
  selected,
  active,
  toggle,
  famous,
  onPress,
}: {
  label: string;
  selected: boolean;
  active: boolean;
  toggle?: boolean;
  famous?: boolean;
  onPress: () => void;
}) {
  // 知名商家：选中时主色背景白字、未选灰底深字（用 HelloFont 风格字体替代用 medium）
  if (famous) {
    return (
      <Pressable
        onPress={onPress}
        style={[
          styles.chip,
          selected ? styles.famousActive : styles.famousIdle,
          active && styles.chipPressed,
        ]}>
        <Text
          style={[
            styles.famousText,
            {color: selected ? '#FFFFFF' : '#254D5A'},
          ]}
          numberOfLines={1}>
          {label}
        </Text>
      </Pressable>
    );
  }

  const borderColor = selected ? colors.primary : 'transparent';
  const textColor = selected ? colors.primary : '#3C4947';
  return (
    <Pressable onPress={onPress} style={[styles.chip, {borderColor}, active && styles.chipPressed]}>
      <Text style={[styles.chipText, {color: textColor}]} numberOfLines={1}>
        {label}
      </Text>
      {!toggle ? (
        <Svg width={10} height={10} viewBox="0 0 10 10">
          <Path d="M2.5 4L5 6.5L7.5 4" stroke={selected ? colors.primary : '#3C4947'} strokeWidth={1.2} strokeLinecap="round" strokeLinejoin="round" fill="none"/>
        </Svg>
      ) : null}
    </Pressable>
  );
}

const styles = StyleSheet.create({
  wrap: {
    backgroundColor: '#FFFFFF',
  },
  row: {
    paddingHorizontal: 16,
    paddingVertical: 6,
    flexDirection: 'row',
    gap: 6,
    alignItems: 'center',
  },
  chip: {
    flexDirection: 'row',
    alignItems: 'center',
    height: 27,
    paddingHorizontal: 8,
    paddingVertical: 6,
    borderRadius: 2,
    borderWidth: 1,
    backgroundColor: '#F3F6F5',
    gap: 3,
  },
  chipPressed: {opacity: 0.7},
  chipText: {fontSize: 12, lineHeight: 15},
  // 知名商家
  famousIdle: {
    borderColor: 'transparent',
    backgroundColor: '#F3F6F5',
  },
  famousActive: {
    borderColor: '#254D5A',
    backgroundColor: '#254D5A',
  },
  famousText: {
    fontSize: 12,
    lineHeight: 15,
    fontWeight: '500',
  },
});
