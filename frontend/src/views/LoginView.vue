<!-- 登录页 -->
<template>
  <n-layout class="auth-layout">
    <div class="auth-container">
      <div class="auth-header">
        <n-icon size="48" color="#2080f0">
          <CreateOutline />
        </n-icon>
        <h1>AI 内容创作工作台</h1>
        <p>登录后开始创建你的爆款内容</p>
      </div>

      <n-card class="auth-card" :bordered="false">
        <n-form ref="formRef" :model="form" :rules="rules">
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
              placeholder="请输入密码"
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
            @click="handleLogin"
            :loading="loading"
            class="login-btn"
          >
            登录
          </n-button>
        </n-form>

        <div class="auth-footer">
          <n-text depth="3">没有账号？</n-text>
          <router-link to="/register" class="auth-link">立即注册</router-link>
        </div>
      </n-card>
    </div>
  </n-layout>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import { CreateOutline, MailOutline, LockClosedOutline } from '@vicons/ionicons5'
import { useAuthStore } from '../stores/auth'
import type { FormRules } from 'naive-ui'
import '../styles/auth.css'

const form = reactive({ email: '', password: '' })
const loading = ref(false)
const router = useRouter()
const auth = useAuthStore()
const message = useMessage()

const rules: FormRules = {
  email: [
    { required: true, message: '请输入邮箱' },
    { type: 'email', message: '邮箱格式不正确' }
  ],
  password: [
    { required: true, message: '请输入密码' },
    { min: 6, message: '密码至少6位' }
  ],
}

async function handleLogin() {
  loading.value = true
  try {
    await auth.doLogin(form.email, form.password)
    message.success('登录成功')
    router.push('/dashboard')
  } catch (e: any) {
    message.error(e.response?.data?.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-btn {
  margin-top: 8px;
  height: 48px;
  font-size: 16px;
}
</style>
