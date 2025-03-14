<script setup>
import useImportExcelStore from '@/stores/useImportExcelStore'
import { UploadOutlined } from '@ant-design/icons-vue'

const serviceStore = useImportExcelStore()

const handleBeforeUpload = (file) => {
  serviceStore.enqueue(file)
  return false
}
</script>

<template>
  <a-upload :showUploadList="false" :multiple="true" :beforeUpload="handleBeforeUpload">
    <a-button>
      <UploadOutlined />
      Import Excel
    </a-button>
  </a-upload>
</template>
