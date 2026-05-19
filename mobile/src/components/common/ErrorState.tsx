import React from 'react';
import {Pressable, StyleSheet, Text, View} from 'react-native';
import {colors} from '../../theme/colors';

type Props = {
  message: string;
  onRetry?: () => void;
};

export function ErrorState({message, onRetry}: Props) {
  return (
    <View style={styles.wrap}>
      <Text style={styles.title}>加载失败</Text>
      <Text style={styles.message}>{message}</Text>
      {onRetry ? (
        <Pressable onPress={onRetry} style={styles.retryButton}>
          <Text style={styles.retryText}>重试</Text>
        </Pressable>
      ) : null}
    </View>
  );
}

const styles = StyleSheet.create({
  wrap: {
    marginTop: 32,
    alignItems: 'center',
    gap: 8,
    paddingHorizontal: 16,
  },
  title: {
    color: colors.danger,
    fontSize: 14,
    fontWeight: '800',
  },
  message: {
    color: colors.textMuted,
    fontSize: 11,
    lineHeight: 18,
    textAlign: 'center',
  },
  retryButton: {
    minWidth: 88,
    height: 34,
    borderRadius: 17,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: colors.primary,
    paddingHorizontal: 16,
  },
  retryText: {
    color: '#FFFFFF',
    fontSize: 12,
    fontWeight: '800',
  },
});
