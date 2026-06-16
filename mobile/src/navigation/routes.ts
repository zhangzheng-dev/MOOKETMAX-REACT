export type RootStackParamList = {
  Login: undefined;
  Home: undefined;
  Search: {category: string; keyword?: string};
  HomeCards: {category?: string; tab?: 0 | 1} | undefined;
  Merchant: {merchantId: number | string; category: string};
  Product: {productId: number; category: string; productName: string; searchKeyword?: string};
  Country: {country: string; category: string; searchKeyword?: string};
  Factory: {country: string; factoryNo: string; category: string; searchKeyword?: string};
  CountryProduct: {country: string; productName: string; category: string; searchKeyword?: string};
  CountryFactoryProduct: {
    country: string;
    factoryNo: string;
    productName: string;
    category: string;
    searchKeyword?: string;
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
  Brand: {brandName: string; category: string; searchKeyword?: string};
  BrandProduct: {brandName: string; productName: string; category: string; searchKeyword?: string};
  Profile: undefined;
  EditProfile: undefined;
  Inventory: undefined;
};
