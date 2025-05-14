<script setup>
import { ref, reactive, onMounted } from 'vue'
import { message, Modal } from 'ant-design-vue'
import requestAPI from '@/services/requestApiService'
import { API_ROUTES_STAFF } from '@/constants/staffConstant'
import { API_ROUTES_EXCEL, GLOBAL_ROUTE_NAMES } from '@/constants/routesConstant'
import {
  PlusOutlined,
  EyeFilled,
  EditFilled,
  UnorderedListOutlined,
  FilterFilled,
  UserDeleteOutlined,
  SearchOutlined,
} from '@ant-design/icons-vue'
import { ROUTE_NAMES } from '@/router/staffRoute'
import { DEFAULT_PAGINATION } from '@/constants'
import useBreadcrumbStore from '@/stores/useBreadCrumbStore'
import useLoadingStore from '@/stores/useLoadingStore'
import ExcelUploadButton from '@/components/excel/ExcelUploadButton.vue'
import { autoAddColumnWidth } from '@/utils/utils'

const breadcrumbStore = useBreadcrumbStore()
const loadingStore = useLoadingStore()
const isLoading = ref(false)

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
const columns = ref(
  autoAddColumnWidth([
    { title: 'STT', dataIndex: 'index', key: 'index' },
    { title: 'Mã sinh viên', dataIndex: 'studentCode', key: 'studentCode' },
    { title: 'Tên sinh viên', dataIndex: 'studentName', key: 'studentName' },
    { title: 'Email', dataIndex: 'studentEmail', key: 'studentEmail' },
    { title: 'Trạng thái', dataIndex: 'studentStatus', key: 'studentStatus' },
    { title: 'Chức năng', key: 'actions' },
  ]),
)

const breadcrumb = ref([
  {
    name: GLOBAL_ROUTE_NAMES.STAFF_PAGE,
    breadcrumbName: 'Phụ trách xưởng',
  },
  {
    name: ROUTE_NAMES.MANAGEMENT_STUDENT,
    breadcrumbName: 'Quản lý Sinh viên',
  },
])

/* ----------------- Methods ----------------- */
// Lấy danh sách sinh viên từ backend, truyền phân trang động
const fetchStudents = async () => {
  loadingStore.show()
  const params = {
    ...filter,
    page: pagination.current,
    size: pagination.pageSize,
  }
  try {
    const [stuRes, faceRes] = await Promise.all([
      requestAPI.get(API_ROUTES_STAFF.FETCH_DATA_STUDENT, { params }),
      requestAPI.get(API_ROUTES_STAFF.FETCH_DATA_STUDENT + '/exist-face', { params }),
    ])

    const list = stuRes.data.data.data // danh sách sinh viên
    const faceMap = faceRes.data.data // map studentId -> hasFace

    // gán thêm hasFace vào từng object dựa trên studentId
    students.value = list.map((student) => ({
      ...student,
      hasFace: faceMap[student.studentId] || false,
    }))

    // cập nhật tổng bản ghi
    if (stuRes.data.data.totalRecords !== undefined) {
      pagination.total = stuRes.data.data.totalRecords
    } else {
      pagination.total = stuRes.data.data.totalPages * pagination.pageSize
    }
  } catch (error) {
    message.error(error.response?.data?.message || 'Lỗi khi lấy danh sách sinh viên')
  } finally {
    loadingStore.hide()
  }
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
          'Lỗi khi thêm sinh viên',
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
          'Lỗi khi lấy chi tiết sinh viên',
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
          'Lỗi khi lấy chi tiết sinh viên',
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
          'Lỗi khi cập nhật sinh viên',
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
              'Lỗi khi đổi trạng thái sinh viên',
          )
        })
        .finally(() => {
          loadingStore.hide()
        })
    },
  })
}
const changeFaceStudent = (record) => {
  Modal.confirm({
    title: 'Xác nhận đổi mặt',
    content: `Bạn có chắc muốn đổi mặt của sinh viên ${record.studentName}?`,
    onOk() {
      loadingStore.show()
      // Giả sử record chứa studentId, nếu không hãy thay đổi cho phù hợp
      requestAPI
        .put(API_ROUTES_STAFF.FETCH_DATA_STUDENT + '/change-face/' + record.studentId)
        .then((response) => {
          message.success(response.data.message || 'Đổi mặt học sinh thành công')
          fetchStudents() // Làm mới danh sách sau khi đổi mặt
        })
        .catch((error) => {
          message.error(error.response?.data?.message || 'Lỗi khi đổi mặt học sinh')
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

const handleClearFilter = () => {
  // Clear all filter values
  Object.keys(filter).forEach((key) => {
    filter[key] = ''
  })
  pagination.current = 1
  fetchStudents() // or whatever your fetch function is named
}

const handleShowModalAdd = () => {
  newStudent.code = null
  newStudent.email = null
  newStudent.name = null

  modalAdd.value = true
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
          <div class="row g-3">
            <!-- Input tìm kiếm theo mã, tên, email -->
            <div class="col-md-6 col-sm-12">
              <div class="label-title">Từ khoá:</div>
              <a-input
                v-model:value="filter.searchQuery"
                placeholder="Tìm kiếm theo mã, tên, email"
                allowClear
                @change="fetchStudents"
              >
                <template #prefix>
                  <SearchOutlined />
                </template>
              </a-input>
            </div>
            <!-- Combobox trạng thái -->
            <div class="col-md-6 col-sm-12">
              <div class="label-title">Trạng thái:</div>
              <a-select
                v-model:value="filter.studentStatus"
                placeholder="Chọn trạng thái"
                allowClear
                class="w-100"
                @change="fetchStudents"
              >
                <a-select-option :value="''">Tất cả trạng thái</a-select-option>
                <a-select-option value="ACTIVE">Hoạt động</a-select-option>
                <a-select-option value="INACTIVE">Không hoạt động</a-select-option>
              </a-select>
            </div>
          </div>
          <div class="row">
            <div class="col-12">
              <div class="d-flex justify-content-center flex-wrap gap-2 mt-3">
                <a-button class="btn-light" @click="fetchStudents"> <FilterFilled /> Lọc </a-button>
                <a-button class="btn-gray" @click="handleClearFilter"> Huỷ lọc </a-button>
              </div>
            </div>
          </div>
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
              <a-button type="primary" @click="handleShowModalAdd">
                <PlusOutlined /> Thêm
              </a-button>
            </a-tooltip>
          </div>
          <a-table
            :dataSource="students"
            :columns="columns"
            rowKey="studentId"
            :scroll="{ x: 'auto' }"
            :loading="isLoading"
            :pagination="pagination"
            @change="handleTableChange"
            class="nowrap"
          >
            <template #bodyCell="{ column, record, index }">
              <template v-if="column.dataIndex === 'index'">
                {{ index + 1 + (pagination.current - 1) * pagination.pageSize }}
              </template>
              <!-- Hiển thị trạng thái -->
              <template v-if="column.dataIndex === 'studentStatus'">
                <span class="nowrap">
                  <a-switch
                    class="me-2"
                    :checked="record.studentStatus === 'ACTIVE' || record.studentStatus === 1"
                    @change="handleChangeStatusStudent(record)"
                  />
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
                </span>
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
                  <a-tooltip title="Cấp quyền thay đổi mặt sinh viên" v-if="record.hasFace">
                    <a-button
                      type="text"
                      class="btn-outline-warning"
                      @click="changeFaceStudent(record)"
                    >
                      <UserDeleteOutlined />
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
