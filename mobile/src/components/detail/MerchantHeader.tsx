import React from 'react';
import {Pressable, StyleSheet, Text, View} from 'react-native';
import {SvgXml} from 'react-native-svg';
import {colors} from '../../theme/colors';
import type {MerchantDetail} from '../../types/api';
import {backArrowXml, merchantBuildingXml} from './productIcons';

type Props = {
  merchant: MerchantDetail | null;
  onBack: () => void;
};

/**
 * 商家详情页 Header（与 Figma 1421:5195 对齐）
 * 24px 返回 + 24px 商家 logo + 商家名 20px + 知名商家标签 + 右侧占位搜索图标
 */
export function MerchantHeader({merchant, onBack}: Props) {
  const isFamous = merchant?.merchantTags?.includes('知名商家');
  const tagText = merchant?.merchantTags?.split('|')?.[0] ?? '';

  return (
    <View style={styles.bar}>
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
    </View>
  );
}

const styles = StyleSheet.create({
  bar: {
    height: 48,
    backgroundColor: '#FFFFFF',
    paddingHorizontal: 16,
    paddingVertical: 12,
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
  tagBadgeText: {color: '#F2FFFD', fontSize: 11, lineHeight: 16, maxWidth: 80},
});
