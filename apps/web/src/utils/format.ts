export function yuan(cents: number | null | undefined): string {
  if (cents == null) return '—';
  const sign = cents < 0 ? '-' : '';
  const abs = Math.abs(cents);
  return `${sign}${(abs / 100).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
}

export function centsFromYuan(input: string): number | null {
  const text = input.trim();
  if (!text) return null;
  const value = Number(text);
  if (!Number.isFinite(value) || value <= 0) return null;
  return Math.round(value * 100);
}

export function centsFromYuanAllowZero(input: string): number | null {
  const text = input.trim();
  if (!text) return null;
  const value = Number(text);
  if (!Number.isFinite(value) || value < 0) return null;
  return Math.round(value * 100);
}

export function dateLabel(iso: string): string {
  const [year, month, day] = iso.split('-').map(Number);
  const week = '日一二三四五六'[new Date(Date.UTC(year, month - 1, day)).getUTCDay()];
  return `${year}年${month}月${day}日 周${week}`;
}

export function deltaText(cents: number): string {
  if (cents === 0) return '与上月持平';
  return `${cents > 0 ? '多' : '少'} ¥${yuan(Math.abs(cents))}`;
}
