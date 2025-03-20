<script setup>
import {
  DeleteFilled,
  DeleteOutlined,
  FilterFilled,
  SyncOutlined,
  UnorderedListOutlined,
} from '@ant-design/icons-vue'
import { ref, reactive, onMounted } from 'vue'
import { message, Modal } from 'ant-design-vue'
import { useRoute } from 'vue-router'
import requestAPI from '@/services/requestApiService'
import { API_ROUTES_TEACHER } from '@/constants/teacherConstant'
import { ROUTE_NAMES } from '@/router/teacherRoute'
import { GLOBAL_ROUTE_NAMES } from '@/constants/routesConstant'
import useBreadcrumbStore from '@/stores/useBreadCrumbStore'
import useLoadingStore from '@/stores/useLoadingStore'
import { DEFAULT_PAGINATION } from '@/constants'

const route = useRoute()
const factoryId = route.query.factoryId
const factoryName = route.query.factoryName

const breadcrumbStore = useBreadcrumbStore()

const breadcrumb = ref([
  {
    name: GLOBAL_ROUTE_NAMES.TEACHER_PAGE,
    breadcrumbName: 'Giảng viên',
  },
  {
    name: ROUTE_NAMES.MANAGEMENT_STUDENT,
    breadcrumbName: 'Nhóm xưởng',
  },
  {
    name: ROUTE_NAMES.MANAGEMENT_STUDENT_FACTORY,
    breadcrumbName: 'Sinh viên nhóm: ' + factoryName,
  },
])

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
  ...DEFAULT_PAGINATION,
})

// Cấu hình cột cho bảng
const columns = ref([
  { title: '#', dataIndex: 'rowNumber', key: 'rowNumber', width: 50 },
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

const loadingStore = useLoadingStore()

// Hàm lấy danh sách học sinh trong nhóm xưởng
const fetchStudentFactory = () => {
  loadingStore.show()
  requestAPI
    .get(API_ROUTES_TEACHER.FETCH_DATA_STUDENT_FACTORY, {
      params: {
        ...filter,
        page: pagination.current,
        size: pagination.pageSize,
      },
    })
    .then((response) => {
      const result = response.data.data
      students.value = result.data
      // Nếu API trả về tổng số trang, sử dụng:
      pagination.total = result.totalPages * filter.pageSize
      // Nếu trả về tổng số bản ghi, thay thế bằng: pagination.total = result.totalRecords
      pagination.current = filter.page
    })
    .catch((error) => {
      message.error(error.response?.data?.message || 'Lỗi khi lấy danh sách học sinh')
    })
    .finally(() => {
      loadingStore.hide()
    })
}

// Xử lý thay đổi trang bảng
const handleTableChange = (pageInfo) => {
  pagination.current = pageInfo.current
  pagination.pageSize = pageInfo.pageSize
  filter.page = pageInfo.current
  filter.pageSize = pageInfo.pageSize // Đồng bộ pageSize cho filter
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
  loadingStore.show()
  requestAPI
    .delete(API_ROUTES_TEACHER.FETCH_DATA_STUDENT_FACTORY + '/' + studentFactoryId)
    .then((response) => {
      message.success(response.data.message || 'Xoá học sinh thành công')
      fetchStudentFactory()
    })
    .catch((error) => {
      message.error(error.response?.data?.message || 'Lỗi khi xoá học sinh')
    })
    .finally(() => {
      loadingStore.hide()
    })
}

const toggleStatusStudentFactory = (record) => {
  Modal.confirm({
    title: 'Xác nhận đổi trạng thái',
    content: `Bạn có chắc muốn thay đổi trạng thái của học sinh ${record.studentName}?`,
    onOk() {
      loadingStore.show()
      requestAPI
        .put(API_ROUTES_TEACHER.FETCH_DATA_STUDENT_FACTORY + '/' + record.studentFactoryId)
        .then((response) => {
          message.success(response.data.message || 'Trạng thái đã được cập nhật thành công')
          fetchStudentFactory() // Làm mới danh sách sau khi đổi trạng thái
        })
        .catch((error) => {
          message.error(error.response?.data?.message || 'Lỗi khi đổi trạng thái sinh viên')
        })
        .finally(() => {
          loadingStore.hide()
        })
    },
  })
}

onMounted(() => {
  breadcrumbStore.setRoutes(breadcrumb.value)
  fetchStudentFactory()
})
</script>

<template>
  <div class="container-fluid">
    <!-- Bộ lọc tìm kiếm -->
    <div class="row g-3">
      <div class="col-12">
        <a-card :bordered="false" class="cart mb-3">
          <template #title> <FilterFilled /> Bộ lọc tìm kiếm </template>
          <a-row :gutter="16" class="filter-container">
            <a-col :span="12" class="col">
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
            <a-col :span="12" class="col">
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
      </div>
    </div>

    <!-- Danh sách học sinh trong nhóm xưởng -->
    <div class="row g-3">
      <div class="col-12">
        <a-card :bordered="false" class="cart">
          <template #title> <UnorderedListOutlined /> Danh sách học sinh </template>
          <a-table
            :loading="isLoading"
            :dataSource="students"
            :columns="columns"
            rowKey="studentFactoryId"
            :pagination="pagination"
            @change="handleTableChange"
            :scroll="{ y: 500, x: 'auto' }"
          >
            <template #bodyCell="{ column, record, index }">
              <template v-if="column.dataIndex">
                <!-- STT -->
                <template v-if="column.dataIndex === 'rowNumber'">
                  {{ index + 1 }}
                </template>
                <!-- Hiển thị trạng thái -->
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
                <!-- Các cột khác -->
                <template v-else>
                  {{ record[column.dataIndex] }}
                </template>
              </template>
              <!-- Các nút hành động -->
              <template v-else-if="column.key === 'actions'">
                <a-space>
                  <a-tooltip title="Xoá học sinh">
                    <a-button
                      type="text"
                      class="btn-outline-danger me-2"
                      @click="confirmDeleteStudent(record)"
                    >
                      <DeleteFilled />
                    </a-button>
                  </a-tooltip>
                  <a-tooltip title="Đổi trạng thái">
                    <a-button
                      type="text"
                      class="btn-outline-warning"
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
    </div>
  </div>
</template>
