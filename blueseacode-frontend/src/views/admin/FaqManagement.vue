<template>
  <div class="faq-management">
    <h2 style="margin-bottom: 20px">FAQ 管理</h2>

    <n-card style="margin-bottom: 16px">
      <n-space>
        <n-input v-model:value="keyword" placeholder="搜索问题/答案" clearable style="width: 260px" @keyup.enter="search" />
        <n-button type="primary" @click="search">搜索</n-button>
        <n-button type="info" @click="openCreate">新增FAQ</n-button>
      </n-space>
    </n-card>

    <n-card>
      <n-data-table :columns="columns" :data="list" :loading="loading" :pagination="pagination" @update:page="handlePageChange" />
    </n-card>

    <!-- 编辑弹窗 -->
    <n-modal v-model:show="showModal" :title="isEdit ? '编辑FAQ' : '新增FAQ'" preset="card" style="width: 600px" :mask-closable="false">
      <n-form :model="form" label-placement="top">
        <n-form-item label="问题">
          <n-input v-model:value="form.question" />
        </n-form-item>
        <n-form-item label="答案">
          <n-input v-model:value="form.answer" type="textarea" :rows="5" />
        </n-form-item>
        <n-form-item label="分类">
          <n-input v-model:value="form.category" placeholder="如：ACCOUNT, TECH" />
        </n-form-item>
        <n-form-item label="状态">
          <n-switch v-model:value="form.status" :checked-value="1" :unchecked-value="0">
            <template #checked>启用</template>
            <template #unchecked>禁用</template>
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
import { getFaqList, createFaq, updateFaq, deleteFaq, type ChatFaq } from '@/api/admin'

const message = useMessage()
const dialog = useDialog()
const loading = ref(false)
const list = ref<ChatFaq[]>([])
const keyword = ref('')
const showModal = ref(false)
const isEdit = ref(false)
const saving = ref(false)
const editId = ref(0)
const form = reactive({ question: '', answer: '', category: '', status: 1 })
const query = reactive({ page: 1, size: 10 })

const pagination = reactive({ page: 1, pageCount: 1, pageSize: 10, onChange: (p: number) => { query.page = p; loadData() } })

const columns = [
  { title: 'ID', key: 'id', width: 60 },
  { title: '问题', key: 'question', ellipsis: true },
  { title: '分类', key: 'category', width: 100 },
  { title: '命中', key: 'hitCount', width: 70 },
  {
    title: '状态', key: 'status', width: 70,
    render(row: ChatFaq) { return h(NTag, { type: row.status === 1 ? 'success' : 'default', size: 'small' }, { default: () => row.status === 1 ? '启用' : '禁用' }) },
  },
  { title: '创建时间', key: 'createTime', width: 170 },
  {
    title: '操作', key: 'actions', width: 160,
    render(row: ChatFaq) {
      return h(NSpace, null, {
        default: () => [
          h(NButton, { size: 'small', type: 'primary', onClick: () => openEdit(row) }, { default: () => '编辑' }),
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
    const res = await getFaqList(keyword.value || undefined, undefined, query.page, query.size)
    if (res.code === 200) {
      list.value = res.data.records
      pagination.pageCount = Math.ceil(res.data.total / res.data.size)
    }
  } catch { message.error('加载失败') }
  finally { loading.value = false }
}

function openCreate() {
  isEdit.value = false
  editId.value = 0
  form.question = ''
  form.answer = ''
  form.category = ''
  form.status = 1
  showModal.value = true
}

function openEdit(row: ChatFaq) {
  isEdit.value = true
  editId.value = row.id
  form.question = row.question
  form.answer = row.answer
  form.category = row.category
  form.status = row.status
  showModal.value = true
}

async function save() {
  if (!form.question || !form.answer) { message.warning('请填写问题与答案'); return }
  saving.value = true
  try {
    const res = isEdit.value ? await updateFaq(editId.value, form) : await createFaq(form)
    if (res.code === 200) {
      message.success(isEdit.value ? '更新成功' : '创建成功')
      showModal.value = false
      loadData()
    }
  } catch { message.error('保存失败') }
  finally { saving.value = false }
}

function doDelete(row: ChatFaq) {
  dialog.warning({
    title: '确认删除',
    content: `确定要删除FAQ "${row.question}" 吗？`,
    positiveText: '确定', negativeText: '取消',
    onPositiveClick: async () => {
      try {
        const res = await deleteFaq(row.id)
        if (res.code === 200) { message.success('删除成功'); loadData() }
      } catch { message.error('删除失败') }
    },
  })
}

onMounted(loadData)
</script>
