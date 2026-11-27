<template>
  <section class="surface tone-buy">
    <div class="panel-head"><h2>还没买</h2></div>
    <RecordList :items="openRows">
      <template #actions="{ item }">
        <el-button class="btn-soft" round @click="$emit('edit', item.id)">编辑</el-button>
        <el-button type="primary" round @click="$emit('buy', item.id)">买到了</el-button>
        <el-button class="btn-danger" round @click="$emit('remove', item.id)">删除</el-button>
      </template>
    </RecordList>
    <Notice v-if="openRows.length === 0" text="清单是空的。想到要买的，先写下来。" kind="empty" />
  </section>
  <section class="surface tone-recent">
    <div class="panel-head"><h2>本月已买</h2></div>
    <RecordList :items="boughtRows">
      <template #actions="{ item }">
        <el-button class="btn-soft" round @click="$emit('reopen', item.id)">改回未买</el-button>
        <el-button class="btn-danger" round @click="$emit('remove', item.id)">删除</el-button>
      </template>
    </RecordList>
    <Notice v-if="boughtRows.length === 0" text="这个月还没有记下买到的东西。" kind="empty" />
  </section>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import Notice from '../../components/Notice.vue';
import RecordList from '../../components/RecordList.vue';
import { yuan } from '../../utils';

const props = defineProps<{
  items: { id: string; title: string; quantity: number; estimateCents: number; status: 'open' | 'bought'; boughtOn: string | null; note: string }[];
  month: string;
}>();
defineEmits<{ buy: [id: string]; remove: [id: string]; edit: [id: string]; reopen: [id: string] }>();

const money = (item: (typeof props.items)[number]) => `¥${yuan(Math.round(item.estimateCents * item.quantity))}`;
const openRows = computed(() => props.items.filter((item) => item.status === 'open').map((item) => ({
  id: item.id, title: item.title, meta: `×${item.quantity}`, note: item.note, amount: money(item),
})));
const boughtRows = computed(() => props.items
  .filter((item) => item.status === 'bought' && item.boughtOn?.startsWith(props.month))
  .map((item) => ({ id: item.id, title: item.title, note: item.boughtOn?.slice(5), amount: money(item) })));
</script>
