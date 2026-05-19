import React, {useCallback, useMemo, useState} from 'react';
import {Alert, FlatList, Pressable, StyleSheet, Text, TextInput, View} from 'react-native';
import {useFocusEffect} from '@react-navigation/native';
import type {NativeStackScreenProps} from '@react-navigation/native-stack';
import {mooketApi} from '../api/mooketApi';
import {sessionStore} from '../store/sessionStore';
import {DeleteIcon, HistoryIcon, SearchIcon} from '../components/common/AppIcons';
import {ArrowLeftIcon, ClearInputIcon} from '../components/login/LoginIcons';
import type {RootStackParamList} from '../navigation/routes';
import {colors} from '../theme/colors';
import type {SearchHistory, SearchSuggest} from '../types/api';

type Props = NativeStackScreenProps<RootStackParamList, 'Search'>;

const examples: Array<[string, string]> = [
  ['巴西', '查找国家相关信息'],
  ['SIF504', '查找厂号'],
  ['牛腱', '查找产品'],
  ['巴西 牛腱', '组合搜索'],
  ['JBS 牛腱', '品牌+产品搜索'],
  ['河南冠乐牛食品有限公司', '查找商家'],
];

export function SearchScreen({route, navigation}: Props) {
  const {category} = route.params;
  const [keyword, setKeyword] = useState('');
  const [suggestions, setSuggestions] = useState<SearchSuggest[]>([]);
  const [histories, setHistories] = useState<SearchHistory[]>([]);
  const [loading, setLoading] = useState(false);

  const loadHistories = useCallback(async () => {
    try {
      const next = await mooketApi.getRecentSearches(12);
      setHistories(next);
    } catch {
      setHistories([]);
    }
  }, []);

  useFocusEffect(
    useCallback(() => {
      loadHistories().catch(() => undefined);
    }, [loadHistories]),
  );

  useFocusEffect(
    useCallback(() => {
      const value = keyword.trim();
      if (!value) {
        setSuggestions([]);
        setLoading(false);
        return undefined;
      }
      setLoading(true);
      const timer = setTimeout(() => {
        mooketApi
          .getSearchSuggestions(category, value)
          .then(result => {
            setSuggestions(result);
            setLoading(false);
          })
          .catch(() => {
            setSuggestions([]);
            setLoading(false);
          });
      }, 250);
      return () => clearTimeout(timer);
    }, [category, keyword]),
  );

  const historyWords = useMemo(
    () => uniqueStrings(histories.map(item => item.searchWord).filter(Boolean)),
    [histories],
  );

  async function handleSelect(item: SearchSuggest) {
    const parts = item.text.split(/\s+/).filter(Boolean);
    try {
      await mooketApi.saveSearchHistory({
        searchWord: item.text,
        searchType: item.type,
        productId: item.matchType === 'product' ? item.targetId : null,
        productName: item.matchType === 'product' ? item.text : null,
        country: ['country', 'factory', 'combined'].includes(item.matchType) ? parts[0] : null,
        factoryNo: ['factory', 'combined'].includes(item.matchType) ? parts[1] : null,
        brandId: item.matchType === 'brand' && item.type !== '品牌+产品' ? item.targetId : null,
        merchantId: item.matchType === 'merchant' ? item.targetId : null,
      });
      // Reload histories after successful save
      await loadHistories();
    } catch (err) {
      // Show error to user for debugging
      Alert.alert('保存搜索记录失败', err instanceof Error ? err.message : String(err));
    }
    navigateSuggestion(item, parts);
  }

  function navigateSuggestion(item: SearchSuggest, parts: string[]) {
    switch (item.matchType) {
      case 'merchant':
        navigation.navigate('Merchant', {merchantId: item.targetId, category});
        return;
      case 'product':
        navigation.navigate('Product', {productId: item.targetId, category, productName: item.text});
        return;
      case 'country':
        navigation.navigate('Country', {country: item.text, category});
        return;
      case 'brand':
        if (item.type === '品牌+产品' && parts.length >= 2) {
          navigation.navigate('BrandProduct', {
            brandName: parts[0],
            productName: parts.slice(1).join(' '),
            category,
          });
        } else {
          navigation.navigate('Brand', {brandName: item.text, category});
        }
        return;
      case 'factory':
        if (parts.length >= 2) {
          navigation.navigate('Factory', {country: parts[0], factoryNo: parts[1], category});
        }
        return;
      case 'combined':
        if (item.type === '国家+产品' && parts.length >= 2) {
          navigation.navigate('CountryProduct', {
            country: parts[0],
            productName: parts.slice(1).join(' '),
            category,
          });
        } else if (parts.length >= 3) {
          navigation.navigate('CountryFactoryProduct', {
            country: parts[0],
            factoryNo: parts[1],
            productName: parts.slice(2).join(' '),
            category,
          });
        }
        return;
      default:
        if (item.type === '国家+产品' && parts.length >= 2) {
          navigation.navigate('CountryProduct', {
            country: parts[0],
            productName: parts.slice(1).join(' '),
            category,
          });
        }
    }
  }

  async function clearHistories() {
    const ids = histories.map(item => item.historyId).filter((id): id is number => Number.isFinite(id));
    if (ids.length === 0) {
      setHistories([]);
      return;
    }
    try {
      await mooketApi.batchDeleteSearchHistory(ids);
      setHistories([]);
    } catch {
      Alert.alert('清除失败', '请稍后重试');
    }
  }

  function confirmClearHistories() {
    if (histories.length === 0) return;
    Alert.alert('清除全部', '确定清除最近搜索记录吗？', [
      {text: '取消', style: 'cancel'},
      {text: '清除', style: 'destructive', onPress: () => clearHistories().catch(() => undefined)},
    ]);
  }

  return (
    <View style={styles.container}>
      <View style={styles.topBar}>
        <Pressable hitSlop={8} onPress={() => navigation.goBack()} style={styles.backButton}>
          <ArrowLeftIcon size={18} />
        </Pressable>
        <View style={styles.searchInputWrap}>
          <SearchIcon size={16} color="#ADB7B5" />
          <TextInput
            autoFocus
            value={keyword}
            onChangeText={setKeyword}
            placeholder="搜索国家、厂号、产品、商家、品牌"
            placeholderTextColor="#ADB7B5"
            style={styles.input}
          />
          {keyword.length > 0 ? (
            <Pressable hitSlop={8} onPress={() => setKeyword('')} style={styles.inputClear}>
              <ClearInputIcon size={16} />
            </Pressable>
          ) : null}
        </View>
      </View>

      {keyword.trim() ? (
        <FlatList
          data={suggestions}
          keyExtractor={(item, index) => `${item.type}-${item.targetId}-${index}`}
          keyboardShouldPersistTaps="handled"
          renderItem={({item}) => (
            <SuggestionItem item={item} keyword={keyword} onPress={() => handleSelect(item)} />
          )}
          ListEmptyComponent={
            loading ? (
              <Text style={styles.loadingText}>搜索中...</Text>
            ) : (
              <Text style={styles.empty}>未找到相关结果</Text>
            )
          }
        />
      ) : (
        <FlatList
          data={historyWords}
          keyExtractor={(item, index) => `${item}-${index}`}
          ListHeaderComponent={
            <View>
              {historyWords.length > 0 ? (
                <>
                  <View style={styles.historyHeader}>
                    <Text style={styles.historyTitle}>最近搜索</Text>
                    <Pressable style={styles.clearAllWrap} onPress={confirmClearHistories}>
                      <Text style={styles.clearAllText}>清除全部</Text>
                      <DeleteIcon />
                    </Pressable>
                  </View>
                  <View style={styles.historyWrap}>
                    {historyWords.map((item, index) => (
                      <Pressable
                        key={`${item}-${index}`}
                        onPress={() => {
                          // 去掉别名后缀，确保搜索联想词能匹配
                          const standard = item.includes('(别名：')
                            ? item.substring(0, item.indexOf('(别名：'))
                            : item;
                          setKeyword(standard);
                        }}
                        style={styles.historyChip}>
                        <HistoryIcon />
                        <Text style={styles.historyChipText} numberOfLines={1}>
                          {item}
                        </Text>
                      </Pressable>
                    ))}
                  </View>
                </>
              ) : null}

              <View style={styles.helperWrap}>
                <Text style={styles.helperTitle}>
                  输入<Text style={styles.helperKeyword}>关键词</Text>开始搜索
                </Text>
                <Text style={styles.helperDesc}>支持搜索国家、厂号、产品、商家、品牌</Text>
              </View>

              <View style={styles.examplesWrap}>
                <Text style={styles.examplesTitle}>搜索示例</Text>
                {examples.map(([word, desc]) => (
                  <View key={word} style={styles.exampleRow}>
                    <SearchIcon size={14} color="#171D1C" />
                    <View style={styles.exampleDivider} />
                    <Text style={styles.exampleWord}>{word}</Text>
                    <Text style={styles.exampleDesc}>· {desc}</Text>
                  </View>
                ))}
              </View>
            </View>
          }
          renderItem={() => null}
        />
      )}
    </View>
  );
}

function SuggestionItem({
  item,
  keyword,
  onPress,
}: {
  item: SearchSuggest;
  keyword: string;
  onPress: () => void;
}) {
  const {main, alias} = parseSuggestionText(item.text);
  return (
    <Pressable onPress={onPress} style={styles.resultRow}>
      <View style={styles.resultMain}>
        <SearchIcon size={16} color="#ADB7B5" />
        <View style={styles.resultText}>
          <Text style={styles.resultPrimary} numberOfLines={1}>
            {renderHighlight(main, keyword)}
          </Text>
          {alias ? <Text style={styles.resultAlias}>（{alias}）</Text> : null}
        </View>
      </View>
      <Text style={styles.resultType}>{item.type}</Text>
    </Pressable>
  );
}

/**
 * 解析"标准品名(别名：XXX)"格式
 */
function parseSuggestionText(text: string): {main: string; alias: string | null} {
  const match = text.match(/^(.*?)\(?[（(]别名[:：]\s*([^)）]+)[)）]?$/);
  if (match) {
    return {main: match[1].trim(), alias: match[2].trim()};
  }
  return {main: text, alias: null};
}

/**
 * 关键词命中高亮（不区分大小写）
 */
function renderHighlight(text: string, keyword: string): React.ReactNode[] {
  const trimmed = keyword.trim();
  if (!trimmed) return [text];
  const lowerText = text.toLowerCase();
  const lowerKeyword = trimmed.toLowerCase();
  const result: React.ReactNode[] = [];
  let cursor = 0;
  let index = lowerText.indexOf(lowerKeyword, cursor);
  while (index >= 0) {
    if (index > cursor) result.push(text.slice(cursor, index));
    result.push(
      <Text key={`hi-${index}`} style={styles.highlight}>
        {text.slice(index, index + trimmed.length)}
      </Text>,
    );
    cursor = index + trimmed.length;
    index = lowerText.indexOf(lowerKeyword, cursor);
  }
  if (cursor < text.length) result.push(text.slice(cursor));
  return result;
}

function uniqueStrings(items: string[]): string[] {
  const set = new Set<string>();
  const out: string[] = [];
  for (const item of items) {
    if (!set.has(item)) {
      set.add(item);
      out.push(item);
    }
  }
  return out;
}

const styles = StyleSheet.create({
  container: {flex: 1, backgroundColor: colors.background, paddingTop: 8},
  topBar: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 16,
    paddingBottom: 12,
    backgroundColor: '#FFFFFF',
  },
  backButton: {
    width: 24,
    height: 24,
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 8,
  },
  searchInputWrap: {
    flex: 1,
    height: 40,
    borderRadius: 4,
    backgroundColor: '#EFF5F3',
    borderWidth: 1,
    borderColor: 'rgba(187,202,198,0.3)',
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 10,
  },
  input: {
    flex: 1,
    marginLeft: 8,
    color: colors.text,
    fontSize: 14,
    paddingVertical: 0,
  },
  inputClear: {width: 18, height: 18, alignItems: 'center', justifyContent: 'center'},
  resultRow: {
    minHeight: 52,
    paddingHorizontal: 16,
    paddingVertical: 16,
    backgroundColor: '#FFFFFF',
    borderBottomWidth: StyleSheet.hairlineWidth,
    borderBottomColor: '#EFF5F3',
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
  },
  resultMain: {flexDirection: 'row', alignItems: 'center', flex: 1, gap: 8},
  resultText: {flex: 1},
  resultPrimary: {color: colors.text, fontSize: 15},
  resultAlias: {marginTop: 2, color: '#9DA4A3', fontSize: 12},
  resultType: {color: '#3C4947', fontSize: 12},
  highlight: {color: colors.primary, fontWeight: '600'},
  loadingText: {marginTop: 32, textAlign: 'center', color: '#9DA4A3', fontSize: 12},
  empty: {marginTop: 32, color: colors.textMuted, textAlign: 'center', fontSize: 12},
  historyHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingHorizontal: 16,
    paddingTop: 12,
    paddingBottom: 10,
  },
  historyTitle: {color: '#3C4947', fontSize: 14, fontWeight: '500'},
  clearAllWrap: {flexDirection: 'row', alignItems: 'center', gap: 4},
  clearAllText: {color: '#9DA4A3', fontSize: 12},
  historyWrap: {flexDirection: 'row', flexWrap: 'wrap', gap: 8, paddingHorizontal: 16},
  historyChip: {
    flexDirection: 'row',
    alignItems: 'center',
    borderRadius: 4,
    backgroundColor: '#EFF5F3',
    borderWidth: 1,
    borderColor: 'rgba(187,202,198,0.5)',
    paddingHorizontal: 10,
    paddingVertical: 6,
    gap: 4,
  },
  historyChipText: {color: colors.text, fontSize: 12, maxWidth: 180},
  helperWrap: {alignItems: 'center', marginTop: 32},
  helperTitle: {color: colors.text, fontSize: 16},
  helperKeyword: {color: colors.primary, fontWeight: '700'},
  helperDesc: {marginTop: 4, color: '#3C4947', fontSize: 12},
  examplesWrap: {marginTop: 22, alignItems: 'center', paddingBottom: 32, gap: 8},
  examplesTitle: {color: '#000000', fontSize: 12, fontWeight: '500', marginBottom: 4},
  exampleRow: {
    flexDirection: 'row',
    alignItems: 'center',
    borderRadius: 2,
    borderWidth: 1,
    borderColor: '#DEE4E1',
    backgroundColor: '#FFFFFF',
    paddingHorizontal: 8,
    paddingVertical: 6,
  },
  exampleDivider: {
    width: 0.5,
    height: 7,
    backgroundColor: '#3C4947',
    marginHorizontal: 4,
  },
  exampleWord: {color: '#171D1C', fontSize: 12, fontWeight: '500'},
  exampleDesc: {color: '#3C4947', fontSize: 12, marginLeft: 4},
});
