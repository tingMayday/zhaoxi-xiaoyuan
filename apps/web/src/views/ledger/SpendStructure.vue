<template>
  <section class="ledger-block tone-spend">
    <span class="panel-corner" :aria-label="rows.length + ' 类'">{{ rows.length }}</span>
    <div class="page-head">
      <h2>消费结构</h2>
    </div>
    <Chart v-if="rows.length" :option="option" :height="chartHeight" />
    <Notice v-else text="这个月还没有支出。记一笔今天的账。" kind="empty" />
  </section>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import type { EChartsCoreOption } from 'echarts/core';
import Chart from '../../components/Chart.vue';
import Notice from '../../components/Notice.vue';
import { yuan } from '../../utils';

const props = defineProps<{ categories: { id: string; name: string; amountCents: number }[] }>();

const ink = '#2c261f';
const muted = '#6d645a';
const clay = '#8a5a42';
const font = 'Segoe UI, PingFang SC, Microsoft YaHei, sans-serif';

const rows = computed(() => props.categories
  .filter((item) => item.amountCents > 0)
  .slice()
  .sort((a, b) => a.amountCents - b.amountCents));

const total = computed(() => rows.value.reduce((sum, item) => sum + item.amountCents, 0) || 1);
const chartHeight = computed(() => `${Math.max(180, rows.value.length * 42 + 36)}px`);

const option = computed<EChartsCoreOption>(() => ({
  color: [clay],
  textStyle: { fontFamily: font },
  tooltip: {
    trigger: 'axis',
    axisPointer: { type: 'shadow' },
    formatter: (params: unknown) => {
      const list = params as { name?: string; value?: number }[];
      const item = list[0];
      if (!item) return '';
      const cents = Math.round(Number(item.value || 0) * 100);
      const share = Math.round((cents / total.value) * 100);
      return `${item.name}<br/>¥${yuan(cents)} · ${share}%`;
    },
  },
  grid: { left: 72, right: 72, top: 8, bottom: 8 },
  xAxis: {
    type: 'value',
    axisLabel: { show: false },
    splitLine: { show: false },
    axisLine: { show: false },
    axisTick: { show: false },
  },
  yAxis: {
    type: 'category',
    data: rows.value.map((item) => item.name),
    axisTick: { show: false },
    axisLine: { show: false },
    axisLabel: { color: muted, fontSize: 13 },
  },
  series: [{
    type: 'bar',
    barWidth: 16,
    data: rows.value.map((item) => Number((item.amountCents / 100).toFixed(2))),
    itemStyle: {
      borderRadius: [0, 999, 999, 0],
      color: clay,
    },
    label: {
      show: true,
      position: 'right',
      color: ink,
      fontSize: 12,
      formatter: (params: unknown) => {
        const item = params as { dataIndex?: number; value?: number };
        const row = rows.value[item.dataIndex || 0];
        if (!row) return '';
        const share = Math.round((row.amountCents / total.value) * 100);
        return `¥${yuan(row.amountCents)}  ${share}%`;
      },
    },
  }],
}));
</script>
