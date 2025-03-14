<script setup>
import { GLOBAL_ROUTE_NAMES } from '@/constants/routesConstant'
import { ROUTE_NAMES } from '@/router/studentRoute'
import useBreadcrumbStore from '@/stores/useBreadCrumbStore'
import { onMounted, ref } from 'vue'

const breadcrumbStore = useBreadcrumbStore()

const breadcrumb = ref([
  {
    name: GLOBAL_ROUTE_NAMES.STAFF_PAGE,
    breadcrumbName: 'Sinh viên',
  },
  {
    name: ROUTE_NAMES.SCHEDULE,
    breadcrumbName: 'Lịch học',
  },
])

onMounted(() => {
  breadcrumbStore.setRoutes(breadcrumb.value)
})
</script>

<template>
  <div class="container-fluid">
    <div class="row g-3">
      <div class="col-12"></div>
    </div>
  </div>
</template>
