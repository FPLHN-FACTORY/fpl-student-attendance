import { ROLE } from '@/constants/roleConstant'
import { GLOBAL_ROUTE_NAMES } from '@/constants/routesConstant'

export const ROUTE_NAMES = {
  MANAGEMENT_STUDENT: 'route_teacher_management_student',
  MANAGEMENT_SCHEDULE: 'route_teacher_management_schedule',
}

export const TeacherRoutes = [
  {
    path: '/Teacher',
    name: GLOBAL_ROUTE_NAMES.TEACHER_PAGE,
    component: import('@/views/layout/TeacherLayout.vue'),
    redirect: { name: ROUTE_NAMES.MANAGEMENT_SCHEDULE },
    meta: {
      title: 'Giảng viên',
      requireRole: ROLE.TEACHER,
    },
    children: [
      {
        path: 'schedule',
        name: ROUTE_NAMES.MANAGEMENT_SCHEDULE,
        component: import('@/views/pages/teacher/TeacherSchedulePage.vue'),
      },
      {
        path: 'management-student',
        name: ROUTE_NAMES.MANAGEMENT_STUDENT,
        component: import('@/views/pages/teacher/TeacherStudentPage.vue'),
      },
    ],
  },
]
