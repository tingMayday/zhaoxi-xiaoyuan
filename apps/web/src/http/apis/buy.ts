import { request } from '..';

export interface BuyItem {
  id: string;
  title: string;
  quantity: number;
  estimateCents: number;
  status: 'open' | 'bought';
  boughtOn: string | null;
  note: string;
}

export interface BuyBoard {
  items: BuyItem[];
  openCount: number;
  estimateCents: number;
  boughtThisMonth: number;
}

export function fetchBuyItems() {
  return request<BuyBoard>('/api/buy-items');
}

export function createBuyItem(body: { title: string; quantity?: number; estimateCents: number; note?: string }) {
  return request('/api/buy-items', { method: 'POST', json: body });
}

export function markBought(id: string) {
  return request(`/api/buy-items/${id}`, { method: 'PATCH', json: { status: 'bought' } });
}

export function removeBuyItem(id: string) {
  return request(`/api/buy-items/${id}`, { method: 'DELETE' });
}
