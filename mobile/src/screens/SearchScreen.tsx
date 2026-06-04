import React, {useCallback, useMemo, useState} from 'react';
import {Alert, FlatList, Keyboard, Pressable, StyleSheet, Text, TextInput, View} from 'react-native';
import {useFocusEffect} from '@react-navigation/native';
import {useSafeAreaInsets} from 'react-native-safe-area-context';
import type {NativeStackScreenProps} from '@react-navigation/native-stack';
import {mooketApi} from '../api/mooketApi';
import {ChevronDownIcon, DeleteIcon, HistoryIcon, SearchIcon} from '../components/common/AppIcons';
import {ArrowLeftIcon, ClearInputIcon} from '../components/login/LoginIcons';
import type {RootStackParamList} from '../navigation/routes';
import {colors} from '../theme/colors';
import type {SearchHistory, SearchSuggest} from '../types/api';

type Props = NativeStackScreenProps<RootStackParamList, 'Search'>;

const categoryOptions = ['牛', '猪'] as const;

const examples: Array<[string, string]> = [
  ['巴西', '查找国家相关信息'],
  ['SIF504', '查找厂号'],
  ['牛腱', '查找产品'],
  ['巴西 牛腱', '组合搜索'],
  ['JBS 牛腱', '品牌加产品搜索'],
  ['河南冠乐食品有限公司', '查找商家'],
];

export function SearchScreen({route, navigation}: Props) {
  const {category} = route.params;
  const insets = useSafeAreaInsets();
  const [selectedCategory, setSelectedCategory] = useState<(typeof categoryOptions)[number]>(
    categoryOptions.includes(category as (typeof categoryOptions)[number])
      ? (category as (typeof categoryOptions)[number])
      : '牛',
  );
  const [categoryMenuOpen, setCategoryMenuOpen] = useState(false);
  const [keyword, setKeyword] = useState('');
  const [isInputFocused, setIsInputFocused] = useState(false);
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
          .getSearchSuggestions(selectedCategory, value)
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
    }, [selectedCategory, keyword]),
  );

  const historyEntries = useMemo(
    () => uniqueBySearchWord(histories.filter(item => item.searchWord)),
    [histories],
  );

  async function handleSelect(item: SearchSuggest) {
    Keyboard.dismiss();
    const parts = item.text.split(/\s+/).filter(Boolean);
    const country = item.country ?? getCountryFromText(parts[0]);
    const factoryNo = item.factoryNo ?? getFactoryFromText(parts[1]);
    const productName = item.productName ?? getProductFromSuggestion(item, parts);
    const brandName = item.brandName ?? getBrandFromSuggestion(item, parts);
    try {
      await mooketApi.saveSearchHistory({
        searchWord: item.text,
        searchType: item.type,
        productId: item.matchType === 'product' ? item.targetId : null,
        productName,
        country: ['country', 'factory', 'combined'].includes(item.matchType) ? country : null,
        factoryNo: ['factory', 'combined'].includes(item.matchType) ? factoryNo : null,
        brandId: item.matchType === 'brand' && item.type !== '品牌+产品' ? item.targetId : null,
        merchantId: item.matchType === 'merchant' ? item.targetId : null,
      });
      await loadHistories();
    } catch (err) {
      Alert.alert('保存搜索记录失败', err instanceof Error ? err.message : String(err));
    }
    navigateSuggestion(item, parts, {country, factoryNo, productName, brandName});
  }

  function handleHistorySelect(history: SearchHistory) {
    Keyboard.dismiss();
    const searchWord = getStandardSearchWord(history.searchWord);

    if (history.merchantId) {
      navigation.navigate('Merchant', {merchantId: history.merchantId, category: selectedCategory});
      return;
    }

    if (history.country && history.factoryNo && history.productName) {
      navigation.navigate('CountryFactoryProduct', {
        country: history.country,
        factoryNo: history.factoryNo,
        productName: history.productName,
        category: selectedCategory,
      });
      return;
    }

    if (history.country && history.productName) {
      navigation.navigate('CountryProduct', {
        country: history.country,
        productName: history.productName,
        category: selectedCategory,
      });
      return;
    }

    if (history.country && history.factoryNo) {
      navigation.navigate('Factory', {country: history.country, factoryNo: history.factoryNo, category: selectedCategory});
      return;
    }

    if (history.productId) {
      navigation.navigate('Product', {
        productId: history.productId,
        category: selectedCategory,
        productName: history.productName ?? searchWord,
      });
      return;
    }

    if (history.brandId) {
      navigation.navigate('Brand', {brandName: searchWord, category: selectedCategory});
      return;
    }

    if (history.country) {
      navigation.navigate('Country', {country: history.country, category: selectedCategory});
      return;
    }

    setKeyword(searchWord);
  }

  function navigateSuggestion(
    item: SearchSuggest,
    parts: string[],
    standard: {
      country?: string | null;
      factoryNo?: string | null;
      productName?: string | null;
      brandName?: string | null;
    },
  ) {
    switch (item.matchType) {
      case 'merchant':
        navigation.navigate('Merchant', {merchantId: item.targetId, category: selectedCategory});
        return;
      case 'product':
        navigation.navigate('Product', {productId: item.targetId, category: selectedCategory, productName: standard.productName ?? item.text});
        return;
      case 'country':
        navigation.navigate('Country', {country: standard.country ?? getStandardSearchWord(item.text), category: selectedCategory});
        return;
      case 'brand':
        if (item.type === '品牌+产品' && (standard.brandName || parts.length >= 2)) {
          navigation.navigate('BrandProduct', {
            brandName: standard.brandName ?? parts[0],
            productName: standard.productName ?? parts.slice(1).join(' '),
            category: selectedCategory,
          });
        } else {
          navigation.navigate('Brand', {brandName: standard.brandName ?? getStandardSearchWord(item.text), category: selectedCategory});
        }
        return;
      case 'factory':
        if (standard.country && standard.factoryNo) {
          navigation.navigate('Factory', {country: standard.country, factoryNo: standard.factoryNo, category: selectedCategory});
        } else if (parts.length >= 2) {
          navigation.navigate('Factory', {country: getCountryFromText(parts[0]), factoryNo: parts[1], category: selectedCategory});
        }
        return;
      case 'combined':
        if (item.type === '国家+产品' && (standard.country || parts.length >= 2)) {
          navigation.navigate('CountryProduct', {
            country: standard.country ?? getCountryFromText(parts[0]),
            productName: standard.productName ?? parts.slice(1).join(' '),
            category: selectedCategory,
          });
        } else if (standard.country && standard.factoryNo && standard.productName) {
          navigation.navigate('CountryFactoryProduct', {
            country: standard.country,
            factoryNo: standard.factoryNo,
            productName: standard.productName,
            category: selectedCategory,
          });
        } else if (parts.length >= 3) {
          navigation.navigate('CountryFactoryProduct', {
            country: getCountryFromText(parts[0]),
            factoryNo: parts[1],
            productName: parts.slice(2).join(' '),
            category: selectedCategory,
          });
        }
        return;
      default:
        if (item.type === '国家+产品' && parts.length >= 2) {
          navigation.navigate('CountryProduct', {
            country: standard.country ?? getCountryFromText(parts[0]),
            productName: standard.productName ?? parts.slice(1).join(' '),
            category: selectedCategory,
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
      <View style={[styles.topBar, {paddingTop: insets.top + 8, minHeight: insets.top + 60}]}>
        <Pressable hitSlop={8} onPress={() => navigation.goBack()} style={styles.backButton}>
          <ArrowLeftIcon size={18} />
        </Pressable>
        <View style={styles.searchInputWrap}>
          <Pressable onPress={() => setCategoryMenuOpen(prev => !prev)} style={styles.categoryButton}>
            <Text style={styles.categoryText}>{selectedCategory}</Text>
            <ChevronDownIcon size={14} color="#3C4947" />
          </Pressable>
          <View style={styles.searchDivider} />
          {!isInputFocused ? <SearchIcon size={16} color="#ADB7B5" /> : null}
          <TextInput
            autoFocus
            value={keyword}
            onFocus={() => setIsInputFocused(true)}
            onBlur={() => setIsInputFocused(false)}
            onChangeText={text => {
              setKeyword(text);
              setCategoryMenuOpen(false);
            }}
            placeholder="搜索国家、厂号、产品、商家、品牌"
            placeholderTextColor="#ADB7B5"
            style={[styles.input, isInputFocused && styles.inputFocused]}
          />
          {keyword.length > 0 ? (
            <Pressable hitSlop={8} onPress={() => setKeyword('')} style={styles.inputClear}>
              <ClearInputIcon size={16} />
            </Pressable>
          ) : null}
        </View>
      </View>

      {categoryMenuOpen ? (
        <>
          <Pressable style={styles.categoryMenuBackdrop} onPress={() => setCategoryMenuOpen(false)} />
          <View style={[styles.categoryMenu, {top: insets.top + 56}]}>
            {categoryOptions.map(item => (
              <Pressable
                key={item}
                onPress={() => {
                  setSelectedCategory(item);
                  setCategoryMenuOpen(false);
                  setSuggestions([]);
                }}
                style={[styles.categoryMenuItem, item === selectedCategory && styles.categoryMenuItemActive]}>
                <Text style={[styles.categoryMenuText, item === selectedCategory && styles.categoryMenuTextActive]}>
                  {item}
                </Text>
              </Pressable>
            ))}
          </View>
        </>
      ) : null}

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
              <Text style={styles.empty}>当前'{selectedCategory}'大类下暂未找到相关内容</Text>
            )
          }
        />
      ) : (
        <FlatList
          data={historyEntries}
          keyExtractor={(item, index) => `${item.searchWord}-${index}`}
          keyboardShouldPersistTaps="always"
          ListHeaderComponent={
            <View>
              {historyEntries.length > 0 ? (
                <>
                  <View style={styles.historyHeader}>
                    <Text style={styles.historyTitle}>最近搜索</Text>
                    <Pressable style={styles.clearAllWrap} onPress={confirmClearHistories}>
                      <Text style={styles.clearAllText}>清除全部</Text>
                      <DeleteIcon />
                    </Pressable>
                  </View>
                  <View style={styles.historyWrap}>
                    {historyEntries.map((item, index) => (
                      <Pressable
                        key={`${item.searchWord}-${index}`}
                        onPress={() => handleHistorySelect(item)}
                        style={styles.historyChip}>
                        <HistoryIcon />
                        <Text style={styles.historyChipText} numberOfLines={1}>
                          {item.searchWord}
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
                    <Text style={styles.exampleDesc}>- {desc}</Text>
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
          {alias ? <Text style={styles.resultAlias}>(别名：{alias})</Text> : null}
        </View>
      </View>
      <Text style={styles.resultType}>{item.type}</Text>
    </Pressable>
  );
}

function parseSuggestionText(text: string): {main: string; alias: string | null} {
  const aliasMarker = '(别名：';
  const index = text.indexOf(aliasMarker);
  if (index >= 0 && text.endsWith(')')) {
    return {
      main: text.slice(0, index).trim(),
      alias: text.slice(index + aliasMarker.length, -1).trim(),
    };
  }
  return {main: text, alias: null};
}

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

function uniqueBySearchWord(items: SearchHistory[]): SearchHistory[] {
  const set = new Set<string>();
  const out: SearchHistory[] = [];
  for (const item of items) {
    if (!set.has(item.searchWord)) {
      set.add(item.searchWord);
      out.push(item);
    }
  }
  return out;
}

function getStandardSearchWord(text: string): string {
  return parseSuggestionText(text).main;
}

function getCountryFromText(text?: string | null): string {
  return getStandardSearchWord(text ?? '');
}

function getFactoryFromText(text?: string | null): string | null {
  if (!text) return null;
  return getStandardSearchWord(text);
}

function getProductFromSuggestion(item: SearchSuggest, parts: string[]): string | null {
  if (item.productName) return item.productName;
  if (item.matchType === 'product') return getStandardSearchWord(item.text);
  if (item.type === '国家+产品' && parts.length >= 2) return getStandardSearchWord(parts.slice(1).join(' '));
  if (item.type === '国家+厂号+产品' && parts.length >= 3) return getStandardSearchWord(parts.slice(2).join(' '));
  if (item.type === '品牌+产品' && parts.length >= 2) return getStandardSearchWord(parts.slice(1).join(' '));
  return null;
}

function getBrandFromSuggestion(item: SearchSuggest, parts: string[]): string | null {
  if (item.brandName) return item.brandName;
  if (item.matchType !== 'brand') return null;
  if (item.type === '品牌+产品' && parts.length >= 1) return getStandardSearchWord(parts[0]);
  return getStandardSearchWord(item.text);
}

const styles = StyleSheet.create({
  container: {flex: 1, backgroundColor: colors.background},
  topBar: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 16,
    paddingBottom: 12,
    backgroundColor: '#FFFFFF',
    zIndex: 20,
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
  categoryButton: {
    height: 28,
    flexDirection: 'row',
    alignItems: 'center',
    gap: 2,
    paddingRight: 6,
  },
  categoryText: {
    color: colors.text,
    fontSize: 14,
    fontWeight: '600',
    lineHeight: 20,
  },
  searchDivider: {
    width: StyleSheet.hairlineWidth,
    height: 16,
    backgroundColor: '#D4DFDC',
    marginRight: 8,
  },
  categoryMenuBackdrop: {
    ...StyleSheet.absoluteFillObject,
    zIndex: 15,
  },
  categoryMenu: {
    position: 'absolute',
    left: 48,
    width: 72,
    borderRadius: 8,
    backgroundColor: '#FFFFFF',
    borderWidth: 1,
    borderColor: '#DEE4E1',
    shadowColor: '#000000',
    shadowOpacity: 0.08,
    shadowRadius: 8,
    shadowOffset: {width: 0, height: 4},
    elevation: 8,
    overflow: 'hidden',
    zIndex: 30,
  },
  categoryMenuItem: {
    height: 40,
    alignItems: 'center',
    justifyContent: 'center',
  },
  categoryMenuItemActive: {
    backgroundColor: '#EFF5F3',
  },
  categoryMenuText: {
    color: '#3C4947',
    fontSize: 14,
    fontWeight: '500',
  },
  categoryMenuTextActive: {
    color: colors.primary,
    fontWeight: '700',
  },
  input: {
    flex: 1,
    marginLeft: 8,
    color: colors.text,
    fontSize: 14,
    paddingVertical: 0,
  },
  inputFocused: {
    marginLeft: 0,
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
