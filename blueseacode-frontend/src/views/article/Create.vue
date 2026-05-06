<template>
  <div class="create-page">
    <n-card :bordered="true" class="create-card">
      <template #header>
        <h2>{{ isEdit ? '编辑文章' : '发布文章' }}</h2>
      </template>

      <n-form ref="formRef" :model="form" :rules="rules" label-placement="top">
        <!-- 标题 -->
        <n-form-item label="标题" path="title">
          <n-input v-model:value="form.title" placeholder="输入文章标题" maxlength="200" />
        </n-form-item>

        <!-- 摘要 -->
        <n-form-item label="摘要">
          <n-input
            v-model:value="form.summary"
            type="textarea"
            :rows="2"
            placeholder="文章摘要（可选，不填则自动截取内容前200字）"
            maxlength="500"
          />
        </n-form-item>

        <!-- 分类 -->
        <n-form-item label="分类" path="categoryId">
          <n-select
            v-model:value="form.categoryId"
            :options="categoryOptions"
            placeholder="选择分类"
            clearable
          />
        </n-form-item>

        <!-- 标签 -->
        <n-form-item label="标签">
          <n-select
            v-model:value="form.tagIds"
            :options="tagOptions"
            placeholder="选择标签"
            multiple
            clearable
          />
        </n-form-item>

        <!-- 封面图 -->
        <n-form-item label="封面图URL">
          <n-input v-model:value="form.coverImage" placeholder="封面图片URL（可选）" />
        </n-form-item>

        <!-- 文章内容 -->
        <n-form-item label="文章内容" path="content">
          <n-input
            v-model:value="form.content"
            type="textarea"
            :rows="16"
            placeholder="开始写文章...&#10;支持 Markdown 语法&#10;标题: # H1, ## H2, ### H3&#10;粗体: **文字**&#10;代码: `code` 或 ```code block```&#10;列表: - item&#10;引用: > quote"
          />
        </n-form-item>

        <!-- 允许评论 -->
        <n-form-item label="允许评论">
          <n-switch v-model:value="form.isComment" />
        </n-form-item>

        <!-- 操作按钮 -->
        <div class="form-actions">
          <n-button @click="handleSaveDraft" :loading="submitting" :disabled="submitting">
            保存草稿
          </n-button>
          <n-button type="primary" @click="handleSubmit" :loading="submitting" :disabled="submitting">
            提交审核
          </n-button>
          <n-button @click="handleCancel" :disabled="submitting">取消</n-button>
        </div>
      </n-form>
    </n-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import type { FormRules, FormInst } from 'naive-ui'
import {
  createArticle,
  updateArticle,
  getArticleDetail,
  getArticleCategories,
  getArticleTags,
  type ArtCategory,
  type ArtTag,
} from '@/api/article'

const router = useRouter()
const route = useRoute()
const message = useMessage()
const formRef = ref<FormInst | null>(null)

const isEdit = ref(false)
const articleId = ref<number | null>(null)
const submitting = ref(false)
const categories = ref<ArtCategory[]>([])
const tags = ref<ArtTag[]>([])

interface ArticleForm {
  title: string
  summary: string
  content: string
  categoryId: number | null
  tagIds: number[]
  coverImage: string
  isComment: boolean
}

const form = reactive<ArticleForm>({
  title: '',
  summary: '',
  content: '',
  categoryId: null,
  tagIds: [],
  coverImage: '',
  isComment: true,
})

const rules: FormRules = {
  title: {
    required: true,
    validator: (_rule, value: string) => {
      if (!value || !value.trim()) {
        return new Error('请输入文章标题')
      }
      return true
    },
    trigger: ['blur', 'input'],
  },
  content: {
    required: true,
    validator: (_rule, value: string) => {
      if (!value || !value.trim()) {
        return new Error('请输入文章内容')
      }
      return true
    },
    trigger: ['blur', 'input'],
  },
}

const categoryOptions = computed(() => {
  return categories.value.map((c) => ({
    label: c.name,
    value: c.id,
  }))
})

const tagOptions = computed(() => {
  return tags.value.map((t) => ({
    label: t.name,
    value: t.id,
  }))
})

async function loadCategories() {
  try {
    const res = await getArticleCategories()
    if (res.code === 200) {
      categories.value = res.data || []
    }
  } catch {
    // ignore
  }
}

async function loadTags() {
  try {
    const res = await getArticleTags()
    if (res.code === 200) {
      tags.value = res.data || []
    }
  } catch {
    // ignore
  }
}

async function loadArticleForEdit() {
  if (!articleId.value) return
  try {
    const res = await getArticleDetail(articleId.value)
    if (res.code === 200 && res.data) {
      const article = res.data
      form.title = article.title
      form.summary = article.summary || ''
      form.content = article.content || ''
      form.categoryId = article.categoryId
      form.coverImage = article.coverImage || ''
      form.isComment = article.isComment === 1
      // tags 是从后端返回的字符串列表，我们需要转换成ID
      // 这里简化处理：如果 tags 名称与现有标签匹配则取ID
      if (article.tags && article.tags.length > 0) {
        form.tagIds = tags.value
          .filter((t) => article.tags.includes(t.name))
          .map((t) => Number(t.id))
      }
    }
  } catch {
    message.error('加载文章信息失败')
  }
}

async function handleSaveDraft() {
  await doSubmit(0)
}

async function handleSubmit() {
  await doSubmit(1)
}

async function doSubmit(status: number) {
  try {
    await formRef.value?.validate()
  } catch {
    message.warning('请完善必填项')
    return
  }

  submitting.value = true
  try {
    if (isEdit.value && articleId.value) {
      await updateArticle(articleId.value, {
        title: form.title.trim(),
        summary: form.summary.trim() || undefined,
        content: form.content,
        categoryId: form.categoryId ?? undefined,
        tagIds: form.tagIds.length > 0 ? form.tagIds : undefined,
        coverImage: form.coverImage || undefined,
        isComment: form.isComment ? 1 : 0,
        status,
      })
      message.success('更新成功')
      router.push(`/articles/${articleId.value}`)
    } else {
      const res = await createArticle({
        title: form.title.trim(),
        summary: form.summary.trim() || undefined,
        content: form.content,
        categoryId: form.categoryId ?? undefined,
        tagIds: form.tagIds.length > 0 ? form.tagIds : undefined,
        coverImage: form.coverImage || undefined,
        isComment: form.isComment ? 1 : 0,
        status,
      })
      if (res.code === 200) {
        message.success(status === 0 ? '草稿已保存' : '发布成功，等待审核')
        router.push('/articles')
      }
    }
  } catch (e: any) {
    message.error(e?.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

function handleCancel() {
  router.push('/articles')
}

onMounted(() => {
  loadCategories()
  loadTags()

  // 检查是否是编辑模式
  const id = route.params.id
  if (id) {
    isEdit.value = true
    articleId.value = Number(id)
    loadArticleForEdit()
  }
})
</script>

<style scoped>
.create-page {
  max-width: 800px;
  margin: 0 auto;
}
.create-card h2 {
  margin: 0;
  font-size: 20px;
}
.form-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}
</style>
