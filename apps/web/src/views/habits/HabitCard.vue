<template>
  <article class="habit">
    <div class="page-head">
      <h2>{{ habit.name }}</h2>
      <el-button text @click="$emit('archive')">删除</el-button>
    </div>
    <Heatmap :days="days" :logs="habit.logs" :target="habit.dailyTarget" />
    <div class="stepper">
      <el-button
        v-if="habit.kind === 'check'"
        round
        :type="current >= habit.dailyTarget ? 'primary' : 'default'"
        @click="$emit('set', current >= habit.dailyTarget ? 0 : 1)"
      >
        {{ current >= habit.dailyTarget ? '已完成' : '打卡' }}
      </el-button>
      <template v-else-if="habit.kind === 'count'">
        <el-button circle @click="$emit('set', Math.max(0, current - 1))">－</el-button>
        <strong>{{ current }}</strong>
        <span class="muted">/ {{ habit.dailyTarget }} {{ habit.unit }}</span>
        <el-button circle @click="$emit('set', current + 1)">＋</el-button>
      </template>
      <template v-else>
        <el-input :model-value="current ? String(current) : ''" inputmode="decimal" @change="onValue" />
        <span class="muted">目标 {{ habit.dailyTarget }} {{ habit.unit }}</span>
      </template>
    </div>
  </article>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import Heatmap from './Heatmap.vue';

const props = defineProps<{
  habit: {
    id: string;
    name: string;
    kind: 'check' | 'count' | 'value';
    unit: string;
    dailyTarget: number;
    logs: { date: string; value: number }[];
  };
  today: string;
  days: string[];
}>();

const emit = defineEmits<{ set: [value: number]; archive: [] }>();
const current = computed(() => props.habit.logs.find((item) => item.date === props.today)?.value || 0);

function onValue(value: string) {
  emit('set', Number(value || 0));
}
</script>
