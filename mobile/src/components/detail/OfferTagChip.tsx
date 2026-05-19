import React from 'react';
import {StyleSheet, Text, View} from 'react-native';
import Svg, {Path} from 'react-native-svg';
import {colors} from '../../theme/colors';

type Props = {
  text: string;
  /** location: 带定位图标，使用主色 */
  variant?: 'location' | 'plain' | 'colored';
  bg?: string;
  fg?: string;
};

/** 报盘列表中的彩色小标签 */
export function OfferTagChip({text, variant = 'plain', bg, fg}: Props) {
  const finalBg = bg ?? (variant === 'location' ? '#F2F8F7' : '#F3F6F5');
  const finalFg = fg ?? (variant === 'location' ? colors.primary : '#3C4947');

  return (
    <View style={[styles.chip, {backgroundColor: finalBg}]}>
      {variant === 'location' ? <LocationIcon color={finalFg} /> : null}
      <Text style={[styles.text, {color: finalFg}]} numberOfLines={1}>
        {text}
      </Text>
    </View>
  );
}

function LocationIcon({color}: {color: string}) {
  return (
    <Svg width={10} height={10} viewBox="0 0 24 24" fill="none">
      <Path
        d="M12 22s7-7.5 7-13A7 7 0 105 9c0 5.5 7 13 7 13z"
        stroke={color}
        strokeWidth={1.6}
        strokeLinejoin="round"
        fill="none"
      />
      <Path
        d="M12 11.5a2.5 2.5 0 100-5 2.5 2.5 0 000 5z"
        stroke={color}
        strokeWidth={1.6}
        fill="none"
      />
    </Svg>
  );
}

const styles = StyleSheet.create({
  chip: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 2,
    paddingHorizontal: 4,
    paddingVertical: 2,
    borderRadius: 1,
  },
  text: {
    fontSize: 10,
  },
});
