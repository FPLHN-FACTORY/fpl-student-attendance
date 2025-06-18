<script setup>
import { ref, onMounted, watch, reactive } from 'vue'
import {
  FilterFilled,
  ProjectOutlined,
  GoldOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
  SyncOutlined,
  BookOutlined,
  UnorderedListOutlined,
} from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import requestAPI from '@/services/requestApiService'
import { API_ROUTES_STUDENT } from '@/constants/studentConstant'
import { ROUTE_NAMES_API } from '@/router/authenticationRoute'
import { ROUTE_NAMES } from '@/router/studentRoute'
import { GLOBAL_ROUTE_NAMES } from '@/constants/routesConstant'
import useBreadcrumbStore from '@/stores/useBreadCrumbStore'
import { debounce, getCurrentSemester, autoAddColumnWidth } from '@/utils/utils'
import useLoadingStore from '@/stores/useLoadingStore'
import WidgetCounter from '@/components/widgets/WidgetCounter.vue'
import ChartLine from '@/components/charts/ChartLine.vue'
import dayjs from 'dayjs'

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
      
      // Tự động chọn học kỳ hiện tại
      if (response.data && response.data.length > 0) {
        const currentSemester = getCurrentSemester(response.data)
        if (currentSemester) {
          dataFilter.idSemester = currentSemester.id
        } else {
          // Nếu không tìm thấy học kỳ hiện tại, chọn học kỳ đầu tiên
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

      <!-- Chart Section -->
      <div class="col-md-12 col-sm-12">
        <a-card :bordered="false" class="dashboard-line-chart">          <template #title>
            <div class="d-flex align-items-center">
              <BookOutlined class="me-2 text-primary" />
              <span>Biểu đồ tỷ lệ điểm danh / vắng</span>
            </div>
          </template>          <template #extra>
            <a-tag color="blue" class="me-2">
              {{ lineChartData.labels.length }} nhóm xưởng
            </a-tag>
          </template>
          <ChartLine :height="310" :data="lineChartData"></ChartLine>        </a-card>
      </div>
    </div>
  </div>
</template>