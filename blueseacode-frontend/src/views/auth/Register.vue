<template>
  <div class="auth-page">
    <n-card class="auth-card" title="注册">
      <n-form ref="formRef" :model="formData" :rules="rules" size="large">
        <n-form-item label="用户名" path="username">
          <n-input v-model:value="formData.username" placeholder="请输入用户名" />
        </n-form-item>
        <n-form-item label="密码" path="password">
          <n-input
            v-model:value="formData.password"
            type="password"
            placeholder="请输入密码"
            show-password-on="click"
          />
        </n-form-item>
        <n-form-item label="昵称" path="nickname">
          <n-input v-model:value="formData.nickname" placeholder="选填" />
        </n-form-item>
        <n-form-item label="邮箱" path="email">
          <n-input v-model:value="formData.email" placeholder="选填" />
        </n-form-item>
        <n-form-item>
          <n-button type="primary" block :loading="loading" @click="handleRegister">
            注册
          </n-button>
        </n-form-item>
      </n-form>
      <div class="auth-link">
        已有账号？<router-link to="/login">去登录</router-link>
      </div>
    </n-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import { useAuthStore } from '@/stores/auth'
import type { FormInst, FormRules } from 'naive-ui'

const router = useRouter()
const message = useMessage()
const authStore = useAuthStore()
const formRef = ref<FormInst | null>(null)
const loading = ref(false)

const formData = reactive({
  username: '',
  password: '',
  nickname: '',
  email: '',
})

const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名' }],
  password: [{ required: true, message: '请输入密码' }, { min: 6, message: '密码至少6位' }],
}

async function handleRegister() {
  loading.value = true
  try {
    const res = await authStore.register(formData)
    if (res.code === 200) {
      message.success('注册成功，请登录')
      router.push('/login')
    } else {
      message.error(res.msg || '注册失败')
    }
  } catch {
    message.error('网络错误，请稍后重试')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: calc(100vh - 64px - 48px);
}
.auth-card {
  width: 400px;
}
.auth-link {
  text-align: center;
  margin-top: 16px;
  color: #666;
}
.auth-link a {
  color: #18a058;
}
</style>
