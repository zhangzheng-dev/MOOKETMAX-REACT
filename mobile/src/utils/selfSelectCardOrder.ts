import AsyncStorage from '@react-native-async-storage/async-storage';
import type {HomeCardItem} from '../types/api';
import {getHomeCardEntityKey} from './homeFallbackCards';

const STORAGE_KEY_PREFIX = '@mooket/self-select-card-order/';

export function getSelfSelectCardOrderKey(card: HomeCardItem): string | null {
  if (card.historyId != null) return `history:${card.historyId}`;
  return card.exampleEntityKey ?? getHomeCardEntityKey(card);
}

export async function applySavedSelfSelectCardOrder(category: string, cards: HomeCardItem[]) {
  try {
    const raw = await AsyncStorage.getItem(`${STORAGE_KEY_PREFIX}${category}`);
    if (!raw) return cards;
    const order = JSON.parse(raw) as string[];
    const positions = new Map(order.map((key, index) => [key, index]));
    return cards
      .map((card, index) => ({
        card,
        index,
        position: positions.get(getSelfSelectCardOrderKey(card) ?? ''),
      }))
      .sort((a, b) => {
        if (a.position == null && b.position == null) return a.index - b.index;
        if (a.position == null) return 1;
        if (b.position == null) return -1;
        return a.position - b.position;
      })
      .map(item => item.card);
  } catch {
    return cards;
  }
}

export async function saveSelfSelectCardOrder(category: string, cards: HomeCardItem[]) {
  const order = cards
    .map(getSelfSelectCardOrderKey)
    .filter((key): key is string => Boolean(key));
  await AsyncStorage.setItem(`${STORAGE_KEY_PREFIX}${category}`, JSON.stringify(order));
}
