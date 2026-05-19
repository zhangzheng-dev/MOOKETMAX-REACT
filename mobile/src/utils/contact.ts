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

/**
 * 兼容 RN 0.85 已移除内建 Clipboard 的环境：
 * - 优先尝试动态加载 @react-native-clipboard/clipboard（如已安装）
 * - 否则使用 Share 让用户自行复制（保证功能可用）
 */
export async function copyToClipboard(text: string, hint = '已复制') {
  if (!text) {
    return;
  }
  try {
    // eslint-disable-next-line @typescript-eslint/no-require-imports
    const ClipboardModule = require('@react-native-clipboard/clipboard');
    const clipboard = ClipboardModule?.default ?? ClipboardModule;
    if (clipboard?.setString) {
      clipboard.setString(text);
      notify(hint);
      return;
    }
  } catch {
    // 模块未安装，回退到 Share
  }
  try {
    await Share.share({message: text});
  } catch {
    Alert.alert('复制', text);
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
