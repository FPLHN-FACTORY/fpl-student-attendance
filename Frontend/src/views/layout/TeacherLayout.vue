<script setup>
import { UserOutlined } from '@ant-design/icons-vue'
import { ROUTE_NAMES } from '@/router/teacherRoute'
import DefaultLayout from './base/DefaultLayout.vue'
</script>

<template>
  <DefaultLayout>
    <a-menu-item key="1">
      <router-link :to="{ name: ROUTE_NAMES.MANAGEMENT_SCHEDULE }">
        <user-outlined />
        <span>Lịch dạy</span>
      </router-link>
    </a-menu-item>
    <a-menu-item key="2">
      <router-link :to="{ name: ROUTE_NAMES.MANAGEMENT_STUDENT }">
        <user-outlined />
        <span>Quản lý sinh viên</span>
      </router-link>
    </a-menu-item>
  </DefaultLayout>
</template>
