<script setup>
import { ref, reactive, onMounted } from 'vue'
import { message, Modal } from 'ant-design-vue'
import router from '@/router'
import requestAPI from '@/services/requestApiService'
import { API_ROUTES_STAFF } from '@/constants/staffConstant'
import { GLOBAL_ROUTE_NAMES } from '@/constants/routesConstant'
import {
  PlusOutlined,
  EyeOutlined,
  EditOutlined,
  SyncOutlined,
  EyeFilled,
  EditFilled,
  UnorderedListOutlined,
  FilterFilled,
} from '@ant-design/icons-vue'
import { ROUTE_NAMES } from '@/router/staffRoute'
import { DEFAULT_PAGINATION } from '@/constants/paginationConstant'
import useBreadcrumbStore from '@/stores/useBreadCrumbStore'

const breadcrumbStore = useBreadcrumbStore()
const isLoading = ref(false)
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
  ...DEFAULT_PAGINATION,
})

// Modal hiển thị
const modalAdd = ref(false)
const modalUpdate = ref(false)

// Dữ liệu thêm mới nhóm xưởng
const newFactory = reactive({
  factoryName: '',
  factoryDescription: '',
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
  { title: '#', dataIndex: 'rowNumber', key: 'rowNumber', width: 50 },
  { title: 'Tên nhóm xưởng', dataIndex: 'name', key: 'name', width: 200 },
  { title: 'Tên dự án', dataIndex: 'projectName', key: 'projectName', width: 200 },
  { title: 'Mã bộ môn', dataIndex: 'subjectCode', key: 'subjectCode', width: 100 },
  { title: 'Tên giảng viên', dataIndex: 'staffName', key: 'staffName', width: 100 },
  { title: 'Mô tả', dataIndex: 'factoryDescription', key: 'factoryDescription', width: 200 },
  { title: 'Trạng thái', dataIndex: 'factoryStatus', key: 'factoryStatus', width: 80 },
  { title: 'Chức năng', key: 'actions' },
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
const handleTableChange = (page) => {
  pagination.current = page.current
  pagination.pageSize = page.pageSize
  fetchFactories()
}

const submitAddFactory = () => {
  if (!newFactory.factoryName || !newFactory.idUserStaff || !newFactory.idProject) {
    message.error('Vui lòng điền đầy đủ thông tin bắt buộc')
    return
  }
  requestAPI
    .post(API_ROUTES_STAFF.FETCH_DATA_FACTORY, newFactory)
    .then((response) => {
      message.success(response.data.message || 'Thêm nhóm xưởng thành công')
      modalAdd.value = false
      fetchFactories()
    })
    .catch((error) => {
      message.error(error.response?.data?.message || 'Lỗi khi tạo nhóm xưởng')
    })
}

/* ----- Update Factory ----- */
// Khi cập nhật, gọi API chi tiết để lấy dữ liệu hiện có
const handleUpdateFactory = (record) => {
  requestAPI
    .get(API_ROUTES_STAFF.FETCH_DATA_FACTORY + '/detail/' + record.id)
    .then((response) => {
      const data = response.data.data
      // Mapping dữ liệu theo data detail của bạn
      detailFactory.id = data.id
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

/* ----- Detail Factory ----- */
// Nút "Chi tiết" đẩy sang router khác (chưa có phần hiển thị phân sinh viên vào nhóm xưởng)
const handleDetailFactory = (record) => {
  // Kiểm tra cấu trúc của record nếu cần debug
  console.log('Record detail:', record)
  router.push({
    name: ROUTE_NAMES.MANAGEMENT_STUDENT_FACTORY,
    query: {
      factoryId: record.id,
      factoryName: record.name,
    },
  })
}

/* ----- Change Status ----- */
const confirmChangeStatus = (record) => {
  Modal.confirm({
    title: 'Xác nhận đổi trạng thái',
    content: `Bạn có chắc muốn đổi trạng thái cho nhóm xưởng "${record.factoryName}"?`,
    onOk() {
      handleChangeStatus(record.id)
    },
  })
}

const handleChangeStatus = (id) => {
  requestAPI
    .put(API_ROUTES_STAFF.FETCH_DATA_FACTORY + '/status/' + id)
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
  breadcrumbStore.setRoutes(breadcrumb.value)
  fetchFactories()
  fetchProjects()
  fetchStaffs()
})
</script>

<template>
  <!-- Modal Thêm nhóm xưởng -->
  <a-modal v-model:open="modalAdd" title="Thêm nhóm xưởng" @ok="submitAddFactory">
    <a-form :model="newFactory" layout="vertical">
      <a-form-item label="Tên nhóm xưởng" required>
        <a-input v-model:value="newFactory.factoryName" placeholder="-- Tên nhóm xưởng --" />
      </a-form-item>
      <a-form-item label="Mô tả">
        <a-input
          v-model:value="newFactory.factoryDescription"
          placeholder="-- Mô tả nhóm xưởng --"
        />
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

  <!-- Modal Cập nhật nhóm xưởng -->
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

  <div class="container-fluid">
    <div class="row g-3">
      <!-- Bộ lọc tìm kiếm -->
      <div class="col-12">
        <a-card :bordered="false" class="cart">
          <template #title> <FilterFilled /> Bộ lọc </template>
          <div class="row g-2">
            <div class="col-md-6 col-sm-12">
              <a-input
                v-model:value="filter.searchQuery"
                placeholder="Tên xưởng, dự án, bộ môn, giảng viên"
                allowClear
                @change="fetchFactories"
                class="w-100"
              />
            </div>
            <div class="col-md-6 col-sm-12">
              <a-select
                v-model:value="filter.status"
                placeholder="Chọn trạng thái"
                allowClear
                class="w-100"
                @change="fetchFactories"
              >
                <a-select-option :value="''">Tất cả trạng thái</a-select-option>
                <a-select-option value="ACTIVE">Hoạt động</a-select-option>
                <a-select-option value="INACTIVE">Không hoạt động</a-select-option>
              </a-select>
            </div>
          </div>
        </a-card>
      </div>

      <!-- Danh sách nhóm xưởng -->
      <div class="col-12">
        <a-card :bordered="false" class="cart">
          <template #title> <UnorderedListOutlined /> Danh sách nhóm xưởng </template>
          <div class="d-flex justify-content-end mb-3">
            <a-tooltip title="Thêm nhóm xưởng">
              <a-button type="primary" @click="modalAdd = true"> <PlusOutlined /> Thêm </a-button>
            </a-tooltip>
          </div>
          <a-table
            rowKey="id"
            :dataSource="factories"
            :columns="columns"
            :pagination="pagination"
            @change="handleTableChange"
            :loading="isLoading"
            :scroll="{ y: 500, x: 'auto' }"
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
                  <a-tooltip title="Chi tiết nhóm xưởng">
                    <a-button
                      type="text"
                      class="btn-outline-primary"
                      @click="handleDetailFactory(record)"
                    >
                      <EyeFilled />
                    </a-button>
                  </a-tooltip>
                  <a-tooltip title="Chỉnh sửa">
                    <a-button
                      type="text"
                      class="btn-outline-info"
                      @click="handleUpdateFactory(record)"
                    >
                      <EditFilled />
                    </a-button>
                  </a-tooltip>
                  <a-tooltip title="Đổi trạng thái">
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
  </div>
</template>