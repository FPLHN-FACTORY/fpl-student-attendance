<script setup>
import { ref, computed, onMounted, reactive } from 'vue'
import { autoAddColumnWidth, dayOfWeek, formatDate } from '@/utils/utils'
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
  pageSize: 5,
})

const attendanceRecords = ref([])
const isLoading = ref(false)
const paginations = ref({})
const loadingExport = reactive({})

const columns = autoAddColumnWidth([
  { title: '#', dataIndex: 'rowNumber', key: 'rowNumber' },
  { title: 'Ngày học', dataIndex: 'planDateStartDate', key: 'planDateStartDate' },
  { title: 'Ca', dataIndex: 'planDateShift', key: 'planDateShift' },
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

const semesters = ref([])
const factories = ref([])

const fetchAllAttendanceHistory = async () => {
  loadingStore.show()
  try {
    const firstResponse = await requestAPI.get(API_ROUTES_STUDENT.FETCH_DATA_HISTORY_ATTENDANCE, {
      params: { ...filter, page: 1 },
    })
    const firstPageData = firstResponse.data.data.data
    const totalPages = firstResponse.data.data.totalPages

    if (totalPages === 1) {
      attendanceRecords.value = firstPageData
    } else {
      const promises = []
      for (let page = 2; page <= totalPages; page++) {
        promises.push(
          requestAPI.get(API_ROUTES_STUDENT.FETCH_DATA_HISTORY_ATTENDANCE, {
            params: { ...filter, page },
          }),
        )
      }
      const responses = await Promise.all(promises)
      const allData = responses.reduce((acc, res) => {
        return acc.concat(res.data.data.data)
      }, firstPageData)
      attendanceRecords.value = allData
    }

    const grouped = groupBy(attendanceRecords.value)
    paginations.value = {}
    for (const factoryId in grouped) {
      paginations.value[factoryId] = {
        ...DEFAULT_PAGINATION,
        total: grouped[factoryId].length,
      }
    }
  } catch (error) {
    message.error(error.response?.data?.message || 'Lỗi khi tải dữ liệu lịch sử điểm danh')
  } finally {
    loadingStore.hide()
  }
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
      semesters.value = response.data.data

      // Find current semester and set it as default
      const now = new Date().getTime()
      const currentSemester = semesters.value.find(
        (semester) => semester.fromDate <= now && now <= semester.toDate,
      )
      if (currentSemester) {
        filter.semesterId = currentSemester.id
      }

      fetchAllAttendanceHistory()
    })
    .catch((error) => {
      message.error(error.response?.data?.message || 'Lỗi khi tải dữ liệu học kỳ')
    })
}

const fetchFactories = () => {
  requestAPI
    .get(API_ROUTES_STUDENT.FETCH_DATA_HISTORY_ATTENDANCE + '/factories')
    .then((response) => {
      factories.value = response.data.data
    })
    .catch((error) => {
      message.error(error.response?.data?.message || 'Lỗi khi tải dữ liệu xưởng')
    })
}

const groupBy = (array) => {
  return array.reduce((result, currentItem) => {
    const groupKey = currentItem.factoryId || 'Chưa xác định'
    if (!result[groupKey]) {
      result[groupKey] = []
    }
    result[groupKey].push(currentItem)
    return result
  }, {})
}

const groupedAttendance = computed(() => {
  return groupBy(attendanceRecords.value)
})

const handleTableChange = (factoryId, pagination) => {
  paginations.value[factoryId].current = pagination.current
  paginations.value[factoryId].pageSize = pagination.pageSize
}

const getFactoryName = (factoryId) => {
  const factory = factories.value.find((f) => f.id === factoryId)
  return factory ? factory.name : 'Chưa xác định'
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

const handleClearFilter = () => {
  filter.factoryId = ''

  // Find current semester when clearing filter
  const now = new Date().getTime()
  const currentSemester = semesters.value.find(
    (semester) => semester.fromDate <= now && now <= semester.toDate,
  )
  if (currentSemester) {
    filter.semesterId = currentSemester.id
  } else {
    filter.semesterId = ''
  }

  fetchAllAttendanceHistory()
}

onMounted(async () => {
  breadcrumbStore.setRoutes(breadcrumb.value)
  await fetchSemesters()
  await fetchFactories()
})
</script>

<template>
  <div class="container-fluid">
    <div class="row g-3">
      <div class="col-12">
        <a-card :bordered="false" class="cart no-body-padding">
          <a-collapse ghost>
            <a-collapse-panel>
              <template #header><FilterFilled /> Bộ lọc</template>
              <div class="row g-3">
                <div class="col-md-6 col-sm-6">
                  <div class="label-title">Học kỳ:</div>
                  <a-select
                    v-model:value="filter.semesterId"
                    placeholder="Chọn học kỳ"
                    class="w-100"
                    @change="fetchAllAttendanceHistory"
                  >
                    <a-select-option
                      v-for="semester in semesters"
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
                    @change="fetchAllAttendanceHistory"
                  >
                    <a-select-option :value="''">-- Tất cả xưởng --</a-select-option>
                    <a-select-option
                      v-for="factory in factories"
                      :key="factory.id"
                      :value="factory.id"
                    >
                      {{ factory.name }}
                    </a-select-option>
                  </a-select>
                </div>
                <div class="col-12">
                  <div class="d-flex justify-content-center flex-wrap gap-2">
                    <a-button class="btn-light" @click="fetchAllAttendanceHistory">
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
      <div class="col-12" v-for="(records, factoryId) in groupedAttendance" :key="factoryId">
        <a-card :bordered="false" class="card">
          <template #title>
            <UnorderedListOutlined />
            Nhóm: 
            {{ getFactoryName(factoryId) }}
          </template>
          <template #extra>
            <a-button
              type="primary"
              :loading="loadingExport[factoryId]"
              @click="exportPDF(factoryId, getFactoryName(factoryId))"
            >
             <FilePdfOutlined /> Tải xuống PDF
            </a-button>
          </template>

          <a-table
            class="nowrap"
            :dataSource="records"
            :columns="columns"
            :rowKey="(record) => record.planDateId"
            :pagination="paginations[factoryId]"
            @change="(pagination, filters, sorter) => handleTableChange(factoryId, pagination)"
            :loading="isLoading"
            :scroll="{ x: 'auto' }"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.dataIndex">
                <template v-if="column.dataIndex === 'planDateStartDate'">
                  {{ dayOfWeek(record.planDateStartDate) }} -
                  {{ formatDate(record.planDateStartDate, 'dd/MM/yyyy HH:mm') }} - 
                  {{ formatDate(record.planDateEndDate, 'HH:mm') }}
                </template>
                <template v-else-if="column.dataIndex === 'planDateShift'">
                  <a-tag color="purple">
                    Ca
                    {{
                      record.planDateShift
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
                  <template v-if="record.requiredCheckIn == STATUS_REQUIRED_ATTENDANCE.ENABLE">
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
                  <template v-if="record.requiredCheckOut == STATUS_REQUIRED_ATTENDANCE.ENABLE">
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

    <!-- Display message when no data is available -->
    <div class="row g-3 mt-3" v-if="Object.keys(groupedAttendance).length === 0 && !isLoading">
      <div class="col-12">
        <a-card :bordered="false" class="card mb-3">
          <div class="d-flex justify-content-center align-items-center p-4">
            <a-empty description="Kỳ học này bạn chưa có lớp học nào" />
          </div>
        </a-card>
      </div>
    </div>
  </div>
</template>
