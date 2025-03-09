<template>
  <h1>Quản lý nhóm xưởng</h1>

  Bộ lọc tìm kiếm
  <a-card title="Bộ lọc" :bordered="false" class="cart">
    <a-row :gutter="16" class="filter-container">
      <!-- Input tìm kiếm theo tên xưởng, tên dự án, mã bộ môn, tên cơ sở, tên nhân viên -->
      <a-col :span="8">
        <a-input
          v-model:value="filter.searchQuery"
          placeholder="Tìm kiếm theo tên xưởng, dự án, bộ môn, nhân viên"
          allowClear
          @change="fetchFactories"
        />
      </a-col>
      <!-- Combobox trạng thái -->
      <a-col :span="8">
        <a-select
          v-model:value="filter.status"
          placeholder="Chọn trạng thái"
          allowClear
          style="width: 100%"
          @change="fetchFactories"
        >
          <a-select-option :value="''">Tất cả trạng thái</a-select-option>
          <a-select-option value="ACTIVE">Hoạt động</a-select-option>
          <a-select-option value="INACTIVE">Không hoạt động</a-select-option>
        </a-select>
      </a-col>
      <!-- Combobox cơ sở -->
      <a-col :span="8">
        <a-select
          v-model:value="filter.idFacility"
          placeholder="Chọn cơ sở"
          allowClear
          style="width: 100%"
          @change="fetchFactories"
        >
          <a-select-option :value="''">Tất cả cơ sở</a-select-option>
          <a-select-option
            v-for="facility in facilitiesList"
            :key="facility.id"
            :value="facility.id"
          >
            {{ facility.name }}
          </a-select-option>
        </a-select>
      </a-col>
    </a-row>
  </a-card>

  <!-- Danh sách nhóm xưởng -->
  <a-card title="Danh sách nhóm xưởng" :bordered="false" class="cart">
    <div style="display: flex; justify-content: flex-end; margin-bottom: 10px">
      <!-- Nút thêm xưởng (chưa triển khai) -->
      <a-tooltip title="Thêm mới xưởng">
        <a-button
          style="background-color: #fff7e6; color: black; border: 1px solid #ffa940"
          @click="handleAddFactory"
        >
          <PlusOutlined />
          Thêm
        </a-button>
      </a-tooltip>
    </div>
    <a-table
      :dataSource="factories"
      :columns="columns"
      rowKey="factoryId"
      bordered
      :pagination="pagination"
      @change="handleTableChange"
    >
      <template #bodyCell="{ column, record, index }">
        <!-- STT -->
        <template v-if="column.dataIndex === 'rowNumber'">
          {{ index + 1 }}
        </template>
        <!-- Tên xưởng -->
        <template v-else-if="column.dataIndex === 'factoryName'">
          {{ record.factoryName }}
        </template>
        <!-- Trạng thái -->
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
        <!-- Tên dự án -->
        <template v-else-if="column.dataIndex === 'projectName'">
          {{ record.projectName }}
        </template>
        <!-- Mã bộ môn -->
        <template v-else-if="column.dataIndex === 'subjectCode'">
          {{ record.subjectCode }}
        </template>
        <!-- Tên cơ sở -->
        <template v-else-if="column.dataIndex === 'facilityName'">
          {{ record.facilityName }}
        </template>
        <!-- Tên giảng viên -->
        <template v-else-if="column.dataIndex === 'staffName'">
          {{ record.staffName }}
        </template>
        <!-- Ngày bắt đầu kế hoạch được format -->
        <template v-else-if="column.dataIndex === 'planStartDate'">
          {{ formatEpochToDate(record.planStartDate) }}
        </template>
        <!-- Ca học -->
        <template v-else-if="column.dataIndex === 'planShift'">
          {{ record.planShift }}
        </template>
        <!-- Cột hành động -->
        <template v-else-if="column.key === 'actions'">
          <a-tooltip title="Xem chi tiết">
            <a-button
              type="text"
              style="background-color: #fff7e6; border: 1px solid #ffa940"
              @click="handleViewDetails(record)"
            >
              <EyeOutlined />
            </a-button>
          </a-tooltip>
        </template>
        <!-- Mặc định -->
        <template v-else>
          {{ record[column.dataIndex] }}
        </template>
      </template>
    </a-table>
  </a-card>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { PlusOutlined, EyeOutlined } from '@ant-design/icons-vue'
import requestAPI from '@/services/requestApiService'
import dayjs from 'dayjs'
import { API_ROUTES_STAFF } from '@/constants/staffConstant'

const factories = ref([])

const facilitiesList = ref([])

const filter = reactive({
  searchQuery: '',
  status: '',
  idFacility: '',
  page: 1,
  pageSize: 5,
})

const pagination = reactive({
  current: 1,
  pageSize: 5,
  total: 0,
  showSizeChanger: false,
})

// Hàm format epoch sang định dạng DD/MM/YYYY
const formatEpochToDate = (epoch) => {
  if (!epoch) return ''
  return dayjs(epoch).format('DD/MM/YYYY')
}

const columns = ref([
  { title: 'STT', dataIndex: 'rowNumber', key: 'rowNumber' },
  { title: 'Tên xưởng', dataIndex: 'factoryName', key: 'factoryName' },
  { title: 'Trạng thái', dataIndex: 'factoryStatus', key: 'factoryStatus' },
  { title: 'Tên dự án', dataIndex: 'projectName', key: 'projectName' },
  { title: 'Mã bộ môn', dataIndex: 'subjectCode', key: 'subjectCode' },
  { title: 'Tên giảng viên', dataIndex: 'staffName', key: 'staffName' },
  { title: 'Ngày bắt đầu kế hoạch', dataIndex: 'planStartDate', key: 'planStartDate' },
  { title: 'Ca học', dataIndex: 'planShift', key: 'planShift' },
  { title: 'Chức năng', key: 'actions' },
])

// Hàm fetch danh sách xưởng từ backend qua getAll
const fetchFactories = () => {
  requestAPI
    .get(API_ROUTES_STAFF.FETCH_DATA_FACTORY, { params: filter })
    .then((response) => {
      // Giả sử API trả về dữ liệu theo cấu trúc: { data: { data: [...], totalPages } }
      factories.value = response.data.data.data
      pagination.total = response.data.data.totalPages * filter.pageSize
      pagination.current = filter.page
    })
    .catch((error) => {
      message.error(
        (error.response && error.response.data && error.response.data.message) ||
          'Lỗi khi lấy danh sách xưởng',
      )
    })
}

// Hàm fetch danh sách cơ sở (cho combobox lọc)
const fetchFacilitiesList = () => {}

// Sự kiện thay đổi trang bảng
const handleTableChange = (paginationData) => {
  filter.page = paginationData.current
  fetchFactories()
}

// Hàm xử lý thêm xưởng (chưa triển khai)
const handleAddFactory = () => {
  message.info('Chức năng Thêm xưởng chưa triển khai')
}

// Hàm xử lý xem chi tiết xưởng (có thể mở modal hoặc chuyển trang)
const handleViewDetails = (record) => {
  message.info(`Xem chi tiết xưởng ${record.factoryName} (chưa triển khai)`)
}

onMounted(() => {
  fetchFactories()
  fetchFacilitiesList()
})
</script>

<style scoped>
.cart {
  margin-top: 5px;
}
.filter-container {
  margin-bottom: 5px;
}
</style>
