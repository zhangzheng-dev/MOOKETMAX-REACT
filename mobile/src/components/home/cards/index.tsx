import React from 'react';
import type {HomeCardItem} from '../../../types/api';
import {BrandCard} from './BrandCard';
import {BrandProductCard} from './BrandProductCard';
import {CountryCard} from './CountryCard';
import {CountryProductCard} from './CountryProductCard';
import {FactoryCard} from './FactoryCard';
import {FactoryProductCard} from './FactoryProductCard';
import {MerchantCard} from './MerchantCard';
import {ProductCard} from './ProductCard';

type Props = {
  card: HomeCardItem;
  onPress?: () => void;
};

/**
 * 8 种卡片类型分发：与原 Android 设计 1:1 对应
 */
export function HomeCardSwitcher({card, onPress}: Props) {
  switch (card.cardType) {
    case 'product':
      return <ProductCard card={card} onPress={onPress} />;
    case 'country':
      return <CountryCard card={card} onPress={onPress} />;
    case 'brand':
      return <BrandCard card={card} onPress={onPress} />;
    case 'merchant':
      return <MerchantCard card={card} onPress={onPress} />;
    case 'factory':
      return <FactoryCard card={card} onPress={onPress} />;
    case 'countryProduct':
      return <CountryProductCard card={card} onPress={onPress} />;
    case 'factoryProduct':
      return <FactoryProductCard card={card} onPress={onPress} />;
    case 'brandProduct':
      return <BrandProductCard card={card} onPress={onPress} />;
    default:
      // 兜底：使�?product 卡样�?
      return <ProductCard card={card} onPress={onPress} />;
  }
}
