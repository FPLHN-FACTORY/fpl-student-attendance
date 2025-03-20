<script setup>
import { GLOBAL_ROUTE_NAMES } from '@/constants/routesConstant'
import { API_ROUTES_STUDENT } from '@/constants/studentConstant'
import { ROUTE_NAMES } from '@/router/studentRoute'
import requestAPI from '@/services/requestApiService'
import useBreadcrumbStore from '@/stores/useBreadCrumbStore'
import { onMounted, ref } from 'vue'
import { message } from 'ant-design-vue'

const breadcrumbStore = useBreadcrumbStore()

const breadcrumb = ref([
  {
    name: GLOBAL_ROUTE_NAMES.STAFF_PAGE,
    breadcrumbName: 'Sinh viên',
  },
  {
    name: ROUTE_NAMES.ATTENDANCE,
    breadcrumbName: 'Điểm danh',
  },
])

onMounted(() => {
  breadcrumbStore.setRoutes(breadcrumb.value)
})
</script>

<template></template>