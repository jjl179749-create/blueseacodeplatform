<template>
  <div class="demand-management">
    <h2 style="margin-bottom: 20px">需求管理</h2>

    <n-card style="margin-bottom: 16px">
      <n-space>
        <n-input v-model:value="query.keyword" placeholder="搜索需求标题" clearable style="width: 200px" @keyup.enter="search" />
        <n-select v-model:value="query.status" :options="statusOptions" placeholder="状态" clearable style="width: 140px" />
        <n-select v-model:value="query.category" :options="categoryOptions" placeholder="分类" clearable style="width: 140px" />
        <n-button type="primary" @click="search">搜索</n-button>
        <n-button @click="reset">重置</n-button>
      </n-space>
    </n-card>

    <n-card>
      <n-data-table :columns="columns" :data="list" :loading="loading" :pagination="pagination" @update:page="(p: number) => { query.page = p; loadData() }" />
    </n-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, h, onMounted } from 'vue'
import { NButton, NSpace, NTag, useMessage } from 'naive-ui'
import {
  getAdminDemands,
  auditDemand,
} from '@/api/admin'
import {
  DEMAND_STATUS_MAP,
  DEMAND_STATUS_TAG,
  DEMAND_CATEGORIES,
} from '@/api/demand'

const message = useMessage()
const loading = ref(false)
const list = ref<any[]>([])

const query = reactive({ page: 1, size: 10, keyword: '', status: undefined as number | undefined, category: '' })
const statusOptions = Object.entries(DEMAND_STATUS_MAP).map(([k, v]) => ({ label: v, value: Number(k) }))
const categoryOptions = DEMAND_CATEGORIES.map((c) => ({ label: c.label, value: c.value }))

const pagination = reactive({ page: 1, pageCount: 1, pageSize: 10, onChange: (p: number) => { query.page = p; loadData() } })

const columns = [
  { title: 'ID', key: 'id', width: 60 },
  { title: '标题', key: 'title', ellipsis: true },
  { title: '发布人', key: 'nickname', width: 100 },
  {
    title: '分类', key: 'category', width: 90,
    render(row: any) {
      const c = DEMAND_CATEGORIES.find((x) => x.value === row.category)
      return c ? c.label : row.category
    },
  },
  {
    title: '状态', key: 'status', width: 80,
    render(row: any) {
      return h(NTag, { type: (DEMAND_STATUS_TAG as any)[row.status], size: 'small' }, { default: () => DEMAND_STATUS_MAP[row.status] || '未知' })
    },
  },
  { title: '预算', key: 'budgetMin', width: 120, render(row: any) { return row.budgetMin != null ? `¥${row.budgetMin} - ¥${row.budgetMax}` : '-' } },
  { title: '创建时间', key: 'createTime', width: 170 },
  {
    title: '操作', key: 'actions', width: 120,
    render(row: any) {
      return h(NSpace, null, {
        default: () => [
          h(NButton, { size: 'small', type: 'primary', onClick: () => toggleAudit(row) }, { default: () => row.status === 0 ? '审核' : '复审' }),
        ],
      })
    },
  },
]

function search() { query.page = 1; loadData() }
function reset() { query.keyword = ''; query.status = undefined; query.category = ''; query.page = 1; loadData() }

async function loadData() {
  loading.value = true
  try {
    const res = await getAdminDemands({ page: query.page, size: query.size, keyword: query.keyword || undefined, status: query.status, category: query.category || undefined })
    if (res.code === 200) {
      list.value = res.data.records
      pagination.pageCount = Math.ceil(res.data.total / res.data.size)
    }
  } catch { message.error('加载失败') }
  finally { loading.value = false }
}

function toggleAudit(row: any) {
  const newStatus = row.status === 0 ? 1 : 0
  const actionText = newStatus === 1 ? '通过' : '驳回为待审核'
  message.warning(`此操作将通过API：设为${actionText}`, { duration: 3000 })
  // 简化：直接切换审核状态
  auditDemand(row.id, newStatus, undefined).then((res) => {
    if (res.code === 200) { message.success('操作成功'); loadData() }
  }).catch(() => message.error('操作失败'))
}

onMounted(loadData)
</script>
