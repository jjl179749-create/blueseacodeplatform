<template>
  <div class="announcement">
    <h2 style="margin-bottom: 20px">公告管理</h2>

    <n-card style="margin-bottom: 16px">
      <n-space>
        <n-select v-model:value="statusFilter" :options="filterOptions" placeholder="状态" clearable style="width: 140px" @update:value="search" />
        <n-button type="primary" @click="search">搜索</n-button>
        <n-button type="info" @click="openCreate">发布公告</n-button>
      </n-space>
    </n-card>

    <n-card>
      <n-data-table :columns="columns" :data="list" :loading="loading" :pagination="pagination" @update:page="handlePageChange" />
    </n-card>

    <!-- 编辑弹窗 -->
    <n-modal v-model:show="showModal" :title="isEdit ? '编辑公告' : '发布公告'" preset="card" style="width: 700px" :mask-closable="false">
      <n-form :model="form" label-placement="top">
        <n-form-item label="标题">
          <n-input v-model:value="form.title" />
        </n-form-item>
        <n-form-item label="内容">
          <n-input v-model:value="form.content" type="textarea" :rows="6" />
        </n-form-item>
        <n-form-item label="排序">
          <n-input-number v-model:value="form.sort" :min="0" />
        </n-form-item>
        <n-form-item label="状态">
          <n-switch v-model:value="form.status" :checked-value="1" :unchecked-value="0">
            <template #checked>发布</template>
            <template #unchecked>草稿</template>
          </n-switch>
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space>
          <n-button type="primary" @click="save" :loading="saving">保存</n-button>
          <n-button @click="showModal = false">取消</n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, h, onMounted } from 'vue'
import { NButton, NSpace, NTag, useMessage, useDialog } from 'naive-ui'
import {
  getAnnouncements,
  getAnnouncement,
  createAnnouncement,
  updateAnnouncement,
  deleteAnnouncement,
  type SysAnnouncement,
} from '@/api/admin'

const message = useMessage()
const dialog = useDialog()
const loading = ref(false)
const list = ref<SysAnnouncement[]>([])
const statusFilter = ref<number | null>(null)
const showModal = ref(false)
const isEdit = ref(false)
const saving = ref(false)
const editId = ref(0)
const form = reactive({ title: '', content: '', sort: 0, status: 0 })
const query = reactive({ page: 1, size: 10 })

const filterOptions = [
  { label: '全部', value: null },
  { label: '草稿', value: 0 },
  { label: '已发布', value: 1 },
]

const pagination = reactive({ page: 1, pageCount: 1, pageSize: 10, onChange: (p: number) => { query.page = p; loadData() } })

const columns = [
  { title: 'ID', key: 'id', width: 60 },
  { title: '标题', key: 'title', ellipsis: true },
  { title: '排序', key: 'sort', width: 60 },
  {
    title: '状态', key: 'status', width: 70,
    render(row: SysAnnouncement) {
      return h(NTag, { type: row.status === 1 ? 'success' : 'default', size: 'small' }, { default: () => row.status === 1 ? '已发布' : '草稿' })
    },
  },
  { title: '创建时间', key: 'createTime', width: 170 },
  {
    title: '操作', key: 'actions', width: 200,
    render(row: SysAnnouncement) {
      return h(NSpace, null, {
        default: () => [
          h(NButton, { size: 'small', type: 'primary', onClick: () => openEdit(row) }, { default: () => '编辑' }),
          h(NButton, {
            size: 'small', type: row.status === 1 ? 'warning' : 'success',
            onClick: () => toggleStatus(row),
          }, { default: () => row.status === 1 ? '下架' : '发布' }),
          h(NButton, { size: 'small', type: 'error', onClick: () => doDelete(row) }, { default: () => '删除' }),
        ],
      })
    },
  },
]

function search() { query.page = 1; loadData() }
function handlePageChange(page: number) { query.page = page; loadData() }

async function loadData() {
  loading.value = true
  try {
    const res = await getAnnouncements(query.page, query.size, statusFilter.value ?? undefined)
    if (res.code === 200) {
      list.value = res.data.records
      pagination.pageCount = Math.ceil(res.data.total / res.data.size)
    }
  } catch { message.error('加载失败') }
  finally { loading.value = false }
}

function openCreate() {
  isEdit.value = false; editId.value = 0
  form.title = ''; form.content = ''; form.sort = 0; form.status = 0
  showModal.value = true
}

async function openEdit(row: SysAnnouncement) {
  // 编辑时获取最新内容
  try {
    const res = await getAnnouncement(row.id)
    if (res.code === 200) {
      const d = res.data
      isEdit.value = true; editId.value = d.id
      form.title = d.title; form.content = d.content; form.sort = d.sort; form.status = d.status
      showModal.value = true
    }
  } catch { message.error('加载详情失败') }
}

async function save() {
  if (!form.title || !form.content) { message.warning('请填写标题与内容'); return }
  saving.value = true
  try {
    const payload = { title: form.title, content: form.content, sort: form.sort, status: form.status }
    const res = isEdit.value ? await updateAnnouncement(editId.value, payload) : await createAnnouncement(payload)
    if (res.code === 200) {
      message.success(isEdit.value ? '更新成功' : '创建成功')
      showModal.value = false; loadData()
    }
  } catch { message.error('保存失败') }
  finally { saving.value = false }
}

async function toggleStatus(row: SysAnnouncement) {
  const newStatus = row.status === 1 ? 0 : 1
  try {
    const res = await updateAnnouncement(row.id, { status: newStatus })
    if (res.code === 200) { message.success(newStatus === 1 ? '已发布' : '已下架'); loadData() }
  } catch { message.error('操作失败') }
}

function doDelete(row: SysAnnouncement) {
  dialog.warning({
    title: '确认删除', content: `确定要删除公告 "${row.title}" 吗？`,
    positiveText: '确定', negativeText: '取消',
    onPositiveClick: async () => {
      try {
        const res = await deleteAnnouncement(row.id)
        if (res.code === 200) { message.success('删除成功'); loadData() }
      } catch { message.error('删除失败') }
    },
  })
}

onMounted(loadData)
</script>
