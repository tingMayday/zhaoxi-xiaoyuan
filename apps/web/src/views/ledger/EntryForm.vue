<template>
  <form class="entry-form" @submit.prevent="submit">
    <Field label="日期"><el-date-picker v-model="form.occurredOn" type="date" value-format="YYYY-MM-DD" /></Field>
    <Field label="方向">
      <el-select v-model="form.direction">
        <el-option label="支出" value="expense" />
        <el-option label="收入" value="income" />
      </el-select>
    </Field>
    <Field label="金额"><el-input v-model="form.amount" inputmode="decimal" /></Field>
    <Field label="分类">
      <el-select v-model="form.categoryId">
        <el-option v-for="item in matching" :key="item.id" :label="item.name" :value="item.id" />
      </el-select>
    </Field>
    <Field label="备注" tone="note"><el-input v-model="form.note" /></Field>
    <el-button class="primary" type="primary" native-type="submit" round>记下</el-button>
  </form>
</template>

<script setup lang="ts">
import { computed, reactive, watch } from 'vue';
import Field from '../../components/Field.vue';
import { bindDraft } from '../../utils';

const props = defineProps<{
  month: string;
  categories: { id: string; name: string; kind: 'income' | 'expense' }[];
  save: (payload: { occurredOn: string; direction: 'income' | 'expense'; amount: string; categoryId: string; note: string }) => Promise<void>;
}>();

const form = reactive({ occurredOn: '', direction: 'expense' as 'income' | 'expense', amount: '', categoryId: '', note: '' });
bindDraft('zhaoxi-draft-ledger', form);
const matching = computed(() => props.categories.filter((item) => item.kind === form.direction));

watch([() => props.month, matching], () => {
  const today = new Intl.DateTimeFormat('en-CA', { timeZone: 'Asia/Shanghai' }).format(new Date());
  if (!form.occurredOn) form.occurredOn = today.startsWith(props.month) ? today : `${props.month}-01`;
  if (!form.categoryId && matching.value[0]) form.categoryId = matching.value[0].id;
}, { immediate: true });

async function submit() {
  await props.save({ ...form });
  form.amount = '';
  form.note = '';
}
</script>
