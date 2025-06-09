<script setup>
import { ref, onMounted, watch, reactive } from 'vue'
import {
  FilterFilled,
  UnorderedListOutlined,
  SearchOutlined,
  EyeFilled,
  ExclamationCircleOutlined,
} from '@ant-design/icons-vue'
import { message, Modal } from 'ant-design-vue'
import requestAPI from '@/services/requestApiService'
import { DEFAULT_PAGINATION, TYPE_SHIFT } from '@/constants'
import useBreadcrumbStore from '@/stores/useBreadCrumbStore'
import { API_ROUTES_EXCEL, GLOBAL_ROUTE_NAMES } from '@/constants/routesConstant'
import { ROUTE_NAMES } from '@/router/teacherRoute'
import useLoadingStore from '@/stores/useLoadingStore'
import { useRoute, useRouter } from 'vue-router'
import { DEFAULT_DATE_FORMAT, SHIFT, STATUS_PLAN_DATE_DETAIL } from '@/constants'
import { autoAddColumnWidth, dayOfWeek, debounce, formatDate } from '@/utils/utils'
import { API_ROUTES_TEACHER } from '@/constants/teacherConstant'
import ExcelUploadButton from '@/components/excel/ExcelUploadButton.vue'

const route = useRoute()
const router = useRouter()

const breadcrumbStore = useBreadcrumbStore()
const loadingStore = useLoadingStore()

const isLoading = ref(false)

const _detail = ref(null)
const lstData = ref([])
const lstShift = ref([])

const configImportExcel = {
  fetchUrl: API_ROUTES_EXCEL.FETCH_IMPORT_PLAN_DATE,
  data: { idFactory: route.params?.id },
  showDownloadTemplate: false,
  showHistoryLog: false,
  showExport: true,
  showImport: false,
  btnExport: 'Export chi tiết điểm danh',
}

const columns = ref(
  autoAddColumnWidth([
    { title: 'Buổi', dataIndex: 'orderNumber', key: 'orderNumber' },
    { title: 'Ngày học', dataIndex: 'startDate', key: 'startDate' },
    { title: 'Thời gian', key: 'time' },
    { title: 'Ca học', dataIndex: 'shift', key: 'shift' },
    { title: 'Nội dung', dataIndex: 'description', key: 'description' },
    { title: 'Phòng học', dataIndex: 'room', key: 'room' },
    { title: 'Link Online', dataIndex: 'link', key: 'link' },
    {
      title: 'Điểm danh trễ',
      dataIndex: 'lateArrival',
      key: 'lateArrival',
      align: 'center',
    },
    { title: 'Trạng thái', dataIndex: 'status', key: 'status' },
    { title: '', key: 'actions' },
  ]),
)

const breadcrumb = ref([
  {
    name: GLOBAL_ROUTE_NAMES.TEACHER_PAGE,
    breadcrumbName: 'Giảng viên',
  },
  {
    name: ROUTE_NAMES.MANAGEMENT_FACTORY,
    breadcrumbName: 'Nhóm xưởng của tôi',
  },
])

const pagination = ref({ ...DEFAULT_PAGINATION })

const dataFilter = reactive({
  keyword: null,
  status: null,
  shift: null,
  type: null,
  startDate: null,
})

const fetchDataDetail = () => {
  loadingStore.show()
  requestAPI
    .get(`${API_ROUTES_TEACHER.FETCH_DATA_SHIFT_FACTORY}/${route.params.id}`)
    .then(({ data: response }) => {
      _detail.value = response.data
      breadcrumbStore.push({
        breadcrumbName: 'Danh sách ca học - ' + _detail.value.factoryName,
      })
      fetchDataList()
    })
    .catch((error) => {
      message.error(error?.response?.data?.message || 'Không thể tải thông tin kế hoạch')
      router.push({ name: ROUTE_NAMES.MANAGEMENT_PLAN_FACTORY, params: { id: route.params?.id } })
    })
    .finally(() => {
      loadingStore.hide()
    })
}

const fetchDataList = () => {
  if (isLoading.value === true) {
    return
  }

  isLoading.value = true
  requestAPI
    .get(`${API_ROUTES_TEACHER.FETCH_DATA_SHIFT_FACTORY}/${route.params.id}/list`, {
      params: {
        page: pagination.value.current,
        size: pagination.value.pageSize,
        ...dataFilter,
        startDate: (dataFilter.startDate && Date.parse(dataFilter.startDate)) || null,
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

const fetchDataShift = () => {
  requestAPI
    .get(`${API_ROUTES_TEACHER.FETCH_DATA_SHIFT_FACTORY}/list/shift`)
    .then(({ data: response }) => {
      lstShift.value = response.data
    })
    .catch((error) => {
      message.error(error?.response?.data?.message || 'Lỗi khi lấy dữ liệu ca học')
    })
}

const handleClearFilter = () => {
  Object.assign(dataFilter, {
    keyword: null,
    status: null,
    shift: null,
    type: null,
    startDate: null,
  })
  fetchDataList()
}

const handleSubmitFilter = () => {
  pagination.value.current = 1
  fetchDataList()
}

const handleTableChange = (page) => {
  pagination.value.current = page.current
  pagination.value.pageSize = page.pageSize
  fetchDataList()
}

const handleShowDescription = (text) => {
  Modal.info({
    title: 'Nội dung chi tiết',
    type: 'info',
    content: text,
    okText: 'Đóng',
    okButtonProps: {
      class: 'btn-gray',
    },
  })
}

const handleShowAttendance = (id) => {
  router.push({
    name: ROUTE_NAMES.MANAGEMENT_PLANDATE_ATTENDANCE_FACTORY,
    params: { id: id },
  })
}

onMounted(() => {
  breadcrumbStore.setRoutes(breadcrumb.value)
  fetchDataDetail()
  fetchDataShift()
})

const debounceFilter = debounce(handleSubmitFilter, 100)
watch(
  dataFilter,
  () => {
    debounceFilter()
  },
  { deep: true },
)
</script>

<template>
  <div class="container-fluid">
    <div class="row g-3">
      <div class="col-12">
        <a-card :bordered="false" class="cart no-body-padding">
          <a-collapse ghost>
            <a-collapse-panel>
              <template #header><FilterFilled /> Bộ lọc</template>
              <div class="row g-2">
                <div class="col-xxl-4 col-lg-8 col-md-8 col-sm-12">
                  <div class="label-title">Từ khoá:</div>
                  <a-input
                    v-model:value="dataFilter.keyword"
                    placeholder="Tìm theo nội dung..."
                    allowClear
                  >
                    <template #prefix>
                      <SearchOutlined />
                    </template>
                  </a-input>
                </div>
                <div class="col-xxl-2 col-lg-4 col-md-4 col-sm-6">
                  <div class="label-title">Trạng thái:</div>
                  <a-select
                    v-model:value="dataFilter.status"
                    class="w-100"
                    :dropdownMatchSelectWidth="false"
                    placeholder="-- Tất cả trạng thái --"
                    allowClear
                  >
                    <a-select-option :value="null">-- Tất cả trạng thái --</a-select-option>
                    <a-select-option
                      v-for="(name, id) in STATUS_PLAN_DATE_DETAIL"
                      :key="id"
                      :value="id"
                    >
                      {{ name }}
                    </a-select-option>
                  </a-select>
                </div>
                <div class="col-xxl-2 col-lg-4 col-md-4 col-sm-6">
                  <div class="label-title">Hình thức học:</div>
                  <a-select
                    v-model:value="dataFilter.type"
                    class="w-100"
                    :dropdownMatchSelectWidth="false"
                    placeholder="-- Tất cả hình thức --"
                    allowClear
                  >
                    <a-select-option :value="null">-- Tất cả hình thức --</a-select-option>
                    <a-select-option v-for="(name, id) in TYPE_SHIFT" :key="id" :value="id">
                      {{ name }}
                    </a-select-option>
                  </a-select>
                </div>
                <div class="col-xxl-2 col-lg-4 col-md-4 col-sm-6">
                  <div class="label-title">Ca học:</div>
                  <a-select
                    v-model:value="dataFilter.shift"
                    class="w-100"
                    :dropdownMatchSelectWidth="false"
                    placeholder="-- Tất cả ca học --"
                    allowClear
                  >
                    <a-select-option :value="null">-- Tất cả ca học --</a-select-option>
                    <a-select-option v-for="o in lstShift" :key="o.id" :value="o.shift">
                      {{ SHIFT[o.shift] }}
                    </a-select-option>
                  </a-select>
                </div>
                <div class="col-xxl-2 col-lg-4 col-md-4 col-sm-6">
                  <div class="label-title">Ngày diễn ra:</div>
                  <a-date-picker
                    class="w-100"
                    placeholder="-- Tất cả các ngày --"
                    v-model:value="dataFilter.startDate"
                    :format="DEFAULT_DATE_FORMAT"
                  />
                </div>
                <div class="col-12">
                  <div class="d-flex justify-content-center flex-wrap gap-2">
                    <a-button class="btn-light" @click="handleSubmitFilter">
                      <FilterFilled /> Lọc
                    </a-button>
                    <a-button class="btn-gray" @click="handleClearFilter"> Huỷ lọc </a-button>
                  </div>
                </div>
              </div>
            </a-collapse-panel>
          </a-collapse>
        </a-card>
      </div>

      <div class="col-12">
        <a-card :bordered="false" class="cart">
          <template #title>
            <UnorderedListOutlined /> Danh sách ca học
            {{ `(${formatDate(_detail?.fromDate)} - ${formatDate(_detail?.toDate)})` }}
          </template>

          <div class="d-flex justify-content-end flex-wrap gap-3 mb-2">
            <ExcelUploadButton v-bind="configImportExcel" />
          </div>

          <div>
            <a-table
              rowKey="id"
              class="nowrap"
              :dataSource="lstData"
              :columns="columns"
              :loading="isLoading"
              :pagination="pagination"
              :scroll="{ x: 'auto' }"
              :row-selection="rowSelection"
              @change="handleTableChange"
            >
              <template #bodyCell="{ column, record }">
                <template v-if="column.dataIndex === 'description' && record.description">
                  <a-typography-link @click="handleShowDescription(record.description)"
                    >Chi tiết</a-typography-link
                  >
                </template>
                <template v-if="column.dataIndex === 'link' && record.link">
                  <a target="_blank" :href="record.link">Link</a>
                </template>
                <template v-if="column.dataIndex === 'lateArrival'">
                  <a-tag color="gold">
                    <ExclamationCircleOutlined /> {{ `${record.lateArrival} phút` }}
                  </a-tag>
                </template>
                <template v-if="column.dataIndex === 'startDate'">
                  {{
                    `${dayOfWeek(record.startDate)}, ${formatDate(
                      record.startDate,
                      DEFAULT_DATE_FORMAT,
                    )}`
                  }}
                </template>
                <template v-if="column.key === 'time'">
                  {{
                    `${formatDate(record.startDate, 'HH:mm')} - ${formatDate(
                      record.endDate,
                      'HH:mm',
                    )}`
                  }}
                </template>
                <template v-if="column.dataIndex === 'shift'">
                  <a-tag :color="record.type === 1 ? 'blue' : 'purple'">
                    {{
                      `Ca ${record.shift
                        .split(',')
                        .map((o) => Number(o))
                        .join(', ')} - ${TYPE_SHIFT[record.type]}`
                    }}
                  </a-tag>
                </template>
                <template v-if="column.dataIndex === 'status'">
                  <a-badge :status="record.status === 'DA_DIEN_RA' ? 'error' : 'success'" />
                  {{ STATUS_PLAN_DATE_DETAIL[record.status] }}
                </template>
                <template v-if="column.dataIndex === 'totalShift'">
                  <a-tag>
                    {{ record.totalShift }}
                  </a-tag>
                </template>
                <template v-if="column.key === 'actions'">
                  <a-tooltip title="Chi tiết điểm danh" v-if="record.status === 'DA_DIEN_RA'">
                    <a-button
                      class="btn-outline-primary border-0 me-2"
                      @click="handleShowAttendance(record.id)"
                    >
                      <EyeFilled />
                    </a-button>
                  </a-tooltip>
                </template>
              </template>
            </a-table>
          </div>
        </a-card>
      </div>
    </div>
  </div>
</template>
