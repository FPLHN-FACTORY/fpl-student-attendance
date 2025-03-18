<script setup>
import { ref, reactive, onMounted } from 'vue'
import { message, Modal } from 'ant-design-vue'
import {
  PlusOutlined,
  EditOutlined,
  SwapOutlined,
  EyeOutlined,
  EditFilled,
  EyeFilled,
  SyncOutlined,
  FilterFilled,
  UnorderedListOutlined,
} from '@ant-design/icons-vue'
import requestAPI from '@/services/requestApiService'
import { API_ROUTES_STAFF } from '@/constants/staffConstant'
import { GLOBAL_ROUTE_NAMES } from '@/constants/routesConstant'
import { ROUTE_NAMES } from '@/router/staffRoute'
import { DEFAULT_PAGINATION } from '@/constants/paginationConstant'
import useBreadcrumbStore from '@/stores/useBreadCrumbStore'

const breadcrumbStore = useBreadcrumbStore()
const isLoading = ref(false)
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
  ...DEFAULT_PAGINATION,
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
  { title: 'STT', dataIndex: 'rowNumber', key: 'rowNumber', width: 50 },
  { title: 'Mã sinh viên', dataIndex: 'studentCode', key: 'studentCode', width: 100 },
  { title: 'Tên sinh viên', dataIndex: 'studentName', key: 'studentName', width: 150 },
  { title: 'Email', dataIndex: 'studentEmail', key: 'studentEmail', width: 250 },
  { title: 'Trạng thái', dataIndex: 'studentStatus', key: 'studentStatus', width: 80 },
  { title: 'Chức năng', key: 'actions', width: 80 },
])

const breadcrumb = ref([
  {
    name: GLOBAL_ROUTE_NAMES.STAFF_PAGE,
    breadcrumbName: 'Phụ trách xưởng',
  },
  {
    name: ROUTE_NAMES.MANAGEMENT_STUDENT,
    breadcrumbName: 'Sinh viên',
  },
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
const handleTableChange = (page) => {
  pagination.page = page.current
  pagination.pageSize = page.pageSize
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
  breadcrumbStore.setRoutes(breadcrumb.value)
  fetchStudents()
})
</script>

<template>
  <div class="container-fluid">
    <!-- Bộ lọc tìm kiếm -->
    <div class="row g-3">
      <div class="col-12">
        <a-card :bordered="false" class="cart mb-3">
          <template #title> <FilterFilled /> Bộ lọc </template>
          <a-row :gutter="16" class="filter-container">
            <!-- Input tìm kiếm theo mã, tên, email -->
            <a-col :span="12" class="col">
              <a-input
                v-model:value="filter.searchQuery"
                placeholder="Tìm kiếm theo mã, tên, email"
                allowClear
                @change="fetchStudents"
              />
            </a-col>
            <!-- Combobox trạng thái -->
            <a-col :span="12" class="col">
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
      </div>
    </div>

    <!-- Danh sách sinh viên -->
    <div class="row g-3">
      <div class="col-12">
        <a-card :bordered="false" class="cart">
          <template #title> <UnorderedListOutlined /> Danh sách sinh viên </template>
          <div class="d-flex justify-content-end mb-3">
            <a-tooltip title="Thêm mới sinh viên">
              <!-- Sử dụng nút primary kiểu filled -->
              <a-button type="primary" @click="modalAdd = true"> <PlusOutlined /> Thêm </a-button>
            </a-tooltip>
          </div>
          <a-table
            :dataSource="students"
            :columns="columns"
            rowKey="studentId"
            :scroll="{ y: 500, x: 'auto' }"
            :loading="isLoading"
            :pagination="pagination"
            @change="handleTableChange"
            class="nowrap"
          >
            <template #bodyCell="{ column, record }">
              <!-- Hiển thị trạng thái -->
              <template v-if="column.dataIndex === 'studentStatus'">
                <a-tag
                  :color="
                    record.studentStatus === 'ACTIVE' || record.studentStatus === 1
                      ? 'green'
                      : 'red'
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
                <a-space>
                  <a-tooltip title="Xem chi tiết sinh viên">
                    <a-button
                      type="text"
                      class="btn-outline-primary"
                      @click="handleDetailStudent(record)"
                    >
                      <EyeFilled />
                    </a-button>
                  </a-tooltip>
                  <a-tooltip title="Sửa sinh viên">
                    <a-button
                      type="text"
                      class="btn-outline-info"
                      @click="handleUpdateStudent(record)"
                    >
                      <EditFilled />
                    </a-button>
                  </a-tooltip>
                  <a-tooltip title="Đổi trạng thái sinh viên">
                    <a-button
                      type="text"
                      class="btn-outline-warning"
                      @click="handleChangeStatusStudent(record)"
                    >
                      <SyncOutlined />
                    </a-button>
                  </a-tooltip>
                </a-space>
              </template>
              <template v-else>
                {{ record[column.dataIndex] }}
              </template>
            </template>
          </a-table>
        </a-card>
      </div>
    </div>

    <!-- Modal thêm sinh viên -->
    <a-modal v-model:open="modalAdd" title="Thêm sinh viên" @ok="handleAddStudent">
      <a-form layout="vertical">
        <a-form-item label="Mã sinh viên" required>
          <a-input v-model:value="newStudent.code" placeholder="--Nhập mã sinh viên--" />
        </a-form-item>
        <a-form-item label="Tên sinh viên" required>
          <a-input v-model:value="newStudent.name" placeholder="--Nhập tên sinh viên--" />
        </a-form-item>
        <a-form-item label="Email" required>
          <a-input v-model:value="newStudent.email" placeholder="--Nhập email sinh viên--" />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- Modal cập nhật sinh viên -->
    <a-modal v-model:open="modalUpdate" title="Cập nhật sinh viên" @ok="updateStudent">
      <a-form layout="vertical">
        <a-form-item label="Mã sinh viên" required>
          <a-input v-model:value="detailStudent.code" placeholder="--Nhập mã sinh viên--" />
        </a-form-item>
        <a-form-item label="Tên sinh viên" required>
          <a-input v-model:value="detailStudent.name" placeholder="--Nhập tên sinh viên--" />
        </a-form-item>
        <a-form-item label="Email" required>
          <a-input v-model:value="detailStudent.email" placeholder="--Nhập email sinh viên--" />
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
  </div>
</template>
