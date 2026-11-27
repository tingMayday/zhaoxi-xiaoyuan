<template>
  <section class="ledger-block tone-remind">
    <div class="page-head">
      <h2>预算</h2>
      <el-button class="btn-soft" round @click="openDialog">
        分类预算
        <em v-if="setCount" class="budget-chip-count">{{ setCount }}</em>
      </el-button>
    </div>

    <div class="budget-board" :class="[statusTone, { empty: !hasBudget }]">
      <div class="budget-total">
        <span class="budget-kicker">本月总额</span>
        <el-input
          class="budget-input"
          :model-value="modelValue"
          inputmode="decimal"
          placeholder="未设置"
          @update:model-value="$emit('update:modelValue', $event)"
          @change="$emit('save', 'total', $event)"
        />
        <div class="budget-status" :class="statusTone">
          <strong>{{ statusTitle }}</strong>
          <p>{{ statusDetail }}</p>
        </div>
      </div>

      <Chart class="budget-chart" :option="ringOption" height="148px" />

      <div class="budget-aside">
        <div class="budget-metric used">
          <span>已花</span>
          <div class="budget-metric-value">
            <strong>¥{{ yuan(expenseCents) }}</strong>
            <em>{{ hasBudget ? `${usedPercent}%` : '—' }}</em>
          </div>
        </div>
        <div class="budget-metric rest">
          <span>{{ isOver ? '超出' : '剩余' }}</span>
          <div class="budget-metric-value">
            <strong>¥{{ yuan(remainAbs) }}</strong>
            <em>{{ hasBudget ? `${remainPercent}%` : '—' }}</em>
          </div>
        </div>
      </div>
    </div>

    <el-dialog
      v-model="open"
      title="分类预算"
      width="min(520px, 94vw)"
      :close-on-click-modal="false"
      @closed="onClosed"
    >
      <p class="muted budget-dialog-hint">为各支出分类单独设限额；留空表示先不设。全部改完后点确认保存。</p>
      <div class="budget-grid">
        <Field v-for="item in categories" :key="item.id" :label="item.name">
          <el-input v-model="drafts[item.id]" inputmode="decimal" placeholder="未设置" />
        </Field>
      </div>
      <Notice v-if="categories.length === 0" text="还没有支出分类可设预算。" kind="empty" />
      <Notice :text="error" kind="error" />
      <template #footer>
        <el-button class="btn-soft" round :disabled="busy" @click="cancel">取消</el-button>
        <el-button type="primary" round :loading="busy" @click="confirm">确认</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue';
import type { EChartsCoreOption } from 'echarts/core';
import Chart from '../../components/Chart.vue';
import Field from '../../components/Field.vue';
import Notice from '../../components/Notice.vue';
import { yuan } from '../../utils';

const props = defineProps<{
  modelValue: string;
  categories: { id: string; name: string; budgetCents: number | null }[];
  expenseCents: number;
  usedRatio: number | null;
  remainingCents: number | null;
  hasBudget: boolean;
  saveCategories: (items: { key: string; raw: string }[]) => Promise<void>;
}>();
defineEmits<{
  'update:modelValue': [value: string];
  save: [key: string, raw: string];
}>();

const open = ref(false);
const busy = ref(false);
const error = ref('');
const drafts = reactive<Record<string, string>>({});

const usedPercent = computed(() => Math.round((props.usedRatio || 0) * 100));
const isOver = computed(() => props.hasBudget && (props.usedRatio || 0) >= 1);
const remainAbs = computed(() => Math.abs(props.remainingCents ?? 0));
const remainPercent = computed(() => {
  if (!props.hasBudget) return 0;
  if (isOver.value) return Math.max(usedPercent.value - 100, 0);
  return Math.max(100 - usedPercent.value, 0);
});
const statusTone = computed(() => {
  if (!props.hasBudget) return 'tone-empty';
  if (isOver.value) return 'tone-danger';
  if (usedPercent.value >= 80) return 'tone-watch';
  return 'tone-ok';
});
const ringColors = computed(() => {
  if (!props.hasBudget) return { used: '#cfc1ab', rest: '#ebe3d6' };
  if (isOver.value) return { used: '#6b4560', rest: '#e2d6c6' };
  if (usedPercent.value >= 80) return { used: '#c47a2c', rest: '#e6d9c4' };
  return { used: '#8a5a42', rest: '#d9ceb8' };
});
const statusTitle = computed(() => {
  if (!props.hasBudget) return '还没设预算';
  if (isOver.value) return '已经超支';
  if (usedPercent.value >= 80) return '接近上限';
  if (usedPercent.value >= 50) return '过半进行中';
  return '进度正常';
});
const statusDetail = computed(() => {
  if (!props.hasBudget) return '填好本月总额后，这里会提示花得稳不稳。';
  if (isOver.value) return `已超出 ¥${yuan(remainAbs.value)}，本月支出偏紧，留意一下大额开销。`;
  if (usedPercent.value >= 80) return `还剩 ¥${yuan(remainAbs.value)}（${remainPercent.value}%），后面几天可以收一收。`;
  if (usedPercent.value >= 50) return `已用一半多，还剩 ¥${yuan(remainAbs.value)} 可以支配。`;
  if (usedPercent.value === 0) return '这个月还没怎么动预算，慢慢记就好。';
  return `花得比较稳，还剩 ¥${yuan(remainAbs.value)}（${remainPercent.value}%）。`;
});
const setCount = computed(() => props.categories.filter((item) => item.budgetCents != null).length);

const ringOption = computed<EChartsCoreOption>(() => {
  const colors = ringColors.value;
  const used = props.hasBudget ? Math.min(Math.max(usedPercent.value, 0), 100) : 0;
  const rest = props.hasBudget ? Math.max(100 - used, 0) : 100;
  return {
    animationDuration: 400,
    series: [{
      type: 'pie',
      radius: ['62%', '88%'],
      center: ['50%', '50%'],
      silent: true,
      label: {
        show: true,
        position: 'center',
        formatter: props.hasBudget ? `{a|${usedPercent.value}%}\n{b|已用}` : '{a|—}\n{b|已用}',
        rich: {
          a: {
            fontFamily: 'Palatino Linotype, Palatino, Songti SC, SimSun, serif',
            fontSize: 28,
            fontWeight: 500,
            color: colors.used,
            lineHeight: 32,
          },
          b: {
            fontSize: 12,
            color: '#6d645a',
            lineHeight: 18,
            letterSpacing: 2,
          },
        },
      },
      labelLine: { show: false },
      data: [
        { name: '已用', value: used || 0.0001, itemStyle: { color: colors.used } },
        { name: '剩余', value: rest || 0.0001, itemStyle: { color: colors.rest } },
      ],
    }],
  };
});

function syncDrafts() {
  for (const key of Object.keys(drafts)) delete drafts[key];
  for (const item of props.categories) {
    drafts[item.id] = item.budgetCents == null ? '' : String(item.budgetCents / 100);
  }
}

function onClosed() {
  error.value = '';
  busy.value = false;
}

function openDialog() {
  syncDrafts();
  error.value = '';
  open.value = true;
}

function cancel() {
  open.value = false;
}

async function confirm() {
  error.value = '';
  const items = props.categories.map((item) => ({
    key: item.id,
    raw: (drafts[item.id] ?? '').trim(),
  }));
  busy.value = true;
  try {
    await props.saveCategories(items);
    open.value = false;
  } catch (err) {
    error.value = err instanceof Error ? err.message : '没有保存';
  } finally {
    busy.value = false;
  }
}
</script>
