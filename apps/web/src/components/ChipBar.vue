<template>
  <div class="filters">
    <el-button
      v-for="option in options"
      :key="option.id"
      round
      :type="active(option.id) ? 'primary' : 'default'"
      @click="$emit('update:modelValue', option.id)"
    >
      {{ option.label }}
    </el-button>
  </div>
</template>

<script setup lang="ts">
const props = defineProps<{ options: { id: string; label: string }[]; modelValue: string | string[] }>();
defineEmits<{ 'update:modelValue': [value: string] }>();

function active(id: string) {
  return Array.isArray(props.modelValue) ? props.modelValue.includes(id) : props.modelValue === id;
}
</script>
