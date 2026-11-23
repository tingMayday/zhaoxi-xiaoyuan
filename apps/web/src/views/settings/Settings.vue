<template>
  <section>
    <PageHeading eyebrow="设置" title="这间院子" />
    <form class="stack" @submit.prevent="save">
      <Field label="称呼"><el-input v-model="form.name" /></Field>
      <Field label="身高 cm"><el-input v-model="form.height" inputmode="decimal" /></Field>
      <Field label="时区"><el-input v-model="form.timezone" /></Field>
      <Notice :text="auth.user?.email" kind="muted" />
      <Notice :text="message" kind="muted" />
      <Notice :text="error" kind="error" />
      <el-button type="primary" native-type="submit" round>保存</el-button>
    </form>
  </section>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import Field from '../../components/Field.vue';
import { updateProfile } from '../../http/api';
import Notice from '../../components/Notice.vue';
import PageHeading from '../../components/PageHeading.vue';
import { useAuthStore } from '../../stores/auth';

const auth = useAuthStore();
const message = ref('');
const error = ref('');
const form = reactive({ name: '', height: '', timezone: 'Asia/Shanghai' });

onMounted(async () => {
  await auth.fetchMe();
  form.name = auth.user?.name || '';
  form.height = auth.user?.heightCm == null ? '' : String(auth.user.heightCm);
  form.timezone = auth.user?.timezone || 'Asia/Shanghai';
});

async function save() {
  error.value = '';
  message.value = '';
  try {
    auth.user = await updateProfile({
      name: form.name,
      timezone: form.timezone,
      heightCm: form.height.trim() ? Number(form.height) : undefined,
    });
    message.value = '已经记下。';
  } catch (err) {
    error.value = err instanceof Error ? err.message : '没有保存';
  }
}
</script>
