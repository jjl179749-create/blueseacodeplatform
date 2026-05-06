<template>
  <div class="notification-page">
    <n-card title="通知中心" :bordered="true" style="max-width: 700px; margin: 0 auto">
      <template #header-extra>
        <n-button size="small" quaternary @click="markAllRead" :disabled="unreadCount === 0">
          全部标记已读
        </n-button>
      </template>
      <n-tabs v-model:value="tab" @update:value="loadData">
        <n-tab-pane name="all" tab="全部">
          <div v-if="loading" style="text-align:center;padding:40px"><n-spin /></div>
          <template v-else-if="list.length > 0">
            <div
              v-for="item in list"
              :key="item.id"
              class="notification-item"
              :class="{ unread: !item.isRead }"
              @click="markRead(item)"
            >
              <div class="noti-header">
                <n-tag size="small" :type="typeTag(item.type)" style="margin-right:8px">{{ typeLabel(item.type) }}</n-tag>
                <span class="noti-title">{{ item.title }}</span>
                <span class="noti-time">{{ formatTime(item.createTime) }}</span>
              </div>
              <div class="noti-body" v-if="item.content">{{ item.content }}</div>
            </div>
            <div style="text-align:center;margin-top:16px">
              <n-button v-if="hasMore" quaternary size="small" @click="loadMore">加载更多</n-button>
              <span v-else style="color:#999;font-size:13px">没有更多了</span>
            </div>
          </template>
          <n-empty v-else description="暂无通知" />
        </n-tab-pane>
        <n-tab-pane name="unread" tab="未读">
          <div v-if="loading" style="text-align:center;padding:40px"><n-spin /></div>
          <template v-else-if="list.length > 0">
            <div
              v-for="item in list"
              :key="item.id"
              class="notification-item unread"
              @click="markRead(item)"
            >
              <div class="noti-header">
                <n-tag size="small" :type="typeTag(item.type)" style="margin-right:8px">{{ typeLabel(item.type) }}</n-tag>
                <span class="noti-title">{{ item.title }}</span>
                <span class="noti-time">{{ formatTime(item.createTime) }}</span>
              </div>
              <div class="noti-body" v-if="item.content">{{ item.content }}</div>
            </div>
            <div style="text-align:center;margin-top:16px">
              <n-button v-if="hasMore" quaternary size="small" @click="loadMore">加载更多</n-button>
              <span v-else style="color:#999;font-size:13px">没有更多了</span>
            </div>
          </template>
          <n-empty v-else description="暂无未读通知" />
        </n-tab-pane>
        <n-tab-pane name="announcement" tab="系统公告">
          <div v-if="loading" style="text-align:center;padding:40px"><n-spin /></div>
          <template v-else-if="announcements.length > 0">
            <div v-for="item in announcements" :key="item.id" class="notification-item">
              <div class="noti-header">
                <n-tag size="small" type="success" style="margin-right:8px">公告</n-tag>
                <span class="noti-title">{{ item.title }}</span>
                <span class="noti-time">{{ formatTime(item.createTime) }}</span>
              </div>
              <div class="noti-body">{{ item.content }}</div>
            </div>
          </template>
          <n-empty v-else description="暂无公告" />
        </n-tab-pane>
      </n-tabs>
    </n-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useMessage } from 'naive-ui'
import { getNotifications, getUnreadCount, markNotificationRead, markAllNotificationsRead, type MsgNotification } from '@/api/user'

interface SysAnnouncement {
  id: number
  title: string
  content: string
  createTime: string
}

const msg = useMessage()
const tab = ref('all')
const list = ref<MsgNotification[]>([])
const announcements = ref<SysAnnouncement[]>([])
const loading = ref(false)
const page = ref(1)
const total = ref(0)
const unreadCount = ref(0)
const pageSize = 20

function typeTag(type: string) {
  const map: Record<string, string> = { SYSTEM: 'info', LIKE: 'success', COMMENT: 'warning', AUDIT: 'primary', FOLLOW: 'success' }
  return map[type] || 'default'
}

function typeLabel(type: string) {
  const map: Record<string, string> = { SYSTEM: '系统', LIKE: '点赞', COMMENT: '评论', AUDIT: '审核', FOLLOW: '关注' }
  return map[type] || type
}

function formatTime(time: string) {
  if (!time) return ''
  return time.replace('T', ' ').substring(0, 19)
}

async function loadNotifications() {
  loading.value = true
  try {
    const params: any = { page: page.value, size: pageSize }
    if (tab.value === 'unread') params.isRead = false
    const res = await getNotifications(params)
    if (res.code === 200) {
      if (page.value === 1) {
        list.value = res.data.records
      } else {
        list.value = [...list.value, ...res.data.records]
      }
      total.value = res.data.total
    }
  } catch {
    msg.error('加载失败')
  } finally {
    loading.value = false
  }
}

async function loadAnnouncements() {
  try {
    const res = await fetch('/api/portal/user/announcements', { headers: { Authorization: 'Bearer ' + localStorage.getItem('accessToken') } })
    const data = await res.json()
    if (data.code === 200) {
      announcements.value = data.data
    }
  } catch {
    // ignore
  }
}

async function loadUnreadCount() {
  const res = await getUnreadCount()
  if (res.code === 200) unreadCount.value = res.data
}

const hasMore = computed(() => list.value.length < total.value)

async function loadData() {
  page.value = 1
  if (tab.value === 'announcement') {
    await loadAnnouncements()
  } else {
    await loadNotifications()
  }
}

async function loadMore() {
  page.value++
  const params: any = { page: page.value, size: pageSize }
  if (tab.value === 'unread') params.isRead = false
  const res = await getNotifications(params)
  if (res.code === 200) {
    list.value = [...list.value, ...res.data.records]
  }
}

async function markRead(item: MsgNotification) {
  if (item.isRead) return
  await markNotificationRead(item.id)
  item.isRead = 1
  unreadCount.value = Math.max(0, unreadCount.value - 1)
}

async function markAllRead() {
  await markAllNotificationsRead()
  list.value.forEach((item) => { item.isRead = 1 })
  unreadCount.value = 0
  msg.success('已全部标记已读')
}

onMounted(() => {
  loadData()
  loadUnreadCount()
})
</script>

<script lang="ts">
import { computed } from 'vue'
export default {}
</script>

<style scoped>
.notification-page {
  max-width: 700px;
  margin: 0 auto;
  padding: 24px 0;
}
.notification-item {
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background 0.2s;
}
.notification-item:hover {
  background: #f8f9fa;
}
.notification-item.unread {
  background: #f0f9ff;
}
.noti-header {
  display: flex;
  align-items: center;
  gap: 8px;
}
.noti-title {
  flex: 1;
  font-size: 14px;
  font-weight: 500;
}
.noti-time {
  font-size: 12px;
  color: #999;
  white-space: nowrap;
}
.noti-body {
  margin-top: 6px;
  font-size: 13px;
  color: #666;
  line-height: 1.5;
}
</style>
