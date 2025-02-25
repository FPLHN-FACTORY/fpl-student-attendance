import { ROLE } from '@/constants/roleConstant'
import { GLOBAL_ROUTE_NAMES } from '@/constants/routesConstant'

export const ROUTE_NAMES = {
  MANAGEMENT_FACILITY: 'route_admin_management_facility',
  MANAGEMENT_SEMESTER: 'route_admin_management_semester',
  MANAGEMENT_SUBJECT: 'route_admin_management_subject',
  MANAGEMENT_LEVEL_PROJECT: 'route_admin_management_level_project',
  MANAGEMENT_STAFF: 'route_admin_management_staff',
  MANAGEMENT_PROJECT: 'route_admin_management_project',
}

export const AdminRoutes = [
  {
    path: '/Admin',
    name: GLOBAL_ROUTE_NAMES.ADMIN_PAGE,
    component: import('@/views/layout/AdminLayout.vue'),
    redirect: { name: ROUTE_NAMES.MANAGEMENT_PROJECT },
    meta: {
      title: 'Ban đào tạo',
      requireRole: ROLE.ADMIN,
    },
    children: [
      {
        path: 'management-facility',
        name: ROUTE_NAMES.MANAGEMENT_FACILITY,
        component: import('@/views/pages/admin/AdminManagementFacilityPage.vue'),
      },
      {
        path: 'management-semester',
        name: ROUTE_NAMES.MANAGEMENT_SEMESTER,
        component: import('@/views/pages/admin/AdminManagementSemesterPage.vue'),
      },
      {
        path: 'management-subject',
        name: ROUTE_NAMES.MANAGEMENT_SUBJECT,
        component: import('@/views/pages/admin/AdminManagementSubjectPage.vue'),
      },
      {
        path: 'management-staff',
        name: ROUTE_NAMES.MANAGEMENT_STAFF,
        component: import('@/views/pages/admin/AdminManagementStaffPage.vue'),
      },
      {
        path: 'management-level-project',
        name: ROUTE_NAMES.MANAGEMENT_LEVEL_PROJECT,
        component: import('@/views/pages/admin/AdminManagementLevelProjectPage.vue'),
      },
      {
        path: 'management-project',
        name: ROUTE_NAMES.MANAGEMENT_PROJECT,
        component: import('@/views/pages/admin/AdminManagementProjectPage.vue'),
      },
    ],
  },
]
