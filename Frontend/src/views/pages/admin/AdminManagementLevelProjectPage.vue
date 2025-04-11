<script setup>
import { ref, reactive, onMounted } from 'vue'
import { message, Modal } from 'ant-design-vue'
import requestAPI from '@/services/requestApiService'
import {
  PlusOutlined,
  EyeOutlined,
  EditOutlined,
  DeleteOutlined,
  UnorderedListOutlined,
  FilterFilled,
  SyncOutlined,
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
  status: null, // Sử dụng null thay vì chuỗi rỗng để phù hợp với số
})

const pagination = reactive({
  ...DEFAULT_PAGINATION,
})

const modalAdd = ref(false)
const modalUpdate = ref(false)
const modalDetail = ref(false)

const newLevel = reactive({
  name: '',
  code: '',
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
  { title: '#', dataIndex: 'rowNumber', key: 'rowNumber', width: 50 },
  { title: 'Tên cấp dự án', dataIndex: 'name', key: 'name', width: 200 },
  { title: 'Mã cấp dự án', dataIndex: 'code', key: 'code', width: 150 },
  { title: 'Mô tả', dataIndex: 'description', key: 'description', width: 250 },
  { title: 'Trạng thái', dataIndex: 'status', key: 'status', width: 100 },
  { title: 'Chức năng', key: 'actions', width: 150 },
])

const breadcrumb = ref([
{
    name: GLOBAL_ROUTE_NAMES.ADMIN_PAGE,
    breadcrumbName: 'Ban đào tạo',
  },
  {
    name: ROUTE_NAMES.MANAGEMENT_LEVEL_PROJECT,
    breadcrumbName: 'Học kỳ',
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
      console.log(filter);
      
      const result = response.data.data
      levels.value = result.data
      // Tổng số bản ghi
      pagination.total =
        result.totalElements || result.totalItems || (result.totalPages * pagination.pageSize)
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
  newLevel.code = ''
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
      <a-form-item label="Mã cấp dự án">
        <a-input v-model:value="newLevel.code" placeholder="Nhập mã cấp dự án" />
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
      <a-form-item label="Mã cấp dự án">
        <a-input v-model:value="detailLevel.code" placeholder="Nhập mã cấp dự án" />
      </a-form-item>
      <a-form-item label="Mô tả">
        <a-textarea v-model:value="detailLevel.description" placeholder="Nhập mô tả" :rows="4" />
      </a-form-item>
      <a-form-item label="Trạng thái">
        <a-select v-model:value="detailLevel.status" placeholder="Chọn trạng thái">
          <a-select-option value="ACTIVE">Hoạt động</a-select-option>
          <a-select-option value="INACTIVE">Không hoạt động</a-select-option>
        </a-select>
      </a-form-item>
    </a-form>
  </a-modal>

  <!-- Modal Xem chi tiết cấp dự án -->
  <a-modal v-model:open="modalDetail" title="Chi tiết cấp dự án" :footer="null">
    <a-descriptions bordered :column="1">
      <a-descriptions-item label="Tên">{{ detailLevel.name }}</a-descriptions-item>
      <a-descriptions-item label="Mã">{{ detailLevel.code }}</a-descriptions-item>
      <a-descriptions-item label="Mô tả">{{ detailLevel.description }}</a-descriptions-item>
      <a-descriptions-item label="Trạng thái">
        <a-tag :color="getStatusColor(detailLevel.status)">
          {{ getStatusText(detailLevel.status) }}
        </a-tag>
      </a-descriptions-item>
      <a-descriptions-item label="Ngày tạo">{{ formatDate(detailLevel.createdAt) }}</a-descriptions-item>
      <a-descriptions-item label="Ngày cập nhật">{{ formatDate(detailLevel.updatedAt) }}</a-descriptions-item>
    </a-descriptions>
  </a-modal>

  <div class="container-fluid">
    <div class="row g-3">
      <!-- Bộ lọc tìm kiếm -->
      <div class="col-12">
        <a-card :bordered="false" class="cart">
          <template #title> <FilterFilled /> Bộ lọc </template>
          <div class="row g-2">
            <div class="col-md-6 col-sm-12">
              <div class="label-title">Tìm kiếm theo tên, mã:</div>
              <a-input
                v-model:value="filter.name"
                placeholder="Tên hoặc mã cấp dự án"
                allowClear
                @change="fetchLevels"
                class="w-100"
              />
            </div>
            <div class="col-md-6 col-sm-12">
              <div class="label-title">Trạng thái:</div>
              <a-select
                v-model:value="filter.status"
                placeholder="Chọn trạng thái"
                allowClear
                class="w-100"
                @change="fetchLevels"
              >
                <a-select-option :value="null">Tất cả trạng thái</a-select-option>
                <a-select-option :value="1">Hoạt động</a-select-option>
                <a-select-option :value="0">Không hoạt động</a-select-option>
              </a-select>
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
                <PlusOutlined /> Thêm 
              </a-button>
            </a-tooltip>
          </div>
          <a-table
            rowKey="id"
            :dataSource="levels"
            :columns="columns"
            :pagination="pagination"
            @change="handleTableChange"
            :loading="loadingStore.isLoading"
            :scroll="{ y: 500, x: 'auto' }"
          >
            <template #bodyCell="{ column, record, index }">
              <template v-if="column.dataIndex">
                <template v-if="column.dataIndex === 'rowNumber'">
                  {{ index + 1 }}
                </template>
                <template v-else-if="column.dataIndex === 'status'">
                  <a-tag :color="record.status === 1 ? 'green' : 'red'">
                    {{ record.status === 1 ? 'Hoạt động' : 'Không hoạt động' }}
                  </a-tag>
                </template>

                <template v-else>
                  {{ record[column.dataIndex] }}
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
                      <EyeOutlined />
                    </a-button>
                  </a-tooltip>
                  <a-tooltip title="Chỉnh sửa">
                    <a-button
                      type="text"
                      class="btn-outline-info"
                      @click="prepareUpdateLevel(record)"
                    >
                      <EditOutlined />
                    </a-button>
                  </a-tooltip>
                  <a-tooltip title="Đổi trạng thái">
                    <a-button
                      type="text"
                      class="btn-outline-warning"
                      @click="confirmChangeStatus(record)"
                    >
                      <SyncOutlined />
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

<style scoped>
.cart {
  margin-top: 5px;
}

.label-title {
  font-weight: 500;
  margin-bottom: 5px;
}

.btn-outline-primary {
  color: #1890ff;
  border-color: #1890ff;
}

.btn-outline-info {
  color: #13c2c2;
  border-color: #13c2c2;
}

.btn-outline-warning {
  color: #faad14;
  border-color: #faad14;
}

.btn-outline-danger {
  color: #ff4d4f;
  border-color: #ff4d4f;
}
</style>