import type {HomeCardItem} from '../types/api';

const exampleTrendPoints = [
  {date: '05-02', avgPrice: 60.5},
  {date: '05-03', avgPrice: 61.2},
  {date: '05-04', avgPrice: 60.8},
  {date: '05-05', avgPrice: 59.5},
  {date: '05-06', avgPrice: 60.1},
  {date: '05-07', avgPrice: 61.5},
  {date: '05-08', avgPrice: 62.3},
];

export function getHomeExampleCards(): HomeCardItem[] {
  return [
    {
      isExample: true,
      cardType: 'product',
      productName: '牛前八件套',
      todayOfferCount: 12400,
      merchantCount: 32,
      factoryCount: 24,
    },
    {
      isExample: true,
      cardType: 'country',
      country: '巴西',
      hotProducts: [
        {rank: 1, productName: '前腱', offerCount: 1200},
        {rank: 2, productName: '牛前八件套', offerCount: 921},
        {rank: 3, productName: '胸肉', offerCount: 642},
      ],
      hotFactories: [
        {factoryNo: 'SIF1440', offerCount: 328},
        {factoryNo: 'SIF504', offerCount: 215},
        {factoryNo: 'SIF4554', offerCount: 189},
      ],
    },
    {
      isExample: true,
      cardType: 'brand',
      brandName: 'JBS S.A.',
      todayOfferCount: 124,
      productCount: 32,
      factoryCount: 24,
    },
    {
      isExample: true,
      cardType: 'factory',
      country: '巴西',
      factoryNo: 'SIF504',
      hotProducts: [
        {rank: 1, productName: '前腱', offerCount: 1200},
        {rank: 2, productName: '牛前八件套', offerCount: 921},
        {rank: 3, productName: '胸肉', offerCount: 642},
      ],
      todayOfferCount: 32,
    },
    {
      isExample: true,
      cardType: 'countryProduct',
      country: '巴西',
      productName: '牛前八件套',
      topFactories: [
        {factoryNo: 'SIF4333', priceMin: 60.5, priceMax: 60.5},
        {factoryNo: 'SIF504', priceMin: 60.5, priceMax: 62.4},
        {factoryNo: 'SIF2583', priceMin: 60.5, priceMax: 60.5},
      ],
      factoryCount: 32,
      todayOfferCount: 24,
    },
    {
      isExample: true,
      cardType: 'factoryProduct',
      country: '巴西',
      factoryNo: 'SIF1440',
      productName: '牛前八件套',
      priceMin: 58.2,
      priceMax: 63,
      priceChange: 0.5,
      priceChangeRate: 2.5,
      trendPoints: exampleTrendPoints,
      hotMerchants: [
        {merchantName: '上海牛一品', priceMin: 60.5, priceMax: 60.5},
        {merchantName: '郑州帮你剩', priceMin: 60.5, priceMax: 62.4},
        {merchantName: '天津大洋时代', priceMin: 60.5, priceMax: 60.5},
      ],
      todayOfferCount: 32,
      inquiryCount: 60,
    },
    {
      isExample: true,
      cardType: 'brandProduct',
      brandName: 'JBS S.A.',
      productName: '牛前八件套',
      priceMin: 58.2,
      priceMax: 63,
      priceChange: 0.5,
      priceChangeRate: 2.5,
      trendPoints: exampleTrendPoints,
      hotFactories: [
        {factoryNo: 'SIF4333', priceMin: 60.5, priceMax: 60.5},
        {factoryNo: 'SIF504', priceMin: 60.5, priceMax: 62.4},
        {factoryNo: 'SIF2583', priceMin: 60.5, priceMax: 60.5},
      ],
      factoryCount: 32,
      todayOfferCount: 24,
    },
  ];
}
