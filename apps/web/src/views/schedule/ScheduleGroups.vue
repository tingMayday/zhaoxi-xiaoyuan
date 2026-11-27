<template>
  <section
    v-for="group in groups"
    :key="group.title"
    class="surface has-corner"
    :class="groupTone(group.title)"
  >
    <span class="panel-corner" :aria-label="group.items.length + ' 项'">{{ group.items.length }}</span>
    <div class="panel-head">
      <h2>{{ group.title }}</h2>
    </div>
    <RecordList :items="rows(group.items)" :highlight-id="highlightId">
      <template #actions="{ item }">
        <el-button class="btn-soft" round @click="$emit('edit', item.id)">编辑</el-button>
        <el-button type="primary" round @click="$emit('toggle', item.id)">{{ statusOf(group.items, item.id) }}</el-button>
        <el-button class="btn-danger" round @click="$emit('remove', item.id)">删除</el-button>
      </template>
    </RecordList>
    <Notice v-if="group.items.length === 0" :text="group.empty" kind="empty" :art="group.art || 'empty'" />
  </section>
</template>

<script setup lang="ts">
import Notice from '../../components/Notice.vue';
import RecordList from '../../components/RecordList.vue';
import type { StateKind } from '../../components/StateIllustration.vue';
import { scheduleClock, type ScheduleItem } from '../../http/apis/schedule';

defineProps<{
  groups: { title: string; empty: string; art?: StateKind; items: ScheduleItem[] }[];
  highlightId?: string | null;
}>();
defineEmits<{ toggle: [id: string]; remove: [id: string]; edit: [id: string] }>();

function groupTone(title: string) {
  if (title.includes('逾期')) return 'tone-spend';
  if (title.includes('今天')) return 'tone-schedule';
  if (title.includes('未来') || title.includes('稍后') || title.includes('之后')) return 'tone-remind';
  if (title.includes('完成')) return 'tone-habit';
  return 'tone-recent';
}

function rows(items: ScheduleItem[]) {
  return items.map((item) => {
    const bits = [item.dueOn.slice(5)];
    if (!item.allDay && item.startsAt) {
      const start = scheduleClock(item.startsAt);
      const end = scheduleClock(item.endsAt);
      bits.push(end ? `${start}-${end}` : start);
    } else {
      bits.push('全天');
    }
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
