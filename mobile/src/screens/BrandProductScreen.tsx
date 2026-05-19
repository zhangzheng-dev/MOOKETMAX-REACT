import React, {useCallback, useEffect, useState} from 'react';
import {ActivityIndicator, SectionList, RefreshControl, StyleSheet, Text, View} from 'react-native';
import type {NativeStackScreenProps} from '@react-navigation/native-stack';
import {mooketApi} from '../api/mooketApi';
import {BrandProductDashboard} from '../components/detail/BrandProductDashboard';
import {DetailTopBar} from '../components/detail/DetailTopBar';
import {SummaryRowCard} from '../components/detail/SummaryRowCard';
import {TabAndSortBar, type OfferTab, type SortMode} from '../components/detail/TabAndSortBar';
import {ErrorState} from '../components/common/ErrorState';
import type {RootStackParamList} from '../navigation/routes';
import {colors} from '../theme/colors';
import type {BrandProductDetailResult, BrandProductSummary} from '../types/api';

type Props = NativeStackScreenProps<RootStackParamList, 'BrandProduct'>;

const pageSize = 20;

export function BrandProductScreen({navigation, route}: Props) {
  const {brandName, productName, category} = route.params;
  const [data, setData] = useState<BrandProductDetailResult | null>(null);
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
      const next = await mooketApi.getBrandProductDetail(
        brandName,
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
  }, [brandName, category, productName, sort, tab]);

  useEffect(() => {
    loadFirst().catch(() => undefined);
  }, [loadFirst]);

  const loadMore = useCallback(async () => {
    if (loading || loadingMore || !data) return;
    if (page >= (data.totalPages ?? 1)) return;
    const next = page + 1;
    setLoadingMore(true);
    try {
      const more = await mooketApi.getBrandProductDetail(
        brandName,
        productName,
        category,
        tab,
        sortToParam(sort),
        next,
        pageSize,
      );
      setPage(next);
      setData(prev =>
        prev ? {...more, summaries: mergeSummaries(prev.summaries, more.summaries)} : more,
      );
    } catch {
      // 静默
    } finally {
      setLoadingMore(false);
    }
  }, [brandName, category, data, loading, loadingMore, page, productName, sort, tab]);

  return (
    <View style={styles.container}>
      <DetailTopBar
        onBack={() => navigation.goBack()}
        tags={[
          {
            text: brandName,
            onClose: () => navigation.navigate('Brand', {brandName, category}),
          },
          {
            text: productName,
            onClose: () => navigation.navigate('Brand', {brandName, category}),
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
          sections={[{key: 'items', data: data.summaries}]}
          keyExtractor={(item, index) => `${item.country ?? ''}-${item.factoryNo ?? ''}-${index}`}
          stickySectionHeadersEnabled
            initialNumToRender={10}
            maxToRenderPerBatch={10}
            windowSize={5}
          ListHeaderComponent={
            <View>
              <BrandProductDashboard
                brandName={stripProductName(data.brandName || brandName, productName)}
                productName={productName}
                isInquiry={tab === 'inquiry'}
                todayOfferCount={data.todayOfferCount}
                todayInquiryCount={data.todayInquiryCount}
                priceMin={data.priceMin}
                priceMax={data.priceMax}
                merchantCount={data.merchantCount}
                factoryCount={data.factoryCount}
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
                item.country && item.factoryNo
                  ? () =>
                      navigation.navigate('CountryFactoryProduct', {
                        country: item.country!,
                        factoryNo: item.factoryNo!,
                        productName,
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

function stripProductName(brandFull: string, productName: string): string {
  if (!brandFull) return brandFull;
  if (!productName) return brandFull;
  // 去除尾部的产品名（可能用空格 / · 分隔）
  const trimmed = brandFull.replace(/[ ·\s]*$/, '');
  if (trimmed.endsWith(productName)) {
    return trimmed.slice(0, trimmed.length - productName.length).replace(/[ ·\s]+$/, '').trim();
  }
  if (brandFull.includes(productName)) {
    return brandFull.replace(productName, '').replace(/\s+/g, ' ').trim();
  }
  return brandFull;
}

function mergeSummaries(prev: BrandProductSummary[], incoming: BrandProductSummary[]) {
  const seen = new Set(
    prev.map(item => `${item.country ?? ''}-${item.factoryNo ?? ''}-${item.productId}`),
  );
  const next = prev.slice();
  for (const item of incoming) {
    const key = `${item.country ?? ''}-${item.factoryNo ?? ''}-${item.productId}`;
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
  footer: {alignItems: 'center', paddingVertical: 16},
  footerText: {color: '#9DA4A3', fontSize: 12},
  empty: {textAlign: 'center', paddingVertical: 48, color: '#9DA4A3', fontSize: 14},
  stickyHeader: {backgroundColor: '#FFFFFF', borderBottomWidth: 1, borderBottomColor: '#DEE4E1', zIndex: 10, elevation: 3},
});
