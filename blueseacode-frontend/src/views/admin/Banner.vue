<template>
  <div class="banner">
    <h2 style="margin-bottom: 20px">轮播图管理</h2>

    <n-card style="margin-bottom: 16px">
      <n-space>
        <n-button type="primary" @click="openCreate">新增轮播图</n-button>
      </n-space>
    </n-card>

    <n-card>
      <n-data-table :columns="columns" :data="list" :loading="loading" :pagination="pagination" @update:page="handlePageChange" />
    </n-card>

    <!-- 编辑弹窗 -->
    <n-modal v-model:show="showModal" :title="isEdit ? '编辑轮播图' : '新增轮播图'" preset="card" style="width: 600px" :mask-closable="false">
      <n-form :model="form" label-placement="top">
        <n-form-item label="标题">
          <n-input v-model:value="form.title" placeholder="可选" />
        </n-form-item>
        <n-form-item label="图片URL">
          <n-input v-model:value="form.imageUrl" placeholder="必填" />
        </n-form-item>
        <n-form-item label="链接URL">
          <n-input v-model:value="form.linkUrl" placeholder="可选" />
        </n-form-item>
        <n-form-item label="排序">
          <n-input-number v-model:value="form.sort" :min="0" />
        </n-form-item>
        <n-form-item label="状态">
          <n-switch v-model:value="form.status" :checked-value="1" :unchecked-value="0">
            <template #checked>显示</template>
            <template #unchecked>隐藏</template>
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
import { getBanners, getBanner, createBanner, updateBanner, deleteBanner, type SysBanner } from '@/api/admin'

const message = useMessage()
const dialog = useDialog()
const loading = ref(false)
const list = ref<SysBanner[]>([])
const showModal = ref(false)
const isEdit = ref(false)
const saving = ref(false)
const editId = ref(0)
const form = reactive({ title: '', imageUrl: '', linkUrl: '', sort: 0, status: 1 })
const query = reactive({ page: 1, size: 10 })

const pagination = reactive({ page: 1, pageCount: 1, pageSize: 10, onChange: (p: number) => { query.page = p; loadData() } })

const columns = [
  { title: 'ID', key: 'id', width: 60 },
  { title: '标题', key: 'title', ellipsis: true },
  { title: '图片', key: 'imageUrl', ellipsis: true },
  { title: '排序', key: 'sort', width: 60 },
  {
    title: '状态', key: 'status', width: 70,
    render(row: SysBanner) {
      return h(NTag, { type: row.status === 1 ? 'success' : 'default', size: 'small' }, { default: () => row.status === 1 ? '显示' : '隐藏' })
    },
  },
  { title: '创建时间', key: 'createTime', width: 170 },
  {
    title: '操作', key: 'actions', width: 200,
    render(row: SysBanner) {
      return h(NSpace, null, {
        default: () => [
          h(NButton, { size: 'small', type: 'primary', onClick: () => openEdit(row) }, { default: () => '编辑' }),
          h(NButton, { size: 'small', type: 'error', onClick: () => doDelete(row) }, { default: () => '删除' }),
        ],
      })
    },
  },
]

function handlePageChange(page: number) { query.page = page; loadData() }

async function loadData() {
  loading.value = true
  try {
    const res = await getBanners(query.page, query.size)
    if (res.code === 200) {
      list.value = res.data.records
      pagination.pageCount = Math.ceil(res.data.total / res.data.size)
    }
  } catch { message.error('加载失败') }
  finally { loading.value = false }
}

function openCreate() {
  isEdit.value = false; editId.value = 0
  form.title = ''; form.imageUrl = ''; form.linkUrl = ''; form.sort = 0; form.status = 1
  showModal.value = true
}

async function openEdit(row: SysBanner) {
  try {
    const res = await getBanner(row.id)
    if (res.code === 200) {
      const d = res.data
      isEdit.value = true; editId.value = d.id
      form.title = d.title || ''; form.imageUrl = d.imageUrl; form.linkUrl = d.linkUrl || ''
      form.sort = d.sort; form.status = d.status
      showModal.value = true
    }
  } catch { message.error('加载详情失败') }
}

async function save() {
  if (!form.imageUrl) { message.warning('请填写图片URL'); return }
  saving.value = true
  try {
    const payload = { title: form.title || null, imageUrl: form.imageUrl, linkUrl: form.linkUrl || null, sort: form.sort, status: form.status }
    const res = isEdit.value ? await updateBanner(editId.value, payload) : await createBanner(payload)
    if (res.code === 200) {
      message.success(isEdit.value ? '更新成功' : '创建成功')
      showModal.value = false; loadData()
    }
  } catch { message.error('保存失败') }
  finally { saving.value = false }
}

function doDelete(row: SysBanner) {
  dialog.warning({
    title: '确认删除', content: `确定要删除该轮播图吗？`,
    positiveText: '确定', negativeText: '取消',
    onPositiveClick: async () => {
      try {
        const res = await deleteBanner(row.id)
        if (res.code === 200) { message.success('删除成功'); loadData() }
      } catch { message.error('删除失败') }
    },
  })
}

onMounted(loadData)
</script>
