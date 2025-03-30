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

const route = useRoute()
const planDateId = route.query.planDateId

const breadcrumbStore = useBreadcrumbStore()
const loadingStore = useLoadingStore()

const breadcrumb = ref([
  {
    name: GLOBAL_ROUTE_NAMES.TEACHER_PAGE,
    breadcrumbName: 'Giảng viên',
  },
  {
    name: ROUTE_NAMES.MANAGEMENT_STUDENT_ATTENDANCE,
    breadcrumbName: 'Điểm danh',
  },
])

const students = ref([])

const columns = ref([
  { title: '#', dataIndex: 'rowNumber', key: 'rowNumber', width: 50 },
  { title: 'Mã học sinh', dataIndex: 'studentCode', key: 'studentCode', width: 150 },
  { title: 'Tên học sinh', dataIndex: 'studentName', key: 'studentName', width: 200 },
  { title: 'Trạng thái', dataIndex: 'status', key: 'status', width: 120 },
])

const handleChangeStatus = (record) => {
  const index = students.value.findIndex(item => item.id === record.id);
  if (index !== -1) {
    students.value[index].status = students.value[index].status === "1" ? "3" : "1";
  }
}

const fetchStudentAttendance = () => {
  loadingStore.show()
  requestAPI
    .get(`${API_ROUTES_TEACHER.FETCH_DATA_STUDENT_PLAN_DATE}/show/${planDateId}`)
    .then((response) => {
      const { data } = response.data
      students.value = data.map((item, index) => ({
        ...item,
        rowNumber: item.rowNumber,
        status: String(item.status)
      }))
    })
    .catch((error) => {
      message.error(error.response?.data?.message || 'Lỗi khi lấy danh sách điểm danh')
    })
    .finally(() => {
      loadingStore.hide()
    })
}

const saveAttendance = () => {
  loadingStore.show();
  const attendanceData = students.value.map(student => ({
    id: student.id,  
    status: student.status,
  }));
  
  Promise.all(attendanceData.map(item => 
    requestAPI.put(API_ROUTES_TEACHER.FETCH_DATA_STUDENT_PLAN_DATE, item)
  ))
  .then(() => {
    message.success("Lưu điểm danh thành công!");
    fetchStudentAttendance(); // Gọi lại hàm fetchStudentAttendance
  })
  .catch(error => {
    message.error(error.response?.data?.message || "Lỗi khi lưu điểm danh!");
  })
  .finally(() => {
    loadingStore.hide();
  });
};

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
          <p class="p-attention">1. Giảng viên thực hiện điểm danh trong vòng 15 phút kể từ khi bắt đầu ca học. Sau khoảng thời gian đó, hệ thống sẽ khóa chức năng điểm danh.</p>
          <p class="p-attention">2. Mặc định trạng thái điểm danh của sinh viên là Có mặt. Giảng viên chuyển từ có mặt thành vắng mặt nếu SV vi phạm một trong các nội quy như ra ngoài không lý do, mất trật tự ...</p>
        </a-card>
      </div>
    </div>

    <div class="row g-3">
      <div class="col-12">
        <a-card :bordered="false" class="cart">
          <template #title> <UnorderedListOutlined /> Danh sách học sinh </template>
          <div class="my-3 text-end">
            <a-button type="primary" @click="saveAttendance">Lưu điểm danh</a-button>
          </div>
          <a-table
            :loading="loadingStore.loading"
            :dataSource="students"
            :columns="columns"
            rowKey="id" 
            :pagination="false"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'rowNumber'">
                {{ record.rowNumber }}
              </template>
              <template v-if="column.dataIndex === 'status'">
                <a-switch
                  class="me-2"
                  :checked="record.status === '3'"
                  @change="handleChangeStatus(record)"
                />
                <a-tag :color="record.status === '3' ? 'green' : 'red'">{{
                  record.status === '3' ? 'Có mặt' : 'Vắng mặt'
                }}</a-tag>
              </template>
              <template v-else>
                {{ record[column.dataIndex] }}
              </template>
            </template>
          </a-table>
          <div class="my-3 text-center">
            <a-button type="primary" @click="saveAttendance">Lưu điểm danh</a-button>
          </div>
        </a-card>
      </div>
    </div>
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
