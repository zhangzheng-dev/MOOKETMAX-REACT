import AsyncStorage from '../platform/storage';
import {TOKEN_KEY} from './inventoryClient';

const BASE_URL = 'https://gateway.mujidigital.com';

const DATA_TYPES = {
  INVENTORY: 1,
  PRICE: 2,
  CONFIG: 3,
  PARAM: 4,
  DELIVERY: 5,
} as const;

const CORE_DATA_TYPES = [
  DATA_TYPES.INVENTORY,
  DATA_TYPES.PRICE,
  DATA_TYPES.CONFIG,
  DATA_TYPES.PARAM,
  DATA_TYPES.DELIVERY,
];

interface StoredToken {
  access_token: string;
  userId: number | string;
}

async function getStoredToken(): Promise<StoredToken | null> {
  try {
    const raw = await AsyncStorage.getItem(TOKEN_KEY);
    if (!raw) return null;
    return JSON.parse(raw);
  } catch {
    return null;
  }
}

// 云端库存字段（大写驼峰，与服务器返回一致）
export interface InventoryItemRaw {
  Inventory_SKU_ID: string;
  Supplier_Contract_ID?: string;
  Container_ID: string;
  SKU_Code?: string;
  Product_Name: string;
  Weight_KG: number;
  Pieces?: number;
  Parameter_Set_ID?: number;
  Funder_ID?: string;
  Future_Price_USD_Per_T?: number;
  Spot_Price_RMB_Per_KG?: number;
  Est_Selling_Price_RMB_Per_KG?: number;
  Prepayment_FX_USD_CNY?: number;
  Transit_Payment_FX_USD_CNY?: number;
  Tax_Payment_FX_USD_CNY?: number;
  Estimated_Weight_KG?: number;
  Actual_Weight_KG?: number;
  Prepayment_Date?: string;
  Production_Date?: string;
  Shipping_Date?: string;
  ETA_Date?: string;
  Actual_Arrival_Date?: string;
  Storage_Entry_Date?: string;
  Transit_Payment_Date?: string;
  Tax_Payment_Date?: string;
  Transit_Payment_Fee?: number;
  Actual_Prepayment_RMB?: number;
  Actual_Transit_Payment_RMB?: number;
  Actual_Tax_Payment_RMB?: number;
  Deposit_Amount?: number;
  Redemption_Status?: string;
  Custom_Group?: string;
  Physical_Status?: string;
  Current_Cost_RMB_Per_KG?: number;
  Estimated_Profit_RMB?: number;
  Estimated_Net_Cash_Before_Delivery?: number;
  Estimated_Net_Cash_After_Delivery?: number;
  Daily_Cost_RMB?: number;
  Factory_Code?: string;
  Country?: string;
  Cold_Storage?: string;
  Port_Misc_Fee?: number;
  Is_Deleted?: number;
  [key: string]: unknown;
}

export interface ParamSet {
  parameter_set_id: number;
  parameter_set_name: string;
  factory_prepayment_ratio: number;
  client_own_fund_ratio: number;
  funder_advance_ratio: number;
  prepayment_interest_rate: number;
  transit_payment_interest_rate: number;
  tax_payment_interest_rate: number;
  storage_cost_per_ton_day: number;
  tariff_rate: number;
  vat_rate: number;
  prepayment_fee_per_container: number;
  misc_cost_per_container: number;
  agent_fee_per_container: number;
  procurement_cost_per_ton: number;
  warehouse_cost_per_ton: number;
  insurance_fee_rate: number;
  default_fx_usd_cny: number;
  prepayment_tiered_rates?: TieredRate[];
  transit_payment_tiered_rates?: TieredRate[];
  tax_payment_tiered_rates?: TieredRate[];
}

export interface TieredRate {
  startDay: number;
  endDay?: number | null;
  rate: number;
}

export interface MarketPrice {
  date: string;
  product: string;
  country: string;
  factoryCode: string;
  currentPrice: number;
  latestPrice: number;
  latestDate: string;
  source: string;
}

export interface ContainerPriceConfig {
  product: string;
  factoryCode: string;
  country: string;
  estSellingPriceRmbPerKg?: number | null;
  lockedPrice?: number | null;
  price?: number | null;
  latestPrice?: number | null;
  currentPrice?: number | null;
  date?: string | null;
}

export type ContainerPriceConfigMap = Record<string, ContainerPriceConfig>;
export type DeliveryDateMap = Record<string, string>;

export interface InventoryDataset {
  items: InventoryItem[];
  paramSets: ParamSet[];
  marketPrices: MarketPrice[];
  priceConfig: ContainerPriceConfigMap;
  deliveryDates: DeliveryDateMap;
}

export interface InventoryItem {
  inventory_sku_id: string;
  supplier_contract_id: string | null;
  container_id: string;
  sku_code: string | null;
  product_name: string;
  weight_kg: number;
  pieces: number;
  parameter_set_id: number | null;
  funder_id: string | null;
  future_price_usd_per_t: number | null;
  spot_price_rmb_per_kg: number | null;
  est_selling_price_rmb_per_kg: number | null;
  prepayment_fx_usd_cny: number | null;
  transit_payment_fx_usd_cny: number | null;
  tax_payment_fx_usd_cny: number | null;
  estimated_weight_kg: number | null;
  actual_weight_kg: number | null;
  prepayment_date: string | null;
  production_date: string | null;
  shipping_date: string | null;
  eta_date: string | null;
  actual_arrival_date: string | null;
  storage_entry_date: string | null;
  transit_payment_date: string | null;
  tax_payment_date: string | null;
  transit_payment_fee: number | null;
  actual_prepayment_rmb: number | null;
  actual_transit_payment_rmb: number | null;
  actual_tax_payment_rmb: number | null;
  deposit_amount: number | null;
  redemption_status: string | null;
  custom_group: string | null;
  physical_status: string | null;
  current_cost_rmb_per_kg: number | null;
  estimated_profit_rmb: number | null;
  estimated_net_cash_before_delivery: number | null;
  estimated_net_cash_after_delivery: number | null;
  daily_cost_rmb: number | null;
  factory_code: string | null;
  country: string | null;
  cold_storage: string | null;
  port_misc_fee: number | null;
  is_deleted: number;
}

interface SpotMarketSummaryRaw {
  id: string | number;
  marketDate?: string;
  priceActual?: number;
  priceActualDesc?: string;
}

interface SpotMarketDetailRaw {
  id: string;
  skuCode?: string;
  category?: number;
  categoryDesc?: string;
  productName?: string;
  country?: number;
  countryDesc?: string;
  plantNo?: string;
  standard?: string;
  leanRatio?: number;
  leanRatioDesc?: string;
  flowCoefficient?: number;
  flowCoefficientDesc?: string;
  marketDate?: string;
  marketPrice?: number;
  priceType?: number;
  priceTypeDesc?: string;
  priceActual?: number;
  priceActualDesc?: string;
  skuGrade?: string;
  skuGradeDesc?: string;
}

function pick(obj: Record<string, unknown>, ...keys: string[]): unknown {
  for (const key of keys) {
    if (obj[key] != null) return obj[key];
  }
  return null;
}

function toStr(val: unknown): string | null {
  if (val == null) return null;
  const s = String(val).trim();
  return s === '' ? null : s;
}

function normalizeDateString(val: unknown): string {
  const raw = toStr(val);
  return raw ? raw.replace(/\//g, '-') : '';
}

function normalizeParamSet(raw: Record<string, unknown>): ParamSet | null {
  const id = toNum(pick(raw, 'Parameter_Set_ID', 'parameter_set_id', 'id'));
  if (id == null) return null;

  return {
    parameter_set_id: id,
    parameter_set_name: toStr(pick(raw, 'Parameter_Set_Name', 'parameter_set_name')) ?? `参数 ${id}`,
    factory_prepayment_ratio:
      toNum(pick(raw, 'Factory_Prepayment_Ratio', 'factory_prepayment_ratio')) ?? 0.4,
    client_own_fund_ratio:
      toNum(pick(raw, 'Client_Own_Fund_Ratio', 'client_own_fund_ratio')) ?? 0.2,
    funder_advance_ratio:
      toNum(pick(raw, 'Funder_Advance_Ratio', 'funder_advance_ratio')) ?? 0.2,
    prepayment_interest_rate:
      toNum(pick(raw, 'Prepayment_Interest_Rate', 'prepayment_interest_rate')) ?? 0.065,
    transit_payment_interest_rate:
      toNum(pick(raw, 'Transit_Payment_Interest_Rate', 'transit_payment_interest_rate')) ?? 0.065,
    tax_payment_interest_rate:
      toNum(pick(raw, 'Tax_Payment_Interest_Rate', 'tax_payment_interest_rate')) ?? 0.065,
    storage_cost_per_ton_day:
      toNum(pick(raw, 'Storage_Cost_Per_Ton_Day', 'storage_cost_per_ton_day')) ?? 2.2,
    tariff_rate: toNum(pick(raw, 'Tariff_Rate', 'tariff_rate')) ?? 0.12,
    vat_rate: toNum(pick(raw, 'VAT_Rate', 'vat_rate')) ?? 0.09,
    prepayment_fee_per_container:
      toNum(pick(raw, 'Prepayment_Fee_Per_Container', 'prepayment_fee_per_container')) ?? 500,
    misc_cost_per_container:
      toNum(pick(raw, 'Misc_Cost_Per_Container', 'misc_cost_per_container')) ?? 500,
    agent_fee_per_container:
      toNum(pick(raw, 'Agent_Fee_Per_Container', 'agent_fee_per_container')) ?? 5500,
    procurement_cost_per_ton:
      toNum(pick(raw, 'Procurement_Cost_Per_Ton', 'procurement_cost_per_ton')) ?? 150,
    warehouse_cost_per_ton:
      toNum(pick(raw, 'Warehouse_Cost_Per_Ton', 'warehouse_cost_per_ton')) ?? 60,
    insurance_fee_rate:
      toNum(pick(raw, 'Insurance_Fee_Rate', 'insurance_fee_rate')) ?? 0.0008,
    default_fx_usd_cny:
      toNum(pick(raw, 'Default_FX_USD_CNY', 'default_fx_usd_cny')) ?? 7,
    prepayment_tiered_rates: normalizeTieredRates(
      pick(raw, 'Prepayment_Tiered_Rates', 'prepayment_tiered_rates'),
    ),
    transit_payment_tiered_rates: normalizeTieredRates(
      pick(raw, 'Transit_Payment_Tiered_Rates', 'transit_payment_tiered_rates'),
    ),
    tax_payment_tiered_rates: normalizeTieredRates(
      pick(raw, 'Tax_Payment_Tiered_Rates', 'tax_payment_tiered_rates'),
    ),
  };
}

function normalizeTieredRates(value: unknown): TieredRate[] | undefined {
  const raw = typeof value === 'string' ? tryParse(value) : value;
  if (!Array.isArray(raw)) return undefined;

  return raw
    .map((item): TieredRate | null => {
      if (!item || typeof item !== 'object') return null;
      const record = item as Record<string, unknown>;
      const startDay = toNum(pick(record, 'startDay', 'start_day'));
      const rate = toNum(pick(record, 'rate', 'interestRate'));
      if (startDay == null || rate == null) return null;
      return {
        startDay,
        endDay: toNum(pick(record, 'endDay', 'end_day')),
        rate,
      };
    })
    .filter((item): item is TieredRate => item != null);
}

function toNum(val: unknown): number | null {
  if (val == null || val === '') return null;
  const n = Number(val);
  return isNaN(n) ? null : n;
}

function normalize(raw: InventoryItemRaw): InventoryItem {
  return {
    inventory_sku_id: String(raw.Inventory_SKU_ID ?? ''),
    supplier_contract_id: toStr(raw.Supplier_Contract_ID),
    container_id: String(raw.Container_ID ?? ''),
    sku_code: toStr(raw.SKU_Code),
    product_name: String(raw.Product_Name ?? ''),
    weight_kg: toNum(raw.Weight_KG) ?? 0,
    pieces: toNum(raw.Pieces) ?? 0,
    parameter_set_id: toNum(raw.Parameter_Set_ID),
    funder_id: toStr(raw.Funder_ID),
    future_price_usd_per_t: toNum(raw.Future_Price_USD_Per_T),
    spot_price_rmb_per_kg: toNum(raw.Spot_Price_RMB_Per_KG),
    est_selling_price_rmb_per_kg: toNum(raw.Est_Selling_Price_RMB_Per_KG),
    prepayment_fx_usd_cny: toNum(raw.Prepayment_FX_USD_CNY),
    transit_payment_fx_usd_cny: toNum(raw.Transit_Payment_FX_USD_CNY),
    tax_payment_fx_usd_cny: toNum(raw.Tax_Payment_FX_USD_CNY),
    estimated_weight_kg: toNum(raw.Estimated_Weight_KG),
    actual_weight_kg: toNum(raw.Actual_Weight_KG),
    prepayment_date: toStr(raw.Prepayment_Date),
    production_date: toStr(raw.Production_Date),
    shipping_date: toStr(raw.Shipping_Date),
    eta_date: toStr(raw.ETA_Date),
    actual_arrival_date: toStr(raw.Actual_Arrival_Date),
    storage_entry_date: toStr(raw.Storage_Entry_Date),
    transit_payment_date: toStr(raw.Transit_Payment_Date),
    tax_payment_date: toStr(raw.Tax_Payment_Date),
    transit_payment_fee: toNum(raw.Transit_Payment_Fee),
    actual_prepayment_rmb: toNum(raw.Actual_Prepayment_RMB),
    actual_transit_payment_rmb: toNum(raw.Actual_Transit_Payment_RMB),
    actual_tax_payment_rmb: toNum(raw.Actual_Tax_Payment_RMB),
    deposit_amount: toNum(raw.Deposit_Amount),
    redemption_status: toStr(raw.Redemption_Status),
    custom_group: toStr(raw.Custom_Group),
    physical_status: toStr(raw.Physical_Status),
    current_cost_rmb_per_kg: toNum(raw.Current_Cost_RMB_Per_KG),
    estimated_profit_rmb: toNum(raw.Estimated_Profit_RMB),
    estimated_net_cash_before_delivery: toNum(raw.Estimated_Net_Cash_Before_Delivery),
    estimated_net_cash_after_delivery: toNum(raw.Estimated_Net_Cash_After_Delivery),
    daily_cost_rmb: toNum(raw.Daily_Cost_RMB),
    factory_code: toStr(raw.Factory_Code),
    country: toStr(raw.Country),
    cold_storage: toStr(raw.Cold_Storage),
    port_misc_fee: toNum(raw.Port_Misc_Fee),
    is_deleted: toNum(raw.Is_Deleted) ?? 0,
  };
}

// Step 1: 查询当前用户可下载的数据类型
async function fetchDownloadableTypes(
  token: string,
  userId: number | string,
): Promise<number[]> {
  if (!userId || userId === 0 || userId === '0') return [DATA_TYPES.INVENTORY]; // fallback

  const res = await fetch(
    `${BASE_URL}/uac/quantification/queryQuantificationUserAction/${userId}`,
    {method: 'GET', headers: {Authorization: `Bearer ${token}`}},
  );
  const data = await res.json();

  if (data.code === 200 && Array.isArray(data.result)) {
    const permitted = (data.result as Array<{dataType: number; canDownload: boolean}>)
      .filter(p => p.canDownload)
      .map(p => p.dataType);
    return permitted.length > 0 ? permitted : [DATA_TYPES.INVENTORY];
  }
  // 权限接口失败时直接尝试拉取 inventory
  return [DATA_TYPES.INVENTORY];
}

// Step 2: 拉取数据快照
// 返回 result: [ {dataType, dataSnapshots(JSON字符串), versionTag}, ... ]
interface SnapshotItem {
  dataType: number;
  dataSnapshots: string;
  versionTag?: number;
  updateTime?: string;
}

async function pullSnapshots(
  token: string,
  dataTypes: number[],
): Promise<SnapshotItem[]> {
  const res = await fetch(
    `${BASE_URL}/info/server/quantification/infoQuantificationGroupSnapshots/sync/pull`,
    {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${token}`,
      },
      body: JSON.stringify({dataTypes}),
    },
  );

  if (!res.ok) throw new Error(`服务器错误 ${res.status}`);

  const data = await res.json();
  if (data.code !== 200) throw new Error(data.message || '拉取失败');

  // result 是数组
  if (!Array.isArray(data.result)) return [];
  return data.result as SnapshotItem[];
}

export async function fetchInventoryDataset(): Promise<InventoryDataset> {
  const tokenData = await getStoredToken();
  if (!tokenData) throw new Error('未登录，请先扫码登录');

  const {access_token, userId} = tokenData;

  // 1. 查询可下载类型
  const downloadable = await fetchDownloadableTypes(access_token, userId);
  if (!downloadable.includes(DATA_TYPES.INVENTORY)) {
    throw new Error('当前账号没有库存数据查看权限');
  }

  // 2. 拉取快照 - 强制拉取所有核心数据类型以确保计算准确性
  const requestedTypes = CORE_DATA_TYPES;
  const snapshots = await pullSnapshots(access_token, requestedTypes);

  // 3. 找到 inventory_data 快照并解析
  const inventorySnapshot = snapshots.find(
    s => s.dataType === DATA_TYPES.INVENTORY,
  );

  if (!inventorySnapshot?.dataSnapshots) {
    return emptyDataset(); // 云端暂无该用户的库存数据
  }

  let items: InventoryItemRaw[];
  try {
    const parsed = JSON.parse(inventorySnapshot.dataSnapshots);
    items = Array.isArray(parsed) ? parsed : [];
  } catch {
    throw new Error('库存数据格式解析失败');
  }

  const paramSets = parseArraySnapshot<Record<string, unknown>>(
    snapshots,
    DATA_TYPES.PARAM,
  )
    .map(normalizeParamSet)
    .filter((item): item is ParamSet => item != null);

  const snapshotMarketPrices = parseArraySnapshot<Record<string, unknown>>(
    snapshots,
    DATA_TYPES.PRICE,
  ).map(normalizeMarketPrice);
  const latestOnlineMarketPrices = await fetchLatestSpotMarketPrices(access_token);

  return {
    items: items
      .filter(item => Number(item.Is_Deleted) !== 1)
      .map(normalize),
    paramSets,
    marketPrices: mergeMarketPrices(snapshotMarketPrices, latestOnlineMarketPrices),
    priceConfig: normalizePriceConfig(parseSnapshot(snapshots, DATA_TYPES.CONFIG)),
    deliveryDates: normalizeDeliveryDates(parseSnapshot(snapshots, DATA_TYPES.DELIVERY)),
  };
}

export async function fetchInventoryData(): Promise<InventoryItem[]> {
  return (await fetchInventoryDataset()).items;
}

function emptyDataset(): InventoryDataset {
  return {
    items: [],
    paramSets: [],
    marketPrices: [],
    priceConfig: {},
    deliveryDates: {},
  };
}

function parseSnapshot(snapshots: SnapshotItem[], dataType: number): unknown {
  const snapshot = snapshots.find(s => s.dataType === dataType);
  if (!snapshot?.dataSnapshots) return null;
  return tryParse(snapshot.dataSnapshots);
}

function parseArraySnapshot<T>(snapshots: SnapshotItem[], dataType: number): T[] {
  const parsed = parseSnapshot(snapshots, dataType);
  return Array.isArray(parsed) ? (parsed as T[]) : [];
}

function tryParse(value: string): unknown {
  try {
    return JSON.parse(value);
  } catch {
    return null;
  }
}

function normalizeMarketPrice(raw: Record<string, unknown>): MarketPrice {
  const currentPrice =
    toNum(pick(raw, 'currentPrice', 'current_price', 'latestPrice', 'market_price')) ?? 0;
  const date = normalizeDateString(pick(raw, 'date', 'latestDate', 'market_date'));
  return {
    date,
    product: toStr(pick(raw, 'product', 'product_name', 'Product_Name')) ?? '',
    country: toStr(pick(raw, 'country', 'country_desc', 'Country')) ?? '',
    factoryCode: toStr(pick(raw, 'factoryCode', 'factory_code', 'plant_no', 'Factory_Code')) ?? '',
    currentPrice,
    latestPrice: toNum(pick(raw, 'latestPrice', 'currentPrice', 'current_price')) ?? currentPrice,
    latestDate: normalizeDateString(pick(raw, 'latestDate', 'date', 'market_date')) || date,
    source: toStr(pick(raw, 'source')) ?? '',
  };
}

function normalizeSpotMarketDetail(raw: SpotMarketDetailRaw): MarketPrice {
  const date = normalizeDateString(raw.marketDate);
  const latestPrice = toNum(raw.marketPrice) ?? 0;
  return {
    date,
    product: toStr(raw.productName) ?? '',
    country: toStr(raw.countryDesc) ?? '',
    factoryCode: toStr(raw.plantNo) ?? '',
    currentPrice: latestPrice,
    latestPrice,
    latestDate: date,
    source: 'online',
  };
}

function marketPriceKey(price: MarketPrice): string {
  return [
    price.product.trim(),
    price.country.trim(),
    price.factoryCode.trim(),
    (price.latestDate || price.date).trim(),
  ].join('__');
}

function shouldPreferMarketPrice(next: MarketPrice, current: MarketPrice): boolean {
  const nextDate = next.latestDate || next.date;
  const currentDate = current.latestDate || current.date;
  if (nextDate !== currentDate) {
    return nextDate > currentDate;
  }

  const nextSourceRank = next.source === 'online' ? 2 : 1;
  const currentSourceRank = current.source === 'online' ? 2 : 1;
  if (nextSourceRank !== currentSourceRank) {
    return nextSourceRank > currentSourceRank;
  }

  return next.latestPrice >= current.latestPrice;
}

function mergeMarketPrices(snapshotPrices: MarketPrice[], onlinePrices: MarketPrice[]): MarketPrice[] {
  const merged = new Map<string, MarketPrice>();

  [...snapshotPrices, ...onlinePrices].forEach(price => {
    const key = marketPriceKey(price);
    const current = merged.get(key);
    if (!current || shouldPreferMarketPrice(price, current)) {
      merged.set(key, price);
    }
  });

  return [...merged.values()];
}

async function fetchLatestSpotMarketPrices(token: string): Promise<MarketPrice[]> {
  try {
    const latestMarkets = await fetchSpotMarketList(token, 1, 1);
    const latestMarket = latestMarkets[0];
    if (!latestMarket?.id) return [];

    const rows: SpotMarketDetailRaw[] = [];
    let page = 1;

    while (true) {
      const current = await fetchSpotMarketDetailsByMarketId(token, String(latestMarket.id), page, 500);
      if (current.length === 0) break;
      rows.push(...current);
      if (current.length < 500) break;
      page += 1;
    }

    return rows.map(normalizeSpotMarketDetail);
  } catch (error) {
    console.warn('[inventory] 加载在线盯市价格失败，回退到云端快照', error);
    return [];
  }
}

async function fetchSpotMarketList(
  token: string,
  pageNum = 1,
  pageSize = 20,
): Promise<SpotMarketSummaryRaw[]> {
  const res = await fetch(`${BASE_URL}/info/serverMtm/queryServerSpotMarketListWithPage`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${token}`,
    },
    body: JSON.stringify({pageNum, pageSize}),
  });

  if (!res.ok) throw new Error(`在线盯市行情日期拉取失败 ${res.status}`);

  const data = await res.json();
  if (data.code !== 200 || !Array.isArray(data.result?.list)) return [];
  return data.result.list as SpotMarketSummaryRaw[];
}

async function fetchSpotMarketDetailsByMarketId(
  token: string,
  marketId: string,
  pageNum = 1,
  pageSize = 500,
): Promise<SpotMarketDetailRaw[]> {
  const res = await fetch(
    `${BASE_URL}/info/serverMtm/queryServerSpotMarketDetailListByMarketIdWithPage`,
    {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${token}`,
      },
      body: JSON.stringify({marketId, pageNum, pageSize}),
    },
  );

  if (!res.ok) throw new Error(`在线盯市行情明细拉取失败 ${res.status}`);

  const data = await res.json();
  if (data.code !== 200 || !Array.isArray(data.result?.list)) return [];
  return data.result.list as SpotMarketDetailRaw[];
}

function normalizePriceConfig(value: unknown): ContainerPriceConfigMap {
  if (!value || typeof value !== 'object' || Array.isArray(value)) return {};
  const result: ContainerPriceConfigMap = {};

  Object.entries(value as Record<string, unknown>).forEach(([key, raw]) => {
    if (!raw || typeof raw !== 'object') return;
    const record = raw as Record<string, unknown>;
    result[key] = {
      product: toStr(pick(record, 'product', 'Product_Name', 'productName')) ?? '',
      factoryCode: toStr(pick(record, 'factoryCode', 'Factory_Code', 'factory_code')) ?? '',
      country: toStr(pick(record, 'country', 'Country')) ?? '',
      estSellingPriceRmbPerKg: toNum(
        pick(
          record,
          'Est_Selling_Price_RMB_Per_KG',
          'estSellingPriceRmbPerKg',
          'est_selling_price_rmb_per_kg',
        ),
      ),
      lockedPrice: toNum(pick(record, 'lockedPrice', 'locked_price')),
      price: toNum(pick(record, 'price')),
      latestPrice: toNum(pick(record, 'latestPrice', 'latest_price')),
      currentPrice: toNum(pick(record, 'currentPrice', 'current_price')),
      date: toStr(pick(record, 'date', 'latestDate')),
    };
  });

  return result;
}

function normalizeDeliveryDates(value: unknown): DeliveryDateMap {
  if (!value || typeof value !== 'object' || Array.isArray(value)) return {};
  const result: DeliveryDateMap = {};
  Object.entries(value as Record<string, unknown>).forEach(([key, val]) => {
    const date = toStr(val);
    if (date) result[key] = date;
  });
  return result;
}

export interface AppVersionInfo {
  version: string;
  downloadUrl?: string;
  releaseNotes?: string;
}

const UPDATE_SERVER = 'http://sh.malleeglobal.com:8188/electron/ai/prod';

export async function checkAppUpdate(currentVersion: string): Promise<{
  hasUpdate: boolean;
  info?: AppVersionInfo;
}> {
  try {
    const res = await fetch(`${UPDATE_SERVER}/latest-mobile.json`);
    if (!res.ok) return {hasUpdate: false};
    const info: AppVersionInfo = await res.json();
    return {
      hasUpdate: info.version !== currentVersion,
      info: info.version !== currentVersion ? info : undefined,
    };
  } catch {
    return {hasUpdate: false};
  }
}
