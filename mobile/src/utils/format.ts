export function formatCount(value?: number | null) {
  if (value == null) {
    return '--';
  }
  if (value >= 10000) {
    return `${(value / 10000).toFixed(value >= 100000 ? 0 : 1)}w`;
  }
  if (value >= 1000) {
    return `${(value / 1000).toFixed(1)}k`;
  }
  return `${value}`;
}

export function formatPriceRange(min?: number | null, max?: number | null) {
  if (min == null && max == null) {
    return '--';
  }
  if (min != null && max != null && Math.abs(min - max) > 0.001) {
    return `¥${min.toFixed(2)}-${max.toFixed(2)}`;
  }
  const value = min ?? max;
  return value == null ? '--' : `¥${value.toFixed(2)}`;
}

export function asText(value: unknown) {
  if (value == null) {
    return '';
  }
  return String(value);
}
