<script setup>
import { ROUTE_NAMES } from '@/router/adminRoute'
import DefaultLayout from './base/DefaultLayout.vue'
import {
  ApartmentOutlined,
  BookOutlined,
  CalendarOutlined,
  ClusterOutlined,
  TeamOutlined,
} from '@ant-design/icons-vue'
</script>

<template>
  <DefaultLayout>
    <a-menu-item key="1">
      <router-link :to="{ name: ROUTE_NAMES.MANAGEMENT_FACILITY }">
        <ApartmentOutlined />
        <span>Quản lý cơ sở</span>
      </router-link>
    </a-menu-item>
    <a-menu-item key="2">
      <router-link :to="{ name: ROUTE_NAMES.MANAGEMENT_SEMESTER }">
        <CalendarOutlined />
        <span>Quản lý học kỳ</span>
      </router-link>
    </a-menu-item>
    <a-menu-item key="3">
      <router-link :to="{ name: ROUTE_NAMES.MANAGEMENT_SUBJECT }">
        <BookOutlined />
        <span>Quản lý bộ môn</span>
      </router-link>
    </a-menu-item>
    <a-menu-item key="4">
      <router-link :to="{ name: ROUTE_NAMES.MANAGEMENT_LEVEL_PROJECT }">
        <ClusterOutlined />
        <span>Quản lý cấp độ dự án</span>
      </router-link>
    </a-menu-item>
    <a-menu-item key="5">
      <router-link :to="{ name: ROUTE_NAMES.MANAGEMENT_STAFF }">
        <TeamOutlined />
        <span>Quản lý phụ trách xưởng</span>
      </router-link>
    </a-menu-item>
  </DefaultLayout>
</template>
