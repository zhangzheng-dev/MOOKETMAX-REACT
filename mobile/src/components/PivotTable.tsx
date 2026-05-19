/* eslint-disable @typescript-eslint/no-unused-vars */
import React from 'react';
import {
  Keyboard,
  NativeScrollEvent,
  NativeSyntheticEvent,
  Platform,
  Pressable,
  ScrollView,
  StyleSheet,
  Text,
  TextStyle,
  View,
} from 'react-native';
import Svg, {Path, Rect} from 'react-native-svg';
import {
  FactoryDetail,
  PivotProduct,
  formatMoneyWan,
  formatPrice,
  formatSignedWan,
  formatWeight,
} from '../utils/pivot';

interface Palette {
  panel: string;
  panelSoft: string;
  panelSoftAlt: string;
  text: string;
  label: string;
  muted: string;
  border: string;
  divider: string;
  accent: string;
  positive: string;
  negative: string;
  selectedBg: string;
  selectedBorder: string;
}

interface HeaderProps {
  showKg: boolean;
  palette: Palette;
  scrollRef?: React.RefObject<ScrollView | null>;
}

interface RowsProps extends HeaderProps {
  products: PivotProduct[];
  onHorizontalScroll?: (x: number) => void;
  initialScrollKey?: string | number;
}

type TableTone = 'positive' | 'negative' | 'neutral' | 'accent';
type PivotRow =
  | {id: string; kind: 'product'; product: PivotProduct}
  | {id: string; kind: 'factory'; product: PivotProduct; factory: FactoryDetail};

interface PivotColumn {
  key: string;
  label: string;
  minWidth: number;
  value: (row: PivotRow, showKg: boolean) => string;
  tone?: (row: PivotRow) => TableTone;
}

const NAME_COLUMN_MIN_WIDTH = 128;
const COLUMN_GAP = 12;
const OUTER_ROW_GAP = 6;
const PRODUCT_ROW_HEIGHT = 45;
const FACTORY_ROW_HEIGHT = 45;

const numericFontFamily = Platform.select({
  ios: 'Manrope-Bold',
  android: 'Manrope-Bold',
  default: undefined,
});

const columns: PivotColumn[] = [
  {
    key: 'cost',
    label: '成本(¥/KG)',
    minWidth: 41,
    value: row => formatPrice(rowCost(row)),
  },
  {
    key: 'pending',
    label: '未发货',
    minWidth: 47,
    value: (row, showKg) => formatWeight(rowWeight(row, 'pending'), showKg).toUpperCase(),
  },
  {
    key: 'transit',
    label: '在途',
    minWidth: 47,
    value: (row, showKg) => formatWeight(rowWeight(row, 'transit'), showKg).toUpperCase(),
  },
  {
    key: 'stock',
    label: '在库',
    minWidth: 85,
    value: (row, showKg) => formatWeight(rowWeight(row, 'stock'), showKg).toUpperCase(),
    tone: row => (rowWeight(row, 'stock') > 0 ? 'accent' : 'neutral'),
  },
  {
    key: 'total',
    label: '总重量',
    minWidth: 85,
    value: (row, showKg) =>
      formatWeight(row.kind === 'product' ? row.product.totalWeight : row.factory.weight_kg, showKg).toUpperCase(),
  },
  {
    key: 'pieces',
    label: '件数',
    minWidth: 37,
    value: row =>
      (row.kind === 'product' ? row.product.totalPieces : row.factory.pieces).toLocaleString(),
  },
  {
    key: 'occupied',
    label: '占用资金',
    minWidth: 48,
    value: row => formatMoneyWan(rowMoney(row, 'occupiedCash')),
  },
  {
    key: 'pnl',
    label: '浮盈亏',
    minWidth: 68,
    value: row => formatSignedWan(rowMoney(row, 'floatingPnL')),
    tone: row => moneyTone(rowMoney(row, 'floatingPnL')),
  },
  {
    key: 'cash',
    label: '可用现金',
    minWidth: 68,
    value: row => formatMoneyWan(rowMoney(row, 'recoverableCash'), true),
    tone: row => moneyTone(rowMoney(row, 'recoverableCash')),
  },
];

function estimateTextWidth(text: string, fontSize: number) {
  let units = 0;
  for (const char of text) {
    if (/[\u3400-\u9FFF\uF900-\uFAFF]/.test(char)) {
      units += 1;
    } else if (/[A-Z]/.test(char)) {
      units += 0.72;
    } else if (/[a-z]/.test(char)) {
      units += 0.6;
    } else if (/[0-9]/.test(char)) {
      units += 0.62;
    } else if (char === ' ') {
      units += 0.35;
    } else {
      units += 0.46;
    }
  }
  return Math.ceil(units * fontSize);
}

function buildPivotRows(products: PivotProduct[]): PivotRow[] {
  const rows: PivotRow[] = [];
  products.forEach(product => {
    rows.push({id: product.product_name, kind: 'product', product});
    product.factories.forEach(factory => {
      rows.push({
        id: `${product.product_name}-${factory.factory_code}`,
        kind: 'factory',
        product,
        factory,
      });
    });
  });
  return rows;
}

function buildPivotLayout(products: PivotProduct[], showKg: boolean) {
  const allRows = buildPivotRows(products);

  const nameWidth = Math.max(
    NAME_COLUMN_MIN_WIDTH,
    estimateTextWidth('品名', 10) + 24,
    ...allRows.flatMap(row => {
      if (row.kind === 'product') {
        return [estimateTextWidth(row.product.product_name || '--', 12) + 40];
      }

      return [
        estimateTextWidth(row.factory.factory_code || '--', 12) + 40,
        estimateTextWidth(
          [row.factory.country, row.factory.cold_storage].filter(Boolean).join('/') || '--',
          10,
        ) + 40,
      ];
    }),
  );

  const resolvedColumns = columns.map(column => {
    const headerLabel = column.key === 'total' ? `${column.label}(${showKg ? 'KG' : 'T'})` : column.label;
    const headerWidth = estimateTextWidth(headerLabel, 10) + 4;
    const contentWidth = allRows.reduce((max, row) => {
      const value = column.value(row, showKg);
      const extra =
        column.key === 'pnl' || column.key === 'cash'
          ? 14
          : 0;
      return Math.max(max, estimateTextWidth(value, 13) + extra);
    }, 0);

    return {
      ...column,
      width: Math.max(column.minWidth, headerWidth, contentWidth),
    };
  });

  const dataContentWidth =
    24 +
    resolvedColumns.reduce((sum, column) => sum + column.width, 0) +
    COLUMN_GAP * (resolvedColumns.length - 1);

  return {
    nameWidth,
    columns: resolvedColumns,
    dataContentWidth,
  };
}

function Header({showKg, palette, scrollRef, products = []}: HeaderProps & {products?: PivotProduct[]}) {
  const layout = React.useMemo(() => buildPivotLayout(products, showKg), [products, showKg]);
  return (
    <View style={styles.headerWrap}>
      <View style={[styles.nameHeaderWrap, {width: layout.nameWidth}]}>
        <Text style={[styles.headerText, styles.nameHeaderText, {color: palette.label}]}>品名</Text>
      </View>

      <ScrollView
        ref={scrollRef}
        horizontal
        scrollEnabled={false}
        showsHorizontalScrollIndicator={false}
        bounces={false}>
        <View style={[styles.headerRow, {width: layout.dataContentWidth}]}>
          {layout.columns.map(column => (
            <View key={column.key} style={[styles.headerCell, {width: column.width}]}>
              <Text style={[styles.headerText, {color: palette.label}]} numberOfLines={1}>
                {column.key === 'total' ? `${column.label}(${showKg ? 'KG' : 'T'})` : column.label}
              </Text>
            </View>
          ))}
        </View>
      </ScrollView>
    </View>
  );
}

function splitUnitValue(value: string) {
  const match = value.match(/^(.*?)(万|KG|T)$/);
  if (!match) return {main: value};
  return {main: match[1], unit: match[2]};
}

function Rows({products, showKg, palette, onHorizontalScroll, initialScrollKey}: RowsProps) {
  const layout = React.useMemo(() => buildPivotLayout(products, showKg), [products, showKg]);
  const [expandedProducts, setExpandedProducts] = React.useState<Set<string>>(() => new Set());
  const dataScrollRef = React.useRef<ScrollView | null>(null);

  React.useEffect(() => {
    dataScrollRef.current?.scrollTo({x: 0, animated: false});
    onHorizontalScroll?.(0);
  }, [initialScrollKey, onHorizontalScroll]);

  React.useEffect(() => {
    if (!products.length) {
      setExpandedProducts(new Set());
    }
  }, [products]);

  const rows = React.useMemo(() => {
    const list: PivotRow[] = [];
    products.forEach(product => {
      list.push({id: product.product_name, kind: 'product', product});
      if (expandedProducts.has(product.product_name)) {
        product.factories.forEach(factory => {
          list.push({
            id: `${product.product_name}-${factory.factory_code}`,
            kind: 'factory',
            product,
            factory,
          });
        });
      }
    });
    return list;
  }, [expandedProducts, products]);

  if (!rows.length) {
    return (
      <View style={styles.emptyWrap}>
        <Text style={[styles.emptyText, {color: palette.muted}]}>暂无库存数据</Text>
      </View>
    );
  }

  function toggleRow(row: PivotRow) {
    if (row.kind !== 'product') return;
    setExpandedProducts(prev => {
      const next = new Set(prev);
      if (next.has(row.product.product_name)) {
        next.delete(row.product.product_name);
      } else {
        next.add(row.product.product_name);
      }
      return next;
      });
  }

  return (
    <View style={styles.rowsWrap}>
      <View style={[styles.nameColumnStack, {width: layout.nameWidth}]}>
        {rows.map(row => {
          const expanded =
            row.kind === 'product' && expandedProducts.has(row.product.product_name);
          const isFactory = row.kind === 'factory';
          const baseBg = isFactory ? palette.selectedBg : palette.panelSoft;
          const borderColor = isFactory ? 'transparent' : palette.border;
          const borderWidth = isFactory ? 0 : 1;
          const rowHeight = row.kind === 'product' ? PRODUCT_ROW_HEIGHT : FACTORY_ROW_HEIGHT;

          return (
            <View
              key={`${row.id}-left`}
              style={[
                styles.rowShellLeft,
                {
                  width: layout.nameWidth,
                  height: rowHeight,
                  backgroundColor: baseBg,
                  borderColor,
                  borderWidth,
                  marginBottom: OUTER_ROW_GAP,
                },
              ]}>
              {row.kind === 'product' ? (
                <Pressable
                  onPress={() => toggleRow(row)}
                  style={({pressed}) => [
                    styles.nameCellUnified,
                    {width: layout.nameWidth},
                    pressed && styles.nameCellPressed,
                  ]}>
                    <ToggleSquare expanded={expanded} color={expanded ? palette.accent : palette.text} />
                  <View style={styles.nameTextWrap}>
                    <Text style={[styles.nameText, {color: palette.text}]} numberOfLines={1}>
                      {row.product.product_name}
                    </Text>
                  </View>
                </Pressable>
              ) : (
                <View style={[styles.nameCellUnified, {width: layout.nameWidth}]}>
                  <FactoryTreeMarker color={palette.accent} />
                  <View style={styles.nameTextWrap}>
                    <Text style={[styles.nameText, {color: palette.text}]} numberOfLines={1}>
                      {row.factory.factory_code}
                    </Text>
                    <Text style={[styles.subNameText, {color: palette.muted}]} numberOfLines={1}>
                      {[row.factory.country, row.factory.cold_storage].filter(Boolean).join('/')}
                    </Text>
                  </View>
                </View>
              )}
            </View>
          );
        })}
      </View>

      <ScrollView
        ref={dataScrollRef}
        style={styles.dataScroll}
        horizontal
        directionalLockEnabled
        nestedScrollEnabled={Platform.OS !== 'android'}
        showsHorizontalScrollIndicator={false}
        bounces={false}
        overScrollMode="never"
        scrollEventThrottle={16}
        keyboardDismissMode="on-drag"
        onScrollBeginDrag={Platform.OS === 'android' ? Keyboard.dismiss : undefined}
        onScroll={(event: NativeSyntheticEvent<NativeScrollEvent>) =>
          onHorizontalScroll?.(event.nativeEvent.contentOffset.x)
        }>
        <View style={[styles.dataColumnStack, {width: layout.dataContentWidth}]}>
          {rows.map(row => {
            const expanded =
              row.kind === 'product' && expandedProducts.has(row.product.product_name);
            const isFactory = row.kind === 'factory';
            const baseBg = isFactory ? palette.selectedBg : palette.panelSoft;
            const borderColor = isFactory ? 'transparent' : palette.border;
            const borderWidth = isFactory ? 0 : 1;
            const rowHeight = row.kind === 'product' ? PRODUCT_ROW_HEIGHT : FACTORY_ROW_HEIGHT;

            return (
              <View
                key={`${row.id}-right`}
                style={[
                  styles.rowShellRight,
                  {
                    height: rowHeight,
                    backgroundColor: baseBg,
                    borderColor,
                    borderWidth,
                    marginBottom: OUTER_ROW_GAP,
                  },
                ]}>
                <View style={styles.dataRowUnified}>
                  {layout.columns.map(column => {
                    const tone = column.tone?.(row) ?? 'neutral';
                    const value = column.value(row, showKg);
                    return (
                      <View key={column.key} style={[styles.dataCell, {width: column.width}]}>
                        <CellValue
                          columnKey={column.key}
                          value={value}
                          tone={tone}
                          palette={palette}
                        />
                      </View>
                    );
                  })}
                </View>
              </View>
            );
          })}
        </View>
      </ScrollView>
    </View>
  );
}

function PivotTable() {
  return null;
}

PivotTable.Header = Header;
PivotTable.Rows = Rows;

export default PivotTable;

function ToggleSquare({expanded, color}: {expanded: boolean; color: string}) {
  return (
    <Svg
      width={12}
      height={12}
      viewBox="0 0 12 12"
      fill="none"
      style={{transform: [{rotate: expanded ? '180deg' : '90deg'}]}}>
      <Path
        d="M7.5 11.375H4.5C1.785 11.375 0.625 10.215 0.625 7.5V4.5C0.625 1.785 1.785 0.625 4.5 0.625H7.5C10.215 0.625 11.375 1.785 11.375 4.5V7.5C11.375 10.215 10.215 11.375 7.5 11.375ZM4.5 1.375C2.195 1.375 1.375 2.195 1.375 4.5V7.5C1.375 9.805 2.195 10.625 4.5 10.625H7.5C9.805 10.625 10.625 9.805 10.625 7.5V4.5C10.625 2.195 9.805 1.375 7.5 1.375H4.5Z"
        fill={color}
      />
      <Path
        d="M7.765 7.105C7.67 7.105 7.575 7.07 7.5 6.995L6 5.495L4.5 6.995C4.355 7.14 4.115 7.14 3.97 6.995C3.825 6.85 3.825 6.61 3.97 6.465L5.735 4.7C5.88 4.555 6.12 4.555 6.265 4.7L8.03 6.465C8.175 6.61 8.175 6.85 8.03 6.995C7.955 7.07 7.86 7.105 7.765 7.105Z"
        fill={color}
      />
    </Svg>
  );
}

function FactoryTreeMarker({color}: {color: string}) {
  return (
    <Svg width={12} height={12} viewBox="0 0 12 12" fill="none">
      <Path
        d="M6 3V7C6 7.55228 6.44772 8 7 8H11"
        stroke={color}
        strokeWidth="0.8"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
    </Svg>
  );
}

function SignedValueIcon({tone}: {tone: 'positive' | 'negative'}) {
  const color = tone === 'positive' ? '#E24B30' : '#229E6C';

  return (
    <Svg width={11} height={12} viewBox="0 0 11 12" fill="none">
      <Rect width={11} height={12} rx={1} fill={color} fillOpacity={0.15} />
      <Path d="M3.3 6H7.7" stroke={color} />
      {tone === 'positive' ? <Path d="M5.5 3.8V8.2" stroke={color} /> : null}
    </Svg>
  );
}

function CellValue({
  columnKey,
  value,
  tone,
  palette,
}: {
  columnKey: string;
  value: string;
  tone: TableTone;
  palette: Palette;
}) {
  const color = toneColor(palette, tone);

  if (columnKey === 'pnl' || columnKey === 'cash') {
    const split = splitUnitValue(value.replace(/^[+-]/, ''));
    const signedTone = value.startsWith('+')
      ? 'positive'
      : value.startsWith('-')
        ? 'negative'
        : null;

    return (
      <View style={styles.signedCellValue}>
        {signedTone ? <SignedValueIcon tone={signedTone} /> : null}
        <Text style={[styles.moneyText, {color}]}>
          {split.main}
          {split.unit ? <Text style={styles.unitText}>{split.unit}</Text> : null}
        </Text>
      </View>
    );
  }

  if (columnKey === 'cost' || columnKey === 'occupied') {
    const split = splitUnitValue(value);
    return (
      <Text style={[styles.moneyText, {color}]}>
        {split.main}
        {split.unit ? <Text style={styles.unitText}>{split.unit}</Text> : null}
      </Text>
    );
  }

  if (columnKey === 'pending' || columnKey === 'transit' || columnKey === 'stock' || columnKey === 'total') {
    const split = splitUnitValue(value);
    return (
      <Text style={[styles.numberText, {color}]}>
        {split.main}
        {split.unit ? <Text style={styles.weightUnitText}>{split.unit}</Text> : null}
      </Text>
    );
  }

  return <Text style={[styles.numberText, {color}]}>{value}</Text>;
}

function rowCost(row: PivotRow) {
  return row.kind === 'product' ? row.product.weightedAvgCost : row.factory.avgCost;
}

function rowWeight(row: PivotRow, key: 'pending' | 'transit' | 'stock') {
  if (row.kind === 'product') {
    if (key === 'pending') return row.product.pendingWeight;
    if (key === 'transit') return row.product.transitWeight;
    return row.product.inStockWeight;
  }
  if (key === 'pending') return row.factory.pendingWeight;
  if (key === 'transit') return row.factory.transitWeight;
  return row.factory.inStockWeight;
}

function rowMoney(row: PivotRow, key: 'occupiedCash' | 'floatingPnL' | 'recoverableCash') {
  return row.kind === 'product' ? row.product[key] : row.factory[key];
}

function moneyTone(value: number): TableTone {
  if (value > 0) return 'positive';
  if (value < 0) return 'negative';
  return 'neutral';
}

function toneColor(palette: Palette, tone: TableTone) {
  if (tone === 'positive') return palette.positive;
  if (tone === 'negative') return palette.negative;
  if (tone === 'accent') return palette.accent;
  return palette.text;
}

const styles = StyleSheet.create({
  headerWrap: {
    flexDirection: 'row',
    alignItems: 'flex-end',
  },
  nameHeaderWrap: {
    width: NAME_COLUMN_MIN_WIDTH,
    paddingLeft: 12,
    paddingBottom: 6,
    justifyContent: 'flex-end',
  },
  nameHeaderText: {
    textAlign: 'left',
  },
  headerRow: {
    height: 20,
    paddingLeft: 12,
    paddingRight: 12,
    paddingBottom: 6,
    flexDirection: 'row',
    alignItems: 'flex-end',
    gap: COLUMN_GAP,
  },
  headerText: {
    fontSize: 10,
    lineHeight: 14,
    textAlign: 'right',
  },
  headerCell: {
    height: '100%',
    alignItems: 'flex-end',
    justifyContent: 'flex-end',
  },
  rowsWrap: {
    paddingBottom: 0,
    flexDirection: 'row',
  },
  nameColumnStack: {
    width: NAME_COLUMN_MIN_WIDTH,
  },
  dataColumnStack: {
    paddingBottom: 0,
  },
  dataScroll: {
    flex: 1,
    minWidth: 0,
  },
  rowShellLeft: {
    width: NAME_COLUMN_MIN_WIDTH,
    borderTopLeftRadius: 4,
    borderBottomLeftRadius: 4,
    borderLeftWidth: 1,
    borderTopWidth: 1,
    borderBottomWidth: 1,
    overflow: 'hidden',
  },
  rowShellRight: {
    marginLeft: -1,
    borderTopRightRadius: 4,
    borderBottomRightRadius: 4,
    borderRightWidth: 1,
    borderTopWidth: 1,
    borderBottomWidth: 1,
    justifyContent: 'center',
    overflow: 'hidden',
  },
  nameCellUnified: {
    width: NAME_COLUMN_MIN_WIDTH,
    height: '100%',
    paddingLeft: 12,
    paddingRight: 8,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'flex-start',
    gap: 4,
  },
  nameCellPressed: {
    opacity: 0.84,
  },
  nameTextWrap: {
    flex: 1,
    minWidth: 0,
  },
  nameText: {
    fontSize: 12,
    lineHeight: 17,
    fontWeight: '500',
  },
  subNameText: {
    marginTop: 1,
    fontSize: 10,
    lineHeight: 14,
  },
  dataRowUnified: {
    height: '100%',
    paddingLeft: 12,
    paddingRight: 12,
    flexDirection: 'row',
    alignItems: 'center',
    gap: COLUMN_GAP,
  },
  dataCell: {
    height: '100%',
    alignItems: 'flex-end',
    justifyContent: 'center',
  },
  emptyWrap: {
    paddingVertical: 24,
    alignItems: 'center',
  },
  emptyText: {
    fontSize: 12,
    lineHeight: 17,
  },
  treeMarker: {
    width: 12,
    height: 12,
  },
  treeVertical: {
    position: 'absolute',
    left: 1,
    top: 0,
    width: 1,
    height: 8,
  },
  treeHorizontal: {
    position: 'absolute',
    left: 1,
    top: 7,
    width: 7,
    height: 1,
  },
  moneyText: {
    fontSize: 13,
    lineHeight: 17,
    textAlign: 'right',
    fontStyle: 'normal',
    fontFamily: numericFontFamily,
    fontVariant: ['tabular-nums'],
  } as TextStyle,
  numberText: {
    fontSize: 13,
    lineHeight: 17,
    textAlign: 'right',
    fontStyle: 'normal',
    fontFamily: numericFontFamily,
    fontVariant: ['tabular-nums'],
  } as TextStyle,
  unitText: {
    fontSize: 8,
    lineHeight: 12,
    fontWeight: '400',
    color: '#171D1C',
    fontFamily: undefined,
  } as TextStyle,
  weightUnitText: {
    fontSize: 8,
    lineHeight: 12,
    fontWeight: '400',
    color: '#171D1C',
    fontFamily: undefined,
  } as TextStyle,
  signedCellValue: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'flex-end',
    gap: 2,
  },
});
