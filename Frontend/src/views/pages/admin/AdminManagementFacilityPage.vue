<script setup>
import { ref, reactive, onMounted } from 'vue'
import {
  PlusOutlined,
  EditOutlined,
  SwapOutlined,
  FilterFilled,
  UnorderedListOutlined,
  EditFilled,
  SyncOutlined,
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

const router = useRouter()
const breadcrumbStore = useBreadcrumbStore()
const loadingStore = useLoadingStore()

const breadcrumb = ref([
  {
    name: GLOBAL_ROUTE_NAMES.ADMIN_PAGE,
    breadcrumbName: 'Ban đào tạo',
  },
  {
    name: ROUTE_NAMES.MANAGEMENT_FACILITY,
    breadcrumbName: 'Cơ sở',
  },
])

// Danh sách cơ sở
const facilities = ref([])

// Biến lọc gửi lên API (không chứa thông tin phân trang)
const filter = reactive({
  name: '',
  status: '',
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
const columns = ref([
  { title: '#', dataIndex: 'facilityIndex', key: 'facilityIndex', width: 60 },
  { title: 'Tên cơ sở', dataIndex: 'facilityName', key: 'facilityName', width: 200 },
  { title: 'Mã cơ sở', dataIndex: 'facilityCode', key: 'facilityCode', width: 150 },
  { title: 'Trạng thái', dataIndex: 'facilityStatus', key: 'facilityStatus', width: 120 },
  { title: 'Chức năng', key: 'actions', width: 120 },
])

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
          'Lỗi khi lấy dữ liệu cơ sở'
      )
    })
    .finally(() => {
      isLoading.value = false
      loadingStore.hide()
    })
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
          'Lỗi khi thêm cơ sở'
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
          'Lỗi khi lấy chi tiết cơ sở'
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
          'Lỗi khi cập nhật cơ sở'
      )
    })
    .finally(() => {
      modalUpdateLoading.value = false
      loadingStore.hide()
    })
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
              'Lỗi khi cập nhật trạng thái cơ sở'
          )
        })
        .finally(() => {
          loadingStore.hide()
        })
    },
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
          <a-row :gutter="16" class="filter-container">
            <a-col :xs="24" :md="12" class="col">
              <div class="label-title">Tìm kiếm tên:</div>
              <a-input
                v-model:value="filter.name"
                placeholder="Tìm kiếm theo tên"
                allowClear
                @change="fetchFacilities"
              />
            </a-col>
            <a-col :xs="24" :md="12" class="col">
              <div class="label-title">Trạng thái:</div>
              <a-select
                v-model:value="filter.status"
                placeholder="Chọn trạng thái"
                allowClear
                style="width: 100%"
                @change="fetchFacilities"
              >
                <a-select-option :value="''">Tất cả trạng thái</a-select-option>
                <a-select-option value="ACTIVE">Hoạt động</a-select-option>
                <a-select-option value="INACTIVE">Không hoạt động</a-select-option>
              </a-select>
            </a-col>
          </a-row>
        </a-card>
      </div>

      <div class="col-12">
        <!-- Danh sách cơ sở -->
        <a-card :bordered="false" class="cart">
          <template #title> <UnorderedListOutlined /> Danh sách cơ sở </template>
          <div class="d-flex justify-content-end mb-3">
            <!-- Nút Thêm sử dụng kiểu primary (filled) -->
            <a-button type="primary" @click="modalAdd = true">
              <PlusOutlined /> Thêm cơ sở
            </a-button>
          </div>

          <a-table
            :dataSource="facilities"
            :columns="columns"
            rowKey="id"
            :pagination="pagination"
            @change="handleTableChange"
            :scroll="{ y: 500, x: 'auto' }"
            :loading="isLoading"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.dataIndex === 'facilityStatus'">
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
              </template>
              <template v-else-if="column.key === 'actions'">
                <a-space>
                  <a-tooltip title="Chỉnh sửa cơ sở">
                    <a-button
                      @click="handleUpdateFacility(record)"
                      type="text"
                      class="btn-outline-info me-2"
                    >
                      <EditFilled />
                    </a-button>
                  </a-tooltip>
                  <a-tooltip title="Đổi trạng thái cơ sở">
                    <a-button
                      @click="handleChangeStatusFacility(record)"
                      type="text"
                      class="btn-outline-warning"
                    >
                      <SyncOutlined />
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
