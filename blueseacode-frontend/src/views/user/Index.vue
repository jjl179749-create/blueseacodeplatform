<template>
  <div class="user-page">
    <n-card :bordered="true" class="user-card" style="max-width: 600px; margin: 0 auto">
      <!-- 加载中 -->
      <n-spin v-if="loading" />

      <!-- 用户信息 -->
      <template v-else-if="user.id > 0">
        <div class="user-header">
          <n-avatar :size="72" round :src="user.avatar || undefined">
            {{ (user.nickname || user.username)?.charAt(0)?.toUpperCase() }}
          </n-avatar>
          <div class="user-info">
            <h2>{{ user.nickname || user.username }}</h2>
            <div class="user-meta">@{{ user.username }}</div>
            <div class="user-bio" v-if="user.bio">{{ user.bio }}</div>
            <!-- 操作按钮: 非自己主页才显示 -->
            <div class="user-actions" v-if="!isSelf">
              <n-button
                :type="isFollowingUser ? 'default' : 'primary'"
                size="small"
                :loading="followLoading"
                @click="toggleFollow"
              >
                {{ isFollowingUser ? '已关注' : '关注' }}
              </n-button>
              <n-button size="small" secondary @click="goToChat">
                发私信
              </n-button>
            </div>
          </div>
        </div>

        <n-divider />

        <div class="user-stats">
          <div class="stat-item">
            <div class="stat-value">{{ user.score ?? 0 }}</div>
            <div class="stat-label">积分</div>
          </div>
          <div class="stat-item" style="cursor: pointer" @click="toggleUserResources">
            <div class="stat-value">{{ user.resourceCount }}</div>
            <div class="stat-label">资源</div>
          </div>
          <div class="stat-item">
            <div class="stat-value">{{ user.articleCount }}</div>
            <div class="stat-label">文章</div>
          </div>
          <div class="stat-item">
            <div class="stat-value">{{ user.demandCount }}</div>
            <div class="stat-label">需求</div>
          </div>
          <div class="stat-item" @click="goToFollows">
            <div class="stat-value">{{ followCount.followeeCount }}</div>
            <div class="stat-label">关注</div>
          </div>
          <div class="stat-item" @click="goToFollowers">
            <div class="stat-value">{{ followCount.followerCount }}</div>
            <div class="stat-label">粉丝</div>
          </div>
        </div>

        <n-divider />

        <div class="user-detail">
          <div class="detail-item">
            <span class="label">注册时间</span>
            <span>{{ formatDate(user.createTime) }}</span>
          </div>
          <div class="detail-item" v-if="user.github">
            <span class="label">GitHub</span>
            <a :href="user.github" target="_blank">{{ user.github }}</a>
          </div>
          <div class="detail-item" v-if="user.website">
            <span class="label">个人网站</span>
            <a :href="user.website" target="_blank">{{ user.website }}</a>
          </div>
        </div>
      </template>

      <n-empty v-else description="用户不存在" />
    </n-card>

    <!-- 用户资源列表 -->
    <n-card v-if="showResources" :bordered="true" style="max-width: 600px; margin: 16px auto 0">
      <template #header>
        <div class="user-resources-header">
          <span>{{ user.nickname || user.username }} 发布的资源</span>
          <n-button size="tiny" text @click="showResources = false">收起</n-button>
        </div>
      </template>
      <div v-if="loadingResources" style="text-align:center;padding:40px">
        <n-spin size="medium" />
      </div>
      <template v-else-if="userResources.length > 0">
        <n-list>
          <n-list-item v-for="res in userResources" :key="res.id">
            <n-thing>
              <template #header>
                <router-link :to="'/resources/' + res.id" class="user-res-link">
                  {{ res.title }}
                </router-link>
              </template>
              <template #description>
                <div class="user-res-meta">
                  <span>{{ res.categoryName || '未分类' }}</span>
                  <span>📥 {{ res.downloadCount }}</span>
                  <span>👁 {{ res.viewCount }}</span>
                  <span>📅 {{ formatDate(res.createTime) }}</span>
                </div>
              </template>
            </n-thing>
          </n-list-item>
        </n-list>
        <div v-if="userResTotal > userResources.length" style="text-align:center;margin-top:12px">
          <n-button quaternary size="small" @click="loadMoreUserResources">加载更多</n-button>
        </div>
      </template>
      <n-empty v-else description="暂无公开资源" />
    </n-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import { useAuthStore } from '@/stores/auth'
import { getUserProfile, type UserProfileVO } from '@/api/user'
import {
  getResources,
  type ResResource,
} from '@/api/resource'
import {
  followUser,
  unfollowUser,
  isFollowing,
  getFollowCount,
} from '@/api/message'

const route = useRoute()
const message = useMessage()
const router = useRouter()
const authStore = useAuthStore()

const userId = Number(route.params.id)
const user = ref<UserProfileVO>({} as UserProfileVO)
const loading = ref(false)

// 是否自己主页
const isSelf = computed(() => authStore.userInfo?.userId === userId)

// 关注状态
const isFollowingUser = ref(false)
const followLoading = ref(false)
// 关注/粉丝数量
const followCount = ref({ followeeCount: 0, followerCount: 0 })

function formatDate(dateStr: string | null) {
  if (!dateStr) return '-'
  return dateStr.replace('T', ' ').substring(0, 19)
}

async function loadUser() {
  if (!userId) {
    message.error('用户ID无效')
    return
  }
  loading.value = true
  try {
    const [profileRes, countRes] = await Promise.all([
      getUserProfile(userId),
      getFollowCount(userId),
    ])
    if (profileRes.code === 200 && profileRes.data) {
      user.value = profileRes.data
      // 非自己主页时检查关注状态
      if (!isSelf.value) {
        const followRes = await isFollowing(userId)
        if (followRes.code === 200) {
          isFollowingUser.value = followRes.data
        }
      }
    } else {
      message.error(profileRes.message || '加载用户信息失败')
    }
    if (countRes.code === 200) {
      followCount.value = countRes.data
    }
  } catch {
    message.error('加载用户信息失败')
  } finally {
    loading.value = false
  }
}

async function toggleFollow() {
  followLoading.value = true
  try {
    if (isFollowingUser.value) {
      const res = await unfollowUser(userId)
      if (res.code === 200) {
        isFollowingUser.value = false
        message.success('已取消关注')
      }
    } else {
      const res = await followUser(userId)
      if (res.code === 200) {
        isFollowingUser.value = true
        message.success('关注成功')
      }
    }
  } catch {
    message.error('操作失败')
  } finally {
    followLoading.value = false
  }
}

function goToChat() {
  router.push({ name: 'Inbox', query: { userId: userId.toString() } })
}

function goToFollows() {
  router.push({ name: 'Inbox', query: { tab: 'follows', userId: userId.toString() } })
}

function goToFollowers() {
  router.push({ name: 'Inbox', query: { tab: 'followers', userId: userId.toString() } })
}

onMounted(loadUser)

// ===== 用户资源列表 =====
const showResources = ref(false)
const userResources = ref<ResResource[]>([])
const userResPage = ref(1)
const userResTotal = ref(0)
const loadingResources = ref(false)
const userResPageSize = 10

function toggleUserResources() {
  showResources.value = !showResources.value
  if (showResources.value && userResources.value.length === 0) {
    loadUserResources()
  }
}

async function loadUserResources() {
  loadingResources.value = true
  try {
    const res = await getResources({ page: userResPage.value, size: userResPageSize, userId, status: 1 })
    if (res.code === 200 && res.data) {
      if (userResPage.value === 1) {
        userResources.value = res.data.records || []
      } else {
        userResources.value = [...userResources.value, ...(res.data.records || [])]
      }
      userResTotal.value = res.data.total || 0
    }
  } catch {
    message.error('加载资源列表失败')
  } finally {
    loadingResources.value = false
  }
}

function loadMoreUserResources() {
  userResPage.value++
  loadUserResources()
}
</script>

<style scoped>
.user-page {
  max-width: 600px;
  margin: 0 auto;
  padding: 24px 0;
}
.user-header {
  display: flex;
  align-items: flex-start;
  gap: 20px;
}
.user-info {
  flex: 1;
}
.user-info h2 {
  margin: 0 0 4px;
  font-size: 22px;
}
.user-meta {
  color: #999;
  font-size: 14px;
  margin-bottom: 8px;
}
.user-bio {
  color: #666;
  font-size: 14px;
  line-height: 1.6;
}
.user-stats {
  display: flex;
  justify-content: space-around;
  text-align: center;
}
.stat-item {
  padding: 8px 16px;
}
.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #18a058;
}
.stat-item:hover .stat-value {
  color: #0f7a3f;
}
.stat-label {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}
.user-actions {
  display: flex;
  gap: 8px;
  margin-top: 10px;
}
.user-detail {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.detail-item {
  display: flex;
  font-size: 14px;
}
.detail-item .label {
  color: #999;
  width: 80px;
  flex-shrink: 0;
}
.detail-item a {
  color: #18a058;
  text-decoration: none;
  word-break: break-all;
}
.user-resources-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.user-res-link {
  color: #333;
  text-decoration: none;
  font-weight: 500;
}
.user-res-link:hover {
  color: #18a058;
}
.user-res-meta {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}
</style>
