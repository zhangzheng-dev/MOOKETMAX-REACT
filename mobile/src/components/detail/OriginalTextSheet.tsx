import React, {useEffect, useMemo, useRef, useState} from 'react';
import {LayoutChangeEvent, Linking, Modal, Pressable, ScrollView, StyleSheet, Text, View} from 'react-native';
import {useSafeAreaInsets} from 'react-native-safe-area-context';
import {colors} from '../../theme/colors';
import {analyzeOriginalText} from '../../utils/originalText';

type Props = {
  visible: boolean;
  text: string;
  keywords?: string[];
  onClose: () => void;
  title?: string;
};

export function OriginalTextSheet({
  visible,
  text,
  keywords = [],
  onClose,
  title = '查看原文',
}: Props) {
  const insets = useSafeAreaInsets();
  const scrollRef = useRef<ScrollView | null>(null);
  const [segmentLayouts, setSegmentLayouts] = useState<Record<number, number>>({});
  const analysis = useMemo(() => analyzeOriginalText(text, keywords), [keywords, text]);

  useEffect(() => {
    if (!visible) {
      setSegmentLayouts({});
    }
  }, [visible]);

  useEffect(() => {
    if (!visible || analysis.bestSegmentIndex < 0) return;
    const y = segmentLayouts[analysis.bestSegmentIndex];
    if (typeof y === 'number') {
      scrollRef.current?.scrollTo({y: Math.max(0, y - 12), animated: true});
    }
  }, [analysis.bestSegmentIndex, segmentLayouts, visible]);

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
            ref={scrollRef}
            style={styles.scroll}
            contentContainerStyle={styles.content}
            showsVerticalScrollIndicator
            nestedScrollEnabled
            keyboardShouldPersistTaps="handled">
            {text ? (
              analysis.segments.length > 0 ? (
                analysis.segments.map((segment, index) => (
                  <Text
                    key={`${index}-${segment.slice(0, 12)}`}
                    style={styles.text}
                    onLayout={(event: LayoutChangeEvent) => {
                      const y = event.nativeEvent.layout.y;
                      setSegmentLayouts(prev => (prev[index] === y ? prev : {...prev, [index]: y}));
                    }}>
                    {renderTextWithPhones(segment)}
                  </Text>
                ))
              ) : (
                <Text style={styles.text}>{renderTextWithPhones(text)}</Text>
              )
            ) : (
              <Text style={[styles.text, styles.textMuted]}>抱歉，暂时无原文。</Text>
            )}
          </ScrollView>
        </View>
      </View>
    </Modal>
  );
}

function renderTextWithPhones(text: string) {
  return text.split(/(1\d{10})/g).map((part, index) => {
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
    marginBottom: 6,
  },
  phoneText: {
    color: colors.primary,
    textDecorationLine: 'underline',
  },
  textMuted: {
    color: '#9DA4A3',
  },
});
