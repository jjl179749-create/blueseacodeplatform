<template>
  <div class="profile-page">
    <n-grid :cols="24" :x-gap="24">
      <!-- 左侧个人信息卡片 -->
      <n-grid-item :span="7">
        <n-card title="个人信息" :bordered="true" class="profile-card">
          <div class="avatar-section">
            <div class="avatar-wrapper" :class="{ uploading: uploadingAvatar }" @click="triggerAvatarUpload">
              <n-avatar
                v-if="profile.avatar"
                :src="profile.avatar"
                :size="80"
                round
                color="#18a058"
                class="avatar-clickable"
              />
              <n-avatar
                v-else
                :size="80"
                round
                color="#18a058"
                class="avatar-clickable"
              >
                {{ profile.nickname || profile.username?.charAt(0)?.toUpperCase() }}
              </n-avatar>
              <div class="avatar-overlay">
                <span class="overlay-icon">📷</span>
                <span>更换头像</span>
              </div>
            </div>
            <input
              ref="avatarInputRef"
              type="file"
              accept="image/jpeg,image/png,image/gif,image/webp"
              style="display: none"
              @change="handleAvatarChange"
            />
            <div class="user-name">{{ profile.nickname || profile.username }}</div>
            <div class="user-role">
              <n-tag v-for="role in profile.roles" :key="role" size="small" type="info" round>
                {{ role.replace('ROLE_', '') }}
              </n-tag>
            </div>
          </div>
          <n-divider />
          <div class="info-stats">
            <div class="stat-item">
              <div class="stat-value">{{ profile.score ?? 0 }}</div>
              <div class="stat-label">积分</div>
            </div>
            <div class="stat-item" style="cursor: pointer" @click="goToMyPosts('resource')">
              <div class="stat-value">{{ profile.resourceCount }}</div>
              <div class="stat-label">资源</div>
            </div>
            <div class="stat-item" style="cursor: pointer" @click="goToMyPosts('article')">
              <div class="stat-value">{{ profile.articleCount }}</div>
              <div class="stat-label">文章</div>
            </div>
            <div class="stat-item" style="cursor: pointer" @click="goToMyPosts('demand')">
              <div class="stat-value">{{ profile.demandCount }}</div>
              <div class="stat-label">需求</div>
            </div>
          </div>
          <n-description label-placement="left" :column="1">
            <n-description-item label="用户名">{{ profile.username }}</n-description-item>
            <n-description-item label="邮箱">{{ profile.email || '未设置' }}</n-description-item>
            <n-description-item label="手机号">{{ profile.phone || '未设置' }}</n-description-item>
            <n-description-item label="注册时间">{{ formatDate(profile.createTime) }}</n-description-item>
          </n-description>
        </n-card>
      </n-grid-item>

      <!-- 右侧内容区域 -->
      <n-grid-item :span="17">
        <n-card :bordered="true">
          <n-tabs type="line" v-model:value="activeTab" animated>
            <!-- Tab 1: 个人信息编辑 -->
            <n-tab-pane name="profile" tab="编辑资料">
              <n-form
                ref="profileFormRef"
                :model="profileForm"
                :rules="profileRules"
                label-placement="left"
                label-width="100"
                style="max-width: 600px"
              >
                <n-form-item label="昵称" path="nickname">
                  <n-input v-model:value="profileForm.nickname" placeholder="输入昵称" />
                </n-form-item>
                <n-form-item label="个人简介" path="bio">
                  <n-input
                    v-model:value="profileForm.bio"
                    type="textarea"
                    :rows="3"
                    placeholder="介绍一下自己..."
                  />
                </n-form-item>
                <n-form-item label="邮箱" path="email">
                  <n-input v-model:value="profileForm.email" placeholder="输入邮箱" />
                </n-form-item>
                <n-form-item label="手机号" path="phone">
                  <n-input v-model:value="profileForm.phone" placeholder="输入手机号" />
                </n-form-item>
                <n-form-item label="GitHub" path="github">
                  <n-input v-model:value="profileForm.github" placeholder="GitHub 地址" />
                </n-form-item>
                <n-form-item label="个人网站" path="website">
                  <n-input v-model:value="profileForm.website" placeholder="个人网站" />
                </n-form-item>
                <n-form-item>
                  <n-button type="primary" :loading="savingProfile" @click="handleSaveProfile">
                    保存修改
                  </n-button>
                </n-form-item>
              </n-form>
            </n-tab-pane>

            <!-- Tab 2: 修改密码 -->
            <n-tab-pane name="password" tab="修改密码">
              <n-form
                ref="passwordFormRef"
                :model="passwordForm"
                :rules="passwordRules"
                label-placement="left"
                label-width="100"
                style="max-width: 500px"
              >
                <n-form-item label="原密码" path="oldPassword">
                  <n-input
                    v-model:value="passwordForm.oldPassword"
                    type="password"
                    show-password-on="click"
                    placeholder="输入原密码"
                  />
                </n-form-item>
                <n-form-item label="新密码" path="newPassword">
                  <n-input
                    v-model:value="passwordForm.newPassword"
                    type="password"
                    show-password-on="click"
                    placeholder="输入新密码"
                  />
                </n-form-item>
                <n-form-item label="确认密码" path="confirmPassword">
                  <n-input
                    v-model:value="passwordForm.confirmPassword"
                    type="password"
                    show-password-on="click"
                    placeholder="再次输入新密码"
                  />
                </n-form-item>
                <n-form-item>
                  <n-button type="primary" :loading="savingPassword" @click="handleChangePassword">
                    修改密码
                  </n-button>
                </n-form-item>
              </n-form>
            </n-tab-pane>

            <!-- Tab 3: 我的收藏 -->
            <n-tab-pane name="my-collects" tab="我的收藏">
              <n-tabs type="segment" v-model:value="collectType" size="small" style="margin-bottom: 16px">
                <n-tab-pane name="resource" tab="资源收藏">
                  <div v-if="loadingCollectedResources" style="text-align:center;padding:40px">
                    <n-spin size="medium" />
                  </div>
                  <template v-else-if="collectedResources.length > 0">
                    <n-list>
                      <n-list-item v-for="res in collectedResources" :key="res.id" class="my-resource-item">
                        <n-thing>
                          <template #header>
                            <div class="my-resource-header">
                              <router-link :to="'/resources/' + res.id" class="res-title-link">
                                {{ res.title }}
                              </router-link>
                              <n-tag size="tiny" :bordered="false">{{ res.categoryName }}</n-tag>
                            </div>
                          </template>
                          <template #description>
                            <div class="my-resource-meta">
                              <span>👤 {{ res.nickname || '匿名' }}</span>
                              <span>👁 {{ res.viewCount }}</span>
                              <span>📥 {{ res.downloadCount }}</span>
                              <span>📅 {{ formatDate(res.createTime) }}</span>
                            </div>
                          </template>
                        </n-thing>
                      </n-list-item>
                    </n-list>
                    <div v-if="collectedResourceTotal > collectedResources.length" style="text-align:center;margin-top:12px">
                      <n-button quaternary size="small" @click="loadMoreCollectedResources">加载更多</n-button>
                    </div>
                  </template>
                  <n-empty v-else description="暂无收藏资源" />
                </n-tab-pane>
                <n-tab-pane name="article" tab="文章收藏">
                  <div v-if="loadingCollectedArticles" style="text-align:center;padding:40px">
                    <n-spin size="medium" />
                  </div>
                  <template v-else-if="collectedArticles.length > 0">
                    <n-list>
                      <n-list-item v-for="art in collectedArticles" :key="art.id" class="my-resource-item">
                        <n-thing>
                          <template #header>
                            <div class="my-resource-header">
                              <router-link :to="'/articles/' + art.id" class="res-title-link">
                                {{ art.title }}
                              </router-link>
                              <n-tag size="tiny" :bordered="false">{{ art.categoryName || '未分类' }}</n-tag>
                            </div>
                          </template>
                          <template #description>
                            <div class="my-resource-meta">
                              <span>👤 {{ art.nickname || '匿名' }}</span>
                              <span>👁 {{ art.viewCount }}</span>
                              <span>👍 {{ art.likeCount }}</span>
                              <span>📅 {{ formatDate(art.createTime) }}</span>
                            </div>
                          </template>
                        </n-thing>
                      </n-list-item>
                    </n-list>
                    <div v-if="collectedArticleTotal > collectedArticles.length" style="text-align:center;margin-top:12px">
                      <n-button quaternary size="small" @click="loadMoreCollectedArticles">加载更多</n-button>
                    </div>
                  </template>
                  <n-empty v-else description="暂无收藏文章" />
                </n-tab-pane>
              </n-tabs>
            </n-tab-pane>

            <!-- Tab 4: 我的消息 -->
            <n-tab-pane name="notifications" tab="我的消息">
              <div class="notification-header">
                <n-badge :value="unreadCount" :max="99">
                  <span style="font-size: 14px">未读消息</span>
                </n-badge>
                <n-button
                  v-if="unreadCount > 0"
                  size="small"
                  text
                  type="primary"
                  @click="handleMarkAllRead"
                >
                  全部已读
                </n-button>
              </div>
              <n-list v-if="notifications.length > 0">
                <n-list-item
                  v-for="item in notifications"
                  :key="item.id"
                  :class="{ 'notification-unread': item.isRead === 0 }"
                >
                  <template #prefix>
                    <n-badge :dot="item.isRead === 0" color="#18a058">
                      <n-tag size="small" :type="notificationTypeMap[item.type] || 'default'">
                        {{ item.type }}
                      </n-tag>
                    </n-badge>
                  </template>
                  <n-thing :title="item.title" :description="formatDate(item.createTime)">
                    {{ item.content }}
                  </n-thing>
                  <template #suffix>
                    <n-button
                      v-if="item.isRead === 0"
                      size="tiny"
                      text
                      type="primary"
                      @click="handleMarkRead(item.id)"
                    >
                      标为已读
                    </n-button>
                  </template>
                </n-list-item>
              </n-list>
              <n-empty v-else description="暂无消息" style="margin-top: 40px" />
              <div v-if="total > notifications.length" class="load-more">
                <n-button quaternary @click="loadMore">加载更多</n-button>
              </div>
            </n-tab-pane>
            <!-- Tab 4: 我的发布 -->
            <n-tab-pane name="my-posts" tab="我的发布">
              <n-tabs type="segment" v-model:value="myPostsType" size="small" style="margin-bottom: 16px">
                <n-tab-pane name="resource" tab="资源">
                  <div v-if="loadingResources" style="text-align:center;padding:40px">
                    <n-spin size="medium" />
                  </div>
                  <template v-else-if="resources.length > 0">
                    <n-list>
                      <n-list-item v-for="res in resources" :key="res.id" class="my-resource-item">
                        <n-thing>
                          <template #header>
                            <div class="my-resource-header">
                              <router-link :to="'/resources/' + res.id" class="res-title-link">
                                {{ res.title }}
                              </router-link>
                              <n-tag v-if="res.status === 0" size="tiny" type="warning">待审核</n-tag>
                              <n-tag v-else-if="res.status === 1" size="tiny" type="success">已通过</n-tag>
                              <n-tag v-else-if="res.status === 2" size="tiny" type="error">已驳回</n-tag>
                              <n-tag v-else-if="res.status === 3" size="tiny" type="default">已下架</n-tag>
                            </div>
                          </template>
                          <template #description>
                            <div class="my-resource-meta">
                              <span>{{ res.categoryName || '未分类' }}</span>
                              <span>📥 {{ res.downloadCount }}</span>
                              <span>👁 {{ res.viewCount }}</span>
                              <span>📅 {{ formatDate(res.createTime) }}</span>
                            </div>
                          </template>
                          <template v-if="res.status === 2 && res.rejectReason" #footer>
                            <div class="reject-reason">驳回原因：{{ res.rejectReason }}</div>
                          </template>
                        </n-thing>
                      </n-list-item>
                    </n-list>
                    <div v-if="resourceTotal > resources.length" style="text-align:center;margin-top:12px">
                      <n-button quaternary size="small" @click="loadMoreResources">加载更多</n-button>
                    </div>
                  </template>
                  <n-empty v-else description="暂无发布资源" />
                </n-tab-pane>
                <n-tab-pane name="article" tab="文章">
                  <div v-if="loadingArticles" style="text-align:center;padding:40px">
                    <n-spin size="medium" />
                  </div>
                  <template v-else-if="articles.length > 0">
                    <n-list>
                      <n-list-item v-for="art in articles" :key="art.id" class="my-resource-item">
                        <n-thing>
                          <template #header>
                            <div class="my-resource-header">
                              <router-link :to="'/articles/' + art.id" class="res-title-link">
                                {{ art.title }}
                              </router-link>
                              <n-tag v-if="art.status === 0" size="tiny" type="warning">草稿</n-tag>
                              <n-tag v-else-if="art.status === 1" size="tiny" type="warning">待审核</n-tag>
                              <n-tag v-else-if="art.status === 2" size="tiny" type="success">已通过</n-tag>
                              <n-tag v-else-if="art.status === 3" size="tiny" type="error">已驳回</n-tag>
                            </div>
                          </template>
                          <template #description>
                            <div class="my-resource-meta">
                              <span>{{ art.categoryName || '未分类' }}</span>
                              <span>👁 {{ art.viewCount }}</span>
                              <span>👍 {{ art.likeCount }}</span>
                              <span>💬 {{ art.commentCount }}</span>
                              <span>📅 {{ formatDate(art.createTime) }}</span>
                            </div>
                          </template>
                          <template v-if="art.status === 3 && art.rejectReason" #footer>
                            <div class="reject-reason">驳回原因：{{ art.rejectReason }}</div>
                          </template>
                        </n-thing>
                      </n-list-item>
                    </n-list>
                    <div v-if="articleTotal > articles.length" style="text-align:center;margin-top:12px">
                      <n-button quaternary size="small" @click="loadMoreArticles">加载更多</n-button>
                    </div>
                  </template>
                  <n-empty v-else description="暂无发布文章" />
                </n-tab-pane>
                <n-tab-pane name="demand" tab="需求">
                  <div v-if="loadingDemands" style="text-align:center;padding:40px">
                    <n-spin size="medium" />
                  </div>
                  <template v-else-if="demands.length > 0">
                    <n-list>
                      <n-list-item v-for="dem in demands" :key="dem.id" class="my-resource-item">
                        <n-thing>
                          <template #header>
                            <div class="my-resource-header">
                              <router-link :to="'/demands/' + dem.id" class="res-title-link">
                                {{ dem.title }}
                              </router-link>
                              <n-tag :type="DEMAND_STATUS_TAG[dem.status] || 'default'" size="tiny">
                                {{ DEMAND_STATUS_MAP[dem.status] || '未知' }}
                              </n-tag>
                            </div>
                          </template>
                          <template #description>
                            <div class="my-resource-meta">
                              <span>{{ dem.category }}</span>
                              <span>📥 {{ dem.orderCount }} 接单</span>
                              <span>👁 {{ dem.viewCount }}</span>
                              <span>📅 {{ formatDate(dem.createTime) }}</span>
                            </div>
                          </template>
                          <template v-if="dem.status === 5 && dem.rejectReason" #footer>
                            <div class="reject-reason">驳回原因：{{ dem.rejectReason }}</div>
                          </template>
                        </n-thing>
                      </n-list-item>
                    </n-list>
                    <div v-if="demandTotal > demands.length" style="text-align:center;margin-top:12px">
                      <n-button quaternary size="small" @click="loadMoreDemands">加载更多</n-button>
                    </div>
                  </template>
                  <n-empty v-else description="暂无发布需求" />
                </n-tab-pane>
              </n-tabs>
            </n-tab-pane>
          </n-tabs>
        </n-card>
      </n-grid-item>
    </n-grid>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import { useAuthStore } from '@/stores/auth'
import {
  getProfile,
  updateProfile as updateProfileApi,
  updatePassword as updatePasswordApi,
  getNotifications,
  markNotificationRead,
  markAllNotificationsRead,
  getUnreadCount,
  uploadAvatar as uploadAvatarApi,
  updateAvatar as updateAvatarApi,
  type UserProfileVO,
  type UserProfileUpdateRequest,
  type MsgNotification,
} from '@/api/user'
import {
  getMyResources,
  type ResResource,
  getMyCollectedResources,
} from '@/api/resource'
import {
  getMyArticles,
  type ArtArticle,
  getMyCollectedArticles,
} from '@/api/article'
import {
  getMyDemands,
  DEMAND_STATUS_MAP,
  DEMAND_STATUS_TAG,
  type DemDemand,
} from '@/api/demand'

const message = useMessage()
const authStore = useAuthStore()
const route = useRoute()
const router = useRouter()

// ===== 头像上传 =====
const avatarInputRef = ref<HTMLInputElement | null>(null)
const uploadingAvatar = ref(false)

function triggerAvatarUpload() {
  avatarInputRef.value?.click()
}

async function handleAvatarChange(event: Event) {
  const input = event.target as HTMLInputElement
  if (!input.files || input.files.length === 0) return

  const file = input.files[0]

  // 校验文件大小（最大5MB）
  if (file.size > 5 * 1024 * 1024) {
    message.error('头像文件大小不能超过5MB')
    input.value = ''
    return
  }

  // 校验文件类型
  const allowedTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp']
  if (!allowedTypes.includes(file.type)) {
    message.error('仅支持 JPG/PNG/GIF/WebP 格式的头像')
    input.value = ''
    return
  }

  uploadingAvatar.value = true
  try {
    // 1. 上传文件到OSS
    const uploadRes = await uploadAvatarApi(file)
    if (uploadRes.code !== 200) {
      message.error(uploadRes.message || '头像上传失败')
      return
    }

    const avatarUrl = uploadRes.data

    // 2. 更新用户头像
    const updateRes = await updateAvatarApi(avatarUrl)
    if (updateRes.code === 200) {
      profile.avatar = avatarUrl
      message.success('头像更新成功')
    } else {
      message.error(updateRes.message || '头像更新失败')
    }
  } catch {
    message.error('头像上传失败，请稍后重试')
  } finally {
    uploadingAvatar.value = false
    input.value = ''
  }
}

const activeTab = ref((route.query.tab as string) || 'profile')
const myPostsType = ref('resource')
const collectType = ref('resource')

// ===== 个人信息 =====
const profile = reactive<UserProfileVO>({
  id: 0,
  username: '',
  nickname: null,
  avatar: null,
  email: null,
  phone: null,
  bio: null,
  github: null,
  website: null,
  score: 0,
  status: 0,
  roles: [],
  createTime: '',
  lastLoginTime: null,
  resourceCount: 0,
  articleCount: 0,
  demandCount: 0,
})

const profileForm = reactive<UserProfileUpdateRequest>({
  nickname: null,
  bio: null,
  github: null,
  website: null,
  email: null,
  phone: null,
})

const profileRules = {
  email: [
    { type: 'email' as const, message: '请输入正确的邮箱格式', trigger: 'blur' },
  ],
}

const savingProfile = ref(false)
const profileFormRef = ref(null)

async function loadProfile() {
  try {
    const res = await getProfile()
    if (res.code === 200 && res.data) {
      Object.assign(profile, res.data)
      Object.assign(profileForm, {
        nickname: res.data.nickname,
        bio: res.data.bio,
        github: res.data.github,
        website: res.data.website,
        email: res.data.email,
        phone: res.data.phone,
      })
    }
  } catch {
    message.error('加载个人信息失败')
  }
}

async function handleSaveProfile() {
  savingProfile.value = true
  try {
    const res = await updateProfileApi(profileForm)
    if (res.code === 200) {
      message.success('保存成功')
      await loadProfile()
    } else {
      message.error(res.message || '保存失败')
    }
  } catch {
    message.error('保存失败')
  } finally {
    savingProfile.value = false
  }
}

// ===== 修改密码 =====
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})

const passwordRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (_rule: unknown, value: string) => {
        return value === passwordForm.newPassword
          ? Promise.resolve()
          : Promise.reject(new Error('两次输入的密码不一致'))
      },
      trigger: 'blur',
    },
  ],
}

const savingPassword = ref(false)
const passwordFormRef = ref(null)

async function handleChangePassword() {
  savingPassword.value = true
  try {
    const res = await updatePasswordApi({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword,
    })
    if (res.code === 200) {
      message.success('密码修改成功')
      passwordForm.oldPassword = ''
      passwordForm.newPassword = ''
      passwordForm.confirmPassword = ''
    } else {
      message.error(res.message || '修改失败')
    }
  } catch {
    message.error('修改密码失败')
  } finally {
    savingPassword.value = false
  }
}

// ===== 我的收藏 =====
const collectedResources = ref<ResResource[]>([])
const collectedResourcePage = ref(1)
const collectedResourceTotal = ref(0)
const loadingCollectedResources = ref(false)
const collectedResourcePageSize = 10

const collectedArticles = ref<ArtArticle[]>([])
const collectedArticlePage = ref(1)
const collectedArticleTotal = ref(0)
const loadingCollectedArticles = ref(false)
const collectedArticlePageSize = 10

async function loadCollectedResources() {
  loadingCollectedResources.value = true
  try {
    const res = await getMyCollectedResources({ page: collectedResourcePage.value, size: collectedResourcePageSize })
    if (res.code === 200 && res.data) {
      if (collectedResourcePage.value === 1) {
        collectedResources.value = res.data.records || []
      } else {
        collectedResources.value = [...collectedResources.value, ...(res.data.records || [])]
      }
      collectedResourceTotal.value = res.data.total || 0
    }
  } catch {
    message.error('加载收藏资源失败')
  } finally {
    loadingCollectedResources.value = false
  }
}

function loadMoreCollectedResources() {
  collectedResourcePage.value++
  loadCollectedResources()
}

async function loadCollectedArticles() {
  loadingCollectedArticles.value = true
  try {
    const res = await getMyCollectedArticles({ page: collectedArticlePage.value, size: collectedArticlePageSize })
    if (res.code === 200 && res.data) {
      if (collectedArticlePage.value === 1) {
        collectedArticles.value = res.data.records || []
      } else {
        collectedArticles.value = [...collectedArticles.value, ...(res.data.records || [])]
      }
      collectedArticleTotal.value = res.data.total || 0
    }
  } catch {
    message.error('加载收藏文章失败')
  } finally {
    loadingCollectedArticles.value = false
  }
}

function loadMoreCollectedArticles() {
  collectedArticlePage.value++
  loadCollectedArticles()
}

// ===== 消息通知 =====
const notifications = ref<MsgNotification[]>([])
const unreadCount = ref(0)
const currentPage = ref(1)
const pageSize = 10
const total = ref(0)
const loadingNotifications = ref(false)

const notificationTypeMap: Record<string, string> = {
  SYSTEM: 'info',
  LIKE: 'success',
  COMMENT: 'warning',
  AUDIT: 'primary',
  FOLLOW: 'info',
}

async function loadNotifications() {
  loadingNotifications.value = true
  try {
    const res = await getNotifications({ page: currentPage.value, size: pageSize })
    if (res.code === 200 && res.data) {
      if (currentPage.value === 1) {
        notifications.value = res.data.records || []
      } else {
        notifications.value = [...notifications.value, ...(res.data.records || [])]
      }
      total.value = res.data.total || 0
    }
  } catch {
    message.error('加载消息失败')
  } finally {
    loadingNotifications.value = false
  }
}

async function loadUnreadCount() {
  try {
    const res = await getUnreadCount()
    if (res.code === 200) {
      unreadCount.value = res.data ?? 0
    }
  } catch {
    // ignore
  }
}

async function handleMarkRead(id: number) {
  try {
    const res = await markNotificationRead(id)
    if (res.code === 200) {
      const item = notifications.value.find((n) => n.id === id)
      if (item) item.isRead = 1
      unreadCount.value = Math.max(0, unreadCount.value - 1)
    }
  } catch {
    message.error('操作失败')
  }
}

async function handleMarkAllRead() {
  try {
    const res = await markAllNotificationsRead()
    if (res.code === 200) {
      notifications.value.forEach((n) => (n.isRead = 1))
      unreadCount.value = 0
      message.success('全部已读')
    }
  } catch {
    message.error('操作失败')
  }
}

function loadMore() {
  currentPage.value++
  loadNotifications()
}

// ===== 我的发布 =====
const resources = ref<ResResource[]>([])
const resourcePage = ref(1)
const resourceTotal = ref(0)
const loadingResources = ref(false)
const resourcePageSize = 10

const articles = ref<ArtArticle[]>([])
const articlePage = ref(1)
const articleTotal = ref(0)
const loadingArticles = ref(false)
const articlePageSize = 10

const demands = ref<DemDemand[]>([])
const demandPage = ref(1)
const demandTotal = ref(0)
const loadingDemands = ref(false)
const demandPageSize = 10

function goToMyPosts(type: string) {
  activeTab.value = 'my-posts'
  myPostsType.value = type
  if (type === 'resource' && resources.value.length === 0) {
    loadMyResources()
  }
  if (type === 'article' && articles.value.length === 0) {
    loadMyArticles()
  }
  if (type === 'demand' && demands.value.length === 0) {
    loadMyDemands()
  }
}

async function loadMyResources() {
  loadingResources.value = true
  try {
    const res = await getMyResources({ page: resourcePage.value, size: resourcePageSize })
    if (res.code === 200 && res.data) {
      if (resourcePage.value === 1) {
        resources.value = res.data.records || []
      } else {
        resources.value = [...resources.value, ...(res.data.records || [])]
      }
      resourceTotal.value = res.data.total || 0
    }
  } catch {
    message.error('加载资源列表失败')
  } finally {
    loadingResources.value = false
  }
}

function loadMoreResources() {
  resourcePage.value++
  loadMyResources()
}

async function loadMyArticles() {
  loadingArticles.value = true
  try {
    const res = await getMyArticles({ page: articlePage.value, size: articlePageSize })
    if (res.code === 200 && res.data) {
      if (articlePage.value === 1) {
        articles.value = res.data.records || []
      } else {
        articles.value = [...articles.value, ...(res.data.records || [])]
      }
      articleTotal.value = res.data.total || 0
    }
  } catch {
    message.error('加载文章列表失败')
  } finally {
    loadingArticles.value = false
  }
}

function loadMoreArticles() {
  articlePage.value++
  loadMyArticles()
}

async function loadMyDemands() {
  loadingDemands.value = true
  try {
    const res = await getMyDemands({ page: demandPage.value, size: demandPageSize })
    if (res.code === 200 && res.data) {
      if (demandPage.value === 1) {
        demands.value = res.data.records || []
      } else {
        demands.value = [...demands.value, ...(res.data.records || [])]
      }
      demandTotal.value = res.data.total || 0
    }
  } catch {
    message.error('加载需求列表失败')
  } finally {
    loadingDemands.value = false
  }
}

function loadMoreDemands() {
  demandPage.value++
  loadMyDemands()
}

// ===== 工具函数 =====
function formatDate(dateStr: string | null) {
  if (!dateStr) return '-'
  return dateStr.replace('T', ' ').substring(0, 19)
}

// ===== 初始化 =====
watch(activeTab, (newTab) => {
  if (newTab === 'my-collects' && collectedResources.value.length === 0) {
    loadCollectedResources()
  }
  if (newTab === 'my-collects' && collectedArticles.value.length === 0) {
    loadCollectedArticles()
  }
})

onMounted(() => {
  if (!authStore.isLoggedIn) {
    window.location.href = '/login'
    return
  }
  loadProfile()
  loadNotifications()
  loadUnreadCount()
  if (activeTab.value === 'my-collects') {
    loadCollectedResources()
    loadCollectedArticles()
  }
})
</script>

<style scoped>
.profile-page {
  max-width: 1200px;
  margin: 0 auto;
}
.profile-card {
  position: sticky;
  top: 24px;
}
.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 8px 0;
}
.avatar-wrapper {
  position: relative;
  cursor: pointer;
  border-radius: 50%;
  overflow: hidden;
}
.avatar-wrapper:hover .avatar-overlay {
  opacity: 1;
}
.avatar-clickable {
  transition: filter 0.2s;
}
.avatar-wrapper:hover .avatar-clickable {
  filter: brightness(0.7);
}
.avatar-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 12px;
  opacity: 0;
  transition: opacity 0.2s;
  background: rgba(0, 0, 0, 0.4);
  border-radius: 50%;
}
.overlay-icon {
  font-size: 20px;
  line-height: 1;
  margin-bottom: 2px;
}
.avatar-wrapper.uploading {
  cursor: wait;
}
.avatar-wrapper.uploading .avatar-overlay {
  opacity: 1;
  background: rgba(0, 0, 0, 0.5);
}
.user-name {
  margin-top: 12px;
  font-size: 18px;
  font-weight: 600;
}
.user-role {
  margin-top: 8px;
  display: flex;
  gap: 4px;
}
.info-stats {
  display: flex;
  justify-content: space-around;
  text-align: center;
  margin: 8px 0;
}
.stat-value {
  font-size: 20px;
  font-weight: 700;
  color: #18a058;
}
.stat-label {
  font-size: 12px;
  color: #999;
  margin-top: 2px;
}
.notification-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.notification-unread {
  background-color: #f0faf0;
}
.load-more {
  text-align: center;
  margin-top: 16px;
}
.my-resource-item {
  border-bottom: 1px solid #f0f0f0;
}
.my-resource-item:last-child {
  border-bottom: none;
}
.my-resource-header {
  display: flex;
  align-items: center;
  gap: 8px;
}
.res-title-link {
  color: #333;
  text-decoration: none;
  font-weight: 500;
}
.res-title-link:hover {
  color: #18a058;
}
.my-resource-meta {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}
.reject-reason {
  color: #e74c3c;
  font-size: 13px;
  background: #fef0ef;
  padding: 6px 10px;
  border-radius: 4px;
  margin-top: 8px;
}
</style>
