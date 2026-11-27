<template>
  <section>
    <PageHeading eyebrow="日程" title="今天要做的" />
    <PageStatus :loading="loading" :error="loadError" :retry="retry" />
    <template v-if="!loading && !loadError">
      <ScheduleForm :today="data?.today || ''" :save="create" />
      <Notice :text="error" kind="error" />
      <ScheduleGroups
        :groups="groups"
        :highlight-id="highlightId"
        @toggle="toggle"
        @remove="remove"
        @edit="openEdit"
      />
    </template>

    <el-dialog v-model="editOpen" title="编辑日程" width="min(480px, 92vw)" @closed="editing = null">
      <form v-if="editing" class="stack" @submit.prevent="saveEdit">
        <Field label="事项"><el-input v-model="editing.title" /></Field>
        <Field label="日期"><el-date-picker v-model="editing.dueOn" type="date" value-format="YYYY-MM-DD" /></Field>
        <Field label="类型">
          <el-select v-model="editing.allDay">
            <el-option label="全天" :value="true" />
            <el-option label="时段" :value="false" />
          </el-select>
        </Field>
        <Field v-if="!editing.allDay" label="开始">
          <el-time-picker v-model="editing.startsAt" format="HH:mm" value-format="HH:mm" />
        </Field>
        <Field v-if="!editing.allDay" label="结束">
          <el-time-picker v-model="editing.endsAt" format="HH:mm" value-format="HH:mm" />
        </Field>
        <Field label="提醒"><el-date-picker v-model="editing.remindAt" type="datetime" value-format="YYYY-MM-DDTHH:mm" /></Field>
        <Field label="备注"><el-input v-model="editing.note" /></Field>
        <Notice :text="editError" kind="error" />
        <el-button type="primary" native-type="submit" round>保存</el-button>
      </form>
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import { computed, nextTick, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import Field from '../../components/Field.vue';
import Notice from '../../components/Notice.vue';
import PageHeading from '../../components/PageHeading.vue';
import PageStatus from '../../components/PageStatus.vue';
import { usePageLoad } from '../../composables/usePageLoad';
import {
  createSchedule,
  fetchSchedule,
  removeSchedule,
  scheduleClock,
  scheduleDateTimeLocal,
  scheduleIsoToLocal,
  scheduleLocalToIso,
  updateSchedule,
  type ScheduleBoard,
} from '../../http/api';
import { confirmAction } from '../../utils';
import { armLocalReminder } from '../../utils/notify';
import ScheduleForm, { type ScheduleFormPayload } from './ScheduleForm.vue';
import ScheduleGroups from './ScheduleGroups.vue';

const route = useRoute();
const router = useRouter();
const data = ref<ScheduleBoard | null>(null);
const error = ref('');
const editOpen = ref(false);
const editError = ref('');
const highlightId = ref<string | null>(null);
const editing = ref<{
  id: string;
  title: string;
  dueOn: string;
  allDay: boolean;
  startsAt: string;
  endsAt: string;
  remindAt: string;
  note: string;
} | null>(null);

const groups = computed(() => {
  const today = data.value?.today || '';
  const items = data.value?.items || [];
  const upcomingEnd = shift(today, 7);
  const open = items.filter((item) => item.status !== 'done');
  const done = items
    .filter((item) => item.status === 'done')
    .slice()
    .sort((a, b) => b.dueOn.localeCompare(a.dueOn) || a.title.localeCompare(b.title));
  return [
    { title: '已逾期', empty: '没有拖着的事。', art: 'rest' as const, items: data.value?.overdueItems || [] },
    { title: '今天', empty: '今天还空着。', art: 'empty' as const, items: open.filter((item) => item.dueOn === today) },
    { title: '未来 7 天', empty: '接下来一周是轻的。', art: 'rest' as const, items: open.filter((item) => item.dueOn > today && item.dueOn <= upcomingEnd) },
    { title: '已完成', empty: '完成的事项会出现在这里。', art: 'rest' as const, items: done },
  ];
});

const { loading, loadError, load, retry } = usePageLoad(async () => {
  data.value = await fetchSchedule();
});

watch(
  () => [route.query.id, loading.value, data.value] as const,
  async ([id, isLoading]) => {
    const focus = typeof id === 'string' ? id : Array.isArray(id) ? id[0] : '';
    highlightId.value = focus || null;
    if (!focus || isLoading || !data.value) return;
    await nextTick();
    document.getElementById('schedule-focus')?.scrollIntoView({ behavior: 'smooth', block: 'center' });
  },
  { immediate: true },
);

function shift(iso: string, days: number) {
  if (!iso) return '';
  const [y, m, d] = iso.split('-').map(Number);
  const date = new Date(Date.UTC(y, m - 1, d));
  date.setUTCDate(date.getUTCDate() + days);
  return date.toISOString().slice(0, 10);
}

function findItem(id: string) {
  return [...(data.value?.overdueItems || []), ...(data.value?.items || [])].find((row) => row.id === id);
}

function armRemind(title: string, remindAt?: string) {
  if (remindAt) armLocalReminder(title, remindAt);
}

function clockToHm(iso?: string | null) {
  const text = scheduleClock(iso);
  return text || '';
}

async function create(payload: ScheduleFormPayload) {
  error.value = '';
  const remindAt = scheduleLocalToIso(payload.remindAt);
  if (payload.remindAt && !remindAt) {
    error.value = '提醒时间格式不正确';
    throw new Error(error.value);
  }
  const startsAt = payload.allDay ? undefined : scheduleDateTimeLocal(payload.dueOn, payload.startsAt);
  const endsAt = payload.allDay ? undefined : scheduleDateTimeLocal(payload.dueOn, payload.endsAt);
  if (!payload.allDay && (!startsAt || !endsAt)) {
    error.value = '请填写开始和结束时间';
    throw new Error(error.value);
  }
  try {
    await createSchedule({
      title: payload.title,
      dueOn: payload.dueOn,
      note: payload.note,
      remindAt,
      allDay: payload.allDay,
      startsAt,
      endsAt,
    });
    armRemind(payload.title, remindAt);
    await load();
  } catch (err) {
    error.value = err instanceof Error ? err.message : '没有安排上';
    throw err;
  }
}

function openEdit(id: string) {
  const item = findItem(id);
  if (!item) return;
  editing.value = {
    id: item.id,
    title: item.title,
    dueOn: item.dueOn,
    allDay: item.allDay !== false && !item.startsAt,
    startsAt: clockToHm(item.startsAt) || '09:00',
    endsAt: clockToHm(item.endsAt) || '10:00',
    remindAt: scheduleIsoToLocal(item.remindAt),
    note: item.note || '',
  };
  editError.value = '';
  editOpen.value = true;
}

async function saveEdit() {
  if (!editing.value) return;
  editError.value = '';
  const remindAt = scheduleLocalToIso(editing.value.remindAt);
  if (editing.value.remindAt && !remindAt) {
    editError.value = '提醒时间格式不正确';
    return;
  }
  const startsAt = editing.value.allDay
    ? null
    : scheduleDateTimeLocal(editing.value.dueOn, editing.value.startsAt) ?? null;
  const endsAt = editing.value.allDay
    ? null
    : scheduleDateTimeLocal(editing.value.dueOn, editing.value.endsAt) ?? null;
  if (!editing.value.allDay && (!startsAt || !endsAt)) {
    editError.value = '请填写开始和结束时间';
    return;
  }
  try {
    await updateSchedule(editing.value.id, {
      title: editing.value.title,
      dueOn: editing.value.dueOn,
      note: editing.value.note,
      remindAt: remindAt ?? null,
      allDay: editing.value.allDay,
      startsAt,
      endsAt,
    });
    armRemind(editing.value.title, remindAt);
    editOpen.value = false;
    await load();
  } catch (err) {
    editError.value = err instanceof Error ? err.message : '没有保存';
  }
}

async function toggle(id: string) {
  const item = findItem(id);
  if (!item) return;
  const done = item.status !== 'done';
  const ok = await confirmAction(
    done ? `把「${item.title}」标成完成？` : `把「${item.title}」改回未完成？`,
    done ? '完成日程' : '取消完成',
    done ? '完成' : '改回',
  );
  if (!ok) return;
  await updateSchedule(item.id, { status: done ? 'done' : 'open' });
  await load();
}

async function remove(id: string) {
  const item = findItem(id);
  const name = item ? `「${item.title}」` : '这条日程';
  if (!await confirmAction(`删除后，${name}就不会再出现。`, '删除日程', '删除')) return;
  await removeSchedule(id);
  if (highlightId.value === id) {
    highlightId.value = null;
    router.replace({ path: route.path, query: {} });
  }
  await load();
}
</script>
