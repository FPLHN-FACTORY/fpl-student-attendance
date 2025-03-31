<script setup>
import { ref, reactive, onMounted } from 'vue'
import { message, Modal } from 'ant-design-vue'
import router from '@/router'
import requestAPI from '@/services/requestApiService'
import { API_ROUTES_STAFF } from '@/constants/staffConstant'
import { API_ROUTES_EXCEL, GLOBAL_ROUTE_NAMES } from '@/constants/routesConstant'
import {
  PlusOutlined,
  EyeOutlined,
  EditOutlined,
  SyncOutlined,
  EyeFilled,
  EditFilled,
  UnorderedListOutlined,
  FilterFilled,
} from '@ant-design/icons-vue'
import { ROUTE_NAMES } from '@/router/staffRoute'
import { DEFAULT_PAGINATION } from '@/constants'
import useBreadcrumbStore from '@/stores/useBreadCrumbStore'
import useLoadingStore from '@/stores/useLoadingStore'
import ExcelUploadButton from '@/components/excel/ExcelUploadButton.vue'

const breadcrumbStore = useBreadcrumbStore()
const loadingStore = useLoadingStore()

/* ----------------- Data & Reactive Variables ----------------- */
// Danh sách sinh viên
const students = ref([])

// Filter: backend mong đợi searchQuery, studentStatus, page và pageSize
const filter = reactive({
  searchQuery: '',
  studentStatus: '',
})

// Dữ liệu phân trang, sử dụng cấu trúc từ DEFAULT_PAGINATION
const pagination = reactive({
  ...DEFAULT_PAGINATION,
})

// Modal hiển thị
const modalAdd = ref(false)
const modalUpdate = ref(false)
const modalDetail = ref(false)

// Dữ liệu thêm mới sinh viên
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

/* ----------------- Methods ----------------- */
// Lấy danh sách sinh viên từ backend, truyền phân trang động
const fetchStudents = () => {
  loadingStore.show()
  requestAPI
    .get(API_ROUTES_STAFF.FETCH_DATA_STUDENT, {
      params: {
        ...filter,
        page: pagination.current,
        size: pagination.pageSize,
      },
    })
    .then((response) => {
      students.value = response.data.data.data
      // Tính tổng số bản ghi: nếu có totalRecords thì dùng luôn, nếu không nhân totalPages với pageSize
      if (response.data.data.totalRecords !== undefined) {
        pagination.total = response.data.data.totalRecords
      } else {
        pagination.total = response.data.data.totalPages * pagination.pageSize
      }
      // Đồng bộ current nếu cần (ở đây giữ nguyên giá trị của filter hoặc pagination)
    })
    .catch((error) => {
      message.error(
        (error.response && error.response.data && error.response.data.message) ||
          'Lỗi khi lấy danh sách sinh viên'
      )
    })
    .finally(() => {
      loadingStore.hide()
    })
}

// Sự kiện thay đổi trang bảng: cập nhật pagination rồi gọi lại API
const handleTableChange = (pageInfo) => {
  // Cập nhật current và pageSize
  pagination.current = pageInfo.current
  pagination.pageSize = pageInfo.pageSize
  // Nếu muốn đồng bộ với filter, bạn có thể cập nhật:
  filter.page = pageInfo.current
  filter.pageSize = pageInfo.pageSize
  fetchStudents()
}

// Hàm thêm sinh viên
const handleAddStudent = () => {
  if (!newStudent.code || !newStudent.name || !newStudent.email) {
    message.error('Vui lòng nhập đầy đủ thông tin')
    return
  }
  loadingStore.show()
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
    .finally(() => {
      loadingStore.hide()
    })
}

// Hàm mở modal cập nhật và load chi tiết sinh viên
const handleUpdateStudent = (record) => {
  loadingStore.show()
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
    .finally(() => {
      loadingStore.hide()
    })
}

// Hàm hiển thị chi tiết sinh viên (không chuyển router)
const handleDetailStudent = (record) => {
  loadingStore.show()
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
    .finally(() => {
      loadingStore.hide()
    })
}

// Hàm submit cập nhật sinh viên
const updateStudent = () => {
  if (!detailStudent.code || !detailStudent.name || !detailStudent.email) {
    message.error('Vui lòng nhập đầy đủ thông tin')
    return
  }
  loadingStore.show()
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
    .finally(() => {
      loadingStore.hide()
    })
}

// Hàm đổi trạng thái sinh viên
const handleChangeStatusStudent = (record) => {
  Modal.confirm({
    title: 'Xác nhận thay đổi trạng thái',
    content: `Bạn có chắc chắn muốn đổi trạng thái cho sinh viên ${record.studentName}?`,
    onOk: () => {
      loadingStore.show()
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
        .finally(() => {
          loadingStore.hide()
        })
    },
  })
}

const configImportExcel = {
  fetchUrl: API_ROUTES_EXCEL.FETCH_IMPORT_STUDENT,
  onSuccess: () => {
    fetchStudents()
  },
  onError: () => {
    message.error('Không thể xử lý file excel')
  },
  showDownloadTemplate: true,
  showHistoryLog: true,
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
              <div class="label-title">Tìm kiếm mã, tên, email:</div>
              <a-input
                v-model:value="filter.searchQuery"
                placeholder="Tìm kiếm theo mã, tên, email"
                allowClear
                @change="fetchStudents"
              />
            </a-col>
            <!-- Combobox trạng thái -->
            <a-col :span="12" class="col">
              <div class="label-title">Trạng thái:</div>
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
          <div class="d-flex justify-content-end mb-3 flex-wrap gap-3">
            <ExcelUploadButton v-bind="configImportExcel" />

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
