<template>
  <form class="stack tone-schedule" @submit.prevent="submit">
    <div class="row">
      <Field label="事项"><el-input v-model="form.title" /></Field>
      <Field label="日期"><el-date-picker v-model="form.dueOn" type="date" value-format="YYYY-MM-DD" /></Field>
      <Field label="类型">
        <el-select v-model="form.allDay">
          <el-option label="全天" :value="true" />
          <el-option label="时段" :value="false" />
        </el-select>
      </Field>
      <Field v-if="!form.allDay" label="开始">
        <el-time-picker v-model="form.startsAt" format="HH:mm" value-format="HH:mm" />
      </Field>
      <Field v-if="!form.allDay" label="结束">
        <el-time-picker v-model="form.endsAt" format="HH:mm" value-format="HH:mm" />
      </Field>
      <Field label="提醒"><el-date-picker v-model="form.remindAt" type="datetime" value-format="YYYY-MM-DDTHH:mm" /></Field>
      <Field label="备注"><el-input v-model="form.note" /></Field>
      <el-button class="primary" type="primary" native-type="submit" round>安排</el-button>
    </div>
    <p class="muted remind-hint">
      网页提醒仅在本页保持打开时有效；需要可靠提醒请用 App。
      <el-button v-if="canNotify" text type="primary" @click="enableNotify">
        {{ notifyReady ? '已开启网页提醒' : '开启网页提醒' }}
      </el-button>
    </p>
  </form>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref, watch } from 'vue';
import Field from '../../components/Field.vue';
import { bindDraft } from '../../utils';
import { canUseNotification, ensureNotifyPermission } from '../../utils/notify';

export type ScheduleFormPayload = {
  title: string;
  dueOn: string;
  allDay: boolean;
  startsAt: string;
  endsAt: string;
  remindAt: string;
  note: string;
};

const props = defineProps<{
  today: string;
  save: (payload: ScheduleFormPayload) => Promise<void>;
}>();

const form = reactive({
  title: '',
  dueOn: '',
  allDay: true,
  startsAt: '09:00',
  endsAt: '10:00',
  remindAt: '',
  note: '',
});
const canNotify = canUseNotification();
const notifyReady = ref(false);
bindDraft('zhaoxi-draft-schedule', form);

onMounted(() => {
  notifyReady.value = canNotify && Notification.permission === 'granted';
});

watch(() => props.today, (today) => {
  if (today && !form.dueOn) form.dueOn = today;
}, { immediate: true });

async function enableNotify() {
  notifyReady.value = await ensureNotifyPermission();
}

async function submit() {
  await props.save({ ...form });
  form.title = '';
  form.note = '';
  form.remindAt = '';
  form.allDay = true;
}
</script>
