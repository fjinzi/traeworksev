import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/store/user'

const routes = [
  {
    path: '/',
    name: 'Seckill',
    component: () => import('@/views/SeckillPage.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/admin',
    name: 'Admin',
    component: () => import('@/views/ProductAdmin.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/admin/users',
    name: 'UserAdmin',
    component: () => import('@/views/UserAdmin.vue'),
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/AuthPage.vue'),
    meta: { requiresAuth: false }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(async (to, from, next) => {
  const userStore = useUserStore()
  
  if (userStore.token && !userStore.user) {
    await userStore.initUser()
  }

  if (to.meta.requiresAuth) {
    if (!userStore.isLoggedIn) {
      next({ name: 'Login', query: { redirect: to.fullPath } })
      return
    }
  }

  if (to.meta.requiresAdmin && userStore.user?.roleType !== 1) {
    next({ name: 'Seckill' })
    return
  }

  if (to.name === 'Login' && userStore.isLoggedIn) {
    next({ name: 'Seckill' })
    return
  }

  next()
})

export default router
