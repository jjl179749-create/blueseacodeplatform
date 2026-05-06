import request from '@/utils/request'
import type { PageResult } from './user'

/** 私信消息 */
export interface MsgPrivateMessage {
  id: number
  fromUserId: number
  toUserId: number
  content: string
  isRead: number
  createTime: string
  fromNickname?: string
  fromAvatar?: string
}

/** 发送私信请求 */
export interface SendMessageRequest {
  toUserId: number
  content: string
}

/** 发送私信 */
export function sendMessage(data: SendMessageRequest) {
  return request.post<any, { code: number; message: string; data: MsgPrivateMessage }>(
    '/api/portal/messages',
    data,
  )
}

/** 获取与某人的会话记录 */
export function getConversation(userId: number, params: { page?: number; size?: number }) {
  return request.get<any, { code: number; message: string; data: PageResult<MsgPrivateMessage> }>(
    `/api/portal/messages/conversation/${userId}`,
    { params },
  )
}

/** 获取最近联系人ID列表 */
export function getContacts() {
  return request.get<any, { code: number; message: string; data: number[] }>(
    '/api/portal/messages/contacts',
  )
}

/** 获取未读私信数 */
export function getUnreadMessageCount() {
  return request.get<any, { code: number; message: string; data: number }>(
    '/api/portal/messages/unread-count',
  )
}

/** 标记单条消息已读 */
export function markMessageRead(id: number) {
  return request.put<any, { code: number; message: string; data: null }>(
    `/api/portal/messages/${id}/read`,
  )
}

/** 标记与某人的会话全部已读 */
export function markConversationRead(userId: number) {
  return request.put<any, { code: number; message: string; data: null }>(
    `/api/portal/messages/read-conversation/${userId}`,
  )
}

// ===== 关注 =====

/** 关注用户 */
export function followUser(userId: number) {
  return request.post<any, { code: number; message: string; data: null }>(
    `/api/portal/follows/${userId}`,
  )
}

/** 取消关注 */
export function unfollowUser(userId: number) {
  return request.delete<any, { code: number; message: string; data: null }>(
    `/api/portal/follows/${userId}`,
  )
}

/** 获取粉丝数 */
export function getFollowerCount(userId: number) {
  return request.get<any, { code: number; message: string; data: number }>(
    `/api/portal/follows/${userId}/followers/count`,
  )
}

/** 获取关注数 */
export function getFolloweeCount(userId: number) {
  return request.get<any, { code: number; message: string; data: number }>(
    `/api/portal/follows/${userId}/followees/count`,
  )
}

/** 判断是否已关注 */
export function isFollowing(userId: number) {
  return request.get<any, { code: number; message: string; data: boolean }>(
    `/api/portal/follows/check/${userId}`,
  )
}

/** 获取当前用户关注的用户列表 */
export function getFollowees() {
  return request.get<any, { code: number; message: string; data: { id: number; nickname: string; avatar: string }[] }>(
    '/api/portal/follows/followees',
  )
}

/** 获取当前用户的粉丝列表 */
export function getFollowers() {
  return request.get<any, { code: number; message: string; data: { id: number; nickname: string; avatar: string }[] }>(
    '/api/portal/follows/followers',
  )
}

/** 获取指定用户的关注和粉丝数量 */
export function getFollowCount(userId: number) {
  return request.get<any, { code: number; message: string; data: { followeeCount: number; followerCount: number } }>(
    `/api/portal/follows/count/${userId}`,
  )
}
