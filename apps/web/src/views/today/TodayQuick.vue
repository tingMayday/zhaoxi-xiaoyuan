<template>
  <nav class="quick">
    <button
      v-for="item in actions"
      :key="item.id"
      type="button"
      class="quick-tile"
      :class="[item.id, { on: panel === item.id }]"
      @click="toggle(item.id)"
    >
      <span class="quick-mark" aria-hidden="true">{{ item.mark }}</span>
      <span class="quick-label">
        <strong>{{ item.title }}</strong>
        <small>{{ item.hint }}</small>
      </span>
    </button>
  </nav>

  <form v-if="panel === 'ledger'" class="composer row tone-spend animate__animated animate__fadeIn" @submit.prevent="submitLedger">
    <Field label="方向">
      <el-select v-model="ledger.direction" @change="onDirection">
        <el-option label="支出" value="expense" />
        <el-option label="收入" value="income" />
      </el-select>
    </Field>
    <Field label="金额"><el-input v-model="ledger.amount" inputmode="decimal" /></Field>
    <Field label="分类">
      <el-select v-model="ledger.categoryId">
        <el-option v-for="item in matching" :key="item.id" :label="item.name" :value="item.id" />
      </el-select>
    </Field>
    <Field label="备注"><el-input v-model="ledger.note" /></Field>
    <el-button class="primary" type="primary" native-type="submit" round>记下</el-button>
  </form>
  <form v-if="panel === 'schedule'" class="composer stack tone-schedule animate__animated animate__fadeIn" @submit.prevent="submitSchedule">
    <div class="row">
      <Field label="事项"><el-input v-model="schedule.title" /></Field>
      <Field label="日期"><el-date-picker v-model="schedule.dueOn" type="date" value-format="YYYY-MM-DD" /></Field>
      <Field label="提醒"><el-date-picker v-model="schedule.remindAt" type="datetime" value-format="YYYY-MM-DDTHH:mm" /></Field>
      <Field label="备注"><el-input v-model="schedule.note" /></Field>
      <el-button class="primary" type="primary" native-type="submit" round>安排</el-button>
    </div>
    <p class="muted remind-hint">网页提醒仅在本页打开时有效；可靠提醒请用 App。</p>
  </form>
  <form v-if="panel === 'body'" class="composer row tone-weight animate__animated animate__fadeIn" @submit.prevent="submitBody">
    <Field label="体重 kg"><el-input v-model="body.weight" inputmode="decimal" /></Field>
    <Field label="摄入 kcal"><el-input v-model="body.intake" inputmode="numeric" /></Field>
    <el-button class="primary" type="primary" native-type="submit" round>记下</el-button>
  </form>
  <form v-if="panel === 'buy'" class="composer row tone-buy animate__animated animate__fadeIn" @submit.prevent="submitBuy">
    <Field label="想买"><el-input v-model="buy.title" /></Field>
    <Field label="估价"><el-input v-model="buy.amount" inputmode="decimal" /></Field>
    <el-button class="primary" type="primary" native-type="submit" round>放入清单</el-button>
  </form>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue';
import Field from '../../components/Field.vue';
import { bindDraft } from '../../utils';

const props = defineProps<{
  categories: { id: string; name: string; kind: 'income' | 'expense' }[];
  date: string;
  saveLedger: (payload: {
    direction: 'income' | 'expense';
    amount: string;
    categoryId: string;
    note: string;
  }) => Promise<void>;
  saveSchedule: (payload: {
    title: string;
    dueOn: string;
    remindAt: string;
    note: string;
  }) => Promise<void>;
  saveBody: (payload: { weight: string; intake: string }) => Promise<void>;
  saveBuy: (payload: { title: string; amount: string }) => Promise<void>;
}>();

const actions = [
  { id: 'ledger', title: '记一笔', hint: '支出或收入', mark: '¥' },
  { id: 'schedule', title: '排日程', hint: '待办与提醒', mark: '日' },
  { id: 'body', title: '记体重', hint: '减脂趋势', mark: 'kg' },
  { id: 'buy', title: '待买物品', hint: '采购清单', mark: '购' },
] as const;

const panel = ref('');
const ledger = reactive({ direction: 'expense' as 'income' | 'expense', amount: '', categoryId: '', note: '' });
const schedule = reactive({ title: '', dueOn: '', remindAt: '', note: '' });
const body = reactive({ weight: '', intake: '' });
const buy = reactive({ title: '', amount: '' });
bindDraft('zhaoxi-draft-today', { ledger, schedule, body, buy });

const matching = computed(() => props.categories.filter((item) => item.kind === ledger.direction));

watch(() => props.date, (date) => {
  if (date && !schedule.dueOn) schedule.dueOn = date;
}, { immediate: true });
watch(matching, (list) => {
  if (!list.find((item) => item.id === ledger.categoryId)) {
    ledger.categoryId = list[0]?.id || '';
  }
}, { immediate: true });

function toggle(next: string) {
  panel.value = panel.value === next ? '' : next;
}

function onDirection() {
  ledger.categoryId = matching.value[0]?.id || '';
}

async function submitLedger() {
  await props.saveLedger({ ...ledger });
  ledger.amount = '';
  ledger.note = '';
}
async function submitSchedule() {
  await props.saveSchedule({ ...schedule });
  schedule.title = '';
  schedule.remindAt = '';
  schedule.note = '';
}
async function submitBody() {
  await props.saveBody({ ...body });
  body.weight = '';
  body.intake = '';
}
async function submitBuy() {
  await props.saveBuy({ ...buy });
  buy.title = '';
  buy.amount = '';
}
</script>
