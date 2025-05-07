<script setup>
import { UnorderedListOutlined } from '@ant-design/icons-vue'
import { ref, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { useRoute } from 'vue-router'
import requestAPI from '@/services/requestApiService'
import { API_ROUTES_TEACHER } from '@/constants/teacherConstant'
import { ROUTE_NAMES } from '@/router/teacherRoute'
import { GLOBAL_ROUTE_NAMES } from '@/constants/routesConstant'
import useBreadcrumbStore from '@/stores/useBreadCrumbStore'
import useLoadingStore from '@/stores/useLoadingStore'

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
const columns = ref([
  { title: '#', dataIndex: 'rowNumber', key: 'rowNumber', width: 50 },
  { title: 'Mã học sinh', dataIndex: 'userStudentCode', key: 'userStudentCode', width: 150 },
  { title: 'Tên học sinh', dataIndex: 'userStudentName', key: 'userStudentName', width: 200 },
  { title: 'Trạng thái', dataIndex: 'attendanceStatus', key: 'attendanceStatus', width: 120 },
])

// Fetch students + trạng thái hiện tại
const fetchStudentAttendance = async () => {
  loadingStore.show()
  try {
    const response = await requestAPI.get(
      `${API_ROUTES_TEACHER.FETCH_DATA_STUDENT_PLAN_DATE}/show/${planDateId}`
    )
    const { data } = response.data
    students.value = data.map((item, idx) => ({
      ...item,
      rowNumber: idx + 1,
      // Nếu chưa có, mặc định '1'
      attendanceStatus: String(item.attendanceStatus ?? '1'),
      // Thêm flag local để đánh dấu đã thay đổi
      _edited: false,
    }))
  } catch (err) {
    message.error(err.response?.data?.message || 'Lỗi khi lấy danh sách điểm danh')
  } finally {
    loadingStore.hide()
  }
}

// Khi switch thay đổi: chỉ cập nhật client
const handleChangeStatus = (record, checked) => {
  record.attendanceStatus = checked ? '3' : '1'
  record._edited = true
}

// Khi nhấn Lưu: duyệt all, gửi API cho những record đã edit
const saveAttendance = async () => {
  loadingStore.show()
  try {
    const edited = students.value.filter((s) => s._edited)
    for (const rec of edited) {
      // payload
      const payload = {
        id: rec.attendanceId || null,
        planDateId: planDateId,
        userStudentId: rec.userStudentId,
        status: rec.attendanceStatus,
      }
      const res = await requestAPI.put(API_ROUTES_TEACHER.FETCH_DATA_STUDENT_PLAN_DATE, payload)
      // nếu tạo mới, cập nhật attendanceId về client
      const updated = res.data.data
      if (updated && updated.id) rec.attendanceId = updated.id
      // reset flag
      rec._edited = false
    }
    message.success('Lưu điểm danh thành công')
    // reload data
    await fetchStudentAttendance()
  } catch (error) {
    message.error(error.response?.data?.message || 'Lỗi khi lưu điểm danh')
  } finally {
    loadingStore.hide()
  }
}

// onMounted
onMounted(() => {
  breadcrumbStore.setRoutes(breadcrumb.value)
  fetchStudentAttendance()
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
      <div class="my-3 text-end">
        <a-button
          type="primary"
          @click="saveAttendance"
          :disabled="students.every((s) => !s._edited)"
        >
          Lưu điểm danh
        </a-button>
      </div>
      <a-table
        :loading="loadingStore.loading"
        :dataSource="students"
        :columns="columns"
        rowKey="userStudentId"
        :pagination="false"
      >
        <template #bodyCell="{ column, record, index }">
          <template v-if="column.key === 'rowNumber'">
            {{ index + 1 }}
          </template>
          <template v-else-if="column.dataIndex === 'attendanceStatus'">
            <a-switch
              :checked="record.attendanceStatus === '3'"
              class="me-2"
              @change="(checked) => handleChangeStatus(record, checked)"
            />
            <a-tag :color="record.attendanceStatus === '3' ? 'green' : 'red'">
              {{ record.attendanceStatus === '3' ? 'Có mặt' : 'Vắng mặt' }}
            </a-tag>
          </template>
          <template v-else>
            {{ record[column.dataIndex] }}
          </template>
        </template>
      </a-table>
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
