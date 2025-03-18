<script setup>
import { ref, reactive, onMounted } from 'vue'
import {
  PlusOutlined,
  EditOutlined,
  SwapOutlined,
  EditFilled,
  SyncOutlined,
} from '@ant-design/icons-vue'
import { message, Modal } from 'ant-design-vue'
import requestAPI from '@/services/requestApiService'
import dayjs from 'dayjs'
import { API_ROUTES_ADMIN } from '@/constants/adminConstant'
import { DEFAULT_PAGINATION } from '@/constants/paginationConstant'
import { useRouter } from 'vue-router'
import { ROUTE_NAMES } from '@/router/adminRoute'
import { GLOBAL_ROUTE_NAMES } from '@/constants/routesConstant'
import useBreadcrumbStore from '@/stores/useBreadCrumbStore'

const router = useRouter()
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
// State
const semesters = ref([])
const filter = reactive({
  semesterCode: '',
  status: '',
  dateRange: null, // Giá trị RangePicker (mảng [start, end])
  page: 1,
  pageSize: 5,
})
const pagination = reactive({
  ...DEFAULT_PAGINATION,
})

const modalAdd = ref(false)
const modalUpdate = ref(false)

// Dữ liệu cho modal Thêm/Cập Nhật (DatePicker trả về đối tượng dayjs)
const newSemester = reactive({
  semesterName: null,
  fromDate: null,
  toDate: null,
})
const detailSemester = ref({})

// Columns cho bảng
const columns = ref([
  { title: '#', dataIndex: 'semesterIndex', key: 'semesterIndex' },
  { title: 'Mã học kỳ', dataIndex: 'semesterCode', key: 'semesterCode' },
  { title: 'Tên học kỳ', dataIndex: 'semesterName', key: 'semesterName' },
  { title: 'Ngày bắt đầu', dataIndex: 'startDate', key: 'startDate' },
  { title: 'Ngày kết thúc', dataIndex: 'endDate', key: 'endDate' },
  { title: 'Trạng thái', dataIndex: 'semesterStatus', key: 'semesterStatus' },
  { title: 'Chức năng', key: 'actions' },
])

// Hàm định dạng epoch sang định dạng "DD/MM/YYYY" sử dụng dayjs
const formatEpochToDate = (epoch) => {
  if (!epoch) return ''
  return dayjs(epoch).format('DD/MM/YYYY')
}

// Khi RangePicker thay đổi, chuyển đổi giá trị sang filter.fromDateSemester và filter.toDateSemester
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
  fetchSemesters()
}

const fetchSemesters = () => {
  // Tạo một bản sao của filter, sau đó loại bỏ dateRange
  const { dateRange, ...params } = filter
  params.fromDateSemester = filter.fromDateSemester ? filter.fromDateSemester.valueOf() : null
  params.toDateSemester = filter.toDateSemester ? filter.toDateSemester.valueOf() : null

  requestAPI
    .get(API_ROUTES_ADMIN.FETCH_DATA_SEMESTER, { params })
    .then((response) => {
      semesters.value = response.data.data.data
      pagination.total = response.data.data.totalPages * filter.pageSize
      pagination.current = filter.page
    })
    .catch((error) => {
      message.error(
        (error.response && error.response.data && error.response.data.message) ||
          'Lỗi khi lấy dữ liệu học kỳ'
      )
    })
}

const handleTableChange = (page) => {
  pagination.page = page.current
  pagination.pageSize = page.pageSize
  fetchSemesters()
}

const handleAddSemester = () => {
  if (!newSemester.semesterName || !newSemester.fromDate || !newSemester.toDate) {
    message.error('Vui lòng nhập đầy đủ thông tin')
    return
  }
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
          'Lỗi khi thêm học kỳ'
      )
    })
}

const handleUpdateSemester = (record) => {
  requestAPI
    .get(`${API_ROUTES_ADMIN.FETCH_DATA_SEMESTER}/${record.id}`)
    .then((response) => {
      const data = response.data.data
      detailSemester.value = {
        ...data,
        fromDate: dayjs(data.fromDate),
        toDate: dayjs(data.toDate),
        semesterId: data.semesterId || data.id,
      }
      modalUpdate.value = true
    })
    .catch((error) => {
      message.error(
        (error.response && error.response.data && error.response.data.message) ||
          'Lỗi khi lấy chi tiết học kỳ'
      )
    })
}

const updateSemester = () => {
  if (
    !detailSemester.value.semesterName ||
    !detailSemester.value.fromDate ||
    !detailSemester.value.toDate
  ) {
    message.error('Vui lòng nhập đầy đủ thông tin')
    return
  }
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
          'Lỗi khi cập nhật học kỳ'
      )
    })
}

const handleChangeStatusSemester = (record) => {
  Modal.confirm({
    title: 'Xác nhận thay đổi trạng thái',
    content: `Bạn có chắc chắn muốn thay đổi trạng thái của học kỳ ${record.semesterName}?`,
    onOk: () => {
      requestAPI
        .put(`${API_ROUTES_ADMIN.FETCH_DATA_SEMESTER}/status/${record.id}`)
        .then(() => {
          message.success('Cập nhật trạng thái học kỳ thành công')
          fetchSemesters()
        })
        .catch((error) => {
          message.error(
            (error.response && error.response.data && error.response.data.message) ||
              'Lỗi khi cập nhật trạng thái học kỳ'
          )
        })
    },
  })
}

const clearFormAdd = () => {
  newSemester.semesterName = ''
  newSemester.fromDate = null
  newSemester.toDate = null
}

onMounted(() => {
  breadcrumbStore.setRoutes(breadcrumb.value)
  fetchSemesters()
})
</script>


<template>
  <div class="container-fluid">
    <!-- Card Bộ lọc -->
    <div class="row g-3">
      <div class="col-12">
        <a-card :bordered="false" class="cart mb-3">
          <template #title> <FilterFilled /> Bộ lọc </template>
          <!-- Hàng 1: Input tìm kiếm & Select trạng thái -->
          <a-row :gutter="16" class="filter-container">
            <a-col :span="12" class="col">
              <a-input
                v-model:value="filter.semesterCode"
                placeholder="Tìm kiếm theo mã học kỳ"
                allowClear
                @change="fetchSemesters"
              />
            </a-col>
            <a-col :span="12" class="col">
              <a-select
                v-model:value="filter.status"
                placeholder="Chọn trạng thái"
                allowClear
                style="width: 100%"
                @change="fetchSemesters"
              >
                <a-select-option :value="''">Tất cả trạng thái</a-select-option>
                <a-select-option value="ACTIVE">Hoạt động</a-select-option>
                <a-select-option value="INACTIVE">Không hoạt động</a-select-option>
              </a-select>
            </a-col>
          </a-row>
          <!-- Hàng 2: RangePicker để chọn khoảng ngày -->
          <a-row :gutter="16" class="filter-container second-row mt-3">
            <a-col :span="24" class="col">
              <a-range-picker
                v-model:value="filter.dateRange"
                placeholder="Chọn khoảng ngày bắt đầu và kết thúc"
                style="width: 100%"
                format="DD/MM/YYYY"
                @change="handleDateRangeChange"
              />
            </a-col>
          </a-row>
        </a-card>
      </div>
    </div>

    <!-- Card Danh sách học kỳ -->
    <div class="row g-3">
      <div class="col-12">
        <a-card :bordered="false" class="cart">
          <template #title> <UnorderedListOutlined /> Danh sách học kỳ </template>
          <!-- Nút Thêm học kỳ -->
          <div class="d-flex justify-content-end mb-3">
            <a-tooltip title="Thêm học kỳ mới">
              <!-- Nút chuyển sang kiểu filled (primary) -->
              <a-button type="primary" @click="modalAdd = true">
                <PlusOutlined />
                Thêm
              </a-button>
            </a-tooltip>
          </div>
          <!-- Bảng hiển thị danh sách học kỳ -->
          <a-table
            class="nowrap"
            :dataSource="semesters"
            :loading="isLoading"
            :columns="columns"
            rowKey="id"
            :pagination="pagination"
            :scroll="{ y: 500, x: 'auto' }"
            @change="handleTableChange"
          >
            <template #bodyCell="{ column, record }">
              <!-- Hiển thị ngày bắt đầu -->
              <template v-if="column.dataIndex === 'startDate'">
                {{ formatEpochToDate(record.startDate) }}
              </template>
              <!-- Hiển thị ngày kết thúc -->
              <template v-else-if="column.dataIndex === 'endDate'">
                {{ formatEpochToDate(record.endDate) }}
              </template>
              <!-- Hiển thị trạng thái -->
              <template v-else-if="column.dataIndex === 'semesterStatus'">
                <a-tag
                  :color="
                    record.semesterStatus === 'ACTIVE' || record.semesterStatus === 1
                      ? 'green'
                      : 'red'
                  "
                >
                  {{
                    record.semesterStatus === 'ACTIVE' || record.semesterStatus === 1
                      ? 'Hoạt động'
                      : 'Không hoạt động'
                  }}
                </a-tag>
              </template>
              <!-- Các chức năng: Sửa & Đổi trạng thái -->
              <template v-else-if="column.key === 'actions'">
                <a-space>
                  <a-tooltip title="Sửa thông tin học kỳ">
                    <a-button
                      @click="handleUpdateSemester(record)"
                      type="text"
                      class="btn-outline-info me-2"
                    >
                      <EditFilled />
                    </a-button>
                  </a-tooltip>
                  <a-tooltip title="Đổi trạng thái học kỳ">
                    <a-button
                      @click="handleChangeStatusSemester(record)"
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

    <!-- Modal Thêm Học Kỳ -->
    <a-modal v-model:open="modalAdd" title="Thêm học kỳ" @ok="handleAddSemester">
      <a-form layout="vertical">
        <a-form-item label="Tên học kỳ" required>
          <a-select
            v-model:value="newSemester.semesterName"
            placeholder="Chọn kỳ học"
            style="width: 100%"
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
            style="width: 100%"
            format="DD/MM/YYYY"
          />
        </a-form-item>
        <a-form-item label="Ngày kết thúc" required>
          <a-date-picker
            v-model:value="newSemester.toDate"
            placeholder="Chọn ngày kết thúc"
            style="width: 100%"
            format="DD/MM/YYYY"
          />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- Modal Cập Nhật Học Kỳ -->
    <a-modal v-model:open="modalUpdate" title="Cập nhật học kỳ" @ok="updateSemester">
      <a-form layout="vertical">
        <a-form-item label="Tên học kỳ" required>
          <a-select
            v-model:value="detailSemester.semesterName"
            placeholder="Chọn kỳ học"
            style="width: 100%"
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
            style="width: 100%"
            format="DD/MM/YYYY"
          />
        </a-form-item>
        <a-form-item label="Ngày kết thúc" required>
          <a-date-picker
            v-model:value="detailSemester.toDate"
            placeholder="Chọn ngày kết thúc"
            style="width: 100%"
            format="DD/MM/YYYY"
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>
