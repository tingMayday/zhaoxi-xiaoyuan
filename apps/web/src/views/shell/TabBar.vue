<template>
  <nav class="tabbar">
    <router-link v-for="item in items" :key="item.to" :to="item.to">
      <NavIcon :name="item.icon" />
      <span>{{ item.label }}</span>
    </router-link>
  </nav>
</template>

<script setup lang="ts">
import NavIcon from './NavIcon.vue';

const items = [
  { to: '/今日', label: '今日', icon: 'home' },
  { to: '/记账', label: '记账', icon: 'ledger' },
  { to: '/习惯', label: '习惯', icon: 'habit' },
  { to: '/健身', label: '健身', icon: 'fitness' },
  { to: '/更多', label: '更多', icon: 'more' },
];
</script>
