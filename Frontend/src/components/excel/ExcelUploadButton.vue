<script setup>
import useImportExcelStore from '@/stores/useImportExcelStore'
import { FileExcelOutlined, UploadOutlined } from '@ant-design/icons-vue'
import { defineProps, onMounted, ref } from 'vue'

const serviceStore = useImportExcelStore()

const isLoadingDownload = ref(false)

const props = defineProps({
  fetchUrl: { type: String, default: null },
  onSuccess: { type: Function, default: null },
  onError: { type: Function, default: null },
  meta: { type: Object, default: {} },
  showDownloadTemplate: { type: Boolean, default: false },
})

const handleBeforeUpload = (file) => {
  serviceStore.enqueue(file, {
    fetchUrl: props.fetchUrl,
    onSuccess: props.onSuccess,
    onError: props.onError,
    meta: props.meta,
  })
  return false
}

const handleDownloadTemplate = async () => {
  isLoadingDownload.value = true
  await serviceStore.downloadTemplate()
  isLoadingDownload.value = false
}

onMounted(() => {
  serviceStore.setUrlDownloadTemplate(props.fetchUrl)
})
</script>

<template>
  <a-button
    @click="handleDownloadTemplate"
    v-if="props.showDownloadTemplate"
    :loading="isLoadingDownload"
    ><FileExcelOutlined />Download tempalte</a-button
  >
  <a-upload
    :showUploadList="false"
    :multiple="true"
    :beforeUpload="handleBeforeUpload"
    accept=".xls,.xlsx"
  >
    <a-button>
      <UploadOutlined />
      Import Excel
    </a-button>
  </a-upload>
</template>
