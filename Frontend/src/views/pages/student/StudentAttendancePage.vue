<script setup>
import { GLOBAL_ROUTE_NAMES } from '@/constants/routesConstant'
import { API_ROUTES_STUDENT } from '@/constants/studentConstant'
import { ROUTE_NAMES } from '@/router/studentRoute'
import requestAPI from '@/services/requestApiService'
import useBreadcrumbStore from '@/stores/useBreadCrumbStore'
import { onMounted, ref } from 'vue'
import { message } from 'ant-design-vue'

const breadcrumbStore = useBreadcrumbStore()

const breadcrumb = ref([
  {
    name: GLOBAL_ROUTE_NAMES.STAFF_PAGE,
    breadcrumbName: 'Sinh viên',
  },
  {
    name: ROUTE_NAMES.ATTENDANCE,
    breadcrumbName: 'Điểm danh',
  },
])

const attendanceList = ref([])
const filter = ref({ page: 1, pageSize: 5, plan: '' })
const pagination = ref({ current: 1, pageSize: 5, total: 0, showSizeChanger: false })

const columns = [
  { title: '#', dataIndex: 'indexs', key: 'indexs' },
  { title: 'Ngày điểm danh', dataIndex: 'attendanceDay', key: 'attendanceDay' },
  { title: 'Mã môn học', dataIndex: 'subjectCode', key: 'subjectCode' },
  { title: 'Tên môn học', dataIndex: 'subjectName', key: 'subjectName' },
  { title: 'Tên giảng viên', dataIndex: 'staffName', key: 'staffName' },
  { title: 'Ca', dataIndex: 'shift', key: 'shift' },
  { title: 'Mô tả', dataIndex: 'description', key: 'description' },
]

const getCurrentTimestamp = () => Date.now()

const convertTime = () => {
  const currentTimestamp = getCurrentTimestamp()
  if (filter.value.plan === '') {
    return currentTimestamp + 7 * 24 * 60 * 60 * 1000 // 7 ngày
  } else if (filter.value.plan === 14) {
    return currentTimestamp + 14 * 24 * 60 * 60 * 1000 // 14 ngày
  } else {
    return currentTimestamp + 30 * 24 * 60 * 60 * 1000 // 30 ngày
  }
}

const fetchAttendanceList = () => {

  
  const req = {
    page: filter.value.page,
    size: filter.value.pageSize,
    now: getCurrentTimestamp(),
    max: convertTime(),
    orderBy: 'ASC',
    sortBy: 'indexs'
  }

  requestAPI
    .post('http://localhost:8765/api/v1/student/plan-attendance/list', req)
    .then(({ data }) => {
      attendanceList.value = data.data.data
      pagination.value.total = data.data.totalPages * filter.value.pageSize
      pagination.value.current = filter.value.page
    })
    .catch(() => {
      message.error('Lỗi khi lấy dữ liệu')
    })

}

const handleTableChange = (pagination) => {
  filter.value.page = pagination.current
  fetchAttendanceList()
}

const formatDate = (timestamp) => {
  if (!timestamp) return 'Không xác định'
  return new Date(timestamp).toLocaleString()
}

onMounted(() => {
  breadcrumbStore.setRoutes(breadcrumb.value)
  fetchAttendanceList()
})
</script>

<template>
  <div class="container-fluid">

    <a-card title="Bộ lọc" :bordered="false" class="cart">
      <a-row :gutter="16" class="filter-container">
        <a-col :span="12">
          <a-select v-model:value="filter.plan" placeholder="Chọn trạng thái" allowClear style="width: 100%" @change="fetchAttendanceList">
            <a-select-option :value="''">7 ngày tới</a-select-option>
            <a-select-option :value="14">14 ngày tới</a-select-option>
            <a-select-option :value="30">30 ngày tới</a-select-option>
          </a-select>
        </a-col>
      </a-row>
    </a-card>

    <a-card title="Danh sách điểm danh" :bordered="false" class="cart">
      <a-table :dataSource="attendanceList" :columns="columns" :rowKey="'id'" bordered :pagination="pagination" @change="handleTableChange">
        <template #bodyCell="{ column, record }">
          <template v-if="column.dataIndex === 'attendanceDay'">{{ formatDate(record.attendanceDay) }}</template>
          <template v-if="column.dataIndex === 'shift'">{{ record.shift }}</template>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<style>
.cart {
  margin-top: 5px;
}

.filter-container {
  margin-bottom: 5px;
}
</style>
