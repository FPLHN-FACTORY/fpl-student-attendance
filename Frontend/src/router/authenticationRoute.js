import { ROUTE_NAMES } from '@/constants/authenticationConstant'

export const AuthenticationRoutes = [
  {
    path: '/',
    name: ROUTE_NAMES.LOGIN_PAGE,
    component: import('@/views/pages/authentication/AuthenticationLandingPage.vue'),
    meta: {
      title: 'Đăng nhập hệ thống',
    },
  },
]
