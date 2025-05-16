<script setup>
import { UnorderedListOutlined } from '@ant-design/icons-vue'
import { ref, onMounted, computed } from 'vue'
import { message } from 'ant-design-vue'
import { useRoute } from 'vue-router'
import requestAPI from '@/services/requestApiService'
import { API_ROUTES_TEACHER } from '@/constants/teacherConstant'
import { ROUTE_NAMES } from '@/router/teacherRoute'
import { GLOBAL_ROUTE_NAMES, WS_ROUTES } from '@/constants/routesConstant'
import useBreadcrumbStore from '@/stores/useBreadCrumbStore'
import useLoadingStore from '@/stores/useLoadingStore'
import { Client } from '@stomp/stompjs'
import { ATTENDANCE_STATUS, STATUS_REQUIRED_ATTENDANCE } from '@/constants'
import { autoAddColumnWidth, formatDate } from '@/utils/utils'

// Lấy planDateId từ query
const route = useRoute()
const planDateId = route.query.idPlanDate

const breadcrumbStore = useBreadcrumbStore()
const loadingStore = useLoadingStore()

// Breadcrumb
const breadcrumb = ref([
  { name: GLOBAL_ROUTE_NAMES.TEACHER_PAGE, breadcrumbName: 'Giảng viên' },
  { name: ROUTE_NAMES.MANAGEMENT_SCHEDULE, breadcrumbName: 'Lịch giảng dạy' },
  { name: ROUTE_NAMES.MANAGEMENT_STUDENT_ATTENDANCE, breadcrumbName: 'Điểm danh' },
])

// Danh sách students
const students = ref([])

// Cột bảng
const columns = ref(
  autoAddColumnWidth([
    { title: '#', dataIndex: 'orderNumber', key: 'orderNumber' },
    {
      title: 'Mã học sinh',
      dataIndex: 'code',
      key: 'code',
    },
    {
      title: 'Tên học sinh',
      dataIndex: 'name',
      key: 'name',
    },
    { title: 'Checkin đầu giờ', dataIndex: 'createdAt', key: 'createdAt' },
    { title: 'Checkout cuối giờ', dataIndex: 'updatedAt', key: 'updatedAt' },
    {
      title: 'Trạng thái',
      dataIndex: 'status',
      key: 'status',
    },
  ]),
)

// Fetch students + trạng thái hiện tại
const fetchStudentAttendance = async () => {
  loadingStore.show()
  try {
    const response = await requestAPI.get(
      `${API_ROUTES_TEACHER.FETCH_DATA_STUDENT_PLAN_DATE}/show/${planDateId}`,
    )
    const { data } = response.data
    students.value = data.map((item, idx) => ({
      ...item,
      _tmpStatus: item.status,
    }))
  } catch (error) {
    message.error(error.response?.data?.message || 'Lỗi khi lấy dữ liệu')
  } finally {
    loadingStore.hide()
  }
}

// Khi switch thay đổi: chỉ cập nhật client
const handleChangeStatus = (record, checked) => {
  if (!checked) {
    return (record.status =
      record._tmpStatus === ATTENDANCE_STATUS.PRESENT.id
        ? ATTENDANCE_STATUS.ABSENT.id
        : record._tmpStatus)
  }

  record.status = ATTENDANCE_STATUS.PRESENT.id
}

const handleReset = () => {
  students.value = students.value.map((o) => ({
    ...o,
    status: o._tmpStatus,
  }))
}

// Khi nhấn Lưu: duyệt all, gửi API cho những record đã edit
const saveAttendance = async () => {
  loadingStore.show()
  requestAPI
    .put(API_ROUTES_TEACHER.FETCH_DATA_STUDENT_PLAN_DATE, {
      students: Array.from(students.value.filter((s) => s._tmpStatus != s.status)).map((o) => ({
        idAttendance: o.idAttendance,
        idPlanDate: planDateId,
        idUserStudent: o.id,
        status: o.status,
      })),
    })
    .then(({ data: response }) => {
      message.success(response.message || 'Lưu điểm danh thành công')
      fetchStudentAttendance()
    })
    .catch((error) => {
      message.error(error.response?.data?.message || 'Lỗi khi lưu điểm danh')
    })
    .finally(() => {
      loadingStore.hide()
    })
}

const connectSocket = () => {
  const client = new Client({
    brokerURL: WS_ROUTES.SERVER_HOST,
    reconnectDelay: 3000,
    onConnect: (frame) => {
      client.subscribe(WS_ROUTES.TOPIC_ATTENDANCE, (msg) => {
        const response = JSON.parse(msg.body)
        const idPlanDate = response?.planDateId || null
        if (planDateId != idPlanDate) {
          return
        }
        const student = students.value.find((o) => o.userStudentId === response?.userStudentId)
        if (!student) {
          return
        }

        student.status = ATTENDANCE_STATUS.PRESENT.id
      })
    },
  })

  client.activate()
}

// onMounted
onMounted(() => {
  breadcrumbStore.setRoutes(breadcrumb.value)
  fetchStudentAttendance()
  connectSocket()
})
</script>

<template>
  <div class="container-fluid">
    <div class="row g-3">
      <div class="col-12">
        <a-card :bordered="false" class="cart mb-3">
          <p class="p-attention">
            1. Giảng viên thực hiện điểm danh trong vòng 15 phút kể từ khi bắt đầu ca học. Sau
            khoảng thời gian đó, hệ thống sẽ khóa chức năng điểm danh.
          </p>
          <p class="p-attention">
            2. Mặc định trạng thái điểm danh của sinh viên là Có mặt. Giảng viên chuyển từ có mặt
            thành vắng mặt nếu SV vi phạm một trong các nội quy như ra ngoài không lý do, mất trật
            tự ...
          </p>
        </a-card>
      </div>
    </div>
    <a-card :bordered="false" class="cart">
      <template #title><UnorderedListOutlined /> Danh sách học sinh</template>
      <a-table
        class="nowrap"
        :loading="loadingStore.loading"
        :dataSource="students"
        :columns="columns"
        rowKey="userStudentId"
        :pagination="false"
        :scroll="{ x: 'auto' }"
      >
        <template #bodyCell="{ column, record, index }">
          <template v-if="column.dataIndex === 'status'">
            <a-switch
              :checked="record.status == ATTENDANCE_STATUS.PRESENT.id"
              class="me-2"
              @change="(checked) => handleChangeStatus(record, checked)"
            />
            <a-tag :color="record.status === ATTENDANCE_STATUS.PRESENT.id ? 'green' : 'red'">
              {{ record.status === ATTENDANCE_STATUS.PRESENT.id ? 'Có mặt' : 'Vắng mặt' }}
            </a-tag>
          </template>

          <template v-if="column.dataIndex === 'createdAt'">
            <template v-if="record.requiredCheckin === STATUS_REQUIRED_ATTENDANCE.ENABLE">
              <span v-if="record.status === ATTENDANCE_STATUS.ABSENT.id">
                <a-badge status="default" /> Đã huỷ checkin
              </span>
              <span
                v-else-if="
                  record.status === ATTENDANCE_STATUS.NOTCHECKIN.id || !record.idAttendance
                "
              >
                <a-badge status="error" /> Chưa checkin
              </span>
              <span v-else>
                <a-badge status="warning" />
                {{ formatDate(record.createdAt, 'dd/MM/yyyy HH:mm') || ' - ' }}
              </span>
            </template>
            <template v-else>
              <a-badge status="default" />
              Không yêu cầu
            </template>
          </template>
          <template v-if="column.dataIndex === 'updatedAt'">
            <template v-if="record.requiredCheckout === STATUS_REQUIRED_ATTENDANCE.ENABLE">
              <span v-if="record.status === ATTENDANCE_STATUS.ABSENT.id">
                <a-badge status="default" /> Đã huỷ checkout
              </span>
              <span
                v-else-if="record.status !== ATTENDANCE_STATUS.PRESENT.id || !record.idAttendance"
              >
                <a-badge status="error" /> Chưa checkout
              </span>
              <span v-else>
                <a-badge status="success" />
                {{ formatDate(record.updatedAt, 'dd/MM/yyyy HH:mm') || ' - ' }}
              </span>
            </template>
            <template v-else>
              <a-badge status="default" />
              Không yêu cầu
            </template>
          </template>
        </template>
      </a-table>
      <div class="my-3">
        <a-button
          type="primary"
          class="w-100"
          @click="saveAttendance"
          :disabled="students.every((s) => s._tmpStatus === s.status)"
        >
          Lưu điểm danh
        </a-button>
      </div>
      <div class="my-3">
        <a-button class="w-100" @click="handleReset"> Huỷ thay đổi </a-button>
      </div>
    </a-card>
  </div>
</template>

<style scoped>
.p-attention {
  color: red;
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 8px;
}
</style>
