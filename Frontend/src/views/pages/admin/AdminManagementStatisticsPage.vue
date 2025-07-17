<script setup>
import { ref, onMounted, watch, reactive } from 'vue'
import {
  BookOutlined,
  UserOutlined,
  TeamOutlined,
  ProjectOutlined,
  DatabaseOutlined,
  FilterFilled,
} from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import requestAPI from '@/services/requestApiService'
import { API_ROUTES_ADMIN } from '@/constants/adminConstant'
import { GLOBAL_ROUTE_NAMES } from '@/constants/routesConstant'
import { ROUTE_NAMES } from '@/router/adminRoute'
import useBreadcrumbStore from '@/stores/useBreadCrumbStore'

import useLoadingStore from '@/stores/useLoadingStore'
import WidgetCounter from '@/components/widgets/WidgetCounter.vue'
import ChartBar from '@/components/charts/ChartBar.vue'
import DoughnutChart from '@/components/charts/DoughnutChart.vue'
import ChartLine from '@/components/charts/ChartLine.vue'

const breadcrumbStore = useBreadcrumbStore()
const loadingStore = useLoadingStore()

const dataStats = reactive({
  subject: 0,
  totalProject: 0,
  teacher: 0,
  staff: 0,
})

import dayjs from 'dayjs'

// Lấy năm hiện tại
const currentYear = new Date().getFullYear()

const dataFilter = reactive({
  fromDay: null,
  toDay: null,
  year: dayjs().year(currentYear),
})

const stats = ref([
  {
    title: 'Bộ môn',
    value: 0,
    icon: BookOutlined,
    class: 'bg-info',
  },
  {
    title: 'Dự án',
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

// Doughnut chart data for subject and project overview
const doughnutChartData = ref({
  labels: ['Bộ môn', 'Dự án'],
  datasets: [
    {
      label: 'Tổng quan bộ môn và dự án',
      backgroundColor: ['#1890FF', '#B37FEB'],
      borderColor: ['#fff', '#fff'],
      borderWidth: 2,
      data: [0, 0],
    },
  ],
})

// Line chart data for semester statistics
const lineChartData = ref({
  labels: [],
  datasets: [
    {
      label: 'Tỷ lệ điểm danh (%)',
      tension: 0.2,
      borderWidth: 3,
      pointRadius: 6,
      borderColor: '#1890ff',
      backgroundColor: 'rgba(24, 144, 255, 0.1)',
      data: [],
      yAxisID: 'y',
    },
    {
      label: 'Tổng xưởng',
      tension: 0.2,
      borderWidth: 3,
      pointRadius: 6,
      borderColor: '#b37feb',
      backgroundColor: 'rgba(82, 196, 26, 0.1)',
      data: [],
      yAxisID: 'y',
    },
    {
      label: 'Tổng sinh viên',
      tension: 0.2,
      borderWidth: 3,
      pointRadius: 6,
      borderColor: '#52c41a',
      backgroundColor: 'rgba(250, 173, 20, 0.1)',
      data: [],
      yAxisID: 'y',
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

const fetchDataAllStats = () => {
  loadingStore.show()

  requestAPI
    .get(`${API_ROUTES_ADMIN.FETCH_DATA_STATISTICS}`).then(({ data: response }) => {
      // Update statistics from statisticsStatResponse
      if (response.data.statisticsStatResponse) {
        Object.assign(dataStats, response.data.statisticsStatResponse)
      }
      // Update chart data from subjectFacilityChartResponse
      const subjectFacilityData = response.data.subjectFacilityChartResponse || []
      barChartData.value.labels = subjectFacilityData.map((o) => o.facilityName)
      barChartData.value.datasets[0].data = subjectFacilityData.map((o) => o.totalSubjectFacility)

      const totalProjectAndSubject = response.data.totalProjectAndSubjectResponse || {}
      const totalSubject = totalProjectAndSubject.totalSubject || 0
      const totalProject = totalProjectAndSubject.totalProject || 0

      // Update doughnut chart data with subject and project totals
      doughnutChartData.value.datasets[0].data = [totalSubject, totalProject]
    })
    .catch((error) => {
      message.error(error?.response?.data?.message || 'Không thể tải dữ liệu thống kê')
    })
    .finally(() => {
      loadingStore.hide()
    })
}

const fetchDataLineChart = () => {
  loadingStore.show()
  requestAPI.get(`${API_ROUTES_ADMIN.FETCH_DATA_STATISTICS}/attendance-percentage`, {
    params: {
      year: dataFilter.year
        ? (typeof dataFilter.year === 'string'
            ? parseInt(dataFilter.year, 10)
            : dataFilter.year.year
              ? dataFilter.year.year()
              : currentYear)
        : currentYear,
    },
  }).then(({ data: response }) => {
    const data = response.data || []
    lineChartData.value.labels = data.map(item => item.code)
    lineChartData.value.datasets[0].data = data.map(item => item.attendancePercentage || 0)
    lineChartData.value.datasets[1].data = data.map(item => item.totalFactory || 0)
    lineChartData.value.datasets[2].data = data.map(item => item.totalStudentFactory || 0)
  })
  .catch((error) => {
    message.error(error?.response?.data?.message || 'Không thể tải dữ liệu thống kê')
  })
  .finally(() => {
    loadingStore.hide()
  })
}

onMounted(() => {
  breadcrumbStore.setRoutes(breadcrumb.value)
  fetchDataAllStats()
  fetchDataLineChart()
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
      </div>
    </div>

    <div class="row g-3 mt-3">
      <!-- Chart Section -->
      <div class="col-xl-6 col-lg-6 col-md-12 col-sm-12">
        <a-card :bordered="false" class="dashboard-bar-line header-solid">
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

      <!-- Doughnut Chart Section -->
      <div class="col-xl-6 col-lg-6 col-md-12 col-sm-12">
        <a-card :bordered="false" class="dashboard-bar-line header-solid">
          <template #title>
            <div class="d-flex align-items-center">
              <ProjectOutlined class="me-2 text-primary" />
              <span>Tổng quan bộ môn và dự án</span>
            </div>
          </template>
          <template #extra>
            <a-tag color="blue" class="me-2">Bộ môn</a-tag>
            <a-tag color="purple" class="me-2">Dự án</a-tag>
          </template>
          <DoughnutChart :height="310" :data="doughnutChartData" />
        </a-card>
      </div>

      <!-- Filter Section chỉ cho line chart -->
      <div class="col-12 mt-4 mb-2">
        <div class="d-flex align-items-center gap-3">
          <a-date-picker
            v-model:value="dataFilter.year"
            picker="year"
            placeholder="Chọn năm"
            style="width: 150px"
            format="YYYY"
            value-format="YYYY"
            :allow-clear="false"
            @change="fetchDataLineChart"
          />
          <a-button type="primary" @click="fetchDataLineChart">
            <FilterFilled /> Lọc
          </a-button>
          <a-button @click="() => { dataFilter.year = dayjs().year(currentYear); fetchDataLineChart(); }">Hủy lọc</a-button>
        </div>
      </div>

      <!-- Line Chart Section -->
      <div class="col-xl-12 col-lg-12 col-md-12 col-sm-12">
        <a-card :bordered="false" class="dashboard-bar-line header-solid">
          <template #title>
            <div class="d-flex align-items-center">
              <ProjectOutlined class="me-2 text-primary" />
              <span>Thống kê chấm công, điểm danh sinh viên</span>
            </div>
          </template>
          <template #extra>
            <a-badge color="primary" class="badge-dot-primary" text="Tỷ lệ điểm danh (%)" />
            <a-badge color="success" class="badge-dot-secondary" text="Tổng xưởng" />
            <a-badge color="warning" class="badge-dot-success" text="Tổng sinh viên" />
          </template>
          <ChartLine :height="320" :data="lineChartData" />
        </a-card>
      </div>
    </div>
  </div>
</template>

