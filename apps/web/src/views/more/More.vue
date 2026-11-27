<template>
  <section>
    <PageHeading eyebrow="更多" title="院里其余的事" />
    <nav class="more-links">
      <router-link to="/日程">日程</router-link>
      <router-link to="/待买">待买</router-link>
      <router-link to="/收藏">收藏</router-link>
      <router-link to="/时光档案">时光档案</router-link>
      <router-link to="/设置">设置</router-link>
    </nav>
    <button type="button" class="exit more-exit" @click="exit">离开小院</button>
  </section>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router';
import PageHeading from '../../components/PageHeading.vue';
import { useAuthStore } from '../../stores/auth';

const auth = useAuthStore();
const router = useRouter();

function exit() {
  auth.logout();
  router.push('/login');
}
</script>
