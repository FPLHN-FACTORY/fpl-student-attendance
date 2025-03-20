import { ROLE } from '@/constants'
import { GLOBAL_ROUTE_NAMES } from '@/constants/routesConstant'
import { CalendarOutlined, UsergroupAddOutlined } from '@ant-design/icons-vue'

export const ROUTE_NAMES = {
  MANAGEMENT_STUDENT: 'route_teacher_management_student',
  MANAGEMENT_SCHEDULE: 'route_teacher_management_schedule',
  MANAGEMENT_STUDENT_FACTORY: 'route_teacher_management_student_factory'
}

let selectedKey = 1

export const TeacherRoutes = [
  {
    path: '/Teacher',
    name: GLOBAL_ROUTE_NAMES.TEACHER_PAGE,
    component: () => import('@/views/layout/TeacherLayout.vue'),
    redirect: { name: ROUTE_NAMES.MANAGEMENT_SCHEDULE },
    meta: {
      title: 'Giảng viên',
      requireRole: ROLE.TEACHER,
    },
    children: [
      {
        path: 'schedule',
        name: ROUTE_NAMES.MANAGEMENT_SCHEDULE,
        component: () => import('@/views/pages/teacher/TeacherSchedulePage.vue'),
        meta: {
          selectedKey: selectedKey++,
          name: 'Lịch giảng dạy',
          icon: CalendarOutlined,
        },
      },
      {
        path: 'management-student',
        name: ROUTE_NAMES.MANAGEMENT_STUDENT,
        component: () => import('@/views/pages/teacher/TeacherStudentPage.vue'),
        meta: {
          selectedKey: selectedKey++,
          name: 'Quản lý sinh viên',
          icon: UsergroupAddOutlined,
        },
      },
      {
        path: 'management-student-factory',
        name: ROUTE_NAMES.MANAGEMENT_STUDENT_FACTORY,
        component: import('@/views/pages/teacher/TeacherStudentFactory.vue'),
      },
    ],
  },
]
