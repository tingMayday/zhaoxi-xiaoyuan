<template>
  <section>
    <PageHeading eyebrow="健身" title="稳稳向前" />
    <StatGrid :items="stats" />
    <Notice v-if="series?.profile" :text="`起点 ${series.profile.startWeightKg} kg，目标 ${series.profile.targetWeightKg} kg。运动 ${series.todayLog?.exerciseMin ?? 0} 分钟。`" kind="muted" />
    <WeightChart v-if="series?.points.length" :points="series.points" />
    <Notice v-else text="还没有体重。记下今天的数字，七日均线会从这里长出来。" kind="empty" />
    <LogForm :save="saveLog" />
    <GoalForm :saved="savedGoal" :today="series?.today || ''" :save="saveProfile" />
    <Notice :text="error" kind="error" />
    <WeekPlan :week="week" @save="saveWeek" />
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import Notice from '../../components/Notice.vue';
import { fetchSeries, fetchWeekPlan, saveBodyLog, saveFitnessProfile, saveWeekPlan, type FitnessSeries } from '../../http/api';
import PageHeading from '../../components/PageHeading.vue';
import StatGrid from '../../components/StatGrid.vue';
import GoalForm from './GoalForm.vue';
import LogForm from './LogForm.vue';
import WeekPlan from './WeekPlan.vue';
import WeightChart from './WeightChart.vue';

const series = ref<FitnessSeries | null>(null);
const error = ref('');
const week = reactive(Array.from({ length: 7 }, () => ({ title: '', detail: '' })));
const savedGoal = computed(() => {
  const profile = series.value?.profile;
  if (!profile) return null;
  return {
    start: String(profile.startWeightKg),
    target: String(profile.targetWeightKg),
    startDate: profile.startDate,
    intake: String(profile.targetIntakeKcal),
  };
});
const stats = computed(() => [
  { label: '当前体重', value: series.value?.latest?.weightKg == null ? '—' : `${series.value.latest.weightKg} kg` },
  { label: 'BMI', value: series.value?.bmi == null ? '—' : String(series.value.bmi) },
  { label: '目标进度', value: series.value?.progress == null ? '—' : `${Math.round(series.value.progress * 100)}%` },
  { label: '今日热量缺口', value: series.value?.deficit == null ? '—' : `${series.value.deficit} kcal` },
]);

onMounted(load);

async function load() {
  const [nextSeries, plan] = await Promise.all([fetchSeries(), fetchWeekPlan()]);
  series.value = nextSeries;
  week.forEach((item) => { item.title = ''; item.detail = ''; });
  plan.forEach((item) => {
    const slot = week[item.weekday - 1];
    if (!slot) return;
    slot.title = item.title;
    slot.detail = item.detail;
  });
}

function optionalNumber(value: string) {
  if (!value.trim()) return undefined;
  const number = Number(value);
  return Number.isFinite(number) ? number : undefined;
}

async function saveLog(payload: { weight: string; fat: string; intake: string; minutes: string }) {
  if (!series.value) return;
  error.value = '';
  try {
    await saveBodyLog(series.value.today, {
      weightKg: optionalNumber(payload.weight),
      bodyFatPct: optionalNumber(payload.fat),
      intakeKcal: optionalNumber(payload.intake),
      exerciseMin: optionalNumber(payload.minutes),
    });
    await load();
  } catch (err) {
    error.value = err instanceof Error ? err.message : '没有记下';
    throw err;
  }
}

async function saveProfile(payload: { start: string; target: string; startDate: string; intake: string }) {
  error.value = '';
  try {
    await saveFitnessProfile({
      startWeightKg: Number(payload.start),
      targetWeightKg: Number(payload.target),
      startDate: payload.startDate,
      targetIntakeKcal: Number(payload.intake),
    });
    await load();
  } catch (err) {
    error.value = err instanceof Error ? err.message : '没有保存';
    throw err;
  }
}

async function saveWeek() {
  await saveWeekPlan(week.map((item, index) => ({ weekday: index + 1, title: item.title, detail: item.detail })).filter((item) => item.title.trim()));
  await load();
}
</script>
