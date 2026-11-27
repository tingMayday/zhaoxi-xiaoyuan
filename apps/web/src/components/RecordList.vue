<template>
  <ul v-if="items.length" class="list">
    <li
      v-for="item in items"
      :key="item.id"
      :id="item.id === highlightId ? 'schedule-focus' : undefined"
      :class="{ highlight: highlightId && item.id === highlightId }"
    >
      <span class="entry">
        <span>{{ item.title }} <em v-if="item.meta" class="muted">{{ item.meta }}</em></span>
        <em v-if="item.note" class="muted">{{ item.note }}</em>
      </span>
      <strong v-if="item.amount" :class="item.tone === 'in' ? 'amount-in' : item.tone === 'out' ? 'amount-out' : undefined">{{ item.amount }}</strong>
      <span class="list-actions">
        <slot name="actions" :item="item" />
      </span>
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
  tone?: 'in' | 'out';
}

defineProps<{ items: RecordItem[]; highlightId?: string | null }>();
</script>
