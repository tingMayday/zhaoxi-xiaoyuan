<template>
  <section class="ledger-block tone-recent">
    <span class="panel-corner" :aria-label="transactions.length + ' 笔'">{{ transactions.length }}</span>
    <div class="page-head">
      <h2>流水</h2>
    </div>
    <div class="ledger-filters">
      <div class="filter-row">
        <span class="filter-label">方向</span>
        <ChipBar :options="directionOptions" :model-value="directionSelected" @update:model-value="onDirection" />
      </div>
      <div class="filter-row">
        <span class="filter-label">分类</span>
        <ChipBar :options="categoryOptions" :model-value="categorySelected" @update:model-value="onCategory" />
      </div>
    </div>
    <div class="ledger-list" :class="{ filtering }">
      <RecordList :items="rows">
        <template #actions="{ item }">
          <el-button class="btn-danger" round @click="$emit('remove', item.id)">删除</el-button>
        </template>
      </RecordList>
      <Notice v-if="transactions.length === 0 && !filtering" text="这组筛选下还没有流水。" kind="empty" />
    </div>
  </section>
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
  filtering?: boolean;
}>();
const emit = defineEmits<{ filter: [id: string]; remove: [id: string] }>();

const visibleCategories = computed(() => props.categories.filter((item) => !props.direction || item.kind === props.direction));
const directionOptions = [
  { id: 'all', label: '全部' },
  { id: 'expense', label: '支出' },
  { id: 'income', label: '收入' },
];
const categoryOptions = computed(() => [
  { id: 'cats', label: '所有分类' },
  ...visibleCategories.value.map((item) => ({ id: item.id, label: item.name })),
]);
const directionSelected = computed(() => props.direction || 'all');
const categorySelected = computed(() => props.categoryId || 'cats');
const rows = computed(() => props.transactions.map((item) => ({
  id: item.id,
  title: item.category.name,
  meta: item.occurredOn.slice(5),
  note: item.note,
  amount: `${item.direction === 'expense' ? '-' : '+'}¥${yuan(item.amountCents)}`,
  tone: item.direction === 'expense' ? 'out' as const : 'in' as const,
})));

function onDirection(id: string) {
  emit('filter', id);
}
function onCategory(id: string) {
  emit('filter', id);
}
</script>
