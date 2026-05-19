import AsyncStorage from '@react-native-async-storage/async-storage';

type StorageValue = string | null;

interface StorageLike {
  getItem: (key: string) => Promise<StorageValue>;
  setItem: (key: string, value: string) => Promise<void>;
  removeItem: (key: string) => Promise<void>;
  multiRemove: (keys: string[]) => Promise<void>;
}

const memoryStore = new Map<string, string>();

let useMemoryFallback = false;

function shouldFallback(error: unknown) {
  const message = error instanceof Error ? error.message : String(error ?? '');
  return (
    message.includes('Native module is null') ||
    message.includes('cannot access legacy storage') ||
    message.includes('AsyncStorage') ||
    message.includes('RCTAsyncStorage')
  );
}

async function withFallback<T>(action: () => Promise<T>, fallback: () => Promise<T>) {
  if (useMemoryFallback) {
    return fallback();
  }

  try {
    return await action();
  } catch (error) {
    if (!shouldFallback(error)) {
      throw error;
    }

    useMemoryFallback = true;
    return fallback();
  }
}

const storage: StorageLike = {
  async getItem(key) {
    return withFallback(
      () => AsyncStorage.getItem(key),
      async () => memoryStore.get(key) ?? null,
    );
  },

  async setItem(key, value) {
    return withFallback(
      () => AsyncStorage.setItem(key, value),
      async () => {
        memoryStore.set(key, value);
      },
    );
  },

  async removeItem(key) {
    return withFallback(
      () => AsyncStorage.removeItem(key),
      async () => {
        memoryStore.delete(key);
      },
    );
  },

  async multiRemove(keys) {
    return withFallback(
      async () => {
        await Promise.all(keys.map(key => AsyncStorage.removeItem(key)));
      },
      async () => {
        keys.forEach(key => memoryStore.delete(key));
      },
    );
  },
};

export default storage;
