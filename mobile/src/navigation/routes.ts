export type RootStackParamList = {
  Login: undefined;
  Home: undefined;
  Search: {category: string};
  HomeCards: {category?: string; tab?: 0 | 1} | undefined;
  Merchant: {merchantId: number | string; category: string};
  Product: {productId: number; category: string; productName: string};
  Country: {country: string; category: string};
  Factory: {country: string; factoryNo: string; category: string};
  CountryProduct: {country: string; productName: string; category: string};
  CountryFactoryProduct: {
    country: string;
    factoryNo: string;
    productName: string;
    category: string;
  };
  SubstituteProduct: {
    country: string;
    factoryNo: string;
    productName: string;
    category: string;
  };
  DataComparison: {
    country: string;
    factoryNos: string[];
    productName: string;
    category: string;
    excludeFactoryNo?: string | null;
  };
  Brand: {brandName: string; category: string};
  BrandProduct: {brandName: string; productName: string; category: string};
  Profile: undefined;
  EditProfile: undefined;
  Inventory: undefined;
};
