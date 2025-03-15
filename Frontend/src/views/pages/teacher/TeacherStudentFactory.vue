<template>
  <div>
    <h1>Quản lý sinh viên nhóm: {{ factoryName }}</h1>

    <!-- Bộ lọc tìm kiếm -->
    <a-card title="Bộ lọc tìm kiếm" :bordered="false" class="cart">
      <a-row gutter="16" class="filter-container">
        <!-- Tìm kiếm theo mã, tên, email học sinh -->
        <a-col :span="12">
          <div class="form-group">
            <label class="form-label">Tìm kiếm</label>
            <a-input
              v-model:value="filter.searchQuery"
              placeholder="Nhập mã, tên hoặc email học sinh"
              allowClear
              @change="fetchStudentFactory"
            />
          </div>
        </a-col>
        <!-- Lọc theo trạng thái (nếu có) -->
        <a-col :span="12">
          <div class="form-group">
            <label class="form-label">Trạng thái</label>
            <a-select
              v-model:value="filter.status"
              placeholder="Chọn trạng thái"
              allowClear
              style="width: 100%"
              @change="fetchStudentFactory"
            >
              <a-select-option :value="''">Tất cả trạng thái</a-select-option>
              <a-select-option value="1">Hoạt động</a-select-option>
              <a-select-option value="0">Không hoạt động</a-select-option>
            </a-select>
          </div>
        </a-col>
      </a-row>
    </a-card>

    <!-- Danh sách học sinh trong nhóm xưởng -->
    <a-card title="Danh sách học sinh" :bordered="false" class="cart">
      <a-table
        :dataSource="students"
        :columns="columns"
        rowKey="studentFactoryId"
        bordered
        :pagination="pagination"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record, index }">
          <template v-if="column.dataIndex">
            <template v-if="column.dataIndex === 'rowNumber'">
              {{ index + 1 }}
            </template>
            <template v-else-if="column.dataIndex === 'statusStudentFactory'">
              <a-tag
                :color="
                  record.statusStudentFactory === 'ACTIVE' || record.statusStudentFactory === 1
                    ? 'green'
                    : 'red'
                "
              >
                {{
                  record.statusStudentFactory === 'ACTIVE' || record.statusStudentFactory === 1
                    ? 'Hoạt động'
                    : 'Không hoạt động'
                }}
              </a-tag>
            </template>
            <template v-else>
              {{ record[column.dataIndex] }}
            </template>
          </template>
          <template v-else-if="column.key === 'actions'">
            <a-space>
              <a-tooltip title="Xoá học sinh">
                <a-button type="text" class="action-button" @click="confirmDeleteStudent(record)">
                  <DeleteOutlined />
                </a-button>
              </a-tooltip>
              <a-tooltip title="Đổi trạng thái">
                <a-button
                  type="text"
                  class="action-button"
                  @click="toggleStatusStudentFactory(record)"
                >
                  <SyncOutlined />
                </a-button>
              </a-tooltip>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script setup>
import { DeleteOutlined, SyncOutlined } from '@ant-design/icons-vue'
import { ref, reactive, onMounted } from 'vue'
import { message, Modal } from 'ant-design-vue'
import { useRoute } from 'vue-router'
import requestAPI from '@/services/requestApiService'
import { API_ROUTES_TEACHER } from '@/constants/teacherConstant'

// Giả sử route cho trang Student Factory được định nghĩa trong ROUTE_NAMES
// (không cần chuyển hướng trong component này vì đây chính là trang hiển thị danh sách học sinh)

const route = useRoute()
const factoryId = route.query.factoryId
const factoryName = route.query.factoryName

// Danh sách học sinh thuộc nhóm xưởng
const students = ref([])
// Filter & phân trang
const filter = reactive({
  searchQuery: '',
  status: '',
  factoryId: factoryId, // từ query string
  page: 1,
  pageSize: 5,
})
const pagination = reactive({
  current: 1,
  pageSize: 5,
  total: 0,
  showSizeChanger: false,
})

// Cấu hình cột cho bảng
const columns = ref([
  { title: 'STT', dataIndex: 'rowNumber', key: 'rowNumber', width: 50 },
  { title: 'Mã học sinh', dataIndex: 'studentCode', key: 'studentCode', width: 150 },
  { title: 'Tên học sinh', dataIndex: 'studentName', key: 'studentName', width: 200 },
  { title: 'Email', dataIndex: 'studentEmail', key: 'studentEmail', width: 250 },
  {
    title: 'Trạng thái',
    dataIndex: 'statusStudentFactory',
    key: 'statusStudentFactory',
    width: 120,
  },
  { title: 'Chức năng', key: 'actions', width: 80 },
])

// Hàm lấy danh sách học sinh trong nhóm xưởng
const fetchStudentFactory = () => {
  requestAPI
    .get(API_ROUTES_TEACHER.FETCH_DATA_STUDENT_FACTORY, { params: filter })
    .then((response) => {
      const result = response.data.data
      students.value = result.data
      pagination.total = result.totalPages * filter.pageSize
      pagination.current = filter.page
    })
    .catch((error) => {
      message.error(error.response?.data?.message || 'Lỗi khi lấy danh sách học sinh')
    })
}

// Xử lý thay đổi trang bảng
const handleTableChange = (paginationData) => {
  filter.page = paginationData.current
  fetchStudentFactory()
}

// Hàm xoá học sinh khỏi nhóm xưởng
const confirmDeleteStudent = (record) => {
  Modal.confirm({
    title: 'Xác nhận xoá',
    content: `Bạn có chắc chắn xoá học sinh ${record.studentName} khỏi nhóm xưởng?`,
    onOk() {
      deleteStudentFactory(record.studentFactoryId)
    },
  })
}

const deleteStudentFactory = (studentFactoryId) => {
  requestAPI
    .delete(API_ROUTES_TEACHER.FETCH_DATA_STUDENT_FACTORY + '/' + studentFactoryId)
    .then((response) => {
      message.success(response.data.message || 'Xoá học sinh thành công')
      fetchStudentFactory()
    })
    .catch((error) => {
      message.error(error.response?.data?.message || 'Lỗi khi xoá học sinh')
    })
}
const toggleStatusStudentFactory = (record) => {
  Modal.confirm({
    title: 'Xác nhận đổi trạng thái',
    content: `Bạn có chắc muốn thay đổi trạng thái của học sinh ${record.studentName}?`,
    onOk() {
      requestAPI
        .put(API_ROUTES_TEACHER.FETCH_DATA_STUDENT_FACTORY + '/' + record.studentFactoryId)
        .then((response) => {
          message.success(response.data.message || 'Trạng thái đã được cập nhật thành công')
          fetchStudentFactory() // Làm mới danh sách sau khi đổi trạng thái
        })
        .catch((error) => {
          message.error(error.response?.data?.message || 'Lỗi khi đổi trạng thái sinh viên')
        })
    },
  })
}

onMounted(() => {
  fetchStudentFactory()
})
</script>

<style scoped>
.cart {
  margin-top: 10px;
}
.filter-container {
  margin-bottom: 10px;
}
.form-group {
  margin-bottom: 12px;
}
.form-label {
  display: block;
  margin-bottom: 4px;
  font-weight: 500;
}
.action-button {
  background-color: #fff7e6;
  color: black;
  border: 1px solid #ffa940;
  margin-right: 8px;
}
</style>
