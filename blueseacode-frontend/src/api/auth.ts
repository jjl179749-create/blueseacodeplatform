import request from '@/utils/request'

export interface LoginRequest {
  username: string
  password: string
}

export interface RegisterRequest {
  username: string
  password: string
  nickname?: string
  email?: string
}

export interface TokenResponse {
  accessToken: string
  refreshToken: string
  tokenType: string
  userId: number
  username: string
  roles: string[]
}

export function login(data: LoginRequest) {
  return request.post<any, { code: number; msg: string; data: TokenResponse }>(
    '/api/portal/auth/login',
    data,
  )
}

export function register(data: RegisterRequest) {
  return request.post<any, { code: number; msg: string; data: null }>(
    '/api/portal/auth/register',
    data,
  )
}

export function refreshToken() {
  const token = localStorage.getItem('refreshToken')
  return request.post<any, { code: number; msg: string; data: TokenResponse }>(
    '/api/portal/auth/refresh',
    null,
    { headers: { 'Refresh-Token': token || '' } },
  )
}

export function logout() {
  return request.post<any, { code: number; msg: string; data: null }>(
    '/api/portal/auth/logout',
  )
}
