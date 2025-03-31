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
  STUDENT_REGISTER_PAGE: 'student_register_page',
}

export const API_ROUTES_EXCEL = {
  FETCH_IMPORT_PLAN_DATE: API_URL + '/excel/plan-date',
  FETCH_IMPORT_STAFF: API_URL + '/excel/staff',
  FETCH_IMPORT_FACTORY: API_URL + '/excel/factory'
}
