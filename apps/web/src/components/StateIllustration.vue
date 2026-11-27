<template>
  <img class="state-art" :src="src" :alt="alt" :class="size" />
</template>

<script setup lang="ts">
import { computed } from 'vue';
import remind from '../assets/state-01-remind.png';
import empty from '../assets/state-02-empty.png';
import done from '../assets/state-03-done.png';
import night from '../assets/state-04-night.png';
import rest from '../assets/state-05-rest.png';

export type StateKind = 'remind' | 'empty' | 'done' | 'night' | 'rest';

const props = withDefaults(defineProps<{
  kind?: StateKind;
  size?: 'sm' | 'md' | 'lg';
}>(), {
  kind: 'empty',
  size: 'md',
});

const catalog: Record<StateKind, { src: string; alt: string }> = {
  remind: { src: remind, alt: '轻轻提醒' },
  empty: { src: empty, alt: '还没有内容' },
  done: { src: done, alt: '完成打卡' },
  night: { src: night, alt: '夜间安抚' },
  rest: { src: rest, alt: '可以休息' },
};

const src = computed(() => catalog[props.kind].src);
const alt = computed(() => catalog[props.kind].alt);
</script>
