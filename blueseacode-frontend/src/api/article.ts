import request from '@/utils/request'

export interface ArtArticle {
  id: number
  userId: number
  title: string
  summary: string | null
  coverImage: string | null
  content: string
  categoryId: number | null
  viewCount: number
  likeCount: number
  commentCount: number
  collectCount: number
  isComment: number
  status: number
  rejectReason: string | null
  nickname: string | null
  avatar: string | null
  categoryName: string | null
  tags: string[]
  isLiked: boolean
  isCollected: boolean
  createTime: string
  updateTime: string
}

export interface ArtCategory {
  id: number
  name: string
  icon: string | null
  sort: number
}

export interface ArtTag {
  id: number
  name: string
}

export interface ArtComment {
  id: number
  articleId: number
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

export interface ArticleCreateRequest {
  title: string
  summary?: string
  coverImage?: string
  categoryId?: number
  content: string
  tagIds?: number[]
  isComment?: number
  status?: number
}

export interface ArticleCommentCreateRequest {
  articleId: number
  content: string
  parentId?: number
}

export interface ArticleQueryParams {
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

/** 获取文章列表 */
export function getArticles(params: ArticleQueryParams) {
  return request.get<any, { code: number; message: string; data: PageResult<ArtArticle> }>(
    '/api/portal/articles',
    { params },
  )
}

/** 获取文章详情 */
export function getArticleDetail(id: number) {
  return request.get<any, { code: number; message: string; data: ArtArticle }>(
    `/api/portal/articles/${id}`,
  )
}

/** 发布文章 */
export function createArticle(data: ArticleCreateRequest) {
  return request.post<any, { code: number; message: string; data: number }>(
    '/api/portal/articles',
    data,
  )
}

/** 编辑文章 */
export function updateArticle(id: number, data: ArticleCreateRequest) {
  return request.put<any, { code: number; message: string; data: null }>(
    `/api/portal/articles/${id}`,
    data,
  )
}

/** 删除文章 */
export function deleteArticle(id: number) {
  return request.delete<any, { code: number; message: string; data: null }>(
    `/api/portal/articles/${id}`,
  )
}

/** 获取我的文章列表 */
export function getMyArticles(params: ArticleQueryParams) {
  return request.get<any, { code: number; message: string; data: PageResult<ArtArticle> }>(
    '/api/portal/articles/my',
    { params },
  )
}

/** 点赞文章 */
export function likeArticle(id: number) {
  return request.post<any, { code: number; message: string; data: null }>(
    `/api/portal/articles/${id}/like`,
  )
}

/** 取消点赞 */
export function cancelLikeArticle(id: number) {
  return request.delete<any, { code: number; message: string; data: null }>(
    `/api/portal/articles/${id}/like`,
  )
}

/** 收藏文章 */
export function collectArticle(id: number) {
  return request.post<any, { code: number; message: string; data: null }>(
    `/api/portal/articles/${id}/collect`,
  )
}

/** 取消收藏 */
export function cancelCollectArticle(id: number) {
  return request.delete<any, { code: number; message: string; data: null }>(
    `/api/portal/articles/${id}/collect`,
  )
}

/** 获取文章分类列表 */
export function getArticleCategories() {
  return request.get<any, { code: number; message: string; data: ArtCategory[] }>(
    '/api/portal/articles/categories',
  )
}

/** 获取文章标签列表 */
export function getArticleTags() {
  return request.get<any, { code: number; message: string; data: ArtTag[] }>(
    '/api/portal/articles/tags',
  )
}

/** 获取热门文章 */
export function getHotArticles() {
  return request.get<any, { code: number; message: string; data: ArtArticle[] }>(
    '/api/portal/articles/hot',
  )
}

/** 获取评论列表 */
export function getArticleComments(articleId: number, page = 1, size = 10) {
  return request.get<any, { code: number; message: string; data: PageResult<ArtComment> }>(
    '/api/portal/article-comments',
    { params: { articleId, page, size } },
  )
}

/** 发表评论 */
export function createArticleComment(data: ArticleCommentCreateRequest) {
  return request.post<any, { code: number; message: string; data: null }>(
    '/api/portal/article-comments',
    data,
  )
}

/** 删除评论 */
export function deleteArticleComment(id: number) {
  return request.delete<any, { code: number; message: string; data: null }>(
    `/api/portal/article-comments/${id}`,
  )
}

/** 获取我收藏的文章列表 */
export function getMyCollectedArticles(params: { page?: number; size?: number }) {
  return request.get<any, { code: number; message: string; data: PageResult<ArtArticle> }>(
    '/api/portal/article-collects/articles',
    { params },
  )
}
