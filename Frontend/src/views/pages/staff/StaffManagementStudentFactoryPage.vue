<script setup>
import { ref, reactive, onMounted } from 'vue'
import { message, Modal } from 'ant-design-vue'
import requestAPI from '@/services/requestApiService'
import { API_ROUTES_STAFF } from '@/constants/staffConstant'
import { PlusOutlined, DeleteOutlined, SyncOutlined, DeleteFilled } from '@ant-design/icons-vue'
import { useRoute } from 'vue-router'
import { DEFAULT_PAGINATION } from '@/constants/paginationConstant'
import useBreadcrumbStore from '@/stores/useBreadCrumbStore'
import { GLOBAL_ROUTE_NAMES } from '@/constants/routesConstant'
import useLoadingStore from '@/stores/useLoadingStore'
import { ROUTE_NAMES } from '@/router/staffRoute'

const route = useRoute()
const factoryId = route.query.factoryId
const factoryName = route.query.factoryName

const breadcrumbStore = useBreadcrumbStore()
const loadingStore = useLoadingStore()

const isLoading = ref(false)

if (!factoryId) {
  message.error('Không tìm thấy factoryId')
}

// -------------------- Danh sách sinh viên đã có trong nhóm xưởng --------------------
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
  { title: 'STT', dataIndex: 'rowNumber', key: 'rowNumber' },
  { title: 'Mã sinh viên', dataIndex: 'studentCode', key: 'studentCode' },
  { title: 'Tên sinh viên', dataIndex: 'studentName', key: 'studentName' },
  { title: 'Email sinh viên', dataIndex: 'studentEmail', key: 'studentEmail' },
  { title: 'Trạng thái', dataIndex: 'statusStudentFactory', key: 'statusStudentFactory' },
  { title: 'Chức năng', key: 'actions', width: 80 },
])

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

const fetchStudentFactories = () => {
  requestAPI
    .get(API_ROUTES_STAFF.FETCH_DATA_STUDENT_FACTORY + '/' + factoryId, {
      params: {
        ...filter,
        factoryId,
        'userStudentRequest.searchQuery': filter.searchQuery,
      },
    })
    .then((response) => {
      const result = response.data.data
      studentFactories.value = result.data
      pagination.total = result.totalPages * filter.pageSize
      pagination.current = filter.page
    })
    .catch((error) => {
      message.error(
        error.response?.data?.message || 'Lỗi khi lấy danh sách sinh viên trong nhóm xưởng'
      )
    })
}

// -------------------- Lấy danh sách sinh viên đã có trong nhóm (để tích checkbox) --------------------
const existingStudents = ref([])

const fetchExistingStudents = () => {
  requestAPI
    .get(API_ROUTES_STAFF.FETCH_DATA_STUDENT_FACTORY + '/exist-student/' + factoryId)
    .then((response) => {
      existingStudents.value = response.data.data || []
      breadcrumbStore.push({
        breadcrumbName: factoryName,
      })
      if (allStudents.value.length) {
        updateAllStudentsCheckStatus()
      }
    })
    .catch((error) => {
      message.error(
        error.response?.data?.message || 'Lỗi khi lấy danh sách sinh viên đã có trong nhóm xưởng'
      )
    })
}

// -------------------- Danh sách tất cả sinh viên (modal thêm học sinh) --------------------
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
  { title: 'STT', dataIndex: 'rowNumber', key: 'rowNumber' },
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
  requestAPI
    .get(API_ROUTES_STAFF.FETCH_DATA_STUDENT_FACTORY + '/students', {
      params: {
        ...studentFilter,
        factoryId,
        'userStudentRequest.searchQuery': studentFilter.searchQuery,
      },
    })
    .then((response) => {
      const result = response.data.data
      allStudents.value = result.data.map((item) => ({
        ...item,
        checked: false,
      }))
      studentPagination.total = result.totalPages * studentFilter.pageSize
      studentPagination.current = studentFilter.page
      updateAllStudentsCheckStatus()
    })
    .catch((error) => {
      message.error(error.response?.data?.message || 'Lỗi khi lấy danh sách sinh viên')
    })
}

const handleStudentTableChange = (page) => {
  studentPagination.current = page.current
  studentPagination.pageSize = page.pageSize
  fetchAllStudents()
}

const handleStudentCheckboxChange = (student, checked) => {
  selectedStudents[student.id] = checked
  if (checked) {
    const payload = {
      studentId: student.id,
      factoryId: factoryId,
    }
    requestAPI
      .post(API_ROUTES_STAFF.FETCH_DATA_STUDENT_FACTORY, payload)
      .then((response) => {
        message.success(response.data.message || 'Thêm sinh viên vào nhóm xưởng thành công')
        fetchStudentFactories()
      })
      .catch((error) => {
        message.error(error.response?.data?.message || 'Lỗi khi thêm sinh viên vào nhóm xưởng')
      })
  } else {
    const existing = existingStudents.value.find((item) => item.studentId === student.id)
    if (existing && existing.studentFactoryId) {
      requestAPI
        .delete(API_ROUTES_STAFF.FETCH_DATA_STUDENT_FACTORY + '/' + existing.studentFactoryId)
        .then((response) => {
          message.success(response.data.message || 'Xóa sinh viên khỏi nhóm xưởng thành công')
          fetchStudentFactories()
        })
        .catch((error) => {
          message.error(error.response?.data?.message || 'Lỗi khi xóa sinh viên khỏi nhóm xưởng')
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
  for (const key in selectedStudents) {
    selectedStudents[key] = false
  }
}

const handleTableChange = (page) => {
  pagination.current = page.current
  pagination.pageSize = page.pageSize
  fetchStudentFactories()
}

const confirmDeleteStudent = (record) => {
  Modal.confirm({
    title: 'Xác nhận xóa',
    content: `Bạn có chắc muốn xóa sinh viên ${record.studentName} khỏi nhóm xưởng?`,
    onOk() {
      deleteStudentFactory(record.studentFactoryId)
    },
  })
}

const deleteStudentFactory = (studentFactoryId) => {
  requestAPI
    .delete(API_ROUTES_STAFF.FETCH_DATA_STUDENT_FACTORY + '/' + studentFactoryId)
    .then((response) => {
      message.success(response.data.message || 'Xóa sinh viên thành công')
      fetchStudentFactories()
    })
    .catch((error) => {
      message.error(error.response?.data?.message || 'Lỗi khi xóa sinh viên khỏi nhóm xưởng')
    })
}

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
  requestAPI
    .put(API_ROUTES_STAFF.FETCH_DATA_STUDENT_FACTORY + '/' + studentFactoryId)
    .then((response) => {
      message.success(response.data.message || 'Đổi trạng thái thành công')
      fetchStudentFactories()
    })
    .catch((error) => {
      message.error(error.response?.data?.message || 'Lỗi khi đổi trạng thái sinh viên')
    })
}

const isAddStudentModalVisible = ref(false)

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
            <a-col :span="12" class="col">
              <a-input
                v-model:value="filter.searchQuery"
                placeholder="Mã, tên hoặc email sinh viên"
                allowClear
                @change="fetchStudentFactories"
              />
            </a-col>
            <a-col :span="12" class="col">
              <a-select
                v-model:value="filter.status"
                placeholder="Chọn trạng thái"
                allowClear
                style="width: 100%"
                @change="fetchStudentFactories"
              >
                <a-select-option :value="''">Tất cả trạng thái</a-select-option>
                <a-select-option value="1">Hoạt động</a-select-option>
                <a-select-option value="0">Không hoạt động</a-select-option>
              </a-select>
            </a-col>
          </a-row>
        </a-card>
      </div>
    </div>

    <div class="row g-3">
      <div class="col-12">
        <a-card :bordered="false" class="cart">
          <template #title> <UnorderedListOutlined /> Danh sách sinh viên </template>
          <div class="d-flex justify-content-end mb-3">
            <a-tooltip title="Thêm học sinh vào nhóm xưởng">
              <a-button type="primary" @click="isAddStudentModalVisible = true">
                <PlusOutlined /> Thêm học sinh
              </a-button>
            </a-tooltip>
          </div>
          <a-table
            class="nowrap"
            :loading="isLoading"
            :scroll="{ y: 500, x: 'auto' }"
            :dataSource="studentFactories"
            :columns="columns"
            rowKey="studentFactoryId"
            bordered
            :pagination="pagination"
            @change="handleTableChange"
          >
            <template #bodyCell="{ column, record, index }">
              <template v-if="column.dataIndex">
                <!-- STT -->
                <template v-if="column.dataIndex === 'rowNumber'">
                  {{ index + 1 }}
                </template>
                <!-- Hiển thị trạng thái với tag -->
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
                <!-- Các cột khác hiển thị giá trị -->
                <template v-else>
                  {{ record[column.dataIndex] }}
                </template>
              </template>
              <!-- Cột chức năng -->
              <template v-else-if="column.key === 'actions'">
                <a-space>
                  <a-tooltip title="Xóa sinh viên khỏi nhóm xưởng">
                    <a-button
                      type="text"
                      class="btn-outline-danger"
                      @click="confirmDeleteStudent(record)"
                    >
                      <DeleteFilled />
                    </a-button>
                  </a-tooltip>
                  <a-tooltip title="Đổi trạng thái sinh viên">
                    <a-button
                      type="text"
                      class="btn-outline-warning"
                      @click="confirmChangeStatus(record)"
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
        :dataSource="allStudents"
        :columns="studentColumns"
        class="nowrap"
        rowKey="id"
        bordered
        :pagination="studentPagination"
        @change="handleStudentTableChange"
        :loading="isLoading"
        :scroll="{ y: 500, x: 'auto' }"
      >
        <template #bodyCell="{ column, record, index }">
          <template v-if="column.dataIndex">
            <!-- STT -->
            <template v-if="column.dataIndex === 'rowNumber'">
              {{ index + 1 }}
            </template>
            <!-- Các cột khác -->
            <template v-else>
              {{ record[column.dataIndex] }}
            </template>
          </template>
          <!-- Cột checkbox -->
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
