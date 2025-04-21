<script setup>
import { ref, reactive, onMounted } from 'vue'
import { message, Modal } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import requestAPI from '@/services/requestApiService'
import {
  PlusOutlined,
  EyeOutlined,
  EditOutlined,
  ClusterOutlined,
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

const router = useRouter()
const breadcrumbStore = useBreadcrumbStore()
const loadingStore = useLoadingStore()

/* ----------------- Data & Reactive Variables ----------------- */
const subjects = ref([])

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

const newSubject = reactive({
  name: '',
  code: '',
})

const detailSubject = reactive({
  id: '',
  name: '',
  code: '',
  status: 1,
  createdAt: '',
  updatedAt: '',
  sizeSubjectSemester: 0,
})

const columns = ref([
  { title: '#', dataIndex: 'indexs', key: 'indexs', width: 50 },
  { title: 'Tên bộ môn', dataIndex: 'name', key: 'name', width: 200 },
  { title: 'Mã bộ môn', dataIndex: 'code', key: 'code', width: 150 },
  {
    title: 'Số lượng bộ môn cơ sở',
    dataIndex: 'sizeSubjectSemester',
    key: 'sizeSubjectSemester',
    width: 150,
  },
  { title: 'Trạng thái', dataIndex: 'status', key: 'status', width: 100 },
  { title: 'Chức năng', key: 'actions', width: 200 },
])

const breadcrumb = ref([
  {
    name: GLOBAL_ROUTE_NAMES.ADMIN_PAGE,
    breadcrumbName: 'Ban đào tạo',
  },
  {
    name: ROUTE_NAMES.MANAGEMENT_SUBJECT,
    breadcrumbName: 'Quản lý bộ môn',
  },
])

/* ----------------- Methods ----------------- */
const fetchSubjects = () => {
  loadingStore.show()
  requestAPI
    .post(
      `${API_ROUTES_ADMIN.FETCH_DATA_SUBJECT}/list`,
      {
        ...filter,
      },
      {
        params: {
          page: pagination.current,
          size: pagination.pageSize,
        },
      }
    )
    .then((response) => {
      const result = response.data.data
      subjects.value = result.data
      pagination.total = result.totalPages * pagination.pageSize
    })
    .catch((error) => {
      message.error(error.response?.data?.message || 'Lỗi khi lấy danh sách bộ môn')
    })
    .finally(() => {
      loadingStore.hide()
    })
}

const handleTableChange = (pageInfo) => {
  pagination.current = pageInfo.current
  pagination.pageSize = pageInfo.pageSize
  fetchSubjects()
}

const showAddModal = (isOpen) => {
  modalAdd.value = isOpen
}

const handleAddSubject = () => {
  if (!newSubject.name) {
    message.error('Vui lòng nhập tên bộ môn')
    return
  }
  loadingStore.show()
  requestAPI
    .post(API_ROUTES_ADMIN.FETCH_DATA_SUBJECT, newSubject)
    .then((response) => {
      message.success(response.data.message || 'Thêm bộ môn thành công')
      modalAdd.value = false
      fetchSubjects()
      newSubject.name = ''
      newSubject.code = ''
    })
    .catch((error) => {
      message.error(error.response?.data?.message || 'Lỗi khi thêm bộ môn')
    })
    .finally(() => {
      loadingStore.hide()
    })
}

const handleDetailSubject = (record) => {
  loadingStore.show()
  requestAPI
    .get(`${API_ROUTES_ADMIN.FETCH_DATA_SUBJECT}/${record.id}`)
    .then((response) => {
      Object.assign(detailSubject, response.data.data)
      modalDetail.value = true
    })
    .catch((error) => {
      message.error(error.response?.data?.message || 'Lỗi khi lấy chi tiết bộ môn')
    })
    .finally(() => {
      loadingStore.hide()
    })
}

const handleUpdateSubject = (record) => {
  loadingStore.show()
  requestAPI
    .get(`${API_ROUTES_ADMIN.FETCH_DATA_SUBJECT}/${record.id}`)
    .then((response) => {
      Object.assign(detailSubject, response.data.data)
      modalUpdate.value = true
    })
    .catch((error) => {
      message.error(error.response?.data?.message || 'Lỗi khi lấy thông tin bộ môn')
    })
    .finally(() => {
      loadingStore.hide()
    })
}

const updateSubject = () => {
  if (!detailSubject.name) {
    message.error('Vui lòng nhập tên bộ môn')
    return
  }
  loadingStore.show()
  const requestData = {
    id: detailSubject.id,
    name: detailSubject.name,
    code: detailSubject.code,
    status: detailSubject.status,
  }

  requestAPI
    .put(`${API_ROUTES_ADMIN.FETCH_DATA_SUBJECT}/${detailSubject.id}`, requestData)
    .then((response) => {
      message.success(response.data.message || 'Cập nhật bộ môn thành công')
      modalUpdate.value = false
      fetchSubjects()
    })
    .catch((error) => {
      message.error(error.response?.data?.message || 'Lỗi khi cập nhật bộ môn')
    })
    .finally(() => {
      loadingStore.hide()
    })
}

const confirmChangeStatus = (record) => {
  Modal.confirm({
    title: 'Xác nhận',
    content: `Bạn có chắc chắn muốn chuyển trạng thái bộ môn ${record.name}?`,
    onOk: () => {
      loadingStore.show()
      requestAPI
        .delete(`${API_ROUTES_ADMIN.FETCH_DATA_SUBJECT}/${record.id}`)
        .then((response) => {
          message.success(response.data.message || ' thành công')
          fetchSubjects()
        })
        .catch((error) => {
          message.error(error.response?.data?.message || 'Lỗi khi chuyển trang thái bộ môn')
        })
        .finally(() => {
          loadingStore.hide()
        })
    },
  })
}

const handleAddSubjectFacility = (record) => {
  router.push({
    name: ROUTE_NAMES.MANAGEMENT_SUBJECT_FACILITY,
    query: { subjectId: record.id },
  })
}

const formatDate = (timestamp) => {
  if (!timestamp) return 'Không xác định'
  return new Date(timestamp).toLocaleString()
}

const getStatusText = (status) => {
  return status === 1 ? 'Hoạt động' : 'Không hoạt động'
}

const getStatusColor = (status) => {
  return status === 1 ? 'green' : 'red'
}

onMounted(() => {
  breadcrumbStore.setRoutes(breadcrumb.value)
  fetchSubjects()
})
</script>

<template>
  <div class="container-fluid">
    <!-- Card Bộ lọc tìm kiếm -->
    <div class="row g-3">
      <div class="col-12">
        <a-card :bordered="false" class="cart mb-3">
          <template #title> <FilterFilled /> Bộ lọc </template>
          <a-row :gutter="16" class="filter-container">
            <!-- Input tìm kiếm theo tên -->
            <a-col :span="12" class="col">
              <div class="label-title">Tìm kiếm theo tên:</div>
              <a-input
                v-model:value="filter.name"
                placeholder="Tìm kiếm theo tên"
                allowClear
                @change="fetchSubjects"
              />
            </a-col>

            <!-- Combobox trạng thái -->
            <a-col :span="12" class="col">
              <div class="label-title">Trạng thái:</div>
              <a-select
                v-model:value="filter.status"
                placeholder="Chọn trạng thái"
                allowClear
                style="width: 100%"
                @change="fetchSubjects"
              >
                <a-select-option :value="null">Tất cả trạng thái</a-select-option>
                <a-select-option :value="1">Hoạt động</a-select-option>
                <a-select-option :value="0">Không hoạt động</a-select-option>
              </a-select>
            </a-col>
          </a-row>
        </a-card>
      </div>
    </div>

    <!-- Card Danh sách bộ môn -->
    <div class="row g-3">
      <div class="col-12">
        <a-card :bordered="false" class="cart">
          <template #title> <UnorderedListOutlined /> Danh sách bộ môn </template>
          <div class="d-flex justify-content-end mb-3">
            <a-tooltip title="Thêm bộ môn">
              <a-button type="primary" @click="showAddModal(true)">
                <PlusOutlined /> Thêm
              </a-button>
            </a-tooltip>
          </div>
          <a-table
            :dataSource="subjects"
            :columns="columns"
            rowKey="id"
            :pagination="pagination"
            @change="handleTableChange"
            :loading="loadingStore.isLoading"
            :scroll="{ y: 500, x: 'auto' }"
          >
            <template #bodyCell="{ column, record, index }">
              <template v-if="column.dataIndex">
                <template v-if="column.dataIndex === 'indexs'">
                  {{ index + 1 }}
                </template>
                <template v-else-if="column.dataIndex === 'name'">
                  <a @click="handleAddSubjectFacility(record)">{{ record.name }}</a>
                </template>
                <template v-else-if="column.dataIndex === 'status'">
                  <a-tag :color="getStatusColor(record.status)">
                    {{ getStatusText(record.status) }}
                  </a-tag>
                </template>
                <template v-else>
                  {{ record[column.dataIndex] }}
                </template>
              </template>
              <template v-else-if="column.key === 'actions'">
                <a-space>
                  <a-tooltip title="Bộ môn cơ sở">
                    <a-button
                      @click="handleAddSubjectFacility(record)"
                      type="text"
                      class="btn-outline-primary me-2"
                    >
                      <ClusterOutlined />
                    </a-button>
                  </a-tooltip>
                  <a-tooltip title="Xem chi tiết">
                    <a-button
                      @click="handleDetailSubject(record)"
                      type="text"
                      class="btn-outline-info me-2"
                    >
                      <EyeOutlined />
                    </a-button>
                  </a-tooltip>
                  <a-tooltip title="Sửa bộ môn">
                    <a-button
                      @click="handleUpdateSubject(record)"
                      type="text"
                      class="btn-outline-warning me-2"
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

    <!-- Modal Thêm bộ môn -->
    <a-modal
      v-model:open="modalAdd"
      title="Thêm bộ môn"
      @ok="handleAddSubject"
      :okButtonProps="{ loading: loadingStore.isLoading }"
    >
      <a-form layout="vertical">
        <a-form-item label="Tên bộ môn" required>
          <a-input v-model:value="newSubject.name" placeholder="Nhập tên bộ môn" />
        </a-form-item>
        <a-form-item label="Mã bộ môn">
          <a-input v-model:value="newSubject.code" placeholder="Nhập mã bộ môn" />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- Modal Xem chi tiết bộ môn -->
    <a-modal v-model:open="modalDetail" title="Chi tiết bộ môn" :footer="null">
      <a-descriptions bordered :column="1">
        <a-descriptions-item label="Tên">{{ detailSubject.name }}</a-descriptions-item>
        <a-descriptions-item label="Mã">{{ detailSubject.code }}</a-descriptions-item>
        <a-descriptions-item label="Trạng thái">
          <a-tag :color="getStatusColor(detailSubject.status)">
            {{ getStatusText(detailSubject.status) }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="Số lượng bộ môn cơ sở">
          {{ detailSubject.sizeSubjectSemester }}
        </a-descriptions-item>
        <a-descriptions-item label="Ngày tạo">
          {{ formatDate(detailSubject.createdAt) }}
        </a-descriptions-item>
        <a-descriptions-item label="Ngày cập nhật">
          {{ formatDate(detailSubject.updatedAt) }}
        </a-descriptions-item>
      </a-descriptions>
    </a-modal>

    <!-- Modal Cập nhật bộ môn -->
    <a-modal
      v-model:open="modalUpdate"
      title="Cập nhật bộ môn"
      @ok="updateSubject"
      :okButtonProps="{ loading: loadingStore.isLoading }"
    >
      <a-form layout="vertical">
        <a-form-item label="Tên bộ môn" required>
          <a-input v-model:value="detailSubject.name" placeholder="Nhập tên bộ môn" />
        </a-form-item>
        <a-form-item label="Mã bộ môn">
          <a-input v-model:value="detailSubject.code" placeholder="Nhập mã bộ môn" />
        </a-form-item>
        <a-form-item label="Trạng thái">
          <a-select v-model:value="detailSubject.status" placeholder="Chọn trạng thái">
            <a-select-option :value="1">Hoạt động</a-select-option>
            <a-select-option :value="0">Không hoạt động</a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<style scoped>
.cart {
  margin-top: 5px;
}

.filter-container {
  margin-bottom: 5px;
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