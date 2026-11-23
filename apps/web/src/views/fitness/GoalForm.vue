<template>
  <h2>目标</h2>
  <form class="row" @submit.prevent="submit">
    <Field label="起点 kg"><el-input v-model="profile.start" inputmode="decimal" /></Field>
    <Field label="目标 kg"><el-input v-model="profile.target" inputmode="decimal" /></Field>
    <Field label="开始日期"><el-date-picker v-model="profile.startDate" type="date" value-format="YYYY-MM-DD" /></Field>
    <Field label="目标摄入"><el-input v-model="profile.intake" inputmode="numeric" /></Field>
    <el-button class="primary" type="primary" native-type="submit" round>保存目标</el-button>
  </form>
</template>

<script setup lang="ts">
import { reactive, watch } from 'vue';
import Field from '../../components/Field.vue';
import { bindDraft } from '../../utils';

const props = defineProps<{
  saved: { start: string; target: string; startDate: string; intake: string } | null;
  today: string;
  save: (payload: { start: string; target: string; startDate: string; intake: string }) => Promise<void>;
}>();
const profile = reactive({ start: '', target: '', startDate: '', intake: '1800' });
bindDraft('zhaoxi-draft-fitness-goal', profile);

watch(() => props.saved, (saved) => {
  if (!saved) return;
  profile.start = saved.start;
  profile.target = saved.target;
  profile.startDate = saved.startDate;
  profile.intake = saved.intake;
}, { immediate: true });
watch(() => props.today, (today) => {
  if (today && !profile.startDate) profile.startDate = today;
}, { immediate: true });

function submit() {
  return props.save({ ...profile });
}
</script>
