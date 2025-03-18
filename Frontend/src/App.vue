<script setup>
import { RouterView } from 'vue-router'
import useLoadingStore from '@/stores/useLoadingStore'
import { themeConfig } from './constants/themeConfig'

const loadingPage = useLoadingStore()
</script>

<template>
  <a-config-provider :theme="themeConfig">
    <a-spin :spinning="loadingPage.isLoading" tip="Đang tải dữ liệu..." class="fullscreen-spin">
      <RouterView />
    </a-spin>
  </a-config-provider>
</template>
