<script setup>
import { ref, reactive } from 'vue'
import { CustomerServiceOutlined, PaperClipOutlined, DeleteOutlined } from '@ant-design/icons-vue'
import { message, Modal } from 'ant-design-vue'
import { API_ROUTES_SUPPORT } from '@/constants/routesConstant'
import requestAPI from '@/services/requestApiService'
import useLoadingStore from '@/stores/useLoadingStore'

const loadingStore = useLoadingStore()
const showModal = ref(false)
const submitting = ref(false)
const fileList = ref([])
const uploadLoading = ref(false)

const formData = reactive({
  title: '',
  message: '',
  files: []
})

const handleSupport = () => {
  showModal.value = true
}

const beforeUpload = (file) => {
  uploadLoading.value = true

  setTimeout(() => {
    fileList.value = [...fileList.value, file]
    formData.files = [...formData.files, file]
    uploadLoading.value = false
  }, 300)

  return false
}

const removeFile = (file) => {
  const index = fileList.value.indexOf(file)
  const newFileList = fileList.value.slice()
  newFileList.splice(index, 1)
  fileList.value = newFileList

  const fileIndex = formData.files.findIndex(f => f.uid === file.uid)
  if (fileIndex !== -1) {
    formData.files.splice(fileIndex, 1)
  }
}

const formatFileSize = (size) => {
  if (size < 1024) return size + ' B'
  if (size < 1024 * 1024) return (size / 1024).toFixed(1) + ' KB'
  if (size < 1024 * 1024 * 1024) return (size / (1024 * 1024)).toFixed(1) + ' MB'
  return (size / (1024 * 1024 * 1024)).toFixed(1) + ' GB'
}

const handleSubmit = () => {
  if (!formData.title) {
    message.error('Vui lòng nhập tiêu đề')
    return
  }

  if (!formData.message) {
    message.error('Vui lòng nhập nội dung')
    return
  }

  Modal.confirm({
    title: 'Xác nhận gửi yêu cầu hỗ trợ',
    content: 'Bạn có chắc chắn muốn gửi yêu cầu hỗ trợ này?',
    okText: 'Gửi',
    cancelText: 'Hủy',
    onOk: sendSupportRequest
  })
}

const sendSupportRequest = async () => {
  try {
    submitting.value = true
    loadingStore.show()

    const formSubmit = new FormData()
    formSubmit.append('title', formData.title)
    formSubmit.append('message', formData.message)

    formData.files.forEach(file => {
      formSubmit.append('files', file)
    })

    const response = await requestAPI.post(API_ROUTES_SUPPORT.FETCH_SEND_SUPPORT, formSubmit, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })

    message.success('Yêu cầu hỗ trợ đã được gửi thành công!')
    resetForm()
    showModal.value = false

  } catch (error) {
    message.error(error.response?.data?.message || 'Có lỗi xảy ra khi gửi yêu cầu hỗ trợ')
  } finally {
    submitting.value = false
    loadingStore.hide()
  }
}

const resetForm = () => {
  formData.title = ''
  formData.message = ''
  formData.files = []
  fileList.value = []
}

const handleCancel = () => {
  showModal.value = false
  resetForm()
}
</script>

<template>
  <div class="support-button-container">
    <a-tooltip
      title="Hỗ trợ người dùng"
      placement="left"
      :mouseEnterDelay="0.5"
    >
      <a-button
        type="default"
        shape="circle"
        size="large"
        class="support-button"
        @click="handleSupport"
        aria-label="Hỗ trợ khách hàng"
      >
        <CustomerServiceOutlined />
      </a-button>
    </a-tooltip>

    <a-modal
      v-model:visible="showModal"
      :confirm-loading="submitting"
      @cancel="handleCancel"
      @ok="handleSubmit"
      okText="Gửi yêu cầu"
      cancelText="Hủy"
      width="600px"
    >
      <template #title>
        <div class="modal-title">
          <CustomerServiceOutlined />
          <span>Yêu cầu hỗ trợ</span>
        </div>
      </template>
      <a-form layout="vertical">
          <a-form-item label="Tiêu đề" required>
            <a-input
              v-model:value="formData.title"
              placeholder="Nhập tiêu đề yêu cầu"
              :maxLength="255"
            />
          </a-form-item>

          <a-form-item label="Nội dung" required>
            <a-textarea
              v-model:value="formData.message"
              placeholder="Mô tả chi tiết vấn đề của bạn..."
              :rows="6"
            />
          </a-form-item>

          <a-form-item label="Tệp đính kèm (tối đa 5 tệp)">
            <a-spin :spinning="uploadLoading" size="small">
              <a-upload
                :file-list="fileList"
                :before-upload="beforeUpload"
                :multiple="true"
                :disabled="fileList.length >= 5 || uploadLoading"
                :show-upload-list="false"
              >
                <a-button :disabled="fileList.length >= 5 || uploadLoading">
                  <PaperClipOutlined /> Tải lên tệp
                </a-button>
              </a-upload>
            </a-spin>

          <!-- Display files -->
          <div class="file-list" v-if="fileList.length > 0">
            <div v-for="(file, index) in fileList" :key="index" class="file-item">
              <div class="file-info">
                <PaperClipOutlined />
                <span class="file-name">{{ file.name }}</span>
                <span class="file-size">({{ formatFileSize(file.size) }})</span>
              </div>
              <a-button
                type="text"
                size="small"
                danger
                @click="removeFile(file)"
                title="Xóa tệp"
              >
                <DeleteOutlined />
              </a-button>
            </div>
          </div>

          <div class="upload-hint" v-if="fileList.length >= 5">
            Đã đạt giới hạn số lượng tệp tải lên
          </div>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<style scoped>
.support-button-container {
  position: fixed;
  bottom: 30px;
  right: 30px;
  z-index: 1000;
}

.modal-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 500;
}

.modal-title .anticon {
  font-size: 18px;
  color: #666;
}

.support-button {
  width: 60px;
  height: 60px;
  background-color: white;
  border-color: #d9d9d9;
  color: #666;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  transition: all 0.3s ease;
}

.support-button:hover {
  transform: scale(1.1);
  background-color: #f5f5f5;
  border-color: #bfbfbf;
  color: #333;
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.2);
}

.file-list {
  margin-top: 16px;
  max-height: 150px;
  overflow-y: auto;
  border: 1px solid #f0f0f0;
  border-radius: 4px;
  padding: 8px;
}

.file-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 6px 8px;
  border-bottom: 1px solid #f5f5f5;
  transition: background-color 0.3s;
}

.file-item:last-child {
  border-bottom: none;
}

.file-item:hover {
  background-color: #fafafa;
}

.file-info {
  display: flex;
  align-items: center;
  gap: 8px;
  overflow: hidden;
}

.file-name {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 300px;
}

.file-size {
  color: #999;
  font-size: 12px;
}

.upload-hint {
  color: #ff4d4f;
  margin-top: 8px;
  font-size: 12px;
}

/* Responsive */
@media (max-width: 768px) {
  .support-button-container {
    bottom: 20px;
    right: 20px;
  }

  .support-button {
    width: 50px;
    height: 50px;
  }

  .file-name {
    max-width: 200px;
  }
}

@media (max-width: 480px) {
  .support-button-container {
    bottom: 15px;
    right: 15px;
  }

  .support-button {
    width: 45px;
    height: 45px;
  }

  .file-name {
    max-width: 150px;
  }
}
</style>
