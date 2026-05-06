import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      component: () => import('@/components/AppLayout.vue'),
      redirect: '/home',
      children: [
        {
          path: 'home',
          name: 'Home',
          meta: { requiresAuth: true },
          component: () => import('@/views/home/Index.vue'),
        },
        {
          path: 'profile',
          name: 'Profile',
          meta: { requiresAuth: true },
          component: () => import('@/views/profile/Index.vue'),
        },
        {
          path: 'resources',
          name: 'Resources',
          meta: { requiresAuth: true },
          component: () => import('@/views/resource/Index.vue'),
        },
        {
          path: 'resources/create',
          name: 'ResourceCreate',
          meta: { requiresAuth: true },
          component: () => import('@/views/resource/Create.vue'),
        },
        {
          path: 'resources/:id',
          name: 'ResourceDetail',
          meta: { requiresAuth: true },
          component: () => import('@/views/resource/Detail.vue'),
        },
        {
          path: 'articles',
          name: 'Articles',
          meta: { requiresAuth: true },
          component: () => import('@/views/article/Index.vue'),
        },
        {
          path: 'articles/create',
          name: 'ArticleCreate',
          meta: { requiresAuth: true },
          component: () => import('@/views/article/Create.vue'),
        },
        {
          path: 'articles/:id',
          name: 'ArticleDetail',
          meta: { requiresAuth: true },
          component: () => import('@/views/article/Detail.vue'),
        },
        {
          path: 'articles/:id/edit',
          name: 'ArticleEdit',
          meta: { requiresAuth: true },
          component: () => import('@/views/article/Create.vue'),
        },
        // ===== 需求发布模块 =====
        {
          path: 'demands',
          name: 'Demands',
          meta: { requiresAuth: true },
          component: () => import('@/views/demand/Index.vue'),
        },
        {
          path: 'demands/create',
          name: 'DemandCreate',
          meta: { requiresAuth: true },
          component: () => import('@/views/demand/Create.vue'),
        },
        {
          path: 'demands/:id',
          name: 'DemandDetail',
          meta: { requiresAuth: true },
          component: () => import('@/views/demand/Detail.vue'),
        },
        {
          path: 'demands/:id/edit',
          name: 'DemandEdit',
          meta: { requiresAuth: true },
          component: () => import('@/views/demand/Create.vue'),
        },
        {
          path: 'chat',
          name: 'Chat',
          meta: { requiresAuth: true },
          component: () => import('@/views/chat/Index.vue'),
        },
        {
          path: 'chat/tickets',
          name: 'ChatTickets',
          meta: { requiresAuth: true },
          component: () => import('@/views/chat/Tickets.vue'),
        },
        {
          path: 'messages',
          name: 'Inbox',
          meta: { requiresAuth: true },
          component: () => import('@/views/chat/Inbox.vue'),
        },
        {
          path: 'notifications',
          name: 'Notifications',
          meta: { requiresAuth: true },
          component: () => import('@/views/chat/NotificationCenter.vue'),
        },
        {
          path: 'user/:id',
          name: 'UserProfile',
          meta: { requiresAuth: true },
          component: () => import('@/views/user/Index.vue'),
        },
        {
          path: 'login',
          name: 'Login',
          meta: { guest: true },
          component: () => import('@/views/auth/Login.vue'),
        },
        {
          path: 'register',
          name: 'Register',
          meta: { guest: true },
          component: () => import('@/views/auth/Register.vue'),
        },
      ],
    },
    // ===== 管理后台路由 =====
    {
      path: '/admin',
      component: () => import('@/components/AdminLayout.vue'),
      redirect: '/admin/dashboard',
      meta: { requiresAuth: true, requiresAdmin: true },
      children: [
        {
          path: 'dashboard',
          name: 'AdminDashboard',
          component: () => import('@/views/admin/Dashboard.vue'),
        },
        {
          path: 'users',
          name: 'AdminUsers',
          component: () => import('@/views/admin/UserManagement.vue'),
        },
        {
          path: 'audit',
          name: 'AdminAudit',
          component: () => import('@/views/admin/Audit.vue'),
        },
        {
          path: 'resources',
          name: 'AdminResources',
          component: () => import('@/views/admin/ResourceManagement.vue'),
        },
        {
          path: 'demands',
          name: 'AdminDemands',
          component: () => import('@/views/admin/DemandManagement.vue'),
        },
        {
          path: 'tickets',
          name: 'AdminTickets',
          component: () => import('@/views/admin/TicketManagement.vue'),
        },
        {
          path: 'faq',
          name: 'AdminFaq',
          component: () => import('@/views/admin/FaqManagement.vue'),
        },
        {
          path: 'knowledge',
          name: 'AdminKnowledge',
          component: () => import('@/views/admin/KnowledgeManagement.vue'),
        },
        {
          path: 'system',
          name: 'AdminSystem',
          component: () => import('@/views/admin/SystemConfig.vue'),
        },
        {
          path: 'announcements',
          name: 'AdminAnnouncements',
          component: () => import('@/views/admin/Announcement.vue'),
        },
        {
          path: 'banners',
          name: 'AdminBanners',
          component: () => import('@/views/admin/Banner.vue'),
        },
        {
          path: 'roles',
          name: 'AdminRoles',
          component: () => import('@/views/admin/RoleManagement.vue'),
        },
        {
          path: 'logs',
          name: 'AdminLogs',
          component: () => import('@/views/admin/LogManagement.vue'),
        },
      ],
    },
  ],
})

/** 解析 JWT 的 payload 部分 */
function parseJwtPayload(token: string): Record<string, unknown> | null {
  try {
    const payload = token.split('.')[1]
    return JSON.parse(atob(payload))
  } catch {
    return null
  }
}

/** 判断 JWT 是否已过期 */
function isJwtExpired(token: string): boolean {
  const payload = parseJwtPayload(token)
  if (!payload || typeof payload.exp !== 'number') return true
  return Date.now() >= payload.exp * 1000
}

router.beforeEach((to) => {
  const authStore = useAuthStore()

  // 如果本地有 token 但已过期，自动清除并视为未登录
  if (authStore.accessToken && isJwtExpired(authStore.accessToken)) {
    authStore.logout()
  }

  // 已登录用户访问登录/注册页，跳转首页
  if (to.meta?.guest && authStore.isLoggedIn) {
    return '/home'
  }
  // 未登录用户访问需要认证的页面，跳转登录
  if (to.meta?.requiresAuth && !authStore.isLoggedIn) {
    return '/login'
  }
  // 管理后台需要 ADMIN 角色
  if (to.meta?.requiresAdmin) {
    const roles = authStore.userInfo?.roles || []
    if (!roles.includes('ROLE_ADMIN') && !roles.includes('ROLE_REVIEWER') && !roles.includes('ROLE_OPERATOR')) {
      return '/home'
    }
  }
})

export default router
