import { ROLE } from '@/constants'
import { GLOBAL_ROUTE_NAMES } from '@/constants/routesConstant'
import { CalendarOutlined, CheckOutlined, HistoryOutlined } from '@ant-design/icons-vue'

export const ROUTE_NAMES = {
  ATTENDANCE: 'route_student_attendance',
  SCHEDULE: 'route_student_schedule',
  HISTORY_ATTENDANCE: 'route_student_history_attendance',
}

let selectedKey = 1

export const StudentRoutes = [
  {
    path: '/Student',
    name: GLOBAL_ROUTE_NAMES.STUDENT_PAGE,
    component: () => import('@/views/layout/StudentLayout.vue'),
    redirect: { name: ROUTE_NAMES.ATTENDANCE },
    meta: {
      title: 'Sinh viên',
      requireRole: ROLE.STUDENT,
    },
    children: [
      {
        path: 'attendance',
        name: ROUTE_NAMES.ATTENDANCE,
        component: () => import('@/views/pages/student/StudentAttendancePage.vue'),
        meta: {
          selectedKey: selectedKey++,
          name: 'Điểm danh',
          icon: CheckOutlined,
        },
      },
      {
        path: 'schedule',
        name: ROUTE_NAMES.SCHEDULE,
        component: () => import('@/views/pages/student/StudentSchedulePage.vue'),
        meta: {
          selectedKey: selectedKey++,
          name: 'Lịch học',
          icon: CalendarOutlined,
        },
      },
      {
        path: 'history-attendance',
        name: ROUTE_NAMES.HISTORY_ATTENDANCE,
        component: () => import('@/views/pages/student/StudentHistoryAttendancePage.vue'),
        meta: {
          selectedKey: selectedKey++,
          name: 'Lịch sử diểm danh',
          icon: HistoryOutlined,
        },
      },
    ],
  },
]
