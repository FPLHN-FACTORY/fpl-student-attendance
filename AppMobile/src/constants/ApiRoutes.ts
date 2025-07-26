import { API_URL } from '.'

export const API_ROUTES = {
  FETCH_DATA_REFRESH_TOKEN: API_URL + '/refresh-token',
  FETCH_DATA_FACILITY: API_URL + '/get-all-facility',
  FETCH_DATA_SEMESTER: API_URL + '/get-all-semester',
  FETCH_DATA_SETTINGS: API_URL + '/settings',
  FETCH_DATA_REGISTER: API_URL + '/student-register',
  FETCH_DATA_STUDENT_UPDATE_FACEID: API_URL + '/student-update-face-id',
  FETCH_DATA_STUDENT_INFO: API_URL + '/student-info',
  FETCH_DATA_STUDENT_HISTORY: API_URL + '/student/attendance-history',
  FETCH_DATA_STUDENT_HISTORY_FACTORIES: API_URL + '/student/attendance-history/factories',
  FETCH_DATA_STUDENT_CALENDAR: API_URL + '/student/plan-attendance/list',
  FETCH_DATA_STUDENT_ATTENDANCE: API_URL + '/student/attendance',
  FETCH_DATA_STUDENT_STATISTICS: API_URL + '/student/statistics',
}
