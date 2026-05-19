import {Platform} from 'react-native';

/**
 * Figma 设计中数字使用 Manrope 字体（已 link 到 Android assets/fonts）。
 * iOS 字体名通过 PostScript name 引用，Android 用 ttf 文件名。
 */
const isIos = Platform.OS === 'ios';

export const fonts = {
  manropeRegular: isIos ? 'Manrope-Regular' : 'Manrope-Regular',
  manropeSemiBold: isIos ? 'Manrope-SemiBold' : 'Manrope-SemiBold',
  manropeBold: isIos ? 'Manrope-Bold' : 'Manrope-Bold',
};
