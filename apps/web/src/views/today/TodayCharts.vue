<template>
  <div class="today-charts">
    <section class="panel">
      <div class="panel-head"><h2>今日构成</h2></div>
      <Chart :option="scoreOption" />
    </section>

    <section class="panel">
      <div class="panel-head">
        <h2>本月支出</h2>
        <router-link to="/记账">去记账</router-link>
      </div>
      <Chart v-if="slices.length" :option="spendOption" />
      <p v-else class="empty">这个月还没有支出。</p>
    </section>

    <section class="panel span">
      <div class="panel-head">
        <h2>体重走势</h2>
        <router-link to="/健身">去记录</router-link>
      </div>
      <Chart v-if="weights.length" :option="weightOption" height="280px" />
      <p v-else class="empty">还没有体重。记下一次，曲线就会出现。</p>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import type { EChartsCoreOption } from 'echarts/core';
import Chart from '../../components/Chart.vue';
import type { FinanceSummary } from '../../http/api';

const props = defineProps<{
  breakdown: { habitPoints: number; schedulePoints: number; recordPoints: number; overduePoints: number };
  categories: FinanceSummary['categories'];
  points: { date: string; weightKg: number | null; ma7: number | null }[];
}>();

const ink = '#2c261f';
const muted = '#6d645a';
const plum = '#6b4560';
const line = '#cfc1ab';
const font = 'Segoe UI, PingFang SC, Microsoft YaHei, sans-serif';

const parts = computed(() => [
  { name: '习惯', value: props.breakdown.habitPoints, cap: 40 },
  { name: '日程', value: props.breakdown.schedulePoints, cap: 30 },
  { name: '记录', value: props.breakdown.recordPoints, cap: 15 },
  { name: '逾期', value: props.breakdown.overduePoints, cap: 15 },
]);

const slices = computed(() => props.categories
  .filter((item) => item.amountCents > 0)
  .map((item) => ({ name: item.name, value: item.amountCents / 100 })));

const weights = computed(() => props.points.filter((item) => item.weightKg != null).slice(-30));

const scoreOption = computed<EChartsCoreOption>(() => ({
  textStyle: { fontFamily: font },
  grid: { left: 48, right: 78, top: 8, bottom: 8 },
  tooltip: {
    trigger: 'axis',
    axisPointer: { type: 'none' },
    formatter: (params: unknown) => {
      const row = (Array.isArray(params) ? params[0] : params) as { data?: { name?: string; raw?: number; cap?: number } };
      const data = row?.data;
      if (!data) return '';
      return `${data.name} ${data.raw} / ${data.cap}`;
    },
  },
  xAxis: { type: 'value', max: 100, show: false },
  yAxis: {
    type: 'category',
    inverse: true,
    data: parts.value.map((item) => item.name),
    axisTick: { show: false },
    axisLine: { show: false },
    axisLabel: { color: ink, fontSize: 13 },
  },
  series: [{
    type: 'bar',
    barWidth: 14,
    showBackground: true,
    backgroundStyle: { color: '#f3ece3', borderRadius: 8 },
    itemStyle: { color: plum, borderRadius: 8 },
    label: {
      show: true,
      position: 'right',
      color: muted,
      fontSize: 12,
      formatter: (params: { data?: { raw?: number; cap?: number } }) => `${params.data?.raw ?? 0} / ${params.data?.cap ?? 0}`,
    },
    data: parts.value.map((item) => ({
      value: Math.round((item.value / item.cap) * 100),
      name: item.name,
      raw: item.value,
      cap: item.cap,
    })),
  }],
}));

const spendOption = computed<EChartsCoreOption>(() => ({
  color: [plum, '#8a5a42', '#5f8a55', '#c4924a', '#9a6a9e', '#c46b52', '#7d8c6a', '#b08968'],
  textStyle: { fontFamily: font },
  tooltip: {
    trigger: 'item',
    formatter: (params: unknown) => {
      const item = params as { name?: string; value?: number; percent?: number };
      const amount = Number(item.value || 0).toFixed(2);
      return `${item.name}<br/>¥${amount} · ${item.percent ?? 0}%`;
    },
  },
  legend: {
    bottom: 0,
    icon: 'circle',
    itemWidth: 8,
    itemHeight: 8,
    textStyle: { color: muted, fontSize: 12 },
  },
  series: [{
    type: 'pie',
    radius: ['48%', '70%'],
    center: ['50%', '42%'],
    label: { show: false },
    data: slices.value,
  }],
}));

const weightOption = computed<EChartsCoreOption>(() => ({
  color: [plum, '#8a5a42'],
  textStyle: { fontFamily: font },
  tooltip: { trigger: 'axis' },
  legend: {
    top: 0,
    right: 0,
    icon: 'circle',
    itemWidth: 8,
    itemHeight: 8,
    textStyle: { color: muted },
  },
  grid: { left: 44, right: 16, top: 36, bottom: 28 },
  xAxis: {
    type: 'category',
    data: weights.value.map((item) => item.date),
    axisLine: { lineStyle: { color: line } },
    axisTick: { show: false },
    axisLabel: {
      color: muted,
      hideOverlap: true,
      formatter: (value: string) => {
        const [, month, day] = value.split('-');
        return `${Number(month)}/${Number(day)}`;
      },
    },
  },
  yAxis: {
    type: 'value',
    scale: true,
    axisLabel: { color: muted },
    splitLine: { lineStyle: { color: '#eadfce' } },
  },
  series: [
    {
      name: '体重',
      type: 'line',
      smooth: true,
      symbol: 'circle',
      symbolSize: 7,
      data: weights.value.map((item) => item.weightKg),
      areaStyle: { color: 'rgba(107, 69, 96, 0.08)' },
    },
    {
      name: '七日均线',
      type: 'line',
      smooth: true,
      symbol: 'none',
      lineStyle: { type: 'dashed', width: 1.5 },
      data: weights.value.map((item) => item.ma7),
    },
  ],
}));
</script>
