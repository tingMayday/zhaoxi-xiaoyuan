<template>
  <section>
    <PageHeading eyebrow="习惯" title="完成一点，就算前进" :note="data ? `今日 ${doneCount} / ${data.habits.length} · 近 30 天` : ''" />
    <HabitForm :save="create" />
    <Notice :text="error" kind="error" />
    <HabitCard
      v-for="habit in data?.habits || []"
      :key="habit.id"
      :habit="habit"
      :today="data?.today || ''"
      :days="data?.days || []"
      @set="setValue(habit.id, $event)"
      @archive="archive(habit.id)"
    />
    <Notice v-if="data && data.habits.length === 0" text="还没有习惯。先加一件今天就能做的小事。" kind="empty" />
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import Notice from '../../components/Notice.vue';
import { archiveHabit, createHabit, listHabits, saveHabitLog, type Habit, type HabitBoard } from '../../http/api';
import { confirmAction } from '../../utils';
import PageHeading from '../../components/PageHeading.vue';
import HabitCard from './HabitCard.vue';
import HabitForm from './HabitForm.vue';

const data = ref<HabitBoard | null>(null);
const error = ref('');
const doneCount = computed(() => data.value?.habits.filter((habit) => {
  const value = habit.logs.find((item) => item.date === data.value?.today)?.value || 0;
  return value >= habit.dailyTarget;
}).length || 0);

onMounted(load);
async function load() {
  data.value = await listHabits();
}

async function setValue(id: string, value: number) {
  if (!data.value || !Number.isFinite(value)) return;
  const habit = data.value.habits.find((item) => item.id === id);
  if (habit?.kind === 'check') {
    const done = value >= habit.dailyTarget;
    const ok = await confirmAction(
      done ? `把「${habit.name}」记成今天完成？` : `取消「${habit.name}」今天的完成？`,
      done ? '完成习惯' : '取消完成',
      done ? '完成' : '改回',
    );
    if (!ok) return;
  }
  await saveHabitLog(id, data.value.today, value);
  await load();
}

async function create(payload: { name: string; kind: Habit['kind']; target: string; unit: string }) {
  error.value = '';
  try {
    await createHabit({
      name: payload.name,
      kind: payload.kind,
      unit: payload.unit,
      dailyTarget: payload.kind === 'check' ? 1 : Number(payload.target || 1),
    });
    await load();
  } catch (err) {
    error.value = err instanceof Error ? err.message : '没有加上';
    throw err;
  }
}

async function archive(id: string) {
  const habit = data.value?.habits.find((item) => item.id === id);
  const name = habit ? `「${habit.name}」` : '这个习惯';
  if (!await confirmAction(`删除后，${name}不再出现在今日，过去的打卡会留下。`, '删除习惯', '删除')) return;
  await archiveHabit(id);
  await load();
}
</script>
