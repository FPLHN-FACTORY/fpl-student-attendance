<script setup>
import { ref, reactive, onMounted } from 'vue'
import { message, Modal } from 'ant-design-vue'
import {
  PlusOutlined,
  EditFilled,
  UnorderedListOutlined,
  FilterFilled,
  DeleteFilled,
  SearchOutlined,
} from '@ant-design/icons-vue'
import requestAPI from '@/services/requestApiService'
import { ROUTE_NAMES } from '@/router/adminRoute'
import { API_ROUTES_ADMIN } from '@/constants/adminConstant'
import { DEFAULT_PAGINATION } from '@/constants'
import useBreadcrumbStore from '@/stores/useBreadCrumbStore'
import useLoadingStore from '@/stores/useLoadingStore'
import useApplicationStore from '@/stores/useApplicationStore'
import { autoAddColumnWidth } from '@/utils/utils'
import { validateFormSubmission } from '@/utils/validationUtils'

// --- Breadcrumb ---
const breadcrumbStore = useBreadcrumbStore()
const breadcrumb = ref([
  { name: ROUTE_NAMES.ADMIN_PAGE, breadcrumbName: 'Admin' },
  { name: ROUTE_NAMES.ADMIN_USER, breadcrumbName: 'Quản lý Admin' },
])

// --- Data & State ---
const users = ref([])
const filter = reactive({ searchQuery: '', status: '' })
const pagination = ref({ ...DEFAULT_PAGINATION })
const applicationStore = useApplicationStore()
const loadingStore = useLoadingStore()
const isLoading = ref(false)

const countFilter = ref(0)

// Staff list for change-power modal
// const staffList = ref([])
// const selectedStaffId = ref(null)
// let currentAdminId = null

// Modals
const modalAdd = ref(false)
const modalAddLoading = ref(false)
const modalEdit = ref(false)
const modalEditLoading = ref(false)
// const modalChangePower = ref(false)
// const modalChangePowerLoading = ref(false)

// Form models
const newUser = reactive({ staffCode: '', staffName: '', email: '' })
const editUser = reactive({ id: '', staffCode: '', staffName: '', email: '' })

// Column config
const columns = ref(
  autoAddColumnWidth([
    { title: '#', dataIndex: 'rowNumber', key: 'rowNumber' },
    { title: 'Mã Admin', dataIndex: 'userAdminCode', key: 'userAdminCode' },
    { title: 'Tên Admin', dataIndex: 'userAdminName', key: 'userAdminName' },
    { title: 'Email', dataIndex: 'userAdminEmail', key: 'userAdminEmail' },
    { title: 'Trạng thái', dataIndex: 'userAdminStatus', key: 'userAdminStatus' },
    { title: 'Chức năng', key: 'actions' },
  ]),
)

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

    countFilter.value = data.totalItems

    const usersWithFlag = await Promise.all(
      data.data.map(async (user) => {
        const flagRes = await requestAPI.get(
          `${API_ROUTES_ADMIN.FETCH_DATA_ADMIN}/is-myself/${user.userAdminId}`,
        )
        return { ...user, isMySelf: flagRes.data.data }
      }),
    )

    users.value = usersWithFlag
  } catch (err) {
    message.error(err?.response?.data?.message || 'Lỗi khi tải danh sách')
  } finally {
    isLoading.value = false
    loadingStore.hide()
  }
}

// const fetchStaffList = async () => {
//   try {
//     const res = await requestAPI.get(API_ROUTES_ADMIN.FETCH_DATA_ADMIN + '/staff')
//     staffList.value = res.data.data
//   } catch (err) {
//     message.error(err?.response?.data?.message || 'Lỗi khi tải danh sách nhân viên')
//   }
// }

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
  // Validate required fields with whitespace check
  const validation = validateFormSubmission(newUser, [
    { key: 'staffCode', label: 'Mã Admin', allowOnlyNumbers: true },
    { key: 'staffName', label: 'Tên Admin' },
    { key: 'email', label: 'Email Admin' },
  ])
  
  if (!validation.isValid) {
    return message.error(validation.message)
  }
  Modal.confirm({
    title: 'Xác nhận thêm mới',
    content: 'Bạn có chắc chắn muốn thêm mới tài khoản admin này?',
    okText: 'Tiếp tục',
    cancelText: 'Hủy bỏ',
    onOk() {
      loadingStore.show()
      requestAPI
        .post(API_ROUTES_ADMIN.FETCH_DATA_ADMIN, newUser)
        .then((response) => {
          message.success(response.data.message || 'Thêm tài khoản admin thành công')
          modalAdd.value = false
          fetchUsers()
          clearNewUser()
        })
        .catch((error) => {
          message.error(error.response?.data?.message || 'Lỗi khi thêm tài khoản admin')
        })
        .finally(() => {
          loadingStore.hide()
        })
    },
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
  // Validate required fields with whitespace check
  const validation = validateFormSubmission(editUser, [
    { key: 'staffCode', label: 'Mã Admin', allowOnlyNumbers: true },
    { key: 'staffName', label: 'Tên Admin' },
    { key: 'email', label: 'Email Admin' },
  ])
  
  if (!validation.isValid) {
    return message.error(validation.message)
  }

  // Check if editing own information
  const isEditingSelf = users.value.find((user) => user.userAdminId === editUser.id)?.isMySelf
  const originalUser = users.value.find((user) => user.userAdminId === editUser.id)
  const isEmailChanged = originalUser && originalUser.userAdminEmail !== editUser.email

  if (isEditingSelf && isEmailChanged) {
    Modal.confirm({
      title: 'Xác nhận thay đổi email',
      content:
        'Bạn đang thay đổi email của chính mình. Sau khi thay đổi, phiên đăng nhập sẽ hết hạn và bạn sẽ bị đăng xuất khỏi hệ thống. Bạn có chắc chắn muốn tiếp tục?',
      onOk() {
        performUpdate()
      },
    })
  } else if (isEditingSelf) {
    Modal.confirm({
      title: 'Xác nhận cập nhật',
      content: 'Bạn đang cập nhật thông tin của chính mình. Bạn có chắc chắn muốn tiếp tục?',
      onOk() {
        performUpdate()
      },
    })
  } else {
    Modal.confirm({
      title: `Xác nhận cập nhật`,
      type: 'info',
      content: `Bạn có chắc muốn lưu lại thay đổi?`,
      okText: 'Tiếp tục',
      cancelText: 'Hủy bỏ',
      onOk() {
        performUpdate()
      },
    })
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

// const openChangePowerModal = async (record) => {
//   currentAdminId = record.userAdminId
//   await fetchStaffList()
//   selectedStaffId.value = null
//   modalChangePower.value = true
// }

// const handleChangePowerShift = () => {
//   const selectedStaff = staffList.value.find((staff) => staff.id === selectedStaffId.value)
//   const staffName = selectedStaff ? selectedStaff.name : 'nhân viên này'
//   Modal.confirm({
//     title: 'Xác nhận chuyển quyền',
//     content: `Bạn có chắc chắn muốn chuyển quyền Admin cho ${staffName}?
//     , nếu đông ý bạn sẽ đăng xuất ngay bây giờ`,
//     onOk() {
//       modalChangePowerLoading.value = true
//       const payload = { userAdminId: currentAdminId, userStaffId: selectedStaffId.value }
//       requestAPI
//         .post(API_ROUTES_ADMIN.FETCH_DATA_ADMIN + '/change-power', payload)
//         .then(() => {
//           message.success('Chuyển quyền thành công')
//           modalChangePower.value = false
//           fetchUsers()
//         })
//         .catch((err) => message.error(err?.response?.data?.message || 'Lỗi khi chuyển quyền'))
//         .finally(() => {
//           modalChangePowerLoading.value = false
//         })
//     },
//   })
// }

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
    content: `Bạn có chắc chắn muốn xóa Admin ${record.userAdminName}?`,
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
      message.success('Xóa Admin thành công')
      fetchUsers()
    })
    .catch((err) => {
      message.error(err?.response?.data?.message || 'Lỗi khi xóa Admin')
    })
    .finally(() => {
      loadingStore.hide()
    })
}

const handleClearFilter = () => {
  // Clear all filter values
  Object.keys(filter).forEach((key) => {
    filter[key] = ''
  })
  pagination.value.current = 1
  fetchUsers()
}

const handleShowModalAdd = () => {
  newUser.email = null
  newUser.staffCode = null
  newUser.staffName = null

  modalAdd.value = true
}

onMounted(() => {
  breadcrumbStore.setRoutes(breadcrumb.value)
  fetchUsers()
})
</script>

<template>
  <div class="container-fluid">
    <!-- Table -->
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
                    v-model:value="filter.searchQuery"
                    placeholder="Nhập mã, tên hoặc email"
                    allowClear
                    @change="fetchUsers"
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
                    @change="fetchUsers"
                  >
                    <a-select-option :value="''">-- Tất cả trạng thái --</a-select-option>
                    <a-select-option value="1">Hoạt động</a-select-option>
                    <a-select-option value="0">Không hoạt động</a-select-option>
                  </a-select>
                </div>

                <div class="col-12">
                  <div class="d-flex justify-content-center flex-wrap gap-2">
                    <a-button class="btn-light" @click="fetchUsers">
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

      <div class="col-12">
        <a-card :bordered="false" class="cart">
          <template #title><UnorderedListOutlined /> Danh sách Admin</template>

          <div class="d-flex justify-content-end mb-2 flex-wrap gap-3">
            <a-button type="primary" @click="handleShowModalAdd"
              ><PlusOutlined /> Thêm admin
            </a-button>
          </div>

          <a-table
            class="nowrap"
            :dataSource="users"
            :columns="columns"
            rowKey="userAdminId"
            :loading="isLoading"
            :pagination="pagination"
            @change="handleTableChange"
            :scroll="{ x: 'auto' }"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.dataIndex === 'userAdminStatus'">
                <a-switch
                  :checked="record.userAdminStatus === 1"
                  :disabled="record.isMySelf"
                  class="me-2"
                  @change="handleChangeStatus(record)"
                />
                <a-tag :color="record.userAdminStatus === 1 ? 'green' : 'red'">{{
                  record.userAdminStatus === 1 ? 'Đang hoạt động' : 'Ngưng hoạt động'
                }}</a-tag>
              </template>

              <template v-else-if="column.key === 'actions'">
                <a-tooltip title="Chỉnh sửa thông tin">
                  <a-button
                    type="text"
                    class="btn-outline-info me-2"
                    @click="handleEditUser(record)"
                    ><EditFilled
                  /></a-button>
                </a-tooltip>
                <!-- Chuyển quyền: chỉ hiện khi là chính mình -->
                <!-- <a-tooltip title="Chuyển quyền" v-if="record.isMySelf">
              <a-button
                type="text"
                class="btn-outline-warning"
                @click="openChangePowerModal(record)"
                ><FilterFilled
              /></a-button>
            </a-tooltip> -->
                <template v-if="!record.isMySelf">
                  <a-tooltip title="Xóa Admin">
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
      </div>
    </div>

    <!-- Modal Thêm -->
    <a-modal
      v-model:open="modalAdd"
      @ok="handleAddUser"
      :okButtonProps="{ loading: modalAddLoading }"
      @cancel="clearNewUser"
      @close="clearNewUser"
    >
      <template #title>
        <PlusOutlined class="me-2 text-primary" />
        Thêm Admin
      </template>
      <a-form layout="vertical">
        <a-form-item label="Mã Admin" required>
          <a-input
            v-model:value="newUser.staffCode"
            placeholder="Nhập mã Admin"
            @keyup.enter="handleAddUser"
          />
        </a-form-item>
        <a-form-item label="Tên Admin" required>
          <a-input
            v-model:value="newUser.staffName"
            placeholder="Nhập tên Admin"
            @keyup.enter="handleAddUser"
          />
        </a-form-item>
        <a-form-item label="Email" required>
          <a-input
            v-model:value="newUser.email"
            placeholder="Nhập email"
            @keyup.enter="handleAddUser"
          />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- Modal Sửa -->
    <a-modal
      v-model:open="modalEdit"
      @ok="handleUpdateUser"
      :okButtonProps="{ loading: modalEditLoading }"
    >
      <template #title>
        <EditFilled class="me-2 text-primary" />
        Cập nhật Admin
      </template>
      <a-form layout="vertical">
        <a-form-item label="Mã Admin" required>
          <a-input
            v-model:value="editUser.staffCode"
            placeholder="Nhập mã Admin"
            @keyup.enter="handleUpdateUser"
          />
        </a-form-item>
        <a-form-item label="Tên Admin" required>
          <a-input
            v-model:value="editUser.staffName"
            placeholder="Nhập tên Admin"
            @keyup.enter="handleUpdateUser"
          />
        </a-form-item>
        <a-form-item label="Email" required>
          <a-input
            v-model:value="editUser.email"
            placeholder="Nhập email"
            @keyup.enter="handleUpdateUser"
          />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- Modal chuyển quyền -->
    <!-- <a-modal
      v-model:open="modalChangePower"
      title="Chuyển quyền Admin"
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
    </a-modal> -->
  </div>
</template>
