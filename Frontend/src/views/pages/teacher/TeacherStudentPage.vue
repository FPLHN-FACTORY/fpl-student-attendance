<template>
  <div>
    <h1>Quản lý nhóm xưởng</h1>
    <!-- Bộ lọc tìm kiếm -->
    <a-card title="Bộ lọc tìm kiếm" :bordered="false" class="cart">
      <a-row gutter="16" class="filter-container">
        <a-col :span="12">
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
        <a-col :span="12">
          <div class="form-group">
            <label class="form-label">Tên dự án</label>
            <a-select
              v-model:value="filter.projectName"
              placeholder="Chọn dự án"
              allowClear
              style="width: 100%"
              @change="fetchProjectByFacility"
            >
              <a-select-option :value="''">Tất cả dự án</a-select-option>
              <a-select-option v-for="item in projects" :key="item.id" :value="item.name">
                {{ item.name }}
              </a-select-option>
            </a-select>
          </div>
        </a-col>
      </a-row>
    </a-card>

    <!-- Danh sách nhóm xưởng -->
    <a-card title="Danh sách nhóm xưởng" :bordered="false" class="cart">
      <a-table
        :dataSource="factories"
        :columns="columns"
        rowKey="factoryId"
        bordered
        :pagination="pagination"
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
                  record.factoryStatus === 'ACTIVE' || record.factoryStatus === 1 ? 'green' : 'red'
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
              <!-- Nút Chi tiết: chuyển sang trang quản lý sinh viên của nhóm xưởng -->
              <a-tooltip title="Quản lý sinh viên nhóm xưởng">
                <a-button type="text" class="action-button" @click="handleDetailFactory(record)">
                  <EyeOutlined />
                </a-button>
              </a-tooltip>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import requestAPI from '@/services/requestApiService'
import { API_ROUTES_TEACHER } from '@/constants/teacherConstant'
import { PlusOutlined, EyeOutlined, EditOutlined, SyncOutlined } from '@ant-design/icons-vue'
import { ROUTE_NAMES } from '@/router/teacherRoute'

const router = useRouter()

// Danh sách nhóm xưởng
const factories = ref([])
// Danh sách dự án, giảng viên (để hiển thị trong combobox filter)
const projects = ref([])

// Filter & phân trang
const filter = reactive({
  factoryName: '',
  projectName: '',
  page: 1,
  pageSize: 5,
})
const pagination = reactive({
  current: 1,
  pageSize: 5,
  total: 0,
  showSizeChanger: false,
})

// Column configuration
const columns = ref([
  { title: 'STT', dataIndex: 'rowNumber', key: 'rowNumber', width: 50 },
  { title: 'Tên nhóm xưởng', dataIndex: 'factoryName', key: 'factoryName', width: 150 },
  { title: 'Tên dự án', dataIndex: 'projectName', key: 'projectName', width: 300 },
  { title: 'Mô tả', dataIndex: 'factoryDescription', key: 'factoryDescription', width: 300 },
  { title: 'Trạng thái', dataIndex: 'factoryStatus', key: 'factoryStatus', width: 120 },
  { title: 'Chức năng', key: 'actions', width: 100 },
])

// Fetch danh sách nhóm xưởng do giảng viên quản lý
const fetchFactoryByTeacher = () => {
  requestAPI
    .get(API_ROUTES_TEACHER.FETCH_DATA_STUDENT, { params: filter })
    .then((response) => {
      console.log('Factory response:', response.data)
      const result = response.data.data
      factories.value = result.data
      pagination.total = result.totalPages * filter.pageSize
      pagination.current = filter.page
    })
    .catch((error) => {
      message.error(error.response?.data?.message || 'Lỗi khi lấy danh sách nhóm xưởng')
    })
}

// Fetch danh sách dự án theo cơ sở
const fetchProjectByFacility = () => {
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
}

// Xử lý phân trang
const handleTableChange = (paginationData) => {
  filter.page = paginationData.current
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
  fetchFactoryByTeacher()
  fetchProjectByFacility()
})
</script>

<style scoped>
.cart {
  margin-top: 10px;
}
.filter-container {
  margin-bottom: 10px;
}
.form-group {
  margin-bottom: 12px;
}
.form-label {
  display: block;
  margin-bottom: 4px;
  font-weight: 500;
}
.action-button {
  background-color: #fff7e6;
  color: black;
  border: 1px solid #ffa940;
  margin-right: 8px;
}
</style>
