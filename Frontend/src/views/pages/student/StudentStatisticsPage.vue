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
import PieChart from '@/components/charts/PieChart.vue'

const breadcrumbStore = useBreadcrumbStore()
const loadingStore = useLoadingStore()

const dataStats = reactive({
  factory: 0,
  pass: 0,
  fail: 0,
  process: 0,
  complete: 0,
  notStarted: 0,
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
      borderColor: '#8bc34a',
      backgroundColor: 'rgba(82, 196, 26, 0.1)',
      fill: true,
      tension: 0.2,
      borderWidth: 3,
      pointBackgroundColor: '#8bc34a',
      pointBorderColor: '#fff',
      pointBorderWidth: 2,
      pointRadius: 6,
      pointHoverRadius: 8,
      data: [],
    },
    {
      label: 'Tỷ lệ vắng (%)',
      borderColor: '#e91e63',
      backgroundColor: 'rgba(255, 77, 79, 0.1)',
      fill: true,
      tension: 0.2,
      borderWidth: 3,
      pointBackgroundColor: '#e91e63',
      pointBorderColor: '#fff',
      pointBorderWidth: 2,
      pointRadius: 6,
      pointHoverRadius: 8,
      data: [],
    },
  ],
})

const pieChartData = ref({
  labels: [],
  datasets: [
    {
      backgroundColor: ['#8bc34a', '#e91e63'],
      data: [],
      borderWidth: 0,
    },
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
      if (response.data.stdStatisticsStatResponse) {
        Object.assign(dataStats, response.data.stdStatisticsStatResponse)
      }

      const attendanceData = response.data.attendanceChartResponses || {
        totalPresent: 0,
        totalAbsent: 0,
        totalShift: 0,
      }

      dataStats.totalAttendance =
        attendanceData.totalPresent || attendanceData.totalShift - attendanceData.totalAbsent || 0
      dataStats.totalAbsent = attendanceData.totalAbsent || 0
      dataStats.totalShift = attendanceData.totalShift || 0
      pieChartData.value.labels = ['Điểm danh', 'Vắng mặt']
      pieChartData.value.datasets[0].data = [dataStats.totalAttendance, dataStats.totalAbsent]

      const factoryChartData = response.data.factoryChartResponse

      const factoryNames = factoryChartData.map((o) => o.factoryName)
      const attendancePercentages = factoryChartData.map((o) => o.attendancePercentage)
      const absencePercentages = factoryChartData.map((o) => 100 - o.attendancePercentage)

      lineChartData.value.labels = ['-', ...factoryNames, '-']
      lineChartData.value.datasets[0].data = [0, ...attendancePercentages, 0]
      lineChartData.value.datasets[1].data = [0, ...absencePercentages, 0]
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

  await fetchDataSemester()

  if (dataFilter.idSemester) {
    fetchDataAllStats()
  }
})

watch(
  dataStats,
  (data) => {
    stats.value[0].value = data.factory || 0
    stats.value[1].value = data.pass || 0
    stats.value[2].value = data.fail || 0
    stats.value[3].value = data.process || 0
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
    </div>

    <div class="row g-3 mt-3">
      <!-- Pie Chart Section -->
      <div class="col-md-6 col-sm-12">
        <a-card :bordered="false" class="dashboard-bar-line header-solid">
          <template #title>
            <h6><ProjectOutlined class="me-2" /> Thống kê điểm danh</h6>
          </template>
          <template #extra>
            <div class="mt-2">
              <a-tag color="blue">Tổng số ca: {{ dataStats.totalShift }}</a-tag>
            </div>
          </template>

          <PieChart :height="310" :data="pieChartData"></PieChart>
        </a-card>
      </div>

      <!-- Line Chart Section -->
      <div class="col-md-6 col-sm-12">
        <a-card :bordered="false" class="dashboard-bar-line header-solid">
          <template #title>
            <h6><BookOutlined class="me-2" /> Biểu đồ tỷ lệ điểm danh / vắng</h6>
          </template>
          <template #extra>
            <a-badge color="primary" class="badge-dot-success" text="Tỷ lệ điểm danh (%)" />
            <a-badge color="primary" class="badge-dot-error" text="Tỷ lệ vắng (%)" />
          </template>

          <ChartLine :height="310" :data="lineChartData"></ChartLine>
          <div class="mt-2 d-flex justify-content-end">
            <a-tag color="blue">Nhóm xưởng: {{ dataStats.factory }}</a-tag>
            <a-tag color="warning">Chưa diễn ra: {{ dataStats.notStarted }}</a-tag>
            <a-tag color="processing">Đang diễn ra: {{ dataStats.process }}</a-tag>
            <a-tag color="success">Kết thúc: {{ dataStats.complete }}</a-tag>
          </div>
        </a-card>
      </div>
    </div>
  </div>
</template>
