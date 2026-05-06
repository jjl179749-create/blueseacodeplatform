import request from '@/utils/request'

export interface UserProfileVO {
  id: number
  username: string
  nickname: string | null
  avatar: string | null
  email: string | null
  phone: string | null
  bio: string | null
  github: string | null
  website: string | null
  score: number
  status: number
  roles: string[]
  createTime: string
  lastLoginTime: string | null
  resourceCount: number
  articleCount: number
  demandCount: number
}

export interface UserProfileUpdateRequest {
  nickname?: string
  bio?: string
  github?: string
  website?: string
  email?: string
  phone?: string
}

export interface PasswordUpdateRequest {
  oldPassword: string
  newPassword: string
}

export interface MsgNotification {
  id: number
  userId: number
  type: string
  title: string
  content: string | null
  relatedId: number | null
  relatedType: string | null
  isRead: number
  createTime: string
}

export interface PageResult<T> {
  records: T[]
  total: number
  page: number
  size: number
}

/** 获取个人信息 */
export function getProfile() {
  return request.get<any, { code: number; message: string; data: UserProfileVO }>(
    '/api/portal/user/profile',
  )
}

/** 获取他人主页信息 */
export function getUserProfile(userId: number) {
  return request.get<any, { code: number; message: string; data: UserProfileVO }>(
    `/api/portal/user/profile/${userId}`,
  )
}

/** 更新个人信息 */
export function updateProfile(data: UserProfileUpdateRequest) {
  return request.put<any, { code: number; message: string; data: null }>(
    '/api/portal/user/profile',
    data,
  )
}

/** 修改密码 */
export function updatePassword(data: PasswordUpdateRequest) {
  return request.put<any, { code: number; message: string; data: null }>(
    '/api/portal/user/password',
    data,
  )
}

/** 更新头像 */
export function updateAvatar(avatarUrl: string) {
  return request.put<any, { code: number; message: string; data: null }>(
    '/api/portal/user/avatar',
    null,
    { params: { avatarUrl } },
  )
}

/** 获取通知列表 */
export function getNotifications(params: {
  page?: number
  size?: number
  isRead?: boolean
}) {
  return request.get<any, { code: number; message: string; data: PageResult<MsgNotification> }>(
    '/api/portal/user/notifications',
    { params },
  )
}

/** 标记单条已读 */
export function markNotificationRead(id: number) {
  return request.put<any, { code: number; message: string; data: null }>(
    `/api/portal/user/notifications/${id}/read`,
  )
}

/** 一键已读 */
export function markAllNotificationsRead() {
  return request.put<any, { code: number; message: string; data: null }>(
    '/api/portal/user/notifications/read-all',
  )
}

/** 获取未读数量 */
export function getUnreadCount() {
  return request.get<any, { code: number; message: string; data: number }>(
    '/api/portal/user/notifications/unread-count',
  )
}

/** 批量获取用户基本信息 */
export function batchGetUsers(userIds: number[]) {
  return request.post<any, { code: number; message: string; data: Record<number, { nickname: string; avatar: string | null }> }>(
    '/api/portal/user/batch',
    userIds,
  )
}

/**
 * 上传文件
 * @param file 文件对象
 * @param directory 存储目录（avatar/resource/article等）
 */
export function uploadFile(file: File, directory = 'common') {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('directory', directory)
  return request.post<any, { code: number; message: string; data: string }>(
    '/api/portal/file/upload',
    formData,
    {
      headers: { 'Content-Type': 'multipart/form-data' },
      timeout: 60000,
    },
  )
}

/**
 * 上传头像
 * @param file 图片文件
 */
export function uploadAvatar(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post<any, { code: number; message: string; data: string }>(
    '/api/portal/file/upload/avatar',
    formData,
    {
      headers: { 'Content-Type': 'multipart/form-data' },
      timeout: 60000,
    },
  )
}
