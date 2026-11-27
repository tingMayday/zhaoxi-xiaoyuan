<template>
  <div v-if="text" class="notice" :class="[kind, { pictured: showArt }]">
    <StateIllustration v-if="showArt" :kind="artKind" size="md" />
    <p>{{ text }}</p>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import StateIllustration, { type StateKind } from './StateIllustration.vue';

const props = withDefaults(defineProps<{
  text?: string;
  kind?: 'error' | 'empty' | 'muted';
  /** 空状态插图；默认 empty。传 false 可关闭。 */
  art?: StateKind | false;
}>(), {
  kind: undefined,
  art: undefined,
});

const showArt = computed(() => props.kind === 'empty' && props.art !== false);
const artKind = computed<StateKind>(() => (props.art === false || props.art == null ? 'empty' : props.art));
</script>
