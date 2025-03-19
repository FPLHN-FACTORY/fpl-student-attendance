<script setup>
import useImportExcelStore from '@/stores/useImportExcelStore'
import {
  CloseOutlined,
  DownOutlined,
  ExclamationCircleFilled,
  FileExcelOutlined,
  InfoCircleFilled,
  RedoOutlined,
  UpOutlined,
} from '@ant-design/icons-vue'
import { Modal } from 'ant-design-vue'
import { ref } from 'vue'

const serviceStore = useImportExcelStore()

const isShow = ref(true)
const isShowLog = ref(false)
const itemLog = ref(false)

const columns = ref([
  { title: 'Dòng', dataIndex: 'index', key: 'index', width: 80 },
  { title: 'Trạng thái', dataIndex: 'status', key: 'status', width: 120 },
  { title: 'Nội dung', dataIndex: 'message', key: 'message' },
])

const handleToggle = () => {
  isShow.value = !isShow.value
}

const handleClose = () => {
  if (!serviceStore.isProcessing()) {
    return serviceStore.stop()
  }

  Modal.confirm({
    title: 'Cảnh báo',
    type: 'danger',
    content: `Quá trình tải lên vẫn đang tiếp tục. Bạn thực sự muốn dừng?`,
    okText: 'Tiếp tục',
    cancelText: 'Hủy bỏ',
    onOk() {
      serviceStore.stop()
    },
  })
}

const handleCancelUpload = (id) => {
  serviceStore.cancelTask(id)
}

const handleRetryUpload = (id) => {
  serviceStore.retryTask(id)
}

const handleShowLog = (item) => {
  itemLog.value = item
  isShowLog.value = true
}
</script>

<template>
  <a-modal v-model:open="isShowLog" :width="800">
    <template #title><InfoCircleFilled class="text-primary" /> {{ itemLog.file.name }} </template>
    <template #footer>
      <a-button @click="isShowLog = false" class="btn-gray">Đóng</a-button>
    </template>
    <a-table
      rowKey="id"
      class="nowrap"
      :dataSource="itemLog.items"
      :columns="columns"
      :pagination="false"
      :scroll="{ y: 500, x: 'auto' }"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'status'">
          <a-tag :color="record.status === serviceStore.STATUS_LOG.SUCCESS ? 'green' : 'red'">{{
            record.status === serviceStore.STATUS_LOG.SUCCESS ? 'Thành công' : 'Thất bại'
          }}</a-tag>
        </template>
      </template>
    </a-table>
  </a-modal>

  <div class="excel-import-list" v-if="serviceStore.size() > 0 && !serviceStore.isStoped()">
    <div class="excel-import-list__header">
      <span v-if="serviceStore.isProcessing()"
        >Đang tải lên {{ serviceStore.countProcessing() }} mục</span
      >
      <span v-else>Đã tải lên {{ serviceStore.countComplete() }} mục</span>
      <span class="ms-auto">
        <a-button class="btn-action-header" shape="circle" @click="handleToggle">
          <DownOutlined v-show="isShow" class="text-gray" />
          <UpOutlined v-show="!isShow" class="text-gray" />
        </a-button>
      </span>
      <span class="ms-2">
        <a-button class="btn-action-header" shape="circle" @click="handleClose">
          <CloseOutlined class="text-gray" />
        </a-button>
      </span>
    </div>
    <div class="excel-import-list__body" v-show="isShow">
      <div class="d-flex align-items-center gap-3 mb-2" v-for="o in serviceStore.queue" :key="o.id">
        <FileExcelOutlined class="text-primary fs-5" />
        <div class="item-file">
          {{ o.file.name }}
        </div>
        <div class="ms-auto">
          <div class="btn-action-item">
            <template v-if="o.status === serviceStore.STATUS_PROCESS.UPLOADING">
              <a-progress type="circle" :percent="o.percent" :size="28" :strokeWidth="5" />
              <CloseOutlined class="btn-cancel" @click="handleCancelUpload(o.id)" />
            </template>
            <template v-else-if="o.status === serviceStore.STATUS_PROCESS.COMPLETE">
              <a-progress
                type="circle"
                :percent="100"
                :size="22"
                :strokeWidth="4"
                @click="handleShowLog(o)"
              />
            </template>
            <template v-else-if="o.status === serviceStore.STATUS_PROCESS.ERROR">
              <ExclamationCircleFilled class="text-danger" />
              <RedoOutlined class="btn-retry" @click="handleRetryUpload(o.id)" />
            </template>
            <template v-else-if="o.status === serviceStore.STATUS_PROCESS.CANCEL">
              <span class="txt-desc">Đã huỷ tải lên</span>
            </template>
            <template v-else>
              <a-progress type="circle" :size="28" :strokeWidth="5" :format="() => ''" />
              <CloseOutlined class="btn-cancel" @click="handleCancelUpload(o.id)" />
            </template>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.excel-import-list {
  position: fixed;
  bottom: 0;
  right: 14px;
  z-index: 99;
  width: 400px;
  max-width: 100%;
  background: #fff;
  overflow: hidden;
  box-shadow:
    rgba(67, 86, 101, 0.3) 0 1px 2px 0,
    rgba(56, 89, 113, 0.15) 0 2px 6px 2px;
  border-radius: 10px 10px 0 0;
}
.excel-import-list__header {
  display: flex;
  align-items: center;
  background: #f6f8fa;
  padding: 16px;
  padding-right: 8px;
  color: rgba(0, 0, 0, 0.88);
  font-weight: 600;
  font-size: 16px;
}
.excel-import-list__body {
  padding: 1rem !important;
  max-height: 290px;
  overflow-y: auto;
}
/* .ant-btn {
  opacity: 1;
  background: #fff;
  color: #03a9f4;
  cursor: pointer;
  box-shadow: none;
  border: 0;
}
.ant-btn:hover {
  background: #fff;
} */

.item-file {
  white-space: nowrap;
  text-overflow: ellipsis;
  overflow: hidden;
}
.btn-action-header {
  border: 0;
  background: #f6f8fa;
  box-shadow: none;
}
.btn-action-header:hover {
  background-color: #e5e8ec;
}

.btn-action-item {
  display: flex;
  font-size: 1.2rem;
  align-items: center;
  justify-content: center;
  padding: 2px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
}

.btn-retry:hover,
.btn-cancel:hover {
  background: #f1f3f5;
  box-shadow: rgba(67, 86, 101, 0.3) 0 1px 2px 0;
}
.btn-cancel {
  display: none;
  width: 100%;
  height: 100%;
  position: absolute;
  align-items: center;
  justify-content: center;
  background: #f6f8fa;
  border-radius: 50%;
  color: #e91e63;
}
.btn-retry {
  display: none;
  width: 100%;
  height: 100%;
  position: absolute;
  align-items: center;
  justify-content: center;
  background: #f6f8fa;
  border-radius: 50%;
  color: #ff9800;
}
.btn-action-item:hover .btn-retry,
.btn-action-item:hover .btn-cancel {
  display: flex;
}

.txt-desc {
  font-size: 0.8rem;
  opacity: 0.8;
}
</style>
