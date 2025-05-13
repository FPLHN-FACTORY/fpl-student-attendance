<script setup>
import { RouterView } from 'vue-router'
import useLoadingStore from '@/stores/useLoadingStore'
import { themeConfig } from './constants/themeConfig'
import viVN from './config/locales/vi-VN'

const loadingPage = useLoadingStore()
</script>

<template>
  <a-config-provider :theme="themeConfig" :locale="viVN">
    <a-spin :spinning="loadingPage.isLoading" tip="Đang tải dữ liệu..." class="fullscreen-spin">
      <RouterView />
    </a-spin>
  </a-config-provider>
</template>
