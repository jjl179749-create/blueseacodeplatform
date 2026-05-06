<template>
  <div class="resource-page">
    <div class="page-header">
      <h1>资源分享</h1>
      <router-link to="/resources/create">
        <n-button type="primary">发布资源</n-button>
      </router-link>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <n-input
        v-model:value="searchKeyword"
        placeholder="搜索资源标题或描述..."
        clearable
        style="width: 300px"
        @keyup.enter="handleSearch"
      >
        <template #prefix>
          <n-icon><span>🔍</span></n-icon>
        </template>
      </n-input>
      <n-select
        v-model:value="query.categoryId"
        :options="categoryOptions"
        placeholder="全部分类"
        clearable
        style="width: 160px"
        @update:value="loadResources"
      />
      <n-select
        v-model:value="query.sortBy"
        :options="sortOptions"
        placeholder="排序"
        style="width: 140px"
        @update:value="loadResources"
      />
    </div>

    <!-- 资源列表 -->
    <div v-if="loading" class="loading-center">
      <n-spin size="large" />
    </div>

    <div v-else-if="resources.length === 0" class="empty-state">
      <n-empty description="暂无资源" />
    </div>

    <n-grid v-else :cols="3" :x-gap="20" :y-gap="20">
      <!-- 引入 NAvatar -->
      <n-grid-item v-for="item in resources" :key="item.id">
        <n-card hoverable size="small" class="resource-card" @click="goDetail(item.id)">
          <template #cover>
            <div class="resource-cover">
              <img
                v-if="item.coverImage"
                :src="item.coverImage"
                alt="封面"
                class="cover-img"
              />
              <div v-else class="cover-placeholder">
                <span class="placeholder-icon">📦</span>
                <span class="placeholder-text">{{ item.categoryName || '资源' }}</span>
              </div>
            </div>
          </template>
          <div class="resource-card-title">{{ item.title }}</div>
          <div class="resource-meta">
            <n-tag size="tiny" :bordered="false" round>{{ item.categoryName }}</n-tag>
            <n-tag v-for="tag in item.tags?.slice(0, 2)" :key="tag" size="tiny" type="info" :bordered="false" round>{{ tag }}</n-tag>
          </div>
          <div class="resource-stats">
            <span class="stat-item"><span class="stat-icon">👁</span>{{ item.viewCount }}</span>
            <span class="stat-item"><span class="stat-icon">📥</span>{{ item.downloadCount }}</span>
            <span class="stat-item"><span class="stat-icon">💬</span>{{ item.commentCount }}</span>
          </div>
          <div class="resource-footer">
            <span class="author-row" @click.stop="(e) => goToUser(e, item.userId)">
              <n-avatar
                v-if="item.avatar"
                :src="item.avatar"
                size="20"
                round
                style="margin-right:4px;vertical-align:middle"
              />
              <span>{{ item.nickname || '匿名' }}</span>
            </span>
            <span v-if="item.downloadPoints > 0" class="points-badge">{{ item.downloadPoints }} 积分</span>
            <span v-else class="free-badge">免费</span>
          </div>
          <div class="resource-time-row">{{ formatDate(item.createTime) }}</div>
        </n-card>
      </n-grid-item>
    </n-grid>

    <!-- 分页 -->
    <div v-if="total > 0" class="pagination">
      <n-pagination
        v-model:page="query.page"
        :page-size="query.size"
        :item-count="total"
        @update:page="loadResources"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import { NAvatar } from 'naive-ui'
import {
  getResources,
  getCategories,
  type ResResource,
  type ResCategory,
  type ResourceQueryParams,
} from '@/api/resource'

const router = useRouter()
const message = useMessage()

const loading = ref(false)
const resources = ref<ResResource[]>([])
const categories = ref<ResCategory[]>([])
const total = ref(0)
const searchKeyword = ref('')

const query = reactive<ResourceQueryParams>({
  page: 1,
  size: 12,
  sortBy: 'latest',
})

const categoryOptions = computed(() => {
  return [
    { label: '全部分类', value: null },
    ...categories.value.map((c) => ({
      label: c.name,
      value: c.id,
    })),
  ]
})

const sortOptions = [
  { label: '最新发布', value: 'latest' },
  { label: '最多下载', value: 'download' },
  { label: '最多浏览', value: 'popular' },
]

async function loadCategories() {
  try {
    const res = await getCategories()
    if (res.code === 200) {
      categories.value = res.data || []
    }
  } catch {
    // ignore
  }
}

async function loadResources() {
  loading.value = true
  try {
    const params: ResourceQueryParams = {
      page: query.page,
      size: query.size,
      sortBy: query.sortBy,
    }
    if (query.categoryId) params.categoryId = query.categoryId
    if (searchKeyword.value) params.keyword = searchKeyword.value

    const res = await getResources(params)
    if (res.code === 200 && res.data) {
      resources.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch {
    message.error('加载资源列表失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  query.page = 1
  loadResources()
}

function goDetail(id: number) {
  router.push(`/resources/${id}`)
}

function goToUser(e: MouseEvent, userId: number) {
  e.stopPropagation()
  router.push(`/user/${userId}`)
}

function formatDate(dateStr: string | null) {
  if (!dateStr) return '-'
  return dateStr.replace('T', ' ').substring(0, 19)
}

onMounted(() => {
  loadCategories()
  loadResources()
})
</script>

<style scoped>
.resource-page {
  max-width: 1200px;
  margin: 0 auto;
}
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}
.page-header h1 {
  font-size: 24px;
  margin: 0;
}
.search-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
  align-items: center;
}
.loading-center {
  display: flex;
  justify-content: center;
  padding: 80px 0;
}
.empty-state {
  padding: 80px 0;
  display: flex;
  justify-content: center;
}
.resource-card {
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
  border-radius: 12px;
  overflow: hidden;
}
.resource-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}
.resource-cover {
  height: 150px;
  overflow: hidden;
  background: #f5f7fa;
}
.cover-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}
.resource-card:hover .cover-img {
  transform: scale(1.08);
}
.cover-placeholder {
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
.placeholder-icon {
  font-size: 32px;
}
.placeholder-text {
  color: rgba(255,255,255,0.8);
  font-size: 13px;
}
.resource-card-title {
  font-size: 15px;
  font-weight: 600;
  color: #1a1a2e;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.resource-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  margin-bottom: 8px;
}
.resource-stats {
  display: flex;
  gap: 14px;
  font-size: 12px;
  color: #999;
  margin-bottom: 8px;
}
.stat-item {
  display: flex;
  align-items: center;
  gap: 2px;
}
.stat-icon {
  font-size: 13px;
}
.resource-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;
}
.author-row {
  color: #666;
  cursor: pointer;
  display: flex;
  align-items: center;
}
.author-row:hover {
  color: #18a058;
}
.points-badge {
  color: #f59e0b;
  font-weight: 600;
  font-size: 12px;
  background: #fffbeb;
  padding: 2px 8px;
  border-radius: 10px;
}
.free-badge {
  color: #18a058;
  font-weight: 600;
  font-size: 12px;
  background: #f0fdf4;
  padding: 2px 8px;
  border-radius: 10px;
}
.resource-time-row {
  font-size: 11px;
  color: #ddd;
  margin-top: 4px;
}
.pagination {
  display: flex;
  justify-content: center;
  margin-top: 32px;
  padding: 24px 0;
}
</style>
