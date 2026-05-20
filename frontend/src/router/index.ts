import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', redirect: '/dashboard' },
    { path: '/login', name: 'Login', component: () => import('../views/LoginView.vue') },
    { path: '/register', name: 'Register', component: () => import('../views/RegisterView.vue') },
    { path: '/dashboard', name: 'Dashboard', component: () => import('../views/DashboardView.vue'), meta: { requiresAuth: true } },
    { path: '/workspace/:id', name: 'Workspace', component: () => import('../views/WorkspaceView.vue'), meta: { requiresAuth: true } },
    { path: '/history', name: 'History', component: () => import('../views/HistoryView.vue'), meta: { requiresAuth: true } },
  ],
})

router.beforeEach((to, _from, next) => {
  if (to.meta.requiresAuth && !localStorage.getItem('userId')) {
    next('/login')
  } else {
    next()
  }
})

export default router
