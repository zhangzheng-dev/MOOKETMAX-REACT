import React from 'react';
import Svg, {Circle, Path} from 'react-native-svg';

type IconProps = {
  size?: number;
  color?: string;
};

export function SearchIcon({size = 18, color = '#006A61'}: IconProps) {
  return (
    <Svg width={size} height={size} viewBox="0 0 24 24" fill="none">
      <Path
        d="M11 19C15.4183 19 19 15.4183 19 11C19 6.58172 15.4183 3 11 3C6.58172 3 3 6.58172 3 11C3 15.4183 6.58172 19 11 19Z"
        stroke={color}
        strokeWidth={1.8}
      />
      <Path d="M21 21L16.65 16.65" stroke={color} strokeWidth={1.8} strokeLinecap="round" />
    </Svg>
  );
}

export function ChevronDownIcon({size = 16, color = '#171D1C'}: IconProps) {
  return (
    <Svg width={size} height={size} viewBox="0 0 24 24" fill="none">
      <Path d="M7 10L12 15L17 10" stroke={color} strokeWidth={1.8} strokeLinecap="round" strokeLinejoin="round" />
    </Svg>
  );
}

export function InventoryIcon({size = 24, color = '#006A61'}: IconProps) {
  return (
    <Svg width={size} height={size} viewBox="0 0 24 24" fill="none">
      <Path
        d="M21 7V17C21 20 19.5 22 16 22H8C4.5 22 3 20 3 17V7C3 4 4.5 2 8 2H16C19.5 2 21 4 21 7Z"
        stroke={color}
        strokeWidth={1.6}
        strokeLinecap="round"
        strokeLinejoin="round"
      />
      <Path
        d="M14.5 4.5V6.5C14.5 7.6 15.4 8.5 16.5 8.5H18.5"
        stroke={color}
        strokeWidth={1.6}
        strokeLinecap="round"
        strokeLinejoin="round"
        opacity={0.45}
      />
      <Path d="M8 13H12" stroke={color} strokeWidth={1.6} strokeLinecap="round" opacity={0.45} />
      <Path d="M8 17H16" stroke={color} strokeWidth={1.6} strokeLinecap="round" opacity={0.45} />
    </Svg>
  );
}

export function ProfileIcon({size = 24, color = '#171D1C'}: IconProps) {
  return (
    <Svg width={size} height={size} viewBox="0 0 24 24" fill="none">
      <Path
        d="M12 12C14.7614 12 17 9.76142 17 7C17 4.23858 14.7614 2 12 2C9.23858 2 7 4.23858 7 7C7 9.76142 9.23858 12 12 12Z"
        stroke={color}
        strokeWidth={1.6}
      />
      <Path
        d="M20.59 22C20.59 18.13 16.74 15 12 15C7.26 15 3.41 18.13 3.41 22"
        stroke={color}
        strokeWidth={1.6}
        opacity={0.45}
      />
    </Svg>
  );
}

export function HistoryIcon({size = 14, color = '#6B7A78'}: IconProps) {
  return (
    <Svg width={size} height={size} viewBox="0 0 24 24" fill="none">
      <Path d="M12 8V12L14.5 14.5" stroke={color} strokeWidth={1.8} strokeLinecap="round" strokeLinejoin="round" />
      <Path
        d="M3.05 11C3.54 6.5 7.36 3 12 3C16.97 3 21 7.03 21 12C21 16.97 16.97 21 12 21C8.73 21 5.86 19.25 4.29 16.63"
        stroke={color}
        strokeWidth={1.8}
        strokeLinecap="round"
        strokeLinejoin="round"
      />
      <Path d="M3 4V8H7" stroke={color} strokeWidth={1.8} strokeLinecap="round" strokeLinejoin="round" />
    </Svg>
  );
}

export function DeleteIcon({size = 14, color = '#9DA4A3'}: IconProps) {
  return (
    <Svg width={size} height={size} viewBox="0 0 24 24" fill="none">
      <Path d="M4 7H20" stroke={color} strokeWidth={1.8} strokeLinecap="round" />
      <Path d="M10 11V17" stroke={color} strokeWidth={1.8} strokeLinecap="round" />
      <Path d="M14 11V17" stroke={color} strokeWidth={1.8} strokeLinecap="round" />
      <Path
        d="M6 7L7 19C7.08 20.01 7.92 20.8 8.94 20.8H15.06C16.08 20.8 16.92 20.01 17 19L18 7"
        stroke={color}
        strokeWidth={1.8}
        strokeLinecap="round"
        strokeLinejoin="round"
      />
      <Path d="M9 7V5.5C9 4.67 9.67 4 10.5 4H13.5C14.33 4 15 4.67 15 5.5V7" stroke={color} strokeWidth={1.8} />
    </Svg>
  );
}

export function StarIcon({size = 16, color = '#006A61'}: IconProps) {
  return (
    <Svg width={size} height={size} viewBox="0 0 24 24" fill="none">
      <Path
        d="M12 3.8L14.53 8.93L20.19 9.75L16.09 13.74L17.06 19.37L12 16.71L6.94 19.37L7.91 13.74L3.81 9.75L9.47 8.93L12 3.8Z"
        fill={color}
      />
    </Svg>
  );
}

export function ClockIcon({size = 16, color = '#3C4947'}: IconProps) {
  return (
    <Svg width={size} height={size} viewBox="0 0 24 24" fill="none">
      <Circle cx={12} cy={12} r={8} stroke={color} strokeWidth={1.8} />
      <Path d="M12 8V12L14.5 13.5" stroke={color} strokeWidth={1.8} strokeLinecap="round" strokeLinejoin="round" />
    </Svg>
  );
}
