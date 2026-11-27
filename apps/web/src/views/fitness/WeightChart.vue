<template>
  <section class="surface tone-weight">
    <div class="panel-head"><h2>体重走势</h2></div>
    <Chart :option="option" height="280px" />
  </section>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import type { EChartsCoreOption } from 'echarts/core';
import Chart from '../../components/Chart.vue';

const props = defineProps<{ points: { date?: string; weightKg: number | null; ma7: number | null }[] }>();

const ink = '#2c261f';
const muted = '#6d645a';
const plum = '#6b4560';
const clay = '#8a5a42';
const line = '#cfc1ab';
const font = 'Segoe UI, PingFang SC, Microsoft YaHei, sans-serif';

const rows = computed(() => props.points.filter((item) => item.weightKg != null));

const option = computed<EChartsCoreOption>(() => ({
  color: [plum, clay],
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
    data: rows.value.map((item, index) => item.date || String(index + 1)),
    axisLine: { lineStyle: { color: line } },
    axisTick: { show: false },
    axisLabel: {
      color: muted,
      hideOverlap: true,
      formatter: (value: string) => {
        if (!value.includes('-')) return value;
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
      data: rows.value.map((item) => item.weightKg),
      areaStyle: { color: 'rgba(107, 69, 96, 0.08)' },
      itemStyle: { color: plum },
      lineStyle: { color: plum },
    },
    {
      name: '七日均线',
      type: 'line',
      smooth: true,
      symbol: 'none',
      lineStyle: { type: 'dashed', width: 1.5, color: clay },
      data: rows.value.map((item) => item.ma7),
    },
  ],
}));
</script>
