import {Alert, Linking, Platform, Share, ToastAndroid} from 'react-native';

/**
 * 提示信息（Android Toast / iOS Alert）
 */
export function notify(message: string) {
  if (Platform.OS === 'android') {
    ToastAndroid.show(message, ToastAndroid.SHORT);
  } else {
    Alert.alert('', message);
  }
}

export async function copyToClipboard(text: string, hint = '已复制') {
  if (!text) {
    return;
  }

  if (Platform.OS === 'ios') {
    try {
      await Share.share({message: text});
    } catch {
      Alert.alert('请手动复制', text);
    }
    return;
  }

  try {
    // Metro in this project resolves the package's built JS entry more reliably
    // than the package root when bundling release builds.
    // eslint-disable-next-line @typescript-eslint/no-require-imports
    const ClipboardModule = require('@react-native-clipboard/clipboard/dist/index.js');
    const clipboard = ClipboardModule?.default ?? ClipboardModule;
    if (!clipboard?.setString) {
      throw new Error('Clipboard unavailable');
    }
    clipboard.setString(text);
    notify(hint);
  } catch {
    Alert.alert('复制失败', text);
  }
}

/**
 * 拨打电话（弹出系统拨号面板）
 */
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
