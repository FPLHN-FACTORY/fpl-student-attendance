<script setup>
import { ref, reactive, onMounted } from 'vue'
import { message, Modal } from 'ant-design-vue'
import {
  PlusOutlined,
  EditFilled,
  UnorderedListOutlined,
  FilterFilled,
} from '@ant-design/icons-vue'
import requestAPI from '@/services/requestApiService'
import { ROUTE_NAMES } from '@/router/adminRoute'
import { API_ROUTES_ADMIN } from '@/constants/adminConstant'
import { DEFAULT_PAGINATION } from '@/constants'
import useBreadcrumbStore from '@/stores/useBreadCrumbStore'
import useLoadingStore from '@/stores/useLoadingStore'

// --- Breadcrumb ---
const breadcrumbStore = useBreadcrumbStore()

const breadcrumb = ref([
  { name: ROUTE_NAMES.ADMIN_PAGE, breadcrumbName: 'Quản lý' },
  { name: ROUTE_NAMES.ADMIN_USER, breadcrumbName: 'Admin User' },
])

// --- Data & State ---
const users = ref([])
const filter = reactive({
  searchQuery: '',
  status: '',
})
const pagination = ref({ ...DEFAULT_PAGINATION })
const loadingStore = useLoadingStore()
const isLoading = ref(false)

// Modals
const modalAdd = ref(false)
const modalAddLoading = ref(false)
const modalEdit = ref(false)
const modalEditLoading = ref(false)

// Form models
const newUser = reactive({
  userAdminCode: '',
  userAdminName: '',
  email: '',
})
const editUser = reactive({
  id: '',
  userAdminCode: '',
  userAdminName: '',
  email: '',
})

// Column config
const columns = ref([
  { title: '#', dataIndex: 'rowNumber', key: 'rowNumber', width: 60 },
  { title: 'Mã ban đào tạo', dataIndex: 'userAdminCode', key: 'userAdminCode', width: 180 },
  { title: 'Tên ban đào tạo', dataIndex: 'userAdminName', key: 'userAdminName', width: 200 },
  { title: 'Email', dataIndex: 'userAdminEmail', key: 'userAdminEmail', width: 220 },
  { title: 'Trạng thái', dataIndex: 'userAdminStatus', key: 'userAdminStatus', width: 140 },
  { title: 'Hành động', key: 'actions', width: 120 },
])

// --- API Calls ---
const fetchUsers = () => {
  loadingStore.show()
  isLoading.value = true
  const params = {
    searchQuery: filter.searchQuery,
    status: filter.status,
    page: pagination.value.current,
    size: pagination.value.pageSize,
  }
  requestAPI
    .get(API_ROUTES_ADMIN.FETCH_DATA_ADMIN, { params })
    .then((res) => {
      const data = res.data.data
      users.value = data.data
      pagination.value.total = data.totalPages * pagination.value.pageSize
    })
    .catch((err) => {
      message.error(err?.response?.data?.message || 'Lỗi khi tải danh sách')
    })
    .finally(() => {
      isLoading.value = false
      loadingStore.hide()
    })
}

const handleTableChange = (pageInfo) => {
  pagination.value.current = pageInfo.current
  pagination.value.pageSize = pageInfo.pageSize
  fetchUsers()
}

const clearNewUser = () => {
  newUser.userAdminCode = ''
  newUser.userAdminName = ''
  newUser.email = ''
}

const handleAddUser = () => {
  if (!newUser.staffCode || !newUser.staffName || !newUser.email) {
    return message.error('Vui lòng điền đầy đủ thông tin')
  }
  modalAddLoading.value = true
  requestAPI
    .post(API_ROUTES_ADMIN.FETCH_DATA_ADMIN, newUser)
    .then(() => {
      message.success('Thêm admin thành công')
      modalAdd.value = false
      clearNewUser()
      fetchUsers()
    })
    .catch((err) => {
      message.error(err?.response?.data?.message || 'Lỗi khi thêm')
    })
    .finally(() => {
      modalAddLoading.value = false
    })
}

const handleEditUser = (record) => {
  editUser.id = record.userAdminId
  editUser.staffCode = record.userAdminCode
  editUser.staffName = record.userAdminName
  editUser.email = record.userAdminEmail
  modalEdit.value = true
}

const handleUpdateUser = () => {
  if (!editUser.staffCode || !editUser.staffName || !editUser.email) {
    return message.error('Vui lòng điền đầy đủ thông tin')
  }
  modalEditLoading.value = true
  requestAPI
    .put(`${API_ROUTES_ADMIN.FETCH_DATA_ADMIN}/${editUser.id}`, editUser)
    .then(() => {
      message.success('Cập nhật thành công')
      modalEdit.value = false
      fetchUsers()
    })
    .catch((err) => {
      message.error(err?.response?.data?.message || 'Lỗi khi cập nhật')
    })
    .finally(() => {
      modalEditLoading.value = false
    })
}

const handleChangeStatus = (record) => {
  Modal.confirm({
    title: 'Xác nhận đổi trạng thái',
    content: `Bạn có chắc muốn thay đổi trạng thái của ${record.userAdminName}?`,
    onOk() {
      requestAPI
        .put(`${API_ROUTES_ADMIN.FETCH_DATA_ADMIN}/status/${record.userAdminId}`)
        .then(() => {
          message.success('Đổi trạng thái thành công')
          fetchUsers()
        })
        .catch((err) => {
          message.error(err?.response?.data?.message || 'Lỗi khi đổi trạng thái')
        })
    },
  })
}

// --- Lifecycle ---
onMounted(() => {
  breadcrumbStore.setRoutes(breadcrumb.value)
  fetchUsers()
})
</script>

<template>
  <div class="container-fluid">
    <div class="row g-3">
      <div class="col-12">
        <!-- Filter Card -->
        <a-card :bordered="false" class="mb-3">
          <template #title><FilterFilled /> Bộ lọc</template>
          <a-row :gutter="16" class="filter-container">
            <a-col :span="12" class="col">
              <div class="label-title">Tìm kiếm:</div>
              <a-input
                v-model:value="filter.searchQuery"
                placeholder="Nhập mã, tên hoặc email"
                allowClear
                @change="fetchUsers"
              />
            </a-col>
            <a-col :span="12" class="col">
              <div class="label-title">Trạng thái:</div>
              <a-select
                v-model:value="filter.status"
                placeholder="Chọn trạng thái"
                allowClear
                style="width: 100%"
                @change="fetchUsers"
              >
                <a-select-option :value="''">Tất cả</a-select-option>
                <a-select-option value="1">Đang hoạt động</a-select-option>
                <a-select-option value="0">Ngừng hoạt động</a-select-option>
              </a-select>
            </a-col>
          </a-row>
        </a-card>
      </div>
    </div>

    <div class="row g-3">
      <div class="col-12">
        <!-- Table Card -->
        <a-card :bordered="false">
          <template #title><UnorderedListOutlined /> Danh sách Admin</template>
          <div class="d-flex justify-content-end mb-3 flex-wrap gap-3">
            <ExcelUploadButton v-bind="configImportExcel" />
            <a-tooltip title="Thêm mới admin">
              <a-button type="primary" @click="modalAdd = true">
                <PlusOutlined /> Thêm mới
              </a-button>
            </a-tooltip>
          </div>
          <a-table
            :dataSource="users"
            :columns="columns"
            rowKey="userAdminId"
            :loading="isLoading"
            :pagination="pagination"
            @change="handleTableChange"
            :scroll="{ x: 'auto', y: 500 }"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.dataIndex === 'userAdminStatus'">
                <a-switch
                  :checked="record.userAdminStatus === 1"
                  @change="handleChangeStatus(record)"
                />
                <a-tag :color="record.userAdminStatus === 1 ? 'green' : 'red'">
                  {{ record.userAdminStatus === 1 ? 'Đang hoạt động' : 'Ngưng' }}
                </a-tag>
              </template>
              <template v-else-if="column.key === 'actions'">
                <a-tooltip title="Sửa nhân viên">
                  <a-button
                    type="text"
                    class="btn-outline-info me-2"
                    @click="handleEditUser(record)"
                  >
                    <EditFilled />
                  </a-button>
                </a-tooltip>
              </template>
              <template v-else>
                {{ record[column.dataIndex] }}
              </template>
            </template>
          </a-table>
        </a-card>
      </div>
    </div>

    <!-- Modal Thêm -->
    <a-modal
      v-model:open="modalAdd"
      title="Thêm Admin"
      @ok="handleAddUser"
      :okButtonProps="{ loading: modalAddLoading }"
    >
      <a-form layout="vertical">
        <a-form-item label="Mã admin" required>
          <a-input v-model:value="newUser.staffCode" placeholder="Nhập mã admin" />
        </a-form-item>
        <a-form-item label="Tên admin" required>
          <a-input v-model:value="newUser.staffName" placeholder="Nhập tên admin" />
        </a-form-item>
        <a-form-item label="Email" required>
          <a-input v-model:value="newUser.email" placeholder="Nhập email" />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- Modal Sửa -->
    <a-modal
      v-model:open="modalEdit"
      title="Cập nhật Admin"
      @ok="handleUpdateUser"
      :okButtonProps="{ loading: modalEditLoading }"
    >
      <a-form layout="vertical">
        <a-form-item label="Mã admin" required>
          <a-input v-model:value="editUser.staffCode" placeholder="Nhập mã admin" />
        </a-form-item>
        <a-form-item label="Tên admin" required>
          <a-input v-model:value="editUser.staffName" placeholder="Nhập tên admin" />
        </a-form-item>
        <a-form-item label="Email" required>
          <a-input v-model:value="editUser.email" placeholder="Nhập email" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>


