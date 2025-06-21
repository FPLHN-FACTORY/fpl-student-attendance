<script setup>
import { ref, onMounted, watch, reactive } from 'vue'
import {
  BookOutlined,
  UserOutlined,
  TeamOutlined,
  ProjectOutlined,
  UnorderedListOutlined,
  DatabaseOutlined,
} from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import requestAPI from '@/services/requestApiService'
import { DEFAULT_DATE_FORMAT } from '@/constants'
import { API_ROUTES_ADMIN } from '@/constants/adminConstant'
import { GLOBAL_ROUTE_NAMES } from '@/constants/routesConstant'
import { ROUTE_NAMES } from '@/router/adminRoute'
import useBreadcrumbStore from '@/stores/useBreadCrumbStore'
import { autoAddColumnWidth, debounce } from '@/utils/utils'
import useLoadingStore from '@/stores/useLoadingStore'
import WidgetCounter from '@/components/widgets/WidgetCounter.vue'
import ChartBar from '@/components/charts/ChartBar.vue'
import ChartLine from '@/components/charts/ChartLine.vue'
import dayjs from 'dayjs'

const breadcrumbStore = useBreadcrumbStore()
const loadingStore = useLoadingStore()

const dataStats = reactive({
  subject: 0,
  totalProject: 0,
  teacher: 0,
  staff: 0,
})

const lstData = ref([])

const pagination = reactive({
  current: 1,
  pageSize: 5,
  total: 0,
  showQuickJumper: true,
})

const dataFilter = reactive({
  fromDay: null,
  toDay: null,
})

const dataRangePresets = [
  {
    label: 'Tuần này',
    value: [dayjs().startOf('week'), dayjs().endOf('week')],
  },
  {
    label: 'Tuần trước',
    value: [dayjs().subtract(1, 'week').startOf('week'), dayjs().subtract(1, 'week').endOf('week')],
  },
  {
    label: 'Tháng này',
    value: [dayjs().startOf('month'), dayjs().endOf('month')],
  },
  {
    label: 'Tháng trước',
    value: [
      dayjs().subtract(1, 'month').startOf('month'),
      dayjs().subtract(1, 'month').endOf('month'),
    ],
  },
]

const stats = ref([
  {
    title: 'Bộ môn',
    value: 0,
    icon: BookOutlined,
    class: 'bg-info',
  },
  {
    title: 'Dự án hoàn thành',
    value: 0,
    icon: ProjectOutlined,
    class: 'bg-primary',
  },
  {
    title: 'Phụ trách xưởng',
    value: 0,
    icon: UserOutlined,
    class: 'bg-success',
  },
  {
    title: 'Giảng viên',
    value: 0,
    icon: TeamOutlined,
    class: 'bg-warning',
  },

])

const barChartData = ref({
  labels: [],
  datasets: [
    {
      label: 'Số bộ môn theo cơ sở',
      backgroundColor: '#fff',
      borderWidth: 0,
      borderSkipped: false,
      borderRadius: 6,
      data: [],
      maxBarThickness: 30,
    },
  ],
})

const lineChartData = ref({
  labels: [],
  datasets: [
    {
      label: 'Tổng bộ môn',
      tension: 0.4,
      borderWidth: 0,
      pointRadius: 0,
      borderColor: '#1890FF',
      borderWidth: 3,
      backgroundColor: 'rgba(24, 144, 255, 0.1)',
      data: [],
      maxBarThickness: 6,
    },
    {
      label: 'Tổng dự án',
      tension: 0.4,
      borderWidth: 0,
      pointRadius: 0,
      borderColor: '#B37FEB',
      borderWidth: 3,
      backgroundColor: 'rgba(179, 127, 235, 0.1)',
      data: [],
      maxBarThickness: 6,
    },
  ],
})

const breadcrumb = ref([
  {
    name: GLOBAL_ROUTE_NAMES.ADMIN_PAGE,
    breadcrumbName: 'Admin',
  },
  {
    name: ROUTE_NAMES.MANAGEMENT_STATISTICS,
    breadcrumbName: 'Thống kê',
  },
])

const columns = ref(
  autoAddColumnWidth([
    { title: '#', dataIndex: 'rowNumber', key: 'rowNumber'},
    { title: 'Tên bộ môn', dataIndex: 'subjectName', key: 'subjectName', width: 150 },
    { title: 'Dự án hoàn thành', dataIndex: 'doneProject', key: 'doneProject', width: 120 },
    { title: 'Dự án đang thực hiện', dataIndex: 'processingProject', key: 'processingProject', width: 130 },
    { title: 'Tổng dự án', dataIndex: 'total', key: 'total', width: 100 },
  ]),
)

const fetchDataAllStats = () => {
  loadingStore.show()
  const params = {
    fromDay: dataFilter.fromDay,
    toDay: dataFilter.toDay,
    pageNumber: pagination.current - 1, // Convert to 0-based indexing for backend
  }

  requestAPI
    .get(`${API_ROUTES_ADMIN.FETCH_DATA_STATISTICS}`, {
      params,
    }).then(({ data: response }) => {
      // Update statistics from statisticsStatResponse
      if (response.data.statisticsStatResponse) {
        Object.assign(dataStats, response.data.statisticsStatResponse)
      }      // Update chart data from subjectFacilityChartResponse
      const subjectFacilityData = response.data.subjectFacilityChartResponse || []
      barChartData.value.labels = subjectFacilityData.map((o) => o.facilityName)
      barChartData.value.datasets[0].data = subjectFacilityData.map((o) => o.totalSubjectFacility)      // Update line chart data using totalProjectAndSubjectResponse
      const totalProjectAndSubject = response.data.totalProjectAndSubjectResponse || {}
      const totalSubject = totalProjectAndSubject.totalSubject || 0
      const totalProject = totalProjectAndSubject.totalProject || 0

      // Simple line chart showing only two points: subjects and projects
      lineChartData.value.labels = ['', '', '']
      lineChartData.value.datasets[0].data = [0, totalSubject, 0]  // Only show subject value at first point
      lineChartData.value.datasets[1].data = [0, totalProject, 0]  // Only show project value at second point
      const projectSubjectData = response.data.projectSubjectFacilityResponses?.content || []
      const paginationInfo = response.data.projectSubjectFacilityResponses || {}

      lstData.value = projectSubjectData.map((item) => ({
        ...item,
        // Backend already provides rowNumber, no need to calculate
      }))

      // Update pagination information
      pagination.current = (paginationInfo.number || 0) + 1
      pagination.total = paginationInfo.totalElements || 0
      pagination.pageSize = paginationInfo.size || 5
    })
    .catch((error) => {
      message.error(error?.response?.data?.message || 'Không thể tải dữ liệu thống kê')
    })
    .finally(() => {
      loadingStore.hide()
    })
}

const handleClearFilter = () => {
  Object.assign(dataFilter, {
    fromDay: null,
    toDay: null,
  })
  pagination.current = 1 // Reset to first page when clearing filter
  fetchDataAllStats()
}

const handleSubmitFilter = () => {
  pagination.current = 1 // Reset to first page when filtering
  fetchDataAllStats()
}

const handlePaginationChange = (page, pageSize) => {
  pagination.current = page
  pagination.pageSize = pageSize
  fetchDataAllStats()
}

const handleDateRangeChange = (dates) => {
  if (dates && dates.length === 2) {
    dataFilter.fromDay = dates[0].valueOf()
    dataFilter.toDay = dates[1].valueOf()
  } else {
    dataFilter.fromDay = null
    dataFilter.toDay = null
  }
}

onMounted(() => {
  breadcrumbStore.setRoutes(breadcrumb.value)
  fetchDataAllStats()
})

watch(
  dataStats,
  (data) => {
    stats.value[0].value = data.subject || 0
    stats.value[1].value = data.totalProject || 0
    stats.value[2].value = data.staff || 0
    stats.value[3].value = data.teacher || 0
  },
  { deep: true },
)

const debounceFilter = debounce(handleSubmitFilter, 300)
watch(
  dataFilter,
  () => {
    debounceFilter()
  },
  { deep: true },
)
</script>

<template>
  <div class="container-fluid">
    <div class="row g-3">
      <!-- Statistics Cards -->
      <div class="col-xl-3 col-md-6 col-sm-6" v-for="(stat, index) in stats" :key="index">
        <WidgetCounter
          :title="stat.title"
          :value="stat.value"
          :prefix="stat.prefix"
          :suffix="stat.suffix"
          :icon="stat.icon"
          :status="stat.status"
        ></WidgetCounter>
      </div>      <!-- Chart Section -->
      <div class="col-xl-6 col-lg-6 col-md-12 col-sm-12">
        <a-card :bordered="false" class="dashboard-bar-chart">
          <template #title>
            <div class="d-flex align-items-center">
              <DatabaseOutlined class="me-2 text-primary" />
              <span>Thống kê bộ môn cơ sở</span>
            </div>
          </template>
          <template #extra>
            <a-tag color="blue" class="me-2">
              {{ barChartData.labels.length }} cơ sở
            </a-tag>
          </template>
          <ChartBar :height="310" :data="barChartData"></ChartBar>
        </a-card>
      </div>

      <!-- Line Chart Section -->
      <div class="col-xl-6 col-lg-6 col-md-12 col-sm-12">
        <a-card :bordered="false" class="dashboard-bar-line header-solid">
          <template #title>
            <div class="d-flex align-items-center">
              <ProjectOutlined class="me-2 text-primary" />
              <span>Tổng quan bộ môn và dự án</span>
            </div>
          </template>
          <template #extra>
            <a-badge color="primary" class="badge-dot-primary" text="Bộ môn" />
            <a-badge color="primary" class="badge-dot-secondary" text="Dự án" />
          </template>
          <ChartLine :height="310" :data="lineChartData" />
        </a-card>
      </div>

      <!-- Detailed Table -->
      <div class="col-xl-12 col-lg-12 col-md-12 col-sm-12">
        <a-card :bordered="false" class="cart card-white"><template #title>
            <div class="d-flex align-items-center">
              <UnorderedListOutlined class="me-2 text-primary" />
              <span>Thống kê dự án</span>
            </div>
          </template>
          <template #extra>
            <a-space>
              <a-tag color="processing">
                {{ pagination.total }} bộ môn
              </a-tag>              <a-tag color="success">
                {{ lstData.reduce((sum, item) => sum + item.doneProject + item.processingProject, 0) }} dự án (trang hiện tại)
              </a-tag>
            </a-space>
          </template>
          <div class="table-responsive">            <a-table
              rowKey="rowNumber"
              class="nowrap"
              :dataSource="lstData"
              :columns="columns"
              :pagination="{
                current: pagination.current,
                pageSize: pagination.pageSize,
                total: pagination.total,
                showSizeChanger: pagination.showSizeChanger,
                showQuickJumper: pagination.showQuickJumper,
                showTotal: pagination.showTotal,
                onChange: handlePaginationChange,
                onShowSizeChange: handlePaginationChange,
              }"
              :scroll="{ x: 'auto' }"
            >
              <template #bodyCell="{ column, record }">
                <template v-if="column.key === 'rowNumber'">
                  <span class="fw-medium text-muted">{{ record.rowNumber }}</span>
                </template>
                <template v-if="column.key === 'subjectName'">
                  <a-typography-link class="fw-medium">{{ record.subjectName }}</a-typography-link>
                </template>
                <template v-if="column.dataIndex === 'doneProject'">
                  <a-tag color="green" class="fw-medium">{{ record.doneProject }}</a-tag>
                </template>
                <template v-if="column.dataIndex === 'processingProject'">
                  <a-tag color="blue" class="fw-medium">{{ record.processingProject }}</a-tag>
                </template>
                <template v-if="column.dataIndex === 'total'">
                  <a-tag color="purple" class="fw-medium">{{ record.doneProject + record.processingProject }}</a-tag>
                </template>
              </template>
            </a-table>
          </div>
        </a-card>
      </div>
    </div>
  </div>
</template>
