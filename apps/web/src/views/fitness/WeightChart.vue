<template>
  <svg v-if="paths.weight" class="chart" viewBox="0 0 640 220" role="img" aria-label="体重与七日均线">
    <path :d="paths.weight" fill="none" stroke="#1c1915" stroke-width="1.6" />
    <path :d="paths.ma" class="ma" fill="none" stroke-width="1.4" stroke-dasharray="4 4" />
  </svg>
</template>

<script setup lang="ts">
import { computed } from 'vue';

const props = defineProps<{ points: { weightKg: number | null; ma7: number | null }[] }>();

const paths = computed(() => {
  const usable = props.points.filter((item) => item.weightKg != null);
  if (!usable.length) return { weight: '', ma: '' };
  const weights = usable.map((item) => item.weightKg as number);
  const averages = usable.map((item) => item.ma7).filter((item): item is number => item != null);
  const min = Math.min(...weights, ...averages) - 0.4;
  const max = Math.max(...weights, ...averages) + 0.4;
  const x = (index: number) => (usable.length === 1 ? 320 : 24 + (index / (usable.length - 1)) * 592);
  const y = (value: number) => 20 + ((max - value) / (max - min || 1)) * 180;
  const line = (pick: (item: (typeof usable)[number]) => number | null) => {
    let started = false;
    return usable.map((item, index) => {
      const value = pick(item);
      if (value == null) return '';
      const command = started ? 'L' : 'M';
      started = true;
      return `${command} ${x(index)} ${y(value)}`;
    }).filter(Boolean).join(' ');
  };
  return { weight: line((item) => item.weightKg), ma: line((item) => item.ma7) };
});
</script>
