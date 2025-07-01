<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { message, Modal } from 'ant-design-vue'
import {
  PlusOutlined,
  EditFilled,
  UnorderedListOutlined,
  FilterFilled,
  SearchOutlined,
} from '@ant-design/icons-vue'
import requestAPI from '@/services/requestApiService'
import { ROUTE_NAMES } from '@/router/adminRoute'
import { API_ROUTES_ADMIN } from '@/constants/adminConstant'
import { DEFAULT_PAGINATION } from '@/constants'
import { API_ROUTES_EXCEL, GLOBAL_ROUTE_NAMES } from '@/constants/routesConstant'
import useBreadcrumbStore from '@/stores/useBreadCrumbStore'
import useLoadingStore from '@/stores/useLoadingStore'
import ExcelUploadButton from '@/components/excel/ExcelUploadButton.vue'
import { autoAddColumnWidth } from '@/utils/utils'

const breadcrumbStore = useBreadcrumbStore()
const loadingStore = useLoadingStore()

const breadcrumb = ref([
  {
    name: GLOBAL_ROUTE_NAMES.ADMIN_PAGE,
    breadcrumbName: 'Admin',
  },
  {
    name: ROUTE_NAMES.MANAGEMENT_STAFF,
    breadcrumbName: 'Quản lý nhân sự',
  },
])

const staffs = ref([])

const facilitiesListCombobox = ref([])

const rolesList = ref([
  { code: '1', name: 'Phụ trách xưởng' },
  { code: '3', name: 'Giảng viên' },
])

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

const filter = reactive({
  searchQuery: '',
  roleCodeFilter: '',
  idFacility: '',
  status: '',
})

const pagination = ref({ ...DEFAULT_PAGINATION })

// Biến loading cho bảng và modal
const isLoading = ref(false)
const modalAddLoading = ref(false)
const modalUpdateLoading = ref(false)

// Modal hiển thị
const modalAdd = ref(false)
const modalUpdate = ref(false)

// Thêm các hằng số cho domain email
const EMAIL_DOMAINS = {
  FE: '@fe.edu.vn',
  FPT: '@fpt.edu.vn',
}

// Sửa lại newStaff để thêm computed properties cho email
const newStaff = reactive({
  staffCode: '',
  name: '',
  emailFe: '',
  emailFpt: '',
  facilityId: null,
  roleCodes: [],
})

// Thêm computed properties cho email
const emailFeWithDomain = computed({
  get: () => newStaff.emailFe,
  set: (value) => {
    // Loại bỏ domain nếu có
    newStaff.emailFe = value.replace(EMAIL_DOMAINS.FE, '')
  },
})

const emailFptWithDomain = computed({
  get: () => newStaff.emailFpt,
  set: (value) => {
    // Loại bỏ domain nếu có
    newStaff.emailFpt = value.replace(EMAIL_DOMAINS.FPT, '')
  },
})

// Dữ liệu cập nhật phụ trách / giảng viên
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
const columns = ref(
  autoAddColumnWidth([
    { title: '#', dataIndex: 'orderNumber', key: 'orderNumber' },
    { title: 'Mã phụ trách / giảng viên', dataIndex: 'staffCode', key: 'staffCode' },
    { title: 'Tên phụ trách / giảng viên', dataIndex: 'staffName', key: 'staffName' },
    { title: 'Email FE', dataIndex: 'staffEmailFe', key: 'staffEmailFe' },
    { title: 'Email FPT', dataIndex: 'staffEmailFpt', key: 'staffEmailFpt' },
    { title: 'Cơ sở', dataIndex: 'facilityName', key: 'facilityName' },
    { title: 'Vai trò', dataIndex: 'roleCode', key: 'roleCode' },
    { title: 'Trạng thái', dataIndex: 'staffStatus', key: 'staffStatus' },
    { title: 'Chức năng', key: 'actions' },
  ]),
)

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
          'Lỗi khi lấy danh sách phụ trách / giảng viên',
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
          'Lỗi khi lấy danh sách cơ sở',
      )
    })
}

// Sự kiện thay đổi trang bảng, cập nhật cả current và pageSize rồi gọi lại fetchStaffs
const handleTableChange = (pageInfo) => {
  pagination.value.current = pageInfo.current
  pagination.value.pageSize = pageInfo.pageSize
  fetchStaffs()
}

// Sửa lại hàm handleAddStaff để thêm domain vào email
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
  Modal.confirm({
    title: 'Xác nhận thêm mới',
    content: 'Bạn có chắc chắn muốn thêm phụ trách / giảng viên mới này?',
    okText: 'Tiếp tục',
    cancelText: 'Hủy bỏ',
    onOk() {
      modalAddLoading.value = true
      loadingStore.show()
      const payload = {
        ...newStaff,
        emailFe: newStaff.emailFe + EMAIL_DOMAINS.FE,
        emailFpt: newStaff.emailFpt + EMAIL_DOMAINS.FPT,
      }
      requestAPI
        .post(API_ROUTES_ADMIN.FETCH_DATA_STAFF, payload)
        .then(() => {
          message.success('Thêm phụ trách / giảng viên thành công')
          modalAdd.value = false
          fetchStaffs()
          clearNewStaffForm()
        })
        .catch((error) => {
          message.error(
            (error.response && error.response.data && error.response.data.message) ||
              'Lỗi khi thêm phụ trách / giảng viên',
          )
        })
        .finally(() => {
          modalAddLoading.value = false
          loadingStore.hide()
        })
    },
  })
}

// Hàm lấy chi tiết phụ trách / giảng viên để cập nhật
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
          'Lỗi khi lấy chi tiết phụ trách / giảng viên',
      )
    })
    .finally(() => {
      loadingStore.hide()
    })
}

// Hàm cập nhật phụ trách / giảng viên
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
  Modal.confirm({
    title: 'Xác nhận cập nhật',
    content: 'Bạn có chắc chắn muốn cập nhật thông tin phụ trách / giảng viên này?',
    okText: 'Tiếp tục',
    cancelText: 'Hủy bỏ',
    onOk() {
      modalUpdateLoading.value = true
      loadingStore.show()
      requestAPI
        .put(`${API_ROUTES_ADMIN.FETCH_DATA_STAFF}/${detailStaff.id}`, detailStaff)
        .then(() => {
          message.success('Cập nhật phụ trách / giảng viên thành công')
          modalUpdate.value = false
          fetchStaffs()
        })
        .catch((error) => {
          message.error(
            (error.response && error.response.data && error.response.data.message) ||
              'Lỗi khi cập nhật phụ trách / giảng viên',
          )
        })
        .finally(() => {
          modalUpdateLoading.value = false
          loadingStore.hide()
        })
    },
  })
}

// Hàm đổi trạng thái phụ trách / giảng viên
const handleChangeStatusStaff = (record) => {
  Modal.confirm({
    title: 'Xác nhận thay đổi trạng thái',
    content: `Bạn có chắc chắn muốn đổi trạng thái cho phụ trách / giảng viên ${record.staffName}?`,
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
              'Lỗi khi đổi trạng thái phụ trách / giảng viên',
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
  showExport: true,
  showHistoryLog: true,
  btnImport: 'Import phụ trách / giảng viên',
  btnExport: 'Export phụ trách / giảng viên',
}

const handleClearFilter = () => {
  // Clear all filter values
  Object.keys(filter).forEach((key) => {
    filter[key] = ''
  })
  pagination.value.current = 1
  fetchStaffs()
}

const handleShowModalAdd = () => {
  newStaff.emailFe = null
  newStaff.emailFpt = null
  newStaff.facilityId = null
  newStaff.name = null
  newStaff.roleCodes = []
  newStaff.staffCode = null

  modalAdd.value = true
}

onMounted(() => {
  breadcrumbStore.setRoutes(breadcrumb.value)
  fetchStaffs()
  fetchFacilitiesListCombobox()
})
</script>

<template>
  <div class="container-fluid">
    <!-- Card Danh sách phụ trách / giảng viên -->
    <div class="row g-3">
      <div class="col-12">
        <a-card :bordered="false" class="cart no-body-padding">
          <a-collapse ghost>
            <a-collapse-panel>
              <template #header><FilterFilled /> Bộ lọc</template>
              <div class="row g-3 filter-container">
                <!-- Input tìm kiếm theo mã, tên, email -->
                <a-col class="col-lg-12 col-md-12 col-sm-12">
                  <div class="label-title">Từ khoá:</div>
                  <a-input
                    v-model:value="filter.searchQuery"
                    placeholder="Tìm kiếm theo mã, tên, email"
                    allowClear
                    @change="fetchStaffs"
                  >
                    <template #prefix>
                      <SearchOutlined />
                    </template>
                  </a-input>
                </a-col>
                <!-- Combobox trạng thái -->
                <a-col class="col-lg-4 col-md-4 col-sm-12">
                  <div class="label-title">Trạng thái:</div>
                  <a-select
                    v-model:value="filter.status"
                    placeholder="Chọn trạng thái"
                    allowClear
                    class="w-100"
                    @change="fetchStaffs"
                  >
                    <a-select-option :value="''">Tất cả trạng thái</a-select-option>
                    <a-select-option value="ACTIVE">Đang hoạt động</a-select-option>
                    <a-select-option value="INACTIVE">Ngừng hoạt động</a-select-option>
                  </a-select>
                </a-col>

                <!-- Combobox vai trò -->
                <a-col class="col-lg-4 col-md-4 col-sm-6">
                  <div class="label-title">Vai trò:</div>
                  <a-select
                    v-model:value="filter.roleCodeFilter"
                    placeholder="Chọn vai trò"
                    allowClear
                    class="w-100"
                    @change="fetchStaffs"
                  >
                    <a-select-option :value="''">Tất cả vai trò</a-select-option>
                    <a-select-option value="1">Phụ trách xưởng</a-select-option>
                    <a-select-option value="3">Giảng viên</a-select-option>
                  </a-select>
                </a-col>
                <!-- Combobox cơ sở -->
                <a-col class="col-lg-4 col-md-4 col-sm-6">
                  <div class="label-title">Cơ sở:</div>
                  <a-select
                    v-model:value="filter.idFacility"
                    placeholder="Chọn cơ sở"
                    allowClear
                    class="w-100"
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

                <div class="col-12">
                  <div class="d-flex justify-content-center flex-wrap gap-2">
                    <a-button class="btn-light" @click="fetchStaffs">
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
          <template #title> <UnorderedListOutlined /> Danh sách giảng viên </template>
          <div class="d-flex justify-content-end flex-wrap gap-3 mb-2">
            <ExcelUploadButton v-bind="configImportExcel" />
            <a-button type="primary" @click="handleShowModalAdd">
              <PlusOutlined /> Thêm giảng viên
            </a-button>
          </div>

          <a-table
            class="nowrap"
            :dataSource="staffs"
            :columns="columns"
            rowKey="id"
            :loading="isLoading"
            :pagination="pagination"
            :scroll="{ x: 'auto' }"
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
                <a-badge status="processing" />
                {{ convertRole(record.roleCode) }}
              </template>
              <template v-else-if="column.dataIndex === 'facilityName'">
                <a-tag>
                  {{ record.facilityName }}
                </a-tag>
              </template>
              <template v-else-if="column.key === 'actions'">
                <a-space>
                  <a-tooltip title="Sửa phụ trách / giảng viên">
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

    <!-- Modal Thêm phụ trách / giảng viên -->
    <a-modal
      v-model:open="modalAdd"
      title="Thêm phụ trách / giảng viên"
      @ok="handleAddStaff"
      :okButtonProps="{ loading: modalAddLoading }"
      @cancel="clearNewStaffForm"
      @close="clearNewStaffForm"
    >
      <a-form layout="vertical">
        <a-form-item label="Mã phụ trách / giảng viên" required>
          <a-input
            v-model:value="newStaff.staffCode"
            placeholder="Nhập mã phụ trách / giảng viên"
            @keyup.enter="handleAddStaff"
          />
        </a-form-item>
        <a-form-item label="Tên phụ trách / giảng viên" required>
          <a-input
            v-model:value="newStaff.name"
            placeholder="Nhập tên phụ trách / giảng viên"
            @keyup.enter="handleAddStaff"
          />
        </a-form-item>
        <a-form-item label="Email FE" required>
          <a-input-group compact>
            <a-input
              v-model:value="emailFeWithDomain"
              placeholder="Nhập email FE"
              style="width: calc(100% - 100px)"
              @keyup.enter="handleAddStaff"
            />
            <a-input
              :value="EMAIL_DOMAINS.FE"
              style="width: 100px; background-color: #f5f5f5"
              disabled
              @keyup.enter="handleAddStaff"
            />
          </a-input-group>
        </a-form-item>
        <a-form-item label="Email FPT" required>
          <a-input-group compact>
            <a-input
              v-model:value="emailFptWithDomain"
              placeholder="Nhập email FPT"
              style="width: calc(100% - 100px)"
              @keyup.enter="handleAddStaff"
            />
            <a-input
              :value="EMAIL_DOMAINS.FPT"
              style="width: 100px; background-color: #f5f5f5"
              disabled
              @keyup.enter="handleAddStaff"
            />
          </a-input-group>
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

    <!-- Modal Cập nhật phụ trách / giảng viên -->
    <a-modal
      v-model:open="modalUpdate"
      title="Cập nhật phụ trách / giảng viên"
      @ok="updateStaff"
      :okButtonProps="{ loading: modalUpdateLoading }"
    >
      <a-form layout="vertical">
        <a-form-item label="Mã phụ trách / giảng viên" required>
          <a-input
            v-model:value="detailStaff.staffCode"
            placeholder="Nhập mã phụ trách / giảng viên"
            @keyup.enter="updateStaff"
          />
        </a-form-item>
        <a-form-item label="Tên phụ trách / giảng viên" required>
          <a-input
            v-model:value="detailStaff.name"
            placeholder="Nhập tên phụ trách / giảng viên"
            @keyup.enter="updateStaff"
          />
        </a-form-item>
        <a-form-item label="Email FE" required>
          <a-input
            v-model:value="detailStaff.emailFe"
            placeholder="Nhập email FE"
            @keyup.enter="updateStaff"
          />
        </a-form-item>
        <a-form-item label="Email FPT" required>
          <a-input
            v-model:value="detailStaff.emailFpt"
            placeholder="Nhập email FPT"
            @keyup.enter="updateStaff"
          />
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
