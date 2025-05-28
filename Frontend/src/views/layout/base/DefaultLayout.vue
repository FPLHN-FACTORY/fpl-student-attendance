<script setup>
import { h, onMounted, onUnmounted, reactive, ref, watch } from 'vue'
import {
  MenuUnfoldOutlined,
  MenuFoldOutlined,
  LogoutOutlined,
  UserSwitchOutlined,
  BellFilled,
  CheckOutlined,
  DeleteOutlined,
  DeleteFilled,
  EyeOutlined,
  EyeInvisibleOutlined,
} from '@ant-design/icons-vue'
import imgLogoUdpm from '@/assets/images/logo-udpm.png'
import useAuthStore from '@/stores/useAuthStore'
import useBreadcrumbStore from '@/stores/useBreadCrumbStore'
import { useRoute, useRouter } from 'vue-router'
import {
  API_ROUTES_NOTIFICATION,
  BASE_URL,
  GLOBAL_ROUTE_NAMES,
  URL_ADMIN_PANEL,
} from '@/constants/routesConstant'
import useApplicationStore from '@/stores/useApplicationStore'
import ExcelUploadList from '@/components/excel/ExcelUploadList.vue'
import { autoAddColumnWidth, formatDate } from '@/utils/utils'
import { DEFAULT_DATE_FORMAT, DEFAULT_PAGINATION, ROLE } from '@/constants'
import requestAPI from '@/services/requestApiService'
import { message } from 'ant-design-vue'

const router = useRouter()
const route = useRoute()

const screenWidth = ref(window.innerWidth)
const screenHeight = ref(window.innerHeight)

const collapsed = ref(false)
const authStore = useAuthStore()
const breadcrumbStore = useBreadcrumbStore()
const applicationStore = useApplicationStore()

const isLoading = ref(false)
const isLoadingMarkReadAll = ref(false)
const isLoadingDeleteAll = ref(false)
const isShowAllNotification = ref(false)
const lstAllNotification = ref([])

const columns = ref(
  autoAddColumnWidth([
    { title: 'Trạng thái', dataIndex: 'status', key: 'status' },
    { title: 'Thời gian', dataIndex: 'createdAt', key: 'createdAt' },
    { title: 'Nội dung thông báo', dataIndex: 'message', key: 'message' },
    { title: '', key: 'actions' },
  ]),
)

const dataFilter = reactive({
  status: null,
})

const pagination = ref({ ...DEFAULT_PAGINATION })

const handleLogout = () => {
  const isAdm =
    authStore?.user?.role.includes(ROLE.ADMIN) ||
    authStore?.user?.role.includes(ROLE.STAFF) ||
    authStore?.user?.role.includes(ROLE.TEACHER)
  authStore.logout()
  window.location.href = isAdm ? URL_ADMIN_PANEL : BASE_URL
}

const handleShowAllNotification = () => {
  isShowAllNotification.value = true
  fetchDataListNotification()
}

const handleNotificationMarkReadAll = () => {
  applicationStore.markReadAll(fetchDataListNotification, isLoadingMarkReadAll)
}

const handleNotificationDeleteAll = () => {
  applicationStore.removeAll(() => {
    lstAllNotification.value = []
    pagination.value.total = 0
  }, isLoadingDeleteAll)
}

const callbackLoadNotification = () => {
  fetchDataListNotification(applicationStore.loadNotification)
}
const handleNotificationMarkRead = (item) => {
  applicationStore.markRead(item, callbackLoadNotification)
}

const handleNotificationMarkUnread = (item) => {
  applicationStore.markUnread(item, callbackLoadNotification)
}

const handleDeleteNotification = (item) => {
  applicationStore.remove(item, callbackLoadNotification)
}

const fetchDataListNotification = (callback) => {
  if (isLoading.value === true) {
    return
  }
  isLoading.value = true
  requestAPI
    .get(`${API_ROUTES_NOTIFICATION.FETCH_LIST}`, {
      params: {
        page: pagination.value.current,
        size: pagination.value.pageSize,
        ...dataFilter,
      },
    })
    .then(({ data: response }) => {
      lstAllNotification.value = response.data.data
      pagination.value.total = response.data.totalPages * pagination.value.pageSize
      typeof callback == 'function' && callback()
    })
    .catch((error) => {
      message.error(error?.response?.data?.message || 'Không thể tải danh sách dữ liệu thông báo')
    })
    .finally(() => {
      isLoading.value = false
    })
}

const handleTableChange = (page) => {
  pagination.value.current = page.current
  pagination.value.pageSize = page.pageSize
  fetchDataListNotification()
}

const handleSwitchRole = () => {
  router.push({ name: GLOBAL_ROUTE_NAMES.SWITCH_ROLE })
}

const handleMenuClick = () => {
  if (screenWidth.value <= 912) {
    collapsed.value = false
    document.body.classList.remove('disable-scroll-x')
  }
}

const handleResizeScreen = () => {
  screenWidth.value = window.innerWidth
  screenHeight.value = window.innerHeight
}

const handleToggleNav = () => {
  collapsed.value = !collapsed.value
  if (collapsed.value && screenWidth.value <= 912) {
    document.body.classList.add('disable-scroll-x')
  } else {
    document.body.classList.remove('disable-scroll-x')
  }
}

onMounted(() => {
  window.addEventListener('resize', handleResizeScreen)
  if (!authStore.user?.facilityID) {
    return router.push({ name: GLOBAL_ROUTE_NAMES.STUDENT_REGISTER_PAGE })
  }
  applicationStore.loadNotification()
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResizeScreen)
})

watch(
  () => route.fullPath,
  () => {
    applicationStore.loadNotification()
  },
)
</script>

<template>
  <a-modal v-model:open="isShowAllNotification" :width="1000" :footer="null">
    <template #title><BellFilled class="text-primary" /> Tất cả thông báo </template>
    <div class="row g-2">
      <div class="col-sm-12 col-md-6">
        <a-select
          v-model:value="dataFilter.status"
          class="w-100"
          :dropdownMatchSelectWidth="false"
          placeholder="-- Tất cả thông báo --"
          @change="fetchDataListNotification"
          allowClear
        >
          <a-select-option :value="null">-- Tất cả trạng thái --</a-select-option>
          <a-select-option :value="1">Thông báo mới</a-select-option>
          <a-select-option :value="0">Đã đọc</a-select-option>
        </a-select>
      </div>
      <div class="col-sm-6 col-md-3">
        <a-button
          type="primary"
          class="btn-outline-primary w-100"
          :loading="isLoadingMarkReadAll"
          @click="handleNotificationMarkReadAll"
        >
          <CheckOutlined /> Đánh dấu tất cả đã đọc
        </a-button>
      </div>
      <div class="col-sm-6 col-md-3">
        <a-button
          type="primary"
          class="btn-outline-danger w-100"
          :loading="isLoadingDeleteAll"
          @click="handleNotificationDeleteAll"
        >
          <DeleteOutlined />Xoá tất cả thông báo
        </a-button>
      </div>
      <div class="col-12">
        <a-table
          rowKey="id"
          class="nowrap"
          :dataSource="lstAllNotification"
          :columns="columns"
          :loading="isLoading"
          :pagination="pagination"
          :scroll="{ x: 'auto' }"
          @change="handleTableChange"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.dataIndex === 'message'">
              <span v-html="record.message"></span>
            </template>
            <template v-if="column.dataIndex === 'createdAt'">
              <span>{{ formatDate(record.createdAt, DEFAULT_DATE_FORMAT + ' HH:mm') }}</span>
            </template>
            <template v-if="column.dataIndex === 'status'">
              <a-tag :color="record.status === 1 ? 'green' : null">{{
                record.status === 1 ? 'Thông báo mới' : 'Đã đọc'
              }}</a-tag>
            </template>
            <template v-if="column.key === 'actions'">
              <a-tooltip title="Đánh dấu chưa xem" v-if="record.status !== 1">
                <a-button
                  class="btn-outline-gray border-0"
                  @click="handleNotificationMarkUnread(record)"
                >
                  <EyeOutlined />
                </a-button>
              </a-tooltip>
              <a-tooltip title="Đánh dấu đã xem" v-else>
                <a-button
                  class="btn-outline-gray border-0"
                  @click="handleNotificationMarkRead(record)"
                >
                  <EyeInvisibleOutlined />
                </a-button>
              </a-tooltip>
              <a-tooltip title="Xoá thông báo">
                <a-button
                  class="btn-outline-danger border-0 ms-2"
                  @click="handleDeleteNotification(record)"
                >
                  <DeleteFilled />
                </a-button>
              </a-tooltip>
            </template>
          </template>
        </a-table>
      </div>
    </div>
  </a-modal>

  <a-layout id="components-layout-demo-custom-trigger">
    <a-layout-sider v-model:collapsed="collapsed" :trigger="null" theme="light" collapsible>
      <div class="logo">
        <img :src="imgLogoUdpm" />
      </div>
      <a-menu
        v-model:selectedKeys="applicationStore.selectedKeys"
        theme="light"
        mode="inline"
        @click="handleMenuClick"
      >
        <slot></slot>
      </a-menu>
    </a-layout-sider>

    <a-layout class="sider-content">
      <!-- HEADER -->
      <a-layout-header class="d-flex justify-content-between bg-white ps-0">
        <component
          :is="collapsed ? MenuUnfoldOutlined : MenuFoldOutlined"
          class="trigger"
          @click="handleToggleNav"
        />
        <img class="logo-mobile" :src="imgLogoUdpm" />

        <!-- Thông tin người dùng + Dropdown -->

        <div class="d-flex align-item-center">
          <a-dropdown placement="bottomRight">
            <a class="notification-menu" @click.prevent>
              <a-badge :count="applicationStore.totalNotification">
                <BellFilled class="notification-menu_icon" />
              </a-badge>
            </a>
            <template #overlay>
              <div class="notification-content">
                <div class="notification-content_header">
                  <div>
                    Thông báo mới
                    <span class="ms-1">({{ applicationStore.totalNotification }})</span>
                  </div>
                  <a-button
                    v-show="applicationStore.totalNotification > 0"
                    class="btn-markReadAll"
                    :loading="applicationStore.isLoadingNotification"
                    :icon="h(CheckOutlined)"
                    @click="applicationStore.markReadAll()"
                    >Đã đọc tất cả</a-button
                  >
                </div>
                <div class="notification-content_body">
                  <div
                    class="notification-content_body-item"
                    v-for="o in applicationStore.lstNotification"
                    :key="o.id"
                    @click="applicationStore.markRead(o)"
                  >
                    <a-badge status="processing" />
                    <span class="notification-time">
                      {{ formatDate(o.createdAt, DEFAULT_DATE_FORMAT + ' HH:mm') }}</span
                    >
                    -
                    <span v-html="o.message"></span>
                  </div>
                  <div
                    class="notification-content_body-empty"
                    v-show="applicationStore.totalNotification < 1"
                  >
                    <svg
                      viewBox="0 0 24 24"
                      fill="none"
                      xmlns="http://www.w3.org/2000/svg"
                      stroke="#c9c9c9"
                    >
                      <g id="SVGRepo_bgCarrier" stroke-width="0"></g>
                      <g
                        id="SVGRepo_tracerCarrier"
                        stroke-linecap="round"
                        stroke-linejoin="round"
                      ></g>
                      <g id="SVGRepo_iconCarrier">
                        <path
                          d="M3 3L21 21M9.37747 3.56325C10.1871 3.19604 11.0827 3 12 3C13.5913 3 15.1174 3.59 16.2426 4.6402C17.3679 5.69041 18 7.11479 18 8.6C18 10.3566 18.2892 11.7759 18.712 12.9122M17 17H15M6.45339 6.46451C6.15686 7.13542 6 7.86016 6 8.6C6 11.2862 5.3238 13.1835 4.52745 14.4866C3.75616 15.7486 3.37051 16.3797 3.38485 16.5436C3.40095 16.7277 3.43729 16.7925 3.58603 16.9023C3.71841 17 4.34762 17 5.60605 17H9M9 17V18C9 19.6569 10.3431 21 12 21C13.6569 21 15 19.6569 15 18V17M9 17H15"
                          stroke="#c7c7c7"
                          stroke-width="2"
                          stroke-linecap="round"
                          stroke-linejoin="round"
                        ></path>
                      </g>
                    </svg>
                    <span>Chưa có thông báo nào!</span>
                  </div>
                </div>
                <a @click="handleShowAllNotification()" class="notification-content_footer"
                  >Tất cả thông báo</a
                >
              </div>
            </template>
          </a-dropdown>
          <a-dropdown>
            <a class="user-menu" @click.prevent>
              <a-avatar size="medium" :src="authStore.user.picture" />
              <div class="user-deital">
                <div class="username">{{ authStore.user.name }}</div>
                <div class="email">{{ authStore.user.sub }}</div>
              </div>
            </a>
            <template #overlay>
              <a-menu>
                <a-menu-item class="active d-lg-none">
                  <b>{{ authStore.user.name }}</b>
                  <div>{{ authStore.user.sub }}</div>
                </a-menu-item>
                <a-menu-divider class="d-lg-none" />
                <a-menu-item
                  key="switchRole"
                  @click="handleSwitchRole()"
                  v-if="authStore.user.role.length > 1"
                >
                  <UserSwitchOutlined />
                  <span class="ms-2">Thay đổi vai trò</span>
                </a-menu-item>
                <a-menu-item key="logout" @click="handleLogout()">
                  <LogoutOutlined />
                  <span class="ms-2">Đăng xuất</span>
                </a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
        </div>
      </a-layout-header>

      <a-layout-content>
        <a-breadcrumb
          class="mb-3 mx-3"
          :routes="breadcrumbStore.routes"
          v-if="breadcrumbStore.routes.length > 0"
        >
          <template #itemRender="{ route, routes }">
            <span v-if="routes.indexOf(route) === routes.length - 1">{{
              route.breadcrumbName
            }}</span>
            <router-link v-else :to="{ name: route.name, params: route?.params }">{{
              route.breadcrumbName
            }}</router-link>
          </template>
        </a-breadcrumb>
        <router-view />
        <ExcelUploadList />
      </a-layout-content>
    </a-layout>
  </a-layout>
</template>
