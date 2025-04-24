import { ROLE } from '@/constants'
import { API_URL, GLOBAL_ROUTE_NAMES } from '@/constants/routesConstant'

export const ROUTE_NAMES_API = {
  FETCH_DATA_FACILITY: API_URL + '/get-all-facility',
  FETCH_DATA_AVATAR: API_URL + '/get-avatar',
  FETCH_DATA_INFO_USER: API_URL + '/get-info-user',
  FETCH_DATA_REGISTER: API_URL + '/student-register',
  FETCH_DATA_UPDATE_FACEID: API_URL + '/student-update-face-id',
  FETCH_DATA_STUDENT_INFO: API_URL + '/student-info',
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
  {
    path: '/student-register',
    name: GLOBAL_ROUTE_NAMES.STUDENT_REGISTER_PAGE,
    component: () => import('@/views/pages/authentication/AuthenticationStudentRegisterPage.vue'),
    meta: {
      title: 'Đăng ký tài khoản sinh viên',
      requireRole: ROLE.STUDENT,
    },
  },
]
