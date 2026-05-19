export type Mode = 'dark' | 'light';

export interface InventoryTheme {
  mode: Mode;
  bg: string;
  panel: string;
  panelSoft: string;
  panelMuted: string;
  text: string;
  subText: string;
  muted: string;
  border: string;
  borderSoft: string;
  accent: string;
  accentStrong: string;
  warning: string;
  positive: string;
  negative: string;
  shadow: string;
}

export const themes: Record<Mode, InventoryTheme> = {
  dark: {
    mode: 'dark',
    bg: '#090e14',
    panel: '#111820',
    panelSoft: '#151c24',
    panelMuted: '#0d131a',
    text: '#eef2f6',
    subText: '#b6bdc8',
    muted: '#8b939f',
    border: '#27313c',
    borderSoft: '#1e2832',
    accent: '#f3cc12',
    accentStrong: '#29c3e7',
    warning: '#f3cc12',
    positive: '#ff4f61',
    negative: '#27c783',
    shadow: 'rgba(0,0,0,0.24)',
  },
  light: {
    mode: 'light',
    bg: '#f5f5f5',
    panel: '#ffffff',
    panelSoft: '#fafafa',
    panelMuted: '#f7f8fa',
    text: '#171c22',
    subText: '#5e6670',
    muted: '#7a818b',
    border: '#e5e7eb',
    borderSoft: '#eef0f2',
    accent: '#f6c914',
    accentStrong: '#30c9ec',
    warning: '#f6c914',
    positive: '#ef3b45',
    negative: '#0f9466',
    shadow: 'rgba(15,23,42,0.08)',
  },
};
