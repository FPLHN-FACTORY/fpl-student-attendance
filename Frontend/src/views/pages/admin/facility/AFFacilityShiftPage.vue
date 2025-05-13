<script setup>
import { ref, onMounted, watch, reactive, h } from 'vue'
import {
  PlusOutlined,
  FilterFilled,
  UnorderedListOutlined,
  EditFilled,
  DeleteFilled,
} from '@ant-design/icons-vue'
import { message, Modal } from 'ant-design-vue'
import requestAPI from '@/services/requestApiService'
import { DEFAULT_PAGINATION, SHIFT, STATUS_FACILITY_IP } from '@/constants'
import { API_ROUTES_ADMIN } from '@/constants/adminConstant'
import useBreadcrumbStore from '@/stores/useBreadCrumbStore'
import { GLOBAL_ROUTE_NAMES } from '@/constants/routesConstant'
import { ROUTE_NAMES } from '@/router/adminRoute'
import useLoadingStore from '@/stores/useLoadingStore'
import { useRoute, useRouter } from 'vue-router'
import dayjs from 'dayjs'
import { autoAddColumnWidth, debounce } from '@/utils/utils'

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

const columns = ref(
  autoAddColumnWidth([
    { title: '#', dataIndex: 'orderNumber', key: 'orderNumber' },
    { title: 'Ca học', dataIndex: 'shift', key: 'shift' },
    {
      title: 'Thời gian bắt đầu',
      dataIndex: 'startTime',
      key: 'startTime',
      align: 'center',
    },
    {
      title: 'Thời gian kết thúc',
      dataIndex: 'endTime',
      key: 'endTime',
      align: 'center',
    },
    { title: 'Trạng thái', dataIndex: 'status', key: 'status' },
    { title: '', key: 'actions' },
  ]),
)

const breadcrumb = ref([
  {
    name: GLOBAL_ROUTE_NAMES.STAFF_PAGE,
    breadcrumbName: 'Ban đào tạo',
  },
  {
    name: ROUTE_NAMES.MANAGEMENT_FACILITY,
    breadcrumbName: 'Quản lý cơ sở',
  },
])

const pagination = ref({ ...DEFAULT_PAGINATION })

const dataFilter = reactive({
  shift: null,
  status: null,
})

const formRefAddOrUpdate = ref(null)

const formData = reactive({
  id: null,
  idFacility: null,
  shift: null,
  timeRange: [],
})

const formRules = reactive({
  shift: [{ required: true, message: 'Vui lòng chọn 1 ca học!' }],
  timeRange: [{ required: true, message: 'Vui lòng chọn thời gian ca học!' }],
})

const fetchDataDetail = () => {
  loadingStore.show()
  requestAPI
    .get(`${API_ROUTES_ADMIN.FETCH_DATA_FACILITY}/${route.params.id}`)
    .then(({ data: response }) => {
      _detail.value = response.data
      formData.idFacility = _detail.value.id
      breadcrumbStore.push({
        name: ROUTE_NAMES.MANAGEMENT_FACILITY,
        params: { id: _detail.value.id },
        breadcrumbName: _detail.value.facilityName,
      })
      breadcrumbStore.push({
        name: ROUTE_NAMES.MANAGEMENT_FACILITY_SHIFT,
        breadcrumbName: 'Quản lý ca học',
      })
      fetchDataList()
    })
    .catch((error) => {
      message.error(error?.response?.data?.message || 'Không thể tải thông tin cơ sở')
      router.push({ name: ROUTE_NAMES.MANAGEMENT_FACILITY })
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
    .get(`${API_ROUTES_ADMIN.FETCH_DATA_FACILITY}/${_detail.value.id}/list-shift`, {
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

const fetchDeleteItem = (id) => {
  loadingStore.show()

  requestAPI
    .delete(`${API_ROUTES_ADMIN.FETCH_DATA_FACILITY}/${id}/delete-shift`)
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
    .post(`${API_ROUTES_ADMIN.FETCH_DATA_FACILITY}/${_detail.value.id}/add-shift`, {
      idFacility: formData.idFacility,
      shift: formData.shift,
      fromHour: formData.timeRange[0]?.format('HH'),
      fromMinute: formData.timeRange[0]?.format('mm'),
      toHour: formData.timeRange[1]?.format('HH'),
      toMinute: formData.timeRange[1]?.format('mm'),
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
    .put(`${API_ROUTES_ADMIN.FETCH_DATA_FACILITY}/${_detail.value.id}/update-shift`, {
      id: formData.id,
      idFacility: formData.idFacility,
      shift: formData.shift,
      fromHour: formData.timeRange[0]?.format('HH'),
      fromMinute: formData.timeRange[0]?.format('mm'),
      toHour: formData.timeRange[1]?.format('HH'),
      toMinute: formData.timeRange[1]?.format('mm'),
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

const fetchSubmitChangeStatus = (id) => {
  requestAPI
    .put(`${API_ROUTES_ADMIN.FETCH_DATA_FACILITY}/${id}/change-status-shift`)
    .then(({ data: response }) => {
      message.success(response.message)
      fetchDataList()
    })
    .catch((error) => {
      message.error(error?.response?.data?.message || 'Không thể thay đổi trạng thái ca học')
    })
}

const handleClearFilter = () => {
  Object.assign(dataFilter, {
    shift: null,
    status: null,
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
    'Thêm ca học mới',
  ])
  modalAddOrUpdate.okText = 'Thêm ngay'
  modalAddOrUpdate.onOk = () => handleSubmitAdd()

  formData.id = null
  formData.shift = Object.keys(SHIFT)[0]
  formData.fromHour = null
  formData.fromMinute = null
  formData.toHour = null
  formData.toMinute = null
  formData.timeRange = []
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
  formData.shift = item.shift
  formData.fromHour = item.fromHour
  formData.fromMinute = item.fromMinute
  formData.toHour = item.toHour
  formData.toMinute = item.toMinute
  formData.timeRange = [
    dayjs(item.fromHour + ':' + item.fromMinute, 'HH:mm'),
    dayjs(item.toHour + ':' + item.toMinute, 'HH:mm'),
  ]
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
      content: `Mọi thay đổi chỉ áp dụng cho kế hoạch tạo mới. Bạn có chắc muốn lưu lại thay đổi?`,
      okText: 'Tiếp tục',
      cancelText: 'Hủy bỏ',
      onOk() {
        fetchUpdateItem()
      },
    })
  } catch (error) {}
}

const handleChangeStatus = (id) => {
  Modal.confirm({
    title: `Xác nhận thay đổi trạng thái`,
    type: 'info',
    content: `Bạn có chắc muốn thay đổi trạng thái ca học này?`,
    okText: 'Tiếp tục',
    cancelText: 'Hủy bỏ',
    onOk() {
      fetchSubmitChangeStatus(id)
    },
  })
}

const handleShowAlertDelete = (item) => {
  Modal.confirm({
    title: `Xoá ca học: ${item.shift}`,
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

onMounted(() => {
  breadcrumbStore.setRoutes(breadcrumb.value)
  fetchDataDetail()
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
        class="col-sm-8"
        label="Thời gian ca học"
        name="timeRange"
        :rules="formRules.timeRange"
      >
        <a-range-picker
          class="w-100"
          v-model:value="formData.timeRange"
          :show-time="{ format: 'HH:mm' }"
          format="HH:mm"
          picker="time"
          :placeholder="['Thời gian bắt đầu', 'Thời gian kết thúc']"
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
            <div class="col-lg-6 col-md-6 col-sm-6">
              <div class="label-title">Ca học:</div>
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
            <div class="col-lg-6 col-md-6 col-sm-6">
              <div class="label-title">Trạng thái:</div>
              <a-select
                v-model:value="dataFilter.status"
                class="w-100"
                :dropdownMatchSelectWidth="false"
                placeholder="-- Tất cả trạng thái --"
                allowClear
              >
                <a-select-option :value="null">-- Tất cả trạng thái --</a-select-option>
                <a-select-option v-for="(name, id) in STATUS_FACILITY_IP" :key="id" :value="id">
                  {{ name }}
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
          <template #title> <UnorderedListOutlined /> Danh sách ca học </template>
          <div class="d-flex justify-content-end mb-3 flex-wrap gap-3">
            <a-button type="primary" @click="handleShowAdd">
              <PlusOutlined /> Thêm ca học mới
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
              :scroll="{ x: 'auto' }"
              @change="handleTableChange"
            >
              <template #bodyCell="{ column, record }">
                <template v-if="column.dataIndex === 'shift'">
                  <a-tag color="purple"> Ca {{ record.shift }} </a-tag>
                </template>
                <template v-if="column.dataIndex === 'status'">
                  <a-switch
                    class="me-2"
                    :checked="record.status === 1"
                    @change="handleChangeStatus(record.id)"
                  />
                  <a-tag :color="record.status === 1 ? 'green' : 'red'">{{
                    record.status === 1 ? 'Đang áp dụng' : 'Không áp dụng'
                  }}</a-tag>
                </template>
                <template v-if="column.key === 'actions'">
                  <a-tooltip title="Chỉnh sửa ca học">
                    <a-button class="btn-outline-info border-0" @click="handleShowUpdate(record)">
                      <EditFilled />
                    </a-button>
                  </a-tooltip>
                  <a-tooltip title="Xoá ca học">
                    <a-button
                      class="btn-outline-danger border-0 ms-2"
                      @click="handleShowAlertDelete(record)"
                    >
                      <DeleteFilled />
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
