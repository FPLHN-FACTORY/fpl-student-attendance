<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { message, Modal } from 'ant-design-vue'
import requestAPI from '@/services/requestApiService'
import { API_ROUTES_STAFF } from '@/constants/staffConstant'
import { API_ROUTES_EXCEL, GLOBAL_ROUTE_NAMES } from '@/constants/routesConstant'
import {
  PlusOutlined,
  DeleteFilled,
  EyeFilled,
  FilterFilled,
  UnorderedListOutlined,
  SearchOutlined,
} from '@ant-design/icons-vue'
import { useRoute } from 'vue-router'
import {
  DEFAULT_DATE_FORMAT,
  DEFAULT_PAGINATION,
  STATUS_PLAN_DATE_DETAIL,
  TYPE_SHIFT,
} from '@/constants'
import useBreadcrumbStore from '@/stores/useBreadCrumbStore'
import useLoadingStore from '@/stores/useLoadingStore'
import { ROUTE_NAMES } from '@/router/staffRoute'
import ExcelUploadButton from '@/components/excel/ExcelUploadButton.vue'
import { autoAddColumnWidth, dayOfWeek, formatDate } from '@/utils/utils'

const route = useRoute()
const factoryId = route.query.factoryId
const factoryName = route.query.factoryName

const breadcrumbStore = useBreadcrumbStore()

const breadcrumb = ref([
  {
    name: GLOBAL_ROUTE_NAMES.STAFF_PAGE,
    breadcrumbName: 'Phụ trách xưởng',
  },
  {
    name: ROUTE_NAMES.MANAGEMENT_FACTORY,
    breadcrumbName: 'Quản lý nhóm xưởng',
  },
])
const loadingStore = useLoadingStore()

const countFilter = ref(0)

const isLoading = ref(false)

if (!factoryId) {
  message.error('Không tìm thấy factoryId')
}

/* -------------------- Danh sách sinh viên đã có trong nhóm xưởng -------------------- */
const studentFactories = ref([])

const filter = reactive({
  searchQuery: '',
  status: null,
  page: 1,
  pageSize: 5,
})
const pagination = reactive({
  ...DEFAULT_PAGINATION,
})

const columns = ref(
  autoAddColumnWidth([
    { title: '#', dataIndex: 'rowNumber', key: 'rowNumber' },
    { title: 'Mã sinh viên', dataIndex: 'studentCode', key: 'studentCode' },
    { title: 'Tên sinh viên', dataIndex: 'studentName', key: 'studentName' },
    { title: 'Email sinh viên', dataIndex: 'studentEmail', key: 'studentEmail' },
    {
      title: 'Số buổi đã nghỉ',
      dataIndex: 'totalAbsentShift',
      key: 'totalAbsentShift',
      align: 'center',
    },
    {
      title: 'Tỉ lệ nghỉ',
      dataIndex: 'percenAbsentShift',
      key: 'percenAbsentShift',
      align: 'center',
    },
    { title: 'Trạng thái', dataIndex: 'statusStudentFactory', key: 'statusStudentFactory' },
    { title: 'Chi tiết', key: 'action' },
  ]),
)

/* -------------------- Phân trang cho danh sách sinh viên trong nhóm xưởng -------------------- */
const fetchStudentFactories = () => {
  loadingStore.show()
  requestAPI
    .get(API_ROUTES_STAFF.FETCH_DATA_STUDENT_FACTORY + '/' + factoryId, {
      params: {
        ...filter,
        factoryId,
        'userStudentRequest.searchQuery': filter.searchQuery,
        page: pagination.current,
        size: pagination.pageSize,
      },
    })
    .then((response) => {
      const result = response.data.data
      studentFactories.value = result.data
      if (result.totalRecords !== undefined) {
        pagination.total = result.totalRecords
      } else {
        pagination.total = result.totalPages * filter.pageSize
      }
      pagination.current = filter.page
      countFilter.value = result.totalItems
    })
    .catch((error) => {
      message.error(error.response?.data?.message || 'Lỗi khi lấy dữ liệu')
    })
    .finally(() => {
      loadingStore.hide()
    })
}

/* -------------------- Lấy danh sách sinh viên đã có trong nhóm (để tích checkbox) -------------------- */
const existingStudents = ref([])

const fetchExistingStudents = () => {
  loadingStore.show()
  requestAPI
    .get(API_ROUTES_STAFF.FETCH_DATA_STUDENT_FACTORY + '/exist-student/' + factoryId)
    .then((response) => {
      existingStudents.value = response.data.data || []
      // Kiểm tra xem breadcrumb đã tồn tại chưa, nếu chưa thì mới push
      if (!breadcrumbStore.routes.some((item) => item.breadcrumbName === factoryName)) {
        breadcrumbStore.push({
          breadcrumbName: factoryName,
        })
      }
      if (allStudents.value.length) {
        updateAllStudentsCheckStatus()
      }
    })
    .catch((error) => {
      message.error(error.response?.data?.message || 'Lỗi khi lấy dữ liệu')
    })
    .finally(() => {
      loadingStore.hide()
    })
}

/* -------------------- Danh sách tất cả sinh viên (modal thêm học sinh) -------------------- */
const studentFilter = reactive({
  searchQuery: '',
  page: 1,
  pageSize: 5,
})
const studentPagination = reactive({
  ...DEFAULT_PAGINATION,
})
const allStudents = ref([])
const studentColumns = ref(
  autoAddColumnWidth([
    { title: '#', dataIndex: 'rowNumber', key: 'rowNumber' },
    { title: 'Mã sinh viên', dataIndex: 'code', key: 'code' },
    { title: 'Tên sinh viên', dataIndex: 'name', key: 'name' },
    { title: 'Email', dataIndex: 'email', key: 'email' },
    { title: 'Chọn', key: 'select' },
  ]),
)
const selectedStudents = reactive({})

const updateAllStudentsCheckStatus = () => {
  const existIds = existingStudents.value.map((item) => item.studentId)
  allStudents.value = allStudents.value.map((item) => ({
    ...item,
    checked: existIds.includes(item.id),
  }))
}

const fetchAllStudents = () => {
  loadingStore.show()
  requestAPI
    .get(API_ROUTES_STAFF.FETCH_DATA_STUDENT_FACTORY + '/students', {
      params: {
        ...studentFilter,
        factoryId,
        'userStudentRequest.searchQuery': studentFilter.searchQuery,
        page: studentPagination.current,
        size: studentPagination.pageSize,
      },
    })
    .then((response) => {
      const result = response.data.data
      allStudents.value = result.data.map((item) => ({
        ...item,
        checked: false,
      }))
      if (result.totalRecords !== undefined) {
        studentPagination.total = result.totalRecords
      } else {
        studentPagination.total = result.totalPages * studentFilter.pageSize
      }
      studentPagination.current = studentFilter.page
      updateAllStudentsCheckStatus()
    })
    .catch((error) => {
      message.error(error.response?.data?.message || 'Lỗi khi lấy dữ liệu')
    })
    .finally(() => {
      loadingStore.hide()
    })
}

const handleStudentTableChange = (paginationObj) => {
  studentPagination.current = paginationObj.current
  studentPagination.pageSize = paginationObj.pageSize
  studentFilter.page = paginationObj.current
  studentFilter.pageSize = paginationObj.pageSize
  fetchAllStudents()
}

/* -------------------- Xử lý checkbox chọn sinh viên -------------------- */
const handleStudentCheckboxChange = (student, checked) => {
  selectedStudents[student.id] = checked
  if (checked) {
    const payload = {
      studentId: student.id,
      factoryId: factoryId,
    }
    loadingStore.show()
    requestAPI
      .post(API_ROUTES_STAFF.FETCH_DATA_STUDENT_FACTORY, payload)
      .then((response) => {
        message.success(response.data.message || 'Thêm sinh viên vào nhóm xưởng thành công')
        fetchExistingStudents() // Cập nhật danh sách sinh viên đã có trong nhóm
        fetchAllStudents() // Cập nhật danh sách tất cả sinh viên trong modal
        fetchStudentFactories() // Cập nhật bảng danh sách
      })
      .catch((error) => {
        message.error(error.response?.data?.message || 'Lỗi khi thêm sinh viên vào nhóm xưởng')
        selectedStudents[student.id] = false
      })
      .finally(() => {
        loadingStore.hide()
      })
  } else {
    // Khi bỏ tích, không thực hiện hành động xóa, chỉ hiển thị thông báo
    message.info('Sinh viên không thể bị xóa khỏi nhóm xưởng bằng cách bỏ tích')
    // Đặt lại giá trị checkbox
    selectedStudents[student.id] = true
  }
}
// trong phần <script setup>
function confirmDelete(record) {
  Modal.confirm({
    title: 'Xác nhận xóa sinh viên',
    content: `Nếu xóa sinh viên ${record.studentName} các dữ liệu về lịch sắp tới của sinh viên sẽ bị mất, bạn có thể thay đổi trạng thái để bảo toàn dữ liệu. Xóa?`,
    okType: 'danger',
    onOk() {
      deleteStudentFactory(record.studentFactoryId)
    },
  })
}

function deleteStudentFactory(studentFactoryId) {
  loadingStore.show()
  requestAPI
    .delete(`${API_ROUTES_STAFF.FETCH_DATA_STUDENT_FACTORY}/${studentFactoryId}`)
    .then((response) => {
      message.success(response.data.message || 'Xóa sinh viên thành công')
      fetchStudentFactories() // load lại danh sách
    })
    .catch((error) => {
      message.error(error.response?.data?.message || 'Lỗi khi xóa sinh viên khỏi nhóm xưởng')
    })
    .finally(() => {
      loadingStore.hide()
    })
}
const handleAddStudents = () => {
  isAddStudentModalVisible.value = false
}

const resetStudentModal = () => {
  isAddStudentModalVisible.value = false
  studentFilter.searchQuery = ''
  studentFilter.page = 1
  studentPagination.current = 1
  for (const key in selectedStudents) {
    selectedStudents[key] = false
  }
}

/* -------------------- Phân trang cho bảng sinh viên trong nhóm xưởng -------------------- */
const handleTableChange = (pageInfo) => {
  // Cập nhật current và pageSize
  pagination.current = pageInfo.current
  pagination.pageSize = pageInfo.pageSize
  filter.page = pageInfo.current
  filter.pageSize = pageInfo.pageSize
  fetchStudentFactories()
}

/* -------------------- Xử lý đổi trạng thái sinh viên -------------------- */
const confirmChangeStatus = (record) => {
  Modal.confirm({
    title: 'Xác nhận đổi trạng thái',
    content: `Bạn có chắc muốn đổi trạng thái cho sinh viên ${record.studentName}?`,
    onOk() {
      changeStatusStudentFactory(record.studentFactoryId)
    },
  })
}

const changeStatusStudentFactory = (studentFactoryId) => {
  loadingStore.show()
  requestAPI
    .put(API_ROUTES_STAFF.FETCH_DATA_STUDENT_FACTORY + '/' + studentFactoryId)
    .then((response) => {
      message.success(response.data.message || 'Đổi trạng thái thành công')
      fetchStudentFactories()
    })
    .catch((error) => {
      message.error(error.response?.data?.message || 'Lỗi khi đổi trạng thái sinh viên')
    })
    .finally(() => {
      loadingStore.hide()
    })
}

const configImportExcel = {
  fetchUrl: API_ROUTES_EXCEL.FETCH_IMPORT_STUDENT_FACTORY,
  onSuccess: () => {
    fetchStudentFactories()
  },
  onError: () => {
    message.error('Không thể xử lý file excel')
  },
  data: { idFactory: factoryId },
  showDownloadTemplate: true,
  showHistoryLog: true,
  showExport: true,
  btnImport: 'Import sinh viên nhóm xưởng',
  btnExport: 'Export sinh viên nhóm xưởng',
}
// state mới cho detail-student modal
const detailModalVisible = ref(false)
const detailStudent = ref(null)

// gọi API lấy detail theo userStudentId
function fetchDetailStudent(userStudentId) {
  loadingStore.show()
  requestAPI
    .get(API_ROUTES_STAFF.FETCH_DATA_STUDENT_FACTORY + '/detail-student/' + userStudentId)
    .then((res) => {
      detailStudent.value = res.data.data
      detailModalVisible.value = true
    })
    .catch((error) => {
      message.error(error.response?.data?.message || 'Lấy chi tiết sinh viên thất bại')
    })
    .finally(() => loadingStore.hide())
}

/* -------------------- Quản lý modal thêm sinh viên -------------------- */
const isAddStudentModalVisible = ref(false)

// State cho modal chi tiết ca
const shiftModalVisible = ref(false)
const shiftFilter = reactive({ startDate: null, status: '' })
const shiftPagination = reactive({ current: 1, pageSize: 5, total: 0 })
const shiftData = ref([])
const shiftColumns = ref(
  autoAddColumnWidth([
    { title: 'Buổi', dataIndex: 'orderNumber', key: 'orderNumber' },
    { title: 'Ngày học', dataIndex: 'startDate', key: 'startDate' },
    { title: 'Thời gian', key: 'time' },
    { title: 'Ca', dataIndex: 'shift', key: 'shift' },
    { title: 'Trạng thái điểm danh', dataIndex: 'statusAttendance', key: 'statusAttendance' },
    { title: 'Trạng thái', dataIndex: 'status', key: 'status' },
  ]),
)

let currentStudentForShift = null

function openShiftModal(userStudentId) {
  currentStudentForShift = userStudentId
  shiftModalVisible.value = true
  shiftPagination.current = 1
  fetchShiftDetails()
}

function closeShiftModal() {
  shiftModalVisible.value = false
  shiftFilter.startDate = null
  shiftFilter.status = null
}

function fetchShiftDetails() {
  isLoading.value = true
  const params = {
    page: shiftPagination.current,
    size: shiftPagination.pageSize,
    startDate: shiftFilter.startDate ? shiftFilter.startDate.valueOf() : null,
    status: shiftFilter.status || null,
  }
  requestAPI
    .get(`${API_ROUTES_STAFF.FETCH_DATA_STUDENT_FACTORY}/detail-shift/${currentStudentForShift}`, {
      params,
    })
    .then((res) => {
      const result = res.data.data
      shiftData.value = result.data
      shiftPagination.total = result.totalRecords || result.totalPages * shiftPagination.pageSize
    })
    .catch((error) => {
      message.error(error.response?.data?.message || 'Lỗi khi lấy chi tiết ca')
    })
    .finally(() => {
      isLoading.value = false
    })
}

function handleShiftTableChange(paginationObj) {
  shiftPagination.current = paginationObj.current
  shiftPagination.pageSize = paginationObj.pageSize
  fetchShiftDetails()
}

watch(isAddStudentModalVisible, (newVal) => {
  if (newVal) {
    studentFilter.searchQuery = ''
    studentFilter.page = 1
    studentPagination.current = 1
    // Cập nhật cả danh sách sinh viên tổng và danh sách đã có trong nhóm
    fetchExistingStudents()
    fetchAllStudents()
  }
})

const handleClearFilter = () => {
  Object.assign(filter, {
    searchQuery: '',
    status: null,
    page: 1,
    pageSize: 5,
  })
  pagination.current = 1
  fetchStudentFactories()
}

onMounted(() => {
  breadcrumbStore.setRoutes(breadcrumb.value)
  fetchStudentFactories()
  fetchExistingStudents()
  fetchAllStudents()
})
</script>

<template>
  <div class="container-fluid">
    <!-- Bảng danh sách sinh viên trong nhóm xưởng -->
    <div class="row g-3">
      <div class="col-12">
        <a-card :bordered="false" class="cart no-body-padding">
          <a-collapse ghost>
            <a-collapse-panel>
              <template #header><FilterFilled /> Bộ lọc ({{ countFilter }})</template>
              <div class="row g-3 filter-container">
                <div class="col-md-6 col-sm-12">
                  <div class="label-title">Từ khoá:</div>
                  <a-input
                    v-model:value="filter.searchQuery"
                    placeholder="Tìm theo mã, tên hoặc email sinh viên"
                    allowClear
                    @change="fetchStudentFactories"
                  >
                    <template #prefix>
                      <SearchOutlined />
                    </template>
                  </a-input>
                </div>
                <div class="col-md-6 col-sm-12">
                  <div class="label-title">Trạng thái:</div>
                  <a-select
                    v-model:value="filter.status"
                    placeholder="-- Tất cả trạng thái --"
                    class="w-100"
                    @change="fetchStudentFactories"
                  >
                    <a-select-option :value="null">-- Tất cả trạng thái --</a-select-option>
                    <a-select-option value="1">Đang học</a-select-option>
                    <a-select-option value="0">Ngưng học</a-select-option>
                  </a-select>
                </div>

                <div class="col-12">
                  <div class="d-flex justify-content-center flex-wrap gap-2">
                    <a-button class="btn-light" @click="fetchStudentFactories">
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
          <template #title> <UnorderedListOutlined /> Danh sách sinh viên </template>
          <div class="d-flex justify-content-end flex-wrap gap-3 mb-2">
            <ExcelUploadButton v-bind="configImportExcel" />
            <a-button type="primary" @click="isAddStudentModalVisible = true">
              <PlusOutlined /> Thêm sinh viên
            </a-button>
          </div>

          <a-table
            class="nowrap"
            :dataSource="studentFactories"
            :columns="columns"
            rowKey="studentFactoryId"
            :pagination="pagination"
            :scroll="{ x: 'auto' }"
            :loading="isLoading"
            @change="handleTableChange"
          >
            <template #bodyCell="{ column, record, index }">
              <template v-if="column.dataIndex">
                <template v-if="column.dataIndex === 'rowNumber'">
                  {{ (pagination.current - 1) * pagination.pageSize + index + 1 }}
                </template>
                <template v-else-if="column.dataIndex === 'statusStudentFactory'">
                  <span class="nowrap">
                    <a-switch
                      class="me-2"
                      :checked="
                        record.statusStudentFactory === 'ACTIVE' ||
                        record.statusStudentFactory === 1
                      "
                      @change="confirmChangeStatus(record)"
                    />
                    <a-tag
                      :color="
                        record.statusStudentFactory === 'ACTIVE' ||
                        record.statusStudentFactory === 1
                          ? 'green'
                          : 'red'
                      "
                    >
                      {{
                        record.statusStudentFactory === 'ACTIVE' ||
                        record.statusStudentFactory === 1
                          ? 'Đang học'
                          : 'Ngưng học'
                      }}
                    </a-tag>
                  </span>
                </template>
                <template v-else-if="column.dataIndex === 'totalAbsentShift'">
                  <a-tag :color="record.totalAbsentShift > 0 ? 'red' : 'green'"
                    >{{
                      record.totalAbsentShift > 0
                        ? Math.min(
                            record.totalAbsentShift + 0.5 * record.currentLateAttendance,
                            record.totalShift,
                          )
                        : 0
                    }}
                    / {{ record.totalShift || 0 }}</a-tag
                  >
                </template>
                <template v-else-if="column.dataIndex === 'percenAbsentShift'">
                  <a-tag
                    :color="
                      record.totalAbsentShift > 0 && record.totalShift > 0 ? 'orange' : 'green'
                    "
                    >{{
                      (
                        record.totalShift && (record.totalAbsentShift / record.totalShift) * 100
                      ).toFixed(1) || 0
                    }}%</a-tag
                  >
                </template>
              </template>
              <template v-else-if="column.key === 'action'">
                <a-tooltip title="Xem chi tiết sinh viên">
                  <a-button
                    type="text"
                    @click="fetchDetailStudent(record.studentId)"
                    class="btn btn-outline-primary me-2"
                  >
                    <EyeFilled />
                  </a-button>
                </a-tooltip>
                <a-tooltip title="Xoá sinh viên ra khỏi nhóm xưởng">
                  <a-button
                    type="text"
                    @click="confirmDelete(record)"
                    class="btn btn-outline-danger"
                  >
                    <DeleteFilled />
                  </a-button>
                </a-tooltip>
              </template>
            </template>
          </a-table>
        </a-card>
      </div>
    </div>
    <!-- Modal Chi tiết sinh viên -->
    <a-modal
      v-model:open="detailModalVisible"
      title="Chi tiết sinh viên"
      :footer="null"
      width="600px"
      @cancel="detailModalVisible = false"
    >
      <a-descriptions bordered :column="1">
        <a-descriptions-item label="Mã sinh viên">
          {{ detailStudent.userStudentCode }}
        </a-descriptions-item>
        <a-descriptions-item label="Tên sinh viên">
          {{ detailStudent.userStudentName }}
        </a-descriptions-item>
        <a-descriptions-item label="Trạng thái">
          {{ detailStudent.userStudentStatus === 1 ? 'Đang học' : 'Ngưng học' }}
        </a-descriptions-item>
        <a-descriptions-item label="Ca đang hoạt động">
          <a @click="openShiftModal(detailStudent.id)">Chi tiết</a>
        </a-descriptions-item>
        <a-descriptions-item label="Học kỳ">
          {{ detailStudent.semesterCode }}
          ( {{ formatDate(detailStudent.startDate) }} -
          {{ formatDate(detailStudent.endDate) }}
          )
        </a-descriptions-item>
      </a-descriptions>
    </a-modal>

    <a-modal
      v-model:open="shiftModalVisible"
      title="Chi tiết ca"
      :footer="null"
      width="80%"
      @cancel="closeShiftModal"
    >
      <div class="row g-3 filter-container mb-3">
        <div class="col-md-6">
          <a-date-picker
            class="w-100"
            placeholder="Ngày học"
            v-model:value="shiftFilter.startDate"
            format="YYYY-MM-DD"
            @change="fetchShiftDetails"
          />
        </div>
        <div class="col-md-6">
          <a-select
            v-model:value="shiftFilter.status"
            placeholder="-- Tất cả trạng thái --"
            class="w-100"
            @change="fetchShiftDetails"
          >
            <a-select-option :value="''">-- Tất cả trạng thái --</a-select-option>
            <a-select-option value="DA_DIEN_RA">Đã diễn ra</a-select-option>
            <a-select-option value="CHUA_DIEN_RA">Chưa diễn ra</a-select-option>
          </a-select>
        </div>
      </div>

      <a-table
        class="nowrap"
        :dataSource="shiftData"
        :columns="shiftColumns"
        rowKey="id"
        :pagination="shiftPagination"
        :loading="isLoading"
        @change="handleShiftTableChange"
        :scroll="{ x: 'auto' }"
      >
        <template #bodyCell="{ column, record, index }">
          <template v-if="column.dataIndex === 'orderNumber'">
            {{ (shiftPagination.current - 1) * shiftPagination.pageSize + index + 1 }}
          </template>
          <template v-else-if="column.dataIndex === 'startDate'">
            {{
              `${dayOfWeek(record.startDate)}, ${formatDate(record.startDate, DEFAULT_DATE_FORMAT)}`
            }}
          </template>
          <template v-else-if="column.key === 'time'">
            {{
              `${formatDate(record.startDate, 'HH:mm')} - ${formatDate(record.endDate, 'HH:mm')}`
            }}
          </template>
          <template v-else-if="column.dataIndex === 'shift'">
            <a-tag :color="record.type === 1 ? 'blue' : 'purple'">
              Ca
              {{
                record.shift
                  .split(',')
                  .map((o) => +o)
                  .join(', ')
              }}
              - {{ TYPE_SHIFT[record.type] }}
            </a-tag>
          </template>
          <template v-else-if="column.dataIndex === 'statusAttendance'">
            <a-tag
              :color="
                record.statusAttendance === 3
                  ? 'success'
                  : record.statusAttendance === null
                    ? null
                    : 'error'
              "
            >
              {{
                record.statusAttendance === 3
                  ? 'Có mặt'
                  : record.statusAttendance === null
                    ? ''
                    : 'Vắng mặt'
              }}
            </a-tag>
          </template>
          <template v-else-if="column.dataIndex === 'status'">
            <a-badge
              :status="
                record.status === 'DA_DIEN_RA'
                  ? 'error'
                  : record.status === 'DANG_DIEN_RA'
                    ? 'processing'
                    : 'success'
              "
            />
            {{ STATUS_PLAN_DATE_DETAIL[record.status] }}
          </template>
        </template>
      </a-table>
    </a-modal>

    <!-- Modal "Thêm học sinh vào nhóm xưởng" -->
    <a-modal
      v-model:open="isAddStudentModalVisible"
      title="Thêm học sinh vào nhóm xưởng"
      width="80%"
      @cancel="resetStudentModal"
      @ok="handleAddStudents"
      :okButtonProps="{ loading: isLoading }"
      :footer="null"
    >
      <div class="row g-3 filter-container" style="margin-bottom: 16px">
        <div class="col-21">
          <a-input
            v-model:value="studentFilter.searchQuery"
            placeholder="Mã, tên hoặc email sinh viên"
            allowClear
            @change="fetchAllStudents"
          />
        </div>
      </div>
      <!-- Bảng danh sách tất cả sinh viên -->
      <a-table
        class="nowrap"
        :key="isAddStudentModalVisible"
        :dataSource="allStudents"
        :columns="studentColumns"
        rowKey="id"
        bordered
        :pagination="studentPagination"
        @change="handleStudentTableChange"
        :loading="isLoading"
        :scroll="{ x: 'auto' }"
      >
        <template #bodyCell="{ column, record, index }">
          <template v-if="column.dataIndex">
            <template v-if="column.dataIndex === 'rowNumber'">
              {{ (studentPagination.current - 1) * studentPagination.pageSize + index + 1 }}
            </template>
            <template v-else>
              {{ record[column.dataIndex] }}
            </template>
          </template>
          <template v-else-if="column.key === 'select'">
            <a-checkbox
              :checked="
                selectedStudents[record.id] !== undefined
                  ? selectedStudents[record.id]
                  : record.checked
              "
              :disabled="record.checked"
              @change="(e) => handleStudentCheckboxChange(record, e.target.checked)"
            />
          </template>
        </template>
      </a-table>

      <!-- Custom footer -->
      <div style="text-align: right; margin-top: 16px">
        <a-button @click="resetStudentModal">Đóng</a-button>
      </div>
    </a-modal>
  </div>
</template>
