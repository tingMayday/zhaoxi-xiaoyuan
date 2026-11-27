<template>
  <section>
    <PageHeading eyebrow="收藏" title="书影音" />
    <PageStatus :loading="loading" :error="loadError" :retry="retry" />
    <template v-if="!loading && !loadError">
      <StatGrid :columns="3" :items="summaryStats" />

      <section class="surface tone-media">
        <div class="panel-head">
          <h2>类型分布</h2>
        </div>
        <Chart v-if="kindOption" :option="kindOption" height="220px" />
        <Notice v-else text="收进几件收藏后，这里会按类型摊开来看。" kind="empty" />
      </section>

      <MediaForm :save="create" />
      <Notice :text="error" kind="error" />

      <section class="surface tone-media">
        <div class="panel-head">
          <h2>收藏</h2>
        </div>
        <div class="media-toolbar">
          <div class="media-toolbar-group">
            <span class="media-toolbar-label">状态</span>
            <ChipBar :options="statusOptions" :model-value="status" @update:model-value="status = $event as Status | ''" />
          </div>
          <div class="media-toolbar-group">
            <span class="media-toolbar-label">视图</span>
            <ChipBar :options="viewOptions" :model-value="view" @update:model-value="setView($event as 'wall' | 'list')" />
          </div>
        </div>
        <MediaGallery v-if="shown.length" :view="view" :items="shown" @remove="remove" @edit="openEdit" />
        <Notice v-else text="这里还空着。把最近看完的一部先放进来。" kind="empty" />
      </section>
    </template>

    <el-dialog v-model="editOpen" title="编辑收藏" width="min(480px, 92vw)" @closed="editing = null">
      <form v-if="editing" class="stack" @submit.prevent="saveEdit">
        <Field label="名称"><el-input v-model="editing.title" /></Field>
        <Field label="作者 / 导演"><el-input v-model="editing.creator" /></Field>
        <Field label="状态">
          <el-select v-model="editing.status">
            <el-option v-for="(label, key) in statusLabel" :key="key" :label="label" :value="key" />
          </el-select>
        </Field>
        <StarRating v-model="editing.rating" />
        <Field label="短评"><el-input v-model="editing.note" type="textarea" maxlength="500" /></Field>
        <Notice :text="editError" kind="error" />
        <el-button type="primary" native-type="submit" round>保存</el-button>
      </form>
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import type { EChartsCoreOption } from 'echarts/core';
import Chart from '../../components/Chart.vue';
import ChipBar from '../../components/ChipBar.vue';
import Field from '../../components/Field.vue';
import Notice from '../../components/Notice.vue';
import PageHeading from '../../components/PageHeading.vue';
import PageStatus from '../../components/PageStatus.vue';
import StatGrid from '../../components/StatGrid.vue';
import { usePageLoad } from '../../composables/usePageLoad';
import { createMedia, fetchMediaStats, listMedia, removeMedia, updateMedia, type MediaItem, type MediaStats } from '../../http/api';
import { confirmAction } from '../../utils';
import { kindLabel, statusLabel, type Kind, type Status } from './labels';
import MediaForm from './MediaForm.vue';
import MediaGallery from './MediaGallery.vue';
import StarRating from './StarRating.vue';

const kindColor: Record<Kind, string> = {
  book: '#8a5a42',
  film: '#6b4560',
  series: '#5b3d8f',
  album: '#3d6b8a',
};

const items = ref<MediaItem[]>([]);
const stats = ref<MediaStats | null>(null);
const status = ref<Status | ''>('');
const view = ref<'wall' | 'list'>((localStorage.getItem('zhaoxi-media-view') as 'wall' | 'list') || 'wall');
const error = ref('');
const editOpen = ref(false);
const editError = ref('');
const editing = ref<{ id: string; title: string; creator: string; status: Status; rating: number; note: string } | null>(null);
const viewOptions = [{ id: 'wall', label: '封面墙' }, { id: 'list', label: '列表' }];
const statusOptions = computed(() => [{ id: '', label: '全部' }, ...Object.entries(statusLabel).map(([id, label]) => ({ id, label }))]);
const shown = computed(() => items.value.filter((item) => !status.value || item.status === status.value));
const topKind = computed(() => {
  const top = [...(stats.value?.kinds || [])].sort((a, b) => b.count - a.count)[0];
  return top && top.count > 0 ? kindLabel[top.kind] : '—';
});
const summaryStats = computed(() => [
  { label: `${stats.value?.year || ''} 看完`, value: String(stats.value?.doneCount ?? 0) },
  { label: '平均星级', value: stats.value?.averageRating == null ? '—' : String(stats.value.averageRating) },
  { label: '最多的一类', value: topKind.value },
]);
const kindRows = computed(() => (stats.value?.kinds || []).filter((item) => item.count > 0));
const kindOption = computed<EChartsCoreOption | null>(() => {
  if (!kindRows.value.length) return null;
  const muted = '#6d645a';
  const font = 'Segoe UI, PingFang SC, Microsoft YaHei, sans-serif';
  return {
    textStyle: { fontFamily: font },
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: 48, right: 20, top: 28, bottom: 28 },
    xAxis: {
      type: 'category',
      data: kindRows.value.map((item) => kindLabel[item.kind]),
      axisTick: { show: false },
      axisLine: { lineStyle: { color: '#cfc1ab' } },
      axisLabel: { color: muted, fontSize: 13 },
    },
    yAxis: {
      type: 'value',
      minInterval: 1,
      axisLabel: { color: muted },
      splitLine: { lineStyle: { color: '#eadfce', type: 'dashed' } },
    },
    series: [{
      type: 'bar',
      barMaxWidth: 36,
      data: kindRows.value.map((item) => ({
        value: item.count,
        itemStyle: {
          borderRadius: [10, 10, 0, 0],
          color: kindColor[item.kind],
        },
      })),
      label: { show: true, position: 'top', color: muted, fontSize: 12 },
    }],
  };
});

const { loading, loadError, load, retry } = usePageLoad(async () => {
  const [nextItems, nextStats] = await Promise.all([listMedia(), fetchMediaStats()]);
  items.value = nextItems;
  stats.value = nextStats;
});
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
function openEdit(id: string) {
  const item = items.value.find((row) => row.id === id);
  if (!item) return;
  editing.value = {
    id: item.id,
    title: item.title,
    creator: item.creator || '',
    status: item.status,
    rating: item.rating || 0,
    note: item.note || '',
  };
  editError.value = '';
  editOpen.value = true;
}
async function saveEdit() {
  if (!editing.value) return;
  editError.value = '';
  try {
    await updateMedia(editing.value.id, {
      title: editing.value.title,
      creator: editing.value.creator,
      status: editing.value.status,
      rating: editing.value.rating || null,
      note: editing.value.note,
    });
    editOpen.value = false;
    await load();
  } catch (err) {
    editError.value = err instanceof Error ? err.message : '没有保存';
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
