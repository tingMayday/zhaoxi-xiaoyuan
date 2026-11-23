import { isReactive, reactive, watch } from 'vue';

export function bindDraft(key: string, state: object) {
  const saved = localStorage.getItem(key);
  if (saved) {
    try {
      const parsed = JSON.parse(saved) as Record<string, unknown>;
      for (const [name, value] of Object.entries(parsed)) {
        const current = (state as Record<string, unknown>)[name];
        if (value && typeof value === 'object' && current && typeof current === 'object' && !Array.isArray(value)) {
          Object.assign(current, value);
        } else if (name in state) {
          (state as Record<string, unknown>)[name] = value;
        }
      }
    } catch {
      localStorage.removeItem(key);
    }
  }
  const source = isReactive(state) ? state : reactive(state);
  watch(source, () => localStorage.setItem(key, JSON.stringify(state)), { deep: true });
}
