<template>
  <section class="login">
    <form @submit.prevent="submit">
      <div class="login-brand">
        <img class="login-logo" :src="logoUrl" alt="" />
        <p class="eyebrow">朝夕小院</p>
      </div>
      <h1>{{ mode === 'login' ? '回来就好' : '为自己留一间院子' }}</h1>
      <p class="muted">记收支、习惯、身体、日程、待买和收藏。</p>
      <Field v-if="mode === 'register'" label="怎么称呼"><el-input v-model="name" maxlength="40" /></Field>
      <Field label="邮箱"><el-input v-model="email" type="email" autocomplete="username" /></Field>
      <Field label="密码"><el-input v-model="password" type="password" show-password autocomplete="current-password" /></Field>
      <Notice :text="error" kind="error" />
      <el-button type="primary" native-type="submit" round>{{ mode === 'login' ? '进入小院' : '建立小院' }}</el-button>
      <el-button text native-type="button" @click="toggle">
        {{ mode === 'login' ? '第一次来，注册' : '已有账号，登录' }}
      </el-button>
    </form>
  </section>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import logoUrl from '../../assets/logo.png';
import Field from '../../components/Field.vue';
import Notice from '../../components/Notice.vue';
import { useAuthStore } from '../../stores/auth';

const auth = useAuthStore();
const router = useRouter();
const mode = ref<'login' | 'register'>('login');
const email = ref('demo@zhaoxi.local');
const password = ref('demo1234');
const name = ref('');
const error = ref('');

function toggle() {
  mode.value = mode.value === 'login' ? 'register' : 'login';
  error.value = '';
}

async function submit() {
  error.value = '';
  try {
    if (mode.value === 'login') await auth.login(email.value, password.value);
    else await auth.register(email.value, password.value, name.value);
    router.push('/今日');
  } catch (err) {
    error.value = err instanceof Error ? err.message : '没有完成';
  }
}
</script>
