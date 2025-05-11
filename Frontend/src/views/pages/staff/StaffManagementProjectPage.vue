<script setup>
import {
  PlusOutlined,
  UnorderedListOutlined,
  EditFilled,
  FilterFilled,
  SearchOutlined,
  SyncOutlined,
} from '@ant-design/icons-vue'
import { message, Modal } from 'ant-design-vue'
import { ref, reactive, onMounted } from 'vue'
import requestAPI from '@/services/requestApiService'
import { API_ROUTES_STAFF } from '@/constants/staffConstant'
import { ROUTE_NAMES } from '@/router/staffRoute'
import { DEFAULT_PAGINATION } from '@/constants'
import useBreadcrumbStore from '@/stores/useBreadCrumbStore'
import useLoadingStore from '@/stores/useLoadingStore'
import { API_ROUTES_EXCEL, GLOBAL_ROUTE_NAMES } from '@/constants/routesConstant'
import ExcelUploadButton from '@/components/excel/ExcelUploadButton.vue'

const breadcrumbStore = useBreadcrumbStore()
const loadingStore = useLoadingStore()
const isLoading = ref(false)

const breadcrumb = ref([
  {
    name: GLOBAL_ROUTE_NAMES.STAFF_PAGE,
    breadcrumbName: 'Phụ trách xưởng',
  },
  {
    name: ROUTE_NAMES.MANAGEMENT_PROJECT,
    breadcrumbName: 'Quản lý dự án',
  },
])

/* ----------------- Data & Reactive Variables ----------------- */
// Danh sách dữ liệu
const levels = ref([])
const projects = ref([])
const semesters = ref([])
const subjects = ref([])

// Trạng thái modal
const modalAdd = ref(false)
const modalDetail = ref(false)
const modalEdit = ref(false)

// Dữ liệu form
const newProject = reactive({
  name: '',
  description: '',
  levelProjectId: null,
  semesterId: null,
  subjectFacilityId: null,
})

const detailProject = reactive({})

// Bộ lọc và phân trang
const filter = reactive({
  name: null,
  status: null,
  page: 1,
  pageSize: 5,
  semesterId: null,
  subjectId: null,
  levelProjectId: null,
  facilityId: null,
})

const pagination = reactive({
  ...DEFAULT_PAGINATION,
})

// Cấu hình cột bảng
const columns = ref([
  { title: '#', dataIndex: 'indexs', key: 'indexs' },
  { title: 'Tên', dataIndex: 'name', key: 'name' },
  { title: 'Cấp dự án', dataIndex: 'nameLevelProject', key: 'nameLevelProject' },
  { title: 'Học kỳ', dataIndex: 'nameSemester', key: 'nameSemester' },
  { title: 'Môn học', dataIndex: 'nameSubject', key: 'nameSubject' },
  { title: 'Mô tả', dataIndex: 'description', key: 'description' },
  { title: 'Trạng thái', dataIndex: 'status', key: 'status' },
  { title: 'Chức năng', key: 'actions' },
])

/* ----------------- Methods ----------------- */
// Chuyển đổi thời gian
const formatDate = (timestamp) => {
  if (!timestamp) return 'Không xác định'
  return new Date(timestamp).toLocaleString()
}

// Lấy danh sách dự án
const fetchProjects = () => {
  loadingStore.show()
  requestAPI
    .post(`${API_ROUTES_STAFF.FETCH_DATA_PROJECT}/list`, {
      name: filter.name,
      levelProjectId: filter.levelProjectId,
      semesterId: filter.semesterId,
      subjectId: filter.subjectId,
      facilityId: filter.facilityId,
      status: filter.status,
      page: pagination.current,
      size: pagination.pageSize,
    })
    .then((response) => {
      projects.value = response.data.data.data
      pagination.total = response.data.data.totalPages * filter.pageSize
      pagination.current = response.data.data.currentPage + 1
    })
    .catch((error) => {
      message.error(error.response?.data?.message || 'Lỗi khi lấy dữ liệu')
    })
    .finally(() => {
      loadingStore.hide()
    })
}

// Lấy danh sách combobox
const fetchLevelCombobox = () => {
  requestAPI
    .get(`${API_ROUTES_STAFF.FETCH_DATA_PROJECT}/level-combobox`)
    .then((response) => {
      levels.value = response.data
    })
    .catch((error) => {
      message.error(error.response?.data?.message || 'Lỗi khi lấy dữ liệu combobox cấp dự án')
    })
}

const fetchSemesters = () => {
  requestAPI
    .get(`${API_ROUTES_STAFF.FETCH_DATA_PROJECT}/semester-combobox`)
    .then((response) => {
      semesters.value = response.data
    })
    .catch((error) => {
      message.error(error.response?.data?.message || 'Lỗi khi lấy dữ liệu combobox học kỳ')
    })
}
const allSemesters = ref([])
const getAllSemesters = () => {
  requestAPI
    .get(`${API_ROUTES_STAFF.FETCH_DATA_PROJECT}/semester`)
    .then((response) => {
      allSemesters.value = response.data
    })
    .catch((error) => {
      message.error(error.response?.data?.message || 'Lỗi khi lấy dữ liệu combobox học kỳ')
    })
}
const fetchSubjects = () => {
  requestAPI
    .get(`${API_ROUTES_STAFF.FETCH_DATA_PROJECT}/subject-facility-combobox`)
    .then((response) => {
      subjects.value = response.data
    })
    .catch((error) => {
      message.error(error.response?.data?.message || 'Lỗi khi lấy dữ liệu combobox môn học')
    })
}

// Xử lý phân trang
const handleTableChange = (pageInfo) => {
  pagination.current = pageInfo.current
  pagination.pageSize = pageInfo.pageSize
  fetchProjects()
}

// Xử lý clear filter
const handleClearFilter = () => {
  Object.assign(filter, {
    name: null,
    status: null,
    page: 1,
    pageSize: 5,
    semesterId: null,
    subjectId: null,
    levelProjectId: null,
    facilityId: null,
  })
  pagination.current = 1
  fetchProjects()
}

// Thêm dự án mới
const handleAddProject = () => {
  if (!newProject.name) {
    message.error('Tên dự án không được bỏ trống')
    return
  }
  if (!newProject.levelProjectId) {
    message.error('Phải chọn cấp dự án')
    return
  }
  if (!newProject.semesterId) {
    message.error('Phải chọn học kỳ')
    return
  }
  if (!newProject.subjectFacilityId) {
    message.error('Phải chọn môn học')
    return
  }

  requestAPI
    .post(API_ROUTES_STAFF.FETCH_DATA_PROJECT, newProject)
    .then(() => {
      message.success('Thêm dự án thành công')
      fetchProjects()
      resetForm()
      modalAdd.value = false
    })
    .catch((error) => {
      message.error(error.response?.data?.message || 'Lỗi khi thêm dự án')
    })
}

// Mở modal sửa dự án
const handleEditProject = (record) => {
  requestAPI
    .get(`${API_ROUTES_STAFF.FETCH_DATA_PROJECT}/${record.id}`)
    .then((response) => {
      Object.assign(detailProject, response.data.data)
      modalEdit.value = true
    })
    .catch((error) => {
      message.error(error.response?.data?.message || 'Lỗi khi lấy thông tin')
    })
}

// Cập nhật dự án
const handleUpdateProject = () => {
  if (!detailProject.name) {
    message.error('Tên dự án không được bỏ trống')
    return
  }
  if (!detailProject.levelProjectId) {
    message.error('Phải chọn cấp dự án')
    return
  }
  if (!detailProject.semesterId) {
    message.error('Phải chọn học kỳ')
    return
  }
  if (!detailProject.subjectFacilityId) {
    message.error('Phải chọn môn học')
    return
  }

  const req = {
    name: detailProject.name,
    description: detailProject.description,
    levelProjectId: detailProject.levelProjectId,
    semesterId: detailProject.semesterId,
    subjectFacilityId: detailProject.subjectFacilityId,
  }

  requestAPI
    .put(`${API_ROUTES_STAFF.FETCH_DATA_PROJECT}/${detailProject.id}`, req)
    .then(() => {
      message.success('Cập nhật dự án thành công')
      modalEdit.value = false
      fetchProjects()
    })
    .catch((error) => {
      message.error(error.response?.data?.message || 'Lỗi khi cập nhật dự án')
    })
}

// Xóa dự án
const handleDeleteProject = (record) => {
  Modal.confirm({
    title: 'Xác nhận',
    content: 'Bạn có chắc chắn muốn đổi trạng thái dự án này?',
    onOk: () => {
      requestAPI
        .delete(`${API_ROUTES_STAFF.FETCH_DATA_PROJECT}/${record.id}`)
        .then(() => {
          message.success('Đổi trạng thái dự án thành công')
          fetchProjects()
        })
        .catch((error) => {
          message.error(error.response?.data?.message || 'Lỗi khi đổi trạng thái dự án')
        })
    },
  })
}
const handleChangeStatusProjectBySemester = () => {
  Modal.confirm({
    title: 'Xác nhận',
    content: 'Bạn có chắc chắn muốn đổi trạng thái tất cả dự án kỳ trước?',
    onOk: () => {
      requestAPI
        .put(`${API_ROUTES_STAFF.FETCH_DATA_PROJECT}/change-status-semester`)
        .then(() => {
          message.success('Đổi trạng thái tất cả dự án kỳ trước thành công')
          fetchProjects()
        })
        .catch((error) => {
          message.error(error.response?.data?.message || 'Lỗi khi đổi trạng thái dự án')
        })
    },
  })
}

const configImportExcel = {
  fetchUrl: API_ROUTES_EXCEL.FETCH_IMPORT_PROJECT,
  onSuccess: () => {
    fetchProjects()
  },
  onError: () => {
    message.error('Không thể xử lý file excel')
  },
  showDownloadTemplate: true,
  showHistoryLog: true,
}
// Reset form
const resetForm = () => {
  Object.assign(newProject, {
    name: '',
    description: '',
    levelProjectId: null,
    semesterId: null,
    subjectFacilityId: null,
  })
  Object.keys(detailProject).forEach((key) => delete detailProject[key])
  modalAdd.value = false
  modalEdit.value = false
  modalDetail.value = false
}

// Khởi tạo dữ liệu khi component mounted
onMounted(() => {
  breadcrumbStore.setRoutes(breadcrumb.value)
  fetchProjects()
  fetchLevelCombobox()
  fetchSemesters()
  fetchSubjects()
  getAllSemesters()
})
</script>

<template>
  <div class="container-fluid">
    <div class="row g-3">
      <div class="col-12">
        <!-- Bộ lọc tìm kiếm -->
        <a-card :bordered="false" class="cart">
          <template #title> <FilterFilled /> Bộ lọc </template>
          <div class="row g-2">
            <div class="col-xxl-6 col-lg-8 col-md-10 col-sm-12">
              <div class="label-title">Từ khoá:</div>
              <a-input
                v-model:value="filter.name"
                placeholder="Tìm kiếm theo tên"
                allowClear
                class="filter-input w-100"
                @change="fetchProjects"
              >
                <template #prefix>
                  <SearchOutlined />
                </template>
              </a-input>
            </div>
            <div class="col-xxl-6 col-lg-8 col-md-10 col-sm-12">
              <div class="label-title">Cấp dự án:</div>
              <a-select
                v-model:value="filter.levelProjectId"
                placeholder="Cấp dự án"
                allowClear
                class="filter-select w-100"
                :dropdownMatchSelectWidth="false"
                :style="{ minWidth: '200px' }"
                @change="fetchProjects"
              >
                <a-select-option :value="null">Tất cả cấp dự án</a-select-option>
                <a-select-option v-for="level in levels" :key="level.id" :value="level.id">
                  {{ level.name }}
                </a-select-option>
              </a-select>
            </div>
          </div>
          <div class="row g-2 mt-2">
            <div class="col-xxl-4 col-lg-6 col-md-8 col-sm-8">
              <div class="label-title">Học kỳ:</div>
              <a-select
                v-model:value="filter.semesterId"
                placeholder="Học kỳ"
                allowClear
                class="filter-select w-100"
                :dropdownMatchSelectWidth="false"
                :style="{ minWidth: '200px' }"
                @change="fetchProjects"
              >
                <a-select-option :value="null">Tất cả học kỳ</a-select-option>
                <a-select-option
                  v-for="semester in allSemesters"
                  :key="semester.id"
                  :value="semester.id"
                >
                  {{ semester.code }}
                </a-select-option>
              </a-select>
            </div>
            <div class="col-xxl-4 col-lg-6 col-md-8 col-sm-8">
              <div class="label-title">Môn học:</div>
              <a-select
                v-model:value="filter.subjectId"
                placeholder="Môn học"
                allowClear
                class="filter-select w-100"
                :dropdownMatchSelectWidth="false"
                :style="{ minWidth: '200px' }"
                @change="fetchProjects"
              >
                <a-select-option :value="null">Tất cả môn học</a-select-option>
                <a-select-option v-for="subject in subjects" :key="subject.id" :value="subject.id">
                  {{ subject.name }}
                </a-select-option>
              </a-select>
            </div>
            <div class="col-xxl-4 col-lg-6 col-md-8 col-sm-8">
              <div class="label-title">Trạng thái:</div>
              <a-select
                v-model:value="filter.status"
                placeholder="Trạng thái"
                allowClear
                class="filter-select w-100"
                :dropdownMatchSelectWidth="false"
                :style="{ minWidth: '200px' }"
                @change="fetchProjects"
              >
                <a-select-option :value="null">Tất cả trạng thái</a-select-option>
                <a-select-option :value="1">Đang triển khai</a-select-option>
                <a-select-option :value="0">Không hoạt động</a-select-option>
              </a-select>
            </div>
          </div>
          <div class="row">
            <div class="col-12">
              <div class="d-flex justify-content-center flex-wrap gap-2 mt-3">
                <a-button class="btn-light" @click="fetchProjects"> <FilterFilled /> Lọc </a-button>
                <a-button class="btn-gray" @click="handleClearFilter"> Huỷ lọc </a-button>
              </div>
            </div>
          </div>
        </a-card>
      </div>

      <div class="col-12">
        <a-card :bordered="false" class="cart">
          <template #title> <UnorderedListOutlined /> Danh sách dự án</template>
          <div class="d-flex justify-content-end mb-3 flex-wrap gap-3">
            <ExcelUploadButton v-bind="configImportExcel" />
            <a-tooltip title="Đổi trạng thái tất cả dự án kỳ trước">
              <a-button
                type="default"
                @click="handleChangeStatusProjectBySemester"
                class="btn-outline-warning me-2"
              >
                <SyncOutlined /> Đổi trạng thái
              </a-button>
            </a-tooltip>

            <a-button type="primary" @click="modalAdd = true">
              <PlusOutlined /> Thêm dự án
            </a-button>
          </div>

          <a-table
            :dataSource="projects"
            :columns="columns"
            rowKey="id"
            :pagination="pagination"
            :scroll="{ y: 500, x: 'auto' }"
            :loading="isLoading"
            @change="handleTableChange"
          >
            <template #bodyCell="{ column, record }">
              <!-- Hiển thị trạng thái -->
              <template v-if="column.dataIndex === 'status'">
                <span class="nowrap">
                  <a-switch
                    class="me-2"
                    :checked="record.status == 'ACTIVE' || record.status == 1"
                    @change="handleDeleteProject(record)"
                  />
                  <a-tag :color="record.status == 1 ? 'green' : 'red'">
                    {{ record.status == 1 ? 'Đang triển khai' : 'Không hoạt động' }}
                  </a-tag>
                </span>
              </template>
              <!-- Các nút chức năng -->
              <template v-else-if="column.key === 'actions'">
                <a-space>
                  <a-tooltip title="Sửa">
                    <a-button
                      @click="handleEditProject(record)"
                      type="text"
                      class="btn-outline-info me-2"
                    >
                      <EditFilled />
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

    <!-- Modal thêm dự án -->
    <a-modal v-model:open="modalAdd" title="Thêm dự án" @ok="handleAddProject" @cancel="resetForm">
      <a-form layout="vertical">
        <a-form-item label="Tên dự án" required>
          <a-input v-model:value="newProject.name" placeholder="Nhập tên dự án" />
        </a-form-item>
        <a-form-item label="Mô tả" required>
          <a-textarea v-model:value="newProject.description" placeholder="Nhập mô tả" />
        </a-form-item>
        <a-form-item label="Cấp dự án" required>
          <a-select
            v-model:value="newProject.levelProjectId"
            placeholder="Chọn cấp dự án"
            allowClear
          >
            <a-select-option v-for="level in levels" :key="level.id" :value="level.id">
              {{ level.name }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="Học kỳ" required>
          <a-select v-model:value="newProject.semesterId" placeholder="Chọn học kỳ" allowClear>
            <a-select-option v-for="semester in semesters" :key="semester.id" :value="semester.id">
              {{ semester.code }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="Môn học" required>
          <a-select
            v-model:value="newProject.subjectFacilityId"
            placeholder="Chọn môn học"
            allowClear
          >
            <a-select-option v-for="subject in subjects" :key="subject.id" :value="subject.id">
              {{ subject.name }}
            </a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- Modal xem chi tiết dự án -->
    <a-modal v-model:open="modalDetail" title="Chi tiết dự án" footer="">
      <p><strong>Tên:</strong> {{ detailProject.name }}</p>
      <p><strong>Cấp dự án:</strong> {{ detailProject.nameLevelProject }}</p>
      <p><strong>Học kỳ:</strong> {{ detailProject.nameSemester }}</p>
      <p><strong>Môn học:</strong> {{ detailProject.nameSubject }}</p>
      <p><strong>Mô tả:</strong> {{ detailProject.description }}</p>
      <p v-if="detailProject.createdAt">
        <strong>Ngày tạo:</strong> {{ formatDate(detailProject.createdAt) }}
      </p>
      <p v-if="detailProject.updatedAt">
        <strong>Ngày sửa:</strong> {{ formatDate(detailProject.updatedAt) }}
      </p>
      <p><strong>Số nhóm xưởng: COMING SOON</strong></p>
      <p><strong>Giảng viên: COMING SOON</strong></p>
      <p>
        <strong>Trạng thái:</strong>
        <a-tag :color="detailProject.status === 'ACTIVE' ? 'green' : 'red'">
          {{ detailProject.status === 'ACTIVE' ? 'Hoạt động' : 'Không hoạt động' }}
        </a-tag>
      </p>
    </a-modal>

    <!-- Modal sửa dự án -->
    <a-modal v-model:open="modalEdit" title="Sửa dự án" @ok="handleUpdateProject">
      <a-form layout="vertical">
        <a-form-item label="Tên dự án" required>
          <a-input v-model:value="detailProject.name" placeholder="Nhập tên dự án" />
        </a-form-item>
        <a-form-item label="Mô tả" required>
          <a-textarea v-model:value="detailProject.description" placeholder="Nhập mô tả" />
        </a-form-item>
        <a-form-item label="Cấp dự án" required>
          <a-select
            v-model:value="detailProject.levelProjectId"
            placeholder="Chọn cấp dự án"
            allowClear
          >
            <a-select-option v-for="level in levels" :key="level.id" :value="level.id">
              {{ level.name }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="Học kỳ" required>
          <a-select v-model:value="detailProject.semesterId" placeholder="Chọn học kỳ" allowClear>
            <a-select-option v-for="semester in semesters" :key="semester.id" :value="semester.id">
              {{ semester.code }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="Môn học" required>
          <a-select
            v-model:value="detailProject.subjectFacilityId"
            placeholder="Chọn môn học"
            allowClear
          >
            <a-select-option v-for="subject in subjects" :key="subject.id" :value="subject.id">
              {{ subject.name }}
            </a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>
