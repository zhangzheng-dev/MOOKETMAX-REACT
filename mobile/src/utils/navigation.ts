import {mooketApi} from '../api/mooketApi';
import type {HomeCardItem, HotSearchItem} from '../types/api';

type Navigation = {
  navigate: (screen: string, params?: Record<string, unknown>) => void;
};

function fireAndForget(task?: Promise<unknown>) {
  task?.catch(() => undefined);
}

function prefetchProduct(category: string, productId?: number | null) {
  if (!productId) return;
  fireAndForget(mooketApi.getProductDetail(productId, category));
}

function prefetchCountry(category: string, country?: string | null) {
  if (!country) return;
  fireAndForget(mooketApi.getCountryDetail(country, category));
}

function prefetchBrand(category: string, brandName?: string | null) {
  if (!brandName) return;
  fireAndForget(mooketApi.getBrandDetail(brandName, category));
}

function prefetchMerchant(category: string, merchantId?: number | string | null) {
  if (!merchantId) return;
  fireAndForget(mooketApi.getMerchantDetail(merchantId, category));
}

function prefetchFactory(category: string, country?: string | null, factoryNo?: string | null) {
  if (!country || !factoryNo) return;
  fireAndForget(mooketApi.getFactoryDetail(country, factoryNo, category));
}

function prefetchCountryProduct(category: string, country?: string | null, productName?: string | null) {
  if (!country || !productName) return;
  fireAndForget(mooketApi.getCountryProductDetail(country, productName, category));
}

function prefetchCountryFactoryProduct(
  category: string,
  country?: string | null,
  factoryNo?: string | null,
  productName?: string | null,
) {
  if (!country || !factoryNo || !productName) return;
  fireAndForget(mooketApi.getCountryFactoryProductDetail(country, factoryNo, productName, category));
}

function prefetchBrandProduct(category: string, brandName?: string | null, productName?: string | null) {
  if (!brandName || !productName) return;
  fireAndForget(mooketApi.getBrandProductDetail(brandName, productName, category));
}

export function openHomeCard(navigation: Navigation, category: string, card: HomeCardItem) {
  switch (card.cardType) {
    case 'product':
      if (card.productId) {
        prefetchProduct(category, card.productId);
        navigation.navigate('Product', {productId: card.productId, category, productName: card.productName ?? ''});
      }
      break;
    case 'country':
      if (card.country) {
        prefetchCountry(category, card.country);
        navigation.navigate('Country', {country: card.country, category});
      }
      break;
    case 'brand':
      if (card.brandName) {
        prefetchBrand(category, card.brandName);
        navigation.navigate('Brand', {brandName: card.brandName, category});
      }
      break;
    case 'merchant':
      if (card.merchantId) {
        prefetchMerchant(category, card.merchantId);
        navigation.navigate('Merchant', {merchantId: card.merchantId, category});
      }
      break;
    case 'factory':
      if (card.country && card.factoryNo) {
        prefetchFactory(category, card.country, card.factoryNo);
        navigation.navigate('Factory', {country: card.country, factoryNo: card.factoryNo, category});
      }
      break;
    case 'countryProduct':
      if (card.country && card.productName) {
        prefetchCountryProduct(category, card.country, card.productName);
        navigation.navigate('CountryProduct', {country: card.country, productName: card.productName, category});
      }
      break;
    case 'factoryProduct':
      if (card.country && card.factoryNo && card.productName) {
        prefetchCountryFactoryProduct(category, card.country, card.factoryNo, card.productName);
        navigation.navigate('CountryFactoryProduct', {
          country: card.country,
          factoryNo: card.factoryNo,
          productName: card.productName,
          category,
        });
      }
      break;
    case 'brandProduct':
      if (card.brandName && card.productName) {
        prefetchBrandProduct(category, card.brandName, card.productName);
        navigation.navigate('BrandProduct', {brandName: card.brandName, productName: card.productName, category});
      }
      break;
    default:
      break;
  }
}

export function openHotSearch(navigation: Navigation, category: string, item: HotSearchItem) {
  const keyword = item.keyword ?? '';

  switch (item.dimension) {
    case '国家厂号产品': {
      const country = item.country;
      const factoryNo = item.factoryNo;
      if (!country || !factoryNo) break;

      let productName = keyword;
      if (keyword.startsWith(country)) {
        productName = keyword.slice(country.length);
      }
      if (productName.startsWith(factoryNo)) {
        productName = productName.slice(factoryNo.length);
      }
      productName = productName.trim() || keyword;

      prefetchCountryFactoryProduct(category, country, factoryNo, productName);
      navigation.navigate('CountryFactoryProduct', {country, factoryNo, productName, category});
      break;
    }
    case '国家产品': {
      const country = item.country;
      if (!country) break;

      let productName = keyword;
      if (keyword.startsWith(country)) {
        productName = keyword.slice(country.length).trim();
      }
      productName = productName || keyword;

      prefetchCountryProduct(category, country, productName);
      navigation.navigate('CountryProduct', {country, productName, category});
      break;
    }
    case '国家':
      if (item.country) {
        prefetchCountry(category, item.country);
        navigation.navigate('Country', {country: item.country, category});
      }
      break;
    case '产品':
      if (item.productId) {
        prefetchProduct(category, item.productId);
        navigation.navigate('Product', {productId: item.productId, category, productName: keyword});
      }
      break;
    case '品牌':
      prefetchBrand(category, keyword);
      navigation.navigate('Brand', {brandName: keyword, category});
      break;
    case '商家':
      if (item.merchantId) {
        prefetchMerchant(category, item.merchantId);
        navigation.navigate('Merchant', {merchantId: item.merchantId, category});
      }
      break;
    case '国家厂号':
      if (item.country && item.factoryNo) {
        prefetchFactory(category, item.country, item.factoryNo);
        navigation.navigate('Factory', {country: item.country, factoryNo: item.factoryNo, category});
      }
      break;
    case '品牌产品': {
      const parts = keyword.split(/\s+/);
      if (parts.length >= 2) {
        const productName = parts[parts.length - 1];
        const brandName = parts.slice(0, -1).join(' ');
        prefetchBrandProduct(category, brandName, productName);
        navigation.navigate('BrandProduct', {brandName, productName, category});
      } else {
        prefetchBrand(category, keyword);
        navigation.navigate('Brand', {brandName: keyword, category});
      }
      break;
    }
    default:
      if (item.merchantId) {
        prefetchMerchant(category, item.merchantId);
        navigation.navigate('Merchant', {merchantId: item.merchantId, category});
      } else if (item.productId) {
        prefetchProduct(category, item.productId);
        navigation.navigate('Product', {productId: item.productId, category, productName: keyword});
      } else if (item.country && item.factoryNo) {
        prefetchFactory(category, item.country, item.factoryNo);
        navigation.navigate('Factory', {country: item.country, factoryNo: item.factoryNo, category});
      } else if (item.country) {
        prefetchCountry(category, item.country);
        navigation.navigate('Country', {country: item.country, category});
      } else {
        navigation.navigate('Search', {category});
      }
      break;
  }
}
