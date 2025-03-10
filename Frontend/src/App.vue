<script setup>
import { RouterView } from 'vue-router'
import useLoadingStore from '@/stores/useLoadingStore'

const loadingPage = useLoadingStore()
</script>

<template>
  <a-config-provider
    :theme="{
      token: {
        colorPrimary: '#7f85c1',
      },
    }"
  >
    <a-spin :spinning="loadingPage.isLoading" tip="Đang tải dữ liệu...">
      <RouterView />
    </a-spin>
  </a-config-provider>
</template>
