export const BASE_URL = import.meta.env.VITE_BASE_URL

export const API_PREFIX = import.meta.env.VITE_PREFIX_API

export const API_URL = BASE_URL + API_PREFIX

export const GLOBAL_ROUTE_NAMES = {
  SWITCH_ROLE: 'switch_role',
  ERROR_403_PAGE: 'error_403_page',
  ERROR_404_PAGE: 'error_404_page',
  ADMIN_PAGE: 'admin_page',
  STAFF_PAGE: 'staff_page',
  TEACHER_PAGE: 'teacher_page',
  STUDENT_PAGE: 'student_page',
}
