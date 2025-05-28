<script setup>
import { ref, onMounted, watch, reactive, h } from 'vue'
import {
  PlusOutlined,
  FilterFilled,
  UnorderedListOutlined,
  SearchOutlined,
  AlignLeftOutlined,
  DeleteFilled,
  EditFilled,
} from '@ant-design/icons-vue'
import { message, Modal } from 'ant-design-vue'
import requestAPI from '@/services/requestApiService'
import { DEFAULT_DATE_FORMAT, DEFAULT_PAGINATION } from '@/constants'
import { API_ROUTES_STAFF } from '@/constants/staffConstant'
import { useRouter } from 'vue-router'
import { ROUTE_NAMES } from '@/router/staffRoute'
import { GLOBAL_ROUTE_NAMES } from '@/constants/routesConstant'
import useBreadcrumbStore from '@/stores/useBreadCrumbStore'
import { autoAddColumnWidth, debounce, formatDate } from '@/utils/utils'
import dayjs from 'dayjs'

const router = useRouter()
const breadcrumbStore = useBreadcrumbStore()

const isLoading = ref(false)
const modalAddOrUpdate = reactive({
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
const lstDataProject = ref([])

const formRefAddOrUpdate = ref(null)

const minRangeDate = ref(dayjs())
const maxRangeDate = ref(dayjs())

const currentProject = ref(null)

const columns = ref(
  autoAddColumnWidth([
    { title: '#', dataIndex: 'orderNumber', key: 'orderNumber' },
    { title: 'Tên kế hoạch', dataIndex: 'planName', key: 'planName' },
    { title: 'Tên dự án', dataIndex: 'projectName', key: 'projectName' },
    { title: 'Nội dung', dataIndex: 'description', key: 'description' },
    { title: 'Bộ môn', dataIndex: 'subjectName', key: 'subjectName' },
    { title: 'Cấp độ', dataIndex: 'level', key: 'level' },
    { title: 'Ngày diễn ra', dataIndex: 'semesterName', key: 'semesterName' },
    { title: 'Trạng thái', dataIndex: 'status', key: 'status' },
    { title: '', key: 'actions' },
  ]),
)

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
  status: null,
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

const formData = reactive({
  id: null,
  idProject: null,
  name: null,
  description: null,
  rangeDate: [],
})

const formRules = reactive({
  idProject: [{ required: true, message: 'Vui lòng chọn 1 dự án!' }],
  name: [{ required: true, message: 'Vui lòng nhập mục này!' }],
  rangeDate: [{ required: true, message: 'Vui lòng nhập mục này!' }],
})

const disabledDate = (current) => {
  return current.isBefore(minRangeDate.value, 'day') || current.isAfter(maxRangeDate.value, 'day')
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
  modalAddOrUpdate.isLoading = true
  requestAPI
    .get(`${API_ROUTES_STAFF.FETCH_DATA_PLAN}/list/project`, {
      params: {
        ...dataFilterAdd,
      },
    })
    .then(({ data: response }) => {
      lstDataProject.value = response.data.filter((o) => o.id !== currentProject.value?.id)
      if (currentProject.value) {
        lstDataProject.value.push(currentProject.value)
        formData.idProject = currentProject.value.id
      }
    })
    .catch((error) => {
      message.error(error?.response?.data?.message || 'Không thể tải danh sách dữ liệu dự án')
    })
    .finally(() => {
      modalAddOrUpdate.isLoading = false
    })
}

const fetchSubmitCreate = () => {
  const data = {
    ...formData,
    rangeDate: [Date.parse(formData.rangeDate[0]), Date.parse(formData.rangeDate[1])],
  }
  modalAddOrUpdate.isLoading = true
  requestAPI
    .post(`${API_ROUTES_STAFF.FETCH_DATA_PLAN}`, data)
    .then(({ data: response }) => {
      message.success(response.message)
      modalAddOrUpdate.isShow = false
      fetchDataList()
    })
    .catch((error) => {
      message.error(error?.response?.data?.message || 'Không thể tạo mới kế hoạch')
    })
    .finally(() => {
      modalAddOrUpdate.isLoading = false
    })
}

const fetchSubmitUpdate = () => {
  const data = {
    ...formData,
    rangeDate: [Date.parse(formData.rangeDate[0]), Date.parse(formData.rangeDate[1])],
  }
  modalAddOrUpdate.isLoading = true
  requestAPI
    .put(`${API_ROUTES_STAFF.FETCH_DATA_PLAN}`, data)
    .then(({ data: response }) => {
      message.success(response.message)
      modalAddOrUpdate.isShow = false
      fetchDataList()
    })
    .catch((error) => {
      message.error(error?.response?.data?.message || 'Không thể cập nhật kế hoạch')
    })
    .finally(() => {
      modalAddOrUpdate.isLoading = false
    })
}

const fetchSubmitChangeStatus = (id) => {
  requestAPI
    .put(`${API_ROUTES_STAFF.FETCH_DATA_PLAN}/${id}/change-status`)
    .then(({ data: response }) => {
      message.success(response.message)
      fetchDataList()
    })
    .catch((error) => {
      message.error(error?.response?.data?.message || 'Không thể thay đổi trạng thái kế hoạch')
    })
}

const fetchSubmitDelete = (id) => {
  requestAPI
    .delete(`${API_ROUTES_STAFF.FETCH_DATA_PLAN}/${id}`)
    .then(({ data: response }) => {
      message.success(response.message)
      fetchDataList()
    })
    .catch((error) => {
      message.error(error?.response?.data?.message || 'Không thể xoá kế hoạch này')
    })
}

const handleShowDescription = (text) => {
  Modal.info({
    title: 'Nội dung kế hoạch',
    type: 'info',
    content: text,
    okText: 'Đóng',
    okButtonProps: {
      class: 'btn-gray',
    },
  })
}

const handleClearFilter = () => {
  Object.assign(dataFilter, {
    keyword: null,
    status: null,
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
    !dataFilterAdd.level &&
    !dataFilterAdd.semester &&
    !dataFilterAdd.subject &&
    !dataFilterAdd.year
  ) {
    return (lstDataProject.value = lstDataProject.value.filter(
      (o) => o.id === currentProject.value?.id,
    ))
  }
  formData.idProject = null
  fetchDataProjectList()
}

const handleSubmitAdd = async () => {
  try {
    await formRefAddOrUpdate.value.validate()
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

const handleSubmitUpdate = async () => {
  try {
    await formRefAddOrUpdate.value.validate()
    Modal.confirm({
      title: `Xác nhận cập nhật`,
      type: 'info',
      content: `Mọi dữ liệu dư thừa trong khoảng thời gian diễn ra có thể mất. Bạn có chắc muốn cập nhật kế hoạch này?`,
      okText: 'Tiếp tục',
      cancelText: 'Hủy bỏ',
      onOk() {
        fetchSubmitUpdate()
      },
    })
  } catch (error) {}
}

const handleChangeStatus = (id) => {
  Modal.confirm({
    title: `Xác nhận thay đổi trạng thái`,
    type: 'info',
    content: `Bạn có chắc muốn thay đổi trạng thái kế hoạch này?`,
    okText: 'Tiếp tục',
    cancelText: 'Hủy bỏ',
    onOk() {
      fetchSubmitChangeStatus(id)
    },
  })
}

const handleDelete = (id) => {
  Modal.confirm({
    title: `Xác nhận xoá kế hoạch`,
    type: 'warning',
    content: `Mọi dữ liệu điểm danh sẽ bị xoá. Bạn vẫn muốn tiếp tục?`,
    okText: 'Tiếp tục',
    cancelText: 'Hủy bỏ',
    onOk() {
      fetchSubmitDelete(id)
    },
  })
}

const handleTableChange = (page) => {
  pagination.value.current = page.current
  pagination.value.pageSize = page.pageSize
  fetchDataList()
}

const handleChangeProjectId = (id) => {
  currentProject.value = lstDataProject.value.find((o) => o.id === id)
  minRangeDate.value = dayjs(currentProject.value.fromDate)
  maxRangeDate.value = dayjs(currentProject.value.toDate)
  formData.rangeDate = [currentProject.value ? minRangeDate.value : dayjs(), maxRangeDate.value]
}

const handleShowDetail = (id) => {
  router.push({ name: ROUTE_NAMES.MANAGEMENT_PLAN_FACTORY, params: { id: id } })
}

const handleShowModalAdd = () => {
  if (formRefAddOrUpdate.value) {
    formRefAddOrUpdate.value.clearValidate()
  }
  modalAddOrUpdate.isShow = true
  modalAddOrUpdate.isLoading = false
  modalAddOrUpdate.title = h('span', [
    h(PlusOutlined, { class: 'me-2 text-primary' }),
    'Tạo kế hoạch mới',
  ])
  modalAddOrUpdate.okText = 'Thêm ngay'
  modalAddOrUpdate.onOk = () => handleSubmitAdd()

  currentProject.value = null

  minRangeDate.value = dayjs()
  maxRangeDate.value = dayjs()

  dataFilterAdd.level = null
  dataFilterAdd.semester = null
  dataFilterAdd.subject = null
  dataFilterAdd.year = null

  lstDataProject.value = []

  formData.id = null
  formData.idProject = null
  formData.name = null
  formData.description = null
  formData.rangeDate = []
}

const handleShowModalUpdate = (item) => {
  if (formRefAddOrUpdate.value) {
    formRefAddOrUpdate.value.clearValidate()
  }
  modalAddOrUpdate.isShow = true
  modalAddOrUpdate.isLoading = false
  modalAddOrUpdate.title = h('span', [
    h(EditFilled, { class: 'me-2 text-primary' }),
    'Chỉnh sửa kế hoạch',
  ])
  modalAddOrUpdate.okText = 'Lưu lại'
  modalAddOrUpdate.onOk = () => handleSubmitUpdate()

  currentProject.value = {
    id: item.projectId,
    name: item.projectName + ' (hiện tại)',
    toDate: item.toDateSemester,
    fromDate: item.fromDateSemester,
  }

  minRangeDate.value = dayjs(item.fromDateSemester)
  maxRangeDate.value = dayjs(item.toDateSemester)

  dataFilterAdd.level = null
  dataFilterAdd.semester = null
  dataFilterAdd.subject = null
  dataFilterAdd.year = null

  lstDataProject.value = [currentProject.value]

  formData.id = item.id
  formData.idProject = item.projectId
  formData.name = item.planName
  formData.description = item.description
  formData.rangeDate = [dayjs(item.fromDate), dayjs(item.toDate)]
}

onMounted(() => {
  breadcrumbStore.setRoutes(breadcrumb.value)
  fetchDataSubject()
  fetchDataLevel()
  fetchDataSemester()
  fetchDataYear()
  fetchDataList()
})

const debounceFilter = debounce(handleSubmitFilter, 100)
watch(
  dataFilter,
  () => {
    debounceFilter()
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
    v-model:open="modalAddOrUpdate.isShow"
    v-bind="modalAddOrUpdate"
    :okButtonProps="{ loading: modalAddOrUpdate.isLoading }"
  >
    <a-form
      ref="formRefAddOrUpdate"
      class="row mt-3"
      layout="vertical"
      autocomplete="off"
      :model="formData"
    >
      <div class="col-12 mb-2">Tìm kiếm dự án</div>
      <a-form-item class="col-sm-3">
        <a-select
          v-model:value="dataFilterAdd.semester"
          class="w-100"
          :dropdownMatchSelectWidth="false"
          placeholder="-- Chọn 1 học kỳ --"
          allowClear
          :disabled="modalAddOrUpdate.isLoading"
        >
          <a-select-option v-for="o in optSemester" :key="o" :value="o">
            {{ o }}
          </a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item class="col-sm-3">
        <a-select
          v-model:value="dataFilterAdd.year"
          class="w-100"
          :dropdownMatchSelectWidth="false"
          placeholder="-- Chọn 1 năm --"
          allowClear
          :disabled="modalAddOrUpdate.isLoading"
        >
          <a-select-option v-for="o in optYear" :key="o" :value="o">
            {{ o }}
          </a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item class="col-sm-3">
        <a-select
          v-model:value="dataFilterAdd.subject"
          class="w-100"
          :dropdownMatchSelectWidth="false"
          placeholder="-- Chọn 1 bộ môn --"
          allowClear
          :disabled="modalAddOrUpdate.isLoading"
        >
          <a-select-option v-for="o in optSubject" :key="o.id" :value="o.id">
            {{ `${o.code} - ${o.name}` }}
          </a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item class="col-sm-3">
        <a-select
          v-model:value="dataFilterAdd.level"
          class="w-100"
          :dropdownMatchSelectWidth="false"
          placeholder="-- Chọn 1 cấp độ --"
          allowClear
          :disabled="modalAddOrUpdate.isLoading"
        >
          <a-select-option v-for="o in optLevel" :key="o.id" :value="o.id">
            {{ `${o.code} - ${o.name}` }}
          </a-select-option>
        </a-select>
      </a-form-item>

      <div class="col-12">
        <hr class="mb-3" />
      </div>

      <a-form-item
        class="col-sm-6"
        :label="`Dự án (${lstDataProject.length})`"
        name="idProject"
        :rules="formRules.idProject"
      >
        <a-select
          v-model:value="formData.idProject"
          placeholder="-- Chọn 1 dự án --"
          class="w-100"
          :disabled="modalAddOrUpdate.isLoading"
          @change="handleChangeProjectId"
        >
          <a-select-option v-for="o in lstDataProject" :key="o.id" :value="o.id">
            {{ o.name }}
          </a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item
        class="col-sm-6"
        label="Khoảng thời gian diễn ra"
        name="rangeDate"
        :rules="formRules.rangeDate"
      >
        <a-range-picker
          class="w-100"
          :placeholder="[DEFAULT_DATE_FORMAT, DEFAULT_DATE_FORMAT]"
          v-model:value="formData.rangeDate"
          :format="DEFAULT_DATE_FORMAT"
          :disabledDate="disabledDate"
          :disabled="modalAddOrUpdate.isLoading"
        />
      </a-form-item>

      <a-form-item class="col-sm-12" label="Tên kế hoạch" name="name" :rules="formRules.name">
        <a-input
          class="w-100"
          v-model:value="formData.name"
          :disabled="modalAddOrUpdate.isLoading"
          allowClear
          @keyup.enter="modalAddOrUpdate.onOk"
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
          v-model:value="formData.description"
          :disabled="modalAddOrUpdate.isLoading"
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
            <div class="col-xxl-2 col-md-4 col-sm-12">
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
            <div class="col-xxl-2 col-md-4 col-sm-12">
              <div class="label-title">Trạng thái:</div>
              <a-select
                v-model:value="dataFilter.status"
                class="w-100"
                :dropdownMatchSelectWidth="false"
                placeholder="-- Tất cả trạng thái --"
                allowClear
              >
                <a-select-option :value="null">-- Tất cả trạng thái --</a-select-option>
                <a-select-option :value="1">Đang triển khai</a-select-option>
                <a-select-option :value="0">Ngừng triển khai</a-select-option>
              </a-select>
            </div>
            <div class="col-xxl-2 col-md-4 col-sm-6">
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
            <div class="col-xxl-2 col-md-4 col-sm-6">
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
            <div class="col-xxl-2 col-md-4 col-sm-6">
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
            <div class="col-xxl-2 col-md-4 col-sm-6">
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
              <div class="d-flex justify-content-center flex-wrap gap-2 mt-2">
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
            :scroll="{ x: 'auto' }"
            @change="handleTableChange"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.dataIndex === 'description'">
                <a-typography-link @click="handleShowDescription(record.description)"
                  >Chi tiết</a-typography-link
                >
              </template>
              <template v-if="column.key === 'planName'">
                <RouterLink
                  :to="{ name: ROUTE_NAMES.MANAGEMENT_PLAN_FACTORY, params: { id: record.id } }"
                >
                  {{ record.planName }}
                </RouterLink>
              </template>

              <template v-if="column.dataIndex === 'semesterName'">
                <span>{{ formatDate(record.fromDate) }}</span> -
                <span>{{ formatDate(record.toDate) }}</span>
                <a-tag class="ms-2" color="orange">{{ record.semesterName }}</a-tag>
              </template>
              <template v-if="column.dataIndex === 'status'">
                <a-switch
                  class="me-2"
                  :checked="record.status === 1"
                  @change="handleChangeStatus(record.id)"
                />
                <a-tag :color="record.status === 1 ? 'green' : 'red'">{{
                  record.status === 1 ? 'Đang triển khai' : 'Ngừng triển khai'
                }}</a-tag>
              </template>
              <template v-if="column.key === 'actions'">
                <a-tooltip title="Phân công nhóm xưởng">
                  <a-button
                    class="btn-outline-primary border-0 me-2"
                    @click="handleShowDetail(record.id)"
                  >
                    <AlignLeftOutlined />
                  </a-button>
                </a-tooltip>
                <a-tooltip title="Chỉnh sửa kế hoạch">
                  <a-button
                    class="btn-outline-info border-0"
                    @click="handleShowModalUpdate(record)"
                  >
                    <EditFilled />
                  </a-button>
                </a-tooltip>

                <a-tooltip title="Xoá kế hoạch" v-if="record.status !== 1">
                  <a-button
                    class="btn-outline-danger border-0 ms-2"
                    @click="handleDelete(record.id)"
                  >
                    <DeleteFilled />
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
