<template>
  <div class="system-config">
    <h2 style="margin-bottom: 20px">系统配置</h2>

    <n-card>
      <n-data-table :columns="columns" :data="list" :loading="loading" />
      <template #footer>
        <n-space justify="flex-end">
          <n-button type="primary" @click="saveAll" :loading="saving">保存全部</n-button>
          <n-button @click="refresh">刷新</n-button>
        </n-space>
      </template>
    </n-card>
  </div>
</template>

<script setup lang="ts">
import { ref, h, onMounted } from 'vue'
import { NButton, NInput, NSpace, useMessage } from 'naive-ui'
import { getConfigs, updateConfigs, type SysConfig } from '@/api/admin'

const message = useMessage()
const loading = ref(false)
const saving = ref(false)
const list = ref<SysConfig[]>([])

const columns = [
  { title: 'ID', key: 'id', width: 60 },
  { title: '配置键', key: 'configKey', width: 200 },
  {
    title: '配置值',
    key: 'configValue',
    render(row: SysConfig) {
      return h(NInput, {
        value: row.configValue,
        onUpdateValue: (val: string) => { row.configValue = val },
      })
    },
  },
  { title: '描述', key: 'description', ellipsis: true },
]

async function loadData() {
  loading.value = true
  try {
    const res = await getConfigs()
    if (res.code === 200) list.value = res.data
  } catch { message.error('加载失败') }
  finally { loading.value = false }
}

async function saveAll() {
  saving.value = true
  try {
    const res = await updateConfigs(list.value)
    if (res.code === 200) message.success('保存成功')
  } catch { message.error('保存失败') }
  finally { saving.value = false }
}

function refresh() { loadData() }

onMounted(loadData)
</script>
