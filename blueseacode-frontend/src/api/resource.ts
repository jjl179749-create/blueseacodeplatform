import request from '@/utils/request'

export interface ResResource {
  id: number
  userId: number
  title: string
  description: string | null
  coverImage: string | null
  categoryId: number
  fileUrl: string
  fileName: string | null
  fileSize: number
  fileFormat: string | null
  downloadPoints: number
  downloadCount: number
  viewCount: number
  commentCount: number
  collectCount: number
  status: number
  rejectReason: string | null
  nickname: string | null
  avatar: string | null
  categoryName: string | null
  tags: string[]
  isCollected: boolean
  createTime: string
  updateTime: string
}

export interface ResCategory {
  id: number
  parentId: number | null
  name: string
  icon: string | null
  sort: number
}

export interface ResTag {
  id: number
  name: string
}

export interface ResComment {
  id: number
  resourceId: number
  userId: number
  content: string
  parentId: number | null
  likeCount: number
  status: number
  nickname: string | null
  avatar: string | null
  replyToNickname: string | null
  createTime: string
}

export interface ResourceCreateRequest {
  title: string
  description?: string
  coverImage?: string
  categoryId: number
  fileUrl: string
  fileName?: string
  downloadPoints?: number
  tagIds?: number[]
}

export interface CommentCreateRequest {
  resourceId: number
  content: string
  parentId?: number
}

export interface ResourceQueryParams {
  page?: number
  size?: number
  keyword?: string
  categoryId?: number
  userId?: number
  status?: number
  sortBy?: string
}

export interface PageResult<T> {
  records: T[]
  total: number
  page: number
  size: number
}

/** 获取资源列表 */
export function getResources(params: ResourceQueryParams) {
  return request.get<any, { code: number; message: string; data: PageResult<ResResource> }>(
    '/api/portal/resources',
    { params },
  )
}

/** 获取资源详情 */
export function getResourceDetail(id: number) {
  return request.get<any, { code: number; message: string; data: ResResource }>(
    `/api/portal/resources/${id}`,
  )
}

/** 发布资源 */
export function createResource(data: ResourceCreateRequest) {
  return request.post<any, { code: number; message: string; data: number }>(
    '/api/portal/resources',
    data,
  )
}

/** 编辑资源 */
export function updateResource(id: number, data: ResourceCreateRequest) {
  return request.put<any, { code: number; message: string; data: null }>(
    `/api/portal/resources/${id}`,
    data,
  )
}

/** 删除资源 */
export function deleteResource(id: number) {
  return request.delete<any, { code: number; message: string; data: null }>(
    `/api/portal/resources/${id}`,
  )
}

/** 下载资源 */
export function downloadResource(id: number) {
  return request.post<any, { code: number; message: string; data: string }>(
    `/api/portal/resources/${id}/download`,
  )
}

/** 获取我的资源列表 */
export function getMyResources(params: ResourceQueryParams) {
  return request.get<any, { code: number; message: string; data: PageResult<ResResource> }>(
    '/api/portal/resources/my',
    { params },
  )
}

/** 获取分类列表 */
export function getCategories() {
  return request.get<any, { code: number; message: string; data: ResCategory[] }>(
    '/api/portal/resources/categories',
  )
}

/** 获取标签列表 */
export function getTags() {
  return request.get<any, { code: number; message: string; data: ResTag[] }>(
    '/api/portal/resources/tags',
  )
}

/** 获取评论列表 */
export function getComments(resourceId: number, page = 1, size = 10) {
  return request.get<any, { code: number; message: string; data: PageResult<ResComment> }>(
    '/api/portal/comments',
    { params: { resourceId, page, size } },
  )
}

/** 发表评论 */
export function createComment(data: CommentCreateRequest) {
  return request.post<any, { code: number; message: string; data: null }>(
    '/api/portal/comments',
    data,
  )
}

/** 删除评论 */
export function deleteComment(id: number) {
  return request.delete<any, { code: number; message: string; data: null }>(
    `/api/portal/comments/${id}`,
  )
}

/** 收藏资源 */
export function collectResource(resourceId: number) {
  return request.post<any, { code: number; message: string; data: null }>(
    `/api/portal/collects/resources/${resourceId}`,
  )
}

/** 取消收藏 */
export function cancelCollect(resourceId: number) {
  return request.delete<any, { code: number; message: string; data: null }>(
    `/api/portal/collects/resources/${resourceId}`,
  )
}

/** 检查是否已收藏 */
export function checkCollected(resourceId: number) {
  return request.get<any, { code: number; message: string; data: boolean }>(
    `/api/portal/collects/resources/${resourceId}/check`,
  )
}

/** 获取我收藏的资源列表 */
export function getMyCollectedResources(params: { page?: number; size?: number }) {
  return request.get<any, { code: number; message: string; data: PageResult<ResResource> }>(
    '/api/portal/collects/resources',
    { params },
  )
}
