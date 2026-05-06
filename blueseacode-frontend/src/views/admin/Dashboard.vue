<template>
  <div class="dashboard">
    <h2 style="margin-bottom: 20px">仪表盘</h2>

    <!-- 统计卡片 -->
    <n-grid :cols="4" :x-gap="16" :y-gap="16">
      <n-gi>
        <n-card title="总用户数" hoverable>
          <div class="stat-value">{{ stats.totalUsers }}</div>
          <div class="stat-desc">今日新增 {{ stats.todayNewUsers }}</div>
        </n-card>
      </n-gi>
      <n-gi>
        <n-card title="资源总数" hoverable>
          <div class="stat-value">{{ stats.totalResources }}</div>
        </n-card>
      </n-gi>
      <n-gi>
        <n-card title="文章总数" hoverable>
          <div class="stat-value">{{ stats.totalArticles }}</div>
        </n-card>
      </n-gi>
      <n-gi>
        <n-card title="需求总数" hoverable>
          <div class="stat-value">{{ stats.totalDemands }}</div>
        </n-card>
      </n-gi>
      <n-gi>
        <n-card title="待审核" hoverable>
          <div class="stat-value warn">{{ stats.pendingAudit }}</div>
          <div class="stat-desc">资源+文章+需求</div>
        </n-card>
      </n-gi>
    </n-grid>

    <!-- 趋势图 -->
    <n-card title="用户注册趋势（近7天）" style="margin-top: 20px">
      <div v-if="trendData.length > 0" class="trend-chart">
        <div v-for="item in trendData" :key="item.date" class="trend-bar-wrapper">
          <div class="trend-bar" :style="{ height: getBarHeight(item.count) + 'px' }">
            <span class="trend-bar-value">{{ item.count }}</span>
          </div>
          <div class="trend-label">{{ item.date.slice(5) }}</div>
        </div>
      </div>
      <n-empty v-else description="暂无趋势数据" />
    </n-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import {
  getDashboardStatistics,
  getDashboardTrend,
  type DashboardVO,
  type TrendVO,
} from '@/api/admin'
import { useMessage } from 'naive-ui'

const message = useMessage()
const stats = ref<DashboardVO>({
  totalUsers: 0,
  todayNewUsers: 0,
  totalResources: 0,
  totalArticles: 0,
  totalDemands: 0,
  pendingAudit: 0,
  todayResourceDownloads: 0,
  totalViews: 0,
})
const trendData = ref<TrendVO[]>([])
const maxCount = ref(1)

function getBarHeight(count: number) {
  return maxCount.value > 0 ? (count / maxCount.value) * 200 : 0
}

async function loadData() {
  try {
    const [statsRes, trendRes] = await Promise.all([
      getDashboardStatistics(),
      getDashboardTrend(7),
    ])
    if (statsRes.code === 200) {
      stats.value = statsRes.data
    }
    if (trendRes.code === 200) {
      trendData.value = trendRes.data
      maxCount.value = Math.max(...trendRes.data.map((d) => d.count), 1)
    }
  } catch (e) {
    message.error('加载仪表盘数据失败')
  }
}

onMounted(loadData)
</script>

<style scoped>
.stat-value {
  font-size: 36px;
  font-weight: bold;
  color: #1890ff;
}
.stat-value.warn {
  color: #faad14;
}
.stat-desc {
  font-size: 13px;
  color: #999;
  margin-top: 4px;
}
.trend-chart {
  display: flex;
  align-items: flex-end;
  gap: 16px;
  height: 240px;
  padding: 20px 0;
}
.trend-bar-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  height: 100%;
  justify-content: flex-end;
}
.trend-bar {
  width: 40px;
  background: linear-gradient(to top, #1890ff, #69c0ff);
  border-radius: 4px 4px 0 0;
  position: relative;
  transition: height 0.3s;
  min-height: 4px;
}
.trend-bar-value {
  position: absolute;
  top: -20px;
  left: 50%;
  transform: translateX(-50%);
  font-size: 12px;
  color: #666;
}
.trend-label {
  margin-top: 8px;
  font-size: 12px;
  color: #999;
}
</style>
