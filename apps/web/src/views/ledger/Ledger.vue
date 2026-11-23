<template>
  <section>
    <PageHeading split eyebrow="记账" title="花得明白">
      <Field label="月份"><el-date-picker v-model="month" type="month" value-format="YYYY-MM" @change="load" /></Field>
    </PageHeading>

    <StatGrid v-if="summary" :columns="3" :items="totals" />
    <Notice v-if="compare && !compare.available" text="有了上月数据后，这里会显示变化。" kind="muted" />
    <p v-else-if="compare?.available">支出比上月{{ deltaText(compare.expenseDeltaCents) }}，收入{{ deltaText(compare.incomeDeltaCents) }}。</p>

    <BudgetPanel
      v-model="budgetYuan"
      :categories="summary?.categories || []"
      :used-ratio="summary?.usedRatio ?? null"
      :remaining-cents="summary?.remainingCents ?? null"
      :has-budget="summary?.totalBudgetCents != null"
      @save="saveBudget"
    />

    <h2>记一笔</h2>
    <EntryForm :month="month" :categories="categories" :save="create" />
    <Notice :text="error" kind="error" />

    <SpendStructure :categories="summary?.categories || []" />
    <TransactionList
      :transactions="transactions"
      :categories="categories"
      :direction="direction"
      :category-id="categoryId"
      @filter="onFilter"
      @remove="remove"
    />
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import Field from '../../components/Field.vue';
import { createTransaction, fetchCompare, fetchSummary, listCategories, listTransactions, removeTransaction, saveBudget as postBudget, type Category, type FinanceSummary, type MonthCompare, type Transaction } from '../../http/api';
import Notice from '../../components/Notice.vue';
import PageHeading from '../../components/PageHeading.vue';
import StatGrid from '../../components/StatGrid.vue';
import { centsFromYuan, centsFromYuanAllowZero, confirmAction, deltaText, yuan } from '../../utils';
import BudgetPanel from './BudgetPanel.vue';
import EntryForm from './EntryForm.vue';
import SpendStructure from './SpendStructure.vue';
import TransactionList from './TransactionList.vue';

const month = ref(new Date().toISOString().slice(0, 7));
const summary = ref<FinanceSummary | null>(null);
const compare = ref<MonthCompare | null>(null);
const categories = ref<Category[]>([]);
const transactions = ref<Transaction[]>([]);
const direction = ref<'' | 'income' | 'expense'>('');
const categoryId = ref('');
const budgetYuan = ref('');
const error = ref('');

const totals = computed(() => summary.value ? [
  { label: '收入', value: `¥${yuan(summary.value.incomeCents)}` },
  { label: '支出', value: `¥${yuan(summary.value.expenseCents)}` },
  { label: '结余', value: `¥${yuan(summary.value.balanceCents)}` },
] : []);

onMounted(load);

async function load() {
  const [nextSummary, nextCompare, nextCategories, nextTx] = await Promise.all([
    fetchSummary(month.value),
    fetchCompare(month.value),
    listCategories(),
    listTransactions({ month: month.value, direction: direction.value, categoryId: categoryId.value }),
  ]);
  summary.value = nextSummary;
  compare.value = nextCompare;
  categories.value = nextCategories;
  transactions.value = nextTx;
  budgetYuan.value = nextSummary.totalBudgetCents == null ? '' : String(nextSummary.totalBudgetCents / 100);
}

function onFilter(id: string) {
  if (id === 'all' || id === 'expense' || id === 'income') {
    direction.value = id === 'all' ? '' : id;
    categoryId.value = '';
  } else {
    categoryId.value = id === 'cats' ? '' : id;
  }
  load();
}

async function saveBudget(key: string, raw: string) {
  const amountCents = centsFromYuanAllowZero(raw);
  if (amountCents == null) return;
  await postBudget({ yearMonth: month.value, categoryKey: key, amountCents });
  await load();
}

async function create(payload: { occurredOn: string; direction: 'income' | 'expense'; amount: string; categoryId: string; note: string }) {
  error.value = '';
  const amountCents = centsFromYuan(payload.amount);
  if (!amountCents) {
    error.value = '请填写大于 0 的金额';
    throw new Error(error.value);
  }
  try {
    await createTransaction({
      occurredOn: payload.occurredOn,
      direction: payload.direction,
      amountCents,
      categoryId: payload.categoryId,
      note: payload.note,
    });
    await load();
  } catch (err) {
    error.value = err instanceof Error ? err.message : '没有记下';
    throw err;
  }
}

async function remove(id: string) {
  const item = transactions.value.find((row) => row.id === id);
  const name = item ? `${item.category.name}${item.note ? `「${item.note}」` : ''}` : '这笔流水';
  if (!await confirmAction(`删除后，${name}不会再出现在流水里。`, '删除流水', '删除')) return;
  await removeTransaction(id);
  await load();
}
</script>
