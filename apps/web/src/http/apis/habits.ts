import { request } from '..';

export interface Habit {
  id: string;
  name: string;
  kind: 'check' | 'count' | 'value';
  unit: string;
  dailyTarget: number;
  logs: { date: string; value: number }[];
}

export interface HabitBoard {
  today: string;
  days: string[];
  habits: Habit[];
}

export function listHabits() {
  return request<HabitBoard>('/api/habits');
}

export function saveHabitLog(id: string, date: string, value: number) {
  return request(`/api/habits/${id}/logs/${date}`, { method: 'PUT', json: { value } });
}

export function createHabit(body: { name: string; kind: Habit['kind']; unit: string; dailyTarget: number }) {
  return request('/api/habits', { method: 'POST', json: body });
}

export function archiveHabit(id: string) {
  return request(`/api/habits/${id}`, { method: 'PATCH', json: { archived: true } });
}
