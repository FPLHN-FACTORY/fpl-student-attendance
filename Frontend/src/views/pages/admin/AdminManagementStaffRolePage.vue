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
      <a-tooltip title="Thêm chức vụ cho nhân viên">
        <a-button
          style="background-color: #fff7e6; color: black; border: 1px solid #ffa940"
          @click="openRoleModal"
        >
          Thêm chức vụ
        </a-button>
      </a-tooltip>
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
          <a-tooltip title="Xóa chức vụ">
            <a-button
              type="text"
              style="background-color: #fff7e6; border: 1px solid #ffa940"
              @click="handleDeleteRole(record)"
            >
              <DeleteOutlined />
            </a-button>
          </a-tooltip>
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
    v-model:open="isRoleModalVisible"
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
          <a-tooltip title="Chọn cơ sở tương ứng">
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
          </a-tooltip>
        </template>
        <!-- Checkbox thay cho nút -->
        <template v-else-if="column.key === 'select'">
          <a-tooltip title="Chọn để cập nhật chức vụ">
            <a-checkbox
              :checked="roleChecked[record.code] || false"
              @change="(e) => handleRoleCheckboxChange(record, e.target.checked)"
            />
          </a-tooltip>
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

const staffDetail = reactive({
  id: '',
  staffCode: '',
  name: '',
  emailFe: '',
  emailFpt: '',
})

const roles = ref([])

const columns = ref([
  { title: 'STT', dataIndex: 'index', key: 'index' },
  { title: 'Mã chức vụ', dataIndex: 'roleCode', key: 'roleCode' },
  { title: 'Tên chức vụ', dataIndex: 'roleName', key: 'roleName' },
  { title: 'Tên cơ sở', dataIndex: 'facilityName', key: 'facilityName' },
  { title: 'Hành động', key: 'actions' },
])

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

const groups = ref([])

const groupColumns = ref([
  { title: 'STT', dataIndex: 'index', key: 'index' },
  { title: 'Mã nhóm xưởng', dataIndex: 'groupCode', key: 'groupCode' },
  { title: 'Tên nhóm xưởng', dataIndex: 'groupName', key: 'groupName' },
  { title: 'Tên cơ sở', dataIndex: 'facilityName', key: 'facilityName' },
  { title: 'Hành động', key: 'actions' },
])

const facilitiesList = ref([])

const defaultRoles = ref([
  { code: 'ADMIN', name: 'Ban đào tạo' },
  { code: 'STAFF', name: 'Phụ trách xưởng' },
  { code: 'TEACHER', name: 'Giảng Viên' },
])

const selectedFacilities = reactive({})

defaultRoles.value.forEach((role) => {
  selectedFacilities[role.code] = '7adf9f59-f5ff-416a-86f8-edf1f201ebe6'
})

const roleChecked = reactive({})

const isRoleModalVisible = ref(false)

function openRoleModal() {
  isRoleModalVisible.value = true
}

function closeRoleModal() {
  isRoleModalVisible.value = false
}

const roleModalColumns = ref([
  { title: 'STT', dataIndex: 'index', key: 'index' },
  { title: 'Mã chức vụ', dataIndex: 'code', key: 'code' },
  { title: 'Tên chức vụ', dataIndex: 'name', key: 'name' },
  { title: 'Chọn cơ sở', dataIndex: 'facility', key: 'facility' },
  { title: 'Chọn', key: 'select' },
])

const route = useRoute()
const staffId = route.query.staffId || route.params.staffId

const roleIdMapping = {
  STAFF: 1,
  ADMIN: 0,
  TEACHER: 3,
}

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

function fetchRoleChecked() {
  const requestPayload = {
    staffId: staffId,
  }

  requestAPI
    .get(`${API_ROUTES_ADMIN.FETCH_DATA_STAFF_ROLE}/role-check`, { params: requestPayload })
    .then((res) => {
      const checkedRoles = res.data.data.data
      defaultRoles.value.forEach((defaultRole) => {
        const roleData = checkedRoles.find((item) => item.roleCode === defaultRole.code)
        if (roleData && roleData.checked === true) {
          roleChecked[defaultRole.code] = true
          const facilityObj = facilitiesList.value.find(
            (f) => f.facilityName === roleData.facilityName,
          )
          if (facilityObj) {
            selectedFacilities[defaultRole.code] = facilityObj.facilityId
          }
        } else {
          roleChecked[defaultRole.code] = false
        }
      })
    })
    .catch((error) => {
      message.error(
        (error.response && error.response.data && error.response.data.message) ||
          'Lỗi khi lấy danh sách vai trò đã tích',
      )
    })
}

function fetchStaffDetail() {
  requestAPI
    .get(`${API_ROUTES_ADMIN.FETCH_DATA_STAFF}/${staffId}`)
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

function fetchFacilitiesList() {
  requestAPI
    .get(`${API_ROUTES_ADMIN.FETCH_DATA_STAFF_ROLE}/facilities`)
    .then((res) => {
      facilitiesList.value = res.data.data
      if (facilitiesList.value && facilitiesList.value.length > 0) {
        const defaultFacilityId = facilitiesList.value[0].facilityId
        defaultRoles.value.forEach((role) => {
          selectedFacilities[role.code] = defaultFacilityId
        })
      }
    })
    .catch((error) => {
      message.error(
        (error.response && error.response.data && error.response.data.message) ||
          'Lỗi khi lấy danh sách cơ sở',
      )
    })
}

function updateSelectedFacility(roleCode, value) {
  selectedFacilities[roleCode] = value
  const facilityObj = facilitiesList.value.find((f) => f.facilityId === value)
  const facilityName = facilityObj ? facilityObj.facilityName : ''
  const exists = roles.value.find((r) => r.roleCode === roleCode && r.facilityName === facilityName)
  roleChecked[roleCode] = exists ? true : false
}

function handleRoleCheckboxChange(role, isChecked) {
  roleChecked[role.code] = isChecked
  const facilityId = selectedFacilities[role.code]
  const facilityObj = facilitiesList.value.find((f) => f.facilityId === facilityId)
  const facilityName = facilityObj ? facilityObj.facilityName : facilityId

  const payload = {
    idStaff: staffDetail.id,
    idRole: roleIdMapping[role.code],
    facilityId: facilityId,
    staffCode: staffDetail.staffCode,
  }

  requestAPI
    .put(`${API_ROUTES_ADMIN.FETCH_DATA_STAFF_ROLE}/change-role`, payload)
    .then(() => {
      if (isChecked) {
        message.success(`Cập nhật vai trò ${role.name} với cơ sở ${facilityName} thành công`)
      } else {
        message.success(`Bỏ vai trò ${role.name} thành công`)
      }
      fetchRoles()
      fetchRoleChecked()
    })
    .catch((error) => {
      message.error(`Lỗi khi cập nhật vai trò ${role.name}`)
    })
}

function handleDeleteRole(record) {
  message.info(`Xóa chức vụ ${record.roleCode} chưa triển khai`)
}

onMounted(() => {
  if (!staffId) {
    message.warning('Không tìm thấy staffId')
  } else {
    fetchStaffDetail()
    fetchRoles()
    fetchRoleChecked()
    fetchFacilitiesList()
  }
})
</script>

<style scoped>
.cart {
  margin-top: 5px;
}
</style>
