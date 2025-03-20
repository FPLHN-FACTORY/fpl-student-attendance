<script setup>
import { ref, onMounted, watch, reactive, h } from 'vue'
import {
  PlusOutlined,
  FilterFilled,
  UnorderedListOutlined,
  SearchOutlined,
  EditFilled,
  DeleteFilled,
} from '@ant-design/icons-vue'
import { message, Modal } from 'ant-design-vue'
import requestAPI from '@/services/requestApiService'
import { DEFAULT_PAGINATION, PAGINATION_SIZE } from '@/constants/paginationConstant'
import { API_ROUTES_STAFF } from '@/constants/staffConstant'
import useBreadcrumbStore from '@/stores/useBreadCrumbStore'
import { GLOBAL_ROUTE_NAMES } from '@/constants/routesConstant'
import { ROUTE_NAMES } from '@/router/staffRoute'
import useLoadingStore from '@/stores/useLoadingStore'
import { useRoute, useRouter } from 'vue-router'
import { SHIFT } from '@/constants/shiftConstant'
import { DEFAULT_DATE_FORMAT, DEFAULT_LATE_ARRIVAL, DEFAULT_MAX_LATE_ARRIVAL } from '@/constants'
import { STATUS_PLAN_DATE_DETAIL } from '@/constants/statusConstant'
import { dayOfWeek, formatDate } from '@/utils/utils'
import dayjs from 'dayjs'

const route = useRoute()
const router = useRouter()

const breadcrumbStore = useBreadcrumbStore()
const loadingStore = useLoadingStore()

const isLoading = ref(false)

const modalAddOrUpdate = reactive({
  isShow: false,
  isLoading: false,
  title: null,
  cancelText: 'Hủy bỏ',
  okText: 'Xác nhận',
  onOk: null,
})

const _detail = ref(null)
const lstData = ref([])

const columns = ref([
  { title: 'Buổi', dataIndex: 'orderNumber', key: 'orderNumber', width: 50 },
  { title: 'Ngày học', dataIndex: 'startDate', key: 'startDate' },
  { title: 'Ca học', dataIndex: 'shift', key: 'shift' },
  { title: 'Nội dung', dataIndex: 'description', key: 'description', width: 300 },
  { title: 'Điểm danh trễ', dataIndex: 'lateArrival', key: 'lateArrival', width: 130 },
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
    breadcrumbName: 'Phân công kế hoạch',
  },
])

const pagination = ref({ ...DEFAULT_PAGINATION })

const dataFilter = reactive({
  keyword: null,
  shift: null,
  startDate: null,
})

const formRefAddOrUpdate = ref(null)

const formData = reactive({
  id: null,
  description: null,
  shift: Object.keys(SHIFT)[0],
  startDate: null,
  lateArrival: DEFAULT_LATE_ARRIVAL,
})

const formRules = reactive({
  startDate: [{ required: true, message: 'Vui lòng chọn ngày học diễn ra!' }],
  shift: [{ required: true, message: 'Vui lòng chọn ca học!' }],
  lateArrival: [{ required: true, message: 'Vui lòng nhập thời gian điểm danh muộn tối đa!' }],
  description: [{ required: true, message: 'Vui lòng nhập nội dung buổi học!' }],
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
      breadcrumbStore.push({
        breadcrumbName: _detail.value.factoryName + ' - ' + _detail.value.projectName,
      })
      fetchDataList()
    })
    .catch((error) => {
      message.error(error?.response?.data?.message || 'Không thể tải thông tin kế hoạch')
      router.push({ name: ROUTE_NAMES.MANAGEMENT_PLAN })
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
    shift: null,
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
  formData.shift = Object.keys(SHIFT)[0]
  formData.lateArrival = DEFAULT_LATE_ARRIVAL
  formData.description = null
}

const handleShowUpdate = (item) => {
  modalAddOrUpdate.isShow = true
  modalAddOrUpdate.isLoading = false
  modalAddOrUpdate.title = h('span', [
    h(EditFilled, { class: 'me-2 text-primary' }),
    'Chỉnh sửa kế hoạch',
  ])
  modalAddOrUpdate.okText = 'Lưu lại'
  modalAddOrUpdate.onOk = () => handleSubmitUpdate()

  formData.id = item.id
  formData.startDate = dayjs(item.startDate)
  formData.shift = item.shift
  formData.lateArrival = item.lateArrival
  formData.description = item.description
}

const handleSubmitAdd = async () => {
  try {
    await formRefAddOrUpdate.value.validate()
    Modal.confirm({
      title: `Xác nhận thêm mới`,
      type: 'info',
      content: `Bạn có chắc muốn thêm mới kế hoạch này?`,
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
    title: `Xoá kế hoạch: ${dayOfWeek(item.startDate)} - ${formatDate(item.startDate)}`,
    type: 'error',
    content: `Bạn có chắc muốn xoá kế hoạch chi tiết này?`,
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

onMounted(() => {
  breadcrumbStore.setRoutes(breadcrumb.value)
  fetchDataDetail()
})

watch(
  dataFilter,
  () => {
    handleSubmitFilter()
  },
  { deep: true }
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
        class="col-sm-8"
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
      <a-form-item class="col-sm-4" label="Ca học" name="shift" :rules="formRules.shift">
        <a-select
          class="w-100"
          v-model:value="formData.shift"
          :disabled="modalAddOrUpdate.isLoading"
        >
          <a-select-option v-for="(name, id) in SHIFT" :key="id" :value="id">
            {{ name }}
          </a-select-option>
        </a-select>
      </a-form-item>

      <a-form-item
        class="col-sm-12"
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

      <a-form-item
        class="col-sm-12"
        label="Nội dung buổi học"
        name="description"
        :rules="formRules.description"
      >
        <a-textarea
          :rows="4"
          class="w-100"
          v-model:value="formData.description"
          :disabled="modalAddOrUpdate.isLoading"
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
            <div class="col-lg-6 col-md-12 col-sm-8">
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
            <div class="col-lg-2 col-md-4 col-sm-4">
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
            <div class="col-lg-2 col-md-4 col-sm-6">
              <a-select
                v-model:value="dataFilter.shift"
                class="w-100"
                :dropdownMatchSelectWidth="false"
                placeholder="-- Tất cả ca học --"
                allowClear
              >
                <a-select-option :value="null">-- Tất cả ca học --</a-select-option>
                <a-select-option v-for="(name, id) in SHIFT" :key="id" :value="id">
                  {{ name }}
                </a-select-option>
              </a-select>
            </div>
            <div class="col-lg-2 col-md-4 col-sm-6">
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
            <UnorderedListOutlined /> Danh sách kế hoạch
            {{ `(${formatDate(_detail?.fromDate)} - ${formatDate(_detail?.toDate)})` }}
          </template>
          <div class="d-flex justify-content-end mb-3">
            <a-button type="primary" @click="handleShowAdd">
              <PlusOutlined /> Thêm chi tiết kế hoạch
            </a-button>
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
              @change="handleTableChange"
            >
              <template #bodyCell="{ column, record }">
                <template v-if="column.dataIndex === 'description' && record.description">
                  <a-typography-link @click="handleShowDescription(record.description)"
                    >Chi tiết</a-typography-link
                  >
                </template>
                <template v-if="column.dataIndex === 'lateArrival'">
                  {{ `${record.lateArrival} phút` }}
                </template>
                <template v-if="column.dataIndex === 'startDate'">
                  {{
                    `${dayOfWeek(record.startDate)} - ${formatDate(
                      record.startDate,
                      DEFAULT_DATE_FORMAT + ' HH:mm'
                    )}`
                  }}
                </template>
                <template v-if="column.dataIndex === 'shift'">
                  <a-tag color="purple">
                    {{ SHIFT[record.shift] }}
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
                    <a-tooltip title="Chỉnh sửa kế hoạch chi tiết">
                      <a-button class="btn-outline-info" @click="handleShowUpdate(record)">
                        <EditFilled />
                      </a-button>
                    </a-tooltip>
                    <a-tooltip title="Xoá kế hoạch chi tiết">
                      <a-button
                        class="btn-outline-danger ms-2"
                        @click="handleShowAlertDelete(record)"
                      >
                        <DeleteFilled />
                      </a-button>
                    </a-tooltip>
                  </template>
                  <a-tag v-else>Không thể chỉnh sửa mục này</a-tag>
                </template>
              </template>
            </a-table>
          </div>
        </a-card>
      </div>
    </div>
  </div>
</template>
