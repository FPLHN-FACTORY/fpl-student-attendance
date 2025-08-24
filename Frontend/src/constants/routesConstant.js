export const BASE_URL = import.meta.env.VITE_BASE_URL

export const BASE_API_URL = import.meta.env.VITE_BASE_API_URL

export const API_PREFIX = import.meta.env.VITE_PREFIX_API

export const PREFIX_ADMIN_PANEL = import.meta.env.VITE_PREFIX_ADMIN_PANEL

export const URL_ADMIN_PANEL = BASE_URL + PREFIX_ADMIN_PANEL

export const API_URL = BASE_API_URL + API_PREFIX

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

export const WS_ROUTES = {
  SERVER_HOST:
    (BASE_API_URL.startsWith('https://')
      ? BASE_API_URL.replace(/^https:\/\//, 'wss://')
      : BASE_API_URL.replace(/^http:\/\//, 'ws://')) + '/ws',
  TOPIC_ATTENDANCE: '/topic/attendance',
}

export const API_ROUTES_EXCEL = {
  FETCH_IMPORT_PLAN_DATE: API_URL + '/excel/plan-date',
  FETCH_IMPORT_STAFF: API_URL + '/excel/staff',
  FETCH_IMPORT_FACTORY: API_URL + '/excel/factory',
  FETCH_IMPORT_STUDENT: API_URL + '/excel/student',
  FETCH_IMPORT_STUDENT_FACTORY: API_URL + '/excel/student-factory',
  FETCH_IMPORT_PROJECT: API_URL + '/excel/project',
  FETCH_IMPORT_ATTENDANCE_RECOVERY: API_URL + '/excel/attendance-recovery',
}

export const API_ROUTES_NOTIFICATION = {
  FETCH_COUNT: API_URL + '/notification/count',
  FETCH_LIST: API_URL + '/notification/list',
  FETCH_DELETE: API_URL + '/notification/delete',
  FETCH_DELETE_ALL: API_URL + '/notification/delete-all',
  FETCH_MARK_READ: API_URL + '/notification/mark-read',
  FETCH_MARK_UNREAD: API_URL + '/notification/mark-unread',
  FETCH_MARK_READ_ALL: API_URL + '/notification/mark-read-all',
}

export const API_ROUTES_SUPPORT = {
  FETCH_SEND_SUPPORT: API_URL + '/support/send-mail',
}
