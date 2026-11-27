<template>
  <div v-if="loading" class="page-status muted">正在打开…</div>
  <div v-else-if="error" class="page-status">
    <Notice :text="error" kind="error" />
    <el-button v-if="retry" round @click="retry">重试</el-button>
  </div>
</template>

<script setup lang="ts">
import Notice from './Notice.vue';

defineProps<{
  loading?: boolean;
  error?: string;
  retry?: () => void;
}>();
</script>
