import { API_URL } from '@/constants/routesConstant'

export const API_ROUTES_TEACHER = {
  FETCH_DATA_FACTORY: API_URL + '/teacher/factory-management',

  FETCH_DATA_STUDENT_FACTORY: API_URL + '/teacher/factory-management/student',

  FETCH_DATA_SHIFT_FACTORY: API_URL + '/teacher/factory-management/shift',

  FETCH_DATA_PLAN_DATE_ATTENDANCE: API_URL + '/teacher/factory-management/attendance',

  FETCH_DATA_SCHEDULE: API_URL + '/teacher/teaching-schedule',

  FETCH_DATA_STUDENT_PLAN_DATE: API_URL + '/teacher/student-attendance',
}
