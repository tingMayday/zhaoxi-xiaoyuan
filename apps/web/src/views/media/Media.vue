<template>
  <section>
    <PageHeading split eyebrow="收藏" title="书影音">
      <ChipBar :options="viewOptions" :model-value="view" @update:model-value="setView($event as 'wall' | 'list')" />
    </PageHeading>
    <StatGrid :columns="3" :items="summaryStats" />
    <Meter v-for="item in stats?.kinds || []" :key="item.kind" :label="kindLabel[item.kind]" :value="String(item.count)" :width="kindWidth(item.count)" />
    <MediaForm :save="create" />
    <Notice :text="error" kind="error" />
    <ChipBar :options="statusOptions" :model-value="status" @update:model-value="status = $event as Status | ''" />
    <MediaGallery v-if="shown.length" :view="view" :items="shown" @remove="remove" />
    <Notice v-else-if="loaded" text="这里还空着。把最近看完的一部先放进来。" kind="empty" />
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import ChipBar from '../../components/ChipBar.vue';
import { createMedia, fetchMediaStats, listMedia, removeMedia, type MediaItem, type MediaStats } from '../../http/api';
import Meter from '../../components/Meter.vue';
import Notice from '../../components/Notice.vue';
import PageHeading from '../../components/PageHeading.vue';
import StatGrid from '../../components/StatGrid.vue';
import { confirmAction } from '../../utils';
import { kindLabel, statusLabel, type Kind, type Status } from './labels';
import MediaForm from './MediaForm.vue';
import MediaGallery from './MediaGallery.vue';

const items = ref<MediaItem[]>([]);
const stats = ref<MediaStats | null>(null);
const status = ref<Status | ''>('');
const view = ref<'wall' | 'list'>((localStorage.getItem('zhaoxi-media-view') as 'wall' | 'list') || 'wall');
const error = ref('');
const loaded = ref(false);
const viewOptions = [{ id: 'wall', label: '封面墙' }, { id: 'list', label: '列表' }];
const statusOptions = computed(() => [{ id: '', label: '全部' }, ...Object.entries(statusLabel).map(([id, label]) => ({ id, label }))]);
const shown = computed(() => items.value.filter((item) => !status.value || item.status === status.value));
const topKind = computed(() => {
  const top = [...(stats.value?.kinds || [])].sort((a, b) => b.count - a.count)[0];
  return top && top.count > 0 ? kindLabel[top.kind] : '—';
});
const maxKind = computed(() => Math.max(...(stats.value?.kinds.map((item) => item.count) || [1]), 1));
const summaryStats = computed(() => [
  { label: `${stats.value?.year || ''} 看完`, value: String(stats.value?.doneCount ?? 0) },
  { label: '平均星级', value: stats.value?.averageRating == null ? '—' : String(stats.value.averageRating) },
  { label: '最多的一类', value: topKind.value },
]);

onMounted(load);
async function load() {
  const [nextItems, nextStats] = await Promise.all([listMedia(), fetchMediaStats()]);
  items.value = nextItems;
  stats.value = nextStats;
  loaded.value = true;
}
function kindWidth(count: number) {
  return `${(count / maxKind.value) * 100}%`;
}
function setView(next: 'wall' | 'list') {
  view.value = next;
  localStorage.setItem('zhaoxi-media-view', next);
}
async function create(form: { kind: Kind; title: string; creator: string; status: Status; note: string; rating: number; coverUrl: string }) {
  error.value = '';
  try {
    await createMedia({ ...form, rating: form.rating || undefined });
    await load();
  } catch (err) {
    error.value = err instanceof Error ? err.message : '没有收进来';
    throw err;
  }
}
async function remove(id: string) {
  const item = items.value.find((row) => row.id === id);
  const name = item ? `「${item.title}」` : '这件收藏';
  if (!await confirmAction(`删除后，${name}就不会再出现在收藏里。`, '删除收藏', '删除')) return;
  await removeMedia(id);
  await load();
}
</script>
