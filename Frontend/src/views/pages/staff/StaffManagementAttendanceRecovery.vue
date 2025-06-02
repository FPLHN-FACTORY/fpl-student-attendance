<script setup>
import useBreadcrumbStore from '@/stores/useBreadCrumbStore';
import useLoadingStore from '@/stores/useLoadingStore';
import { ref, reactive, onMounted } from 'vue';
import { DEFAULT_PAGINATION, DEFAULT_DATE_FORMAT } from '@/constants';
import { autoAddColumnWidth, formatDate } from '@/utils/utils';
import { API_ROUTES_EXCEL, GLOBAL_ROUTE_NAMES } from '@/constants/routesConstant'
import { ROUTE_NAMES } from '@/router/staffRoute';
import requestAPI from '@/services/requestApiService';
import { API_ROUTES_STAFF } from '@/constants/staffConstant';
import { message, Modal } from 'ant-design-vue';
import { EditFilled, FilterFilled, PlusOutlined, SearchOutlined, UnorderedListOutlined } from '@ant-design/icons-vue';
import dayjs from 'dayjs'
import ExcelUploadButton from '@/components/excel/ExcelUploadButton.vue';

const breadcrumbStore = useBreadcrumbStore()
const loadingStore = useLoadingStore()
const isLoading = ref(false)

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
    { title: '#', dataIndex: 'rowNumber', key: 'rowNumber' },
    { title: 'Tên hoạt động', dataIndex: 'name', key: 'name' },
    { title: 'Mô tả', dataIndex: 'description', key: 'description' },
    { title: 'Ngày', dataIndex: 'dayHappen', key: 'dayHappen' },
    { title: 'Tổng sinh viên', dataIndex: 'student', key: 'student' },
    {
      title: 'Chức năng',
      key: 'action',
      fixed: 'right',
      width: 100
    }
  ])
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
  pagination.current = 1
  fetchAttendanceRecovery()
}

const handleClearFilter = () => {
  Object.keys(filter).forEach((key) => {
    filter[key] = ''
  })
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
  if (!newEvent.name || !newEvent.dayHappen) {
    message.error('Vui lòng nhập đầy đủ thông tin')
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
      requestAPI.post(API_ROUTES_STAFF.FETCH_DATA_ATTENDANCE_RECOVERY, payload)
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
      message.error(error.response?.data?.message || 'Lỗi khi lấy chi tiết sự kiện khôi phục điểm danh')
    })
    .finally(() => {
      loadingStore.hide()
    })
}

const handleEditEvent = () => {
  if(!editEvent.name || !editEvent.day) {
    message.error('Vui lòng nhập đầy đủ thông tin')
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
      requestAPI.put(API_ROUTES_STAFF.FETCH_DATA_ATTENDANCE_RECOVERY + '/' + editEvent.id, payload)
        .then(() => {
          message.success('Cập nhật sự kiện khôi phục điểm danh thành công')
          clearData()
          fetchAttendanceRecovery()
        })
        .catch((error) => {
          message.error(error.response?.data?.message || 'Lỗi khi cập nhật sự kiện khôi phục điểm danh')
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

const configImportExcel = {
  fetchUrl: API_ROUTES_EXCEL.FETCH_IMPORT_ATTENDANCE_RECOVERY,
  onSuccess: () => {
    fetchAttendanceRecovery()
  },
  onError: () => {
    message.error('Không thể xử lý file excel')
  },
  showDownloadTemplate: true,
  showHistoryLog: true,
}

onMounted(() => {
  breadcrumbStore.setRoutes(breadcrumb.value)
  fetchAttendanceRecovery()
  fetchSemester()
})
</script>
<template>
  <!-- Danh sách sự kiện khôi phục điểm danh -->
  <div class="container-fluid">
    <div class="row g-3">
      <div class="col-12">
        <a-card :bordered="false" class="cart">
          <template #title>
            <UnorderedListOutlined /> Danh sách sự kiện khôi phục điểm danh
          </template>

          <div class="row g-2">
            <div class="col-md-6 col-sm-12">
              <div class="label-title">Từ khoá:</div>
              <a-input
                v-model:value="filter.searchQuery"
                placeholder="Tìm theo tên hoặc mô tả"
                allowClear
                @change="fetchAttendanceRecovery"
                class="w-100"
              >
                <template #suffix> <SearchOutlined /> </template>
              </a-input>
            </div>
            <div class="col-md-6 col-sm-12">
              <div class="label-title">Khoảng ngày:</div>
              <a-range-picker
                v-model:value="filter.dateRange"
                class="w-100"
                :format="DEFAULT_DATE_FORMAT"
                @change="handleDateRangeChange"
              />
            </div>
            <div class="col-md-2 col-sm-12">
              <div class="d-flex justify-content-end mb-3 flex-wrap gap-3">
                <a-button class="btn-light" @click="fetchAttendanceRecovery">
                  <FilterFilled /> Lọc
                </a-button>
                <a-button class="btn-gray" @click="handleClearFilter"> Huỷ lọc </a-button>
                <a-tooltip title="Thêm Sự kiện khôi phục điểm danh">
                  <a-button type="primary" @click="handleShowModalAdd">
                    <PlusOutlined /> Thêm mới
                  </a-button>
                </a-tooltip>
              </div>
            </div>
          </div>

          <div class="col-12">
            <a-card :bordered="false" class="cart">
              <template #title> <UnorderedListOutlined /> Danh sách sự kiện khôi phục điểm danh </template>
              <div class="d-flex justify-content-end mb-3 flex-wrap gap-3">
                <a-space>
                  <a-tooltip>
                    <template #title>Thêm Sự kiện khôi phục điểm danh</template>
                    <a-button type="primary" @click="handleShowModalAdd">
                      <PlusOutlined /> Thêm
                    </a-button>
                  </a-tooltip>
                </a-space>
              </div>
              <a-table
                class="nowrap"
                rowKey="id"
                :dataSource="attendanceRecovery"
                :columns="columns"
                :pagination="pagination"
                @change="handleTableChange"
                :loading="isLoading"
                :scroll="{ x: 'auto' }"
              >
                <template #bodyCell="{ column, record, index }">
                  <template v-if="column.dataIndex">
                    <template v-if="column.dataIndex === 'rowNumber'">
                      {{ (pagination.current - 1) * pagination.pageSize + index + 1 }}
                    </template>
                    <template v-else-if="column.dataIndex === 'dayHappen'">
                      {{ formatDate(record.dayHappen, DEFAULT_DATE_FORMAT) }}
                    </template>
                    <template v-else>
                      {{ record[column.dataIndex] }}
                    </template>
                  </template>
                  <template v-else-if="column.key === 'action'">
                    <div class="d-flex flex-wrap gap-2">
                      <a-tooltip>
                        <template #title>Sửa thông tin khôi phục điểm danh</template>
                        <a-button type="text" class="btn-outline-info" @click="handleShowModalEdit(record)">
                          <EditFilled />
                        </a-button>
                      </a-tooltip>
                      <div class="excel-upload-wrapper">
                        <ExcelUploadButton v-bind="configImportExcel" />
                      </div>
                    </div>
                  </template>
                </template>
              </a-table>
            </a-card>
          </div>
        </a-card>
      </div>
    </div>
  </div>

  <!-- Modal Thêm sự kiện khôi phục điểm danh -->
  <a-modal
    v-model:open="modalAddEvent"
    title="Thêm sự kiện khôi phục điểm danh"
    :okButtonProps="{ loading: modalAddLoading }"
    @ok="handleAddEvent"
    @cancel="clearData"
    @close="clearData"
  >
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

  <!-- Modal Sửa sự kiện khôi phục điểm danh -->
  <a-modal v-model:open="modalEditEvent"
  title="Sửa sự kiện khôi phục điểm danh"
  :okButtonProps="{ loading: modalUpdateLoading }"
  @ok="handleEditEvent"
  @cancel="clearData"
  @close="clearData">

    <a-form :model="editEvent" layout="vertical">
      <a-form-item label="Tên sự kiện" required>
        <a-input v-model:value="editEvent.name" placeholder="Nhập tên sự kiện" />
      </a-form-item>
      <a-form-item label="Mô tả">
        <a-textarea v-model:value="editEvent.description" placeholder="Nhập mô tả" />
      </a-form-item>
      <a-form-item label="Ngày" required>
        <a-date-picker v-model:value="editEvent.day" placeholder="Chọn ngày" :format="DEFAULT_DATE_FORMAT" class="w-100" />
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<style scoped>
.excel-upload-wrapper {
  display: inline-flex;
  gap: 8px;
}

/* Đảm bảo các nút trong ExcelUploadButton không bị dính */
.excel-upload-wrapper :deep(.ant-space) {
  gap: 8px !important;
}

.excel-upload-wrapper :deep(.ant-btn) {
  margin: 0 !important;
}
</style>

