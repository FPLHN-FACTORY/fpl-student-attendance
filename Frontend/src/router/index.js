import { createRouter, createWebHistory } from 'vue-router'
import { AuthenticationRoutes, ROUTE_NAMES as RouteNameAuth } from './authenticationRoute'
import { AdminRoutes } from './adminRoute'
import { StaffRoutes } from './staffRoute'
import { StudentRoutes } from './studentRoute'
import useAuthStore from '@/stores/useAuthStore'
import { GLOBAL_ROUTE_NAMES } from '@/constants/routesConstant'

const routes = [
  { path: '/:pathMatch(.*)*', component: import('@/views/pages/errors/Error404Page.vue') },
  {
    path: '/error/403',
    name: GLOBAL_ROUTE_NAMES.ERROR_403_PAGE,
    component: import('@/views/pages/errors/Error403Page.vue'),
  },
  ...AuthenticationRoutes,
  ...AdminRoutes,
  ...StaffRoutes,
  ...StudentRoutes,
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
})

router.beforeEach((to, from, next) => {
  document.title = to.meta.title || ''

  const authStore = useAuthStore()

  const requireRole = to.meta.requireRole || null

  const user = authStore.user || null

  if (!user && requireRole) {
    return next({ name: RouteNameAuth.LOGIN_PAGE })
  }

  if (requireRole && requireRole.toLowerCase() !== user.role.toLowerCase()) {
    return next({ name: GLOBAL_ROUTE_NAMES.ERROR_403_PAGE })
  }

  next()
})

export default router
