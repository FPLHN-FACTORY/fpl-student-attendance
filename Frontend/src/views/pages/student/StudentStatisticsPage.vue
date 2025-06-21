<script setup>
import { ref, onMounted, watch, reactive } from 'vue'
import {
  FilterFilled,
  GoldOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
  SyncOutlined,
  BookOutlined,
  ProjectOutlined,
} from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import requestAPI from '@/services/requestApiService'
import { API_ROUTES_STUDENT } from '@/constants/studentConstant'
import { ROUTE_NAMES_API } from '@/router/authenticationRoute'
import { ROUTE_NAMES } from '@/router/studentRoute'
import { GLOBAL_ROUTE_NAMES } from '@/constants/routesConstant'
import useBreadcrumbStore from '@/stores/useBreadCrumbStore'
import { debounce, getCurrentSemester } from '@/utils/utils'
import useLoadingStore from '@/stores/useLoadingStore'
import WidgetCounter from '@/components/widgets/WidgetCounter.vue'
import ChartLine from '@/components/charts/ChartLine.vue'
import ChartBar from '@/components/charts/ChartBar.vue'

const breadcrumbStore = useBreadcrumbStore()
const loadingStore = useLoadingStore()

const dataStats = reactive({
  factory: 0,
  project: 0,
  pass: 0,
  fail: 0,
  process: 0,
})

const lstSemester = ref([])

const dataFilter = reactive({
  idSemester: null,
})

const stats = ref([
  {
    title: 'Nhóm xưởng',
    value: 0,
    icon: GoldOutlined,
    class: 'bg-primary',
  },
  {
    title: 'Hoàn thành',
    value: 0,
    icon: CheckCircleOutlined,
    class: 'bg-success',
  },
  {
    title: 'Không đạt',
    value: 0,
    icon: CloseCircleOutlined,
    class: 'bg-danger',
  },
  {
    title: 'Đang diễn ra',
    value: 0,
    icon: SyncOutlined,
    class: 'bg-warning',
  },
])

const lineChartData = ref({
  labels: [],
  datasets: [
    {
      label: 'Tỷ lệ điểm danh (%)',
      borderColor: '#52c41a',
      backgroundColor: 'rgba(82, 196, 26, 0.1)',
      fill: true,
      tension: 0.4,
      borderWidth: 3,
      pointBackgroundColor: '#52c41a',
      pointBorderColor: '#fff',
      pointBorderWidth: 2,
      pointRadius: 6,
      pointHoverRadius: 8,
      data: [],
    },
    {
      label: 'Tỷ lệ vắng (%)',
      borderColor: '#ff4d4f',
      backgroundColor: 'rgba(255, 77, 79, 0.1)',
      fill: true,
      tension: 0.4,
      borderWidth: 3,
      pointBackgroundColor: '#ff4d4f',
      pointBorderColor: '#fff',
      pointBorderWidth: 2,
      pointRadius: 6,
      pointHoverRadius: 8,
      data: [],
    },
  ],
})

const barChartData = ref({
  labels: [],
  datasets: [
    {
      label: 'Điểm danh',
      backgroundColor: '#52c41a',
      data: [],
      borderWidth: 0,
      borderRadius: 4,
    },
    {
      label: 'Vắng mặt',
      backgroundColor: '#ff4d4f',
      data: [],
      borderWidth: 0,
      borderRadius: 4,
    }
  ],
})

const breadcrumb = ref([
  {
    name: GLOBAL_ROUTE_NAMES.STUDENT_PAGE,
    breadcrumbName: 'Sinh viên',
  },
  {
    name: ROUTE_NAMES.STATISTICS,
    breadcrumbName: 'Thống kê cá nhân',
  },
])

const fetchDataAllStats = () => {
  loadingStore.show()
  requestAPI
    .get(`${API_ROUTES_STUDENT.FETCH_DATA_STATISTICS}`, {
      params: {
        idSemester: dataFilter.idSemester,
      },
    })
    .then(({ data: response }) => {
      // Update statistics from stdStatisticsStatResponse
      if (response.data.stdStatisticsStatResponse) {
        Object.assign(dataStats, response.data.stdStatisticsStatResponse)
      }      // Update chart data from factoryChartResponse
      const factoryChartData = response.data.factoryChartResponse || []
      lineChartData.value.labels = factoryChartData.map((o) => o.factoryName)
      lineChartData.value.datasets[0].data = factoryChartData.map((o) => o.attendancePercentage)
      lineChartData.value.datasets[1].data = factoryChartData.map((o) => o.absentPercentage)

      // Update bar chart data from factoryAttendanceChartResponse
      const factoryAttendanceData = response.data.factoryAttendanceChartResponse || []
      barChartData.value.labels = factoryAttendanceData.map((o) => o.factoryName)
      barChartData.value.datasets[0].data = factoryAttendanceData.map((o) => o.totalShift - o.totalAbsent)
      barChartData.value.datasets[1].data = factoryAttendanceData.map((o) => o.totalAbsent)

      // Calculate semester totals for attendance and absence
      dataStats.totalAttendance = factoryAttendanceData.reduce((sum, item) => sum + (item.totalShift - item.totalAbsent), 0)
      dataStats.totalAbsent = factoryAttendanceData.reduce((sum, item) => sum + item.totalAbsent, 0)
    })
    .catch((error) => {
      message.error(error?.response?.data?.message || 'Không thể tải dữ liệu thống kê')
    })
    .finally(() => {
      loadingStore.hide()
    })
}

const fetchDataSemester = () => {
  return requestAPI
    .get(ROUTE_NAMES_API.FETCH_DATA_SEMESTER)
    .then(({ data: response }) => {
      lstSemester.value = response.data

      if (response.data && response.data.length > 0) {
        const currentSemester = getCurrentSemester(response.data)
        if (currentSemester) {
          dataFilter.idSemester = currentSemester.id
        } else {

          dataFilter.idSemester = response.data[0].id
        }
      }
    })
    .catch((error) => {
      message.error(error?.response?.data?.message || 'Lỗi khi lấy dữ liệu học kỳ')
    })
}

const handleClearFilter = () => {
  // Tự động chọn học kỳ hiện tại khi clear filter
  let defaultSemesterId = null
  if (lstSemester.value && lstSemester.value.length > 0) {
    const currentSemester = getCurrentSemester(lstSemester.value)
    defaultSemesterId = currentSemester ? currentSemester.id : lstSemester.value[0].id
  }

  Object.assign(dataFilter, {
    idSemester: defaultSemesterId,
  })
  fetchDataAllStats()
}

const handleSubmitFilter = () => {
  fetchDataAllStats()
}

onMounted(async () => {
  breadcrumbStore.setRoutes(breadcrumb.value)

  // Fetch semester data first, then fetch stats after semester is selected
  await fetchDataSemester()

  // Fetch stats data after semester is selected
  if (dataFilter.idSemester) {
    fetchDataAllStats()
  }
})

watch(
  dataStats,
  (data) => {
    stats.value[0].value = data.factory || 0     // Nhóm xưởng
    stats.value[1].value = data.pass || 0        // Hoàn thành
    stats.value[2].value = data.fail || 0        // Không đạt
    stats.value[3].value = data.process || 0     // Đang diễn ra
  },
  { deep: true },
)

const debounceFilter = debounce(handleSubmitFilter, 100)
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

      <!-- Filter Section -->
      <div class="col-12">
        <div class="row g-2" :style="{ maxWidth: '500px' }">
          <div class="col-sm-8">
            <a-select
              class="w-100 bg-white"
              v-model:value="dataFilter.idSemester"
              :dropdownMatchSelectWidth="false"
              placeholder="-- Vui lòng chọn 1 học kỳ --"
            >
              <a-select-option v-for="o in lstSemester" :key="o.id" :value="o.id">
                {{ o.code }}
              </a-select-option>
            </a-select>
          </div>
          <div class="col-sm-4">
            <div class="d-flex justify-content-center gap-2">
              <a-button class="btn-light" @click="handleSubmitFilter">
                <FilterFilled /> Lọc
              </a-button>
              <a-button class="btn-gray" @click="handleClearFilter"> Huỷ lọc </a-button>
            </div>
          </div>
        </div>
      </div>

      <!-- Statistics Cards -->
      <div class="col-xl col-lg col-md-4 col-sm-6" v-for="(stat, index) in stats" :key="index">
        <WidgetCounter
          :title="stat.title"
          :value="stat.value"
          :prefix="stat.prefix"
          :suffix="stat.suffix"
          :icon="stat.icon"
          :status="stat.status"
        ></WidgetCounter>
      </div>

      <!-- Bar Chart Section -->
      <div class="col-md-12 col-sm-12">
          <a-card :bordered="false" class="dashboard-bar-line header-solid">
          <template #title>
            <h6><ProjectOutlined class="me-2" /> Thống kê điểm danh theo nhóm xưởng</h6>
          </template>
          <template #extra>
            <div class="mt-2">
              <a-tag color="blue">Nhóm xưởng: {{ barChartData.labels.length }}</a-tag>
              <a-tag color="success">Điểm danh: {{ dataStats.totalAttendance }}</a-tag>
              <a-tag color="error">Vắng mặt: {{ dataStats.totalAbsent }}</a-tag>
            </div>
          </template>

          <ChartBar :height="310" :data="barChartData"></ChartBar>
        </a-card>
      </div>

      <!-- Line Chart Section -->
      <div class="col-md-12 col-sm-12">
        <a-card :bordered="false" class="dashboard-bar-line header-solid">
          <template #title>
            <h6><BookOutlined class="me-2" /> Biểu đồ tỷ lệ điểm danh / vắng</h6>
          </template>
          <template #extra>
            <a-badge color="primary" class="badge-dot-primary" text="Tỷ lệ điểm danh (%)" />
            <a-badge color="primary" class="badge-dot-secondary" text="Tỷ lệ vắng (%)" />
          </template>

          <ChartLine :height="310" :data="lineChartData"></ChartLine>
          <div class="mt-2 d-flex justify-content-end">
              <a-tag color="blue">Nhóm xưởng: {{ lineChartData.labels.length }}</a-tag>
              <a-tag color="warning">Chưa diễn ra: {{ dataStats.notStarted }}</a-tag>
              <a-tag color="processing">Đang diễn ra: {{ dataStats.process }}</a-tag>
              <a-tag color="success">Kết thúc: {{ dataStats.pass }}</a-tag>
            </div>
        </a-card>
      </div>
    </div>
  </div>
</template>
