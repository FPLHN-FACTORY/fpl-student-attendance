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
import { API_ROUTES_ADMIN } from '@/constants/adminConstant'
import { DEFAULT_PAGINATION } from '@/constants'
import { API_ROUTES_EXCEL, GLOBAL_ROUTE_NAMES } from '@/constants/routesConstant'
import useBreadcrumbStore from '@/stores/useBreadCrumbStore'
import useLoadingStore from '@/stores/useLoadingStore'
import ExcelUploadButton from '@/components/excel/ExcelUploadButton.vue'

const breadcrumbStore = useBreadcrumbStore()
const loadingStore = useLoadingStore()

const breadcrumb = ref([
  {
    name: GLOBAL_ROUTE_NAMES.ADMIN_PAGE,
    breadcrumbName: 'Quản lý',
  },
  {
    name: ROUTE_NAMES.MANAGEMENT_STAFF,
    breadcrumbName: 'Quản lý giảng viên/ phụ trách xưởng',
  },
])

// Danh sách nhân viên
const staffs = ref([])

// Danh sách cơ sở (để hiển thị trong select option)
const facilitiesListCombobox = ref([])

// Danh sách vai trò (cố định dựa trên backend, xoá vai trò 'Ban đào tạo')
const rolesList = ref([
  { code: '1', name: 'Phụ trách xưởng' },
  { code: '3', name: 'Giảng viên' },
])

// Hàm mapping để chuyển đổi mã vai trò thành tên hiển thị, xoá vai trò 'Ban đào tạo'
const roleMapping = {
  1: 'Phụ trách xưởng',
  3: 'Giảng viên',
}
const convertRole = (roleCodes) => {
  if (typeof roleCodes === 'string') {
    return roleCodes
      .split(',')
      .map((code) => roleMapping[code.trim()] || code.trim())
      .join(', ')
  }
  if (Array.isArray(roleCodes)) {
    return roleCodes.map((code) => roleMapping[code] || code).join(', ')
  }
  return roleCodes
}

// Biến lọc và phân trang gửi lên API
const filter = reactive({
  searchQuery: '',
  idFacility: '',
  status: '',
})

// Sử dụng pagination dưới dạng ref giống mẫu plandate
const pagination = ref({ ...DEFAULT_PAGINATION })

// Biến loading cho bảng và modal
const isLoading = ref(false)
const modalAddLoading = ref(false)
const modalUpdateLoading = ref(false)

// Modal hiển thị
const modalAdd = ref(false)
const modalUpdate = ref(false)

// Dữ liệu thêm mới nhân viên
const newStaff = reactive({
  staffCode: '',
  name: '',
  emailFe: '',
  emailFpt: '',
  facilityId: null,
  roleCodes: [],
})

// Dữ liệu cập nhật nhân viên
const detailStaff = reactive({
  id: '',
  staffCode: '',
  name: '',
  emailFe: '',
  emailFpt: '',
  facilityId: null,
  roleCodes: [],
})

// Cấu hình cột cho bảng
const columns = ref([
  { title: '#', dataIndex: 'orderNumber', key: 'orderNumber', width: 50 },
  { title: 'Mã nhân viên', dataIndex: 'staffCode', key: 'staffCode', width: 250 },
  { title: 'Tên nhân viên', dataIndex: 'staffName', key: 'staffName', width: 250 },
  { title: 'Email FE', dataIndex: 'staffEmailFe', key: 'staffEmailFe', width: 250 },
  { title: 'Email FPT', dataIndex: 'staffEmailFpt', key: 'staffEmailFpt', width: 250 },
  { title: 'Cơ sở', dataIndex: 'facilityName', key: 'facilityName', width: 200 },
  { title: 'Vai trò', dataIndex: 'roleCode', key: 'roleCode', width: 300 },
  { title: 'Trạng thái', dataIndex: 'staffStatus', key: 'staffStatus', width: 180 },
  { title: 'Chức năng', key: 'actions', width: 120 },
])

// Hàm lấy danh sách nhân viên, dùng pagination.value.current và pagination.value.pageSize
const fetchStaffs = () => {
  if (isLoading.value) return
  loadingStore.show()
  isLoading.value = true
  requestAPI
    .get(API_ROUTES_ADMIN.FETCH_DATA_STAFF, {
      params: {
        ...filter,
        page: pagination.value.current,
        size: pagination.value.pageSize,
      },
    })
    .then((response) => {
      staffs.value = response.data.data.data
      pagination.value.total = response.data.data.totalPages * pagination.value.pageSize
    })
    .catch((error) => {
      message.error(
        (error.response && error.response.data && error.response.data.message) ||
          'Lỗi khi lấy danh sách nhân viên'
      )
    })
    .finally(() => {
      isLoading.value = false
      loadingStore.hide()
    })
}

const fetchFacilitiesListCombobox = () => {
  requestAPI
    .get(`${API_ROUTES_ADMIN.FETCH_DATA_STAFF}/facility`)
    .then((response) => {
      facilitiesListCombobox.value = response.data.data.data || response.data.data
    })
    .catch((error) => {
      message.error(
        (error.response && error.response.data && error.response.data.message) ||
          'Lỗi khi lấy danh sách cơ sở'
      )
    })
}

// Sự kiện thay đổi trang bảng, cập nhật cả current và pageSize rồi gọi lại fetchStaffs
const handleTableChange = (pageInfo) => {
  pagination.value.current = pageInfo.current
  pagination.value.pageSize = pageInfo.pageSize
  fetchStaffs()
}

// Hàm thêm nhân viên
const handleAddStaff = () => {
  if (
    !newStaff.staffCode ||
    !newStaff.name ||
    !newStaff.emailFe ||
    !newStaff.emailFpt ||
    !newStaff.facilityId ||
    newStaff.roleCodes.length === 0
  ) {
    message.error('Vui lòng nhập đầy đủ thông tin, bao gồm cơ sở và ít nhất một vai trò')
    return
  }
  modalAddLoading.value = true
  loadingStore.show()
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
    .finally(() => {
      modalAddLoading.value = false
      loadingStore.hide()
    })
}

// Hàm lấy chi tiết nhân viên để cập nhật
const handleUpdateStaff = (record) => {
  loadingStore.show()
  requestAPI
    .get(`${API_ROUTES_ADMIN.FETCH_DATA_STAFF}/${record.id}`)
    .then((response) => {
      const staff = response.data.data
      detailStaff.id = staff.id
      detailStaff.staffCode = staff.staffCode
      detailStaff.name = staff.staffName
      detailStaff.emailFe = staff.staffEmailFe
      detailStaff.emailFpt = staff.staffEmailFpt
      detailStaff.facilityId = staff.facilityId
      detailStaff.roleCodes = staff.roleCode.split(',').map((role) => role.trim())
      modalUpdate.value = true
    })
    .catch((error) => {
      message.error(
        (error.response && error.response.data && error.response.data.message) ||
          'Lỗi khi lấy chi tiết nhân viên'
      )
    })
    .finally(() => {
      loadingStore.hide()
    })
}

// Hàm cập nhật nhân viên
const updateStaff = () => {
  if (
    !detailStaff.staffCode ||
    !detailStaff.name ||
    !detailStaff.emailFe ||
    !detailStaff.emailFpt ||
    !detailStaff.facilityId ||
    detailStaff.roleCodes.length === 0
  ) {
    message.error('Vui lòng nhập đầy đủ thông tin, bao gồm cơ sở và vai trò')
    return
  }
  modalUpdateLoading.value = true
  loadingStore.show()
  requestAPI
    .put(`${API_ROUTES_ADMIN.FETCH_DATA_STAFF}/${detailStaff.id}`, detailStaff)
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
    .finally(() => {
      modalUpdateLoading.value = false
      loadingStore.hide()
    })
}

// Hàm đổi trạng thái nhân viên
const handleChangeStatusStaff = (record) => {
  Modal.confirm({
    title: 'Xác nhận thay đổi trạng thái',
    content: `Bạn có chắc chắn muốn đổi trạng thái cho nhân viên ${record.staffName}?`,
    onOk: () => {
      loadingStore.show()
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
        .finally(() => {
          loadingStore.hide()
        })
    },
  })
}

const clearNewStaffForm = () => {
  newStaff.staffCode = ''
  newStaff.name = ''
  newStaff.emailFe = ''
  newStaff.emailFpt = ''
  newStaff.facilityId = null
  newStaff.roleCodes = []
}
const configImportExcel = {
  fetchUrl: API_ROUTES_EXCEL.FETCH_IMPORT_STAFF,
  onSuccess: () => {
    fetchStaffs()
  },
  onError: () => {
    message.error('Không thể xử lý file excel')
  },
  showDownloadTemplate: true,
  showHistoryLog: true,
}

const handleClearFilter = () => {
  // Clear all filter values
  Object.keys(filter).forEach(key => {
    filter[key] = ''
  })
  pagination.value.current = 1
  fetchStaffs()
}

onMounted(() => {
  breadcrumbStore.setRoutes(breadcrumb.value)
  fetchStaffs()
  fetchFacilitiesListCombobox()
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
              <div class="label-title">Tìm kiếm theo mã, tên, email :</div>
              <a-input
                v-model:value="filter.searchQuery"
                placeholder="Tìm kiếm theo mã, tên, email"
                allowClear
                @change="fetchStaffs"
              />
            </a-col>
            <!-- Combobox trạng thái -->
            <a-col :span="8" class="col">
              <div class="label-title">Trạng thái :</div>
              <a-select
                v-model:value="filter.status"
                placeholder="Chọn trạng thái"
                allowClear
                style="width: 100%"
                @change="fetchStaffs"
              >
                <a-select-option :value="''">Tất cả trạng thái</a-select-option>
                <a-select-option value="ACTIVE">Đang hoạt động</a-select-option>
                <a-select-option value="INACTIVE">Ngừng hoạt động</a-select-option>
              </a-select>
            </a-col>
            <!-- Combobox cơ sở -->
            <a-col :span="8" class="col">
              <div class="label-title">Cơ sở :</div>
              <a-select
                v-model:value="filter.idFacility"
                placeholder="Chọn cơ sở"
                allowClear
                style="width: 100%"
                @change="fetchStaffs"
              >
                <a-select-option :value="''">Tất cả cơ sở</a-select-option>
                <a-select-option
                  v-for="facility in facilitiesListCombobox"
                  :key="facility.id"
                  :value="facility.id"
                >
                  {{ facility.name }}
                </a-select-option>
              </a-select>
            </a-col>
          </a-row>
          <div class="row">
            <div class="col-12">
              <div class="d-flex justify-content-center flex-wrap gap-2 mt-3">
                <a-button class="btn-light" @click="fetchStaffs">
                  <FilterFilled /> Lọc
                </a-button>
                <a-button class="btn-gray" @click="handleClearFilter"> Huỷ lọc </a-button>
              </div>
            </div>
          </div>
        </a-card>
      </div>
    </div>

    <!-- Card Danh sách nhân viên -->
    <div class="row g-3">
      <div class="col-12">
        <a-card :bordered="false" class="cart">
          <template #title> <UnorderedListOutlined /> Danh sách nhân viên </template>
          <div class="d-flex justify-content-end mb-3 flex-wrap gap-3">
            <ExcelUploadButton v-bind="configImportExcel" />
            <a-tooltip title="Thêm mới nhân viên">
              <a-button type="primary" @click="modalAdd = true"> <PlusOutlined /> Thêm </a-button>
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
              <template v-if="column.dataIndex === 'staffStatus'">
                <span class="nowrap">
                  <a-switch
                    class="me-2"
                    :checked="record.staffStatus === 'ACTIVE' || record.staffStatus === 1"
                    @change="handleChangeStatusStaff(record)"
                  />
                  <a-tag
                    :color="
                      record.staffStatus === 'ACTIVE' || record.staffStatus === 1 ? 'green' : 'red'
                    "
                  >
                    {{
                      record.staffStatus === 'ACTIVE' || record.staffStatus === 1
                        ? 'Đang hoạt động'
                        : 'Ngừng hoạt động'
                    }}
                  </a-tag>
                </span>
              </template>
              <template v-else-if="column.dataIndex === 'roleCode'">
                {{ convertRole(record.roleCode) }}
              </template>
              <template v-else-if="column.key === 'actions'">
                <a-space>
                  <a-tooltip title="Sửa nhân viên">
                    <a-button
                      @click="handleUpdateStaff(record)"
                      type="text"
                      class="btn-outline-info me-2"
                    >
                      <EditFilled />
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
    <a-modal
      v-model:open="modalAdd"
      title="Thêm nhân viên"
      @ok="handleAddStaff"
      :okButtonProps="{ loading: modalAddLoading }"
    >
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
        <a-form-item label="Cơ sở" required>
          <a-select v-model:value="newStaff.facilityId" placeholder="Chọn cơ sở">
            <a-select-option
              v-for="facility in facilitiesListCombobox"
              :key="facility.id"
              :value="facility.id"
            >
              {{ facility.name }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="Vai trò" required>
          <a-select v-model:value="newStaff.roleCodes" mode="multiple" placeholder="Chọn vai trò">
            <a-select-option v-for="role in rolesList" :key="role.code" :value="role.code">
              {{ role.name }}
            </a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- Modal Cập nhật nhân viên -->
    <a-modal
      v-model:open="modalUpdate"
      title="Cập nhật nhân viên"
      @ok="updateStaff"
      :okButtonProps="{ loading: modalUpdateLoading }"
    >
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
        <a-form-item label="Cơ sở" required>
          <a-select v-model:value="detailStaff.facilityId" placeholder="Chọn cơ sở">
            <a-select-option
              v-for="facility in facilitiesListCombobox"
              :key="facility.id"
              :value="facility.id"
            >
              {{ facility.name }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="Vai trò" required>
          <a-select
            v-model:value="detailStaff.roleCodes"
            mode="multiple"
            placeholder="Chọn vai trò"
          >
            <a-select-option v-for="role in rolesList" :key="role.code" :value="role.code">
              {{ role.name }}
            </a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>
