<template>
  <section>
    <PageHeading eyebrow="待买" title="先搁在这里" />
    <PageStatus :loading="loading" :error="loadError" :retry="retry" />
    <template v-if="!loading && !loadError">
      <StatGrid :columns="3" :items="stats" />
      <BuyForm :save="create" />
      <Notice :text="error" kind="error" />
      <BuyLists
        :items="payload?.items || []"
        :month="month"
        @buy="mark"
        @remove="remove"
        @edit="openEdit"
        @reopen="reopen"
      />
    </template>

    <el-dialog v-model="editOpen" title="编辑待买" width="min(440px, 92vw)" @closed="editing = null">
      <form v-if="editing" class="stack" @submit.prevent="saveEdit">
        <Field label="名称"><el-input v-model="editing.title" /></Field>
        <Field label="数量"><el-input v-model="editing.quantity" inputmode="decimal" /></Field>
        <Field label="估价"><el-input v-model="editing.amount" inputmode="decimal" /></Field>
        <Field label="备注"><el-input v-model="editing.note" /></Field>
        <Notice :text="editError" kind="error" />
        <el-button type="primary" native-type="submit" round>保存</el-button>
      </form>
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import Field from '../../components/Field.vue';
import Notice from '../../components/Notice.vue';
import PageHeading from '../../components/PageHeading.vue';
import PageStatus from '../../components/PageStatus.vue';
import StatGrid from '../../components/StatGrid.vue';
import { usePageLoad } from '../../composables/usePageLoad';
import { createBuyItem, fetchBuyItems, markBought, removeBuyItem, updateBuyItem, type BuyBoard } from '../../http/api';
import { centsFromYuanAllowZero, confirmAction, yuan, zonedMonth } from '../../utils';
import BuyForm from './BuyForm.vue';
import BuyLists from './BuyLists.vue';

const payload = ref<BuyBoard | null>(null);
const error = ref('');
const month = zonedMonth();
const editOpen = ref(false);
const editError = ref('');
const editing = ref<{ id: string; title: string; quantity: string; amount: string; note: string } | null>(null);
const stats = computed(() => [
  { label: '未买', value: String(payload.value?.openCount ?? 0) },
  { label: '预计', value: `¥${yuan(payload.value?.estimateCents)}` },
  { label: '本月买到', value: String(payload.value?.boughtThisMonth ?? 0) },
]);

const { loading, loadError, load, retry } = usePageLoad(async () => {
  payload.value = await fetchBuyItems();
});

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

async function reopen(id: string) {
  const item = findItem(id);
  const name = item ? `「${item.title}」` : '这件';
  if (!await confirmAction(`把${name}改回未买？`, '改回未买', '改回')) return;
  await updateBuyItem(id, { status: 'open' });
  await load();
}

function openEdit(id: string) {
  const item = findItem(id);
  if (!item) return;
  editing.value = {
    id: item.id,
    title: item.title,
    quantity: String(item.quantity || 1),
    amount: item.estimateCents ? String(item.estimateCents / 100) : '',
    note: item.note || '',
  };
  editError.value = '';
  editOpen.value = true;
}

async function saveEdit() {
  if (!editing.value) return;
  editError.value = '';
  const quantity = Number(editing.value.quantity || 1);
  if (!Number.isFinite(quantity) || quantity <= 0) {
    editError.value = '数量需大于 0';
    return;
  }
  try {
    await updateBuyItem(editing.value.id, {
      title: editing.value.title,
      quantity,
      estimateCents: centsFromYuanAllowZero(editing.value.amount) ?? 0,
      note: editing.value.note,
    });
    editOpen.value = false;
    await load();
  } catch (err) {
    editError.value = err instanceof Error ? err.message : '没有保存';
  }
}

async function remove(id: string) {
  const item = findItem(id);
  const name = item ? `「${item.title}」` : '这件';
  if (!await confirmAction(`删除后，${name}就不会再留在清单里。`, '删除待买', '删除')) return;
  await removeBuyItem(id);
  await load();
}
</script>
