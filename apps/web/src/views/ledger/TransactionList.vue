<template>
  <h2>流水</h2>
  <ChipBar :options="options" :model-value="selected" @update:model-value="$emit('filter', $event)" />
  <RecordList :items="rows">
    <template #actions="{ item }">
      <el-button text @click="$emit('remove', item.id)">删除</el-button>
    </template>
  </RecordList>
  <Notice v-if="transactions.length === 0" text="这组筛选下还没有流水。" kind="empty" />
</template>

<script setup lang="ts">
import { computed } from 'vue';
import ChipBar from '../../components/ChipBar.vue';
import Notice from '../../components/Notice.vue';
import RecordList from '../../components/RecordList.vue';
import { yuan } from '../../utils';

const props = defineProps<{
  transactions: { id: string; occurredOn: string; direction: 'income' | 'expense'; amountCents: number; note: string; category: { name: string } }[];
  categories: { id: string; name: string; kind: 'income' | 'expense' }[];
  direction: '' | 'income' | 'expense';
  categoryId: string;
}>();
defineEmits<{ filter: [id: string]; remove: [id: string] }>();

const visibleCategories = computed(() => props.categories.filter((item) => !props.direction || item.kind === props.direction));
const options = computed(() => [
  { id: 'all', label: '全部' },
  { id: 'expense', label: '支出' },
  { id: 'income', label: '收入' },
  { id: 'cats', label: '所有分类' },
  ...visibleCategories.value.map((item) => ({ id: item.id, label: item.name })),
]);
const selected = computed(() => [props.direction || 'all', props.categoryId || 'cats']);
const rows = computed(() => props.transactions.map((item) => ({
  id: item.id,
  title: item.category.name,
  meta: item.occurredOn.slice(5),
  note: item.note,
  amount: `${item.direction === 'expense' ? '-' : '+'}¥${yuan(item.amountCents)}`,
})));
</script>
