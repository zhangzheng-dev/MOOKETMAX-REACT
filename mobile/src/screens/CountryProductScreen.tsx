import React, {useCallback, useEffect, useState} from 'react';
import {ActivityIndicator, SectionList, RefreshControl, StyleSheet, Text, View} from 'react-native';
import type {NativeStackScreenProps} from '@react-navigation/native-stack';
import {mooketApi} from '../api/mooketApi';
import {CountryProductDashboard} from '../components/detail/CountryProductDashboard';
import {DetailTopBar} from '../components/detail/DetailTopBar';
import {SummaryRowCard} from '../components/detail/SummaryRowCard';
import {TabAndSortBar, type OfferTab, type SortMode} from '../components/detail/TabAndSortBar';
import {ErrorState} from '../components/common/ErrorState';
import type {RootStackParamList} from '../navigation/routes';
import {colors} from '../theme/colors';
import type {CountryProductDetail, CountryProductFactory} from '../types/api';

type Props = NativeStackScreenProps<RootStackParamList, 'CountryProduct'>;

const pageSize = 20;

export function CountryProductScreen({navigation, route}: Props) {
  const {country, productName, category} = route.params;
  const [data, setData] = useState<CountryProductDetail | null>(null);
  const [tab, setTab] = useState<OfferTab>('offer');
  const [sort, setSort] = useState<SortMode>({kind: 'comprehensive'});
  const [page, setPage] = useState(1);
  const [loading, setLoading] = useState(false);
  const [loadingMore, setLoadingMore] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const loadFirst = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      setPage(1);
      const next = await mooketApi.getCountryProductDetail(
        country,
        productName,
        category,
        tab,
        sortToParam(sort),
        1,
        pageSize,
      );
      setData(next);
    } catch (e) {
      setError(e instanceof Error ? e.message : '加载失败');
    } finally {
      setLoading(false);
    }
  }, [category, country, productName, sort, tab]);

  useEffect(() => {
    loadFirst().catch(() => undefined);
  }, [loadFirst]);

  const loadMore = useCallback(async () => {
    if (loading || loadingMore || !data) return;
    if (page >= (data.totalPages ?? 1)) return;
    const nextPage = page + 1;
    setLoadingMore(true);
    try {
      const more = await mooketApi.getCountryProductDetail(
        country,
        productName,
        category,
        tab,
        sortToParam(sort),
        nextPage,
        pageSize,
      );
      setPage(nextPage);
      setData(prev =>
        prev ? {...more, factories: mergeFactories(prev.factories, more.factories)} : more,
      );
    } catch {
      // 静默
    } finally {
      setLoadingMore(false);
    }
  }, [category, country, data, loading, loadingMore, page, productName, sort, tab]);

  return (
    <View style={styles.container}>
      <DetailTopBar
        onBack={() => navigation.goBack()}
        tags={[
          {
            text: country,
            onClose: () => {
              if (data?.productId) {
                navigation.navigate('Product', {
                  productId: data.productId,
                  category,
                  productName,
                });
              }
            },
          },
          {
            text: productName,
            onClose: () => navigation.navigate('Country', {country, category}),
          },
        ]}
      />

      {loading && !data ? (
        <View style={styles.loading}>
          <ActivityIndicator color={colors.primary} />
        </View>
      ) : error && !data ? (
        <ErrorState message={error} onRetry={loadFirst} />
      ) : data ? (
        <SectionList
          sections={[{key: 'items', data: data.factories}]}
          keyExtractor={(item, index) => `${item.country ?? ''}-${item.factoryNo ?? ''}-${index}`}
          stickySectionHeadersEnabled
            initialNumToRender={10}
            maxToRenderPerBatch={10}
            windowSize={5}
          ListHeaderComponent={
            <View>
              <CountryProductDashboard
                country={data.country || country}
                productName={data.productName || productName}
                isInquiry={tab === 'inquiry'}
                priceMin={data.priceMin}
                priceMax={data.priceMax}
                priceChange={data.priceChange}
                priceChangeRate={data.priceChangeRate}
                offerCount={data.offerCount}
                inquiryCount={data.inquiryCount}
                merchantCount={data.merchantCount}
                history7Days={data.priceHistory7Days}
                history30Days={data.priceHistory30Days}
              />
              <View style={styles.gap} />
            </View>
          }
          renderSectionHeader={() => (
            <View style={styles.stickyHeader}>
              <TabAndSortBar tab={tab} onTabChange={setTab} sort={sort} onSortChange={setSort} />
            </View>
          )}
          renderItem={({item}) => (
            <SummaryRowCard
              title={
                item.countryFactory ||
                [item.country, item.factoryNo].filter(Boolean).join(' ') ||
                '--'
              }
              merchantNames={item.merchantNames}
              merchantCount={item.merchantCount}
              count={item.offerCount}
              countLabel={tab === 'offer' ? '报盘' : '求购'}
              priceMin={item.priceMin}
              priceMax={item.priceMax}
              onPress={
                item.factoryNo
                  ? () =>
                      navigation.navigate('CountryFactoryProduct', {
                        country: item.country || data.country || country,
                        factoryNo: item.factoryNo!,
                        productName: data.productName || productName,
                        category,
                      })
                  : undefined
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

function mergeFactories(prev: CountryProductFactory[], incoming: CountryProductFactory[]) {
  const seen = new Set(prev.map(item => `${item.country ?? ''}-${item.factoryNo ?? ''}`));
  const next = prev.slice();
  for (const item of incoming) {
    const key = `${item.country ?? ''}-${item.factoryNo ?? ''}`;
    if (!seen.has(key)) {
      seen.add(key);
      next.push(item);
    }
  }
  return next;
}

const styles = StyleSheet.create({
  container: {flex: 1, backgroundColor: colors.background},
  loading: {paddingVertical: 48, alignItems: 'center'},
  gap: {height: 12, backgroundColor: '#F4FBF8'},
  trendWrap: {paddingHorizontal: 16, paddingTop: 12, backgroundColor: '#FFFFFF'},
  footer: {alignItems: 'center', paddingVertical: 16},
  footerText: {color: '#9DA4A3', fontSize: 12},
  empty: {textAlign: 'center', paddingVertical: 48, color: '#9DA4A3', fontSize: 14},
  stickyHeader: {backgroundColor: '#FFFFFF', borderBottomWidth: 1, borderBottomColor: '#DEE4E1', zIndex: 10, elevation: 3},
});
