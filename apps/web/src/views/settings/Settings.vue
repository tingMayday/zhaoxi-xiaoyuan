<template>
  <section>
    <PageHeading eyebrow="设置" title="这间院子" />
    <PageStatus :loading="loading" :error="loadError" :retry="retry" />
    <form v-if="!loading && !loadError" class="stack tone-score" @submit.prevent="save">
      <Field label="称呼"><el-input v-model="form.name" /></Field>
      <Field label="身高 cm"><el-input v-model="form.height" inputmode="decimal" /></Field>
      <Field label="时区"><el-input v-model="form.timezone" placeholder="Asia/Shanghai" /></Field>
      <Notice :text="auth.user?.email" kind="muted" />
      <Notice :text="message" kind="muted" />
      <Notice :text="error" kind="error" />
      <el-button type="primary" native-type="submit" round>保存</el-button>
      <el-button class="exit-inline" round @click="exit">离开小院</el-button>
    </form>
  </section>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import Field from '../../components/Field.vue';
import Notice from '../../components/Notice.vue';
import PageHeading from '../../components/PageHeading.vue';
import PageStatus from '../../components/PageStatus.vue';
import { usePageLoad } from '../../composables/usePageLoad';
import { updateProfile } from '../../http/api';
import { useAuthStore } from '../../stores/auth';

const auth = useAuthStore();
const router = useRouter();
const message = ref('');
const error = ref('');
const form = reactive({ name: '', height: '', timezone: 'Asia/Shanghai' });

const { loading, loadError, retry } = usePageLoad(async () => {
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

function exit() {
  auth.logout();
  router.push('/login');
}
</script>
