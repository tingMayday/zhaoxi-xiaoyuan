<template>
  <article class="habit tone-habit">
    <div class="page-head">
      <h2>{{ habit.name }}</h2>
      <el-button class="btn-danger" round @click="$emit('archive')">删除</el-button>
    </div>
    <Heatmap :days="days" :logs="habit.logs" :target="habit.dailyTarget" />
    <div class="stepper">
      <el-button
        v-if="habit.kind === 'check'"
        class="habit-check-btn"
        round
        :type="current >= habit.dailyTarget ? 'default' : 'primary'"
        :class="current >= habit.dailyTarget ? 'btn-soft is-done' : ''"
        @click="$emit('set', current >= habit.dailyTarget ? 0 : 1)"
      >
        {{ current >= habit.dailyTarget ? '已完成' : '打卡' }}
      </el-button>
      <template v-else-if="habit.kind === 'count'">
        <div class="habit-step">
          <button type="button" class="habit-step-btn" aria-label="减少" @click="$emit('set', Math.max(0, current - 1))">−</button>
          <strong class="habit-step-val">{{ current }}</strong>
          <button type="button" class="habit-step-btn" aria-label="增加" @click="$emit('set', current + 1)">+</button>
        </div>
        <span class="muted">/ {{ habit.dailyTarget }} {{ habit.unit }}</span>
      </template>
      <template v-else>
        <el-input v-model="draft" inputmode="decimal" @change="commit" @keyup.enter="commit" />
        <span class="muted">目标 {{ habit.dailyTarget }} {{ habit.unit }}</span>
      </template>
    </div>
  </article>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue';
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
const draft = ref('');

watch(
  () => [props.habit.id, props.today, current.value] as const,
  () => {
    draft.value = current.value ? String(current.value) : '';
  },
  { immediate: true },
);

function commit() {
  const next = Number(draft.value || 0);
  if (!Number.isFinite(next)) {
    draft.value = current.value ? String(current.value) : '';
    return;
  }
  if (next === current.value) return;
  emit('set', next);
}
</script>
