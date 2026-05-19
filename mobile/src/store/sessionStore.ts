import AsyncStorage from '@react-native-async-storage/async-storage';
import {create} from 'zustand';
import type {AuthResult, UserProfile} from '../types/api';

const TOKEN_KEY = 'mooket.token';
const USER_KEY = 'mooket.user';
const GATEWAY_TOKEN_KEY = 'mooketquant_auth_token';

type SessionState = {
  isHydrated: boolean;
  token: string | null;
  user: UserProfile | null;
  hydrate: () => Promise<void>;
  setAuth: (auth: AuthResult) => Promise<void>;
  setUser: (user: UserProfile | null) => Promise<void>;
  clear: () => Promise<void>;
};

export const sessionStore = create<SessionState>(set => ({
  isHydrated: false,
  token: null,
  user: null,

  async hydrate() {
    const [token, userJson] = await Promise.all([
      AsyncStorage.getItem(TOKEN_KEY),
      AsyncStorage.getItem(USER_KEY),
    ]);
    set({
      token,
      user: userJson ? JSON.parse(userJson) : null,
      isHydrated: true,
    });
  },

  async setAuth(auth) {
    await AsyncStorage.setItem(TOKEN_KEY, auth.token);
    // Save gateway token for inventory (same key as inventoryClient.ts TOKEN_KEY)
    if (auth.gatewayAccessToken) {
      const gatewayData = JSON.stringify({
        access_token: auth.gatewayAccessToken,
        userId: auth.gatewayUserId ?? auth.userId ?? '0',
      });
      await AsyncStorage.setItem(GATEWAY_TOKEN_KEY, gatewayData);
    }
    set({token: auth.token});
  },

  async setUser(user) {
    if (user) {
      await AsyncStorage.setItem(USER_KEY, JSON.stringify(user));
    } else {
      await AsyncStorage.removeItem(USER_KEY);
    }
    set({user});
  },

  async clear() {
    await Promise.all([
      AsyncStorage.removeItem(TOKEN_KEY),
      AsyncStorage.removeItem(USER_KEY),
      AsyncStorage.removeItem(GATEWAY_TOKEN_KEY),
    ]);
    set({token: null, user: null});
  },
}));

