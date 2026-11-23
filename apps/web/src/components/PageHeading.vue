<template>
  <div v-if="split" class="page-head">
    <div>
      <p class="eyebrow">{{ eyebrow }}</p>
      <h1>{{ title }}</h1>
    </div>
    <slot />
  </div>
  <template v-else>
    <p class="eyebrow">{{ eyebrow }}</p>
    <h1>{{ title }}</h1>
    <p v-if="note" class="muted">{{ note }}</p>
  </template>
</template>

<script setup lang="ts">
defineProps<{ eyebrow: string; title: string; note?: string; split?: boolean }>();
</script>
