import request from '@/utils/request'

/** AI 聊天响应 */
export interface ChatResponse {
  type: 'FAQ' | 'KNOWLEDGE' | 'AI' | 'TRANSFER'
  content: string
  suggestions: string[] | null
  canTransfer: boolean
  sessionId: string
}

/** 发送消息请求 */
export interface ChatSendRequest {
  sessionId?: string
  question: string
}

/** 对话记录 */
export interface ChatConversation {
  id: number
  userId: number
  sessionId: string
  question: string
  answer: string
  answerType: string
  createTime: string
}

/** 工单 */
export interface ChatTicket {
  id: number
  userId: number
  title: string
  description: string | null
  category: string
  status: string
  priority: number
  assigneeId: number | null
  closeReason: string | null
  nickname: string | null
  assigneeName: string | null
  replies: ChatTicketReply[]
  createTime: string
  updateTime: string
}

/** 工单回复 */
export interface ChatTicketReply {
  id: number
  ticketId: number
  userId: number
  content: string
  isStaff: number
  attachments: string | null
  createTime: string
}

/** 创建工单请求 */
export interface TicketCreateRequest {
  title: string
  description?: string
  category?: string
  priority?: number
}

export interface PageResult<T> {
  records: T[]
  total: number
  page: number
  size: number
}

/** 工单分类选项 */
export const TICKET_CATEGORIES = [
  { label: '账号问题', value: 'ACCOUNT' },
  { label: '技术咨询', value: 'TECH' },
  { label: '内容举报', value: 'REPORT' },
  { label: '其他', value: 'OTHER' },
]

/** 工单状态映射 */
export const TICKET_STATUS_MAP: Record<string, string> = {
  PENDING: '待处理',
  PROCESSING: '处理中',
  RESOLVED: '已解决',
  CLOSED: '已关闭',
}

export const TICKET_STATUS_TAG: Record<string, string> = {
  PENDING: 'warning',
  PROCESSING: 'primary',
  RESOLVED: 'success',
  CLOSED: 'default',
}

/** 优先级映射 */
export const TICKET_PRIORITY_MAP: Record<number, string> = {
  1: '低',
  2: '中',
  3: '高',
  4: '紧急',
}

// ==================== AI 对话 ====================

/** 发送消息给AI */
export function sendChatMessage(data: ChatSendRequest) {
  return request.post<any, { code: number; message: string; data: ChatResponse }>(
    '/api/portal/chat/send',
    data,
  )
}

/** 获取对话历史 */
export function getChatHistory(sessionId?: string) {
  return request.get<any, { code: number; message: string; data: ChatConversation[] }>(
    '/api/portal/chat/history',
    { params: sessionId ? { sessionId } : {} },
  )
}

// ==================== 工单 ====================

/** 创建工单 */
export function createTicket(data: TicketCreateRequest) {
  return request.post<any, { code: number; message: string; data: number }>(
    '/api/portal/tickets',
    data,
  )
}

/** 我的工单列表 */
export function getMyTickets(page = 1, size = 10) {
  return request.get<any, { code: number; message: string; data: PageResult<ChatTicket> }>(
    '/api/portal/tickets',
    { params: { page, size } },
  )
}

/** 工单详情 */
export function getTicketDetail(id: number) {
  return request.get<any, { code: number; message: string; data: ChatTicket }>(
    `/api/portal/tickets/${id}`,
  )
}

/** 回复工单 */
export function replyTicket(id: number, content: string) {
  return request.post<any, { code: number; message: string; data: null }>(
    `/api/portal/tickets/${id}/reply`,
    null,
    { params: { content } },
  )
}

/** 关闭工单 */
export function closeTicket(id: number, reason: string) {
  return request.post<any, { code: number; message: string; data: null }>(
    `/api/portal/tickets/${id}/close`,
    null,
    { params: { reason } },
  )
}
