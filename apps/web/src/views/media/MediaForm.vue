<template>
  <form class="stack" @submit.prevent="submit">
    <div class="row">
      <Field label="类型">
        <el-select v-model="form.kind">
          <el-option v-for="(label, key) in kindLabel" :key="key" :label="label" :value="key" />
        </el-select>
      </Field>
      <Field label="名称"><el-input v-model="form.title" /></Field>
      <Field label="作者 / 导演"><el-input v-model="form.creator" /></Field>
      <Field label="状态">
        <el-select v-model="form.status">
          <el-option v-for="(label, key) in statusLabel" :key="key" :label="label" :value="key" />
        </el-select>
      </Field>
    </div>
    <Field label="短评"><el-input v-model="form.note" type="textarea" maxlength="500" /></Field>
    <StarRating v-model="form.rating" />
    <Field label="封面">
      <div class="cover-field">
        <img class="cover-preview" :src="coverOf(form.kind, form.coverUrl)" :alt="`${kindLabel[form.kind]}默认封面`" />
        <div>
          <el-upload :auto-upload="false" :show-file-list="false" accept="image/*" :on-change="upload">
            <el-button round>{{ form.coverUrl ? '已选择封面' : '选择封面' }}</el-button>
          </el-upload>
          <p class="muted">{{ form.coverUrl ? '已换成自己的封面' : `未选封面，先用${kindLabel[form.kind]}的默认图` }}</p>
        </div>
      </div>
    </Field>
    <el-button class="primary" type="primary" native-type="submit" round>收入收藏</el-button>
  </form>
</template>

<script setup lang="ts">
import { reactive } from 'vue';
import type { UploadFile } from 'element-plus';
import Field from '../../components/Field.vue';
import { uploadCover } from '../../http/api';
import { bindDraft } from '../../utils';
import { coverOf, kindLabel, statusLabel, type Kind, type Status } from './labels';
import StarRating from './StarRating.vue';

const props = defineProps<{
  save: (payload: { kind: Kind; title: string; creator: string; status: Status; note: string; rating: number; coverUrl: string }) => Promise<void>;
}>();
const form = reactive({ kind: 'film' as Kind, title: '', creator: '', status: 'want' as Status, note: '', rating: 0, coverUrl: '' });
bindDraft('zhaoxi-draft-media', form);

async function upload(file: UploadFile) {
  if (!file.raw) return;
  const result = await uploadCover(file.raw);
  form.coverUrl = result.url;
}

async function submit() {
  await props.save({ ...form });
  form.title = '';
  form.creator = '';
  form.note = '';
  form.rating = 0;
  form.coverUrl = '';
}
</script>
