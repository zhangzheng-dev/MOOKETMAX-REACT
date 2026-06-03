import {CURRENT_APP_VERSION, CURRENT_APP_VERSION_CODE} from '../config/env';
import {apiClient, unwrap} from './client';
import type {
  AppVersionInfo,
  AuthResult,
  BrandDetail,
  BrandProductDetailResult,
  CountryDetail,
  CountryFactoryProductDetail,
  CountryProductDetail,
  FactoryDetail,
  FactoryPriceComparison,
  HomeCardsResponse,
  HomeStatData,
  HotSearchItem,
  MerchantDetail,
  ProductDetail,
  RegisterRequest,
  SearchHistory,
  SearchSuggest,
  SendCodeResult,
  SubstituteProduct,
  SubstituteProductDetail,
  UpdateProfileRequest,
  UserProfile,
} from '../types/api';

export const mooketApi = {
  getHotSearchRecommendations(category: string) {
    return unwrap<HotSearchItem[]>(apiClient.get('api/v1/home/hot-search', {params: {category}}));
  },

  getHomeStatData(category: string) {
    return unwrap<HomeStatData>(apiClient.get('api/v1/home/stat', {params: {category}}));
  },

  getHomeCards(category: string, tab = 0) {
    return unwrap<HomeCardsResponse>(apiClient.get('api/v1/home/cards', {params: {category, tab}}));
  },

  getRecentSearchCards(category: string) {
    return unwrap<HomeCardsResponse>(apiClient.get('api/v1/search-history/cards/recent', {params: {category}}));
  },

  getSelfSelectCards(category: string) {
    return unwrap<HomeCardsResponse>(apiClient.get('api/v1/search-history/cards/self-select', {params: {category}}));
  },

  getSearchSuggestions(category: string, keyword: string) {
    return unwrap<SearchSuggest[]>(apiClient.get('api/v1/search/suggest', {params: {category, keyword}}));
  },

  saveSearchHistory(params: {
    searchWord: string;
    searchType: string;
    isSelfSelect?: number;
    productId?: number | null;
    productName?: string | null;
    country?: string | null;
    factoryNo?: string | null;
    brandId?: number | null;
    merchantId?: number | null;
  }) {
    // Remove null/undefined values to avoid sending "null" as string
    const cleanParams: Record<string, string | number> = {};
    Object.entries(params).forEach(([key, value]) => {
      if (value != null) cleanParams[key] = value;
    });
    return unwrap<void>(apiClient.post('api/v1/search/history', null, {params: cleanParams}));
  },

  getRecentSearches(limit = 20) {
    return unwrap<SearchHistory[]>(apiClient.get('api/v1/search-history/recent', {params: {limit}}));
  },

  deleteSearchHistory(historyId: number) {
    return unwrap<void>(apiClient.delete(`api/v1/search-history/${historyId}`));
  },

  batchDeleteSearchHistory(historyIds: number[]) {
    return unwrap<void>(apiClient.delete('api/v1/search-history/batch', {data: historyIds}));
  },

  moveToSelfSelect(historyId: number) {
    return unwrap<void>(apiClient.post(`api/v1/search-history/self-select/move/${historyId}`));
  },

  cancelSelfSelect(historyId: number) {
    return unwrap<void>(apiClient.post(`api/v1/search-history/self-select/cancel/${historyId}`));
  },

  sendCode(phone: string, deviceId?: string) {
    return unwrap<SendCodeResult>(
      apiClient.post('api/v1/auth/send-code', {phone}, {
        headers: deviceId ? {'X-Device-Id': deviceId} : undefined,
      }),
    );
  },

  login(phone: string, code: string, deviceId?: string) {
    const payload: Record<string, string | undefined> = {phone, code};
    if (deviceId) payload.deviceId = deviceId;
    return unwrap<AuthResult>(apiClient.post('api/v1/auth/login', payload, {timeout: 30000}));
  },

  register(token: string, payload: RegisterRequest) {
    return unwrap<AuthResult>(
      apiClient.post('api/v1/auth/register', payload, {
        headers: {Authorization: `Bearer ${token}`},
      }),
    );
  },

  getUserProfile() {
    return unwrap<UserProfile>(apiClient.get('api/v1/user/profile'));
  },

  updateProfile(payload: UpdateProfileRequest) {
    return unwrap<{message: string}>(apiClient.post('api/v1/user/profile/update', payload));
  },

  uploadAvatar(file: {uri: string; type?: string; name?: string}) {
    const formData = new FormData();
    formData.append('file', {
      uri: file.uri,
      type: file.type ?? 'image/jpeg',
      name: file.name ?? 'avatar.jpg',
    } as never);
    return unwrap<{avatarUrl: string; message: string}>(
      apiClient.post('api/v1/user/avatar/upload', formData, {
        headers: {'Content-Type': 'multipart/form-data'},
      }),
    );
  },

  logout() {
    return unwrap<void>(apiClient.post('api/v1/user/logout'));
  },

  cancelAccount() {
    return unwrap<{message: string}>(apiClient.post('api/v1/user/cancel-account'));
  },

  getAppVersion() {
    return unwrap<AppVersionInfo>(
      apiClient.get('api/v1/app/version', {
        params: {
          version: CURRENT_APP_VERSION,
          versionCode: CURRENT_APP_VERSION_CODE,
        },
      }),
    );
  },

  getMerchantDetail(merchantId: number | string, category: string) {
    return unwrap<MerchantDetail>(apiClient.get(`api/v1/merchant/${merchantId}`, {params: {category}}));
  },

  getMerchantProducts(
    merchantId: number | string,
    type: string,
    category: string,
    page: number,
    pageSize: number,
    sortBy = 'comprehensive',
  ) {
    return unwrap<import('../types/api').MerchantProductPage>(
      apiClient.get(`api/v1/merchant/${merchantId}/products`, {
        params: {type, category, page, pageSize, sortBy},
      }),
    );
  },

  getProductDetail(
    productId: number,
    category: string,
    type = 'offer',
    sortBy = 'comprehensive',
    page = 1,
    pageSize = 20,
  ) {
    return unwrap<ProductDetail>(
      apiClient.get(`api/v1/product/${productId}`, {
        params: {category, type, sortBy, page, pageSize},
      }),
    );
  },

  getCountryDetail(
    country: string,
    category: string,
    type = 'offer',
    sortBy = 'comprehensive',
    page = 1,
    pageSize = 20,
  ) {
    return unwrap<CountryDetail>(
      apiClient.get(`api/v1/country/${country}`, {
        params: {category, type, sortBy, page, pageSize},
      }),
    );
  },

  getFactoryDetail(
    country: string,
    factoryNo: string,
    category: string,
    type = 'offer',
    sortBy = 'comprehensive',
    page = 1,
    pageSize = 20,
  ) {
    return unwrap<FactoryDetail>(
      apiClient.get('api/v1/factory/detail', {
        params: {country, factoryNo, category, type, sortBy, page, pageSize},
      }),
    );
  },

  getCountryProductDetail(
    country: string,
    productName: string,
    category: string,
    type = 'offer',
    sortBy = 'comprehensive',
    page = 1,
    pageSize = 20,
  ) {
    return unwrap<CountryProductDetail>(
      apiClient.get('api/v1/country-product', {
        params: {country, productName, category, type, sortBy, page, pageSize},
      }),
    );
  },

  getCountryFactoryProductDetail(
    country: string,
    factoryNo: string,
    productName: string,
    category: string,
    type = 'offer',
    sortBy = 'comprehensive',
    page = 1,
    pageSize = 20,
  ) {
    return unwrap<CountryFactoryProductDetail>(
      apiClient.get('api/v1/country-factory-product', {
        params: {country, factoryNo, productName, category, type, sortBy, page, pageSize},
      }),
    );
  },

  getSubstituteProducts(country: string, factoryNo: string, productName: string, category: string) {
    return unwrap<SubstituteProduct>(
      apiClient.get('api/v1/substitute/products', {
        params: {country, factoryNo, productName, category},
      }),
    );
  },

  getSubstituteProductDetail(
    country: string,
    factoryNo: string,
    productName: string,
    category: string,
    type = 'offer',
    sortBy = 'comprehensive',
    page = 1,
    pageSize = 10,
  ) {
    return unwrap<SubstituteProductDetail>(
      apiClient.get('api/v1/substitute/product/detail', {
        params: {country, factoryNo, productName, category, type, sortBy, page, pageSize},
      }),
    );
  },

  getFactoryPriceComparison(
    country: string,
    factoryNos: string[],
    productName: string,
    category: string,
    offerType = '报盘',
    days = 30,
  ) {
    return unwrap<FactoryPriceComparison>(
      apiClient.get('api/v1/price-trend/compare', {
        params: {country, factoryNos: factoryNos.join(','), productName, category, offerType, days},
      }),
    );
  },

  getBrandDetail(
    brandName: string,
    category: string,
    type = 'offer',
    sortBy = 'comprehensive',
    page = 1,
    pageSize = 20,
  ) {
    return unwrap<BrandDetail>(
      apiClient.get(`api/v1/brand/${brandName}`, {
        params: {category, type, sortBy, page, pageSize},
      }),
    );
  },

  getBrandProductDetail(
    brandName: string,
    productName: string,
    category: string,
    type = 'offer',
    sortBy = 'comprehensive',
    page = 1,
    pageSize = 20,
  ) {
    return unwrap<BrandProductDetailResult>(
      apiClient.get(`api/v1/brand/${brandName}/product/${productName}`, {
        params: {category, type, sortBy, page, pageSize},
      }),
    );
  },
};
