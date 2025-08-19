<script setup>
import useBreadcrumbStore from '@/stores/useBreadCrumbStore'
import useLoadingStore from '@/stores/useLoadingStore'
import { ref, reactive, onMounted } from 'vue'
import { DEFAULT_PAGINATION, DEFAULT_DATE_FORMAT } from '@/constants'
import { autoAddColumnWidth, formatDate } from '@/utils/utils'
import { validateFormSubmission } from '@/utils/validationUtils'
import { API_ROUTES_EXCEL, GLOBAL_ROUTE_NAMES } from '@/constants/routesConstant'
import { ROUTE_NAMES } from '@/router/staffRoute'
import requestAPI from '@/services/requestApiService'
import { API_ROUTES_STAFF } from '@/constants/staffConstant'
import { message, Modal } from 'ant-design-vue'
import {
  EditFilled,
  FilterFilled,
  HistoryOutlined,
  InfoCircleFilled,
  PlusOutlined,
  SearchOutlined,
  UnorderedListOutlined,
} from '@ant-design/icons-vue'
import dayjs from 'dayjs'
import ExcelUploadButton from '@/components/excel/ExcelUploadButton.vue'

const breadcrumbStore = useBreadcrumbStore()
const loadingStore = useLoadingStore()
const isLoading = ref(false)

const countFilter = ref(0)

const breadcrumb = ref([
  {
    name: GLOBAL_ROUTE_NAMES.STAFF_PAGE,
    breadcrumbName: 'Phụ trách xưởng',
  },
  {
    name: ROUTE_NAMES.MANAGEMENT_ATTENDANCE_RECOVERY,
    breadcrumbName: 'Khôi phục điểm danh',
  },
])

const filter = reactive({
  searchQuery: '',
  fromDate: null,
  toDate: null,
  dateRange: null,
  semesterId: null,
})

const pagination = reactive({
  ...DEFAULT_PAGINATION,
})

const columns = ref(
  autoAddColumnWidth([
    { title: '#', key: 'rowNumber' },
    { title: 'Tên hoạt động', dataIndex: 'name', key: 'name' },
    { title: 'Mô tả', dataIndex: 'description', key: 'description' },
    { title: 'Ngày', dataIndex: 'dayHappen', key: 'dayHappen' },
    { title: 'Tổng sinh viên', dataIndex: 'totalStudent', key: 'totalStudent' },
    {
      title: 'Chức năng',
      key: 'action',
      width: 300,
    },
  ]),
)

const attendanceRecovery = ref([])
const fetchAttendanceRecovery = () => {
  loadingStore.show()
  requestAPI
    .get(API_ROUTES_STAFF.FETCH_DATA_ATTENDANCE_RECOVERY, {
      params: {
        searchQuery: filter.searchQuery,
        fromDate: filter.fromDate,
        toDate: filter.toDate,
        // semesterId: filter.semesterId,
        page: pagination.current,
        size: pagination.pageSize,
      },
    })
    .then((response) => {
      const result = response.data.data
      attendanceRecovery.value = result.data
      if (result.totalRecords !== undefined) {
        pagination.total = result.totalRecords
      } else {
        pagination.total = result.totalPages * pagination.pageSize
      }
      countFilter.value = result.totalItems
    })
    .catch((error) => {
      message.error(error.response?.data?.message || 'Lỗi khi lấy danh sách khôi phục điểm danh')
    })
    .finally(() => {
      loadingStore.hide()
    })
}

const handleTableChange = (pageInfo) => {
  pagination.current = pageInfo.current
  pagination.pageSize = pageInfo.pageSize
  fetchAttendanceRecovery()
}

const semester = ref([])
const fetchSemester = () => {
  loadingStore.show()
  requestAPI
    .get(API_ROUTES_STAFF.FETCH_DATA_ATTENDANCE_RECOVERY + '/semesters')
    .then((response) => {
      semester.value = response.data.data
    })
    .catch((error) => {
      message.error(error.response?.data?.message || 'Lỗi khi lấy danh sách kỳ học')
    })
    .finally(() => {
      loadingStore.hide()
    })
}

const handleDateRangeChange = (range) => {
  if (range && range.length === 2) {
    filter.dateRange = range
    filter.fromDate = range[0].valueOf()
    filter.toDate = range[1].valueOf()
  } else {
    filter.dateRange = null
    filter.fromDate = null
    filter.toDate = null
  }
  handleSubmitFilter()
}

const handleClearFilter = () => {
  Object.keys(filter).forEach((key) => {
    filter[key] = ''
  })
  fetchAttendanceRecovery()
}

const handleSubmitFilter = () => {
  pagination.current = 1
  fetchAttendanceRecovery()
}

const modalAddEvent = ref(false)
const modalAddLoading = ref(false)
const newEvent = reactive({
  name: '',
  description: '',
  dayHappen: dayjs(),
  semesterId: null,
})
const handleShowModalAdd = () => {
  newEvent.name = ''
  newEvent.description = ''
  newEvent.dayHappen = dayjs()
  newEvent.semesterId = null
  modalAddEvent.value = true
}
const handleAddEvent = () => {
  // Validate required fields with whitespace check
  const validation = validateFormSubmission(newEvent, [{ key: 'name', label: 'Tên sự kiện' }])

  if (!validation.isValid) {
    message.error(validation.message)
    return
  }

  if (!newEvent.dayHappen) {
    message.error('Vui lòng chọn ngày diễn ra')
    return
  }
  Modal.confirm({
    title: 'Xác nhận thêm mới',
    content: 'Bạn có chắc chắn muốn thêm sự kiện khôi phục điểm danh mới này?',
    okText: 'Tiếp tục',
    cancelText: 'Hủy bỏ',
    onOk() {
      modalAddLoading.value = true
      loadingStore.show()
      const payload = {
        name: newEvent.name,
        description: newEvent.description,
        day: newEvent.dayHappen.valueOf(),
      }
      requestAPI
        .post(API_ROUTES_STAFF.FETCH_DATA_ATTENDANCE_RECOVERY, payload)
        .then(() => {
          message.success('Thêm sự kiện khôi phục điểm danh thành công')
          clearData()
          fetchAttendanceRecovery()
        })
        .catch((error) => {
          message.error(error.response?.data?.message || 'Lỗi khi thêm sự kiện khôi phục điểm danh')
        })
        .finally(() => {
          modalAddLoading.value = false
          loadingStore.hide()
        })
    },
  })
}

const modalEditEvent = ref(false)
const modalUpdateLoading = ref(false)
const editEvent = reactive({
  id: null,
  name: '',
  description: '',
  day: null,
})

const handleShowModalEdit = (record) => {
  loadingStore.show()
  requestAPI
    .get(API_ROUTES_STAFF.FETCH_DATA_ATTENDANCE_RECOVERY + '/detail/' + record.id)
    .then((response) => {
      const data = response.data.data
      editEvent.id = data.id
      editEvent.name = data.name
      editEvent.description = data.description
      editEvent.day = dayjs(data.day)
      modalEditEvent.value = true
    })
    .catch((error) => {
      message.error(
        error.response?.data?.message || 'Lỗi khi lấy chi tiết sự kiện khôi phục điểm danh',
      )
    })
    .finally(() => {
      loadingStore.hide()
    })
}

const handleEditEvent = () => {
  // Validate required fields with whitespace check
  const validation = validateFormSubmission(editEvent, [{ key: 'name', label: 'Tên sự kiện' }])

  if (!validation.isValid) {
    message.error(validation.message)
    return
  }

  if (!editEvent.day) {
    message.error('Vui lòng chọn ngày diễn ra')
    return
  }
  Modal.confirm({
    title: 'Xác nhận cập nhật',
    content: 'Bạn có chắc chắn muốn cập nhật thông tin sự kiện khôi phục điểm danh này?',
    okText: 'Tiếp tục',
    cancelText: 'Hủy bỏ',
    onOk() {
      modalUpdateLoading.value = true
      loadingStore.show()
      const payload = {
        id: editEvent.id,
        name: editEvent.name,
        description: editEvent.description,
        day: editEvent.day.valueOf(),
      }
      requestAPI
        .put(API_ROUTES_STAFF.FETCH_DATA_ATTENDANCE_RECOVERY + '/' + editEvent.id, payload)
        .then(() => {
          message.success('Cập nhật sự kiện khôi phục điểm danh thành công')
          clearData()
          fetchAttendanceRecovery()
        })
        .catch((error) => {
          message.error(
            error.response?.data?.message || 'Lỗi khi cập nhật sự kiện khôi phục điểm danh',
          )
        })
        .finally(() => {
          modalUpdateLoading.value = false
          loadingStore.hide()
        })
    },
  })
}

const clearData = () => {
  newEvent.name = ''
  newEvent.description = ''
  newEvent.dayHappen = null
  newEvent.semesterId = null
  modalAddEvent.value = false

  editEvent.id = null
  editEvent.name = ''
  editEvent.description = ''
  editEvent.day = null
  modalEditEvent.value = false
}

const deleteOldAttendanceData = (attendanceRecoveryId) => {
  return new Promise((resolve, reject) => {
    loadingStore.show()
    requestAPI
      .delete(
        API_ROUTES_STAFF.FETCH_DATA_ATTENDANCE_RECOVERY +
          '/delete-attendance/' +
          attendanceRecoveryId,
      )
      .then(() => {
        message.success('Đã xóa dữ liệu khôi phục điểm danh cũ')
        resolve(true)
      })
      .catch((error) => {
        message.error(error.response?.data?.message || 'Lỗi khi xóa dữ liệu khôi phục điểm danh cũ')
        reject(error)
      })
      .finally(() => {
        loadingStore.hide()
      })
  })
}

const checkAttendanceRecoveryHistory = (data) => {
  return new Promise((resolve, reject) => {
    try {
      // Find the attendance recovery record by ID
      const attendanceRecord = attendanceRecovery.value.find(
        (record) => record.id === data.attendanceRecoveryId,
      )

      const hasHistory = attendanceRecord && attendanceRecord.idImportLog != null

      if (hasHistory) {
        Modal.confirm({
          title: 'Cảnh báo',
          content:
            'Sự kiện này đã được khôi phục điểm danh cho sinh viên rồi. Nếu import tiếp, những sinh viên cũ sẽ bị xóa. Bạn có muốn tiếp tục?',
          okText: 'Tiếp tục',
          cancelText: 'Hủy bỏ',
          okType: 'danger',
          async onOk() {
            try {
              // Xóa dữ liệu cũ trước khi import mới
              await deleteOldAttendanceData(data.attendanceRecoveryId)
              resolve(true)
            } catch (error) {
              resolve(false)
            }
          },
          onCancel() {
            resolve(false)
          },
        })
      } else {
        resolve(true)
      }
    } catch (error) {
      reject(error)
    }
  })
}

const configImportExcel = {
  fetchUrl: API_ROUTES_EXCEL.FETCH_IMPORT_ATTENDANCE_RECOVERY,
  onBeforeImport: async (data) => {
    try {
      const shouldContinue = await checkAttendanceRecoveryHistory(data)
      return shouldContinue
    } catch (error) {
      return false
    }
  },
  onSuccess: () => {
    fetchAttendanceRecovery()
  },
  onError: () => {
    message.error('Không thể xử lý file excel')
  },
  showDownloadTemplate: false,
  showHistoryLog: false,
}

const columnsImportLog = ref(
  autoAddColumnWidth([
    { title: 'Thời gian', dataIndex: 'createdAt', key: 'createdAt' },
    { title: 'Tệp tin', dataIndex: 'fileName', key: 'fileName' },
    {
      title: 'Thành công',
      dataIndex: 'totalSuccess',
      key: 'totalSuccess',
    },
    { title: 'Lỗi', dataIndex: 'totalError', key: 'totalError' },
    { title: '', key: 'actions' },
  ]),
)

const importHistory = ref([])
const isShowHistoryLog = ref(false)

const handleShowImportHistory = (idImportLog) => {
  loadingStore.show()
  requestAPI
    .get(API_ROUTES_STAFF.FETCH_DATA_ATTENDANCE_RECOVERY + '/history-log/' + idImportLog)
    .then((response) => {
      importHistory.value = response.data.data.data
      isShowHistoryLog.value = true
    })
    .catch((error) => {
      message.error(error.response?.data?.message || 'Lỗi khi lấy lịch sử import excel')
    })
    .finally(() => {
      loadingStore.hide()
    })
}

const columnsDetail = ref(
  autoAddColumnWidth([
    { title: 'Dòng', dataIndex: 'line', key: 'line' },
    { title: 'Trạng thái', dataIndex: 'status', key: 'status' },
    { title: 'Nội dung', dataIndex: 'message', key: 'message' },
  ]),
)
const importHistoryDetailLog = ref([])
const isShowHistoryLogDetail = ref(false)

const handleShowImportLogDetail = (id) => {
  loadingStore.show()
  requestAPI
    .get(API_ROUTES_STAFF.FETCH_DATA_ATTENDANCE_RECOVERY + '/detail-history-log/' + id)
    .then((response) => {
      importHistoryDetailLog.value = response.data.data
      isShowHistoryLogDetail.value = true
    })
    .catch((error) => {
      message.error(error.response?.data?.message || 'Lỗi khi lấy lịch sử chi tiết import excel')
    })
    .finally(() => {
      loadingStore.hide()
    })
}

const configImportExcelTemplate = {
  fetchUrl: API_ROUTES_EXCEL.FETCH_IMPORT_ATTENDANCE_RECOVERY,
  onBeforeImport: null,
  onSuccess: null,
  onError: null,
  showDownloadTemplate: true,
  showHistoryLog: false,
  showImport: false,
}
onMounted(() => {
  breadcrumbStore.setRoutes(breadcrumb.value)
  fetchAttendanceRecovery()
  fetchSemester()
})
</script>
<template>
  <div class="container-fluid">
    <div class="row g-3">
      <div class="col-12">
        <a-card :bordered="false" class="cart no-body-padding">
          <a-collapse ghost>
            <a-collapse-panel>
              <template #header><FilterFilled /> Bộ lọc ({{ countFilter }})</template>
              <div class="row g-3">
                <div class="col-lg-6 col-md-6 col-sm-12">
                  <div class="label-title">Từ khoá:</div>
                  <a-input
                    v-model:value="filter.searchQuery"
                    placeholder="Tìm theo tên hoặc mô tả"
                    allowClear
                    class="filter-input w-100"
                    @change="handleSubmitFilter"
                  >
                    <template #prefix>
                      <SearchOutlined />
                    </template>
                  </a-input>
                </div>
                <div class="col-lg-6 col-md-6 col-sm-12">
                  <div class="label-title">Khoảng ngày:</div>
                  <a-range-picker
                    v-model:value="filter.dateRange"
                    class="filter-input w-100"
                    :format="DEFAULT_DATE_FORMAT"
                    @change="handleDateRangeChange"
                  />
                </div>
                <div class="col-12">
                  <div class="d-flex justify-content-center flex-wrap gap-2">
                    <a-button class="btn-light" @click="handleSubmitFilter">
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
          <template #title>
            <UnorderedListOutlined /> Danh sách sự kiện khôi phục điểm danh
          </template>
          <div class="d-flex justify-content-end mb-3 flex-wrap gap-3">
            <ExcelUploadButton v-bind="configImportExcelTemplate" />
            <a-space>
              <a-button type="primary" @click="handleShowModalAdd">
                <PlusOutlined /> Thêm sự kiện
              </a-button>
            </a-space>
          </div>
          <a-table
            class="nowrap"
            rowKey="id"
            :dataSource="attendanceRecovery"
            :columns="columns"
            :pagination="pagination"
            @change="handleTableChange"
            :loading="loadingStore.isLoading"
            :scroll="{ x: 'auto' }"
          >
            <template #bodyCell="{ column, record, index }">
              <template v-if="column.key === 'rowNumber'">
                {{ (pagination.current - 1) * pagination.pageSize + index + 1 }}
              </template>
              <template v-else-if="column.dataIndex === 'dayHappen'">
                {{ formatDate(record.dayHappen, DEFAULT_DATE_FORMAT) }}
              </template>
              <template v-else-if="column.dataIndex === 'totalStudent'">
                <a-tag v-if="record.totalStudent > 0">
                  {{ record.totalStudent + ' Sinh Viên' }}
                </a-tag>
                <a-tag v-else> Chưa có sinh viên </a-tag>
              </template>
              <template v-else-if="column.key === 'action'">
                <div class="d-flex gap-2">
                  <a-tooltip>
                    <template #title>Sửa thông tin khôi phục điểm danh</template>
                    <a-button
                      type="text"
                      class="btn-outline-info"
                      @click="handleShowModalEdit(record)"
                    >
                      <EditFilled />
                    </a-button>
                  </a-tooltip>
                  <template v-if="record.idImportLog !== null">
                    <a-tooltip>
                      <template #title>Lịch sử import</template>
                      <a-button
                        type="text"
                        class="btn-gray"
                        @click="handleShowImportHistory(record.idImportLog)"
                      >
                        <HistoryOutlined class="text-primary" />
                      </a-button>
                    </a-tooltip>
                  </template>
                  <div class="excel-upload-wrapper">
                    <ExcelUploadButton
                      v-bind="configImportExcel"
                      :data="{ attendanceRecoveryId: record.id }"
                    />
                  </div>
                </div>
              </template>
              <template v-else>
                {{ record[column.dataIndex] }}
              </template>
            </template>
          </a-table>
        </a-card>
      </div>
    </div>
  </div>

  <a-modal
    v-model:open="modalAddEvent"
    :okButtonProps="{ loading: modalAddLoading }"
    @ok="handleAddEvent"
    @cancel="clearData"
    @close="clearData"
  >
    <template #title>
      <PlusOutlined class="me-2 text-primary" />
      Thêm sự kiện khôi phục điểm danh
    </template>
    <a-form :model="newEvent" layout="vertical">
      <a-form-item label="Tên sự kiện" required>
        <a-input v-model:value="newEvent.name" placeholder="Nhập tên sự kiện" />
      </a-form-item>
      <a-form-item label="Mô tả">
        <a-textarea v-model:value="newEvent.description" placeholder="Nhập mô tả" />
      </a-form-item>
      <a-form-item label="Ngày" required>
        <a-date-picker
          v-model:value="newEvent.dayHappen"
          placeholder="Chọn ngày"
          :format="DEFAULT_DATE_FORMAT"
          class="w-100"
        />
      </a-form-item>
    </a-form>
  </a-modal>

  <a-modal
    v-model:open="modalEditEvent"
    :okButtonProps="{ loading: modalUpdateLoading }"
    @ok="handleEditEvent"
    @cancel="clearData"
    @close="clearData"
  >
    <template #title>
      <EditFilled class="me-2 text-primary" />
      Sửa sự kiện khôi phục điểm danh
    </template>
    <a-form :model="editEvent" layout="vertical">
      <a-form-item label="Tên sự kiện" required>
        <a-input v-model:value="editEvent.name" placeholder="Nhập tên sự kiện" />
      </a-form-item>
      <a-form-item label="Mô tả">
        <a-textarea v-model:value="editEvent.description" placeholder="Nhập mô tả" />
      </a-form-item>
      <a-form-item label="Ngày" required>
        <a-date-picker
          v-model:value="editEvent.day"
          placeholder="Chọn ngày"
          :format="DEFAULT_DATE_FORMAT"
          class="w-100"
        />
      </a-form-item>
    </a-form>
  </a-modal>

  <a-modal v-model:open="isShowHistoryLog" :width="1000">
    <template #title><HistoryOutlined class="text-primary" /> Lịch sử import</template>
    <template #footer>
      <a-button @click="isShowHistoryLog = false" class="btn-gray">Đóng</a-button>
    </template>
    <a-table
      rowKey="id"
      class="nowrap"
      :dataSource="importHistory"
      :columns="columnsImportLog"
      :pagination="pagination"
      :scroll="{ x: 'auto' }"
      :loading="isLoading"
      @change="handleShowImportHistory"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'createdAt'">
          {{ formatDate(record.createdAt, 'dd/MM/yyyy HH:mm:ss') }}
        </template>
        <template v-if="column.dataIndex === 'status'">
          <a-tag :color="record.status == true ? 'green' : 'red'">{{
            record.status == true ? 'Thành công' : 'Thất bại'
          }}</a-tag>
        </template>
        <template v-if="column.dataIndex === 'totalSuccess'">
          <a-tag :color="record.totalSuccess > 0 ? 'green' : ''">{{ record.totalSuccess }}</a-tag>
        </template>
        <template v-if="column.dataIndex === 'totalError'">
          <a-tag :color="record.totalError > 0 ? 'red' : ''">{{ record.totalError }}</a-tag>
        </template>
        <template v-if="column.key === 'actions'">
          <a-typography-link @click="handleShowImportLogDetail(record.id)"
            >Chi tiết</a-typography-link
          >
        </template>
      </template>
    </a-table>
  </a-modal>

  <a-modal v-model:open="isShowHistoryLogDetail" :width="800">
    <template #title><InfoCircleFilled class="text-primary" /> Chi tiết import excel </template>
    <template #footer>
      <a-button @click="isShowHistoryLogDetail = false" class="btn-gray">Đóng</a-button>
    </template>
    <a-table
      rowKey="id"
      class="nowrap"
      :dataSource="importHistoryDetailLog"
      :columns="columnsDetail"
      :pagination="false"
      :scroll="{ x: 'auto' }"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'status'">
          <a-tag :color="record.status === 1 ? 'green' : 'red'">{{
            record.status === 1 ? 'Thành công' : 'Thất bại'
          }}</a-tag>
        </template>
      </template>
    </a-table>
  </a-modal>
</template>

<style scoped>
.excel-upload-wrapper {
  display: inline-flex;
  gap: 8px;
}

.excel-upload-wrapper :deep(.ant-space) {
  gap: 8px !important;
}

.excel-upload-wrapper :deep(.ant-btn) {
  margin: 0 !important;
}
</style>
