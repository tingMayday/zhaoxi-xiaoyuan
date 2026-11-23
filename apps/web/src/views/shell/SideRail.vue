<template>
  <aside class="rail">
    <div class="brand">
      <i aria-hidden="true">院</i>
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
    <el-button class="exit" text @click="$emit('exit')">离开</el-button>
  </aside>
</template>

<script setup lang="ts">
import NavIcon from './NavIcon.vue';

defineProps<{
  name: string;
  groups: { label: string; items: { to: string; label: string; icon: string }[] }[];
}>();
defineEmits<{ exit: [] }>();
</script>
