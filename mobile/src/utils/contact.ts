import {Alert, Linking, Platform, ToastAndroid} from 'react-native';

export function notify(message: string) {
  if (Platform.OS === 'android') {
    ToastAndroid.show(message, ToastAndroid.SHORT);
  } else {
    Alert.alert('', message);
  }
}

function loadClipboard() {
  // Metro in this project resolves the built JS entry more reliably than the
  // package root when bundling release builds.
  // eslint-disable-next-line @typescript-eslint/no-require-imports
  const ClipboardModule = require('@react-native-clipboard/clipboard/dist/index.js');
  return ClipboardModule?.default ?? ClipboardModule;
}

export async function copyToClipboard(text: string, hint = '已复制手机号') {
  if (!text) {
    return;
  }

  try {
    const clipboard = loadClipboard();
    if (!clipboard?.setString) {
      throw new Error('Clipboard unavailable');
    }
    clipboard.setString(text);
    notify(hint);
  } catch {
    Alert.alert('请手动复制', text);
  }
}

export async function dialPhone(phone?: string | null) {
  const value = (phone ?? '').trim();
  if (!value) {
    notify('暂无联系方式');
    return;
  }

  const url = `tel:${value}`;
  try {
    await Linking.openURL(url);
  } catch {
    notify('拨号失败');
  }
}
