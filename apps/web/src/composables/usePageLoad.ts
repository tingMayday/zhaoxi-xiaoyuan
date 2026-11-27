import { onMounted, ref } from 'vue';

/** 页面首屏加载：loading / loadError / 可重试。 */
export function usePageLoad(loader: () => Promise<void>) {
  const loading = ref(true);
  const loadError = ref('');

  async function load() {
    loading.value = true;
    loadError.value = '';
    try {
      await loader();
    } catch (err) {
      loadError.value = err instanceof Error ? err.message : '加载失败';
    } finally {
      loading.value = false;
    }
  }

  onMounted(load);

  return { loading, loadError, load, retry: load };
}
