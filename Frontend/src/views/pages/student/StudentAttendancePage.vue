<script setup>
import { GLOBAL_ROUTE_NAMES } from '@/constants/routesConstant'
import { API_ROUTES_STAFF } from '@/constants/staffConstant'
import { API_ROUTES_STUDENT } from '@/constants/studentConstant'
import { ROUTE_NAMES } from '@/router/studentRoute'
import requestAPI from '@/services/requestApiService'
import useBreadcrumbStore from '@/stores/useBreadCrumbStore'
import { onMounted, ref } from 'vue'

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
  requestAPI
    .get(`${API_ROUTES_STUDENT.FETCH_DATA_TEST}`)
    .then(({ data: response }) => {
      console.log(response)
    })
    .catch((error) => {})
})
</script>

<template>
  <div class="container-fluid">
    <div class="row g-3">
      <div class="col-12"></div>
    </div>
  </div>
</template>
