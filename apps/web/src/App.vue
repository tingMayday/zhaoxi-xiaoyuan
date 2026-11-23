<template>
  <router-view v-slot="{ Component, route }">
    <transition mode="out-in" enter-active-class="animate__animated animate__fadeIn animate__faster">
      <component :is="Component" :key="route.matched[0]?.path || route.path" />
    </transition>
  </router-view>
</template>
