<script setup>
import {
  FileTextOutlined,
  GoldOutlined,
  ProjectOutlined,
  UserOutlined,
} from '@ant-design/icons-vue'
import { ROUTE_NAMES } from '@/router/staffRoute'
import DefaultLayout from './base/DefaultLayout.vue'
</script>

<template>
  <DefaultLayout>
    <a-menu-item key="1">
      <router-link :to="{ name: ROUTE_NAMES.MANAGEMENT_PROJECT }">
        <ProjectOutlined />
        <span>Quản lý dự án</span>
      </router-link>
    </a-menu-item>
    <a-menu-item key="2">
      <router-link :to="{ name: ROUTE_NAMES.MANAGEMENT_FACTORY }">
        <GoldOutlined />
        <span>Quản lý nhóm xưởng</span>
      </router-link>
    </a-menu-item>
    <a-menu-item key="3">
      <router-link :to="{ name: ROUTE_NAMES.MANAGEMENT_PLAN }">
        <FileTextOutlined />
        <span>Phân công kế hoạch</span>
      </router-link>
    </a-menu-item>
    <a-menu-item key="4">
      <router-link :to="{ name: ROUTE_NAMES.MANAGEMENT_STUDENT }">
        <user-outlined />
        <span>Quản lý sinh viên</span>
      </router-link>
    </a-menu-item>
  </DefaultLayout>
</template>
