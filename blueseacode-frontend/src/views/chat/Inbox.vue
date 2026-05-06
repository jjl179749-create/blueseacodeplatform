<template>
  <div class="inbox">
    <div class="inbox-container">
      <!-- 左侧联系人列表 -->
      <div class="contacts-panel">
        <div class="panel-header">
          <div class="panel-tabs">
            <span
              class="panel-tab"
              :class="{ active: activeTab === 'messages' }"
              @click="activeTab = 'messages'"
            >消息</span>
            <span
              class="panel-tab"
              :class="{ active: activeTab === 'follows' }"
              @click="switchToFollows"
            >关注</span>
            <span
              class="panel-tab"
              :class="{ active: activeTab === 'followers' }"
              @click="switchToFollowers"
            >粉丝</span>
          </div>
          <n-badge :value="unreadTotal" :max="99">
            <n-button quaternary size="small" @click="loadContacts">
              <template #icon><n-icon><svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><path fill="currentColor" d="M17.65 6.35A7.96 7.96 0 0 0 12 4c-4.42 0-7.99 3.58-7.99 8s3.57 8 7.99 8c3.73 0 6.84-2.55 7.73-6h-2.08A5.99 5.99 0 0 1 12 18c-3.31 0-6-2.69-6-6s2.69-6 6-6c1.66 0 3.14.69 4.22 1.78L13 11h7V4l-2.35 2.35z"/></svg></n-icon></template>
            </n-button>
          </n-badge>
        </div>
        <n-input v-model:value="searchText" placeholder="搜索联系人..." size="small" style="margin: 8px" />
        <!-- 消息列表 -->
        <template v-if="activeTab === 'messages'">
          <div class="contact-list" v-if="contacts.length > 0">
            <div
              v-for="c in contacts"
              :key="c.userId"
              class="contact-item"
              :class="{ active: activeUserId === c.userId }"
              @click="selectContact(c)"
            >
              <n-avatar
                :src="c.avatar || undefined"
                size="small"
                round
                fallback-src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24'%3E%3Cpath fill='%23ccc' d='M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z'/%3E%3C/svg%3E"
              />
              <div class="contact-info">
                <div class="contact-name">{{ c.nickname || `用户${c.userId}` }}</div>
                <div class="contact-preview" v-if="c.lastMessage">{{ c.lastMessage }}</div>
              </div>
              <div class="contact-meta">
                <n-badge v-if="c.unread > 0" :value="c.unread" :max="99" type="error" />
              </div>
            </div>
          </div>
          <div class="empty-contacts" v-else>
            <n-empty description="暂无消息" />
          </div>
        </template>
        <!-- 关注列表 -->
        <template v-if="activeTab === 'follows'">
          <div class="contact-list" v-if="followees.length > 0">
            <div
              v-for="u in followees"
              :key="u.id"
              class="contact-item"
              :class="{ active: activeUserId === u.id }"
              @click="selectFollowee(u)"
            >
              <n-avatar
                :src="u.avatar || undefined"
                size="small"
                round
                fallback-src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24'%3E%3Cpath fill='%23ccc' d='M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z'/%3E%3C/svg%3E"
              />
              <div class="contact-info">
                <div class="contact-name">{{ u.nickname || `用户${u.id}` }}</div>
              </div>
            </div>
          </div>
          <div class="empty-contacts" v-else>
            <n-empty v-if="followeesLoading" description="加载中..." />
            <n-empty v-else description="还没有关注的人" />
          </div>
        </template>
        <!-- 粉丝列表 -->
        <template v-if="activeTab === 'followers'">
          <div class="contact-list" v-if="followersList.length > 0">
            <div
              v-for="u in followersList"
              :key="u.id"
              class="contact-item"
              :class="{ active: activeUserId === u.id }"
              @click="selectFollowee(u)"
            >
              <n-avatar
                :src="u.avatar || undefined"
                size="small"
                round
                fallback-src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24'%3E%3Cpath fill='%23ccc' d='M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z'/%3E%3C/svg%3E"
              />
              <div class="contact-info">
                <div class="contact-name">{{ u.nickname || `用户${u.id}` }}</div>
              </div>
            </div>
          </div>
          <div class="empty-contacts" v-else>
            <n-empty v-if="followersLoading" description="加载中..." />
            <n-empty v-else description="还没有粉丝" />
          </div>
        </template>
      </div>

      <!-- 右侧聊天区域 -->
      <div class="chat-panel">
        <template v-if="activeUserId">
          <div class="chat-header">
            <span class="chat-user-name">{{ activeUserNickname }}</span>
          </div>
          <div class="messages-area" ref="messagesRef">
            <div
              v-for="msg in messages"
              :key="msg.id"
              class="message-item"
              :class="{ mine: msg.fromUserId === myUserId, unread: !msg.isRead && msg.fromUserId !== myUserId }"
            >
              <n-avatar
                v-if="msg.fromUserId !== myUserId"
                :src="msg.fromAvatar || undefined"
                size="small"
                round
                fallback-src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24'%3E%3Cpath fill='%23ccc' d='M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z'/%3E%3C/svg%3E"
              />
              <div class="message-content">
                <div class="message-bubble">{{ msg.content }}</div>
                <div class="message-time">{{ formatTime(msg.createTime) }}</div>
              </div>
            </div>
            <div v-if="messages.length === 0" class="no-messages">
              <n-empty description="暂无消息，发送第一条消息吧" />
            </div>
          </div>
          <div class="chat-input-area">
            <n-input
              v-model:value="inputText"
              type="textarea"
              :rows="2"
              placeholder="输入消息..."
              :autosize="{ minRows: 2, maxRows: 4 }"
              @keydown.enter.prevent="handleSend"
            />
            <n-button type="primary" @click="handleSend" :disabled="!inputText.trim()" style="margin-top: 8px; align-self: flex-end">
              发送
            </n-button>
          </div>
        </template>
        <div class="no-chat" v-else>
          <n-empty description="选择一个联系人开始聊天" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import { useMessage, NButton, NIcon, NBadge, NEmpty, NInput, NAvatar } from 'naive-ui'
import {
  sendMessage,
  getConversation,
  getContacts,
  getUnreadMessageCount,
  markConversationRead,
  getFollowees,
  getFollowers,
} from '@/api/message'
import { getProfile, getUserProfile, batchGetUsers } from '@/api/user'
import type { MsgPrivateMessage } from '@/api/message'

interface Contact {
  userId: number
  nickname: string
  avatar: string | null
  unread: number
  lastMessage: string | null
}

const msg = useMessage()
const route = useRoute()
const messagesRef = ref<HTMLElement | null>(null)
const myUserId = ref(0)
const activeUserId = ref<number | null>(null)
const activeUserNickname = ref('')
const contacts = ref<Contact[]>([])
const messages = ref<MsgPrivateMessage[]>([])
const inputText = ref('')
const searchText = ref('')
const unreadTotal = ref(0)

// 关注列表
const activeTab = ref<'messages' | 'follows' | 'followers'>('messages')
const followees = ref<{ id: number; nickname: string; avatar: string }[]>([])
const followeesLoading = ref(false)
// 粉丝列表
const followersList = ref<{ id: number; nickname: string; avatar: string }[]>([])
const followersLoading = ref(false)

async function loadMyInfo() {
  const res = await getProfile()
  if (res.code === 200) {
    myUserId.value = res.data.id
  }
}

async function loadContacts() {
  try {
    const [contactsRes, unreadRes] = await Promise.all([
      getContacts(),
      getUnreadMessageCount(),
    ])
    unreadTotal.value = unreadRes.code === 200 ? unreadRes.data : 0

    if (contactsRes.code === 200 && contactsRes.data.length > 0) {
      const userIds = contactsRes.data
      // 批量获取用户昵称和头像
      const batchRes = await batchGetUsers(userIds)
      const userMap = batchRes.code === 200 ? batchRes.data : {}
      contacts.value = userIds.map((id) => ({
        userId: id,
        nickname: userMap[id]?.nickname || `用户${id}`,
        avatar: userMap[id]?.avatar || null,
        unread: 0,
        lastMessage: null,
      }))
    } else {
      contacts.value = []
    }
  } catch {
    // ignore
  }
}

async function loadFollowees() {
  followeesLoading.value = true
  try {
    const res = await getFollowees()
    if (res.code === 200) {
      followees.value = res.data
    }
  } catch {
    // ignore
  } finally {
    followeesLoading.value = false
  }
}

async function loadFollowers() {
  followersLoading.value = true
  try {
    const res = await getFollowers()
    if (res.code === 200) {
      followersList.value = res.data
    }
  } catch {
    // ignore
  } finally {
    followersLoading.value = false
  }
}

async function switchToFollows() {
  activeTab.value = 'follows'
  if (followees.value.length === 0 && !followeesLoading.value) {
    await loadFollowees()
  }
}

async function switchToFollowers() {
  activeTab.value = 'followers'
  if (followersList.value.length === 0 && !followersLoading.value) {
    await loadFollowers()
  }
}

async function selectFollowee(u: { id: number; nickname: string; avatar: string }) {
  activeUserId.value = u.id
  activeUserNickname.value = u.nickname || `用户${u.id}`
  await loadMessages()
}

async function selectContact(contact: Contact) {
  activeUserId.value = contact.userId
  activeUserNickname.value = contact.nickname
  await loadMessages()
  // 标记已读
  await markConversationRead(contact.userId)
  markMessagesRead()
}

async function loadMessages() {
  if (!activeUserId.value) return
  try {
    const res = await getConversation(activeUserId.value, { page: 1, size: 50 })
    if (res.code === 200) {
      messages.value = res.data.records.reverse() // 倒序显示
      await nextTick()
      scrollToBottom()
    }
  } catch {
    msg.error('加载消息失败')
  }
}

function markMessagesRead() {
  messages.value.forEach((m) => {
    if (m.fromUserId !== myUserId.value) m.isRead = 1
  })
}

async function handleSend() {
  const text = inputText.value.trim()
  if (!text || !activeUserId.value) return
  inputText.value = ''
  try {
    const res = await sendMessage({ toUserId: activeUserId.value, content: text })
    if (res.code === 200) {
      messages.value.push(res.data)
      await nextTick()
      scrollToBottom()
    }
  } catch {
    msg.error('发送失败')
  }
}

function scrollToBottom() {
  if (messagesRef.value) {
    messagesRef.value.scrollTop = messagesRef.value.scrollHeight
  }
}

function formatTime(time: string) {
  if (!time) return ''
  const d = new Date(time)
  const now = new Date()
  const isToday = d.toDateString() === now.toDateString()
  if (isToday) return d.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  return d.toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
}

onMounted(() => {
  loadMyInfo()
  loadContacts()
  // 如果 URL 有 tab 参数，切换到对应标签
  const tabParam = route.query.tab as string
  if (tabParam === 'follows') {
    switchToFollows()
  } else if (tabParam === 'followers') {
    switchToFollowers()
  }
})

// 如果从外部传入 userId 参数，自动选中对应联系人
watch(
  () => route.query.userId,
  async (userId) => {
    if (userId && myUserId.value > 0) {
      const targetId = Number(userId)
      if (targetId === myUserId.value) return
      // 检查是否已在联系人列表中
      const existing = contacts.value.find((c) => c.userId === targetId)
      if (existing) {
        selectContact(existing)
      } else {
        // 获取用户信息并添加到联系人
        try {
          const res = await getUserProfile(targetId)
          if (res.code === 200) {
            const profile = res.data
            const newContact: Contact = {
              userId: targetId,
              nickname: profile.nickname || profile.username,
              avatar: profile.avatar,
              unread: 0,
              lastMessage: null,
            }
            contacts.value.unshift(newContact)
            selectContact(newContact)
          }
        } catch {
          // ignore
        }
      }
      // 清除 query 参数
      window.history.replaceState({}, '', '/messages')
    }
  },
)
</script>

<style scoped>
.inbox {
  height: calc(100vh - 120px);
  padding: 16px;
}

.inbox-container {
  display: flex;
  height: 100%;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  overflow: hidden;
}

.contacts-panel {
  width: 280px;
  border-right: 1px solid #eee;
  display: flex;
  flex-direction: column;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  border-bottom: 1px solid #eee;
}

.panel-tabs {
  display: flex;
  gap: 4px;
}

.panel-tab {
  padding: 4px 12px;
  font-size: 14px;
  cursor: pointer;
  border-radius: 4px;
  color: #666;
  transition: all 0.2s;
  user-select: none;
}

.panel-tab:hover {
  background: #f0f0f0;
}

.panel-tab.active {
  color: #18a058;
  font-weight: 600;
  background: #e8f8ee;
}

.contact-list {
  flex: 1;
  overflow-y: auto;
}

.contact-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  cursor: pointer;
  transition: background 0.2s;
  gap: 10px;
}

.contact-item:hover {
  background: #f5f7fa;
}

.contact-item.active {
  background: #e8f0fe;
}

.contact-info {
  flex: 1;
  min-width: 0;
}

.contact-name {
  font-size: 14px;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.contact-preview {
  font-size: 12px;
  color: #999;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.contact-meta {
  flex-shrink: 0;
}

.empty-contacts {
  padding: 40px 0;
}

.chat-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.chat-header {
  padding: 12px 20px;
  border-bottom: 1px solid #eee;
  font-weight: 600;
  font-size: 15px;
}

.messages-area {
  flex: 1;
  padding: 16px 20px;
  overflow-y: auto;
  background: #f8f9fa;
}

.message-item {
  display: flex;
  gap: 10px;
  margin-bottom: 16px;
  align-items: flex-start;
}

.message-item.mine {
  flex-direction: row-reverse;
}

.message-item.mine .message-bubble {
  background: #e8f4ff;
  border: 1px solid #b3d8f0;
}

.message-item.unread .message-bubble {
  background: #fff3e0;
}

.message-bubble {
  background: #fff;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  padding: 8px 14px;
  font-size: 14px;
  line-height: 1.6;
  max-width: 400px;
  word-break: break-word;
}

.message-time {
  font-size: 11px;
  color: #bbb;
  margin-top: 4px;
}

.message-item.mine .message-time {
  text-align: right;
}

.no-messages,
.no-chat {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
}

.chat-input-area {
  padding: 12px 20px;
  border-top: 1px solid #eee;
  display: flex;
  flex-direction: column;
  background: #fff;
}
</style>
