<template>
  <div class="create-page">
    <n-card :title="isEdit ? '编辑需求' : '发布需求'" :bordered="true" style="max-width: 800px; margin: 0 auto">
      <n-form ref="formRef" :model="form" :rules="rules" label-placement="top">
        <n-form-item label="标题" path="title">
          <n-input v-model:value="form.title" placeholder="输入需求标题，简明扼要" maxlength="200" show-count />
        </n-form-item>

        <n-form-item label="分类" path="category">
          <n-select
            v-model:value="form.category"
            :options="DEMAND_CATEGORIES"
            placeholder="选择需求分类"
          />
        </n-form-item>

        <n-row :gutter="16">
          <n-col :span="12">
            <n-form-item label="预算下限（元）" path="budgetMin">
              <n-input-number v-model:value="form.budgetMin" :min="0" placeholder="可选" clearable style="width:100%" />
            </n-form-item>
          </n-col>
          <n-col :span="12">
            <n-form-item label="预算上限（元）" path="budgetMax">
              <n-input-number v-model:value="form.budgetMax" :min="0" placeholder="可选" clearable style="width:100%" />
            </n-form-item>
          </n-col>
        </n-row>

        <n-form-item label="截止日期" path="deadline">
          <n-date-picker
            v-model:formatted-value="form.deadline"
            type="datetime"
            placeholder="选择截止日期（可选）"
            value-format="yyyy-MM-dd'T'HH:mm:ss"
            clearable
            style="width:100%"
          />
        </n-form-item>

        <n-form-item label="联系方式" path="contact">
          <n-input v-model:value="form.contact" placeholder="QQ/微信/邮箱等（可选）" />
        </n-form-item>

        <n-form-item label="需求描述" path="description">
          <n-input
            v-model:value="form.description"
            type="textarea"
            :rows="8"
            placeholder="详细描述你的需求、技术要求、预算说明等（支持 Markdown 格式）"
          />
        </n-form-item>

        <n-form-item>
          <n-space>
            <n-button type="primary" @click="handleSubmit" :loading="submitting" size="large">
              {{ isEdit ? '保存修改' : '发布需求' }}
            </n-button>
            <n-button @click="router.back()" size="large">取消</n-button>
          </n-space>
        </n-form-item>
      </n-form>
    </n-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useMessage } from 'naive-ui'
import {
  createDemand,
  updateDemand,
  getDemandDetail,
  DEMAND_CATEGORIES,
  type DemandCreateRequest,
} from '@/api/demand'

const router = useRouter()
const route = useRoute()
const message = useMessage()

const isEdit = ref(false)
const demandId = ref<number | null>(null)
const submitting = ref(false)
const formRef = ref()

const form = ref<DemandCreateRequest>({
  title: '',
  description: '',
  category: '',
  budgetMin: undefined,
  budgetMax: undefined,
  deadline: undefined,
  contact: '',
})

const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  description: [{ required: true, message: '请输入需求描述', trigger: 'blur' }],
  category: [{ required: true, message: '请选择分类', trigger: 'change' }],
}

onMounted(async () => {
  if (route.params.id) {
    isEdit.value = true
    demandId.value = Number(route.params.id)
    try {
      const res = await getDemandDetail(demandId.value)
      if (res.code === 200 && res.data) {
        const d = res.data
        form.value = {
          title: d.title,
          description: d.description || '',
          category: d.category,
          budgetMin: d.budgetMin ?? undefined,
          budgetMax: d.budgetMax ?? undefined,
          deadline: d.deadline || undefined,
          contact: d.contact || '',
        }
      }
    } catch {
      message.error('加载需求信息失败')
    }
  }
})

async function handleSubmit() {
  try {
    await formRef.value?.validate()
  } catch {
    return
  }

  submitting.value = true
  try {
    if (isEdit.value && demandId.value) {
      await updateDemand(demandId.value, form.value)
      message.success('修改成功')
      router.push('/demands/' + demandId.value)
    } else {
      const res = await createDemand(form.value)
      if (res.code === 200) {
        message.success('发布成功，等待审核')
        router.push('/demands')
      }
    }
  } catch (e: any) {
    message.error(e?.message || '操作失败')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.create-page {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px 0;
}
</style>
