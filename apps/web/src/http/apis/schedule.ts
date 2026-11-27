import { request } from '..';

export interface ScheduleItem {
  id: string;
  title: string;
  dueOn: string;
  status: 'open' | 'done';
  note: string;
  startsAt?: string | null;
  endsAt?: string | null;
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
  startsAt?: string;
  endsAt?: string;
};

export function fetchSchedule() {
  return request<ScheduleBoard>('/api/schedule');
}

export function createSchedule(body: ScheduleWrite) {
  return request('/api/schedule', { method: 'POST', json: body });
}

export type SchedulePatch = {
  title?: string;
  dueOn?: string;
  status?: 'open' | 'done';
  note?: string;
  remindAt?: string | null;
  allDay?: boolean;
  startsAt?: string | null;
  endsAt?: string | null;
};

export function updateSchedule(id: string, body: SchedulePatch) {
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

/** 日期 + HH:mm → ISO。 */
export function scheduleDateTimeLocal(dueOn: string, hm?: string) {
  const time = hm?.trim();
  if (!dueOn || !time) return undefined;
  return scheduleLocalToIso(`${dueOn}T${time}`);
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

/** ISO → 表单用的本地 YYYY-MM-DDTHH:mm。 */
export function scheduleIsoToLocal(iso?: string | null) {
  if (!iso) return '';
  const date = new Date(iso);
  if (Number.isNaN(date.getTime())) return '';
  const pad = (n: number) => String(n).padStart(2, '0');
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}`;
}
