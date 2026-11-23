<template>
  <section>
    <PageHeading eyebrow="待买" title="先搁在这里" />
    <StatGrid :columns="3" :items="stats" />
    <BuyForm :save="create" />
    <Notice :text="error" kind="error" />
    <BuyLists :items="payload?.items || []" :month="month" @buy="mark" @remove="remove" />
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import Notice from '../../components/Notice.vue';
import { createBuyItem, fetchBuyItems, markBought, removeBuyItem, type BuyBoard } from '../../http/api';
import PageHeading from '../../components/PageHeading.vue';
import StatGrid from '../../components/StatGrid.vue';
import { centsFromYuanAllowZero, confirmAction, yuan } from '../../utils';
import BuyForm from './BuyForm.vue';
import BuyLists from './BuyLists.vue';

const payload = ref<BuyBoard | null>(null);
const error = ref('');
const month = new Date().toISOString().slice(0, 7);
const stats = computed(() => [
  { label: '未买', value: String(payload.value?.openCount ?? 0) },
  { label: '预计', value: `¥${yuan(payload.value?.estimateCents)}` },
  { label: '本月买到', value: String(payload.value?.boughtThisMonth ?? 0) },
]);

onMounted(load);
async function load() {
  payload.value = await fetchBuyItems();
}

async function create(form: { title: string; quantity: string; amount: string; note: string }) {
  error.value = '';
  try {
    await createBuyItem({
      title: form.title,
      quantity: Number(form.quantity || 1),
      estimateCents: centsFromYuanAllowZero(form.amount) ?? 0,
      note: form.note,
    });
    await load();
  } catch (err) {
    error.value = err instanceof Error ? err.message : '没有加入';
    throw err;
  }
}

function findItem(id: string) {
  return payload.value?.items.find((row) => row.id === id);
}

async function mark(id: string) {
  const item = findItem(id);
  const name = item ? `「${item.title}」` : '这件';
  if (!await confirmAction(`把${name}标成已经买到？`, '买到了', '买到了')) return;
  await markBought(id);
  await load();
}
async function remove(id: string) {
  const item = findItem(id);
  const name = item ? `「${item.title}」` : '这件';
  if (!await confirmAction(`删除后，${name}就不会再留在清单里。`, '删除待买', '删除')) return;
  await removeBuyItem(id);
  await load();
}
</script>
