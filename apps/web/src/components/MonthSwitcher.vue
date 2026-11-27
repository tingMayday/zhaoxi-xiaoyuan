<template>
  <div class="month-switcher">
    <button type="button" class="month-nav" aria-label="上个月" @click="shift(-1)">‹</button>
    <el-date-picker
      class="month-picker"
      :model-value="modelValue"
      type="month"
      value-format="YYYY-MM"
      format="YYYY年 M月"
      :clearable="false"
      @update:model-value="onPick"
    />
    <button type="button" class="month-nav" aria-label="下个月" @click="shift(1)">›</button>
  </div>
</template>

<script setup lang="ts">
const props = defineProps<{ modelValue: string }>();
const emit = defineEmits<{ 'update:modelValue': [value: string]; change: [value: string] }>();

function onPick(value: string | null) {
  if (!value) return;
  emit('update:modelValue', value);
  emit('change', value);
}

function shift(delta: number) {
  const [y, m] = props.modelValue.split('-').map(Number);
  const date = new Date(Date.UTC(y, m - 1 + delta, 1));
  const next = `${date.getUTCFullYear()}-${String(date.getUTCMonth() + 1).padStart(2, '0')}`;
  emit('update:modelValue', next);
  emit('change', next);
}
</script>
