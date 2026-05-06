<template>
  <div class="detail-page">
    <n-grid :cols="24" :x-gap="24">
      <!-- 主要内容区 -->
      <n-grid-item :span="17">
        <div v-if="loading" class="loading-center">
          <n-spin size="large" />
        </div>

        <template v-else-if="article">
          <!-- 文章基本信息 -->
          <n-card :bordered="true" class="detail-card">
            <template #header>
              <div class="detail-header">
                <h1>{{ article.title }}</h1>
                <div class="header-actions">
                  <n-button
                    :type="article.isLiked ? 'error' : 'default'"
                    size="small"
                    @click="handleToggleLike"
                    :loading="liking"
                  >
                    {{ article.isLiked ? '❤️ ' : '🤍 ' }}{{ article.likeCount || 0 }}
                  </n-button>
                  <n-button
                    :type="article.isCollected ? 'warning' : 'default'"
                    size="small"
                    @click="handleToggleCollect"
                    :loading="collecting"
                  >
                    {{ article.isCollected ? '⭐ 已收藏' : '☆ 收藏' }}
                  </n-button>
                </div>
              </div>
            </template>

            <div class="detail-meta">
              <n-tag :bordered="false" size="small">{{ article.categoryName }}</n-tag>
              <n-tag v-for="tag in article.tags" :key="tag" size="small" type="info" :bordered="false">
                {{ tag }}
              </n-tag>
            </div>

            <div class="detail-stats">
              <span>👤 {{ article.nickname || '匿名' }}</span>
              <span>👁 {{ article.viewCount }} 阅读</span>
              <span>👍 {{ article.likeCount }} 点赞</span>
              <span>💬 {{ article.commentCount }} 评论</span>
              <span>⭐ {{ article.collectCount }} 收藏</span>
              <span>📅 {{ formatDate(article.createTime) }}</span>
            </div>

            <n-divider />

            <!-- 文章内容 -->
            <div class="article-content markdown-body" v-html="renderedContent"></div>
          </n-card>

          <!-- 评论区域 -->
          <n-card title="评论" :bordered="true" class="comment-card">
            <div v-if="article.isComment === 1">
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
                      <router-link :to="'/user/' + group.parent.userId" class="comment-user-link">
                        <n-avatar v-if="group.parent.avatar" :src="group.parent.avatar" :size="24" round style="vertical-align:middle;margin-right:4px" />
                        <n-avatar v-else :size="24" round style="vertical-align:middle;margin-right:4px">{{ group.parent.nickname?.charAt(0) || '?' }}</n-avatar>
                        <strong>{{ group.parent.nickname || '匿名用户' }}</strong>
                      </router-link>
                      <span class="comment-time">{{ formatDate(group.parent.createTime) }}</span>
                    </div>
                    <div class="comment-body">{{ group.parent.content }}</div>
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
                      <n-button v-else size="tiny" text @click="cancelReply">取消回复</n-button>
                    </div>
                    <!-- 回复输入框 -->
                    <div v-if="replyingTo === group.parent.id" class="reply-input-area">
                      <n-input
                        v-model:value="replyContent"
                        type="textarea"
                        :rows="2"
                        :placeholder="'回复 @' + (group.parent.nickname || '匿名用户')"
                      />
                      <div class="reply-actions">
                        <n-button type="primary" size="tiny" @click="handleSubmitReply(group.parent.id)" :loading="commenting">
                          回复
                        </n-button>
                      </div>
                    </div>
                  </div>
                  <!-- 回复列表 -->
                  <div v-for="reply in group.replies" :key="reply.id" class="reply-item">
                    <div class="reply-content-wrapper">
                      <div class="reply-header">
                        <router-link :to="'/user/' + reply.userId" class="comment-user-link">
                          <n-avatar v-if="reply.avatar" :src="reply.avatar" :size="24" round style="vertical-align:middle;margin-right:4px" />
                          <n-avatar v-else :size="24" round style="vertical-align:middle;margin-right:4px">{{ reply.nickname?.charAt(0) || '?' }}</n-avatar>
                          <strong>{{ reply.nickname || '匿名用户' }}</strong>
                        </router-link>
                        <span class="reply-to-label">回复 @{{ reply.replyToNickname || '匿名用户' }}</span>
                        <span class="comment-time">{{ formatDate(reply.createTime) }}</span>
                      </div>
                      <div class="reply-body">{{ reply.content }}</div>
                    </div>
                  </div>
                </div>
                <div v-if="commentTotal > commentPage * commentSize" class="load-more-comments">
                  <n-button quaternary size="small" @click="loadMoreComments">加载更多</n-button>
                </div>
              </div>
              <n-empty v-else description="暂无评论，快来抢沙发吧" />
            </div>
            <div v-else>
              <n-empty description="作者已关闭评论" />
            </div>
          </n-card>
        </template>

        <n-empty v-else description="文章不存在" />
      </n-grid-item>

      <!-- 侧边栏 -->
      <n-grid-item :span="7">
        <!-- 作者信息 -->
        <n-card title="作者" :bordered="true" class="sidebar-card">
          <router-link :to="'/user/' + article?.userId" class="author-info-link">
            <div class="author-info">
              <n-avatar v-if="article?.avatar" :src="article.avatar" :size="60" round />
              <n-avatar v-else :size="60" round>{{ article?.nickname?.charAt(0) || '?' }}</n-avatar>
              <div class="author-name">{{ article?.nickname || '匿名' }}</div>
            </div>
          </router-link>
        </n-card>

        <!-- 文章统计 -->
        <n-card title="统计" :bordered="true" class="sidebar-card">
          <div class="stat-item"><span>阅读量</span><span>{{ article?.viewCount || 0 }}</span></div>
          <div class="stat-item"><span>点赞数</span><span>{{ article?.likeCount || 0 }}</span></div>
          <div class="stat-item"><span>评论数</span><span>{{ article?.commentCount || 0 }}</span></div>
          <div class="stat-item"><span>收藏数</span><span>{{ article?.collectCount || 0 }}</span></div>
        </n-card>
      </n-grid-item>
    </n-grid>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import {
  getArticleDetail,
  likeArticle,
  cancelLikeArticle,
  collectArticle,
  cancelCollectArticle,
  getArticleComments,
  createArticleComment,
  type ArtArticle,
  type ArtComment,
} from '@/api/article'

const route = useRoute()
const router = useRouter()
const message = useMessage()

const article = ref<ArtArticle | null>(null)
const loading = ref(true)
const liking = ref(false)
const collecting = ref(false)
const commenting = ref(false)
const commentContent = ref('')
const replyContent = ref('')
const replyingTo = ref<number | null>(null)

const comments = ref<ArtComment[]>([])
const commentTotal = ref(0)
const commentPage = ref(1)
const commentSize = 10

interface CommentGroup {
  parent: ArtComment
  replies: ArtComment[]
}

const commentGroups = computed<CommentGroup[]>(() => {
  const groups: CommentGroup[] = []
  for (const c of comments.value) {
    if (c.parentId == null) {
      groups.push({
        parent: c,
        replies: comments.value.filter((r) => r.parentId === c.id),
      })
    }
  }
  return groups
})

// 简单的 Markdown 渲染（将换行转 br，识别粗体和标题）
const renderedContent = computed(() => {
  if (!article.value?.content) return ''
  let html = article.value.content
  // 转义 HTML
  html = html.replace(/</g, '&lt;').replace(/>/g, '&gt;')
  // 代码块
  html = html.replace(/```(\w*)\n?([\s\S]*?)```/g, '<pre><code class="language-$1">$2</code></pre>')
  // 行内代码
  html = html.replace(/`([^`]+)`/g, '<code>$1</code>')
  // 标题
  html = html.replace(/^### (.+)$/gm, '<h3>$1</h3>')
  html = html.replace(/^## (.+)$/gm, '<h2>$1</h2>')
  html = html.replace(/^# (.+)$/gm, '<h1>$1</h1>')
  // 粗体
  html = html.replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
  // 列表
  html = html.replace(/^- (.+)$/gm, '<li>$1</li>')
  // 空行
  html = html.replace(/\n\n/g, '</p><p>')
  html = html.replace(/\n/g, '<br/>')
  return '<p>' + html + '</p>'
})

async function loadArticle() {
  const id = Number(route.params.id)
  if (!id) {
    message.error('文章ID无效')
    router.push('/articles')
    return
  }
  loading.value = true
  try {
    const res = await getArticleDetail(id)
    if (res.code === 200 && res.data) {
      article.value = res.data
    } else {
      message.error('文章不存在')
      router.push('/articles')
    }
  } catch {
    message.error('加载文章详情失败')
  } finally {
    loading.value = false
  }
}

async function loadComments(page = 1) {
  const id = Number(route.params.id)
  try {
    const res = await getArticleComments(id, page, commentSize)
    if (res.code === 200 && res.data) {
      if (page === 1) {
        comments.value = res.data.records || []
      } else {
        comments.value.push(...(res.data.records || []))
      }
      commentTotal.value = res.data.total || 0
      commentPage.value = page
    }
  } catch {
    // ignore
  }
}

function loadMoreComments() {
  loadComments(commentPage.value + 1)
}

async function handleToggleLike() {
  if (!article.value) return
  liking.value = true
  try {
    if (article.value.isLiked) {
      await cancelLikeArticle(article.value.id)
      article.value.isLiked = false
      article.value.likeCount = Math.max(0, (article.value.likeCount || 0) - 1)
    } else {
      await likeArticle(article.value.id)
      article.value.isLiked = true
      article.value.likeCount = (article.value.likeCount || 0) + 1
    }
  } catch (e: any) {
    message.error(e?.message || '操作失败')
  } finally {
    liking.value = false
  }
}

async function handleToggleCollect() {
  if (!article.value) return
  collecting.value = true
  try {
    if (article.value.isCollected) {
      await cancelCollectArticle(article.value.id)
      article.value.isCollected = false
      article.value.collectCount = Math.max(0, (article.value.collectCount || 0) - 1)
    } else {
      await collectArticle(article.value.id)
      article.value.isCollected = true
      article.value.collectCount = (article.value.collectCount || 0) + 1
    }
  } catch (e: any) {
    message.error(e?.message || '操作失败')
  } finally {
    collecting.value = false
  }
}

async function handleSubmitComment() {
  if (!commentContent.value.trim()) {
    message.warning('请输入评论内容')
    return
  }
  const id = Number(route.params.id)
  commenting.value = true
  try {
    const res = await createArticleComment({
      articleId: id,
      content: commentContent.value.trim(),
    })
    if (res.code === 200) {
      message.success('评论成功')
      commentContent.value = ''
      loadComments(1)
    }
  } catch (e: any) {
    message.error(e?.message || '评论失败')
  } finally {
    commenting.value = false
  }
}

function startReply(comment: ArtComment) {
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
  const id = Number(route.params.id)
  commenting.value = true
  try {
    const res = await createArticleComment({
      articleId: id,
      content: replyContent.value.trim(),
      parentId,
    })
    if (res.code === 200) {
      message.success('回复成功')
      replyContent.value = ''
      replyingTo.value = null
      loadComments(1)
    }
  } catch (e: any) {
    message.error(e?.message || '回复失败')
  } finally {
    commenting.value = false
  }
}

function formatDate(dateStr: string | null) {
  if (!dateStr) return '-'
  return dateStr.replace('T', ' ').substring(0, 19)
}

onMounted(() => {
  loadArticle()
  loadComments()
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
}
.detail-header h1 {
  font-size: 24px;
  margin: 0;
  flex: 1;
}
.header-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
  margin-left: 16px;
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
.article-content {
  line-height: 1.8;
  font-size: 15px;
}
.article-content :deep(h1),
.article-content :deep(h2),
.article-content :deep(h3) {
  margin: 24px 0 12px;
}
.article-content :deep(p) {
  margin: 8px 0;
}
.article-content :deep(pre) {
  background: #f5f7fa;
  padding: 16px;
  border-radius: 8px;
  overflow-x: auto;
  margin: 12px 0;
}
.article-content :deep(code) {
  background: #f0f0f0;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 13px;
}
.article-content :deep(pre code) {
  background: none;
  padding: 0;
}
.article-content :deep(img) {
  max-width: 100%;
  border-radius: 8px;
  margin: 12px 0;
}
.article-content :deep(blockquote) {
  border-left: 4px solid #18a058;
  padding: 8px 16px;
  margin: 12px 0;
  background: #f6ffed;
}
.article-content :deep(li) {
  margin: 4px 0;
}
.comment-card {
  margin-top: 16px;
}
.comment-input-area {
  margin-bottom: 16px;
}
.comment-group {
  margin-bottom: 16px;
  padding: 12px;
  background: #fafafa;
  border-radius: 8px;
}
.comment-item {
  padding: 8px 0;
}
.comment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}
.comment-user-link {
  text-decoration: none;
  color: #18a058;
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
.comment-actions {
  margin-top: 4px;
}
.reply-input-area {
  margin-top: 8px;
  padding-left: 16px;
}
.reply-actions {
  margin-top: 4px;
}
.reply-item {
  padding: 8px 0 8px 24px;
  border-left: 2px solid #e8e8e8;
  margin: 4px 0;
}
.reply-content-wrapper {
  padding: 4px 8px;
}
.reply-header {
  display: flex;
  gap: 8px;
  align-items: center;
  margin-bottom: 2px;
}
.reply-to-label {
  font-size: 12px;
  color: #999;
}
.reply-body {
  font-size: 14px;
  line-height: 1.6;
}
.load-more-comments {
  text-align: center;
  margin-top: 8px;
}
.sidebar-card {
  margin-bottom: 16px;
}
.author-info-link {
  text-decoration: none;
  color: inherit;
  display: block;
}
.author-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 8px 0;
}
.author-name {
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
