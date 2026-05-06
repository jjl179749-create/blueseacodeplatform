<template>
  <n-message-provider>
    <n-dialog-provider>
      <div class="admin-layout">
        <n-layout position="absolute" has-sider>
          <!-- 侧边栏 -->
          <n-layout-sider
            bordered
            collapse-mode="width"
            :collapsed-width="64"
            :width="220"
            :collapsed="collapsed"
            show-trigger="bar"
            @collapse="collapsed = true"
            @expand="collapsed = false"
            :native-scrollbar="false"
            class="admin-sider"
          >
            <div class="admin-logo">
              <router-link to="/admin/dashboard">
                <span v-if="!collapsed">蓝海管理后台</span>
                <span v-else>后台</span>
              </router-link>
            </div>
            <n-menu
              :collapsed="collapsed"
              :collapsed-width="64"
              :collapsed-icon-size="22"
              :options="menuOptions"
              :value="activeMenu"
              @update:value="handleMenuSelect"
            />
          </n-layout-sider>

          <!-- 主内容 -->
          <n-layout>
            <!-- 顶部栏 -->
            <n-layout-header bordered class="admin-header">
              <div class="header-left">
                <n-button quaternary @click="goBackToPortal">
                  <template #icon><n-icon><svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><path fill="currentColor" d="M20 11H7.83l5.59-5.59L12 4l-8 8 8 8 1.41-1.41L7.83 13H20v-2z"/></svg></n-icon></template>
                  返回前台
                </n-button>
              </div>
              <div class="header-right">
                <n-dropdown trigger="click" :options="adminUserMenu" @select="handleUserMenu">
                  <n-button quaternary>
                    {{ authStore.userInfo?.username || '管理员' }}
                    <template #icon>
                      <n-icon><svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><path fill="currentColor" d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/></svg></n-icon>
                    </template>
                  </n-button>
                </n-dropdown>
              </div>
            </n-layout-header>

            <!-- 内容区域 -->
            <n-layout-content class="admin-content" :native-scrollbar="false">
              <router-view />
            </n-layout-content>
          </n-layout>
        </n-layout>
      </div>
    </n-dialog-provider>
  </n-message-provider>
</template>

<script setup lang="ts">
import { ref, computed, h } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { NIcon } from 'naive-ui'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const collapsed = ref(false)

function renderIcon(icon: string) {
  return () =>
    h(NIcon, null, {
      default: () => h('svg', { xmlns: 'http://www.w3.org/2000/svg', viewBox: '0 0 24 24' }, [
        h('path', { fill: 'currentColor', d: icon }),
      ]),
    })
}

// 菜单图标 SVG path
const icons = {
  dashboard: 'M3 13h8V3H3v10zm0 8h8v-6H3v6zm10 0h8V11h-8v10zm0-18v6h8V3h-8z',
  users: 'M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z',
  audit: 'M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41L9 16.17z',
  resource: 'M19.35 10.04C18.67 6.59 15.64 4 12 4 9.11 4 6.6 5.64 5.35 8.04 2.34 8.36 0 10.91 0 14c0 3.31 2.69 6 6 6h13c2.76 0 5-2.24 5-5 0-2.64-2.05-4.78-4.65-4.96z',
  demand: 'M20 2H4c-1.1 0-2 .9-2 2v18l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zm0 14H5.17L4 17.17V4h16v12z',
  cs: 'M20 2H4c-1.1 0-1.99.9-1.99 2L2 22l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zm0 14H5.17L4 17.17V4h16v12zM7 9h2v2H7V9zm8 0h2v2h-2V9zm-4 0h2v2h-2V9z',
  settings: 'M19.14 12.94c.04-.3.06-.61.06-.94 0-.32-.02-.64-.07-.94l2.03-1.58c.18-.14.23-.41.12-.61l-1.92-3.32c-.12-.22-.37-.29-.59-.22l-2.39.96c-.5-.38-1.03-.7-1.62-.94l-.36-2.54c-.04-.24-.24-.41-.48-.41h-3.84c-.24 0-.43.17-.47.41l-.36 2.54c-.59.24-1.13.57-1.62.94l-2.39-.96c-.22-.08-.47 0-.59.22L2.74 8.87c-.12.21-.08.47.12.61l2.03 1.58c-.05.3-.07.62-.07.94s.02.64.07.94l-2.03 1.58c-.18.14-.23.41-.12.61l1.92 3.32c.12.22.37.29.59.22l2.39-.96c.5.38 1.03.7 1.62.94l.36 2.54c.05.24.24.41.48.41h3.84c.24 0 .44-.17.47-.41l.36-2.54c.59-.24 1.13-.56 1.62-.94l2.39.96c.22.08.47 0 .59-.22l1.92-3.32c.12-.22.07-.47-.12-.61l-2.01-1.58zM12 15.6c-1.98 0-3.6-1.62-3.6-3.6s1.62-3.6 3.6-3.6 3.6 1.62 3.6 3.6-1.62 3.6-3.6 3.6z',
  announcement: 'M12 8l-6 6h12l-6-6zM3 12c0 4.97 4.03 9 9 9s9-4.03 9-9-4.03-9-9-9-9 4.03-9 9z',
  banner: 'M21 19V5c0-1.1-.9-2-2-2H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2zM8.5 13.5l2.5 3.01L14.5 12l4.5 6H5l3.5-4.5z',
  perm: 'M18 8h-1V6c0-2.76-2.24-5-5-5S7 3.24 7 6v2H6c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V10c0-1.1-.9-2-2-2zm-6 9c-1.1 0-2-.9-2-2s.9-2 2-2 2 .9 2 2-.9 2-2 2zm3.1-9H8.9V6c0-1.71 1.39-3.1 3.1-3.1s3.1 1.39 3.1 3.1v2z',
  logs: 'M4 6h16v2H4V6zm0 5h16v2H4v-2zm0 5h16v2H4v-2zM2 2v20h20V2H2zm18 18H4V4h16v16z',
}

const menuOptions = [
  { label: '仪表盘', key: '/admin/dashboard', icon: renderIcon(icons.dashboard) },
  { label: '用户管理', key: '/admin/users', icon: renderIcon(icons.users) },
  { label: '内容审核', key: '/admin/audit', icon: renderIcon(icons.audit) },
  { label: '资源管理', key: '/admin/resources', icon: renderIcon(icons.resource) },
  { label: '需求管理', key: '/admin/demands', icon: renderIcon(icons.demand) },
  { label: '工单管理', key: '/admin/tickets', icon: renderIcon(icons.cs) },
  { label: '系统配置', key: '/admin/system', icon: renderIcon(icons.settings) },
  {
    label: '内容管理',
    key: '/admin/content',
    icon: renderIcon(icons.announcement),
    children: [
      { label: '公告管理', key: '/admin/announcements' },
      { label: '轮播图管理', key: '/admin/banners' },
    ],
  },
  { label: '权限管理', key: '/admin/roles', icon: renderIcon(icons.perm) },
  { label: '日志管理', key: '/admin/logs', icon: renderIcon(icons.logs) },
  { label: 'FAQ管理', key: '/admin/faq', icon: renderIcon(icons.cs) },
  { label: '知识库管理', key: '/admin/knowledge', icon: renderIcon(icons.cs) },
]

const activeMenu = computed(() => {
  const path = route.path
  // 匹配子路径
  const matched = menuOptions.find(
    (item) => item.key === path || (item.children && item.children.some((c: any) => path.startsWith(c.key))),
  )
  if (matched) return matched.key
  // 检查子菜单
  for (const item of menuOptions) {
    if (item.children) {
      const child = item.children.find((c: any) => path.startsWith(c.key))
      if (child) return child.key
    }
  }
  return '/admin/dashboard'
})

const adminUserMenu = [
  { label: '返回前台', key: 'back' },
  { type: 'divider' as const, key: 'd1' },
  { label: '退出登录', key: 'logout' },
]

function handleMenuSelect(key: string) {
  router.push(key)
}

function handleUserMenu(key: string) {
  if (key === 'back') {
    router.push('/home')
  } else if (key === 'logout') {
    authStore.logout()
    router.push('/login')
  }
}

function goBackToPortal() {
  router.push('/home')
}
</script>

<style scoped>
.admin-layout {
  height: 100vh;
}
.admin-sider {
  background: #001529;
}
.admin-sider :deep(.n-menu) {
  background: #001529;
}
.admin-sider :deep(.n-menu-item) {
  color: rgba(255, 255, 255, 0.65);
}
.admin-sider :deep(.n-menu-item:hover) {
  color: #fff;
}
.admin-sider :deep(.n-menu-item--selected) {
  color: #fff;
  background: #1890ff;
}
.admin-logo {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: bold;
  color: #fff;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}
.admin-logo a {
  color: #fff;
  text-decoration: none;
}
.admin-header {
  height: 64px;
  display: flex;
  align-items: center;
  padding: 0 24px;
  background: #fff;
  justify-content: space-between;
}
.admin-content {
  padding: 24px;
  background: #f0f2f5;
  min-height: calc(100vh - 64px);
}
.header-left,
.header-right {
  display: flex;
  align-items: center;
}
</style>
