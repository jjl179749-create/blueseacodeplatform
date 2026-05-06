<template>
  <div class="tickets-page">
    <div class="page-header">
      <h2>我的工单</h2>
      <n-button type="primary" @click="showCreateDialog = true">创建工单</n-button>
    </div>

    <!-- 工单列表 -->
    <n-card v-if="!selectedTicket" :bordered="false" class="tickets-card">
      <n-list>
        <n-list-item v-for="ticket in tickets" :key="ticket.id" clickable @click="loadTicketDetail(ticket.id)">
          <template #prefix>
            <n-tag :type="statusTag(ticket.status)" size="small">
              {{ statusLabel(ticket.status) }}
            </n-tag>
          </template>
          <n-ellipsis :line-clamp="1">{{ ticket.title }}</n-ellipsis>
          <template #suffix>
            <div class="ticket-meta">
              <span v-if="ticket.priority" class="ticket-priority" :class="'priority-' + ticket.priority">
                {{ priorityLabel(ticket.priority) }}
              </span>
              <span class="ticket-time">{{ ticket.createTime?.slice(0, 10) }}</span>
            </div>
          </template>
        </n-list-item>
      </n-list>
      <div v-if="tickets.length === 0" class="empty-state">
        <p>暂无工单</p>
      </div>
      <n-pagination
        v-if="total > size"
        v-model:page="page"
        :page-count="Math.ceil(total / size)"
        size="small"
        @update:page="loadTickets"
        style="margin-top: 16px; justify-content: center;"
      />
    </n-card>

    <!-- 工单详情 -->
    <n-card v-else :bordered="false" class="tickets-card">
      <template #header>
        <div class="detail-header">
          <n-button quaternary @click="selectedTicket = null">← 返回</n-button>
          <n-tag :type="statusTag(selectedTicket.status)" size="small">
            {{ statusLabel(selectedTicket.status) }}
          </n-tag>
        </div>
      </template>

      <h3>{{ selectedTicket.title }}</h3>
      <div class="detail-info">
        <n-description label-placement="left" :column="2">
          <n-description-item label="分类">{{ selectedTicket.category || '-' }}</n-description-item>
          <n-description-item label="优先级">
            <n-tag :type="selectedTicket.priority >= 3 ? 'error' : 'default'" size="small">
              {{ priorityLabel(selectedTicket.priority) }}
            </n-tag>
          </n-description-item>
          <n-description-item label="创建时间">{{ selectedTicket.createTime }}</n-description-item>
          <n-description-item label="处理人">{{ selectedTicket.assigneeName || '未分配' }}</n-description-item>
        </n-description>
      </div>
      <div v-if="selectedTicket.description" class="detail-desc">
        <p>{{ selectedTicket.description }}</p>
      </div>

      <!-- 回复列表 -->
      <div class="reply-section">
        <h4>回复记录</h4>
        <div v-if="selectedTicket.replies && selectedTicket.replies.length > 0">
          <div v-for="reply in selectedTicket.replies" :key="reply.id" class="reply-item" :class="{ staff: reply.isStaff }">
            <div class="reply-header">
              <n-tag :type="reply.isStaff ? 'info' : 'default'" size="tiny">
                {{ reply.isStaff ? '客服' : '我' }}
              </n-tag>
              <span class="reply-time">{{ reply.createTime }}</span>
            </div>
            <div class="reply-content">{{ reply.content }}</div>
          </div>
        </div>
        <div v-else class="empty-replies">暂无回复</div>
      </div>

      <!-- 回复输入 -->
      <div v-if="selectedTicket.status !== 'CLOSED'" class="reply-input">
        <n-input
          v-model:value="replyContent"
          type="textarea"
          :rows="2"
          placeholder="输入回复内容..."
        />
        <div class="reply-actions">
          <n-button
            type="primary"
            size="small"
            :loading="sendingReply"
            :disabled="!replyContent.trim()"
            @click="sendReply"
          >
            回复
          </n-button>
          <n-button
            size="small"
            @click="showCloseDialog = true"
          >
            关闭工单
          </n-button>
        </div>
      </div>
    </n-card>

    <!-- 创建工单对话框 -->
    <n-modal v-model:show="showCreateDialog" title="创建工单" preset="card" style="width: 520px">
      <n-form ref="formRef" :model="form" :rules="formRules">
        <n-form-item label="标题" path="title">
          <n-input v-model:value="form.title" placeholder="请简要描述您的问题" />
        </n-form-item>
        <n-form-item label="分类" path="category">
          <n-select v-model:value="form.category" :options="categoryOptions" placeholder="请选择分类" />
        </n-form-item>
        <n-form-item label="详细描述" path="description">
          <n-input v-model:value="form.description" type="textarea" :rows="4" placeholder="请详细描述您遇到的问题" />
        </n-form-item>
        <n-form-item label="优先级">
          <n-select v-model:value="form.priority" :options="priorityOptions" />
        </n-form-item>
      </n-form>
      <template #footer>
        <n-button @click="showCreateDialog = false">取消</n-button>
        <n-button type="primary" :loading="submitting" @click="handleCreate">提交</n-button>
      </template>
    </n-modal>

    <!-- 关闭工单对话框 -->
    <n-modal v-model:show="showCloseDialog" title="关闭工单" preset="card" style="width: 400px">
      <n-input v-model:value="closeReason" type="textarea" :rows="3" placeholder="请填写关闭原因（可选）" />
      <template #footer>
        <n-button @click="showCloseDialog = false">取消</n-button>
        <n-button type="error" :loading="closing" @click="handleClose">确认关闭</n-button>
      </template>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import {
  getMyTickets,
  getTicketDetail,
  createTicket,
  replyTicket,
  closeTicket,
  TICKET_STATUS_MAP,
  TICKET_STATUS_TAG,
  TICKET_PRIORITY_MAP,
  TICKET_CATEGORIES,
  type ChatTicket,
} from '@/api/ai'
import { useMessage } from 'naive-ui'

const message = useMessage()

// 列表
const tickets = ref<ChatTicket[]>([])
const page = ref(1)
const size = 10
const total = ref(0)

// 详情
const selectedTicket = ref<ChatTicket | null>(null)
const replyContent = ref('')
const sendingReply = ref(false)
const showCloseDialog = ref(false)
const closeReason = ref('')
const closing = ref(false)

// 创建
const showCreateDialog = ref(false)
const submitting = ref(false)
const form = ref({ title: '', category: null as string | null, description: '', priority: 2 })
const formRules = { title: [{ required: true, message: '请输入标题', trigger: 'blur' }] }
const categoryOptions = TICKET_CATEGORIES.map((c) => ({ label: c.label, value: c.value }))
const priorityOptions = [
  { label: '低', value: 1 },
  { label: '中', value: 2 },
  { label: '高', value: 3 },
  { label: '紧急', value: 4 },
]

function statusLabel(s: string) { return TICKET_STATUS_MAP[s] || s }
function statusTag(s: string) { return TICKET_STATUS_TAG[s] || 'default' }
function priorityLabel(p: number) { return TICKET_PRIORITY_MAP[p] || '-' }

/** 加载工单列表 */
async function loadTickets() {
  try {
    const res = await getMyTickets(page.value, size)
    if (res.code === 200 && res.data) {
      tickets.value = res.data.records
      total.value = res.data.total
    }
  } catch {
    message.error('加载工单列表失败')
  }
}

/** 加载工单详情 */
async function loadTicketDetail(id: number) {
  try {
    const res = await getTicketDetail(id)
    if (res.code === 200 && res.data) {
      selectedTicket.value = res.data
    } else {
      message.error(res.message || '加载失败')
    }
  } catch {
    message.error('网络错误')
  }
}

/** 回复工单 */
async function sendReply() {
  if (!selectedTicket.value || !replyContent.value.trim()) return
  sendingReply.value = true
  try {
    const res = await replyTicket(selectedTicket.value.id, replyContent.value)
    if (res.code === 200) {
      message.success('回复成功')
      replyContent.value = ''
      loadTicketDetail(selectedTicket.value.id)
    } else {
      message.error(res.message || '回复失败')
    }
  } catch {
    message.error('网络错误')
  } finally {
    sendingReply.value = false
  }
}

/** 关闭工单 */
async function handleClose() {
  if (!selectedTicket.value) return
  closing.value = true
  try {
    const res = await closeTicket(selectedTicket.value.id, closeReason.value)
    if (res.code === 200) {
      message.success('工单已关闭')
      showCloseDialog.value = false
      loadTicketDetail(selectedTicket.value.id)
      loadTickets()
    } else {
      message.error(res.message || '关闭失败')
    }
  } catch {
    message.error('网络错误')
  } finally {
    closing.value = false
  }
}

/** 创建工单 */
async function handleCreate() {
  submitting.value = true
  try {
    const res = await createTicket({
      title: form.value.title,
      category: form.value.category || undefined,
      description: form.value.description || undefined,
      priority: form.value.priority,
    })
    if (res.code === 200) {
      message.success('工单创建成功')
      showCreateDialog.value = false
      form.value = { title: '', category: null, description: '', priority: 2 }
      loadTickets()
    } else {
      message.error(res.message || '创建失败')
    }
  } catch {
    message.error('网络错误')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadTickets()
})
</script>

<style scoped>
.tickets-page {
  max-width: 900px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.page-header h2 {
  margin: 0;
  font-size: 20px;
}

.tickets-card {
  background: #fff;
  border-radius: 8px;
}

.empty-state {
  text-align: center;
  padding: 40px;
  color: #999;
}

.ticket-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 12px;
  color: #999;
}

.ticket-priority {
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 11px;
}

.priority-3, .priority-4 {
  background: #fff1f0;
  color: #f5222d;
}

.priority-2 {
  background: #fff7e6;
  color: #fa8c16;
}

.priority-1 {
  background: #f6ffed;
  color: #52c41a;
}

/* 详情 */
.detail-header {
  display: flex;
  align-items: center;
  gap: 12px;
}

.detail-info {
  margin: 16px 0;
  padding: 16px;
  background: #fafafa;
  border-radius: 8px;
}

.detail-desc {
  margin: 16px 0;
  padding: 16px;
  background: #fafafa;
  border-radius: 8px;
  line-height: 1.7;
}

.detail-desc p {
  margin: 0;
  white-space: pre-wrap;
}

/* 回复 */
.reply-section {
  margin: 20px 0;
}

.reply-section h4 {
  margin-bottom: 12px;
  font-size: 15px;
}

.reply-item {
  padding: 12px;
  border-left: 3px solid #e8e8e8;
  margin-bottom: 8px;
  background: #fafafa;
  border-radius: 4px;
}

.reply-item.staff {
  border-left-color: #18a058;
  background: #f0fff0;
}

.reply-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}

.reply-time {
  font-size: 11px;
  color: #999;
}

.reply-content {
  font-size: 14px;
  line-height: 1.6;
  white-space: pre-wrap;
}

.empty-replies {
  text-align: center;
  color: #999;
  padding: 20px;
}

.reply-input {
  margin-top: 16px;
}

.reply-actions {
  display: flex;
  gap: 8px;
  margin-top: 8px;
}
</style>
