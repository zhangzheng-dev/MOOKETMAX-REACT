/* eslint-disable @typescript-eslint/no-unused-vars */
import {
  ContainerPriceConfig,
  ContainerPriceConfigMap,
  DeliveryDateMap,
  InventoryItem,
  MarketPrice,
  ParamSet,
  TieredRate,
} from '../api/inventory';

const DEFAULT_PARAM_SET: ParamSet = {
  parameter_set_id: 1,
  parameter_set_name: '标准配置',
  factory_prepayment_ratio: 0.4,
  client_own_fund_ratio: 0.2,
  funder_advance_ratio: 0.2,
  prepayment_interest_rate: 0.065,
  transit_payment_interest_rate: 0.065,
  tax_payment_interest_rate: 0.065,
  storage_cost_per_ton_day: 2.2,
  tariff_rate: 0.12,
  vat_rate: 0.09,
  prepayment_fee_per_container: 500,
  misc_cost_per_container: 500,
  agent_fee_per_container: 5500,
  procurement_cost_per_ton: 150,
  warehouse_cost_per_ton: 60,
  insurance_fee_rate: 0.0008,
  default_fx_usd_cny: 7,
};

const DEFAULT_DEPOSIT_AMOUNT = 200000;
const DEFAULT_DELIVERY_DELAY_DAYS = 5;
const MS_PER_DAY = 1000 * 60 * 60 * 24;

type PivotStatus = 'pending' | 'transit' | 'stock';

export interface PivotContext {
  paramSets?: ParamSet[];
  priceConfig?: ContainerPriceConfigMap;
  deliveryDates?: DeliveryDateMap;
  marketPrices?: MarketPrice[];
  valuationDate?: Date;
}

interface EnrichedInventoryItem extends InventoryItem {
  physical_status_resolved: string;
  storage_days: number;
  current_cost_rmb_per_kg_resolved: number;
  current_total_cost_rmb: number;
  delivery_cost_rmb_per_kg: number | null;
  delivery_total_cost_rmb: number | null;
  market_price_rmb_per_kg: number;
  estimated_receivable_rmb: number;
  estimated_net_cash_before_delivery_resolved: number;
  estimated_net_cash_after_delivery_resolved: number;
  estimated_profit_rmb_resolved: number;
  daily_cost_rmb_resolved: number;
}

export interface FactoryDetail {
  factory_code: string;
  country: string | null;
  weight_kg: number;
  pieces: number;
  containers: number;
  pendingWeight: number;
  transitWeight: number;
  inStockWeight: number;
  occupiedCash: number;
  floatingPnL: number;
  recoverableCash: number;
  avgCost: number;
  avgStorageAgeDays: number | null;
  cold_storage: string | null;
}

export interface PivotProduct {
  product_name: string;
  totalWeight: number;
  totalPieces: number;
  containers: number;
  weightedAvgCost: number;
  pendingWeight: number;
  transitWeight: number;
  inStockWeight: number;
  occupiedCash: number;
  floatingPnL: number;
  recoverableCash: number;
  netCashBefore: number;
  dailyBurn: number;
  sourceContainers: string[];
  factories: FactoryDetail[];
}

export interface PivotSummary {
  totalProducts: number;
  totalWeight: number;
  totalPieces: number;
  totalItems: number;
  totalContainers: number;
  totalOccupiedCash: number;
  totalFloatingPnL: number;
  totalRecoverableCash: number;
  totalNetCashBefore: number;
  totalDailyBurn: number;
  watchedProducts: number;
  estimatedProfit: number;
}

export interface ResolvedInventoryRow {
  id: string;
  contractId: string;
  productName: string;
  containerId: string;
  skuCode: string;
  factoryCode: string;
  country: string;
  coldStorage: string;
  funder: string;
  customGroup: string;
  pieces: number;
  weightKg: number;
  costPerKg: number;
  sellingPricePerKg: number;
  dailyCost: number;
  recoverableCash: number;
  profit: number;
  productionDate: string;
  status: string;
  occupiedCash: number;
}

function validDate(dateStr: string | null | undefined): Date | null {
  if (!dateStr) return null;
  const d = new Date(dateStr);
  return isNaN(d.getTime()) ? null : d;
}

function startOfDay(date: Date): Date {
  const next = new Date(date);
  next.setHours(0, 0, 0, 0);
  return next;
}

function formatDate(date: Date): string {
  return date.toISOString().split('T')[0];
}

function addDays(date: Date, days: number): Date {
  const next = new Date(date);
  next.setDate(next.getDate() + days);
  return next;
}

function daysBetweenDates(from: string | null | undefined, to: Date): number {
  const start = validDate(from);
  if (!start) return 0;
  return Math.floor((to.getTime() - start.getTime()) / MS_PER_DAY);
}

function inventoryAgeDays(item: InventoryItem, today = new Date()): number | null {
  const date = validDate(item.production_date);
  if (!date) return null;
  return Math.max(0, Math.floor((today.getTime() - date.getTime()) / MS_PER_DAY));
}

function itemWeight(item: InventoryItem): number {
  return item.actual_weight_kg || item.estimated_weight_kg || item.weight_kg || 0;
}

function isValidPivotItem(item: InventoryItem): boolean {
  const hasSpotPrice = Boolean(item.spot_price_rmb_per_kg && item.spot_price_rmb_per_kg > 0);
  const hasPrice =
    hasSpotPrice || Boolean(item.future_price_usd_per_t && item.future_price_usd_per_t > 0);
  const hasWeight = hasSpotPrice
    ? Boolean(item.weight_kg || item.actual_weight_kg || item.estimated_weight_kg)
    : Boolean(item.estimated_weight_kg && item.estimated_weight_kg > 0);

  return Boolean(item.container_id?.trim() && item.product_name?.trim() && hasPrice && hasWeight);
}

function getPhysicalStatus(item: InventoryItem, today = new Date()): string {
  if (item.physical_status) return item.physical_status;

  const storageEntry = validDate(item.storage_entry_date);
  if (storageEntry && today >= storageEntry) return '现货(在库)';

  const actualArrival = validDate(item.actual_arrival_date);
  if (actualArrival && today >= actualArrival && !storageEntry) return '清关/待入库';

  const shipping = validDate(item.shipping_date);
  if (shipping && today >= shipping) {
    const actualOrEta = validDate(item.actual_arrival_date) ?? validDate(item.eta_date);
    if (!actualOrEta || today < actualOrEta) return '在途(海运)';
    return '在途(海运)';
  }

  return '待发货';
}

function statusBucket(status: string): PivotStatus {
  if (status === '现货(在库)') return 'stock';
  if (status === '在途(海运)' || status === '清关/待入库') return 'transit';
  return 'pending';
}

function paramFor(item: InventoryItem, context?: PivotContext): ParamSet {
  const params = context?.paramSets?.length ? context.paramSets : [DEFAULT_PARAM_SET];
  return (
    params.find(param => param.parameter_set_id === item.parameter_set_id) ??
    params[0] ??
    DEFAULT_PARAM_SET
  );
}

function safeDiv(numerator: number, denominator: number, fallback = 0): number {
  if (!denominator || !isFinite(denominator)) return fallback;
  const result = numerator / denominator;
  return isFinite(result) ? result : fallback;
}

function prepaymentDate(item: InventoryItem): string | null {
  if (item.prepayment_date) return item.prepayment_date;
  const shipping = validDate(item.shipping_date);
  return shipping ? formatDate(addDays(shipping, -15)) : null;
}

function arrivalDate(item: InventoryItem): string | null {
  return item.actual_arrival_date ?? item.eta_date;
}

function transitPaymentDate(item: InventoryItem): string | null {
  if (item.transit_payment_date) return item.transit_payment_date;
  const arrival = validDate(arrivalDate(item));
  return arrival ? formatDate(addDays(arrival, -14)) : null;
}

function taxPaymentDate(item: InventoryItem): string | null {
  return item.tax_payment_date ?? arrivalDate(item);
}

function interestFor(amount: number, days: number, rate: number, tieredRates?: TieredRate[]): number {
  if (days <= 0 || amount <= 0) return 0;
  if (!tieredRates?.length) return (amount * rate * days) / 360;

  let total = 0;
  let cursor = 0;
  const sortedRates = [...tieredRates].sort((a, b) => a.startDay - b.startDay);

  while (cursor < days) {
    let tier: TieredRate | null = null;
    for (const candidate of sortedRates) {
      const endDay = candidate.endDay ?? Number.POSITIVE_INFINITY;
      if (cursor >= candidate.startDay && cursor < endDay) {
        tier = candidate;
        break;
      }
    }

    let activeRate = rate;
    let span = days - cursor;
    if (tier) {
      activeRate = tier.rate;
      span = Math.min((tier.endDay ?? Number.POSITIVE_INFINITY) - cursor, days - cursor);
    } else {
      const nextStart = sortedRates
        .filter(candidate => candidate.startDay > cursor)
        .reduce((min, candidate) => Math.min(min, candidate.startDay), Number.POSITIVE_INFINITY);
      span = Math.min(nextStart - cursor, days - cursor);
    }

    total += (amount * activeRate * span) / 360;
    cursor += span;
  }

  return total;
}

function tieredRateFor(days: number, defaultRate: number, tieredRates?: TieredRate[]): number {
  if (days <= 0 || !tieredRates?.length) return defaultRate;
  const sortedRates = [...tieredRates].sort((a, b) => a.startDay - b.startDay);
  const tier = sortedRates.find(rate => {
    const endDay = rate.endDay ?? Number.POSITIVE_INFINITY;
    return days >= rate.startDay && days < endDay;
  });
  return tier?.rate ?? defaultRate;
}

function calcBaseCostPerKg(item: InventoryItem, param: ParamSet, status: string): number {
  if (item.spot_price_rmb_per_kg != null && item.spot_price_rmb_per_kg > 0) {
    return item.spot_price_rmb_per_kg;
  }

  const future = item.future_price_usd_per_t;
  if (!future || future <= 0) return 0;

  const futurePerKg = future / 1000;
  const insuranceRate = param.insurance_fee_rate;
  const tariffRate = param.tariff_rate;
  const vatRate = param.vat_rate;

  if (status === '待发货' || status === '在途(海运)') {
    const fx = item.prepayment_fx_usd_cny || param.default_fx_usd_cny;
    const weight = item.estimated_weight_kg || item.weight_kg || 0;
    if (weight <= 0) return 0;

    const goods = futurePerKg * weight * fx;
    const insurance = goods * 1.1 * insuranceRate;
    const dutiable = goods + insurance;
    const tariff = dutiable * tariffRate;
    const vat = (dutiable + tariff) * vatRate;
    return safeDiv(goods + tariff + vat, weight);
  }

  const estimateWeight = item.estimated_weight_kg || item.weight_kg || 0;
  if (!estimateWeight) return 0;

  const prepaymentFx = item.prepayment_fx_usd_cny || param.default_fx_usd_cny;
  const estimateGoodsRmb = futurePerKg * estimateWeight * prepaymentFx;
  const prepayment = estimateGoodsRmb * param.factory_prepayment_ratio;

  const actualWeight = item.actual_weight_kg || estimateWeight;
  const transitFx = item.transit_payment_fx_usd_cny || prepaymentFx || param.default_fx_usd_cny;
  const actualGoodsUsd = futurePerKg * actualWeight;
  const prepaidGoodsUsd = futurePerKg * estimateWeight * param.factory_prepayment_ratio;
  const estimatedTransitPayment = (actualGoodsUsd - prepaidGoodsUsd) * transitFx;
  const transitPayment = item.actual_transit_payment_rmb ?? estimatedTransitPayment;

  const taxFx = item.tax_payment_fx_usd_cny || prepaymentFx || param.default_fx_usd_cny;
  const goodsForTax = futurePerKg * actualWeight * taxFx;
  const insurance = goodsForTax * 1.1 * insuranceRate;
  const dutiable = goodsForTax + insurance;
  const tariff = dutiable * tariffRate;
  const vat = (dutiable + tariff) * vatRate;
  const taxPayment = item.actual_tax_payment_rmb ?? tariff + vat;

  return safeDiv(prepayment + transitPayment + taxPayment, actualWeight);
}

function calcContainerCosts(param: ParamSet, actualWeight: number, estimateWeight: number, status: string) {
  const prepaymentWeight = estimateWeight || actualWeight || 1;
  return {
    misc: status === '待发货' ? safeDiv(param.misc_cost_per_container, actualWeight || 1) : 0,
    agent: safeDiv(param.agent_fee_per_container, actualWeight || 1),
    prepaymentFee: safeDiv(param.prepayment_fee_per_container, prepaymentWeight),
  };
}

function calcTonCosts(param: ParamSet, status: string) {
  return {
    procurement: param.procurement_cost_per_ton / 1000,
    warehouse: status === '现货(在库)' ? param.warehouse_cost_per_ton / 1000 : 0,
  };
}

function calcInsurancePerKg(item: InventoryItem, param: ParamSet, actualWeight: number): number {
  if (!item.future_price_usd_per_t || !item.estimated_weight_kg) return 0;
  const fx = item.prepayment_fx_usd_cny || param.default_fx_usd_cny;
  const insurance =
    (item.future_price_usd_per_t / 1000) *
    item.estimated_weight_kg *
    fx *
    1.1 *
    param.insurance_fee_rate;
  return safeDiv(insurance, actualWeight);
}

function calcPrepaymentInterest(
  item: InventoryItem,
  param: ParamSet,
  targetDate: Date,
  status: string,
): number {
  const date = prepaymentDate(item);
  if ((status === '待发货' && !date) || !date) return 0;

  const days = daysBetweenDates(date, targetDate);
  if (days <= 0) return 0;

  const isSpot = Boolean(item.spot_price_rmb_per_kg && item.spot_price_rmb_per_kg > 0);
  if (isSpot) {
    if (item.actual_prepayment_rmb != null && item.actual_prepayment_rmb > 0) {
      return interestFor(
        item.actual_prepayment_rmb,
        days,
        param.prepayment_interest_rate,
        param.prepayment_tiered_rates,
      );
    }
    return 0;
  }

  const estimateWeight = item.estimated_weight_kg || item.weight_kg || 0;
  if (!item.future_price_usd_per_t || !estimateWeight) return 0;

  const fx = item.prepayment_fx_usd_cny || param.default_fx_usd_cny;
  const goods = (item.future_price_usd_per_t / 1000) * estimateWeight * fx;
  const factoryPrepayment = goods * param.factory_prepayment_ratio;
  const principal =
    item.actual_prepayment_rmb != null
      ? factoryPrepayment - item.actual_prepayment_rmb
      : goods * param.funder_advance_ratio;

  if (principal <= 0) return 0;
  return interestFor(principal, days, param.prepayment_interest_rate, param.prepayment_tiered_rates);
}

function calcTransitInterest(
  item: InventoryItem,
  param: ParamSet,
  targetDate: Date,
  status: string,
): number {
  const date = transitPaymentDate(item);
  const isSpot = Boolean(item.spot_price_rmb_per_kg && item.spot_price_rmb_per_kg > 0);
  if ((status === '待发货' && !isSpot) || !date) return 0;

  const days = daysBetweenDates(date, targetDate);
  if (days <= 0) return 0;

  let principal = 0;
  if (item.actual_transit_payment_rmb != null) {
    principal = item.actual_transit_payment_rmb;
  } else if (isSpot) {
    const weight = item.actual_weight_kg || item.estimated_weight_kg || item.weight_kg || 0;
    if (!weight || !item.spot_price_rmb_per_kg) return 0;
    principal = Math.max(0, item.spot_price_rmb_per_kg * weight - (item.actual_prepayment_rmb || 0));
  } else {
    const weight = item.actual_weight_kg || item.estimated_weight_kg || item.weight_kg || 0;
    if (!item.future_price_usd_per_t || !weight) return 0;
    const fx =
      item.transit_payment_fx_usd_cny || item.prepayment_fx_usd_cny || param.default_fx_usd_cny;
    const futurePerKg = item.future_price_usd_per_t / 1000;
    const estimateWeight = item.estimated_weight_kg || item.weight_kg || 0;
    const actualGoodsUsd = futurePerKg * weight;
    const prepaidGoodsUsd = futurePerKg * estimateWeight * param.factory_prepayment_ratio;
    principal = (actualGoodsUsd - prepaidGoodsUsd) * fx;
  }

  return interestFor(
    principal,
    days,
    param.transit_payment_interest_rate,
    param.transit_payment_tiered_rates,
  );
}

function calcTaxInterest(
  item: InventoryItem,
  param: ParamSet,
  targetDate: Date,
  status: string,
): number {
  const date = taxPaymentDate(item);
  const isSpot = Boolean(item.spot_price_rmb_per_kg && item.spot_price_rmb_per_kg > 0);
  if ((status === '待发货' && !isSpot) || !date) return 0;

  const days = daysBetweenDates(date, targetDate);
  if (days <= 0) return 0;

  let principal = 0;
  if (item.actual_tax_payment_rmb != null) {
    principal = item.actual_tax_payment_rmb;
  } else {
    if (!item.future_price_usd_per_t || !item.actual_weight_kg) return 0;
    const fx = item.tax_payment_fx_usd_cny || item.prepayment_fx_usd_cny || param.default_fx_usd_cny;
    const goods = (item.future_price_usd_per_t / 1000) * item.actual_weight_kg * fx;
    const insurance = goods * 1.1 * param.insurance_fee_rate;
    const dutiable = goods + insurance;
    const tariff = dutiable * param.tariff_rate;
    const vat = (dutiable + tariff) * param.vat_rate;
    principal = tariff + vat;
  }

  return interestFor(principal, days, param.tax_payment_interest_rate, param.tax_payment_tiered_rates);
}

function calcStorageCost(item: InventoryItem, param: ParamSet, targetDate: Date): number {
  const storageEntry = validDate(item.storage_entry_date);
  if (!storageEntry || targetDate < storageEntry) return 0;

  const days = daysBetweenDates(item.storage_entry_date, targetDate);
  if (days <= 0) return 0;

  const weight = item.actual_weight_kg || item.weight_kg || 0;
  return (weight / 1000) * param.storage_cost_per_ton_day * days;
}

function defaultDeliveryDate(item: InventoryItem, today: Date): string {
  const arrival = validDate(arrivalDate(item));
  if (!arrival) return formatDate(startOfDay(today));

  const currentDay = startOfDay(today);
  const minDeliveryDay = startOfDay(addDays(arrival, DEFAULT_DELIVERY_DELAY_DAYS));
  return formatDate(currentDay > minDeliveryDay ? currentDay : minDeliveryDay);
}

function deliveryDateFor(item: InventoryItem, context: PivotContext | undefined, today: Date): string {
  return context?.deliveryDates?.[item.container_id] ?? defaultDeliveryDate(item, today);
}

function itemRowKey(item: Pick<InventoryItem, 'container_id' | 'product_name'>): string {
  return `${item.container_id?.trim() ?? ''}__${item.product_name?.trim() ?? ''}`;
}

function normalizeCompareText(value: string | null | undefined): string {
  return String(value ?? '')
    .trim()
    .toLowerCase()
    .replace(/\s+/g, '')
    .replace(/[()（）\-_/]/g, '');
}

function factoryCodeAliases(value: string | null | undefined): string[] {
  const raw = String(value ?? '').trim();
  if (!raw) return [];
  const normalized = normalizeCompareText(raw);
  const compact = normalized.replace(/^sif/, '');
  const set = new Set([normalized, compact, `sif${compact}`]);
  return [...set].filter(Boolean);
}

function stripMarketSuffix(value: string | null | undefined): string {
  return String(value ?? '')
    .replace(/[-\s](F[\d+-]+|[\d]+[+-][\d]*)$/i, '')
    .trim();
}

function findBestMarketPrice(
  config: ContainerPriceConfig,
  marketPrices?: MarketPrice[],
): MarketPrice | null {
  if (!marketPrices?.length) return null;

  const targetFactory = normalizeCompareText(config.factoryCode);
  const targetProduct = normalizeCompareText(config.product);
  if (!targetFactory || !targetProduct) return null;

  const latestByKey = new Map<string, MarketPrice>();
  for (const price of marketPrices) {
    const key = `${normalizeCompareText(price.factoryCode)}||${normalizeCompareText(price.product)}`;
    const existing = latestByKey.get(key);
    const nextDate = price.latestDate || price.date || '';
    const currentDate = existing?.latestDate || existing?.date || '';
    if (!existing || nextDate > currentDate) {
      latestByKey.set(key, price);
    }
  }

  const exactKey = `${targetFactory}||${targetProduct}`;
  const exact = latestByKey.get(exactKey);
  if (exact) return exact;

  const fuzzyTarget = normalizeCompareText(stripMarketSuffix(config.product));
  for (const [, price] of latestByKey) {
    const priceFactory = normalizeCompareText(price.factoryCode);
    const priceProduct = normalizeCompareText(price.product);
    if (priceFactory !== targetFactory) continue;
    if (priceProduct.includes(fuzzyTarget) || fuzzyTarget.includes(priceProduct)) {
      return price;
    }
  }

  return null;
}

function hasExactPriceConfig(
  item: Pick<InventoryItem, 'container_id' | 'product_name'>,
  context?: PivotContext,
): boolean {
  const config = getPriceConfig(item as InventoryItem, context?.priceConfig);
  if (!config) return false;
  return Boolean(
    (config.lockedPrice != null && config.lockedPrice > 0) ||
      (config.estSellingPriceRmbPerKg != null && config.estSellingPriceRmbPerKg > 0) ||
      (config.latestPrice != null && config.latestPrice > 0) ||
      (config.currentPrice != null && config.currentPrice > 0) ||
      (config.price != null && config.price > 0) ||
      (config.factoryCode && config.product),
  );
}

function getMarketQuote(
  item: InventoryItem,
  context?: PivotContext,
): {price: number; date?: string | null} | null {
  const config = getPriceConfig(item, context?.priceConfig);

  if (config?.lockedPrice != null && config.lockedPrice > 0) {
    return {price: config.lockedPrice, date: config?.date ?? '手动锁价'};
  }

  const configuredPrice =
    config?.estSellingPriceRmbPerKg ??
    config?.latestPrice ??
    config?.currentPrice ??
    config?.price;
  if (configuredPrice != null && configuredPrice > 0) {
    return {price: configuredPrice, date: config?.date ?? null};
  }

  if (!config) return null;

  const matchedMarketPrice = findBestMarketPrice(config, context?.marketPrices);
  if (matchedMarketPrice && matchedMarketPrice.latestPrice > 0) {
    return {
      price: matchedMarketPrice.latestPrice,
      date: matchedMarketPrice.latestDate || matchedMarketPrice.date || null,
    };
  }

  return null;
}

function getPriceConfig(
  item: InventoryItem,
  priceConfig?: ContainerPriceConfigMap,
): ContainerPriceConfig | null {
  if (!priceConfig) return null;
  const skuKey = itemRowKey(item);
  return priceConfig[skuKey] ?? priceConfig[item.container_id] ?? null;
}

function calcDailyCost(
  item: InventoryItem,
  param: ParamSet,
  today: Date,
  status: string,
  actualWeight: number,
  estimateWeight: number,
): number {
  const prepayDate = prepaymentDate(item);
  const transitDate = transitPaymentDate(item);
  const taxDate = taxPaymentDate(item);
  const prepayDays = prepayDate ? Math.max(0, daysBetweenDates(prepayDate, today)) : 0;
  const transitDays = transitDate ? Math.max(0, daysBetweenDates(transitDate, today)) : 0;
  const taxDays = taxDate ? Math.max(0, daysBetweenDates(taxDate, today)) : 0;

  const fx = item.prepayment_fx_usd_cny || param.default_fx_usd_cny;
  const futurePerKg = (item.future_price_usd_per_t || 0) / 1000;
  const goods = futurePerKg * estimateWeight * fx;
  const expectedPrepayment = goods * param.factory_prepayment_ratio;
  const prepaymentPrincipal =
    item.actual_prepayment_rmb != null
      ? Math.max(0, expectedPrepayment - item.actual_prepayment_rmb)
      : goods * param.funder_advance_ratio;

  const transitFx = item.transit_payment_fx_usd_cny || fx;
  const transitPrincipal =
    item.actual_transit_payment_rmb ??
    (futurePerKg * actualWeight - futurePerKg * estimateWeight * param.factory_prepayment_ratio) *
      transitFx;

  const taxFx = item.tax_payment_fx_usd_cny || fx;
  const taxGoods = futurePerKg * actualWeight * taxFx;
  const taxInsurance = taxGoods * 1.1 * param.insurance_fee_rate;
  const dutiable = taxGoods + taxInsurance;
  const tariff = dutiable * param.tariff_rate;
  const vat = (dutiable + tariff) * param.vat_rate;
  const taxPrincipal = item.actual_tax_payment_rmb ?? tariff + vat;

  const prepayRate = tieredRateFor(
    prepayDays,
    param.prepayment_interest_rate,
    param.prepayment_tiered_rates,
  );
  const transitRate = tieredRateFor(
    transitDays,
    param.transit_payment_interest_rate,
    param.transit_payment_tiered_rates,
  );
  const taxRate = tieredRateFor(taxDays, param.tax_payment_interest_rate, param.tax_payment_tiered_rates);

  const prepayDaily = prepayDays > 0 ? (prepaymentPrincipal * prepayRate) / 360 : 0;
  const transitDaily = transitDays > 0 ? (transitPrincipal * transitRate) / 360 : 0;
  const taxDaily = taxDays > 0 ? (taxPrincipal * taxRate) / 360 : 0;
  const storageDaily = (actualWeight / 1000) * param.storage_cost_per_ton_day;

  if (status === '待发货') return prepayDaily;
  if (status === '在途(海运)') return prepayDaily + transitDaily;
  if (status === '清关/待入库') return prepayDaily + transitDaily + taxDaily;
  if (status === '现货(在库)') return prepayDaily + transitDaily + taxDaily + storageDaily;
  return 0;
}

function enrichItem(item: InventoryItem, context?: PivotContext): EnrichedInventoryItem {
  const today = context?.valuationDate ?? new Date();
  const param = paramFor(item, context);
  const status = getPhysicalStatus(item, today);
  const estimateWeight = item.estimated_weight_kg || item.weight_kg || 0;
  const actualWeight = item.actual_weight_kg || item.estimated_weight_kg || item.weight_kg || 1;
  const marketQuote = getMarketQuote(item, context);

  const baseCost = calcBaseCostPerKg(item, param, status);
  const containerCosts = calcContainerCosts(param, actualWeight, estimateWeight, status);
  const tonCosts = calcTonCosts(param, status);
  const insurancePerKg = calcInsurancePerKg(item, param, actualWeight);
  const prepaymentInterest = calcPrepaymentInterest(item, param, today, status);
  const transitInterest = calcTransitInterest(item, param, today, status);
  const taxInterest = calcTaxInterest(item, param, today, status);
  const storageCost = calcStorageCost(item, param, today);
  const portMiscPerKg = safeDiv(item.port_misc_fee || 0, actualWeight);
  const transitFeePerKg = safeDiv(item.transit_payment_fee || 0, actualWeight);
  const financingPerKg = safeDiv(prepaymentInterest + transitInterest + taxInterest, actualWeight);
  const storagePerKg = safeDiv(storageCost, actualWeight);

  let currentCost =
    baseCost +
    containerCosts.agent +
    containerCosts.prepaymentFee +
    tonCosts.procurement +
    insurancePerKg +
    financingPerKg +
    containerCosts.misc;

  if (status === '在途(海运)' || status === '清关/待入库' || status === '现货(在库)') {
    currentCost += transitFeePerKg;
  }
  if (status === '清关/待入库' || status === '现货(在库)') {
    currentCost += portMiscPerKg;
  }
  if (status === '现货(在库)') {
    currentCost += tonCosts.warehouse + storagePerKg;
  }

  const resolvedCost = item.current_cost_rmb_per_kg ?? currentCost;
  const resolvedCurrentTotalCost = resolvedCost * actualWeight;
  const deliveryDate = deliveryDateFor(item, context, today);
  const deliveryTarget = validDate(deliveryDate);
  let deliveryCost: number | null = null;
  let deliveryTotalCost: number | null = null;

  if (deliveryTarget) {
    const deliveryPrepayInterest = calcPrepaymentInterest(item, param, deliveryTarget, status);
    const deliveryTransitInterest = calcTransitInterest(item, param, deliveryTarget, status);
    const deliveryTaxInterest = calcTaxInterest(item, param, deliveryTarget, status);
    const deliveryStorageCost = calcStorageCost(item, param, deliveryTarget);
    const deliveryFinancingPerKg = safeDiv(
      deliveryPrepayInterest + deliveryTransitInterest + deliveryTaxInterest,
      actualWeight,
    );
    const deliveryStoragePerKg = safeDiv(deliveryStorageCost, actualWeight);

    deliveryCost =
      baseCost +
      containerCosts.agent +
      containerCosts.prepaymentFee +
      tonCosts.procurement +
      insurancePerKg +
      containerCosts.misc +
      deliveryFinancingPerKg +
      portMiscPerKg +
      transitFeePerKg;

    const storageEntry = validDate(item.storage_entry_date);
    if (storageEntry && deliveryTarget >= storageEntry) {
      deliveryCost += tonCosts.warehouse + deliveryStoragePerKg;
    }
    deliveryTotalCost = deliveryCost * actualWeight;
  }

  const marketPrice = marketQuote?.price ?? 0;
  const receivable = marketPrice * actualWeight;
  const pnlCostPerKg = item.current_cost_rmb_per_kg ?? deliveryCost ?? currentCost;
  const effectiveDeliveryTotalCost =
    item.current_cost_rmb_per_kg != null ? resolvedCurrentTotalCost : deliveryTotalCost ?? resolvedCurrentTotalCost;
  const profit = (marketPrice - pnlCostPerKg) * actualWeight;
  const deposit = item.deposit_amount ?? DEFAULT_DEPOSIT_AMOUNT;
  const ownFund = estimateWeight
    ? ((item.future_price_usd_per_t || 0) / 1000) *
      estimateWeight *
      (item.prepayment_fx_usd_cny || param.default_fx_usd_cny) *
      param.client_own_fund_ratio
    : 0;
  const paidPrepayment = item.actual_prepayment_rmb ?? ownFund;
  const deliveryPayable = effectiveDeliveryTotalCost - paidPrepayment;
  const netCashAfter = receivable - deliveryPayable - deposit;
  const dailyCost = calcDailyCost(item, param, today, status, actualWeight, estimateWeight);

  // 客户端逻辑：只有在存在明确锁价/配置价/配置映射行情时，才按当前盯市结果重算；
  // 否则回退使用服务器下发的派生字段，避免把未配置 SKU 错算成已盯市。
  const useClientValuation = marketQuote != null;

  // 利润与交割后现金：无配置时优先使用服务器返回的值；有配置时使用当前重算值。
  const serverProfit = item.estimated_profit_rmb;
  const calculatedProfit = profit;
  const resolvedProfit = useClientValuation ? calculatedProfit : serverProfit ?? calculatedProfit;

  const resolvedNetCashAfter = useClientValuation
    ? netCashAfter
    : item.estimated_net_cash_after_delivery ?? netCashAfter;
  const resolvedNetCashBefore = useClientValuation
    ? deposit
    : item.estimated_net_cash_before_delivery ?? deposit;
  const resolvedDailyCost = useClientValuation ? dailyCost : item.daily_cost_rmb ?? dailyCost;

  return {
    ...item,
    physical_status_resolved: status,
    storage_days: item.storage_entry_date ? daysBetweenDates(item.storage_entry_date, today) : 0,
    current_cost_rmb_per_kg_resolved: resolvedCost,
    current_total_cost_rmb: resolvedCurrentTotalCost,
    delivery_cost_rmb_per_kg: deliveryCost,
    delivery_total_cost_rmb: deliveryTotalCost,
    market_price_rmb_per_kg: marketPrice,
    estimated_receivable_rmb: receivable,
    estimated_net_cash_before_delivery_resolved: resolvedNetCashBefore,
    estimated_net_cash_after_delivery_resolved: resolvedNetCashAfter,
    estimated_profit_rmb_resolved: resolvedProfit,
    daily_cost_rmb_resolved: resolvedDailyCost,
  };
}

function calcOccupiedCash(item: EnrichedInventoryItem, param: ParamSet): number {
  const weight = itemWeight(item);
  const fx = item.prepayment_fx_usd_cny || param.default_fx_usd_cny;
  const goods = ((item.future_price_usd_per_t || 0) / 1000) * weight * fx;
  return item.actual_prepayment_rmb ?? goods * param.factory_prepayment_ratio;
}

function matchesQuery(item: EnrichedInventoryItem, query: string): boolean {
  const q = query.trim().toLowerCase();
  if (!q) return true;

  return [
    item.product_name,
    item.factory_code,
    item.sku_code,
    item.container_id,
    item.country,
    item.cold_storage,
  ].some(value => String(value ?? '').toLowerCase().includes(q));
}

function summarizeItems(items: EnrichedInventoryItem[], context?: PivotContext) {
  let totalWeight = 0;
  let totalPieces = 0;
  let pendingWeight = 0;
  let transitWeight = 0;
  let inStockWeight = 0;
  let occupiedCash = 0;
  let floatingPnL = 0;
  let recoverableCash = 0;
  let netCashBefore = 0;
  let dailyBurn = 0;
  let costWeight = 0;

  for (const item of items) {
    const weight = itemWeight(item);
    const pieces = item.pieces ?? 0;
    const bucket = statusBucket(item.physical_status_resolved);
    const cost = item.current_cost_rmb_per_kg_resolved;
    const param = paramFor(item, context);

    totalWeight += weight;
    totalPieces += pieces;
    costWeight += cost * weight;
    occupiedCash += calcOccupiedCash(item, param);
    floatingPnL += item.estimated_profit_rmb_resolved;
    recoverableCash += item.estimated_net_cash_after_delivery_resolved;
    netCashBefore += item.estimated_net_cash_before_delivery_resolved;
    dailyBurn += item.daily_cost_rmb_resolved;

    if (bucket === 'pending') pendingWeight += weight;
    if (bucket === 'transit') transitWeight += weight;
    if (bucket === 'stock') inStockWeight += weight;
  }

  return {
    totalWeight,
    totalPieces,
    pendingWeight,
    transitWeight,
    inStockWeight,
    occupiedCash,
    floatingPnL,
    recoverableCash,
    netCashBefore,
    dailyBurn,
    weightedAvgCost: totalWeight > 0 ? costWeight / totalWeight : 0,
  };
}

export function computePivot(
  items: InventoryItem[],
  ageMin?: number | null,
  ageMax?: number | null,
  searchQuery?: string,
  context?: PivotContext,
): {products: PivotProduct[]; summary: PivotSummary} {
  const today = context?.valuationDate ?? new Date();
  const enrichedItems = items.map(item => enrichItem(item, {...context, valuationDate: today}));

  const filtered = enrichedItems.filter(item => {
    if (item.is_deleted === 1) return false;
    if (!isValidPivotItem(item)) return false;
    if (!matchesQuery(item, searchQuery ?? '')) return false;

    const age = inventoryAgeDays(item, today);
    if (ageMin != null && (age == null || age < ageMin)) return false;
    if (ageMax != null && (age == null || age > ageMax)) return false;
    return true;
  });

  const productMap = new Map<string, EnrichedInventoryItem[]>();
  for (const item of filtered) {
    const key = item.product_name || '未知品名';
    if (!productMap.has(key)) productMap.set(key, []);
    productMap.get(key)!.push(item);
  }

  const products: PivotProduct[] = [];

  for (const [product_name, group] of productMap.entries()) {
    const productSummary = summarizeItems(group, context);
    const sourceContainers = [
      ...new Set(group.map(item => item.container_id).filter(Boolean)),
    ];

    const factoryMap = new Map<string, EnrichedInventoryItem[]>();
    for (const item of group) {
      const key = item.factory_code || '未知厂号';
      if (!factoryMap.has(key)) factoryMap.set(key, []);
      factoryMap.get(key)!.push(item);
    }

    const factories: FactoryDetail[] = [];
    for (const [factory_code, fGroup] of factoryMap.entries()) {
      const s = summarizeItems(fGroup, context);
      const ages = fGroup
        .map(item => inventoryAgeDays(item, today))
        .filter((age): age is number => age != null);

      factories.push({
        factory_code,
        country: fGroup[0]?.country ?? null,
        weight_kg: s.totalWeight,
        pieces: s.totalPieces,
        containers: new Set(fGroup.map(item => item.container_id)).size,
        pendingWeight: s.pendingWeight,
        transitWeight: s.transitWeight,
        inStockWeight: s.inStockWeight,
        occupiedCash: s.occupiedCash,
        floatingPnL: s.floatingPnL,
        recoverableCash: s.recoverableCash,
        avgCost: s.weightedAvgCost,
        avgStorageAgeDays:
          ages.length > 0 ? ages.reduce((sum, age) => sum + age, 0) / ages.length : null,
        cold_storage: fGroup[0]?.cold_storage ?? null,
      });
    }

    factories.sort((a, b) => b.weight_kg - a.weight_kg);

    products.push({
      product_name,
      totalWeight: productSummary.totalWeight,
      totalPieces: productSummary.totalPieces,
      containers: sourceContainers.length,
      weightedAvgCost: productSummary.weightedAvgCost,
      pendingWeight: productSummary.pendingWeight,
      transitWeight: productSummary.transitWeight,
      inStockWeight: productSummary.inStockWeight,
      occupiedCash: productSummary.occupiedCash,
      floatingPnL: productSummary.floatingPnL,
      recoverableCash: productSummary.recoverableCash,
      netCashBefore: productSummary.netCashBefore,
      dailyBurn: productSummary.dailyBurn,
      sourceContainers,
      factories,
    });
  }

  products.sort((a, b) => b.totalWeight - a.totalWeight);

  const summary: PivotSummary = {
    totalProducts: products.length,
    totalWeight: products.reduce((sum, product) => sum + product.totalWeight, 0),
    totalPieces: products.reduce((sum, product) => sum + product.totalPieces, 0),
    totalItems: filtered.length,
    totalContainers: new Set(filtered.map(item => item.container_id).filter(Boolean)).size,
    totalOccupiedCash: products.reduce((sum, product) => sum + product.occupiedCash, 0),
    totalFloatingPnL: products.reduce((sum, product) => sum + product.floatingPnL, 0),
    totalRecoverableCash: products.reduce((sum, product) => sum + product.recoverableCash, 0),
    totalNetCashBefore: products.reduce((sum, product) => sum + product.netCashBefore, 0),
    totalDailyBurn: products.reduce((sum, product) => sum + product.dailyBurn, 0),
    watchedProducts: filtered.filter(item => hasExactPriceConfig(item, context)).length,
    estimatedProfit: products.reduce((sum, product) => sum + product.floatingPnL, 0),
  };

  return {products, summary};
}

export function resolveInventoryRows(
  items: InventoryItem[],
  context?: PivotContext,
  searchQuery?: string,
): ResolvedInventoryRow[] {
  const today = context?.valuationDate ?? new Date();
  const enrichedItems = items.map(item => enrichItem(item, {...context, valuationDate: today}));

  return enrichedItems
    .filter(item => {
      if (item.is_deleted === 1) return false;
      if (!isValidPivotItem(item)) return false;
      if (!matchesQuery(item, searchQuery ?? '')) return false;
      return true;
    })
    .map(item => {
      const param = paramFor(item, context);
      return {
        id: item.inventory_sku_id,
        contractId: item.supplier_contract_id ?? '--',
        productName: item.product_name,
        containerId: item.container_id,
        skuCode: item.sku_code ?? '--',
        factoryCode: item.factory_code ?? '--',
        country: item.country ?? '--',
        coldStorage: item.cold_storage ?? '--',
        funder: item.funder_id ?? '--',
        customGroup: item.custom_group ?? '--',
        pieces: item.pieces ?? 0,
        weightKg: itemWeight(item),
        costPerKg: item.current_cost_rmb_per_kg_resolved ?? 0,
        sellingPricePerKg: item.market_price_rmb_per_kg ?? 0,
        dailyCost: item.daily_cost_rmb_resolved ?? 0,
        recoverableCash: item.estimated_net_cash_after_delivery_resolved ?? 0,
        profit: item.estimated_profit_rmb_resolved ?? 0,
        productionDate: item.production_date ?? '--',
        status: item.physical_status_resolved,
        occupiedCash: calcOccupiedCash(item, param),
      };
    });
}

export function formatWeight(kg: number, showKg = false): string {
  if (showKg) return `${kg.toFixed(3)}kg`;
  return `${(kg / 1000).toFixed(3)}t`;
}

export function formatPrice(val: number | null): string {
  if (val == null || !isFinite(val)) return '—';
  return `¥${val.toFixed(2)}`;
}

export function formatMoneyWan(val: number | null, signed = false): string {
  if (val == null || !isFinite(val)) return '—';
  const amount = Math.abs(val / 10000).toFixed(1);
  if (val < 0) return `-¥${amount}万`;
  return `${signed ? '+' : ''}¥${amount}万`;
}

export function formatSignedWan(val: number | null): string {
  if (val == null || !isFinite(val)) return '—';
  const sign = val >= 0 ? '+' : '';
  return `${sign}${(val / 10000).toFixed(1)}万`;
}
