import type {HomeCardItem, HotSearchItem} from '../types/api';

type Navigation = {
  navigate: (screen: string, params?: Record<string, unknown>) => void;
};

/**
 * 首页卡片点击路由
 */
export function openHomeCard(navigation: Navigation, category: string, card: HomeCardItem) {
  switch (card.cardType) {
    case 'product':
      if (card.productId) {
        navigation.navigate('Product', {productId: card.productId, category, productName: card.productName ?? ''});
      }
      break;
    case 'country':
      if (card.country) {
        navigation.navigate('Country', {country: card.country, category});
      }
      break;
    case 'brand':
      if (card.brandName) {
        navigation.navigate('Brand', {brandName: card.brandName, category});
      }
      break;
    case 'merchant':
      if (card.merchantId) {
        navigation.navigate('Merchant', {merchantId: card.merchantId, category});
      }
      break;
    case 'factory':
      if (card.country && card.factoryNo) {
        navigation.navigate('Factory', {country: card.country, factoryNo: card.factoryNo, category});
      }
      break;
    case 'countryProduct':
      if (card.country && card.productName) {
        navigation.navigate('CountryProduct', {country: card.country, productName: card.productName, category});
      }
      break;
    case 'factoryProduct':
      if (card.country && card.factoryNo && card.productName) {
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
        navigation.navigate('BrandProduct', {brandName: card.brandName, productName: card.productName, category});
      }
      break;
    default:
      break;
  }
}

/**
 * 热门搜索点击路由（与原 Android navigateToDetail 对齐）
 */
export function openHotSearch(navigation: Navigation, category: string, item: HotSearchItem) {
  const keyword = item.keyword ?? '';

  switch (item.dimension) {
    case '国家厂号产品': {
      const c = item.country;
      const fn = item.factoryNo;
      if (c && fn) {
        // keyword 格式: "巴西SIF941牛腩"，解析出产品名
        let productName = keyword;
        if (keyword.startsWith(c)) {
          productName = keyword.slice(c.length);
        }
        if (productName.startsWith(fn)) {
          productName = productName.slice(fn.length);
        }
        productName = productName.trim() || keyword;
        navigation.navigate('CountryFactoryProduct', {country: c, factoryNo: fn, productName, category});
      }
      break;
    }
    case '国家产品': {
      const c = item.country;
      if (c) {
        let productName = keyword;
        if (keyword.startsWith(c)) {
          productName = keyword.slice(c.length).trim();
        }
        productName = productName || keyword;
        navigation.navigate('CountryProduct', {country: c, productName, category});
      }
      break;
    }
    case '国家': {
      if (item.country) {
        navigation.navigate('Country', {country: item.country, category});
      }
      break;
    }
    case '产品': {
      if (item.productId) {
        navigation.navigate('Product', {productId: item.productId, category, productName: keyword});
      }
      break;
    }
    case '品牌': {
      navigation.navigate('Brand', {brandName: keyword, category});
      break;
    }
    case '商家': {
      if (item.merchantId) {
        navigation.navigate('Merchant', {merchantId: item.merchantId, category});
      }
      break;
    }
    case '国家厂号': {
      if (item.country && item.factoryNo) {
        navigation.navigate('Factory', {country: item.country, factoryNo: item.factoryNo, category});
      }
      break;
    }
    case '品牌产品': {
      // keyword 格式: "JBS S.A. 牛前八件套"
      const parts = keyword.split(/\s+/);
      if (parts.length >= 2) {
        const productName = parts[parts.length - 1];
        const brandName = parts.slice(0, -1).join(' ');
        navigation.navigate('BrandProduct', {brandName, productName, category});
      } else {
        navigation.navigate('Brand', {brandName: keyword, category});
      }
      break;
    }
    default: {
      // 兜底：尝试用已有字段路由
      if (item.merchantId) {
        navigation.navigate('Merchant', {merchantId: item.merchantId, category});
      } else if (item.productId) {
        navigation.navigate('Product', {productId: item.productId, category, productName: keyword});
      } else if (item.country && item.factoryNo) {
        navigation.navigate('Factory', {country: item.country, factoryNo: item.factoryNo, category});
      } else if (item.country) {
        navigation.navigate('Country', {country: item.country, category});
      } else {
        navigation.navigate('Search', {category});
      }
      break;
    }
  }
}
