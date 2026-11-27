<template>
  <form class="row tone-buy" @submit.prevent="submit">
    <Field label="物品"><el-input v-model="form.title" /></Field>
    <Field label="数量"><el-input v-model="form.quantity" inputmode="decimal" /></Field>
    <Field label="估价"><el-input v-model="form.amount" inputmode="decimal" /></Field>
    <Field label="备注"><el-input v-model="form.note" /></Field>
    <el-button class="primary" type="primary" native-type="submit" round>加入</el-button>
  </form>
</template>

<script setup lang="ts">
import { reactive } from 'vue';
import Field from '../../components/Field.vue';
import { bindDraft } from '../../utils';

const props = defineProps<{
  save: (payload: { title: string; quantity: string; amount: string; note: string }) => Promise<void>;
}>();
const form = reactive({ title: '', quantity: '1', amount: '', note: '' });
bindDraft('zhaoxi-draft-buy', form);

async function submit() {
  await props.save({ ...form });
  form.title = '';
  form.amount = '';
  form.note = '';
}
</script>
