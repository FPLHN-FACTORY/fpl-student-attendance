<script setup>
import { DEFAULT_PAGINATION } from '@/constants'
import useImportExcelStore from '@/stores/useImportExcelStore'
import { autoAddColumnWidth, formatDate } from '@/utils/utils'
import {
  FileExcelOutlined,
  UploadOutlined,
  HistoryOutlined,
  InfoCircleFilled,
} from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import { defineProps, onMounted, ref } from 'vue'
import { toast } from 'vue3-toastify'

const serviceStore = useImportExcelStore()

const isShowHistoryLog = ref(false)
const isShowHistoryLogDetail = ref(false)

const isLoadingTable = ref(false)
const isLoadingDownload = ref(false)
const isLoadingShowLog = ref(false)

const lstData = ref([])
const lstDataDetail = ref([])

const pagination = ref({ ...DEFAULT_PAGINATION })

const props = defineProps({
  fetchUrl: { type: String, default: null },
  onSuccess: { type: Function, default: null },
  onError: { type: Function, default: null },
  data: { type: Object, default: {} },
  showDownloadTemplate: { type: Boolean, default: false },
  showHistoryLog: { type: Boolean, default: false },
})

const columns = ref(
  autoAddColumnWidth([
    { title: 'Thời gian', dataIndex: 'createdAt', key: 'createdAt' },
    { title: 'Tệp tin', dataIndex: 'fileName', key: 'fileName' },
    {
      title: 'Thành công',
      dataIndex: 'totalSuccess',
      key: 'totalSuccess',
    },
    { title: 'Lỗi', dataIndex: 'totalError', key: 'totalError' },
    { title: '', key: 'actions' },
  ]),
)

const columnsDetail = ref(
  autoAddColumnWidth([
    { title: 'Dòng', dataIndex: 'line', key: 'line' },
    { title: 'Trạng thái', dataIndex: 'status', key: 'status' },
    { title: 'Nội dung', dataIndex: 'message', key: 'message' },
  ]),
)

const handleShowDetail = (id) => {
  isLoadingTable.value = true
  lstDataDetail.value = []
  fetchDataHistoryLogDetail(id)
}

const handleBeforeUpload = (file) => {
  serviceStore.enqueue(file, {
    fetchUrl: props.fetchUrl,
    onSuccess: props.onSuccess,
    onError: props.onError,
    data: props.data,
  })
  return false
}

const handleDownloadTemplate = async () => {
  isLoadingDownload.value = true
  await serviceStore.downloadTemplate()
  isLoadingDownload.value = false
}

const handleShowHistoryLog = async () => {
  try {
    isLoadingShowLog.value = true
    await fetchDataHistoryLog()
  } catch (error) {
    toast.warning('Không thể lấy lịch sử import')
    isLoadingShowLog.value = false
  }
}

const handleTableChange = (page) => {
  pagination.value.current = page.current
  pagination.value.pageSize = page.pageSize
  fetchDataHistoryLog()
}

const fetchDataHistoryLog = async () => {
  try {
    const response = await serviceStore.getHistoryLogs(pagination)
    lstData.value = response?.data?.data?.data || []
    isShowHistoryLog.value = true
    isLoadingShowLog.value = false
    pagination.value.total = response.data.data.totalPages * pagination.value.pageSize
  } catch (error) {
    lstData.value = []
    isLoadingShowLog.value = false
    message.error('Không tải danh sách lịch sử import')
  }
}

const fetchDataHistoryLogDetail = async (id) => {
  try {
    const response = await serviceStore.getHistoryLogsDetail(id)
    lstDataDetail.value = response?.data?.data || []
    isLoadingTable.value = false
    isShowHistoryLogDetail.value = true
  } catch (error) {
    isLoadingTable.value = false
    message.error('Không tải danh sách lịch sử import chi tiết')
  }
}

onMounted(() => {
  serviceStore.setData(props.data)
  serviceStore.setUrlFetch(props.fetchUrl)
})
</script>

<template>
  <a-modal v-model:open="isShowHistoryLogDetail" :width="800">
    <template #title><InfoCircleFilled class="text-primary" /> Chi tiết import excel </template>
    <template #footer>
      <a-button @click="isShowHistoryLogDetail = false" class="btn-gray">Đóng</a-button>
    </template>
    <a-table
      rowKey="id"
      class="nowrap"
      :dataSource="lstDataDetail"
      :columns="columnsDetail"
      :pagination="false"
      :scroll="{ x: 'auto' }"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'status'">
          <a-tag :color="record.status === 1 ? 'green' : 'red'">{{
            record.status === 1 ? 'Thành công' : 'Thất bại'
          }}</a-tag>
        </template>
      </template>
    </a-table>
  </a-modal>

  <a-modal v-model:open="isShowHistoryLog" :width="1000">
    <template #title><HistoryOutlined class="text-primary" /> Lịch sử import</template>
    <template #footer>
      <a-button @click="isShowHistoryLog = false" class="btn-gray">Đóng</a-button>
    </template>
    <a-table
      rowKey="id"
      class="nowrap"
      :dataSource="lstData"
      :columns="columns"
      :pagination="pagination"
      :scroll="{ x: 'auto' }"
      :loading="isLoadingTable"
      @change="handleTableChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'createdAt'">
          {{ formatDate(record.createdAt, 'dd/MM/yyyy HH:mm:ss') }}
        </template>
        <template v-if="column.dataIndex === 'status'">
          <a-tag :color="record.status == true ? 'green' : 'red'">{{
            record.status == true ? 'Thành công' : 'Thất bại'
          }}</a-tag>
        </template>
        <template v-if="column.dataIndex === 'totalSuccess'">
          <a-tag :color="record.totalSuccess > 0 ? 'green' : ''">{{ record.totalSuccess }}</a-tag>
        </template>
        <template v-if="column.dataIndex === 'totalError'">
          <a-tag :color="record.totalError > 0 ? 'red' : ''">{{ record.totalError }}</a-tag>
        </template>
        <template v-if="column.key === 'actions'">
          <a-typography-link @click="handleShowDetail(record.id)">Chi tiết</a-typography-link>
        </template>
      </template>
    </a-table>
  </a-modal>

  <a-button @click="handleShowHistoryLog" v-if="props.showHistoryLog" :loading="isLoadingShowLog"
    ><HistoryOutlined />Lịch sử import</a-button
  >
  <a-button
    @click="handleDownloadTemplate"
    v-if="props.showDownloadTemplate"
    :loading="isLoadingDownload"
    ><FileExcelOutlined />Tải xuống template</a-button
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
