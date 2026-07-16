import type {HomeCardItem, SearchHistory} from '../types/api';

function parseLocalDateTime(value?: string | null) {
  if (!value) return 0;
  const match = value
    .trim()
    .match(/^(\d{4})-(\d{1,2})-(\d{1,2})(?:[ T](\d{1,2}):(\d{1,2})(?::(\d{1,2}))?)?/);
  if (match) {
    const [, y, m, d, h = '0', min = '0', s = '0'] = match;
    return new Date(
      Number(y),
      Number(m) - 1,
      Number(d),
      Number(h),
      Number(min),
      Number(s),
    ).getTime();
  }
  const parsed = Date.parse(value);
  return Number.isFinite(parsed) ? parsed : 0;
}

export function sortSelfSelectCardsByCreateTime(
  cards: HomeCardItem[],
  histories: SearchHistory[] = [],
) {
  const historyById = new Map(histories.map(item => [item.historyId, item]));

  return cards
    .map((card, index) => {
      const history =
        card.historyId == null ? undefined : historyById.get(card.historyId);
      const createTime = card.createTime ?? history?.createTime ?? null;
      return {
        card: createTime ? {...card, createTime} : card,
        index,
        createdAt: parseLocalDateTime(createTime),
      };
    })
    .sort((a, b) => {
      if (a.createdAt !== b.createdAt) return b.createdAt - a.createdAt;
      const aId = a.card.historyId ?? 0;
      const bId = b.card.historyId ?? 0;
      if (aId !== bId) return bId - aId;
      return a.index - b.index;
    })
    .map(item => item.card);
}
