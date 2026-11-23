<template>
  <div class="heat">
    <i v-for="day in days" :key="day" class="cell" :class="level(day)" :title="day"></i>
  </div>
</template>

<script setup lang="ts">
const props = defineProps<{
  days: string[];
  logs: { date: string; value: number }[];
  target: number;
}>();

function level(day: string) {
  const value = props.logs.find((item) => item.date === day)?.value || 0;
  if (value <= 0) return '';
  const step = Math.min(4, Math.max(1, Math.ceil((value / props.target) * 4)));
  return `l${step}`;
}
</script>
