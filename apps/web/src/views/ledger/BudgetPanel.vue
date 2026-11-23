<template>
  <h2>预算</h2>
  <Field label="本月总额" tone="short">
    <el-input :model-value="modelValue" inputmode="decimal" @update:model-value="$emit('update:modelValue', $event)" @change="$emit('save', 'total', $event)" />
  </Field>
  <Notice v-if="hasBudget" :text="`已使用 ${Math.round((usedRatio || 0) * 100)}%，还剩 ¥${yuan(remainingCents)}`" kind="muted" />
  <div class="track" v-if="usedRatio != null"><span :style="{ width: Math.min(usedRatio * 100, 100) + '%' }"></span></div>
  <div class="budget-grid">
    <Field v-for="item in categories" :key="item.id" :label="`${item.name}预算`">
      <el-input :model-value="item.budgetCents == null ? '' : String(item.budgetCents / 100)" inputmode="decimal" @change="$emit('save', item.id, $event)" />
    </Field>
  </div>
</template>

<script setup lang="ts">
import Field from '../../components/Field.vue';
import Notice from '../../components/Notice.vue';
import { yuan } from '../../utils';

defineProps<{
  modelValue: string;
  categories: { id: string; name: string; budgetCents: number | null }[];
  usedRatio: number | null;
  remainingCents: number | null;
  hasBudget: boolean;
}>();
defineEmits<{ 'update:modelValue': [value: string]; save: [key: string, raw: string] }>();
</script>
