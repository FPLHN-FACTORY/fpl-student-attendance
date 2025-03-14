import { API_URL } from '@/constants/routesConstant'

export const ROUTE_NAMES_API = {
  FETCH_DATA_FACILITY: API_URL + '/get-all-facility',
  FETCH_DATA_INFO_USER: API_URL + '/get-info-user',
}

export const ROUTE_NAMES = {
  LOGIN_PAGE: 'at_login_page',
  LOGOUT_PAGE: 'at_logout_page',
}

export const AuthenticationRoutes = [
  {
    path: '/',
    name: ROUTE_NAMES.LOGIN_PAGE,
    component: () => import('@/views/pages/authentication/AuthenticationLandingPage.vue'),
    meta: {
      title: 'Đăng nhập hệ thống',
    },
  },
]
