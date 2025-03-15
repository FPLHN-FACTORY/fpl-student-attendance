<template>
  <h1>Quản lý nhân viên</h1>

  <!-- Bộ lọc tìm kiếm -->
  <a-card title="Bộ lọc" :bordered="false" class="cart">
    <a-row :gutter="16" class="filter-container">
      <!-- Input tìm kiếm theo mã nhân viên -->
      <a-col :span="8">
        <a-input
          v-model:value="filter.searchQuery"
          placeholder="Tìm kiếm theo mã, tên, email"
          allowClear
          @change="fetchStaffs"
        />
      </a-col>
      <!-- Combobox trạng thái -->
      <a-col :span="8">
        <a-select
          v-model:value="filter.status"
          placeholder="Chọn trạng thái"
          allowClear
          style="width: 100%"
          @change="fetchStaffs"
        >
          <a-select-option :value="''">Tất cả trạng thái</a-select-option>
          <a-select-option value="ACTIVE">Hoạt động</a-select-option>
          <a-select-option value="INACTIVE">Không hoạt động</a-select-option>
        </a-select>
      </a-col>
      <!-- Combobox cơ sở được fetch từ backend -->
      <a-col :span="8">
        <a-select
          v-model:value="filter.idFacility"
          placeholder="Chọn cơ sở"
          allowClear
          style="width: 100%"
          @change="fetchStaffs"
        >
          <a-select-option :value="''">Tất cả cơ sở</a-select-option>
          <a-select-option
            v-for="facility in facilitiesList"
            :key="facility.facilityId"
            :value="facility.facilityId"
          >
            {{ facility.facilityName }}
          </a-select-option>
        </a-select>
      </a-col>
    </a-row>
  </a-card>

  <!-- Danh sách nhân viên -->
  <a-card title="Danh sách nhân viên" :bordered="false" class="cart">
    <div style="display: flex; justify-content: flex-end; margin-bottom: 10px">
      <!-- Nút thêm nhân viên với tooltip -->
      <a-tooltip title="Thêm mới nhân viên">
        <a-button
          style="background-color: #fff7e6; color: black; border: 1px solid #ffa940"
          @click="() => (modalAdd = true)"
        >
          <PlusOutlined />
          Thêm
        </a-button>
      </a-tooltip>
    </div>
    <a-table
      :dataSource="staffs"
      :columns="columns"
      rowKey="id"
      bordered
      :pagination="pagination"
      @change="handleTableChange"
    >
      <template #bodyCell="{ column, record }">
        <!-- Hiển thị trạng thái -->
        <template v-if="column.dataIndex === 'staffStatus'">
          <a-tag
            :color="record.staffStatus === 'ACTIVE' || record.staffStatus === 1 ? 'green' : 'red'"
          >
            {{
              record.staffStatus === 'ACTIVE' || record.staffStatus === 1
                ? 'Hoạt động'
                : 'Không hoạt động'
            }}
          </a-tag>
        </template>
        <!-- Các nút chức năng có tooltip -->
        <template v-else-if="column.key === 'actions'">
          <a-tooltip title="Sửa nhân viên">
            <a-button
              @click="handleUpdateStaff(record)"
              type="text"
              style="background-color: #fff7e6; margin-right: 8px; border: 1px solid #ffa940"
            >
              <EditOutlined />
            </a-button>
          </a-tooltip>
          <a-tooltip title="Chức vụ/ cơ sở/ bộ môn">
            <a-button
              @click="handleDetailStaff(record)"
              type="text"
              style="background-color: #fff7e6; margin-right: 8px; border: 1px solid #ffa940"
            >
              <EyeOutlined />
            </a-button>
          </a-tooltip>
          <a-tooltip title="Đổi trạng thái nhân viên">
            <a-button
              @click="handleChangeStatusStaff(record)"
              type="text"
              style="background-color: #fff7e6; border: 1px solid #ffa940"
            >
              <SwapOutlined />
            </a-button>
          </a-tooltip>
        </template>
        <template v-else>
          {{ record[column.dataIndex] }}
        </template>
      </template>
    </a-table>
  </a-card>

  <!-- Modal thêm nhân viên -->
  <a-modal v-model:open="modalAdd" title="Thêm nhân viên" @ok="handleAddStaff">
    <a-form layout="vertical">
      <a-form-item label="Mã nhân viên" required>
        <a-input v-model:value="newStaff.staffCode" />
      </a-form-item>
      <a-form-item label="Tên nhân viên" required>
        <a-input v-model:value="newStaff.name" />
      </a-form-item>
      <a-form-item label="Email FE" required>
        <a-input v-model:value="newStaff.emailFe" />
      </a-form-item>
      <a-form-item label="Email FPT" required>
        <a-input v-model:value="newStaff.emailFpt" />
      </a-form-item>
    </a-form>
  </a-modal>

  <!-- Modal cập nhật nhân viên -->
  <a-modal v-model:open="modalUpdate" title="Cập nhật nhân viên" @ok="updateStaff">
    <a-form layout="vertical">
      <a-form-item label="Mã nhân viên" required>
        <a-input v-model:value="detailStaff.staffCode" />
      </a-form-item>
      <a-form-item label="Tên nhân viên" required>
        <a-input v-model:value="detailStaff.name" />
      </a-form-item>
      <a-form-item label="Email FE" required>
        <a-input v-model:value="detailStaff.emailFe" />
      </a-form-item>
      <a-form-item label="Email FPT" required>
        <a-input v-model:value="detailStaff.emailFpt" />
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { message, Modal } from 'ant-design-vue'
import { PlusOutlined, EditOutlined, SwapOutlined, EyeOutlined } from '@ant-design/icons-vue'
import requestAPI from '@/services/requestApiService'
import { ROUTE_NAMES } from '@/router/adminRoute'
import router from '@/router'
import { API_ROUTES_ADMIN } from '@/constants/adminConstant'

// Danh sách nhân viên
const staffs = ref([])

// Danh sách cơ sở (để hiển thị trong select option)
const facilitiesList = ref([])

// Biến lọc: backend mong đợi searchQuery, idFacility và status
const filter = reactive({
  searchQuery: '',
  idFacility: '',
  status: '',
  page: 1,
  pageSize: 5,
})

// Dữ liệu phân trang
const pagination = reactive({
  current: 1,
  pageSize: 5,
  total: 0,
  showSizeChanger: false,
})

// Modal hiển thị
const modalAdd = ref(false)
const modalUpdate = ref(false)

// Dữ liệu thêm mới nhân viên
const newStaff = reactive({
  staffCode: '',
  name: '',
  emailFe: '',
  emailFpt: '',
})

// Dữ liệu chi tiết nhân viên (cập nhật)
const detailStaff = reactive({
  id: '',
  staffCode: '',
  name: '',
  emailFe: '',
  emailFpt: '',
})

// Cấu hình cột cho bảng
const columns = ref([
  { title: 'STT', dataIndex: 'rowNumber', key: 'rowNumber' },
  { title: 'Mã nhân viên', dataIndex: 'staffCode', key: 'staffCode' },
  { title: 'Tên nhân viên', dataIndex: 'staffName', key: 'staffName' },
  { title: 'Email FE', dataIndex: 'staffEmailFe', key: 'staffEmailFe' },
  { title: 'Email FPT', dataIndex: 'staffEmailFpt', key: 'staffEmailFpt' },
  { title: 'Cơ sở', dataIndex: 'facilityName', key: 'facilityName' },
  { title: 'Trạng thái', dataIndex: 'staffStatus', key: 'staffStatus' },
  { title: 'Chức năng', key: 'actions' },
])

// Hàm lấy danh sách nhân viên từ backend
const fetchStaffs = () => {
  requestAPI
    .get(API_ROUTES_ADMIN.FETCH_DATA_STAFF, { params: filter })
    .then((response) => {
      staffs.value = response.data.data.data
      pagination.total = response.data.data.totalPages * filter.pageSize
      pagination.current = filter.page
    })
    .catch((error) => {
      message.error(
        (error.response && error.response.data && error.response.data.message) ||
          'Lỗi khi lấy danh sách nhân viên'
      )
    })
}

// Hàm lấy danh sách cơ sở để hiển thị trong combobox
const fetchFacilitiesList = () => {
  requestAPI
    .get(`${API_ROUTES_ADMIN.FETCH_DATA_STAFF_ROLE}/facilities`)
    .then((response) => {
      // Giả sử API trả về { data: { data: [ { id, name }, ... ] } }
      facilitiesList.value = response.data.data.data || response.data.data
    })
    .catch((error) => {
      message.error(
        (error.response && error.response.data && error.response.data.message) ||
          'Lỗi khi lấy danh sách cơ sở'
      )
    })
}

// Sự kiện thay đổi trang bảng
const handleTableChange = (paginationData) => {
  filter.page = paginationData.current
  fetchStaffs()
}

// Hàm thêm nhân viên
const handleAddStaff = () => {
  if (!newStaff.staffCode || !newStaff.name || !newStaff.emailFe || !newStaff.emailFpt) {
    message.error('Vui lòng nhập đầy đủ thông tin')
    return
  }
  requestAPI
    .post(API_ROUTES_ADMIN.FETCH_DATA_STAFF, newStaff)
    .then(() => {
      message.success('Thêm nhân viên thành công')
      modalAdd.value = false
      fetchStaffs()
      clearNewStaffForm()
    })
    .catch((error) => {
      message.error(
        (error.response && error.response.data && error.response.data.message) ||
          'Lỗi khi thêm nhân viên'
      )
    })
}

// Hàm mở modal cập nhật, load chi tiết nhân viên
const handleUpdateStaff = (record) => {
  requestAPI
    .get(`${API_ROUTES_ADMIN.FETCH_DATA_STAFF}/${record.id}`)
    .then((response) => {
      const staff = response.data.data
      detailStaff.id = staff.id
      detailStaff.staffCode = staff.code
      detailStaff.name = staff.name
      detailStaff.emailFe = staff.emailFe
      detailStaff.emailFpt = staff.emailFpt
      modalUpdate.value = true
    })
    .catch((error) => {
      message.error(
        (error.response && error.response.data && error.response.data.message) ||
          'Lỗi khi lấy chi tiết nhân viên'
      )
    })
}

// Hàm submit cập nhật nhân viên
const updateStaff = () => {
  if (
    !detailStaff.staffCode ||
    !detailStaff.name ||
    !detailStaff.emailFe ||
    !detailStaff.emailFpt
  ) {
    message.error('Vui lòng nhập đầy đủ thông tin')
    return
  }
  requestAPI
    .put(API_ROUTES_ADMIN.FETCH_DATA_STAFF, detailStaff)
    .then(() => {
      message.success('Cập nhật nhân viên thành công')
      modalUpdate.value = false
      fetchStaffs()
    })
    .catch((error) => {
      message.error(
        (error.response && error.response.data && error.response.data.message) ||
          'Lỗi khi cập nhật nhân viên'
      )
    })
}

// Hàm xem chi tiết nhân viên (ví dụ)
const handleDetailStaff = (record) => {
  router.push({
    name: ROUTE_NAMES.MANAGEMENT_STAFF_ROLE,
    query: { staffId: record.id },
  })
}

// Hàm đổi trạng thái nhân viên
const handleChangeStatusStaff = (record) => {
  Modal.confirm({
    title: 'Xác nhận thay đổi trạng thái',
    content: `Bạn có chắc chắn muốn đổi trạng thái cho nhân viên ${record.staffName}?`,
    onOk: () => {
      requestAPI
        .put(`${API_ROUTES_ADMIN.FETCH_DATA_STAFF}/status/${record.id}`)
        .then(() => {
          message.success('Đổi trạng thái thành công')
          fetchStaffs()
        })
        .catch((error) => {
          message.error(
            (error.response && error.response.data && error.response.data.message) ||
              'Lỗi khi đổi trạng thái nhân viên'
          )
        })
    },
  })
}

const clearNewStaffForm = () => {
  newStaff.staffCode = ''
  newStaff.name = ''
  newStaff.emailFe = ''
  newStaff.emailFpt = ''
}

onMounted(() => {
  fetchStaffs()
  fetchFacilitiesList()
})
</script>

<style scoped>
.cart {
  margin-top: 5px;
}
.filter-container {
  margin-bottom: 5px;
}
</style>
