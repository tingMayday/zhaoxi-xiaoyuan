import { request } from '..';

export interface ScheduleItem {
  id: string;
  title: string;
  dueOn: string;
  status: 'open' | 'done';
  note: string;
  startsAt?: string | null;
  remindAt?: string | null;
  allDay?: boolean;
}

export interface ScheduleBoard {
  today: string;
  items: ScheduleItem[];
  overdueItems: ScheduleItem[];
}

export type ScheduleWrite = {
  title: string;
  dueOn: string;
  note?: string;
  remindAt?: string;
  allDay?: boolean;
};

export function fetchSchedule() {
  return request<ScheduleBoard>('/api/schedule');
}

export function createSchedule(body: ScheduleWrite) {
  return request('/api/schedule', { method: 'POST', json: body });
}

export function updateSchedule(id: string, body: { status: 'open' | 'done' }) {
  return request(`/api/schedule/${id}`, { method: 'PATCH', json: body });
}

export function removeSchedule(id: string) {
  return request(`/api/schedule/${id}`, { method: 'DELETE' });
}

/** 把表单本地时间 YYYY-MM-DDTHH:mm 转成 ISO，空值返回 undefined。 */
export function scheduleLocalToIso(value?: string) {
  const raw = value?.trim();
  if (!raw) return undefined;
  const date = new Date(raw.includes('T') ? raw : raw.replace(' ', 'T'));
  if (Number.isNaN(date.getTime())) return undefined;
  return date.toISOString();
}

export function scheduleClock(iso?: string | null) {
  if (!iso) return '';
  const date = new Date(iso);
  if (Number.isNaN(date.getTime())) return '';
  return new Intl.DateTimeFormat('zh-CN', {
    hour: '2-digit',
    minute: '2-digit',
    hour12: false,
  }).format(date);
}
