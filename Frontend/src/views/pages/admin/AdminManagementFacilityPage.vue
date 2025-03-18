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
import { DEFAULT_PAGINATION } from '@/constants/paginationConstant'
import { useRouter } from 'vue-router'
import { ROUTE_NAMES } from '@/router/adminRoute'
import { GLOBAL_ROUTE_NAMES } from '@/constants/routesConstant'
import useBreadcrumbStore from '@/stores/useBreadCrumbStore'

const router = useRouter()
const breadcrumbStore = useBreadcrumbStore()

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

const facilities = ref([])
const filter = reactive({ name: '', status: '', page: 1, pageSize: 5 })
const modalAdd = ref(false)
const modalUpdate = ref(false)
const newFacility = reactive({ facilityName: '' })
const detailFacility = ref({})

const columns = ref([
  { title: '#', dataIndex: 'facilityIndex', key: 'facilityIndex', width: 60 },
  { title: 'Tên cơ sở', dataIndex: 'facilityName', key: 'facilityName', width: 200 },
  { title: 'Mã cơ sở', dataIndex: 'facilityCode', key: 'facilityCode', width: 150 },
  { title: 'Trạng thái', dataIndex: 'facilityStatus', key: 'facilityStatus', width: 120 },
  { title: 'Chức năng', key: 'actions', width: 120 },
])

const pagination = reactive({
  ...DEFAULT_PAGINATION,
})

// Các phương thức giữ nguyên như mã gốc
const fetchFacilities = () => {
  requestAPI
    .get(API_ROUTES_ADMIN.FETCH_DATA_FACILITY, { params: filter })
    .then((response) => {
      facilities.value = response.data.data.data
      pagination.total = response.data.data.totalPages * filter.pageSize
      pagination.current = filter.page
    })
    .catch((error) => {
      message.error(
        (error.response && error.response.data && error.response.data.message) ||
          'Lỗi khi lấy dữ liệu cơ sở'
      )
    })
}

const handleTableChange = (page) => {
  pagination.page = page.current
  pagination.pageSize = page.pageSize
  fetchFacilities()
}

const handleAddFacility = () => {
  if (!newFacility.facilityName) {
    message.error('Tên cơ sở không được bỏ trống')
    return
  }
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
}

const handleUpdateFacility = (record) => {
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
}

const updateFacility = () => {
  if (!detailFacility.value.facilityName) {
    message.error('Tên cơ sở không được bỏ trống')
    return
  }
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
}

const handleChangeStatusFacility = (record) => {
  Modal.confirm({
    title: 'Xác nhận thay đổi trạng thái',
    content: `Bạn có chắc chắn muốn thay đổi trạng thái của cơ sở ${record.facilityName} ?`,
    onOk: () => {
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
              <a-input
                v-model:value="filter.name"
                placeholder="Tìm kiếm theo tên"
                allowClear
                @change="fetchFacilities"
              />
            </a-col>
            <a-col :xs="24" :md="12" class="col">
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
              <PlusOutlined /> Thêm cở sở
            </a-button>
          </div>

          <a-table
            :dataSource="facilities"
            :columns="columns"
            rowKey="id"
            :pagination="pagination"
            @change="handleTableChange"
            :scroll="{ y: 500, x: 'auto' }"
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
    <a-modal v-model:open="modalAdd" title="Thêm cơ sở" @ok="handleAddFacility">
      <a-form layout="vertical">
        <a-form-item label="Tên cơ sở" required>
          <a-input v-model:value="newFacility.facilityName" placeholder="--Tên cơ sở--" />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- Modal Cập nhật cơ sở -->
    <a-modal v-model:open="modalUpdate" title="Cập nhật cơ sở" @ok="updateFacility">
      <a-form layout="vertical">
        <a-form-item label="Tên cơ sở" required>
          <a-input v-model:value="detailFacility.facilityName" placeholder="--Tên cơ sở--" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>
