import React, {useCallback, useEffect, useMemo, useState} from 'react';
import {
  ActivityIndicator,
  Alert,
  FlatList,
  Pressable,
  RefreshControl,
  StatusBar,
  StyleSheet,
  Text,
  TextInput,
  View,
} from 'react-native';
import type {NativeStackScreenProps} from '@react-navigation/native-stack';
import {useFocusEffect} from '@react-navigation/native';
import {useSafeAreaInsets} from 'react-native-safe-area-context';
import Svg, {Path} from 'react-native-svg';
import type {RootStackParamList} from '../navigation/routes';
import {colors} from '../theme/colors';
import {fonts} from '../theme/typography';
import {copyToClipboard, dialPhone} from '../utils/contact';
import {
  getIntentPlates,
  getRecentContactPlates,
  recordRecentContactPlate,
  removeIntentPlate,
  updateIntentPlate,
  type ContactAction,
  type FollowStatus,
  type PlateSnapshot,
} from '../utils/plateFollowStore';

type Props = NativeStackScreenProps<RootStackParamList, 'PlateFollow'>;
type FollowTab = 'intent' | 'recent';

export function PlateFollowScreen({navigation, route}: Props) {
  const insets = useSafeAreaInsets();
  const [tab, setTab] = useState<FollowTab>(route.params?.initialTab ?? 'intent');
  const category = route.params?.category ?? '牛';
  const [intentItems, setIntentItems] = useState<PlateSnapshot[]>([]);
  const [recentItems, setRecentItems] = useState<PlateSnapshot[]>([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);

  const load = useCallback(async (mode: 'initial' | 'refresh' = 'initial') => {
    if (mode === 'refresh') {
      setRefreshing(true);
    } else {
      setLoading(true);
    }

    try {
      const [intentData, recentData] = await Promise.all([getIntentPlates(), getRecentContactPlates()]);
      setIntentItems(intentData);
      setRecentItems(recentData);
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  }, []);

  useEffect(() => {
    load().catch(() => undefined);
  }, [load]);

  useFocusEffect(
    useCallback(() => {
      load().catch(() => undefined);
    }, [load]),
  );

  const items = tab === 'intent' ? intentItems : recentItems;

  const subtitle = useMemo(() => {
    if (tab === 'intent') {
      return `已暂存 ${intentItems.length} 条感兴趣的盘`;
    }
    return `最近联系过 ${recentItems.length} 条盘`;
  }, [intentItems.length, recentItems.length, tab]);

  const handleContact = useCallback(async (item: PlateSnapshot, action: ContactAction) => {
    await recordRecentContactPlate(item, action).catch(() => undefined);
    if (action === 'wechat') {
      copyToClipboard(item.contactPhone ?? '', '已复制手机号').catch(() => undefined);
    } else {
      dialPhone(item.contactPhone ?? null);
    }
    load().catch(() => undefined);
  }, [load]);

  const handleRemoveIntent = useCallback((item: PlateSnapshot) => {
    Alert.alert('移出意向盘', `确定将“${item.title}”移出意向盘吗？`, [
      {text: '取消', style: 'cancel'},
      {
        text: '移出',
        style: 'destructive',
        onPress: () => {
          removeIntentPlate(item.key)
            .then(() => load())
            .catch(error => {
              Alert.alert('移出失败', error instanceof Error ? error.message : '请稍后重试');
            });
        },
      },
    ]);
  }, [load]);

  const handleUpdateIntent = useCallback(async (item: PlateSnapshot, patch: Partial<Pick<PlateSnapshot, 'note' | 'followStatus'>>) => {
    setIntentItems(prev => prev.map(current => (current.key === item.key ? {...current, ...patch} : current)));
    await updateIntentPlate(item.key, patch).catch(() => load().catch(() => undefined));
  }, [load]);

  return (
    <View style={styles.container}>
      <StatusBar barStyle="dark-content" backgroundColor="#FFFFFF" translucent={false} />
      <View style={[styles.safeTop, {height: insets.top}]} />
      <View style={styles.header}>
        <Pressable onPress={() => navigation.goBack()} hitSlop={10} style={styles.backButton}>
          <BackIcon />
        </Pressable>
        <View style={styles.titleWrap}>
          <Text style={styles.title}>我的跟进</Text>
          <Text style={styles.subtitle}>{subtitle}</Text>
        </View>
        <View style={styles.backButton} />
      </View>

      <View style={styles.tabBar}>
        <FollowTabButton text="意向盘" active={tab === 'intent'} count={intentItems.length} onPress={() => setTab('intent')} />
        <FollowTabButton text="最近沟通" active={tab === 'recent'} count={recentItems.length} onPress={() => setTab('recent')} />
      </View>

      <FlatList
        data={items}
        keyExtractor={item => item.key}
        renderItem={({item}) => (
          <PlateCard
            item={item}
            mode={tab}
            onCopyWechat={() => handleContact(item, 'wechat')}
            onDial={() => handleContact(item, 'phone')}
            onRemove={tab === 'intent' ? () => handleRemoveIntent(item) : undefined}
            onUpdate={tab === 'intent' ? patch => handleUpdateIntent(item, patch) : undefined}
          />
        )}
        contentContainerStyle={[styles.listContent, items.length === 0 && styles.listContentEmpty]}
        refreshControl={<RefreshControl refreshing={refreshing} onRefresh={() => load('refresh')} />}
        ListEmptyComponent={
          loading ? (
            <ActivityIndicator color={colors.primary} />
          ) : (
            <EmptyState
              tab={tab}
              onSearch={() => navigation.navigate('Search', {category})}
            />
          )
        }
      />
    </View>
  );
}

function FollowTabButton({
  text,
  active,
  count,
  onPress,
}: {
  text: string;
  active: boolean;
  count: number;
  onPress: () => void;
}) {
  return (
    <Pressable onPress={onPress} style={[styles.tabButton, active && styles.tabButtonActive]}>
      <Text style={[styles.tabText, active && styles.tabTextActive]}>{text}</Text>
      <View style={[styles.tabCountBadge, active && styles.tabCountBadgeActive]}>
        <Text style={[styles.tabCountText, active && styles.tabCountTextActive]}>{count}</Text>
      </View>
    </Pressable>
  );
}

function PlateCard({
  item,
  mode,
  onCopyWechat,
  onDial,
  onRemove,
  onUpdate,
}: {
  item: PlateSnapshot;
  mode: FollowTab;
  onCopyWechat: () => void;
  onDial: () => void;
  onRemove?: () => void;
  onUpdate?: (patch: Partial<Pick<PlateSnapshot, 'note' | 'followStatus'>>) => void;
}) {
  const price = formatPrice(item);
  const meta = [item.merchantName, item.publisherName].filter(Boolean).join(' | ') || '未知发布人';
  const time = mode === 'recent' ? formatRelativeTime(item.lastContactedAt) : formatRelativeTime(item.createdAt);
  const details = buildPlateDetails(item);

  return (
    <View style={styles.card}>
      <View style={styles.cardHead}>
        <View style={[styles.typeBadge, item.type === 'inquiry' ? styles.typeBadgeInquiry : styles.typeBadgeOffer]}>
          <Text style={[styles.typeBadgeText, item.type === 'inquiry' ? styles.typeBadgeTextInquiry : styles.typeBadgeTextOffer]}>
            {item.type === 'inquiry' ? '求购' : '报盘'}
          </Text>
        </View>
        <Text style={styles.cardTitle} numberOfLines={1}>{item.title}</Text>
        {onRemove ? (
          <Pressable onPress={onRemove} hitSlop={8} style={styles.removeButton}>
            <Text style={styles.removeText}>移出</Text>
          </Pressable>
        ) : null}
      </View>

      {details.length > 0 ? (
        <View style={styles.detailWrap}>
          {details.map(detail => (
            <View key={`${detail.label}-${detail.value}`} style={styles.detailItem}>
              <Text style={styles.detailLabel}>{detail.label}</Text>
              <Text style={styles.detailValue} numberOfLines={2}>{detail.value}</Text>
            </View>
          ))}
        </View>
      ) : null}

      <View style={styles.cardBodyRow}>
        <View style={styles.cardBodyLeft}>
          <Text style={styles.metaText} numberOfLines={1}>{meta}</Text>
          <Text style={styles.timeText}>{time}</Text>
        </View>
        {price ? (
          <View style={styles.priceLine}>
            <Text style={styles.priceValue}>{price.amount}</Text>
            {price.unit ? <Text style={styles.priceUnit}>{price.unit}</Text> : null}
          </View>
        ) : null}
      </View>

      {onUpdate ? (
        <View style={styles.followBox}>
          <Text style={styles.followLabel}>跟进状态</Text>
          <View style={styles.statusRow}>
            {followStatusOptions.map(option => (
              <Pressable
                key={option.value}
                onPress={() => onUpdate({followStatus: option.value})}
                style={[styles.statusChip, (item.followStatus ?? 'new') === option.value && styles.statusChipActive]}>
                <Text style={[styles.statusText, (item.followStatus ?? 'new') === option.value && styles.statusTextActive]}>
                  {option.label}
                </Text>
              </Pressable>
            ))}
          </View>
          <FollowNoteInput value={item.note ?? ''} onSubmit={note => onUpdate({note})} />
        </View>
      ) : item.lastContactAction ? (
        <Text style={styles.recentActionText}>
          最近动作：{item.lastContactAction === 'wechat' ? '添加微信' : '拨打电话'}
        </Text>
      ) : null}

      <View style={styles.actionDivider} />
      <View style={styles.actionRow}>
        <Pressable style={styles.actionButton} onPress={onCopyWechat}>
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

const followStatusOptions: Array<{label: string; value: FollowStatus}> = [
  {label: '新加入', value: 'new'},
  {label: '已联系', value: 'contacted'},
  {label: '待回复', value: 'waiting'},
  {label: '重点', value: 'key'},
  {label: '已成交', value: 'done'},
  {label: '放弃', value: 'abandoned'},
];

function FollowNoteInput({value, onSubmit}: {value: string; onSubmit: (value: string) => void}) {
  const [draft, setDraft] = useState(value);

  useEffect(() => {
    setDraft(value);
  }, [value]);

  return (
    <TextInput
      value={draft}
      onChangeText={setDraft}
      onBlur={() => onSubmit(draft.trim())}
      onSubmitEditing={() => onSubmit(draft.trim())}
      placeholder="添加备注，例如：已问价、等回电、重点对比"
      placeholderTextColor="#A8B2B0"
      multiline
      style={styles.noteInput}
    />
  );
}

function EmptyState({tab, onSearch}: {tab: FollowTab; onSearch: () => void}) {
  const title = tab === 'intent' ? '暂无意向盘' : '暂无最近沟通';
  const desc = tab === 'intent' ? '看到感兴趣的盘，可以先加入意向盘。' : '点击添加微信或拨打电话后，会自动记录在这里。';

  return (
    <View style={styles.emptyWrap}>
      <View style={styles.emptyIcon}>
        <PathIcon />
      </View>
      <Text style={styles.emptyTitle}>{title}</Text>
      <Text style={styles.emptyDesc}>{desc}</Text>
      <Pressable onPress={onSearch} style={styles.emptyButton}>
        <Text style={styles.emptyButtonText}>去搜索</Text>
      </Pressable>
    </View>
  );
}

function buildPlateDetails(item: PlateSnapshot) {
  const details: Array<{label: string; value: string}> = [];
  const add = (label: string, value?: string | null) => {
    const text = value?.trim();
    if (text) details.push({label, value: text});
  };

  add('货物地', item.goodsLocation ?? item.region);
  add('货物类型', item.goodsType);
  add('数量', item.weight);
  add('饲养方式', item.feedingType);
  add('瘦肉率', item.fatRatio);
  add('牛种', item.cattleBreed);
  add('标签', normalizeTagText(item.tags));
  add('备注', item.remark);

  return details;
}

function normalizeTagText(value?: string | null) {
  if (!value) return '';
  return value
    .split(/[|,，、\s]+/)
    .map(item => item.trim())
    .filter(Boolean)
    .join(' / ');
}

function formatPrice(item: PlateSnapshot) {
  if (item.price == null || item.price <= 0) return item.type === 'offer' ? {amount: '协商报价', unit: ''} : null;
  const amount = item.priceMax != null && item.priceMax > 0 && item.priceMax !== item.price
    ? `¥${trimNum(item.price)}-${trimNum(item.priceMax)}`
    : `¥${trimNum(item.price)}`;
  return {amount, unit: '/kg'};
}

function trimNum(value: number) {
  return Number.isInteger(value) ? `${value}` : value.toFixed(1);
}

function formatRelativeTime(value?: number) {
  if (!value) return '';
  const diffMs = Math.max(0, Date.now() - value);
  const minutes = Math.floor(diffMs / 60000);
  if (minutes < 1) return '刚刚';
  if (minutes < 60) return `${minutes}分钟前`;
  const hours = Math.floor(minutes / 60);
  if (hours < 24) return `${hours}小时前`;
  const days = Math.floor(hours / 24);
  return `${days}天前`;
}

function BackIcon() {
  return (
    <Svg width={24} height={24} viewBox="0 0 24 24" fill="none">
      <Path d="M15 18L9 12L15 6" stroke="#171D1C" strokeWidth={2} strokeLinecap="round" strokeLinejoin="round" />
    </Svg>
  );
}

function PathIcon() {
  return (
    <Svg width={44} height={44} viewBox="0 0 44 44" fill="none">
      <Path d="M15 8H29C31.2 8 33 9.8 33 12V36L22 30L11 36V12C11 9.8 12.8 8 15 8Z" stroke={colors.primary} strokeWidth={2} strokeLinejoin="round" />
      <Path d="M22 15V25M17 20H27" stroke={colors.primary} strokeWidth={2} strokeLinecap="round" />
    </Svg>
  );
}

function AddSquareIcon() {
  return (
    <Svg width={16} height={16} viewBox="0 0 24 24" fill="none">
      <Path d="M9 22h6c5 0 7-2 7-7V9c0-5-2-7-7-7H9C4 2 2 4 2 9v6c0 5 2 7 7 7Z" stroke="#3C4947" strokeWidth={1.5} />
      <Path d="M8 12h8M12 16V8" stroke="#3C4947" strokeWidth={1.5} strokeLinecap="round" />
    </Svg>
  );
}

function PhoneIcon() {
  return (
    <Svg width={16} height={16} viewBox="0 0 24 24" fill="none">
      <Path
        d="M22 16.9v3a2 2 0 0 1-2.2 2 19.8 19.8 0 0 1-8.6-3.1 19.5 19.5 0 0 1-6-6A19.8 19.8 0 0 1 2.1 4.2 2 2 0 0 1 4.1 2h3a2 2 0 0 1 2 1.7 12.8 12.8 0 0 0 .7 2.8 2 2 0 0 1-.4 2.1L8.1 9.9a16 16 0 0 0 6 6l1.3-1.3a2 2 0 0 1 2.1-.4c.9.3 1.8.6 2.8.7a2 2 0 0 1 1.7 2Z"
        stroke={colors.primary}
        strokeWidth={1.5}
      />
    </Svg>
  );
}

const styles = StyleSheet.create({
  container: {flex: 1, backgroundColor: '#F4F7F6'},
  safeTop: {backgroundColor: '#FFFFFF'},
  header: {
    minHeight: 58,
    backgroundColor: '#FFFFFF',
    paddingHorizontal: 12,
    flexDirection: 'row',
    alignItems: 'center',
    borderBottomWidth: 1,
    borderBottomColor: colors.border,
  },
  backButton: {width: 40, height: 40, alignItems: 'center', justifyContent: 'center'},
  titleWrap: {flex: 1, alignItems: 'center'},
  title: {color: colors.text, fontSize: 18, lineHeight: 24, fontWeight: '700'},
  subtitle: {marginTop: 2, color: colors.textMuted, fontSize: 11, lineHeight: 15},
  tabBar: {
    paddingHorizontal: 12,
    paddingVertical: 10,
    backgroundColor: '#FFFFFF',
    flexDirection: 'row',
    gap: 8,
  },
  tabButton: {
    flex: 1,
    height: 38,
    borderRadius: 6,
    backgroundColor: '#F4F7F6',
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    gap: 6,
  },
  tabButtonActive: {backgroundColor: colors.primary},
  tabText: {color: colors.textSecondary, fontSize: 14, lineHeight: 18, fontWeight: '600'},
  tabTextActive: {color: '#FFFFFF'},
  tabCountBadge: {
    minWidth: 20,
    height: 20,
    paddingHorizontal: 6,
    borderRadius: 10,
    backgroundColor: '#FFFFFF',
    alignItems: 'center',
    justifyContent: 'center',
  },
  tabCountBadgeActive: {backgroundColor: 'rgba(255,255,255,0.2)'},
  tabCountText: {color: colors.primary, fontSize: 11, fontWeight: '700'},
  tabCountTextActive: {color: '#FFFFFF'},
  listContent: {padding: 10, paddingBottom: 32},
  listContentEmpty: {flexGrow: 1, alignItems: 'center', justifyContent: 'center'},
  card: {
    marginBottom: 10,
    paddingHorizontal: 12,
    paddingTop: 12,
    paddingBottom: 8,
    borderRadius: 8,
    backgroundColor: '#FFFFFF',
    borderWidth: 1,
    borderColor: '#E2ECE9',
  },
  cardHead: {flexDirection: 'row', alignItems: 'center', gap: 8},
  typeBadge: {
    minWidth: 38,
    height: 24,
    paddingHorizontal: 7,
    borderRadius: 4,
    borderWidth: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  typeBadgeOffer: {backgroundColor: '#EAF9F7', borderColor: '#BDE9E4'},
  typeBadgeInquiry: {backgroundColor: '#EEF4FF', borderColor: '#C8D8FF'},
  typeBadgeText: {fontSize: 13, lineHeight: 18},
  typeBadgeTextOffer: {color: colors.primary},
  typeBadgeTextInquiry: {color: '#3767D6'},
  cardTitle: {flex: 1, minWidth: 0, color: colors.text, fontSize: 16, lineHeight: 22, fontWeight: '600'},
  removeButton: {paddingHorizontal: 6, paddingVertical: 4},
  removeText: {color: colors.textMuted, fontSize: 12, lineHeight: 16},
  detailWrap: {
    marginTop: 10,
    borderRadius: 6,
    backgroundColor: '#F7FBFA',
    borderWidth: 1,
    borderColor: '#E3EEEB',
    paddingHorizontal: 8,
    paddingVertical: 6,
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: 6,
  },
  detailItem: {
    minWidth: '30%',
    maxWidth: '100%',
    paddingHorizontal: 6,
    paddingVertical: 4,
    borderRadius: 4,
    backgroundColor: '#FFFFFF',
  },
  detailLabel: {color: '#8A9693', fontSize: 10, lineHeight: 14},
  detailValue: {marginTop: 2, color: colors.text, fontSize: 12, lineHeight: 17},
  cardBodyRow: {marginTop: 10, minHeight: 36, flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', gap: 10},
  cardBodyLeft: {flex: 1, minWidth: 0},
  metaText: {color: colors.textMuted, fontSize: 13, lineHeight: 18},
  timeText: {marginTop: 3, color: '#9DA4A3', fontSize: 11, lineHeight: 15},
  priceLine: {flexDirection: 'row', alignItems: 'baseline', flexShrink: 0},
  priceValue: {fontFamily: fonts.manropeSemiBold, color: colors.price, fontSize: 17, lineHeight: 22},
  priceUnit: {color: colors.text, fontSize: 10, lineHeight: 18, marginLeft: 2},
  followBox: {marginTop: 10, borderRadius: 6, backgroundColor: '#F8FAFA', padding: 8},
  followLabel: {color: colors.textSecondary, fontSize: 12, lineHeight: 17, fontWeight: '600'},
  statusRow: {marginTop: 6, flexDirection: 'row', flexWrap: 'wrap', gap: 6},
  statusChip: {
    minHeight: 26,
    paddingHorizontal: 8,
    borderRadius: 13,
    backgroundColor: '#FFFFFF',
    borderWidth: 1,
    borderColor: '#DDE8E5',
    alignItems: 'center',
    justifyContent: 'center',
  },
  statusChipActive: {backgroundColor: '#E7F5F2', borderColor: '#BDE3DD'},
  statusText: {color: colors.textSecondary, fontSize: 11, lineHeight: 15},
  statusTextActive: {color: colors.primary, fontWeight: '700'},
  noteInput: {
    marginTop: 8,
    minHeight: 38,
    maxHeight: 82,
    borderRadius: 4,
    borderWidth: 1,
    borderColor: '#DDE8E5',
    backgroundColor: '#FFFFFF',
    paddingHorizontal: 8,
    paddingVertical: 7,
    color: colors.text,
    fontSize: 12,
    lineHeight: 18,
    textAlignVertical: 'top',
  },
  recentActionText: {marginTop: 8, color: colors.textMuted, fontSize: 12, lineHeight: 17},
  actionDivider: {marginTop: 10, height: StyleSheet.hairlineWidth, backgroundColor: 'rgba(0,106,97,0.12)'},
  actionRow: {minHeight: 38, flexDirection: 'row', alignItems: 'center'},
  actionButton: {flex: 1, flexDirection: 'row', alignItems: 'center', justifyContent: 'center', gap: 4, paddingVertical: 8},
  actionText: {color: '#3C4947', fontSize: 13, lineHeight: 18},
  actionTextPrimary: {color: colors.primary, fontWeight: '600'},
  actionVDivider: {width: StyleSheet.hairlineWidth, height: 14, backgroundColor: 'rgba(60,73,71,0.26)'},
  emptyWrap: {alignItems: 'center', paddingHorizontal: 28},
  emptyIcon: {width: 72, height: 72, borderRadius: 36, backgroundColor: '#E8F5F3', alignItems: 'center', justifyContent: 'center'},
  emptyTitle: {marginTop: 14, color: colors.text, fontSize: 17, lineHeight: 24, fontWeight: '700'},
  emptyDesc: {marginTop: 6, color: colors.textMuted, fontSize: 13, lineHeight: 20, textAlign: 'center'},
  emptyButton: {marginTop: 18, height: 38, paddingHorizontal: 22, borderRadius: 4, backgroundColor: colors.primary, alignItems: 'center', justifyContent: 'center'},
  emptyButtonText: {color: '#FFFFFF', fontSize: 14, lineHeight: 18, fontWeight: '600'},
});
