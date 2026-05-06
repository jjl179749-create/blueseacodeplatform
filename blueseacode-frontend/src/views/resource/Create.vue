<template>
  <div class="create-resource-page">
    <n-card title="发布资源" style="max-width: 800px; margin: 0 auto">
      <n-form ref="formRef" :model="form" :rules="rules" label-placement="top">
        <!-- 标题 -->
        <n-form-item label="资源标题" path="title">
          <n-input v-model:value="form.title" placeholder="输入资源标题" maxlength="200" />
        </n-form-item>

        <!-- 分类 -->
        <n-form-item label="资源分类" path="categoryId">
          <n-select
            v-model:value="form.categoryId"
            :options="categoryOptions"
            placeholder="选择分类"
            @update:value="handleCategoryChange"
          />
        </n-form-item>

        <!-- 标签 -->
        <n-form-item label="标签（可选）">
          <n-select
            v-model:value="form.tagIds"
            :options="tagOptions"
            multiple
            placeholder="选择标签（可多选）"
          />
        </n-form-item>

        <!-- 文件上传 -->
        <n-form-item label="上传文件" path="fileUrl">
          <div class="file-upload-area">
            <input
              ref="fileInputRef"
              type="file"
              style="display: none"
              @change="handleFileChange"
            />
            <n-button @click="fileInputRef?.click()" :disabled="uploadingFile">
              选择文件
            </n-button>
            <span v-if="form.fileName" class="file-name">{{ form.fileName }}</span>
            <span v-if="uploadingFile" class="uploading-text">上传中...</span>
          </div>
        </n-form-item>

        <!-- 封面图 -->
        <n-form-item label="封面图（可选）">
          <div class="cover-upload-area">
            <input
              ref="coverInputRef"
              type="file"
              accept="image/*"
              style="display: none"
              @change="handleCoverChange"
            />
            <n-button @click="coverInputRef?.click()" :disabled="uploadingCover">
              选择封面图
            </n-button>
            <img v-if="form.coverImage" :src="form.coverImage" class="cover-preview" />
          </div>
        </n-form-item>

        <!-- 下载积分 -->
        <n-form-item label="下载所需积分（0 为免费）">
          <n-input-number
            v-model:value="form.downloadPoints"
            :min="0"
            :max="100"
            style="width: 160px"
          />
        </n-form-item>

        <!-- 描述 -->
        <n-form-item label="资源描述">
          <n-input
            v-model:value="form.description"
            type="textarea"
            :rows="6"
            placeholder="输入资源描述（支持 Markdown）"
          />
        </n-form-item>

        <!-- 操作按钮 -->
        <n-form-item>
          <n-button type="primary" :loading="submitting" @click="handleSubmit">
            提交审核
          </n-button>
          <n-button style="margin-left: 12px" @click="handleReset">重置</n-button>
        </n-form-item>
      </n-form>
    </n-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import {
  createResource,
  getCategories,
  getTags,
  type ResCategory,
  type ResTag,
} from '@/api/resource'
import { uploadFile } from '@/api/user'

const router = useRouter()
const message = useMessage()

const categories = ref<ResCategory[]>([])
const tags = ref<ResTag[]>([])

const fileInputRef = ref<HTMLInputElement | null>(null)
const coverInputRef = ref<HTMLInputElement | null>(null)
const formRef = ref(null)

const uploadingFile = ref(false)
const uploadingCover = ref(false)
const submitting = ref(false)

const form = reactive({
  title: '',
  description: '',
  categoryId: null as number | null,
  fileUrl: '',
  fileName: '',
  coverImage: '',
  downloadPoints: 0,
  tagIds: [] as number[],
})

const rules = {
  title: [{ required: true, message: '请输入资源标题', trigger: 'blur' }],
  categoryId: [
    { required: true, message: '请选择分类', trigger: ['blur', 'change'] },
  ],
  fileUrl: [{ required: true, message: '请上传文件', trigger: 'change' }],
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

function handleCategoryChange() {
  // 强制重新校验categoryId字段，清除之前触发的验证提示
  if (formRef.value) {
    formRef.value.validate('categoryId', () => {})
  }
}

async function loadCategories() {
  try {
    const res = await getCategories()
    if (res.code === 200) categories.value = res.data || []
  } catch {
    // ignore
  }
}

async function loadTags() {
  try {
    const res = await getTags()
    if (res.code === 200) {
      tags.value = res.data || []
      if (tags.value.length === 0) {
        console.warn('标签数据为空，请检查数据库是否已插入初始标签')
      }
    } else {
      console.error('加载标签失败:', res.message)
    }
  } catch (e) {
    console.error('加载标签异常:', e)
  }
}

async function handleFileChange(event: Event) {
  const input = event.target as HTMLInputElement
  if (!input.files || input.files.length === 0) return

  const file = input.files[0]

  // 校验文件大小（最大100MB）
  if (file.size > 100 * 1024 * 1024) {
    message.error('文件大小不能超过100MB')
    input.value = ''
    return
  }

  uploadingFile.value = true
  try {
    const res = await uploadFile(file, 'resource')
    if (res.code === 200) {
      form.fileUrl = res.data
      form.fileName = file.name
      message.success('文件上传成功')
    } else {
      message.error(res.message || '上传失败')
    }
  } catch {
    message.error('文件上传失败')
  } finally {
    uploadingFile.value = false
    input.value = ''
  }
}

async function handleCoverChange(event: Event) {
  const input = event.target as HTMLInputElement
  if (!input.files || input.files.length === 0) return

  const file = input.files[0]

  // 校验文件大小（最大5MB）
  if (file.size > 5 * 1024 * 1024) {
    message.error('封面上传大小不能超过5MB')
    input.value = ''
    return
  }

  uploadingCover.value = true
  try {
    const res = await uploadFile(file, 'cover')
    if (res.code === 200) {
      form.coverImage = res.data
      message.success('封面上传成功')
    } else {
      message.error(res.message || '上传失败')
    }
  } catch {
    message.error('封面上传失败')
  } finally {
    uploadingCover.value = false
    input.value = ''
  }
}

async function handleSubmit() {
  submitting.value = true
  try {
    const res = await createResource({
      title: form.title,
      description: form.description || undefined,
      categoryId: form.categoryId!,
      fileUrl: form.fileUrl,
      fileName: form.fileName || undefined,
      coverImage: form.coverImage || undefined,
      downloadPoints: form.downloadPoints || 0,
      tagIds: form.tagIds.length > 0 ? form.tagIds : undefined,
    })
    if (res.code === 200) {
      message.success('发布成功，等待审核')
      router.push('/resources')
    } else {
      message.error(res.message || '发布失败')
    }
  } catch {
    message.error('发布失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

function handleReset() {
  form.title = ''
  form.description = ''
  form.categoryId = null
  form.fileUrl = ''
  form.fileName = ''
  form.coverImage = ''
  form.downloadPoints = 0
  form.tagIds = []
  if (fileInputRef.value) fileInputRef.value.value = ''
  if (coverInputRef.value) coverInputRef.value.value = ''
}

onMounted(() => {
  loadCategories()
  loadTags()
})
</script>

<style scoped>
.create-resource-page {
  max-width: 800px;
  margin: 0 auto;
}
.file-upload-area {
  display: flex;
  align-items: center;
  gap: 12px;
}
.file-name {
  color: #666;
  font-size: 13px;
}
.uploading-text {
  color: #18a058;
  font-size: 13px;
}
.cover-upload-area {
  display: flex;
  align-items: center;
  gap: 12px;
}
.cover-preview {
  width: 120px;
  height: 80px;
  object-fit: cover;
  border-radius: 4px;
  border: 1px solid #eee;
}
</style>
