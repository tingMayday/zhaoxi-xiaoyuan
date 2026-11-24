<template>
  <template v-for="group in groups" :key="group.title">
    <h2>{{ group.title }} · {{ group.items.length }}</h2>
    <RecordList :items="rows(group.items)">
      <template #actions="{ item }">
        <el-button text @click="$emit('toggle', item.id)">{{ statusOf(group.items, item.id) }}</el-button>
        <el-button text @click="$emit('remove', item.id)">删除</el-button>
      </template>
    </RecordList>
    <Notice v-if="group.items.length === 0" :text="group.empty" kind="empty" />
  </template>
</template>

<script setup lang="ts">
import Notice from '../../components/Notice.vue';
import RecordList from '../../components/RecordList.vue';
import { scheduleClock, type ScheduleItem } from '../../http/apis/schedule';

defineProps<{ groups: { title: string; empty: string; items: ScheduleItem[] }[] }>();
defineEmits<{ toggle: [id: string]; remove: [id: string] }>();

function rows(items: ScheduleItem[]) {
  return items.map((item) => {
    const bits = [item.dueOn.slice(5)];
    const remind = scheduleClock(item.remindAt);
    if (remind) bits.push(`提醒 ${remind}`);
    return {
      id: item.id,
      title: item.title,
      meta: bits.join(' · '),
      note: item.note,
    };
  });
}

function statusOf(items: ScheduleItem[], id: string) {
  return items.find((item) => item.id === id)?.status === 'done' ? '已完成' : '完成';
}
</script>
