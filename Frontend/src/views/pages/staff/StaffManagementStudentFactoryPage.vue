<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { message, Modal } from 'ant-design-vue'
import router from '@/router'
import requestAPI from '@/services/requestApiService'
import { API_ROUTES_STAFF } from '@/constants/staffConstant'
import { API_ROUTES_EXCEL, GLOBAL_ROUTE_NAMES } from '@/constants/routesConstant'
import {
  PlusOutlined,
  DeleteOutlined,
  SyncOutlined,
  DeleteFilled,
  EyeFilled,
  FilterFilled,
  UnorderedListOutlined,
  UserDeleteOutlined,
} from '@ant-design/icons-vue'
import { useRoute } from 'vue-router'
import { DEFAULT_PAGINATION } from '@/constants'
import useBreadcrumbStore from '@/stores/useBreadCrumbStore'
import useLoadingStore from '@/stores/useLoadingStore'
import { ROUTE_NAMES } from '@/router/staffRoute'
import ExcelUploadButton from '@/components/excel/ExcelUploadButton.vue'

const route = useRoute()
const factoryId = route.query.factoryId
const factoryName = route.query.factoryName

const breadcrumbStore = useBreadcrumbStore()

const breadcrumb = ref([
  {
    name: GLOBAL_ROUTE_NAMES.STAFF_PAGE,
    breadcrumbName: 'Phụ trách xưởng',
  },
  {
    name: ROUTE_NAMES.MANAGEMENT_FACTORY,
    breadcrumbName: 'Nhóm xưởng',
  },
])
const loadingStore = useLoadingStore()

const isLoading = ref(false)

if (!factoryId) {
  message.error('Không tìm thấy factoryId')
}

/* -------------------- Danh sách sinh viên đã có trong nhóm xưởng -------------------- */
const studentFactories = ref([])

const filter = reactive({
  searchQuery: '',
  status: '',
  page: 1,
  pageSize: 5,
})
const pagination = reactive({
  ...DEFAULT_PAGINATION,
})

const columns = ref([
  { title: '#', dataIndex: 'rowNumber', key: 'rowNumber' },
  { title: 'Mã sinh viên', dataIndex: 'studentCode', key: 'studentCode' },
  { title: 'Tên sinh viên', dataIndex: 'studentName', key: 'studentName' },
  { title: 'Email sinh viên', dataIndex: 'studentEmail', key: 'studentEmail' },
  { title: 'Trạng thái', dataIndex: 'statusStudentFactory', key: 'statusStudentFactory' },
  { title: 'Chức năng', key: 'actions', width: 80 },
])

/* -------------------- Phân trang cho danh sách sinh viên trong nhóm xưởng -------------------- */
const fetchStudentFactories = () => {
  loadingStore.show()
  requestAPI
    .get(API_ROUTES_STAFF.FETCH_DATA_STUDENT_FACTORY + '/' + factoryId, {
      params: {
        ...filter,
        factoryId,
        'userStudentRequest.searchQuery': filter.searchQuery,
        page: pagination.current,
        size: pagination.pageSize,
      },
    })
    .then((response) => {
      const result = response.data.data
      studentFactories.value = result.data
      if (result.totalRecords !== undefined) {
        pagination.total = result.totalRecords
      } else {
        pagination.total = result.totalPages * filter.pageSize
      }
      pagination.current = filter.page
    })
    .catch((error) => {
      message.error(
        error.response?.data?.message || 'Lỗi khi lấy danh sách sinh viên trong nhóm xưởng'
      )
    })
    .finally(() => {
      loadingStore.hide()
    })
}

/* -------------------- Lấy danh sách sinh viên đã có trong nhóm (để tích checkbox) -------------------- */
const existingStudents = ref([])

const fetchExistingStudents = () => {
  loadingStore.show()
  requestAPI
    .get(API_ROUTES_STAFF.FETCH_DATA_STUDENT_FACTORY + '/exist-student/' + factoryId)
    .then((response) => {
      existingStudents.value = response.data.data || []
      // Kiểm tra xem breadcrumb đã tồn tại chưa, nếu chưa thì mới push
      if (!breadcrumbStore.routes.some((item) => item.breadcrumbName === factoryName)) {
        breadcrumbStore.push({
          breadcrumbName: factoryName,
        })
      }
      if (allStudents.value.length) {
        updateAllStudentsCheckStatus()
      }
    })
    .catch((error) => {
      message.error(
        error.response?.data?.message || 'Lỗi khi lấy danh sách sinh viên đã có trong nhóm xưởng'
      )
    })
    .finally(() => {
      loadingStore.hide()
    })
}

/* -------------------- Danh sách tất cả sinh viên (modal thêm học sinh) -------------------- */
const studentFilter = reactive({
  searchQuery: '',
  page: 1,
  pageSize: 5,
})
const studentPagination = reactive({
  ...DEFAULT_PAGINATION,
})
const allStudents = ref([])
const studentColumns = ref([
  { title: '#', dataIndex: 'rowNumber', key: 'rowNumber' },
  { title: 'Mã sinh viên', dataIndex: 'code', key: 'code' },
  { title: 'Tên sinh viên', dataIndex: 'name', key: 'name' },
  { title: 'Email', dataIndex: 'email', key: 'email' },
  { title: 'Chọn', key: 'select' },
])
const selectedStudents = reactive({})

const updateAllStudentsCheckStatus = () => {
  const existIds = existingStudents.value.map((item) => item.studentId)
  allStudents.value = allStudents.value.map((item) => ({
    ...item,
    checked: existIds.includes(item.id),
  }))
}

const fetchAllStudents = () => {
  loadingStore.show()
  requestAPI
    .get(API_ROUTES_STAFF.FETCH_DATA_STUDENT_FACTORY + '/students', {
      params: {
        ...studentFilter,
        factoryId,
        'userStudentRequest.searchQuery': studentFilter.searchQuery,
        page: studentPagination.current,
        size: studentPagination.pageSize,
      },
    })
    .then((response) => {
      const result = response.data.data
      allStudents.value = result.data.map((item) => ({
        ...item,
        checked: false,
      }))
      if (result.totalRecords !== undefined) {
        studentPagination.total = result.totalRecords
      } else {
        studentPagination.total = result.totalPages * studentFilter.pageSize
      }
      studentPagination.current = studentFilter.page
      updateAllStudentsCheckStatus()
    })
    .catch((error) => {
      message.error(error.response?.data?.message || 'Lỗi khi lấy danh sách sinh viên')
    })
    .finally(() => {
      loadingStore.hide()
    })
}

const handleStudentTableChange = (paginationObj) => {
  console.log('Student Table Change:', paginationObj)
  studentPagination.current = paginationObj.current
  studentPagination.pageSize = paginationObj.pageSize
  studentFilter.page = paginationObj.current
  studentFilter.pageSize = paginationObj.pageSize
  fetchAllStudents()
}

/* -------------------- Xử lý checkbox chọn sinh viên -------------------- */
const handleStudentCheckboxChange = (student, checked) => {
  selectedStudents[student.id] = checked
  if (checked) {
    const payload = {
      studentId: student.id,
      factoryId: factoryId,
    }
    loadingStore.show()
    requestAPI
      .post(API_ROUTES_STAFF.FETCH_DATA_STUDENT_FACTORY, payload)
      .then((response) => {
        message.success(response.data.message || 'Thêm sinh viên vào nhóm xưởng thành công')
        fetchStudentFactories()
      })
      .catch((error) => {
        message.error(error.response?.data?.message || 'Lỗi khi thêm sinh viên vào nhóm xưởng')
      })
      .finally(() => {
        loadingStore.hide()
      })
  } else {
    const existing = existingStudents.value.find((item) => item.studentId === student.id)
    if (existing && existing.studentFactoryId) {
      loadingStore.show()
      requestAPI
        .delete(API_ROUTES_STAFF.FETCH_DATA_STUDENT_FACTORY + '/' + existing.studentFactoryId)
        .then((response) => {
          message.success(response.data.message || 'Xóa sinh viên khỏi nhóm xưởng thành công')
          fetchStudentFactories()
        })
        .catch((error) => {
          message.error(error.response?.data?.message || 'Lỗi khi xóa sinh viên khỏi nhóm xưởng')
        })
        .finally(() => {
          loadingStore.hide()
        })
    } else {
      message.warning('Sinh viên này chưa có trong nhóm xưởng')
    }
  }
}

const handleAddStudents = () => {
  isAddStudentModalVisible.value = false
}

const resetStudentModal = () => {
  isAddStudentModalVisible.value = false
  studentFilter.searchQuery = ''
  studentFilter.page = 1
  studentPagination.current = 1
  for (const key in selectedStudents) {
    selectedStudents[key] = false
  }
}

/* -------------------- Phân trang cho bảng sinh viên trong nhóm xưởng -------------------- */
const handleTableChange = (pageInfo) => {
  // Cập nhật current và pageSize
  pagination.current = pageInfo.current
  pagination.pageSize = pageInfo.pageSize
  // Nếu muốn đồng bộ với filter, bạn có thể cập nhật:
  filter.page = pageInfo.current
  filter.pageSize = pageInfo.pageSize
  fetchStudentFactories()
}

/* -------------------- Xử lý xóa sinh viên khỏi nhóm -------------------- */
const confirmDeleteStudent = (record) => {
  Modal.confirm({
    title: 'Xác nhận xóa',
    content: `Bạn có chắc muốn xóa sinh viên ${record.studentName} khỏi nhóm xưởng?`,
    onOk() {
      deleteStudentFactory(record.studentFactoryId)
    },
  })
}

/* -------------------- Xử lý đổi trạng thái sinh viên -------------------- */
const confirmChangeStatus = (record) => {
  Modal.confirm({
    title: 'Xác nhận đổi trạng thái',
    content: `Bạn có chắc muốn đổi trạng thái cho sinh viên ${record.studentName}?`,
    onOk() {
      changeStatusStudentFactory(record.studentFactoryId)
    },
  })
}

const changeStatusStudentFactory = (studentFactoryId) => {
  loadingStore.show()
  requestAPI
    .put(API_ROUTES_STAFF.FETCH_DATA_STUDENT_FACTORY + '/' + studentFactoryId)
    .then((response) => {
      message.success(response.data.message || 'Đổi trạng thái thành công')
      fetchStudentFactories()
    })
    .catch((error) => {
      message.error(error.response?.data?.message || 'Lỗi khi đổi trạng thái sinh viên')
    })
    .finally(() => {
      loadingStore.hide()
    })
}

const configImportExcel = {
  fetchUrl: API_ROUTES_EXCEL.FETCH_IMPORT_STUDENT_FACTORY,
  onSuccess: () => {
    fetchStudentFactories()
  },
  onError: () => {
    message.error('Không thể xử lý file excel')
  },
  data: { idFactory: factoryId },
  showDownloadTemplate: true,
  showHistoryLog: true,
}
const changeFaceStudent = (record) => {
  Modal.confirm({
    title: 'Xác nhận đổi mặt',
    content: `Bạn có chắc muốn đổi mặt của học sinh ${record.studentName}?`,
    onOk() {
      loadingStore.show()
      // Giả sử record chứa studentId, nếu không hãy thay đổi cho phù hợp
      requestAPI
        .put(API_ROUTES_STAFF.FETCH_DATA_STUDENT_FACTORY + '/change-face/' + record.studentId)
        .then((response) => {
          message.success(response.data.message || 'Đổi mặt học sinh thành công')
          fetchStudentFactories() // Làm mới danh sách sau khi đổi mặt
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
/* -------------------- Quản lý modal thêm sinh viên -------------------- */
const isAddStudentModalVisible = ref(false)
watch(isAddStudentModalVisible, (newVal) => {
  if (newVal) {
    studentFilter.searchQuery = ''
    studentFilter.page = 1
    studentPagination.current = 1
    // Cập nhật cả danh sách sinh viên tổng và danh sách đã có trong nhóm
    fetchExistingStudents()
    fetchAllStudents()
  }
})

onMounted(() => {
  breadcrumbStore.setRoutes(breadcrumb.value)
  fetchStudentFactories()
  fetchExistingStudents()
  fetchAllStudents()
})
</script>

<template>
  <div class="container-fluid">
    <!-- Bộ lọc tìm kiếm -->
    <div class="row g-3">
      <div class="col-12">
        <a-card :bordered="false" class="cart mb-3">
          <template #title> <FilterFilled /> Bộ lọc </template>
          <a-row :gutter="16" class="filter-container row">
            <a-col :span="6" class="col">
              <div class="label-title">Tìm kiếm mã, tên, email:</div>
              <a-input
                v-model:value="filter.searchQuery"
                placeholder="Mã, tên hoặc email sinh viên"
                allowClear
                @change="fetchStudentFactories"
              />
            </a-col>
            <a-col :span="6" class="col">
              <div class="label-title">Trạng thái:</div>
              <a-select
                v-model:value="filter.status"
                placeholder="Chọn trạng thái"
                allowClear
                style="width: 100%"
                @change="fetchStudentFactories"
              >
                <a-select-option :value="''">Tất cả trạng thái</a-select-option>
                <a-select-option value="1">Đang học</a-select-option>
                <a-select-option value="0">Ngưng học</a-select-option>
              </a-select>
            </a-col>
          </a-row>
        </a-card>
      </div>
    </div>

    <!-- Bảng danh sách sinh viên trong nhóm xưởng -->
    <div class="row g-3">
      <div class="col-12">
        <a-card :bordered="false" class="cart">
          <template #title> <UnorderedListOutlined /> Danh sách sinh viên </template>
          <div class="d-flex justify-content-end mb-3 flex-wrap gap-3">
            <ExcelUploadButton v-bind="configImportExcel" />
            <a-tooltip title="Thêm học sinh vào nhóm xưởng">
              <a-button type="primary" @click="isAddStudentModalVisible = true">
                <PlusOutlined /> Thêm học sinh
              </a-button>
            </a-tooltip>
          </div>
          <a-table
            :dataSource="studentFactories"
            :columns="columns"
            rowKey="studentFactoryId"
            :pagination="pagination"
            :scroll="{ y: 500, x: 'auto' }"
            :loading="isLoading"
            @change="handleTableChange"
          >
            <template #bodyCell="{ column, record, index }">
              <template v-if="column.dataIndex">
                <template v-if="column.dataIndex === 'rowNumber'">
                  {{ index + 1 }}
                </template>
                <template v-else-if="column.dataIndex === 'statusStudentFactory'">
                  <span class="nowrap">
                    <a-switch
                      class="me-2"
                      :checked="
                        record.statusStudentFactory === 'ACTIVE' ||
                        record.statusStudentFactory === 1
                      "
                      @change="confirmChangeStatus(record)"
                    />
                    <a-tag
                      :color="
                        record.statusStudentFactory === 'ACTIVE' ||
                        record.statusStudentFactory === 1
                          ? 'green'
                          : 'red'
                      "
                    >
                      {{
                        record.statusStudentFactory === 'ACTIVE' ||
                        record.statusStudentFactory === 1
                          ? 'Đang học'
                          : 'Ngưng học'
                      }}
                    </a-tag>
                  </span>
                </template>
                <template v-else>
                  {{ record[column.dataIndex] }}
                </template>
              </template>
              <template v-else-if="column.key === 'actions'">
                <a-space>
                  <a-tooltip title="Cấp quyền thay đổi mặt sinh viên">
                    <a-button
                      type="text"
                      class="btn-outline-info"
                      @click="changeFaceStudent(record)"
                    >
                      <UserDeleteOutlined />
                    </a-button>
                  </a-tooltip>
                </a-space>
              </template>
            </template>
          </a-table>
        </a-card>
      </div>
    </div>

    <!-- Modal "Thêm học sinh vào nhóm xưởng" -->
    <a-modal
      v-model:open="isAddStudentModalVisible"
      title="Thêm học sinh vào nhóm xưởng"
      width="80%"
      @cancel="resetStudentModal"
      @ok="handleAddStudents"
    >
      <div class="row">
        <div class="col-12">
          <!-- Bộ lọc cho modal danh sách tất cả sinh viên -->
          <a-row :gutter="16" class="filter-container row" style="margin-bottom: 16px">
            <a-col :span="21" class="col">
              <a-input
                v-model:value="studentFilter.searchQuery"
                placeholder="Mã, tên hoặc email sinh viên"
                allowClear
                @change="fetchAllStudents"
              />
            </a-col>
          </a-row>
        </div>
      </div>
      <!-- Bảng danh sách tất cả sinh viên -->
      <a-table
        :key="isAddStudentModalVisible"
        :dataSource="allStudents"
        :columns="studentColumns"
        rowKey="id"
        bordered
        :pagination="studentPagination"
        @change="handleStudentTableChange"
        :loading="isLoading"
        :scroll="{ y: 500, x: 'auto' }"
      >
        <template #bodyCell="{ column, record, index }">
          <template v-if="column.dataIndex">
            <template v-if="column.dataIndex === 'rowNumber'">
              {{ index + 1 }}
            </template>
            <template v-else>
              {{ record[column.dataIndex] }}
            </template>
          </template>
          <template v-else-if="column.key === 'select'">
            <a-checkbox
              :checked="
                selectedStudents[record.id] !== undefined
                  ? selectedStudents[record.id]
                  : record.checked
              "
              @change="(e) => handleStudentCheckboxChange(record, e.target.checked)"
            />
          </template>
        </template>
      </a-table>
    </a-modal>
  </div>
</template>
