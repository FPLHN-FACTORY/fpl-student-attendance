<script setup>
import { ref, reactive, onMounted } from 'vue'
import {
  PlusOutlined,
  EditFilled,
  UnorderedListOutlined,
  FilterFilled,
  SearchOutlined,
} from '@ant-design/icons-vue'
import { message, Modal } from 'ant-design-vue'
import requestAPI from '@/services/requestApiService'
import dayjs from 'dayjs'
import { API_ROUTES_ADMIN } from '@/constants/adminConstant'
import { ROUTE_NAMES } from '@/router/adminRoute'
import { DEFAULT_PAGINATION } from '@/constants'

import { GLOBAL_ROUTE_NAMES } from '@/constants/routesConstant'
import useBreadcrumbStore from '@/stores/useBreadCrumbStore'
import useLoadingStore from '@/stores/useLoadingStore'
import { autoAddColumnWidth } from '@/utils/utils'
import { validateFormSubmission } from '@/utils/validationUtils'

const breadcrumbStore = useBreadcrumbStore()
const loadingStore = useLoadingStore()

const countFilter = ref(0)

const breadcrumb = ref([
  {
    name: GLOBAL_ROUTE_NAMES.ADMIN_PAGE,
    breadcrumbName: 'Admin',
  },
  {
    name: ROUTE_NAMES.MANAGEMENT_SEMESTER,
    breadcrumbName: 'Quản lý học kỳ',
  },
])

const semesters = ref([])
const filter = reactive({
  semesterCode: '',
  status: '',
  dateRange: null, // Giá trị RangePicker (mảng [start, end])
})

const pagination = reactive({
  ...DEFAULT_PAGINATION,
})

const isLoading = ref(false)
const modalAddLoading = ref(false)
const modalUpdateLoading = ref(false)

const modalAdd = ref(false)
const modalUpdate = ref(false)

const getSemesterByMonth = () => {
  const currentMonth = dayjs().month() + 1 // +1 vì month() trả về 0-11
  if (currentMonth >= 1 && currentMonth <= 4) {
    return 'SPRING'
  } else if (currentMonth >= 5 && currentMonth <= 8) {
    return 'SUMMER'
  } else {
    return 'FALL'
  }
}

const defaultDateRange = reactive({
  fromDate: dayjs().add(1, 'day'), // Ngày mai
  toDate: dayjs().add(4, 'month'), // 4 tháng sau
  semesterName: getSemesterByMonth(), // Tự động chọn học kỳ dựa trên tháng
})

const newSemester = reactive({
  semesterName: defaultDateRange.semesterName, // Set mặc định là học kỳ hiện tại
  fromDate: defaultDateRange.fromDate,
  toDate: defaultDateRange.toDate,
})
const detailSemester = ref({})

const columns = ref(
  autoAddColumnWidth([
    { title: '#', key: 'rowNumber' },
    { title: 'Mã học kỳ', dataIndex: 'semesterCode', key: 'semesterCode' },
    { title: 'Tên học kỳ', dataIndex: 'semesterName', key: 'semesterName' },
    { title: 'Ngày bắt đầu', dataIndex: 'startDate', key: 'startDate' },
    { title: 'Ngày kết thúc', dataIndex: 'endDate', key: 'endDate' },
    { title: 'Trạng thái', dataIndex: 'semesterStatus', key: 'semesterStatus' },
    { title: 'Chức năng', key: 'actions' },
  ]),
)

const formatEpochToDate = (epoch) => {
  if (!epoch) return ''
  return dayjs(epoch).format('DD/MM/YYYY')
}

const handleDateRangeChange = (range) => {
  if (range && range.length === 2) {
    filter.dateRange = range
    filter.fromDateSemester = range[0]
    filter.toDateSemester = range[1]
  } else {
    filter.dateRange = null
    filter.fromDateSemester = null
    filter.toDateSemester = null
  }
  pagination.current = 1
  fetchSemesters()
}

const fetchSemesters = () => {
  if (isLoading.value) return
  loadingStore.show()
  isLoading.value = true

  const { dateRange, ...filteredParams } = filter

  filteredParams.fromDateSemester = filter.fromDateSemester
    ? filter.fromDateSemester.valueOf()
    : null
  filteredParams.toDateSemester = filter.toDateSemester ? filter.toDateSemester.valueOf() : null
  filteredParams.page = pagination.current
  filteredParams.size = pagination.pageSize

  requestAPI
    .get(API_ROUTES_ADMIN.FETCH_DATA_SEMESTER, { params: filteredParams })
    .then((response) => {
      semesters.value = response.data.data.data
      if (response.data.data.totalRecords !== undefined) {
        pagination.total = response.data.data.totalRecords
      } else {
        pagination.total = response.data.data.totalPages * pagination.pageSize
      }
      countFilter.value = response.data.data.totalItems
    })
    .catch((error) => {
      message.error(
        (error.response && error.response.data && error.response.data.message) ||
          'Lỗi khi lấy dữ liệu học kỳ',
      )
    })
    .finally(() => {
      isLoading.value = false
      loadingStore.hide()
    })
}

const handleTableChange = (pageInfo) => {
  pagination.current = pageInfo.current
  pagination.pageSize = pageInfo.pageSize
  fetchSemesters()
}

const handleShowModalAdd = () => {
  newSemester.semesterName = defaultDateRange.semesterName
  newSemester.fromDate = defaultDateRange.fromDate
  newSemester.toDate = defaultDateRange.toDate
  modalAdd.value = true
}

const handleAddSemester = () => {
  // Validate required fields with whitespace check
  const validation = validateFormSubmission(newSemester, [
    { key: 'semesterName', label: 'Tên học kỳ' },
  ])

  if (!validation.isValid) {
    message.error(validation.message)
    return
  }

  if (!newSemester.fromDate || !newSemester.toDate) {
    message.error('Vui lòng chọn ngày bắt đầu và ngày kết thúc')
    return
  }
  Modal.confirm({
    title: 'Xác nhận thêm mới',
    content: 'Bạn có chắc chắn muốn thêm học kỳ mới này?',
    okText: 'Tiếp tục',
    cancelText: 'Hủy bỏ',
    onOk() {
      modalAddLoading.value = true
      loadingStore.show()
      const payload = {
        ...newSemester,
        fromDate: newSemester.fromDate.valueOf(),
        toDate: newSemester.toDate.valueOf(),
      }
      requestAPI
        .post(API_ROUTES_ADMIN.FETCH_DATA_SEMESTER, payload)
        .then(() => {
          message.success('Thêm học kỳ thành công')
          modalAdd.value = false
          fetchSemesters()
          clearFormAdd()
        })
        .catch((error) => {
          message.error(
            (error.response && error.response.data && error.response.data.message) ||
              'Lỗi khi thêm học kỳ',
          )
        })
        .finally(() => {
          modalAddLoading.value = false
          loadingStore.hide()
        })
    },
  })
}

const handleUpdateSemester = (record) => {
  if (!canEditSemester(record)) {
    message.warning('Không thể chỉnh sửa học kỳ đã kết thúc')
    return
  }

  loadingStore.show()
  requestAPI
    .get(`${API_ROUTES_ADMIN.FETCH_DATA_SEMESTER}/${record.id}`)
    .then((response) => {
      const data = response.data.data
      detailSemester.value = {
        ...data,
        fromDate: dayjs(data.fromDate),
        toDate: dayjs(data.toDate),
        semesterId: data.semesterId || data.id,
        originalFromDate: data.fromDate,
      }
      modalUpdate.value = true
    })
    .catch((error) => {
      message.error(
        (error.response && error.response.data && error.response.data.message) ||
          'Lỗi khi lấy chi tiết học kỳ',
      )
    })
    .finally(() => {
      loadingStore.hide()
    })
}

const updateSemester = () => {
  // Validate required fields with whitespace check
  const validation = validateFormSubmission(detailSemester.value, [
    { key: 'semesterName', label: 'Tên học kỳ' },
  ])

  if (!validation.isValid) {
    message.error(validation.message)
    return
  }

  if (!detailSemester.value.fromDate || !detailSemester.value.toDate) {
    message.error('Vui lòng chọn ngày bắt đầu và ngày kết thúc')
    return
  }

  const originalFromDate = dayjs(detailSemester.value.originalFromDate)
  if (
    isStartDateBeforeToday(originalFromDate) &&
    !originalFromDate.isSame(detailSemester.value.fromDate, 'day')
  ) {
    message.error('Không thể thay đổi ngày bắt đầu của học kỳ đã qua')
    return
  }

  if (detailSemester.value.toDate.isBefore(detailSemester.value.fromDate)) {
    message.error('Ngày kết thúc phải sau ngày bắt đầu')
    return
  }

  Modal.confirm({
    title: 'Xác nhận cập nhật học kỳ',
    content:
      'Lưu ý: Các lịch mà sinh viên đã được phân công theo lịch của học kỳ cũ vẫn sẽ hoạt động bình thường',
    okText: 'Cập nhật',
    cancelText: 'Hủy',
    onOk: () => {
      modalUpdateLoading.value = true
      loadingStore.show()
      const payload = {
        ...detailSemester.value,
        fromDate: detailSemester.value.fromDate.valueOf(),
        toDate: detailSemester.value.toDate.valueOf(),
      }
      requestAPI
        .put(API_ROUTES_ADMIN.FETCH_DATA_SEMESTER, payload)
        .then(() => {
          message.success('Cập nhật học kỳ thành công')
          modalUpdate.value = false
          fetchSemesters()
        })
        .catch((error) => {
          message.error(
            (error.response && error.response.data && error.response.data.message) ||
              'Lỗi khi cập nhật học kỳ',
          )
        })
        .finally(() => {
          modalUpdateLoading.value = false
          loadingStore.hide()
        })
    },
  })
}

const handleChangeStatusSemester = (record) => {
  // Kiểm tra xem có thể thay đổi trạng thái không
  if (!canChangeStatus(record)) {
    if (isSemesterInProgress(record)) {
      message.warning('Không thể thay đổi trạng thái của học kỳ đang diễn ra')
    } else if (isSemesterEnded(record)) {
      message.warning('Không thể thay đổi trạng thái của học kỳ đã kết thúc')
    }
    return
  }

  Modal.confirm({
    title: 'Xác nhận thay đổi trạng thái',
    content: `Bạn có chắc muốn thay đổi trạng thái của học kỳ ${record.semesterCode}?`,
    onOk: () => {
      loadingStore.show()
      requestAPI
        .put(`${API_ROUTES_ADMIN.FETCH_DATA_SEMESTER}/status/${record.id}`)
        .then(() => {
          message.success('Cập nhật trạng thái học kỳ thành công')
          fetchSemesters()
        })
        .catch((error) => {
          message.error(
            (error.response && error.response.data && error.response.data.message) ||
              'Lỗi khi cập nhật trạng thái học kỳ',
          )
        })
        .finally(() => {
          loadingStore.hide()
        })
    },
  })
}

const clearFormAdd = () => {
  newSemester.semesterName = defaultDateRange.semesterName
  newSemester.fromDate = defaultDateRange.fromDate
  newSemester.toDate = defaultDateRange.toDate
}

const handleClearFilter = () => {
  Object.keys(filter).forEach((key) => {
    filter[key] = ''
  })
  handleSubmitFilter()
}

const handleSubmitFilter = () => {
  pagination.current = 1
  fetchSemesters()
}

const isStartDateBeforeToday = (startDate) => {
  if (!startDate) return false
  return dayjs().startOf('day') > dayjs(startDate).startOf('day')
}

const isSemesterNotStarted = (semester) => {
  if (!semester.startDate) return false
  const today = dayjs().startOf('day')
  const startDate = dayjs(semester.startDate).startOf('day')
  return today < startDate
}

const isSemesterInProgress = (semester) => {
  if (!semester.startDate || !semester.endDate) return false
  const today = dayjs().startOf('day')
  const startDate = dayjs(semester.startDate).startOf('day')
  const endDate = dayjs(semester.endDate).startOf('day')
  return today >= startDate && today <= endDate
}

const isSemesterEnded = (semester) => {
  if (!semester.endDate) return false
  const today = dayjs().startOf('day')
  const endDate = dayjs(semester.endDate).startOf('day')
  return today > endDate
}

const canChangeStatus = (semester) => {
  // Có thể thay đổi nếu học kỳ chưa bắt đầu hoặc trạng thái hiện tại là không hoạt động (INACTIVE)
  return isSemesterNotStarted(semester) || semester.status === 'INACTIVE'
}

const canEditSemester = (semester) => {
  return !isSemesterEnded(semester)
}

const shouldDisableStartDate = (current) => {
  if (!current) return false

  if (detailSemester.value && detailSemester.value.fromDate) {
    const originalDate = dayjs(
      detailSemester.value.originalFromDate || detailSemester.value.fromDate,
    )

    if (isStartDateBeforeToday(originalDate)) {
      return !current.isSame(originalDate, 'day')
    }
  }

  return current < dayjs().startOf('day')
}

onMounted(() => {
  breadcrumbStore.setRoutes(breadcrumb.value)
  fetchSemesters()
})
</script>

<template>
  <div class="container-fluid">
    <!-- Card Danh sách học kỳ -->
    <div class="row g-3">
      <div class="col-12">
        <a-card :bordered="false" class="cart no-body-padding">
          <a-collapse ghost>
            <a-collapse-panel>
              <template #header><FilterFilled /> Bộ lọc ({{ countFilter }})</template>
              <div class="row g-3 filter-container">
                <div class="col-xl-6 col-md-12 col-sm-12">
                  <div class="label-title">Từ khoá:</div>
                  <a-input
                    v-model:value="filter.semesterCode"
                    placeholder="Tìm kiếm theo mã học kỳ"
                    allowClear
                    @change="handleSubmitFilter"
                  >
                    <template #prefix>
                      <SearchOutlined />
                    </template>
                  </a-input>
                </div>
                <div class="col-xl-3 col-md-6 col-sm-6">
                  <div class="label-title">Trạng thái:</div>
                  <a-select
                    v-model:value="filter.status"
                    placeholder="-- Tất cả trạng thái --"
                    class="w-100"
                    @change="handleSubmitFilter"
                  >
                    <a-select-option :value="''">-- Tất cả trạng thái --</a-select-option>
                    <a-select-option value="ACTIVE">Đang hoạt động</a-select-option>
                    <a-select-option value="INACTIVE">Không hoạt động</a-select-option>
                  </a-select>
                </div>

                <div class="col-xl-3 col-md-6 col-sm-6">
                  <div class="label-title">Khoảng ngày:</div>
                  <a-range-picker
                    v-model:value="filter.dateRange"
                    class="w-100"
                    format="DD/MM/YYYY"
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
          <template #title> <UnorderedListOutlined /> Danh sách học kỳ </template>
          <!-- Nút Thêm học kỳ -->
          <div class="d-flex justify-content-end mb-2">
            <a-tooltip title="Thêm học kỳ mới">
              <a-button type="primary" @click="handleShowModalAdd">
                <PlusOutlined />
                Thêm học kỳ
              </a-button>
            </a-tooltip>
          </div>

          <!-- Bảng hiển thị danh sách học kỳ với chỉ báo loading -->
          <a-table
            class="nowrap"
            :dataSource="semesters"
            :loading="isLoading"
            :columns="columns"
            rowKey="id"
            :pagination="pagination"
            :scroll="{ x: 'auto' }"
            @change="handleTableChange"
          >
            <template #bodyCell="{ column, record, index }">
              <template v-if="column.key === 'rowNumber'">
                {{ (pagination.current - 1) * pagination.pageSize + index + 1 }}
              </template>
              <template v-else-if="column.dataIndex === 'startDate'">
                {{ formatEpochToDate(record.startDate) }}
              </template>
              <template v-else-if="column.dataIndex === 'endDate'">
                {{ formatEpochToDate(record.endDate) }}
              </template>
              <template v-else-if="column.dataIndex === 'semesterStatus'">
                <span class="nowrap">
                  <a-switch
                    class="me-2"
                    :checked="record.semesterStatus === 'ACTIVE' || record.semesterStatus === 1"
                    :disabled="!canChangeStatus(record)"
                    @change="handleChangeStatusSemester(record)"
                  />

                  <a-tag
                    :color="
                      isSemesterEnded(record)
                        ? 'gold'
                        : isSemesterNotStarted(record)
                          ? 'red'
                          : isSemesterInProgress(record)
                            ? 'green'
                            : null
                    "
                  >
                    {{
                      isSemesterEnded(record)
                        ? 'Đã kết thúc'
                        : isSemesterInProgress(record)
                          ? 'Đang diễn ra'
                          : isSemesterNotStarted(record) &&
                              (record.semesterStatus === 'ACTIVE' || record.semesterStatus === 1)
                            ? 'Chưa bắt đầu'
                            : record.semesterStatus === 'ACTIVE' || record.semesterStatus === 1
                              ? 'Đang hoạt động'
                              : 'Không hoạt động'
                    }}
                  </a-tag>
                </span>
              </template>
              <template v-else-if="column.key === 'actions'">
                <a-space>
                  <a-tooltip
                    :title="
                      canEditSemester(record)
                        ? 'Sửa thông tin học kỳ'
                        : 'Không thể sửa học kỳ đã kết thúc'
                    "
                  >
                    <a-button
                      @click="handleUpdateSemester(record)"
                      type="text"
                      :class="['btn-outline-info', 'me-2', { disabled: !canEditSemester(record) }]"
                      :disabled="!canEditSemester(record)"
                      :style="
                        !canEditSemester(record) ? { opacity: 0.5, cursor: 'not-allowed' } : {}
                      "
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

    <a-modal
      v-model:open="modalAdd"
      @ok="handleAddSemester"
      :okButtonProps="{ loading: modalAddLoading }"
      @cancel="clearFormAdd"
      @close="clearFormAdd"
    >
      <template #title>
        <PlusOutlined class="me-2 text-primary" />
        Thêm học kỳ
      </template>
      <a-form layout="vertical">
        <a-form-item label="Tên học kỳ" required>
          <a-select
            v-model:value="newSemester.semesterName"
            placeholder="Chọn kỳ học"
            class="w-100"
          >
            <a-select-option value="SPRING">SPRING</a-select-option>
            <a-select-option value="SUMMER">SUMMER</a-select-option>
            <a-select-option value="FALL">FALL</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="Ngày bắt đầu" required>
          <a-date-picker
            v-model:value="newSemester.fromDate"
            placeholder="Chọn ngày bắt đầu"
            class="w-100"
            format="DD/MM/YYYY"
            @keyup.enter="handleAddSemester"
            :disabledDate="(current) => current && current < dayjs().startOf('day')"
          />
        </a-form-item>
        <a-form-item label="Ngày kết thúc" required>
          <a-date-picker
            v-model:value="newSemester.toDate"
            placeholder="Chọn ngày kết thúc"
            class="w-100"
            format="DD/MM/YYYY"
            @keyup.enter="handleAddSemester"
            :disabledDate="(current) => current && current < newSemester.fromDate"
          />
        </a-form-item>
      </a-form>
    </a-modal>

    <a-modal
      v-model:open="modalUpdate"
      @ok="updateSemester"
      :okButtonProps="{ loading: modalUpdateLoading }"
    >
      <template #title>
        <EditFilled class="me-2 text-primary" />
        Cập nhật học kỳ
      </template>
      <a-form layout="vertical">
        <a-form-item label="Tên học kỳ" required>
          <a-select
            v-model:value="detailSemester.semesterName"
            placeholder="Chọn kỳ học"
            class="w-100"
          >
            <a-select-option value="SPRING">SPRING</a-select-option>
            <a-select-option value="SUMMER">SUMMER</a-select-option>
            <a-select-option value="FALL">FALL</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="Ngày bắt đầu" required>
          <a-date-picker
            v-model:value="detailSemester.fromDate"
            placeholder="Chọn ngày bắt đầu"
            class="w-100"
            format="DD/MM/YYYY"
            @keyup.enter="updateSemester"
            :disabled="
              (detailSemester.fromDate &&
                isStartDateBeforeToday(detailSemester.originalFromDate)) ||
              isSemesterInProgress(detailSemester)
            "
            :disabledDate="shouldDisableStartDate"
          />
          <div
            v-if="
              (detailSemester.fromDate &&
                isStartDateBeforeToday(detailSemester.originalFromDate)) ||
              isSemesterInProgress(detailSemester)
            "
            class="ant-form-item-explain"
          >
            <div class="ant-form-item-explain-error">
              {{
                isSemesterInProgress(detailSemester)
                  ? 'Không thể chỉnh sửa ngày bắt đầu của học kỳ đang diễn ra'
                  : 'Không thể chỉnh sửa ngày bắt đầu đã qua'
              }}
            </div>
          </div>
        </a-form-item>
        <a-form-item label="Ngày kết thúc" required>
          <a-date-picker
            v-model:value="detailSemester.toDate"
            placeholder="Chọn ngày kết thúc"
            class="w-100"
            format="DD/MM/YYYY"
            @keyup.enter="updateSemester"
            :disabledDate="(current) => current && current < detailSemester.fromDate"
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>
