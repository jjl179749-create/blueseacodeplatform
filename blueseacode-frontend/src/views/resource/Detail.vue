<template>
  <div class="detail-page">
    <n-grid :cols="24" :x-gap="24">
      <!-- 主要内容区 -->
      <n-grid-item :span="17">
        <!-- 资源基本信息 -->
        <n-card :bordered="true" class="detail-card">
          <template #header>
            <div class="detail-header">
              <h1>{{ resource.title }}</h1>
              <div class="header-actions">
                <n-button
                  :type="resource.isCollected ? 'warning' : 'default'"
                  size="small"
                  @click="handleToggleCollect"
                  :loading="collecting"
                >
                  {{ resource.isCollected ? '❤️ 已收藏' : '🤍 收藏' }}
                </n-button>
              </div>
            </div>
          </template>

          <div class="detail-meta">
            <n-tag :bordered="false" size="small">{{ resource.categoryName }}</n-tag>
            <n-tag v-for="tag in resource.tags" :key="tag" size="small" type="info" :bordered="false">
              {{ tag }}
            </n-tag>
          </div>

          <div class="detail-stats">
            <span>👤 {{ resource.nickname || '匿名' }}</span>
            <span>📥 {{ resource.downloadCount }} 下载</span>
            <span>👁 {{ resource.viewCount }} 浏览</span>
            <span>💬 {{ resource.commentCount }} 评论</span>
            <span>📅 {{ formatDate(resource.createTime) }}</span>
          </div>

          <n-divider />

          <!-- 文件信息 -->
          <div class="file-info">
            <div class="file-info-item">
              <span class="label">文件名：</span>
              <span>{{ resource.fileName || '未知' }}</span>
            </div>
            <div class="file-info-item">
              <span class="label">文件格式：</span>
              <span>{{ resource.fileFormat || '未知' }}</span>
            </div>
            <div class="file-info-item">
              <span class="label">文件大小：</span>
              <span>{{ formatFileSize(resource.fileSize) }}</span>
            </div>
            <div class="file-info-item" v-if="resource.downloadPoints > 0">
              <span class="label">下载积分：</span>
              <span class="points">{{ resource.downloadPoints }} 积分</span>
            </div>
            <div class="file-info-item" v-else>
              <span class="label">下载积分：</span>
              <span class="free">免费</span>
            </div>
          </div>

          <!-- 操作按钮 -->
          <div class="action-buttons">
            <n-button type="primary" size="large" @click="handleDownload" :loading="downloading">
              📥 下载资源
            </n-button>
            <n-button v-if="isOwner" size="small" style="margin-left: 12px" @click="handleDelete">
              删除
            </n-button>
          </div>
        </n-card>

        <!-- 资源描述 -->
        <n-card title="资源描述" :bordered="true" class="desc-card">
          <div class="description-content" v-if="resource.description">
            {{ resource.description }}
          </div>
          <n-empty v-else description="暂无描述" />
        </n-card>

        <!-- 评论区域 -->
        <n-card title="评论" :bordered="true" class="comment-card">
          <!-- 评论输入 -->
          <div class="comment-input-area">
            <n-input
              v-model:value="commentContent"
              type="textarea"
              :rows="3"
              placeholder="写下你的评论..."
            />
            <n-button
              type="primary"
              size="small"
              style="margin-top: 8px"
              @click="handleSubmitComment"
              :loading="commenting"
            >
              发表评论
            </n-button>
          </div>

          <n-divider />

          <!-- 评论列表 -->
          <div v-if="commentGroups.length > 0">
            <div v-for="group in commentGroups" :key="group.parent.id" class="comment-group">
              <!-- 父评论 -->
              <div class="comment-item">
                <div class="comment-header">
                  <router-link
                    :to="'/user/' + group.parent.userId"
                    class="comment-user-link"
                  >
                    <n-avatar v-if="group.parent.avatar" :src="group.parent.avatar" :size="24" round style="vertical-align:middle;margin-right:4px" />
                    <n-avatar v-else :size="24" round style="vertical-align:middle;margin-right:4px">{{ group.parent.nickname?.charAt(0) || '?' }}</n-avatar>
                    <strong>{{ group.parent.nickname || '匿名用户' }}</strong>
                  </router-link>
                  <span class="comment-time">{{ formatDate(group.parent.createTime) }}</span>
                </div>
                <div class="comment-body">
                  {{ group.parent.content }}
                </div>
                <div class="comment-actions">
                  <n-button
                    v-if="replyingTo !== group.parent.id"
                    size="tiny"
                    text
                    type="primary"
                    @click="startReply(group.parent)"
                  >
                    回复
                  </n-button>
                  <n-button
                    v-else
                    size="tiny"
                    text
                    @click="cancelReply"
                  >
                    取消回复
                  </n-button>
                </div>
                <!-- 回复输入框（仅在父评论下） -->
                <div v-if="replyingTo === group.parent.id" class="reply-input-area">
                  <n-input
                    v-model:value="replyContent"
                    type="textarea"
                    :rows="2"
                    :placeholder="'回复 @' + (group.parent.nickname || '匿名用户')"
                  />
                  <div class="reply-actions">
                    <n-button
                      type="primary"
                      size="tiny"
                      @click="handleSubmitReply(group.parent.id)"
                      :loading="commenting"
                    >
                      回复
                    </n-button>
                  </div>
                </div>
              </div>
              <!-- 回复列表（嵌套在父评论下） -->
              <div v-for="reply in group.replies" :key="reply.id" class="reply-item">
                <div class="reply-content-wrapper">
                  <div class="reply-header">
                    <router-link
                      :to="'/user/' + reply.userId"
                      class="comment-user-link"
                    >
                      <n-avatar v-if="reply.avatar" :src="reply.avatar" :size="24" round style="vertical-align:middle;margin-right:4px" />
                      <n-avatar v-else :size="24" round style="vertical-align:middle;margin-right:4px">{{ reply.nickname?.charAt(0) || '?' }}</n-avatar>
                      <strong>{{ reply.nickname || '匿名用户' }}</strong>
                    </router-link>
                    <span class="reply-to-label">回复 @{{ reply.replyToNickname || '匿名用户' }}</span>
                    <span class="comment-time">{{ formatDate(reply.createTime) }}</span>
                  </div>
                  <div class="reply-body">
                    {{ reply.content }}
                  </div>
                </div>
              </div>
            </div>
            <div v-if="commentTotal > comments.length" class="load-more-comments">
              <n-button quaternary size="small" @click="loadMoreComments">加载更多</n-button>
            </div>
          </div>
          <n-empty v-else description="暂无评论，快来抢沙发吧" />
        </n-card>
      </n-grid-item>

      <!-- 侧边栏 -->
      <n-grid-item :span="7">
        <!-- 封面 -->
        <n-card :bordered="true" class="sidebar-card" v-if="resource.coverImage">
          <img :src="resource.coverImage" class="sidebar-cover" />
        </n-card>

        <!-- 作者信息 -->
        <n-card title="作者" :bordered="true" class="sidebar-card">
          <router-link :to="'/user/' + resource.userId" class="author-info-link">
            <div class="author-info">
              <n-avatar v-if="resource.avatar" :src="resource.avatar" :size="48" round />
              <n-avatar v-else :size="48" round>{{ resource.nickname?.charAt(0) || '?' }}</n-avatar>
              <div class="author-name">{{ resource.nickname || '匿名' }}</div>
            </div>
          </router-link>
        </n-card>

        <!-- 资源统计 -->
        <n-card title="统计" :bordered="true" class="sidebar-card">
          <div class="stat-list">
            <div class="stat-row">
              <span>下载次数</span>
              <span class="stat-value">{{ resource.downloadCount }}</span>
            </div>
            <div class="stat-row">
              <span>浏览次数</span>
              <span class="stat-value">{{ resource.viewCount }}</span>
            </div>
            <div class="stat-row">
              <span>评论数量</span>
              <span class="stat-value">{{ resource.commentCount }}</span>
            </div>
            <div class="stat-row">
              <span>收藏数量</span>
              <span class="stat-value">{{ resource.collectCount }}</span>
            </div>
          </div>
        </n-card>
      </n-grid-item>
    </n-grid>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import { useAuthStore } from '@/stores/auth'
import {
  getResourceDetail,
  downloadResource,
  deleteResource,
  collectResource,
  cancelCollect,
  getComments,
  createComment,
  type ResResource,
  type ResComment,
} from '@/api/resource'

const route = useRoute()
const router = useRouter()
const message = useMessage()
const authStore = useAuthStore()

const resourceId = computed(() => Number(route.params.id))
const resource = ref<ResResource>({} as ResResource)
const loading = ref(false)
const downloading = ref(false)
const collecting = ref(false)
const commenting = ref(false)

const commentContent = ref('')
const comments = ref<ResComment[]>([])
const commentPage = ref(1)
const commentTotal = ref(0)
const commentPageSize = 10
const replyingTo = ref<number | null>(null)
const replyContent = ref('')

interface CommentGroup {
  parent: ResComment
  replies: ResComment[]
}

const isOwner = computed(() => {
  return resource.value.userId === authStore.userInfo?.id
})

// 将评论按父/子关系分组
const commentGroups = computed<CommentGroup[]>(() => {
  const replyMap = new Map<number, ResComment[]>()
  const groups: CommentGroup[] = []

  // 第一遍：分离出所有回复
  for (const c of comments.value) {
    if (c.parentId) {
      const list = replyMap.get(c.parentId) || []
      list.push(c)
      replyMap.set(c.parentId, list)
    }
  }

  // 第二遍：构建分组（仅保留顶级评论）
  for (const c of comments.value) {
    if (!c.parentId) {
      groups.push({
        parent: c,
        replies: replyMap.get(c.id) || [],
      })
    }
  }

  return groups
})

async function loadDetail() {
  loading.value = true
  try {
    const res = await getResourceDetail(resourceId.value)
    if (res.code === 200 && res.data) {
      resource.value = res.data
    } else {
      message.error(res.message || '资源不存在')
      router.push('/resources')
    }
  } catch {
    message.error('加载失败')
  } finally {
    loading.value = false
  }
}

async function loadComments() {
  try {
    const res = await getComments(resourceId.value, commentPage.value, commentPageSize)
    if (res.code === 200 && res.data) {
      if (commentPage.value === 1) {
        comments.value = res.data.records || []
      } else {
        comments.value = [...comments.value, ...(res.data.records || [])]
      }
      commentTotal.value = res.data.total || 0
    }
  } catch {
    // ignore
  }
}

function loadMoreComments() {
  commentPage.value++
  loadComments()
}

async function handleDownload() {
  downloading.value = true
  try {
    const res = await downloadResource(resourceId.value)
    if (res.code === 200 && res.data) {
      message.success('下载成功，正在打开文件...')
      // 在新窗口打开文件
      window.open(res.data, '_blank')
    } else {
      message.error(res.message || '下载失败')
    }
  } catch {
    message.error('下载失败')
  } finally {
    downloading.value = false
  }
}

async function handleToggleCollect() {
  collecting.value = true
  try {
    if (resource.value.isCollected) {
      const res = await cancelCollect(resourceId.value)
      if (res.code === 200) {
        resource.value.isCollected = false
        resource.value.collectCount = Math.max(0, resource.value.collectCount - 1)
        message.success('已取消收藏')
      }
    } else {
      const res = await collectResource(resourceId.value)
      if (res.code === 200) {
        resource.value.isCollected = true
        resource.value.collectCount += 1
        message.success('收藏成功')
      }
    }
  } catch {
    message.error('操作失败')
  } finally {
    collecting.value = false
  }
}

function startReply(comment: ResComment) {
  replyingTo.value = comment.id
  replyContent.value = ''
}

function cancelReply() {
  replyingTo.value = null
  replyContent.value = ''
}

async function handleSubmitReply(parentId: number) {
  if (!replyContent.value.trim()) {
    message.warning('请输入回复内容')
    return
  }
  commenting.value = true
  try {
    const res = await createComment({
      resourceId: resourceId.value,
      content: replyContent.value.trim(),
      parentId,
    })
    if (res.code === 200) {
      message.success('回复成功')
      replyContent.value = ''
      replyingTo.value = null
      commentPage.value = 1
      resource.value.commentCount += 1
      await loadComments()
    } else {
      message.error(res.message || '回复失败')
    }
  } catch {
    message.error('回复失败')
  } finally {
    commenting.value = false
  }
}

async function handleSubmitComment() {
  if (!commentContent.value.trim()) {
    message.warning('请输入评论内容')
    return
  }
  commenting.value = true
  try {
    const res = await createComment({
      resourceId: resourceId.value,
      content: commentContent.value.trim(),
    })
    if (res.code === 200) {
      message.success('评论成功')
      commentContent.value = ''
      commentPage.value = 1
      resource.value.commentCount += 1
      await loadComments()
    } else {
      message.error(res.message || '评论失败')
    }
  } catch {
    message.error('评论失败')
  } finally {
    commenting.value = false
  }
}

async function handleDelete() {
  const confirmed = confirm('确定要删除这个资源吗？')
  if (!confirmed) return
  try {
    const res = await deleteResource(resourceId.value)
    if (res.code === 200) {
      message.success('删除成功')
      router.push('/resources')
    } else {
      message.error(res.message || '删除失败')
    }
  } catch {
    message.error('删除失败')
  }
}

function formatDate(dateStr: string | null) {
  if (!dateStr) return '-'
  return dateStr.replace('T', ' ').substring(0, 19)
}

function formatFileSize(bytes: number) {
  if (!bytes) return '未知'
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  if (bytes < 1024 * 1024 * 1024) return (bytes / (1024 * 1024)).toFixed(1) + ' MB'
  return (bytes / (1024 * 1024 * 1024)).toFixed(1) + ' GB'
}

onMounted(() => {
  loadDetail()
  loadComments()
})
</script>

<style scoped>
.detail-page {
  max-width: 1200px;
  margin: 0 auto;
}
.detail-card {
  margin-bottom: 24px;
}
.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.detail-header h1 {
  font-size: 22px;
  margin: 0;
  flex: 1;
}
.detail-meta {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 12px;
}
.detail-stats {
  display: flex;
  gap: 16px;
  font-size: 13px;
  color: #999;
  flex-wrap: wrap;
}
.file-info {
  background: #f9f9f9;
  padding: 16px;
  border-radius: 8px;
  margin-bottom: 16px;
}
.file-info-item {
  display: flex;
  margin-bottom: 8px;
  font-size: 14px;
}
.file-info-item .label {
  color: #999;
  width: 100px;
  flex-shrink: 0;
}
.points {
  color: #f59e0b;
  font-weight: 600;
}
.free {
  color: #18a058;
  font-weight: 600;
}
.action-buttons {
  display: flex;
  align-items: center;
}
.desc-card {
  margin-bottom: 24px;
}
.description-content {
  white-space: pre-wrap;
  line-height: 1.8;
  color: #333;
}
.comment-card {
  margin-bottom: 24px;
}
.comment-input-area {
  margin-bottom: 8px;
}
.comment-item {
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}
.comment-item:last-child {
  border-bottom: none;
}
.comment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}
.comment-time {
  font-size: 12px;
  color: #ccc;
}
.comment-body {
  font-size: 14px;
  line-height: 1.6;
  color: #333;
}
.comment-user-link {
  text-decoration: none;
  color: inherit;
}
.comment-user-link:hover {
  color: #18a058;
}
.comment-actions {
  margin-top: 4px;
}
.reply-input-area {
  margin-top: 8px;
  padding: 8px;
  background: #f9f9f9;
  border-radius: 6px;
}
.reply-actions {
  margin-top: 8px;
  text-align: right;
}
.load-more-comments {
  text-align: center;
  margin-top: 12px;
}
.comment-group {
  margin-bottom: 16px;
}
.comment-group:last-child {
  margin-bottom: 0;
}
.reply-item {
  margin-left: 32px;
  padding: 8px 12px;
  background: #f9f9f9;
  border-radius: 6px;
  margin-top: 8px;
}
.reply-content-wrapper {
  width: 100%;
}
.reply-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}
.reply-to-label {
  font-size: 12px;
  color: #999;
}
.reply-body {
  font-size: 14px;
  line-height: 1.6;
  color: #333;
}
.sidebar-card {
  margin-bottom: 24px;
}
.sidebar-cover {
  width: 100%;
  border-radius: 4px;
}
.author-info {
  display: flex;
  align-items: center;
  gap: 12px;
}
.author-name {
  font-size: 16px;
  font-weight: 500;
}
.author-info-link {
  text-decoration: none;
  color: inherit;
  display: block;
}
.author-info-link:hover .author-name {
  color: #18a058;
}
.stat-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.stat-row {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
  color: #666;
}
.stat-value {
  font-weight: 600;
  color: #333;
}
</style>
