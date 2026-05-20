/**
 * 解析重量字符串：数字部分四舍五入保留 1 位小数，整数则不带小数
 * 例如："1000吨" -> ["1000", "吨"], "1000 MT" -> ["1000", "MT"]
 */
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

/**
 * 从 "河北省/北京" 等格式中抽取末尾城市
 */
export function extractCity(location?: string | null): string {
  if (!location) {
    return '';
  }
  const parts = location.split(/[\\/／、]/).map(item => item.trim()).filter(Boolean);
  return parts[parts.length - 1] ?? '';
}

/**
 * "2025-05-16 10:30:00" 等时间字符串 -> "MM-dd HH:mm"，失败时原样返回
 */
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

/**
 * 价格区间格式（含 ¥ 与单位 /kg）。
 * - employeeOffers 优先取所有员工的最小/最大价
 * - 否则用 OfferSummary 自身 price/priceMax
 * 返回 [区间文本, 单位] 或 [null, null]
 */
export function computePriceRange(
  employeePrices: Array<number | null | undefined> | null | undefined,
  fallbackMin?: number | null,
  fallbackMax?: number | null,
): [string | null, string | null] {
  const valid = (employeePrices ?? []).filter((value): value is number => typeof value === 'number' && Number.isFinite(value) && value > 0);
  if (valid.length > 0) {
    const min = Math.min(...valid);
    const max = Math.max(...valid);
    if (min !== max) {
      return [`¥ ${min} - ${max}`, '/kg'];
    }
    return [`¥ ${min}`, '/kg'];
  }
  if (fallbackMin != null && fallbackMin > 0 && fallbackMax != null && fallbackMax > 0 && fallbackMin !== fallbackMax) {
    return [`¥ ${fallbackMin} - ${fallbackMax}`, '/kg'];
  }
  if (fallbackMin != null && fallbackMin > 0) {
    return [`¥ ${fallbackMin}`, '/kg'];
  }
  return ['协商报价', null];
}

/**
 * 标签颜色（背景/文字）映射规则，匹配原 Android 设计：
 * - 大日期/日期：蓝色
 * - 可开票/票：黄色
 * - 整柜/柜：红色
 * - 一口价/价：灰色
 */
export function colorForTag(tag: string): {bg: string; fg: string} {
  if (tag.includes('大日期') || tag.includes('日期')) {
    return {bg: '#F2F3FF', fg: '#3163DC'};
  }
  if (tag.includes('可开票') || tag.includes('票')) {
    return {bg: '#FFF5E4', fg: '#A07D17'};
  }
  if (tag.includes('整柜') || tag.includes('柜')) {
    return {bg: '#FFF0ED', fg: '#D54941'};
  }
  return {bg: '#F3F6F5', fg: '#3C4947'};
}

/**
 * 切割 tags 字段（逗号或中文逗号分隔），最多 maxCount 条
 */
export function splitTags(tags?: string | null, maxCount = 4): string[] {
  if (!tags) {
    return [];
  }
  return tags
    .split(/[,，]/)
    .map(item => item.trim())
    .filter(Boolean)
    .slice(0, maxCount);
}
