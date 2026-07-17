export type RootStackParamList = {
  Login: undefined;
  Home: undefined;
  Search: {category: string; keyword?: string; initialTab?: 'offer' | 'inquiry' | 'merchant'};
  OfferFeed: {
    category: string;
    initialTab?: 'offer' | 'inquiry';
    inquiryOnly?: boolean;
    keyword?: string;
    queryKeyword?: string;
    merchantId?: number | string;
    brandName?: string;
    productName?: string;
    keywordScope?: 'all' | 'product';
    initialFilters?: {
      country?: string | null;
      factoryNo?: string | null;
    };
  };
  PlateFollow: {initialTab?: 'intent' | 'recent'; category?: string} | undefined;
  HomeCards: {category?: string} | undefined;
  Merchant: {merchantId: number | string; category: string; initialTab?: 'offer' | 'inquiry'; initialCategory?: 'all' | '牛' | '猪'};
  Product: {productId: number; category: string; productName: string; searchKeyword?: string; initialTab?: 'offer' | 'inquiry'};
  Country: {country: string; category: string; searchKeyword?: string; initialTab?: 'offer' | 'inquiry'};
  Factory: {country: string; factoryNo: string; category: string; searchKeyword?: string; initialTab?: 'offer' | 'inquiry'};
  CountryProduct: {country: string; productName: string; category: string; searchKeyword?: string; initialTab?: 'offer' | 'inquiry'};
  CountryFactoryProduct: {
    country: string;
    factoryNo: string;
    productName: string;
    category: string;
    searchKeyword?: string;
    initialTab?: 'offer' | 'inquiry';
  };
  SubstituteProduct: {
    country: string;
    factoryNo: string;
    productName: string;
    category: string;
    searchKeyword?: string;
  };
  DataComparison: {
    country: string;
    factoryNos: string[];
    productName: string;
    category: string;
    excludeFactoryNo?: string | null;
  };
  Brand: {brandName: string; category: string; searchKeyword?: string; initialTab?: 'offer' | 'inquiry'};
  BrandProduct: {brandName: string; productName: string; category: string; searchKeyword?: string; initialTab?: 'offer' | 'inquiry'};
  Profile: undefined;
  EditProfile: undefined;
  Inventory: undefined;
};
