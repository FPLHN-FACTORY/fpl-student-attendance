<template>
  <div>
    <h1>Quản lý nhóm xưởng</h1>

    <!-- Bộ lọc tìm kiếm -->
    <a-card title="Bộ lọc" :bordered="false" class="cart">
      <a-row :gutter="16" class="filter-container">
        <!-- Input tìm kiếm theo tên nhóm xưởng, dự án, bộ môn, giảng viên -->
        <a-col :span="12">
          <a-input
            v-model:value="filter.searchQuery"
            placeholder="Tên xưởng, dự án, bộ môn, giảng viên"
            allowClear
            @change="fetchFactories"
          />
        </a-col>
        <!-- Combobox trạng thái -->
        <a-col :span="12">
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
      </a-row>
    </a-card>

    <!-- Danh sách nhóm xưởng -->
    <a-card title="Danh sách nhóm xưởng" :bordered="false" class="cart">
      <div style="display: flex; justify-content: flex-end; margin-bottom: 10px">
        <a-tooltip title="Thêm nhóm xưởng">
          <a-button
            style="background-color: #fff7e6; color: black; border: 1px solid #ffa940"
            @click="modalAdd = true"
          >
            <PlusOutlined /> Thêm
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
              <!-- Nút Chi tiết: đẩy sang router khác -->
              <!-- <a-tooltip title="Chi tiết">
                <a-button type="text" class="action-button" @click="handleDetailFactory(record)">
                  <EyeOutlined />
                </a-button>
              </a-tooltip> -->
              <!-- Nút Chỉnh sửa -->
              <a-tooltip title="Chỉnh sửa">
                <a-button type="text" class="action-button" @click="handleUpdateFactory(record)">
                  <EditOutlined />
                </a-button>
              </a-tooltip>
              <!-- Nút Đổi trạng thái -->
              <a-tooltip title="Đổi trạng thái">
                <a-button type="text" class="action-button" @click="confirmChangeStatus(record)">
                  <SyncOutlined />
                </a-button>
              </a-tooltip>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- Modal Thêm nhóm xưởng -->
    <a-modal v-model:open="modalAdd" title="Thêm nhóm xưởng" @ok="submitAddFactory">
      <a-form :model="newFactory" layout="vertical">
        <a-form-item label="Tên nhóm xưởng" required>
          <a-input v-model:value="newFactory.name" />
        </a-form-item>
        <a-form-item label="Mô tả">
          <a-input v-model:value="newFactory.description" />
        </a-form-item>
        <a-form-item label="Giảng viên" required>
          <a-select v-model:value="newFactory.idUserStaff" placeholder="Chọn giảng viên">
            <a-select-option v-for="item in staffs" :key="item.id" :value="item.id">
              {{ item.name }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="Dự án" required>
          <a-select v-model:value="newFactory.idProject" placeholder="Chọn dự án">
            <a-select-option v-for="item in projects" :key="item.id" :value="item.id">
              {{ item.name }}
            </a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- Modal Cập nhật nhóm xưởng (sử dụng API chi tiết) -->
    <a-modal v-model:open="modalUpdate" title="Cập nhật nhóm xưởng" @ok="submitUpdateFactory">
      <a-form :model="detailFactory" layout="vertical">
        <a-form-item label="Tên nhóm xưởng" required>
          <a-input v-model:value="detailFactory.factoryName" />
        </a-form-item>
        <a-form-item label="Mô tả">
          <a-input v-model:value="detailFactory.factoryDescription" />
        </a-form-item>
        <a-form-item label="Giảng viên" required>
          <a-select v-model:value="detailFactory.idUserStaff" placeholder="Chọn giảng viên">
            <a-select-option v-for="item in staffs" :key="item.id" :value="item.id">
              {{ item.name }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="Dự án" required>
          <a-select v-model:value="detailFactory.idProject" placeholder="Chọn dự án">
            <a-select-option v-for="item in projects" :key="item.id" :value="item.id">
              {{ item.name }}
            </a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { message, Modal } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import requestAPI from '@/services/requestApiService'
import { API_ROUTES_STAFF } from '@/constants/staffConstant'
import { PlusOutlined, EyeOutlined, EditOutlined, SyncOutlined } from '@ant-design/icons-vue'

const router = useRouter()

/* ----------------- Data & Reactive Variables ----------------- */
// Danh sách nhóm xưởng
const factories = ref([])
// Danh sách dự án, giảng viên (để chọn trong form)
const projects = ref([])
const staffs = ref([])

// Filter & phân trang (cấu trúc giống như bên student)
const filter = reactive({
  searchQuery: '',
  status: '',
  page: 1,
  pageSize: 5,
})
const pagination = reactive({
  current: 1,
  pageSize: 5,
  total: 0,
  showSizeChanger: false,
})

// Modal hiển thị
const modalAdd = ref(false)
const modalUpdate = ref(false)

// Dữ liệu thêm mới nhóm xưởng
const newFactory = reactive({
  name: '',
  description: '',
  idUserStaff: '',
  idProject: '',
})

// Dữ liệu chi tiết nhóm xưởng dùng cho cập nhật (được load qua API chi tiết)
const detailFactory = reactive({
  id: '',
  factoryName: '',
  factoryDescription: '',
  idUserStaff: '',
  idProject: '',
  factoryStatus: '',
  projectName: '',
  subjectCode: '',
  staffName: '',
})

/* ----------------- Column Configuration ----------------- */
const columns = ref([
  { title: 'STT', dataIndex: 'rowNumber', key: 'rowNumber' },
  { title: 'Tên nhóm xưởng', dataIndex: 'factoryName', key: 'factoryName' },
  { title: 'Tên dự án', dataIndex: 'projectName', key: 'projectName' },
  { title: 'Mã bộ môn', dataIndex: 'subjectCode', key: 'subjectCode' },
  { title: 'Tên giảng viên', dataIndex: 'staffName', key: 'staffName' },
  { title: 'Mô tả', dataIndex: 'factoryDescription', key: 'factoryDescription' },
  { title: 'Trạng thái', dataIndex: 'factoryStatus', key: 'factoryStatus' },
  { title: 'Chức năng', key: 'actions' },
])

/* ----------------- Methods ----------------- */
// Lấy danh sách nhóm xưởng từ backend
const fetchFactories = () => {
  requestAPI
    .get(API_ROUTES_STAFF.FETCH_DATA_FACTORY, { params: filter })
    .then((response) => {
      const result = response.data.data
      factories.value = result.data
      pagination.total = result.totalPages * filter.pageSize
      pagination.current = filter.page
    })
    .catch((error) => {
      message.error(error.response?.data?.message || 'Lỗi khi lấy danh sách nhóm xưởng')
    })
}

// Lấy danh sách dự án
const fetchProjects = () => {
  requestAPI
    .get(API_ROUTES_STAFF.FETCH_DATA_FACTORY + '/project')
    .then((response) => {
      projects.value = response.data.data
    })
    .catch((error) => {
      message.error(error.response?.data?.message || 'Lỗi khi lấy danh sách dự án')
    })
}

// Lấy danh sách giảng viên
const fetchStaffs = () => {
  requestAPI
    .get(API_ROUTES_STAFF.FETCH_DATA_FACTORY + '/staff')
    .then((response) => {
      staffs.value = response.data.data
    })
    .catch((error) => {
      message.error(error.response?.data?.message || 'Lỗi khi lấy danh sách giảng viên')
    })
}

// Sự kiện thay đổi trang bảng
const handleTableChange = (paginationData) => {
  filter.page = paginationData.current
  fetchFactories()
}

/* ----- Update Factory ----- */
// Khi cập nhật, gọi API chi tiết để lấy dữ liệu hiện có
const handleUpdateFactory = (record) => {
  requestAPI
    .get(API_ROUTES_STAFF.FETCH_DATA_FACTORY + '/' + record.factoryId)
    .then((response) => {
      const data = response.data.data
      // Mapping dữ liệu theo data detail của bạn
      detailFactory.id = data.factoryId
      detailFactory.factoryName = data.factoryName
      detailFactory.factoryDescription = data.factoryDescription
      detailFactory.idUserStaff = data.staffId
      detailFactory.idProject = data.projectId
      detailFactory.projectName = data.nameProject
      detailFactory.subjectCode = data.subjectCode // có thể null
      detailFactory.staffName = data.staffName
      // Nếu cần hiển thị staffCode, có thể lưu vào trường khác
      // detailFactory.staffCode = data.staffCode;
      modalUpdate.value = true
    })
    .catch((error) => {
      message.error(error.response?.data?.message || 'Lỗi khi lấy chi tiết nhóm xưởng')
    })
}

const submitUpdateFactory = () => {
  if (!detailFactory.factoryName || !detailFactory.idUserStaff || !detailFactory.idProject) {
    message.error('Vui lòng điền đầy đủ thông tin bắt buộc')
    return
  }
  requestAPI
    .put(API_ROUTES_STAFF.FETCH_DATA_FACTORY, detailFactory)
    .then((response) => {
      message.success(response.data.message || 'Cập nhật nhóm xưởng thành công')
      modalUpdate.value = false
      fetchFactories()
    })
    .catch((error) => {
      message.error(error.response?.data?.message || 'Lỗi khi cập nhật nhóm xưởng')
    })
}

// /* ----- Detail Factory ----- */
// // Nút "Chi tiết" đẩy sang router khác
// const handleDetailFactory = (record) => {
//   router.push({
//     name: 'MANAGEMENT_FACTORY_DETAIL',
//     query: { factoryId: record.factoryId },
//   })
// }

/* ----- Change Status ----- */
const confirmChangeStatus = (record) => {
  Modal.confirm({
    title: 'Xác nhận đổi trạng thái',
    content: `Bạn có chắc muốn đổi trạng thái cho nhóm xưởng "${record.factoryName}"?`,
    onOk() {
      handleChangeStatus(record.factoryId)
    },
  })
}

const handleChangeStatus = (factoryId) => {
  requestAPI
    .put(API_ROUTES_STAFF.FETCH_DATA_FACTORY + '/status/' + factoryId)
    .then((response) => {
      message.success(response.data.message || 'Đổi trạng thái thành công')
      fetchFactories()
    })
    .catch((error) => {
      message.error(error.response?.data?.message || 'Lỗi khi đổi trạng thái')
    })
}

/* ----------------- Lifecycle Hook ----------------- */
onMounted(() => {
  fetchFactories()
  fetchProjects()
  fetchStaffs()
})
</script>

<style scoped>
.cart {
  margin-top: 10px;
}
.filter-container {
  margin-bottom: 10px;
}

/* Các nút hành động giống màu bên student */
.action-button {
  background-color: #fff7e6;
  color: black;
  border: 1px solid #ffa940;
  margin-right: 8px;
}
</style>
