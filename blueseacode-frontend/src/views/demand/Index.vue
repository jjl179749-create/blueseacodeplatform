<template>
  <div class="demand-list-page">
    <n-card :bordered="true" class="list-card">
      <template #header>
        <div class="page-header">
          <h2>需求市场</h2>
          <n-button type="primary" @click="router.push('/demands/create')">
            <template #icon>
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
            </template>
            发布需求
          </n-button>
        </div>
      </template>

      <!-- 筛选栏 -->
      <n-space vertical>
        <n-space align="center">
          <n-tag :type="categoryFilter === '' ? 'primary' : 'default'" style="cursor:pointer" @click="categoryFilter = ''">全部</n-tag>
          <n-tag v-for="cat in DEMAND_CATEGORIES" :key="cat.value"
            :type="categoryFilter === cat.value ? 'primary' : 'default'"
            style="cursor:pointer"
            @click="categoryFilter = cat.value"
          >
            {{ cat.label }}
          </n-tag>
        </n-space>

        <n-space align="center">
          <n-input v-model:value="keyword" placeholder="搜索需求..." clearable style="width:300px" @keyup.enter="search" />
          <n-select
            v-model:value="sortBy"
            :options="sortOptions"
            style="width:140px"
            @update:value="loadData"
          />
        </n-space>
      </n-space>

      <n-divider />

      <!-- 加载状态 -->
      <div v-if="loading" class="loading-center">
        <n-spin size="large" />
      </div>

      <!-- 列表 -->
      <template v-else-if="demands.length > 0">
        <div v-for="item in demands" :key="item.id" class="demand-item" @click="router.push('/demands/' + item.id)">
          <div class="demand-main">
            <div class="demand-title-row">
              <h3 class="demand-title">{{ item.title }}</h3>
              <n-tag :type="DEMAND_STATUS_TAG[item.status] || 'default'" size="small" round>
                {{ DEMAND_STATUS_MAP[item.status] || '未知' }}
              </n-tag>
            </div>
            <div class="demand-category">
              {{ categoryLabel(item.category) }}
              <template v-if="item.budgetMin != null || item.budgetMax != null">
                · {{ formatBudget(item.budgetMin, item.budgetMax) }}
              </template>
            </div>
            <div class="demand-desc">{{ item.description?.substring(0, 200) }}</div>
            <div class="demand-meta">
              <span>👤 {{ item.nickname || '匿名' }}</span>
              <span>👁 {{ item.viewCount }}</span>
              <span>📋 {{ item.orderCount }} 人接单</span>
              <span v-if="item.deadline">⏰ {{ formatDate(item.deadline) }}</span>
              <span>📅 {{ formatDate(item.createTime) }}</span>
            </div>
          </div>
        </div>

        <!-- 分页 -->
        <div v-if="total > 0" class="pagination">
          <n-pagination
            v-model:page="page"
            :page-size="size"
            :item-count="total"
            @update:page="loadData"
          />
        </div>
      </template>

      <n-empty v-else description="暂无需求" />
    </n-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import {
  getDemands,
  DEMAND_CATEGORIES,
  DEMAND_STATUS_MAP,
  DEMAND_STATUS_TAG,
  type DemDemand,
} from '@/api/demand'

const router = useRouter()
const message = useMessage()

const demands = ref<DemDemand[]>([])
const loading = ref(true)
const total = ref(0)
const page = ref(1)
const size = 10
const keyword = ref('')
const categoryFilter = ref('')
const sortBy = ref('latest')

const sortOptions = [
  { label: '最新发布', value: 'latest' },
  { label: '预算最高', value: 'budget' },
  { label: '即将截止', value: 'deadline' },
]

function categoryLabel(value: string) {
  const cat = DEMAND_CATEGORIES.find((c) => c.value === value)
  return cat ? cat.label : value
}

function formatBudget(min: number | null, max: number | null) {
  if (min != null && max != null) return `¥${min} - ¥${max}`
  if (min != null) return `¥${min}起`
  if (max != null) return `最高¥${max}`
  return '预算面议'
}

function formatDate(dateStr: string | null) {
  if (!dateStr) return '-'
  return dateStr.replace('T', ' ').substring(0, 16)
}

function search() {
  page.value = 1
  loadData()
}

async function loadData() {
  loading.value = true
  try {
    const res = await getDemands({
      page: page.value,
      size,
      keyword: keyword.value || undefined,
      category: categoryFilter.value || undefined,
      sortBy: sortBy.value,
    })
    if (res.code === 200 && res.data) {
      demands.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch {
    message.error('加载需求列表失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.demand-list-page {
  max-width: 1000px;
  margin: 0 auto;
}
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.page-header h2 {
  margin: 0;
  font-size: 20px;
}
.loading-center {
  display: flex;
  justify-content: center;
  padding: 60px 0;
}
.demand-item {
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background 0.2s;
}
.demand-item:hover {
  background: #fafafa;
}
.demand-item:last-child {
  border-bottom: none;
}
.demand-title-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}
.demand-title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #333;
}
.demand-category {
  font-size: 13px;
  color: #999;
  margin-bottom: 6px;
}
.demand-desc {
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
  line-height: 1.5;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}
.demand-meta {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: #bbb;
}
.pagination {
  display: flex;
  justify-content: center;
  padding: 20px 0;
}
</style>
