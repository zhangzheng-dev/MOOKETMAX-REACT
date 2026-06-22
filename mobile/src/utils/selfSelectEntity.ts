import type {HomeCardItem} from '../types/api';

export function getSelfSelectEntityName(card: HomeCardItem | null | undefined) {
  if (!card) return '';

  switch (card.cardType) {
    case 'product':
      return card.productName?.trim() || '';
    case 'country':
      return card.country?.trim() || '';
    case 'brand':
      return card.brandName?.trim() || '';
    case 'merchant':
      return card.merchantName?.trim() || card.merchantShortName?.trim() || '';
    case 'factory':
      return `${card.country ?? ''}${card.factoryNo ?? ''}`.trim();
    case 'countryProduct':
      return `${card.country ?? ''}${card.productName ?? ''}`.trim();
    case 'factoryProduct':
      return `${card.country ?? ''}${card.factoryNo ?? ''}${card.productName ?? ''}`.trim();
    case 'brandProduct':
      return `${card.brandName ?? ''}${card.productName ?? ''}`.trim();
    default:
      return '';
  }
}

export function getAddSelfSelectMessage(card: HomeCardItem | null | undefined) {
  const entityName = getSelfSelectEntityName(card);
  return entityName
    ? `确定将“${entityName}”添加为自选吗？`
    : '确定加入自选吗？';
}

export function getRemoveSelfSelectMessage(card: HomeCardItem | null | undefined) {
  const entityName = getSelfSelectEntityName(card);
  return entityName
    ? `确定将“${entityName}”移出自选吗？`
    : '确定移出自选吗？';
}
