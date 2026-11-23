<template>
  <h2>一周安排</h2>
  <form class="week" @submit.prevent="$emit('save')">
    <div v-for="(item, index) in week" :key="index" class="plan-row">
      <span>周{{ '一二三四五六日'[index] }}</span>
      <el-input v-model="item.title" placeholder="安排" />
      <el-input v-model="item.detail" placeholder="备注" />
    </div>
    <el-button class="primary" type="primary" native-type="submit" round>保存一周</el-button>
  </form>
</template>

<script setup lang="ts">
defineProps<{ week: { title: string; detail: string }[] }>();
defineEmits<{ save: [] }>();
</script>
