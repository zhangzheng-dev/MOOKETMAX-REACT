import React from 'react';
import {Pressable, StyleSheet, Text, View} from 'react-native';
import {SvgXml} from 'react-native-svg';
import {colors} from '../../theme/colors';
import {backArrowXml, searchIconXml, tagCloseXml} from './productIcons';

type Tag = {
  text: string;
  onClose: () => void;
};

type Props = {
  onBack: () => void;
  tags: Tag[];
};

/**
 * 详情页顶部栏（Figma node-id 158:174）
 * - 高 48dp（py:12 + 24px back icon）
 * - 4dp gap，含返回 + 类搜索框（绿底 chip + 搜索图标）
 */
export function DetailTopBar({onBack, tags}: Props) {
  return (
    <View style={styles.bar}>
      <Pressable onPress={onBack} hitSlop={8} style={styles.backButton}>
        <SvgXml xml={backArrowXml} width={24} height={24} />
      </Pressable>
      <View style={styles.searchBox}>
        <View style={styles.tagsRow}>
          {tags.map((tag, index) => (
            <SearchTag key={`${tag.text}-${index}`} text={tag.text} onPress={tag.onClose} />
          ))}
        </View>
        <View style={styles.searchIcon}>
          <SvgXml xml={searchIconXml} width={16} height={16} />
        </View>
      </View>
    </View>
  );
}

function SearchTag({text, onPress}: {text: string; onPress: () => void}) {
  return (
    <Pressable onPress={onPress} style={styles.tag}>
      <Text style={styles.tagText} numberOfLines={1}>
        {text}
      </Text>
      <View style={styles.tagClose}>
        <SvgXml xml={tagCloseXml} width={8} height={8} />
      </View>
    </Pressable>
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
  searchBox: {
    flex: 1,
    height: 28,
    paddingLeft: 7,
    paddingRight: 13,
    paddingVertical: 7,
    borderRadius: 4,
    borderWidth: 1,
    borderColor: 'rgba(187,202,198,0.3)',
    backgroundColor: '#EFF5F3',
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
  },
  tagsRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 6,
    flex: 1,
  },
  tag: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 6,
    height: 28,
    paddingHorizontal: 8,
    paddingVertical: 4,
    borderRadius: 2,
    backgroundColor: colors.primary,
  },
  tagText: {
    color: '#FFFFFF',
    fontSize: 12,
    fontWeight: '500',
    lineHeight: 18,
    maxWidth: 120,
  },
  tagClose: {
    width: 8,
    height: 8,
    alignItems: 'center',
    justifyContent: 'center',
  },
  searchIcon: {
    width: 16,
    height: 16,
    alignItems: 'center',
    justifyContent: 'center',
  },
});
