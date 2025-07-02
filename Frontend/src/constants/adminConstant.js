import { API_URL } from '@/constants/routesConstant'

export const API_ROUTES_ADMIN = {
  FETCH_DATA_FACILITY: API_URL + '/admin/facilities',

  FETCH_DATA_SETTINGS: API_URL + '/admin/settings',

  FETCH_DATA_SHIFT: API_URL + '/admin/shift',

  FETCH_DATA_PROJECT: API_URL + '/admin/project-management',

  FETCH_DATA_SEMESTER: API_URL + '/admin/semesters',

  FETCH_DATA_SUBJECT: API_URL + '/admin/subject-management',

  FETCH_DATA_SUBJECT_FACILITY: API_URL + '/admin/subject-facility-management',

  FETCH_DATA_LEVEL_PROJECT: API_URL + '/admin/level-project-management',

  FETCH_DATA_STAFF: API_URL + '/admin/staff-management',

  FETCH_DATA_ADMIN: API_URL + '/admin/admin-management',

  FETCH_DATA_USER_LOG_ACTIVITY: API_URL + '/admin/user-activity-management',

  FETCH_DATA_STATISTICS: API_URL + '/admin/statistics-management',
}
