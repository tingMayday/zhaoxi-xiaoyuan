<template>
  <div class="stars">
    <span class="label">星级</span>
    <el-rate :model-value="modelValue" clearable @update:model-value="$emit('update:modelValue', $event)" />
  </div>
</template>

<script setup lang="ts">
defineProps<{ modelValue: number }>();
defineEmits<{ 'update:modelValue': [value: number] }>();
</script>
