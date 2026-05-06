<template>
  <div class="chat-page">
    <div class="chat-layout">
      <!-- 侧边栏：会话列表 -->
      <div class="chat-sidebar">
        <div class="sidebar-header">
          <h3>AI 智能客服</h3>
          <n-button size="small" @click="startNewSession">新建对话</n-button>
        </div>
        <div class="session-list">
          <div
            v-for="session in sessions"
            :key="session.id"
            class="session-item"
            :class="{ active: session.sessionId === currentSessionId }"
            @click="switchSession(session.sessionId)"
          >
            <div class="session-question">{{ session.question }}</div>
            <div class="session-time">{{ formatTime(session.createTime) }}</div>
          </div>
          <div v-if="sessions.length === 0" class="empty-sessions">
            暂无对话记录
          </div>
        </div>
        <div class="sidebar-footer">
          <router-link to="/chat/tickets">
            <n-button quaternary size="small" style="width: 100%">
              我的工单
            </n-button>
          </router-link>
        </div>
      </div>

      <!-- 主聊天区域 -->
      <div class="chat-main">
        <!-- 消息列表 -->
        <div ref="messageListRef" class="message-list">
          <template v-if="messages.length === 0">
            <div class="welcome-message">
              <div class="welcome-icon">🤖</div>
              <h2>您好！我是蓝海编程平台的智能客服</h2>
              <p>我可以帮您解答以下问题：</p>
              <div class="quick-questions">
                <n-button
                  v-for="(q, idx) in quickQuestions"
                  :key="idx"
                  size="small"
                  @click="sendQuickQuestion(q)"
                >
                  {{ q }}
                </n-button>
              </div>
            </div>
          </template>

          <template v-else>
            <div
              v-for="(msg, idx) in messages"
              :key="idx"
              class="message-item"
              :class="msg.role"
            >
              <div class="message-avatar">
                {{ msg.role === 'user' ? '👤' : '🤖' }}
              </div>
              <div class="message-content">
                <div class="message-bubble">
                  <div v-if="msg.role === 'ai' && msg.type" class="message-type-tag">
                    <n-tag :type="msg.type === 'FAQ' ? 'success' : msg.type === 'KNOWLEDGE' ? 'info' : 'primary'" size="small">
                      {{ msg.type === 'FAQ' ? '常见问题' : msg.type === 'KNOWLEDGE' ? '知识库' : 'AI回答' }}
                    </n-tag>
                  </div>
                  <div class="message-text">{{ msg.content }}</div>
                  <div v-if="msg.type === 'AI' || msg.type === 'KNOWLEDGE'" class="message-suggestions">
                    <n-button
                      v-if="msg.canTransfer"
                      size="tiny"
                      @click="showCreateTicket = true"
                    >
                      转人工客服
                    </n-button>
                  </div>
                </div>
                <div class="message-time">{{ formatTime(msg.createTime) }}</div>
              </div>
            </div>
          </template>

          <!-- 加载状态 -->
          <div v-if="loading" class="message-item ai">
            <div class="message-avatar">🤖</div>
            <div class="message-content">
              <div class="message-bubble thinking">
                <n-spin size="small" /> 正在思考...
              </div>
            </div>
          </div>
        </div>

        <!-- 输入区域 -->
        <div class="chat-input-area">
          <div class="input-wrapper">
            <n-input
              v-model:value="inputMessage"
              type="textarea"
              :autosize="{ minRows: 1, maxRows: 4 }"
              placeholder="输入您的问题..."
              @keydown.enter.prevent="sendMessage"
              :disabled="loading"
            />
            <n-button
              type="primary"
              :loading="loading"
              :disabled="!inputMessage.trim()"
              @click="sendMessage"
            >
              发送
            </n-button>
          </div>
          <div class="input-tips">
            <span>Enter 发送，Shift+Enter 换行</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 创建工单对话框 -->
    <n-modal v-model:show="showCreateTicket" title="创建工单" preset="card" style="width: 520px">
      <n-form ref="ticketFormRef" :model="ticketForm" :rules="ticketRules">
        <n-form-item label="标题" path="title">
          <n-input v-model:value="ticketForm.title" placeholder="请简要描述您的问题" />
        </n-form-item>
        <n-form-item label="分类" path="category">
          <n-select v-model:value="ticketForm.category" :options="ticketCategories" placeholder="请选择分类" />
        </n-form-item>
        <n-form-item label="详细描述" path="description">
          <n-input
            v-model:value="ticketForm.description"
            type="textarea"
            :rows="4"
            placeholder="请详细描述您遇到的问题"
          />
        </n-form-item>
        <n-form-item label="优先级" path="priority">
          <n-select
            v-model:value="ticketForm.priority"
            :options="[
              { label: '低', value: 1 },
              { label: '中', value: 2 },
              { label: '高', value: 3 },
              { label: '紧急', value: 4 },
            ]"
          />
        </n-form-item>
      </n-form>
      <template #footer>
        <n-button @click="showCreateTicket = false">取消</n-button>
        <n-button type="primary" :loading="submittingTicket" @click="submitTicket">提交工单</n-button>
      </template>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick, computed } from 'vue'
import {
  sendChatMessage,
  getChatHistory,
  createTicket,
  type ChatConversation,
  type ChatResponse,
} from '@/api/ai'
import { TICKET_CATEGORIES } from '@/api/ai'
import { useMessage } from 'naive-ui'

const message = useMessage()

// ==================== 状态 ====================
const messages = ref<Array<{
  role: 'user' | 'ai'
  content: string
  type?: string
  canTransfer?: boolean
  createTime?: string
}>>([])

const sessions = ref<ChatConversation[]>([])
const currentSessionId = ref<string>('')
const inputMessage = ref('')
const loading = ref(false)
const messageListRef = ref<HTMLElement | null>(null)

// 快捷问题
const quickQuestions = [
  '积分怎么获取？',
  '如何发布资源？',
  '如何发布文章？',
  '怎么发布需求？',
  '怎么联系客服？',
]

// 创建工单
const showCreateTicket = ref(false)
const submittingTicket = ref(false)
const ticketForm = ref({
  title: '',
  category: null as string | null,
  description: '',
  priority: 2,
})
const ticketCategories = TICKET_CATEGORIES.map((c) => ({ label: c.label, value: c.value }))
const ticketRules = {
  title: [{ required: true, message: '请输入工单标题', trigger: 'blur' }],
}

// ==================== 方法 ====================

/** 加载历史会话列表 */
async function loadSessions() {
  try {
    const res = await getChatHistory()
    if (res.code === 200 && res.data) {
      // 按 sessionId 分组取最新的
      const sessionMap = new Map<string, ChatConversation>()
      res.data.forEach((item) => {
        sessionMap.set(item.sessionId, item)
      })
      sessions.value = Array.from(sessionMap.values()).slice(-20)
    }
  } catch {
    // 静默处理
  }
}

/** 加载指定会话的对话 */
async function loadSessionMessages(sessionId: string) {
  currentSessionId.value = sessionId
  messages.value = []
  loading.value = true
  try {
    const res = await getChatHistory(sessionId)
    if (res.code === 200 && res.data) {
      messages.value = res.data.flatMap((item) => [
        { role: 'user' as const, content: item.question, createTime: item.createTime },
        {
          role: 'ai' as const,
          content: item.answer,
          type: item.answerType,
          canTransfer: true,
          createTime: item.createTime,
        },
      ])
    }
  } catch {
    message.error('加载对话历史失败')
  } finally {
    loading.value = false
    scrollToBottom()
  }
}

/** 切换会话 */
function switchSession(sessionId: string) {
  if (sessionId !== currentSessionId.value) {
    loadSessionMessages(sessionId)
  }
}

/** 新建对话 */
function startNewSession() {
  currentSessionId.value = ''
  messages.value = []
  inputMessage.value = ''
}

/** 发送消息 */
async function sendMessage() {
  const text = inputMessage.value.trim()
  if (!text || loading.value) return

  // 添加用户消息
  messages.value.push({ role: 'user', content: text })
  inputMessage.value = ''
  loading.value = true
  scrollToBottom()

  try {
    const res = await sendChatMessage({
      question: text,
      sessionId: currentSessionId.value || undefined,
    })

    if (res.code === 200 && res.data) {
      const data = res.data
      // 保存 sessionId
      if (data.sessionId && data.sessionId !== currentSessionId.value) {
        currentSessionId.value = data.sessionId
      }
      // 添加AI回复
      messages.value.push({
        role: 'ai',
        content: data.content,
        type: data.type,
        canTransfer: data.canTransfer,
        createTime: new Date().toISOString(),
      })
      // 刷新会话列表
      loadSessions()
    } else {
      message.error(res.message || '发送失败')
    }
  } catch {
    message.error('网络错误，请稍后重试')
  } finally {
    loading.value = false
    scrollToBottom()
  }
}

/** 发送快捷问题 */
function sendQuickQuestion(q: string) {
  inputMessage.value = q
  sendMessage()
}

/** 提交工单 */
async function submitTicket() {
  submittingTicket.value = true
  try {
    const res = await createTicket({
      title: ticketForm.value.title,
      category: ticketForm.value.category || undefined,
      description: ticketForm.value.description || undefined,
      priority: ticketForm.value.priority,
    })
    if (res.code === 200) {
      message.success('工单创建成功')
      showCreateTicket.value = false
      ticketForm.value = { title: '', category: null, description: '', priority: 2 }
    } else {
      message.error(res.message || '创建失败')
    }
  } catch {
    message.error('网络错误')
  } finally {
    submittingTicket.value = false
  }
}

/** 滚动到底部 */
function scrollToBottom() {
  nextTick(() => {
    if (messageListRef.value) {
      messageListRef.value.scrollTop = messageListRef.value.scrollHeight
    }
  })
}

/** 格式化时间 */
function formatTime(t: string | undefined) {
  if (!t) return ''
  const d = new Date(t)
  return `${d.getHours().toString().padStart(2, '0')}:${d.getMinutes().toString().padStart(2, '0')}`
}

// ==================== 初始化 ====================
onMounted(() => {
  loadSessions()
})
</script>

<style scoped>
.chat-page {
  height: calc(100vh - 112px);
  max-width: 1200px;
  margin: 0 auto;
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.chat-layout {
  display: flex;
  height: 100%;
}

/* 侧边栏 */
.chat-sidebar {
  width: 260px;
  border-right: 1px solid #eee;
  display: flex;
  flex-direction: column;
  background: #fafafa;
}

.sidebar-header {
  padding: 16px;
  border-bottom: 1px solid #eee;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.sidebar-header h3 {
  margin: 0;
  font-size: 16px;
}

.session-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.session-item {
  padding: 10px 12px;
  border-radius: 6px;
  cursor: pointer;
  margin-bottom: 4px;
  transition: background 0.2s;
}

.session-item:hover {
  background: #f0f0f0;
}

.session-item.active {
  background: #e8f5e9;
}

.session-question {
  font-size: 13px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: #333;
}

.session-time {
  font-size: 11px;
  color: #999;
  margin-top: 2px;
}

.empty-sessions {
  text-align: center;
  color: #999;
  padding: 24px;
  font-size: 13px;
}

.sidebar-footer {
  padding: 12px;
  border-top: 1px solid #eee;
}

/* 主聊天区 */
.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
}

/* 欢迎消息 */
.welcome-message {
  text-align: center;
  padding: 60px 24px;
}

.welcome-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.welcome-message h2 {
  font-size: 20px;
  margin-bottom: 12px;
  color: #333;
}

.welcome-message p {
  color: #666;
  margin-bottom: 24px;
}

.quick-questions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  justify-content: center;
}

/* 消息 */
.message-item {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}

.message-item.user {
  flex-direction: row-reverse;
}

.message-avatar {
  font-size: 28px;
  flex-shrink: 0;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: #f5f5f5;
}

.message-item.user .message-avatar {
  background: #e8f5e9;
}

.message-content {
  max-width: 70%;
}

.message-item.user .message-content {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

.message-bubble {
  padding: 12px 16px;
  border-radius: 12px;
  background: #f5f7fa;
  line-height: 1.6;
  font-size: 14px;
  white-space: pre-wrap;
  word-break: break-word;
}

.message-item.user .message-bubble {
  background: #18a058;
  color: #fff;
}

.message-item.ai .message-bubble {
  border-top-left-radius: 4px;
}

.message-item.user .message-bubble {
  border-bottom-right-radius: 4px;
}

.message-type-tag {
  margin-bottom: 6px;
}

.message-text {
  line-height: 1.7;
}

.message-time {
  font-size: 11px;
  color: #bbb;
  margin-top: 4px;
}

.message-suggestions {
  margin-top: 10px;
  display: flex;
  gap: 6px;
}

.thinking {
  color: #999;
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 输入区 */
.chat-input-area {
  padding: 12px 24px 20px;
  border-top: 1px solid #eee;
  background: #fff;
}

.input-wrapper {
  display: flex;
  gap: 12px;
  align-items: flex-end;
}

.input-wrapper .n-input {
  flex: 1;
}

.input-tips {
  text-align: right;
  font-size: 11px;
  color: #bbb;
  margin-top: 4px;
}
</style>
