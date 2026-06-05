import React from 'react';
import {Linking, Modal, Pressable, ScrollView, StyleSheet, Text, View} from 'react-native';
import {useSafeAreaInsets} from 'react-native-safe-area-context';
import {colors} from '../../theme/colors';

type Props = {
  visible: boolean;
  text: string;
  onClose: () => void;
  title?: string;
};

export function OriginalTextSheet({visible, text, onClose, title = '原文内容'}: Props) {
  const insets = useSafeAreaInsets();

  return (
    <Modal visible={visible} transparent animationType="slide" onRequestClose={onClose} statusBarTranslucent>
      <View style={styles.overlay}>
        <Pressable style={styles.backdrop} onPress={onClose} />
        <View style={[styles.sheet, {paddingBottom: Math.max(insets.bottom, 24)}]}>
          <View style={styles.handle} />
          <View style={styles.titleRow}>
            <Text style={styles.title}>{title}</Text>
            <Pressable hitSlop={8} onPress={onClose}>
              <Text style={styles.close}>关闭</Text>
            </Pressable>
          </View>
          <ScrollView
            style={styles.scroll}
            contentContainerStyle={styles.content}
            showsVerticalScrollIndicator
            nestedScrollEnabled
            keyboardShouldPersistTaps="handled">
            {text ? (
              <Text style={styles.text}>{renderTextWithPhones(text)}</Text>
            ) : (
              <Text style={[styles.text, styles.textMuted]}>抱歉，暂无原文！</Text>
            )}
          </ScrollView>
        </View>
      </View>
    </Modal>
  );
}

function renderTextWithPhones(text: string) {
  const parts = text.split(/(1\d{10})/g);
  return parts.map((part, index) => {
    if (/^1\d{10}$/.test(part)) {
      return (
        <Text key={`${part}-${index}`} style={styles.phoneText} onPress={() => void dialPhone(part)}>
          {part}
        </Text>
      );
    }
    return <Text key={`${index}`}>{part}</Text>;
  });
}

async function dialPhone(phone: string) {
  const url = `tel:${phone}`;
  const supported = await Linking.canOpenURL(url);
  if (supported) {
    await Linking.openURL(url);
  }
}

const styles = StyleSheet.create({
  overlay: {
    flex: 1,
    justifyContent: 'flex-end',
  },
  backdrop: {
    ...StyleSheet.absoluteFillObject,
    backgroundColor: 'rgba(0,0,0,0.35)',
  },
  sheet: {
    maxHeight: '80%',
    borderTopLeftRadius: 12,
    borderTopRightRadius: 12,
    backgroundColor: '#FFFFFF',
    paddingTop: 8,
  },
  handle: {
    alignSelf: 'center',
    width: 32,
    height: 4,
    borderRadius: 2,
    backgroundColor: '#DEE4E1',
    marginBottom: 8,
  },
  titleRow: {
    paddingHorizontal: 20,
    paddingBottom: 12,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
  },
  title: {
    color: colors.text,
    fontSize: 18,
    fontWeight: '600',
  },
  close: {
    color: colors.primary,
    fontSize: 13,
    fontWeight: '600',
  },
  scroll: {
    flexGrow: 0,
  },
  content: {
    paddingHorizontal: 20,
    paddingBottom: 24,
  },
  text: {
    color: '#3C4947',
    fontSize: 14,
    lineHeight: 22,
  },
  phoneText: {
    color: colors.primary,
    textDecorationLine: 'underline',
  },
  textMuted: {
    color: '#9DA4A3',
  },
});
