<script setup>
import { ref, reactive, onMounted } from 'vue'
import {
  PlusOutlined,
  FilterFilled,
  UnorderedListOutlined,
  EditFilled,
  GlobalOutlined,
  ArrowUpOutlined,
  ArrowDownOutlined,
  SearchOutlined,
  AimOutlined,
  FieldBinaryOutlined,
} from '@ant-design/icons-vue'
import { message, Modal } from 'ant-design-vue'
import requestAPI from '@/services/requestApiService'
import { API_ROUTES_ADMIN } from '@/constants/adminConstant'
import { DEFAULT_PAGINATION } from '@/constants'
import { useRouter } from 'vue-router'
import { ROUTE_NAMES } from '@/router/adminRoute'
import { GLOBAL_ROUTE_NAMES } from '@/constants/routesConstant'
import useBreadcrumbStore from '@/stores/useBreadCrumbStore'
import useLoadingStore from '@/stores/useLoadingStore'
import { autoAddColumnWidth } from '@/utils/utils'

const router = useRouter()
const breadcrumbStore = useBreadcrumbStore()
const loadingStore = useLoadingStore()

const breadcrumb = ref([
  {
    name: GLOBAL_ROUTE_NAMES.ADMIN_PAGE,
    breadcrumbName: 'Admin',
  },
  {
    name: ROUTE_NAMES.MANAGEMENT_FACILITY,
    breadcrumbName: 'Quản lý cơ sở',
  },
])

// Danh sách cơ sở
const facilities = ref([])

// Biến lọc gửi lên API (không chứa thông tin phân trang)
const filter = reactive({
  name: null,
  status: null,
})

// Sử dụng pagination dưới dạng reactive (theo mẫu plandate)
const pagination = reactive({
  ...DEFAULT_PAGINATION,
})

// Loading cho bảng và modal
const isLoading = ref(false)
const modalAddLoading = ref(false)
const modalUpdateLoading = ref(false)

// Modal hiển thị
const modalAdd = ref(false)
const modalUpdate = ref(false)

// Dữ liệu thêm mới cơ sở
const newFacility = reactive({ facilityName: '' })
// Dữ liệu cập nhật cơ sở
const detailFacility = ref({})

// Cấu hình cột cho bảng
const columns = ref(
  autoAddColumnWidth([
    { title: '#', dataIndex: 'facilityIndex', key: 'facilityIndex' },
    {
      title: 'Tên cơ sở',
      dataIndex: 'facilityName',
      key: 'facilityName',
    },
    { title: 'Mã cơ sở', dataIndex: 'facilityCode', key: 'facilityCode' },
    {
      title: 'Trạng thái',
      dataIndex: 'facilityStatus',
      key: 'facilityStatus',
    },
    { title: 'Chức năng', key: 'actions' },
  ]),
)

// Hàm lấy danh sách cơ sở
const fetchFacilities = () => {
  if (isLoading.value) return
  loadingStore.show()
  isLoading.value = true
  requestAPI
    .get(API_ROUTES_ADMIN.FETCH_DATA_FACILITY, {
      params: {
        ...filter,
        page: pagination.current,
        size: pagination.pageSize,
      },
    })
    .then((response) => {
      facilities.value = response.data.data.data
      // Nếu API trả về totalRecords thì dùng luôn trường đó,
      // nếu không thì tính bằng cách nhân totalPages với pageSize.
      if (response.data.data.totalRecords !== undefined) {
        pagination.total = response.data.data.totalRecords
      } else {
        pagination.total = response.data.data.totalPages * pagination.pageSize
      }
    })
    .catch((error) => {
      message.error(
        (error.response && error.response.data && error.response.data.message) ||
          'Lỗi khi lấy dữ liệu cơ sở',
      )
    })
    .finally(() => {
      isLoading.value = false
      loadingStore.hide()
    })
}

const handleClearFilter = () => {
  Object.assign(filter, {
    name: null,
    status: null,
  })
  fetchFacilities()
}

const handleSubmitFilter = () => {
  pagination.value.current = 1
  fetchFacilities()
}

const handleShowModalAdd = () => {
  newFacility.facilityName = null
  modalAdd.value = true
}

// Sự kiện thay đổi phân trang (dynamic)
const handleTableChange = (pageInfo) => {
  pagination.current = pageInfo.current
  pagination.pageSize = pageInfo.pageSize
  fetchFacilities()
}

// Hàm thêm cơ sở
const handleAddFacility = () => {
  if (!newFacility.facilityName) {
    message.error('Tên cơ sở không được bỏ trống')
    return
  }
  modalAddLoading.value = true
  loadingStore.show()
  requestAPI
    .post(API_ROUTES_ADMIN.FETCH_DATA_FACILITY, newFacility)
    .then(() => {
      message.success('Thêm cơ sở thành công')
      modalAdd.value = false
      fetchFacilities()
      clearFormAdd()
    })
    .catch((error) => {
      message.error(
        (error.response && error.response.data && error.response.data.message) ||
          'Lỗi khi thêm cơ sở',
      )
    })
    .finally(() => {
      modalAddLoading.value = false
      loadingStore.hide()
    })
}

// Hàm lấy chi tiết cơ sở để cập nhật
const handleUpdateFacility = (record) => {
  loadingStore.show()
  requestAPI
    .get(`${API_ROUTES_ADMIN.FETCH_DATA_FACILITY}/${record.id}`)
    .then((response) => {
      detailFacility.value = response.data.data
      modalUpdate.value = true
    })
    .catch((error) => {
      message.error(
        (error.response && error.response.data && error.response.data.message) ||
          'Lỗi khi lấy chi tiết cơ sở',
      )
    })
    .finally(() => {
      loadingStore.hide()
    })
}

// Hàm cập nhật cơ sở
const updateFacility = () => {
  if (!detailFacility.value.facilityName) {
    message.error('Tên cơ sở không được bỏ trống')
    return
  }
  modalUpdateLoading.value = true
  loadingStore.show()
  requestAPI
    .put(`${API_ROUTES_ADMIN.FETCH_DATA_FACILITY}/${detailFacility.value.id}`, detailFacility.value)
    .then(() => {
      message.success('Cập nhật cơ sở thành công')
      modalUpdate.value = false
      fetchFacilities()
    })
    .catch((error) => {
      message.error(
        (error.response && error.response.data && error.response.data.message) ||
          'Lỗi khi cập nhật cơ sở',
      )
    })
    .finally(() => {
      modalUpdateLoading.value = false
      loadingStore.hide()
    })
}

const handleShowIP = (id) => {
  router.push({ name: ROUTE_NAMES.MANAGEMENT_FACILITY_IP, params: { id: id } })
}

const handleShowShift = (id) => {
  router.push({ name: ROUTE_NAMES.MANAGEMENT_FACILITY_SHIFT, params: { id: id } })
}

const handleShowLocation = (id) => {
  router.push({ name: ROUTE_NAMES.MANAGEMENT_FACILITY_LOCATION, params: { id: id } })
}

// Hàm đổi trạng thái cơ sở
const handleChangeStatusFacility = (record) => {
  Modal.confirm({
    title: 'Xác nhận thay đổi trạng thái',
    content: `Bạn có chắc chắn muốn thay đổi trạng thái của cơ sở ${record.facilityName} ?`,
    onOk: () => {
      loadingStore.show()
      requestAPI
        .put(`${API_ROUTES_ADMIN.FETCH_DATA_FACILITY}/status/${record.id}`)
        .then(() => {
          message.success('Cập nhật trạng thái cơ sở thành công')
          fetchFacilities()
        })
        .catch((error) => {
          message.error(
            (error.response && error.response.data && error.response.data.message) ||
              'Lỗi khi cập nhật trạng thái cơ sở',
          )
        })
        .finally(() => {
          loadingStore.hide()
        })
    },
  })
}

const handleUp = (id) => {
  loadingStore.show()
  requestAPI
    .put(`${API_ROUTES_ADMIN.FETCH_DATA_FACILITY}/up/${id}`)
    .then(({ data: response }) => {
      message.success(response.message || 'Tăng mức ưu tiên hiển thị thành công')
      fetchFacilities()
    })
    .catch((error) => {
      message.error(
        (error.response && error.response.data && error.response.data.message) ||
          'Có lỗi xảy ra. Vui lòng thử lại sau ít phút',
      )
    })
    .finally(() => {
      loadingStore.hide()
    })
}

const handleDown = (id) => {
  loadingStore.show()
  requestAPI
    .put(`${API_ROUTES_ADMIN.FETCH_DATA_FACILITY}/down/${id}`)
    .then(({ data: response }) => {
      message.success(response.message || 'Giảm mức ưu tiên hiển thị thành công')
      fetchFacilities()
    })
    .catch((error) => {
      message.error(
        (error.response && error.response.data && error.response.data.message) ||
          'Có lỗi xảy ra. Vui lòng thử lại sau ít phút',
      )
    })
    .finally(() => {
      loadingStore.hide()
    })
}

const clearFormAdd = () => {
  newFacility.facilityName = ''
}

onMounted(() => {
  breadcrumbStore.setRoutes(breadcrumb.value)
  fetchFacilities()
})
</script>

<template>
  <div class="container-fluid">
    <div class="row g-3">
      <div class="col-12">
        <!-- Bộ lọc tìm kiếm -->
        <a-card :bordered="false" class="cart">
          <template #title> <FilterFilled /> Bộ lọc </template>
          <div class="row g-3">
            <div class="col-xxl-8 col-md-8 col-sm-6">
              <div class="label-title">Từ khoá:</div>
              <a-input
                v-model:value="filter.name"
                placeholder="Tìm kiếm theo tên"
                allowClear
                @change="fetchFacilities"
              >
                <template #prefix>
                  <SearchOutlined />
                </template>
              </a-input>
            </div>
            <div class="col-xxl-4 col-md-4 col-sm-6">
              <div class="label-title">Trạng thái:</div>
              <a-select
                v-model:value="filter.status"
                placeholder="Chọn trạng thái"
                allowClear
                class="w-100"
                @change="fetchFacilities"
              >
                <a-select-option :value="null">Tất cả trạng thái</a-select-option>
                <a-select-option value="ACTIVE">Hoạt động</a-select-option>
                <a-select-option value="INACTIVE">Không hoạt động</a-select-option>
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
        <!-- Danh sách cơ sở -->
        <a-card :bordered="false" class="cart">
          <template #title> <UnorderedListOutlined /> Danh sách cơ sở </template>
          <div class="d-flex justify-content-end mb-3">
            <!-- Nút Thêm sử dụng kiểu primary (filled) -->
            <a-button type="primary" @click="handleShowModalAdd">
              <PlusOutlined /> Thêm cơ sở
            </a-button>
          </div>

          <a-table
            class="nowrap"
            :dataSource="facilities"
            :columns="columns"
            rowKey="id"
            :pagination="pagination"
            @change="handleTableChange"
            :scroll="{ x: 'auto' }"
            :loading="isLoading"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.dataIndex === 'facilityStatus'">
                <span class="nowrap">
                  <a-switch
                    class="me-2"
                    :checked="record.facilityStatus === 'ACTIVE' || record.facilityStatus === 1"
                    @change="handleChangeStatusFacility(record)"
                  />
                  <a-tag
                    :color="
                      record.facilityStatus === 'ACTIVE' || record.facilityStatus === 1
                        ? 'green'
                        : 'red'
                    "
                  >
                    {{
                      record.facilityStatus === 'ACTIVE' || record.facilityStatus === 1
                        ? 'Hoạt động'
                        : 'Không hoạt động'
                    }}
                  </a-tag>
                </span>
              </template>
              <template v-else-if="column.key === 'actions'">
                <a-space>
                  <a-tooltip title="Chỉnh sửa cơ sở">
                    <a-button
                      @click="handleUpdateFacility(record)"
                      type="text"
                      class="btn-outline-info border-0 me-2"
                    >
                      <EditFilled />
                    </a-button>
                  </a-tooltip>
                  <a-tooltip title="Quản lý ca học">
                    <a-button
                      class="btn-outline-primary border-0 me-2"
                      @click="handleShowShift(record.id)"
                    >
                      <FieldBinaryOutlined />
                    </a-button>
                  </a-tooltip>
                  <a-tooltip title="Quản lý ip cơ sở">
                    <a-button
                      class="btn-outline-primary border-0 me-2"
                      @click="handleShowIP(record.id)"
                    >
                      <GlobalOutlined />
                    </a-button>
                  </a-tooltip>
                  <a-tooltip title="Quản lý địa điểm cơ sở">
                    <a-button
                      class="btn-outline-primary border-0 me-2"
                      @click="handleShowLocation(record.id)"
                    >
                      <AimOutlined />
                    </a-button>
                  </a-tooltip>
                  <a-tooltip title="Lên trên" v-if="record.facilityIndex > 1">
                    <a-button class="btn-outline border-0 me-2" @click="handleUp(record.id)">
                      <ArrowUpOutlined />
                    </a-button>
                  </a-tooltip>
                  <a-tooltip title="Xuống dưới" v-if="record.facilityIndex < record.maxPosition">
                    <a-button class="btn-outline border-0 me-2" @click="handleDown(record.id)">
                      <ArrowDownOutlined />
                    </a-button>
                  </a-tooltip>
                </a-space>
              </template>
              <template v-else>
                {{ record[column.dataIndex] }}
              </template>
            </template>
          </a-table>
        </a-card>
      </div>
    </div>

    <!-- Modal Thêm cơ sở -->
    <a-modal
      v-model:open="modalAdd"
      title="Thêm cơ sở"
      @ok="handleAddFacility"
      :okButtonProps="{ loading: modalAddLoading }"
    >
      <a-form layout="vertical">
        <a-form-item label="Tên cơ sở" required>
          <a-input v-model:value="newFacility.facilityName" placeholder="--Tên cơ sở--" />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- Modal Cập nhật cơ sở -->
    <a-modal
      v-model:open="modalUpdate"
      title="Cập nhật cơ sở"
      @ok="updateFacility"
      :okButtonProps="{ loading: modalUpdateLoading }"
    >
      <a-form layout="vertical">
        <a-form-item label="Tên cơ sở" required>
          <a-input v-model:value="detailFacility.facilityName" placeholder="--Tên cơ sở--" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>
