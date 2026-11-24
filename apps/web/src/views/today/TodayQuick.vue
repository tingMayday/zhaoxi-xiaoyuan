<template>
  <nav class="quick">
    <el-button round :type="panel === 'ledger' ? 'primary' : 'default'" @click="toggle('ledger')">
      <span class="quick-label"><strong>记一笔</strong><small>支出或收入</small></span>
    </el-button>
    <el-button round :type="panel === 'schedule' ? 'primary' : 'default'" @click="toggle('schedule')">
      <span class="quick-label"><strong>排日程</strong><small>待办与提醒</small></span>
    </el-button>
    <el-button round :type="panel === 'body' ? 'primary' : 'default'" @click="toggle('body')">
      <span class="quick-label"><strong>记体重</strong><small>减脂趋势</small></span>
    </el-button>
    <el-button round :type="panel === 'buy' ? 'primary' : 'default'" @click="toggle('buy')">
      <span class="quick-label"><strong>待买物品</strong><small>采购清单</small></span>
    </el-button>
  </nav>

  <form v-if="panel === 'ledger'" class="composer row animate__animated animate__fadeIn" @submit.prevent="submitExpense">
    <Field label="金额"><el-input v-model="expense.amount" inputmode="decimal" /></Field>
    <Field label="分类">
      <el-select v-model="expense.categoryId">
        <el-option v-for="item in categories" :key="item.id" :label="item.name" :value="item.id" />
      </el-select>
    </Field>
    <Field label="备注"><el-input v-model="expense.note" /></Field>
    <el-button class="primary" type="primary" native-type="submit" round>记下</el-button>
  </form>
  <form v-if="panel === 'schedule'" class="composer row animate__animated animate__fadeIn" @submit.prevent="submitSchedule">
    <Field label="事项"><el-input v-model="schedule.title" /></Field>
    <Field label="日期"><el-date-picker v-model="schedule.dueOn" type="date" value-format="YYYY-MM-DD" /></Field>
    <Field label="提醒"><el-date-picker v-model="schedule.remindAt" type="datetime" value-format="YYYY-MM-DDTHH:mm" /></Field>
    <Field label="备注"><el-input v-model="schedule.note" /></Field>
    <el-button class="primary" type="primary" native-type="submit" round>安排</el-button>
  </form>
  <form v-if="panel === 'body'" class="composer row animate__animated animate__fadeIn" @submit.prevent="submitBody">
    <Field label="体重 kg"><el-input v-model="body.weight" inputmode="decimal" /></Field>
    <Field label="摄入 kcal"><el-input v-model="body.intake" inputmode="numeric" /></Field>
    <el-button class="primary" type="primary" native-type="submit" round>记下</el-button>
  </form>
  <form v-if="panel === 'buy'" class="composer row animate__animated animate__fadeIn" @submit.prevent="submitBuy">
    <Field label="想买"><el-input v-model="buy.title" /></Field>
    <Field label="估价"><el-input v-model="buy.amount" inputmode="decimal" /></Field>
    <el-button class="primary" type="primary" native-type="submit" round>放入清单</el-button>
  </form>
</template>

<script setup lang="ts">
import { reactive, ref, watch } from 'vue';
import Field from '../../components/Field.vue';
import { bindDraft } from '../../utils';

const props = defineProps<{
  categories: { id: string; name: string }[];
  date: string;
  saveExpense: (payload: { amount: string; categoryId: string; note: string }) => Promise<void>;
  saveSchedule: (payload: {
    title: string;
    dueOn: string;
    remindAt: string;
    note: string;
  }) => Promise<void>;
  saveBody: (payload: { weight: string; intake: string }) => Promise<void>;
  saveBuy: (payload: { title: string; amount: string }) => Promise<void>;
}>();

const panel = ref('');
const expense = reactive({ amount: '', categoryId: '', note: '' });
const schedule = reactive({ title: '', dueOn: '', remindAt: '', note: '' });
const body = reactive({ weight: '', intake: '' });
const buy = reactive({ title: '', amount: '' });
bindDraft('zhaoxi-draft-today', { expense, schedule, body, buy });



watch(() => props.date, (date) => {
  if (date && !schedule.dueOn) schedule.dueOn = date;
}, { immediate: true });
watch(() => props.categories, (list) => {
  if (!expense.categoryId && list[0]) expense.categoryId = list[0].id;
}, { immediate: true });

function toggle(next: string) {
  panel.value = panel.value === next ? '' : next;
}

async function submitExpense() {
  await props.saveExpense({ ...expense });
  expense.amount = '';
  expense.note = '';
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
