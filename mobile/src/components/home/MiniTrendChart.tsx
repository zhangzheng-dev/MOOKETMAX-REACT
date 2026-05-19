import React from 'react';
import {View} from 'react-native';
import Svg, {Polyline} from 'react-native-svg';
import {colors} from '../../theme/colors';

type Props = {
  data: number[];
  width?: number;
  height?: number;
  color?: string;
};

/**
 * 卡片中的迷你价格趋势线（30 日）
 */
export function MiniTrendChart({data, width = 280, height = 46, color = colors.primary}: Props) {
  if (data.length < 2) {
    return <View style={{height}} />;
  }
  const min = Math.min(...data);
  const max = Math.max(...data);
  const padX = 4;
  const padY = 4;
  const points = data
    .map((value, index) => {
      const x = padX + (index / (data.length - 1)) * (width - padX * 2);
      const ratio = max === min ? 0.5 : (value - min) / (max - min);
      const y = height - padY - ratio * (height - padY * 2);
      return `${x.toFixed(1)},${y.toFixed(1)}`;
    })
    .join(' ');
  return (
    <Svg width="100%" height={height} viewBox={`0 0 ${width} ${height}`} preserveAspectRatio="none">
      <Polyline points={points} fill="none" stroke={color} strokeWidth={2} strokeLinecap="round" strokeLinejoin="round" />
    </Svg>
  );
}
