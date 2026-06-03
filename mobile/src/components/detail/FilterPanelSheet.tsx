import React from 'react';
import {Keyboard, Modal, Pressable, ScrollView, StyleSheet, Text, View} from 'react-native';
import {colors} from '../../theme/colors';

type Props = {
  visible: boolean;
  title: string;
  onClose: () => void;
  onReset?: () => void;
  onConfirm?: () => void;
  /** 自定义内容（最多占 60% 屏高） */
  children: React.ReactNode;
};

/**
 * 通用底部筛选抽屉（5 类筛选共用）
 */
export function FilterPanelSheet({visible, title, onClose, onReset, onConfirm, children}: Props) {
  return (
    <Modal visible={visible} transparent animationType="slide" onRequestClose={onClose}>
      <Pressable style={styles.backdrop} onPress={onClose}>
        <Pressable style={styles.sheet} onPress={() => {}}>
          <View style={styles.titleRow}>
            <Text style={styles.title}>{title}</Text>
          </View>
          <ScrollView
            style={styles.scroll}
            contentContainerStyle={styles.content}
            keyboardShouldPersistTaps="always">
            {children}
          </ScrollView>
          <View style={styles.actions}>
            <Pressable style={[styles.button, styles.resetButton]} onPress={onReset}>
              <Text style={styles.resetText}>重置</Text>
            </Pressable>
            <Pressable style={[styles.button, styles.confirmButton]} onPress={onConfirm ?? onClose}>
              <Text style={styles.confirmText}>确定</Text>
            </Pressable>
          </View>
        </Pressable>
      </Pressable>
    </Modal>
  );
}

/** 多选 chip 列表 */
export function MultiSelectChips({
  options,
  selected,
  onToggle,
}: {
  options: string[];
  selected: Set<string>;
  onToggle: (option: string) => void;
}) {
  if (options.length === 0) {
    return <Text style={chipStyles.empty}>暂无可选项</Text>;
  }
  return (
    <View style={chipStyles.wrap}>
      {options.map(option => {
        const active = selected.has(option);
        return (
          <Pressable
            key={option}
            onPress={() => {
              Keyboard.dismiss();
              onToggle(option);
            }}
            style={[chipStyles.chip, active && chipStyles.chipActive]}>
            <Text style={[chipStyles.text, active && chipStyles.textActive]} numberOfLines={1}>
              {option}
            </Text>
          </Pressable>
        );
      })}
    </View>
  );
}

const styles = StyleSheet.create({
  backdrop: {
    flex: 1,
    justifyContent: 'flex-end',
    backgroundColor: 'rgba(0,0,0,0.35)',
  },
  sheet: {
    maxHeight: '70%',
    borderTopLeftRadius: 12,
    borderTopRightRadius: 12,
    backgroundColor: '#FFFFFF',
    paddingBottom: 12,
  },
  titleRow: {
    paddingHorizontal: 20,
    paddingTop: 16,
    paddingBottom: 12,
  },
  title: {
    color: colors.text,
    fontSize: 16,
    fontWeight: '600',
  },
  scroll: {
    flexGrow: 0,
  },
  content: {
    paddingHorizontal: 20,
    paddingBottom: 16,
  },
  actions: {
    paddingHorizontal: 20,
    paddingTop: 8,
    flexDirection: 'row',
    gap: 12,
  },
  button: {
    flex: 1,
    height: 44,
    borderRadius: 4,
    alignItems: 'center',
    justifyContent: 'center',
  },
  resetButton: {
    backgroundColor: '#F3F6F5',
  },
  resetText: {
    color: colors.text,
    fontSize: 14,
    fontWeight: '500',
  },
  confirmButton: {
    backgroundColor: colors.primary,
  },
  confirmText: {
    color: '#FFFFFF',
    fontSize: 14,
    fontWeight: '600',
  },
});

const chipStyles = StyleSheet.create({
  wrap: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: 8,
  },
  chip: {
    paddingHorizontal: 12,
    paddingVertical: 6,
    minHeight: 32,
    borderRadius: 4,
    borderWidth: 1,
    borderColor: colors.border,
    backgroundColor: '#FFFFFF',
    alignItems: 'center',
    justifyContent: 'center',
  },
  chipActive: {
    borderColor: colors.primary,
    backgroundColor: colors.primaryLight,
  },
  text: {
    color: colors.text,
    fontSize: 12,
  },
  textActive: {
    color: colors.primary,
    fontWeight: '600',
  },
  empty: {
    color: '#9DA4A3',
    fontSize: 12,
    textAlign: 'center',
    paddingVertical: 24,
  },
});
