<template>
  <!-- Thông tin nhân viên (4 ô input trên 1 hàng) -->
  <a-card title="Thông tin nhân viên" :bordered="false" class="cart">
    <a-form layout="vertical">
      <a-row :gutter="16">
        <!-- Mã nhân viên -->
        <a-col :span="6">
          <a-form-item label="Mã nhân viên">
            <a-input :value="staffDetail.staffCode" disabled />
          </a-form-item>
        </a-col>
        <!-- Tên nhân viên -->
        <a-col :span="6">
          <a-form-item label="Tên nhân viên">
            <a-input :value="staffDetail.name" disabled />
          </a-form-item>
        </a-col>
        <!-- Email FE -->
        <a-col :span="6">
          <a-form-item label="Email FE">
            <a-input :value="staffDetail.emailFe" disabled />
          </a-form-item>
        </a-col>
        <!-- Email FPT -->
        <a-col :span="6">
          <a-form-item label="Email FPT">
            <a-input :value="staffDetail.emailFpt" disabled />
          </a-form-item>
        </a-col>
      </a-row>
    </a-form>
  </a-card>

  <!-- Danh sách chức vụ -->
  <a-card title="Danh sách chức vụ" :bordered="false" class="cart">
    <!-- Nút mở modal thêm chức vụ -->
    <div style="display: flex; justify-content: flex-end; margin-bottom: 10px">
      <a-button
        style="background-color: #fff7e6; color: black; border: 1px solid #ffa940"
        @click="openRoleModal"
      >
        Thêm chức vụ
      </a-button>
    </div>
    <!-- Bảng hiển thị chức vụ -->
    <a-table :dataSource="roles" :columns="columns" rowKey="roleId" bordered>
      <template #bodyCell="{ column, record, index }">
        <!-- STT -->
        <template v-if="column.dataIndex === 'index'">
          {{ index + 1 }}
        </template>
        <!-- Mã chức vụ -->
        <template v-else-if="column.dataIndex === 'roleCode'">
          {{ record.roleCode }}
        </template>
        <!-- Tên chức vụ (map từ roleCode) -->
        <template v-else-if="column.dataIndex === 'roleName'">
          {{ mapRoleCodeToName(record.roleCode) }}
        </template>
        <!-- Tên cơ sở -->
        <template v-else-if="column.dataIndex === 'facilityName'">
          {{ record.facilityName }}
        </template>
        <!-- Cột hành động (xóa) -->
        <template v-else-if="column.key === 'actions'">
          <a-button
            type="text"
            style="background-color: #fff7e6; border: 1px solid #ffa940"
            @click="handleDeleteRole(record)"
          >
            <DeleteOutlined />
          </a-button>
        </template>
        <!-- Mặc định -->
        <template v-else>
          {{ record[column.dataIndex] }}
        </template>
      </template>
    </a-table>
  </a-card>

  <!-- Danh sách nhóm xưởng theo cơ sở -->
  <a-card title="Nhóm xưởng phụ trách" :bordered="false" class="cart">
    <!-- Nút thêm nhóm xưởng -->
    <div style="display: flex; justify-content: flex-end; margin-bottom: 10px">
      <a-button
        style="background-color: #fff7e6; color: black; border: 1px solid #ffa940"
        @click="handleAddFactory"
      >
        Thêm nhóm xưởng
      </a-button>
    </div>
    <!-- Bảng hiển thị nhóm xưởng -->
    <a-table :dataSource="groups" :columns="groupColumns" rowKey="groupId" bordered>
      <template #bodyCell="{ column, record, index }">
        <!-- STT -->
        <template v-if="column.dataIndex === 'index'">
          {{ index + 1 }}
        </template>
        <!-- Mã nhóm xưởng -->
        <template v-else-if="column.dataIndex === 'groupCode'">
          {{ record.groupCode }}
        </template>
        <!-- Tên nhóm xưởng -->
        <template v-else-if="column.dataIndex === 'groupName'">
          {{ record.groupName }}
        </template>
        <!-- Tên cơ sở -->
        <template v-else-if="column.dataIndex === 'facilityName'">
          {{ record.facilityName }}
        </template>
        <!-- Cột hành động -->
        <template v-else-if="column.key === 'actions'">
          <a-button
            type="text"
            style="background-color: #fff7e6; border: 1px solid #ffa940"
            @click="handleDeleteFactory(record)"
          >
            <DeleteOutlined />
          </a-button>
        </template>
        <!-- Mặc định -->
        <template v-else>
          {{ record[column.dataIndex] }}
        </template>
      </template>
    </a-table>
  </a-card>

  <!-- Modal thêm chức vụ -->
  <a-modal
    v-model:visible="isRoleModalVisible"
    title="Thêm chức vụ cho nhân viên"
    @cancel="closeRoleModal"
    width="60%"
    :footer="null"
  >
    <a-table :dataSource="defaultRoles" :columns="roleModalColumns" rowKey="code" bordered>
      <template #bodyCell="{ column, record, index }">
        <!-- STT -->
        <template v-if="column.dataIndex === 'index'">
          {{ index + 1 }}
        </template>
        <!-- Mã chức vụ -->
        <template v-else-if="column.dataIndex === 'code'">
          {{ record.code }}
        </template>
        <!-- Tên chức vụ -->
        <template v-else-if="column.dataIndex === 'name'">
          {{ record.name }}
        </template>
        <!-- Combobox chọn cơ sở -->
        <template v-else-if="column.dataIndex === 'facility'">
          <a-select
            style="width: 100%"
            v-model:value="selectedFacilities[record.code]"
            @change="(value) => updateSelectedFacility(record.code, value)"
          >
            <a-select-option
              v-for="facility in facilitiesList"
              :key="facility.facilityId"
              :value="facility.facilityId"
            >
              {{ facility.facilityName }}
            </a-select-option>
          </a-select>
        </template>
        <!-- Checkbox thay cho nút -->
        <template v-else-if="column.key === 'select'">
          <a-checkbox
            :checked="roleChecked[record.code] || false"
            @change="(e) => handleRoleCheckboxChange(record, e.target.checked)"
          />
        </template>
      </template>
    </a-table>
  </a-modal>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { message } from 'ant-design-vue'
import requestAPI from '@/services/requestApiService'
import { DeleteOutlined } from '@ant-design/icons-vue'
import { API_ROUTES_ADMIN } from '@/constants/adminConstant'

// Thông tin nhân viên (read-only)
const staffDetail = reactive({
  id: '',
  staffCode: '',
  name: '',
  emailFe: '',
  emailFpt: '',
})

// Danh sách chức vụ
const roles = ref([])

// Cấu hình cột cho bảng chức vụ
const columns = ref([
  { title: 'STT', dataIndex: 'index', key: 'index' },
  { title: 'Mã chức vụ', dataIndex: 'roleCode', key: 'roleCode' },
  { title: 'Tên chức vụ', dataIndex: 'roleName', key: 'roleName' },
  { title: 'Tên cơ sở', dataIndex: 'facilityName', key: 'facilityName' },
  { title: 'Hành động', key: 'actions' },
])

// Map mã chức vụ -> tên hiển thị
function mapRoleCodeToName(code) {
  switch (code) {
    case 'TEACHER':
      return 'Giảng Viên'
    case 'STAFF':
      return 'Phụ trách xưởng'
    case 'ADMIN':
      return 'Ban đào tạo'
    default:
      return code
  }
}

// Danh sách nhóm xưởng
const groups = ref([])

// Cấu hình cột cho bảng nhóm xưởng
const groupColumns = ref([
  { title: 'STT', dataIndex: 'index', key: 'index' },
  { title: 'Mã nhóm xưởng', dataIndex: 'groupCode', key: 'groupCode' },
  { title: 'Tên nhóm xưởng', dataIndex: 'groupName', key: 'groupName' },
  { title: 'Tên cơ sở', dataIndex: 'facilityName', key: 'facilityName' },
  { title: 'Hành động', key: 'actions' },
])

// Danh sách cơ sở cho combobox (fetch từ API)
const facilitiesList = ref([])

// Mảng danh sách chức vụ mặc định (lấy từ enum RoleConstant)
const defaultRoles = ref([
  { code: 'ADMIN', name: 'Ban đào tạo' },
  { code: 'STAFF', name: 'Phụ trách xưởng' },
  { code: 'TEACHER', name: 'Giảng Viên' },
])

// Object lưu trạng thái combobox của từng role trong modal, mặc định là facilityId được chỉ định
const selectedFacilities = reactive({})

// Mặc định, mỗi role sẽ chọn cơ sở có facilityId: "7adf9f59-f5ff-416a-86f8-edf1f201ebe6"
defaultRoles.value.forEach((role) => {
  selectedFacilities[role.code] = '7adf9f59-f5ff-416a-86f8-edf1f201ebe6'
})

// Object lưu trạng thái checkbox của từng role (ban đầu chưa chọn)
const roleChecked = reactive({})

// Modal thêm chức vụ
const isRoleModalVisible = ref(false)

function openRoleModal() {
  isRoleModalVisible.value = true
}

function closeRoleModal() {
  isRoleModalVisible.value = false
}

// Cấu hình cột cho modal thêm chức vụ
const roleModalColumns = ref([
  { title: 'STT', dataIndex: 'index', key: 'index' },
  { title: 'Mã chức vụ', dataIndex: 'code', key: 'code' },
  { title: 'Tên chức vụ', dataIndex: 'name', key: 'name' },
  { title: 'Chọn cơ sở', dataIndex: 'facility', key: 'facility' },
  { title: 'Chọn', key: 'select' },
])

// Lấy staffId từ route
const route = useRoute()
const staffId = route.query.staffId || route.params.staffId

// Định nghĩa mapping cho idRole
const roleIdMapping = {
  STAFF: 1,
  ADMIN: 2,
  TEACHER: 3,
}

// API lấy chi tiết nhân viên
function fetchStaffDetail() {
  requestAPI
    .get(`${API_ROUTES_ADMIN.FETCH_DATA_STAFF_ROLE}/${staffId}`)
    .then((res) => {
      const data = res.data.data
      staffDetail.id = data.id
      staffDetail.staffCode = data.code
      staffDetail.name = data.name
      staffDetail.emailFe = data.emailFe
      staffDetail.emailFpt = data.emailFpt
    })
    .catch((error) => {
      message.error(
        (error.response && error.response.data && error.response.data.message) ||
          'Lỗi khi lấy chi tiết nhân viên',
      )
    })
}

// API lấy danh sách chức vụ
function fetchRoles() {
  requestAPI
    .get(`${API_ROUTES_ADMIN.FETCH_DATA_STAFF_ROLE}/${staffId}`)
    .then((res) => {
      roles.value = res.data.data
    })
    .catch((error) => {
      message.error(
        (error.response && error.response.data && error.response.data.message) ||
          'Lỗi khi lấy danh sách chức vụ',
      )
    })
}

// API lấy danh sách cơ sở (fetch từ controller /facilities)
function fetchFacilitiesList() {
  requestAPI
    .get(`${API_ROUTES_ADMIN.FETCH_DATA_STAFF_ROLE}/facilities`)
    .then((res) => {
      // Giả sử API trả về: { data: [ { facilityId, facilityName }, ... ] }
      facilitiesList.value = res.data.data
    })
    .catch((error) => {
      message.error(
        (error.response && error.response.data && error.response.data.message) ||
          'Lỗi khi lấy danh sách cơ sở',
      )
    })
}

// Cập nhật trạng thái combobox cho từng role trong modal
function updateSelectedFacility(roleCode, value) {
  selectedFacilities[roleCode] = value
}

// Khi checkbox thay đổi, gọi API để thêm (hoặc bỏ) vai trò cho nhân viên theo cơ sở
function handleRoleCheckboxChange(role, isChecked) {
  roleChecked[role.code] = isChecked
  const facilityId = selectedFacilities[role.code]
  const payload = {
    idStaff: staffDetail.id,
    // Sử dụng mapping: nếu role.code là STAFF thì idRole = 1, ADMIN -> 2, TEACHER -> 3
    idRole: roleIdMapping[role.code],
    facilityId: facilityId,
  }
  // Gọi API PUT để thay đổi vai trò
  requestAPI
    .put(`${API_ROUTES_ADMIN.FETCH_DATA_STAFF_ROLE}/change-role`, payload)
    .then(() => {
      if (isChecked) {
        message.success(`Cập nhật vai trò ${role.name} với cơ sở ${facilityId} thành công`)
      } else {
        message.success(`Bỏ vai trò ${role.name} thành công`)
      }
      fetchRoles() // refresh danh sách chức vụ sau khi cập nhật
    })
    .catch((error) => {
      message.error(`Lỗi khi cập nhật vai trò ${role.name}`)
    })
}

function handleDeleteRole(record) {
  message.info(`Xóa chức vụ ${record.roleCode} chưa triển khai`)
}

function handleAddFactory() {
  message.info('Chức năng Thêm nhóm xưởng chưa triển khai')
}

function handleDeleteFactory(record) {
  message.info(`Xóa nhóm xưởng ${record.groupCode} chưa triển khai`)
}

onMounted(() => {
  if (!staffId) {
    message.warning('Không tìm thấy staffId')
  } else {
    fetchStaffDetail()
    fetchRoles()
    fetchFacilitiesList()
  }
})
</script>

<style scoped>
.cart {
  margin-top: 5px;
}
</style>
