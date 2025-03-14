import { createRouter, createWebHistory } from 'vue-router'
import { AuthenticationRoutes, ROUTE_NAMES as RouteNameAuth } from './authenticationRoute'
import { AdminRoutes } from './adminRoute'
import { StaffRoutes } from './staffRoute'
import { TeacherRoutes } from './teacherRoute'
import { StudentRoutes } from './studentRoute'
import { GLOBAL_ROUTE_NAMES } from '@/constants/routesConstant'
import useAuthStore from '@/stores/useAuthStore'
import useBreadcrumbStore from '@/stores/useBreadCrumbStore'
import useApplicationStore from '@/stores/useApplicationStore'

const routes = [
  {
    path: '/',
    name: GLOBAL_ROUTE_NAMES.SWITCH_ROLE,
    component: () => import('@/views/pages/authentication/AuthenticationLandingPage.vue'),
    meta: {
      title: 'Thay đổi vai trò',
    },
  },
  { path: '/:pathMatch(.*)*', component: () => import('@/views/pages/errors/Error404Page.vue') },
  {
    path: '/error/403',
    name: GLOBAL_ROUTE_NAMES.ERROR_403_PAGE,
    component: () => import('@/views/pages/errors/Error403Page.vue'),
  },
  ...AuthenticationRoutes,
  ...AdminRoutes,
  ...StaffRoutes,
  ...TeacherRoutes,
  ...StudentRoutes,
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
})

router.beforeEach((to, from, next) => {
  document.title = to.meta?.name ? to.meta.title + ' - ' + to.meta.name : to.meta.title || ''

  const applicationStore = useApplicationStore()

  if (to.meta?.selectedKey > 0) {
    applicationStore.setSelectedKeys(to.meta.selectedKey)
  }

  const authStore = useAuthStore()

  const breadcrumbStore = useBreadcrumbStore()

  breadcrumbStore.clear()

  const requireRole = to.meta.requireRole || null

  const user = authStore.user || null

  if (!user && requireRole) {
    return next({ name: RouteNameAuth.LOGIN_PAGE })
  }

  if (requireRole && !user.role.includes(requireRole.toUpperCase())) {
    return next({ name: GLOBAL_ROUTE_NAMES.ERROR_403_PAGE })
  }

  next()
})

export default router
