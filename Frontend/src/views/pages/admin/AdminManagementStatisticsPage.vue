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
    { title: '#', dataIndex: 'index', key: 'index'},
    { title: 'Tên cơ sở', dataIndex: 'facilityName', key: 'facilityName', width: 100 },
    { title: 'Số bộ môn', dataIndex: 'totalSubjectFacility', key: 'totalSubjectFacility' },
    { title: 'Tỷ lệ bộ môn / cơ sở', dataIndex: 'percent', key: 'percent', width: 30 },
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
      </div>

      <!-- Chart Section -->
      <div class="col-xl-6 col-lg-4 col-md-12 col-sm-12">
        <a-card :bordered="false" class="dashboard-bar-chart">
          <template #title>
            <div class="d-flex align-items-center">
              <DatabaseOutlined class="me-2 text-primary" />
              <span>Thống kê bộ môn cơ sở</span>
            </div>
          </template>
          <template #extra>
            <a-tag color="blue" class="me-2">
              {{ lstData.length }} cơ sở
            </a-tag>
          </template>
          <ChartBar :height="310" :data="barChartData"></ChartBar>
        </a-card>
      </div>

      

      <!-- Detailed Table -->
      <div class="col-xl-6 col-lg-8 col-md-12 col-sm-12">
        <a-card :bordered="false" class="cart card-white">
          <template #title>
            <div class="d-flex align-items-center">
              <UnorderedListOutlined class="me-2 text-primary" />
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
          </template>
          <div class="table-responsive">
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
