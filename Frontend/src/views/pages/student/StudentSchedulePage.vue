<script setup>
import { GLOBAL_ROUTE_NAMES } from '@/constants/routesConstant'
import { ROUTE_NAMES } from '@/router/studentRoute'
import { API_ROUTES_STUDENT } from '@/constants/studentConstant'
import requestAPI from '@/services/requestApiService'
import useBreadcrumbStore from '@/stores/useBreadCrumbStore'
import { DEFAULT_PAGINATION, TYPE_SHIFT } from '@/constants'
import { onMounted, ref } from 'vue'
import useLoadingStore from '@/stores/useLoadingStore'
import { Modal, message } from 'ant-design-vue'
import * as XLSX from 'xlsx'
import jsPDF from 'jspdf'
import autoTable from 'jspdf-autotable'

const breadcrumbStore = useBreadcrumbStore()

const breadcrumb = ref([
  {
    name: GLOBAL_ROUTE_NAMES.STAFF_PAGE,
    breadcrumbName: 'Sinh viên',
  },
  {
    name: ROUTE_NAMES.SCHEDULE,
    breadcrumbName: 'Lịch học',
  },
])
const loadingStore = useLoadingStore()
const isLoading = ref(false)

const attendanceList = ref([])
const filter = ref({ page: 1, pageSize: 5, plan: '' })
const pagination = ref({ ...DEFAULT_PAGINATION })

const columns = [
  { title: '#', dataIndex: 'indexs', key: 'indexs' },
  { title: 'Ngày điểm danh', dataIndex: 'attendanceDay', key: 'attendanceDay' },
  { title: 'Ca', dataIndex: 'shift', key: 'shift' },
  { title: 'Nhóm xưởng', dataIndex: 'factoryName', key: 'factoryName' },
  { title: 'Dự án', dataIndex: 'projectName', key: 'projectName' },
  { title: 'Tên môn học', dataIndex: 'subjectName', key: 'subjectName' },
  { title: 'Tên giảng viên', dataIndex: 'staffName', key: 'staffName' },
  { title: 'Địa điểm', dataIndex: 'location', key: 'location', width: 100 },
  { title: 'Link học', dataIndex: 'link', key: 'link', width: 180 },
  { title: 'Mô tả', dataIndex: 'description', key: 'description' },
]

const getCurrentTimestamp = () => Date.now()

const convertTime = () => {
  const currentTimestamp = getCurrentTimestamp()
  const plan = filter.value.plan
  if (!plan) {
    return currentTimestamp + 7 * 24 * 60 * 60 * 1000
  }
  return currentTimestamp + plan * 24 * 60 * 60 * 1000
}

const fetchAttendanceList = () => {
  loadingStore.show()
  requestAPI
    .get(API_ROUTES_STUDENT.FETCH_DATA_STUDENT_PLAN + '/list', {
      params: {
        now: getCurrentTimestamp(),
        max: convertTime(),
        page: pagination.value.current,
        size: pagination.value.pageSize,
      },
    })
    .then(({ data }) => {
      attendanceList.value = data.data.data
      pagination.value.total = data.data.totalPages * filter.value.pageSize
      pagination.value.current = filter.value.page
    })
    .catch(() => {
      message.error('Lỗi khi lấy dữ liệu')
    })
    .finally(() => {
      loadingStore.hide()
    })
}

const handleTableChange = (pageInfo) => {
  pagination.value.current = pageInfo.current
  pagination.value.pageSize = pageInfo.pageSize
  filter.value.page = pageInfo.current
  filter.value.pageSize = pageInfo.pageSize
  fetchAttendanceList()
}
const handleShowDescription = (text) => {
  Modal.info({
    title: 'Nội dung buổi học',
    type: 'info',
    content: text || 'Buổi học chưa có nội dung',
    okText: 'Đóng',
    okButtonProps: {
      class: 'btn-gray',
    },
  })
}
const formatDate = (timestamp) => {
  if (!timestamp) return 'Không xác định'
  return new Date(timestamp).toLocaleString()
}
const exportToExcel = () => {
  const worksheet = XLSX.utils.json_to_sheet(
    attendanceList.value.map((item, index) => ({
      STT: index + 1,
      'Ngày điểm danh': formatDate(item.attendanceDay),
      Ca: 'Ca ' + item.shift,
      'Nhóm xưởng': item.factoryName,
      'Dự án': item.projectName,
      'Tên môn học': item.subjectName,
      'Tên giảng viên': item.staffName,
      'Mô tả': item.description || '',
    }))
  )
  const workbook = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(workbook, worksheet, 'DanhSach')
  XLSX.writeFile(workbook, 'DiemDanh.xlsx')
}

const exportToPDF = () => {
  const doc = new jsPDF()
  const rows = attendanceList.value.map((item, index) => [
    index + 1,
    formatDate(item.attendanceDay),
    'Ca ' + item.shift,
    item.factoryName,
    item.projectName,
    item.subjectName,
    item.staffName,
    item.description || '',
  ])
  autoTable(doc, {
    head: [
      ['STT', 'Ngày điểm danh', 'Ca', 'Nhóm xưởng', 'Dự án', 'Môn học', 'Giảng viên', 'Mô tả'],
    ],
    body: rows,
    startY: 20,
  })
  doc.save('DiemDanh.pdf')
}

onMounted(() => {
  filter.value.plan = 7
  breadcrumbStore.setRoutes(breadcrumb.value)
  fetchAttendanceList()
})
</script>

<template>
  <div class="container-fluid">
    <div class="row g-3">
      <div class="col-12">
        <div class="container-fluid">
          <a-card title="Bộ lọc" :bordered="false" class="cart mb-3">
            <div class="label-title">Thời gian:</div>
            <a-row :gutter="16" class="filter-container">
              <a-col :span="24">
                <a-select
                  v-model:value="filter.plan"
                  placeholder="Chọn trạng thái"
                  allowClear
                  style="width: 100%"
                  @change="fetchAttendanceList"
                >
                  <a-select-option :value="-90">90 ngày trước</a-select-option>
                  <a-select-option :value="-30">30 ngày trước</a-select-option>
                  <a-select-option :value="-14">14 ngày trước</a-select-option>
                  <a-select-option :value="-7">7 ngày trước</a-select-option>
                  <a-select-option :value="7">7 ngày tới</a-select-option>
                  <a-select-option :value="14">14 ngày tới</a-select-option>
                  <a-select-option :value="30">30 ngày tới</a-select-option>
                  <a-select-option :value="90">90 ngày tới</a-select-option>
                </a-select>
              </a-col>
            </a-row>
          </a-card>

          <a-card title="Danh sách điểm danh" :bordered="false" class="cart">
            <div class="d-flex justify-content-end mb-3">
              <a-button type="primary" @click="exportToExcel" class="me-3"
                >Tải xuống Excel</a-button
              >
              <a-button type="default" @click="exportToPDF">Tải xuống PDF</a-button>
            </div>

            <a-table
              :dataSource="attendanceList"
              :columns="columns"
              :rowKey="'id'"
              :loading="isLoading"
              :scroll="{ y: 500, x: 'auto' }"
              :pagination="pagination"
              class="nowrap"
              @change="handleTableChange"
            >
              <template #bodyCell="{ column, record }">
                <template v-if="column.dataIndex === 'attendanceDay'">{{
                  formatDate(record.attendanceDay)
                }}</template>
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
                <template v-if="column.dataIndex === 'link'">
                  <a v-if="record.link" :href="record.link" target="_blank">{{ record.link }}</a>
                </template>
                <template v-if="column.dataIndex === 'description'">
                  <a-typography-link @click="handleShowDescription(record.description)"
                    >Chi tiết</a-typography-link
                  >
                </template>
                <template v-if="column.dataIndex === 'staffName'">
                  <a-tag color="lime">{{ record.staffName }}</a-tag></template
                >
                <template v-if="column.dataIndex === 'factoryName'">
                  <a-badge status="processing" :text="record.factoryName" />
                </template>
              </template>
            </a-table>
          </a-card>
        </div>
      </div>
    </div>
  </div>
</template>
