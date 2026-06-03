import React, {memo, useState} from 'react';
import {Pressable, StyleSheet, Text, View} from 'react-native';
import Svg, {Path} from 'react-native-svg';
import {colors} from '../../theme/colors';
import {fonts} from '../../theme/typography';
import type {EmployeeOfferItem, MerchantOfferGroup} from '../../types/api';
import {copyToClipboard, dialPhone} from '../../utils/contact';
import {colorForTag, extractCity, formatPublishTime, parseWeight, splitTags} from '../../utils/offer';
import {OfferTagChip} from './OfferTagChip';

type Props = {
  group: MerchantOfferGroup;
  isInquiry?: boolean;
  onCopyPhone?: string;
  onDial?: string;
  onViewOriginalText?: (text: string) => void;
  /** 是否默认展开（默认 false） */
  defaultExpanded?: boolean;
};

/**
 * 国家+厂号+产品 / 平替页 商家分组卡（与 Figma 1421:4624 对齐）：
 * - 折叠态：商家名 + 知名标签 + 价格区间 + 标签 chips + 右侧箭头（点击展开）
 * - 展开态：员工报盘列表
 */
function MerchantOfferGroupCardInner({group, isInquiry, onCopyPhone, onDial, onViewOriginalText, defaultExpanded}: Props) {
  const [expanded, setExpanded] = useState(defaultExpanded ?? false);
  const merchantName = group.merchantName || `商家-${group.merchantId ?? ''}`;

  // 价格区间汇总
  const prices = (group.employeeOffers ?? [])
    .map(e => Number(e.price))
    .filter(v => Number.isFinite(v) && v > 0);
  const priceMin = prices.length ? Math.min(...prices) : null;
  const priceMax = prices.length ? Math.max(...prices) : null;

  // 聚合所有员工的标签
  const allTagsSet = new Set<string>();
  let firstLocation = '';
  (group.employeeOffers ?? []).forEach(emp => {
    if (!firstLocation && emp.goodsLocation) firstLocation = extractCity(emp.goodsLocation);
    if (emp.goodsType) allTagsSet.add(emp.goodsType);
    if (emp.feedingType) allTagsSet.add(emp.feedingType);
    splitTags(emp.tags, 4).forEach(t => allTagsSet.add(t));
  });
  const allTags = Array.from(allTagsSet).slice(0, 4);

  return (
    <View style={styles.wrap}>
      <Pressable onPress={() => setExpanded(prev => !prev)} style={styles.header}>
        <View style={styles.headerLeft}>
          <View style={styles.titleRow}>
            {group.isFamousMerchant ? (
              <View style={styles.famousBadge}>
                <Text style={styles.famousText}>知名商家</Text>
                <FamousCrown />
              </View>
            ) : null}
            <Text style={styles.merchantName} numberOfLines={1}>
              {merchantName}
            </Text>
          </View>
          <View style={styles.tagRow}>
            {firstLocation ? <OfferTagChip text={firstLocation} variant="location" /> : null}
            {allTags.map(tag => {
              const {bg, fg} = colorForTag(tag);
              return <OfferTagChip key={tag} text={tag} variant="colored" bg={bg} fg={fg} />;
            })}
          </View>
        </View>
        <View style={styles.headerRight}>
          {priceMin != null && priceMax != null ? (
            <View style={styles.priceLine}>
              <Text style={styles.priceValue}>
                ¥ {numStr(priceMin)}{priceMin !== priceMax ? ` - ${numStr(priceMax)}` : ''}
              </Text>
              <Text style={styles.priceUnit}>/kg </Text>
            </View>
          ) : (
            <Text style={styles.negotiateText}>协商报价</Text>
          )}
          <View style={[styles.arrow, expanded && styles.arrowDown]}>
            <Svg width={16} height={16} viewBox="0 0 16 16" fill="none">
              <Path d={expanded ? 'M4 10L8 6L12 10' : 'M4 6L8 10L12 6'} stroke="#3C4947" strokeWidth={1.4} strokeLinecap="round" strokeLinejoin="round" />
            </Svg>
          </View>
        </View>
      </Pressable>

      {expanded ? (
        <View style={styles.expandedBody}>
          {(group.employeeOffers ?? []).map((offer, index) => (
            <EmployeeOfferRow
              key={`${offer.offerId ?? `${offer.userNickname}-${index}`}`}
              offer={offer}
              merchantPhone={offer.contactPhone ?? onCopyPhone ?? null}
              onCopyPhone={() =>
                copyToClipboard(offer.contactPhone ?? onCopyPhone ?? '', '已复制手机号').catch(() => undefined)
              }
              onDial={() => dialPhone(offer.contactPhone ?? onDial ?? null)}
              onViewOriginalText={onViewOriginalText}
            />
          ))}
          {group.offerCount > (group.employeeOffers?.length ?? 0) ? (
            <Text style={styles.moreCount}>共 {group.offerCount} 条{isInquiry ? '求购' : '报盘'}</Text>
          ) : null}
        </View>
      ) : null}
    </View>
  );
}

export const MerchantOfferGroupCard = memo(MerchantOfferGroupCardInner);

function FamousCrown() {
  // 黄色皇冠
  return (
    <Svg width={12} height={12} viewBox="0 0 12 12" fill="none">
      <Path d="M2 4l1.5 4h5L10 4l-2 1.5L6 3 4 5.5 2 4Z" fill="#FFD23A" stroke="#E0A914" strokeWidth={0.5} strokeLinejoin="round"/>
    </Svg>
  );
}

function EmployeeOfferRow({
  offer,
  merchantPhone,
  onCopyPhone,
  onDial,
  onViewOriginalText,
}: {
  offer: EmployeeOfferItem;
  merchantPhone?: string | null;
  onCopyPhone?: () => void;
  onDial?: () => void;
  onViewOriginalText?: (text: string) => void;
}) {
  const [weightValue, weightUnit] = parseWeight(offer.weight);
  const time = formatPublishTime(offer.publishTime);
  const tags = splitTags(offer.tags, 4);

  return (
    <View style={styles.offerCard}>
      <View style={styles.offerHead}>
        <View style={styles.userBlock}>
          <UserSquareIcon />
          <Text style={styles.userName} numberOfLines={1}>
            {(offer.userNickname ?? '')}{merchantPhone ?? ''}
          </Text>
        </View>
        <View style={styles.priceCol}>
          {weightValue ? (
            <View style={styles.priceLineSmall}>
              <Text style={styles.weightValue}>{weightValue}</Text>
              {weightUnit ? <Text style={styles.weightUnit}>{weightUnit}</Text> : null}
            </View>
          ) : null}
          <View style={styles.priceLineSmall}>
            {offer.price && !isNaN(Number(offer.price)) && Number(offer.price) > 0 ? (
              <>
                <Text style={styles.priceMain}>¥{offer.price}</Text>
                <Text style={styles.priceUnitInner}>/kg </Text>
              </>
            ) : (
              <Text style={styles.negotiateTextSmall}>协商报价</Text>
            )}
          </View>
        </View>
      </View>

      <View style={styles.offerTagRow}>
        {time ? <Text style={styles.timeText}>{time}</Text> : null}
        {offer.goodsLocation ? (
          <OfferTagChip text={extractCity(offer.goodsLocation)} variant="location" />
        ) : null}
        {offer.goodsType ? <OfferTagChip text={offer.goodsType} /> : null}
        {offer.feedingType ? <OfferTagChip text={offer.feedingType} /> : null}
        {tags.map(tag => {
          const {bg, fg} = colorForTag(tag);
          return <OfferTagChip key={tag} text={tag} variant="colored" bg={bg} fg={fg} />;
        })}
      </View>

      <View style={styles.actionDivider} />

      <View style={styles.actionRow}>
        <Pressable style={styles.actionButton} onPress={() => onViewOriginalText?.(offer.offerOriginalText ?? '')}>
          <BookIcon />
          <Text style={styles.actionText}>查看原文</Text>
        </Pressable>
        <View style={styles.actionVDivider} />
        <Pressable style={styles.actionButton} onPress={onCopyPhone}>
          <AddSquareIcon />
          <Text style={styles.actionText}>添加微信</Text>
        </Pressable>
        <View style={styles.actionVDivider} />
        <Pressable style={styles.actionButton} onPress={onDial}>
          <PhoneIcon />
          <Text style={[styles.actionText, styles.actionTextPrimary]}>拨打电话</Text>
        </Pressable>
      </View>
    </View>
  );
}

function UserSquareIcon() {
  return (
    <Svg width={18} height={18} viewBox="0 0 18 18" fill="none">
      <Path d="M12.75 16.5H5.25C2.25 16.5 1.5 15.75 1.5 12.75V5.25C1.5 2.25 2.25 1.5 5.25 1.5H12.75C15.75 1.5 16.5 2.25 16.5 5.25V12.75C16.5 15.75 15.75 16.5 12.75 16.5Z" fill="#D2E8E5" stroke="#5098AA" strokeWidth={0.8} strokeLinecap="round" strokeLinejoin="round"/>
      <Path d="M12.75 14.25C12.75 12.5932 11.0711 11.25 9 11.25C6.92893 11.25 5.25 12.5932 5.25 14.25" stroke="#244C56" strokeWidth={0.8} strokeLinecap="round" strokeLinejoin="round"/>
      <Path d="M9 11.25C10.2426 11.25 11.25 10.2426 11.25 9C11.25 7.75736 10.2426 6.75 9 6.75C7.75736 6.75 6.75 7.75736 6.75 9C6.75 10.2426 7.75736 11.25 9 11.25Z" fill="#D2E8E5" stroke="#244C56" strokeWidth={0.8} strokeLinecap="round" strokeLinejoin="round"/>
    </Svg>
  );
}

function BookIcon() {
  return (
    <Svg width={16} height={16} viewBox="0 0 24 24" fill="none">
      <Path d="M22 16.7V4.7c0-1.2-1-2.1-2.2-2C16.3 3 11.1 3.9 7.7 6c-.4.2-.7.7-.7 1.2v15.6c0 .8.8 1.4 1.6 1.2 3.5-2 8.5-2.8 11.7-3.1 1-.1 1.7-1 1.7-2v-2.2" stroke="#3C4947" strokeWidth={1.5}/>
      <Path d="M2 18.5V5C2 3.4 3.3 2.7 4.8 3.4 6.5 4.2 9.7 5.5 11.5 6.4" stroke="#3C4947" strokeWidth={1.5}/>
    </Svg>
  );
}

function AddSquareIcon() {
  return (
    <Svg width={16} height={16} viewBox="0 0 24 24" fill="none">
      <Path d="M9 22h6c5 0 7-2 7-7V9c0-5-2-7-7-7H9C4 2 2 4 2 9v6c0 5 2 7 7 7Z" stroke="#3C4947" strokeWidth={1.5}/>
      <Path d="M8 12h8M12 16V8" stroke="#3C4947" strokeWidth={1.5} strokeLinecap="round"/>
    </Svg>
  );
}

function PhoneIcon() {
  return (
    <Svg width={16} height={16} viewBox="0 0 24 24" fill="none">
      <Path d="M22 16.9v3a2 2 0 0 1-2.2 2 19.8 19.8 0 0 1-8.6-3.1 19.5 19.5 0 0 1-6-6A19.8 19.8 0 0 1 2.1 4.2 2 2 0 0 1 4.1 2h3a2 2 0 0 1 2 1.7 12.8 12.8 0 0 0 .7 2.8 2 2 0 0 1-.4 2.1L8.1 9.9a16 16 0 0 0 6 6l1.3-1.3a2 2 0 0 1 2.1-.4c.9.3 1.8.6 2.8.7a2 2 0 0 1 1.7 2Z" stroke={colors.primary} strokeWidth={1.5}/>
    </Svg>
  );
}

function numStr(value: number) {
  return Number.isInteger(value) ? `${value}` : value.toFixed(1);
}

const styles = StyleSheet.create({
  wrap: {
    paddingHorizontal: 16,
    backgroundColor: '#FFFFFF',
    borderBottomWidth: 1,
    borderBottomColor: colors.border,
  },
  header: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingVertical: 12,
    gap: 8,
  },
  headerLeft: {flex: 1, gap: 8},
  headerRight: {flexDirection: 'row', alignItems: 'center', gap: 4},
  titleRow: {flexDirection: 'row', alignItems: 'center', gap: 4, flex: 1},
  merchantName: {color: colors.text, fontSize: 16, fontWeight: '500', lineHeight: 20, flexShrink: 1},
  famousBadge: {
    height: 18,
    paddingLeft: 4.5,
    paddingRight: 4,
    borderRadius: 2,
    backgroundColor: '#254D5A',
    flexDirection: 'row',
    alignItems: 'center',
    gap: 2,
  },
  famousText: {color: '#F2FFFD', fontSize: 9.9, lineHeight: 14, fontWeight: '500'},
  tagRow: {flexDirection: 'row', flexWrap: 'wrap', alignItems: 'center', gap: 4},
  priceLine: {flexDirection: 'row', alignItems: 'baseline'},
  priceValue: {fontFamily: fonts.manropeSemiBold, color: colors.price, fontSize: 16, lineHeight: 20},
  priceUnit: {fontFamily: fonts.manropeRegular, color: colors.text, fontSize: 10, lineHeight: 20},
  negotiateText: {color: colors.textSecondary, fontSize: 12, lineHeight: 20},
  negotiateTextSmall: {fontFamily: fonts.manropeSemiBold, color: colors.primary, fontSize: 16, lineHeight: 20},
  arrow: {width: 16, height: 16, alignItems: 'center', justifyContent: 'center'},
  arrowDown: {},

  expandedBody: {paddingTop: 0, paddingBottom: 12, gap: 12},
  offerCard: {
    borderRadius: 4,
    backgroundColor: '#FBFFFE',
    borderWidth: 0.5,
    borderColor: 'rgba(0,106,97,0.2)',
    padding: 12,
    gap: 12,
  },
  offerHead: {flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', gap: 8},
  userBlock: {flexDirection: 'row', alignItems: 'center', gap: 8, flex: 1},
  userName: {color: colors.text, fontSize: 14, fontWeight: '500', flexShrink: 1},
  priceCol: {flexDirection: 'row', alignItems: 'center', gap: 16},
  priceLineSmall: {flexDirection: 'row', alignItems: 'baseline'},
  weightValue: {fontFamily: fonts.manropeSemiBold, color: colors.text, fontSize: 16, lineHeight: 20},
  weightUnit: {fontFamily: fonts.manropeRegular, color: colors.text, fontSize: 10, lineHeight: 20, marginLeft: 1},
  priceMain: {fontFamily: fonts.manropeSemiBold, color: colors.price, fontSize: 16, lineHeight: 20},
  priceUnitInner: {fontFamily: fonts.manropeRegular, color: colors.text, fontSize: 10, lineHeight: 20, marginLeft: 1},
  offerTagRow: {flexDirection: 'row', flexWrap: 'wrap', alignItems: 'center', gap: 6},
  timeText: {color: '#3C4947', fontSize: 11, lineHeight: 14},
  actionDivider: {height: 1, backgroundColor: 'rgba(0,106,97,0.05)'},
  actionRow: {flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between'},
  actionButton: {flex: 1, flexDirection: 'row', alignItems: 'center', justifyContent: 'center', gap: 4, paddingVertical: 4},
  actionText: {color: '#3C4947', fontSize: 12, lineHeight: 16},
  actionTextPrimary: {color: colors.primary, fontWeight: '500'},
  actionVDivider: {width: 0.5, height: 13, backgroundColor: '#3C4947', opacity: 0.3},
  moreCount: {color: '#9DA4A3', fontSize: 11, textAlign: 'center'},
});
