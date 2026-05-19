import AsyncStorage from '../platform/storage';

const BASE_URL = 'https://gateway.mujidigital.com';
const UAC_BASE = `${BASE_URL}/uac`;

// Basic auth: mallee-muji-uac:maliSoaClientSecret
const BASIC_AUTH = 'Basic bWFsbGVlLW11amktdWFjOm1hbGlTb2FDbGllbnRTZWNyZXQ=';

export const TOKEN_KEY = 'mooketquant_auth_token';
export const USER_INFO_KEY = 'mooketquant_user_info';

async function getToken(): Promise<string | null> {
  try {
    const raw = await AsyncStorage.getItem(TOKEN_KEY);
    if (!raw) return null;
    const parsed = JSON.parse(raw);
    return parsed.access_token ?? null;
  } catch {
    return null;
  }
}

async function request<T>(
  url: string,
  options: RequestInit = {},
  withAuth = true,
): Promise<T> {
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    ...(options.headers as Record<string, string>),
  };

  if (withAuth) {
    const token = await getToken();
    if (token) headers.Authorization = `Bearer ${token}`;
  }

  const res = await fetch(url, {...options, headers});

  if (!res.ok) {
    throw new Error(`HTTP ${res.status}: ${res.statusText}`);
  }

  return res.json();
}

export const api = {
  // POST to UAC with Basic auth (for login)
  uacPost: <T>(path: string, params: Record<string, string>): Promise<T> => {
    const qs = new URLSearchParams(params).toString();
    return request<T>(`${UAC_BASE}${path}?${qs}`, {
      method: 'POST',
      headers: {Authorization: BASIC_AUTH},
    }, false);
  },

  // Authenticated POST
  post: <T>(path: string, body: unknown): Promise<T> =>
    request<T>(`${BASE_URL}${path}`, {
      method: 'POST',
      body: JSON.stringify(body),
    }),

  // Authenticated GET
  get: <T>(path: string): Promise<T> =>
    request<T>(`${BASE_URL}${path}`, {method: 'GET'}),
};
