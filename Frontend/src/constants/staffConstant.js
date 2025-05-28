import { API_URL } from '@/constants/routesConstant'

export const API_ROUTES_STAFF = {
  FETCH_DATA_FACTORY: API_URL + '/staff/factory-management',

  FETCH_DATA_STUDENT_FACTORY: API_URL + '/staff/factory-management/student',

  FETCH_DATA_PROJECT: API_URL + '/staff/project-management',

  FETCH_DATA_STUDENT: API_URL + '/staff/student-management',

  FETCH_DATA_PLAN: API_URL + '/staff/plan-management',

  FETCH_DATA_PLAN_FACTORY: API_URL + '/staff/plan-factory-management',

  FETCH_DATA_PLAN_DATE: API_URL + '/staff/plan-date-management',

  FETCH_DATA_STATISTICS: API_URL + '/staff/statistics-management',

  FETCH_DATA_PLAN_DATE_ATTENDANCE: API_URL + '/staff/plan-date-attendance-management',
}
