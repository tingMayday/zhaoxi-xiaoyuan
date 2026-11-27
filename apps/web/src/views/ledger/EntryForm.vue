<template>
  <div class="entry-compose" :class="form.direction">
    <div class="entry-dir">
      <div class="dir-toggle" role="group" aria-label="方向">
        <button type="button" :class="{ on: form.direction === 'expense' }" @click="setDirection('expense')">支出</button>
        <button type="button" :class="{ on: form.direction === 'income' }" @click="setDirection('income')">收入</button>
      </div>
    </div>

    <form class="entry-form" @submit.prevent="submit">
      <Field label="日期"><el-date-picker v-model="form.occurredOn" type="date" value-format="YYYY-MM-DD" /></Field>
      <Field label="金额"><el-input v-model="form.amount" inputmode="decimal" placeholder="0.00" /></Field>
      <Field label="分类">
        <el-select v-model="form.categoryId" placeholder="选择分类">
          <el-option v-for="item in matching" :key="item.id" :label="item.name" :value="item.id" />
        </el-select>
      </Field>
      <Field label="备注" tone="note"><el-input v-model="form.note" placeholder="可选" /></Field>
      <el-button class="primary" type="primary" native-type="submit" round :loading="busy">记下</el-button>
    </form>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue';
import Field from '../../components/Field.vue';
import { bindDraft, zonedToday } from '../../utils';

const props = defineProps<{
  month: string;
  categories: { id: string; name: string; kind: 'income' | 'expense' }[];
  save: (payload: { occurredOn: string; direction: 'income' | 'expense'; amount: string; categoryId: string; note: string }) => Promise<void>;
}>();

const busy = ref(false);
const form = reactive({ occurredOn: '', direction: 'expense' as 'income' | 'expense', amount: '', categoryId: '', note: '' });
bindDraft('zhaoxi-draft-ledger', form);
const matching = computed(() => props.categories.filter((item) => item.kind === form.direction));

watch([() => props.month, matching], () => {
  const today = zonedToday();
  if (!form.occurredOn) form.occurredOn = today.startsWith(props.month) ? today : `${props.month}-01`;
  if (!matching.value.some((item) => item.id === form.categoryId)) {
    form.categoryId = matching.value[0]?.id || '';
  }
}, { immediate: true });

function setDirection(next: 'income' | 'expense') {
  if (form.direction === next) return;
  form.direction = next;
  form.categoryId = props.categories.find((item) => item.kind === next)?.id || '';
}

async function submit() {
  busy.value = true;
  try {
    await props.save({ ...form });
    form.amount = '';
    form.note = '';
  } finally {
    busy.value = false;
  }
}
</script>
