import { useAuthStore } from '../stores/auth';

/** 当前用户时区，未设置时回落到上海。 */
export function userTimezone(): string {
  return useAuthStore().user?.timezone?.trim() || 'Asia/Shanghai';
}

/** 按指定时区取本地年月日。 */
export function zonedParts(date = new Date(), timeZone = userTimezone()) {
  const parts = new Intl.DateTimeFormat('en-CA', {
    timeZone,
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  }).formatToParts(date);
  const get = (type: Intl.DateTimeFormatPartTypes) => parts.find((part) => part.type === type)?.value || '';
  return { year: get('year'), month: get('month'), day: get('day') };
}

/** 用户时区下的今天 YYYY-MM-DD。 */
export function zonedToday(timeZone = userTimezone()): string {
  const { year, month, day } = zonedParts(new Date(), timeZone);
  return `${year}-${month}-${day}`;
}

/** 用户时区下的本月 YYYY-MM。 */
export function zonedMonth(timeZone = userTimezone()): string {
  return zonedToday(timeZone).slice(0, 7);
}

/** 用户时区下的当前小时 0–23。 */
export function zonedHour(timeZone = userTimezone()): number {
  const hour = new Intl.DateTimeFormat('en-GB', {
    timeZone,
    hour: '2-digit',
    hourCycle: 'h23',
  }).formatToParts(new Date()).find((part) => part.type === 'hour')?.value || '0';
  return Number(hour);
}
