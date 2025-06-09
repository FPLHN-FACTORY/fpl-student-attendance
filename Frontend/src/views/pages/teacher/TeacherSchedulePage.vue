<script setup>
import { ref, onMounted, reactive, computed } from 'vue'
import {
  FilterFilled,
  UnorderedListOutlined,
  ExclamationCircleOutlined,
  CheckOutlined,
  DownloadOutlined,
} from '@ant-design/icons-vue'
import { message, Modal } from 'ant-design-vue'
import requestAPI from '@/services/requestApiService'
import { API_ROUTES_TEACHER } from '@/constants/teacherConstant'
import useBreadcrumbStore from '@/stores/useBreadCrumbStore'
import { GLOBAL_ROUTE_NAMES } from '@/constants/routesConstant'
import { ROUTE_NAMES, TeacherRoutes } from '@/router/teacherRoute'
import { DEFAULT_DATE_FORMAT, DEFAULT_PAGINATION, TYPE_SHIFT } from '@/constants'
import { formatDate, dayOfWeek, autoAddColumnWidth } from '@/utils/utils'
import useLoadingStore from '@/stores/useLoadingStore'
import dayjs from 'dayjs'
import router from '@/router'
import useApplicationStore from '@/stores/useApplicationStore'

// Khởi tạo breadcrumb
const applicationStore = useApplicationStore()
const breadcrumbStore = useBreadcrumbStore()
const breadcrumb = ref([
  { name: GLOBAL_ROUTE_NAMES.TEACHER_PAGE, breadcrumbName: 'Giảng viên' },
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
const columns = autoAddColumnWidth([
  { title: '#', dataIndex: 'indexs', key: 'indexs' },
  {
    title: 'Ngày dạy',
    dataIndex: 'startTeaching',
    key: 'startTeaching',
  },
  { title: 'Thời gian', key: 'time' },
  { title: 'Ca học', dataIndex: 'shift', key: 'shift' },
  {
    title: 'Điểm danh muộn',
    dataIndex: 'lateArrival',
    key: 'lateArrival',
  },
  { title: 'Nhóm Xưởng', dataIndex: 'factoryName', key: 'factoryName' },
  { title: 'Dự án', dataIndex: 'projectName', key: 'projectName' },
  { title: 'Địa điểm học', dataIndex: 'room', key: 'room' },
  { title: 'Hình thức', dataIndex: 'type', key: 'type' },
  { title: 'Link học', dataIndex: 'link', key: 'link' },
  {
    title: 'Chi tiết / Sửa',
    dataIndex: 'description',
    key: 'description',
  },
])

// Cột hiển thị cho table lịch dạy hôm nay
const columnsTeachingPresent = autoAddColumnWidth([
  { title: '#', dataIndex: 'indexs', key: 'indexs' },
  { title: 'Thời gian', key: 'time' },
  { title: 'Ca học', dataIndex: 'shift', key: 'shift' },
  {
    title: 'Điểm danh muộn ',
    dataIndex: 'lateArrival',
    key: 'lateArrival',
  },
  { title: 'Xưởng', dataIndex: 'factoryName', key: 'factoryName' },
  { title: 'Dự án', dataIndex: 'projectName', key: 'projectName' },
  { title: 'Địa điểm học', dataIndex: 'room', key: 'room' },
  { title: 'Link học', dataIndex: 'link', key: 'link' },
  {
    title: 'Chi tiết / Sửa',
    dataIndex: 'description',
    key: 'description',
  },
  { title: 'Điểm danh', key: 'action' },
])

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
  if (Date.now() <= record.startTeaching - 10 * 60 * 1000) {
    message.error('Chưa đến giờ điểm danh cho buổi học này')
  } else if (Date.now > record.endTeaching) {
    message.error('Đã quá giờ điểm danh cho buổi học này')
  } else {
    router.push({
      name: ROUTE_NAMES.MANAGEMENT_STUDENT_ATTENDANCE,
      query: {
        idPlanDate: record.idPlanDate || record.id,
      },
    })
  }
}
// Lấy danh sách phụ trợ
const fetchSubjects = () => {
  requestAPI
    .get(API_ROUTES_TEACHER.FETCH_DATA_SCHEDULE + '/subjects')
    .then((res) => {
      subjects.value = res.data.data
    })
    .catch((error) => {
      message.error(error.response?.data?.message || 'Lỗi khi tải danh sách môn học')
    })
}
const fetchFactories = () => {
  requestAPI
    .get(API_ROUTES_TEACHER.FETCH_DATA_SCHEDULE + '/factories')
    .then((res) => {
      factories.value = res.data.data
    })
    .catch((error) => {
      message.error(error.response?.data?.message || 'Lỗi khi tải danh sách xưởng')
    })
}
const fetchProjects = () => {
  requestAPI
    .get(API_ROUTES_TEACHER.FETCH_DATA_SCHEDULE + '/projects')
    .then((res) => {
      projects.value = res.data.data
    })
    .catch((error) => {
      message.error(error.response?.data?.message || 'Lỗi khi tải danh sách dự án')
    })
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
const detailModalContent = ref('')
const detailLateArrival = ref(0)
const detailLink = ref('')
const detailRoom = ref('')
const currentPlanDateId = ref('')
const isUpdateModalVisible = ref(false)
const formUpdateData = reactive({
  description: '',
  lateArrival: 0,
  planDateId: '',
  link: '',
  room: '',
})
const formUpdateRules = {
  lateArrival: [
    { required: true, message: 'Vui lòng nhập thời gian điểm danh muộn', trigger: 'change' },
  ],
}
const handleShowDescription = (record) => {
  requestAPI
    .get(`${API_ROUTES_TEACHER.FETCH_DATA_SCHEDULE}/${record.idPlanDate}`)
    .then((res) => {
      const d = res.data.data
      detailModalContent.value = d.description
      detailLateArrival.value = d.lateArrival
      detailLink.value = d.link
      detailRoom.value = d.room || ''
      currentPlanDateId.value = d.planDateId
      handleShowUpdate()
    })
    .catch((error) => {
      message.error(error.response?.data?.message || 'Lỗi khi lấy chi tiết kế hoạch')
    })
}
const handleShowUpdate = () => {
  formUpdateData.description = detailModalContent.value
  formUpdateData.lateArrival = detailLateArrival.value
  formUpdateData.link = detailLink.value
  formUpdateData.room = detailRoom.value
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
          room: formUpdateData.room,
        })
        .then(({ data: response }) => {
          message.success(response.message || 'Cập nhật buổi học thành công')
          isUpdateModalVisible.value = false
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
      link.download = `lich_day__${formatDate(computedStartDate.value, 'dd-MM-yyyy')}__${formatDate(computedEndDate.value, 'dd-MM-yyyy')}.pdf`
      link.click()
      message.success('Xuất file PDF thành công')
    })
    .catch((error) => {
      message.error(error.response?.data?.message || 'Lỗi khi xuất file PDF')
    })
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
function handleChangeType(record, room = '') {
  loadingStore.show()
  const id = record.idPlanDate
  requestAPI
    .put(`${API_ROUTES_TEACHER.FETCH_DATA_SCHEDULE}/change-type/${id}`, null, { params: { room } })
    .then(({ data: response }) => {
      message.success(response.message || 'Đã đổi hình thức ca học')
      fetchTeachingSchedule()
      fetchTeachingSchedulePresent()
    })
    .catch((error) => {
      message.error(error.response?.data?.message || 'Lỗi đổi hình thức')
    })
    .finally(() => loadingStore.hide())
}

const showLinkModal = ref(false)
const pendingRecord = ref(null)
const linkInput = ref('')
const showRoomModal = ref(false)
const roomInput = ref('')
const pendingChangeRecord = ref(null)
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
        // tắt → mở modal nhập phòng
        pendingChangeRecord.value = record
        roomInput.value = record.room || ''
        showRoomModal.value = true
      }
    },
  })
}
// 3) Khi confirm chuyển về Offline: gọi change-type với roomInput.value
function confirmRoomModal() {
  if (!roomInput.value) {
    return message.error('Vui lòng nhập phòng học!')
  }
  showRoomModal.value = false
  loadingStore.show()
  // Gọi chung hàm, ghi đè room mới
  handleChangeType(pendingChangeRecord.value, roomInput.value)
}
// khi user confirm nhập link
function confirmLinkModal() {
  if (!linkInput.value) {
    return message.error('Vui lòng nhập link học!')
  }
  showLinkModal.value = false
  loadingStore.show()
  // 1) Cập nhật link
  requestAPI
    .put(API_ROUTES_TEACHER.FETCH_DATA_SCHEDULE, {
      idPlanDate: pendingRecord.value.idPlanDate,
      link: linkInput.value,
      description: pendingRecord.value.description,
      lateArrival: pendingRecord.value.lateArrival,
      room: pendingRecord.value.room, // giữ nguyên phòng cũ trên update nếu cần
    })
    // 2) Sau đó toggle sang ONLINE và clear room bằng ''
    .then(() => handleChangeType(pendingRecord.value, ''))
    .then(() => {
      message.success('Chuyển Online thành công với link mới')
      fetchTeachingSchedule()
      fetchTeachingSchedulePresent()
    })
    .catch((error) => {
      message.error(error.response?.data?.message || 'Lỗi khi cập nhật link/kiểu học')
    })
    .finally(() => loadingStore.hide())
}

const handleClearFilter = () => {
  // Clear all filter values except durationOption
  Object.keys(filter).forEach((key) => {
    if (key !== 'durationOption' && key !== 'page' && key !== 'pageSize') {
      filter[key] = ''
    }
  })
  pagination.value.current = 1
  fetchTeachingSchedule()
}

const handleShowFactory = (item) => {
  applicationStore.setSelectedKeys(
    TeacherRoutes[0].children.find((o) => o.name == ROUTE_NAMES.MANAGEMENT_FACTORY).meta
      .selectedKey,
  )
  router.push({ name: ROUTE_NAMES.MANAGEMENT_SHIFT_FACTORY, params: { id: item.factoryId } })
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
            :scroll="{ x: 'auto' }"
          >
            <template #bodyCell="{ column, record, index }">
              <template v-if="column.key === 'time'">
                {{
                  `${formatDate(record.startTeaching, 'HH:mm')} - ${formatDate(
                    record.endTeaching,
                    'HH:mm',
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
                      DEFAULT_DATE_FORMAT,
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
                <template v-else-if="column.dataIndex === 'factoryName'">
                  <a-typography-link @click="handleShowFactory(record)">{{
                    record.factoryName
                  }}</a-typography-link>
                </template>
                <template v-else-if="column.dataIndex === 'description'">
                  <a-tooltip v-if="record.description" title="Xem, sửa chi tiết buổi dạy">
                    <a-typography-link @click="handleShowDescription(record)"
                      >Chi tiết</a-typography-link
                    >
                  </a-tooltip>
                  <span v-else>Không có mô tả</span>
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
                <a-button type="primary" @click="handleExportPDF" :loading="isLoading">
                  <DownloadOutlined /> Xuất PDF
                </a-button>
              </div>
            </div>
          </template>

          <a-collapse ghost>
            <a-collapse-panel>
              <template #header><FilterFilled /> Bộ lọc</template>
              <div class="row g-3">
                <div class="col-md-4 col-sm-6">
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
                <div class="col-md-4 col-sm-6">
                  <a-select
                    v-model:value="filter.shiftType"
                    placeholder="Chọn hình thức học"
                    allowClear
                    class="w-100"
                    @change="fetchTeachingSchedule"
                  >
                    <a-select-option :value="''">Tất cả hình thức học</a-select-option>
                    <a-select-option value="1">Online</a-select-option>
                    <a-select-option value="0">Offline</a-select-option>
                  </a-select>
                </div>
                <div class="col-md-4 col-sm-12">
                  <div class="d-flex justify-content-center justify-content-md-start gap-2">
                    <a-button class="btn-light" @click="fetchTeachingSchedule">
                      <FilterFilled /> Lọc
                    </a-button>
                    <a-button class="btn-gray" @click="handleClearFilter"> Huỷ lọc </a-button>
                  </div>
                </div>
              </div>
            </a-collapse-panel>
          </a-collapse>

          <a-table
            :dataSource="teachingScheduleRecords"
            :columns="columns"
            :rowKey="(r) => r.idPlanDate"
            :pagination="pagination"
            @change="handleTableChange"
            :loading="isLoading"
            class="nowrap"
            :scroll="{ x: 'auto' }"
          >
            <template #bodyCell="{ column, record, index }">
              <!-- Xử lý cột Thời gian -->
              <template v-if="column.key === 'time'">
                {{
                  `${formatDate(record.startTeaching, 'HH:mm')} - ${formatDate(
                    record.endTeaching,
                    'HH:mm',
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
                      DEFAULT_DATE_FORMAT,
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
                  <a-tooltip v-if="record.description" title="Xem, sửa chi tiết buổi dạy">
                    <a-typography-link @click="handleShowDescription(record)"
                      >Chi tiết</a-typography-link
                    >
                  </a-tooltip>
                  <span v-else>Không có mô tả</span>
                </template>
                <template v-else-if="column.dataIndex === 'factoryName'">
                  <a-typography-link @click="handleShowFactory(record)">{{
                    record.factoryName
                  }}</a-typography-link>
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
          <a-textarea
            v-model:value="formUpdateData.description"
            rows="4"
            class="w-100"
            placeholder="Không có mô tả"
          />
        </a-form-item>
        <a-form-item label="Điểm danh muộn" name="lateArrival">
          <a-input-number
            v-model:value="formUpdateData.lateArrival"
            :min="0"
            class="w-100"
            @keyup.enter="handleUpdatePlanDate"
          />
        </a-form-item>
        <a-form-item label="Link học" name="link">
          <a-input
            v-model:value="formUpdateData.link"
            placeholder="https://"
            class="w-100"
            allowClear
            @keyup.enter="handleUpdatePlanDate"
          />
        </a-form-item>
        <a-form-item label="Địa điểm học" name="room">
          <a-input
            v-model:value="formUpdateData.room"
            placeholder="Nhập phòng học"
            class="w-100"
            @keyup.enter="handleUpdatePlanDate"
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
      :okButtonProps="{ loading: isLoading }"
    >
      <a-form layout="vertical">
        <a-form-item label="Link học">
          <a-input v-model:value="linkInput" placeholder="https://" />
        </a-form-item>
      </a-form>
    </a-modal>

    <a-modal
      v-model:open="showRoomModal"
      title="Nhập phòng học để chuyển sang Offline"
      @ok="confirmRoomModal"
      @cancel="showRoomModal = false"
      :okButtonProps="{ loading: isLoading }"
    >
      <a-form layout="vertical">
        <a-form-item label="Phòng học">
          <a-input v-model:value="roomInput" placeholder="Nhập phòng học" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>
