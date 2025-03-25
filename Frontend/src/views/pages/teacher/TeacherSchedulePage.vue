<script setup>
import { ref, onMounted, reactive, computed } from 'vue'
import {
  FilterFilled,
  UnorderedListOutlined,
  EyeFilled,
  EditFilled,
  ExclamationCircleOutlined,
} from '@ant-design/icons-vue'
import { message, Modal } from 'ant-design-vue'
import requestAPI from '@/services/requestApiService'
import { API_ROUTES_TEACHER } from '@/constants/teacherConstant'
import useBreadcrumbStore from '@/stores/useBreadCrumbStore'
import { GLOBAL_ROUTE_NAMES } from '@/constants/routesConstant'
import { ROUTE_NAMES } from '@/router/teacherRoute'
import { DEFAULT_DATE_FORMAT, DEFAULT_PAGINATION, SHIFT } from '@/constants'
import { formatDate, dayOfWeek } from '@/utils/utils'
import useLoadingStore from '@/stores/useLoadingStore'
import dayjs from 'dayjs'

// Khởi tạo breadcrumb
const breadcrumbStore = useBreadcrumbStore()
const breadcrumb = ref([
  { name: GLOBAL_ROUTE_NAMES.TEACHER_PAGE, breadcrumbName: 'Giáo viên' },
  { name: ROUTE_NAMES.TEACHING_SCHEDULE, breadcrumbName: 'Lịch dạy' },
])

// Store loading
const loadingStore = useLoadingStore()
const isLoading = ref(false)

// Đối tượng filter
// durationOption có format "future_7" hoặc "past_14", ...
const filter = reactive({
  idSubject: '',
  idFactory: '',
  idProject: '',
  shift: '',
  durationOption: 'future_7', // mặc định là 7 ngày tới
  page: 1,
  pageSize: 5,
})

// Tính toán startDate và endDate dựa trên durationOption
const computedStartDate = computed(() => {
  const [type, daysStr] = filter.durationOption.split('_')
  const days = parseInt(daysStr)
  return type === 'future' ? Date.now() : Date.now() - days * 24 * 60 * 60 * 1000
})

const computedEndDate = computed(() => {
  const [type, daysStr] = filter.durationOption.split('_')
  const days = parseInt(daysStr)
  return type === 'future' ? Date.now() + days * 24 * 60 * 60 * 1000 : Date.now()
})

// Dữ liệu lịch dạy hôm nay
const teachingSchedulePresent = ref([])
const presentPagination = ref({ ...DEFAULT_PAGINATION })
// Dữ liệu lịch dạy và phân trang
const teachingScheduleRecords = ref([])
const pagination = ref({ ...DEFAULT_PAGINATION })

// Cột hiển thị trong table
const columns = [
  { title: '#', dataIndex: 'indexs', key: 'indexs', width: 50 },
  { title: 'Ngày dạy', dataIndex: 'teachingDay', key: 'teachingDay', width: 100 },
  { title: 'Ca học', dataIndex: 'shift', key: 'shift', width: 50 },
  {
    title: 'Điểm danh muộn tối đa (phút)',
    dataIndex: 'lateArrival',
    key: 'lateArrival',
    width: 50,
  },
  { title: 'Mã môn', dataIndex: 'subjectCode', key: 'subjectCode', width: 50 },
  { title: 'Xưởng', dataIndex: 'factoryName', key: 'factoryName', width: 100 },
  { title: 'Dự án', dataIndex: 'projectName', key: 'projectName', width: 180 },
  { title: 'Mô tả / Hành động', dataIndex: 'description', key: 'description', width: 100 },
]

// phần lịch dạy hiện tại để quản lý điểm danh cho sinh viên
const columnsTeachingPresent = [
  { title: '#', dataIndex: 'indexs', key: 'indexs', width: 50 },
  { title: 'Ngày dạy', dataIndex: 'teachingDay', key: 'teachingDay', width: 100 },
  { title: 'Ca học', dataIndex: 'shift', key: 'shift', width: 50 },
  {
    title: 'Điểm danh muộn tối đa (phút)',
    dataIndex: 'lateArrival',
    key: 'lateArrival',
    width: 50,
  },
  { title: 'Mã môn', dataIndex: 'subjectCode', key: 'subjectCode', width: 50 },
  { title: 'Xưởng', dataIndex: 'factoryName', key: 'factoryName', width: 100 },
  { title: 'Dự án', dataIndex: 'projectName', key: 'projectName', width: 180 },
  { title: 'Mô tả / Hành động', dataIndex: 'description', key: 'description', width: 80 },
  { title: 'Điểm danh', dataIndex: 'attendance', key: 'action', width: 80 },
]
// Các danh sách dropdown
const subjects = ref([])
const factories = ref([])
const projects = ref([])

// Hàm chuyển đổi giá trị filter rỗng thành null
const prepareFilterParams = (params) => {
  const copy = { ...params }
  Object.keys(copy).forEach((key) => {
    if (copy[key] === '') {
      copy[key] = null
    }
    // Nếu là trường shift, chuyển sang số (nếu có giá trị)
    if (key === 'shift' && copy[key] != null) {
      copy[key] = parseInt(copy[key])
    }
  })
  return copy
}
const fetchTeachingSchedulePresent = () => {
  loadingStore.show()
  requestAPI
    .get(API_ROUTES_TEACHER.FETCH_DATA_SCHEDULE + '/schedule-present', {
      params: {
        page: presentPagination.value.current,
        size: presentPagination.value.pageSize,
      },
    })
    .then((response) => {
      const result = response.data.data
      teachingSchedulePresent.value = result.data
      presentPagination.value.total =
        result.totalRecords || result.totalPages * presentPagination.value.pageSize
      presentPagination.value.current = result.page || presentPagination.value.current
    })
    .catch((error) => {
      message.error(error.response?.data?.message || 'Lỗi khi tải dữ liệu lịch dạy hôm nay')
    })
    .finally(() => {
      loadingStore.hide()
    })
}

// Lấy dữ liệu lịch dạy
const fetchTeachingSchedule = () => {
  loadingStore.show()
  // Tách durationOption ra, và chuẩn hóa các giá trị filter
  const { durationOption, ...rest } = filter
  const filterParams = prepareFilterParams(rest)
  requestAPI
    .get(API_ROUTES_TEACHER.FETCH_DATA_SCHEDULE, {
      params: {
        ...filterParams,
        startDate: computedStartDate.value,
        endDate: computedEndDate.value,
        page: pagination.value.current,
        size: pagination.value.pageSize,
      },
    })
    .then((response) => {
      const result = response.data.data
      teachingScheduleRecords.value = result.data
      // Cập nhật pagination: nếu API trả về totalRecords thì dùng nó,
      // nếu không thì nhân với pageSize
      pagination.value.total = result.totalRecords || result.totalPages * filter.pageSize
      pagination.value.current = filter.page
    })
    .catch((error) => {
      message.error(error.response?.data?.message || 'Lỗi khi tải dữ liệu lịch dạy')
    })
    .finally(() => {
      loadingStore.hide()
    })
}

// Lấy danh sách môn học
const fetchSubjects = () => {
  requestAPI
    .get(API_ROUTES_TEACHER.FETCH_DATA_SCHEDULE + '/subjects')
    .then((response) => {
      subjects.value = response.data.data
    })
    .catch((error) => {
      message.error(error.response?.data?.message || 'Lỗi khi tải danh sách môn học')
    })
}

// Lấy danh sách xưởng
const fetchFactories = () => {
  requestAPI
    .get(API_ROUTES_TEACHER.FETCH_DATA_SCHEDULE + '/factories')
    .then((response) => {
      factories.value = response.data.data
    })
    .catch((error) => {
      message.error(error.response?.data?.message || 'Lỗi khi tải danh sách xưởng')
    })
}

// Lấy danh sách dự án
const fetchProjects = () => {
  requestAPI
    .get(API_ROUTES_TEACHER.FETCH_DATA_SCHEDULE + '/projects')
    .then((response) => {
      projects.value = response.data.data
    })
    .catch((error) => {
      message.error(error.response?.data?.message || 'Lỗi khi tải danh sách dự án')
    })
}
const handlePresentTableChange = (paginationData) => {
  filter.page = paginationData.current
  filter.pageSize = paginationData.pageSize
  presentPagination.value.current = paginationData.current
  presentPagination.value.pageSize = paginationData.pageSize
  fetchTeachingSchedulePresent()
}

// Xử lý phân trang
const handleTableChange = (paginationData) => {
  filter.page = paginationData.current
  filter.pageSize = paginationData.pageSize
  pagination.value.current = paginationData.current
  pagination.value.pageSize = paginationData.pageSize
  fetchTeachingSchedule()
}

// Modal chi tiết
const isDetailModalVisible = ref(false)
const detailModalContent = ref('')
const detailLateArrival = ref('')
const currentPlanDateId = ref('')

// Xem mô tả
const handleShowDescription = (record) => {
  requestAPI
    .get(`${API_ROUTES_TEACHER.FETCH_DATA_SCHEDULE}/${record.idPlanDate}`)
    .then((response) => {
      const detailData = response.data.data
      detailModalContent.value = detailData.description || 'Không có mô tả'
      detailLateArrival.value = detailData.lateArrival
      currentPlanDateId.value = detailData.planDateId
      isDetailModalVisible.value = true
    })
    .catch((error) => {
      message.error(error.response?.data?.message || 'Lỗi khi lấy chi tiết kế hoạch')
    })
}

// Modal cập nhật
const isUpdateModalVisible = ref(false)
const formUpdateData = reactive({
  description: '',
  lateArrival: 0,
  planDateId: '',
})
const formUpdateRules = {
  description: [{ required: true, message: 'Vui lòng nhập mô tả', trigger: 'blur' }],
  lateArrival: [
    { required: true, message: 'Vui lòng nhập thời gian điểm danh muộn', trigger: 'change' },
  ],
}

// Mở modal cập nhật
const handleShowUpdate = () => {
  formUpdateData.description = detailModalContent.value
  formUpdateData.lateArrival = detailLateArrival.value
  isUpdateModalVisible.value = true
}

// Cập nhật buổi dạy
const handleUpdatePlanDate = () => {
  const requestBody = {
    idPlanDate: currentPlanDateId.value,
    description: formUpdateData.description,
    lateArrival: formUpdateData.lateArrival,
  }
  requestAPI
    .put(API_ROUTES_TEACHER.FETCH_DATA_SCHEDULE, requestBody)
    .then((response) => {
      message.success(response.data.message || 'Cập nhật buổi học thành công')
      isUpdateModalVisible.value = false
      isDetailModalVisible.value = false
      fetchTeachingSchedule()
    })
    .catch((error) => {
      message.error(error.response?.data?.message || 'Lỗi khi cập nhật buổi học')
    })
}

// Danh sách chọn ngày tới/ngày trước
const durationOptions = [
  { label: '7 ngày tới', value: 'future_7' },
  { label: '14 ngày tới', value: 'future_14' },
  { label: '30 ngày tới', value: 'future_30' },
  { label: '90 ngày tới', value: 'future_90' },
  { label: '7 ngày trước', value: 'past_7' },
  { label: '14 ngày trước', value: 'past_14' },
  { label: '30 ngày trước', value: 'past_30' },
  { label: '90 ngày trước', value: 'past_90' },
]

onMounted(() => {
  breadcrumbStore.setRoutes(breadcrumb.value)
  fetchTeachingSchedule()
  fetchTeachingSchedulePresent()
  fetchSubjects()
  fetchFactories()
  fetchProjects()
})
</script>

<template>
  <div class="container-fluid">
    <!-- Row cho bộ lọc -->
    <div class="row g-5 mb-3">
      <div class="col-12">
        <a-card :bordered="false" class="cart">
          <template #title> <FilterFilled /> Bộ lọc </template>
          <div class="row g-4">
            <!-- Môn học -->
            <div class="col-lg-6 col-md-8 col-sm-12">
              <div class="label-title">Môn học:</div>
              <a-select
                v-model:value="filter.idSubject"
                placeholder="Chọn môn học"
                class="w-100"
                allowClear
                @change="fetchTeachingSchedule"
              >
                <a-select-option :value="''">Tất cả môn học</a-select-option>
                <a-select-option v-for="subject in subjects" :key="subject.id" :value="subject.id">
                  {{ subject.code }}
                </a-select-option>
              </a-select>
            </div>
            <!-- Xưởng -->
            <div class="col-lg-6 col-md-8 col-sm-12">
              <div class="label-title">Xưởng:</div>
              <a-select
                v-model:value="filter.idFactory"
                placeholder="Chọn xưởng"
                class="w-100"
                allowClear
                @change="fetchTeachingSchedule"
              >
                <a-select-option :value="''">Tất cả xưởng</a-select-option>
                <a-select-option v-for="factory in factories" :key="factory.id" :value="factory.id">
                  {{ factory.name }}
                </a-select-option>
              </a-select>
            </div>
            <!-- Dự án -->
            <div class="col-lg-6 col-md-8 col-sm-12">
              <div class="label-title">Dự án:</div>
              <a-select
                v-model:value="filter.idProject"
                placeholder="Chọn dự án"
                class="w-100"
                allowClear
                @change="fetchTeachingSchedule"
              >
                <a-select-option :value="''">Tất cả dự án</a-select-option>
                <a-select-option v-for="project in projects" :key="project.id" :value="project.id">
                  {{ project.name }}
                </a-select-option>
              </a-select>
            </div>
            <!-- Ca học -->
            <div class="col-lg-6 col-md-8 col-sm-12">
              <div class="label-title">Ca học:</div>
              <a-select
                v-model:value="filter.shift"
                placeholder="Chọn ca học"
                class="w-100"
                allowClear
                @change="fetchTeachingSchedule"
              >
                <a-select-option :value="''">Tất cả ca học</a-select-option>
                <a-select-option v-for="(name, id) in SHIFT" :key="id" :value="id">
                  {{ name }}
                </a-select-option>
              </a-select>
            </div>
            <!-- Khoảng thời gian -->
            <div class="col-12">
              <div class="label-title">Khoảng thời gian:</div>
              <a-select
                v-model:value="filter.durationOption"
                placeholder="Chọn khoảng thời gian"
                class="w-100"
                @change="fetchTeachingSchedule"
              >
                <a-select-option
                  v-for="option in durationOptions"
                  :key="option.value"
                  :value="option.value"
                >
                  {{ option.label }}
                </a-select-option>
              </a-select>
            </div>
          </div>
        </a-card>
      </div>
    </div>
    <!-- Row cho table lịch dạy hôm nay -->
    <div class="row g-4 mb-3">
      <div class="col-12">
        <a-card :bordered="false" class="cart">
          <template #title> <UnorderedListOutlined /> Lịch dạy hôm nay </template>
          <a-table
            :dataSource="teachingSchedulePresent"
            :columns="columnsTeachingPresent"
            :rowKey="(record) => record.idPlanDate"
            :pagination="presentPagination"
            @change="handlePresentTableChange"
            :loading="isLoading"
            :scroll="{ y: 500, x: 'auto' }"
          >
            <template #bodyCell="{ column, record, index }">
              <template v-if="column.dataIndex">
                <!-- Cột số thứ tự -->
                <template v-if="column.dataIndex === 'indexs'">
                  {{ (presentPagination.current - 1) * presentPagination.pageSize + index + 1 }}
                </template>
                <!-- Cột ngày dạy -->
                <template v-else-if="column.dataIndex === 'teachingDay'">
                  {{
                    `${dayOfWeek(record.teachingDay)} - ${formatDate(
                      record.teachingDay,
                      DEFAULT_DATE_FORMAT + ' HH:mm'
                    )}`
                  }}
                </template>
                <!-- Cột ca học -->
                <template v-else-if="column.dataIndex === 'shift'">
                  <a-tag color="purple">
                    {{ SHIFT[record.shift] }}
                  </a-tag>
                </template>
                <!-- Cột điểm danh muộn -->
                <template v-else-if="column.dataIndex === 'lateArrival'">
                  <a-tag :color="record.lateArrival > 0 ? 'gold' : 'green'">
                    {{ record.lateArrival }}
                    <ExclamationCircleOutlined />
                  </a-tag>
                </template>
                <!-- Cột mô tả / hành động -->
                <template v-else-if="column.dataIndex === 'description'">
                  <a-tooltip title="Xem, sửa mô tả">
                    <a-typography-link @click="handleShowDescription(record)">
                      Chi tiết
                    </a-typography-link>
                  </a-tooltip>
                </template>
                <!-- Các cột khác -->
                <template v-else>
                  {{ record[column.dataIndex] }}
                </template>
              </template>
            </template>
          </a-table>
        </a-card>
      </div>
    </div>

    <!-- Row cho bảng lịch dạy -->
    <div class="row g-4">
      <div class="col-12">
        <a-card :bordered="false" class="cart">
          <template #title>
            <UnorderedListOutlined /> Danh sách lịch dạy
            <span v-if="filter.durationOption">
              ({{ formatDate(computedStartDate, DEFAULT_DATE_FORMAT) }} -
              {{ formatDate(computedEndDate, DEFAULT_DATE_FORMAT) }})
            </span>
          </template>

          <!-- Sử dụng slot bodyCell để tạo index động và hiển thị các cột -->
          <a-table
            :dataSource="teachingScheduleRecords"
            :columns="columns"
            :rowKey="(record) => record.idPlanDate"
            :pagination="pagination"
            @change="handleTableChange"
            :loading="isLoading"
            :scroll="{ y: 500, x: 'auto' }"
          >
            <template #bodyCell="{ column, record, index }">
              <template v-if="column.dataIndex">
                <!-- Cột số thứ tự -->
                <template v-if="column.dataIndex === 'indexs'">
                  {{ (pagination.current - 1) * pagination.pageSize + index + 1 }}
                </template>
                <!-- Cột ngày dạy -->
                <template v-else-if="column.dataIndex === 'teachingDay'">
                  {{
                    `${dayOfWeek(record.teachingDay)} - ${formatDate(
                      record.teachingDay,
                      DEFAULT_DATE_FORMAT + ' HH:mm'
                    )}`
                  }}
                </template>
                <!-- Cột ca học -->
                <template v-else-if="column.dataIndex === 'shift'">
                  <a-tag color="purple">
                    {{ SHIFT[record.shift] }}
                  </a-tag>
                </template>
                <!-- Cột điểm danh muộn -->
                <template v-else-if="column.dataIndex === 'lateArrival'">
                  <a-tag :color="record.lateArrival > 0 ? 'gold' : 'green'">
                    {{ record.lateArrival }}
                    <ExclamationCircleOutlined />
                  </a-tag>
                </template>
                <!-- Cột mô tả / hành động -->
                <template v-else-if="column.dataIndex === 'description'">
                  <a-tooltip title="Xem, sửa mô tả">
                    <a-typography-link @click="handleShowDescription(record)">
                      Chi tiết
                    </a-typography-link>
                  </a-tooltip>
                </template>
                <!-- Các cột khác -->
                <template v-else>
                  {{ record[column.dataIndex] }}
                </template>
              </template>
            </template>
          </a-table>
        </a-card>
      </div>
    </div>

    <!-- Modal chi tiết mô tả có nút "Sửa" -->
    <a-modal
      v-model:open="isDetailModalVisible"
      title="Chi tiết mô tả buổi dạy"
      :footer="null"
      maskClosable
    >
      <div class="mb-3">{{ detailModalContent }}</div>
      <div class="d-flex justify-content-end gap-2">
        <a-button @click="isDetailModalVisible = false" class="btn-gray"> Đóng </a-button>
        <a-button type="primary" @click="handleShowUpdate"> Sửa </a-button>
      </div>
    </a-modal>

    <!-- Modal cập nhật với 2 ô input: description và lateArrival -->
    <a-modal
      v-model:open="isUpdateModalVisible"
      title="Cập nhật buổi dạy"
      :okButtonProps="{ loading: isLoading }"
      @ok="handleUpdatePlanDate"
      @cancel="isUpdateModalVisible = false"
    >
      <a-form layout="vertical" :model="formUpdateData" :rules="formUpdateRules">
        <a-form-item label="Nội dung buổi học" name="description">
          <a-textarea v-model:value="formUpdateData.description" rows="4" class="w-100" />
        </a-form-item>
        <a-form-item label="Điểm danh muộn tối đa (phút)" name="lateArrival">
          <a-input-number v-model:value="formUpdateData.lateArrival" :min="0" class="w-100" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>
