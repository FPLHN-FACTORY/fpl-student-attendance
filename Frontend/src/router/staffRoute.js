import { ROUTE_NAMES } from '@/constants/staffConstant'
import { ROLE } from '@/constants/roleConstant'
import { GLOBAL_ROUTE_NAMES } from '@/constants/routesConstant'

export const StaffRoutes = [
  {
    path: '/Staff',
    name: GLOBAL_ROUTE_NAMES.STAFF_PAGE,
    component: import('@/views/layout/StaffLayout.vue'),
    redirect: { name: ROUTE_NAMES.MANAGEMENT_PROJECT },
    meta: {
      title: 'Phụ trách xưởng',
      requireRole: ROLE.STAFF,
    },
    children: [
      {
        path: 'management-project',
        name: ROUTE_NAMES.MANAGEMENT_PROJECT,
        component: import('@/views/pages/staff/StaffManagementProjectPage.vue'),
      },
      {
        path: 'management-plan',
        name: ROUTE_NAMES.MANAGEMENT_PLAN,
        component: import('@/views/pages/staff/StaffManagementPlanPage.vue'),
      },
      {
        path: 'management-student',
        name: ROUTE_NAMES.MANAGEMENT_STUDENT,
        component: import('@/views/pages/staff/StaffManagementStudentPage.vue'),
      },
    ],
  },
]
