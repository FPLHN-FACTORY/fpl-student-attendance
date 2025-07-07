import { ROLE } from '@/constants'
import { GLOBAL_ROUTE_NAMES } from '@/constants/routesConstant'
import {
  ApartmentOutlined,
  BookOutlined,
  CalendarOutlined,
  ClusterOutlined,
  PieChartOutlined,
  SolutionOutlined,
  TeamOutlined,
  UserOutlined,
} from '@ant-design/icons-vue'

export const ROUTE_NAMES = {
  MANAGEMENT_STATISTICS: 'route_admin_management_statistics',
  MANAGEMENT_FACILITY: 'route_admin_management_facility',
  MANAGEMENT_FACILITY_IP: 'route_admin_management_facility_ip',
  MANAGEMENT_FACILITY_LOCATION: 'route_admin_management_facility_location',
  MANAGEMENT_FACILITY_SHIFT: 'route_admin_management_facility_shift',
  MANAGEMENT_SEMESTER: 'route_admin_management_semester',
  MANAGEMENT_SUBJECT: 'route_admin_management_subject',
  MANAGEMENT_SUBJECT_FACILITY: 'route_admin_management_subject_facility',
  MANAGEMENT_LEVEL_PROJECT: 'route_admin_management_level_project',
  MANAGEMENT_STAFF: 'route_admin_management_staff',
  MANAGEMENT_ADMIN: 'route_admin_management_admin',
  MANAGEMENT_USER_LOG_ACTIVITY: 'route_admin_management_user_log_activity',
}
let selectedKey = 1

export const AdminRoutes = [
  {
    path: '/Admin',
    name: GLOBAL_ROUTE_NAMES.ADMIN_PAGE,
    component: () => import('@/views/layout/AdminLayout.vue'),
    redirect: { name: ROUTE_NAMES.MANAGEMENT_STATISTICS },
    meta: {
      title: 'Admin',
      requireRole: ROLE.ADMIN,
    },
    children: [
      {
        path: 'management-statistics',
        name: ROUTE_NAMES.MANAGEMENT_STATISTICS,
        component: () => import('@/views/pages/admin/AdminManagementStatisticsPage.vue'),
        meta: {
          selectedKey: selectedKey++,
          name: 'Thống kê',
          icon: PieChartOutlined,
        },
      },
      {
        path: 'management-facility',
        name: ROUTE_NAMES.MANAGEMENT_FACILITY,
        component: () => import('@/views/pages/admin/facility/AFFacilityPage.vue'),
        meta: {
          selectedKey: selectedKey++,
          name: 'Quản lý cơ sở',
          icon: ApartmentOutlined,
        },
      },
      {
        path: 'management-facility/:id',
        name: ROUTE_NAMES.MANAGEMENT_FACILITY_IP,
        component: () => import('@/views/pages/admin/facility/AFFacilityIPPage.vue'),
        meta: {
          name: 'Quản lý IP cơ sở',
        },
      },
      {
        path: 'management-shift/:id',
        name: ROUTE_NAMES.MANAGEMENT_FACILITY_SHIFT,
        component: () => import('@/views/pages/admin/facility/AFFacilityShiftPage.vue'),
        meta: {
          name: 'Quản lý ca học cơ sở',
        },
      },
      {
        path: 'management-location/:id',
        name: ROUTE_NAMES.MANAGEMENT_FACILITY_LOCATION,
        component: () => import('@/views/pages/admin/facility/AFFacilityLocationPage.vue'),
        meta: {
          name: 'Quản lý địa điểm cơ sở',
        },
      },
      {
        path: 'management-semester',
        name: ROUTE_NAMES.MANAGEMENT_SEMESTER,
        component: () => import('@/views/pages/admin/AdminManagementSemesterPage.vue'),
        meta: {
          selectedKey: selectedKey++,
          name: 'Quản lý học kỳ',
          icon: CalendarOutlined,
        },
      },
      {
        path: 'management-subject',
        name: ROUTE_NAMES.MANAGEMENT_SUBJECT,
        component: () => import('@/views/pages/admin/AdminManagementSubjectPage.vue'),
        meta: {
          selectedKey: selectedKey++,
          name: 'Quản lý bộ môn',
          icon: BookOutlined,
        },
      },
      {
        path: 'management-level-project',
        name: ROUTE_NAMES.MANAGEMENT_LEVEL_PROJECT,
        component: () => import('@/views/pages/admin/AdminManagementLevelProjectPage.vue'),
        meta: {
          selectedKey: selectedKey++,
          name: 'Quản lý nhóm dự án',
          icon: ClusterOutlined,
        },
      },
      {
        path: 'management-staff',
        name: ROUTE_NAMES.MANAGEMENT_STAFF,
        component: () => import('@/views/pages/admin/AdminManagementStaffPage.vue'),
        meta: {
          selectedKey: selectedKey++,
          name: 'Quản lý nhân sự',
          icon: TeamOutlined,
        },
      },
      {
        path: 'management-admin',
        name: ROUTE_NAMES.MANAGEMENT_ADMIN,
        component: () => import('@/views/pages/admin/AdminManagementUserAdminPage.vue'),
        meta: {
          selectedKey: selectedKey++,
          name: 'Quản lý Admin',
          icon: UserOutlined,
        },
      },
      {
        path: 'management-user-log-activity',
        name: ROUTE_NAMES.MANAGEMENT_USER_LOG_ACTIVITY,
        component: () => import('@/views/pages/admin/AdminManagementUserLogActivity.vue'),
        meta: {
          selectedKey: selectedKey++,
          name: 'Lịch sử hoạt động',
          icon: SolutionOutlined,
        },
      },

      {
        path: 'management-subject_facility',
        name: ROUTE_NAMES.MANAGEMENT_SUBJECT_FACILITY,
        component: () => import('@/views/pages/admin/AdminManagementSubjectFacilityPage.vue'),
        meta: {
          name: 'Bộ môn theo cơ sở',
        },
      },
    ],
  },
]
