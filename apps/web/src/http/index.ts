const TOKEN_KEY = 'zhaoxi-token';

export function getToken() {
  return localStorage.getItem(TOKEN_KEY) || '';
}

export function setToken(token: string | null) {
  if (token) localStorage.setItem(TOKEN_KEY, token);
  else localStorage.removeItem(TOKEN_KEY);
}

interface RequestOptions extends Omit<RequestInit, 'body'> {
  json?: unknown;
  body?: BodyInit | null;
}

export async function request<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const { json, body: rawBody, ...rest } = options;
  const headers = new Headers(options.headers);
  const body = json === undefined ? rawBody : JSON.stringify(json);
  if (!(body instanceof FormData) && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json');
  }
  const token = getToken();
  if (token) headers.set('Authorization', `Bearer ${token}`);
  const response = await fetch(path, { ...rest, body, headers });
  const text = await response.text();
  const data = text ? JSON.parse(text) : null;
  if (response.status === 401) {
    setToken(null);
    if (!location.pathname.startsWith('/login')) location.assign('/login');
    throw new Error('请重新登录');
  }
  if (!response.ok) {
    const message = data?.message;
    throw new Error(Array.isArray(message) ? message.join('；') : message || '请求失败');
  }
  return data as T;
}
