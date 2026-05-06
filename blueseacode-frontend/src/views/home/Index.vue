<template>
  <div class="home-page">
    <!-- ===== Hero 区域 ===== -->
    <div class="hero-section">
      <div class="hero-bg"></div>
      <div class="hero-content">
        <h1 class="hero-title">蓝海编程平台</h1>
        <p class="hero-desc">编程爱好者的综合社区 — 分享资源、提升技能、对接需求</p>
        <div class="hero-stats">
          <div class="hero-stat">
            <span class="hero-stat-num">{{ stats.resources }}</span>
            <span class="hero-stat-label">资源</span>
          </div>
          <div class="hero-stat">
            <span class="hero-stat-num">{{ stats.articles }}</span>
            <span class="hero-stat-label">文章</span>
          </div>
          <div class="hero-stat">
            <span class="hero-stat-num">{{ stats.demands }}</span>
            <span class="hero-stat-label">需求</span>
          </div>
          <div class="hero-stat">
            <span class="hero-stat-num">{{ stats.users }}</span>
            <span class="hero-stat-label">用户</span>
          </div>
        </div>
        <div class="hero-actions">
          <n-button type="primary" size="large" round @click="router.push('/resources')">
            浏览资源
          </n-button>
          <n-button size="large" ghost round style="margin-left: 16px" @click="router.push('/articles')">
            阅读文章
          </n-button>
        </div>
      </div>
    </div>

    <div class="home-body">
      <!-- ===== 推荐资源 ===== -->
      <section class="section">
        <div class="section-header">
          <h2 class="section-title">🔥 热门资源</h2>
          <n-button text type="primary" @click="router.push('/resources')">查看全部 →</n-button>
        </div>
        <div v-if="loadingResources" class="loading-center">
          <n-spin size="medium" />
        </div>
        <n-grid v-else :cols="4" :x-gap="16" :y-gap="16" :collapsed="true" :cols-s="2" :cols-m="3">
          <n-grid-item v-for="item in hotResources" :key="item.id">
            <n-card hoverable class="recommend-card" size="small" @click="router.push('/resources/' + item.id)">
              <template #cover>
                <div class="recommend-cover">
                  <img v-if="item.coverImage" :src="item.coverImage" alt="" class="cover-img" />
                  <div v-else class="cover-placeholder">
                    <span class="placeholder-icon">📦</span>
                  </div>
                </div>
              </template>
              <div class="recommend-title">{{ item.title }}</div>
              <div class="recommend-meta">
                <n-tag size="tiny" :bordered="false">{{ item.categoryName }}</n-tag>
                <span class="meta-item">👁 {{ item.viewCount }}</span>
                <span class="meta-item">📥 {{ item.downloadCount }}</span>
              </div>
            </n-card>
          </n-grid-item>
        </n-grid>
      </section>

      <!-- ===== 热门文章 ===== -->
      <section class="section">
        <div class="section-header">
          <h2 class="section-title">📝 热门文章</h2>
          <n-button text type="primary" @click="router.push('/articles')">查看全部 →</n-button>
        </div>
        <div v-if="loadingArticles" class="loading-center">
          <n-spin size="medium" />
        </div>
        <div v-else class="article-list">
          <div v-for="(item, idx) in hotArticles" :key="item.id" class="article-row" @click="router.push('/articles/' + item.id)">
            <span class="article-rank" :class="{ 'rank-top': idx < 3 }">{{ idx + 1 }}</span>
            <div class="article-row-body">
              <div class="article-row-title">{{ item.title }}</div>
              <div class="article-row-meta">
                <span>👤 {{ item.nickname || '匿名' }}</span>
                <span>👁 {{ item.viewCount }}</span>
                <span>👍 {{ item.likeCount }}</span>
                <span>💬 {{ item.commentCount }}</span>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- ===== 最新需求 ===== -->
      <section class="section">
        <div class="section-header">
          <h2 class="section-title">🎯 最新需求</h2>
          <n-button text type="primary" @click="router.push('/demands')">查看全部 →</n-button>
        </div>
        <div v-if="loadingDemands" class="loading-center">
          <n-spin size="medium" />
        </div>
        <n-grid v-else :cols="2" :x-gap="16" :y-gap="16">
          <n-grid-item v-for="item in latestDemands" :key="item.id">
            <n-card hoverable size="small" class="demand-card" @click="router.push('/demands/' + item.id)">
              <div class="demand-card-header">
                <n-tag :type="DEMAND_STATUS_TAG[item.status] || 'default'" size="tiny" round>{{ DEMAND_STATUS_MAP[item.status] }}</n-tag>
                <n-tag v-if="item.budgetMin != null || item.budgetMax != null" size="tiny" type="warning" round>
                  {{ formatBudget(item.budgetMin, item.budgetMax) }}
                </n-tag>
              </div>
              <div class="demand-card-title">{{ item.title }}</div>
              <div class="demand-card-desc">{{ item.description?.substring(0, 80) }}</div>
              <div class="demand-card-meta">
                <span>👤 {{ item.nickname || '匿名' }}</span>
                <span>👁 {{ item.viewCount }}</span>
                <span>📋 {{ item.orderCount }}</span>
              </div>
            </n-card>
          </n-grid-item>
        </n-grid>
      </section>
    </div>

    <!-- ===== 页脚 ===== -->
    <footer class="site-footer">
      <div class="footer-inner">
        <div class="footer-col">
          <h4>蓝海编程平台</h4>
          <p>编程爱好者的综合社区</p>
        </div>
        <div class="footer-col">
          <h4>快速链接</h4>
          <a @click="router.push('/resources')">资源分享</a>
          <a @click="router.push('/articles')">编程提升</a>
          <a @click="router.push('/demands')">需求发布</a>
          <a @click="router.push('/chat')">AI 客服</a>
        </div>
        <div class="footer-col">
          <h4>帮助与支持</h4>
          <a @click="router.push('/profile')">个人中心</a>
          <a @click="router.push('/messages')">消息中心</a>
        </div>
        <div class="footer-col">
          <h4>关于我们</h4>
          <p>蓝海编程平台 © 2026</p>
          <p>让代码创造价值</p>
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getResources, type ResResource } from '@/api/resource'
import { getHotArticles, type ArtArticle } from '@/api/article'
import { getDemands, DEMAND_STATUS_MAP, DEMAND_STATUS_TAG, type DemDemand } from '@/api/demand'
import { getProfile } from '@/api/user'

const router = useRouter()

const loadingResources = ref(true)
const loadingArticles = ref(true)
const loadingDemands = ref(true)

const hotResources = ref<ResResource[]>([])
const hotArticles = ref<ArtArticle[]>([])
const latestDemands = ref<DemDemand[]>([])

const stats = ref({
  resources: 0,
  articles: 0,
  demands: 0,
  users: 0,
})

function formatBudget(min: number | null, max: number | null) {
  if (min != null && max != null) return `¥${min}-${max}`
  if (min != null) return `¥${min}起`
  if (max != null) return `最高¥${max}`
  return '预算面议'
}

onMounted(async () => {
  // 并行加载首页数据
  const [resRes, artRes, demRes] = await Promise.all([
    getResources({ page: 1, size: 8, sortBy: 'popular' }),
    getHotArticles(),
    getDemands({ page: 1, size: 6, sortBy: 'latest' }),
  ])

  if (resRes.code === 200 && resRes.data) {
    hotResources.value = resRes.data.records || []
    stats.value.resources = resRes.data.total || 0
    loadingResources.value = false
  }

  if (artRes.code === 200 && artRes.data) {
    hotArticles.value = artRes.data || []
    stats.value.articles = hotArticles.value.length
    loadingArticles.value = false
  }

  if (demRes.code === 200 && demRes.data) {
    latestDemands.value = demRes.data.records || []
    stats.value.demands = demRes.data.total || 0
    loadingDemands.value = false
  }
})
</script>

<style scoped>
/* ===== Hero ===== */
.hero-section {
  position: relative;
  min-height: 360px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  border-radius: 16px;
  margin-bottom: 32px;
}
.hero-bg {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, #0f0c29 0%, #302b63 50%, #24243e 100%);
  z-index: 0;
}
.hero-bg::after {
  content: '';
  position: absolute;
  inset: 0;
  background: radial-gradient(circle at 30% 50%, rgba(24, 160, 88, 0.15) 0%, transparent 60%),
              radial-gradient(circle at 70% 50%, rgba(24, 144, 255, 0.1) 0%, transparent 60%);
}
.hero-content {
  position: relative;
  z-index: 1;
  text-align: center;
  padding: 60px 40px;
  max-width: 800px;
}
.hero-title {
  font-size: 42px;
  font-weight: 800;
  color: #fff;
  margin: 0 0 12px;
  letter-spacing: -0.5px;
}
.hero-desc {
  font-size: 18px;
  color: rgba(255, 255, 255, 0.75);
  margin: 0 0 32px;
}
.hero-stats {
  display: flex;
  justify-content: center;
  gap: 48px;
  margin-bottom: 36px;
}
.hero-stat {
  display: flex;
  flex-direction: column;
  align-items: center;
}
.hero-stat-num {
  font-size: 28px;
  font-weight: 700;
  color: #fff;
}
.hero-stat-label {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.6);
  margin-top: 2px;
}
.hero-actions {
  display: flex;
  justify-content: center;
}

/* ===== 首页主体 ===== */
.home-body {
  max-width: 1200px;
  margin: 0 auto;
}
.section {
  margin-bottom: 40px;
}
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.section-title {
  font-size: 22px;
  font-weight: 700;
  margin: 0;
  color: #1a1a2e;
}

/* ===== 推荐资源卡片 ===== */
.recommend-card {
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}
.recommend-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}
.recommend-cover {
  height: 120px;
  overflow: hidden;
  background: #f0f2f5;
}
.cover-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.cover-placeholder {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
.placeholder-icon {
  font-size: 36px;
}
.recommend-title {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin-bottom: 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.recommend-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: #999;
}

/* ===== 热门文章排行 ===== */
.article-list {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}
.article-row {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 14px 20px;
  cursor: pointer;
  transition: background 0.2s;
  border-bottom: 1px solid #f5f5f5;
}
.article-row:last-child {
  border-bottom: none;
}
.article-row:hover {
  background: #f8f9ff;
}
.article-rank {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 700;
  color: #999;
  background: #f5f5f5;
  flex-shrink: 0;
}
.rank-top {
  color: #fff;
  background: linear-gradient(135deg, #f59e0b, #f97316);
}
.article-row-body {
  flex: 1;
  min-width: 0;
}
.article-row-title {
  font-size: 15px;
  font-weight: 500;
  color: #333;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.article-row-meta {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: #bbb;
}

/* ===== 需求卡片 ===== */
.demand-card {
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}
.demand-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}
.demand-card-header {
  display: flex;
  gap: 6px;
  margin-bottom: 8px;
}
.demand-card-title {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  margin-bottom: 6px;
}
.demand-card-desc {
  font-size: 13px;
  color: #999;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}
.demand-card-meta {
  display: flex;
  gap: 14px;
  font-size: 12px;
  color: #bbb;
}

.loading-center {
  display: flex;
  justify-content: center;
  padding: 40px 0;
}

/* ===== 页脚 ===== */
.site-footer {
  margin-top: 60px;
  background: #1a1a2e;
  border-radius: 16px 16px 0 0;
  padding: 48px 0 32px;
}
.footer-inner {
  max-width: 1200px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 32px;
  padding: 0 24px;
}
.footer-col h4 {
  color: #fff;
  font-size: 16px;
  margin: 0 0 16px;
}
.footer-col p, .footer-col a {
  color: rgba(255, 255, 255, 0.6);
  font-size: 14px;
  margin: 0 0 8px;
  line-height: 1.8;
  display: block;
  cursor: pointer;
  text-decoration: none;
}
.footer-col a:hover {
  color: #18a058;
}
</style>
