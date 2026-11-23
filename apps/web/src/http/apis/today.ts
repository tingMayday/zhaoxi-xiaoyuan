import { request } from '..';

export interface TodayHabit {
  id: string;
  name: string;
  kind: 'check' | 'count' | 'value';
  unit: string;
  dailyTarget: number;
  value: number;
}

export interface TodayTask {
  id: string;
  title: string;
  note: string;
  status: 'open' | 'done';
  time: string;
}

export interface TodayRecent {
  id: string;
  date: string;
  kind: string;
  title: string;
  detail: string;
  value: string;
}

export interface TodayOverview {
  date: string;
  quote: string;
  score: number;
  breakdown: { habitPoints: number; schedulePoints: number; recordPoints: number; overduePoints: number };
  finance: { expenseCents: number; totalBudgetCents: number | null; remainingCents: number | null };
  habits: { done: number; total: number; items: TodayHabit[] };
  schedule: { todayOpen: number; todayTotal: number; overdue: number; next7: number; items: TodayTask[] };
  recent: TodayRecent[];
  fitness: { weightKg: number | null; weightDate: string | null };
  buy: { openCount: number; estimateCents: number };
  media: { yearDone: number; averageRating: number | null };
}

export function fetchToday() {
  return request<TodayOverview>('/api/today');
}
