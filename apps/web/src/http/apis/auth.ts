import { request } from '..';

export interface Profile {
  id: string;
  email: string;
  name: string;
  timezone: string;
  heightCm: number | null;
}

export interface AuthSession {
  token: string;
  user: Profile;
}

export function login(email: string, password: string) {
  return request<AuthSession>('/api/auth/login', { method: 'POST', json: { email, password } });
}

export function register(email: string, password: string, name: string) {
  return request<AuthSession>('/api/auth/register', { method: 'POST', json: { email, password, name } });
}

export function fetchProfile() {
  return request<Profile>('/api/me');
}

export function updateProfile(body: { name: string; timezone: string; heightCm?: number }) {
  return request<Profile>('/api/me', { method: 'PATCH', json: body });
}
