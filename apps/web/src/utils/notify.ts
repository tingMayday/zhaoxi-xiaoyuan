/** 网页本地提醒：仅当前标签页存活时有效。 */

export function canUseNotification() {
  return typeof window !== 'undefined' && 'Notification' in window;
}

export async function ensureNotifyPermission() {
  if (!canUseNotification()) return false;
  if (Notification.permission === 'granted') return true;
  if (Notification.permission === 'denied') return false;
  return (await Notification.requestPermission()) === 'granted';
}

/** 在提醒时刻弹出通知；超过 7 天或已过期则跳过。 */
export function armLocalReminder(title: string, atIso: string) {
  if (!canUseNotification() || Notification.permission !== 'granted') return false;
  const delay = new Date(atIso).getTime() - Date.now();
  if (!Number.isFinite(delay) || delay <= 0 || delay > 7 * 24 * 60 * 60 * 1000) return false;
  window.setTimeout(() => {
    try {
      new Notification('朝夕小院', { body: title, tag: `schedule-${atIso}` });
    } catch {
      // ignore
    }
  }, delay);
  return true;
}
