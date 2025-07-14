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
import { LMap, LTileLayer, LMarker, LCircle } from '@vue-leaflet/vue-leaflet'
import L from 'leaflet'
import 'leaflet-control-geocoder'

import 'leaflet/dist/leaflet.css'
import 'leaflet-control-geocoder/dist/Control.Geocoder.css'
import { autoAddColumnWidth, debounce } from '@/utils/utils'

delete L.Icon.Default.prototype._getIconUrl

L.Icon.Default.mergeOptions({
  iconRetinaUrl: new URL('leaflet/dist/images/marker-icon-2x.png', import.meta.url).href,
  iconUrl: new URL('leaflet/dist/images/marker-icon.png', import.meta.url).href,
  shadowUrl: new URL('leaflet/dist/images/marker-shadow.png', import.meta.url).href,
})

const route = useRoute()
const router = useRouter()

const breadcrumbStore = useBreadcrumbStore()
const loadingStore = useLoadingStore()

const isLoading = ref(false)

const mapRef = ref(null)
const mapCenter = ref([0, 0])
const selectedLatLng = ref(null)
const leafletMap = ref(null)
const MAP_ZOOM = 17
const MAP_CENTER_DEFAULT = [21.0278, 105.8342]

const tileUrl = 'https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png'

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

const columns = ref(
  autoAddColumnWidth([
    { title: '#', key: 'rowNumber' },
    { title: 'Tên vị trí', dataIndex: 'name', key: 'name' },
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
})

const formRefAddOrUpdate = ref(null)

const formData = reactive({
  id: null,
  idFacility: null,
  name: null,
  latitude: null,
  longitude: null,
  radius: 1,
})

const formRules = reactive({
  name: [{ required: true, message: 'Vui lòng nhập tên địa điểm!' }],
  latitude: [{ required: true, message: 'Vui lòng nhập vĩ độ!' }],
  longitude: [{ required: true, message: 'Vui lòng nhập kinh độ!' }],
  radius: [{ required: true, message: 'Vui lòng nhập bán kính!' }],
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
        name: ROUTE_NAMES.MANAGEMENT_FACILITY_LOCATION,
        breadcrumbName: 'Quản lý địa điểm',
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
    .get(`${API_ROUTES_ADMIN.FETCH_DATA_FACILITY}/${_detail.value.id}/list-location`, {
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
    .delete(`${API_ROUTES_ADMIN.FETCH_DATA_FACILITY}/${id}/delete-location`)
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
    .post(`${API_ROUTES_ADMIN.FETCH_DATA_FACILITY}/${_detail.value.id}/add-location`, {
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
    .put(`${API_ROUTES_ADMIN.FETCH_DATA_FACILITY}/${_detail.value.id}/update-location`, formData)
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
    .put(`${API_ROUTES_ADMIN.FETCH_DATA_FACILITY}/${id}/change-status-location`)
    .then(({ data: response }) => {
      message.success(response.message)
      fetchDataList()
    })
    .catch((error) => {
      message.error(error?.response?.data?.message || 'Không thể thay đổi trạng thái địa điểm')
    })
}

const handleClearFilter = () => {
  Object.assign(dataFilter, {
    keyword: null,
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
    'Thêm địa điểm mới',
  ])
  modalAddOrUpdate.okText = 'Thêm ngay'
  modalAddOrUpdate.onOk = () => handleSubmitAdd()

  getCurrentLocation()

  formData.id = null
  formData.name = null
  formData.longitude = null
  formData.latitude = null
  formData.radius = 1
}

const handleShowUpdate = (item) => {
  if (formRefAddOrUpdate.value) {
    formRefAddOrUpdate.value.clearValidate()
  }

  modalAddOrUpdate.isShow = true
  modalAddOrUpdate.isLoading = false
  modalAddOrUpdate.title = h('span', [
    h(EditFilled, { class: 'me-2 text-primary' }),
    'Chỉnh sửa địa điểm',
  ])
  modalAddOrUpdate.okText = 'Lưu lại'
  modalAddOrUpdate.onOk = () => handleSubmitUpdate()

  formData.id = item.id
  formData.name = item.name
  formData.latitude = item.latitude
  formData.longitude = item.longitude
  formData.radius = item.radius

  mapCenter.value = [item.latitude, item.longitude]
  selectedLatLng.value = {
    lat: mapCenter.value[0],
    lng: mapCenter.value[1],
  }
}

const handleSubmitAdd = async () => {
  try {
    await formRefAddOrUpdate.value.validate()
    Modal.confirm({
      title: `Xác nhận thêm mới`,
      type: 'info',
      content: `Bạn có chắc muốn thêm mới địa điểm này?`,
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
    content: `Bạn có chắc muốn thay đổi trạng thái địa điểm này?`,
    okText: 'Tiếp tục',
    cancelText: 'Hủy bỏ',
    onOk() {
      fetchSubmitChangeStatus(id)
    },
  })
}

const handleShowAlertDelete = (item) => {
  Modal.confirm({
    title: `Xoá địa điểm: ${item.name}`,
    type: 'error',
    content: `Bạn có chắc muốn xoá địa điểm này?`,
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

const handleGetCurrentLocation = () => {
  getCurrentLocation()
}

const onMapReady = (mapInstance) => {
  leafletMap.value = mapInstance

  L.Control.geocoder({
    defaultMarkGeocode: false,
  })
    .on('markgeocode', function (e) {
      const center = e.geocode.center
      formData.latitude = center.lat
      formData.longitude = center.lng
      leafletMap.value.setView(center, MAP_ZOOM)
      selectedLatLng.value = center
    })
    .addTo(leafletMap.value)
}

const onMapClick = (event) => {
  selectedLatLng.value = event.latlng
  formData.latitude = event.latlng.lat
  formData.longitude = event.latlng.lng
}

const getCurrentLocation = () => {
  navigator.geolocation.getCurrentPosition(
    (position) => {
      const { latitude, longitude } = position.coords || {
        latitude: MAP_CENTER_DEFAULT[0],
        longitude: MAP_CENTER_DEFAULT[1],
      }
      mapCenter.value = [latitude, longitude]
      formData.latitude = latitude
      formData.longitude = longitude
    },
    (error) => {
      mapCenter.value = MAP_CENTER_DEFAULT
      formData.latitude = mapCenter.value[0]
      formData.longitude = mapCenter.value[1]
    },
  )
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

watch(formData, () => {
  if (formData.latitude && formData.longitude) {
    mapCenter.value = [formData.latitude, formData.longitude]
  }
})

watch(mapCenter, (newCenter) => {
  selectedLatLng.value = {
    lat: newCenter[0],
    lng: newCenter[1],
  }
  if (mapRef.value?.leafletObject && newCenter[0] && newCenter[1]) {
    mapRef.value.leafletObject.setView(newCenter, MAP_ZOOM)
  }
})
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
      <a-form-item class="col-sm-12" label="Tên địa điểm" name="name" :rules="formRules.name">
        <a-input
          class="w-100"
          v-model:value="formData.name"
          :disabled="modalAddOrUpdate.isLoading"
          allowClear
          @keyup.enter="modalAddOrUpdate.onOk"
        />
      </a-form-item>
      <a-form-item class="col-sm-4" label="Vĩ độ" name="latitude" :rules="formRules.latitude">
        <a-input-number
          class="w-100"
          v-model:value="formData.latitude"
          :min="-90"
          :max="90"
          :disabled="modalAddOrUpdate.isLoading"
          allowClear
          @keyup.enter="modalAddOrUpdate.onOk"
        />
      </a-form-item>

      <a-form-item class="col-sm-4" label="Kinh độ" name="longitude" :rules="formRules.longitude">
        <a-input-number
          class="w-100"
          v-model:value="formData.longitude"
          :min="-180"
          :max="180"
          :disabled="modalAddOrUpdate.isLoading"
          allowClear
          @keyup.enter="modalAddOrUpdate.onOk"
        />
      </a-form-item>
      <a-form-item class="col-sm-2" label="Bán kính (m)" name="radius" :rules="formRules.radius">
        <a-input-number
          class="w-100"
          v-model:value="formData.radius"
          :min="1"
          :disabled="modalAddOrUpdate.isLoading"
          allowClear
          @keyup.enter="modalAddOrUpdate.onOk"
        />
      </a-form-item>
      <a-form-item class="col-sm-2">
        <a-button type="primary" class="mt-4" @click="handleGetCurrentLocation"
          >Vị trí hiện tại</a-button
        >
      </a-form-item>
      <a-form-item class="col-sm-12">
        <LMap
          ref="mapRef"
          style="height: 400px"
          :zoom="MAP_ZOOM"
          :center="mapCenter"
          @click="onMapClick"
          @ready="onMapReady"
        >
          <LTileLayer :url="tileUrl" attribution="" />
          <LMarker v-if="selectedLatLng" :lat-lng="selectedLatLng" />
          <LCircle
            v-if="selectedLatLng"
            :lat-lng="selectedLatLng"
            :radius="formData.radius"
            color="#41395b"
            fillColor="#41395b"
            :fillOpacity="0.2"
          />
        </LMap>
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
                <div class="col-lg-8 col-md-6 col-sm-6">
                  <div class="label-title">Từ khoá:</div>
                  <a-input
                    v-model:value="dataFilter.keyword"
                    placeholder="Tìm theo địa điểm..."
                    allowClear
                  >
                    <template #prefix>
                      <SearchOutlined />
                    </template>
                  </a-input>
                </div>
                <div class="col-lg-4 col-md-6 col-sm-6">
                  <div class="label-title">Trạng thái:</div>
                  <a-select
                    v-model:value="dataFilter.status"
                    class="w-100"
                    :dropdownMatchSelectWidth="false"
                    placeholder="-- Tất cả trạng thái --"
                  >
                    <a-select-option :value="null">-- Tất cả trạng thái --</a-select-option>
                    <a-select-option v-for="(name, id) in STATUS_FACILITY_IP" :key="id" :value="id">
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
          <template #title> <UnorderedListOutlined /> Danh sách địa điểm cho phép </template>

          <div class="d-flex justify-content-end mb-2 flex-wrap gap-3">
            <a-button type="primary" @click="handleShowAdd">
              <PlusOutlined /> Thêm địa điểm mới
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
                <template v-else-if="column.dataIndex === 'radius'">
                  <a-tag color="purple"> {{ record.radius }}m </a-tag>
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
                  <a-tooltip title="Chỉnh sửa địa điểm">
                    <a-button class="btn-outline-info border-0" @click="handleShowUpdate(record)">
                      <EditFilled />
                    </a-button>
                  </a-tooltip>
                  <a-tooltip title="Xoá địa điểm">
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
