<script setup>
import { ref, reactive, onMounted } from 'vue'
import { message, Modal } from 'ant-design-vue'
import {
  PlusOutlined,
  EditFilled,
  UnorderedListOutlined,
  FilterFilled,
  DeleteFilled,
} from '@ant-design/icons-vue'
import requestAPI from '@/services/requestApiService'
import { ROUTE_NAMES } from '@/router/adminRoute'
import { API_ROUTES_ADMIN } from '@/constants/adminConstant'
import { DEFAULT_PAGINATION } from '@/constants'
import useBreadcrumbStore from '@/stores/useBreadCrumbStore'
import useLoadingStore from '@/stores/useLoadingStore'
import useApplicationStore from '@/stores/useApplicationStore'

// --- Breadcrumb ---
const breadcrumbStore = useBreadcrumbStore()
const breadcrumb = ref([
  { name: ROUTE_NAMES.ADMIN_PAGE, breadcrumbName: 'Quản lý' },
  { name: ROUTE_NAMES.ADMIN_USER, breadcrumbName: 'Quản lý ban đào tạo' },
])

// --- Data & State ---
const users = ref([])
const filter = reactive({ searchQuery: '', status: '' })
const pagination = ref({ ...DEFAULT_PAGINATION })
const applicationStore = useApplicationStore()
const loadingStore = useLoadingStore()
const isLoading = ref(false)

// Staff list for change-power modal
const staffList = ref([])
const selectedStaffId = ref(null)
let currentAdminId = null

// Modals
const modalAdd = ref(false)
const modalAddLoading = ref(false)
const modalEdit = ref(false)
const modalEditLoading = ref(false)
const modalChangePower = ref(false)
const modalChangePowerLoading = ref(false)

// Form models
const newUser = reactive({ staffCode: '', staffName: '', email: '' })
const editUser = reactive({ id: '', staffCode: '', staffName: '', email: '' })

// Column config
const columns = ref([
  { title: '#', dataIndex: 'rowNumber', key: 'rowNumber', width: 60 },
  { title: 'Mã ban đào tạo', dataIndex: 'userAdminCode', key: 'userAdminCode', width: 180 },
  { title: 'Tên ban đào tạo', dataIndex: 'userAdminName', key: 'userAdminName', width: 200 },
  { title: 'Email', dataIndex: 'userAdminEmail', key: 'userAdminEmail', width: 220 },
  { title: 'Trạng thái', dataIndex: 'userAdminStatus', key: 'userAdminStatus', width: 140 },
  { title: 'Chức năng', key: 'actions', width: 120 },
])

// --- API Calls ---
const fetchUsers = async () => {
  loadingStore.show()
  isLoading.value = true

  try {
    const res = await requestAPI.get(API_ROUTES_ADMIN.FETCH_DATA_ADMIN, {
      params: {
        searchQuery: filter.searchQuery,
        status: filter.status,
        page: pagination.value.current,
        size: pagination.value.pageSize,
      },
    })
    const data = res.data.data
    pagination.value.total = data.totalPages * pagination.value.pageSize

    const usersWithFlag = await Promise.all(
      data.data.map(async (user) => {
        const flagRes = await requestAPI.get(
          `${API_ROUTES_ADMIN.FETCH_DATA_ADMIN}/is-myself/${user.userAdminId}`
        )
        return { ...user, isMySelf: flagRes.data.data }
      })
    )

    users.value = usersWithFlag
  } catch (err) {
    message.error(err?.response?.data?.message || 'Lỗi khi tải danh sách')
  } finally {
    isLoading.value = false
    loadingStore.hide()
  }
}

const fetchStaffList = async () => {
  try {
    const res = await requestAPI.get(API_ROUTES_ADMIN.FETCH_DATA_ADMIN + '/staff')
    staffList.value = res.data.data
  } catch (err) {
    message.error(err?.response?.data?.message || 'Lỗi khi tải danh sách nhân viên')
  }
}

const handleTableChange = (pageInfo) => {
  pagination.value.current = pageInfo.current
  pagination.value.pageSize = pageInfo.pageSize
  fetchUsers()
}

const clearNewUser = () => {
  newUser.staffCode = ''
  newUser.staffName = ''
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
      message.success('Thêm ban đào tạo thành công')
      modalAdd.value = false
      applicationStore.loadNotification()
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

  // Check if editing own information
  const isEditingSelf = users.value.find(user => user.userAdminId === editUser.id)?.isMySelf
  const originalUser = users.value.find(user => user.userAdminId === editUser.id)
  const isEmailChanged = originalUser && originalUser.userAdminEmail !== editUser.email

  if (isEditingSelf && isEmailChanged) {
    Modal.confirm({
      title: 'Xác nhận thay đổi email',
      content: 'Bạn đang thay đổi email của chính mình. Sau khi thay đổi, phiên đăng nhập sẽ hết hạn và bạn sẽ bị đăng xuất khỏi hệ thống. Bạn có chắc chắn muốn tiếp tục?',
      onOk() {
        performUpdate()
      }
    })
  } else if (isEditingSelf) {
    Modal.confirm({
      title: 'Xác nhận cập nhật',
      content: 'Bạn đang cập nhật thông tin của chính mình. Bạn có chắc chắn muốn tiếp tục?',
      onOk() {
        performUpdate()
      }
    })
  } else {
    performUpdate()
  }
}

const performUpdate = () => {
  modalEditLoading.value = true
  requestAPI
    .put(`${API_ROUTES_ADMIN.FETCH_DATA_ADMIN}/${editUser.id}`, editUser)
    .then(() => {
      message.success('Cập nhật thành công')
      modalEdit.value = false
      fetchUsers()
      applicationStore.loadNotification()
    })
    .catch((err) => {
      message.error(err?.response?.data?.message || 'Lỗi khi cập nhật')
    })
    .finally(() => {
      modalEditLoading.value = false
    })
}

const openChangePowerModal = async (record) => {
  currentAdminId = record.userAdminId
  await fetchStaffList()
  selectedStaffId.value = null
  modalChangePower.value = true
}

const handleChangePowerShift = () => {
  const selectedStaff = staffList.value.find((staff) => staff.id === selectedStaffId.value)
  const staffName = selectedStaff ? selectedStaff.name : 'nhân viên này'
  Modal.confirm({
    title: 'Xác nhận chuyển quyền',
    content: `Bạn có chắc chắn muốn chuyển quyền ban đào tạo cho ${staffName}?
    , nếu đông ý bạn sẽ đăng xuất ngay bây giờ`,
    onOk() {
      modalChangePowerLoading.value = true
      const payload = { userAdminId: currentAdminId, userStaffId: selectedStaffId.value }
      requestAPI
        .post(API_ROUTES_ADMIN.FETCH_DATA_ADMIN + '/change-power', payload)
        .then(() => {
          message.success('Chuyển quyền thành công')
          modalChangePower.value = false
          fetchUsers()
        })
        .catch((err) => message.error(err?.response?.data?.message || 'Lỗi khi chuyển quyền'))
        .finally(() => {
          modalChangePowerLoading.value = false
        })
    },
  })
}

const handleChangeStatus = (record) => {
  if (record.isMySelf) {
    return message.error('Không thể thay đổi trạng thái chính mình')
  }
  Modal.confirm({
    title: 'Xác nhận đổi trạng thái',
    content: `Bạn có chắc muốn thay đổi trạng thái của ${record.userAdminName}?`,
    onOk() {
      requestAPI
        .put(`${API_ROUTES_ADMIN.FETCH_DATA_ADMIN}/change-status/${record.userAdminId}`)
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

const handleDelete = (record) => {
  Modal.confirm({
    title: 'Xác nhận xóa',
    content: `Bạn có chắc chắn muốn xóa ban đào tạo ${record.userAdminName}?`,
    onOk() {
      handleDeleteUser(record.userAdminId)
    },
  })
}

const handleDeleteUser = (id) => {
  loadingStore.show()
  requestAPI
    .delete(`${API_ROUTES_ADMIN.FETCH_DATA_ADMIN}/${id}`)
    .then(() => {
      message.success('Xóa ban đào tạo thành công')
      fetchUsers()
    })
    .catch((err) => {
      message.error(err?.response?.data?.message || 'Lỗi khi xóa ban đào tạo')
    })
    .finally(() => {
      loadingStore.hide()
    })
}

const handleClearFilter = () => {
  // Clear all filter values
  Object.keys(filter).forEach(key => {
    filter[key] = ''
  })
  pagination.value.current = 1
  fetchUsers()
}

onMounted(() => {
  breadcrumbStore.setRoutes(breadcrumb.value)
  fetchUsers()
})
</script>

<template>
  <div class="container-fluid">
    <div class="row g-3">
      <div class="col-12">
        <a-card :bordered="false" class="cart mb-3">
          <template #title> <FilterFilled /> Bộ lọc tìm kiếm </template>
          <a-row :gutter="16" class="filter-container">
            <div class="col-md-6 col-sm-6">
              <label class="label-title">Từ khoá:</label>
              <a-input
                v-model:value="filter.searchQuery"
                placeholder="Nhập mã, tên hoặc email"
                allowClear
                @change="fetchUsers"
              />
            </div>
            <div class="col-md-6 col-sm-6">
              <label class="label-title">Trạng thái:</label>
              <a-select
                v-model:value="filter.status"
                placeholder="Chọn trạng thái"
                allowClear
                style="width: 100%"
                @change="fetchUsers"
              >
                <a-select-option :value="''">Tất cả trạng thái</a-select-option>
                <a-select-option value="1">Hoạt động</a-select-option>
                <a-select-option value="0">Không hoạt động</a-select-option>
              </a-select>
            </div>
          </a-row>
          <div class="row">
            <div class="col-12">
              <div class="d-flex justify-content-center flex-wrap gap-2 mt-3">
                <a-button class="btn-light" @click="fetchUsers">
                  <FilterFilled /> Lọc
                </a-button>
                <a-button class="btn-gray" @click="handleClearFilter"> Huỷ lọc </a-button>
              </div>
            </div>
          </div>
        </a-card>
      </div>
    </div>

    <!-- Table -->
    <a-card>
      <template #title><UnorderedListOutlined /> Danh sách Ban đào tạo</template>
      <div class="d-flex justify-content-end mb-3 flex-wrap gap-3">
        <a-tooltip title="Thêm mới ban đào tạo">
          <a-button type="primary" @click="modalAdd = true"><PlusOutlined /> Thêm mới</a-button>
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
              class="me-2"
              @change="handleChangeStatus(record)"
            />
            <a-tag :color="record.userAdminStatus === 1 ? 'green' : 'red'">{{
              record.userAdminStatus === 1 ? 'Đang hoạt động' : 'Ngưng hoạt động'
            }}</a-tag>
          </template>

          <template v-else-if="column.key === 'actions'">
            <a-tooltip title="Sửa ban đào tạo">
              <a-button type="text" class="btn-outline-info me-2" @click="handleEditUser(record)"
                ><EditFilled
              /></a-button>
            </a-tooltip>
            <!-- Chuyển quyền: chỉ hiện khi là chính mình -->
            <a-tooltip title="Chuyển quyền" v-if="record.isMySelf">
              <a-button
                type="text"
                class="btn-outline-warning"
                @click="openChangePowerModal(record)"
                ><FilterFilled
              /></a-button>
            </a-tooltip>
            <template v-if="!record.isMySelf">
              <a-tooltip title="Xóa ban đào tạo">
                <a-button type="text" class="btn-outline-danger" @click="handleDelete(record)">
                  <DeleteFilled />
                </a-button>
              </a-tooltip>
            </template>
          </template>
          <template v-else>
            {{ record[column.dataIndex] }}
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- Modal Thêm -->
    <a-modal
      v-model:open="modalAdd"
      title="Thêm Ban đào tạo"
      @ok="handleAddUser"
      :okButtonProps="{ loading: modalAddLoading }"
    >
      <a-form layout="vertical">
        <a-form-item label="Mã ban đào tạo" required>
          <a-input v-model:value="newUser.staffCode" placeholder="Nhập mã ban đào tạo" />
        </a-form-item>
        <a-form-item label="Tên ban đào tạo" required>
          <a-input v-model:value="newUser.staffName" placeholder="Nhập tên ban đào tạo" />
        </a-form-item>
        <a-form-item label="Email" required>
          <a-input v-model:value="newUser.email" placeholder="Nhập email" />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- Modal Sửa -->
    <a-modal
      v-model:open="modalEdit"
      title="Cập nhật Ban đào tạo"
      @ok="handleUpdateUser"
      :okButtonProps="{ loading: modalEditLoading }"
    >
      <a-form layout="vertical">
        <a-form-item label="Mã ban đào tạo" required>
          <a-input v-model:value="editUser.staffCode" placeholder="Nhập mã ban đào tạo" />
        </a-form-item>
        <a-form-item label="Tên ban đào tạo" required>
          <a-input v-model:value="editUser.staffName" placeholder="Nhập tên ban đào tạo" />
        </a-form-item>
        <a-form-item label="Email" required>
          <a-input v-model:value="editUser.email" placeholder="Nhập email" />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- Modal chuyển quyền -->
    <a-modal
      v-model:open="modalChangePower"
      title="Chuyển quyền Ban đào tạo"
      @ok="handleChangePowerShift"
      :okButtonProps="{ loading: modalChangePowerLoading, disabled: !selectedStaffId }"
      @cancel="
        () => {
          modalChangePower.value = false
        }
      "
    >
      <a-form layout="vertical">
        <a-form-item label="Chọn nhân viên chuyển quyền" required>
          <a-select v-model:value="selectedStaffId" placeholder="Chọn nhân viên">
            <a-select-option v-for="staff in staffList" :key="staff.id" :value="staff.id">
              {{ staff.name }} ({{ staff.code }})
            </a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>
