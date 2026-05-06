<template>
  <div class="log-management">
    <h2 style="margin-bottom: 20px">日志管理</h2>

    <n-tabs type="line" default-value="operations" @update:value="handleTabChange">
      <!-- 操作日志 -->
      <n-tab-pane name="operations" tab="操作日志">
        <n-card style="margin-bottom: 16px">
          <n-space>
            <n-input v-model:value="opQuery.module" placeholder="模块" clearable style="width: 140px" />
            <n-input v-model:value="opQuery.action" placeholder="操作" clearable style="width: 140px" />
            <n-date-picker v-model:value="opDateRange" type="datetimerange" clearable style="width: 300px" placeholder="日期范围" />
            <n-button type="primary" @click="searchOps">搜索</n-button>
            <n-button @click="resetOps">重置</n-button>
          </n-space>
        </n-card>
        <n-card>
          <n-data-table :columns="opColumns" :data="opList" :loading="opLoading" :pagination="opPagination" @update:page="(p: number) => { opQuery.page = p; loadOps() }" />
        </n-card>
      </n-tab-pane>

      <!-- 登录日志 -->
      <n-tab-pane name="login" tab="登录日志">
        <n-card style="margin-bottom: 16px">
          <n-space>
            <n-input v-model:value="loginQuery.username" placeholder="用户名" clearable style="width: 160px" />
            <n-select v-model:value="loginQuery.result" :options="loginResultOptions" placeholder="结果" clearable style="width: 120px" />
            <n-button type="primary" @click="searchLogins">搜索</n-button>
            <n-button @click="resetLogins">重置</n-button>
          </n-space>
        </n-card>
        <n-card>
          <n-data-table :columns="loginColumns" :data="loginList" :loading="loginLoading" :pagination="loginPagination" @update:page="(p: number) => { loginQuery.page = p; loadLogins() }" />
        </n-card>
      </n-tab-pane>
    </n-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, h, onMounted } from 'vue'
import { NTag, useMessage } from 'naive-ui'
import { getOperationLogs, getLoginLogs, type SysOperationLog, type SysLoginLog } from '@/api/admin'

const message = useMessage()

// ===== 操作日志 =====
const opList = ref<SysOperationLog[]>([])
const opLoading = ref(false)
const opQuery = reactive({ page: 1, size: 10, module: '', action: '', startDate: '', endDate: '' })
const opDateRange = ref<[number, number] | null>(null)
const opPagination = reactive({ page: 1, pageCount: 1, pageSize: 10, onChange: (p: number) => { opQuery.page = p; loadOps() } })

const opColumns = [
  { title: 'ID', key: 'id', width: 60 },
  { title: '用户', key: 'username', width: 100 },
  { title: '模块', key: 'module', width: 80 },
  { title: '操作', key: 'action', width: 80 },
  { title: 'IP', key: 'requestIp', width: 120 },
  { title: '耗时(ms)', key: 'duration', width: 80 },
  { title: '时间', key: 'createTime', width: 170 },
]

async function loadOps() {
  opLoading.value = true
  try {
    if (opDateRange.value) {
      opQuery.startDate = new Date(opDateRange.value[0]).toISOString()
      opQuery.endDate = new Date(opDateRange.value[1]).toISOString()
    } else {
      opQuery.startDate = ''
      opQuery.endDate = ''
    }
    const res = await getOperationLogs({
      page: opQuery.page, size: opQuery.size,
      module: opQuery.module || undefined,
      action: opQuery.action || undefined,
      startDate: opQuery.startDate || undefined,
      endDate: opQuery.endDate || undefined,
    })
    if (res.code === 200) {
      opList.value = res.data.records
      opPagination.pageCount = Math.ceil(res.data.total / res.data.size)
    }
  } catch { message.error('加载失败') }
  finally { opLoading.value = false }
}

function searchOps() { opQuery.page = 1; loadOps() }
function resetOps() { opQuery.module = ''; opQuery.action = ''; opDateRange.value = null; opQuery.page = 1; loadOps() }

// ===== 登录日志 =====
const loginList = ref<SysLoginLog[]>([])
const loginLoading = ref(false)
const loginQuery = reactive({ page: 1, size: 10, username: '', result: null as number | null })
const loginPagination = reactive({ page: 1, pageCount: 1, pageSize: 10, onChange: (p: number) => { loginQuery.page = p; loadLogins() } })
const loginResultOptions = [
  { label: '全部', value: null },
  { label: '成功', value: 1 },
  { label: '失败', value: 0 },
]

const loginColumns = [
  { title: 'ID', key: 'id', width: 60 },
  { title: '用户名', key: 'username', width: 120 },
  { title: 'IP', key: 'ip', width: 120 },
  { title: '设备', key: 'device', ellipsis: true },
  {
    title: '结果', key: 'result', width: 70,
    render(row: SysLoginLog) {
      return h(NTag, { type: row.result === 1 ? 'success' : 'error', size: 'small' }, { default: () => row.result === 1 ? '成功' : '失败' })
    },
  },
  {
    title: '失败原因', key: 'failReason', ellipsis: true,
  },
  { title: '时间', key: 'createTime', width: 170 },
]

async function loadLogins() {
  loginLoading.value = true
  try {
    const res = await getLoginLogs({
      page: loginQuery.page, size: loginQuery.size,
      username: loginQuery.username || undefined,
      result: loginQuery.result ?? undefined,
    })
    if (res.code === 200) {
      loginList.value = res.data.records
      loginPagination.pageCount = Math.ceil(res.data.total / res.data.size)
    }
  } catch { message.error('加载失败') }
  finally { loginLoading.value = false }
}

function searchLogins() { loginQuery.page = 1; loadLogins() }
function resetLogins() { loginQuery.username = ''; loginQuery.result = null; loginQuery.page = 1; loadLogins() }

function handleTabChange(tab: string) {
  if (tab === 'operations' && opList.value.length === 0) loadOps()
  if (tab === 'login' && loginList.value.length === 0) loadLogins()
}

onMounted(loadOps)
</script>
