<template>
  <h2>今天</h2>
  <form class="row" @submit.prevent="submit">
    <Field label="体重 kg"><el-input v-model="log.weight" inputmode="decimal" /></Field>
    <Field label="体脂 %"><el-input v-model="log.fat" inputmode="decimal" /></Field>
    <Field label="摄入 kcal"><el-input v-model="log.intake" inputmode="numeric" /></Field>
    <Field label="运动分钟"><el-input v-model="log.minutes" inputmode="numeric" /></Field>
    <el-button class="primary" type="primary" native-type="submit" round>记下</el-button>
  </form>
</template>

<script setup lang="ts">
import { reactive } from 'vue';
import Field from '../../components/Field.vue';
import { bindDraft } from '../../utils';

const props = defineProps<{
  save: (payload: { weight: string; fat: string; intake: string; minutes: string }) => Promise<void>;
}>();
const log = reactive({ weight: '', fat: '', intake: '', minutes: '' });
bindDraft('zhaoxi-draft-fitness-log', log);

async function submit() {
  await props.save({ ...log });
  log.weight = '';
  log.fat = '';
  log.intake = '';
  log.minutes = '';
}
</script>
