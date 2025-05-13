<script setup>
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import requestAPI from '@/services/requestApiService'
import { API_ROUTES_TEACHER } from '@/constants/teacherConstant'
import {
  FilterFilled,
  UnorderedListOutlined,
  AlignLeftOutlined,
  SearchOutlined,
} from '@ant-design/icons-vue'
import { ROUTE_NAMES } from '@/router/teacherRoute'
import { GLOBAL_ROUTE_NAMES } from '@/constants/routesConstant'
import useBreadcrumbStore from '@/stores/useBreadCrumbStore'
import useLoadingStore from '@/stores/useLoadingStore'
import { DEFAULT_PAGINATION } from '@/constants'
import { autoAddColumnWidth } from '@/utils/utils'

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
    breadcrumbName: 'Nhóm xưởng của tôi',
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
  semesterId: null,
  page: 1,
  pageSize: 5,
})
const pagination = reactive({
  ...DEFAULT_PAGINATION,
})

// Column configuration
const columns = ref(
  autoAddColumnWidth([
    { title: '#', dataIndex: 'rowNumber', key: 'rowNumber' },
    {
      title: 'Tên nhóm xưởng',
      dataIndex: 'factoryName',
      key: 'factoryName',
    },
    { title: 'Tên dự án', dataIndex: 'projectName', key: 'projectName' },
    {
      title: 'Mô tả',
      dataIndex: 'factoryDescription',
      key: 'factoryDescription',
    },
    {
      title: 'Trạng thái',
      dataIndex: 'factoryStatus',
      key: 'factoryStatus',
    },
    { title: 'Chức năng', key: 'actions' },
  ]),
)

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
const semesters = ref([])
const fetchSemesters = () => {
  loadingStore.show()
  requestAPI
    .get(API_ROUTES_TEACHER.FETCH_DATA_STUDENT + '/semesters')
    .then((response) => {
      console.log('Projects response:', response.data)
      const result = response.data.data
      semesters.value = result
    })
    .catch((error) => {
      message.error(error.response?.data?.message || 'Lỗi khi lấy danh sách học kỳ')
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

const handleClearFilter = () => {
  // Clear all filter values
  Object.keys(filter).forEach((key) => {
    if (key !== 'page' && key !== 'pageSize') {
      // Keep pagination values
      filter[key] = ''
    }
  })
  pagination.current = 1
  fetchFactoryByTeacher()
}

onMounted(() => {
  breadcrumbStore.setRoutes(breadcrumb.value)
  fetchFactoryByTeacher()
  fetchProjectByFacility()
  fetchSemesters()
})
</script>

<template>
  <div class="container-fluid">
    <div class="row g-3">
      <div class="col-12">
        <a-card :bordered="false" class="cart mb-3">
          <template #title> <FilterFilled /> Bộ lọc</template>
          <div class="row g-3 filter-container">
            <div class="col-xl-6 col-md-12 col-sm-12">
              <div class="label-title">Từ khoá:</div>
              <a-input
                v-model:value="filter.factoryName"
                placeholder="Tìm theo tên nhóm xưởng"
                allowClear
                @change="fetchFactoryByTeacher"
              >
                <template #prefix>
                  <SearchOutlined />
                </template>
              </a-input>
            </div>
            <div class="col-xl-3 col-md-6 col-sm-6">
              <div class="label-title">Dự án:</div>
              <a-select
                v-model:value="filter.projectId"
                placeholder="Chọn dự án"
                allowClear
                class="w-100"
                @change="fetchFactoryByTeacher"
              >
                <a-select-option :value="''">Tất cả dự án</a-select-option>
                <a-select-option v-for="item in projects" :key="item.id" :value="item.id">
                  {{ item.name }}
                </a-select-option>
              </a-select>
            </div>
            <div class="col-xl-3 col-md-6 col-sm-6">
              <div class="label-title">Học kỳ:</div>
              <a-select
                v-model:value="filter.semesterId"
                placeholder="Chọn học kỳ"
                allowClear
                class="w-100"
                @change="fetchFactoryByTeacher"
              >
                <a-select-option :value="null">Tất cả học kỳ</a-select-option>
                <a-select-option v-for="item in semesters" :key="item.id" :value="item.id">
                  {{ item.code }}
                </a-select-option>
              </a-select>
            </div>
          </div>
          <div class="row">
            <div class="col-12">
              <div class="d-flex justify-content-center flex-wrap gap-2 mt-3">
                <a-button class="btn-light" @click="fetchFactoryByTeacher">
                  <FilterFilled /> Lọc
                </a-button>
                <a-button class="btn-gray" @click="handleClearFilter"> Huỷ lọc </a-button>
              </div>
            </div>
          </div>
        </a-card>
      </div>
    </div>

    <!-- Danh sách nhóm xưởng -->
    <div class="row g-3">
      <div class="col-12">
        <a-card :bordered="false" class="cart">
          <template #title> <UnorderedListOutlined /> Danh sách nhóm xưởng </template>
          <a-table
            class="nowrap"
            :loading="isLoading"
            :dataSource="factories"
            :columns="columns"
            rowKey="factoryId"
            :pagination="pagination"
            :scroll="{ x: 'auto' }"
            @change="handleTableChange"
          >
            <template #bodyCell="{ column, record, index }">
              <template v-if="column.dataIndex">
                <template v-if="column.dataIndex === 'rowNumber'">
                  {{ index + 1 }}
                </template>
                <template v-else-if="column.dataIndex === 'factoryName'">
                  <a @click="handleDetailFactory(record)">{{ record.factoryName }}</a>
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
                      <AlignLeftOutlined />
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
