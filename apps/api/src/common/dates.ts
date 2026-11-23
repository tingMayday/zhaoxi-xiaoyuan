/** 按用户时区取今天，格式 YYYY-MM-DD。时区无效时退回上海。 */
export function zonedToday(timeZone: string): string {
  try {
    return new Intl.DateTimeFormat('en-CA', {
      timeZone,
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
    }).format(new Date());
  } catch {
    return new Intl.DateTimeFormat('en-CA', {
      timeZone: 'Asia/Shanghai',
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
    }).format(new Date());
  }
}

/** 在日期上加减天数，不经过本地时区。 */
export function addDays(iso: string, days: number): string {
  const [y, m, d] = iso.split('-').map(Number);
  const date = new Date(Date.UTC(y, m - 1, d));
  date.setUTCDate(date.getUTCDate() + days);
  return date.toISOString().slice(0, 10);
}

/** 月份 YYYY-MM 前后移动，用于和上月对比。 */
export function shiftMonth(yearMonth: string, delta: number): string {
  const [y, m] = yearMonth.split('-').map(Number);
  const date = new Date(Date.UTC(y, m - 1 + delta, 1));
  const month = String(date.getUTCMonth() + 1).padStart(2, '0');
  return `${date.getUTCFullYear()}-${month}`;
}

/** 某个月的第一天和最后一天。 */
export function monthBounds(yearMonth: string): { start: string; end: string } {
  const [y, m] = yearMonth.split('-').map(Number);
  const end = new Date(Date.UTC(y, m, 0));
  const endIso = end.toISOString().slice(0, 10);
  return { start: `${yearMonth}-01`, end: endIso };
}

/** 0 是周日，用来挑选今日一句。 */
export function weekdayIndex(iso: string): number {
  const [y, m, d] = iso.split('-').map(Number);
  return new Date(Date.UTC(y, m - 1, d)).getUTCDay();
}

const QUOTES = [
  '周日宜把这一周轻轻放下。',
  '规律不是把每天塞满，而是知道什么值得留下。',
  '记清楚一笔，心里就少一件事。',
  '完成一点，就算前进。',
  '花得明白，不必紧绷。',
  '身体的变化，用七天来看。',
  '想看的和想买的，先搁在院里。',
];

/** 按星期固定一句，同一天每次打开都一样。 */
export function quoteFor(iso: string): string {
  return QUOTES[weekdayIndex(iso)];
}

/** 含结束日在内，向前数 count 天。 */
export function lastDays(endIso: string, count: number): string[] {
  const days: string[] = [];
  for (let i = count - 1; i >= 0; i -= 1) days.push(addDays(endIso, -i));
  return days;
}
