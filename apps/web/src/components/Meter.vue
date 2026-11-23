<template>
  <div class="bar">
    <span>{{ label }}</span>
    <div class="track"><span :style="{ width }"></span></div>
    <span>{{ value }}</span>
  </div>
</template>

<script setup lang="ts">
defineProps<{ label: string; value: string; width: string }>();
</script>
