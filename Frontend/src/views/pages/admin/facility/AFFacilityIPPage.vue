<script setup>
import { ref, onMounted, watch, reactive, h, computed } from 'vue'
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
import { DEFAULT_PAGINATION, STATUS_FACILITY_IP, TYPE_FACILITY_IP } from '@/constants'
import { API_ROUTES_ADMIN } from '@/constants/adminConstant'
import useBreadcrumbStore from '@/stores/useBreadCrumbStore'
import { GLOBAL_ROUTE_NAMES } from '@/constants/routesConstant'
import { ROUTE_NAMES } from '@/router/adminRoute'
import useLoadingStore from '@/stores/useLoadingStore'
import { useRoute, useRouter } from 'vue-router'
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
    { title: '#', key: 'rowNumber' },
    { title: 'Tên IP', dataIndex: 'name', key: 'name' },
    { title: 'Địa chỉ IP', dataIndex: 'ipAddress', key: 'ipAddress' },
    { title: 'Mô tả', dataIndex: 'description', key: 'description' },
    { title: 'Trạng thái', dataIndex: 'status', key: 'status' },
    { title: 'Chức năng', key: 'actions' },
  ]),
)

const breadcrumb = ref([
  {
    name: GLOBAL_ROUTE_NAMES.STAFF_PAGE,
    breadcrumbName: 'Admin',
  },
  {
    name: ROUTE_NAMES.MANAGEMENT_FACILITY,
    breadcrumbName: 'Quản lý cơ sở',
  },
])

const pagination = ref({ ...DEFAULT_PAGINATION })

const dataFilter = reactive({
  keyword: null,
  status: null,
  type: null,
})

const formRefAddOrUpdate = ref(null)

const formData = reactive({
  id: null,
  idFacility: null,
  ip: null,
  type: null,
})

const formRules = reactive({
  ip: [{ required: true, message: 'Vui lòng nhập ip hoặc dải ip!' }],
  type: [{ required: true, message: 'Vui lòng chọn kiểu ip!' }],
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
        name: ROUTE_NAMES.MANAGEMENT_FACILITY_IP,
        breadcrumbName: 'Quản lý IP',
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
    .get(`${API_ROUTES_ADMIN.FETCH_DATA_FACILITY}/${_detail.value.id}/list-ip`, {
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
    .delete(`${API_ROUTES_ADMIN.FETCH_DATA_FACILITY}/${id}/delete-ip`)
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
    .post(`${API_ROUTES_ADMIN.FETCH_DATA_FACILITY}/${_detail.value.id}/add-ip`, {
      ...formData,
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
    .put(`${API_ROUTES_ADMIN.FETCH_DATA_FACILITY}/${_detail.value.id}/update-ip`, formData)
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
    .put(`${API_ROUTES_ADMIN.FETCH_DATA_FACILITY}/${id}/change-status-ip`)
    .then(({ data: response }) => {
      message.success(response.message)
      fetchDataList()
    })
    .catch((error) => {
      message.error(error?.response?.data?.message || 'Không thể thay đổi trạng thái IP')
    })
}

const handleClearFilter = () => {
  Object.assign(dataFilter, {
    keyword: null,
    status: null,
    type: null,
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
    'Thêm IP mới',
  ])
  modalAddOrUpdate.okText = 'Thêm ngay'
  modalAddOrUpdate.onOk = () => handleSubmitAdd()

  formData.id = null
  formData.ip = null
  formData.type = null
}

const handleShowUpdate = (item) => {
  if (formRefAddOrUpdate.value) {
    formRefAddOrUpdate.value.clearValidate()
  }
  modalAddOrUpdate.isShow = true
  modalAddOrUpdate.isLoading = false
  modalAddOrUpdate.title = h('span', [
    h(EditFilled, { class: 'me-2 text-primary' }),
    'Chỉnh sửa IP',
  ])
  modalAddOrUpdate.okText = 'Lưu lại'
  modalAddOrUpdate.onOk = () => handleSubmitUpdate()

  formData.id = item.id
  formData.ip = item.ip
  formData.type = String(item.type)
}

const handleSubmitAdd = async () => {
  try {
    await formRefAddOrUpdate.value.validate()
    Modal.confirm({
      title: `Xác nhận thêm mới`,
      type: 'info',
      content: `Bạn có chắc muốn thêm mới IP này?`,
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

const handleChangeStatus = (id) => {
  Modal.confirm({
    title: `Xác nhận thay đổi trạng thái`,
    type: 'info',
    content: `Bạn có chắc muốn thay đổi trạng thái IP này?`,
    okText: 'Tiếp tục',
    cancelText: 'Hủy bỏ',
    onOk() {
      fetchSubmitChangeStatus(id)
    },
  })
}

const handleShowAlertDelete = (item) => {
  Modal.confirm({
    title: `Xoá IP: ${item.ip}`,
    type: 'error',
    content: `Bạn có chắc muốn xoá IP này?`,
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
      <a-form-item class="col-sm-4" label="Kiểu IP" name="type" :rules="formRules.type">
        <a-select
          class="w-100"
          v-model:value="formData.type"
          :disabled="modalAddOrUpdate.isLoading"
        >
          <a-select-option v-for="(name, id) in TYPE_FACILITY_IP" :key="id" :value="id">
            {{ name }}
          </a-select-option>
        </a-select>
      </a-form-item>

      <a-form-item
        class="col-sm-8"
        :label="formData.type === Object.keys(TYPE_FACILITY_IP)[2] ? 'Giá trị' : 'IP/Dải IP'"
        name="ip"
        :rules="formRules.ip"
      >
        <a-input
          class="w-100"
          v-model:value="formData.ip"
          :disabled="modalAddOrUpdate.isLoading"
          allowClear
          @keyup.enter="modalAddOrUpdate.onOk"
        />
      </a-form-item>
    </a-form>
  </a-modal>

  <div class="container-fluid">
    <div class="row g-3">
      <div class="col-12">
        <a-card :bordered="false" class="cart no-body-padding">
          <a-collapse ghost>
            <a-collapse-panel>
              <template #header><FilterFilled /> Bộ lọc</template>
              <div class="row g-3">
                <div class="col-lg-6 col-md-12 col-sm-12">
                  <div class="label-title">Từ khoá:</div>
                  <a-input
                    v-model:value="dataFilter.keyword"
                    placeholder="Tìm theo IP..."
                    allowClear
                  >
                    <template #prefix>
                      <SearchOutlined />
                    </template>
                  </a-input>
                </div>
                <div class="col-lg-3 col-md-6 col-sm-6">
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
                <div class="col-lg-3 col-md-6 col-sm-6">
                  <div class="label-title">Kiểu IP:</div>
                  <a-select
                    v-model:value="dataFilter.type"
                    class="w-100"
                    :dropdownMatchSelectWidth="false"
                    placeholder="-- Tất cả kiểu IP --"
                    allowClear
                  >
                    <a-select-option :value="null">-- Tất cả kiểu IP --</a-select-option>
                    <a-select-option v-for="(name, id) in TYPE_FACILITY_IP" :key="id" :value="id">
                      {{ name }}
                    </a-select-option>
                  </a-select>
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
          <template #title> <UnorderedListOutlined /> Danh sách IP cho phép </template>
          <div class="d-flex justify-content-end flex-wrap gap-3 mb-2">
            <a-button type="primary" @click="handleShowAdd">
              <PlusOutlined /> Thêm IP mới
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
              <template #bodyCell="{ column, record, index }">
                <template v-if="column.key === 'rowNumber'">
                  {{ (pagination.value.current - 1) * pagination.value.pageSize + index + 1 }}
                </template>
                <template v-else-if="column.dataIndex === 'type'">
                  <a-tag color="purple">
                    {{ TYPE_FACILITY_IP[record.type] }}
                  </a-tag>
                </template>
                <template v-else-if="column.dataIndex === 'status'">
                  <a-switch
                    class="me-2"
                    :checked="record.status === 1"
                    @change="handleChangeStatus(record.id)"
                  />
                  <a-tag :color="record.status === 1 ? 'green' : 'red'">{{
                    record.status === 1 ? 'Đang áp dụng' : 'Không áp dụng'
                  }}</a-tag>
                </template>
                <template v-else-if="column.key === 'actions'">
                  <a-tooltip title="Chỉnh sửa IP">
                    <a-button class="btn-outline-info border-0" @click="handleShowUpdate(record)">
                      <EditFilled />
                    </a-button>
                  </a-tooltip>
                  <a-tooltip title="Xoá IP">
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
