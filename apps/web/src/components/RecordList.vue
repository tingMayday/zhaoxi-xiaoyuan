<template>
  <ul v-if="items.length" class="list">
    <li v-for="item in items" :key="item.id">
      <span class="entry">
        <span>{{ item.title }} <em v-if="item.meta" class="muted">{{ item.meta }}</em></span>
        <em v-if="item.note" class="muted">{{ item.note }}</em>
      </span>
      <strong v-if="item.amount">{{ item.amount }}</strong>
      <slot name="actions" :item="item" />
    </li>
  </ul>
</template>

<script setup lang="ts">
export interface RecordItem {
  id: string;
  title: string;
  meta?: string;
  note?: string;
  amount?: string;
}

defineProps<{ items: RecordItem[] }>();
</script>
