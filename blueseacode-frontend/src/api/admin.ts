import request from '@/utils/request'

// ==================== 通用类型 ====================
export interface PageResult<T> {
  records: T[]
  total: number
  page: number
  size: number
}

// ==================== 仪表盘 ====================
export interface DashboardVO {
  totalUsers: number
  todayNewUsers: number
  totalResources: number
  totalArticles: number
  totalDemands: number
  pendingAudit: number
  todayResourceDownloads: number
  totalViews: number
}

export interface TrendVO {
  date: string
  count: number
}

export function getDashboardStatistics() {
  return request.get<any, { code: number; message: string; data: DashboardVO }>(
    '/api/admin/dashboard/statistics',
  )
}

export function getDashboardTrend(days = 7) {
  return request.get<any, { code: number; message: string; data: TrendVO[] }>(
    '/api/admin/dashboard/trend',
    { params: { days } },
  )
}

// ==================== 用户管理 ====================
export interface SysUser {
  id: number
  username: string
  nickname: string | null
  avatar: string | null
  email: string | null
  phone: string | null
  bio: string | null
  score: number
  status: number
  roles: string[]
  lastLoginIp: string | null
  lastLoginTime: string | null
  createTime: string
  updateTime: string
}

export function getAdminUsers(params: {
  page?: number
  size?: number
  keyword?: string
  status?: number
}) {
  return request.get<any, { code: number; message: string; data: PageResult<SysUser> }>(
    '/api/admin/users',
    { params },
  )
}

export function getAdminUserDetail(id: number) {
  return request.get<any, { code: number; message: string; data: SysUser }>(
    `/api/admin/users/${id}`,
  )
}

export function changeUserStatus(id: number, status: number) {
  return request.put<any, { code: number; message: string; data: null }>(
    `/api/admin/users/${id}/status`,
    null,
    { params: { status } },
  )
}

export function assignUserRoles(id: number, roleIds: number[]) {
  return request.put<any, { code: number; message: string; data: null }>(
    `/api/admin/users/${id}/roles`,
    roleIds,
  )
}

// ==================== 角色管理 ====================
export interface SysRole {
  id: number
  roleCode: string
  roleName: string
  description: string | null
  sort: number
  createTime: string
}

export function getRoles() {
  return request.get<any, { code: number; message: string; data: SysRole[] }>(
    '/api/admin/roles',
  )
}

export function createRole(data: Partial<SysRole>) {
  return request.post<any, { code: number; message: string; data: null }>(
    '/api/admin/roles',
    data,
  )
}

export function updateRole(id: number, data: Partial<SysRole>) {
  return request.put<any, { code: number; message: string; data: null }>(
    `/api/admin/roles/${id}`,
    data,
  )
}

export function deleteRole(id: number) {
  return request.delete<any, { code: number; message: string; data: null }>(
    `/api/admin/roles/${id}`,
  )
}

// ==================== 权限管理 ====================
export interface SysPermission {
  id: number
  parentId: number | null
  name: string
  permission: string | null
  path: string | null
  component: string | null
  type: number
  icon: string | null
  sort: number
  status: number
  children: SysPermission[] | null
}

export function getPermissionTree() {
  return request.get<any, { code: number; message: string; data: SysPermission[] }>(
    '/api/admin/roles/permissions',
  )
}

export function getRolePermissions(id: number) {
  return request.get<any, { code: number; message: string; data: number[] }>(
    `/api/admin/roles/${id}/permissions`,
  )
}

export function assignRolePermissions(id: number, permissionIds: number[]) {
  return request.put<any, { code: number; message: string; data: null }>(
    `/api/admin/roles/${id}/permissions`,
    permissionIds,
  )
}

// ==================== 系统配置 ====================
export interface SysConfig {
  id: number
  configKey: string
  configValue: string
  description: string | null
}

export function getConfigs() {
  return request.get<any, { code: number; message: string; data: SysConfig[] }>(
    '/api/admin/config',
  )
}

export function updateConfigs(configs: SysConfig[]) {
  return request.put<any, { code: number; message: string; data: null }>(
    '/api/admin/config',
    configs,
  )
}

export function getConfigByKey(key: string) {
  return request.get<any, { code: number; message: string; data: string }>(
    `/api/admin/config/${key}`,
  )
}

// ==================== 公告管理 ====================
export interface SysAnnouncement {
  id: number
  title: string
  content: string
  status: number
  sort: number
  createTime: string
  updateTime: string
}

export function getAnnouncements(page = 1, size = 10, status?: number) {
  return request.get<any, { code: number; message: string; data: PageResult<SysAnnouncement> }>(
    '/api/admin/announcements',
    { params: { page, size, status } },
  )
}

export function getAnnouncement(id: number) {
  return request.get<any, { code: number; message: string; data: SysAnnouncement }>(
    `/api/admin/announcements/${id}`,
  )
}

export function createAnnouncement(data: Partial<SysAnnouncement>) {
  return request.post<any, { code: number; message: string; data: null }>(
    '/api/admin/announcements',
    data,
  )
}

export function updateAnnouncement(id: number, data: Partial<SysAnnouncement>) {
  return request.put<any, { code: number; message: string; data: null }>(
    `/api/admin/announcements/${id}`,
    data,
  )
}

export function deleteAnnouncement(id: number) {
  return request.delete<any, { code: number; message: string; data: null }>(
    `/api/admin/announcements/${id}`,
  )
}

// ==================== 轮播图管理 ====================
export interface SysBanner {
  id: number
  title: string | null
  imageUrl: string
  linkUrl: string | null
  sort: number
  status: number
  createTime: string
  updateTime: string
}

export function getBanners(page = 1, size = 10) {
  return request.get<any, { code: number; message: string; data: PageResult<SysBanner> }>(
    '/api/admin/banners',
    { params: { page, size } },
  )
}

export function getBanner(id: number) {
  return request.get<any, { code: number; message: string; data: SysBanner }>(
    `/api/admin/banners/${id}`,
  )
}

export function createBanner(data: Partial<SysBanner>) {
  return request.post<any, { code: number; message: string; data: null }>(
    '/api/admin/banners',
    data,
  )
}

export function updateBanner(id: number, data: Partial<SysBanner>) {
  return request.put<any, { code: number; message: string; data: null }>(
    `/api/admin/banners/${id}`,
    data,
  )
}

export function deleteBanner(id: number) {
  return request.delete<any, { code: number; message: string; data: null }>(
    `/api/admin/banners/${id}`,
  )
}

// ==================== 日志管理 ====================
export interface SysOperationLog {
  id: number
  userId: number | null
  username: string | null
  module: string | null
  action: string | null
  targetId: number | null
  targetType: string | null
  requestIp: string | null
  params: string | null
  result: string | null
  duration: number | null
  createTime: string
}

export interface SysLoginLog {
  id: number
  userId: number | null
  username: string
  ip: string | null
  device: string | null
  result: number
  failReason: string | null
  createTime: string
}

export function getOperationLogs(params: {
  page?: number
  size?: number
  module?: string
  action?: string
  startDate?: string
  endDate?: string
}) {
  return request.get<any, { code: number; message: string; data: PageResult<SysOperationLog> }>(
    '/api/admin/logs/operations',
    { params },
  )
}

export function getLoginLogs(params: {
  page?: number
  size?: number
  username?: string
  result?: number
}) {
  return request.get<any, { code: number; message: string; data: PageResult<SysLoginLog> }>(
    '/api/admin/logs/login',
    { params },
  )
}

// ==================== 需求管理（管理端） ====================
export function getAdminDemands(params: {
  page?: number
  size?: number
  keyword?: string
  category?: string
  status?: number
  sortBy?: string
}) {
  return request.get<any, { code: number; message: string; data: PageResult<any> }>(
    '/api/admin/demands',
    { params },
  )
}

export function auditDemand(id: number, status: number, rejectReason?: string) {
  return request.put<any, { code: number; message: string; data: null }>(
    `/api/admin/demands/${id}/audit`,
    null,
    { params: { status, rejectReason } },
  )
}

// ==================== 内容审核 ====================
export function getAuditResources(params: { page?: number; size?: number }) {
  return request.get<any, { code: number; message: string; data: PageResult<any> }>(
    '/api/admin/audit/resources',
    { params },
  )
}

export function auditResource(id: number, status: number, rejectReason?: string) {
  return request.put<any, { code: number; message: string; data: null }>(
    `/api/admin/audit/resources/${id}`,
    null,
    { params: { status, rejectReason } },
  )
}

export function getAuditArticles(params: { page?: number; size?: number }) {
  return request.get<any, { code: number; message: string; data: PageResult<any> }>(
    '/api/admin/audit/articles',
    { params },
  )
}

export function auditArticle(id: number, status: number, rejectReason?: string) {
  return request.put<any, { code: number; message: string; data: null }>(
    `/api/admin/audit/articles/${id}`,
    null,
    { params: { status, rejectReason } },
  )
}

export function getAuditDemands(params: { page?: number; size?: number }) {
  return request.get<any, { code: number; message: string; data: PageResult<any> }>(
    '/api/admin/audit/demands',
    { params },
  )
}

// ==================== 工单管理 ====================
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
  replies: any[]
  createTime: string
  updateTime: string
}

export function getAdminTickets(page = 1, size = 10, status?: string) {
  return request.get<any, { code: number; message: string; data: PageResult<ChatTicket> }>(
    '/api/admin/ai/tickets',
    { params: { page, size, status } },
  )
}

export function getAdminTicketDetail(id: number) {
  return request.get<any, { code: number; message: string; data: ChatTicket }>(
    `/api/admin/ai/tickets/${id}`,
  )
}

export function assignTicket(id: number, assigneeId: number) {
  return request.put<any, { code: number; message: string; data: null }>(
    `/api/admin/ai/tickets/${id}/assign`,
    null,
    { params: { assigneeId } },
  )
}

export function replyTicket(id: number, content: string, isStaff = true, userId?: number) {
  return request.post<any, { code: number; message: string; data: null }>(
    `/api/admin/ai/tickets/${id}/reply`,
    null,
    { params: { content, isStaff, userId } },
  )
}

export function closeTicket(id: number, reason: string) {
  return request.post<any, { code: number; message: string; data: null }>(
    `/api/admin/ai/tickets/${id}/close`,
    null,
    { params: { reason } },
  )
}

// ==================== FAQ管理 ====================
export interface ChatFaq {
  id: number
  question: string
  answer: string
  category: string
  sort: number
  status: number
  hitCount: number
  createTime: string
}

export function getFaqList(keyword?: string, category?: string, page = 1, size = 10) {
  return request.get<any, { code: number; message: string; data: PageResult<ChatFaq> }>(
    '/api/admin/ai/faq',
    { params: { keyword, category, page, size } },
  )
}

export function createFaq(data: Partial<ChatFaq>) {
  return request.post<any, { code: number; message: string; data: null }>(
    '/api/admin/ai/faq',
    data,
  )
}

export function updateFaq(id: number, data: Partial<ChatFaq>) {
  return request.put<any, { code: number; message: string; data: null }>(
    `/api/admin/ai/faq/${id}`,
    data,
  )
}

export function deleteFaq(id: number) {
  return request.delete<any, { code: number; message: string; data: null }>(
    `/api/admin/ai/faq/${id}`,
  )
}

// ==================== 知识库管理 ====================
export interface ChatKnowledgeBase {
  id: number
  title: string
  content: string
  category: string
  keywords: string | null
  status: number
  hitCount: number
  createTime: string
}

export function getKnowledgeList(keyword?: string, category?: string, page = 1, size = 10) {
  return request.get<any, { code: number; message: string; data: PageResult<ChatKnowledgeBase> }>(
    '/api/admin/ai/knowledge',
    { params: { keyword, category, page, size } },
  )
}

export function createKnowledge(data: Partial<ChatKnowledgeBase>) {
  return request.post<any, { code: number; message: string; data: null }>(
    '/api/admin/ai/knowledge',
    data,
  )
}

export function updateKnowledge(id: number, data: Partial<ChatKnowledgeBase>) {
  return request.put<any, { code: number; message: string; data: null }>(
    `/api/admin/ai/knowledge/${id}`,
    data,
  )
}

export function deleteKnowledge(id: number) {
  return request.delete<any, { code: number; message: string; data: null }>(
    `/api/admin/ai/knowledge/${id}`,
  )
}

// ==================== 资源管理（管理端） ====================
export function getAdminResources(params: { page?: number; size?: number; keyword?: string; status?: number; categoryId?: number }) {
  return request.get<any, { code: number; message: string; data: PageResult<any> }>(
    '/api/admin/resources',
    { params },
  )
}

export function adminUpdateResource(id: number, data: any) {
  return request.put<any, { code: number; message: string; data: null }>(
    `/api/admin/resources/${id}`,
    data,
  )
}

export function changeResourceStatus(id: number, status: number, rejectReason?: string) {
  return request.put<any, { code: number; message: string; data: null }>(
    `/api/admin/resources/${id}/status`,
    null,
    { params: { status, rejectReason } },
  )
}

export function adminDeleteResource(id: number) {
  return request.delete<any, { code: number; message: string; data: null }>(
    `/api/admin/resources/${id}`,
  )
}

// ==================== 资源分类/标签 ====================
export interface ResCategory {
  id: number
  name: string
  icon: string | null
  sort: number
}

export interface ResTag {
  id: number
  name: string
}

export function getAdminCategories() {
  return request.get<any, { code: number; message: string; data: ResCategory[] }>(
    '/api/admin/resources/categories',
  )
}

export function getAdminTags() {
  return request.get<any, { code: number; message: string; data: ResTag[] }>(
    '/api/admin/resources/tags',
  )
}
