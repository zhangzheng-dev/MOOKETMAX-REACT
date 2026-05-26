export type ApiResponse<T> = {
  code: number;
  message: string;
  data: T | null;
};

export type SearchSuggest = {
  text: string;
  keyword: string;
  type: string;
  priority: number;
  targetId: number;
  matchType: string;
};

export type HotSearchItem = {
  keyword: string;
  dimension: string;
  todayOfferCount: number;
  country?: string | null;
  factoryNo?: string | null;
  productId?: number | null;
  brandId?: number | null;
  merchantId?: number | string | null;
};

export type HomeStatData = {
  totalOfferCount: string;
  totalInquiryCount: string;
  merchantCount: string;
  statTime: string;
};

export type SearchHistory = {
  historyId: number;
  searchWord: string;
  searchType: string;
  isSelfSelect: number;
  createTime: string;
  productId?: number | null;
  brandId?: number | null;
  merchantId?: number | string | null;
  country?: string | null;
  factoryNo?: string | null;
  productName?: string | null;
};

export type HomeCardItem = {
  cardType?: string | null;
  isExample?: boolean;
  rank?: number | null;
  todayOfferCount?: number | null;
  historyId?: number | null;
  productId?: number | null;
  productName?: string | null;
  merchantCount?: number | null;
  factoryCount?: number | null;
  priceMin?: number | null;
  priceMax?: number | null;
  country?: string | null;
  countryAlias?: string | null;
  hotFactories?: Record<string, unknown>[] | null;
  hotProducts?: Record<string, unknown>[] | null;
  brandId?: number | null;
  brandName?: string | null;
  productCount?: number | null;
  merchantId?: number | string | null;
  merchantName?: string | null;
  merchantShortName?: string | null;
  merchantTags?: string | null;
  latestOffers?: Record<string, unknown>[] | null;
  factoryNo?: string | null;
  priceChange?: number | null;
  priceChangeRate?: number | null;
  trendPoints?: Record<string, unknown>[] | null;
  topFactories?: Record<string, unknown>[] | null;
  hotMerchants?: Record<string, unknown>[] | null;
  inquiryCount?: number | null;
};

export type HomeCardsResponse = {
  cards: HomeCardItem[];
  updateTime: string | null;
};

export type SendCodeResult = {
  message?: string | null;
  isRegistered?: boolean;
  clientId?: string | null;
};

export type AuthResult = {
  token: string;
  isNewUser: boolean;
  userId?: number | null;
  phone?: string | null;
  nickname?: string | null;
  gatewayAccessToken?: string | null;
  gatewayUserId?: string | null;
  mooketId?: string | null;
};

export type RegisterRequest = {
  nickname: string;
  identityTags: string[];
  gatewayAccessToken: string;
  code: string;
  clientId?: string | null;
  deviceId?: string | null;
};

export type UserProfile = {
  userId: number;
  nickname?: string | null;
  avatarUrl?: string | null;
  phone?: string | null;
  mooketNo?: string | null;
  mooketId?: string | null;
  realName?: string | null;
  realNameStatus?: string | null;
  identityTags?: string[] | null;
};

export type UpdateProfileRequest = {
  nickname?: string | null;
  identityTags?: string[] | null;
};

export type AppVersionInfo = {
  version: string;
  versionCode: number;
  hasUpdate: boolean;
  updateUrl?: string | null;
  updateContent?: string | null;
};

export type EmployeeOffer = {
  offerId?: number | null;
  userNickname?: string | null;
  price?: number | null;
  priceMax?: number | null;
  weight?: string | null;
  goodsLocation?: string | null;
  tags?: string | null;
  goodsType?: string | null;
  feedingMethod?: string | null;
  offerOriginalText?: string | null;
  publishTime?: string | null;
};

export type OfferSummary = {
  offerId?: number | null;
  productName?: string | null;
  country?: string | null;
  factoryNo?: string | null;
  price?: number | null;
  priceMax?: number | null;
  goodsLocation?: string | null;
  tags?: string | null;
  goodsType?: string | null;
  feedingType?: string | null;
  publishTime?: string | null;
  employeeOffers?: EmployeeOffer[] | null;
};

export type MerchantDetail = {
  merchantId: number | string;
  merchantName: string;
  merchantShortName?: string | null;
  merchantTags?: string | null;
  contactPhone?: string | null;
  todayOfferCount: number;
  todayInquiryCount: number;
  todayProductCount: number;
  todayFactoryCount: number;
  offers: OfferSummary[];
  inquiries: OfferSummary[];
  totalOffers?: number | null;
  totalInquiries?: number | null;
};

export type MerchantProductPage = {
  products: OfferSummary[];
  totalCount: number;
  page: number;
  pageSize: number;
  totalPages: number;
  offerType: string;
};

export type ProductSummary = {
  country?: string | null;
  factoryNo?: string | null;
  countryFactory?: string | null;
  priceMin?: number | null;
  priceMax?: number | null;
  merchantNames?: string[] | null;
  merchantCount: number;
  offerCount: number;
};

export type ProductDetail = {
  productId: number;
  productName: string;
  category: string;
  offerCount: number;
  priceMin?: number | null;
  priceMax?: number | null;
  merchantCount: number;
  factoryCount: number;
  summaries: ProductSummary[];
  totalCount: number;
  page?: number | null;
  pageSize?: number | null;
  totalPages?: number | null;
};

export type HotFactory = {
  factoryNo: string;
  offerCount: number;
  rank?: number | null;
};

export type HotProduct = {
  productName: string;
  offerCount: number;
  rank?: number | null;
};

export type CountryProductSummary = {
  productId: number;
  productName: string;
  priceMin?: number | null;
  priceMax?: number | null;
  factoryNos: string[];
  factoryCount: number;
  offerCount: number;
};

export type CountryDetail = {
  country: string;
  offerCount: number;
  merchantCount: number;
  factoryCount: number;
  priceMin?: number | null;
  priceMax?: number | null;
  hotFactories: HotFactory[];
  hotProducts: HotProduct[];
  summaries: CountryProductSummary[];
  totalCount: number;
  page?: number | null;
  pageSize?: number | null;
  totalPages?: number | null;
};

export type FactoryProduct = {
  productId: number;
  productName: string;
  priceMin?: number | null;
  priceMax?: number | null;
  merchantNames: string[];
  merchantCount: number;
  offerCount: number;
};

export type FactoryDetail = {
  factoryId?: number | null;
  country: string;
  countryAlias?: string | null;
  factoryNo: string;
  productCount: number;
  inquiryCount: number;
  recentOfferCount: number;
  products: FactoryProduct[];
  totalCount: number;
  page?: number | null;
  pageSize?: number | null;
  totalPages?: number | null;
};

export type DailyPrice = {
  date: string;
  fullDate: string;
  avgPrice?: number | null;
  priceUnit?: string | null;
  offerCount?: number | null;
};

export type CountryProductFactory = {
  country?: string | null;
  factoryNo?: string | null;
  countryFactory?: string | null;
  priceMin?: number | null;
  priceMax?: number | null;
  merchantNames?: string[] | null;
  merchantCount: number;
  offerCount: number;
};

export type CountryProductDetail = {
  country: string;
  productId?: number | null;
  productName: string;
  priceMin?: number | null;
  priceMax?: number | null;
  priceChange?: number | null;
  priceChangeRate?: number | null;
  offerCount: number;
  inquiryCount: number;
  merchantCount: number;
  priceHistory7Days: DailyPrice[];
  priceHistory30Days: DailyPrice[];
  factories: CountryProductFactory[];
  totalCount: number;
  page?: number | null;
  pageSize?: number | null;
  totalPages?: number | null;
};

export type EmployeeOfferItem = {
  offerId?: number | null;
  userNickname?: string | null;
  contactPhone?: string | null;
  price: string;
  weight?: string | null;
  goodsLocation?: string | null;
  goodsType?: string | null;
  feedingType?: string | null;
  tags?: string | null;
  offerType?: string | null;
  publishTime?: string | null;
  offerOriginalText?: string | null;
};

export type MerchantOfferGroup = {
  merchantId?: number | string | null;
  merchantName?: string | null;
  merchantPhone?: string | null;
  offerCount: number;
  isFamousMerchant?: boolean;
  employeeOffers: EmployeeOfferItem[];
};

export type CountryFactoryProductDetail = {
  country: string;
  factoryNo: string;
  productId?: number | null;
  productName: string;
  priceMin?: number | null;
  priceMax?: number | null;
  priceChange?: number | null;
  priceChangeRate?: number | null;
  offerCount: number;
  inquiryCount: number;
  merchantCount: number;
  priceHistory7Days: DailyPrice[];
  priceHistory30Days: DailyPrice[];
  merchantOffers: MerchantOfferGroup[];
  totalCount: number;
  page?: number | null;
  pageSize?: number | null;
  totalPages?: number | null;
  hasSubstitute?: boolean;
};

export type SubstituteFactory = {
  factoryNo: string;
  priceMin?: number | null;
  priceMax?: number | null;
  offerCount: number;
  merchantCount: number;
  isSelected: boolean;
};

export type SubstituteProduct = {
  category: string;
  productName: string;
  currentFactoryNo: string;
  tier?: string | null;
  priceMin?: number | null;
  priceMax?: number | null;
  offerCount: number;
  merchantCount: number;
  factories: SubstituteFactory[];
};

export type SubstituteProductDetail = {
  country: string;
  factoryNo: string;
  productName: string;
  tier?: string | null;
  productId?: number | null;
  priceMin?: number | null;
  priceMax?: number | null;
  priceChange?: number | null;
  priceChangeRate?: number | null;
  offerCount: number;
  inquiryCount: number;
  merchantCount: number;
  priceHistory7Days: DailyPrice[];
  priceHistory30Days: DailyPrice[];
  merchantOffers: MerchantOfferGroup[];
  totalCount: number;
  page: number;
  pageSize: number;
  totalPages: number;
};

export type FactoryTrendPoint = {
  date: string;
  fullDate: string;
  avgPrice?: number | null;
  offerCount?: number | null;
};

export type FactoryTrendData = {
  factoryNo: string;
  trend: FactoryTrendPoint[];
  avgPrice?: number | null;
};

export type FactoryPriceComparison = {
  country: string;
  productName: string;
  category: string;
  offerType: string;
  factories: FactoryTrendData[];
};

export type BrandProductSummary = {
  productId: number;
  productName: string;
  priceMin?: number | null;
  priceMax?: number | null;
  factoryNos: string;
  factoryCount: number;
  offerCount: number;
  country?: string | null;
  factoryNo?: string | null;
  countryFactory?: string | null;
  merchantNames?: string[] | null;
  merchantCount?: number | null;
};

export type BrandDetail = {
  brandName: string;
  todayOfferCount: number;
  yesterdayOfferCount: number;
  totalOfferCount: number;
  todayInquiryCount: number;
  yesterdayInquiryCount: number;
  totalInquiryCount: number;
  factoryCount: number;
  productCount: number;
  merchantCount?: number | null;
  priceMin?: number | null;
  priceMax?: number | null;
  summaries: BrandProductSummary[];
  totalCount: number;
  page?: number | null;
  pageSize?: number | null;
  totalPages?: number | null;
};

export type BrandProductDetailResult = {
  brandName: string;
  factoryCount: number;
  productCount: number;
  merchantCount?: number | null;
  priceMin?: number | null;
  priceMax?: number | null;
  todayOfferCount: number;
  yesterdayOfferCount: number;
  todayInquiryCount: number;
  yesterdayInquiryCount: number;
  summaries: BrandProductSummary[];
  totalCount: number;
  page?: number | null;
  pageSize?: number | null;
  totalPages?: number | null;
};
