import React, {memo} from 'react';
import {Pressable, ScrollView, StyleSheet, Text, View} from 'react-native';
import {SvgXml} from 'react-native-svg';
import Svg, {Path} from 'react-native-svg';
import {colors} from '../../theme/colors';
import {fonts} from '../../theme/typography';
import type {EmployeeOffer, OfferSummary} from '../../types/api';
import {colorForOfferField, colorForTag, computePriceRange, extractCity, formatPublishTime, parseWeight, splitTags} from '../../utils/offer';
import {OfferTagChip} from './OfferTagChip';

type Props = {
  offer: OfferSummary;
  expanded: boolean;
  onToggle: () => void;
  merchantPhone?: string | null;
  onCopyPhone?: () => void;
  onDial?: () => void;
  onViewOriginalText?: (text: string) => void;
};

/**
 * 商家详情页 报盘卡（与 Figma 1421:5239 对齐）
 * - 未展开：产品名 + 价格 + 横向标签（地区/标签）+ 右侧"展开"小箭头
 * - 展开：合并员工卡（带电话/微信操作）
 */
function OfferCardCompactInner({
  offer,
  expanded,
  onToggle,
  merchantPhone,
  onCopyPhone,
  onDial,
  onViewOriginalText,
}: Props) {
  const employeePrices = offer.employeeOffers?.map(item => item.price);
  const [priceText, priceUnit] = computePriceRange(employeePrices, offer.price, offer.priceMax);
  const titleText = offer.factoryNo
    ? `${offer.productName ?? ''} ${offer.country ?? ''}${offer.factoryNo}`
    : `${offer.productName ?? ''} ${offer.country ?? ''}厂号不限`;

  const allLocations = unique([
    extractCity(offer.goodsLocation),
    ...(offer.employeeOffers ?? []).map(item => extractCity(item.goodsLocation)),
  ]);
  const allGoodsTypes = unique([
    offer.goodsType ?? '',
    ...(offer.employeeOffers ?? []).map(item => item.goodsType ?? ''),
  ]);
  const allFeedings = unique([
    offer.feedingType ?? '',
    ...(offer.employeeOffers ?? []).map(item => item.feedingType ?? item.feedingMethod ?? ''),
  ]);
  const allFatRatios = unique((offer.employeeOffers ?? []).map(item => item.fatRatio ?? ''));
  const allBreeds = unique((offer.employeeOffers ?? []).map(item => item.cattleBreed ?? ''));
  const allRemarks = unique((offer.employeeOffers ?? []).map(item => item.remark ?? ''));
  const allTags = unique([
    ...splitTags(offer.tags, 4),
    ...(offer.employeeOffers ?? []).flatMap(item => splitTags(item.tags, 4)),
  ]).slice(0, 4);

  const hasAnyTag =
    allLocations.length > 0 ||
    allGoodsTypes.length > 0 ||
    allFeedings.length > 0 ||
    allFatRatios.length > 0 ||
    allBreeds.length > 0 ||
    allRemarks.length > 0 ||
    allTags.length > 0;

  return (
    <View style={styles.itemWrap}>
      <Pressable onPress={onToggle} style={styles.row}>
        <View style={styles.body}>
          <View style={styles.titleRow}>
            <Text style={styles.title} numberOfLines={1}>
              {titleText}
            </Text>
            {priceText ? (
              <View style={styles.priceLine}>
                <Text style={styles.priceValue}>{priceText}</Text>
                {priceUnit ? <Text style={styles.priceUnit}>{priceUnit}</Text> : null}
              </View>
            ) : null}
          </View>

          {hasAnyTag ? (
            <ScrollView
              horizontal
              showsHorizontalScrollIndicator={false}
              contentContainerStyle={styles.tagRow}>
              {allLocations.map(loc => (
                <OfferTagChip key={`loc-${loc}`} text={loc} variant="location" />
              ))}
              {allGoodsTypes.map(t => (
                <OfferTagChip key={`g-${t}`} text={t} variant="colored" {...colorForOfferField('goodsType')} />
              ))}
              {allFeedings.map(t => (
                <OfferTagChip key={`f-${t}`} text={t} variant="colored" {...colorForOfferField('feedingType')} />
              ))}
              {allFatRatios.map(t => (
                <OfferTagChip key={`fat-${t}`} text={t} variant="colored" {...colorForOfferField('fatRatio')} />
              ))}
              {allBreeds.map(t => (
                <OfferTagChip key={`breed-${t}`} text={t} variant="colored" {...colorForOfferField('cattleBreed')} />
              ))}
              {allTags.map(tag => {
                const {bg, fg} = colorForTag(tag);
                return <OfferTagChip key={`t-${tag}`} text={tag} variant="colored" bg={bg} fg={fg} />;
              })}
              {allRemarks.slice(0, 2).map(t => (
                <OfferTagChip key={`remark-${t}`} text={t} variant="colored" maxWidth={180} {...colorForOfferField('remark')} />
              ))}
            </ScrollView>
          ) : null}
        </View>
        <ExpandArrow expanded={expanded} />
      </Pressable>

      {expanded
        ? (offer.employeeOffers ?? []).map((item, index) => (
            <EmployeeRow
              key={`${item.offerId ?? `${item.userNickname}-${index}`}`}
              offer={item}
              merchantPhone={merchantPhone ?? null}
              onCopyPhone={onCopyPhone}
              onDial={onDial}
              onViewOriginalText={onViewOriginalText}
            />
          ))
        : null}
    </View>
  );
}

export const OfferCardCompact = memo(OfferCardCompactInner);

function EmployeeRow({
  offer,
  merchantPhone,
  onCopyPhone,
  onDial,
  onViewOriginalText,
}: {
  offer: EmployeeOffer;
  merchantPhone?: string | null;
  onCopyPhone?: () => void;
  onDial?: () => void;
  onViewOriginalText?: (text: string) => void;
}) {
  const [weightValue, weightUnit] = parseWeight(offer.weight);
  const time = formatPublishTime(offer.publishTime);
  const tags = splitTags(offer.tags, 4);
  const feedingType = offer.feedingType ?? offer.feedingMethod ?? '';
  const fatRatio = offer.fatRatio?.trim() ?? '';
  const cattleBreed = offer.cattleBreed?.trim() ?? '';
  const remark = offer.remark?.trim() ?? '';

  return (
    <View style={empStyles.wrap}>
      <View style={empStyles.card}>
        <View style={empStyles.head}>
          <View style={empStyles.userBlock}>
            <UserSquareIcon />
            <Text style={empStyles.userName} numberOfLines={1}>
              {offer.userNickname ?? ''}
              {merchantPhone ? merchantPhone : ''}
            </Text>
          </View>
          <View style={empStyles.priceCol}>
            {weightValue ? (
              <View style={empStyles.priceLine}>
                <Text style={empStyles.weight}>{weightValue}</Text>
                {weightUnit ? <Text style={empStyles.weightUnit}>{weightUnit}</Text> : null}
              </View>
            ) : null}
            {offer.price != null && offer.price > 0 ? (
              <View style={empStyles.priceLine}>
                <Text style={empStyles.price}>¥{offer.price}</Text>
                <Text style={empStyles.priceUnit}>/kg</Text>
              </View>
            ) : (
              <Text style={empStyles.negotiate}>协商报价</Text>
            )}
          </View>
        </View>

        <View style={empStyles.tagRow}>
          {time ? <Text style={empStyles.time}>{time}</Text> : null}
          {offer.goodsLocation ? (
            <OfferTagChip text={extractCity(offer.goodsLocation)} variant="location" />
          ) : null}
          {offer.goodsType ? <OfferTagChip text={offer.goodsType} variant="colored" {...colorForOfferField('goodsType')} /> : null}
          {feedingType ? <OfferTagChip text={feedingType} variant="colored" {...colorForOfferField('feedingType')} /> : null}
          {fatRatio ? <OfferTagChip text={fatRatio} variant="colored" {...colorForOfferField('fatRatio')} /> : null}
          {cattleBreed ? <OfferTagChip text={cattleBreed} variant="colored" {...colorForOfferField('cattleBreed')} /> : null}
          {tags.map(tag => {
            const {bg, fg} = colorForTag(tag);
            return <OfferTagChip key={tag} text={tag} variant="colored" bg={bg} fg={fg} />;
          })}
          {remark ? <OfferTagChip text={remark} variant="colored" maxWidth={220} {...colorForOfferField('remark')} /> : null}
        </View>

        <View style={empStyles.actionDivider} />
        <View style={empStyles.actions}>
          <ActionButton
            text="查看原文"
            onPress={() => onViewOriginalText?.(offer.offerOriginalText ?? '')}
          />
          <View style={empStyles.actionVDivider} />
          <ActionButton text="添加微信" onPress={onCopyPhone} />
          <View style={empStyles.actionVDivider} />
          <ActionButton text="拨打电话" onPress={onDial} primary />
        </View>
      </View>
    </View>
  );
}

function ActionButton({text, primary, onPress}: {text: string; primary?: boolean; onPress?: () => void}) {
  return (
    <Pressable onPress={onPress} style={empStyles.actionButton}>
      <Text style={[empStyles.actionText, primary && empStyles.actionTextPrimary]}>{text}</Text>
    </Pressable>
  );
}

function ExpandArrow({expanded}: {expanded: boolean}) {
  return (
    <View style={styles.arrowWrap}>
      <SvgXml
        xml={`<svg viewBox="0 0 16 16" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path d="${expanded ? 'M4 10L8 6L12 10' : 'M4 6L8 10L12 6'}" stroke="#BFCAC8" stroke-width="1.4" stroke-linecap="round" stroke-linejoin="round" fill="none"/>
        </svg>`}
        width={16}
        height={16}
      />
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

function unique(values: Array<string | null | undefined>): string[] {
  const seen = new Set<string>();
  const out: string[] = [];
  for (const item of values) {
    if (!item) continue;
    if (seen.has(item)) continue;
    seen.add(item);
    out.push(item);
  }
  return out;
}

const styles = StyleSheet.create({
  itemWrap: {
    borderBottomWidth: 1,
    borderBottomColor: colors.border,
    backgroundColor: '#FFFFFF',
  },
  row: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
    paddingHorizontal: 16,
    paddingVertical: 12,
    backgroundColor: '#FFFFFF',
  },
  body: {flex: 1, gap: 8},
  titleRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: 8,
  },
  title: {
    flex: 1,
    color: colors.text,
    fontSize: 16,
    fontWeight: '500',
    lineHeight: 20,
  },
  priceLine: {flexDirection: 'row', alignItems: 'baseline'},
  priceValue: {
    fontFamily: fonts.manropeSemiBold,
    color: colors.price,
    fontSize: 16,
    lineHeight: 20,
  },
  priceUnit: {
    fontFamily: fonts.manropeRegular,
    color: colors.text,
    fontSize: 10,
    lineHeight: 20,
  },
  tagRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 4,
    paddingRight: 8,
  },
  arrowWrap: {width: 16, height: 16, alignItems: 'center', justifyContent: 'center'},
});

const empStyles = StyleSheet.create({
  wrap: {
    paddingHorizontal: 16,
    paddingBottom: 12,
    backgroundColor: '#FFFFFF',
  },
  card: {
    borderRadius: 4,
    backgroundColor: '#FBFFFE',
    borderWidth: StyleSheet.hairlineWidth,
    borderColor: 'rgba(0,106,97,0.2)',
    padding: 12,
    gap: 12,
  },
  head: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: 8,
  },
  userBlock: {flexDirection: 'row', alignItems: 'center', gap: 8, flex: 1, minWidth: 0},
  userName: {
    color: colors.text,
    fontSize: 14,
    fontWeight: '500',
    lineHeight: 20,
    maxWidth: 180,
  },
  priceCol: {flexDirection: 'row', alignItems: 'center', gap: 16},
  priceLine: {flexDirection: 'row', alignItems: 'baseline'},
  weight: {
    fontFamily: fonts.manropeSemiBold,
    color: colors.text,
    fontSize: 16,
    lineHeight: 20,
  },
  weightUnit: {
    fontFamily: fonts.manropeRegular,
    color: colors.text,
    fontSize: 10,
    lineHeight: 20,
  },
  price: {
    fontFamily: fonts.manropeSemiBold,
    color: colors.price,
    fontSize: 16,
    lineHeight: 20,
  },
  priceUnit: {
    fontFamily: fonts.manropeRegular,
    color: colors.text,
    fontSize: 10,
    lineHeight: 20,
  },
  negotiate: {
    fontFamily: fonts.manropeSemiBold,
    color: colors.primary,
    fontSize: 16,
    lineHeight: 20,
  },
  tagRow: {flexDirection: 'row', flexWrap: 'wrap', alignItems: 'center', gap: 8},
  time: {color: '#3C4947', fontSize: 11, lineHeight: 14},
  actionDivider: {height: StyleSheet.hairlineWidth, backgroundColor: 'rgba(0,106,97,0.05)'},
  actions: {flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between'},
  actionButton: {flex: 1, alignItems: 'center', justifyContent: 'center', paddingVertical: 4},
  actionText: {color: '#3C4947', fontSize: 12, lineHeight: 16},
  actionTextPrimary: {color: colors.primary, fontWeight: '500'},
  actionVDivider: {width: StyleSheet.hairlineWidth, height: 13, backgroundColor: '#3C4947'},
});
