<template>
  <section>
    <PageHeading eyebrow="数据" title="时光档案" note="所有日常，都有出处" />
    <section class="surface tone-archive">
      <div class="page-head">
        <h2>按日期折叠的生活记录</h2>
        <ChipBar :options="filters" :model-value="kind" @update:model-value="kind = $event as typeof kind" />
      </div>
    </section>
    <PageStatus :loading="loading" :error="loadError" :retry="retry" />
    <template v-if="!loading && !loadError">
    <article v-for="day in shown" :key="day.date" class="archive-day">
      <button type="button" @click="toggle(day.date)">
        <strong>{{ dayTitle(day.date) }}</strong>
        <span>{{ day.items.length }} 条记录</span>
      </button>
      <ul v-if="open.has(day.date)">
        <li v-for="(item, index) in day.items" :key="index">
          <em>{{ kindLabel[item.kind] }}</em>
          <div>
            <strong>{{ item.title }}</strong>
            <span>{{ item.detail }}</span>
          </div>
          <b>{{ item.value }}</b>
        </li>
      </ul>
    </article>
    <Notice v-if="board && shown.length === 0" text="这段时间还没有留下记录。" kind="empty" />
    </template>
  </section>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import ChipBar from '../../components/ChipBar.vue';
import Notice from '../../components/Notice.vue';
import PageHeading from '../../components/PageHeading.vue';
import PageStatus from '../../components/PageStatus.vue';
import { usePageLoad } from '../../composables/usePageLoad';
import { fetchArchive, type ArchiveBoard, type ArchiveKind } from '../../http/api';

const filters = [
  { id: '', label: '全部' },
  { id: 'finance', label: '财务' },
  { id: 'health', label: '健康' },
  { id: 'schedule', label: '日程' },
  { id: 'buy', label: '待买' },
];
const kindLabel: Record<ArchiveKind, string> = {
  finance: '财务',
  health: '健康',
  schedule: '日程',
  buy: '待买',
};

const board = ref<ArchiveBoard | null>(null);
const kind = ref<'' | ArchiveKind>('');
const open = ref(new Set<string>());

const shown = computed(() => {
  if (!board.value) return [];
  return board.value.days
    .map((day) => ({
      date: day.date,
      items: kind.value ? day.items.filter((item) => item.kind === kind.value) : day.items,
    }))
    .filter((day) => day.items.length > 0);
});

const { loading, loadError, retry } = usePageLoad(async () => {
  board.value = await fetchArchive();
  open.value = new Set(board.value.days.slice(0, 3).map((day) => day.date));
});

function toggle(date: string) {
  const next = new Set(open.value);
  if (next.has(date)) next.delete(date);
  else next.add(date);
  open.value = next;
}

function dayTitle(date: string) {
  const today = board.value?.today || '';
  if (date === today) return '今天';
  if (today && date === shift(today, -1)) return '昨天';
  const [, month, day] = date.split('-');
  return `${Number(month)} 月 ${Number(day)} 日`;
}

function shift(iso: string, days: number) {
  const [year, month, day] = iso.split('-').map(Number);
  const date = new Date(Date.UTC(year, month - 1, day));
  date.setUTCDate(date.getUTCDate() + days);
  return date.toISOString().slice(0, 10);
}
</script>
