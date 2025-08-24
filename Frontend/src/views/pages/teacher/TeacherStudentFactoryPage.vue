<script setup>
import {
  FilterFilled,
  InfoCircleOutlined,
  SearchOutlined,
  UnorderedListOutlined,
} from '@ant-design/icons-vue'
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { useRoute } from 'vue-router'
import requestAPI from '@/services/requestApiService'
import { API_ROUTES_TEACHER } from '@/constants/teacherConstant'
import { ROUTE_NAMES } from '@/router/teacherRoute'
import { GLOBAL_ROUTE_NAMES } from '@/constants/routesConstant'
import useBreadcrumbStore from '@/stores/useBreadCrumbStore'
import useLoadingStore from '@/stores/useLoadingStore'
import {
  ATTENDANCE_STATUS,
  DEFAULT_DATE_FORMAT,
  DEFAULT_PAGINATION,
  STATUS_REQUIRED_ATTENDANCE,
} from '@/constants'
import { autoAddColumnWidth, dayOfWeek, formatDate } from '@/utils/utils'

const route = useRoute()
const factoryId = route.query.factoryId
const factoryName = route.query.factoryName

const breadcrumbStore = useBreadcrumbStore()
const isShowModalDetail = ref(false)
const detailAttendance = ref(null)
const lstAttendance = ref([])

const isLoading = ref(false)
const isLoadingDetail = ref(false)

const breadcrumb = ref([
  {
    name: GLOBAL_ROUTE_NAMES.TEACHER_PAGE,
    breadcrumbName: 'Giảng viên',
  },
  {
    name: ROUTE_NAMES.MANAGEMENT_FACTORY,
    breadcrumbName: 'Nhóm xưởng của tôi',
  },
  {
    name: ROUTE_NAMES.MANAGEMENT_STUDENT_FACTORY,
    breadcrumbName: 'Sinh viên nhóm: ' + factoryName,
  },
])

const students = ref([])
const filter = reactive({
  searchQuery: '',
  status: '',
  factoryId: factoryId, // từ query string
})
const pagination = reactive({
  ...DEFAULT_PAGINATION,
})

// Cấu hình cột cho bảng
const columns = ref(
  autoAddColumnWidth([
    { title: '#', dataIndex: 'rowNumber', key: 'rowNumber' },
    {
      title: 'Mã sinh viên',
      dataIndex: 'studentCode',
      key: 'studentCode',
    },
    {
      title: 'Tên sinh viên ',
      dataIndex: 'studentName',
      key: 'studentName',
    },
    { title: 'Email', dataIndex: 'studentEmail', key: 'studentEmail' },
    {
      title: 'Checkin/checkout bù',
      dataIndex: 'lateAttendance',
      key: 'lateAttendance',
      align: 'center',
    },
    {
      title: 'Số ca vắng mặt',
      dataIndex: 'totalAbsentShift',
      key: 'totalAbsentShift',
      align: 'center',
    },
    {
      title: 'Tỉ lệ vắng mặt',
      dataIndex: 'percenAbsentShift',
      key: 'percenAbsentShift',
      align: 'center',
    },
    {
      title: 'Chi tiết điểm danh',
      dataIndex: 'detailAttendance',
      key: 'detailAttendance',
      align: 'center',
    },
    {
      title: 'Trạng thái',
      dataIndex: 'statusStudentFactory',
      key: 'statusStudentFactory',
    },
  ]),
)

const countFilter = ref(0)

const columnsDetail = ref(
  autoAddColumnWidth([
    { title: '#', dataIndex: 'orderNumber', key: 'orderNumber' },
    { title: 'Ngày điểm danh', dataIndex: 'startDate', key: 'startDate' },
    { title: 'Thời gian', dataIndex: 'time', key: 'time' },
    { title: 'Ca', dataIndex: 'shift', key: 'shift' },
    { title: 'Checkin đầu giờ', dataIndex: 'createdAt', key: 'createdAt' },
    { title: 'Checkout cuối giờ', dataIndex: 'updatedAt', key: 'updatedAt' },
    { title: 'Trạng thái', dataIndex: 'status', key: 'status' },
  ]),
)

const loadingStore = useLoadingStore()

const fetchStudentFactory = () => {
  loadingStore.show()
  requestAPI
    .get(API_ROUTES_TEACHER.FETCH_DATA_STUDENT_FACTORY, {
      params: {
        ...filter,
        page: pagination.current,
        size: pagination.pageSize,
      },
    })
    .then((response) => {
      const result = response.data.data
      students.value = result.data
      // Nếu API trả về tổng số trang, sử dụng:
      pagination.total = result.totalItems

      countFilter.value = result.totalItems
    })
    .catch((error) => {
      message.error(error.data?.message || 'Lỗi khi lấy dữ liệu')
    })
    .finally(() => {
      loadingStore.hide()
    })
}

// Xử lý thay đổi trang bảng
const handleTableChange = (pageInfo) => {
  pagination.current = pageInfo.current
  pagination.pageSize = pageInfo.pageSize
  fetchStudentFactory()
}

const handleClearFilter = () => {
  // Preserve factoryId and reset only search filters
  filter.searchQuery = ''
  filter.status = ''
  handleSubmitFilter()
}

const handleSubmitFilter = () => {
  pagination.current = 1
  fetchStudentFactory()
}

const handleShowDetailAttendance = (item) => {
  detailAttendance.value = item
  isShowModalDetail.value = true
  fetchDataDetailAttendance(item.studentId)
}

const fetchDataDetailAttendance = (idUserStudent) => {
  isLoadingDetail.value = true
  requestAPI
    .post(API_ROUTES_TEACHER.FETCH_DATA_STUDENT_FACTORY, {
      idUserStudent: idUserStudent,
      idFactory: route.query.factoryId,
    })
    .then(({ data: response }) => {
      lstAttendance.value = response.data
    })
    .catch((error) => {
      message.error(error?.response?.data?.message || 'Không thể tải thông tin chi tiết điểm danh')
    })
    .finally(() => {
      isLoadingDetail.value = false
    })
}

onMounted(() => {
  breadcrumbStore.setRoutes(breadcrumb.value)
  fetchStudentFactory()
})
</script>

<template>
  <a-modal v-model:open="isShowModalDetail" :width="1000" :footer="null">
    <template #title
      ><InfoCircleOutlined class="text-primary" /> Chi tiết điểm danh:
      {{ detailAttendance.studentCode }} - {{ detailAttendance.studentName }}</template
    >
    <div class="row g-2">
      <div class="col-12">
        <a-table
          rowKey="id"
          class="nowrap"
          :dataSource="lstAttendance"
          :columns="columnsDetail"
          :loading="isLoadingDetail"
          :pagination="false"
          :scroll="{ x: 'auto' }"
        >
          <template #bodyCell="{ column, record, index }">
            <template v-if="column.dataIndex === 'orderNumber'">
              {{ index + 1 }}
            </template>
            <template v-if="column.dataIndex === 'startDate'">
              {{
                `${dayOfWeek(record.startDate)}, ${formatDate(
                  record.startDate,
                  DEFAULT_DATE_FORMAT,
                )}`
              }}
            </template>
            <template v-if="column.dataIndex === 'time'">
              {{
                `${formatDate(record.startDate, 'HH:mm')} - ${formatDate(record.endDate, 'HH:mm')}`
              }}
            </template>
            <template v-if="column.dataIndex === 'shift'">
              <a-tag color="purple">
                {{
                  `Ca ${record.shift
                    .split(',')
                    .map((o) => Number(o))
                    .join(', ')}`
                }}
              </a-tag>
            </template>
            <template v-if="column.dataIndex === 'createdAt'">
              <template v-if="record.startDate <= Date.now()">
                <template v-if="record.requiredCheckin === STATUS_REQUIRED_ATTENDANCE.ENABLE">
                  <span v-if="record.status === ATTENDANCE_STATUS.ABSENT.id">
                    <a-badge status="default" /> Đã huỷ checkin
                  </span>
                  <span v-else-if="record.status === ATTENDANCE_STATUS.NOTCHECKIN.id">
                    <a-badge status="error" /> Chưa checkin
                  </span>
                  <span v-else>
                    <template v-if="record.lateCheckin">
                      <a-badge status="warning" /> Checkin bù -
                    </template>
                    <a-badge v-else status="success" />
                    {{ formatDate(record.createdAt, 'dd/MM/yyyy HH:mm') }}
                  </span>
                </template>
                <template v-else>
                  <a-badge status="default" />
                  Không yêu cầu
                </template>
              </template>
              <template v-else><a-badge status="default" /> Chưa diễn ra</template>
            </template>
            <template v-if="column.dataIndex === 'updatedAt'">
              <template v-if="record.startDate <= Date.now()">
                <template v-if="record.requiredCheckout === STATUS_REQUIRED_ATTENDANCE.ENABLE">
                  <span v-if="record.status === ATTENDANCE_STATUS.ABSENT.id">
                    <a-badge status="default" /> Đã huỷ checkout
                  </span>
                  <span v-else-if="record.status !== ATTENDANCE_STATUS.PRESENT.id">
                    <a-badge status="error" /> Chưa checkout
                  </span>
                  <span v-else>
                    <template v-if="record.lateCheckout">
                      <a-badge status="warning" /> Checkout bù -
                    </template>
                    <a-badge v-else status="success" />
                    {{ formatDate(record.updatedAt, 'dd/MM/yyyy HH:mm') }}
                  </span>
                </template>
                <template v-else>
                  <a-badge status="default" />
                  Không yêu cầu
                </template>
              </template>
              <template v-else>{{ null }}</template>
            </template>
            <template v-if="column.dataIndex === 'status'">
              <template v-if="record.startDate <= Date.now()">
                <a-tag color="green" v-if="record.status === ATTENDANCE_STATUS.PRESENT.id">
                  Có mặt
                </a-tag>
                <a-tag color="red" v-else> Vắng mặt </a-tag>
              </template>
              <template v-else>{{ null }}</template>
            </template>
          </template>
        </a-table>
      </div>
    </div>
  </a-modal>

  <div class="container-fluid">
    <div class="row g-3">
      <div class="col-12">
        <a-card :bordered="false" class="cart no-body-padding">
          <a-collapse ghost>
            <a-collapse-panel>
              <template #header><FilterFilled /> Bộ lọc ({{ countFilter }})</template>
              <div class="row g-3 filter-container">
                <div class="col-md-6 col-sm-6">
                  <div class="label-title">Từ khoá:</div>
                  <a-input
                    v-model:value="filter.searchQuery"
                    placeholder="Nhập mã, tên hoặc email sinh viên"
                    allowClear
                    @change="handleSubmitFilter"
                  >
                    <template #prefix>
                      <SearchOutlined />
                    </template>
                  </a-input>
                </div>
                <div class="col-md-6 col-sm-6">
                  <div class="label-title">Trạng thái:</div>
                  <a-select
                    v-model:value="filter.status"
                    placeholder="-- Tất cả trạng thái --"
                    class="w-100"
                    @change="handleSubmitFilter"
                  >
                    <a-select-option :value="''">-- Tất cả trạng thái --</a-select-option>
                    <a-select-option value="1">Đang hoạt động</a-select-option>
                    <a-select-option value="0">Ngưng hoạt động</a-select-option>
                  </a-select>
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
          <template #title> <UnorderedListOutlined /> Danh sách sinh viên </template>

          <a-table
            class="nowrap"
            :loading="isLoading"
            :dataSource="students"
            :columns="columns"
            rowKey="studentFactoryId"
            :pagination="pagination"
            @change="handleTableChange"
            :scroll="{ x: 'auto' }"
          >
            <template #bodyCell="{ column, record, index }">
              <template v-if="column.dataIndex">
                <!-- STT -->
                <template v-if="column.dataIndex === 'rowNumber'">
                  {{ (pagination.current - 1) * pagination.pageSize + index + 1 }}
                </template>
                <!-- Hiển thị trạng thái -->
                <template v-else-if="column.dataIndex === 'statusStudentFactory'">
                  <a-tag
                    :color="
                      record.statusStudentFactory === 'ACTIVE' || record.statusStudentFactory === 1
                        ? 'green'
                        : 'red'
                    "
                  >
                    {{
                      record.statusStudentFactory === 'ACTIVE' || record.statusStudentFactory === 1
                        ? 'Đang hoạt động'
                        : 'Ngưng hoạt động'
                    }}
                  </a-tag>
                </template>
                <template v-if="column.dataIndex === 'totalAbsentShift'">
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
                <template v-if="column.dataIndex === 'percenAbsentShift'">
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
                <template v-if="column.dataIndex === 'lateAttendance'">
                  <a-tag
                    :color="
                      record.totalLateAttendance > 0
                        ? record.currentLateAttendance >= record.totalLateAttendance
                          ? 'red'
                          : 'green'
                        : 'default'
                    "
                    >{{ record.currentLateAttendance || 0 }} /
                    {{ record.totalLateAttendance }} lần</a-tag
                  >
                </template>
                <template v-if="column.dataIndex === 'detailAttendance'">
                  <a-typography-link @click="handleShowDetailAttendance(record)"
                    >Chi tiết</a-typography-link
                  >
                </template>
              </template>
              <!-- Các nút hành động -->
            </template>
          </a-table>
        </a-card>
      </div>
    </div>
  </div>
</template>
