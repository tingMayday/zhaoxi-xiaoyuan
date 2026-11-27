<template>
  <Chart v-if="days.length" :option="option" height="132px" />
</template>

<script setup lang="ts">
import { computed } from 'vue';
import type { EChartsCoreOption } from 'echarts/core';
import Chart from '../../components/Chart.vue';

const props = defineProps<{
  days: string[];
  logs: { date: string; value: number }[];
  target: number;
}>();

const muted = '#6d645a';
const font = 'Segoe UI, PingFang SC, Microsoft YaHei, sans-serif';

const values = computed(() => {
  const map = new Map(props.logs.map((item) => [item.date, item.value]));
  return props.days.map((day) => [day, map.get(day) || 0] as [string, number]);
});

const option = computed<EChartsCoreOption>(() => {
  const start = props.days[0];
  const end = props.days[props.days.length - 1] || start;
  return {
    textStyle: { fontFamily: font },
    tooltip: {
      formatter: (params: unknown) => {
        const item = params as { value?: [string, number] };
        const [day, value] = item.value || ['', 0];
        return `${day}<br/>完成 ${value} / ${props.target}`;
      },
    },
    visualMap: {
      show: false,
      min: 0,
      max: Math.max(props.target, 1),
      inRange: {
        color: ['#efe6d8', '#d7c3a4', '#b08968', '#8a5a42', '#6b4560'],
      },
    },
    calendar: {
      top: 18,
      left: 28,
      right: 8,
      bottom: 8,
      cellSize: ['auto', 14],
      range: start && end ? [start, end] : undefined,
      itemStyle: {
        borderWidth: 2,
        borderColor: 'transparent',
      },
      splitLine: { show: false },
      yearLabel: { show: false },
      monthLabel: {
        color: muted,
        fontSize: 11,
        nameMap: 'ZH',
      },
      dayLabel: {
        color: muted,
        fontSize: 10,
        nameMap: ['日', '一', '二', '三', '四', '五', '六'],
      },
    },
    series: [{
      type: 'heatmap',
      coordinateSystem: 'calendar',
      data: values.value,
    }],
  };
});
</script>
