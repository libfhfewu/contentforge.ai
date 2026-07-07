/** Axios 实例：baseURL=/api，自动携带 Cookie 和 CSRF Token，401 时跳转登录页 */
import axios from 'axios'

const client = axios.create({
  baseURL: '/api',
  withCredentials: true,
})

/**
 * 请求拦截器：自动添加 CSRF Token 到请求头
 */
client.interceptors.request.use((config) => {
  // 从 Cookie 中读取 XSRF-TOKEN
  const csrfToken = document.cookie
    .split('; ')
    .find(row => row.startsWith('XSRF-TOKEN='))
    ?.split('=')[1]

  if (csrfToken) {
    config.headers['X-XSRF-TOKEN'] = csrfToken
  }
  return config
})

/**
 * 响应拦截器：401 时清除本地状态并跳转登录页
 */
client.interceptors.response.use(
  (res) => res,
  (err) => {
    if (err.response?.status === 401) {
      localStorage.removeItem('userId')
      window.location.href = '/login'
    }
    return Promise.reject(err)
  }
)

export default client
