<!-- 注册页 -->
<template>
  <n-layout class="auth-layout">
    <div class="auth-container">
      <div class="auth-header">
        <n-icon size="48" color="#2080f0">
          <CreateOutline />
        </n-icon>
        <h1>AI 内容创作工作台</h1>
        <p>注册账号，开始你的内容创作之旅</p>
      </div>

      <n-card class="auth-card" :bordered="false">
        <n-form ref="formRef" :model="form" :rules="rules">
          <n-form-item path="username" label="用户名">
            <n-input
              v-model:value="form.username"
              placeholder="请输入用户名"
              size="large"
            >
              <template #prefix>
                <n-icon><PersonOutline /></n-icon>
              </template>
            </n-input>
          </n-form-item>

          <n-form-item path="email" label="邮箱">
            <n-input
              v-model:value="form.email"
              placeholder="请输入邮箱地址"
              size="large"
            >
              <template #prefix>
                <n-icon><MailOutline /></n-icon>
              </template>
            </n-input>
          </n-form-item>

          <n-form-item path="password" label="密码">
            <n-input
              v-model:value="form.password"
              type="password"
              placeholder="请输入密码（至少6位）"
              size="large"
              show-password-on="click"
            >
              <template #prefix>
                <n-icon><LockClosedOutline /></n-icon>
              </template>
            </n-input>
          </n-form-item>

          <n-form-item path="confirmPassword" label="确认密码">
            <n-input
              v-model:value="form.confirmPassword"
              type="password"
              placeholder="请再次输入密码"
              size="large"
              show-password-on="click"
            >
              <template #prefix>
                <n-icon><LockClosedOutline /></n-icon>
              </template>
            </n-input>
          </n-form-item>

          <n-button
            type="primary"
            block
            size="large"
            @click="handleRegister"
            :loading="loading"
            class="register-btn"
          >
            注册
          </n-button>
        </n-form>

        <div class="auth-footer">
          <n-text depth="3">已有账号？</n-text>
          <router-link to="/login" class="auth-link">立即登录</router-link>
        </div>
      </n-card>
    </div>
  </n-layout>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import {
  CreateOutline,
  PersonOutline,
  MailOutline,
  LockClosedOutline
} from '@vicons/ionicons5'
import { useAuthStore } from '../stores/auth'
import type { FormRules } from 'naive-ui'
import '../styles/auth.css'

const form = reactive({
  username: '',
  email: '',
  password: '',
  confirmPassword: ''
})
const loading = ref(false)
const router = useRouter()
const auth = useAuthStore()
const message = useMessage()

const rules: FormRules = {
  username: [
    { required: true, message: '请输入用户名' },
    { min: 2, max: 20, message: '用户名长度2-20位' }
  ],
  email: [
    { required: true, message: '请输入邮箱' },
    { type: 'email', message: '邮箱格式不正确' }
  ],
  password: [
    { required: true, message: '请输入密码' },
    { min: 6, message: '密码至少6位' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码' },
    {
      validator: (_rule: any, value: string) => {
        return value === form.password
      },
      message: '两次密码不一致',
      trigger: 'input'
    }
  ],
}

async function handleRegister() {
  if (form.password !== form.confirmPassword) {
    message.error('两次密码不一致')
    return
  }

  loading.value = true
  try {
    await auth.doRegister(form.username, form.email, form.password)
    message.success('注册成功，请登录')
    router.push('/login')
  } catch (e: any) {
    message.error(e.response?.data?.message || '注册失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-btn {
  margin-top: 8px;
  height: 48px;
  font-size: 16px;
}
</style>
