import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import {
  login as loginApi,
  register as registerApi,
  logout as logoutApi,
  type LoginRequest,
  type RegisterRequest,
} from '@/api/auth'

/** 解析 JWT 的 payload 部分（base64 解码） */
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

/** 初始化时检查 token 是否过期，过期则清除 */
function clearExpiredToken(): string {
  const token = localStorage.getItem('accessToken') || ''
  if (token && isJwtExpired(token)) {
    localStorage.removeItem('accessToken')
    localStorage.removeItem('refreshToken')
    localStorage.removeItem('userInfo')
    return ''
  }
  return token
}

/** 从 localStorage 恢复用户信息 */
function restoreUserInfo(): { userId: number; username: string; roles: string[] } | null {
  try {
    const json = localStorage.getItem('userInfo')
    return json ? JSON.parse(json) : null
  } catch {
    return null
  }
}

export const useAuthStore = defineStore('auth', () => {
  const accessToken = ref(clearExpiredToken())
  const refreshToken = ref(localStorage.getItem('refreshToken') || '')
  // 如果 accessToken 被清空，同步清空 refreshToken
  if (!accessToken.value) {
    refreshToken.value = ''
    localStorage.removeItem('refreshToken')
  }
  const userInfo = ref<{
    userId: number
    username: string
    roles: string[]
  } | null>(restoreUserInfo())

  const isLoggedIn = computed(() => !!accessToken.value)

  async function login(loginReq: LoginRequest) {
    const res = await loginApi(loginReq)
    if (res.code === 200) {
      const data = res.data
      accessToken.value = data.accessToken
      refreshToken.value = data.refreshToken
      userInfo.value = {
        userId: data.userId,
        username: data.username,
        roles: data.roles,
      }
      localStorage.setItem('accessToken', data.accessToken)
      localStorage.setItem('refreshToken', data.refreshToken)
      localStorage.setItem('userInfo', JSON.stringify({
        userId: data.userId,
        username: data.username,
        roles: data.roles,
      }))
    }
    return res
  }

  async function register(registerReq: RegisterRequest) {
    const res = await registerApi(registerReq)
    return res
  }

  function logout() {
    try {
      logoutApi()
    } catch {
      // ignore
    }
    accessToken.value = ''
    refreshToken.value = ''
    userInfo.value = null
    localStorage.removeItem('accessToken')
    localStorage.removeItem('refreshToken')
    localStorage.removeItem('userInfo')
  }

  return {
    accessToken,
    refreshToken,
    userInfo,
    isLoggedIn,
    login,
    register,
    logout,
  }
})
