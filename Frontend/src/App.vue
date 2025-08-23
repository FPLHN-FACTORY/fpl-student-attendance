<script setup>
import { RouterView } from 'vue-router'
import useLoadingStore from '@/stores/useLoadingStore'
import { themeConfig } from './constants/themeConfig'
import viVN from './config/locales/vi-VN'

const loadingPage = useLoadingStore()
</script>

<template>
  <a-config-provider :theme="themeConfig" :locale="viVN">
    <RouterView />
    <div v-if="loadingPage.isLoading" class="fullscreen-spin">
      <a-spin tip="Vui lòng chờ..." size="large" />
    </div>
  </a-config-provider>
</template>
