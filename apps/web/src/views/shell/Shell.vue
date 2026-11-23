<template>
  <div class="shell">
    <SideRail :name="auth.user?.name || '个人日常'" :groups="groups" @exit="exit" />
    <main class="page">
      <router-view v-slot="{ Component, route }">
        <transition mode="out-in" enter-active-class="animate__animated animate__fadeInUp animate__faster">
          <component :is="Component" :key="route.path" />
        </transition>
      </router-view>
    </main>
    <TabBar />
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '../../stores/auth';
import SideRail from './SideRail.vue';
import TabBar from './TabBar.vue';

const auth = useAuthStore();
const router = useRouter();
const groups = [
  { label: '', items: [{ to: '/今日', label: '今日总览', icon: 'home' }] },
  {
    label: '生活模块',
    items: [
      { to: '/记账', label: '记账理财', icon: 'ledger' },
      { to: '/习惯', label: '习惯健康', icon: 'habit' },
      { to: '/健身', label: '减脂健身', icon: 'fitness' },
      { to: '/日程', label: '日程统筹', icon: 'schedule' },
      { to: '/待买', label: '待买清单', icon: 'buy' },
      { to: '/收藏', label: '书影音', icon: 'media' },
    ],
  },
  {
    label: '数据',
    items: [
      { to: '/时光档案', label: '时光档案', icon: 'archive' },
      { to: '/设置', label: '设置', icon: 'settings' },
    ],
  },
];

onMounted(() => {
  auth.fetchMe().catch(() => undefined);
});

function exit() {
  auth.logout();
  router.push('/login');
}
</script>
