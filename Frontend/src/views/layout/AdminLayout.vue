<script setup>
import { ref, watch, onMounted } from 'vue'
import {
  MenuUnfoldOutlined,
  MenuFoldOutlined,
  ProjectOutlined,
  ApartmentOutlined,
  CalendarOutlined,
  BookOutlined,
  ClusterOutlined,
  TeamOutlined,
  LogoutOutlined,
} from '@ant-design/icons-vue'
import imgLogoUdpm from '@/assets/images/logo-udpm.png'
import useAuthStore from '@/stores/useAuthStore'
import { ROUTE_NAMES } from '@/router/adminRoute'

const selectedKeys = ref(['1'])
const collapsed = ref(false)
const authStore = useAuthStore()

const handleLogout = () => {
  authStore.logout()
  window.location.reload()
}

onMounted(() => {
  const savedKeys = sessionStorage.getItem('selectedKeys')
  if (savedKeys) {
    selectedKeys.value = JSON.parse(savedKeys)
  }
})

watch(
  selectedKeys,
  (newValue) => {
    sessionStorage.setItem('selectedKeys', JSON.stringify(newValue))
  },
  { deep: true },
)
</script>

<template>
  <a-layout id="components-layout-demo-custom-trigger">
    <a-layout-sider v-model:collapsed="collapsed" :trigger="null" theme="light" collapsible>
      <div class="logo">
        <img :src="imgLogoUdpm" />
      </div>
      <a-menu v-model:selectedKeys="selectedKeys" theme="light" mode="inline">
        <a-menu-item key="1">
          <router-link :to="{ name: ROUTE_NAMES.MANAGEMENT_PROJECT }">
            <project-outlined />
            <span>Quản lý dự án</span>
          </router-link>
        </a-menu-item>
        <a-menu-item key="2">
          <router-link :to="{ name: ROUTE_NAMES.MANAGEMENT_FACILITY }">
            <apartment-outlined />
            <span>Quản lý cơ sở</span>
          </router-link>
        </a-menu-item>
        <a-menu-item key="3">
          <router-link :to="{ name: ROUTE_NAMES.MANAGEMENT_SEMESTER }">
            <calendar-outlined />
            <span>Quản lý học kỳ</span>
          </router-link>
        </a-menu-item>
        <a-menu-item key="4">
          <router-link :to="{ name: ROUTE_NAMES.MANAGEMENT_SUBJECT }">
            <book-outlined />
            <span>Quản lý bộ môn</span>
          </router-link>
        </a-menu-item>
        <a-menu-item key="5">
          <router-link :to="{ name: ROUTE_NAMES.MANAGEMENT_LEVEL_PROJECT }">
            <cluster-outlined />
            <span>Quản lý cấp độ dự án</span>
          </router-link>
        </a-menu-item>
        <a-menu-item key="6">
          <router-link :to="{ name: ROUTE_NAMES.MANAGEMENT_STAFF }">
            <team-outlined />
            <span>Quản lý nhân viên</span>
          </router-link>
        </a-menu-item>
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
              <a-menu-item key="logout" @click="handleLogout()">
                <LogoutOutlined />
                <span class="ms-2">Đăng xuất</span>
              </a-menu-item>
            </a-menu>
          </template>
        </a-dropdown>
      </a-layout-header>

      <a-layout-content>
        <router-view />
      </a-layout-content>
    </a-layout>
  </a-layout>
</template>
