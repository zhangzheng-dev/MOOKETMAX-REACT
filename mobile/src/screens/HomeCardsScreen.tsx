import React, {useCallback, useMemo, useState} from 'react';
import {
  ActivityIndicator,
  Alert,
  Pressable,
  RefreshControl,
  ScrollView,
  StyleSheet,
  Text,
  View,
} from 'react-native';
import Svg, {Path} from 'react-native-svg';
import {useFocusEffect} from '@react-navigation/native';
import type {NativeStackScreenProps} from '@react-navigation/native-stack';
import {mooketApi} from '../api/mooketApi';
import {ChevronDownIcon, ClockIcon, StarIcon} from '../components/common/AppIcons';
import {HomeCardSwitcher} from '../components/home/cards';
import {DEFAULT_CATEGORY} from '../config/env';
import type {RootStackParamList} from '../navigation/routes';
import {colors} from '../theme/colors';
import type {HomeCardItem, HomeCardsResponse} from '../types/api';
import {openHomeCard} from '../utils/navigation';
import {sessionStore} from '../store/sessionStore';

type Props = NativeStackScreenProps<RootStackParamList, 'HomeCards'>;

const categories = ['牛', '猪', '羊', '禽', '水产'];

export function HomeCardsScreen({navigation, route}: Props) {
  const [category, setCategory] = useState(route.params?.category ?? DEFAULT_CATEGORY);
  const [tab, setTab] = useState<0 | 1>(route.params?.tab ?? 0);
  const [cards, setCards] = useState<HomeCardItem[]>([]);
  const [loading, setLoading] = useState(false);
  const [editMode, setEditMode] = useState(false);
  const [menuOpen, setMenuOpen] = useState(false);

  const loadFn = useCallback(async (cat: string, t: 0 | 1): Promise<HomeCardsResponse> => {
    const token = sessionStore.getState().token;
    if (t === 0) return mooketApi.getSelfSelectCards(cat);
    return mooketApi.getRecentSearchCards(cat);
  }, []);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const result = await loadFn(category, tab);
      setCards(result.cards ?? []);
    } finally {
      setLoading(false);
    }
  }, [category, loadFn, tab]);

  useFocusEffect(
    useCallback(() => {
      load().catch(() => undefined);
    }, [load]),
  );

  function switchCategory(value: string) {
    setCategory(value);
    setMenuOpen(false);
    setEditMode(false);
  }

  function switchTab(value: 0 | 1) {
    setTab(value);
    setEditMode(false);
  }

  async function deleteHistory(historyId: number) {
    const previous = cards;
    setCards(prev => prev.filter(item => item.historyId !== historyId));
    try {
      await mooketApi.deleteSearchHistory(historyId);
    } catch (error) {
      setCards(previous);
      Alert.alert('删除失败', error instanceof Error ? error.message : '请稍后重试');
    }
  }

  async function moveToSelfSelect(historyId: number) {
    const previous = cards;
    setCards(prev => prev.filter(item => item.historyId !== historyId));
    try {
      await mooketApi.moveToSelfSelect(historyId);
    } catch (error) {
      setCards(previous);
      Alert.alert('添加失败', error instanceof Error ? error.message : '请稍后重试');
    }
  }

  async function cancelSelfSelect(historyId: number) {
    const previous = cards;
    setCards(prev => prev.filter(item => item.historyId !== historyId));
    try {
      await mooketApi.cancelSelfSelect(historyId);
    } catch (error) {
      setCards(previous);
      Alert.alert('移出失败', error instanceof Error ? error.message : '请稍后重试');
    }
  }

  const {leftColumn, rightColumn} = useMemo(() => splitColumns(cards), [cards]);

  return (
    <View style={styles.container}>
      <View style={styles.header}>
        <Pressable hitSlop={8} onPress={() => navigation.goBack()} style={styles.headerButton}>
          <Svg width={20} height={20} viewBox="0 0 24 24" fill="none">
            <Path d="M15 5L8 12L15 19" stroke={colors.text} strokeWidth={2} strokeLinecap="round" strokeLinejoin="round" />
          </Svg>
        </Pressable>
        <Text style={styles.headerTitle}>数据卡片</Text>
        <View style={styles.headerActions}>
          <Pressable onPress={() => setMenuOpen(prev => !prev)} style={styles.categoryTrigger}>
            <Text style={styles.categoryTriggerText}>{category}</Text>
            <ChevronDownIcon size={14} />
          </Pressable>
          <Pressable hitSlop={8} onPress={() => setEditMode(prev => !prev)}>
            <Text style={[styles.editText, editMode && styles.editTextActive]}>
              {editMode ? '完成' : '编辑'}
            </Text>
          </Pressable>
        </View>
        {menuOpen ? (
          <View style={styles.categoryMenu}>
            {categories.map(item => {
              const selected = item === category;
              return (
                <Pressable key={item} onPress={() => switchCategory(item)} style={styles.categoryMenuItem}>
                  <Text style={[styles.categoryMenuText, selected && styles.categoryMenuTextActive]}>
                    {item}
                  </Text>
                </Pressable>
              );
            })}
          </View>
        ) : null}
      </View>

      <View style={styles.tabsRow}>
        <TabItem
          icon={<StarIcon size={16} color={tab === 0 ? colors.primary : '#9DA4A3'} />}
          text="自选数据"
          active={tab === 0}
          onPress={() => switchTab(0)}
        />
        <TabItem
          icon={<ClockIcon size={16} color={tab === 1 ? colors.primary : '#3C4947'} />}
          text="历史搜索数据"
          active={tab === 1}
          onPress={() => switchTab(1)}
        />
      </View>

      <ScrollView
        style={styles.scroll}
        contentContainerStyle={styles.content}
        refreshControl={<RefreshControl refreshing={loading} onRefresh={load} />}>
        {cards.length === 0 ? (
          loading ? (
            <ActivityIndicator color={colors.primary} style={styles.loading} />
          ) : (
            <Text style={styles.empty}>{tab === 0 ? '暂无自选数据' : '暂无历史搜索数据'}</Text>
          )
        ) : (
          <View style={styles.gridRow}>
            <View style={styles.gridColumn}>
              {leftColumn.map((card, index) => (
                <CardWithActions
                  key={`l-${card.historyId ?? card.rank ?? index}`}
                  card={card}
                  category={category}
                  tab={tab}
                  editMode={editMode}
                  onPress={() => openHomeCard(navigation, category, card)}
                  onDelete={() => confirmDelete(card.historyId, deleteHistory)}
                  onMoveToSelfSelect={() => confirmMoveToSelfSelect(card.historyId, moveToSelfSelect)}
                  onCancelSelfSelect={() => confirmCancelSelfSelect(card.historyId, cancelSelfSelect)}
                />
              ))}
            </View>
            <View style={styles.gridColumn}>
              {rightColumn.map((card, index) => (
                <CardWithActions
                  key={`r-${card.historyId ?? card.rank ?? index}`}
                  card={card}
                  category={category}
                  tab={tab}
                  editMode={editMode}
                  onPress={() => openHomeCard(navigation, category, card)}
                  onDelete={() => confirmDelete(card.historyId, deleteHistory)}
                  onMoveToSelfSelect={() => confirmMoveToSelfSelect(card.historyId, moveToSelfSelect)}
                  onCancelSelfSelect={() => confirmCancelSelfSelect(card.historyId, cancelSelfSelect)}
                />
              ))}
            </View>
          </View>
        )}
      </ScrollView>
    </View>
  );
}

function CardWithActions({
  card,
  tab,
  editMode,
  onPress,
  onDelete,
  onMoveToSelfSelect,
  onCancelSelfSelect,
}: {
  card: HomeCardItem;
  category: string;
  tab: 0 | 1;
  editMode: boolean;
  onPress: () => void;
  onDelete: () => void;
  onMoveToSelfSelect: () => void;
  onCancelSelfSelect: () => void;
}) {
  return (
    <View style={styles.cardWrap}>
      <HomeCardSwitcher card={card} onPress={onPress} />
      {editMode ? (
        <View style={styles.actions}>
          {tab === 0 ? (
            <Pressable style={styles.actionButton} onPress={onCancelSelfSelect}>
              <Text style={styles.actionText}>移出自选</Text>
            </Pressable>
          ) : (
            <>
              <Pressable style={styles.actionButton} onPress={onMoveToSelfSelect}>
                <Text style={styles.actionText}>添加自选</Text>
              </Pressable>
              <Pressable style={[styles.actionButton, styles.actionDanger]} onPress={onDelete}>
                <Text style={[styles.actionText, styles.actionDangerText]}>删除</Text>
              </Pressable>
            </>
          )}
        </View>
      ) : null}
    </View>
  );
}

function TabItem({
  icon,
  text,
  active,
  onPress,
}: {
  icon: React.ReactNode;
  text: string;
  active: boolean;
  onPress: () => void;
}) {
  return (
    <Pressable onPress={onPress} style={styles.tabItem}>
      <View style={styles.tabIconRow}>
        {icon}
        <Text style={[styles.tabText, active && styles.tabTextActive]}>{text}</Text>
      </View>
      <View style={[styles.tabIndicator, active && styles.tabIndicatorActive]} />
    </Pressable>
  );
}

function splitColumns(cards: HomeCardItem[]) {
  const left: HomeCardItem[] = [];
  const right: HomeCardItem[] = [];
  cards.forEach((card, index) => {
    if (index % 2 === 0) left.push(card);
    else right.push(card);
  });
  return {leftColumn: left, rightColumn: right};
}

function confirmDelete(historyId: number | null | undefined, onConfirm: (id: number) => void | Promise<void>) {
  if (!historyId) return;
  Alert.alert('删除记录', '确定删除这条历史搜索记录吗？', [
    {text: '取消', style: 'cancel'},
    {text: '删除', style: 'destructive', onPress: () => onConfirm(historyId)},
  ]);
}

function confirmMoveToSelfSelect(historyId: number | null | undefined, onConfirm: (id: number) => void | Promise<void>) {
  if (!historyId) return;
  Alert.alert('添加自选', '确定把这张卡片加入自选吗？', [
    {text: '取消', style: 'cancel'},
    {text: '确定', onPress: () => onConfirm(historyId)},
  ]);
}

function confirmCancelSelfSelect(historyId: number | null | undefined, onConfirm: (id: number) => void | Promise<void>) {
  if (!historyId) return;
  Alert.alert('移出自选', '确定把这张卡片移出自选吗？', [
    {text: '取消', style: 'cancel'},
    {text: '移出', style: 'destructive', onPress: () => onConfirm(historyId)},
  ]);
}

const styles = StyleSheet.create({
  container: {flex: 1, backgroundColor: colors.background},
  header: {
    height: 56,
    paddingHorizontal: 16,
    backgroundColor: '#FFFFFF',
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    borderBottomWidth: 1,
    borderBottomColor: colors.border,
  },
  headerButton: {width: 24, height: 24, alignItems: 'center', justifyContent: 'center'},
  headerTitle: {color: colors.text, fontSize: 18, fontWeight: '600'},
  headerActions: {flexDirection: 'row', alignItems: 'center', gap: 16},
  categoryTrigger: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 4,
    paddingHorizontal: 6,
    paddingVertical: 2,
  },
  categoryTriggerText: {color: colors.text, fontSize: 14, fontWeight: '600'},
  editText: {color: '#9DA4A3', fontSize: 14},
  editTextActive: {color: colors.primary, fontWeight: '600'},
  categoryMenu: {
    position: 'absolute',
    top: 50,
    right: 60,
    width: 90,
    borderRadius: 4,
    backgroundColor: '#FFFFFF',
    borderWidth: 1,
    borderColor: colors.border,
    overflow: 'hidden',
    zIndex: 20,
  },
  categoryMenuItem: {paddingHorizontal: 12, paddingVertical: 10},
  categoryMenuText: {color: colors.text, fontSize: 14},
  categoryMenuTextActive: {color: colors.primary, fontWeight: '600'},
  tabsRow: {
    flexDirection: 'row',
    backgroundColor: '#FFFFFF',
    paddingHorizontal: 16,
    paddingVertical: 8,
    gap: 24,
  },
  tabItem: {alignItems: 'center', gap: 4},
  tabIconRow: {flexDirection: 'row', alignItems: 'center', gap: 4},
  tabText: {color: '#3C4947', fontSize: 14},
  tabTextActive: {color: colors.primary, fontWeight: '600'},
  tabIndicator: {height: 3, width: 0, backgroundColor: 'transparent', borderRadius: 1.5},
  tabIndicatorActive: {width: 18, backgroundColor: colors.primary},
  scroll: {flex: 1},
  content: {padding: 16, paddingBottom: 32},
  gridRow: {flexDirection: 'row', gap: 12},
  gridColumn: {flex: 1, gap: 12},
  cardWrap: {gap: 6},
  actions: {flexDirection: 'row', gap: 8, justifyContent: 'flex-end'},
  actionButton: {
    height: 26,
    paddingHorizontal: 10,
    borderRadius: 4,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: '#EFF5F3',
    borderWidth: 1,
    borderColor: colors.border,
  },
  actionDanger: {backgroundColor: '#FFF4F2', borderColor: '#F3C8BF'},
  actionText: {color: colors.primary, fontSize: 11, fontWeight: '700'},
  actionDangerText: {color: colors.danger},
  loading: {marginTop: 32},
  empty: {marginTop: 48, textAlign: 'center', color: '#9DA4A3', fontSize: 14},
});
