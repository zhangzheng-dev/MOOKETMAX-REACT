import React, {useCallback, useMemo, useRef, useState} from 'react';
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
import {SvgXml} from 'react-native-svg';
import {useFocusEffect} from '@react-navigation/native';
import {useSafeAreaInsets} from 'react-native-safe-area-context';
import type {NativeStackScreenProps} from '@react-navigation/native-stack';
import DraggableFlatList, {ScaleDecorator} from 'react-native-draggable-flatlist';
import {mooketApi} from '../api/mooketApi';
import {ChevronDownIcon, ClockIcon, StarIcon} from '../components/common/AppIcons';
import {HomeCardSwitcher} from '../components/home/cards';
import {DEFAULT_CATEGORY} from '../config/env';
import type {RootStackParamList} from '../navigation/routes';
import {colors} from '../theme/colors';
import type {HomeCardItem, HomeCardsResponse} from '../types/api';
import {buildHomeCardSearchHistoryPayload, buildHomeFallbackExampleCards, getHomeCardEntityKey} from '../utils/homeFallbackCards';
import {openHomeCard} from '../utils/navigation';
import {getAddSelfSelectMessage, getRemoveSelfSelectMessage} from '../utils/selfSelectEntity';
import {
  applySavedSelfSelectCardOrder,
  getSelfSelectCardOrderKey,
  saveSelfSelectCardOrder,
} from '../utils/selfSelectCardOrder';

type Props = NativeStackScreenProps<RootStackParamList, 'HomeCards'>;

const categories = ['牛', '猪'];
const archiveAddIconXml = `<svg viewBox="0 0 16 16" fill="none" xmlns="http://www.w3.org/2000/svg"><path d="M9.51562 6.98914H6.23438" stroke="#171D1C" stroke-width="1.125" stroke-miterlimit="10" stroke-linecap="round" stroke-linejoin="round"/><path d="M7.875 5.38782V8.66907" stroke="#171D1C" stroke-width="1.125" stroke-miterlimit="10" stroke-linecap="round" stroke-linejoin="round"/><path d="M11.0382 1.3125H4.71196C3.31415 1.3125 2.17883 2.45438 2.17883 3.84563V13.0922C2.17883 14.2734 3.0254 14.7722 4.06227 14.2012L7.26477 12.4228C7.60602 12.2325 8.15727 12.2325 8.49196 12.4228L11.6945 14.2012C12.7313 14.7787 13.5779 14.28 13.5779 13.0922V3.84563C13.5713 2.45438 12.436 1.3125 11.0382 1.3125Z" stroke="#171D1C" stroke-width="1.125" stroke-linecap="round" stroke-linejoin="round"/></svg>`;
const archiveDelIconPath = "M12.6152 1.5C14.2125 1.50013 15.5098 2.80482 15.5176 4.39453V14.9629C15.5174 16.32 14.5499 16.8901 13.3652 16.2305L9.70508 14.1973C9.32267 13.9798 8.69274 13.9799 8.30273 14.1973L4.64258 16.2305C3.45775 16.8828 2.49042 16.3126 2.49023 14.9629V4.39453C2.49049 2.80482 3.78753 1.50012 5.38477 1.5H12.6152ZM7.125 7.4248C6.81756 7.4248 6.5626 7.67989 6.5625 7.9873C6.5625 8.2948 6.8175 8.5498 7.125 8.5498H10.875C11.1825 8.5498 11.4375 8.2948 11.4375 7.9873C11.4374 7.67989 11.1824 7.4248 10.875 7.4248H7.125Z";

export function HomeCardsScreen({navigation, route}: Props) {
  const insets = useSafeAreaInsets();
  const [category, setCategory] = useState(route.params?.category ?? DEFAULT_CATEGORY);
  const [tab, setTab] = useState<0 | 1>(route.params?.tab ?? 0);
  const [cards, setCards] = useState<HomeCardItem[]>([]);
  const [loading, setLoading] = useState(false);
  const [editMode, setEditMode] = useState(false);
  const [menuOpen, setMenuOpen] = useState(false);
  const [dismissedExampleKeys, setDismissedExampleKeys] = useState<Set<string>>(new Set());
  const [promotedExampleKeys, setPromotedExampleKeys] = useState<Set<string>>(new Set());
  const autoTabSwitchReadyRef = useRef(true);

  const loadFn = useCallback(async (cat: string, t: 0 | 1): Promise<HomeCardsResponse> => {
    const [selfSelectData, recentData] = await Promise.all([
      mooketApi.getSelfSelectCards(cat),
      mooketApi.getRecentSearchCards(cat),
    ]);
    const selfSelectCards = selfSelectData.cards ?? [];
    const recentCards = recentData.cards ?? [];

    if (autoTabSwitchReadyRef.current && selfSelectCards.length === 0) {
      autoTabSwitchReadyRef.current = false;
      if (t !== 1) {
        setTab(1);
      }
      return {
        cards: recentCards.length > 0
          ? recentCards
          : buildHomeFallbackExampleCards((await mooketApi.getHomeCards(cat, 0)).cards ?? [], promotedExampleKeys, dismissedExampleKeys),
        updateTime: recentData.updateTime ?? null,
      };
    }

    autoTabSwitchReadyRef.current = false;

    return {
      cards:
        t === 0
          ? selfSelectCards
          : recentCards.length > 0
            ? recentCards
            : buildHomeFallbackExampleCards(
                (await mooketApi.getHomeCards(cat, 0)).cards ?? [],
                promotedExampleKeys,
                dismissedExampleKeys,
              ),
      updateTime: (t === 0 ? selfSelectData.updateTime : recentData.updateTime) ?? null,
    };
  }, [dismissedExampleKeys, promotedExampleKeys]);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const result = await loadFn(category, tab);
      const nextCards = result.cards ?? [];
      setCards(tab === 0 ? await applySavedSelfSelectCardOrder(category, nextCards) : nextCards);
    } finally {
      setLoading(false);
    }
  }, [category, loadFn, tab]);

  const handleDragEnd = useCallback(
    ({data}: {data: HomeCardItem[]}) => {
      setCards(data);
      saveSelfSelectCardOrder(category, data).catch(() => undefined);
    },
    [category],
  );

  const handleEditToggle = useCallback(() => {
    setEditMode(prev => {
      const next = !prev;
      if (prev && !next) {
        load().catch(() => undefined);
      }
      return next;
    });
  }, [load]);

  useFocusEffect(
    useCallback(() => {
      load().catch(() => undefined);
    }, [load]),
  );

  function switchCategory(value: string) {
    setCategory(value);
    setMenuOpen(false);
    setEditMode(false);
    setDismissedExampleKeys(new Set());
    setPromotedExampleKeys(new Set());
    autoTabSwitchReadyRef.current = true;
  }

  function switchTab(value: 0 | 1) {
    setTab(value);
    setEditMode(false);
    autoTabSwitchReadyRef.current = false;
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

  async function addExampleCardToSelfSelect(card: HomeCardItem) {
    const payload = buildHomeCardSearchHistoryPayload(card);
    const entityKey = card.exampleEntityKey ?? getHomeCardEntityKey(card);
    if (!payload || !entityKey) return;

    try {
      await mooketApi.saveSearchHistory({...payload, isSelfSelect: 1});
      setPromotedExampleKeys(prev => {
        const next = new Set(prev);
        next.add(entityKey);
        return next;
      });
      setCards(prev =>
        prev.filter(item => (item.exampleEntityKey ?? getHomeCardEntityKey(item)) !== entityKey),
      );
    } catch (error) {
      Alert.alert('添加失败', error instanceof Error ? error.message : '请稍后重试');
    }
  }

  function dismissExampleCard(card: HomeCardItem) {
    const entityKey = card.exampleEntityKey ?? getHomeCardEntityKey(card);
    if (!entityKey) return;
    setDismissedExampleKeys(prev => {
      const next = new Set(prev);
      next.add(entityKey);
      return next;
    });
    setCards(prev => prev.filter(item => (item.exampleEntityKey ?? getHomeCardEntityKey(item)) !== entityKey));
  }

  const {leftColumn, rightColumn} = useMemo(() => splitColumns(cards), [cards]);

  return (
    <View style={styles.container}>
      <View style={[styles.header, {paddingTop: insets.top, minHeight: insets.top + 56}]}>
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
          <Pressable hitSlop={8} onPress={handleEditToggle}>
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

      {editMode && tab === 0 && cards.length > 0 ? (
        <DraggableFlatList
          data={cards}
          numColumns={2}
          keyExtractor={(item, index) => getSelfSelectCardOrderKey(item) ?? `card-${index}`}
          contentContainerStyle={styles.content}
          columnWrapperStyle={styles.draggableRow}
          onDragEnd={handleDragEnd}
          renderItem={({item, drag, isActive}) => (
            <View style={styles.draggableCell}>
              <ScaleDecorator activeScale={1.03}>
                <View style={[styles.draggableCardWrap, isActive && styles.draggingCard]}>
                  <CardWithActions
                    card={item}
                    category={category}
                    tab={tab}
                    editMode={editMode}
                    onPress={() => undefined}
                    onLongPress={drag}
                    onDelete={() => confirmDelete(item.historyId, deleteHistory)}
                    onMoveToSelfSelect={() => confirmMoveToSelfSelect(item, moveToSelfSelect)}
                    onCancelSelfSelect={() => confirmCancelSelfSelect(item, cancelSelfSelect)}
                  />
                </View>
              </ScaleDecorator>
            </View>
          )}
        />
      ) : (
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
                  onDelete={() => (card.isExample ? confirmDeleteExample(card, dismissExampleCard) : confirmDelete(card.historyId, deleteHistory))}
                  onMoveToSelfSelect={() => (card.isExample ? confirmMoveExampleToSelfSelect(card, addExampleCardToSelfSelect) : confirmMoveToSelfSelect(card, moveToSelfSelect))}
                  onCancelSelfSelect={() => confirmCancelSelfSelect(card, cancelSelfSelect)}
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
                  onDelete={() => (card.isExample ? confirmDeleteExample(card, dismissExampleCard) : confirmDelete(card.historyId, deleteHistory))}
                  onMoveToSelfSelect={() => (card.isExample ? confirmMoveExampleToSelfSelect(card, addExampleCardToSelfSelect) : confirmMoveToSelfSelect(card, moveToSelfSelect))}
                  onCancelSelfSelect={() => confirmCancelSelfSelect(card, cancelSelfSelect)}
                />
              ))}
            </View>
          </View>
        )}
        </ScrollView>
      )}
    </View>
  );
}

function CardWithActions({
  card,
  tab,
  editMode,
  onPress,
  onLongPress,
  onDelete,
  onMoveToSelfSelect,
  onCancelSelfSelect,
}: {
  card: HomeCardItem;
  category: string;
  tab: 0 | 1;
  editMode: boolean;
  onPress: () => void;
  onLongPress?: () => void;
  onDelete: () => void;
  onMoveToSelfSelect: () => void;
  onCancelSelfSelect: () => void;
}) {
  return (
    <View style={styles.cardWrap}>
      <HomeCardSwitcher card={card} onPress={onPress} onLongPress={onLongPress} />
      {card.isExample && !editMode ? (
        <View style={styles.exampleBadge}>
          <Text style={styles.exampleBadgeText}>例</Text>
        </View>
      ) : null}
      {editMode && (card.historyId || (tab === 1 && card.isExample)) ? (
        <>
          {tab === 0 ? (
            <Pressable hitSlop={4} onPress={onCancelSelfSelect} style={styles.editIconSingle}>
              <Svg width={16} height={16} viewBox="0 0 18 18" fill="none">
                <Path d={archiveDelIconPath} fill="#006A61" />
              </Svg>
            </Pressable>
          ) : (
            <View style={styles.editIconColumn}>
              <Pressable hitSlop={4} onPress={onMoveToSelfSelect} style={styles.editIconAdd}>
                <SvgXml xml={archiveAddIconXml} width={16} height={16} />
              </Pressable>
              <Pressable hitSlop={4} onPress={onDelete} style={styles.editIconDel}>
                <Svg width={16} height={16} viewBox="0 0 18 18" fill="none">
                  <Path d={archiveDelIconPath} fill="#006A61" />
                </Svg>
              </Pressable>
            </View>
          )}
        </>
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

function confirmDeleteExample(card: HomeCardItem, onConfirm: (card: HomeCardItem) => void) {
  Alert.alert('删除示例', '确定删除这张示例卡片吗？', [
    {text: '取消', style: 'cancel'},
    {text: '删除', style: 'destructive', onPress: () => onConfirm(card)},
  ]);
}

function confirmMoveToSelfSelect(card: HomeCardItem, onConfirm: (id: number) => void | Promise<void>) {
  const historyId = card.historyId;
  if (!historyId) return;
  Alert.alert('加入自选', getAddSelfSelectMessage(card), [
    {text: '取消', style: 'cancel'},
    {text: '确定', onPress: () => onConfirm(historyId)},
  ]);
}

function confirmMoveExampleToSelfSelect(
  card: HomeCardItem,
  onConfirm: (card: HomeCardItem) => void | Promise<void>,
) {
  Alert.alert('加入自选', getAddSelfSelectMessage(card), [
    {text: '取消', style: 'cancel'},
    {text: '确定', onPress: () => onConfirm(card)},
  ]);
}

function confirmCancelSelfSelect(card: HomeCardItem, onConfirm: (id: number) => void | Promise<void>) {
  const historyId = card.historyId;
  if (!historyId) return;
  Alert.alert('移出自选', getRemoveSelfSelectMessage(card), [
    {text: '取消', style: 'cancel'},
    {text: '移出', style: 'destructive', onPress: () => onConfirm(historyId)},
  ]);
}

const styles = StyleSheet.create({
  container: {flex: 1, backgroundColor: colors.background},
  header: {
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
  draggableRow: {gap: 12},
  draggableCell: {flex: 1, maxWidth: '48.5%'},
  draggableCardWrap: {width: '100%', marginBottom: 12},
  draggingCard: {opacity: 0.94, elevation: 8},
  gridRow: {flexDirection: 'row', gap: 12},
  gridColumn: {flex: 1, gap: 12},
  cardWrap: {gap: 6, position: 'relative'},
  exampleBadge: {
    position: 'absolute',
    top: 4,
    right: 4,
    width: 20,
    height: 20,
    borderRadius: 10,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: 'rgba(0,106,97,0.85)',
    zIndex: 2,
  },
  exampleBadgeText: {color: '#FFFFFF', fontSize: 9, fontWeight: '700'},
  editIconColumn: {
    position: 'absolute',
    top: 0,
    right: 0,
    alignItems: 'center',
    zIndex: 2,
  },
  editIconAdd: {
    width: 28,
    height: 28,
    borderTopRightRadius: 8,
    borderBottomLeftRadius: 4,
    backgroundColor: 'rgba(0,0,0,0.04)',
    alignItems: 'center',
    justifyContent: 'center',
  },
  editIconDel: {
    width: 28,
    height: 28,
    borderBottomLeftRadius: 4,
    borderBottomRightRadius: 4,
    backgroundColor: 'rgba(0,106,97,0.06)',
    alignItems: 'center',
    justifyContent: 'center',
    marginTop: 2,
  },
  editIconSingle: {
    position: 'absolute',
    top: 0,
    right: 0,
    width: 28,
    height: 28,
    borderTopRightRadius: 8,
    borderBottomLeftRadius: 4,
    backgroundColor: 'rgba(0,0,0,0.04)',
    alignItems: 'center',
    justifyContent: 'center',
    zIndex: 2,
  },
  loading: {marginTop: 32},
  empty: {marginTop: 48, textAlign: 'center', color: '#9DA4A3', fontSize: 14},
});
