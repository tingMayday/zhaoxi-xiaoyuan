<template>
  <h2>消费结构</h2>
  <Meter v-for="item in rows" :key="item.id" :label="item.name" :value="`¥${yuan(item.amountCents)}`" :width="item.width" />
  <Notice v-if="rows.length === 0" text="这个月还没有支出。记一笔今天的账。" kind="empty" />
</template>

<script setup lang="ts">
import { computed } from 'vue';
import Meter from '../../components/Meter.vue';
import Notice from '../../components/Notice.vue';
import { yuan } from '../../utils';

const props = defineProps<{ categories: { id: string; name: string; amountCents: number }[] }>();
const spent = computed(() => props.categories.filter((item) => item.amountCents > 0));
const maxAmount = computed(() => Math.max(...spent.value.map((item) => item.amountCents), 1));
const rows = computed(() => spent.value.map((item) => ({ ...item, width: `${(item.amountCents / maxAmount.value) * 100}%` })));
</script>
