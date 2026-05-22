/**
 * Configured Axios instance with base URL, credentials, and 401 redirect handling.
 */
import axios from 'axios'

const client = axios.create({
  baseURL: '/api',
  withCredentials: true,
})

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
