<template>
  <div class="ticket-management">
    <h2 style="margin-bottom: 20px">工单管理</h2>

    <n-card style="margin-bottom: 16px">
      <n-space>
        <n-select v-model:value="query.status" :options="statusOptions" placeholder="工单状态" clearable style="width: 160px" @update:value="search" />
        <n-button type="primary" @click="search">刷新</n-button>
      </n-space>
    </n-card>

    <n-card>
      <n-data-table :columns="columns" :data="list" :loading="loading" :pagination="pagination" @update:page="handlePageChange" />
    </n-card>

    <!-- 工单详情弹窗 -->
    <n-modal v-model:show="showDetail" title="工单详情" preset="card" style="width: 700px" :mask-closable="false">
      <template v-if="currentTicket">
        <n-descriptions label-placement="left" bordered :column="2">
          <n-descriptions-item label="标题">{{ currentTicket.title }}</n-descriptions-item>
          <n-descriptions-item label="用户">{{ currentTicket.nickname }}</n-descriptions-item>
          <n-descriptions-item label="状态">{{ statusMap[currentTicket.status] || currentTicket.status }}</n-descriptions-item>
          <n-descriptions-item label="优先级">{{ priorityMap[currentTicket.priority] || currentTicket.priority }}</n-descriptions-item>
          <n-descriptions-item label="描述" :span="2">{{ currentTicket.description || '无' }}</n-descriptions-item>
        </n-descriptions>
        <n-divider>回复</n-divider>
        <div v-if="currentTicket.replies && currentTicket.replies.length > 0">
          <div v-for="reply in currentTicket.replies" :key="reply.id" class="reply-item">
            <div class="reply-meta">
              <strong>{{ reply.isStaff ? '客服' : '用户' }}</strong>
              <span style="color: #999; margin-left: 12px">{{ reply.createTime }}</span>
            </div>
            <div class="reply-content">{{ reply.content }}</div>
          </div>
        </div>
        <n-empty v-else description="暂无回复" style="margin: 16px 0" />
        <n-divider />
        <n-space vertical>
          <n-input v-model:value="replyContent" type="textarea" placeholder="输入回复内容" :rows="3" />
          <n-space>
            <n-button type="primary" @click="doReply" :loading="replying">回复</n-button>
            <n-button v-if="currentTicket.status !== 'CLOSED'" type="warning" @click="doClose">关闭工单</n-button>
          </n-space>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, h, onMounted } from 'vue'
import { NButton, NSpace, NTag, useMessage } from 'naive-ui'
import {
  getAdminTickets,
  getAdminTicketDetail,
  replyTicket as adminReplyTicket,
  closeTicket as adminCloseTicket,
} from '@/api/admin'
import {
  type ChatTicket,
  TICKET_STATUS_MAP,
  TICKET_STATUS_TAG,
  TICKET_PRIORITY_MAP,
} from '@/api/ai'

const message = useMessage()
const loading = ref(false)
const list = ref<ChatTicket[]>([])
const showDetail = ref(false)
const currentTicket = ref<ChatTicket | null>(null)
const replyContent = ref('')
const replying = ref(false)

const statusMap = TICKET_STATUS_MAP
const priorityMap = TICKET_PRIORITY_MAP
const statusOptions = [
  { label: '全部', value: '' },
  ...Object.entries(TICKET_STATUS_MAP).map(([k, v]) => ({ label: v, value: k })),
]

const query = reactive({ page: 1, size: 10, status: '' })
const pagination = reactive({ page: 1, pageCount: 1, pageSize: 10, onChange: (p: number) => { query.page = p; loadData() } })

const columns = [
  { title: 'ID', key: 'id', width: 60 },
  { title: '标题', key: 'title', ellipsis: true },
  { title: '用户', key: 'nickname', width: 100 },
  {
    title: '状态', key: 'status', width: 90,
    render(row: ChatTicket) {
      return h(NTag, { type: (TICKET_STATUS_TAG as any)[row.status] || 'default', size: 'small' }, { default: () => TICKET_STATUS_MAP[row.status] || row.status })
    },
  },
  {
    title: '优先级', key: 'priority', width: 70,
    render(row: ChatTicket) { return h('span', null, { default: () => TICKET_PRIORITY_MAP[row.priority] || row.priority }) },
  },
  { title: '创建时间', key: 'createTime', width: 170 },
  {
    title: '操作', key: 'actions', width: 120,
    render(row: ChatTicket) {
      return h(NButton, { size: 'small', type: 'primary', onClick: () => viewDetail(row.id) }, { default: () => '详情' })
    },
  },
]

function search() { query.page = 1; loadData() }
function handlePageChange(page: number) { query.page = page; loadData() }

async function loadData() {
  loading.value = true
  try {
    const res = await getAdminTickets(query.page, query.size, query.status || undefined)
    if (res.code === 200) {
      list.value = res.data.records
      pagination.pageCount = Math.ceil(res.data.total / res.data.size)
    }
  } catch { message.error('加载失败') }
  finally { loading.value = false }
}

async function viewDetail(id: number) {
  try {
    const res = await getAdminTicketDetail(id)
    if (res.code === 200) {
      currentTicket.value = res.data
      replyContent.value = ''
      showDetail.value = true
    }
  } catch { message.error('加载详情失败') }
}

async function doReply() {
  if (!replyContent.value || !currentTicket.value) { message.warning('请输入回复内容'); return }
  replying.value = true
  try {
    const res = await adminReplyTicket(currentTicket.value.id, replyContent.value)
    if (res.code === 200) {
      message.success('回复成功')
      replyContent.value = ''
      viewDetail(currentTicket.value.id)
    }
  } catch { message.error('回复失败') }
  finally { replying.value = false }
}

async function doClose() {
  if (!currentTicket.value) return
  try {
    const res = await adminCloseTicket(currentTicket.value.id, '管理员关闭')
    if (res.code === 200) {
      message.success('已关闭')
      showDetail.value = false
      loadData()
    }
  } catch { message.error('关闭失败') }
}

onMounted(loadData)
</script>

<style scoped>
.reply-item {
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}
.reply-meta {
  margin-bottom: 6px;
  font-size: 13px;
}
.reply-content {
  color: #333;
  line-height: 1.6;
}
</style>
