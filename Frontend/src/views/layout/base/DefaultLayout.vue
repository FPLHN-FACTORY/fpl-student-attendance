<script setup>
import { onMounted, ref, watch } from 'vue'
import {
  MenuUnfoldOutlined,
  MenuFoldOutlined,
  LogoutOutlined,
  UserSwitchOutlined,
} from '@ant-design/icons-vue'
import imgLogoUdpm from '@/assets/images/logo-udpm.png'
import useAuthStore from '@/stores/useAuthStore'
import useBreadcrumbStore from '@/stores/useBreadCrumbStore'
import { useRouter } from 'vue-router'
import { GLOBAL_ROUTE_NAMES } from '@/constants/routesConstant'
import useApplicationStore from '@/stores/useApplicationStore'
import ExcelUploadList from '@/components/excel/ExcelUploadList.vue'

const router = useRouter()

const collapsed = ref(false)
const authStore = useAuthStore()
const breadcrumbStore = useBreadcrumbStore()
const applicationStore = useApplicationStore()

const handleLogout = () => {
  authStore.logout()
  window.location.reload()
}

const handleSwitchRole = () => {
  router.push({ name: GLOBAL_ROUTE_NAMES.SWITCH_ROLE })
}

onMounted(() => {
  if (!authStore.user?.facilityID) {
    return router.push({ name: GLOBAL_ROUTE_NAMES.STUDENT_REGISTER_PAGE })
  }
})
</script>

<template>
  <a-layout id="components-layout-demo-custom-trigger">
    <a-layout-sider v-model:collapsed="collapsed" :trigger="null" theme="light" collapsible>
      <div class="logo">
        <img :src="imgLogoUdpm" />
      </div>
      <a-menu v-model:selectedKeys="applicationStore.selectedKeys" theme="light" mode="inline">
        <slot></slot>
      </a-menu>
    </a-layout-sider>

    <a-layout>
      <!-- HEADER -->
      <a-layout-header class="d-flex justify-content-between bg-white ps-0">
        <component
          :is="collapsed ? MenuUnfoldOutlined : MenuFoldOutlined"
          class="trigger"
          @click="collapsed = !collapsed"
        />
        <img class="logo-mobile" :src="imgLogoUdpm" />

        <!-- Thông tin người dùng + Dropdown -->

        <a-dropdown>
          <a class="user-menu" @click.prevent>
            <a-avatar size="medium" :src="authStore.user.picture" />
            <span class="username"> {{ authStore.user.name }} </span>
          </a>
          <template #overlay>
            <a-menu>
              <a-menu-item class="active">
                <b>{{ authStore.user.name }}</b>
                <div>{{ authStore.user.sub }}</div>
              </a-menu-item>
              <a-menu-divider />
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
