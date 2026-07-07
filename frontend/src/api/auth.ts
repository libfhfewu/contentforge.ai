/** 认证 API：登录、注册、登出、会话检查 */
import client from './client'
import type { ApiResponse, User } from '../types'

export function login(email: string, password: string) {
  return client.post<ApiResponse<User>>('/auth/login', { email, password })
}

export function register(username: string, email: string, password: string) {
  return client.post<ApiResponse<User>>('/auth/register', { username, email, password })
}

export function logout() {
  return client.post<ApiResponse<null>>('/auth/logout')
}

export function checkAuth() {
  return client.get<ApiResponse<{ authenticated: boolean; userId: number; username: string }>>('/auth/check')
}
