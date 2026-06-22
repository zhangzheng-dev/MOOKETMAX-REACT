import React from 'react';
import { Pressable, StyleSheet, Text, View } from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { SvgXml } from 'react-native-svg';
import { colors } from '../../theme/colors';
import type { MerchantDetail } from '../../types/api';
import { backArrowXml, merchantBuildingXml } from './productIcons';

type Props = {
  merchant: MerchantDetail | null;
  onBack: () => void;
  rightAction?: React.ReactNode;
};

export function MerchantHeader({ merchant, onBack, rightAction }: Props) {
  const insets = useSafeAreaInsets();
  const tagText = merchant?.merchantTags?.split('|')?.[0] ?? '';

  return (
    <View
      style={[
        styles.bar,
        { paddingTop: insets.top + 12, minHeight: insets.top + 48 },
      ]}
    >
      <Pressable hitSlop={8} onPress={onBack} style={styles.backButton}>
        <SvgXml xml={backArrowXml} width={24} height={24} />
      </Pressable>

      <View style={styles.titleWrap}>
        <SvgXml xml={merchantBuildingXml} width={22} height={21} />
        {merchant ? (
          <>
            <Text style={styles.name} numberOfLines={1}>
              {merchant.merchantShortName || merchant.merchantName}
            </Text>
            {tagText ? (
              <View style={styles.tagBadge}>
                <Text style={styles.tagBadgeText} numberOfLines={1}>
                  {tagText}
                </Text>
              </View>
            ) : null}
          </>
        ) : null}
      </View>
      {rightAction ? (
        <View style={styles.rightAction}>{rightAction}</View>
      ) : null}
    </View>
  );
}

const styles = StyleSheet.create({
  bar: {
    backgroundColor: '#FFFFFF',
    paddingHorizontal: 16,
    paddingBottom: 12,
    flexDirection: 'row',
    alignItems: 'center',
    gap: 4,
  },
  backButton: {
    width: 24,
    height: 24,
    alignItems: 'center',
    justifyContent: 'center',
  },
  titleWrap: {
    flex: 1,
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
    minWidth: 0,
  },
  name: {
    color: colors.text,
    fontSize: 20,
    fontWeight: '500',
    lineHeight: 30,
    flexShrink: 1,
  },
  tagBadge: {
    height: 20,
    paddingHorizontal: 7,
    borderRadius: 2,
    backgroundColor: '#244C56',
    alignItems: 'center',
    justifyContent: 'center',
  },
  tagBadgeText: {
    color: '#F2FFFD',
    fontSize: 11,
    lineHeight: 16,
    maxWidth: 80,
  },
  rightAction: {
    width: 36,
    height: 36,
    alignItems: 'center',
    justifyContent: 'center',
  },
});
