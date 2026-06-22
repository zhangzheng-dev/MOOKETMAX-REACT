import React, {useCallback, useMemo, useState} from 'react';
import type {LayoutChangeEvent} from 'react-native';
import {StyleSheet, View} from 'react-native';
import {Gesture, GestureDetector} from 'react-native-gesture-handler';
import Animated, {
  runOnJS,
  useAnimatedStyle,
  useSharedValue,
  withTiming,
} from 'react-native-reanimated';
import type {HomeCardItem} from '../../types/api';

const HORIZONTAL_PADDING = 16;
const GAP = 12;

type CardLayout = {
  height: number;
  index: number;
  left: number;
  top: number;
  width: number;
};

type Props = {
  cards: HomeCardItem[];
  estimateHeight: (card: HomeCardItem) => number;
  keyExtractor: (card: HomeCardItem, index: number) => string;
  onReorder: (cards: HomeCardItem[]) => void;
  renderCard: (card: HomeCardItem) => React.ReactNode;
};

export function MasonryDraggableGrid({
  cards,
  estimateHeight,
  keyExtractor,
  onReorder,
  renderCard,
}: Props) {
  const [containerWidth, setContainerWidth] = useState(0);
  const [measuredHeights, setMeasuredHeights] = useState<Record<string, number>>({});

  const {layouts, contentHeight} = useMemo(() => {
    const cardWidth = Math.max(0, (containerWidth - HORIZONTAL_PADDING * 2 - GAP) / 2);
    const columnHeights = [0, 0];
    const nextLayouts: CardLayout[] = cards.map((card, index) => {
      const key = keyExtractor(card, index);
      const column = columnHeights[0] <= columnHeights[1] ? 0 : 1;
      const height = measuredHeights[key] ?? estimateHeight(card);
      const layout = {
        height,
        index,
        left: HORIZONTAL_PADDING + column * (cardWidth + GAP),
        top: columnHeights[column],
        width: cardWidth,
      };
      columnHeights[column] += height + GAP;
      return layout;
    });

    return {
      layouts: nextLayouts,
      contentHeight: Math.max(0, ...columnHeights) - (cards.length > 0 ? GAP : 0),
    };
  }, [cards, containerWidth, estimateHeight, keyExtractor, measuredHeights]);

  const handleContainerLayout = useCallback((event: LayoutChangeEvent) => {
    setContainerWidth(event.nativeEvent.layout.width);
  }, []);

  const handleCardLayout = useCallback((key: string, event: LayoutChangeEvent) => {
    const height = Math.ceil(event.nativeEvent.layout.height);
    setMeasuredHeights(previous =>
      previous[key] === height ? previous : {...previous, [key]: height},
    );
  }, []);

  const handleDrop = useCallback(
    (fromIndex: number, translationX: number, translationY: number) => {
      const origin = layouts[fromIndex];
      if (!origin) return;

      const dropX = origin.left + origin.width / 2 + translationX;
      const dropY = origin.top + origin.height / 2 + translationY;
      let targetIndex = fromIndex;
      let closestDistance = Number.POSITIVE_INFINITY;

      layouts.forEach(layout => {
        const centerX = layout.left + layout.width / 2;
        const centerY = layout.top + layout.height / 2;
        const distance = Math.hypot(dropX - centerX, dropY - centerY);
        if (distance < closestDistance) {
          closestDistance = distance;
          targetIndex = layout.index;
        }
      });

      if (targetIndex === fromIndex) return;
      const reordered = [...cards];
      const [movedCard] = reordered.splice(fromIndex, 1);
      reordered.splice(targetIndex, 0, movedCard);
      onReorder(reordered);
    },
    [cards, layouts, onReorder],
  );

  return (
    <View
      onLayout={handleContainerLayout}
      style={[styles.container, {height: contentHeight}]}>
      {containerWidth > 0
        ? cards.map((card, index) => {
            const key = keyExtractor(card, index);
            const layout = layouts[index];
            return (
              <SortableCard
                key={key}
                index={index}
                layout={layout}
                onDrop={handleDrop}
                onLayout={event => handleCardLayout(key, event)}>
                {renderCard(card)}
              </SortableCard>
            );
          })
        : null}
    </View>
  );
}

function SortableCard({
  children,
  index,
  layout,
  onDrop,
  onLayout,
}: {
  children: React.ReactNode;
  index: number;
  layout: CardLayout;
  onDrop: (index: number, translationX: number, translationY: number) => void;
  onLayout: (event: LayoutChangeEvent) => void;
}) {
  const translateX = useSharedValue(0);
  const translateY = useSharedValue(0);
  const active = useSharedValue(0);

  const gesture = useMemo(
    () =>
      Gesture.Pan()
        .activateAfterLongPress(250)
        .onStart(() => {
          active.value = 1;
        })
        .onUpdate(event => {
          translateX.value = event.translationX;
          translateY.value = event.translationY;
        })
        .onEnd(event => {
          runOnJS(onDrop)(index, event.translationX, event.translationY);
        })
        .onFinalize(() => {
          active.value = 0;
          translateX.value = withTiming(0, {duration: 160});
          translateY.value = withTiming(0, {duration: 160});
        }),
    [active, index, onDrop, translateX, translateY],
  );

  const animatedStyle = useAnimatedStyle(() => ({
    elevation: active.value ? 10 : 0,
    opacity: active.value ? 0.96 : 1,
    transform: [
      {translateX: translateX.value},
      {translateY: translateY.value},
      {scale: active.value ? 1.025 : 1},
    ],
    zIndex: active.value ? 20 : 1,
  }));

  return (
    <GestureDetector gesture={gesture}>
      <Animated.View
        onLayout={onLayout}
        style={[
          styles.card,
          {left: layout.left, top: layout.top, width: layout.width},
          animatedStyle,
        ]}>
        {children}
      </Animated.View>
    </GestureDetector>
  );
}

const styles = StyleSheet.create({
  container: {position: 'relative', width: '100%', marginTop: 12},
  card: {position: 'absolute'},
});
