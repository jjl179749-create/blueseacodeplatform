<template>
  <div class="resource-management">
    <h2 style="margin-bottom: 20px">资源管理</h2>

    <n-card style="margin-bottom: 16px">
      <n-space>
        <n-input v-model:value="query.keyword" placeholder="搜索资源标题" clearable style="width: 200px" @keyup.enter="search" />
        <n-select v-model:value="query.status" :options="statusOptions" placeholder="状态" clearable style="width: 140px" />
        <n-select v-model:value="query.categoryId" :options="categoryOptions" placeholder="分类" clearable style="width: 140px" />
        <n-button type="primary" @click="search">搜索</n-button>
        <n-button @click="reset">重置</n-button>
      </n-space>
    </n-card>

    <n-card>
      <n-data-table :columns="columns" :data="list" :loading="loading" :pagination="pagination" @update:page="(p: number) => { query.page = p; loadData() }" />
    </n-card>

    <!-- 编辑弹窗 -->
    <n-modal v-model:show="showEditModal" title="编辑资源" preset="card" style="width: 600px">
      <n-form :model="editForm" label-placement="left" label-width="80px">
        <n-form-item label="标题">
          <n-input v-model:value="editForm.title" />
        </n-form-item>
        <n-form-item label="描述">
          <n-input v-model:value="editForm.description" type="textarea" :rows="3" />
        </n-form-item>
        <n-form-item label="状态">
          <n-select v-model:value="editForm.status" :options="editStatusOptions" />
        </n-form-item>
      </n-form>
      <template #footer>
        <n-button type="primary" @click="saveEdit" :loading="editSaving">保存</n-button>
      </template>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, h, onMounted } from 'vue'
import { NButton, NSpace, NTag, useMessage, useDialog } from 'naive-ui'
import {
  getAdminResources,
  adminUpdateResource,
  changeResourceStatus,
  adminDeleteResource,
  getAdminCategories,
  type ResCategory,
} from '@/api/admin'

const message = useMessage()
const dialog = useDialog()
const loading = ref(false)
const list = ref<any[]>([])
const categories = ref<ResCategory[]>([])
const showEditModal = ref(false)
const editSaving = ref(false)
const editForm = reactive({ title: '', description: '', status: 0 })

const query = reactive({ page: 1, size: 10, keyword: '', status: undefined as number | undefined, categoryId: undefined as number | undefined })

const statusOptions = [
  { label: '待审核', value: 0 },
  { label: '已上架', value: 1 },
  { label: '已下架', value: 2 },
  { label: '已驳回', value: 3 },
]
const editStatusOptions = [
  { label: '待审核', value: 0 },
  { label: '已上架', value: 1 },
  { label: '已下架', value: 2 },
]
const categoryOptions = ref<{ label: string; value: number }[]>([])

const pagination = reactive({ page: 1, pageCount: 1, pageSize: 10, onChange: (p: number) => { query.page = p; loadData() } })

const columns = [
  { title: 'ID', key: 'id', width: 60 },
  { title: '标题', key: 'title', ellipsis: true },
  { title: '发布人', key: 'nickname', width: 100 },
  { title: '分类', key: 'categoryName', width: 100 },
  { title: '下载量', key: 'downloadCount', width: 80 },
  {
    title: '状态', key: 'status', width: 80,
    render(row: any) {
      return h(NTag, { type: ['warning', 'success', 'default', 'error'][row.status], size: 'small' }, { default: () => ['待审核', '已上架', '已下架', '已驳回'][row.status] })
    },
  },
  { title: '创建时间', key: 'createTime', width: 170 },
  {
    title: '操作', key: 'actions', width: 240,
    render(row: any) {
      return h(NSpace, null, {
        default: () => [
          h(NButton, { size: 'small', type: 'primary', onClick: () => openEdit(row) }, { default: () => '编辑' }),
          row.status === 1
            ? h(NButton, { size: 'small', onClick: () => toggleStatus(row, 2) }, { default: () => '下架' })
            : h(NButton, { size: 'small', type: 'success', onClick: () => toggleStatus(row, 1) }, { default: () => '上架' }),
          h(NButton, { size: 'small', type: 'error', onClick: () => doDelete(row) }, { default: () => '删除' }),
        ],
      })
    },
  },
]

let currentEditId = 0

function openEdit(row: any) {
  currentEditId = row.id
  editForm.title = row.title
  editForm.description = row.description || ''
  editForm.status = row.status
  showEditModal.value = true
}

async function saveEdit() {
  editSaving.value = true
  try {
    const res = await adminUpdateResource(currentEditId, { title: editForm.title, description: editForm.description, status: editForm.status })
    if (res.code === 200) {
      message.success('保存成功')
      showEditModal.value = false
      loadData()
    }
  } catch { message.error('保存失败') }
  finally { editSaving.value = false }
}

function toggleStatus(row: any, status: number) {
  const text = status === 1 ? '上架' : '下架'
  dialog.warning({
    title: '确认操作',
    content: `确定要${text}资源 "${row.title}" 吗？`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        const res = await changeResourceStatus(row.id, status)
        if (res.code === 200) message.success(`${text}成功`)
        loadData()
      } catch { message.error('操作失败') }
    },
  })
}

function doDelete(row: any) {
  dialog.warning({
    title: '确认删除',
    content: `确定要删除资源 "${row.title}" 吗？此操作不可恢复。`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        const res = await adminDeleteResource(row.id)
        if (res.code === 200) { message.success('删除成功'); loadData() }
      } catch { message.error('删除失败') }
    },
  })
}

function search() { query.page = 1; loadData() }
function reset() { query.keyword = ''; query.status = undefined; query.categoryId = undefined; query.page = 1; loadData() }

async function loadData() {
  loading.value = true
  try {
    const res = await getAdminResources({ page: query.page, size: query.size, keyword: query.keyword || undefined, status: query.status, categoryId: query.categoryId })
    if (res.code === 200) {
      list.value = res.data.records
      pagination.pageCount = Math.ceil(res.data.total / res.data.size)
    }
  } catch { message.error('加载失败') }
  finally { loading.value = false }
}

onMounted(async () => {
  loadData()
  const catRes = await getAdminCategories()
  if (catRes.code === 200) {
    categories.value = catRes.data
    categoryOptions.value = catRes.data.map((c) => ({ label: c.name, value: c.id }))
  }
})
</script>
