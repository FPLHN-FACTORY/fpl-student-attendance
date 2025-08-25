<script setup>
import { ref, reactive, onMounted, createVNode, nextTick } from 'vue'
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
  CheckCircleOutlined,
  CloseCircleOutlined,
  ExpandOutlined,
  ExclamationCircleOutlined,
} from '@ant-design/icons-vue'
import { ROUTE_NAMES } from '@/router/staffRoute'
import { DEFAULT_PAGINATION } from '@/constants'
import useBreadcrumbStore from '@/stores/useBreadCrumbStore'
import useLoadingStore from '@/stores/useLoadingStore'
import ExcelUploadButton from '@/components/excel/ExcelUploadButton.vue'
import { autoAddColumnWidth, base64ToBlob, generateSignature } from '@/utils/utils'
import { validateFormSubmission } from '@/utils/validationUtils'
import useFaceIDStore from '@/stores/useFaceIDStore'
import WebcamFaceID from '@/components/faceid/WebcamFaceID.vue'

const breadcrumbStore = useBreadcrumbStore()
const loadingStore = useLoadingStore()
const isLoading = ref(false)

const faceIDStore = useFaceIDStore()
const isShowCamera = ref(false)

/* ----------------- Data & Reactive Variables ----------------- */
// Danh sách sinh viên
const students = ref([])

// Filter: backend mong đợi searchQuery, studentStatus, page và pageSize
const filter = reactive({
  searchQuery: '',
  studentStatus: '',
  isHasFace: null,
})

// Dữ liệu phân trang, sử dụng cấu trúc từ DEFAULT_PAGINATION
const pagination = reactive({
  ...DEFAULT_PAGINATION,
})

// Modal hiển thị
const modalAdd = ref(false)
const modalUpdate = ref(false)
const modalDetail = ref(false)

const countFilter = ref(0)

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
  status: null,
  hasFace: false,
})

// Cấu hình cột cho bảng
const columns = ref(
  autoAddColumnWidth([
    { title: '#', dataIndex: 'index', key: 'index' },
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
  isLoading.value = true
  loadingStore.show()
  const params = {
    ...filter,
    page: pagination.current,
    size: pagination.pageSize,
  }

  requestAPI
    .get(API_ROUTES_STAFF.FETCH_DATA_STUDENT, { params })
    .then(({ data: response }) => {
      students.value = response.data.data.map((student) => ({
        ...student,
        hasFace: !!student?.faceEmbedding,
      }))

      pagination.total = response.data.totalItems
      countFilter.value = response.data.totalItems
    })
    .catch((error) => {
      message.error(error.response?.data?.message || 'Lỗi khi lấy danh sách sinh viên')
    })
    .finally(() => {
      isLoading.value = false
      loadingStore.hide()
    })
}

// Debounce timer
let searchTimeout = null

const handleTableChange = (pageInfo) => {
  pagination.current = pageInfo.current
  pagination.pageSize = pageInfo.pageSize
  fetchStudents()
}

const handleSearchChange = () => {
  if (searchTimeout) {
    clearTimeout(searchTimeout)
  }
  searchTimeout = setTimeout(() => {
    pagination.current = 1
    fetchStudents()
  }, 500)
}

const handleStatusChange = () => {
  pagination.current = 1
  fetchStudents()
}

const handleFaceChange = () => {
  pagination.current = 1
  fetchStudents()
}

const handleAddStudent = () => {
  const validation = validateFormSubmission(newStudent, [
    { key: 'code', label: 'Mã sinh viên', allowOnlyNumbers: true },
    { key: 'name', label: 'Tên sinh viên' },
    { key: 'email', label: 'Email sinh viên' },
  ])

  if (!validation.isValid) {
    message.error(validation.message)
    return
  }
  Modal.confirm({
    title: 'Xác nhận thêm sinh viên mới',
    content: 'Bạn có chắc chắn muốn thêm sinh viên mới này vào hệ thống không?',
    okText: 'Thêm sinh viên',
    cancelText: 'Hủy',
    onOk() {
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
    },
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
      detailStudent.status = student.status
      detailStudent.hasFace = !!student.faceEmbedding
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
  // Validate required fields with whitespace check
  const validation = validateFormSubmission(detailStudent, [
    { key: 'code', label: 'Mã sinh viên', allowOnlyNumbers: true },
    { key: 'name', label: 'Tên sinh viên' },
    { key: 'email', label: 'Email sinh viên' },
  ])

  if (!validation.isValid) {
    message.error(validation.message)
    return
  }
  Modal.confirm({
    title: 'Xác nhận cập nhật thông tin sinh viên',
    content: 'Bạn có chắc chắn muốn cập nhật thông tin của sinh viên này không?',
    okText: 'Cập nhật',
    cancelText: 'Hủy',
    onOk() {
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
    },
  })
}

// Hàm đổi trạng thái sinh viên
const handleChangeStatusStudent = (record) => {
  Modal.confirm({
    title: 'Xác nhận thay đổi trạng thái sinh viên',
    content: `Bạn có chắc chắn muốn thay đổi trạng thái của sinh viên "${record.studentName}" không?`,
    onOk: () => {
      loadingStore.show()
      requestAPI
        .put(`${API_ROUTES_STAFF.FETCH_DATA_STUDENT}/status/${record.studentId}`)
        .then(() => {
          message.success('Thay đổi trạng thái sinh viên thành công')
          fetchStudents()
        })
        .catch((error) => {
          message.error(
            (error.response && error.response.data && error.response.data.message) ||
              'Lỗi khi thay đổi trạng thái sinh viên',
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
    title: 'Xác nhận xoá bỏ dữ liệu khuôn mặt',
    content: `Bạn có chắc chắn tiếp tục loại bỏ dữ liệu khuôn mặt của sinh viên "${record.studentName}" không?`,
    onOk() {
      loadingStore.show()
      // Giả sử record chứa studentId, nếu không hãy thay đổi cho phù hợp
      requestAPI
        .put(API_ROUTES_STAFF.FETCH_DATA_STUDENT + '/change-face/' + record.studentId)
        .then((response) => {
          message.success(response.data.message || 'Xoá bỏ dữ liệu khuôn mặt sinh viên thành công')
          fetchStudents() // Làm mới danh sách sau khi đổi mặt
        })
        .catch((error) => {
          message.error(error.response?.data?.message || 'Có lỗi xảy ra. Vui lòng thử lại')
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
  showExport: true,
  btnImport: 'Import sinh viên',
  btnExport: 'Export sinh viên',
}

const clearNewStudentForm = () => {
  newStudent.code = ''
  newStudent.name = ''
  newStudent.email = ''
}

const handleClearFilter = () => {
  // Clear all filter values
  filter.searchQuery = ''
  filter.studentStatus = null
  filter.isHasFace = null
  handleSubmitFilter()
}

const handleSubmitFilter = () => {
  pagination.current = 1
  fetchStudents()
}

const clearUpdateStudentForm = () => {
  detailStudent.id = ''
  detailStudent.code = ''
  detailStudent.name = ''
  detailStudent.email = ''
  modalUpdate.value = false
}

const handleShowModalAdd = () => {
  newStudent.code = null
  newStudent.email = null
  newStudent.name = null

  modalAdd.value = true
}

const handleShowModalUpdateFace = async () => {
  modalDetail.value = false
  isShowCamera.value = true
  faceIDStore.setFullStep(false)
  faceIDStore.setAllowGlasses(false)
  faceIDStore.setAllowMask(false)
  faceIDStore.setAllowReaction(false)
  faceIDStore.setCallback((descriptor) => {
    Modal.confirm({
      title: 'Xác nhận cập nhật dữ liệu khuôn mặt',
      icon: createVNode(ExclamationCircleOutlined),
      content: 'Bạn có chắc chắn muốn tiếp tục không?',
      okText: 'Cập nhật',
      cancelText: 'Hủy',
      onOk: () => {
        loadingStore.show()
        const data = new FormData()
        const dataImage = base64ToBlob(faceIDStore.dataImage)
        data.append('image', dataImage)
        requestAPI
          .put(`${API_ROUTES_STAFF.FETCH_DATA_STUDENT}/update-face/${detailStudent.id}`, data, {
            headers: {
              'Content-Type': 'multipart/form-data',
              'X-Signature': generateSignature(detailStudent.id, dataImage.size),
            },
          })
          .then(({ data: response }) => {
            message.success(response.message)
            isShowCamera.value = false
            fetchStudents()
          })
          .catch((error) => {
            message.error(
              error.response?.data?.message ||
                'Không thể cập nhật khuôn mặt sinh viên này. Vui lòng thử lại!',
            )
          })
          .finally(() => {
            loadingStore.hide()
          })
      },
      onCancel: () => {
        isShowCamera.value = false
      },
    })
  })

  await nextTick()
  await faceIDStore.startVideo()
}

onMounted(() => {
  breadcrumbStore.setRoutes(breadcrumb.value)
  fetchStudents()
})
</script>

<template>
  <div class="container-fluid">
    <!-- Danh sách sinh viên -->
    <div class="row g-3">
      <div class="col-12">
        <a-card :bordered="false" class="cart no-body-padding">
          <a-collapse ghost>
            <a-collapse-panel>
              <template #header><FilterFilled /> Bộ lọc ({{ countFilter }})</template>
              <div class="row g-3">
                <!-- Input tìm kiếm theo mã, tên, email -->
                <div class="col-md-6 col-sm-12">
                  <div class="label-title">Từ khoá:</div>
                  <a-input
                    v-model:value="filter.searchQuery"
                    placeholder="Tìm kiếm theo mã, tên, email"
                    allowClear
                    @change="handleSearchChange"
                  >
                    <template #prefix>
                      <SearchOutlined />
                    </template>
                  </a-input>
                </div>
                <!-- Combobox trạng thái -->
                <div class="col-md-3 col-sm-12">
                  <div class="label-title">Trạng thái:</div>
                  <a-select
                    v-model:value="filter.studentStatus"
                    placeholder="-- Tất cả trạng thái --"
                    class="w-100"
                    @change="handleStatusChange"
                  >
                    <a-select-option :value="''">-- Tất cả trạng thái --</a-select-option>
                    <a-select-option :value="1">-- Hoạt động --</a-select-option>
                    <a-select-option :value="0">-- Không hoạt động --</a-select-option>
                  </a-select>
                </div>
                <div class="col-md-3 col-sm-12">
                  <div class="label-title">Đăng Ký FaceID:</div>
                  <a-select
                    v-model:value="filter.isHasFace"
                    placeholder="-- Tất cả --"
                    class="w-100"
                    @change="handleFaceChange"
                  >
                    <a-select-option :value="null">-- Tất cả --</a-select-option>
                    <a-select-option :value="true">Đã đăng ký</a-select-option>
                    <a-select-option :value="false">Chưa đăng ký</a-select-option>
                  </a-select>
                </div>
                <div class="col-12">
                  <div class="d-flex justify-content-center flex-wrap gap-2">
                    <a-button class="btn-light" @click="handleSubmitFilter">
                      <FilterFilled /> Lọc
                    </a-button>
                    <a-button class="btn-gray" @click="handleClearFilter"> Huỷ lọc </a-button>
                  </div>
                </div>
              </div>
            </a-collapse-panel>
          </a-collapse>
        </a-card>
      </div>

      <div class="col-12">
        <a-card :bordered="false" class="cart">
          <template #title> <UnorderedListOutlined /> Danh sách sinh viên </template>
          <div class="d-flex justify-content-end flex-wrap gap-3 mb-2">
            <ExcelUploadButton v-bind="configImportExcel" />

            <a-tooltip title="Thêm mới sinh viên">
              <!-- Sử dụng nút primary kiểu filled -->
              <a-button type="primary" @click="handleShowModalAdd">
                <PlusOutlined /> Thêm sinh viên
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
    <a-modal
      v-model:open="modalAdd"
      @ok="handleAddStudent"
      :okButtonProps="{ loading: isLoading }"
      @cancel="clearNewStudentForm"
      @close="clearNewStudentForm"
    >
      <template #title>
        <PlusOutlined class="me-2 text-primary" />
        Thêm sinh viên
      </template>
      <a-form layout="vertical">
        <a-form-item label="Mã sinh viên" required>
          <a-input
            v-model:value="newStudent.code"
            placeholder="--Nhập mã sinh viên--"
            @keyup.enter="handleAddStudent"
          />
        </a-form-item>
        <a-form-item label="Tên sinh viên" required>
          <a-input
            v-model:value="newStudent.name"
            placeholder="--Nhập tên sinh viên--"
            @keyup.enter="handleAddStudent"
          />
        </a-form-item>
        <a-form-item label="Email" required>
          <a-input
            v-model:value="newStudent.email"
            placeholder="--Nhập email sinh viên--"
            @keyup.enter="handleAddStudent"
          />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- Modal cập nhật sinh viên -->
    <a-modal
      v-model:open="modalUpdate"
      @ok="updateStudent"
      :okButtonProps="{ loading: isLoading }"
      @cancel="clearUpdateStudentForm"
      @close="clearUpdateStudentForm"
    >
      <template #title>
        <EditFilled class="me-2 text-primary" />
        Cập nhật sinh viên
      </template>
      <a-form layout="vertical">
        <a-form-item label="Mã sinh viên" required>
          <a-input
            v-model:value="detailStudent.code"
            placeholder="--Nhập mã sinh viên--"
            @keyup.enter="updateStudent"
          />
        </a-form-item>
        <a-form-item label="Tên sinh viên" required>
          <a-input
            v-model:value="detailStudent.name"
            placeholder="--Nhập tên sinh viên--"
            @keyup.enter="updateStudent"
          />
        </a-form-item>
        <a-form-item label="Email" required>
          <a-input
            v-model:value="detailStudent.email"
            placeholder="--Nhập email sinh viên--"
            @keyup.enter="updateStudent"
          />
        </a-form-item>
      </a-form>
    </a-modal>

    <a-modal v-model:open="modalDetail" title="Chi tiết sinh viên" :footer="null">
      <a-descriptions bordered :column="1">
        <a-descriptions-item label="Mã sinh viên">
          {{ detailStudent.code }}
        </a-descriptions-item>
        <a-descriptions-item label="Tên sinh viên">
          {{ detailStudent.name }}
        </a-descriptions-item>
        <a-descriptions-item label="Email">
          {{ detailStudent.email }}
        </a-descriptions-item>
        <a-descriptions-item label="Trạng thái">
          <a-tag :color="detailStudent.status === 'ACTIVE' ? 'green' : 'red'">
            {{ detailStudent.status === 'ACTIVE' ? 'Hoạt động' : 'Không hoạt động' }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="Dữ liệu khuôn mặt">
          <template v-if="detailStudent.hasFace">
            <a-tag color="success">
              <template #icon>
                <CheckCircleOutlined />
              </template>
              Đã đăng ký dữ liệu khuôn mặt
            </a-tag>
          </template>
          <template v-else>
            <a-tag color="error">
              <template #icon>
                <CloseCircleOutlined />
              </template>
              Chưa đăng ký dữ liệu khuôn mặt
            </a-tag>
          </template>
        </a-descriptions-item>
      </a-descriptions>
      <div class="d-flex justify-content-end mt-3" v-if="!detailStudent.hasFace">
        <a-button type="primary" @click="handleShowModalUpdateFace">
          <ExpandOutlined /> Cập nhật dữ liệu khuôn mặt
        </a-button>
      </div>
    </a-modal>

    <!-- Modal cập nhật mặt -->
    <a-modal v-model:open="isShowCamera" @cancel="faceIDStore.stopVideo()" :footer="null">
      <template #title>
        <ExpandOutlined class="me-2 text-primary" />
        Cập nhật dữ liệu khuôn mặt sinh viên
      </template>
      <WebcamFaceID />
    </a-modal>
  </div>
</template>
