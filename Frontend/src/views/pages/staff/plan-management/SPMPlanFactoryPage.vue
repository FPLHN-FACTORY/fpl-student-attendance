<script setup>
import { ref, onMounted, watch, reactive } from 'vue'
import {
  PlusOutlined,
  FilterFilled,
  UnorderedListOutlined,
  SearchOutlined,
  EyeFilled,
  DeleteFilled,
} from '@ant-design/icons-vue'
import { message, Modal } from 'ant-design-vue'
import requestAPI from '@/services/requestApiService'
import { DEFAULT_PAGINATION, STATUS_TYPE, TYPE_SHIFT } from '@/constants'
import { API_ROUTES_STAFF } from '@/constants/staffConstant'
import { useRoute, useRouter } from 'vue-router'
import { ROUTE_NAMES } from '@/router/staffRoute'
import { GLOBAL_ROUTE_NAMES } from '@/constants/routesConstant'
import useBreadcrumbStore from '@/stores/useBreadCrumbStore'
import {
  DAY_OF_WEEK,
  DEFAULT_DATE_FORMAT,
  DEFAULT_LATE_ARRIVAL,
  DEFAULT_MAX_LATE_ARRIVAL,
  SHIFT,
} from '@/constants'
import { autoAddColumnWidth, debounce, formatDate } from '@/utils/utils'
import useLoadingStore from '@/stores/useLoadingStore'

const router = useRouter()
const route = useRoute()

const breadcrumbStore = useBreadcrumbStore()
const loadingStore = useLoadingStore()

const _detail = ref(null)

const isLoading = ref(false)
const modalAdd = reactive({
  isShow: false,
  isLoading: false,
  okText: 'Xác nhận',
  cancelText: 'Huỷ bỏ',
  width: 800,
  onOk: () => handleSubmitAdd(),
})

const lstData = ref([])
const lstDataAdd = ref([])
const lstShift = ref([])

const formRefAdd = ref(null)

const columns = ref(
  autoAddColumnWidth([
    { title: '#', dataIndex: 'orderNumber', key: 'orderNumber' },
    {
      title: 'Tên nhóm xưởng',
      dataIndex: 'factoryName',
      key: 'factoryName',
    },
    { title: 'Giảng viên', dataIndex: 'staffName', key: 'staffName' },
    { title: 'Thời gian thực tế', key: 'time' },
    { title: 'Số buổi', dataIndex: 'totalShift', key: 'totalShift' },
    {
      title: 'Số sinh viên',
      dataIndex: 'totalStudent',
      key: 'totalStudent',
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
  rangeDate: [],
})

const formDataAdd = reactive({
  idFactory: null,
  type: Object.keys(TYPE_SHIFT)[0],
  days: [],
  shift: [],
  link: null,
  room: null,
  requiredCheckin: STATUS_TYPE.ENABLE,
  requiredCheckout: STATUS_TYPE.ENABLE,
  requiredLocation: STATUS_TYPE.ENABLE,
  requiredIp: STATUS_TYPE.ENABLE,
  lateArrival: DEFAULT_LATE_ARRIVAL,
})

const formRules = reactive({
  idFactory: [{ required: true, message: 'Vui lòng chọn 1 nhóm xưởng - dự án!' }],
  days: [{ required: true, message: 'Vui lòng chọn ít nhất 1 ngày trong tuần!' }],
  shift: [{ required: true, message: 'Vui lòng chọn 1 ca học!' }],
  type: [{ required: true, message: 'Vui lòng chọn 1 hình thức!' }],
  lateArrival: [{ required: true, message: 'Vui lòng nhập mục này!' }],
})

const fetchDataDetail = () => {
  loadingStore.show()
  requestAPI
    .get(`${API_ROUTES_STAFF.FETCH_DATA_PLAN}/${route.params.id}`)
    .then(({ data: response }) => {
      _detail.value = response.data
      breadcrumbStore.push({
        breadcrumbName: _detail.value.planName,
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
    .get(`${API_ROUTES_STAFF.FETCH_DATA_PLAN_FACTORY}/${route.params?.id}`, {
      params: {
        page: pagination.value.current,
        size: pagination.value.pageSize,
        keyword: dataFilter.keyword,
        status: dataFilter.status,
        fromDate: dataFilter.rangeDate[0] ? Date.parse(dataFilter.rangeDate[0]) : null,
        toDate: dataFilter.rangeDate[1] ? Date.parse(dataFilter.rangeDate[1]) : null,
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

const fetchDataFactoryList = () => {
  modalAdd.isLoading = true
  requestAPI
    .get(`${API_ROUTES_STAFF.FETCH_DATA_PLAN_FACTORY}/${_detail.value.id}/list/factory`)
    .then(({ data: response }) => {
      lstDataAdd.value = response.data
    })
    .catch((error) => {
      message.error(error?.response?.data?.message || 'Không thể tải danh sách dữ liệu nhóm xưởng')
    })
    .finally(() => {
      modalAdd.isLoading = false
    })
}

const fetchSubmitCreate = () => {
  modalAdd.isLoading = true
  requestAPI
    .post(`${API_ROUTES_STAFF.FETCH_DATA_PLAN_FACTORY}/${route.params?.id}`, formDataAdd)
    .then(({ data: response }) => {
      message.success(response.message)
      modalAdd.isShow = false
      fetchDataList()
    })
    .catch((error) => {
      message.error(error?.response?.data?.message || 'Không thể thêm nhóm xưởng vào kế hoạch')
    })
    .finally(() => {
      modalAdd.isLoading = false
    })
}

const fetchSubmitChangeStatus = (id) => {
  requestAPI
    .put(`${API_ROUTES_STAFF.FETCH_DATA_PLAN_FACTORY}/${id}/change-status`)
    .then(({ data: response }) => {
      message.success(response.message)
      fetchDataList()
    })
    .catch((error) => {
      message.error(
        error?.response?.data?.message || 'Không thể thay đổi trạng thái nhóm xưởng này',
      )
    })
}

const fetchSubmitDelete = (id) => {
  requestAPI
    .delete(`${API_ROUTES_STAFF.FETCH_DATA_PLAN_FACTORY}/${id}`)
    .then(({ data: response }) => {
      message.success(response.message)
      fetchDataList()
    })
    .catch((error) => {
      message.error(error?.response?.data?.message || 'Không thể xoá nhóm xưởng khỏi kế hoạch này')
    })
}

const handleClearFilter = () => {
  Object.assign(dataFilter, {
    keyword: null,
    status: null,
    rangeDate: [],
  })
  fetchDataList()
}

const handleSubmitFilter = () => {
  pagination.value.current = 1
  fetchDataList()
}

const handleSubmitAdd = async () => {
  try {
    await formRefAdd.value.validate()
    Modal.confirm({
      title: `Xác nhận phân công`,
      type: 'info',
      content: `Bạn có chắc muốn thêm nhóm xưởng vào kế hoạch này?`,
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

const handleShowDetail = (id) => {
  router.push({
    name: ROUTE_NAMES.MANAGEMENT_PLAN_DATE,
    params: { id: id },
  })
}

const handleShowModalAdd = () => {
  if (formRefAdd.value) {
    formRefAdd.value.clearValidate()
  }
  fetchDataFactoryList()

  modalAdd.isShow = true

  lstDataAdd.value = []

  formDataAdd.idFactory = null
  formDataAdd.type = Object.keys(TYPE_SHIFT)[0]
  formDataAdd.lateArrival = DEFAULT_LATE_ARRIVAL
  formDataAdd.shift = []
  formDataAdd.days = []
  formDataAdd.link = null
  formDataAdd.room = null
  formDataAdd.requiredLocation = STATUS_TYPE.ENABLE
  formDataAdd.requiredIp = STATUS_TYPE.ENABLE
  formDataAdd.requiredCheckin = STATUS_TYPE.ENABLE
  formDataAdd.requiredCheckout = STATUS_TYPE.ENABLE
}

const handleChangeStatus = (id) => {
  Modal.confirm({
    title: `Xác nhận thay đổi trạng thái`,
    type: 'info',
    content: `Bạn có chắc muốn thay đổi trạng thái kế hoạch nhóm xưởng này?`,
    okText: 'Tiếp tục',
    cancelText: 'Hủy bỏ',
    onOk() {
      fetchSubmitChangeStatus(id)
    },
  })
}

const handleDelete = (id) => {
  Modal.confirm({
    title: `Xác nhận xoá nhóm xưởng khỏi kế hoạch`,
    type: 'warning',
    content: `Mọi dữ liệu điểm danh sẽ bị xoá. Bạn vẫn muốn tiếp tục?`,
    okText: 'Tiếp tục',
    cancelText: 'Hủy bỏ',
    onOk() {
      fetchSubmitDelete(id)
    },
  })
}

onMounted(() => {
  breadcrumbStore.setRoutes(breadcrumb.value)
  fetchDataDetail()
  fetchDataList()
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
    v-model:open="modalAdd.isShow"
    v-bind="modalAdd"
    :okButtonProps="{ loading: modalAdd.isLoading }"
  >
    <template #title> <PlusOutlined class="text-primary" /> Phân công nhóm xưởng </template>
    <a-form
      ref="formRefAdd"
      class="row mt-3"
      layout="vertical"
      autocomplete="off"
      :model="formDataAdd"
    >
      <a-form-item
        class="col-12"
        :label="`Nhóm xưởng (${lstDataAdd.length})`"
        name="idFactory"
        :rules="formRules.idFactory"
      >
        <a-select
          v-model:value="formDataAdd.idFactory"
          placeholder="-- Chọn 1 nhóm xưởng --"
          class="w-100"
          :disabled="modalAdd.isLoading"
        >
          <a-select-option v-for="o in lstDataAdd" :key="o.id" :value="o.id">
            {{ `${o.factoryName} / (${o.staffName})` }}
          </a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item
        class="col-sm-12"
        label="Ngày học trong tuần"
        name="days"
        :rules="formRules.days"
      >
        <a-select
          v-model:value="formDataAdd.days"
          class="w-100"
          mode="multiple"
          allow-clear
          :disabled="modalAdd.isLoading"
        >
          <a-select-option v-for="(name, id) in DAY_OF_WEEK" :key="id" :value="id">
            {{ name }}
          </a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item class="col-sm-12" label="Ca học" name="shift" :rules="formRules.shift">
        <a-select
          class="w-100"
          v-model:value="formDataAdd.shift"
          :disabled="modalAdd.isLoading"
          mode="multiple"
          allow-clear
        >
          <a-select-option v-for="o in lstShift" :key="o.id" :value="o.shift">
            {{ SHIFT[o.shift] }}
          </a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item class="col-sm-4" label="Hình thức học" name="type" :rules="formRules.type">
        <a-select
          v-model:value="formDataAdd.type"
          class="w-100"
          :dropdownMatchSelectWidth="false"
          placeholder="-- Hình thức học --"
        >
          <a-select-option v-for="(name, id) in TYPE_SHIFT" :key="id" :value="id">
            {{ name }}
          </a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item
        class="col-sm-4"
        label="Điểm danh muộn tối đa (phút)"
        name="lateArrival"
        :rules="formRules.lateArrival"
      >
        <a-input-number
          class="w-100"
          v-model:value="formDataAdd.lateArrival"
          :min="0"
          :max="DEFAULT_MAX_LATE_ARRIVAL"
          :step="1"
          :disabled="modalAdd.isLoading"
          allowClear
        />
      </a-form-item>
      <a-form-item class="col-sm-4" label="Phòng học">
        <a-input
          class="w-100"
          v-model:value="formDataAdd.room"
          placeholder="Địa điểm học chi tiết"
          :disabled="modalAdd.isLoading || formDataAdd.type == '1'"
          allowClear
        />
      </a-form-item>
      <a-form-item class="col-sm-12" label="Link học online" name="link">
        <a-input
          class="w-100"
          v-model:value="formDataAdd.link"
          placeholder="https://"
          :disabled="modalAdd.isLoading"
          allowClear
        />
      </a-form-item>
      <a-form-item class="col-sm-12" label="Điều kiện điểm danh">
        <div class="row g-3">
          <div class="col-sm-6">
            <a-switch
              class="me-2"
              :checked="formDataAdd.requiredCheckin === STATUS_TYPE.ENABLE"
              @change="
                formDataAdd.requiredCheckin =
                  formDataAdd.requiredCheckin === STATUS_TYPE.ENABLE
                    ? STATUS_TYPE.DISABLE
                    : STATUS_TYPE.ENABLE
              "
            />
            <span :class="{ disabled: formDataAdd.requiredCheckin !== STATUS_TYPE.ENABLE }"
              >Yêu cầu checkin</span
            >
          </div>
          <div class="col-sm-6">
            <a-switch
              class="me-2"
              :checked="formDataAdd.requiredCheckout === STATUS_TYPE.ENABLE"
              @change="
                formDataAdd.requiredCheckout =
                  formDataAdd.requiredCheckout === STATUS_TYPE.ENABLE
                    ? STATUS_TYPE.DISABLE
                    : STATUS_TYPE.ENABLE
              "
            />
            <span :class="{ disabled: formDataAdd.requiredCheckout !== STATUS_TYPE.ENABLE }"
              >Yêu cầu checkout</span
            >
          </div>
          <div class="col-sm-6">
            <a-switch
              class="me-2"
              :checked="formDataAdd.requiredIp === STATUS_TYPE.ENABLE"
              @change="
                formDataAdd.requiredIp =
                  formDataAdd.requiredIp === STATUS_TYPE.ENABLE
                    ? STATUS_TYPE.DISABLE
                    : STATUS_TYPE.ENABLE
              "
            />
            <span :class="{ disabled: formDataAdd.requiredIp !== STATUS_TYPE.ENABLE }"
              >Phải kết nối mạng trường</span
            >
          </div>
          <div class="col-sm-6">
            <a-switch
              class="me-2"
              :checked="formDataAdd.requiredLocation === STATUS_TYPE.ENABLE"
              @change="
                formDataAdd.requiredLocation =
                  formDataAdd.requiredLocation === STATUS_TYPE.ENABLE
                    ? STATUS_TYPE.DISABLE
                    : STATUS_TYPE.ENABLE
              "
            />
            <span :class="{ disabled: formDataAdd.requiredLocation !== STATUS_TYPE.ENABLE }"
              >Phải ở trong địa điểm cơ sở</span
            >
          </div>
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
            <div class="col-md-4 col-sm-12">
              <div class="label-title">Từ khoá:</div>
              <a-input
                v-model:value="dataFilter.keyword"
                placeholder="Tìm theo tên nhóm xưởng, giảng viên..."
                allowClear
              >
                <template #prefix>
                  <SearchOutlined />
                </template>
              </a-input>
            </div>
            <div class="col-md-4 col-sm-6">
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
            <div class="col-md-4 col-sm-6">
              <div class="label-title">Thời gian diễn ra:</div>
              <a-range-picker
                class="w-100"
                :placeholder="['Ngày bắt đầu', 'Ngày kết thúc']"
                v-model:value="dataFilter.rangeDate"
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
          <template #title> <UnorderedListOutlined /> Danh sách phân công nhóm xưởng </template>
          <div class="d-flex justify-content-end mb-3 gap-3">
            <a-button type="primary" @click="handleShowModalAdd">
              <PlusOutlined /> Phân công nhóm xưởng
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
              <template v-if="column.key === 'factoryName'">
                <RouterLink
                  :to="{ name: ROUTE_NAMES.MANAGEMENT_PLAN_DATE, params: { id: record.id } }"
                >
                  {{ record.factoryName }}
                </RouterLink>
              </template>
              <template v-if="column.key === 'time'">
                <a-tag color="blue">{{ formatDate(record.fromDate) }}</a-tag
                >->
                <a-tag color="red">{{ formatDate(record.toDate) }}</a-tag>
              </template>
              <template v-if="column.dataIndex === 'totalShift'">
                <a-tag> {{ record.totalShift }} buổi </a-tag>
              </template>
              <template v-if="column.dataIndex === 'totalStudent'">
                <a-tag> {{ record.totalStudent }} sinh viên </a-tag>
              </template>
              <template v-if="column.dataIndex === 'status'">
                <a-switch
                  class="me-2"
                  :checked="record.status === 1"
                  :disabled="_detail.status !== 1"
                  @change="handleChangeStatus(record.id)"
                />
                <a-tag :color="record.status === 1 && _detail.status === 1 ? 'green' : 'red'">{{
                  record.status === 1 ? 'Đang triển khai' : 'Ngừng triển khai'
                }}</a-tag>
              </template>
              <template v-if="column.key === 'actions'">
                <a-tooltip title="Chi tiết ca học">
                  <a-button
                    class="btn-outline-primary border-0 me-2"
                    @click="handleShowDetail(record.id)"
                  >
                    <EyeFilled />
                  </a-button>
                </a-tooltip>

                <a-tooltip title="Xoá nhóm khỏi kế hoạch" v-if="record.status !== 1">
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
