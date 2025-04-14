<script setup>
import { ref, reactive, onMounted } from 'vue'
import {
  PlusOutlined,
  EditOutlined,
  SwapOutlined,
  EditFilled,
  SyncOutlined,
  UnorderedListOutlined,
  FilterFilled,
} from '@ant-design/icons-vue'
import { message, Modal } from 'ant-design-vue'
import requestAPI from '@/services/requestApiService'
import dayjs from 'dayjs'
import { API_ROUTES_ADMIN } from '@/constants/adminConstant'
import { ROUTE_NAMES } from '@/router/adminRoute'
import { DEFAULT_PAGINATION } from '@/constants'
import { useRouter } from 'vue-router'

import { GLOBAL_ROUTE_NAMES } from '@/constants/routesConstant'
import useBreadcrumbStore from '@/stores/useBreadCrumbStore'
import useLoadingStore from '@/stores/useLoadingStore'

const router = useRouter()
const breadcrumbStore = useBreadcrumbStore()
const loadingStore = useLoadingStore()

const breadcrumb = ref([
  {
    name: GLOBAL_ROUTE_NAMES.ADMIN_PAGE,
    breadcrumbName: 'Ban đào tạo',
  },
  {
    name: ROUTE_NAMES.MANAGEMENT_SEMESTER,
    breadcrumbName: 'Học kỳ',
  },
])

// Danh sách học kỳ
const semesters = ref([])
// Dùng filter để lưu trạng thái tìm kiếm & phân trang (không chứa thông tin phân trang)
const filter = reactive({
  semesterCode: '',
  status: '',
  dateRange: null, // Giá trị RangePicker (mảng [start, end])
  // Các tham số phân trang sẽ được lấy từ đối tượng pagination
})

// Đối tượng pagination dùng cho component a-table (đã được làm reactive)
const pagination = reactive({
  ...DEFAULT_PAGINATION,
})

// Biến loading cho bảng và modal
const isLoading = ref(false)
const modalAddLoading = ref(false)
const modalUpdateLoading = ref(false)

// Modal hiển thị
const modalAdd = ref(false)
const modalUpdate = ref(false)

// Dữ liệu cho modal Thêm/Cập Nhật (DatePicker trả về đối tượng dayjs)
const newSemester = reactive({
  semesterName: null,
  fromDate: null,
  toDate: null,
})
const detailSemester = ref({})

// Cấu hình cột cho bảng
const columns = ref([
  { title: '#', dataIndex: 'semesterIndex', key: 'semesterIndex', width: 80 },
  { title: 'Mã học kỳ', dataIndex: 'semesterCode', key: 'semesterCode', width: 180 },
  { title: 'Tên học kỳ', dataIndex: 'semesterName', key: 'semesterName', width: 180 },
  { title: 'Ngày bắt đầu', dataIndex: 'startDate', key: 'startDate', width: 180 },
  { title: 'Ngày kết thúc', dataIndex: 'endDate', key: 'endDate', width: 180 },
  { title: 'Trạng thái', dataIndex: 'semesterStatus', key: 'semesterStatus', width: 180 },
  { title: 'Chức năng', key: 'actions', width: 120 },
])

// Hàm định dạng epoch sang "DD/MM/YYYY"
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
  // Đặt lại trang về 1 và gọi lại fetchSemesters
  pagination.current = 1
  fetchSemesters()
}

// Hàm lấy danh sách học kỳ, truyền phân trang từ pagination
const fetchSemesters = () => {
  if (isLoading.value) return
  loadingStore.show()
  isLoading.value = true

  // Tạo bản sao của filter, chuyển đổi ngày nếu cần
  const params = { ...filter }
  params.fromDateSemester = filter.fromDateSemester ? filter.fromDateSemester.valueOf() : null
  params.toDateSemester = filter.toDateSemester ? filter.toDateSemester.valueOf() : null
  params.page = pagination.current
  params.size = pagination.pageSize

  requestAPI
    .get(API_ROUTES_ADMIN.FETCH_DATA_SEMESTER, { params })
    .then((response) => {
      semesters.value = response.data.data.data
      // Cập nhật tổng số bản ghi: nếu có totalRecords, dùng luôn, nếu không dùng totalPages * pageSize
      if (response.data.data.totalRecords !== undefined) {
        pagination.total = response.data.data.totalRecords
      } else {
        pagination.total = response.data.data.totalPages * pagination.pageSize
      }
    })
    .catch((error) => {
      message.error(
        (error.response && error.response.data && error.response.data.message) ||
          'Lỗi khi lấy dữ liệu học kỳ'
      )
    })
    .finally(() => {
      isLoading.value = false
      loadingStore.hide()
    })
}

// Hàm xử lý thay đổi trang (cập nhật current và pageSize rồi gọi lại API)
const handleTableChange = (pageInfo) => {
  pagination.current = pageInfo.current
  pagination.pageSize = pageInfo.pageSize
  fetchSemesters()
}

const handleAddSemester = () => {
  if (!newSemester.semesterName || !newSemester.fromDate || !newSemester.toDate) {
    message.error('Vui lòng nhập đầy đủ thông tin')
    return
  }
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
          'Lỗi khi thêm học kỳ'
      )
    })
    .finally(() => {
      modalAddLoading.value = false
      loadingStore.hide()
    })
}

const handleUpdateSemester = (record) => {
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
      }
      modalUpdate.value = true
    })
    .catch((error) => {
      message.error(
        (error.response && error.response.data && error.response.data.message) ||
          'Lỗi khi lấy chi tiết học kỳ'
      )
    })
    .finally(() => {
      loadingStore.hide()
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
          'Lỗi khi cập nhật học kỳ'
      )
    })
    .finally(() => {
      modalUpdateLoading.value = false
      loadingStore.hide()
    })
}

const handleChangeStatusSemester = (record) => {
  Modal.confirm({
    title: 'Xác nhận thay đổi trạng thái',
    content: `Bạn có chắc muốn thay đổi trạng thái của học kỳ ${record.semesterName}?`,
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
              'Lỗi khi cập nhật trạng thái học kỳ'
          )
        })
        .finally(() => {
          loadingStore.hide()
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
              <div class="label-title">Tìm kiếm mã học kỳ :</div>
              <a-input
                v-model:value="filter.semesterCode"
                placeholder="Tìm kiếm theo mã học kỳ"
                allowClear
                @change="fetchSemesters"
              />
            </a-col>
            <a-col :span="12" class="col">
              <div class="label-title">Trạng thái :</div>
              <a-select
                v-model:value="filter.status"
                placeholder="Chọn trạng thái"
                allowClear
                style="width: 100%"
                @change="fetchSemesters"
              >
                <a-select-option :value="''">Tất cả trạng thái</a-select-option>
                <a-select-option value="ACTIVE">Đang hoạt động</a-select-option>
                <a-select-option value="INACTIVE">Đã kết thúc</a-select-option>
              </a-select>
            </a-col>
          </a-row>
          <!-- Hàng 2: RangePicker để chọn khoảng ngày -->
          <a-row :gutter="16" class="filter-container second-row mt-3">
            <a-col :span="24" class="col">
              <div class="label-title">Tìm kiếm theo khoảng ngày :</div>
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
              <a-button type="primary" @click="modalAdd = true">
                <PlusOutlined />
                Thêm
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
                <span class="nowrap">
                  <a-switch
                    class="me-2"
                    :checked="record.semesterStatus === 'ACTIVE' || record.semesterStatus === 1"
                    @change="handleChangeStatusSemester(record)"
                  />

                  <a-tag
                    :color="
                      record.semesterStatus === 'ACTIVE' || record.semesterStatus === 1
                        ? 'green'
                        : 'red'
                    "
                  >
                    {{
                      record.semesterStatus === 'ACTIVE' || record.semesterStatus === 1
                        ? 'Đang hoạt động'
                        : 'Đã kết thúc'
                    }}
                  </a-tag>
                </span>
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
    <a-modal
      v-model:open="modalAdd"
      title="Thêm học kỳ"
      @ok="handleAddSemester"
      :okButtonProps="{ loading: modalAddLoading }"
    >
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
    <a-modal
      v-model:open="modalUpdate"
      title="Cập nhật học kỳ"
      @ok="updateSemester"
      :okButtonProps="{ loading: modalUpdateLoading }"
    >
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
