/** Vue Router 配置 */
import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', redirect: '/dashboard' },
    { path: '/login', name: 'Login', component: () => import('../views/LoginView.vue') },
    { path: '/register', name: 'Register', component: () => import('../views/RegisterView.vue') },
    { path: '/dashboard', name: 'Dashboard', component: () => import('../views/DashboardView.vue'), meta: { requiresAuth: true } },
    { path: '/workspace/:id', name: 'Workspace', component: () => import('../views/WorkspaceView.vue'), meta: { requiresAuth: true } },
    { path: '/history', name: 'History', component: () => import('../views/HistoryView.vue'), meta: { requiresAuth: true } },

    // 新增页面
    { path: '/models', name: 'Models', component: () => import('../views/ModelView.vue'), meta: { requiresAuth: true } },
    { path: '/workflows', name: 'Workflows', component: () => import('../views/WorkflowView.vue'), meta: { requiresAuth: true } },
    { path: '/templates', name: 'Templates', component: () => import('../views/TemplateView.vue'), meta: { requiresAuth: true } },
    { path: '/workspace/:id/versions', name: 'Versions', component: () => import('../views/VersionHistoryView.vue'), meta: { requiresAuth: true } },
  ],
})

router.beforeEach(async (to, _from, next) => {
  if (!to.meta.requiresAuth) {
    next()
    return
  }

  if (!localStorage.getItem('userId')) {
    next('/login')
    return
  }

  const auth = useAuthStore()

  // 如果已认证，直接放行
  if (auth.isAuthenticated) {
    next()
    return
  }

  // 尝试恢复认证状态（使用缓存的 checkAuth 结果）
  await auth.initAuth()

  if (auth.isAuthenticated) {
    next()
  } else {
    localStorage.removeItem('userId')
    next('/login')
  }
})

export default router
