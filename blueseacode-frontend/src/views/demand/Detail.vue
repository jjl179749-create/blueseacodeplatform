<template>
  <div class="detail-page">
    <n-grid :cols="24" :x-gap="24">
      <!-- 主要内容区 -->
      <n-grid-item :span="17">
        <div v-if="loading" class="loading-center">
          <n-spin size="large" />
        </div>

        <template v-else-if="demand">
          <n-card :bordered="true" class="detail-card">
            <template #header>
              <div class="detail-header">
                <div>
                  <h1>{{ demand.title }}</h1>
                  <n-tag :type="DEMAND_STATUS_TAG[demand.status] || 'default'" size="small" round>
                    {{ DEMAND_STATUS_MAP[demand.status] || '未知' }}
                  </n-tag>
                </div>
                <div class="header-actions">
                  <!-- 发布者操作：待审状态可编辑 -->
                  <n-button v-if="demand.userId === currentUserId && demand.status === 0" size="small" @click="router.push('/demands/' + demand.id + '/edit')">
                    编辑
                  </n-button>
                  <!-- 发布者操作：进行中可确认完成 -->
                  <n-button
                    v-if="demand.userId === currentUserId && demand.status === 2"
                    type="primary"
                    size="small"
                    @click="handleComplete"
                    :loading="completing"
                  >
                    确认完成
                  </n-button>
                  <!-- 接单按钮（仅招募中状态，非发布者本人） -->
                  <n-button
                    v-if="demand.status === 1 && demand.userId !== currentUserId"
                    type="success"
                    size="small"
                    @click="handleTakeOrder"
                    :loading="takingOrder"
                  >
                    立即接单
                  </n-button>
                </div>
              </div>
            </template>

            <!-- 需求基本信息 -->
            <n-descriptions :column="2" label-placement="left" bordered size="small">
              <n-descriptions-item label="分类">{{ categoryLabel(demand.category) }}</n-descriptions-item>
              <n-descriptions-item label="预算">
                {{ formatBudget(demand.budgetMin, demand.budgetMax) }}
              </n-descriptions-item>
              <n-descriptions-item label="截止日期">{{ demand.deadline ? formatDate(demand.deadline) : '无' }}</n-descriptions-item>
              <n-descriptions-item label="联系方式">{{ demand.contact || '私信联系' }}</n-descriptions-item>
              <n-descriptions-item label="浏览数">{{ demand.viewCount }}</n-descriptions-item>
              <n-descriptions-item label="接单数">{{ demand.orderCount }}</n-descriptions-item>
            </n-descriptions>

            <n-divider />

            <!-- 需求描述 -->
            <div class="demand-content markdown-body" v-html="renderedContent"></div>

            <!-- 附件 -->
            <template v-if="demand.attachments && demand.attachments.length > 0">
              <n-divider />
              <h3>附件</h3>
              <n-space>
                <n-tag v-for="att in demand.attachments" :key="att.id" type="info" :bordered="false">
                  <a :href="att.fileUrl" target="_blank" style="text-decoration:none;color:inherit">
                    📎 {{ att.fileName }}
                  </a>
                </n-tag>
              </n-space>
            </template>
          </n-card>
        </template>

        <n-empty v-else description="需求不存在" />
      </n-grid-item>

      <!-- 侧边栏 -->
      <n-grid-item :span="7">
        <!-- 发布者信息 -->
        <n-card title="发布者" :bordered="true" class="sidebar-card">
          <router-link :to="'/user/' + demand?.userId" class="user-info-link">
            <div class="user-info">
              <n-avatar v-if="demand?.avatar" :src="demand.avatar" :size="60" round />
              <n-avatar v-else :size="60" round>{{ demand?.nickname?.charAt(0) || '?' }}</n-avatar>
              <div class="user-name">{{ demand?.nickname || '匿名' }}</div>
            </div>
          </router-link>
        </n-card>

        <!-- 接单者信息 -->
        <n-card v-if="demand?.takerId" title="接单者" :bordered="true" class="sidebar-card">
          <router-link :to="'/user/' + demand.takerId" class="user-info-link">
            <div class="user-info">
              <n-avatar v-if="demand.takerAvatar" :src="demand.takerAvatar" :size="60" round />
              <n-avatar v-else :size="60" round>{{ demand.takerNickname?.charAt(0) || '?' }}</n-avatar>
              <div class="user-name">{{ demand.takerNickname || '匿名' }}</div>
            </div>
          </router-link>
        </n-card>

        <!-- 统计 -->
        <n-card title="统计" :bordered="true" class="sidebar-card">
          <div class="stat-item"><span>浏览</span><span>{{ demand?.viewCount || 0 }}</span></div>
          <div class="stat-item"><span>接单</span><span>{{ demand?.orderCount || 0 }}</span></div>
        </n-card>
      </n-grid-item>
    </n-grid>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useMessage, useDialog } from 'naive-ui'
import {
  getDemandDetail,
  takeOrder,
  completeDemand,
  DEMAND_CATEGORIES,
  DEMAND_STATUS_MAP,
  DEMAND_STATUS_TAG,
  type DemDemand,
} from '@/api/demand'

const route = useRoute()
const router = useRouter()
const message = useMessage()
const dialog = useDialog()

const demand = ref<DemDemand | null>(null)
const loading = ref(true)
const takingOrder = ref(false)
const completing = ref(false)

const currentUserId = ref<number | null>(null)

// 解析当前用户ID（从token中）
function parseCurrentUserId(): number | null {
  try {
    const token = localStorage.getItem('accessToken')
    if (!token) return null
    const payload = JSON.parse(atob(token.split('.')[1]))
    return payload.userId || null
  } catch {
    return null
  }
}

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
  return dateStr.replace('T', ' ').substring(0, 19)
}

// 简单的 Markdown 渲染
const renderedContent = computed(() => {
  if (!demand.value?.description) return ''
  let html = demand.value.description
  html = html.replace(/</g, '&lt;').replace(/>/g, '&gt;')
  html = html.replace(/```(\w*)\n?([\s\S]*?)```/g, '<pre><code class="language-$1">$2</code></pre>')
  html = html.replace(/`([^`]+)`/g, '<code>$1</code>')
  html = html.replace(/^### (.+)$/gm, '<h3>$1</h3>')
  html = html.replace(/^## (.+)$/gm, '<h2>$1</h2>')
  html = html.replace(/^# (.+)$/gm, '<h1>$1</h1>')
  html = html.replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
  html = html.replace(/^- (.+)$/gm, '<li>$1</li>')
  html = html.replace(/\n\n/g, '</p><p>')
  html = html.replace(/\n/g, '<br/>')
  return '<p>' + html + '</p>'
})

async function loadDemand() {
  const id = Number(route.params.id)
  if (!id) {
    message.error('需求ID无效')
    router.push('/demands')
    return
  }
  loading.value = true
  try {
    const res = await getDemandDetail(id)
    if (res.code === 200 && res.data) {
      demand.value = res.data
    } else {
      message.error('需求不存在')
      router.push('/demands')
    }
  } catch {
    message.error('加载需求详情失败')
  } finally {
    loading.value = false
  }
}

async function handleTakeOrder() {
  if (!demand.value) return
  dialog.warning({
    title: '确认接单',
    content: '确定要接这个需求吗？接单后需要与发布者协商完成。',
    positiveText: '确定接单',
    negativeText: '取消',
    onPositiveClick: async () => {
      takingOrder.value = true
      try {
        await takeOrder(demand.value!.id)
        message.success('接单成功')
        loadDemand()
      } catch (e: any) {
        message.error(e?.message || '接单失败')
      } finally {
        takingOrder.value = false
      }
    },
  })
}

async function handleComplete() {
  if (!demand.value) return
  dialog.warning({
    title: '确认完成',
    content: '确定要标记该需求为已完成吗？',
    positiveText: '确认完成',
    negativeText: '取消',
    onPositiveClick: async () => {
      completing.value = true
      try {
        await completeDemand(demand.value!.id)
        message.success('已标记为完成')
        loadDemand()
      } catch (e: any) {
        message.error(e?.message || '操作失败')
      } finally {
        completing.value = false
      }
    },
  })
}

onMounted(() => {
  currentUserId.value = parseCurrentUserId()
  loadDemand()
})
</script>

<style scoped>
.detail-page {
  max-width: 1200px;
  margin: 0 auto;
}
.loading-center {
  display: flex;
  justify-content: center;
  padding: 80px 0;
}
.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
}
.detail-header h1 {
  font-size: 24px;
  margin: 0 0 8px 0;
}
.header-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}
.demand-content {
  line-height: 1.8;
  font-size: 15px;
}
.demand-content :deep(h1),
.demand-content :deep(h2),
.demand-content :deep(h3) {
  margin: 24px 0 12px;
}
.demand-content :deep(p) {
  margin: 8px 0;
}
.demand-content :deep(pre) {
  background: #f5f7fa;
  padding: 16px;
  border-radius: 8px;
  overflow-x: auto;
  margin: 12px 0;
}
.demand-content :deep(code) {
  background: #f0f0f0;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 13px;
}
.demand-content :deep(pre code) {
  background: none;
  padding: 0;
}
.sidebar-card {
  margin-bottom: 16px;
}
.user-info-link {
  text-decoration: none;
  color: inherit;
  display: block;
}
.user-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 8px 0;
}
.user-name {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}
.stat-item {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
  font-size: 14px;
}
.stat-item:last-child {
  border-bottom: none;
}
</style>
