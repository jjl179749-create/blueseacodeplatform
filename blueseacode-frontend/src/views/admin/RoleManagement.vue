<template>
  <div class="role-management">
    <h2 style="margin-bottom: 20px">角色权限管理</h2>

    <n-grid :cols="2" :x-gap="16">
      <!-- 角色列表 -->
      <n-gi>
        <n-card title="角色列表" style="margin-bottom: 16px">
          <template #header-extra>
            <n-button size="small" type="primary" @click="openCreateRole">新增角色</n-button>
          </template>
          <n-data-table :columns="roleColumns" :data="roles" :loading="rolesLoading" :pagination="false" :bordered="false" />
        </n-card>
      </n-gi>

      <!-- 权限分配 -->
      <n-gi>
        <n-card title="权限分配" style="margin-bottom: 16px">
          <template v-if="selectedRole">
            <p style="margin-bottom: 12px; color: #666">为角色「{{ selectedRole.roleName }}」分配权限</p>
            <n-tree
              :data="permissionTree"
              :checked-keys="checkedPermissionIds"
              key-field="id"
              label-field="name"
              children-field="children"
              checkable
              default-expand-all
              :selectable="false"
              @update:checked-keys="onPermissionChange"
            />
            <n-button type="primary" style="margin-top: 16px" @click="savePermissions" :loading="permSaving">保存权限</n-button>
          </template>
          <n-empty v-else description="请先选择一个角色" />
        </n-card>
      </n-gi>
    </n-grid>

    <!-- 角色编辑弹窗 -->
    <n-modal v-model:show="showRoleModal" :title="editingRole ? '编辑角色' : '新增角色'" preset="card" style="width: 500px" :mask-closable="false">
      <n-form :model="roleForm" label-placement="top">
        <n-form-item label="角色编码">
          <n-input v-model:value="roleForm.roleCode" placeholder="如：ADMIN, OPERATOR" />
        </n-form-item>
        <n-form-item label="角色名称">
          <n-input v-model:value="roleForm.roleName" placeholder="如：管理员" />
        </n-form-item>
        <n-form-item label="描述">
          <n-input v-model:value="roleForm.description" type="textarea" :rows="3" />
        </n-form-item>
        <n-form-item label="排序">
          <n-input-number v-model:value="roleForm.sort" :min="0" />
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space>
          <n-button type="primary" @click="saveRole" :loading="roleSaving">保存</n-button>
          <n-button @click="showRoleModal = false">取消</n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, h, onMounted } from 'vue'
import { NButton, NSpace, NTag, useMessage, useDialog } from 'naive-ui'
import {
  getRoles,
  createRole,
  updateRole,
  deleteRole,
  getPermissionTree,
  getRolePermissions,
  assignRolePermissions,
  type SysRole,
  type SysPermission,
} from '@/api/admin'

const message = useMessage()
const dialog = useDialog()
const roles = ref<SysRole[]>([])
const rolesLoading = ref(false)
const selectedRole = ref<SysRole | null>(null)
const permissionTree = ref<any[]>([])
const checkedPermissionIds = ref<number[]>([])
const permSaving = ref(false)
const showRoleModal = ref(false)
const editingRole = ref<SysRole | null>(null)
const roleSaving = ref(false)
const roleForm = reactive({ roleCode: '', roleName: '', description: '', sort: 0 })

const roleColumns = [
  { title: '编码', key: 'roleCode', width: 100 },
  { title: '名称', key: 'roleName', width: 100 },
  { title: '描述', key: 'description', ellipsis: true },
  {
    title: '操作', key: 'actions', width: 180,
    render(row: SysRole) {
      return h(NSpace, null, {
        default: () => [
          h(NButton, {
            size: 'small',
            type: selectedRole.value?.id === row.id ? 'primary' : 'default',
            onClick: () => selectRole(row),
          }, { default: () => '分配权限' }),
          h(NButton, { size: 'small', onClick: () => openEditRole(row) }, { default: () => '编辑' }),
          h(NButton, { size: 'small', type: 'error', onClick: () => doDeleteRole(row) }, { default: () => '删除' }),
        ],
      })
    },
  },
]

async function loadRoles() {
  rolesLoading.value = true
  try {
    const res = await getRoles()
    if (res.code === 200) roles.value = res.data
  } catch { message.error('加载角色失败') }
  finally { rolesLoading.value = false }
}

async function loadPermissions() {
  try {
    const res = await getPermissionTree()
    if (res.code === 200) permissionTree.value = res.data
  } catch { message.error('加载权限树失败') }
}

async function selectRole(role: SysRole) {
  selectedRole.value = role
  try {
    const res = await getRolePermissions(role.id)
    if (res.code === 200) checkedPermissionIds.value = res.data
  } catch { message.error('加载角色权限失败') }
}

function onPermissionChange(keys: (string | number)[]) {
  checkedPermissionIds.value = keys as number[]
}

async function savePermissions() {
  if (!selectedRole.value) return
  permSaving.value = true
  try {
    const res = await assignRolePermissions(selectedRole.value.id, checkedPermissionIds.value)
    if (res.code === 200) message.success('权限保存成功')
  } catch { message.error('保存权限失败') }
  finally { permSaving.value = false }
}

function openCreateRole() {
  editingRole.value = null
  roleForm.roleCode = ''; roleForm.roleName = ''; roleForm.description = ''; roleForm.sort = 0
  showRoleModal.value = true
}

function openEditRole(role: SysRole) {
  editingRole.value = role
  roleForm.roleCode = role.roleCode
  roleForm.roleName = role.roleName
  roleForm.description = role.description || ''
  roleForm.sort = role.sort
  showRoleModal.value = true
}

async function saveRole() {
  if (!roleForm.roleCode || !roleForm.roleName) { message.warning('请填写编码和名称'); return }
  roleSaving.value = true
  try {
    const payload = { roleCode: roleForm.roleCode, roleName: roleForm.roleName, description: roleForm.description || null, sort: roleForm.sort }
    const res = editingRole.value
      ? await updateRole(editingRole.value.id, payload)
      : await createRole(payload)
    if (res.code === 200) {
      message.success(editingRole.value ? '更新成功' : '创建成功')
      showRoleModal.value = false
      loadRoles()
    }
  } catch { message.error('保存失败') }
  finally { roleSaving.value = false }
}

function doDeleteRole(role: SysRole) {
  dialog.warning({
    title: '确认删除',
    content: `确定要删除角色 "${role.roleName}" 吗？`,
    positiveText: '确定', negativeText: '取消',
    onPositiveClick: async () => {
      try {
        const res = await deleteRole(role.id)
        if (res.code === 200) {
          message.success('删除成功')
          if (selectedRole.value?.id === role.id) selectedRole.value = null
          loadRoles()
        }
      } catch { message.error('删除失败') }
    },
  })
}

onMounted(() => {
  loadRoles()
  loadPermissions()
})
</script>
