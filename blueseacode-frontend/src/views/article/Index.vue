<template>
  <div class="article-page">
    <div class="page-header">
      <h1>编程提升</h1>
      <router-link to="/articles/create">
        <n-button type="primary">写文章</n-button>
      </router-link>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <n-input
        v-model:value="searchKeyword"
        placeholder="搜索文章标题或摘要..."
        clearable
        style="width: 320px"
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
        @update:value="loadArticles"
      />
      <n-select
        v-model:value="query.sortBy"
        :options="sortOptions"
        placeholder="排序"
        style="width: 140px"
        @update:value="loadArticles"
      />
    </div>

    <!-- 文章列表 -->
    <div v-if="loading" class="loading-center">
      <n-spin size="large" />
    </div>

    <div v-else-if="articles.length === 0" class="empty-state">
      <n-empty description="暂无文章" />
    </div>

    <n-grid v-else :cols="2" :x-gap="20" :y-gap="20">
      <n-grid-item v-for="item in articles" :key="item.id">
        <n-card hoverable class="article-card" @click="goDetail(item.id)">
          <div class="article-card-inner">
            <div class="article-cover-wrap">
              <img v-if="item.coverImage" :src="item.coverImage" alt="封面" class="article-cover-img" />
              <div v-else class="article-cover-placeholder">
                <span>📝</span>
              </div>
              <div class="article-category-tag">
                <n-tag size="tiny" :bordered="false" round>{{ item.categoryName || '未分类' }}</n-tag>
              </div>
            </div>
            <div class="article-card-body">
              <h3 class="article-card-title">{{ item.title }}</h3>
              <p class="article-card-summary">{{ item.summary || '暂无摘要' }}</p>
              <div class="article-tags" v-if="item.tags?.length">
                <n-tag v-for="tag in item.tags.slice(0, 3)" :key="tag" size="tiny" type="info" :bordered="false" round>{{ tag }}</n-tag>
              </div>
              <div class="article-card-footer">
                <span class="article-author" @click.stop="(e) => goToUser(e, item.userId)">
                  <n-avatar v-if="item.avatar" :src="item.avatar" size="22" round style="margin-right:4px;vertical-align:middle" />
                  <span>{{ item.nickname || '匿名' }}</span>
                </span>
                <div class="article-card-stats">
                  <span>👁 {{ item.viewCount }}</span>
                  <span>👍 {{ item.likeCount }}</span>
                  <span>💬 {{ item.commentCount }}</span>
                </div>
              </div>
              <div class="article-card-time">{{ formatDate(item.createTime) }}</div>
            </div>
          </div>
        </n-card>
      </n-grid-item>
    </n-grid>

    <!-- 分页 -->
    <div v-if="total > 0" class="pagination">
      <n-pagination
        v-model:page="query.page"
        :page-size="query.size"
        :item-count="total"
        @update:page="loadArticles"
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
  getArticles,
  getArticleCategories,
  type ArtArticle,
  type ArtCategory,
  type ArticleQueryParams,
} from '@/api/article'

const router = useRouter()
const message = useMessage()

const loading = ref(false)
const articles = ref<ArtArticle[]>([])
const categories = ref<ArtCategory[]>([])
const total = ref(0)
const searchKeyword = ref('')

const query = reactive<ArticleQueryParams>({
  page: 1,
  size: 10,
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
  { label: '最多浏览', value: 'hottest' },
]

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

async function loadArticles() {
  loading.value = true
  try {
    const params: ArticleQueryParams = {
      page: query.page,
      size: query.size,
      sortBy: query.sortBy,
    }
    if (query.categoryId) params.categoryId = query.categoryId
    if (searchKeyword.value) params.keyword = searchKeyword.value

    const res = await getArticles(params)
    if (res.code === 200 && res.data) {
      articles.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch {
    message.error('加载文章列表失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  query.page = 1
  loadArticles()
}

function goDetail(id: number) {
  router.push(`/articles/${id}`)
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
  loadArticles()
})
</script>

<style scoped>
.article-page {
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
.article-card {
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
  border-radius: 12px;
  overflow: hidden;
}
.article-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}
.article-card-inner {
  display: flex;
  gap: 0;
  flex-direction: column;
}
.article-cover-wrap {
  position: relative;
  width: 100%;
  height: 180px;
  overflow: hidden;
  background: #f0f2f5;
}
.article-cover-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}
.article-card:hover .article-cover-img {
  transform: scale(1.08);
}
.article-cover-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 48px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
.article-category-tag {
  position: absolute;
  top: 12px;
  left: 12px;
}
.article-card-body {
  padding: 16px;
}
.article-card-title {
  font-size: 18px;
  font-weight: 600;
  margin: 0 0 8px;
  color: #1a1a2e;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.article-card-summary {
  font-size: 13px;
  color: #999;
  margin: 0 0 10px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.5;
}
.article-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  margin-bottom: 10px;
}
.article-card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;
}
.article-author {
  display: flex;
  align-items: center;
  cursor: pointer;
  color: #666;
}
.article-author:hover {
  color: #18a058;
}
.article-card-stats {
  display: flex;
  gap: 12px;
  font-size: 12px;
  color: #bbb;
}
.article-card-time {
  font-size: 11px;
  color: #ddd;
  margin-top: 6px;
}
.pagination {
  display: flex;
  justify-content: center;
  margin-top: 32px;
  padding: 24px 0;
}
</style>
