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
const lstData = ref([])

const columns = autoAddColumnWidth([
  {
    title: '#',
    dataIndex: 'index',
    key: 'index',
  },
  {
    title: 'Tên nhóm xưởng',
    dataIndex: 'factoryName',
    key: 'factoryName',
    width: '200px',
  },
  {
    title: 'Tỷ lệ điểm danh',
    dataIndex: 'attendancePercentage',
    key: 'attendancePercentage',
    width: '200px',
  },
  {
    title: 'Kết quả',
    dataIndex: 'result',
    key: 'result',
    width: '200px',
  },
])

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
  // {
  //   title: 'Dự án',
  //   value: 0,
  //   icon: ProjectOutlined,
  //   class: 'bg-info',
  // },
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
      borderColor: '#1890ff',
      backgroundColor: 'rgba(24, 144, 255, 0.1)',
      fill: true,
      tension: 0.4,
      borderWidth: 3,
      pointBackgroundColor: '#1890ff',
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
      lineChartData.value.labels = ['-', ...factoryChartData.map((o) => o.factoryName), '-']
      lineChartData.value.datasets[0].data = [0, ...factoryChartData.map((o) => o.attendancePercentage), 0]

      // Prepare table data with index
      lstData.value = factoryChartData.map((item, index) => ({
        ...item,
        index: index + 1,
        key: index,
      }))
    })
    .catch((error) => {
      message.error(error?.response?.data?.message || 'Không thể tải dữ liệu thống kê')
    })
    .finally(() => {
      loadingStore.hide()
    })
}

const fetchDataSemester = () => {
  requestAPI
    .get(ROUTE_NAMES_API.FETCH_DATA_SEMESTER)
    .then(({ data: response }) => {
      lstSemester.value = response.data
    })
    .catch((error) => {
      message.error(error?.response?.data?.message || 'Lỗi khi lấy dữ liệu học kỳ')
    })
}

const handleClearFilter = () => {
  Object.assign(dataFilter, {
    idSemester: lstSemester.value?.[0]?.id || null,
  })
  fetchDataAllStats()
}

const handleSubmitFilter = () => {
  fetchDataAllStats()
}

onMounted(() => {
  breadcrumbStore.setRoutes(breadcrumb.value)
  fetchDataSemester()
  fetchDataAllStats()
})

watch(
  dataStats,
  (data) => {
    stats.value[0].value = data.factory || 0
    stats.value[1].value = data.project || 0
    stats.value[2].value = data.pass || 0
    stats.value[3].value = data.fail || 0
    stats.value[4].value = data.process || 0
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
      <div class="col-xl col-lg col-md-6 col-sm-12">
        <a-card :bordered="false" class="dashboard-line-chart">
          <template #title>
            <div class="d-flex align-items-center">
              <BookOutlined class="me-2 text-primary" />
              <span>Tỷ lệ điểm danh / vắng</span>
            </div>
          </template>
          <template #extra>
            <a-tag color="blue" class="me-2">
              {{ lstData.length }} nhóm xưởng
            </a-tag>
          </template>
          <ChartLine :height="310" :data="lineChartData"></ChartLine>
        </a-card>
      </div>

      <!-- Summary Card
      <div class="col-xl-4 col-lg-12">
        <a-card :bordered="false" class="cart card-white h-100">
          <template #title>
            <div class="d-flex align-items-center">
              <CheckCircleOutlined class="me-2 text-success" />
              <span>Tổng quan kết quả học tập</span>
            </div>
          </template>

          <div class="summary-stats">
            <div class="stat-item mb-3">
              <div class="d-flex justify-content-between align-items-center">
                <span class="stat-label">Tổng nhóm xưởng tham gia:</span>
                <span class="stat-value text-primary fw-bold">
                  {{ dataStats.factory }}
                </span>
              </div>
            </div>

            <div class="stat-item mb-3">
              <div class="d-flex justify-content-between align-items-center">
                <span class="stat-label">Tổng dự án:</span>
                <span class="stat-value text-info">{{ dataStats.project }}</span>
              </div>
            </div>

            <div class="stat-item mb-3">
              <div class="d-flex justify-content-between align-items-center">
                <span class="stat-label">Đã hoàn thành:</span>
                <span class="stat-value text-success">{{ dataStats.pass }}</span>
              </div>
            </div>

            <div class="stat-item mb-3">
              <div class="d-flex justify-content-between align-items-center">
                <span class="stat-label">Không đạt yêu cầu:</span>
                <span class="stat-value text-danger">{{ dataStats.fail }}</span>
              </div>
            </div>

            <div class="stat-item mb-3">
              <div class="d-flex justify-content-between align-items-center">
                <span class="stat-label">Đang thực hiện:</span>
                <span class="stat-value text-warning">{{ dataStats.process }}</span>
              </div>
            </div>

            <div class="stat-item">
              <div class="d-flex justify-content-between align-items-center">
                <span class="stat-label">Tỷ lệ hoàn thành:</span>
                <span class="stat-value text-success fw-bold">
                  {{ dataStats.factory > 0 ? Math.round((dataStats.pass / dataStats.factory) * 100) : 0 }}%
                </span>
              </div>
            </div>
          </div>
        </a-card>
      </div> -->

      <!-- Detailed Table -->
      <div class="col-xl col-lg col-md-6 col-sm-12">
        <a-card :bordered="false" class="cart card-white">
          <template #title>
            <div class="d-flex align-items-center">
              <BookOutlined class="me-2 text-primary" />
              <span>Chi tiết tỷ lệ điểm danh theo nhóm xưởng</span>
            </div>
          </template>
          <template #extra>
            <a-space>
              <a-tag color="processing">
                {{ lstData.length }} nhóm xưởng
              </a-tag>
            </a-space>
          </template>          
          <div>
            <a-table
              :columns="columns"
              :data-source="lstData"
              :pagination="false"
              size="middle"
              :scroll="{ y: 400 }"
              class="custom-table"
            >
              <template #bodyCell="{ column, record }">
                <template v-if="column.key === 'factoryName'">
                  <a-typography-link class="fw-medium">
                    {{ record.factoryName }}
                  </a-typography-link>
                </template>
                <template v-else-if="column.key === 'attendancePercentage'">
                  <div class="d-flex ">
                    <a-progress
                      :percent="Math.round(record.attendancePercentage)"
                      size="default"
                      :stroke-color="record.attendancePercentage >= 80 ? '#52c41a' : record.attendancePercentage >= 60 ? '#faad14' : '#ff4d4f'"
                      style="width: 150px"
                    />
                  </div>
                </template>
                <template v-else-if="column.key === 'result'">
                  <a-tag 
                    :color="record.attendancePercentage >= 80 ? 'success' : record.attendancePercentage >= 60 ? 'warning' : 'error'"
                  >
                    {{ record.attendancePercentage >= 80 ? 'Đạt yêu cầu' : record.attendancePercentage >= 60 ? 'Cần cải thiện' : 'Không đạt' }}
                  </a-tag>
                </template>
              </template>
            </a-table>
          </div>
        </a-card>
      </div>
    </div>
  </div>
</template>

<style scoped>
.summary-stats {
  padding: 16px 0;
}

.stat-item {
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
}

.stat-item:last-child {
  border-bottom: none;
}

.stat-label {
  color: #666;
  font-size: 14px;
}

.stat-value {
  font-size: 16px;
  font-weight: 500;
}

.dashboard-line-chart {
  min-height: 420px;
}
</style>