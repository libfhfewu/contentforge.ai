/**
 * Login, register, and logout API call wrappers.
 */
import client from './client'

export function login(email: string, password: string) {
  return client.post('/auth/login', { email, password })
}

export function register(username: string, email: string, password: string) {
  return client.post('/auth/register', { username, email, password })
}

export function logout() {
  return client.post('/auth/logout')
}
