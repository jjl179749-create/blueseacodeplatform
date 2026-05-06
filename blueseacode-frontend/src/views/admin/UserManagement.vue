<template>
  <div class="user-management">
    <h2 style="margin-bottom: 20px">用户管理</h2>

    <!-- 搜索栏 -->
    <n-card style="margin-bottom: 16px">
      <n-space>
        <n-input v-model:value="query.keyword" placeholder="搜索用户名/昵称/邮箱" clearable style="width: 260px" @keyup.enter="search" />
        <n-select v-model:value="query.status" :options="statusOptions" placeholder="状态筛选" clearable style="width: 140px" />
        <n-button type="primary" @click="search">搜索</n-button>
        <n-button @click="reset">重置</n-button>
      </n-space>
    </n-card>

    <!-- 用户列表 -->
    <n-card>
      <n-data-table
        :columns="columns"
        :data="users"
        :pagination="pagination"
        :loading="loading"
        :row-key="(row: any) => row.id"
        @update:page="handlePageChange"
      />
    </n-card>

    <!-- 角色分配弹窗 -->
    <n-modal v-model:show="showRoleModal" title="分配角色" preset="card" style="width: 500px">
      <n-space vertical>
        <n-checkbox-group v-model:value="selectedRoleIds">
          <n-space>
            <n-checkbox v-for="role in roles" :key="role.id" :value="role.id" :label="role.roleName" />
          </n-space>
        </n-checkbox-group>
        <n-button type="primary" @click="saveRoles" :loading="roleSaving">保存</n-button>
      </n-space>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, h, onMounted } from 'vue'
import { NButton, NSpace, NTag, useMessage, useDialog } from 'naive-ui'
import {
  getAdminUsers,
  changeUserStatus,
  assignUserRoles,
  getRoles,
  type SysUser,
  type SysRole,
} from '@/api/admin'

const message = useMessage()
const dialog = useDialog()
const loading = ref(false)
const users = ref<SysUser[]>([])
const roles = ref<SysRole[]>([])
const showRoleModal = ref(false)
const roleSaving = ref(false)
const currentUser = ref<SysUser | null>(null)
const selectedRoleIds = ref<number[]>([])

const query = reactive({
  page: 1,
  size: 10,
  keyword: '',
  status: null as number | null,
})

const statusOptions = [
  { label: '正常', value: 0 },
  { label: '禁用', value: 1 },
]

const pagination = reactive({
  page: 1,
  pageSize: 10,
  pageCount: 1,
  showSizePicker: false,
  onChange: (page: number) => {
    query.page = page
    loadUsers()
  },
})

const columns = [
  { title: 'ID', key: 'id', width: 70 },
  { title: '用户名', key: 'username', width: 120 },
  { title: '昵称', key: 'nickname', width: 120 },
  { title: '邮箱', key: 'email', width: 180 },
  { title: '积分', key: 'score', width: 70 },
  {
    title: '状态',
    key: 'status',
    width: 80,
    render(row: SysUser) {
      return h(NTag, { type: row.status === 0 ? 'success' : 'error', size: 'small' }, { default: () => row.status === 0 ? '正常' : '禁用' })
    },
  },
  { title: '注册时间', key: 'createTime', width: 180 },
  {
    title: '操作',
    key: 'actions',
    width: 200,
    render(row: SysUser) {
      return h(NSpace, null, {
        default: () => [
          h(NButton, {
            size: 'small',
            type: row.status === 0 ? 'warning' : 'success',
            onClick: () => toggleStatus(row),
          }, { default: () => row.status === 0 ? '禁用' : '启用' }),
          h(NButton, {
            size: 'small',
            type: 'primary',
            onClick: () => openRoleModal(row),
          }, { default: () => '分配角色' }),
        ],
      })
    },
  },
]

function search() {
  query.page = 1
  loadUsers()
}

function reset() {
  query.keyword = ''
  query.status = null
  query.page = 1
  loadUsers()
}

async function loadUsers() {
  loading.value = true
  try {
    const res = await getAdminUsers({ page: query.page, size: query.size, keyword: query.keyword || undefined, status: query.status ?? undefined })
    if (res.code === 200) {
      users.value = res.data.records
      pagination.page = res.data.page
      pagination.pageCount = Math.ceil(res.data.total / res.data.size)
    }
  } catch (e) {
    message.error('加载用户列表失败')
  } finally {
    loading.value = false
  }
}

function handlePageChange(page: number) {
  query.page = page
  loadUsers()
}

function toggleStatus(row: SysUser) {
  const newStatus = row.status === 0 ? 1 : 0
  const actionText = newStatus === 1 ? '禁用' : '启用'
  dialog.warning({
    title: '确认操作',
    content: `确定要${actionText}用户 "${row.username}" 吗？`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        const res = await changeUserStatus(row.id, newStatus)
        if (res.code === 200) {
          message.success(res.message || `${actionText}成功`)
          loadUsers()
        }
      } catch (e) {
        message.error('操作失败')
      }
    },
  })
}

async function openRoleModal(row: SysUser) {
  currentUser.value = row
  selectedRoleIds.value = []
  try {
    const rolesRes = await getRoles()
    if (rolesRes.code === 200) {
      roles.value = rolesRes.data
      // 预选当前用户的角色
      const userRoles = row.roles || []
      selectedRoleIds.value = roles.value.filter((r) => userRoles.includes(r.roleCode)).map((r) => r.id)
    }
    showRoleModal.value = true
  } catch (e) {
    message.error('加载角色列表失败')
  }
}

async function saveRoles() {
  if (!currentUser.value) return
  roleSaving.value = true
  try {
    const res = await assignUserRoles(currentUser.value.id, selectedRoleIds.value)
    if (res.code === 200) {
      message.success('角色分配成功')
      showRoleModal.value = false
      loadUsers()
    }
  } catch (e) {
    message.error('角色分配失败')
  } finally {
    roleSaving.value = false
  }
}

onMounted(loadUsers)
</script>
