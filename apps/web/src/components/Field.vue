<template>
  <label class="field" :class="tone">
    <span class="label">{{ label }}</span>
    <slot />
  </label>
</template>

<script setup lang="ts">
defineProps<{ label: string; tone?: 'short' | 'note' }>();
</script>
