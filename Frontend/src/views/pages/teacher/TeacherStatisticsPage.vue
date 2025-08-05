<script setup>
import { ref, onMounted, watch, reactive, h } from 'vue'
import {
  FilterFilled,
  ProjectOutlined,
  GoldOutlined,
  UsergroupAddOutlined,
  SendOutlined,
  MailOutlined,
  CalendarOutlined,
} from '@ant-design/icons-vue'
import { message, Modal } from 'ant-design-vue'
import requestAPI from '@/services/requestApiService'
import { DEFAULT_DATE_FORMAT, DEFAULT_PAGINATION } from '@/constants'
import { API_ROUTES_TEACHER } from '@/constants/teacherConstant'
import { ROUTE_NAMES_API } from '@/router/authenticationRoute'
import { ROUTE_NAMES } from '@/router/teacherRoute'
import { GLOBAL_ROUTE_NAMES } from '@/constants/routesConstant'
import useBreadcrumbStore from '@/stores/useBreadCrumbStore'
import { autoAddColumnWidth, debounce, getCurrentSemester } from '@/utils/utils'
import useLoadingStore from '@/stores/useLoadingStore'
import WidgetCounter from '@/components/widgets/WidgetCounter.vue'
import ChartBar from '@/components/charts/ChartBar.vue'
import ChartLine from '@/components/charts/ChartLine.vue'
import dayjs from 'dayjs'

const breadcrumbStore = useBreadcrumbStore()
const loadingStore = useLoadingStore()

const isLoading = ref(false)
const isLoadingSubmit = ref(false)

const dataStats = reactive({
  totalProject: 0,
  totalFactory: 0,
  totalShiftToday: 0,
  totalStudent: 0,
  totalPLanProcess: 0,
  totalPLanComplete: 0,
})

const lstSemester = ref([])

const lstData = ref([])

const lstUser = reactive({
  admin: [],
  staff: [],
})

const minRangeDate = ref(null)
const maxRangeDate = ref(null)

const disabledDate = (current) => {
  return (
    current.isBefore(minRangeDate.value, 'day') ||
    current.isAfter(Math.min(maxRangeDate.value, dayjs().valueOf()), 'day')
  )
}

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

const rangePresets = ref(dataRangePresets)

const formRefSendMail = ref(null)

const formData = reactive({
  emailAdmin: [],
  emailStaff: [],
  rangeDate: [],
})

const formRules = reactive({
  rangeDate: [{ required: true, message: 'Vui lòng chọn khoảng thời gian muốn thống kê!' }],
})

const stats = ref([
  {
    title: 'Lịch quản lý hôm nay',
    value: 0,
    icon: CalendarOutlined,
  },
  {
    title: 'Số dự án',
    value: 0,
    icon: ProjectOutlined,
  },
  {
    title: 'Số nhóm xưởng',
    value: 0,
    icon: GoldOutlined,
  },
  {
    title: 'Số sinh viên',
    value: 0,
    icon: UsergroupAddOutlined,
  },
])

const plan_stats = ref([
  {
    title: 'Đang triển khai',
    value: 0,
    class: 'text-primary',
  },
  {
    title: 'Đã hoàn thành',
    value: 0,
    class: 'text-success',
  },
  {
    title: 'Ngừng triển khai',
    value: 0,
    class: 'text-danger',
  },
])

const barChartData = ref({
  labels: [],
  datasets: [
    {
      label: 'Số dự án',
      backgroundColor: '#fff',
      borderWidth: 0,
      borderSkipped: false,
      borderRadius: 6,
      data: [],
      maxBarThickness: 20,
    },
  ],
})

const lineChartData = ref({
  labels: [],
  datasets: [
    {
      label: 'Dự án',
      tension: 0.4,
      borderWidth: 0,
      pointRadius: 0,
      borderColor: '#1890FF',
      borderWidth: 3,
      data: [],
      maxBarThickness: 6,
    },
    {
      label: 'Sinh viên',
      tension: 0.4,
      borderWidth: 0,
      pointRadius: 0,
      borderColor: '#B37FEB',
      borderWidth: 3,
      data: [],
      maxBarThickness: 6,
    },
  ],
})

const breadcrumb = ref([
  {
    name: GLOBAL_ROUTE_NAMES.TEACHER_PAGE,
    breadcrumbName: 'Giảng viên',
  },
  {
    name: ROUTE_NAMES.MANAGEMENT_STATISTICS,
    breadcrumbName: 'Thống kê',
  },
])

const columns = ref(
  autoAddColumnWidth([
    { title: '#', dataIndex: 'orderNumber', key: 'orderNumber' },
    { title: 'Nhóm xưởng', dataIndex: 'factoryName', key: 'factoryName' },
    { title: 'Dự án', dataIndex: 'projectName', key: 'projectName' },
    { title: 'Nhóm dự án', dataIndex: 'levelProject', key: 'levelProject' },
    { title: 'Bộ môn', dataIndex: 'subjectName', key: 'subjectName' },
    { title: 'Số ca', dataIndex: 'totalShift', key: 'totalShift' },
    { title: 'Số sinh viên', dataIndex: 'totalStudent', key: 'totalStudent' },
    { title: 'Tiến độ', dataIndex: 'process', key: 'process' },
    { title: '', dataIndex: 'status', key: 'status' },
  ]),
)

const pagination = ref({ ...DEFAULT_PAGINATION })

const dataFilter = reactive({
  semester: null,
})

const fetchDataAllStats = () => {
  loadingStore.show()
  requestAPI
    .get(`${API_ROUTES_TEACHER.FETCH_DATA_STATISTICS}/${dataFilter.semester}`)
    .then(({ data: response }) => {
      Object.assign(dataStats, response.data.stats)

      const levelStats = response.data.levelStats
      barChartData.value.labels = [...levelStats.map((o) => o.label)]
      barChartData.value.datasets[0].data = [...levelStats.map((o) => o.totalProject)]

      const subjectFacilityStats = response.data.subjectFacilityStats
      lineChartData.value.labels = ['-', ...subjectFacilityStats.map((o) => o.label), '-']
      lineChartData.value.datasets[0].data = [
        0,
        ...subjectFacilityStats.map((o) => o.totalProject),
        0,
      ]
      lineChartData.value.datasets[1].data = [
        0,
        ...subjectFacilityStats.map((o) => o.totalStudent),
        0,
      ]

      fetchDataList()
    })
    .catch((error) => {
      message.error(error?.response?.data?.message || 'Không thể tải danh sách dữ liệu')
    })
    .finally(() => {
      loadingStore.hide()
    })
}

const fetchDataList = () => {
  isLoading.value = true
  requestAPI
    .get(`${API_ROUTES_TEACHER.FETCH_DATA_STATISTICS}/${dataFilter.semester}/list-factory`, {
      params: {
        page: pagination.value.current,
        size: pagination.value.pageSize,
      },
    })
    .then(({ data: response }) => {
      lstData.value = response.data.data
      pagination.value.total = response.data.totalPages * pagination.value.pageSize
    })
    .catch((error) => {
      message.error(error?.response?.data?.message || 'Không thể tải danh sách thống kê nhóm xưởng')
    })
    .finally(() => {
      isLoading.value = false
    })
}

const fetchDataSemester = () => {
  requestAPI
    .get(ROUTE_NAMES_API.FETCH_DATA_SEMESTER)
    .then(({ data: response }) => {
      lstSemester.value = response.data
      const semester = getCurrentSemester(lstSemester.value)
      if (semester) {
        dataFilter.semester = semester.id
        minRangeDate.value = semester.fromDate
        maxRangeDate.value = semester.toDate
      }
    })
    .catch((error) => {
      message.error(error?.response?.data?.message || 'Lỗi khi lấy dữ liệu học kỳ')
    })
}

const fetchDataUser = () => {
  isLoadingSubmit.value = true
  requestAPI
    .get(`${API_ROUTES_TEACHER.FETCH_DATA_STATISTICS}/${dataFilter.semester}/list-user`)
    .then(({ data: response }) => {
      Object.assign(lstUser, response.data)
      modalSendMail.isShow = true
    })
    .catch((error) => {
      message.error(
        error?.response?.data?.message || 'Không thể lấy danh sách tài khoản người dùng',
      )
    })
    .finally(() => {
      isLoadingSubmit.value = false
    })
}

const fetchSendMail = () => {
  modalSendMail.isLoading = true
  requestAPI
    .post(`${API_ROUTES_TEACHER.FETCH_DATA_STATISTICS}/${dataFilter.semester}`, {
      ...formData,
      rangeDate: [Date.parse(formData.rangeDate[0]), Date.parse(formData.rangeDate[1])],
    })
    .then(({ data: response }) => {
      message.success(response.message)
      modalSendMail.isShow = false
    })
    .catch((error) => {
      message.error(error?.response?.data?.message || 'Không thể gửi báo cáo thống kê')
    })
    .finally(() => {
      modalSendMail.isLoading = false
    })
}

const handleShowModalSendMail = () => {
  formData.emailAdmin = []
  formData.emailStaff = []
  formData.rangeDate = []
  fetchDataUser()
}

const handleClearFilter = () => {
  Object.assign(dataFilter, {
    semester: lstSemester.value?.[0]?.id || null,
  })
  fetchDataAllStats()
}

const handleSubmitFilter = () => {
  pagination.value.current = 1
  fetchDataAllStats()
}

const handleTableChange = (page) => {
  pagination.value.current = page.current
  pagination.value.pageSize = page.pageSize
  fetchDataList()
}

onMounted(() => {
  breadcrumbStore.setRoutes(breadcrumb.value)
  fetchDataSemester()
})

const modalSendMail = reactive({
  isShow: false,
  isLoading: false,
  title: h('span', [h(MailOutlined, { class: 'me-2 text-primary' }), 'Gửi mail báo cáo thống kê']),
  okText: 'Gửi ngay',
  cancelText: 'Huỷ bỏ',
  width: 800,
  onOk: () => handleSubmitSendMail(),
})

const handleSubmitSendMail = async () => {
  try {
    await formRefSendMail.value.validate()
    Modal.confirm({
      title: `Xác nhận gửi mail báo cáo thống kê`,
      type: 'info',
      content: `Bạn và những người được chọn sẽ nhận được email báo cáo?`,
      okText: 'Tiếp tục',
      cancelText: 'Hủy bỏ',
      onOk() {
        fetchSendMail()
      },
    })
  } catch (error) {}
}

watch(
  dataStats,
  (data) => {
    stats.value[0].value = data.totalShiftToday || 0
    stats.value[1].value = data.totalProject || 0
    stats.value[2].value = data.totalFactory || 0
    stats.value[3].value = data.totalStudent || 0

    plan_stats.value[0].value = data.totalPLanProcess || 0
    plan_stats.value[1].value = data.totalPLanComplete || 0
    plan_stats.value[2].value = data.totalPLanCancel || 0

    rangePresets.value = dataRangePresets
      .map((preset) => {
        const [start, end] = preset.value
        if (end.isBefore(minRangeDate.value)) {
          return null
        }
        let newStart = start
        let newEnd = end

        if (start.isBefore(dayjs(minRangeDate.value))) {
          newStart = dayjs(minRangeDate.value)
        }
        const maxLimit = dayjs(Math.min(maxRangeDate.value, dayjs().valueOf()))
        if (end.isAfter(maxLimit)) {
          newEnd = maxLimit
        }

        if (newStart !== start || newEnd !== end) {
          return {
            ...preset,
            value: [newStart, newEnd],
          }
        }
        return preset
      })
      .filter(Boolean)
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
  <a-modal
    v-model:open="modalSendMail.isShow"
    v-bind="modalSendMail"
    :okButtonProps="{ loading: modalSendMail.isLoading }"
  >
    <p>Một mail chứa tệp Excel thống kê sẽ được gửi tới email của bạn và những người được chọn.</p>
    <a-form
      ref="formRefSendMail"
      class="row mt-3"
      layout="vertical"
      autocomplete="off"
      :model="formData"
    >
      <a-form-item
        class="col-sm-12"
        label="Khoảng thời gian thống kê:"
        name="rangeDate"
        :rules="formRules.rangeDate"
      >
        <a-range-picker
          class="w-100"
          :placeholder="[DEFAULT_DATE_FORMAT, DEFAULT_DATE_FORMAT]"
          v-model:value="formData.rangeDate"
          :format="DEFAULT_DATE_FORMAT"
          :presets="rangePresets"
          :disabledDate="disabledDate"
          :disabled="modalSendMail.isLoading"
        />
      </a-form-item>

      <a-form-item class="col-sm-12" label="Gửi tới Admin (tuỳ chọn):">
        <a-select
          class="w-100"
          placeholder="Chọn 1 hoặc nhiều Admin muốn nhận báo cáo"
          v-model:value="formData.emailAdmin"
          :disabled="modalSendMail.isLoading"
          mode="multiple"
          allow-clear
        >
          <a-select-option v-for="o in lstUser.admin" :key="o.id" :value="o.email">
            {{ o.code }} - {{ o.name }}
          </a-select-option>
        </a-select>
      </a-form-item>

      <a-form-item class="col-sm-12" label="Gửi tới Phụ trách xưởng (tuỳ chọn):">
        <a-select
          class="w-100"
          placeholder="Chọn 1 hoặc nhiều Phụ trách xưởng muốn nhận báo cáo"
          v-model:value="formData.emailStaff"
          :disabled="modalSendMail.isLoading"
          mode="multiple"
          allow-clear
        >
          <a-select-option v-for="o in lstUser.staff" :key="o.id" :value="o.email">
            {{ o.code }} - {{ o.name }}
          </a-select-option>
        </a-select>
      </a-form-item>
    </a-form>
  </a-modal>

  <div class="container-fluid">
    <div class="row g-3">
      <div class="col-12">
        <div class="row g-2" :style="{ maxWidth: '500px' }">
          <div class="col-sm-8">
            <a-select
              class="w-100 bg-white"
              v-model:value="dataFilter.semester"
              :dropdownMatchSelectWidth="false"
              placeholder="-- Vui lòng chọn 1 học kỳ --"
            >
              <a-select-option v-for="o in lstSemester" :key="o.id" :value="o.id">{{
                o.code
              }}</a-select-option>
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

      <div class="col-xl-4 col-lg-12">
        <a-card :bordered="false" class="dashboard-bar-chart">
          <div class="card-title">
            <h6>Thống kê dự án</h6>
          </div>
          <ChartBar :height="220" :data="barChartData"></ChartBar>
          <div class="card-content mt-3">
            <p>Chi tiết số lượng trình trạng dự án:</p>
          </div>
          <div class="card-footer">
            <div v-for="(o, i) in plan_stats" :key="i">
              <h4 :class="{ [o.class]: o.value > 0 }">{{ o.value }}</h4>
              <span>{{ o.title }}</span>
            </div>
          </div>
        </a-card>
      </div>

      <div class="col-xl-8 col-lg-12">
        <a-card :bordered="false" class="dashboard-bar-line header-solid">
          <template #title>
            <h6>Dự án theo bộ môn</h6>
          </template>
          <template #extra>
            <a-badge color="primary" class="badge-dot-primary" text="Số dự án" />
            <a-badge color="primary" class="badge-dot-secondary" text="Số sinh viên" />
          </template>
          <ChartLine :height="310" :data="lineChartData" />
        </a-card>
      </div>

      <div class="col-12">
        <a-card :bordered="false" class="cart card-white">
          <template #title>Thống kê nhóm xưởng</template>
          <template #extra>
            <a-button
              type="primary"
              @click="handleShowModalSendMail"
              :loading="isLoadingSubmit"
              :disabled="lstData.length < 1"
            >
              <SendOutlined /> Gửi báo cáo
            </a-button>
          </template>

          <a-table
            rowKey="id"
            class="nowrap"
            :dataSource="lstData"
            :columns="columns"
            :loading="isLoading"
            :pagination="pagination"
            :scroll="{ x: 'auto' }"
            @change="handleTableChange"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'factoryName'">
                <a-typography-link>{{ record.factoryName }}</a-typography-link>
              </template>
              <template v-if="column.dataIndex === 'totalShift'">
                <a-tag color="purple"> {{ record.totalShift }} buổi </a-tag>
              </template>
              <template v-if="column.dataIndex === 'totalStudent'">
                <a-tag color="blue"> {{ record.totalStudent }} sinh viên </a-tag>
              </template>
              <template v-if="column.dataIndex === 'process'">
                <a-progress
                  :percent="Math.round((record.totalCurrentShift / record.totalShift) * 100)"
                  :steps="5"
                  :stroke-color="['#FDD835', '#FFCA28', '#CDDC39', '#7CB342', '#4CAF50']"
                />
              </template>
              <template v-if="column.key === 'status'">
                <span v-if="record.status !== 1"><a-badge status="error" /> Ngừng triển khai</span>
                <span v-else-if="record.totalCurrentShift === record.totalShift"
                  ><a-badge status="success" /> Đã hoàn thành</span
                >
                <span v-else><a-badge status="processing" /> Đang tiến hành</span>
              </template>
            </template>
          </a-table>
        </a-card>
      </div>
    </div>
  </div>
</template>
