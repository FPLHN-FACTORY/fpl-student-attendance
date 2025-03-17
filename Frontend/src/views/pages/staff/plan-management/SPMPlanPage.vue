<script setup>
import { ref, onMounted, watch, reactive } from 'vue'
import {
  PlusOutlined,
  FilterFilled,
  UnorderedListOutlined,
  SearchOutlined,
  EyeFilled,
  AlignLeftOutlined,
} from '@ant-design/icons-vue'
import { message, Modal } from 'ant-design-vue'
import requestAPI from '@/services/requestApiService'
import { DEFAULT_DATE_FORMAT, DEFAULT_PAGINATION } from '@/constants'
import { API_ROUTES_STAFF } from '@/constants/staffConstant'
import { useRouter } from 'vue-router'
import { ROUTE_NAMES } from '@/router/staffRoute'
import { GLOBAL_ROUTE_NAMES } from '@/constants/routesConstant'
import useBreadcrumbStore from '@/stores/useBreadCrumbStore'
import { formatDate } from '@/utils/utils'
import dayjs from 'dayjs'

const router = useRouter()
const breadcrumbStore = useBreadcrumbStore()

const isLoading = ref(false)
const modalAdd = reactive({
  isShow: false,
  isLoading: false,
  okText: 'Xác nhận',
  cancelText: 'Huỷ bỏ',
  width: 800,
  onOk: () => handleSubmitAdd(),
})

const optSubject = ref([])
const optLevel = ref([])
const optSemester = ref([])
const optYear = ref([])
const lstData = ref([])
const lstDataAdd = ref([])

const formRefAdd = ref(null)

const maxRangeDate = ref(dayjs())

const columns = ref([
  { title: '#', dataIndex: 'orderNumber', key: 'orderNumber', width: 50 },
  { title: 'Tên kế hoạch', dataIndex: 'planName', key: 'planName', width: 120 },
  { title: 'Tên dự án', dataIndex: 'projectName', key: 'projectName' },
  { title: 'Bộ môn', dataIndex: 'subjectName', key: 'subjectName' },
  { title: 'Cấp độ', dataIndex: 'level', key: 'level', width: 120 },
  { title: 'Ngày diễn ra', dataIndex: 'semesterName', key: 'semesterName' },
  { title: 'Trạng thái', dataIndex: 'status', key: 'status' },
  { title: '', key: 'actions' },
])

const breadcrumb = ref([
  {
    name: GLOBAL_ROUTE_NAMES.STAFF_PAGE,
    breadcrumbName: 'Phụ trách xưởng',
  },
  {
    name: ROUTE_NAMES.MANAGEMENT_PLAN,
    breadcrumbName: 'Danh sách kế hoạch',
  },
])

const pagination = ref({ ...DEFAULT_PAGINATION })

const dataFilter = reactive({
  keyword: null,
  level: null,
  semester: null,
  year: null,
  subject: null,
})

const dataFilterAdd = reactive({
  level: null,
  semester: null,
  year: null,
  subject: null,
})

const formDataAdd = reactive({
  idProject: null,
  name: null,
  description: null,
  rangeDate: [],
})

const formRules = reactive({
  idProject: [{ required: true, message: 'Vui lòng chọn 1 dự án!' }],
  name: [{ required: true, message: 'Vui lòng nhập mục này!' }],
  description: [{ required: true, message: 'Vui lòng nhập mục này!' }],
  rangeDate: [{ required: true, message: 'Vui lòng nhập mục này!' }],
})

const disabledDate = (current) => {
  return current.isBefore(dayjs(), 'day') || current.isAfter(maxRangeDate.value, 'day')
}

const fetchDataSubject = () => {
  requestAPI
    .get(`${API_ROUTES_STAFF.FETCH_DATA_PLAN}/list/subject`)
    .then(({ data: response }) => {
      optSubject.value = response.data
    })
    .catch((error) => {
      message.error(error?.response?.data?.message || 'Lỗi khi lấy dữ liệu bộ môn')
    })
}

const fetchDataLevel = () => {
  requestAPI
    .get(`${API_ROUTES_STAFF.FETCH_DATA_PLAN}/list/level-project`)
    .then(({ data: response }) => {
      optLevel.value = response.data
    })
    .catch((error) => {
      message.error(error?.response?.data?.message || 'Lỗi khi lấy dữ liệu cấp độ dự án')
    })
}

const fetchDataSemester = () => {
  requestAPI
    .get(`${API_ROUTES_STAFF.FETCH_DATA_PLAN}/list/semester`)
    .then(({ data: response }) => {
      optSemester.value = response.data
    })
    .catch((error) => {
      message.error(error?.response?.data?.message || 'Lỗi khi lấy dữ liệu học kỳ')
    })
}

const fetchDataYear = () => {
  requestAPI
    .get(`${API_ROUTES_STAFF.FETCH_DATA_PLAN}/list/year`)
    .then(({ data: response }) => {
      optYear.value = response.data
    })
    .catch((error) => {
      message.error(error?.response?.data?.message || 'Lỗi khi lấy dữ liệu năm học')
    })
}

const fetchDataList = () => {
  if (isLoading.value === true) {
    return
  }

  isLoading.value = true
  requestAPI
    .get(`${API_ROUTES_STAFF.FETCH_DATA_PLAN}`, {
      params: {
        page: pagination.value.current,
        size: pagination.value.pageSize,
        ...dataFilter,
      },
    })
    .then(({ data: response }) => {
      lstData.value = response.data.data
      pagination.value.total = response.data.totalPages * pagination.value.pageSize
    })
    .catch((error) => {
      message.error(error?.response?.data?.message || 'Không thể tải danh sách dữ liệu')
    })
    .finally(() => {
      isLoading.value = false
    })
}

const fetchDataProjectList = () => {
  modalAdd.isLoading = true
  requestAPI
    .get(`${API_ROUTES_STAFF.FETCH_DATA_PLAN}/list/project`, {
      params: {
        ...dataFilterAdd,
      },
    })
    .then(({ data: response }) => {
      lstDataAdd.value = response.data
    })
    .catch((error) => {
      message.error(error?.response?.data?.message || 'Không thể tải danh sách dữ liệu dự án')
    })
    .finally(() => {
      modalAdd.isLoading = false
    })
}

const fetchSubmitCreate = () => {
  const data = {
    ...formDataAdd,
    rangeDate: [Date.parse(formDataAdd.rangeDate[0]), Date.parse(formDataAdd.rangeDate[1])],
  }
  modalAdd.isLoading = true
  requestAPI
    .post(`${API_ROUTES_STAFF.FETCH_DATA_PLAN}/create`, data)
    .then(({ data: response }) => {
      message.success(response.message)
      modalAdd.isShow = false
      fetchDataList()
    })
    .catch((error) => {
      message.error(error?.response?.data?.message || 'Không thể tạo mới kế hoạch')
    })
    .finally(() => {
      modalAdd.isLoading = false
    })
}

const handleClearFilter = () => {
  Object.assign(dataFilter, {
    keyword: null,
    level: null,
    semester: null,
    year: null,
    subject: null,
  })
  fetchDataList()
}

const handleSubmitFilter = () => {
  pagination.value.current = 1
  fetchDataList()
}

const handleSubmitFilterAdd = () => {
  if (
    !dataFilterAdd.level ||
    !dataFilterAdd.semester ||
    !dataFilterAdd.subject ||
    !dataFilterAdd.year
  ) {
    return
  }
  formDataAdd.idProject = null
  fetchDataProjectList()
}

const handleSubmitAdd = async () => {
  try {
    await formRefAdd.value.validate()
    Modal.confirm({
      title: `Xác nhận thêm mới`,
      type: 'info',
      content: `Bạn có chắc muốn tạo kế hoạch này?`,
      okText: 'Tiếp tục',
      cancelText: 'Hủy bỏ',
      onOk() {
        fetchSubmitCreate()
      },
    })
  } catch (error) {}
}

const handleTableChange = (page) => {
  pagination.value.current = page.current
  pagination.value.pageSize = page.pageSize
  fetchDataList()
}

const handleChangeProjectId = (id) => {
  const project = lstDataAdd.value.find((o) => o.id === id)
  maxRangeDate.value = dayjs(project.toDate)
  formDataAdd.rangeDate = [dayjs(), maxRangeDate.value]
}

const handleShowDetail = (id) => {
  router.push({ name: ROUTE_NAMES.MANAGEMENT_PLAN_DETAIL, params: { id: id } })
}

const handleShowModalAdd = () => {
  modalAdd.isShow = true

  maxRangeDate.value = dayjs()

  dataFilterAdd.level = null
  dataFilterAdd.semester = null
  dataFilterAdd.subject = null
  dataFilterAdd.year = null

  lstDataAdd.value = []

  formDataAdd.idProject = null
  formDataAdd.name = null
  formDataAdd.description = null
  formDataAdd.rangeDate = []
}

onMounted(() => {
  breadcrumbStore.setRoutes(breadcrumb.value)
  fetchDataSubject()
  fetchDataLevel()
  fetchDataSemester()
  fetchDataYear()
  fetchDataList()
})

watch(
  dataFilter,
  () => {
    handleSubmitFilter()
  },
  { deep: true },
)

watch(
  dataFilterAdd,
  () => {
    handleSubmitFilterAdd()
  },
  { deep: true },
)
</script>

<template>
  <a-modal
    v-model:open="modalAdd.isShow"
    v-bind="modalAdd"
    :okButtonProps="{ loading: modalAdd.isLoading }"
  >
    <template #title> <PlusOutlined class="text-primary" /> Tạo kế hoạch mới </template>
    <a-form
      ref="formRefAdd"
      class="row mt-3"
      layout="vertical"
      autocomplete="off"
      :model="formDataAdd"
    >
      <a-form-item class="col-sm-3" label="Học kỳ">
        <a-select
          v-model:value="dataFilterAdd.semester"
          class="w-100"
          :dropdownMatchSelectWidth="false"
          placeholder="-- Chọn 1 học kỳ --"
          allowClear
          :disabled="modalAdd.isLoading"
        >
          <a-select-option v-for="o in optSemester" :key="o" :value="o">
            {{ o }}
          </a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item class="col-sm-3" label="Năm học">
        <a-select
          v-model:value="dataFilterAdd.year"
          class="w-100"
          :dropdownMatchSelectWidth="false"
          placeholder="-- Chọn 1 năm --"
          allowClear
          :disabled="modalAdd.isLoading"
        >
          <a-select-option v-for="o in optYear" :key="o" :value="o">
            {{ o }}
          </a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item class="col-sm-3" label="Bộ môn">
        <a-select
          v-model:value="dataFilterAdd.subject"
          class="w-100"
          :dropdownMatchSelectWidth="false"
          placeholder="-- Chọn 1 bộ môn --"
          allowClear
          :disabled="modalAdd.isLoading"
        >
          <a-select-option v-for="o in optSubject" :key="o.id" :value="o.id">
            {{ `${o.code} - ${o.name}` }}
          </a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item class="col-sm-3" label="Cấp độ dự án">
        <a-select
          v-model:value="dataFilterAdd.level"
          class="w-100"
          :dropdownMatchSelectWidth="false"
          placeholder="-- Chọn 1 cấp độ --"
          allowClear
          :disabled="modalAdd.isLoading"
        >
          <a-select-option v-for="o in optLevel" :key="o.id" :value="o.id">
            {{ `${o.code} - ${o.name}` }}
          </a-select-option>
        </a-select>
      </a-form-item>

      <a-form-item
        class="col-sm-6"
        :label="`Dự án (${lstDataAdd.length})`"
        name="idProject"
        :rules="formRules.idProject"
      >
        <a-select
          v-model:value="formDataAdd.idProject"
          placeholder="-- Chọn 1 dự án --"
          class="w-100"
          :disabled="modalAdd.isLoading"
          @change="handleChangeProjectId"
        >
          <a-select-option v-for="o in lstDataAdd" :key="o.id" :value="o.id">
            {{ o.name }}
          </a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item
        class="col-sm-6"
        label="Thời gian diễn ra"
        name="rangeDate"
        :rules="formRules.rangeDate"
      >
        <a-range-picker
          class="w-100"
          :placeholder="[DEFAULT_DATE_FORMAT, DEFAULT_DATE_FORMAT]"
          v-model:value="formDataAdd.rangeDate"
          :format="DEFAULT_DATE_FORMAT"
          :disabledDate="disabledDate"
          :disabled="modalAdd.isLoading"
        />
      </a-form-item>

      <a-form-item class="col-sm-12" label="Tên kế hoạch" name="name" :rules="formRules.name">
        <a-input
          class="w-100"
          v-model:value="formDataAdd.name"
          :disabled="modalAdd.isLoading"
          allowClear
        />
      </a-form-item>

      <a-form-item
        class="col-sm-12"
        label="Nội dung kế hoạch"
        name="description"
        :rules="formRules.description"
      >
        <a-textarea
          class="w-100"
          v-model:value="formDataAdd.description"
          :disabled="modalAdd.isLoading"
          :rows="4"
          allowClear
        />
      </a-form-item>
    </a-form>
  </a-modal>

  <div class="container-fluid">
    <div class="row g-3">
      <div class="col-12">
        <!-- Bộ lọc tìm kiếm -->
        <a-card :bordered="false" class="cart">
          <template #title> <FilterFilled /> Bộ lọc </template>
          <div class="row g-2">
            <div class="col-md-8 col-sm-12">
              <div class="label-title">Từ khoá:</div>
              <a-input
                v-model:value="dataFilter.keyword"
                placeholder="Tìm theo tên kế hoạch..."
                allowClear
              >
                <template #prefix>
                  <SearchOutlined />
                </template>
              </a-input>
            </div>
            <div class="col-md-4 col-sm-6">
              <div class="label-title">Bộ môn:</div>
              <a-select
                v-model:value="dataFilter.subject"
                class="w-100"
                :dropdownMatchSelectWidth="false"
                placeholder="-- Tất cả bộ môn --"
                allowClear
              >
                <a-select-option :value="null">-- Tất cả bộ môn --</a-select-option>
                <a-select-option v-for="o in optSubject" :key="o.id" :value="o.id">
                  {{ `${o.code} - ${o.name}` }}
                </a-select-option>
              </a-select>
            </div>
            <div class="col-md-4 col-sm-6">
              <div class="label-title">Cấp độ dự án:</div>
              <a-select
                v-model:value="dataFilter.level"
                class="w-100"
                :dropdownMatchSelectWidth="false"
                placeholder="-- Tất cả level --"
                allowClear
              >
                <a-select-option :value="null">-- Tất cả level --</a-select-option>
                <a-select-option v-for="o in optLevel" :key="o.id" :value="o.id">
                  {{ `${o.code} - ${o.name}` }}
                </a-select-option>
              </a-select>
            </div>
            <div class="col-md-4 col-sm-6">
              <div class="label-title">Học kỳ:</div>
              <a-select
                v-model:value="dataFilter.semester"
                class="w-100"
                :dropdownMatchSelectWidth="false"
                placeholder="-- Tất cả học kỳ --"
                allowClear
              >
                <a-select-option :value="null">-- Tất cả học kỳ --</a-select-option>
                <a-select-option v-for="o in optSemester" :key="o" :value="o">
                  {{ o }}
                </a-select-option>
              </a-select>
            </div>
            <div class="col-md-4 col-sm-6">
              <div class="label-title">Năm học:</div>
              <a-select
                v-model:value="dataFilter.year"
                class="w-100"
                :dropdownMatchSelectWidth="false"
                placeholder="-- Tất cả năm học --"
                allowClear
              >
                <a-select-option :value="null">-- Tất cả năm học --</a-select-option>
                <a-select-option v-for="o in optYear" :key="o" :value="o">
                  {{ o }}
                </a-select-option>
              </a-select>
            </div>
            <div class="col-12">
              <div class="d-flex justify-content-center flex-wrap gap-2 mt-3">
                <a-button class="btn-light" @click="handleSubmitFilter">
                  <FilterFilled /> Lọc
                </a-button>
                <a-button class="btn-gray" @click="handleClearFilter"> Huỷ lọc </a-button>
              </div>
            </div>
          </div>
        </a-card>
      </div>

      <div class="col-12">
        <a-card :bordered="false" class="cart">
          <template #title> <UnorderedListOutlined /> Danh sách kế hoạch </template>
          <div class="d-flex justify-content-end mb-3 gap-3">
            <a-button type="primary" @click="handleShowModalAdd">
              <PlusOutlined /> Tạo kế hoạch mới
            </a-button>
          </div>

          <a-table
            rowKey="id"
            class="nowrap"
            :dataSource="lstData"
            :columns="columns"
            :loading="isLoading"
            :pagination="pagination"
            :scroll="{ y: 500, x: 'auto' }"
            @change="handleTableChange"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.dataIndex === 'semesterName'">
                <span class="text-info">{{ formatDate(record.fromDate) }}</span> -
                <span class="text-danger">{{ formatDate(record.toDate) }}</span>
                <a-tag class="ms-2">{{ record.semesterName }}</a-tag>
              </template>
              <template v-if="column.dataIndex === 'status'">
                <a-tag :color="record.status === 1 ? 'green' : 'red'">{{
                  record.status === 1 ? 'Đang triển khai' : 'Không triển khai'
                }}</a-tag>
              </template>
              <template v-if="column.key === 'actions'">
                <a-tooltip title="Chi tiết kế hoạch">
                  <a-button class="btn-outline-primary" @click="handleShowDetail(record.id)">
                    <AlignLeftOutlined />
                  </a-button>
                </a-tooltip>
              </template>
            </template>
          </a-table>
        </a-card>
      </div>
    </div>
  </div>
</template>
