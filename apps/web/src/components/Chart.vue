<template>
  <div ref="host" class="chart-host" :style="{ height }" />
</template>

<script setup lang="ts">
import { BarChart, HeatmapChart, LineChart, PieChart } from 'echarts/charts';
import {
  CalendarComponent,
  GridComponent,
  LegendComponent,
  TooltipComponent,
  VisualMapComponent,
} from 'echarts/components';
import { CanvasRenderer } from 'echarts/renderers';
import { init, use, type ECharts, type EChartsCoreOption } from 'echarts/core';
import { onBeforeUnmount, onMounted, ref, shallowRef, watch } from 'vue';

use([
  BarChart,
  HeatmapChart,
  LineChart,
  PieChart,
  CalendarComponent,
  GridComponent,
  LegendComponent,
  TooltipComponent,
  VisualMapComponent,
  CanvasRenderer,
]);

const props = withDefaults(defineProps<{ option: EChartsCoreOption; height?: string }>(), {
  height: '260px',
});

const host = ref<HTMLElement | null>(null);
const chart = shallowRef<ECharts | null>(null);
let observer: ResizeObserver | null = null;

onMounted(() => {
  if (!host.value) return;
  const instance = init(host.value);
  instance.setOption(props.option);
  chart.value = instance;
  observer = new ResizeObserver(() => instance.resize());
  observer.observe(host.value);
});

watch(() => props.option, (option) => {
  chart.value?.setOption(option, true);
});

onBeforeUnmount(() => {
  observer?.disconnect();
  chart.value?.dispose();
});
</script>
