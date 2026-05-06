<template>
  <div class="audit">
    <h2 style="margin-bottom: 20px">内容审核</h2>

    <n-tabs type="line" default-value="resources" @update:value="handleTabChange">
      <!-- ===== 待审资源 ===== -->
      <n-tab-pane name="resources" tab="待审资源">
        <n-data-table
          :columns="resourceColumns"
          :data="resources"
          :loading="resLoading"
          :pagination="resPagination"
          @update:page="(p: number) => { resQuery.page = p; loadResources() }"
        />
      </n-tab-pane>

      <!-- ===== 待审文章 ===== -->
      <n-tab-pane name="articles" tab="待审文章">
        <n-data-table
          :columns="articleColumns"
          :data="articles"
          :loading="artLoading"
          :pagination="artPagination"
          @update:page="(p: number) => { artQuery.page = p; loadArticles() }"
        />
      </n-tab-pane>

      <!-- ===== 待审需求 ===== -->
      <n-tab-pane name="demands" tab="待审需求">
        <n-data-table
          :columns="demandColumns"
          :data="demands"
          :loading="demLoading"
          :pagination="demPagination"
          @update:page="(p: number) => { demQuery.page = p; loadDemands() }"
        />
      </n-tab-pane>
    </n-tabs>

    <!-- ===== 审核详情弹窗 ===== -->
    <n-modal
      v-model:show="showDetailModal"
      :title="detailTitle"
      preset="card"
      style="width: 800px; max-height: 85vh"
      :mask-closable="false"
      :segmented="{ content: true }"
      transform-origin="center"
    >
      <template v-if="currentItem">
        <!-- 资源详情 -->
        <template v-if="currentType === 'resource'">
          <n-descriptions label-placement="left" bordered :column="2">
            <n-descriptions-item label="标题" :span="2">{{ currentItem.title }}</n-descriptions-item>
            <n-descriptions-item label="发布人">{{ currentItem.nickname }}</n-descriptions-item>
            <n-descriptions-item label="分类">{{ currentItem.categoryName }}</n-descriptions-item>
            <n-descriptions-item label="文件">{{ currentItem.fileName }}</n-descriptions-item>
            <n-descriptions-item label="格式">{{ currentItem.fileFormat }}</n-descriptions-item>
            <n-descriptions-item label="大小">{{ formatFileSize(currentItem.fileSize) }}</n-descriptions-item>
            <n-descriptions-item label="所需积分">{{ currentItem.downloadPoints }}</n-descriptions-item>
            <n-descriptions-item label="描述" :span="2">{{ currentItem.description || '无' }}</n-descriptions-item>
          </n-descriptions>
        </template>

        <!-- 文章详情 -->
        <template v-if="currentType === 'article'">
          <n-descriptions label-placement="left" bordered :column="2">
            <n-descriptions-item label="标题" :span="2">{{ currentItem.title }}</n-descriptions-item>
            <n-descriptions-item label="作者">{{ currentItem.nickname }}</n-descriptions-item>
            <n-descriptions-item label="分类">{{ currentItem.categoryName }}</n-descriptions-item>
            <n-descriptions-item label="摘要" :span="2">{{ currentItem.summary || '无' }}</n-descriptions-item>
          </n-descriptions>
          <n-divider>正文内容</n-divider>
          <div class="content-preview">{{ currentItem.content }}</div>
        </template>

        <!-- 需求详情 -->
        <template v-if="currentType === 'demand'">
          <n-descriptions label-placement="left" bordered :column="2">
            <n-descriptions-item label="标题" :span="2">{{ currentItem.title }}</n-descriptions-item>
            <n-descriptions-item label="发布人">{{ currentItem.nickname }}</n-descriptions-item>
            <n-descriptions-item label="分类">{{ demandCategoryLabel(currentItem.category) }}</n-descriptions-item>
            <n-descriptions-item label="预算">
              <template v-if="currentItem.budgetMin != null">
                ¥{{ currentItem.budgetMin }} ~ ¥{{ currentItem.budgetMax }}
              </template>
              <span v-else>面议</span>
            </n-descriptions-item>
            <n-descriptions-item label="截止日期">{{ currentItem.deadline || '不限' }}</n-descriptions-item>
            <n-descriptions-item label="联系方式">{{ currentItem.contact || '未填' }}</n-descriptions-item>
            <n-descriptions-item label="描述" :span="2">{{ currentItem.description || '无' }}</n-descriptions-item>
          </n-descriptions>
        </template>

        <n-divider />

        <!-- 审核操作区 -->
        <n-space vertical>
          <n-input
            v-model:value="rejectReason"
            type="textarea"
            placeholder="驳回原因（驳回时必填）"
            :rows="3"
            :disabled="auditing"
          />
          <n-space justify="center" style="margin-top: 8px">
            <n-button type="success" size="large" @click="doAudit(1)" :loading="auditing" :disabled="auditing">
              <template #icon><n-icon><svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><path fill="currentColor" d="M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41L9 16.17z"/></svg></n-icon></template>
              审核通过
            </n-button>
            <n-button type="warning" size="large" @click="doAudit(2)" :loading="auditing" :disabled="auditing">
              <template #icon><n-icon><svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><path fill="currentColor" d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12 19 6.41z"/></svg></n-icon></template>
              驳回
            </n-button>
          </n-space>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, h, onMounted } from 'vue'
import { NButton, NSpace, NTag, NIcon, useMessage } from 'naive-ui'
import {
  getAuditResources,
  auditResource,
  getAuditArticles,
  auditArticle,
  getAuditDemands,
} from '@/api/admin'
import { DEMAND_CATEGORIES } from '@/api/demand'

const message = useMessage()

const showDetailModal = ref(false)
const currentType = ref<'resource' | 'article' | 'demand'>('resource')
const currentItem = ref<any>(null)
const rejectReason = ref('')
const auditing = ref(false)

const detailTitle = computed(() => {
  const typeMap = { resource: '资源详情', article: '文章详情', demand: '需求详情' }
  const prefix = typeMap[currentType.value] || '详情'
  return `${prefix} - ${currentItem.value?.title || ''}`
})

function formatFileSize(bytes: number | null) {
  if (!bytes) return '-'
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(1) + ' MB'
}

function demandCategoryLabel(value: string) {
  const c = DEMAND_CATEGORIES.find((x) => x.value === value)
  return c ? c.label : value
}

// ===== 资源审核 =====
const resources = ref<any[]>([])
const resLoading = ref(false)
const resQuery = reactive({ page: 1, size: 10 })
const resPagination = reactive({ page: 1, pageCount: 1, pageSize: 10, onChange: (p: number) => { resQuery.page = p; loadResources() } })

const resourceColumns = [
  { title: 'ID', key: 'id', width: 60 },
  { title: '标题', key: 'title', ellipsis: true },
  { title: '发布人', key: 'nickname', width: 100 },
  { title: '分类', key: 'categoryName', width: 100 },
  { title: '创建时间', key: 'createTime', width: 170 },
  {
    title: '操作', key: 'actions', width: 100,
    render(row: any) {
      return h(NButton, { size: 'small', type: 'primary', onClick: () => openDetail('resource', row) }, { default: () => '审核' })
    },
  },
]

async function loadResources() {
  resLoading.value = true
  try {
    const res = await getAuditResources({ page: resQuery.page, size: resQuery.size })
    if (res.code === 200) {
      resources.value = res.data.records
      resPagination.pageCount = Math.ceil(res.data.total / res.data.size)
    }
  } catch { message.error('加载失败') }
  finally { resLoading.value = false }
}

// ===== 文章审核 =====
const articles = ref<any[]>([])
const artLoading = ref(false)
const artQuery = reactive({ page: 1, size: 10 })
const artPagination = reactive({ page: 1, pageCount: 1, pageSize: 10, onChange: (p: number) => { artQuery.page = p; loadArticles() } })

const articleColumns = [
  { title: 'ID', key: 'id', width: 60 },
  { title: '标题', key: 'title', ellipsis: true },
  { title: '作者', key: 'nickname', width: 100 },
  { title: '分类', key: 'categoryName', width: 100 },
  { title: '创建时间', key: 'createTime', width: 170 },
  {
    title: '操作', key: 'actions', width: 100,
    render(row: any) {
      return h(NButton, { size: 'small', type: 'primary', onClick: () => openDetail('article', row) }, { default: () => '审核' })
    },
  },
]

async function loadArticles() {
  artLoading.value = true
  try {
    const res = await getAuditArticles({ page: artQuery.page, size: artQuery.size })
    if (res.code === 200) {
      articles.value = res.data.records
      artPagination.pageCount = Math.ceil(res.data.total / res.data.size)
    }
  } catch { message.error('加载失败') }
  finally { artLoading.value = false }
}

// ===== 需求审核 =====
const demands = ref<any[]>([])
const demLoading = ref(false)
const demQuery = reactive({ page: 1, size: 10 })
const demPagination = reactive({ page: 1, pageCount: 1, pageSize: 10, onChange: (p: number) => { demQuery.page = p; loadDemands() } })

const demandColumns = [
  { title: 'ID', key: 'id', width: 60 },
  { title: '标题', key: 'title', ellipsis: true },
  { title: '发布人', key: 'nickname', width: 100 },
  {
    title: '分类', key: 'category', width: 90,
    render(row: any) {
      const c = DEMAND_CATEGORIES.find((x) => x.value === row.category)
      return c ? c.label : row.category
    },
  },
  { title: '创建时间', key: 'createTime', width: 170 },
  {
    title: '操作', key: 'actions', width: 100,
    render(row: any) {
      return h(NButton, { size: 'small', type: 'primary', onClick: () => openDetail('demand', row) }, { default: () => '审核' })
    },
  },
]

async function loadDemands() {
  demLoading.value = true
  try {
    const res = await getAuditDemands({ page: demQuery.page, size: demQuery.size })
    if (res.code === 200) {
      demands.value = res.data.records
      demPagination.pageCount = Math.ceil(res.data.total / res.data.size)
    }
  } catch { message.error('加载失败') }
  finally { demLoading.value = false }
}

// ===== 审核操作 =====

/** 打开详情弹窗 */
function openDetail(type: 'resource' | 'article' | 'demand', row: any) {
  currentType.value = type
  currentItem.value = row
  rejectReason.value = ''
  auditing.value = false
  showDetailModal.value = true
}

/** 执行审核 */
async function doAudit(status: number) {
  if (status === 2 && !rejectReason.value) {
    message.warning('驳回必须填写原因')
    return
  }
  auditing.value = true
  try {
    let res: any
    if (currentType.value === 'resource') {
      res = await auditResource(currentItem.value.id, status === 1 ? 1 : 2, rejectReason.value || undefined)
    } else if (currentType.value === 'article') {
      res = await auditArticle(currentItem.value.id, status === 1 ? 2 : 3, rejectReason.value || undefined)
    } else {
      res = await auditDemand(currentItem.value.id, status === 1 ? 1 : 5, rejectReason.value || undefined)
    }
    if (res.code === 200) {
      message.success(status === 1 ? '审核通过' : '已驳回')
      showDetailModal.value = false
      // 刷新所有列表
      loadResources()
      loadArticles()
      loadDemands()
    }
  } catch {
    message.error('操作失败')
  } finally {
    auditing.value = false
  }
}

function handleTabChange(tab: string) {
  if (tab === 'resources' && resources.value.length === 0) loadResources()
  if (tab === 'articles' && articles.value.length === 0) loadArticles()
  if (tab === 'demands' && demands.value.length === 0) loadDemands()
}

onMounted(loadResources)
</script>

<style scoped>
.content-preview {
  background: #f8f9fa;
  padding: 16px;
  border-radius: 4px;
  max-height: 300px;
  overflow-y: auto;
  white-space: pre-wrap;
  line-height: 1.8;
  font-size: 14px;
}
</style>
