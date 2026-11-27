import { request } from '..';

export interface Category { id: string; name: string; kind: 'income' | 'expense' }

export interface FinanceSummary {
  incomeCents: number;
  expenseCents: number;
  balanceCents: number;
  totalBudgetCents: number | null;
  remainingCents: number | null;
  usedRatio: number | null;
  categories: { id: string; name: string; amountCents: number; budgetCents: number | null }[];
}

export interface MonthCompare {
  available: boolean;
  expenseDeltaCents: number;
  incomeDeltaCents: number;
}

export interface Transaction {
  id: string;
  occurredOn: string;
  direction: 'income' | 'expense';
  amountCents: number;
  note: string;
  category: { name: string };
}

export interface NewTransaction {
  occurredOn: string;
  direction: 'income' | 'expense';
  amountCents: number;
  categoryId: string;
  note: string;
}

export function listCategories() {
  return request<Category[]>('/api/categories');
}

export function createCategory(body: { name: string; kind: 'income' | 'expense' }) {
  return request<Category>('/api/categories', { method: 'POST', json: body });
}

export function archiveCategory(id: string) {
  return request(`/api/categories/${id}`, { method: 'PATCH' });
}

export function fetchSummary(month: string) {
  return request<FinanceSummary>(`/api/finance/summary?month=${month}`);
}

export function fetchCompare(month: string) {
  return request<MonthCompare>(`/api/finance/compare?month=${month}`);
}

export function listTransactions(query: { month: string; direction?: string; categoryId?: string }) {
  const params = new URLSearchParams({ month: query.month });
  if (query.direction) params.set('direction', query.direction);
  if (query.categoryId) params.set('categoryId', query.categoryId);
  return request<Transaction[]>(`/api/transactions?${params}`);
}

export function createTransaction(body: NewTransaction) {
  return request('/api/transactions', { method: 'POST', json: body });
}

export function removeTransaction(id: string) {
  return request(`/api/transactions/${id}`, { method: 'DELETE' });
}

export function saveBudget(body: { yearMonth: string; categoryKey: string; amountCents: number }) {
  return request('/api/budgets', { method: 'POST', json: body });
}
