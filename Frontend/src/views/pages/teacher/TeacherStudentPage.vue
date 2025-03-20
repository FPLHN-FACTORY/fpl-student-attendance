<script setup>
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import requestAPI from '@/services/requestApiService'
import { API_ROUTES_TEACHER } from '@/constants/teacherConstant'
import {
  PlusOutlined,
  EyeOutlined,
  EditOutlined,
  SyncOutlined,
  EyeFilled,
  FilterFilled,
  UnorderedListOutlined,
} from '@ant-design/icons-vue'
import { ROUTE_NAMES } from '@/router/teacherRoute'
import { GLOBAL_ROUTE_NAMES } from '@/constants/routesConstant'
import useBreadcrumbStore from '@/stores/useBreadCrumbStore'
import useLoadingStore from '@/stores/useLoadingStore'
import { DEFAULT_PAGINATION } from '@/constants'

const router = useRouter()
const breadcrumbStore = useBreadcrumbStore()
const loadingStore = useLoadingStore()
const isLoading = ref(false)
const breadcrumb = ref([
  {
    name: GLOBAL_ROUTE_NAMES.TEACHER_PAGE,
    breadcrumbName: 'Giảng viên',
  },
  {
    name: ROUTE_NAMES.MANAGEMENT_STUDENT,
    breadcrumbName: 'Nhóm xưởng',
  },
])

// Danh sách nhóm xưởng
const factories = ref([])
// Danh sách dự án, giảng viên (để hiển thị trong combobox filter)
const projects = ref([])

// Filter & phân trang
const filter = reactive({
  factoryName: '',
  projectId: '',
  page: 1,
  pageSize: 5,
})
const pagination = reactive({
  ...DEFAULT_PAGINATION,
})

// Column configuration
const columns = ref([
  { title: '#', dataIndex: 'rowNumber', key: 'rowNumber', width: 50 },
  { title: 'Tên nhóm xưởng', dataIndex: 'factoryName', key: 'factoryName', width: 150 },
  { title: 'Tên dự án', dataIndex: 'projectName', key: 'projectName', width: 300 },
  { title: 'Mô tả', dataIndex: 'factoryDescription', key: 'factoryDescription', width: 300 },
  { title: 'Trạng thái', dataIndex: 'factoryStatus', key: 'factoryStatus', width: 120 },
  { title: 'Chức năng', key: 'actions', width: 100 },
])

// Fetch danh sách nhóm xưởng do giảng viên quản lý
const fetchFactoryByTeacher = () => {
  loadingStore.show()
  requestAPI
    .get(API_ROUTES_TEACHER.FETCH_DATA_STUDENT, {
      params: { ...filter, page: pagination.current, size: pagination.pageSize },
    })
    .then((response) => {
      console.log('Factory response:', response.data)
      const result = response.data.data
      factories.value = result.data
      // Nếu API trả về tổng số trang, sử dụng:
      pagination.total = result.totalPages * filter.pageSize
      // Nếu trả về tổng số bản ghi, thay thế bằng: pagination.total = result.totalRecords
      pagination.current = filter.page
    })
    .catch((error) => {
      message.error(error.response?.data?.message || 'Lỗi khi lấy danh sách nhóm xưởng')
    })
    .finally(() => {
      loadingStore.hide()
    })
}

// Fetch danh sách dự án theo cơ sở
const fetchProjectByFacility = () => {
  loadingStore.show()
  requestAPI
    .get(API_ROUTES_TEACHER.FETCH_DATA_STUDENT + '/projects')
    .then((response) => {
      console.log('Projects response:', response.data)
      const result = response.data.data
      projects.value = result
    })
    .catch((error) => {
      message.error(error.response?.data?.message || 'Lỗi khi lấy danh sách dự án')
    })
    .finally(() => {
      loadingStore.hide()
    })
}

// Xử lý phân trang
const handleTableChange = (pageInfo) => {
  pagination.current = pageInfo.current
  pagination.pageSize = pageInfo.pageSize
  filter.page = pageInfo.current
  filter.pageSize = pageInfo.pageSize // Đồng bộ pageSize cho filter
  fetchFactoryByTeacher()
}

// Khi nhấn nút "Chi tiết", chuyển hướng sang trang quản lý sinh viên của nhóm xưởng
const handleDetailFactory = (record) => {
  console.log('Detail record:', record)
  router.push({
    name: ROUTE_NAMES.MANAGEMENT_STUDENT_FACTORY,
    query: {
      factoryId: record.factoryId || record.id,
      factoryName: record.factoryName || record.name,
    },
  })
}

onMounted(() => {
  breadcrumbStore.setRoutes(breadcrumb.value)
  fetchFactoryByTeacher()
  fetchProjectByFacility()
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
                <label class="form-label">Tên nhóm xưởng</label>
                <a-input
                  v-model:value="filter.factoryName"
                  placeholder="Nhập tên nhóm xưởng"
                  allowClear
                  @change="fetchFactoryByTeacher"
                />
              </div>
            </a-col>
            <a-col :span="12" class="col">
              <div class="form-group">
                <label class="form-label">Tên dự án</label>
                <a-select
                  v-model:value="filter.projectId"
                  placeholder="Chọn dự án"
                  allowClear
                  style="width: 100%"
                  @change="fetchFactoryByTeacher"
                >
                  <a-select-option :value="''">Tất cả dự án</a-select-option>
                  <a-select-option v-for="item in projects" :key="item.id" :value="item.id">
                    {{ item.name }}
                  </a-select-option>
                </a-select>
              </div>
            </a-col>
          </a-row>
        </a-card>
      </div>
    </div>

    <!-- Danh sách nhóm xưởng -->
    <div class="row g-3">
      <div class="col-12">
        <a-card :bordered="false" class="cart">
          <template #title> <UnorderedListOutlined /> Danh sách nhóm xưởng </template>
          <a-table
            :loading="isLoading"
            :dataSource="factories"
            :columns="columns"
            rowKey="factoryId"
            :pagination="pagination"
            :scroll="{ y: 500, x: 'auto' }"
            @change="handleTableChange"
          >
            <template #bodyCell="{ column, record, index }">
              <template v-if="column.dataIndex">
                <template v-if="column.dataIndex === 'rowNumber'">
                  {{ index + 1 }}
                </template>
                <template v-else-if="column.dataIndex === 'factoryStatus'">
                  <a-tag
                    :color="
                      record.factoryStatus === 'ACTIVE' || record.factoryStatus === 1
                        ? 'green'
                        : 'red'
                    "
                  >
                    {{
                      record.factoryStatus === 'ACTIVE' || record.factoryStatus === 1
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
                  <a-tooltip title="Quản lý sinh viên nhóm xưởng">
                    <a-button
                      type="text"
                      class="btn-outline-primary"
                      @click="handleDetailFactory(record)"
                    >
                      <EyeFilled />
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
