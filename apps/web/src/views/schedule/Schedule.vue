<template>
  <section>
    <PageHeading eyebrow="日程" title="今天要做的" />
    <ScheduleForm :today="data?.today || ''" :save="create" />
    <Notice :text="error" kind="error" />
    <ScheduleGroups :groups="groups" @toggle="toggle" @remove="remove" />
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import Notice from '../../components/Notice.vue';
import { createSchedule, fetchSchedule, removeSchedule, updateSchedule, type ScheduleBoard } from '../../http/api';
import { confirmAction } from '../../utils';
import PageHeading from '../../components/PageHeading.vue';
import ScheduleForm from './ScheduleForm.vue';
import ScheduleGroups from './ScheduleGroups.vue';

const data = ref<ScheduleBoard | null>(null);
const error = ref('');

const groups = computed(() => {
  const today = data.value?.today || '';
  const items = data.value?.items || [];
  const upcomingEnd = shift(today, 7);
  return [
    { title: '已逾期', empty: '没有拖着的事。', items: data.value?.overdueItems || [] },
    { title: '今天', empty: '今天还空着。', items: items.filter((item) => item.dueOn === today) },
    { title: '未来 7 天', empty: '接下来一周是轻的。', items: items.filter((item) => item.dueOn > today && item.dueOn <= upcomingEnd) },
  ];
});

onMounted(load);
async function load() {
  data.value = await fetchSchedule();
}

function shift(iso: string, days: number) {
  if (!iso) return '';
  const [y, m, d] = iso.split('-').map(Number);
  const date = new Date(Date.UTC(y, m - 1, d));
  date.setUTCDate(date.getUTCDate() + days);
  return date.toISOString().slice(0, 10);
}

async function create(payload: { title: string; dueOn: string; startsAt: string; note: string }) {
  error.value = '';
  try {
    await createSchedule({
      title: payload.title,
      dueOn: payload.dueOn,
      note: payload.note,
      startsAt: payload.startsAt ? new Date(payload.startsAt).toISOString() : undefined,
      allDay: !payload.startsAt,
    });
    await load();
  } catch (err) {
    error.value = err instanceof Error ? err.message : '没有安排上';
    throw err;
  }
}

function findItem(id: string) {
  return [...(data.value?.overdueItems || []), ...(data.value?.items || [])].find((row) => row.id === id);
}

async function toggle(id: string) {
  const item = findItem(id);
  if (!item) return;
  const done = item.status !== 'done';
  const ok = await confirmAction(
    done ? `把「${item.title}」标成完成？` : `把「${item.title}」改回未完成？`,
    done ? '完成日程' : '取消完成',
    done ? '完成' : '改回',
  );
  if (!ok) return;
  await updateSchedule(item.id, { status: done ? 'done' : 'open' });
  await load();
}

async function remove(id: string) {
  const item = findItem(id);
  const name = item ? `「${item.title}」` : '这条日程';
  if (!await confirmAction(`删除后，${name}就不会再出现。`, '删除日程', '删除')) return;
  await removeSchedule(id);
  await load();
}
</script>
