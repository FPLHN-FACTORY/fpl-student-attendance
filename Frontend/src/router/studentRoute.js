import { ROUTE_NAMES } from '@/constants/studentConstant'
import { ROLE } from '@/constants/roleConstant'
import { GLOBAL_ROUTE_NAMES } from '@/constants/routesConstant'

export const StudentRoutes = [
  {
    path: '/Student',
    name: GLOBAL_ROUTE_NAMES.STUDENT_PAGE,
    component: import('@/views/layout/StudentLayout.vue'),
    redirect: { name: ROUTE_NAMES.ATTENDANCE },
    meta: {
      title: 'Sinh viên',
      requireRole: ROLE.STUDENT,
    },
    children: [
      {
        path: 'attendance',
        name: ROUTE_NAMES.ATTENDANCE,
        component: import('@/views/pages/student/StudentAttendancePage.vue'),
      },
      {
        path: 'history-attendance',
        name: ROUTE_NAMES.HISTORY_ATTENDANCE,
        component: import('@/views/pages/student/StudentHistoryAttendancePage.vue'),
      },
    ],
  },
]
