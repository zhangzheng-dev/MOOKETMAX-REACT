import React, {useCallback, useEffect, useMemo, useState} from 'react';
import {ActivityIndicator, SectionList, RefreshControl, StyleSheet, Text, View} from 'react-native';
import type {NativeStackScreenProps} from '@react-navigation/native-stack';
import {mooketApi} from '../api/mooketApi';
import {DetailTopBar} from '../components/detail/DetailTopBar';
import {SelfSelectButton} from '../components/detail/SelfSelectButton';
import {FactoryDashboard} from '../components/detail/FactoryDashboard';
import {SummaryRowCard} from '../components/detail/SummaryRowCard';
import {OfferInquiryTabs, TabAndSortBar, type OfferTab, type SortMode} from '../components/detail/TabAndSortBar';
import {ErrorState} from '../components/common/ErrorState';
import type {RootStackParamList} from '../navigation/routes';
import {colors} from '../theme/colors';
import type {FactoryDetail, FactoryProduct} from '../types/api';
import {getTabCount, getTabProductCount} from '../utils/tabStats';

type Props = NativeStackScreenProps<RootStackParamList, 'Factory'>;

const pageSize = 20;

export function FactoryScreen({navigation, route}: Props) {
  const {country, factoryNo, category, searchKeyword: routeSearchKeyword, initialTab} = route.params;
  const searchKeyword = routeSearchKeyword ?? `${country}${factoryNo}`;
  const [data, setData] = useState<FactoryDetail | null>(null);
  const [tab, setTab] = useState<OfferTab>(initialTab ?? 'offer');
  const [sort, setSort] = useState<SortMode>({kind: 'comprehensive'});
  const [page, setPage] = useState(1);
  const [loading, setLoading] = useState(false);
  const [loadingMore, setLoadingMore] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const requestSortParam = useMemo(
    () => sortToParam(sort),
    [sort],
  );
  const products = data?.products ?? [];
  const currentCountry = data?.country || country;
  const currentFactoryNo = data?.factoryNo || factoryNo;
  const selfSelectCard = currentCountry && currentFactoryNo
    ? {cardType: 'factory', country: currentCountry, factoryNo: currentFactoryNo}
    : null;
  const selfSelectPayload = currentCountry && currentFactoryNo
    ? {
        searchWord: `${currentCountry}${currentFactoryNo}`,
        searchType: '\u56fd\u5bb6\u5382\u53f7',
        country: currentCountry,
        factoryNo: currentFactoryNo,
      }
    : null;

  const loadFirst = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      setPage(1);
      const next = await mooketApi.getFactoryDetail(
        country,
        factoryNo,
        category,
        tab,
        requestSortParam,
        1,
        pageSize,
      );
      setData(next);
    } catch (e) {
      setError(e instanceof Error ? e.message : '加载失败');
    } finally {
      setLoading(false);
    }
  }, [category, country, factoryNo, requestSortParam, tab]);

  useEffect(() => {
    loadFirst().catch(() => undefined);
  }, [loadFirst]);

  const loadMore = useCallback(async () => {
    if (loading || loadingMore || !data) return;
    if (page >= (data.totalPages ?? 1)) return;
    const next = page + 1;
    setLoadingMore(true);
    try {
      const more = await mooketApi.getFactoryDetail(
        country,
        factoryNo,
        category,
        tab,
        requestSortParam,
        next,
        pageSize,
      );
      setPage(next);
      setData(prev =>
        prev ? {...more, products: mergeProducts(prev.products, more.products)} : more,
      );
    } catch {
      // 静默
    } finally {
      setLoadingMore(false);
    }
  }, [category, country, data, factoryNo, loading, loadingMore, page, requestSortParam, tab]);

  const tagText = `${country}${factoryNo}`;

  return (
    <View style={styles.container}>
      <DetailTopBar
        onBack={() => navigation.goBack()}
        onSearchPress={() => {
          navigation.popToTop();
          navigation.navigate('Search', {category, keyword: searchKeyword, initialTab: tab});
        }}
        tags={[
          {
            text: tagText,
            onClose: () => {
              navigation.popToTop();
              navigation.navigate('Search', {category, keyword: searchKeyword, initialTab: tab});
            },
          },
        ]}
        topSlot={
          <OfferInquiryTabs
            tab={tab}
            onTabChange={setTab}
            showMerchant
            onMerchantPress={() => {
              navigation.replace('MerchantSearchResults', {
                category,
                searchKeyword,
                tags: [tagText],
                merchantSearch: {
                  display: searchKeyword,
                  matchType: 'factory',
                  type: '国家+厂号',
                  country,
                  factoryNo,
                },
                target: {screen: 'Factory', country, factoryNo},
              });
            }}
          />
        }
        rightAction={
          <SelfSelectButton category={category} card={selfSelectCard} payload={selfSelectPayload} />
        }
      />

      {loading && !data ? (
        <View style={styles.loading}>
          <ActivityIndicator color={colors.primary} />
        </View>
      ) : error && !data ? (
        <ErrorState message={error} onRetry={loadFirst} />
      ) : data ? (
        <SectionList
          sections={[{key: 'items', data: products}]}
          keyExtractor={(item, index) => `${item.productId}-${index}`}
          stickySectionHeadersEnabled
            initialNumToRender={8}
            maxToRenderPerBatch={5}
            windowSize={3}
            removeClippedSubviews
          ListHeaderComponent={
            <View>
              <FactoryDashboard
                country={data.country || country}
                factoryNo={data.factoryNo || factoryNo}
                isInquiry={tab === 'inquiry'}
                productCount={getTabProductCount(data, tab)}
                inquiryCount={getTabCount(data, 'inquiry')}
                recentOfferCount={getTabCount(data, 'offer')}
                secondaryCountLabel={tab === 'offer' ? '报盘数' : '求购数'}
                secondaryCount={getTabCount(data, tab)}
              />
              <View style={styles.gap} />
            </View>
          }
          renderSectionHeader={() => (
            <View style={styles.stickyHeader}>
              <TabAndSortBar tab={tab} onTabChange={setTab} sort={sort} onSortChange={setSort} showTabs={false} />
            </View>
          )}
          renderItem={({item}) => (
            <SummaryRowCard
              title={item.productName}
              merchantNames={item.merchantNames}
              merchantCount={item.merchantCount}
              count={getTabCount(item, tab)}
              countLabel={tab === 'offer' ? '报盘' : '求购'}
              priceMin={item.priceMin}
              priceMax={item.priceMax}
              onPress={() =>
                navigation.navigate('CountryFactoryProduct', {
                  country: data.country || country,
                  factoryNo: data.factoryNo || factoryNo,
                  productName: item.productName,
                  category,
                  initialTab: tab,
                })
              }
            />
          )}
          refreshControl={<RefreshControl refreshing={loading} onRefresh={loadFirst} />}
          onEndReached={loadMore}
          onEndReachedThreshold={0.4}
          ListFooterComponent={
            <View style={styles.footer}>
              {loadingMore ? (
                <Text style={styles.footerText}>加载中...</Text>
              ) : page >= (data.totalPages ?? 1) ? (
                <Text style={styles.footerText}>没有更多了～</Text>
              ) : null}
            </View>
          }
          ListEmptyComponent={!loading ? <Text style={styles.empty}>暂无数据</Text> : null}
        />
      ) : null}
    </View>
  );
}

function sortToParam(sort: SortMode): string {
  if (sort.kind === 'comprehensive') return 'comprehensive';
  if (sort.kind === 'publishTime') return 'publish_time';
  return sort.order === 'asc' ? 'price_asc' : sort.order === 'desc' ? 'price_desc' : 'comprehensive';
}

function mergeProducts(prev: FactoryProduct[], incoming: FactoryProduct[]) {
  const seen = new Set(prev.map(item => `${item.productId}`));
  const next = prev.slice();
  for (const item of incoming) {
    if (!seen.has(`${item.productId}`)) {
      seen.add(`${item.productId}`);
      next.push(item);
    }
  }
  return next;
}

const styles = StyleSheet.create({
  container: {flex: 1, backgroundColor: colors.background},
  loading: {paddingVertical: 48, alignItems: 'center'},
  topTabs: {borderTopWidth: StyleSheet.hairlineWidth, borderTopColor: '#EFF5F3', borderBottomWidth: 1, borderBottomColor: colors.border},
  gap: {height: 12, backgroundColor: '#F4FBF8'},
  footer: {alignItems: 'center', paddingVertical: 16},
  footerText: {color: '#9DA4A3', fontSize: 12},
  empty: {textAlign: 'center', paddingVertical: 48, color: '#9DA4A3', fontSize: 14},
  stickyHeader: {backgroundColor: '#FFFFFF', borderBottomWidth: 1, borderBottomColor: '#DEE4E1', zIndex: 10, elevation: 3},
});
