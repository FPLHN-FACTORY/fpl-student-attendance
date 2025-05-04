import { ROLE } from '@/constants'
import { GLOBAL_ROUTE_NAMES } from '@/constants/routesConstant'
import {
  FileTextOutlined,
  GoldOutlined,
  ProjectOutlined,
  UsergroupAddOutlined,
} from '@ant-design/icons-vue'

export const ROUTE_NAMES = {
  MANAGEMENT_PROJECT: 'route_staff_management_project',
  MANAGEMENT_FACTORY: 'route_staff_management_factory',
  MANAGEMENT_STUDENT_FACTORY: 'route_staff_management_student_factory',
  MANAGEMENT_PLAN: 'route_staff_management_plan',
  MANAGEMENT_PLAN_DATE: 'route_staff_management_plan_date',
  MANAGEMENT_PLAN_FACTORY: 'route_staff_management_plan_factory',
  MANAGEMENT_STUDENT: 'route_staff_management_student',
}

let selectedKey = 1

export const StaffRoutes = [
  {
    path: '/Staff',
    name: GLOBAL_ROUTE_NAMES.STAFF_PAGE,
    component: () => import('@/views/layout/StaffLayout.vue'),
    redirect: { name: ROUTE_NAMES.MANAGEMENT_PROJECT },
    meta: {
      title: 'Phụ trách xưởng',
      requireRole: ROLE.STAFF,
    },
    children: [
      {
        path: 'management-project',
        name: ROUTE_NAMES.MANAGEMENT_PROJECT,
        component: () => import('@/views/pages/staff/StaffManagementProjectPage.vue'),
        meta: {
          selectedKey: selectedKey++,
          name: 'Quản lý dự án',
          icon: ProjectOutlined,
        },
      },
      {
        path: 'management-factory',
        name: ROUTE_NAMES.MANAGEMENT_FACTORY,
        component: () => import('@/views/pages/staff/StaffManagementFactoryPage.vue'),
        meta: {
          selectedKey: selectedKey++,
          name: 'Quản lý nhóm xưởng',
          icon: GoldOutlined,
        },
      },
      {
        path: 'management-plan',
        name: ROUTE_NAMES.MANAGEMENT_PLAN,
        component: () => import('@/views/pages/staff/plan-management/SPMPlanPage.vue'),
        meta: {
          selectedKey: selectedKey++,
          name: 'Danh sách kế hoạch',
          icon: FileTextOutlined,
        },
      },
      {
        path: 'management-plan/:id',
        name: ROUTE_NAMES.MANAGEMENT_PLAN_FACTORY,
        component: () => import('@/views/pages/staff/plan-management/SPMPlanFactoryPage.vue'),
        meta: {
          name: 'Chi tiết kế hoạch',
        },
      },
      {
        path: 'management-plan-date/:id',
        name: ROUTE_NAMES.MANAGEMENT_PLAN_DATE,
        component: () => import('@/views/pages/staff/plan-management/SPMPlanDatePage.vue'),
        meta: {
          name: 'Phân công kế hoạch',
        },
      },
      {
        path: 'management-student',
        name: ROUTE_NAMES.MANAGEMENT_STUDENT,
        component: () => import('@/views/pages/staff/StaffManagementStudentPage.vue'),
        meta: {
          selectedKey: selectedKey++,
          name: 'Quản lý sinh viên',
          icon: UsergroupAddOutlined,
        },
      },
      {
        path: 'management-student-factory',
        name: ROUTE_NAMES.MANAGEMENT_STUDENT_FACTORY,
        component: () => import('@/views/pages/staff/StaffManagementStudentFactoryPage.vue'),
      },
    ],
  },
]
