<script setup>
import { ref } from 'vue'
import { MenuUnfoldOutlined, MenuFoldOutlined, UserOutlined } from '@ant-design/icons-vue'
import imgLogoFpt from '@/assets/images/logo-fpt.png'
import useAuthStore from '@/stores/useAuthStore'
import { ROUTE_NAMES } from '@/router/studentRoute'

const selectedKeys = ref(['1'])
const collapsed = ref(false)
const authStore = useAuthStore()

const handleLogout = () => {
  authStore.logout()
  window.location.reload()
}
</script>

<template>
  <a-layout id="components-layout-demo-custom-trigger" style="height: 100vh">
    <a-layout-sider
      v-model:collapsed="collapsed"
      :trigger="null"
      theme="light"
      :width="250"
      collapsible
    >
      <div class="logo">
        <img :src="imgLogoFpt" />
      </div>
      <a-menu v-model:selectedKeys="selectedKeys" theme="light" mode="inline">
        <a-menu-item key="1">
          <router-link :to="{ name: ROUTE_NAMES.ATTENDANCE }">
            <user-outlined />
            <span>Điểm danh</span>
          </router-link>
        </a-menu-item>
        <a-menu-item key="2">
          <router-link :to="{ name: ROUTE_NAMES.HISTORY_ATTENDANCE }">
            <user-outlined />
            <span>Lịch sử điểm danh</span>
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

        <!-- Thông tin người dùng + Dropdown -->

        <a-dropdown>
          <a class="user-menu" @click.prevent>
            <a-avatar size="medium" :src="authStore.user.picture" />
            <span class="username"> {{ authStore.user.name }} </span>
          </a>
          <template #overlay>
            <a-menu>
              <a-menu-item key="logout" @click="handleLogout()">
                <logout-outlined />
                <span>Đăng xuất</span>
              </a-menu-item>
            </a-menu>
          </template>
        </a-dropdown>
      </a-layout-header>

      <!-- CONTENT -->
      <a-layout-content
        :style="{ margin: '24px 16px', padding: '24px', background: '#fff', minHeight: '280px' }"
      >
        <router-view />
      </a-layout-content>
    </a-layout>
  </a-layout>
</template>

<style scoped>
#components-layout-demo-custom-trigger .trigger {
  font-size: 18px;
  line-height: 64px;
  padding: 0 24px;
  cursor: pointer;
  transition: color 0.3s;
}

#components-layout-demo-custom-trigger .trigger:hover {
  color: #1890ff;
}

#components-layout-demo-custom-trigger .logo {
  height: 32px;
  background: rgba(255, 255, 255, 0.3);
  margin: 16px;
}

.site-layout .site-layout-background {
  background: #fff;
}

.bg-white {
  background-color: #fff;
}
.logo {
  text-align: center;
}
.logo img {
  max-width: 100%;
  max-height: 100%;
}

/* User menu */
.user-menu {
  display: flex;
  align-items: center;
  cursor: pointer;
}

.username {
  margin-left: 8px;
  font-weight: 500;
  color: #333;
}
</style>
