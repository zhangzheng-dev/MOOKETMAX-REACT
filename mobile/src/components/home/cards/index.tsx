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
  onLongPress?: () => void;
};

/**
 * 8 绉嶅崱鐗囩被鍨嬪垎鍙戯細涓庡師 Android 璁捐 1:1 瀵瑰簲
 */
export function HomeCardSwitcher({card, onPress, onLongPress}: Props) {
  switch (card.cardType) {
    case 'product':
      return <ProductCard card={card} onPress={onPress} onLongPress={onLongPress} />;
    case 'country':
      return <CountryCard card={card} onPress={onPress} onLongPress={onLongPress} />;
    case 'brand':
      return <BrandCard card={card} onPress={onPress} onLongPress={onLongPress} />;
    case 'merchant':
      return <MerchantCard card={card} onPress={onPress} onLongPress={onLongPress} />;
    case 'factory':
      return <FactoryCard card={card} onPress={onPress} onLongPress={onLongPress} />;
    case 'countryProduct':
      return <CountryProductCard card={card} onPress={onPress} onLongPress={onLongPress} />;
    case 'factoryProduct':
      return <FactoryProductCard card={card} onPress={onPress} onLongPress={onLongPress} />;
    case 'brandProduct':
      return <BrandProductCard card={card} onPress={onPress} onLongPress={onLongPress} />;
    default:
      // 鍏滃簳锛氫娇鐢?product 鍗℃牱寮?
      return <ProductCard card={card} onPress={onPress} onLongPress={onLongPress} />;
  }
}
