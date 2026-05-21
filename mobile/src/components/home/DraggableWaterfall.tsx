import React, {useCallback, useEffect, useMemo, useRef, useState} from 'react';
import {Dimensions, StyleSheet, View} from 'react-native';
import {Gesture, GestureDetector} from 'react-native-gesture-handler';
import Animated, {
  runOnJS,
  useAnimatedStyle,
  useSharedValue,
  withSpring,
} from 'react-native-reanimated';

const COLUMN_GAP = 12;
const PADDING_H = 16;
const screenWidth = Dimensions.get('window').width;
const COL_WIDTH = (screenWidth - PADDING_H * 2 - COLUMN_GAP) / 2;

const SPRING_CONFIG = {damping: 20, stiffness: 200, mass: 0.8};

type Props<T> = {
  data: T[];
  keyExtractor: (item: T, index: number) => string;
  renderItem: (item: T, index: number) => React.ReactNode;
  estimateHeight: (item: T) => number;
  onDragEnd: (data: T[]) => void;
  editMode: boolean;
};

function computePositions(heights: number[]): {x: number; y: number}[] {
  let leftH = 0;
  let rightH = 0;
  return heights.map(h => {
    if (leftH <= rightH) {
      const pos = {x: PADDING_H, y: leftH};
      leftH += h + COLUMN_GAP;
      return pos;
    } else {
      const pos = {x: PADDING_H + COL_WIDTH + COLUMN_GAP, y: rightH};
      rightH += h + COLUMN_GAP;
      return pos;
    }
  });
}

function getContainerHeight(heights: number[]): number {
  let leftH = 0;
  let rightH = 0;
  heights.forEach(h => {
    if (leftH <= rightH) leftH += h + COLUMN_GAP;
    else rightH += h + COLUMN_GAP;
  });
  return Math.max(leftH, rightH);
}

export function DraggableWaterfall<T>({
  data,
  keyExtractor,
  renderItem,
  estimateHeight,
  onDragEnd,
  editMode,
}: Props<T>) {
  const [order, setOrder] = useState<number[]>(() => data.map((_, i) => i));
  const [measuredHeights, setMeasuredHeights] = useState<Map<number, number>>(new Map());
  const [draggingOrigIdx, setDraggingOrigIdx] = useState<number | null>(null);
  const orderRef = useRef(order);
  orderRef.current = order;

  useEffect(() => {
    setOrder(data.map((_, i) => i));
    setMeasuredHeights(new Map());
  }, [data.length]);

  // Heights in current order
  const heights = useMemo(
    () => order.map(idx => measuredHeights.get(idx) ?? estimateHeight(data[idx])),
    [order, measuredHeights, data, estimateHeight],
  );

  // Positions recalculated after every swap (proper waterfall, no overlaps)
  const positions = useMemo(() => computePositions(heights), [heights]);
  const containerHeight = useMemo(() => getContainerHeight(heights), [heights]);

  // Map: originalIdx -> position
  const positionMap = useMemo(() => {
    const map = new Map<number, {x: number; y: number}>();
    order.forEach((origIdx, slotIdx) => {
      map.set(origIdx, positions[slotIdx]);
    });
    return map;
  }, [order, positions]);

  const handleMeasure = useCallback((originalIndex: number, height: number) => {
    setMeasuredHeights(prev => {
      const rounded = Math.round(height);
      if (prev.get(originalIndex) === rounded) return prev;
      const next = new Map(prev);
      next.set(originalIndex, rounded);
      return next;
    });
  }, []);

  // Swap two slots
  const handleReorder = useCallback((fromSlot: number, toSlot: number) => {
    setOrder(prev => {
      const next = [...prev];
      [next[fromSlot], next[toSlot]] = [next[toSlot], next[fromSlot]];
      return next;
    });
  }, []);

  const handleDragDone = useCallback(() => {
    setDraggingOrigIdx(null);
    const reorderedData = orderRef.current.map(idx => data[idx]);
    onDragEnd(reorderedData);
  }, [data, onDragEnd]);

  const handleDragStart = useCallback((origIdx: number) => {
    setDraggingOrigIdx(origIdx);
  }, []);

  if (!editMode) {
    const leftColumn: {item: T; index: number}[] = [];
    const rightColumn: {item: T; index: number}[] = [];
    let leftH = 0;
    let rightH = 0;
    data.forEach((item, index) => {
      const h = measuredHeights.get(index) ?? estimateHeight(item);
      if (leftH <= rightH) {
        leftColumn.push({item, index});
        leftH += h;
      } else {
        rightColumn.push({item, index});
        rightH += h;
      }
    });

    return (
      <View style={styles.gridRow}>
        <View style={styles.gridCol}>
          {leftColumn.map(({item, index}) => (
            <View key={keyExtractor(item, index)} onLayout={e => handleMeasure(index, e.nativeEvent.layout.height)}>
              {renderItem(item, index)}
            </View>
          ))}
        </View>
        <View style={styles.gridCol}>
          {rightColumn.map(({item, index}) => (
            <View key={keyExtractor(item, index)} onLayout={e => handleMeasure(index, e.nativeEvent.layout.height)}>
              {renderItem(item, index)}
            </View>
          ))}
        </View>
      </View>
    );
  }

  return (
    <View style={[styles.dragContainer, {height: containerHeight}]}>
      {order.map((originalIdx, slotIdx) => (
        <DraggableCard
          key={`drag-${originalIdx}`}
          originalIdx={originalIdx}
          slotIdx={slotIdx}
          targetPos={positionMap.get(originalIdx) ?? {x: 0, y: 0}}
          allPositions={positions}
          heights={heights}
          isDragging={draggingOrigIdx === originalIdx}
          onDragStart={handleDragStart}
          onReorder={handleReorder}
          onDragDone={handleDragDone}
          onMeasure={handleMeasure}>
          {renderItem(data[originalIdx], originalIdx)}
        </DraggableCard>
      ))}
    </View>
  );
}

function DraggableCard({
  originalIdx,
  slotIdx,
  targetPos,
  allPositions,
  heights,
  isDragging,
  onDragStart,
  onReorder,
  onDragDone,
  onMeasure,
  children,
}: {
  originalIdx: number;
  slotIdx: number;
  targetPos: {x: number; y: number};
  allPositions: {x: number; y: number}[];
  heights: number[];
  isDragging: boolean;
  onDragStart: (origIdx: number) => void;
  onReorder: (fromSlot: number, toSlot: number) => void;
  onDragDone: () => void;
  onMeasure: (originalIdx: number, height: number) => void;
  children: React.ReactNode;
}) {
  const active = useSharedValue(false);
  const posX = useSharedValue(targetPos.x);
  const posY = useSharedValue(targetPos.y);
  const offsetX = useSharedValue(0);
  const offsetY = useSharedValue(0);
  const scaleVal = useSharedValue(1);

  const slotRef = useRef(slotIdx);
  slotRef.current = slotIdx;
  const dragStartPos = useRef({x: targetPos.x, y: targetPos.y});

  // When not dragging, animate to new target position (spring)
  // When dragging, don't update posX/posY (finger controls position)
  useEffect(() => {
    if (!isDragging) {
      posX.value = withSpring(targetPos.x, SPRING_CONFIG);
      posY.value = withSpring(targetPos.y, SPRING_CONFIG);
    } else {
      // While dragging, update the base position immediately (no animation)
      // so that when we drop, offset → 0 lands us at the right place
      posX.value = targetPos.x;
      posY.value = targetPos.y;
    }
  }, [targetPos.x, targetPos.y, isDragging]);

  const findTarget = useCallback((absX: number, absY: number): number => {
    const cx = absX + COL_WIDTH / 2;
    const cy = absY + (heights[slotRef.current] ?? 150) / 2;
    let closest = slotRef.current;
    let minDist = Infinity;
    allPositions.forEach((pos, i) => {
      const ph = heights[i] ?? 150;
      const dist = Math.abs(cx - (pos.x + COL_WIDTH / 2)) + Math.abs(cy - (pos.y + ph / 2));
      if (dist < minDist) {
        minDist = dist;
        closest = i;
      }
    });
    return closest;
  }, [allPositions, heights]);

  const handleMove = useCallback((tx: number, ty: number) => {
    const absX = dragStartPos.current.x + tx;
    const absY = dragStartPos.current.y + ty;
    const target = findTarget(absX, absY);
    if (target !== slotRef.current) {
      onReorder(slotRef.current, target);
    }
  }, [findTarget, onReorder]);

  const gesture = Gesture.Pan()
    .activateAfterLongPress(250)
    .onStart(() => {
      active.value = true;
      scaleVal.value = withSpring(1.05, SPRING_CONFIG);
      offsetX.value = 0;
      offsetY.value = 0;
      runOnJS(onDragStart)(originalIdx);
    })
    .onUpdate((e) => {
      offsetX.value = e.translationX;
      offsetY.value = e.translationY;
      runOnJS(handleMove)(e.translationX, e.translationY);
    })
    .onEnd(() => {
      active.value = false;
      scaleVal.value = withSpring(1, SPRING_CONFIG);
      // Snap: offset goes to 0 instantly, posX/posY already at target
      offsetX.value = 0;
      offsetY.value = 0;
      runOnJS(onDragDone)();
    });

  // Remember start position when drag begins
  useEffect(() => {
    if (isDragging) {
      dragStartPos.current = {x: targetPos.x, y: targetPos.y};
    }
  }, [isDragging]);

  const animatedStyle = useAnimatedStyle(() => ({
    left: posX.value + offsetX.value,
    top: posY.value + offsetY.value,
    transform: [{scale: scaleVal.value}],
    zIndex: active.value ? 999 : 0,
    elevation: active.value ? 20 : 0,
  }));

  return (
    <GestureDetector gesture={gesture}>
      <Animated.View
        style={[styles.dragCard, {width: COL_WIDTH}, animatedStyle]}
        onLayout={e => onMeasure(originalIdx, e.nativeEvent.layout.height)}>
        {children}
      </Animated.View>
    </GestureDetector>
  );
}

const styles = StyleSheet.create({
  gridRow: {flexDirection: 'row', paddingHorizontal: PADDING_H, paddingTop: 12, gap: COLUMN_GAP},
  gridCol: {flex: 1, gap: COLUMN_GAP},
  dragContainer: {
    position: 'relative',
    marginHorizontal: 0,
    marginTop: 12,
  },
  dragCard: {
    position: 'absolute',
  },
});
