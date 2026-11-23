<template>
  <form class="row compact" @submit.prevent="submit">
    <Field label="新习惯"><el-input v-model="form.name" /></Field>
    <Field label="方式">
      <el-select v-model="form.kind">
        <el-option label="勾选" value="check" />
        <el-option label="计数" value="count" />
        <el-option label="数值" value="value" />
      </el-select>
    </Field>
    <Field v-if="form.kind !== 'check'" label="每日目标"><el-input v-model="form.target" inputmode="decimal" /></Field>
    <Field v-if="form.kind !== 'check'" label="单位"><el-input v-model="form.unit" /></Field>
    <el-button class="primary" type="primary" native-type="submit" round>添加</el-button>
  </form>
</template>

<script setup lang="ts">
import { reactive } from 'vue';
import Field from '../../components/Field.vue';
import { bindDraft } from '../../utils';

const props = defineProps<{
  save: (payload: { name: string; kind: 'check' | 'count' | 'value'; target: string; unit: string }) => Promise<void>;
}>();
const form = reactive({ name: '', kind: 'check' as 'check' | 'count' | 'value', target: '1', unit: '' });
bindDraft('zhaoxi-draft-habit', form);

async function submit() {
  await props.save({ ...form });
  form.name = '';
}
</script>
