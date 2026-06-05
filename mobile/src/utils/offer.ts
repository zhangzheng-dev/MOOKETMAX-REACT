export function parseWeight(weight?: string | null): [string, string] {
  if (!weight || weight.trim() === '') {
    return ['', ''];
  }
  const match = weight.trim().match(/^([0-9]+(?:\.[0-9]+)?)(.*)$/);
  if (!match) {
    return [weight.trim(), ''];
  }
  const numeric = Number(match[1]);
  const unit = match[2].trim();
  if (!Number.isFinite(numeric)) {
    return [match[1], unit];
  }
  const rounded = Math.round(numeric * 10) / 10;
  const text = Number.isInteger(rounded) ? `${rounded}` : rounded.toFixed(1);
  return [text, unit];
}

export function extractCity(location?: string | null): string {
  if (!location) {
    return '';
  }
  const parts = location
    .split(/[\/\\,，、\s]+/)
    .map(item => item.trim())
    .filter(Boolean);
  return parts[parts.length - 1] ?? '';
}

export function formatPublishTime(time?: string | null): string {
  if (!time) {
    return '';
  }
  const match = time.match(/(\d{4})-(\d{2})-(\d{2})[T ](\d{2}):(\d{2})/);
  if (!match) {
    return time;
  }
  return `${match[2]}-${match[3]} ${match[4]}:${match[5]}`;
}

export function computePriceRange(
  employeePrices: Array<number | null | undefined> | null | undefined,
  fallbackMin?: number | null,
  fallbackMax?: number | null,
): [string | null, string | null] {
  const valid = (employeePrices ?? []).filter(
    (value): value is number => typeof value === 'number' && Number.isFinite(value) && value > 0,
  );
  if (valid.length > 0) {
    const min = Math.min(...valid);
    const max = Math.max(...valid);
    if (min !== max) {
      return [`¥ ${min} - ${max}`, '/kg'];
    }
    return [`¥ ${min}`, '/kg'];
  }
  if (
    fallbackMin != null &&
    fallbackMin > 0 &&
    fallbackMax != null &&
    fallbackMax > 0 &&
    fallbackMin !== fallbackMax
  ) {
    return [`¥ ${fallbackMin} - ${fallbackMax}`, '/kg'];
  }
  if (fallbackMin != null && fallbackMin > 0) {
    return [`¥ ${fallbackMin}`, '/kg'];
  }
  return ['协商报价', null];
}

export function colorForTag(tag: string): {bg: string; fg: string} {
  if (tag.includes('大日期') || tag.includes('日期')) {
    return {bg: '#F2F3FF', fg: '#3163DC'};
  }
  if (tag.includes('可开证') || tag.includes('证')) {
    return {bg: '#FFF5E4', fg: '#A07D17'};
  }
  if (tag.includes('整柜') || tag.includes('柜')) {
    return {bg: '#FFF0ED', fg: '#D54941'};
  }
  return {bg: '#F3F6F5', fg: '#3C4947'};
}

export type OfferFieldKind =
  | 'goodsType'
  | 'feedingType'
  | 'fatRatio'
  | 'cattleBreed'
  | 'remark'
  | 'tag';

export function colorForOfferField(kind: OfferFieldKind): {bg: string; fg: string} {
  switch (kind) {
    case 'goodsType':
      return {bg: '#EEF4FF', fg: '#3767D6'};
    case 'feedingType':
      return {bg: '#EEF8F2', fg: '#1F8A55'};
    case 'fatRatio':
      return {bg: '#FFF2E8', fg: '#C96A1A'};
    case 'cattleBreed':
      return {bg: '#F7EEFF', fg: '#7A47B8'};
    case 'remark':
      return {bg: '#FFF1F0', fg: '#D54941'};
    case 'tag':
    default:
      return {bg: '#F3F6F5', fg: '#3C4947'};
  }
}

export function splitTags(tags?: string | null, maxCount = 4): string[] {
  if (!tags) {
    return [];
  }
  return tags
    .split(/[,，、]+/)
    .map(item => item.trim())
    .filter(Boolean)
    .slice(0, maxCount);
}
