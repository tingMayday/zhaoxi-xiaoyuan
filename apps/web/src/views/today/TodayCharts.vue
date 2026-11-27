<template>
  <div class="today-charts">
    <section class="panel tone-score">
      <div class="panel-head"><h2>今日构成</h2></div>
      <Chart :option="scoreOption" />
    </section>

    <section class="panel tone-spend">
      <div class="panel-head">
        <h2>本月支出</h2>
        <router-link to="/记账">去记账</router-link>
      </div>
      <Chart v-if="slices.length" class="spend-chart" :option="spendOption" height="280px" />
      <Notice v-else text="这个月还没有支出。" kind="empty" />
    </section>

    <section class="panel span tone-weight">
      <div class="panel-head">
        <h2>体重走势</h2>
        <router-link to="/健身">去记录</router-link>
      </div>
      <Chart v-if="weights.length" :option="weightOption" height="280px" />
      <Notice v-else text="还没有体重。记下一次，曲线就会出现。" kind="empty" />
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import type { EChartsCoreOption } from 'echarts/core';
import Chart from '../../components/Chart.vue';
import Notice from '../../components/Notice.vue';
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

const spendTotal = computed(() => slices.value.reduce((sum, item) => sum + item.value, 0));

const spendOption = computed<EChartsCoreOption>(() => ({
  // 高对比度色相，避免相邻扇区发糊
  color: ['#d4533a', '#5b3d8f', '#1f8a6a', '#d4921a', '#2f6fad', '#a34d2e', '#c44f8a', '#5a7a2e'],
  textStyle: { fontFamily: font },
  title: {
    text: `¥${spendTotal.value.toFixed(0)}`,
    subtext: '本月合计',
    left: '50%',
    top: '39%',
    textAlign: 'center',
    textStyle: {
      color: ink,
      fontFamily: 'Palatino Linotype, Palatino, Songti SC, SimSun, serif',
      fontSize: 26,
      fontWeight: 600,
      lineHeight: 32,
    },
    subtextStyle: { color: muted, fontSize: 13, lineHeight: 22 },
  },
  tooltip: {
    trigger: 'item',
    backgroundColor: 'rgba(255, 253, 248, 0.96)',
    borderColor: line,
    textStyle: { color: ink, fontSize: 13 },
    formatter: (params: unknown) => {
      const item = params as { name?: string; value?: number; percent?: number; marker?: string };
      const amount = Number(item.value || 0).toFixed(2);
      return `${item.marker || ''}${item.name}<br/><b>¥${amount}</b> · ${item.percent ?? 0}%`;
    },
  },
  legend: { show: false },
  series: [{
    type: 'pie',
    radius: ['44%', '66%'],
    center: ['50%', '50%'],
    roseType: 'radius',
    minShowLabelAngle: 4,
    padAngle: 2,
    itemStyle: {
      borderColor: '#fffdf8',
      borderWidth: 3,
      shadowBlur: 10,
      shadowColor: 'rgba(62, 42, 24, 0.1)',
    },
    label: {
      show: true,
      formatter: (params: unknown) => {
        const item = params as { name?: string; value?: number; percent?: number };
        return `{name|${item.name}}\n{amt|¥${Number(item.value || 0).toFixed(0)}}  {pct|${Math.round(item.percent || 0)}%}`;
      },
      rich: {
        name: {
          color: ink,
          fontSize: 16,
          fontWeight: 700,
          lineHeight: 24,
        },
        amt: {
          color: '#a34d2e',
          fontSize: 15,
          fontWeight: 600,
          lineHeight: 22,
        },
        pct: {
          color: muted,
          fontSize: 14,
          lineHeight: 22,
        },
      },
    },
    labelLine: {
      show: true,
      length: 28,
      length2: 22,
      minTurnAngle: 90,
      smooth: 0.1,
      lineStyle: {
        color: '#9a8870',
        width: 1.4,
      },
    },
    emphasis: {
      scale: true,
      scaleSize: 6,
      itemStyle: {
        shadowBlur: 16,
        shadowColor: 'rgba(62, 42, 24, 0.18)',
      },
      label: { show: true },
    },
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
