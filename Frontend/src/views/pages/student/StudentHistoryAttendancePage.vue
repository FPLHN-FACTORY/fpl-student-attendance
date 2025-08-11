<script setup>
import { ref, onMounted, reactive, watch } from 'vue'
import { autoAddColumnWidth, dayOfWeek, formatDate, getCurrentSemester } from '@/utils/utils'
import {
  FilterFilled,
  UnorderedListOutlined,
  ExclamationCircleOutlined,
  FilePdfOutlined,
} from '@ant-design/icons-vue'
import { message, Modal } from 'ant-design-vue'
import requestAPI from '@/services/requestApiService'
import { API_ROUTES_STUDENT } from '@/constants/studentConstant'
import useBreadcrumbStore from '@/stores/useBreadCrumbStore'
import { GLOBAL_ROUTE_NAMES } from '@/constants/routesConstant'
import { ROUTE_NAMES } from '@/router/studentRoute'
import { STATUS_REQUIRED_ATTENDANCE } from '@/constants'
import { DEFAULT_PAGINATION } from '@/constants'
import useLoadingStore from '@/stores/useLoadingStore'

const breadcrumbStore = useBreadcrumbStore()
const breadcrumb = ref([
  { name: GLOBAL_ROUTE_NAMES.STUDENT_PAGE, breadcrumbName: 'Sinh viên' },
  { name: ROUTE_NAMES.HISTORY_ATTENDANCE, breadcrumbName: 'Lịch sử điểm danh' },
])
const loadingStore = useLoadingStore()

const filter = reactive({
  semesterId: '',
  factoryId: '',
  page: 1,
})

const countFilter = ref(0)

const isLoading = ref(false)
const paginations = ref({})
const loadingExport = reactive({})

const columns = autoAddColumnWidth([
  { title: '#', dataIndex: 'rowNumber', key: 'rowNumber' },
  { title: 'Ngày điểm danh', dataIndex: 'planDateStartDate', key: 'planDateStartDate' },
  { title: 'Ca', dataIndex: 'shift', key: 'shift' },
  {
    title: 'Điểm danh muộn',
    dataIndex: 'lateArrival',
    key: 'lateArrival',
  },
  { title: 'Nội dung', dataIndex: 'planDateDescription', key: 'planDateDescription' },
  { title: 'Check in', dataIndex: 'checkIn', key: 'checkIn' },
  { title: 'Check out', dataIndex: 'checkOut', key: 'checkOut' },
  { title: 'Trạng thái', dataIndex: 'statusAttendance', key: 'statusAttendance' },
])

const lstSemesters = ref([])
const lstFactories = ref([])
const lstGroupFactory = ref([])

const fetchAttendanceHistory = async (factoryId) => {
  const factory = lstFactories.value.find((o) => o.id === factoryId)
  const group = lstGroupFactory.value.find((o) => o.id === factory.id)

  group && (group.isLoading = true)
  requestAPI
    .get(API_ROUTES_STUDENT.FETCH_DATA_HISTORY_ATTENDANCE, {
      params: {
        semesterId: filter.semesterId,
        factoryId: factory.id,
        page: paginations.value[factory.id].current,
        size: paginations.value[factory.id].pageSize,
      },
    })
    .then(({ data: response }) => {
      if (!group) {
        lstGroupFactory.value.push({
          id: factory.id,
          name: factory.name,
          isLoading: false,
          data: response.data,
        })
      } else {
        group.data = response.data
      }
      countFilter.value = lstGroupFactory.value.reduce((sum, group) => {
        return sum + (group.data?.page?.totalItems || 0)
      }, 0)
      paginations.value[factory.id].total = response.data.page.totalItems
    })
    .catch((error) => {
      message.error('Không thể tải dữ liệu nhóm xưởng:' + factory.name)
    })
    .finally(() => {
      group && (group.isLoading = false)
    })
}

const fetchAllAttendanceHistory = async () => {
  const lstIdFactories = lstFactories.value.filter((o) =>
    filter.factoryId ? o.id === filter.factoryId : o.id,
  )

  paginations.value = {}
  lstGroupFactory.value = []
  countFilter.value = 0

  loadingStore.show()
  lstIdFactories.forEach((factory) => {
    paginations.value[factory.id] = { ...DEFAULT_PAGINATION }
    fetchAttendanceHistory(factory.id)
  })
  loadingStore.hide()
}

const handleShowDescription = (text) => {
  Modal.info({
    title: 'Nội dung buổi',
    type: 'info',
    content: text || 'Không có mô tả',
    okText: 'Đóng',
    okButtonProps: {
      class: 'btn-gray',
    },
  })
}

const fetchSemesters = () => {
  requestAPI
    .get(API_ROUTES_STUDENT.FETCH_DATA_HISTORY_ATTENDANCE + '/semesters')
    .then((response) => {
      lstSemesters.value = response.data.data
      const semester = getCurrentSemester(lstSemesters.value)
      if (semester) {
        filter.semesterId = semester.id
      }
    })
    .catch((error) => {
      message.error(error.response?.data?.message || 'Lỗi khi tải dữ liệu học kỳ')
    })
}

const fetchFactories = () => {
  filter.factoryId = ''
  requestAPI
    .get(API_ROUTES_STUDENT.FETCH_DATA_HISTORY_ATTENDANCE + '/factories/' + filter.semesterId)
    .then((response) => {
      lstFactories.value = response.data.data
      fetchAllAttendanceHistory()
    })
    .catch((error) => {
      message.error(error.response?.data?.message || 'Lỗi khi tải dữ liệu xưởng')
    })
}

const handleTableChange = (factoryId, pagination) => {
  paginations.value[factoryId].current = pagination.current
  paginations.value[factoryId].pageSize = pagination.pageSize
  fetchAttendanceHistory(factoryId)
}

const exportPDF = async (factoryId, factoryName) => {
  loadingExport[factoryId] = true
  try {
    const response = await requestAPI.get(
      API_ROUTES_STUDENT.FETCH_DATA_HISTORY_ATTENDANCE + '/export-pdf',
      {
        params: { factoryName, factoryId },
        responseType: 'blob',
      },
    )
    const url = window.URL.createObjectURL(new Blob([response.data], { type: 'application/pdf' }))
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', `lich-su-diem-danh-${factoryName}.pdf`)
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
  } catch (error) {
    message.error('Lỗi khi xuất PDF: ' + (error.response?.data?.message || error.message))
  } finally {
    loadingExport[factoryId] = false
  }
}

const handleSubmitFilter = () => {
  fetchAllAttendanceHistory()
}

const handleClearFilter = () => {
  filter.factoryId = ''
  filter.semesterId = getCurrentSemester(lstSemesters.value).id
}

onMounted(async () => {
  breadcrumbStore.setRoutes(breadcrumb.value)
  fetchSemesters()
})

watch(
  () => filter.semesterId,
  (newVal) => {
    fetchFactories()
  },
)

watch(
  () => filter.factoryId,
  (newVal) => {
    fetchAllAttendanceHistory()
  },
)
</script>

<template>
  <div class="container-fluid">
    <div class="row g-3">
      <div class="col-12">
        <a-card :bordered="false" class="cart no-body-padding">
          <a-collapse ghost>
            <a-collapse-panel>
              <template #header><FilterFilled /> Bộ lọc ({{ countFilter }})</template>
              <div class="row g-3">
                <div class="col-md-6 col-sm-6">
                  <div class="label-title">Học kỳ:</div>
                  <a-select
                    v-model:value="filter.semesterId"
                    placeholder="Chọn học kỳ"
                    class="w-100"
                  >
                    <a-select-option
                      v-for="semester in lstSemesters"
                      :key="semester.id"
                      :value="semester.id"
                    >
                      {{ semester.code }}
                    </a-select-option>
                  </a-select>
                </div>
                <div class="col-md-6 col-sm-6">
                  <div class="label-title">Nhóm xưởng:</div>
                  <a-select
                    v-model:value="filter.factoryId"
                    placeholder="Chọn xưởng"
                    class="w-100"
                    allowClear
                  >
                    <a-select-option :value="''">-- Tất cả xưởng --</a-select-option>
                    <a-select-option
                      v-for="factory in lstFactories"
                      :key="factory.id"
                      :value="factory.id"
                    >
                      {{ factory.name }}
                    </a-select-option>
                  </a-select>
                </div>
                <div class="col-12">
                  <div class="d-flex justify-content-center flex-wrap gap-2">
                    <a-button class="btn-light" @click="handleSubmitFilter">
                      <FilterFilled /> Lọc
                    </a-button>
                    <a-button class="btn-gray" @click="handleClearFilter"> Huỷ lọc </a-button>
                  </div>
                </div>
              </div>
            </a-collapse-panel>
          </a-collapse>
        </a-card>
      </div>
      <div class="col-12" v-for="(records, id) in lstGroupFactory" :key="id">
        <a-card :bordered="false" class="card">
          <template #title>
            <UnorderedListOutlined />
            Nhóm:
            {{ records.name }}
          </template>
          <template #extra>
            <a-button
              type="primary"
              :loading="loadingExport[records.id]"
              @click="exportPDF(records.id, records.name)"
            >
              <FilePdfOutlined /> Tải xuống PDF
            </a-button>
          </template>

          <div class="my-2">
            Đã vắng
            <strong class="gray-text" :class="{ 'text-danger': records.data.totalAbsent > 0 }">{{
              records.data.totalAbsent
            }}</strong
            >, có mặt
            <strong class="gray-text" :class="{ 'text-success': records.data.totalPresent > 0 }">{{
              records.data.totalPresent
            }}</strong>
            trong tổng số <strong class="gray-text">{{ records.data.totalShift }}</strong
            >.
          </div>
          <a-table
            class="nowrap"
            :dataSource="records.data.page.data"
            :columns="columns"
            :rowKey="planDateId"
            :pagination="paginations[records.id]"
            @change="(pagination, filters, sorter) => handleTableChange(records.id, pagination)"
            :loading="records.isLoading"
            :scroll="{ x: 'auto' }"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.dataIndex">
                <template v-if="column.dataIndex === 'planDateStartDate'">
                  {{ dayOfWeek(record.planDateStartDate) }} -
                  {{ formatDate(record.planDateStartDate, 'dd/MM/yyyy HH:mm') }} -
                  {{ formatDate(record.planDateEndDate, 'HH:mm') }}
                </template>
                <template v-else-if="column.dataIndex === 'shift'">
                  <a-tag color="purple">
                    Ca
                    {{
                      record.shift
                        .split(',')
                        .map((o) => Number(o))
                        .join(', ')
                    }}
                  </a-tag>
                </template>
                <template v-else-if="column.dataIndex === 'lateArrival'">
                  <a-tag :color="record.lateArrival > 0 ? 'gold' : 'green'">
                    <ExclamationCircleOutlined />
                    {{ record.lateArrival + ' phút' }}
                  </a-tag>
                </template>
                <template v-else-if="column.dataIndex === 'planDateDescription'">
                  <a-typography-link
                    v-if="record.planDateDescription"
                    @click="handleShowDescription(record.planDateDescription)"
                  >
                    Chi tiết
                  </a-typography-link>
                  <span v-else>Không có mô tả</span>
                </template>
                <template v-else-if="column.dataIndex === 'checkIn'">
                  <template v-if="record.requiredCheckIn === STATUS_REQUIRED_ATTENDANCE.ENABLE">
                    <span v-if="!record.checkIn"> <a-badge status="error" /> Chưa checkin </span>
                    <span v-else>
                      <a-badge status="success" />
                      {{ formatDate(record.checkIn, 'dd/MM/yyyy HH:mm') }}
                    </span>
                  </template>
                  <template v-else>
                    <a-badge status="default" />
                    Không yêu cầu
                  </template>
                </template>
                <template v-else-if="column.dataIndex === 'checkOut'">
                  <template v-if="record.requiredCheckOut === STATUS_REQUIRED_ATTENDANCE.ENABLE">
                    <span v-if="record.statusAttendance === 'CO_MAT'">
                      <a-badge status="success" />
                      {{ formatDate(record.checkOut, 'dd/MM/yyyy HH:mm') }}
                    </span>
                    <span v-else> <a-badge status="error" /> Chưa checkout </span>
                  </template>
                  <template v-else>
                    <a-badge status="default" />
                    Không yêu cầu
                  </template>
                </template>
                <template v-else-if="column.dataIndex === 'statusAttendance'">
                  <a-badge
                    :status="
                      record.statusAttendance === 'CHUA_DIEN_RA'
                        ? 'warning'
                        : record.statusAttendance === 'DANG_DIEN_RA'
                          ? 'processing'
                          : record.statusAttendance === 'CO_MAT'
                            ? 'success'
                            : 'error'
                    "
                    :text="
                      record.statusAttendance === 'CHUA_DIEN_RA'
                        ? 'Chưa diễn ra'
                        : record.statusAttendance === 'DANG_DIEN_RA'
                          ? 'Đang diễn ra'
                          : record.statusAttendance === 'CO_MAT'
                            ? 'Có mặt'
                            : 'Vắng mặt'
                    "
                  />
                </template>
                <template v-else>
                  {{ record[column.dataIndex] }}
                </template>
              </template>
            </template>
          </a-table>
        </a-card>
      </div>
    </div>

    <div class="row g-3 mt-3" v-if="lstGroupFactory.length === 0 && !isLoading">
      <div class="col-12">
        <a-card :bordered="false" class="card mb-3">
          <div class="d-flex justify-content-center align-items-center p-4">
            <a-empty description="Kỳ học này bạn chưa có nhóm xưởng nào" />
          </div>
        </a-card>
      </div>
    </div>
  </div>
</template>
