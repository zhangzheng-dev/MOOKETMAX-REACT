import React from 'react';
import {Pressable, StyleSheet, Text, View} from 'react-native';
import Svg, {Path} from 'react-native-svg';
import {useSafeAreaInsets} from 'react-native-safe-area-context';
import {InventoryTheme} from '../theme/inventoryTheme';

export type MainTabKey = 'inventory' | 'profile';
export const MAIN_TAB_BAR_BASE_HEIGHT = 54;
export const MAIN_TAB_BAR_MIN_BOTTOM_INSET = 12;

const ACTIVE_COLOR = '#09AE92';
const INACTIVE_COLOR = '#3C4947';

export function getMainTabBarHeight(bottomInset: number) {
  return MAIN_TAB_BAR_BASE_HEIGHT + Math.max(bottomInset, MAIN_TAB_BAR_MIN_BOTTOM_INSET);
}

interface Props {
  activeTab: MainTabKey;
  onChange: (tab: MainTabKey) => void;
  theme: InventoryTheme;
}

export default function MainTabBar({activeTab, onChange, theme}: Props) {
  const insets = useSafeAreaInsets();
  const bottomInset = Math.max(insets.bottom, MAIN_TAB_BAR_MIN_BOTTOM_INSET);

  return (
    <View pointerEvents="box-none" style={styles.wrap}>
      <View
        style={[
          styles.bar,
          {
            height: MAIN_TAB_BAR_BASE_HEIGHT + bottomInset,
            paddingBottom: bottomInset,
            backgroundColor: theme.panel,
            shadowColor: theme.shadow,
          },
        ]}>
        <TabItem
          active={activeTab === 'inventory'}
          label="库存"
          onPress={() => onChange('inventory')}
          icon={<DocumentTextIcon color={activeTab === 'inventory' ? ACTIVE_COLOR : INACTIVE_COLOR} />}
        />
        <TabItem
          active={activeTab === 'profile'}
          label="我的"
          onPress={() => onChange('profile')}
          icon={<UserIcon color={activeTab === 'profile' ? ACTIVE_COLOR : INACTIVE_COLOR} />}
        />
      </View>
    </View>
  );
}

function TabItem({
  active,
  icon,
  label,
  onPress,
}: {
  active: boolean;
  icon: React.ReactNode;
  label: string;
  onPress: () => void;
}) {
  return (
    <Pressable style={styles.item} onPress={onPress}>
      {icon}
      <Text style={[styles.label, active ? styles.labelActive : styles.labelInactive]}>{label}</Text>
    </Pressable>
  );
}

function DocumentTextIcon({color}: {color: string}) {
  return (
    <Svg width={24} height={24} viewBox="0 0 24 24" fill="none">
      <Path
        d="M21 7V17C21 20 19.5 22 16 22H8C4.5 22 3 20 3 17V7C3 4 4.5 2 8 2H16C19.5 2 21 4 21 7Z"
        stroke={color}
        strokeWidth={1.5}
        strokeLinecap="round"
        strokeLinejoin="round"
      />
      <Path
        d="M14.5 4.5V6.5C14.5 7.6 15.4 8.5 16.5 8.5H18.5"
        stroke={color}
        strokeWidth={1.5}
        strokeLinecap="round"
        strokeLinejoin="round"
        opacity={0.4}
      />
      <Path d="M8 13H12" stroke={color} strokeWidth={1.5} strokeLinecap="round" strokeLinejoin="round" opacity={0.4} />
      <Path d="M8 17H16" stroke={color} strokeWidth={1.5} strokeLinecap="round" strokeLinejoin="round" opacity={0.4} />
    </Svg>
  );
}

function UserIcon({color}: {color: string}) {
  return (
    <Svg width={24} height={24} viewBox="0 0 24 24" fill="none">
      <Path
        d="M12 12C14.7614 12 17 9.76142 17 7C17 4.23858 14.7614 2 12 2C9.23858 2 7 4.23858 7 7C7 9.76142 9.23858 12 12 12Z"
        stroke={color}
        strokeWidth={1.5}
        strokeLinecap="round"
        strokeLinejoin="round"
      />
      <Path
        d="M20.59 22C20.59 18.13 16.74 15 12 15C7.26 15 3.41 18.13 3.41 22"
        stroke={color}
        strokeWidth={1.5}
        strokeLinecap="round"
        strokeLinejoin="round"
        opacity={0.4}
      />
    </Svg>
  );
}

const styles = StyleSheet.create({
  wrap: {
    position: 'absolute',
    left: 0,
    right: 0,
    bottom: 0,
  },
  bar: {
    borderTopLeftRadius: 12,
    borderTopRightRadius: 12,
    flexDirection: 'row',
    justifyContent: 'space-evenly',
    alignItems: 'center',
    paddingTop: 6,
    paddingHorizontal: 24,
    shadowOpacity: 0.1,
    shadowRadius: 2,
    shadowOffset: {width: 0, height: -3},
    elevation: 10,
  },
  item: {
    flex: 1,
    paddingVertical: 4,
    alignItems: 'center',
    justifyContent: 'center',
    gap: 2,
  },
  label: {
    fontSize: 10,
    lineHeight: 14,
    letterSpacing: 0,
    fontWeight: '500',
  },
  labelActive: {
    color: ACTIVE_COLOR,
  },
  labelInactive: {
    color: INACTIVE_COLOR,
  },
});
