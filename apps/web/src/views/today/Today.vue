<template>
  <section>
    <p class="eyebrow">{{ today ? dateLabel(today.date) : '今天' }}</p>
    <h1>今天，慢慢来</h1>
    <p v-if="today" class="score"><strong>{{ today.score }}</strong><span> / 100</span></p>
    <p v-if="today" class="quote">{{ today.quote }}</p>

    <TodayQuick
      :categories="expenseCategories"
      :date="today?.date || ''"
      :save-expense="addExpense"
      :save-schedule="addSchedule"
      :save-body="addBody"
      :save-buy="addBuy"
    />
    <Notice :text="error" kind="error" />

    <StatGrid v-if="today" :items="stats" />
    <TodayCharts
      v-if="today"
      :breakdown="today.breakdown"
      :categories="summary?.categories || []"
      :points="series?.points || []"
    />
    <Notice v-else text="正在把今天铺开。" kind="muted" />

    <div v-if="today" class="today-board">
      <section class="panel">
        <div class="panel-head">
          <h2>待办清单</h2>
          <router-link to="/日程">查看全部</router-link>
        </div>
        <ul v-if="today.schedule.items.length">
          <li v-for="item in today.schedule.items" :key="item.id">
            <div>
              <strong>{{ item.title }}</strong>
              <span>{{ [item.note, item.time].filter(Boolean).join(' · ') || (item.status === 'done' ? '已完成' : '今天') }}</span>
            </div>
            <el-button text @click="finish(item)">{{ item.status === 'done' ? '已完成' : '完成' }}</el-button>
          </li>
        </ul>
        <p v-else class="empty">今天还空着。</p>
      </section>

      <section class="panel">
        <div class="panel-head">
          <h2>习惯打卡</h2>
          <router-link to="/习惯">管理</router-link>
        </div>
        <ul v-if="today.habits.items.length">
          <li v-for="habit in today.habits.items" :key="habit.id">
            <div>
              <strong>{{ habit.name }}</strong>
              <span>{{ habitText(habit) }}</span>
            </div>
            <el-button v-if="habit.kind === 'check'" text @click="check(habit)">
              {{ habit.value >= habit.dailyTarget ? '已完成' : '打卡' }}
            </el-button>
          </li>
        </ul>
        <p v-else class="empty">还没有习惯。</p>
      </section>

      <div class="today-split span">
        <section class="panel">
          <div class="panel-head">
            <h2>最近记录</h2>
            <router-link to="/时光档案">进入档案</router-link>
          </div>
          <ul v-if="today.recent.length">
            <li v-for="item in today.recent" :key="item.kind + item.id">
              <em>{{ item.kind }}</em>
              <div>
                <strong>{{ item.title }}</strong>
                <span>{{ recentWhen(item.date) }} · {{ item.detail }}</span>
              </div>
              <b>{{ item.value }}</b>
            </li>
          </ul>
          <p v-else class="empty">最近还没有记下什么。</p>
        </section>

        <section class="panel">
          <div class="panel-head"><h2>轻提醒</h2></div>
          <p v-for="line in reminders" :key="line">{{ line }}</p>
        </section>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { useRoute } from 'vue-router';
import Notice from '../../components/Notice.vue';
import StatGrid from '../../components/StatGrid.vue';
import { centsFromYuan, centsFromYuanAllowZero, dateLabel, yuan } from '../../utils';
import { createBuyItem, createSchedule, createTransaction, fetchSeries, fetchSummary, fetchToday, listCategories, saveBodyLog, saveHabitLog, scheduleLocalToIso, updateSchedule, type Category, type FinanceSummary, type FitnessSeries, type TodayHabit, type TodayOverview, type TodayTask } from '../../http/api';

import { confirmAction } from '../../utils';
import TodayCharts from './TodayCharts.vue';
import TodayQuick from './TodayQuick.vue';

const route = useRoute();
const today = ref<TodayOverview | null>(null);
const categories = ref<Category[]>([]);
const summary = ref<FinanceSummary | null>(null);
const series = ref<FitnessSeries | null>(null);
const error = ref('');

const expenseCategories = computed(() => categories.value.filter((item) => item.kind === 'expense'));
const stats = computed(() => {
  if (!today.value) return [];
  const remaining = today.value.finance.remainingCents;
  return [
    { label: '本月支出', value: `¥${yuan(today.value.finance.expenseCents)}`, hint: remaining == null ? '还没有月预算' : `预算还剩 ¥${yuan(remaining)}` },
    { label: '今日习惯', value: `${today.value.habits.done} / ${today.value.habits.total}`, hint: today.value.habits.total ? '从一件小事开始' : '还没有习惯' },
    { label: '今日待办', value: String(today.value.schedule.todayOpen), hint: `逾期 ${today.value.schedule.overdue} · 未来 7 天 ${today.value.schedule.next7}` },
    { label: '最近体重', value: today.value.fitness.weightKg == null ? '—' : `${today.value.fitness.weightKg} kg`, hint: today.value.fitness.weightDate || '还没有记录' },
  ];
});

function isTodayPath(path: string) {
  try {
    return decodeURIComponent(path) === '/今日';
  } catch {
    return path === '/今日';
  }
}

onMounted(refresh);
watch(
  () => route.path,
  (path, previous) => {
    if (previous && isTodayPath(path)) refresh();
  },
);

async function refresh() {
  const month = new Intl.DateTimeFormat('en-CA', { timeZone: 'Asia/Shanghai' }).format(new Date()).slice(0, 7);
  const [nextToday, nextCategories, nextSummary, nextSeries] = await Promise.all([
    fetchToday(),
    listCategories(),
    fetchSummary(month),
    fetchSeries(),
  ]);
  today.value = nextToday;
  categories.value = nextCategories;
  summary.value = nextSummary;
  series.value = nextSeries;
}

async function run(action: () => Promise<void>) {
  error.value = '';
  try {
    await action();
    await refresh();
  } catch (err) {
    error.value = err instanceof Error ? err.message : '没有记下';
    throw err;
  }
}

function addExpense(payload: { amount: string; categoryId: string; note: string }) {
  const amountCents = centsFromYuan(payload.amount);
  if (!amountCents || !today.value) {
    error.value = '请填写大于 0 的金额';
    return Promise.reject(new Error('请填写大于 0 的金额'));
  }
  return run(() => createTransaction({
    occurredOn: today.value!.date,
    direction: 'expense',
    amountCents,
    categoryId: payload.categoryId,
    note: payload.note,
  }));
}

function addSchedule(payload: { title: string; dueOn: string; remindAt: string; note: string }) {
  const remindAt = scheduleLocalToIso(payload.remindAt);
  if (payload.remindAt && !remindAt) {
    error.value = '提醒时间格式不正确';
    return Promise.reject(new Error(error.value));
  }
  return run(() => createSchedule({
    title: payload.title,
    dueOn: payload.dueOn,
    note: payload.note,
    remindAt,
    allDay: true,
  }));
}

function addBody(payload: { weight: string; intake: string }) {
  const weight = Number(payload.weight);
  if (!today.value || !Number.isFinite(weight)) {
    error.value = '请填写体重';
    return Promise.reject(new Error('请填写体重'));
  }
  const intake = payload.intake.trim() ? Number(payload.intake) : undefined;
  return run(() => saveBodyLog(today.value!.date, { weightKg: weight, intakeKcal: intake }));
}

const reminders = computed(() => {
  if (!today.value) return [];
  const lines: string[] = [];
  const { schedule, habits, finance, buy, media, breakdown } = today.value;
  if (schedule.overdue) lines.push(`有 ${schedule.overdue} 件事过了日子还没完成。`);
  if (habits.total && habits.done < habits.total) lines.push(`还有 ${habits.total - habits.done} 个习惯可以顺手做掉。`);
  if (finance.remainingCents != null && finance.remainingCents < 0) lines.push('这个月的预算已经用完了。');
  if (buy.openCount) lines.push(`清单里还有 ${buy.openCount} 件想买的，预计 ¥${yuan(buy.estimateCents)}。`);
  if (media.yearDone) lines.push(`今年看完 ${media.yearDone} 部${media.averageRating == null ? '' : `，平均 ${media.averageRating} 星`}。`);
  lines.push(`这 ${today.value.score} 分来自习惯 ${breakdown.habitPoints} 分、日程 ${breakdown.schedulePoints} 分、记录 ${breakdown.recordPoints} 分、逾期 ${breakdown.overduePoints} 分。`);
  if (lines.length === 1) lines.unshift('今天的事不多，留一点空白也好。');
  return lines;
});

function habitText(habit: TodayHabit) {
  if (habit.kind === 'check') return habit.value >= habit.dailyTarget ? '今天已完成' : '待打卡';
  return `${habit.value} / ${habit.dailyTarget}${habit.unit ? ` ${habit.unit}` : ''}`;
}

function recentWhen(date: string) {
  if (!today.value) return date;
  if (date === today.value.date) return '今天';
  const [, month, day] = date.split('-');
  return `${Number(month)} 月 ${Number(day)} 日`;
}

async function finish(item: TodayTask) {
  const done = item.status !== 'done';
  const ok = await confirmAction(
    done ? `把「${item.title}」标成完成？` : `把「${item.title}」改回未完成？`,
    done ? '完成日程' : '取消完成',
    done ? '完成' : '改回',
  );
  if (!ok) return;
  await run(() => updateSchedule(item.id, { status: done ? 'done' : 'open' }));
}

async function check(habit: TodayHabit) {
  if (!today.value) return;
  const done = habit.value < habit.dailyTarget;
  const ok = await confirmAction(
    done ? `把「${habit.name}」记成今天完成？` : `取消「${habit.name}」今天的完成？`,
    done ? '完成习惯' : '取消完成',
    done ? '完成' : '改回',
  );
  if (!ok) return;
  await run(() => saveHabitLog(habit.id, today.value!.date, done ? habit.dailyTarget : 0));
}

function addBuy(payload: { title: string; amount: string }) {
  const estimate = payload.amount.trim() ? centsFromYuanAllowZero(payload.amount) : 0;
  return run(() => createBuyItem({ title: payload.title, estimateCents: estimate ?? 0 }));
}
</script>
