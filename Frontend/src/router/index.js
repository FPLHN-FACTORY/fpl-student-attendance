import { createRouter, createWebHistory } from 'vue-router'
import { AuthenticationRoutes, ROUTE_NAMES as RouteNameAuth } from './authenticationRoute'
import { AdminRoutes } from './adminRoute'
import { StaffRoutes } from './staffRoute'
import { TeacherRoutes } from './teacherRoute'
import { StudentRoutes } from './studentRoute'
import { GLOBAL_ROUTE_NAMES, PREFIX_ADMIN_PANEL } from '@/constants/routesConstant'
import useAuthStore from '@/stores/useAuthStore'
import useBreadcrumbStore from '@/stores/useBreadCrumbStore'
import useApplicationStore from '@/stores/useApplicationStore'

const routes = [
  {
    path: '/test',
    component: () => import('@/views/pages/Test.vue'),
    meta: {
      title: 'Đăng nhập hệ thống',
    },
  },
  {
    path: '/',
    component: () => import('@/views/pages/authentication/AuthenticationLandingPage.vue'),
    meta: {
      title: 'Đăng nhập hệ thống',
    },
  },
  {
    path: PREFIX_ADMIN_PANEL,
    name: GLOBAL_ROUTE_NAMES.SWITCH_ROLE,
    component: () => import('@/views/pages/authentication/AuthenticationLandingPage.vue'),
    meta: {
      title: 'Admin panel',
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
  history: createWebHistory(),
  routes,
  scrollBehavior(to, from, savedPosition) {
    if (to.hash) {
      return {
        el: to.hash,
        behavior: 'smooth',
      }
    }
    return {
      left: 0,
      top: 0,
      behavior: 'smooth',
    }
  },
})

router.beforeEach((to, from, next) => {
  document.title =
    (to.meta?.name ? to.meta.title + ' - ' + to.meta.name : to.meta.title || '') +
    ' | ' +
    import.meta.env.VITE_APP_NAME

  const applicationStore = useApplicationStore()

  if (to.meta?.selectedKey > 0) {
    applicationStore.setSelectedKeys(to.meta.selectedKey)
  }

  const authStore = useAuthStore()

  const breadcrumbStore = useBreadcrumbStore()

  breadcrumbStore.clear()

  const requireAuth = to.meta.requireAuth || null

  const requireRole = to.meta.requireRole || null

  const user = authStore.user || null

  if ((requireAuth && !user) || (!user && requireRole)) {
    return next({ path: '/' })
  }

  if (requireRole && !user.role.includes(requireRole.toUpperCase())) {
    return next({ name: GLOBAL_ROUTE_NAMES.ERROR_403_PAGE })
  }

  next()
})

export default router
