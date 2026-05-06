<template>
  <n-message-provider>
    <n-dialog-provider>
      <div class="app-layout">
      <n-layout>
        <n-layout-header bordered>
          <div class="header-content">
            <div class="logo">
              <router-link to="/home">蓝海编程平台</router-link>
            </div>
            <div class="nav">
              <router-link to="/home">首页</router-link>
              <router-link to="/resources">资源分享</router-link>
              <router-link to="/articles">编程提升</router-link>
              <router-link to="/demands">需求发布</router-link>
              <router-link to="/chat">AI客服</router-link>
              <router-link to="/messages">私信</router-link>
              <router-link v-if="authStore.userInfo?.roles?.includes('ROLE_ADMIN')" to="/admin/dashboard" style="color: #f5222d">后台管理</router-link>
            </div>
            <div class="user-area">
              <template v-if="authStore.isLoggedIn">
                <n-badge :value="notifyCount" :max="99" style="margin-right:12px">
                  <n-button quaternary circle size="small" @click="goToNotifications">
                    <template #icon>
                      <n-icon><svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><path fill="currentColor" d="M12 22c1.1 0 2-.9 2-2h-4c0 1.1.89 2 2 2zm6-6v-5c0-3.07-1.64-5.64-4.5-6.32V4c0-.83-.67-1.5-1.5-1.5s-1.5.67-1.5 1.5v.68C7.63 5.36 6 7.92 6 11v5l-2 2v1h16v-1l-2-2z"/></svg></n-icon>
                    </template>
                  </n-button>
                </n-badge>
                <n-dropdown trigger="click" :options="userMenuOptions" @select="handleMenuSelect">
                  <n-button quaternary>
                    {{ authStore.userInfo?.username || '用户' }}
                  </n-button>
                </n-dropdown>
              </template>
              <template v-else>
                <router-link to="/login">
                  <n-button type="primary" ghost>登录</n-button>
                </router-link>
                <router-link to="/register">
                  <n-button type="primary" style="margin-left: 8px">注册</n-button>
                </router-link>
              </template>
            </div>
          </div>
        </n-layout-header>
        <n-layout-content class="main-content">
          <router-view />
        </n-layout-content>
      </n-layout>
      </div>
    </n-dialog-provider>
  </n-message-provider>
</template>

<script setup lang="ts">
import { useAuthStore } from '@/stores/auth'
import { ref, onMounted, onUnmounted } from 'vue'
import { getUnreadCount } from '@/api/user'
import { NBadge, NButton, NIcon } from 'naive-ui'

const authStore = useAuthStore()
const notifyCount = ref(0)
let timer: ReturnType<typeof setInterval> | null = null

async function fetchNotifyCount() {
  const res = await getUnreadCount()
  if (res.code === 200) {
    notifyCount.value = res.data
  }
}

function goToNotifications() {
  window.location.href = '/notifications'
}

onMounted(() => {
  fetchNotifyCount()
  timer = setInterval(fetchNotifyCount, 30000) // 每30秒刷新
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})

const userMenuOptions = [
  { label: '个人中心', key: 'profile' },
  { label: '我的消息', key: 'messages' },
  { label: '通知中心', key: 'notifications' },
  { type: 'divider' as const, key: 'd1' },
  { label: '退出登录', key: 'logout' },
]

function handleMenuSelect(key: string) {
  if (key === 'profile') {
    window.location.href = '/profile'
  } else if (key === 'messages') {
    window.location.href = '/messages'
  } else if (key === 'notifications') {
    window.location.href = '/notifications'
  } else if (key === 'logout') {
    authStore.logout()
    window.location.href = '/login'
  }
}
</script>

<style scoped>
.header-content {
  display: flex;
  align-items: center;
  padding: 0 24px;
  height: 64px;
  max-width: 1200px;
  margin: 0 auto;
}
.logo {
  font-size: 20px;
  font-weight: bold;
  margin-right: 40px;
}
.logo a {
  color: inherit;
  text-decoration: none;
}
.nav {
  flex: 1;
  display: flex;
  gap: 24px;
}
.nav a {
  color: #666;
  text-decoration: none;
  font-size: 15px;
  transition: color 0.2s;
}
.nav a:hover {
  color: #18a058;
}
.user-area {
  display: flex;
  align-items: center;
}
.main-content {
  min-height: calc(100vh - 64px);
  padding: 24px;
  background: #f5f7fa;
}
</style>
