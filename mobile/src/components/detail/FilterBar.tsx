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
  toggle?: boolean;
};

type Props = {
  filters: FilterDef[];
  active: FilterKey | null;
  onPress: (key: FilterKey) => void;
};

export function FilterBar({filters, active, onPress}: Props) {
  return (
    <View style={styles.wrap}>
      <ScrollView horizontal showsHorizontalScrollIndicator={false} contentContainerStyle={styles.row}>
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
  if (famous) {
    return (
      <Pressable
        onPress={onPress}
        style={[
          styles.chip,
          styles.famousChip,
          selected ? styles.famousActive : styles.famousIdle,
          active && styles.chipPressed,
        ]}>
        <Text style={[styles.famousText, {color: selected ? '#FFFFFF' : '#254D5A'}]} numberOfLines={1}>
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
          <Path
            d="M2.5 4L5 6.5L7.5 4"
            stroke={selected ? colors.primary : '#3C4947'}
            strokeWidth={1.2}
            strokeLinecap="round"
            strokeLinejoin="round"
            fill="none"
          />
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
    minHeight: 30,
    paddingHorizontal: 8,
    paddingVertical: 7,
    borderRadius: 2,
    borderWidth: 1,
    backgroundColor: '#F3F6F5',
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    gap: 3,
  },
  famousChip: {
    paddingHorizontal: 10,
  },
  chipPressed: {
    opacity: 0.7,
  },
  chipText: {
    fontSize: 12,
    lineHeight: 16,
    includeFontPadding: false,
    textAlignVertical: 'center',
  },
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
    lineHeight: 16,
    fontWeight: '500',
    includeFontPadding: false,
    textAlignVertical: 'center',
  },
});
