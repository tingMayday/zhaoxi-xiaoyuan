<template>
  <p v-if="text" :class="kind">{{ text }}</p>
</template>

<script setup lang="ts">
defineProps<{ text?: string; kind?: 'error' | 'empty' | 'muted' }>();
</script>
