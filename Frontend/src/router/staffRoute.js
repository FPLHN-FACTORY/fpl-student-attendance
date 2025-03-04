import { ROLE } from '@/constants/roleConstant'
import { GLOBAL_ROUTE_NAMES } from '@/constants/routesConstant'

export const ROUTE_NAMES = {
  MANAGEMENT_PROJECT: 'route_staff_management_project',
  MANAGEMENT_FACTORY: 'route_staff_management_factory',
  MANAGEMENT_PLAN: 'route_staff_management_plan',
  MANAGEMENT_LEVEL_PROJECT: 'route_staff_management_level_project',
  MANAGEMENT_STUDENT: 'route_staff_management_student',
}

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
        path: 'management-factory',
        name: ROUTE_NAMES.MANAGEMENT_FACTORY,
        component: import('@/views/pages/staff/StaffManagementFactoryPage.vue'),
      },
      {
        path: 'management-plan',
        name: ROUTE_NAMES.MANAGEMENT_PLAN,
        component: import('@/views/pages/staff/StaffManagementPlanPage.vue'),
      },
      {
        path: 'management-level-project',
        name: ROUTE_NAMES.MANAGEMENT_LEVEL_PROJECT,
        component: import('@/views/pages/staff/StaffManagementLevelProjectPage.vue'),
      },
      {
        path: 'management-student',
        name: ROUTE_NAMES.MANAGEMENT_STUDENT,
        component: import('@/views/pages/staff/StaffManagementStudentPage.vue'),
      },
    ],
  },
]
