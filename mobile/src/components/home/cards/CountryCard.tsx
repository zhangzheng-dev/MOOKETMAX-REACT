import React from 'react';
import {Pressable, StyleSheet, Text, View} from 'react-native';
import {colors} from '../../../theme/colors';
import type {HomeCardItem} from '../../../types/api';
import {asText} from '../../../utils/format';
import {getCountryFlag} from '../../../utils/country';
import {cardBaseStyle, sharedStyles} from './shared';

type Props = {card: HomeCardItem; onPress?: () => void; onLongPress?: () => void};

/**
 * 国家卡（按 Figma 263:2481 对齐）：国旗 + 国家名 + 双列热门厂号/热门产品（不显示今日报盘数）
 */
export function CountryCard({card, onPress, onLongPress}: Props) {
  const flag = getCountryFlag(card.country);
  const factories = (card.hotFactories ?? []).slice(0, 3) as Array<Record<string, unknown>>;
  const products = (card.hotProducts ?? []).slice(0, 3) as Array<Record<string, unknown>>;

  return (
    <Pressable
      disabled={!onPress && !onLongPress}
      delayLongPress={250}
      onLongPress={onLongPress}
      onPress={onPress}
      style={({pressed}) => [styles.card, pressed && styles.pressed]}>
      <View style={styles.titleWrap}>
        {flag ? <Text style={styles.flag}>{flag}</Text> : null}
        <Text style={styles.titleText} numberOfLines={1}>
          {card.countryAlias ?? card.country ?? '--'}
        </Text>
      </View>

      <View style={styles.tableRow}>
        <View style={styles.col}>
          <Text style={sharedStyles.smallLabel}>热门厂号</Text>
          {[0, 1, 2].map(index => (
            <Text key={index} style={styles.cellText} numberOfLines={1}>
              {asText(factories[index]?.factoryNo) || '--'}
            </Text>
          ))}
        </View>
        <View style={styles.colDivider} />
        <View style={styles.col}>
          <Text style={sharedStyles.smallLabel}>热门产品</Text>
          {[0, 1, 2].map(index => (
            <Text key={index} style={styles.cellText} numberOfLines={1}>
              {asText(products[index]?.productName) || '--'}
            </Text>
          ))}
        </View>
      </View>
    </Pressable>
  );
}

const styles = StyleSheet.create({
  card: {
    ...cardBaseStyle,
    gap: 8,
    backgroundColor: '#F8FBFF',
  },
  pressed: {opacity: 0.85},
  titleWrap: {flexDirection: 'row', alignItems: 'center', gap: 6},
  flag: {fontSize: 20, lineHeight: 28},
  titleText: {color: colors.text, fontSize: 16, lineHeight: 28, fontWeight: '500', flex: 1},
  tableRow: {flexDirection: 'row', gap: 8, paddingTop: 0},
  col: {flex: 1, gap: 4},
  colDivider: {width: 1, alignSelf: 'stretch', backgroundColor: 'rgba(0,0,0,0.04)'},
  cellText: {color: colors.text, fontSize: 11, lineHeight: 18},
});
