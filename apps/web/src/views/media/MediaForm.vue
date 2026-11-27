<template>
  <form class="stack tone-media" @submit.prevent="submit">
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
          <el-upload :auto-upload="false" :show-file-list="false" accept="image/*" :disabled="uploading" :on-change="upload">
            <el-button round :loading="uploading">{{ coverButton }}</el-button>
          </el-upload>
          <p v-if="coverError" class="error">{{ coverError }}</p>
        </div>
      </div>
    </Field>
    <el-button class="primary" type="primary" native-type="submit" round :disabled="uploading">收入收藏</el-button>
  </form>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue';
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
const uploading = ref(false);
const coverError = ref('');
bindDraft('zhaoxi-draft-media', form);

const coverButton = computed(() => {
  if (uploading.value) return '上传中…';
  return form.coverUrl ? '已选择封面' : '选择封面';
});

async function upload(file: UploadFile) {
  if (!file.raw) return;
  uploading.value = true;
  coverError.value = '';
  try {
    const result = await uploadCover(file.raw);
    form.coverUrl = result.url;
  } catch (err) {
    coverError.value = err instanceof Error ? err.message : '封面没有传上去';
  } finally {
    uploading.value = false;
  }
}

async function submit() {
  await props.save({ ...form });
  form.title = '';
  form.creator = '';
  form.note = '';
  form.rating = 0;
  form.coverUrl = '';
  coverError.value = '';
}
</script>
