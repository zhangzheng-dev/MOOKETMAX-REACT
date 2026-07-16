import React from 'react';
import {
  Pressable,
  StyleSheet,
  Text,
  View,
  type ViewStyle,
} from 'react-native';
import {SvgXml} from 'react-native-svg';
import {colors} from '../../theme/colors';
import {sortArrowsAsc, sortArrowsDefault, sortArrowsDesc} from './productIcons';

export type OfferTab = 'offer' | 'inquiry';
export type SortOrder = 'none' | 'asc' | 'desc';
export type SortMode =
  | {kind: 'comprehensive'}
  | {kind: 'publishTime'}
  | {kind: 'price'; order: SortOrder};

type Props = {
  tab: OfferTab;
  onTabChange: (next: OfferTab) => void;
  sort: SortMode;
  onSortChange: (next: SortMode) => void;
  showTabs?: boolean;
  showRecommend?: boolean;
  showPublishTime?: boolean;
  offerLabel?: string;
  inquiryLabel?: string;
};

type OfferInquiryTabsProps = {
  tab: OfferTab;
  onTabChange: (next: OfferTab) => void;
  style?: ViewStyle;
  offerLabel?: string;
  inquiryLabel?: string;
  showMerchant?: boolean;
  onMerchantPress?: () => void;
};

export function OfferInquiryTabs({
  tab,
  onTabChange,
  style,
  offerLabel = '报盘',
  inquiryLabel = '求购',
  showMerchant = false,
  onMerchantPress,
}: OfferInquiryTabsProps) {
  const [visualTab, setVisualTab] = React.useState<OfferTab>(tab);
  const pendingFrame = React.useRef<number | null>(null);

  React.useEffect(() => {
    setVisualTab(tab);
  }, [tab]);

  React.useEffect(
    () => () => {
      if (pendingFrame.current != null) {
        cancelAnimationFrame(pendingFrame.current);
      }
    },
    [],
  );

  const handleTabPress = React.useCallback(
    (next: OfferTab) => {
      if (next === visualTab) return;
      setVisualTab(next);
      if (pendingFrame.current != null) {
        cancelAnimationFrame(pendingFrame.current);
      }
      pendingFrame.current = requestAnimationFrame(() => {
        pendingFrame.current = null;
        onTabChange(next);
      });
    },
    [onTabChange, visualTab],
  );

  return (
    <View style={[styles.topTabs, style]}>
      <TopTabItem text={offerLabel} active={visualTab === 'offer'} onPress={() => handleTabPress('offer')} />
      <TopTabItem
        text={inquiryLabel}
        active={visualTab === 'inquiry'}
        onPress={() => handleTabPress('inquiry')}
      />
      {showMerchant ? (
        <TopTabItem
          text="商家"
          active={false}
          onPress={() => {
            if (pendingFrame.current != null) {
              cancelAnimationFrame(pendingFrame.current);
              pendingFrame.current = null;
            }
            onMerchantPress?.();
          }}
        />
      ) : null}
    </View>
  );
}

export function TabAndSortBar({
  tab,
  onTabChange,
  sort,
  onSortChange,
  showTabs = true,
  showRecommend = true,
  showPublishTime = false,
  offerLabel = '报盘',
  inquiryLabel = '求购',
}: Props) {
  const priceOrder = sort.kind === 'price' ? sort.order : 'none';

  function togglePrice() {
    if (sort.kind !== 'price') {
      onSortChange({kind: 'price', order: 'asc'});
      return;
    }
    const next: SortOrder =
      sort.order === 'asc' ? 'desc' : sort.order === 'desc' ? 'none' : 'asc';
    onSortChange(next === 'none' ? {kind: 'comprehensive'} : {kind: 'price', order: next});
  }

  const arrowsXml =
    priceOrder === 'asc'
      ? sortArrowsAsc()
      : priceOrder === 'desc'
        ? sortArrowsDesc()
        : sortArrowsDefault();

  return (
    <View style={styles.bar}>
      {showTabs ? (
        <View style={styles.left}>
          <TabItem text={offerLabel} active={tab === 'offer'} onPress={() => onTabChange('offer')} />
          <TabItem
            text={inquiryLabel}
            active={tab === 'inquiry'}
            onPress={() => onTabChange('inquiry')}
          />
        </View>
      ) : null}
      <View style={[styles.right, !showTabs && styles.rightOnly]}>
        {showRecommend ? (
          <SortItem
            text="综合推荐"
            active={sort.kind === 'comprehensive'}
            onPress={() => onSortChange({kind: 'comprehensive'})}
          />
        ) : null}
        {showPublishTime ? (
          <SortItem
            text="发布时间"
            active={sort.kind === 'publishTime'}
            onPress={() => onSortChange({kind: 'publishTime'})}
          />
        ) : null}
        <SortItem
          text="价格"
          active={sort.kind === 'price' && priceOrder !== 'none'}
          onPress={togglePrice}
          rightSlot={<SvgXml xml={arrowsXml} width={6} height={12} />}
        />
      </View>
    </View>
  );
}

function TabItem({text, active, onPress}: {text: string; active: boolean; onPress: () => void}) {
  return (
    <Pressable onPress={onPress} style={styles.item}>
      <Text style={[styles.text, active && styles.textActive]}>{text}</Text>
      <View style={[styles.indicator, active && styles.indicatorActive]} />
    </Pressable>
  );
}

function TopTabItem({text, active, onPress}: {text: string; active: boolean; onPress: () => void}) {
  return (
    <Pressable onPress={onPress} style={styles.item}>
      <Text style={[styles.topTabText, active && styles.topTabTextActive]}>{text}</Text>
      <View style={[styles.indicator, active && styles.indicatorActive]} />
    </Pressable>
  );
}

function SortItem({
  text,
  active,
  onPress,
  rightSlot,
}: {
  text: string;
  active: boolean;
  onPress: () => void;
  rightSlot?: React.ReactNode;
}) {
  return (
    <Pressable onPress={onPress} style={styles.item}>
      <View style={styles.labelRow}>
        <Text style={[styles.text, active && styles.textActive]}>{text}</Text>
        {rightSlot ? <View style={styles.iconWrap}>{rightSlot}</View> : null}
      </View>
      <View style={[styles.indicator, active && styles.indicatorActive]} />
    </Pressable>
  );
}

const styles = StyleSheet.create({
  topTabs: {
    minHeight: 40,
    backgroundColor: '#FFFFFF',
    flexDirection: 'row',
    alignItems: 'flex-end',
    justifyContent: 'center',
    gap: 32,
    paddingTop: 6,
    paddingBottom: 4,
  },
  bar: {
    minHeight: 44,
    backgroundColor: '#FFFFFF',
    paddingHorizontal: 16,
    paddingTop: 6,
    paddingBottom: 4,
    flexDirection: 'row',
    alignItems: 'flex-end',
    justifyContent: 'space-between',
    borderBottomWidth: 1,
    borderBottomColor: colors.border,
  },
  left: {
    flexDirection: 'row',
    alignItems: 'flex-end',
    gap: 20,
    flexShrink: 0,
  },
  right: {
    flexDirection: 'row',
    alignItems: 'flex-end',
    gap: 16,
    flexShrink: 1,
    justifyContent: 'flex-end',
  },
  rightOnly: {
    flex: 1,
  },
  item: {
    alignItems: 'center',
    justifyContent: 'flex-end',
    gap: 2,
  },
  labelRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 3,
  },
  iconWrap: {
    width: 12,
    height: 12,
    alignItems: 'center',
    justifyContent: 'center',
  },
  topTabText: {
    color: '#171D1C',
    fontSize: 17,
    lineHeight: 24,
    fontWeight: '500',
    textAlign: 'center',
  },
  topTabTextActive: {
    color: colors.primary,
    fontWeight: '700',
  },
  text: {
    color: '#3C4947',
    fontSize: 14,
    lineHeight: 20,
    fontWeight: '400',
    textAlign: 'center',
  },
  textActive: {
    color: colors.text,
    fontWeight: '600',
  },
  indicator: {
    width: 18,
    height: 3,
    backgroundColor: 'transparent',
  },
  indicatorActive: {
    backgroundColor: colors.primary,
  },
});
