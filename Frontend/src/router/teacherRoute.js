import { ROLE } from '@/constants'
import { GLOBAL_ROUTE_NAMES } from '@/constants/routesConstant'
import { CalendarOutlined, PieChartOutlined, UsergroupAddOutlined } from '@ant-design/icons-vue'

export const ROUTE_NAMES = {
  MANAGEMENT_STATISTICS: 'route_teacher_management_statistics',
  MANAGEMENT_FACTORY: 'route_teacher_management_factory',
  MANAGEMENT_SCHEDULE: 'route_teacher_management_schedule',
  MANAGEMENT_STUDENT_FACTORY: 'route_teacher_management_student_factory',
  MANAGEMENT_SHIFT_FACTORY: 'route_teacher_management_shift_factory',
  MANAGEMENT_PLANDATE_ATTENDANCE_FACTORY: 'route_teacher_management_plan_date_attendance_factory',
  MANAGEMENT_STUDENT_ATTENDANCE: 'route_teacher_management_student_attendance',
}

let selectedKey = 1

export const TeacherRoutes = [
  {
    path: '/Teacher',
    name: GLOBAL_ROUTE_NAMES.TEACHER_PAGE,
    component: () => import('@/views/layout/TeacherLayout.vue'),
    redirect: { name: ROUTE_NAMES.MANAGEMENT_STATISTICS },
    meta: {
      title: 'Giảng viên',
      requireRole: ROLE.TEACHER,
    },
    children: [
      {
        path: 'management-statistics',
        name: ROUTE_NAMES.MANAGEMENT_STATISTICS,
        component: () => import('@/views/pages/teacher/TeacherStatisticsPage.vue'),
        meta: {
          selectedKey: selectedKey++,
          name: 'Thống kê',
          icon: PieChartOutlined,
        },
      },
      {
        path: 'schedule',
        name: ROUTE_NAMES.MANAGEMENT_SCHEDULE,
        component: () => import('@/views/pages/teacher/TeacherSchedulePage.vue'),
        meta: {
          selectedKey: selectedKey++,
          name: 'Lịch quản lý',
          icon: CalendarOutlined,
        },
      },
      {
        path: 'management-factory',
        name: ROUTE_NAMES.MANAGEMENT_FACTORY,
        component: () => import('@/views/pages/teacher/TeacherFactoryPage.vue'),
        meta: {
          selectedKey: selectedKey++,
          name: 'Nhóm xưởng của tôi',
          icon: UsergroupAddOutlined,
        },
      },
      {
        path: 'management-student-factory',
        name: ROUTE_NAMES.MANAGEMENT_STUDENT_FACTORY,
        component: () => import('@/views/pages/teacher/TeacherStudentFactoryPage.vue'),
        meta: {
          name: 'Danh sách sinh viên',
        },
      },
      {
        path: 'management-shift-factory/:id',
        name: ROUTE_NAMES.MANAGEMENT_SHIFT_FACTORY,
        component: () => import('@/views/pages/teacher/TeacherShiftFactoryPage.vue'),
        meta: {
          name: 'Danh sách ca',
        },
      },
      {
        path: 'management-plandate-attendance-factory/:id',
        name: ROUTE_NAMES.MANAGEMENT_PLANDATE_ATTENDANCE_FACTORY,
        component: () => import('@/views/pages/teacher/TeacherPlanDateAttendancePage.vue'),
        meta: {
          name: 'Chi tiết điểm danh',
        },
      },
      {
        path: 'management-student-attendance',
        name: ROUTE_NAMES.MANAGEMENT_STUDENT_ATTENDANCE,
        component: () => import('@/views/pages/teacher/TeacherAttendancePage.vue'),
        meta: {
          name: 'Điểm danh sinh viên',
        },
      },
    ],
  },
]
