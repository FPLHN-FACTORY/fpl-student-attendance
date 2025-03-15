<script setup>
import { UserOutlined } from '@ant-design/icons-vue'
import { ROUTE_NAMES } from '@/router/studentRoute'
import DefaultLayout from './base/DefaultLayout.vue'
</script>

<template>
  <DefaultLayout>
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
  </DefaultLayout>
</template>
