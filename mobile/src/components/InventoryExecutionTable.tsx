import React from 'react';
import {
  Pressable,
  ScrollView,
  StyleSheet,
  Text,
  View,
} from 'react-native';
import Svg, {Path} from 'react-native-svg';

interface Palette {
  bg: string;
  panel: string;
  panelSoft: string;
  text: string;
  label: string;
  muted: string;
  border: string;
  accent: string;
  positive: string;
  negative: string;
}

interface InventoryRow {
  id: string;
  date: string;
  quantity: number;
  floatingPnL: number;
  term: string;
}

interface Props {
  rows: InventoryRow[];
  palette: Palette;
  onClosePosition?: (row: InventoryRow) => void;
}

export default function InventoryExecutionTable({rows, palette, onClosePosition}: Props) {
  if (rows.length === 0) {
    return (
      <View style={styles.emptyWrap}>
        <Text style={[styles.emptyText, {color: palette.muted}]}>暂无执行数据</Text>
      </View>
    );
  }

  return (
    <View style={styles.container}>
      {/* Table Header */}
      <View style={[styles.tableHeader, {borderBottomColor: palette.border}]}>
        <Text style={[styles.headerCell, styles.dateCell, {color: palette.label}]}>日期</Text>
        <Text style={[styles.headerCell, styles.quantityCell, {color: palette.label}]}>单量</Text>
        <Text style={[styles.headerCell, styles.pnlCell, {color: palette.label}]}>浮动</Text>
        <Text style={[styles.headerCell, styles.termCell, {color: palette.label}]}>期限</Text>
        <Text style={[styles.headerCell, styles.actionCell, {color: palette.label}]}>操作</Text>
      </View>

      {/* Table Rows */}
      <ScrollView style={styles.tableBody}>
        {rows.map(row => (
          <View
            key={row.id}
            style={[styles.tableRow, {borderBottomColor: palette.border}]}>
            <Text style={[styles.dataCell, styles.dateCell, {color: palette.text}]}>
              {row.date}
            </Text>
            <Text style={[styles.dataCell, styles.quantityCell, {color: palette.text}]}>
              {row.quantity.toLocaleString()}
            </Text>
            <Text
              style={[
                styles.dataCell,
                styles.pnlCell,
                {color: row.floatingPnL >= 0 ? palette.positive : palette.negative},
              ]}>
              {row.floatingPnL >= 0 ? '+' : ''}
              {row.floatingPnL.toFixed(2)}
            </Text>
            <Text style={[styles.dataCell, styles.termCell, {color: palette.text}]}>
              {row.term}
            </Text>
            <View style={styles.actionCell}>
              <Pressable
                style={styles.closeBtn}
                onPress={() => onClosePosition?.(row)}>
                <Text style={styles.closeBtnText}>平仓</Text>
              </Pressable>
            </View>
          </View>
        ))}
      </ScrollView>
    </View>
  );
}

function BackButton({onPress}: {onPress: () => void}) {
  return (
    <Pressable onPress={onPress} style={styles.backButton}>
      <Svg width={24} height={24} viewBox="0 0 24 24" fill="none">
        <Path
          d="M15 18L9 12L15 6"
          stroke="#171D1C"
          strokeWidth={1.5}
          strokeLinecap="round"
          strokeLinejoin="round"
        />
      </Svg>
    </Pressable>
  );
}

function Header({title, onBack}: {title: string; onBack?: () => void}) {
  return (
    <View style={styles.header}>
      {onBack && <BackButton onPress={onBack} />}
      <Text style={styles.headerTitle}>{title}</Text>
    </View>
  );
}

InventoryExecutionTable.Header = Header;

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  header: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 16,
    paddingVertical: 12,
  },
  backButton: {
    padding: 4,
    marginRight: 8,
  },
  headerTitle: {
    fontSize: 17,
    fontWeight: '600',
    color: '#171D1C',
  },
  tableHeader: {
    flexDirection: 'row',
    paddingHorizontal: 12,
    paddingVertical: 10,
    borderBottomWidth: 1,
  },
  headerCell: {
    fontSize: 12,
    fontWeight: '600',
    textAlign: 'center',
  },
  tableBody: {
    flex: 1,
  },
  tableRow: {
    flexDirection: 'row',
    paddingHorizontal: 12,
    paddingVertical: 14,
    borderBottomWidth: 1,
    alignItems: 'center',
  },
  dataCell: {
    fontSize: 13,
    fontWeight: '500',
    textAlign: 'center',
  },
  dateCell: {
    flex: 1,
    textAlign: 'left',
  },
  quantityCell: {
    width: 70,
  },
  pnlCell: {
    width: 80,
    fontWeight: '700',
    fontVariant: ['tabular-nums'],
  },
  termCell: {
    width: 60,
  },
  actionCell: {
    width: 50,
    alignItems: 'flex-end',
  },
  closeBtn: {
    paddingHorizontal: 10,
    paddingVertical: 5,
    borderRadius: 4,
    backgroundColor: '#E55757',
  },
  closeBtnText: {
    fontSize: 12,
    fontWeight: '600',
    color: '#FFFFFF',
  },
  emptyWrap: {
    paddingVertical: 24,
    alignItems: 'center',
  },
  emptyText: {
    fontSize: 14,
  },
});
