<template>
  <div class="container">
    <h1 class="mb-3">Quản lý cơ sở</h1>
    <!-- Bộ lọc tìm kiếm -->
    <a-card title="Bộ lọc" :bordered="false" class="cart mb-4">
      <a-row :gutter="16" class="filter-container">
        <a-col :xs="24" :md="12">
          <a-input
            v-model:value="filter.name"
            placeholder="Tìm kiếm theo tên"
            allowClear
            @change="fetchFacilities"
          />
        </a-col>
        <a-col :xs="24" :md="12">
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

    <!-- Danh sách cơ sở -->
    <a-card title="Danh sách cơ sở" :bordered="false" class="cart mb-4">
      <div class="d-flex justify-content-end mb-3">
        <a-button
          class="px-3 py-2"
          style="background-color: #fff7e6; color: black; border: 1px solid #ffa940"
          @click="() => (modalAdd = true)"
        >
          <PlusOutlined />
          Thêm
        </a-button>
      </div>

      <div style="max-height: 500px; overflow-y: auto">
        <a-table
          :dataSource="facilities"
          :columns="columns"
          rowKey="id"
          bordered
          :pagination="pagination"
          @change="handleTableChange"
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
            <template v-if="column.key === 'actions'">
              <a-button
                @click="handleUpdateFacility(record)"
                type="text"
                class="me-2"
                style="background-color: #fff7e6; border: 1px solid #ffa940"
              >
                <EditOutlined />
              </a-button>
              <a-button
                @click="handleChangeStatusFacility(record)"
                type="text"
                style="background-color: #fff7e6; border: 1px solid #ffa940"
              >
                <SwapOutlined />
              </a-button>
            </template>
          </template>
        </a-table>
      </div>
    </a-card>

    <a-modal v-model:open="modalAdd" title="Thêm cơ sở" @ok="handleAddFacility">
      <a-form layout="vertical">
        <a-form-item label="Tên cơ sở" required>
          <a-input v-model:value="newFacility.facilityName" />
        </a-form-item>
      </a-form>
    </a-modal>

    <a-modal v-model:open="modalUpdate" title="Cập nhật cơ sở" @ok="updateFacility">
      <a-form layout="vertical">
        <a-form-item label="Tên cơ sở" required>
          <a-input v-model:value="detailFacility.facilityName" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { SearchOutlined, PlusOutlined, EditOutlined, SwapOutlined } from '@ant-design/icons-vue'
import { message, Modal } from 'ant-design-vue'
import requestAPI from '@/services/requestApiService'

const facilities = ref([])
const filter = reactive({ name: '', status: '', page: 1, pageSize: 5 })
const modalAdd = ref(false)
const modalUpdate = ref(false)
const newFacility = reactive({ facilityName: '' })
const detailFacility = ref({})

const columns = ref([
  { title: 'STT', dataIndex: 'facilityIndex', key: 'facilityIndex', width: 60 },
  { title: 'Tên cơ sở', dataIndex: 'facilityName', key: 'facilityName', width: 200 },
  { title: 'Mã cơ sở', dataIndex: 'facilityCode', key: 'facilityCode', width: 150 },
  { title: 'Trạng thái', dataIndex: 'facilityStatus', key: 'facilityStatus', width: 120 },
  { title: 'Chức năng', key: 'actions', width: 120 },
])

const pagination = reactive({
  current: 1,
  pageSize: 5,
  total: 0,
  showSizeChanger: false,
})

// Các phương thức giữ nguyên như mã gốc
const fetchFacilities = () => {
  requestAPI
    .get('http://localhost:8765/api/v1/admin/facilities', { params: filter })
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

const handleTableChange = (paginationData) => {
  filter.page = paginationData.current
  fetchFacilities()
}

const handleAddFacility = () => {
  if (!newFacility.facilityName) {
    message.error('Tên cơ sở không được bỏ trống')
    return
  }
  requestAPI
    .post('http://localhost:8765/api/v1/admin/facilities', newFacility)
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
    .get(`http://localhost:8765/api/v1/admin/facilities/${record.id}`)
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
    .put(
      `http://localhost:8765/api/v1/admin/facilities/${detailFacility.value.id}`,
      detailFacility.value
    )
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
        .put(`http://localhost:8765/api/v1/admin/facilities/status/${record.id}`)
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
  fetchFacilities()
})
</script>

<style scoped>
.container {
  width: 100%;
  max-width: none;
}
.cart {
  margin-top: 5px;
}
.filter-container {
  margin-bottom: 5px;
}
</style>
