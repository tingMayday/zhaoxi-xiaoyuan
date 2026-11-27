<template>
  <section>
    <PageHeading split eyebrow="记账" title="花得明白">
      <MonthSwitcher v-model="month" @change="load" />
    </PageHeading>

    <PageStatus :loading="loading" :error="loadError" :retry="retry" />
    <template v-if="!loading && !loadError">
      <StatGrid v-if="summary" :columns="3" :items="totals" />
      <p v-if="compare && !compare.available" class="ledger-insight muted">有了上月数据后，这里会显示变化。</p>
      <div v-else-if="compare?.available" class="ledger-insight">
        <span :class="deltaClass(compare.expenseDeltaCents, 'expense')">支出{{ deltaText(compare.expenseDeltaCents) }}</span>
        <span class="ledger-insight-sep" aria-hidden="true">·</span>
        <span :class="deltaClass(compare.incomeDeltaCents, 'income')">收入{{ deltaText(compare.incomeDeltaCents) }}</span>
      </div>

      <BudgetPanel
        v-model="budgetYuan"
        :categories="summary?.categories || []"
        :expense-cents="summary?.expenseCents ?? 0"
        :used-ratio="summary?.usedRatio ?? null"
        :remaining-cents="summary?.remainingCents ?? null"
        :has-budget="summary?.totalBudgetCents != null"
        :save-categories="saveCategoryBudgets"
        @save="saveBudget"
      />

      <section class="ledger-block tone-spend">
        <div class="page-head">
          <h2>记一笔</h2>
          <el-button class="btn-soft" round @click="catOpen = true">分类管理</el-button>
        </div>
        <EntryForm :month="month" :categories="categories" :save="create" />
        <Notice :text="error" kind="error" />
      </section>

      <SpendStructure :categories="summary?.categories || []" />
      <TransactionList
        :transactions="transactions"
        :categories="categories"
        :direction="direction"
        :category-id="categoryId"
        :filtering="filtering"
        @filter="onFilter"
        @remove="remove"
      />
    </template>

    <el-dialog v-model="catOpen" title="分类管理" width="min(520px, 94vw)" destroy-on-close>
      <CategoryPanel :categories="categories" @changed="load" />
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import MonthSwitcher from '../../components/MonthSwitcher.vue';
import Notice from '../../components/Notice.vue';
import PageHeading from '../../components/PageHeading.vue';
import PageStatus from '../../components/PageStatus.vue';
import StatGrid from '../../components/StatGrid.vue';
import { usePageLoad } from '../../composables/usePageLoad';
import { createTransaction, fetchCompare, fetchSummary, listCategories, listTransactions, removeTransaction, saveBudget as postBudget, type Category, type FinanceSummary, type MonthCompare, type Transaction } from '../../http/api';
import { centsFromYuan, centsFromYuanAllowZero, confirmAction, deltaText, yuan, zonedMonth } from '../../utils';
import BudgetPanel from './BudgetPanel.vue';
import CategoryPanel from './CategoryPanel.vue';
import EntryForm from './EntryForm.vue';
import SpendStructure from './SpendStructure.vue';
import TransactionList from './TransactionList.vue';

const month = ref(zonedMonth());
const summary = ref<FinanceSummary | null>(null);
const compare = ref<MonthCompare | null>(null);
const categories = ref<Category[]>([]);
const transactions = ref<Transaction[]>([]);
const direction = ref<'' | 'income' | 'expense'>('');
const categoryId = ref('');
const budgetYuan = ref('');
const error = ref('');
const catOpen = ref(false);
const filtering = ref(false);

const totals = computed(() => {
  if (!summary.value) return [];
  const c = compare.value?.available ? compare.value : null;
  return [
    {
      label: '收入',
      value: `¥${yuan(summary.value.incomeCents)}`,
      hint: c ? deltaText(c.incomeDeltaCents) : undefined,
    },
    {
      label: '支出',
      value: `¥${yuan(summary.value.expenseCents)}`,
      hint: c ? deltaText(c.expenseDeltaCents) : undefined,
    },
    {
      label: '结余',
      value: `¥${yuan(summary.value.balanceCents)}`,
      hint: summary.value.balanceCents >= 0 ? '本月还有结余' : '本月已透支',
    },
  ];
});

async function fetchAll() {
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

const { loading, loadError, load, retry } = usePageLoad(fetchAll);

function deltaClass(cents: number, kind: 'income' | 'expense') {
  if (cents === 0) return 'delta-flat';
  const better = kind === 'income' ? cents > 0 : cents < 0;
  return better ? 'delta-good' : 'delta-warn';
}

async function onFilter(id: string) {
  if (id === 'all' || id === 'expense' || id === 'income') {
    direction.value = id === 'all' ? '' : id;
    categoryId.value = '';
  } else {
    categoryId.value = id === 'cats' ? '' : id;
  }
  filtering.value = true;
  try {
    transactions.value = await listTransactions({
      month: month.value,
      direction: direction.value,
      categoryId: categoryId.value,
    });
  } finally {
    filtering.value = false;
  }
}

async function softLoad() {
  await fetchAll();
}

async function saveBudget(key: string, raw: string) {
  const amountCents = centsFromYuanAllowZero(raw);
  if (amountCents == null) return;
  await postBudget({ yearMonth: month.value, categoryKey: key, amountCents });
  await softLoad();
}

async function saveCategoryBudgets(items: { key: string; raw: string }[]) {
  const payload = items
    .map((item) => ({ key: item.key, amountCents: centsFromYuanAllowZero(item.raw) }))
    .filter((item): item is { key: string; amountCents: number } => item.amountCents != null);
  for (const item of payload) {
    await postBudget({ yearMonth: month.value, categoryKey: item.key, amountCents: item.amountCents });
  }
  await softLoad();
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
    await softLoad();
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
  await softLoad();
}
</script>
