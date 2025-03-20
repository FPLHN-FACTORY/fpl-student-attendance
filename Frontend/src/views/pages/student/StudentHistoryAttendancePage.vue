<script setup>
import { ref, computed, onMounted, reactive } from 'vue'
import dayjs from 'dayjs'
import { dayOfWeek, formatDate } from '@/utils/utils'
import {
  FilterFilled,
  UnorderedListOutlined,
  EyeOutlined,
  EyeFilled,
  ExclamationCircleOutlined,
} from '@ant-design/icons-vue'
import { message, Modal } from 'ant-design-vue'
import requestAPI from '@/services/requestApiService'
import { API_ROUTES_STUDENT } from '@/constants/studentConstant'
import useBreadcrumbStore from '@/stores/useBreadCrumbStore'
import { GLOBAL_ROUTE_NAMES } from '@/constants/routesConstant'
import { ROUTE_NAMES } from '@/router/studentRoute'
import { SHIFT } from '@/constants'
import { DEFAULT_DATE_FORMAT } from '@/constants'
import { DEFAULT_PAGINATION } from '@/constants'
import useLoadingStore from '@/stores/useLoadingStore'

const breadcrumbStore = useBreadcrumbStore()
const breadcrumb = ref([
  { name: GLOBAL_ROUTE_NAMES.STUDENT_PAGE, breadcrumbName: 'Sinh viên' },
  { name: ROUTE_NAMES.HISTORY_ATTENDANCE, breadcrumbName: 'Lịch sử điểm danh' },
])
const loadingStore = useLoadingStore()
const isLoading = ref(false)

const filter = reactive({
  semesterId: '',
  factoryId: '',
  page: 1,
  pageSize: 5,
})

const attendanceRecords = ref([])
const loading = ref(false)
const paginations = ref({})

const columns = [
  { title: 'Bài học', dataIndex: 'rowNumber', key: 'rowNumber', width: 50 },
  { title: 'Ngày học', dataIndex: 'planDateStartDate', key: 'planDateStartDate', width: 150 },
  { title: 'Ca học', dataIndex: 'planDateShift', key: 'planDateShift', width: 30 },
  { title: 'Nội dung', dataIndex: 'planDateDescription', key: 'planDateDescription', width: 80 },
  {
    title: 'Điểm danh muộn tối đa (phút)',
    dataIndex: 'lateArrival',
    key: 'lateArrival',
    width: 100,
  },
  { title: 'Trạng thái đi học', dataIndex: 'statusAttendance', key: 'statusAttendance', width: 80 },
  { title: 'Hành động', key: 'actions', width: 100 },
]

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
          })
        )
      }
      const responses = await Promise.all(promises)
      const allData = responses.reduce((acc, res) => {
        return acc.concat(res.data.data.data)
      }, firstPageData)
      attendanceRecords.value = allData
    }

    const grouped = groupBy(attendanceRecords.value, 'factoryId')
    paginations.value = {}
    for (const factoryId in grouped) {
      paginations.value[factoryId] = {
        current: 1,
        pageSize: 5,
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
    title: 'Nội dung buổi học',
    type: 'info',
    content: text || 'Buổi học chưa có nội dung',
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

const groupBy = (array, key) => {
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
  return groupBy(attendanceRecords.value, 'factoryId')
})

const handleTableChange = (factoryId, pagination) => {
  paginations.value[factoryId].current = pagination.current
  paginations.value[factoryId].pageSize = pagination.pageSize
}

const handleDetail = (record) => {
  message.warning('Chưa triển khai tính năng chi tiết')
  console.log('Chi tiết điểm danh:', record)
}

const getFactoryName = (factoryId) => {
  const factory = factories.value.find((f) => f.id === factoryId)
  return factory ? factory.name : 'Chưa xác định'
}

onMounted(() => {
  breadcrumbStore.setRoutes(breadcrumb.value)
  fetchAllAttendanceHistory()
  fetchSemesters()
  fetchFactories()
})
</script>

<template>
  <div class="container-fluid">
    <div class="row g-3">
      <div class="col-12">
        <a-card :bordered="false" class="card mb-3">
          <template #title> <FilterFilled /> Bộ lọc </template>
          <a-row :gutter="16" class="row g-2">
            <a-col :xs="24" :md="12">
              <div class="label-title">Học kỳ:</div>
              <a-select
                v-model:value="filter.semesterId"
                placeholder="Chọn học kỳ"
                style="width: 100%"
                allowClear
                @change="fetchAllAttendanceHistory"
              >
                <a-select-option :value="''">Tất cả học kỳ</a-select-option>
                <a-select-option
                  v-for="semester in semesters"
                  :key="semester.id"
                  :value="semester.id"
                >
                  {{ semester.code }}
                </a-select-option>
              </a-select>
            </a-col>
            <a-col :xs="24" :md="12">
              <div class="label-title">Nhóm xưởng:</div>
              <a-select
                v-model:value="filter.factoryId"
                placeholder="Chọn xưởng"
                style="width: 100%"
                allowClear
                @change="fetchAllAttendanceHistory"
              >
                <a-select-option :value="''">Tất cả xưởng</a-select-option>
                <a-select-option v-for="factory in factories" :key="factory.id" :value="factory.id">
                  {{ factory.name }}
                </a-select-option>
              </a-select>
            </a-col>
          </a-row>
        </a-card>
      </div>
    </div>

    <div class="row g-3" v-for="(records, factoryId) in groupedAttendance" :key="factoryId">
      <div class="col-12">
        <a-card :bordered="false" class="card mb-3">
          <template #title>
            <UnorderedListOutlined />
            Danh sách điểm danh nhóm: {{ getFactoryName(factoryId) }}
          </template>
          <a-table
            :dataSource="records"
            :columns="columns"
            :rowKey="(record) => record.id"
            :pagination="paginations[factoryId]"
            @change="(pagination, filters, sorter) => handleTableChange(factoryId, pagination)"
            :loading="loading"
            :scroll="{ y: 4000, x: 'auto' }"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.dataIndex">
                <template v-if="column.dataIndex === 'planDateStartDate'">
                  {{ dayOfWeek(record.planDateStartDate) }} -
                  {{ dayjs(record.planDateStartDate).format(DEFAULT_DATE_FORMAT + ' HH:mm') }}
                </template>
                <template v-else-if="column.dataIndex === 'planDateShift'">
                  <a-tag color="purple">
                    {{ SHIFT[record.planDateShift] }}
                  </a-tag>
                </template>
                <template v-else-if="column.dataIndex === 'lateArrival'">
                  <a-tag :color="record.lateArrival > 0 ? 'gold' : 'green'">
                    {{ record.lateArrival }}
                    <ExclamationCircleOutlined />
                  </a-tag>
                </template>
                <template v-else-if="column.dataIndex === 'planDateDescription'">
                  <a-typography-link @click="handleShowDescription(record.description)"
                    >Chi tiết</a-typography-link
                  >
                </template>
                <template v-else-if="column.dataIndex === 'statusAttendance'">
                  <a-badge
                    :status="
                      record.statusAttendance === 'CHUA_DIEN_RA'
                        ? 'warning'
                        : record.statusAttendance === 'CO_MAT'
                        ? 'success'
                        : 'error'
                    "
                    :text="record.statusAttendance"
                  />
                </template>
                <template v-else>
                  {{ record[column.dataIndex] }}
                </template>
              </template>
              <template v-else-if="column.key === 'actions'">
                <a-space>
                  <a-tooltip title="Xem chi tiết">
                    <a-button
                      type="text"
                      @click="handleDetail(record)"
                      class="btn-outline-secondary"
                    >
                      <EyeFilled />
                    </a-button>
                  </a-tooltip>
                </a-space>
              </template>
            </template>
          </a-table>
        </a-card>
      </div>
    </div>
  </div>
</template>