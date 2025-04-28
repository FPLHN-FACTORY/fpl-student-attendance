<script setup>
import { ref, reactive, onMounted } from 'vue'
import { message, Modal } from 'ant-design-vue'
import router from '@/router'
import requestAPI from '@/services/requestApiService'
import { API_ROUTES_STAFF } from '@/constants/staffConstant'
import { API_ROUTES_EXCEL, GLOBAL_ROUTE_NAMES } from '@/constants/routesConstant'
import {
  PlusOutlined,
  EyeOutlined,
  EditOutlined,
  SyncOutlined,
  EyeFilled,
  EditFilled,
  UnorderedListOutlined,
  FilterFilled,
  AlignLeftOutlined,
} from '@ant-design/icons-vue'
import { ROUTE_NAMES } from '@/router/staffRoute'
import { DEFAULT_PAGINATION } from '@/constants'
import useBreadcrumbStore from '@/stores/useBreadCrumbStore'
import useLoadingStore from '@/stores/useLoadingStore'
import ExcelUploadButton from '@/components/excel/ExcelUploadButton.vue'

const breadcrumbStore = useBreadcrumbStore()
const loadingStore = useLoadingStore()

/* ----------------- Data & Reactive Variables ----------------- */
const factories = ref([])
const projects = ref([])
const staffs = ref([])
const semesters = ref([])
const filter = reactive({
  factoryName: '',
  status: '',
  idProject: null,
  idStaff: null,
  idSemester: null,
})

const pagination = reactive({
  ...DEFAULT_PAGINATION,
})

const modalAdd = ref(false)
const modalUpdate = ref(false)

const newFactory = reactive({
  factoryName: '',
  factoryDescription: '',
  idUserStaff: null,
  idProject: null,
})

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

const columns = ref([
  { title: '#', dataIndex: 'rowNumber', key: 'rowNumber', width: 50 },
  { title: 'Tên nhóm xưởng', dataIndex: 'name', key: 'name', width: 200 },
  { title: 'Tên dự án', dataIndex: 'projectName', key: 'projectName', width: 200 },
  { title: 'Mã bộ môn', dataIndex: 'subjectCode', key: 'subjectCode', width: 100 },
  { title: 'Tên giảng viên', dataIndex: 'staffName', key: 'staffName', width: 100 },
  { title: 'Mô tả', dataIndex: 'factoryDescription', key: 'factoryDescription', width: 200 },
  { title: 'Trạng thái', dataIndex: 'factoryStatus', key: 'factoryStatus', width: 80 },
  { title: 'Chức năng', key: 'actions', width: 100 },
])

const breadcrumb = ref([
  {
    name: GLOBAL_ROUTE_NAMES.STAFF_PAGE,
    breadcrumbName: 'Phụ trách xưởng',
  },
  {
    name: ROUTE_NAMES.MANAGEMENT_FACTORY,
    breadcrumbName: 'Quản lý nhóm xưởng',
  },
])

const fetchFactories = () => {
  loadingStore.show()
  requestAPI
    .get(API_ROUTES_STAFF.FETCH_DATA_FACTORY, {
      params: {
        ...filter,
        page: pagination.current,
        size: pagination.pageSize,
      },
    })
    .then((response) => {
      const result = response.data.data
      factories.value = result.data
      if (result.totalRecords !== undefined) {
        pagination.total = result.totalRecords
      } else {
        pagination.total = result.totalPages * pagination.pageSize
      }
    })
    .catch((error) => {
      message.error(error.response?.data?.message || 'Lỗi khi lấy danh sách nhóm xưởng')
    })
    .finally(() => {
      loadingStore.hide()
    })
}

const clearData = () => {
  newFactory.factoryName = ''
  newFactory.factoryDescription = ''
  newFactory.idProject = null
  newFactory.idUserStaff = null
}

const fetchProjects = () => {
  loadingStore.show()
  requestAPI
    .get(API_ROUTES_STAFF.FETCH_DATA_FACTORY + '/project')
    .then((response) => {
      projects.value = response.data.data
    })
    .catch((error) => {
      message.error(error.response?.data?.message || 'Lỗi khi lấy danh sách dự án')
    })
    .finally(() => {
      loadingStore.hide()
    })
}

const fetchStaffs = () => {
  loadingStore.show()
  requestAPI
    .get(API_ROUTES_STAFF.FETCH_DATA_FACTORY + '/staff')
    .then((response) => {
      staffs.value = response.data.data
    })
    .catch((error) => {
      message.error(error.response?.data?.message || 'Lỗi khi lấy danh sách giảng viên')
    })
    .finally(() => {
      loadingStore.hide()
    })
}

const fetchSemesters = () => {
  loadingStore.show()
  requestAPI
    .get(API_ROUTES_STAFF.FETCH_DATA_FACTORY + '/semesters')
    .then((response) => {
      semesters.value = response.data.data
    })
    .catch((error) => {
      message.error(error.response?.data?.message || 'Lỗi khi lấy danh sách kỳ học')
    })
    .finally(() => {
      loadingStore.hide()
    })
}

const onFilterChange = () => {
  pagination.current = 1
  fetchFactories()
}

const handleTableChange = (pageInfo) => {
  pagination.current = pageInfo.current
  pagination.pageSize = pageInfo.pageSize
  fetchFactories()
}

const submitAddFactory = () => {
  if (!newFactory.factoryName || !newFactory.idUserStaff || !newFactory.idProject) {
    message.error('Vui lòng điền đầy đủ thông tin bắt buộc')
    return
  }
  loadingStore.show()
  requestAPI
    .post(API_ROUTES_STAFF.FETCH_DATA_FACTORY, newFactory)
    .then((response) => {
      message.success(response.data.message || 'Thêm nhóm xưởng thành công')
      modalAdd.value = false
      fetchFactories()
      clearData()
    })
    .catch((error) => {
      message.error(error.response?.data?.message || 'Lỗi khi tạo nhóm xưởng')
    })
    .finally(() => {
      loadingStore.hide()
    })
}

const handleUpdateFactory = (record) => {
  loadingStore.show()
  requestAPI
    .get(API_ROUTES_STAFF.FETCH_DATA_FACTORY + '/detail/' + record.id)
    .then((response) => {
      const data = response.data.data
      detailFactory.id = data.id
      detailFactory.factoryName = data.factoryName
      detailFactory.factoryDescription = data.factoryDescription
      detailFactory.idUserStaff = data.staffId
      detailFactory.idProject = data.projectId
      detailFactory.projectName = data.nameProject
      detailFactory.subjectCode = data.subjectCode
      detailFactory.staffName = data.staffName
      modalUpdate.value = true
    })
    .catch((error) => {
      message.error(error.response?.data?.message || 'Lỗi khi lấy chi tiết nhóm xưởng')
    })
    .finally(() => {
      loadingStore.hide()
    })
}

const submitUpdateFactory = () => {
  if (!detailFactory.factoryName || !detailFactory.idUserStaff || !detailFactory.idProject) {
    message.error('Vui lòng điền đầy đủ thông tin bắt buộc')
    return
  }
  loadingStore.show()
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
    .finally(() => {
      loadingStore.hide()
    })
}

const handleDetailFactory = (record) => {
  loadingStore.show()
  requestAPI
    .get(API_ROUTES_STAFF.FETCH_DATA_FACTORY + '/exist-plan/' + record.id)
    .then((response) => {
      const existsPlan = response.data.data
      if (!existsPlan) {
        Modal.info({
          title: 'Thông báo',
          content: 'Nhóm xưởng chưa có kế hoạch',
          onOk() {},
        })
      } else {
        router.push({
          name: ROUTE_NAMES.MANAGEMENT_STUDENT_FACTORY,
          query: {
            factoryId: record.id,
            factoryName: record.name,
          },
        })
      }
    })
    .catch((error) => {
      Modal.error({
        title: 'Lỗi',
        content: error.response?.data?.message || 'Lỗi khi kiểm tra kế hoạch của nhóm xưởng',
      })
    })
    .finally(() => {
      loadingStore.hide()
    })
}

const confirmChangeStatus = (record) => {
  Modal.confirm({
    title: 'Xác nhận đổi trạng thái',
    content: `Bạn có chắc muốn đổi trạng thái cho nhóm xưởng "${record.name}"?`,
    onOk() {
      handleChangeStatus(record.id)
    },
  })
}

const handleChangeStatus = (id) => {
  loadingStore.show()
  requestAPI
    .put(API_ROUTES_STAFF.FETCH_DATA_FACTORY + '/status/' + id)
    .then((response) => {
      message.success(response.data.message || 'Đổi trạng thái thành công')
      fetchFactories()
    })
    .catch((error) => {
      message.error(error.response?.data?.message || 'Lỗi khi đổi trạng thái')
    })
    .finally(() => {
      loadingStore.hide()
    })
}

const changeAllStatusBySemester = () => {
  Modal.confirm({
    title: 'Xác nhận đổi trạng thái',
    content: 'Bạn có chắc muốn đổi trạng thái cho tất cả các nhóm xưởng của kỳ trước?',
    onOk() {
      loadingStore.show()
      requestAPI
        .put(API_ROUTES_STAFF.FETCH_DATA_FACTORY + '/change-all-status')
        .then((response) => {
          message.success(response.data.message || 'Đổi trạng thái nhóm xưởng kỳ trước thành công')
          fetchFactories()
        })
        .catch((error) => {
          message.error(error.response?.data?.message || 'Lỗi khi đổi trạng thái nhóm xưởng')
        })
        .finally(() => {
          loadingStore.hide()
        })
    },
  })
}

const configImportExcel = {
  fetchUrl: API_ROUTES_EXCEL.FETCH_IMPORT_FACTORY,
  onSuccess: () => {
    fetchFactories()
  },
  onError: () => {
    message.error('Không thể xử lý file excel')
  },
  showDownloadTemplate: true,
  showHistoryLog: true,
}

onMounted(() => {
  breadcrumbStore.setRoutes(breadcrumb.value)
  fetchFactories()
  fetchProjects()
  fetchStaffs()
  fetchSemesters()
})
</script>

<template>
  <!-- Modal Thêm nhóm xưởng -->
  <a-modal v-model:open="modalAdd" title="Thêm nhóm xưởng" @ok="submitAddFactory">
    <a-form :model="newFactory" layout="vertical">
      <a-form-item label="Tên nhóm xưởng" required>
        <a-input v-model:value="newFactory.factoryName" placeholder="-- Tên nhóm xưởng --" />
      </a-form-item>
      <a-form-item label="Mô tả" required>
        <a-input
          v-model:value="newFactory.factoryDescription"
          placeholder="-- Mô tả nhóm xưởng --"
        />
      </a-form-item>
      <a-form-item label="Giảng viên" required>
        <a-select
          v-if="staffs.length > 0"
          v-model:value="newFactory.idUserStaff"
          placeholder="Chọn giảng viên"
          show-search
          :filter-option="
            (input, option) => {
              const text = option.label || ''
              return text.toLowerCase().includes(input.toLowerCase())
            }
          "
        >
          <a-select-option
            v-for="item in staffs"
            :key="item.id"
            :value="item.id"
            :label="item.name"
          >
            {{ item.code + ' - ' + item.name }}
          </a-select-option>
        </a-select>
        <div v-else>Cơ sở chưa có giảng viên nào!</div>
      </a-form-item>
      <a-form-item label="Dự án" required>
        <a-select
          v-if="projects.length > 0"
          v-model:value="newFactory.idProject"
          placeholder="Chọn dự án"
          show-search
          :filter-option="
            (input, option) => {
              const text = option.label || ''
              return text.toLowerCase().includes(input.toLowerCase())
            }
          "
        >
          <a-select-option
            v-for="item in projects"
            :key="item.id"
            :value="item.id"
            :label="item.name"
          >
            {{ item.name + ' - ' + item.levelProject.name }}
          </a-select-option>
        </a-select>
        <div v-else>Cơ sở chưa có dự án nào!</div>
      </a-form-item>
    </a-form>
  </a-modal>

  <!-- Modal Cập nhật nhóm xưởng -->
  <a-modal v-model:open="modalUpdate" title="Cập nhật nhóm xưởng" @ok="submitUpdateFactory">
    <a-form :model="detailFactory" layout="vertical">
      <a-form-item label="Tên nhóm xưởng" required>
        <a-input v-model:value="detailFactory.factoryName" />
      </a-form-item>
      <a-form-item label="Mô tả" required>
        <a-input v-model:value="detailFactory.factoryDescription" />
      </a-form-item>
      <a-form-item label="Giảng viên" required>
        <a-select
          v-if="staffs.length > 0"
          v-model:value="detailFactory.idUserStaff"
          placeholder="Chọn giảng viên"
          show-search
          :filter-option="
            (input, option) => {
              const text = option.label || ''
              return text.toLowerCase().includes(input.toLowerCase())
            }
          "
        >
          <a-select-option
            v-for="item in staffs"
            :key="item.id"
            :value="item.id"
            :label="item.name"
          >
            {{ item.code + ' - ' + item.name }}
          </a-select-option>
        </a-select>
        <div v-else>Cơ sở chưa có giảng viên nào!</div>
      </a-form-item>
      <a-form-item label="Dự án" required>
        <a-select
          v-if="projects.length > 0"
          v-model:value="detailFactory.idProject"
          placeholder="Chọn dự án"
          show-search
          :filter-option="
            (input, option) => {
              const text = option.label || ''
              return text.toLowerCase().includes(input.toLowerCase())
            }
          "
        >
          <a-select-option
            v-for="item in projects"
            :key="item.id"
            :value="item.id"
            :label="item.name"
          >
            {{ item.name + ' - ' + item.levelProject.name }}
          </a-select-option>
        </a-select>
        <div v-else>Cơ sở chưa có dự án nào!</div>
      </a-form-item>
    </a-form>
  </a-modal>

  <div class="container-fluid">
    <div class="row g-3">
      <!-- Bộ lọc tìm kiếm -->
      <div class="col-12">
        <a-card :bordered="false" class="cart">
          <template #title> <FilterFilled /> Bộ lọc </template>
          <!-- First Row: Search Query and Status -->
          <div class="row g-2">
            <div class="col-md-6 col-sm-12">
              <div class="label-title">Tìm kiếm tên xưởng</div>
              <a-input
                v-model:value="filter.factoryName"
                placeholder="Tên xưởng"
                allowClear
                @change="onFilterChange"
                class="w-100"
              />
            </div>
            <div class="col-md-3 col-sm-9">
              <div class="label-title">Giảng viên</div>
              <a-select
                v-model:value="filter.idStaff"
                placeholder="Chọn giảng viên"
                allowClear
                show-search
                @change="onFilterChange"
                class="w-100"
                :filter-option="
                  (input, option) =>
                    (option.label || '').toLowerCase().includes(input.toLowerCase())
                "
              >
                <a-select-option
                  v-for="staff in staffs"
                  :key="staff.id"
                  :value="staff.id"
                  :label="staff.code + ' - ' + staff.name"
                >
                  {{ staff.code + ' - ' + staff.name }}
                </a-select-option>
              </a-select>
            </div>
            <div class="col-md-3 col-sm-9">
              <div class="label-title">Kỳ học</div>
              <a-select
                v-model:value="filter.idSemester"
                placeholder="Chọn kỳ học"
                allowClear
                show-search
                @change="onFilterChange"
                class="w-100"
                :filter-option="
                  (input, option) =>
                    (option.label || '').toLowerCase().includes(input.toLowerCase())
                "
              >
                <a-select-option
                  v-for="semester in semesters"
                  :key="semester.id"
                  :value="semester.id"
                  :label="semester.code"
                >
                  {{ semester.code }}
                </a-select-option>
              </a-select>
            </div>
          </div>
          <!-- Second Row: Project and Staff -->
          <div class="row g-2 mt-2">
            <div class="col-md-6 col-sm-12">
              <div class="label-title">Dự án</div>
              <a-select
                v-model:value="filter.idProject"
                placeholder="Chọn dự án"
                allowClear
                show-search
                @change="onFilterChange"
                class="w-100"
                :filter-option="
                  (input, option) =>
                    (option.label || '').toLowerCase().includes(input.toLowerCase())
                "
              >
                <a-select-option
                  v-for="project in projects"
                  :key="project.id"
                  :value="project.id"
                  :label="project.name + ' - ' + project.levelProject.name"
                >
                  {{ project.name + ' - ' + project.levelProject.name }}
                </a-select-option>
              </a-select>
            </div>
            <div class="col-md-6 col-sm-12">
              <div class="label-title">Trạng thái:</div>
              <a-select
                v-model:value="filter.status"
                placeholder="Chọn trạng thái"
                allowClear
                class="w-100"
                @change="onFilterChange"
              >
                <a-select-option :value="''">Tất cả trạng thái</a-select-option>
                <a-select-option value="ACTIVE">Đang hoạt động</a-select-option>
                <a-select-option value="INACTIVE">Ngừng hoạt động</a-select-option>
              </a-select>
            </div>
          </div>
        </a-card>
      </div>

      <!-- Danh sách nhóm xưởng -->
      <div class="col-12">
        <a-card :bordered="false" class="cart">
          <template #title> <UnorderedListOutlined /> Danh sách nhóm xưởng </template>
          <div class="d-flex justify-content-end mb-3 flex-wrap gap-3">
            <ExcelUploadButton v-bind="configImportExcel" />
            <a-tooltip title="Đổi trạng thái tất cả nhóm xưởng kỳ trước">
              <a-button
                type="default"
                @click="changeAllStatusBySemester"
                class="btn-outline-warning me-2"
              >
                <SyncOutlined /> Đổi trạng thái
              </a-button>
            </a-tooltip>
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
                <template v-else-if="column.dataIndex === 'name'">
                  <a @click="handleDetailFactory(record)">{{ record.name }}</a>
                </template>
                <template v-else-if="column.dataIndex === 'factoryStatus'">
                  <span class="nowrap">
                    <a-switch
                      class="me-2"
                      :checked="record.factoryStatus === 'ACTIVE' || record.factoryStatus === 1"
                      @change="confirmChangeStatus(record)"
                    />
                    <a-tag
                      :color="
                        record.factoryStatus === 'ACTIVE' || record.factoryStatus === 1
                          ? 'green'
                          : 'red'
                      "
                    >
                      {{
                        record.factoryStatus === 'ACTIVE' || record.factoryStatus === 1
                          ? 'Đang hoạt động'
                          : 'Ngừng hoạt động'
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
                  <a-tooltip title="Chi tiết nhóm xưởng">
                    <a-button
                      type="text"
                      class="btn-outline-primary"
                      @click="handleDetailFactory(record)"
                    >
                      <AlignLeftOutlined />
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
                </a-space>
              </template>
            </template>
          </a-table>
        </a-card>
      </div>
    </div>
  </div>
</template>