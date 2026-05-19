import axios, {AxiosError} from 'axios';
import {API_BASE_URL} from '../config/env';
import {sessionStore} from '../store/sessionStore';
import type {ApiResponse} from '../types/api';

export const apiClient = axios.create({
  baseURL: API_BASE_URL,
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
    Accept: 'application/json',
  },
  // 处理大数字ID（超过JS安全整数范围），将它们转为字符串
  transformResponse: [(data: string) => {
    try {
      // 把超过15位的整数转为字符串，防止精度丢失
      const fixed = data.replace(/:\s*(\d{16,})/g, ':"$1"');
      return JSON.parse(fixed);
    } catch {
      return data;
    }
  }],
});

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
    return response;
  },
  error => {
    // eslint-disable-next-line no-console
    console.log('[API ERR]', error.config?.url, error.message, error.code, error.response?.status);
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

      // 优先取后端返回的 message 字段
      let message: string | undefined;
      if (data && typeof data === 'object') {
        // 后端 ApiResponse 格式: {code, message, data}
        message = (data as {message?: unknown}).message as string | undefined;
        // message 为 null/undefined/空字符串时视为无效
        if (!message) message = undefined;
      }

      // 如果后端没给有效 message，根据 HTTP 状态给用户友好的中文提示
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
          // 网络层错误（超时、拒绝连接等）
          message = '网络异常，请检查网络后重试';
        }
      }

      const detail = httpStatus ? `HTTP ${httpStatus}` : error.code || 'no-response';
      throw new Error(`${message} [${detail}]`);
    }
    throw error;
  }
}
