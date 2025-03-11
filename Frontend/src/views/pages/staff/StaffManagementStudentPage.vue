<template>
  <h1>Quản lý sinh viên</h1>

  <!-- Bộ lọc tìm kiếm -->
  <a-card title="Bộ lọc" :bordered="false" class="cart">
    <a-row :gutter="16" class="filter-container">
      <!-- Input tìm kiếm theo mã, tên, email -->
      <a-col :span="12">
        <a-input
          v-model:value="filter.searchQuery"
          placeholder="Tìm kiếm theo mã, tên, email"
          allowClear
          @change="fetchStudents"
        />
      </a-col>
      <!-- Combobox trạng thái -->
      <a-col :span="12">
        <a-select
          v-model:value="filter.studentStatus"
          placeholder="Chọn trạng thái"
          allowClear
          style="width: 100%"
          @change="fetchStudents"
        >
          <a-select-option :value="''">Tất cả trạng thái</a-select-option>
          <a-select-option value="ACTIVE">Hoạt động</a-select-option>
          <a-select-option value="INACTIVE">Không hoạt động</a-select-option>
        </a-select>
      </a-col>
    </a-row>
  </a-card>

  <!-- Danh sách sinh viên -->
  <a-card title="Danh sách sinh viên" :bordered="false" class="cart">
    <div style="display: flex; justify-content: flex-end; margin-bottom: 10px">
      <!-- Nút thêm sinh viên với tooltip -->
      <a-tooltip title="Thêm mới sinh viên">
        <a-button
          style="background-color: #fff7e6; color: black; border: 1px solid #ffa940"
          @click="() => (modalAdd = true)"
        >
          <PlusOutlined />
          Thêm
        </a-button>
      </a-tooltip>
    </div>
    <a-table
      :dataSource="students"
      :columns="columns"
      rowKey="studentId"
      bordered
      :pagination="pagination"
      @change="handleTableChange"
    >
      <template #bodyCell="{ column, record }">
        <!-- Hiển thị trạng thái -->
        <template v-if="column.dataIndex === 'studentStatus'">
          <a-tag
            :color="
              record.studentStatus === 'ACTIVE' || record.studentStatus === 1 ? 'green' : 'red'
            "
          >
            {{
              record.studentStatus === 'ACTIVE' || record.studentStatus === 1
                ? 'Hoạt động'
                : 'Không hoạt động'
            }}
          </a-tag>
        </template>
        <!-- Các nút chức năng có tooltip -->
        <template v-else-if="column.key === 'actions'">
          <a-tooltip title="Sửa sinh viên">
            <a-button
              @click="handleUpdateStudent(record)"
              type="text"
              style="background-color: #fff7e6; margin-right: 8px; border: 1px solid #ffa940"
            >
              <EditOutlined />
            </a-button>
          </a-tooltip>
          <a-tooltip title="Xem chi tiết sinh viên">
            <a-button
              @click="handleDetailStudent(record)"
              type="text"
              style="background-color: #fff7e6; margin-right: 8px; border: 1px solid #ffa940"
            >
              <EyeOutlined />
            </a-button>
          </a-tooltip>
          <a-tooltip title="Đổi trạng thái sinh viên">
            <a-button
              @click="handleChangeStatusStudent(record)"
              type="text"
              style="background-color: #fff7e6; border: 1px solid #ffa940"
            >
              <SwapOutlined />
            </a-button>
          </a-tooltip>
        </template>
        <template v-else>
          {{ record[column.dataIndex] }}
        </template>
      </template>
    </a-table>
  </a-card>

  <!-- Modal thêm sinh viên -->
  <a-modal v-model:open="modalAdd" title="Thêm sinh viên" @ok="handleAddStudent">
    <a-form layout="vertical">
      <a-form-item label="Mã sinh viên" required>
        <a-input v-model:value="newStudent.code" />
      </a-form-item>
      <a-form-item label="Tên sinh viên" required>
        <a-input v-model:value="newStudent.name" />
      </a-form-item>
      <a-form-item label="Email" required>
        <a-input v-model:value="newStudent.email" />
      </a-form-item>
    </a-form>
  </a-modal>

  <!-- Modal cập nhật sinh viên -->
  <a-modal v-model:open="modalUpdate" title="Cập nhật sinh viên" @ok="updateStudent">
    <a-form layout="vertical">
      <a-form-item label="Mã sinh viên" required>
        <a-input v-model:value="detailStudent.code" />
      </a-form-item>
      <a-form-item label="Tên sinh viên" required>
        <a-input v-model:value="detailStudent.name" />
      </a-form-item>
      <a-form-item label="Email" required>
        <a-input v-model:value="detailStudent.email" />
      </a-form-item>
    </a-form>
  </a-modal>

  <!-- Modal hiển thị chi tiết sinh viên (chỉ xem, các input disable) -->
  <a-modal v-model:open="modalDetail" title="Chi tiết sinh viên" :footer="null">
    <a-form layout="vertical">
      <a-form-item label="Mã sinh viên">
        <a-input v-model:value="detailStudent.code" disabled />
      </a-form-item>
      <a-form-item label="Tên sinh viên">
        <a-input v-model:value="detailStudent.name" disabled />
      </a-form-item>
      <a-form-item label="Email">
        <a-input v-model:value="detailStudent.email" disabled />
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { message, Modal } from 'ant-design-vue'
import { PlusOutlined, EditOutlined, SwapOutlined, EyeOutlined } from '@ant-design/icons-vue'
import requestAPI from '@/services/requestApiService'
import { API_ROUTES_STAFF } from '@/constants/staffConstant'

// Danh sách sinh viên
const students = ref([])

// Biến lọc: backend mong đợi searchQuery, studentStatus, page và pageSize
const filter = reactive({
  searchQuery: '',
  studentStatus: '',
  page: 1,
  pageSize: 5,
})

// Dữ liệu phân trang
const pagination = reactive({
  current: 1,
  pageSize: 5,
  total: 0,
  showSizeChanger: false,
})

// Modal hiển thị
const modalAdd = ref(false)
const modalUpdate = ref(false)
const modalDetail = ref(false)

// Dữ liệu thêm mới sinh viên (StudentCreateUpdateRequest: code, name, email)
const newStudent = reactive({
  code: '',
  name: '',
  email: '',
})

// Dữ liệu chi tiết sinh viên (dùng cho cập nhật và hiển thị detail)
const detailStudent = reactive({
  id: '',
  code: '',
  name: '',
  email: '',
})

// Cấu hình cột cho bảng
const columns = ref([
  { title: 'STT', dataIndex: 'rowNumber', key: 'rowNumber' },
  { title: 'Mã sinh viên', dataIndex: 'studentCode', key: 'studentCode' },
  { title: 'Tên sinh viên', dataIndex: 'studentName', key: 'studentName' },
  { title: 'Email', dataIndex: 'studentEmail', key: 'studentEmail' },
  { title: 'Trạng thái', dataIndex: 'studentStatus', key: 'studentStatus' },
  { title: 'Chức năng', key: 'actions' },
])

// Hàm lấy danh sách sinh viên từ backend
const fetchStudents = () => {
  requestAPI
    .get(API_ROUTES_STAFF.FETCH_DATA_STUDENT, { params: filter })
    .then((response) => {
      students.value = response.data.data.data
      pagination.total = response.data.data.totalPages * filter.pageSize
      pagination.current = filter.page
    })
    .catch((error) => {
      message.error(
        (error.response && error.response.data && error.response.data.message) ||
          'Lỗi khi lấy danh sách sinh viên'
      )
    })
}

// Sự kiện thay đổi trang bảng
const handleTableChange = (paginationData) => {
  filter.page = paginationData.current
  fetchStudents()
}

// Hàm thêm sinh viên
const handleAddStudent = () => {
  if (!newStudent.code || !newStudent.name || !newStudent.email) {
    message.error('Vui lòng nhập đầy đủ thông tin')
    return
  }
  requestAPI
    .post(API_ROUTES_STAFF.FETCH_DATA_STUDENT, newStudent)
    .then(() => {
      message.success('Thêm sinh viên thành công')
      modalAdd.value = false
      fetchStudents()
      clearNewStudentForm()
    })
    .catch((error) => {
      message.error(
        (error.response && error.response.data && error.response.data.message) ||
          'Lỗi khi thêm sinh viên'
      )
    })
}

// Hàm mở modal cập nhật và load chi tiết sinh viên
const handleUpdateStudent = (record) => {
  requestAPI
    .get(`${API_ROUTES_STAFF.FETCH_DATA_STUDENT}/${record.studentId}`)
    .then((response) => {
      const student = response.data.data
      detailStudent.id = student.id
      detailStudent.code = student.code
      detailStudent.name = student.name
      detailStudent.email = student.email
      modalUpdate.value = true
    })
    .catch((error) => {
      message.error(
        (error.response && error.response.data && error.response.data.message) ||
          'Lỗi khi lấy chi tiết sinh viên'
      )
    })
}

// Hàm hiển thị chi tiết sinh viên (không chuyển router)
const handleDetailStudent = (record) => {
  requestAPI
    .get(`${API_ROUTES_STAFF.FETCH_DATA_STUDENT}/${record.studentId}`)
    .then((response) => {
      const student = response.data.data
      detailStudent.id = student.id
      detailStudent.code = student.code
      detailStudent.name = student.name
      detailStudent.email = student.email
      modalDetail.value = true
    })
    .catch((error) => {
      message.error(
        (error.response && error.response.data && error.response.data.message) ||
          'Lỗi khi lấy chi tiết sinh viên'
      )
    })
}

// Hàm submit cập nhật sinh viên
const updateStudent = () => {
  if (!detailStudent.code || !detailStudent.name || !detailStudent.email) {
    message.error('Vui lòng nhập đầy đủ thông tin')
    return
  }
  requestAPI
    .put(API_ROUTES_STAFF.FETCH_DATA_STUDENT, detailStudent)
    .then(() => {
      message.success('Cập nhật sinh viên thành công')
      modalUpdate.value = false
      fetchStudents()
    })
    .catch((error) => {
      message.error(
        (error.response && error.response.data && error.response.data.message) ||
          'Lỗi khi cập nhật sinh viên'
      )
    })
}

// Hàm đổi trạng thái sinh viên
const handleChangeStatusStudent = (record) => {
  Modal.confirm({
    title: 'Xác nhận thay đổi trạng thái',
    content: `Bạn có chắc chắn muốn đổi trạng thái cho sinh viên ${record.studentName}?`,
    onOk: () => {
      requestAPI
        .put(`${API_ROUTES_STAFF.FETCH_DATA_STUDENT}/status/${record.studentId}`)
        .then(() => {
          message.success('Đổi trạng thái thành công')
          fetchStudents()
        })
        .catch((error) => {
          message.error(
            (error.response && error.response.data && error.response.data.message) ||
              'Lỗi khi đổi trạng thái sinh viên'
          )
        })
    },
  })
}

const clearNewStudentForm = () => {
  newStudent.code = ''
  newStudent.name = ''
  newStudent.email = ''
}

onMounted(() => {
  fetchStudents()
})
</script>

<style scoped>
.cart {
  margin-top: 5px;
}
.filter-container {
  margin-bottom: 5px;
}
</style>
