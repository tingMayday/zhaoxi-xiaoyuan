<template>
  <div v-if="view === 'wall'" class="wall">
    <article v-for="item in items" :key="item.id" class="media-card" @click="$emit('edit', item.id)">
      <img class="cover" :src="coverOf(item.kind, item.coverUrl)" :alt="item.title" />
      <h2>{{ item.title }}</h2>
      <p class="muted">{{ kindLabel[item.kind] }} · {{ statusLabel[item.status] }} · {{ item.rating ? `${item.rating} 星` : '未评分' }}</p>
      <p v-if="item.note">{{ item.note }}</p>
      <el-button class="btn-danger" round @click.stop="$emit('remove', item.id)">删除</el-button>
    </article>
  </div>
  <ul v-else class="list">
    <li v-for="item in items" :key="item.id" class="media-line" @click="$emit('edit', item.id)">
      <img class="thumb" :src="coverOf(item.kind, item.coverUrl)" alt="" />
      <span>
        <strong>{{ item.title }}</strong>
        <em class="muted">{{ intro(item) }}</em>
      </span>
      <span class="media-line-meta">
        <em>{{ statusLabel[item.status] }} {{ item.rating || '—' }}</em>
        <el-button class="btn-danger" round @click.stop="$emit('remove', item.id)">删除</el-button>
      </span>
    </li>
  </ul>
</template>

<script setup lang="ts">
import { coverOf, kindLabel, statusLabel, type Kind, type Status } from './labels';

defineProps<{
  view: 'wall' | 'list';
  items: { id: string; kind: Kind; title: string; creator: string; coverUrl: string; status: Status; rating: number | null; note: string }[];
}>();
defineEmits<{ remove: [id: string]; edit: [id: string] }>();

function intro(item: { creator: string; note: string }) {
  const parts = [item.creator, item.note].map((text) => text.trim()).filter(Boolean);
  return parts.length ? parts.join(' · ') : '还没有介绍';
}
</script>
