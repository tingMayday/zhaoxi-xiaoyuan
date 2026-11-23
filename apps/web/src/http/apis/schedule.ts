import { request } from '..';

export interface ScheduleItem { id: string; title: string; dueOn: string; status: 'open' | 'done'; note: string }

export interface ScheduleBoard {
  today: string;
  items: ScheduleItem[];
  overdueItems: ScheduleItem[];
}

export function fetchSchedule() {
  return request<ScheduleBoard>('/api/schedule');
}

export function createSchedule(body: { title: string; dueOn: string; note?: string; startsAt?: string; allDay?: boolean }) {
  return request('/api/schedule', { method: 'POST', json: body });
}

export function updateSchedule(id: string, body: { status: 'open' | 'done' }) {
  return request(`/api/schedule/${id}`, { method: 'PATCH', json: body });
}

export function removeSchedule(id: string) {
  return request(`/api/schedule/${id}`, { method: 'DELETE' });
}
