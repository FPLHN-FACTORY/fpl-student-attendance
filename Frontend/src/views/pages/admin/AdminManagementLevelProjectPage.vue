<script setup>
import { ref, reactive, onMounted } from 'vue'
import { message, Modal } from 'ant-design-vue'
import requestAPI from '@/services/requestApiService'
import {
  PlusOutlined,
  UnorderedListOutlined,
  FilterFilled,
  EditFilled,
  EyeFilled,
  SearchOutlined,
} from '@ant-design/icons-vue'
import { DEFAULT_PAGINATION } from '@/constants'
import useBreadcrumbStore from '@/stores/useBreadCrumbStore'
import useLoadingStore from '@/stores/useLoadingStore'
import { API_ROUTES_ADMIN } from '@/constants/adminConstant'
import { ROUTE_NAMES } from '@/router/adminRoute'
import { GLOBAL_ROUTE_NAMES } from '@/constants/routesConstant'
import { autoAddColumnWidth } from '@/utils/utils'
import { validateFormSubmission } from '@/utils/validationUtils'

const breadcrumbStore = useBreadcrumbStore()
const loadingStore = useLoadingStore()

/* ----------------- Data & Reactive Variables ----------------- */
const levels = ref([])

const countFilter = ref(0)

const filter = reactive({
  name: '',
  status: null,
})

const pagination = reactive({
  ...DEFAULT_PAGINATION,
})

const modalAdd = ref(false)
const modalUpdate = ref(false)
const modalDetail = ref(false)

const newLevel = reactive({
  name: '',
  description: '',
})

const detailLevel = reactive({
  id: '',
  name: '',
  code: '',
  description: '',
  status: 1, // Mặc định là 1 (Hoạt động)
  createdAt: '',
  updatedAt: '',
})

const columns = ref(
  autoAddColumnWidth([
    { title: '#', key: 'rowNumber' },
    { title: 'Mã nhóm dự án', dataIndex: 'code', key: 'code' },
    { title: 'Tên nhóm dự án', dataIndex: 'name', key: 'name' },
    { title: 'Mô tả', dataIndex: 'description', key: 'description' },
    { title: 'Trạng thái', dataIndex: 'status', key: 'status' },
    { title: 'Chức năng', key: 'actions' },
  ]),
)

const breadcrumb = ref([
  {
    name: GLOBAL_ROUTE_NAMES.ADMIN_PAGE,
    breadcrumbName: 'Admin',
  },
  {
    name: ROUTE_NAMES.MANAGEMENT_LEVEL_PROJECT,
    breadcrumbName: 'Quản lý nhóm dự án',
  },
])

/* ----------------- Methods ----------------- */
const fetchLevels = () => {
  loadingStore.show()
  requestAPI
    .get(`${API_ROUTES_ADMIN.FETCH_DATA_LEVEL_PROJECT}/list`, {
      params: {
        ...filter,
        page: pagination.current,
        size: pagination.pageSize,
      },
    })
    .then((response) => {
      const result = response.data.data
      levels.value = result.data
      // Tổng số bản ghi
      pagination.total =
        result.totalElements || result.totalItems || result.totalPages * pagination.pageSize

      countFilter.value = result.totalItems
    })
    .catch((error) => {
      message.error(error.response?.data?.message || 'Lỗi khi lấy danh sách nhóm dự án')
    })
    .finally(() => {
      loadingStore.hide()
    })
}

const handleTableChange = (pageInfo) => {
  pagination.current = pageInfo.current
  pagination.pageSize = pageInfo.pageSize
  fetchLevels()
}

const handleAddLevelProject = () => {
  // Validate required fields with whitespace and number-only check
  const validation = validateFormSubmission(newLevel, [
    { key: 'name', label: 'Tên nhóm dự án' },
  ])

  if (!validation.isValid) {
    message.error(validation.message)
    return
  }
  Modal.confirm({
    title: 'Xác nhận thêm mới',
    content: 'Bạn có chắc chắn muốn thêm nhóm dự án mới này?',
    okText: 'Tiếp tục',
    cancelText: 'Hủy bỏ',
    onOk() {
      loadingStore.show()
      requestAPI
        .post(API_ROUTES_ADMIN.FETCH_DATA_LEVEL_PROJECT, newLevel)
        .then((response) => {
          message.success(response.data.message || 'Thêm nhóm dự án thành công')
          modalAdd.value = false
          fetchLevels()
        })
        .catch((error) => {
          message.error(error.response?.data?.message || 'Lỗi khi thêm nhóm dự án')
        })
        .finally(() => {
          loadingStore.hide()
        })
    },
  })
}

const handleDetailLevel = (record) => {
  loadingStore.show()
  requestAPI
    .get(`${API_ROUTES_ADMIN.FETCH_DATA_LEVEL_PROJECT}/${record.id}`)
    .then((response) => {
      Object.assign(detailLevel, response.data.data)
      modalDetail.value = true
    })
    .catch((error) => {
      message.error(error.response?.data?.message || 'Lỗi khi lấy chi tiết nhóm dự án')
    })
    .finally(() => {
      loadingStore.hide()
    })
}

const prepareUpdateLevel = (record) => {
  loadingStore.show()
  requestAPI
    .get(`${API_ROUTES_ADMIN.FETCH_DATA_LEVEL_PROJECT}/${record.id}`)
    .then((response) => {
      Object.assign(detailLevel, response.data.data)
      modalUpdate.value = true
    })
    .catch((error) => {
      message.error(error.response?.data?.message || 'Lỗi khi lấy thông tin nhóm dự án')
    })
    .finally(() => {
      loadingStore.hide()
    })
}

const submitUpdateLevel = () => {
  // Validate required fields with whitespace and number-only check
  const validation = validateFormSubmission(detailLevel, [
    { key: 'name', label: 'Tên nhóm dự án' },
  ])

  if (!validation.isValid) {
    message.error(validation.message)
    return
  }
  Modal.confirm({
    title: 'Xác nhận cập nhật',
    content: 'Bạn có chắc chắn muốn cập nhật thông tin nhóm dự án này?',
    okText: 'Tiếp tục',
    cancelText: 'Hủy bỏ',
    onOk() {
      loadingStore.show()
      requestAPI
        .put(`${API_ROUTES_ADMIN.FETCH_DATA_LEVEL_PROJECT}/${detailLevel.id}`, detailLevel)
        .then((response) => {
          message.success(response.data.message || 'Cập nhật nhóm dự án thành công')
          modalUpdate.value = false
          fetchLevels()
        })
        .catch((error) => {
          message.error(error.response?.data?.message || 'Lỗi khi cập nhật nhóm dự án')
        })
        .finally(() => {
          loadingStore.hide()
        })
    },
  })
}

const confirmChangeStatus = (record) => {
  Modal.confirm({
    title: 'Xác nhận đổi trạng thái',
    content: `Bạn có chắc muốn đổi trạng thái cho nhóm dự án "${record.name}" ?`,
    onOk() {
      changeStatus(record.id)
    },
  })
}

const changeStatus = (id) => {
  loadingStore.show()
  requestAPI
    .delete(`${API_ROUTES_ADMIN.FETCH_DATA_LEVEL_PROJECT}/${id}`)
    .then((response) => {
      message.success(response.data.message || 'Chuyển trạng thái nhóm dự án thành công')
      fetchLevels()
    })
    .catch((error) => {
      message.error(error.response?.data?.message || 'Lỗi khi thay đổi trạng thái nhóm dự án')
    })
    .finally(() => {
      loadingStore.hide()
    })
}

const formatDate = (timestamp) => {
  if (!timestamp) return 'Không xác định'
  return new Date(timestamp).toLocaleString()
}

const getStatusText = (status) => {
  return status === 'ACTIVE' ? 'Hoạt động' : 'Không hoạt động'
}

const getStatusColor = (status) => {
  return status === 'ACTIVE' ? 'green' : 'red'
}

const handleClearFilter = () => {
  // Clear all filter values
  Object.keys(filter).forEach((key) => {
    filter[key] = null
  })
  // Không reset về trang 1 khi hủy lọc để giữ nguyên dữ liệu hiện tại
  fetchLevels()
}

const handleShowModalAdd = () => {
  newLevel.name = null
  newLevel.description = null

  modalAdd.value = true
}

onMounted(() => {
  breadcrumbStore.setRoutes(breadcrumb.value)
  fetchLevels()
})
</script>

<template>
  <!-- Modal Thêm cấp dự án -->
  <a-modal
    v-model:open="modalAdd"
    @ok="handleAddLevelProject"
    :okButtonProps="{ loading: loadingStore.isLoading }"
  >
    <template #title>
      <PlusOutlined class="me-2 text-primary" />
      Thêm nhóm dự án
    </template>
    <a-form :model="newLevel" layout="vertical">
      <a-form-item label="Tên nhóm dự án" required>
        <a-input
          v-model:value="newLevel.name"
          placeholder="Nhập tên nhóm dự án"
          @keyup.enter="handleAddLevelProject"
        />
      </a-form-item>
      <a-form-item label="Mô tả">
        <a-textarea v-model:value="newLevel.description" placeholder="Nhập mô tả" :rows="4" />
      </a-form-item>
    </a-form>
  </a-modal>

  <!-- Modal Cập nhật cấp dự án -->
  <a-modal
    v-model:open="modalUpdate"
    :okButtonProps="{ loading: loadingStore.isLoading }"
    @ok="submitUpdateLevel"
  >
    <template #title>
      <EditFilled class="me-2 text-primary" />
      Cập nhật nhóm dự án
    </template>
    <a-form :model="detailLevel" layout="vertical">
      <a-form-item label="Tên nhóm dự án" required>
        <a-input
          v-model:value="detailLevel.name"
          placeholder="Nhập tên nhóm dự án"
          @keyup.enter="submitUpdateLevel"
        />
      </a-form-item>
      <a-form-item label="Mô tả">
        <a-textarea v-model:value="detailLevel.description" placeholder="Nhập mô tả" :rows="4" />
      </a-form-item>
    </a-form>
  </a-modal>

  <!-- Modal Xem chi tiết cấp dự án -->
  <a-modal v-model:open="modalDetail" title="Chi tiết nhóm dự án" :footer="null">
    <a-descriptions bordered :column="1">
      <a-descriptions-item label="Mã">{{ detailLevel.code }}</a-descriptions-item>
      <a-descriptions-item label="Tên">{{ detailLevel.name }}</a-descriptions-item>
      <a-descriptions-item label="Mô tả">{{
        detailLevel.description || 'Không có mô tả'
      }}</a-descriptions-item>
      <a-descriptions-item label="Trạng thái">
        <a-tag :color="getStatusColor(detailLevel.status)">
          {{ getStatusText(detailLevel.status) }}
        </a-tag>
      </a-descriptions-item>
      <a-descriptions-item label="Ngày tạo">{{
        formatDate(detailLevel.createdAt)
      }}</a-descriptions-item>
      <a-descriptions-item label="Ngày cập nhật">{{
        formatDate(detailLevel.updatedAt)
      }}</a-descriptions-item>
    </a-descriptions>
  </a-modal>

  <div class="container-fluid">
    <div class="row g-3">
      <div class="col-12">
        <a-card :bordered="false" class="cart no-body-padding">
          <a-collapse ghost>
            <a-collapse-panel>
              <template #header><FilterFilled /> Bộ lọc ({{ countFilter }})</template>
              <div class="row g-3 filter-container">
                <div class="col-md-8 col-sm-6">
                  <div class="label-title">Từ khoá:</div>
                  <a-input
                    v-model:value="filter.name"
                    placeholder="Tìm theo tên hoặc mã nhóm dự án"
                    allowClear
                    @change="fetchLevels"
                  >
                    <template #prefix>
                      <SearchOutlined />
                    </template>
                  </a-input>
                </div>
                <div class="col-md-4 col-sm-6">
                  <div class="label-title">Trạng thái:</div>
                  <a-select
                    v-model:value="filter.status"
                    placeholder="-- Tất cả trạng thái --"
                    class="w-100"
                    @change="fetchLevels"
                  >
                    <a-select-option :value="null">-- Tất cả trạng thái --</a-select-option>
                    <a-select-option value="1">Hoạt động</a-select-option>
                    <a-select-option value="0">Không hoạt động</a-select-option>
                  </a-select>
                </div>
                <div class="col-12">
                  <div class="d-flex justify-content-center flex-wrap gap-2">
                    <a-button class="btn-light" @click="fetchLevels">
                      <FilterFilled /> Lọc
                    </a-button>
                    <a-button class="btn-gray" @click="handleClearFilter"> Huỷ lọc </a-button>
                  </div>
                </div>
              </div>
            </a-collapse-panel>
          </a-collapse>
        </a-card>
      </div>

      <!-- Danh sách cấp dự án -->
      <div class="col-12">
        <a-card :bordered="false" class="cart">
          <template #title> <UnorderedListOutlined /> Danh sách nhóm dự án </template>

          <div class="d-flex justify-content-end mb-2">
            <a-tooltip title="Thêm nhóm dự án">
              <a-button type="primary" @click="handleShowModalAdd">
                <PlusOutlined /> Thêm nhóm dự án
              </a-button>
            </a-tooltip>
          </div>

          <a-table
            class="nowrap"
            rowKey="id"
            :dataSource="levels"
            :columns="columns"
            :pagination="pagination"
            @change="handleTableChange"
            :loading="loadingStore.isLoading"
            :scroll="{ x: 'auto' }"
          >
            <template #bodyCell="{ column, record, index }">
              <template v-if="column.key === 'rowNumber'">
                {{ (pagination.current - 1) * pagination.pageSize + index + 1 }}
              </template>
              <template v-else-if="column.dataIndex === 'status'">
                <span class="nowrap">
                  <a-switch
                    class="me-2"
                    :checked="record.status === 'ACTIVE' || record.status === 1"
                    @change="confirmChangeStatus(record)"
                  />
                  <a-tag
                    :color="record.status === 'ACTIVE' || record.status === 1 ? 'green' : 'red'"
                  >
                    {{
                      record.status === 'ACTIVE' || record.status === 1
                        ? 'Hoạt động'
                        : 'Không hoạt động'
                    }}
                  </a-tag>
                </span>
              </template>

              <template v-else-if="column.dataIndex === 'description'">
                <template v-if="record.description">
                  <span>{{ record.description }}</span>
                </template>
                <template v-else>
                  <span>Không có mô tả</span>
                </template>
              </template>

              <template v-else-if="column.key === 'actions'">
                <a-space>
                  <a-tooltip title="Xem chi tiết">
                    <a-button
                      type="text"
                      class="btn-outline-primary"
                      @click="handleDetailLevel(record)"
                    >
                      <EyeFilled />
                    </a-button>
                  </a-tooltip>
                  <a-tooltip title="Chỉnh sửa">
                    <a-button
                      type="text"
                      class="btn-outline-info"
                      @click="prepareUpdateLevel(record)"
                    >
                      <EditFilled />
                    </a-button>
                  </a-tooltip>
                </a-space>
              </template>
            </template>
          </a-table>
        </a-card>
      </div>
    </div>
  </div>
</template>
