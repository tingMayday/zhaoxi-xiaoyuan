<template>
  <form class="row" @submit.prevent="submit">
    <Field label="事项"><el-input v-model="form.title" /></Field>
    <Field label="日期"><el-date-picker v-model="form.dueOn" type="date" value-format="YYYY-MM-DD" /></Field>
    <Field label="提醒"><el-date-picker v-model="form.remindAt" type="datetime" value-format="YYYY-MM-DDTHH:mm" /></Field>
    <Field label="备注"><el-input v-model="form.note" /></Field>
    <el-button class="primary" type="primary" native-type="submit" round>安排</el-button>
  </form>
</template>

<script setup lang="ts">
import { reactive, watch } from 'vue';
import Field from '../../components/Field.vue';
import { bindDraft } from '../../utils';

export type ScheduleFormPayload = {
  title: string;
  dueOn: string;
  remindAt: string;
  note: string;
};

const props = defineProps<{
  today: string;
  save: (payload: ScheduleFormPayload) => Promise<void>;
}>();

const form = reactive({ title: '', dueOn: '', remindAt: '', note: '' });
bindDraft('zhaoxi-draft-schedule', form);

watch(() => props.today, (today) => {
  if (today && !form.dueOn) form.dueOn = today;
}, { immediate: true });

async function submit() {
  await props.save({ ...form });
  form.title = '';
  form.note = '';
  form.remindAt = '';
}
</script>
