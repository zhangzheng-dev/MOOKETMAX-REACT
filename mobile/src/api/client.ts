import axios, {AxiosError} from 'axios';
import {Alert} from 'react-native';
import {API_BASE_URL} from '../config/env';
import {resetToLogin} from '../navigation/navigationService';
import {sessionStore} from '../store/sessionStore';
import type {ApiResponse} from '../types/api';

export const apiClient = axios.create({
  baseURL: API_BASE_URL,
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
    Accept: 'application/json',
  },
  transformResponse: [(data: string) => {
    try {
      const fixed = data.replace(/:\s*(\d{16,})/g, ':"$1"');
      return JSON.parse(fixed);
    } catch {
      return data;
    }
  }],
});

const FORCED_LOGOUT_HINT = '您的账号已在另一台设备登录';
let handlingAuthFailure = false;

function getBusinessCode(data: unknown): number | undefined {
  if (!data || typeof data !== 'object') {
    return undefined;
  }
  const value = (data as {code?: unknown}).code;
  return typeof value === 'number' ? value : undefined;
}

function getBusinessMessage(data: unknown): string | undefined {
  if (!data || typeof data !== 'object') {
    return undefined;
  }
  const value = (data as {message?: unknown}).message;
  return typeof value === 'string' ? value : undefined;
}

async function handleAuthFailure(message?: string) {
  if (handlingAuthFailure) {
    return;
  }
  handlingAuthFailure = true;

  try {
    await sessionStore.getState().clear();
    resetToLogin();

    if (message?.includes(FORCED_LOGOUT_HINT)) {
      Alert.alert(
        '账号已下线',
        '您的账号已在另一台设备登录，当前设备已自动退出。为保障账号安全，请重新登录；如非本人操作，请及时修改密码。',
        [{text: '我知道了'}],
        {cancelable: false},
      );
    }
  } finally {
    setTimeout(() => {
      handlingAuthFailure = false;
    }, 300);
  }
}

apiClient.interceptors.request.use(config => {
  const token = sessionStore.getState().token;
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  // eslint-disable-next-line no-console
  console.log('[API REQ]', config.method?.toUpperCase(), config.baseURL ?? '', config.url);
  return config;
});

apiClient.interceptors.response.use(
  response => {
    // eslint-disable-next-line no-console
    console.log('[API RES]', response.config.url, response.status, response.data?.code);

    const businessCode = getBusinessCode(response.data);
    const isAuthRequest =
      response.config.url?.includes('api/v1/auth/login') || response.config.url?.includes('api/v1/auth/send-code');

    if (businessCode === 401 && !isAuthRequest) {
      void handleAuthFailure(getBusinessMessage(response.data));
      return Promise.reject(new Error(getBusinessMessage(response.data) ?? '登录状态已失效'));
    }

    return response;
  },
  error => {
    // eslint-disable-next-line no-console
    console.log('[API ERR]', error.config?.url, error.message, error.code, error.response?.status);

    const message = getBusinessMessage(error.response?.data);
    const isAuthRequest =
      error.config?.url?.includes('api/v1/auth/login') || error.config?.url?.includes('api/v1/auth/send-code');

    if (error.response?.status === 401 && !isAuthRequest) {
      void handleAuthFailure(message);
    }
    return Promise.reject(error);
  },
);

export async function unwrap<T>(request: Promise<{data: ApiResponse<T>}>): Promise<T> {
  try {
    const response = await request;
    const body = response.data;
    if (body.code !== 200) {
      throw new Error(body.message || `请求失败：${body.code}`);
    }
    return body.data as T;
  } catch (error) {
    if (error instanceof AxiosError) {
      const data = error.response?.data;
      const httpStatus = error.response?.status;

      let message: string | undefined;
      if (data && typeof data === 'object') {
        message = (data as {message?: unknown}).message as string | undefined;
        if (!message) {
          message = undefined;
        }
      }

      if (!message) {
        if (httpStatus === 401) {
          message = '登录状态已失效，请重新登录';
        } else if (httpStatus === 403) {
          message = '账号已被禁用，请联系客服';
        } else if (httpStatus === 400) {
          message = error.message || '请求参数错误';
        } else if (httpStatus === 404) {
          message = '请求的资源不存在';
        } else if (httpStatus !== undefined && httpStatus >= 500) {
          message = '服务器繁忙，请稍后重试';
        } else {
          message = '网络异常，请检查网络后重试';
        }
      }

      const detail = httpStatus ? `HTTP ${httpStatus}` : error.code || 'no-response';
      throw new Error(`${message} [${detail}]`);
    }
    throw error;
  }
}
