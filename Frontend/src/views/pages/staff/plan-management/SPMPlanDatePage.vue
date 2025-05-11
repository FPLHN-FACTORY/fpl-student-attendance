<script setup>
import { ref, onMounted, watch, reactive, h, computed, unref } from 'vue'
import {
  PlusOutlined,
  FilterFilled,
  UnorderedListOutlined,
  SearchOutlined,
  EditFilled,
  DeleteFilled,
  EyeFilled,
} from '@ant-design/icons-vue'
import { message, Modal } from 'ant-design-vue'
import requestAPI from '@/services/requestApiService'
import { DEFAULT_PAGINATION, STATUS_TYPE, TYPE_SHIFT } from '@/constants'
import { API_ROUTES_STAFF } from '@/constants/staffConstant'
import useBreadcrumbStore from '@/stores/useBreadCrumbStore'
import { API_ROUTES_EXCEL, GLOBAL_ROUTE_NAMES } from '@/constants/routesConstant'
import { ROUTE_NAMES } from '@/router/staffRoute'
import useLoadingStore from '@/stores/useLoadingStore'
import { useRoute, useRouter } from 'vue-router'
import {
  DEFAULT_DATE_FORMAT,
  DEFAULT_LATE_ARRIVAL,
  DEFAULT_MAX_LATE_ARRIVAL,
  SHIFT,
  STATUS_PLAN_DATE_DETAIL,
} from '@/constants'
import { autoAddColumnWidth, dayOfWeek, debounce, formatDate, rowSelectTable } from '@/utils/utils'
import dayjs from 'dayjs'
import ExcelUploadButton from '@/components/excel/ExcelUploadButton.vue'

const route = useRoute()
const router = useRouter()

const breadcrumbStore = useBreadcrumbStore()
const loadingStore = useLoadingStore()

const isLoading = ref(false)

const configImportExcel = {
  fetchUrl: API_ROUTES_EXCEL.FETCH_IMPORT_PLAN_DATE,
  onSuccess: () => {
    fetchDataList()
  },
  onError: () => {
    message.error('Không thể xử lý file excel')
  },
  data: { idPlanFactory: route.params?.id },
  showDownloadTemplate: true,
  showHistoryLog: true,
}

const modalAddOrUpdate = reactive({
  isShow: false,
  isLoading: false,
  title: null,
  cancelText: 'Hủy bỏ',
  okText: 'Xác nhận',
  onOk: null,
  width: 800,
})

const _detail = ref(null)
const lstData = ref([])
const lstShift = ref([])

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
    },
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
  shift: null,
  type: null,
  startDate: null,
})

const formRefAddOrUpdate = ref(null)

const formData = reactive({
  id: null,
  idPlan: null,
  description: null,
  shift: [],
  link: null,
  type: null,
  room: null,
  requiredLocation: STATUS_TYPE.ENABLE,
  requiredIp: STATUS_TYPE.ENABLE,
  startDate: null,
  lateArrival: DEFAULT_LATE_ARRIVAL,
})

const formRules = reactive({
  startDate: [{ required: true, message: 'Vui lòng chọn ngày học diễn ra!' }],
  shift: [{ required: true, message: 'Vui lòng chọn ca học!' }],
  type: [{ required: true, message: 'Vui lòng chọn hình thức học!' }],
  lateArrival: [{ required: true, message: 'Vui lòng nhập thời gian điểm danh muộn tối đa!' }],
})

const disabledDate = (current) => {
  return current.isBefore(dayjs(), 'day') || current.isAfter(dayjs(_detail.value?.toDate), 'day')
}

const fetchDataDetail = () => {
  loadingStore.show()
  requestAPI
    .get(`${API_ROUTES_STAFF.FETCH_DATA_PLAN_DATE}/${route.params.id}`)
    .then(({ data: response }) => {
      _detail.value = response.data
      formData.idPlan = _detail.value.planId
      breadcrumbStore.push({
        name: ROUTE_NAMES.MANAGEMENT_PLAN_FACTORY,
        params: { id: _detail.value.planId },
        breadcrumbName: _detail.value.planName,
      })
      breadcrumbStore.push({
        breadcrumbName: _detail.value.factoryName,
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
    .get(`${API_ROUTES_STAFF.FETCH_DATA_PLAN_DATE}/${_detail.value.id}/list`, {
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
    .get(`${API_ROUTES_STAFF.FETCH_DATA_PLAN_FACTORY}/list/shift`)
    .then(({ data: response }) => {
      lstShift.value = response.data
    })
    .catch((error) => {
      message.error(error?.response?.data?.message || 'Lỗi khi lấy dữ liệu ca học')
    })
}

const fetchDeleteItem = (id) => {
  loadingStore.show()

  requestAPI
    .delete(`${API_ROUTES_STAFF.FETCH_DATA_PLAN_DATE}/${_detail.value.id}/delete/${id}`)
    .then(({ data: response }) => {
      message.success(response.message)
      fetchDataList()
    })
    .catch((error) => {
      message.error(error?.response?.data?.message || 'Không thể xoá mục này')
    })
    .finally(() => {
      loadingStore.hide()
    })
}

const fetchDeleteMultipleItem = (ids) => {
  loadingStore.show()

  requestAPI
    .delete(`${API_ROUTES_STAFF.FETCH_DATA_PLAN_DATE}/${_detail.value.id}/delete`, {
      data: {
        ids: ids,
      },
    })
    .then(({ data: response }) => {
      message.success(response.message)
      selectedRowKeys.value = []
      fetchDataList()
    })
    .catch((error) => {
      message.error(error?.response?.data?.message || 'Không thể xoá mục đã chọn')
    })
    .finally(() => {
      loadingStore.hide()
    })
}

const fetchAddItem = () => {
  modalAddOrUpdate.isLoading = true
  requestAPI
    .post(`${API_ROUTES_STAFF.FETCH_DATA_PLAN_DATE}/${_detail.value.id}/add`, {
      ...formData,
      startDate: Date.parse(formData.startDate),
    })
    .then(({ data: response }) => {
      message.success(response.message)
      modalAddOrUpdate.isShow = false
      fetchDataList()
    })
    .catch((error) => {
      message.error(error?.response?.data?.message || 'Không thể thêm mới mục này')
    })
    .finally(() => {
      modalAddOrUpdate.isLoading = false
    })
}

const fetchUpdateItem = () => {
  modalAddOrUpdate.isLoading = true
  requestAPI
    .put(`${API_ROUTES_STAFF.FETCH_DATA_PLAN_DATE}/${_detail.value.id}/update`, {
      ...formData,
      startDate: Date.parse(formData.startDate),
    })
    .then(({ data: response }) => {
      message.success(response.message)
      modalAddOrUpdate.isShow = false
      fetchDataList()
    })
    .catch((error) => {
      message.error(error?.response?.data?.message || 'Không thể cập nhật mục này')
    })
    .finally(() => {
      modalAddOrUpdate.isLoading = false
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

const handleShowAdd = () => {
  if (formRefAddOrUpdate.value) {
    formRefAddOrUpdate.value.clearValidate()
  }
  modalAddOrUpdate.isShow = true
  modalAddOrUpdate.isLoading = false
  modalAddOrUpdate.title = h('span', [
    h(PlusOutlined, { class: 'me-2 text-primary' }),
    'Thêm kế hoạch mới',
  ])
  modalAddOrUpdate.okText = 'Thêm ngay'
  modalAddOrUpdate.onOk = () => handleSubmitAdd()

  formData.id = null
  formData.startDate = dayjs()
  formData.shift = []
  formData.link = null
  formData.type = null
  formData.room = null
  formData.requiredLocation = STATUS_TYPE.ENABLE
  formData.requiredIp = STATUS_TYPE.ENABLE
  formData.lateArrival = DEFAULT_LATE_ARRIVAL
  formData.description = null
}

const handleShowUpdate = (item) => {
  if (formRefAddOrUpdate.value) {
    formRefAddOrUpdate.value.clearValidate()
  }
  modalAddOrUpdate.isShow = true
  modalAddOrUpdate.isLoading = false
  modalAddOrUpdate.title = h('span', [
    h(EditFilled, { class: 'me-2 text-primary' }),
    'Chỉnh sửa ca học',
  ])
  modalAddOrUpdate.okText = 'Lưu lại'
  modalAddOrUpdate.onOk = () => handleSubmitUpdate()

  formData.id = item.id
  formData.startDate = dayjs(item.startDate)
  formData.shift = item.shift.split(',').map((o) => Number(o))
  formData.link = item.link
  formData.room = item.room
  formData.type = String(item.type)
  formData.requiredLocation = item.requiredLocation
  formData.requiredIp = item.requiredIp
  formData.lateArrival = item.lateArrival
  formData.description = item.description
}

const handleSubmitAdd = async () => {
  try {
    await formRefAddOrUpdate.value.validate()
    Modal.confirm({
      title: `Xác nhận thêm mới`,
      type: 'info',
      content: `Bạn có chắc muốn thêm mới ca học này?`,
      okText: 'Tiếp tục',
      cancelText: 'Hủy bỏ',
      onOk() {
        fetchAddItem()
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
      content: `Bạn có chắc muốn lưu lại thay đổi?`,
      okText: 'Tiếp tục',
      cancelText: 'Hủy bỏ',
      onOk() {
        fetchUpdateItem()
      },
    })
  } catch (error) {}
}

const handleShowAlertDelete = (item) => {
  Modal.confirm({
    title: `Xoá ca học: ${dayOfWeek(item.startDate)} - ${formatDate(item.startDate)}`,
    type: 'error',
    content: `Bạn có chắc muốn xoá ca học này?`,
    okText: 'Tiếp tục',
    cancelText: 'Hủy bỏ',
    okButtonProps: {
      class: 'btn-danger',
    },
    cancelButtonProps: {
      class: 'btn-gray',
    },
    onOk() {
      fetchDeleteItem(item.id)
    },
  })
}

const handleShowAlertMultipleDelete = () => {
  Modal.confirm({
    title: `Xoá ca học đã chọn`,
    type: 'error',
    content: `Bạn có chắc muốn xoá ${selectedRowKeys.value.length} ca học đã chọn?`,
    okText: 'Tiếp tục',
    cancelText: 'Hủy bỏ',
    okButtonProps: {
      class: 'btn-danger',
    },
    cancelButtonProps: {
      class: 'btn-gray',
    },
    onOk() {
      fetchDeleteMultipleItem(selectedRowKeys.value)
    },
  })
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
    name: ROUTE_NAMES.MANAGEMENT_PLAN_DATE_ATTENDANCE,
    params: { id: id },
  })
}

const handleChangeShift = (newValues) => {
  const updated = new Set(newValues)

  const sorted = [...updated].sort((a, b) => a - b)

  for (let i = 0; i < sorted.length - 1; i++) {
    const start = sorted[i]
    const end = sorted[i + 1]

    if (end - start > 1) {
      for (let j = start + 1; j < end; j++) {
        updated.add(j)
      }
    }
  }

  formData.shift = Array.from(updated).sort((a, b) => a - b)
}

const selectedRowKeys = ref([])

const isDisabledSelectTable = (key) => {
  const record = lstData.value.find((item) => item.id === key)
  return record?.status === 'DA_DIEN_RA'
}

const rowSelection = computed(() => rowSelectTable(selectedRowKeys, isDisabledSelectTable))

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
      <a-form-item
        class="col-sm-4"
        label="Ngày học diễn ra"
        name="startDate"
        :rules="formRules.startDate"
      >
        <a-date-picker
          class="w-100"
          :placeholder="DEFAULT_DATE_FORMAT"
          v-model:value="formData.startDate"
          :format="DEFAULT_DATE_FORMAT"
          :disabledDate="disabledDate"
          :disabled="modalAddOrUpdate.isLoading"
        />
      </a-form-item>
      <a-form-item class="col-sm-8" label="Phòng học">
        <a-input
          class="w-100"
          v-model:value="formData.room"
          placeholder="Địa điểm học chi tiết"
          :disabled="modalAddOrUpdate.isLoading || formData.type == '1'"
          allowClear
        />
      </a-form-item>
      <a-form-item class="col-sm-12" label="Ca học" name="shift" :rules="formRules.shift">
        <a-select
          class="w-100"
          v-model:value="formData.shift"
          :disabled="modalAddOrUpdate.isLoading"
          @change="handleChangeShift"
          mode="multiple"
          allow-clear
        >
          <a-select-option v-for="o in lstShift" :key="o.id" :value="o.shift">
            {{ SHIFT[o.shift] }}
          </a-select-option>
        </a-select>
      </a-form-item>

      <a-form-item class="col-sm-5" label="Hình thức học" name="type" :rules="formRules.type">
        <a-select
          v-model:value="formData.type"
          class="w-100"
          :dropdownMatchSelectWidth="false"
          placeholder="-- Hình thức học --"
          allowClear
        >
          <a-select-option v-for="(name, id) in TYPE_SHIFT" :key="id" :value="id">
            {{ name }}
          </a-select-option>
        </a-select>
      </a-form-item>

      <a-form-item
        class="col-sm-7"
        label="Điểm danh muộn tối đa (phút)"
        name="lateArrival"
        :rules="formRules.lateArrival"
      >
        <a-input-number
          class="w-100"
          v-model:value="formData.lateArrival"
          :min="0"
          :max="DEFAULT_MAX_LATE_ARRIVAL"
          :step="1"
          :disabled="modalAddOrUpdate.isLoading"
          allowClear
        />
      </a-form-item>

      <a-form-item class="col-sm-12" label="Nội dung buổi học" name="description">
        <a-textarea
          :rows="4"
          class="w-100"
          v-model:value="formData.description"
          :disabled="modalAddOrUpdate.isLoading"
          allowClear
        />
      </a-form-item>
      <a-form-item class="col-sm-12" label="Link học online" name="link">
        <a-input
          class="w-100"
          v-model:value="formData.link"
          placeholder="https://"
          :disabled="modalAddOrUpdate.isLoading"
          allowClear
        />
      </a-form-item>
      <a-form-item class="col-sm-12" label="Điều kiện điểm danh">
        <div class="mt-2">
          <a-switch
            class="me-2"
            :checked="formData.requiredIp === STATUS_TYPE.ENABLE"
            @change="
              formData.requiredIp =
                formData.requiredIp === STATUS_TYPE.ENABLE
                  ? STATUS_TYPE.DISABLE
                  : STATUS_TYPE.ENABLE
            "
          />
          <span :class="{ disabled: formData.requiredIp !== STATUS_TYPE.ENABLE }"
            >Phải kết nối mạng trường</span
          >
        </div>
        <div class="mt-3">
          <a-switch
            class="me-2"
            :checked="formData.requiredLocation === STATUS_TYPE.ENABLE"
            @change="
              formData.requiredLocation =
                formData.requiredLocation === STATUS_TYPE.ENABLE
                  ? STATUS_TYPE.DISABLE
                  : STATUS_TYPE.ENABLE
            "
          />
          <span :class="{ disabled: formData.requiredLocation !== STATUS_TYPE.ENABLE }"
            >Phải ở trong địa điểm cơ sở</span
          >
        </div>
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
            <div class="col-xxl-4 col-lg-8 col-md-8 col-sm-12">
              <div class="label-title">Từ khoá:</div>
              <a-input
                v-model:value="dataFilter.keyword"
                placeholder="Tìm theo mô tả..."
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
          <template #title>
            <UnorderedListOutlined /> Danh sách ca học
            {{ `(${formatDate(_detail?.fromDate)} - ${formatDate(_detail?.toDate)})` }}
          </template>
          <div class="d-flex justify-content-end mb-3 flex-wrap gap-3">
            <a-button
              v-show="selectedRowKeys.length > 0"
              class="btn-outline-danger"
              @click="handleShowAlertMultipleDelete"
              ><DeleteFilled /> Xoá mục đã chọn</a-button
            >
            <ExcelUploadButton v-bind="configImportExcel" />
            <a-button type="primary" @click="handleShowAdd"> <PlusOutlined /> Thêm mới </a-button>
          </div>

          <div>
            <a-table
              rowKey="id"
              class="nowrap"
              :dataSource="lstData"
              :columns="columns"
              :loading="isLoading"
              :pagination="pagination"
              :scroll="{ y: 500, x: 'auto' }"
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
                  {{ `${record.lateArrival} phút` }}
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
                  <template v-if="record.status !== 'DA_DIEN_RA'">
                    <a-tooltip title="Chỉnh sửa phân công">
                      <a-button class="btn-outline-info border-0" @click="handleShowUpdate(record)">
                        <EditFilled />
                      </a-button>
                    </a-tooltip>
                    <a-tooltip title="Xoá kế hoạch chi tiết">
                      <a-button
                        class="btn-outline-danger border-0 ms-2"
                        @click="handleShowAlertDelete(record)"
                      >
                        <DeleteFilled />
                      </a-button>
                    </a-tooltip>
                  </template>
                  <a-tooltip title="Chi tiết điểm danh" v-else>
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
