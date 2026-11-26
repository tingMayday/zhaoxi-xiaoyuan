<template>
  <aside class="rail">
    <div class="brand">
      <img class="brand-logo" :src="logoUrl" alt="朝夕小院" />
      <div>
        <strong>朝夕小院</strong>
        <span>{{ name }}</span>
      </div>
    </div>
    <nav>
      <template v-for="group in groups" :key="group.label || 'top'">
        <p v-if="group.label" class="group-label">{{ group.label }}</p>
        <router-link v-for="item in group.items" :key="item.to" :to="item.to">
          <NavIcon :name="item.icon" />
          <span>{{ item.label }}</span>
        </router-link>
      </template>
    </nav>
    <div class="rail-foot">
      <button type="button" class="exit" @click="$emit('exit')">
        <NavIcon name="exit" />
        <span>离开小院</span>
      </button>
    </div>
  </aside>
</template>

<script setup lang="ts">
import logoUrl from '../../assets/logo.png';
import NavIcon from './NavIcon.vue';

defineProps<{
  name: string;
  groups: { label: string; items: { to: string; label: string; icon: string }[] }[];
}>();
defineEmits<{ exit: [] }>();
</script>
