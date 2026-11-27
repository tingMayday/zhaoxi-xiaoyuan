<template>
  <div class="category-panel">
    <form class="row category-form" @submit.prevent="create">
      <Field label="名称"><el-input v-model="form.name" maxlength="20" /></Field>
      <Field label="方向">
        <el-select v-model="form.kind">
          <el-option label="支出" value="expense" />
          <el-option label="收入" value="income" />
        </el-select>
      </Field>
      <el-button class="primary" type="primary" native-type="submit" round>添加</el-button>
    </form>
    <Notice :text="error" kind="error" />
    <ul class="category-list">
      <li v-for="item in categories" :key="item.id">
        <span>
          <strong>{{ item.name }}</strong>
          <em class="muted">{{ item.kind === 'income' ? '收入' : '支出' }}</em>
        </span>
        <el-button class="btn-soft" round @click="archive(item.id)">归档</el-button>
      </li>
    </ul>
    <Notice v-if="categories.length === 0" text="还没有分类。" kind="empty" />
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue';
import Field from '../../components/Field.vue';
import Notice from '../../components/Notice.vue';
import { archiveCategory, createCategory, type Category } from '../../http/api';
import { confirmAction } from '../../utils';

const props = defineProps<{ categories: Category[] }>();
const emit = defineEmits<{ changed: [] }>();
const error = ref('');
const form = reactive({ name: '', kind: 'expense' as 'income' | 'expense' });

async function create() {
  error.value = '';
  if (!form.name.trim()) {
    error.value = '请填写分类名称';
    return;
  }
  try {
    await createCategory({ name: form.name.trim(), kind: form.kind });
    form.name = '';
    emit('changed');
  } catch (err) {
    error.value = err instanceof Error ? err.message : '没有加上';
  }
}

async function archive(id: string) {
  const item = props.categories.find((row) => row.id === id);
  const name = item ? `「${item.name}」` : '这个分类';
  if (!await confirmAction(`归档后，${name}不再出现在可选列表，已有流水会保留。`, '归档分类', '归档')) return;
  await archiveCategory(id);
  emit('changed');
}
</script>
