<template>
  <dl :class="columns === 3 ? 'numbers' : 'facts'">
    <div v-for="item in items" :key="item.label">
      <dt>{{ item.label }}</dt>
      <dd>{{ item.value }}</dd>
      <small v-if="item.hint">{{ item.hint }}</small>
    </div>
  </dl>
</template>

<script setup lang="ts">
export interface StatItem {
  label: string;
  value: string;
  hint?: string;
}

defineProps<{ items: StatItem[]; columns?: 3 | 4 }>();
</script>
