import request from '@/utils/request'

export interface DemDemand {
  id: number
  userId: number
  title: string
  description: string | null
  category: string
  budgetMin: number | null
  budgetMax: number | null
  deadline: string | null
  contact: string | null
  viewCount: number
  orderCount: number
  takerId: number | null
  status: number
  rejectReason: string | null
  nickname: string | null
  avatar: string | null
  takerNickname: string | null
  takerAvatar: string | null
  attachments: DemAttachment[]
  isTaker: boolean
  createTime: string
  updateTime: string
}

export interface DemAttachment {
  id: number
  demandId: number
  fileName: string
  fileUrl: string
  fileSize: number
  createTime: string
}

export interface DemOrder {
  id: number
  demandId: number
  userId: number
  nickname: string | null
  avatar: string | null
  createTime: string
}

export interface DemandCreateRequest {
  title: string
  description: string
  category: string
  budgetMin?: number
  budgetMax?: number
  deadline?: string
  contact?: string
  attachments?: DemandAttachmentDTO[]
}

export interface DemandAttachmentDTO {
  fileName: string
  fileUrl: string
  fileSize: number
}

export interface DemandQueryParams {
  page?: number
  size?: number
  keyword?: string
  category?: string
  status?: number
  userId?: number
  takerId?: number
  sortBy?: string
}

export interface PageResult<T> {
  records: T[]
  total: number
  page: number
  size: number
}

/** 需求分类 */
export const DEMAND_CATEGORIES = [
  { label: '项目外包', value: 'PROJECT' },
  { label: '技术咨询', value: 'TECH_CONSULT' },
  { label: '组队招募', value: 'TEAM_UP' },
  { label: '其他', value: 'OTHER' },
]

/** 需求状态映射 */
export const DEMAND_STATUS_MAP: Record<number, string> = {
  0: '待审核',
  1: '招募中',
  2: '进行中',
  3: '已完成',
  4: '已关闭',
  5: '已驳回',
}

export const DEMAND_STATUS_TAG: Record<number, string> = {
  0: 'warning',
  1: 'success',
  2: 'primary',
  3: 'default',
  4: 'info',
  5: 'error',
}

/** 获取需求列表 */
export function getDemands(params: DemandQueryParams) {
  return request.get<any, { code: number; message: string; data: PageResult<DemDemand> }>(
    '/api/portal/demands',
    { params },
  )
}

/** 获取我的需求列表 */
export function getMyDemands(params: DemandQueryParams) {
  return request.get<any, { code: number; message: string; data: PageResult<DemDemand> }>(
    '/api/portal/demands/my',
    { params },
  )
}

/** 获取我接的单列表 */
export function getMyOrders(params: DemandQueryParams) {
  return request.get<any, { code: number; message: string; data: PageResult<DemDemand> }>(
    '/api/portal/demands/my-orders',
    { params },
  )
}

/** 获取需求详情 */
export function getDemandDetail(id: number) {
  return request.get<any, { code: number; message: string; data: DemDemand }>(
    `/api/portal/demands/${id}`,
  )
}

/** 发布需求 */
export function createDemand(data: DemandCreateRequest) {
  return request.post<any, { code: number; message: string; data: number }>(
    '/api/portal/demands',
    data,
  )
}

/** 编辑需求 */
export function updateDemand(id: number, data: DemandCreateRequest) {
  return request.put<any, { code: number; message: string; data: null }>(
    `/api/portal/demands/${id}`,
    data,
  )
}

/** 删除需求 */
export function deleteDemand(id: number) {
  return request.delete<any, { code: number; message: string; data: null }>(
    `/api/portal/demands/${id}`,
  )
}

/** 接单 */
export function takeOrder(id: number) {
  return request.post<any, { code: number; message: string; data: null }>(
    `/api/portal/demands/${id}/take`,
  )
}

/** 确认完成 */
export function completeDemand(id: number) {
  return request.post<any, { code: number; message: string; data: null }>(
    `/api/portal/demands/${id}/complete`,
  )
}

/** 获取需求附件列表 */
export function getDemandAttachments(id: number) {
  return request.get<any, { code: number; message: string; data: DemAttachment[] }>(
    `/api/portal/demands/${id}/attachments`,
  )
}

/** 获取接单记录 */
export function getDemandOrders(id: number, page = 1, size = 10) {
  return request.get<any, { code: number; message: string; data: PageResult<DemOrder> }>(
    `/api/portal/demands/${id}/orders`,
    { params: { page, size } },
  )
}
