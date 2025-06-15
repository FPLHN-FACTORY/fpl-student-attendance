<script setup>
import { ref, onMounted, watch, reactive } from 'vue'
import {
  FilterFilled,
  UserOutlined,
  TeamOutlined,
  ReadOutlined,
  AuditOutlined,
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
import dayjs from 'dayjs'

const breadcrumbStore = useBreadcrumbStore()
const loadingStore = useLoadingStore()

const dataStats = reactive({
  admin: 0,
  teacher: 0,
  staff: 0,
  logActivity: 0,
})

const lstData = ref([])

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
    title: 'Admin',
    value: 0,
    icon: UserOutlined,
    class: 'bg-primary',
  },
  {
    title: 'Phụ trách xưởng',
    value: 0,
    icon: TeamOutlined,
    class: 'bg-success',
  },
  {
    title: 'Giảng viên',
    value: 0,
    icon: ReadOutlined,
    class: 'bg-warning',
  },
  {
    title: 'Hoạt động hệ thống',
    value: 0,
    icon: AuditOutlined,
    class: 'bg-info',
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
    { title: '#', dataIndex: 'index', key: 'index', width: 60 },
    { title: 'Tên cơ sở', dataIndex: 'facilityName', key: 'facilityName' },
    { title: 'Số bộ môn', dataIndex: 'totalSubjectFacility', key: 'totalSubjectFacility' },
    { title: 'Tỷ lệ', dataIndex: 'percent', key: 'percent',  width: 120 },
  ]),
)

const fetchDataAllStats = () => {
  loadingStore.show()
  requestAPI
    .get(`${API_ROUTES_ADMIN.FETCH_DATA_STATISTICS}`, {
      params: {
        fromDay: dataFilter.fromDay,
        toDay: dataFilter.toDay,
      },
    })
    .then(({ data: response }) => {
      // Update statistics from statisticsStatResponse
      if (response.data.statisticsStatResponse) {
        Object.assign(dataStats, response.data.statisticsStatResponse)
      }

      // Update chart data from subjectFacilityChartResponse
      const subjectFacilityData = response.data.subjectFacilityChartResponse || []  
      barChartData.value.labels = subjectFacilityData.map((o) => o.facilityName)
      barChartData.value.datasets[0].data = subjectFacilityData.map((o) => o.totalSubjectFacility)

      // Add index for table display
      lstData.value = subjectFacilityData.map((item, index) => ({
        ...item,
        index: index + 1,
      }))
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
  fetchDataAllStats()
}

const handleSubmitFilter = () => {
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
    stats.value[0].value = data.admin || 0
    stats.value[1].value = data.staff || 0
    stats.value[2].value = data.teacher || 0
    stats.value[3].value = data.logActivity || 0
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
      </div>

      <!-- Chart Section -->
      <div class="col-xl-8 col-lg-12">
        <a-card :bordered="false" class="dashboard-bar-chart">
          <template #title>
            <div class="d-flex align-items-center">
              <DatabaseOutlined class="me-2 text-primary" />
              <h6 class="mb-0">Thống kê bộ môn theo cơ sở</h6>
            </div>
          </template>
          <template #extra>
            <a-tag color="blue" class="me-2">
              {{ lstData.length }} cơ sở
            </a-tag>
            <a-tag color="green">
              {{ lstData.reduce((sum, item) => sum + item.totalSubjectFacility, 0) }} bộ môn
            </a-tag>
          </template>
          <ChartBar :height="320" :data="barChartData"></ChartBar>
        </a-card>
      </div>

      <!-- Summary Card -->
      <div class="col-xl-4 col-lg-12">
        <a-card :bordered="false" class="cart card-white h-100">
          <template #title>
            <div class="d-flex align-items-center">
              <AuditOutlined class="me-2 text-success" />
              <span>Tổng quan hệ thống</span>
            </div>
          </template>

          <div class="summary-stats">
            <div class="stat-item mb-3">
              <div class="d-flex justify-content-between align-items-center">
                <span class="stat-label">Tổng người dùng:</span>
                <span class="stat-value text-primary fw-bold">
                  {{ dataStats.admin + dataStats.staff + dataStats.teacher }}
                </span>
              </div>
            </div>

            <div class="stat-item mb-3">
              <div class="d-flex justify-content-between align-items-center">
                <span class="stat-label">Quản trị viên:</span>
                <span class="stat-value text-info">{{ dataStats.admin }}</span>
              </div>
            </div>

            <div class="stat-item mb-3">
              <div class="d-flex justify-content-between align-items-center">
                <span class="stat-label">Phụ trách xưởng:</span>
                <span class="stat-value text-success">{{ dataStats.staff }}</span>
              </div>
            </div>

            <div class="stat-item mb-3">
              <div class="d-flex justify-content-between align-items-center">
                <span class="stat-label">Giảng viên:</span>
                <span class="stat-value text-warning">{{ dataStats.teacher }}</span>
              </div>
            </div>

            <div class="stat-item mb-3">
              <div class="d-flex justify-content-between align-items-center">
                <span class="stat-label">Hoạt động hệ thống:</span>
                <span class="stat-value text-danger">{{ dataStats.logActivity }}</span>
              </div>
            </div>

            <div class="stat-item mb-3">
              <div class="d-flex justify-content-between align-items-center">
                <span class="stat-label">Tổng cơ sở:</span>
                <span class="stat-value text-purple">{{ lstData.length }}</span>
              </div>
            </div>

            <div class="stat-item">
              <div class="d-flex justify-content-between align-items-center">
                <span class="stat-label">Tổng bộ môn:</span>
                <span class="stat-value text-primary fw-bold">
                  {{ lstData.reduce((sum, item) => sum + item.totalSubjectFacility, 0) }}
                </span>
              </div>
            </div>
          </div>
        </a-card>
      </div>

      <!-- Detailed Table -->
      <div class="col-12">
        <a-card :bordered="false" class="cart card-white">
          <template #title>
            <div class="d-flex align-items-center">
              <DatabaseOutlined class="me-2 text-primary" />
              <span>Chi tiết thống kê theo cơ sở</span>
            </div>
          </template>
          <template #extra>
            <a-space>
              <a-tag color="processing">
                {{ lstData.length }} cơ sở
              </a-tag>
              <a-tag color="success">
                {{ lstData.reduce((sum, item) => sum + item.totalSubjectFacility, 0) }} bộ môn
              </a-tag>
            </a-space>
          </template>          <div class="table-responsive">
            <a-table
              rowKey="id"
              class="nowrap"
              :dataSource="lstData"
              :columns="columns"
              :pagination="false"
              :scroll="{ x: 'auto' }"
            >
              <template #bodyCell="{ column, record }">
                <template v-if="column.key === 'facilityName'">
                  <a-typography-link class="fw-medium">{{ record.facilityName }}</a-typography-link>
                </template>
                <template v-if="column.dataIndex === 'totalSubjectFacility'">
                  <a-tag color="blue" class="fw-medium">{{ record.totalSubjectFacility }} bộ môn</a-tag>
                </template>
                <template v-if="column.dataIndex === 'percent'">
                  <a-progress
                    :percent="Math.round((record.totalSubjectFacility / Math.max(lstData.reduce((sum, i) => sum + i.totalSubjectFacility, 0), 1)) * 100)"
                    size="small"
                    :stroke-color="record.index <= 3 ? '#52c41a' : '#1890ff'"
                  />
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

.text-purple {
  color: #722ed1 !important;
}

.dashboard-bar-chart {
  min-height: 420px;
}

.table-responsive {
  max-height: 400px;
  overflow-y: auto;
}
</style>