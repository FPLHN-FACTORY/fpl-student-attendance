<script setup>
import { ref, onMounted, reactive, computed } from 'vue'
import {
  FilterFilled,
  UnorderedListOutlined,
  EyeFilled,
  EditFilled,
  ExclamationCircleOutlined,
  CheckOutlined,
  DownloadOutlined,
} from '@ant-design/icons-vue'
import { message, Modal } from 'ant-design-vue'
import requestAPI from '@/services/requestApiService'
import { API_ROUTES_TEACHER } from '@/constants/teacherConstant'
import useBreadcrumbStore from '@/stores/useBreadCrumbStore'
import { GLOBAL_ROUTE_NAMES } from '@/constants/routesConstant'
import { ROUTE_NAMES } from '@/router/teacherRoute'
import { DEFAULT_DATE_FORMAT, DEFAULT_PAGINATION, SHIFT, TYPE_SHIFT } from '@/constants'
import { formatDate, dayOfWeek } from '@/utils/utils'
import useLoadingStore from '@/stores/useLoadingStore'
import dayjs from 'dayjs'
import router from '@/router'

// Khởi tạo breadcrumb
const breadcrumbStore = useBreadcrumbStore()
const breadcrumb = ref([
  { name: GLOBAL_ROUTE_NAMES.TEACHER_PAGE, breadcrumbName: 'Giáo viên' },
  { name: ROUTE_NAMES.TEACHING_SCHEDULE, breadcrumbName: 'Lịch giảng dạy' },
])

// Store loading
const loadingStore = useLoadingStore()
const isLoading = ref(false)

// Đối tượng filter
const filter = reactive({
  idSubject: '',
  idFactory: '',
  idProject: '',
  shift: '',
  shiftType: '',
  durationOption: 'future_7', // mặc định là 7 ngày tới
  page: 1,
  pageSize: 5,
})

// Tính toán startDate và endDate dựa trên durationOption (trả về dayjs objects)
const computedStartDate = computed(() => {
  const [type, daysStr] = filter.durationOption.split('_')
  const days = parseInt(daysStr, 10)
  return type === 'future' ? dayjs() : dayjs().subtract(days, 'day')
})

const computedEndDate = computed(() => {
  const [type, daysStr] = filter.durationOption.split('_')
  const days = parseInt(daysStr, 10)
  return type === 'future' ? dayjs().add(days, 'day') : dayjs()
})

// Dữ liệu lịch dạy hôm nay
const teachingSchedulePresent = ref([])
const presentPagination = ref({ ...DEFAULT_PAGINATION })
// Dữ liệu lịch dạy và phân trang
const teachingScheduleRecords = ref([])
const pagination = ref({ ...DEFAULT_PAGINATION })

// Cột hiển thị trong table lịch dạy chung
const columns = [
  { title: '#', dataIndex: 'indexs', key: 'indexs', width: 50 },
  { title: 'Ngày dạy', dataIndex: 'startTeaching', key: 'startTeaching', width: 100 },
  { title: 'Thời gian', key: 'time', width: 100 },
  { title: 'Ca học', dataIndex: 'shift', key: 'shift', width: 50 },
  { title: 'Điểm danh muộn', dataIndex: 'lateArrival', key: 'lateArrival', width: 50 },
  { title: 'Xưởng', dataIndex: 'factoryName', key: 'factoryName', width: 100 },
  { title: 'Dự án', dataIndex: 'projectName', key: 'projectName', width: 180 },
  { title: 'Địa điểm', dataIndex: 'location', key: 'location', width: 100 },
  { title: 'Hình thức', dataIndex: 'type', key: 'type', width: 100 },
  { title: 'Link học', dataIndex: 'link', key: 'link', width: 180 },
  { title: 'Chi tiết / Sửa', dataIndex: 'description', key: 'description', width: 100 },
]

// Cột hiển thị cho table lịch dạy hôm nay
const columnsTeachingPresent = [
  { title: '#', dataIndex: 'indexs', key: 'indexs', width: 50 },
  { title: 'Thời gian', key: 'time', width: 100 },
  { title: 'Ca học', dataIndex: 'shift', key: 'shift', width: 50 },
  { title: 'Điểm danh muộn ', dataIndex: 'lateArrival', key: 'lateArrival', width: 50 },
  { title: 'Xưởng', dataIndex: 'factoryName', key: 'factoryName', width: 100 },
  { title: 'Dự án', dataIndex: 'projectName', key: 'projectName', width: 180 },
  { title: 'Địa điểm', dataIndex: 'location', key: 'location', width: 100 },
  { title: 'Link học', dataIndex: 'link', key: 'link', width: 180 },
  { title: 'Chi tiết / Sửa', dataIndex: 'description', key: 'description', width: 100 },
  { title: 'Điểm danh', key: 'action', width: 100 },
]

// Các danh sách dropdown
const subjects = ref([])
const factories = ref([])
const projects = ref([])

// Hàm chuyển đổi giá trị filter rỗng thành null
const prepareFilterParams = (params) => {
  const copy = { ...params }
  Object.keys(copy).forEach((key) => {
    if (copy[key] === '') copy[key] = null
    if (key === 'shift' && copy[key] != null) copy[key] = parseInt(copy[key], 10)
  })
  return copy
}

// Lấy dữ liệu lịch dạy hôm nay
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

// Lấy dữ liệu lịch dạy chung
const fetchTeachingSchedule = () => {
  loadingStore.show()
  const { durationOption, ...rest } = filter
  const filterParams = prepareFilterParams(rest)
  requestAPI
    .get(API_ROUTES_TEACHER.FETCH_DATA_SCHEDULE, {
      params: {
        ...filterParams,
        startDate: computedStartDate.value.valueOf(),
        endDate: computedEndDate.value.valueOf(),
        page: pagination.value.current,
        size: pagination.value.pageSize,
      },
    })
    .then((response) => {
      const result = response.data.data
      teachingScheduleRecords.value = result.data
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
const handleAttendance = (record) => {
  console.log('Detail record:', record)
  router.push({
    name: ROUTE_NAMES.MANAGEMENT_STUDENT_ATTENDANCE,
    query: {
      planDateId: record.idPlanDate || record.id,
    },
  })
}
// Lấy danh sách phụ trợ
const fetchSubjects = () => {
  requestAPI
    .get(API_ROUTES_TEACHER.FETCH_DATA_SCHEDULE + '/subjects')
    .then((res) => {
      subjects.value = res.data.data
    })
    .catch(() => message.error(error.response?.data?.message || 'Lỗi khi tải danh sách môn học'))
}
const fetchFactories = () => {
  requestAPI
    .get(API_ROUTES_TEACHER.FETCH_DATA_SCHEDULE + '/factories')
    .then((res) => {
      factories.value = res.data.data
    })
    .catch(() => message.error('Lỗi khi tải danh sách xưởng'))
}
const fetchProjects = () => {
  requestAPI
    .get(API_ROUTES_TEACHER.FETCH_DATA_SCHEDULE + '/projects')
    .then((res) => {
      projects.value = res.data.data
    })
    .catch(() => message.error(error.response?.data?.message || 'Lỗi khi tải danh sách dự án'))
}

// Phân trang
const handlePresentTableChange = (pag) => {
  filter.page = pag.current
  filter.pageSize = pag.pageSize
  presentPagination.value.current = pag.current
  presentPagination.value.pageSize = pag.pageSize
  fetchTeachingSchedulePresent()
}
const handleTableChange = (pag) => {
  filter.page = pag.current
  filter.pageSize = pag.pageSize
  pagination.value.current = pag.current
  pagination.value.pageSize = pag.pageSize
  fetchTeachingSchedule()
}

// Modal chi tiết & cập nhật
const isDetailModalVisible = ref(false)
const detailModalContent = ref('')
const detailLateArrival = ref(0)
const detailLink = ref('')
const currentPlanDateId = ref('')
const isUpdateModalVisible = ref(false)
const formUpdateData = reactive({ description: '', lateArrival: 0, planDateId: '', link: '' })
const formUpdateRules = {
  description: [{ required: true, message: 'Vui lòng nhập mô tả', trigger: 'blur' }],
  lateArrival: [
    { required: true, message: 'Vui lòng nhập thời gian điểm danh muộn', trigger: 'change' },
  ],
}
const handleShowDescription = (record) => {
  requestAPI
    .get(`${API_ROUTES_TEACHER.FETCH_DATA_SCHEDULE}/${record.idPlanDate}`)
    .then((res) => {
      const d = res.data.data
      detailModalContent.value = d.description || 'Không có mô tả'
      detailLateArrival.value = d.lateArrival
      detailLink.value = d.link
      currentPlanDateId.value = d.planDateId
      isDetailModalVisible.value = true
    })
    .catch((e) => message.error(error.response?.data?.message || 'Lỗi khi lấy chi tiết kế hoạch'))
}
const handleShowUpdate = () => {
  formUpdateData.description = detailModalContent.value
  formUpdateData.lateArrival = detailLateArrival.value
  formUpdateData.link = detailLink.value
  isUpdateModalVisible.value = true
}
const handleUpdatePlanDate = () => {
  Modal.confirm({
    title: 'Xác nhận cập nhật buổi dạy',
    content: 'Bạn có chắc muốn lưu thay đổi này không?',
    okText: 'Đồng ý',
    cancelText: 'Hủy',
    onOk() {
      requestAPI
        .put(API_ROUTES_TEACHER.FETCH_DATA_SCHEDULE, {
          idPlanDate: currentPlanDateId.value,
          description: formUpdateData.description,
          lateArrival: formUpdateData.lateArrival,
          link: formUpdateData.link,
        })
        .then(() => {
          message.success('Cập nhật buổi học thành công')
          isUpdateModalVisible.value = false
          isDetailModalVisible.value = false
          fetchTeachingSchedule()
          fetchTeachingSchedulePresent()
        })
        .catch((e) => message.error(e.response?.data?.message || 'Lỗi khi cập nhật buổi học'))
    },
  })
}

// Xuất PDF
const handleExportPDF = () => {
  loadingStore.show()
  const { durationOption, page, pageSize, ...rest } = filter
  const params = {
    ...prepareFilterParams(rest),
    startDate: computedStartDate.value.valueOf(),
    endDate: computedEndDate.value.valueOf(),
  }
  requestAPI
    .get(API_ROUTES_TEACHER.FETCH_DATA_SCHEDULE + '/export-pdf', {
      params,
      responseType: 'blob',
    })
    .then((res) => {
      const blob = new Blob([res.data], { type: 'application/pdf' })
      const link = document.createElement('a')
      link.href = URL.createObjectURL(blob)
      link.download = 'lich-day.pdf'
      link.click()
      message.success('Xuất file PDF thành công')
    })
    .catch((e) => message.error(e.response?.data?.message || 'Lỗi khi xuất file PDF'))
    .finally(() => loadingStore.hide())
}

// Danh sách chọn khoảng thời gian
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

// --- MỞ ĐẦU: thêm hàm để gọi endpoint PUT /change-type/{id}
function handleChangeType(record) {
  loadingStore.show()
  requestAPI
    .put(`${API_ROUTES_TEACHER.FETCH_DATA_SCHEDULE}/change-type/${record.idPlanDate}`)
    .then(() => {
      message.success('Đã đổi hình thức ca học')
      fetchTeachingSchedule()
      fetchTeachingSchedulePresent()
    })
    .catch((e) => message.error(e.response?.data?.message || 'Lỗi đổi hình thức'))
    .finally(() => loadingStore.hide())
}

const showLinkModal = ref(false)
const pendingRecord = ref(null)
const linkInput = ref('')

function handleTypeToggle(record, checked) {
  Modal.confirm({
    title: 'Xác nhận thay đổi hình thức',
    content: checked
      ? 'Bạn có chắc muốn bật Online và nhập link học không?'
      : 'Bạn có chắc muốn chuyển về Offline không?',
    okText: 'Đồng ý',
    cancelText: 'Hủy',
    onOk() {
      if (checked) {
        // bật → mở modal nhập link
        pendingRecord.value = record
        linkInput.value = record.link || ''
        showLinkModal.value = true
      } else {
        // tắt → gọi API ngay
        handleChangeType(record)
      }
    },
  })
}

// khi user confirm nhập link
function confirmLinkModal() {
  if (!linkInput.value) {
    return message.error('Vui lòng nhập link học!')
  }
  showLinkModal.value = false
  loadingStore.show()
  // 1) update link
  requestAPI
    .put(API_ROUTES_TEACHER.FETCH_DATA_SCHEDULE, {
      idPlanDate: pendingRecord.value.idPlanDate,
      link: linkInput.value,
      description: pendingRecord.value.description,
      lateArrival: pendingRecord.value.lateArrival,
    })
    // 2) sau đó toggle sang ONLINE
    .then(() =>
      requestAPI.put(
        `${API_ROUTES_TEACHER.FETCH_DATA_SCHEDULE}/change-type/${pendingRecord.value.idPlanDate}`
      )
    )
    .then(() => {
      message.success('Chuyển Online thành công với link mới')
      fetchTeachingSchedule()
      fetchTeachingSchedulePresent()
    })
    .catch((e) => message.error(e.response?.data?.message || 'Lỗi khi cập nhật link/kiểu học'))
    .finally(() => loadingStore.hide())
}

onMounted(() => {
  breadcrumbStore.setRoutes(breadcrumb.value)
  fetchSubjects()
  fetchFactories()
  fetchProjects()
  fetchTeachingSchedule()
  fetchTeachingSchedulePresent()
})
</script>

<template>
  <div class="container-fluid">
    <!-- Bộ lọc -->
    <div class="row g-5 mb-3">
      <div class="col-12">
        <a-card :bordered="false" class="cart">
          <template #title> <FilterFilled /> Bộ lọc tìm kiếm </template>
          <div class="row g-4">
            <!-- ... các filter giống cũ ... -->
            <div class="col-6">
              <div class="label-title">Khoảng thời gian:</div>
              <a-select
                v-model:value="filter.durationOption"
                class="w-100"
                @change="fetchTeachingSchedule"
              >
                <a-select-option
                  v-for="opt in durationOptions"
                  :key="opt.value"
                  :value="opt.value"
                  >{{ opt.label }}</a-select-option
                >
              </a-select>
            </div>
            <div class="col-6">
              <div class="label-title">Hình thức học:</div>
              <a-select
                v-model:value="filter.shiftType"
                placeholder="Chọn hình thức học"
                allowClear
                style="width: 100%"
                @change="fetchTeachingSchedule"
              >
                <a-select-option :value="''">Tất cả hình thức học</a-select-option>
                <a-select-option value="1">Online</a-select-option>
                <a-select-option value="0">Offline</a-select-option>
              </a-select>
            </div>
          </div>
        </a-card>
      </div>
    </div>

    <!-- Lịch dạy hôm nay -->
    <div class="row g-4 mb-3">
      <div class="col-12">
        <a-card :bordered="false" class="cart">
          <template #title><UnorderedListOutlined /> Lịch dạy hôm nay</template>
          <a-table
            :dataSource="teachingSchedulePresent"
            :columns="columnsTeachingPresent"
            :rowKey="(r) => r.idPlanDate"
            :pagination="presentPagination"
            @change="handlePresentTableChange"
            :loading="isLoading"
            class="nowrap"
            :scroll="{ y: 500, x: 'auto' }"
          >
            <template #bodyCell="{ column, record, index }">
              <template v-if="column.key === 'time'">
                {{
                  `${formatDate(record.startTeaching, 'HH:mm')} - ${formatDate(
                    record.endTeaching,
                    'HH:mm'
                  )}`
                }}
              </template>

              <!-- Các cột có dataIndex -->
              <template v-else-if="column.dataIndex">
                <template v-if="column.dataIndex === 'indexs'">
                  {{ (pagination.current - 1) * pagination.pageSize + index + 1 }}
                </template>
                <template v-else-if="column.dataIndex === 'startTeaching'">
                  {{
                    `${dayOfWeek(record.startTeaching)}, ${formatDate(
                      record.startTeaching,
                      DEFAULT_DATE_FORMAT
                    )}`
                  }}
                </template>
                <template v-else-if="column.dataIndex === 'shift'">
                  <a-tag :color="record.type === 1 ? 'blue' : 'purple'">
                    {{
                      `Ca ${record.shift
                        .split(',')
                        .map((o) => Number(o))
                        .join(', ')} - ${TYPE_SHIFT[record.type]}`
                    }}
                  </a-tag>
                </template>
                <template v-else-if="column.dataIndex === 'link'">
                  <a v-if="record.link" :href="record.link" target="_blank">{{ record.link }}</a>
                </template>
                <template v-else-if="column.dataIndex === 'lateArrival'">
                  <a-tag :color="record.lateArrival > 0 ? 'gold' : 'green'">
                    <ExclamationCircleOutlined /> {{ record.lateArrival + ' phút' }}
                  </a-tag>
                </template>
                <template v-else-if="column.dataIndex === 'description'">
                  <a-tooltip title="Xem, sửa mô tả">
                    <a-typography-link @click="handleShowDescription(record)"
                      >Chi tiết</a-typography-link
                    >
                  </a-tooltip>
                </template>
              </template>

              <!-- Cột action -->
              <template v-else-if="column.key === 'action'">
                <span v-if="Date.now() <= record.startTeaching - 10 * 60 * 1000">
                  <a-badge status="warning" /> Chưa đến giờ điểm danh
                </span>
                <span v-else-if="Date.now() > record.endTeaching">
                  <a-badge status="error" /> Đã quá giờ điểm danh
                </span>
                <a-tooltip v-else title="Điểm danh">
                  <a-button
                    type="primary"
                    class="btn-info ms-2 border-0"
                    @click="handleAttendance(record)"
                  >
                    <CheckOutlined /> Điểm danh
                  </a-button>
                </a-tooltip>
              </template>
            </template>
          </a-table>
        </a-card>
      </div>
    </div>

    <!-- Danh sách lịch dạy chung -->
    <div class="row g-4">
      <div class="col-12">
        <a-card :bordered="false" class="cart">
          <template #title>
            <div class="d-flex justify-content-between align-items-center">
              <div>
                <UnorderedListOutlined /> Danh sách lịch dạy
                <span v-if="filter.durationOption">
                  ({{ formatDate(computedStartDate, DEFAULT_DATE_FORMAT) }}
                  –
                  {{ formatDate(computedEndDate, DEFAULT_DATE_FORMAT) }})
                </span>
              </div>
              <div>
                <a-button type="primary" @click="handleExportPDF">
                  <DownloadOutlined /> Xuất PDF
                </a-button>
              </div>
            </div>
          </template>

          <a-table
            :dataSource="teachingScheduleRecords"
            :columns="columns"
            :rowKey="(r) => r.idPlanDate"
            :pagination="pagination"
            @change="handleTableChange"
            :loading="isLoading"
            class="nowrap"
            :scroll="{ y: 500, x: 'auto' }"
          >
            <template #bodyCell="{ column, record, index }">
              <!-- Xử lý cột Thời gian -->
              <template v-if="column.key === 'time'">
                {{
                  `${formatDate(record.startTeaching, 'HH:mm')} - ${formatDate(
                    record.endTeaching,
                    'HH:mm'
                  )}`
                }}
              </template>

              <!-- Các cột có dataIndex -->
              <template v-else-if="column.dataIndex">
                <template v-if="column.dataIndex === 'indexs'">
                  {{ (pagination.current - 1) * pagination.pageSize + index + 1 }}
                </template>
                <template v-else-if="column.dataIndex === 'startTeaching'">
                  {{
                    `${dayOfWeek(record.startTeaching)}, ${formatDate(
                      record.startTeaching,
                      DEFAULT_DATE_FORMAT
                    )}`
                  }}
                </template>
                <template v-else-if="column.dataIndex === 'shift'">
                  <a-tag :color="record.shift === 1 ? 'blue' : 'purple'">
                    {{ `Ca ${record.shift}` }}
                  </a-tag>
                </template>
                <template v-else-if="column.dataIndex === 'type'">
                  <a-tag :color="record.type === 1 ? 'blue' : 'cyan'">
                    <span>{{ TYPE_SHIFT[record.type] }}</span>
                  </a-tag>
                  <a-switch
                    :checked="record.type === 1"
                    @change="(checked) => handleTypeToggle(record, checked)"
                  />
                </template>
                <template v-else-if="column.dataIndex === 'link'">
                  <a v-if="record.link" :href="record.link" target="_blank">{{ record.link }}</a>
                </template>
                <template v-else-if="column.dataIndex === 'lateArrival'">
                  <a-tag :color="record.lateArrival > 0 ? 'gold' : 'green'">
                    <ExclamationCircleOutlined /> {{ record.lateArrival + ' phút' }}
                  </a-tag>
                </template>
                <template v-else-if="column.dataIndex === 'description'">
                  <a-tooltip title="Xem, sửa mô tả">
                    <a-typography-link @click="handleShowDescription(record)"
                      >Chi tiết</a-typography-link
                    >
                  </a-tooltip>
                </template>
                <template v-else>
                  {{ record[column.dataIndex] }}
                </template>
              </template>
            </template>
          </a-table>
        </a-card>
      </div>
    </div>

    <!-- Modal chi tiết -->
    <a-modal
      v-model:open="isDetailModalVisible"
      title="Chi tiết mô tả buổi dạy"
      :footer="null"
      maskClosable
    >
      <div class="mb-3">{{ detailModalContent }}</div>

      <div v-if="detailLink">
        Link học: <a :href="detailLink" target="_blank">{{ detailLink }}</a>
      </div>
      <div class="d-flex justify-content-end gap-2">
        <a-button @click="isDetailModalVisible = false" class="btn-gray"> Đóng </a-button>
        <a-button type="primary" @click="handleShowUpdate"> Sửa </a-button>
      </div>
    </a-modal>

    <!-- Modal cập nhật -->
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
        <a-form-item label="Điểm danh muộn" name="lateArrival">
          <a-input-number v-model:value="formUpdateData.lateArrival" :min="0" class="w-100" />
        </a-form-item>
        <a-form-item label="Link học" name="link">
          <a-input
            v-model:value="formUpdateData.link"
            placeholder="https://"
            class="w-100"
            allowClear
          />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- modal mới: bắt nhập link khi bật sang Online -->
    <a-modal
      v-model:open="showLinkModal"
      title="Nhập link học để chuyển sang Online"
      @ok="confirmLinkModal"
      @cancel="showLinkModal = false"
    >
      <a-form layout="vertical">
        <a-form-item label="Link học">
          <a-input v-model:value="linkInput" placeholder="https://" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>
