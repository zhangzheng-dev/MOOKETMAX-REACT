import React from 'react';
import {Pressable, StyleSheet, Text, View} from 'react-native';
import {useSafeAreaInsets} from 'react-native-safe-area-context';
import {SvgXml} from 'react-native-svg';
import {colors} from '../../theme/colors';
import {backArrowXml, searchIconXml, tagCloseXml} from './productIcons';

type Tag = {
  text: string;
  onClose: () => void;
  onPress?: () => void;
};

type Props = {
  onBack: () => void;
  tags: Tag[];
  onSearchPress?: () => void;
};

/**
 * 详情页顶部栏（Figma node-id 158:174）
 * - 高 48dp（py:12 + 24px back icon）
 * - 4dp gap，含返回 + 类搜索框（绿底 chip + 搜索图标）
 */
export function DetailTopBar({onBack, tags, onSearchPress}: Props) {
  const insets = useSafeAreaInsets();

  return (
    <View style={[styles.bar, {paddingTop: insets.top + 12, minHeight: insets.top + 48}]}>
      <Pressable onPress={onBack} hitSlop={8} style={styles.backButton}>
        <SvgXml xml={backArrowXml} width={24} height={24} />
      </Pressable>
      <Pressable onPress={onSearchPress} style={styles.searchBox}>
        <View style={styles.tagsRow}>
          {tags.map((tag, index) => (
            <SearchTag key={`${tag.text}-${index}`} text={tag.text} onPress={tag.onClose} />
          ))}
        </View>
        <View style={styles.searchIcon}>
          <SvgXml xml={searchIconXml} width={16} height={16} />
        </View>
      </Pressable>
    </View>
  );
}

function SearchTag({text, onPress}: {text: string; onPress: () => void}) {
  return (
    <Pressable
      onPress={event => {
        event.stopPropagation();
        onPress();
      }}
      style={styles.tag}>
      <Text style={styles.tagText} numberOfLines={1}>
        {text}
      </Text>
      <Pressable
        hitSlop={8}
        onPress={event => {
          event.stopPropagation();
          onPress();
        }}
        style={styles.tagClose}>
        <SvgXml xml={tagCloseXml} width={8} height={8} />
      </Pressable>
    </Pressable>
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
    minWidth: 0,
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
    flexShrink: 1,
    maxWidth: '100%',
  },
  tagText: {
    color: '#FFFFFF',
    fontSize: 12,
    fontWeight: '500',
    lineHeight: 18,
    flexShrink: 1,
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
    marginLeft: 8,
    flexShrink: 0,
    alignItems: 'center',
    justifyContent: 'center',
  },
});
