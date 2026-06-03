export type PriceSortOrder = 'asc' | 'desc';

export function isValidPrice(value?: number | null): value is number {
  return typeof value === 'number' && Number.isFinite(value) && value > 0;
}

export function hasPriceRange(min?: number | null, max?: number | null) {
  return isValidPrice(min) || isValidPrice(max);
}

export function normalizePriceRange(min?: number | null, max?: number | null) {
  const normalizedMin = isValidPrice(min) ? min : null;
  const normalizedMax = isValidPrice(max) ? max : null;

  if (normalizedMin != null && normalizedMax != null) {
    return {
      min: Math.min(normalizedMin, normalizedMax),
      max: Math.max(normalizedMin, normalizedMax),
    };
  }

  if (normalizedMin != null) {
    return {min: normalizedMin, max: normalizedMin};
  }

  if (normalizedMax != null) {
    return {min: normalizedMax, max: normalizedMax};
  }

  return {min: null, max: null};
}

export function comparePriceRange(
  aMin?: number | null,
  aMax?: number | null,
  bMin?: number | null,
  bMax?: number | null,
  order: PriceSortOrder = 'asc',
) {
  const a = normalizePriceRange(aMin, aMax);
  const b = normalizePriceRange(bMin, bMax);
  const aHasPrice = a.min != null;
  const bHasPrice = b.min != null;

  if (aHasPrice && !bHasPrice) return -1;
  if (!aHasPrice && bHasPrice) return 1;
  if (!aHasPrice && !bHasPrice) return 0;

  if (order === 'asc') {
    if (a.min !== b.min) return (a.min ?? 0) - (b.min ?? 0);
    return (a.max ?? 0) - (b.max ?? 0);
  }

  if (a.max !== b.max) return (b.max ?? 0) - (a.max ?? 0);
  return (b.min ?? 0) - (a.min ?? 0);
}

export function sortByPriceRangeWithNoPriceLast<T>(
  items: T[],
  getMin: (item: T) => number | null | undefined,
  getMax: (item: T) => number | null | undefined,
  order: PriceSortOrder = 'asc',
) {
  return items.slice().sort((left, right) =>
    comparePriceRange(getMin(left), getMax(left), getMin(right), getMax(right), order),
  );
}
