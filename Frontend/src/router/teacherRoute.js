import { ROLE } from '@/constants/roleConstant'
import { GLOBAL_ROUTE_NAMES } from '@/constants/routesConstant'

export const ROUTE_NAMES = {
  MANAGEMENT_STUDENT: 'route_teacher_management_student',
  MANAGEMENT_SCHEDULE: 'route_teacher_management_schedule',
  MANAGEMENT_STUDENT_FACTORY: 'route_teacher_management_student_factory'
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
      {
        path: 'management-student-factory',
        name: ROUTE_NAMES.MANAGEMENT_STUDENT_FACTORY,
        component: import('@/views/pages/teacher/TeacherStudentFactory.vue'),
      },
    ],
  },
]
