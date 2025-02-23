<script setup>
import { RouterView } from 'vue-router'
import useLoadingStore from '@/stores/useLoadingStore'

const loadingPage = useLoadingStore()
</script>

<template>
  <a-spin :spinning="loadingPage.isLoading" tip="Đang tải dữ liệu...">
    <RouterView />
  </a-spin>
</template>
