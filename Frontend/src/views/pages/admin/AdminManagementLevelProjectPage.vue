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
} from '@ant-design/icons-vue'
import { DEFAULT_PAGINATION } from '@/constants'
import useBreadcrumbStore from '@/stores/useBreadCrumbStore'
import useLoadingStore from '@/stores/useLoadingStore'
import { API_ROUTES_ADMIN } from '@/constants/adminConstant'
import { ROUTE_NAMES } from '@/router/adminRoute'
import { GLOBAL_ROUTE_NAMES } from '@/constants/routesConstant'

const breadcrumbStore = useBreadcrumbStore()
const loadingStore = useLoadingStore()

/* ----------------- Data & Reactive Variables ----------------- */
const levels = ref([])

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

const columns = ref([
  { title: '#', dataIndex: 'orderNumber', key: 'orderNumber', width: 50 },
  { title: 'Mã', dataIndex: 'code', key: 'code', width: 150, ellipsis: true },
  { title: 'Tên cấp dự án', dataIndex: 'name', key: 'name', width: 200, ellipsis: true },
  { title: 'Mô tả', dataIndex: 'description', key: 'description', width: 250, ellipsis: true },
  { title: 'Trạng thái', dataIndex: 'status', key: 'status', width: 100, ellipsis: true },
  { title: 'Chức năng', key: 'actions' },
])

const breadcrumb = ref([
  {
    name: GLOBAL_ROUTE_NAMES.ADMIN_PAGE,
    breadcrumbName: 'Ban đào tạo',
  },
  {
    name: ROUTE_NAMES.MANAGEMENT_LEVEL_PROJECT,
    breadcrumbName: 'Quản lý cấp độ dự án',
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
    })
    .catch((error) => {
      message.error(error.response?.data?.message || 'Lỗi khi lấy danh sách cấp dự án')
    })
    .finally(() => {
      loadingStore.hide()
    })
}

const clearNewLevelForm = () => {
  newLevel.name = ''
  newLevel.description = ''
}

const handleTableChange = (pageInfo) => {
  pagination.current = pageInfo.current
  pagination.pageSize = pageInfo.pageSize
  fetchLevels()
}

const submitAddLevel = () => {
  if (!newLevel.name || !newLevel.name.trim()) {
    message.error('Vui lòng nhập tên cấp dự án')
    return
  }
  loadingStore.show()
  requestAPI
    .post(API_ROUTES_ADMIN.FETCH_DATA_LEVEL_PROJECT, newLevel)
    .then((response) => {
      message.success(response.data.message || 'Thêm cấp dự án thành công')
      modalAdd.value = false
      fetchLevels()
      clearNewLevelForm()
    })
    .catch((error) => {
      message.error(error.response?.data?.message || 'Lỗi khi thêm cấp dự án')
    })
    .finally(() => {
      loadingStore.hide()
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
      message.error(error.response?.data?.message || 'Lỗi khi lấy chi tiết cấp dự án')
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
      message.error(error.response?.data?.message || 'Lỗi khi lấy thông tin cấp dự án')
    })
    .finally(() => {
      loadingStore.hide()
    })
}

const submitUpdateLevel = () => {
  if (!detailLevel.name) {
    message.error('Vui lòng nhập tên cấp dự án')
    return
  }
  loadingStore.show()
  requestAPI
    .put(`${API_ROUTES_ADMIN.FETCH_DATA_LEVEL_PROJECT}/${detailLevel.id}`, detailLevel)
    .then((response) => {
      message.success(response.data.message || 'Cập nhật cấp dự án thành công')
      modalUpdate.value = false
      fetchLevels()
    })
    .catch((error) => {
      message.error(error.response?.data?.message || 'Lỗi khi cập nhật cấp dự án')
    })
    .finally(() => {
      loadingStore.hide()
    })
}

const confirmChangeStatus = (record) => {
  Modal.confirm({
    title: 'Xác nhận đổi trạng thái',
    content: `Bạn có chắc muốn đổi trạng thái cho cấp dự án "${record.name}" ?`,
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
      message.success(response.data.message || 'Chuyển trạng thái cấp dự án thành công')
      fetchLevels()
    })
    .catch((error) => {
      message.error(error.response?.data?.message || 'Lỗi khi thay đổi trạng thái cấp dự án')
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
  Object.keys(filter).forEach(key => {
    filter[key] = ''
  })
  pagination.current = 1
  fetchLevels()
}

onMounted(() => {
  breadcrumbStore.setRoutes(breadcrumb.value)
  fetchLevels()
})
</script>

<template>
  <!-- Modal Thêm cấp dự án -->
  <a-modal v-model:open="modalAdd" title="Thêm cấp dự án" @ok="submitAddLevel">
    <a-form :model="newLevel" layout="vertical">
      <a-form-item label="Tên cấp dự án" required>
        <a-input v-model:value="newLevel.name" placeholder="Nhập tên cấp dự án" />
      </a-form-item>
      <a-form-item label="Mô tả">
        <a-textarea v-model:value="newLevel.description" placeholder="Nhập mô tả" :rows="4" />
      </a-form-item>
    </a-form>
  </a-modal>

  <!-- Modal Cập nhật cấp dự án -->
  <a-modal v-model:open="modalUpdate" title="Cập nhật cấp dự án" @ok="submitUpdateLevel">
    <a-form :model="detailLevel" layout="vertical">
      <a-form-item label="Tên cấp dự án" required>
        <a-input v-model:value="detailLevel.name" placeholder="Nhập tên cấp dự án" />
      </a-form-item>
      <a-form-item label="Mô tả">
        <a-textarea v-model:value="detailLevel.description" placeholder="Nhập mô tả" :rows="4" />
      </a-form-item>
    </a-form>
  </a-modal>

  <!-- Modal Xem chi tiết cấp dự án -->
  <a-modal v-model:open="modalDetail" title="Chi tiết cấp dự án" :footer="null">
    <a-descriptions bordered :column="1">
      <a-descriptions-item label="Mã">{{ detailLevel.code }}</a-descriptions-item>
      <a-descriptions-item label="Tên">{{ detailLevel.name }}</a-descriptions-item>
      <a-descriptions-item label="Mô tả">{{ detailLevel.description }}</a-descriptions-item>
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
        <a-card :bordered="false" class="cart mb-3">
          <template #title> <FilterFilled /> Bộ lọc tìm kiếm </template>
          <div class="row g-3 filter-container">
            <div class="col-md-6 col-sm-6">
              <label class="label-title">Từ khoá:</label>
              <a-input
                v-model:value="filter.name"
                placeholder="Nhập tên hoặc mã cấp độ"
                allowClear
                @change="fetchLevels"
              />
            </div>
            <div class="col-md-6 col-sm-6">
              <label class="label-title">Trạng thái:</label>
              <a-select
                v-model:value="filter.status"
                placeholder="Chọn trạng thái"
                allowClear
                style="width: 100%"
                @change="fetchLevels"
              >
                <a-select-option :value="''">Tất cả trạng thái</a-select-option>
                <a-select-option value="1">Hoạt động</a-select-option>
                <a-select-option value="0">Không hoạt động</a-select-option>
              </a-select>
            </div>
          </div>
          <div class="row">
            <div class="col-12">
              <div class="d-flex justify-content-center flex-wrap gap-2 mt-3">
                <a-button class="btn-light" @click="fetchLevels">
                  <FilterFilled /> Lọc
                </a-button>
                <a-button class="btn-gray" @click="handleClearFilter"> Huỷ lọc </a-button>
              </div>
            </div>
          </div>
        </a-card>
      </div>

      <!-- Danh sách cấp dự án -->
      <div class="col-12">
        <a-card :bordered="false" class="cart">
          <template #title> <UnorderedListOutlined /> Danh sách cấp dự án </template>
          <div class="d-flex justify-content-end mb-3">
            <a-tooltip title="Thêm cấp dự án">
              <a-button type="primary" @click="modalAdd = true">
                <PlusOutlined /> Thêm mới
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
            :scroll="{ y: 500, x: 'auto' }"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.dataIndex === 'status'">
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
