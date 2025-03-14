<script setup>
import useImportExcelStore from '@/stores/useImportExcelStore'
import {
  CheckCircleFilled,
  ClockCircleOutlined,
  CloseOutlined,
  DownOutlined,
  ExclamationCircleFilled,
  FileExcelOutlined,
  UpOutlined,
} from '@ant-design/icons-vue'
import { Modal } from 'ant-design-vue'
import { ref } from 'vue'

const serviceStore = useImportExcelStore()

const isShow = ref(true)

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
</script>

<template>
  <div class="excel-import-list" v-if="serviceStore.size() > 0 && !serviceStore.isStoped()">
    <div class="excel-import-list__header">
      <span>Đã tải lên {{ serviceStore.size() }} mục</span>
      <span class="ms-auto">
        <a-button class="btn-action" shape="circle" @click="handleToggle">
          <DownOutlined v-show="isShow" class="text-gray" />
          <UpOutlined v-show="!isShow" class="text-gray" />
        </a-button>
      </span>
      <span class="ms-2">
        <a-button class="btn-action" shape="circle" @click="handleClose">
          <CloseOutlined class="text-gray" />
        </a-button>
      </span>
    </div>
    <div class="excel-import-list__body" v-show="isShow">
      <div class="d-flex align-items-center gap-3" v-for="o in serviceStore.queue" :key="o.id">
        <FileExcelOutlined class="text-primary fs-5" />
        <div class="item-file">
          {{ o.file.name }}
        </div>
        <div class="ms-auto">
          <a-button v-if="o.status === 2" shape="circle">
            <CheckCircleFilled class="text-success fs-5" />
          </a-button>
          <a-button v-else-if="o.status === 3" shape="circle">
            <ExclamationCircleFilled class="text-danger fs-5"
          /></a-button>
          <a-button v-else-if="o.status === 1" type="primary" shape="circle" loading />
          <a-button v-else shape="circle">
            <ClockCircleOutlined class="text-gray fs-5" />
          </a-button>
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
.ant-btn {
  opacity: 1;
  background: #fff;
  color: #03a9f4;
  cursor: pointer;
  box-shadow: none;
  border: 0;
}
.ant-btn:hover {
  background: #fff;
}

.item-file {
  white-space: nowrap;
  text-overflow: ellipsis;
  overflow: hidden;
}
.btn-action {
  border: 0;
  background: #f6f8fa;
  box-shadow: none;
}
.btn-action:hover {
  background-color: #e5e8ec;
}
</style>
