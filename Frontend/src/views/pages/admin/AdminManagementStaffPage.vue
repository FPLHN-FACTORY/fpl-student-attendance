<script setup>
import { ref, reactive, onMounted } from 'vue'
import { message, Modal } from 'ant-design-vue'
import {
  PlusOutlined,
  EditOutlined,
  SwapOutlined,
  EyeOutlined,
  EditFilled,
  EyeFilled,
  SyncOutlined,
  UnorderedListOutlined,
  FilterFilled,
} from '@ant-design/icons-vue'
import requestAPI from '@/services/requestApiService'
import { ROUTE_NAMES } from '@/router/adminRoute'
import router from '@/router'
import { useRouter } from 'vue-router'
import { API_ROUTES_ADMIN } from '@/constants/adminConstant'
import { DEFAULT_PAGINATION } from '@/constants/paginationConstant'
import { GLOBAL_ROUTE_NAMES } from '@/constants/routesConstant'
import useBreadcrumbStore from '@/stores/useBreadCrumbStore'

const breadcrumbStore = useBreadcrumbStore()

const breadcrumb = ref([
  {
    name: GLOBAL_ROUTE_NAMES.ADMIN_PAGE,
    breadcrumbName: 'Ban đào tạo',
  },
  {
    name: ROUTE_NAMES.MANAGEMENT_STAFF,
    breadcrumbName: 'Giảng viên',
  },
])

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
  ...DEFAULT_PAGINATION,
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
  { title: '#', dataIndex: 'rowNumber', key: 'rowNumber', width: 50 },
  { title: 'Mã nhân viên', dataIndex: 'staffCode', key: 'staffCode', width: 250 },
  { title: 'Tên nhân viên', dataIndex: 'staffName', key: 'staffName', width: 350 },
  { title: 'Email FE', dataIndex: 'staffEmailFe', key: 'staffEmailFe', width: 250 },
  { title: 'Email FPT', dataIndex: 'staffEmailFpt', key: 'staffEmailFpt', width: 250 },
  { title: 'Cơ sở', dataIndex: 'facilityName', key: 'facilityName', width: 80 },
  { title: 'Trạng thái', dataIndex: 'staffStatus', key: 'staffStatus', width: 80 },
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
const handleTableChange = (page) => {
  pagination.page = page.current
  pagination.pageSize = page.pageSize
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
  breadcrumbStore.setRoutes(breadcrumb.value)
  fetchStaffs()
  fetchFacilitiesList()
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
            <!-- Input tìm kiếm theo mã, tên, email -->
            <a-col :span="8" class="col">
              <a-input
                v-model:value="filter.searchQuery"
                placeholder="Tìm kiếm theo mã, tên, email"
                allowClear
                @change="fetchStaffs"
              />
            </a-col>
            <!-- Combobox trạng thái -->
            <a-col :span="8" class="col">
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
            <!-- Combobox cơ sở (fetch từ backend) -->
            <a-col :span="8" class="col">
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
      </div>
    </div>

    <!-- Card Danh sách nhân viên -->
    <div class="row g-3">
      <div class="col-12">
        <a-card :bordered="false" class="cart">
          <template #title> <UnorderedListOutlined /> Danh sách nhân viên </template>
          <div class="d-flex justify-content-end mb-3">
            <a-tooltip title="Thêm mới nhân viên">
              <!-- Sử dụng kiểu filled cho nút Thêm -->
              <a-button type="primary" @click="modalAdd = true">
                <PlusOutlined />
                Thêm
              </a-button>
            </a-tooltip>
          </div>
          <a-table
            :dataSource="staffs"
            :columns="columns"
            rowKey="id"
            :loading="isLoading"
            :pagination="pagination"
            :scroll="{ y: 500, x: 'auto' }"
            @change="handleTableChange"
          >
            <template #bodyCell="{ column, record }">
              <!-- Hiển thị trạng thái -->
              <template v-if="column.dataIndex === 'staffStatus'">
                <a-tag
                  :color="
                    record.staffStatus === 'ACTIVE' || record.staffStatus === 1 ? 'green' : 'red'
                  "
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
                <a-space>
                  <a-tooltip title="Chức vụ/ cơ sở/ bộ môn">
                    <a-button
                      @click="handleDetailStaff(record)"
                      type="text"
                      class="btn-outline-primary me-2"
                    >
                      <EyeFilled />
                    </a-button>
                  </a-tooltip>
                  <a-tooltip title="Sửa nhân viên">
                    <a-button
                      @click="handleUpdateStaff(record)"
                      type="text"
                      class="btn-outline-info me-2"
                    >
                      <EditFilled />
                    </a-button>
                  </a-tooltip>

                  <a-tooltip title="Đổi trạng thái nhân viên">
                    <a-button
                      @click="handleChangeStatusStaff(record)"
                      type="text"
                      class="btn-outline-warning"
                    >
                      <SyncOutlined />
                    </a-button>
                  </a-tooltip>
                </a-space>
              </template>
              <template v-else>
                {{ record[column.dataIndex] }}
              </template>
            </template>
          </a-table>
        </a-card>
      </div>
    </div>

    <!-- Modal Thêm nhân viên -->
    <!-- Modal Thêm nhân viên -->
    <a-modal v-model:open="modalAdd" title="Thêm nhân viên" @ok="handleAddStaff">
      <a-form layout="vertical">
        <a-form-item label="Mã nhân viên" required>
          <a-input v-model:value="newStaff.staffCode" placeholder="Nhập mã nhân viên" />
        </a-form-item>
        <a-form-item label="Tên nhân viên" required>
          <a-input v-model:value="newStaff.name" placeholder="Nhập tên nhân viên" />
        </a-form-item>
        <a-form-item label="Email FE" required>
          <a-input v-model:value="newStaff.emailFe" placeholder="Nhập email FE" />
        </a-form-item>
        <a-form-item label="Email FPT" required>
          <a-input v-model:value="newStaff.emailFpt" placeholder="Nhập email FPT" />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- Modal Cập nhật nhân viên -->
    <a-modal v-model:open="modalUpdate" title="Cập nhật nhân viên" @ok="updateStaff">
      <a-form layout="vertical">
        <a-form-item label="Mã nhân viên" required>
          <a-input v-model:value="detailStaff.staffCode" placeholder="Nhập mã nhân viên" />
        </a-form-item>
        <a-form-item label="Tên nhân viên" required>
          <a-input v-model:value="detailStaff.name" placeholder="Nhập tên nhân viên" />
        </a-form-item>
        <a-form-item label="Email FE" required>
          <a-input v-model:value="detailStaff.emailFe" placeholder="Nhập email FE" />
        </a-form-item>
        <a-form-item label="Email FPT" required>
          <a-input v-model:value="detailStaff.emailFpt" placeholder="Nhập email FPT" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>
