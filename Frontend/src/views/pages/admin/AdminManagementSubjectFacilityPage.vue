<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { message, Modal } from 'ant-design-vue'
import {
  PlusOutlined,
  EyeOutlined,
  EditOutlined,
  DeleteOutlined,
  UnorderedListOutlined,
  FilterFilled,
  SyncOutlined,
  EyeFilled,
  EditFilled,
} from '@ant-design/icons-vue'
import requestAPI from '@/services/requestApiService'
import { DEFAULT_PAGINATION } from '@/constants'
import useLoadingStore from '@/stores/useLoadingStore'
import { API_ROUTES_ADMIN } from '@/constants/adminConstant'
import useBreadcrumbStore from '@/stores/useBreadCrumbStore'
import { ROUTE_NAMES } from '@/router/adminRoute'
import { GLOBAL_ROUTE_NAMES } from '@/constants/routesConstant'
import { autoAddColumnWidth } from '@/utils/utils'

const route = useRoute()
const loadingStore = useLoadingStore()
const breadcrumbStore = useBreadcrumbStore()

const breadcrumb = ref([
  {
    name: GLOBAL_ROUTE_NAMES.ADMIN_PAGE,
    breadcrumbName: 'Ban đào tạo',
  },
  {
    name: ROUTE_NAMES.MANAGEMENT_SUBJECT,
    breadcrumbName: 'Quản lý bộ môn',
  },
  {
    name: ROUTE_NAMES.MANAGEMENT_SUBJECT_FACILITY,
    breadcrumbName: 'Bộ môn - ' + route.query.name,
  },
])

/* ----------------- Data & Reactive Variables ----------------- */
const subjectFacility = ref([])
const facility = ref([])
const facilitySubject = ref([])

const filter = reactive({
  name: '',
  status: null,
  facilityId: null,
  subjectId: route.query.subjectId,
})

const pagination = reactive({
  ...DEFAULT_PAGINATION,
})

const modalAdd = ref(false)
const modalDetail = ref(false)
const modalUpdate = ref(false)

const newSubjectFacility = reactive({
  subjectId: null,
  name: '',
  facilityId: [],
})

const updateSubjectFacility = reactive({
  id: null,
  subjectId: null,
  name: '',
  facilityId: null,
})

const detailSubjectFacility = reactive({
  id: '',
  subjectName: null,
  facilityName: null,
  status: 1,
  createdAt: '',
  updatedAt: '',
})

const columns = ref(
  autoAddColumnWidth([
    { title: '#', dataIndex: 'orderNumber', key: 'orderNumber' },
    { title: 'Tên bộ môn', dataIndex: 'subjectName', key: 'subjectName' },
    {
      title: 'Tên cơ sở',
      dataIndex: 'facilityName',
      key: 'facilityName',
    },
    { title: 'Trạng thái', dataIndex: 'status', key: 'status' },
    { title: 'Chức năng', key: 'actions' },
  ]),
)

/* ----------------- Methods ----------------- */
const fetchSubjectFacility = () => {
  loadingStore.show()
  requestAPI
    .post(
      `${API_ROUTES_ADMIN.FETCH_DATA_SUBJECT_FACILITY}/list`,
      {
        ...filter,
      },
      {
        params: {
          page: pagination.current,
          size: pagination.pageSize,
        },
      },
    )
    .then((response) => {
      const result = response.data.data
      subjectFacility.value = result.data
      pagination.total = result.totalPages * pagination.pageSize
    })
    .catch((error) => {
      message.error(error.response?.data?.message || 'Lỗi khi lấy danh sách bộ môn cơ sở')
    })
    .finally(() => {
      loadingStore.hide()
    })
}

const fetchSubject = () => {
  loadingStore.show()
  requestAPI
    .get(`${API_ROUTES_ADMIN.FETCH_DATA_SUBJECT}/${filter.subjectId}`)
    .then((response) => {
      newSubjectFacility.subjectId = response.data.data.id
      newSubjectFacility.name = response.data.data.name
    })
    .catch((error) => {
      message.error(error.response?.data?.message || 'Lỗi khi lấy thông tin bộ môn')
    })
    .finally(() => {
      loadingStore.hide()
    })
}

const fetchFacilityCombobox = () => {
  loadingStore.show()
  requestAPI
    .get(`${API_ROUTES_ADMIN.FETCH_DATA_SUBJECT_FACILITY}/facility-combobox`)
    .then((response) => {
      facility.value = response.data
    })
    .catch((error) => {
      message.error(error.response?.data?.message || 'Lỗi khi lấy danh sách cơ sở')
    })
    .finally(() => {
      loadingStore.hide()
    })
}

const fetchFacilitySubjectCombobox = () => {
  loadingStore.show()
  requestAPI
    .post(`${API_ROUTES_ADMIN.FETCH_DATA_SUBJECT_FACILITY}/facility-combobox`, {
      subjectId: filter.subjectId,
    })
    .then((response) => {
      facilitySubject.value = response.data.data
    })
    .catch((error) => {
      message.error(error.response?.data?.message || 'Lỗi khi lấy danh sách cơ sở chưa có')
    })
    .finally(() => {
      loadingStore.hide()
    })
}

const handleTableChange = (pageInfo) => {
  pagination.current = pageInfo.current
  pagination.pageSize = pageInfo.pageSize
  fetchSubjectFacility()
}

const showAddModal = () => {
  modalAdd.value = true
  newSubjectFacility.facilityId = null
}

const handleAddSubjectFacility = () => {
  if (newSubjectFacility.facilityId === null) {
    return message.error('Vui lòng chọn 1 cơ sở')
  }

  loadingStore.show()

  requestAPI
    .post(API_ROUTES_ADMIN.FETCH_DATA_SUBJECT_FACILITY, {
      facilityId: newSubjectFacility.facilityId,
      subjectId: newSubjectFacility.subjectId,
    })
    .then(({ data: response }) => {
      message.success(response.message)
      modalAdd.value = false
      fetchSubjectFacility()
    })
    .catch((error) => {
      message.error(error.response?.data?.message || 'Không thể thêm bộ môn cơ sở')
    })
    .finally(() => {
      loadingStore.hide()
    })
}

const handleUpdateSubjectFacility = () => {
  if (updateSubjectFacility.facilityId === null) {
    return message.error('Vui lòng chọn 1 cơ sở')
  }

  loadingStore.show()

  requestAPI
    .put(
      API_ROUTES_ADMIN.FETCH_DATA_SUBJECT_FACILITY + '/' + updateSubjectFacility.id,
      updateSubjectFacility,
    )
    .then(({ data: response }) => {
      message.success(response.message)
      modalUpdate.value = false
      fetchSubjectFacility()
    })
    .catch((error) => {
      message.error(error.response?.data?.message || 'Không thể cập nhật bộ môn cơ sở')
    })
    .finally(() => {
      loadingStore.hide()
    })
}

const handleUpdateProject = (record) => {
  modalUpdate.value = true
  updateSubjectFacility.id = record.id
  updateSubjectFacility.facilityId = record.facilityId
  updateSubjectFacility.subjectId = record.subjectId
  updateSubjectFacility.name = record.subjectName
}

const handleDeleteSubjectFacility = (record) => {
  Modal.confirm({
    title: 'Xác nhận đổi trạng thái',
    content: `Bạn có chắc chắn muốn chuyển đổi trạng thái bộ môn ${record.subjectName} cơ sở ${record.facilityName}?`,
    onOk: () => {
      loadingStore.show()
      requestAPI
        .delete(`${API_ROUTES_ADMIN.FETCH_DATA_SUBJECT_FACILITY}/${record.id}`)
        .then(() => {
          message.success('Đổi trạng thái bộ môn cơ sở thành công')
          fetchSubjectFacility()
        })
        .catch((error) => {
          message.error(error.response?.data?.message || 'Lỗi khi đổi trạng thái bộ môn cơ sở')
        })
        .finally(() => {
          loadingStore.hide()
        })
    },
  })
}

const handleDetailSubjectFacility = (record) => {
  loadingStore.show()
  requestAPI
    .get(`${API_ROUTES_ADMIN.FETCH_DATA_SUBJECT_FACILITY}/${record.id}`)
    .then((response) => {
      Object.assign(detailSubjectFacility, response.data.data)
      modalDetail.value = true
    })
    .catch((error) => {
      message.error(error.response?.data?.message || 'Lỗi khi lấy chi tiết bộ môn cơ sở')
    })
    .finally(() => {
      loadingStore.hide()
    })
}

const formatDate = (timestamp) => {
  if (!timestamp) return 'Không xác định'
  return new Date(timestamp).toLocaleString()
}

const getStatusText = (status) => {
  return status == 1 || status === 'ACTIVE' ? 'Hoạt động' : 'Không hoạt động'
}

const getStatusColor = (status) => {
  return status == 1 || status === 'ACTIVE' ? 'green' : 'red'
}

const handleClearFilter = () => {
  // Clear all filter values
  Object.keys(filter).forEach((key) => {
    filter[key] = ''
  })
  pagination.current = 1
  fetchSubjectFacility()
}

/* ----------------- Lifecycle Hooks ----------------- */
onMounted(() => {
  breadcrumbStore.setRoutes(breadcrumb.value)
  fetchSubjectFacility()
  fetchSubject()
  fetchFacilityCombobox()
  fetchFacilitySubjectCombobox()
})
</script>

<template>
  <div class="container-fluid">
    <!-- Card Bộ lọc tìm kiếm -->
    <div class="row g-3">
      <div class="col-12">
        <a-card :bordered="false" class="cart mb-3">
          <template #title> <FilterFilled /> Bộ lọc tìm kiếm </template>
          <div class="row g-3 filter-container">
            <div class="col-md-4 col-sm-6">
              <label class="label-title">Từ khoá:</label>
              <a-input
                v-model:value="filter.name"
                placeholder="Nhập tên hoặc mã cơ sở vật chất"
                allowClear
                @change="fetchSubjectFacility"
              />
            </div>
            <div class="col-md-4 col-sm-6">
              <label class="label-title">Bộ môn:</label>
              <a-select
                v-model:value="filter.idSubject"
                placeholder="Chọn bộ môn"
                allowClear
                style="width: 100%"
                @change="fetchSubjectFacility"
              >
                <a-select-option :value="''">Tất cả bộ môn</a-select-option>
                <a-select-option v-for="item in subject" :key="item.id" :value="item.id">
                  {{ item.name }}
                </a-select-option>
              </a-select>
            </div>
            <div class="col-md-4 col-sm-6">
              <label class="label-title">Trạng thái:</label>
              <a-select
                v-model:value="filter.status"
                placeholder="Chọn trạng thái"
                allowClear
                style="width: 100%"
                @change="fetchSubjectFacility"
              >
                <a-select-option :value="''">Tất cả trạng thái</a-select-option>
                <a-select-option value="1">Hoạt động</a-select-option>
                <a-select-option value="0">Không hoạt động</a-select-option>
              </a-select>
            </div>
          </div>
          <div class="row">
            <div class="col-12">
              <div class="d-flex justify-content-center flex-wrap gap-2 mt-3">
                <a-button class="btn-light" @click="fetchSubjectFacility">
                  <FilterFilled /> Lọc
                </a-button>
                <a-button class="btn-gray" @click="handleClearFilter"> Huỷ lọc </a-button>
              </div>
            </div>
          </div>
        </a-card>
      </div>

      <!-- Card Danh sách bộ môn cơ sở -->
      <div class="col-12">
        <a-card :bordered="false" class="cart">
          <template #title> <UnorderedListOutlined /> Danh sách bộ môn cơ sở </template>
          <div class="d-flex justify-content-end mb-3">
            <a-tooltip title="Thêm bộ môn cơ sở">
              <a-button type="primary" @click="showAddModal"> <PlusOutlined /> Thêm mới </a-button>
            </a-tooltip>
          </div>
          <a-table
            class="nowrap"
            :dataSource="subjectFacility"
            :columns="columns"
            rowKey="id"
            :pagination="pagination"
            @change="handleTableChange"
            :loading="loadingStore.isLoading"
            :scroll="{ y: 500, x: 'auto' }"
          >
            <template #bodyCell="{ column, record, index }">
              <template v-if="column.dataIndex === 'status'">
                <span class="nowrap">
                  <a-switch
                    class="me-2"
                    :checked="record.status == 'ACTIVE' || record.status == 1"
                    @change="handleDeleteSubjectFacility(record)"
                  />
                  <a-tag :color="getStatusColor(record.status)">
                    {{ getStatusText(record.status) }}
                  </a-tag>
                </span>
              </template>
              <template v-else>
                {{ record[column.dataIndex] }}
              </template>
              <template v-if="column.key === 'actions'">
                <a-space>
                  <a-tooltip title="Xem chi tiết">
                    <a-button
                      @click="handleDetailSubjectFacility(record)"
                      type="text"
                      class="btn-outline-primary me-2"
                    >
                      <EyeFilled />
                    </a-button>
                  </a-tooltip>
                  <a-tooltip title="Sửa">
                    <a-button
                      @click="handleUpdateProject(record)"
                      type="text"
                      class="btn-outline-info me-2"
                    >
                      <EditFilled />
                    </a-button>
                  </a-tooltip>
                </a-space>
              </template>
            </template>
          </a-table>
        </a-card>
      </div>
    </div>

    <!-- Modal Thêm bộ môn cơ sở -->
    <a-modal
      v-model:open="modalAdd"
      title="Thêm Bộ Môn Cơ Sở"
      @ok="handleAddSubjectFacility"
      :okButtonProps="{ loading: loadingStore.isLoading }"
    >
      <a-form layout="vertical">
        <a-form-item label="Tên Bộ Môn">
          <a-input v-model:value="newSubjectFacility.name" disabled />
        </a-form-item>
        <a-form-item label="Cơ Sở" required>
          <a-select
            v-model:value="newSubjectFacility.facilityId"
            placeholder="Chọn một cơ sở"
            allowClear
          >
            <a-select-option v-for="f in facilitySubject" :key="f.id" :value="f.id">
              {{ f.name }}
            </a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- Modal Xem chi tiết bộ môn cơ sở -->
    <a-modal v-model:open="modalDetail" title="Chi tiết bộ môn cơ sở" :footer="null">
      <a-descriptions bordered :column="1">
        <a-descriptions-item label="Tên bộ môn">
          {{ detailSubjectFacility.subjectName }}
        </a-descriptions-item>
        <a-descriptions-item label="Tên cơ sở">
          {{ detailSubjectFacility.facilityName }}
        </a-descriptions-item>
        <a-descriptions-item label="Trạng thái">
          <a-tag :color="getStatusColor(detailSubjectFacility.status)">
            {{ getStatusText(detailSubjectFacility.status) }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="Ngày tạo">
          {{ formatDate(detailSubjectFacility.createdAt) }}
        </a-descriptions-item>
        <a-descriptions-item label="Ngày cập nhật">
          {{ formatDate(detailSubjectFacility.updatedAt) }}
        </a-descriptions-item>
      </a-descriptions>
    </a-modal>

    <!-- Modal Sửa bộ môn cơ sở -->
    <a-modal
      v-model:open="modalUpdate"
      title="Sửa bộ môn cơ sở"
      @ok="handleUpdateSubjectFacility"
      :okButtonProps="{ loading: loadingStore.isLoading }"
    >
      <a-form layout="vertical">
        <a-form-item label="Tên Bộ Môn">
          <a-input v-model:value="updateSubjectFacility.name" disabled />
        </a-form-item>
        <a-form-item label="Cơ Sở" required>
          <a-select
            v-model:value="updateSubjectFacility.facilityId"
            placeholder="Chọn một cơ sở"
            allowClear
          >
            <a-select-option v-for="f in facilitySubject" :key="f.id" :value="f.id">
              {{ f.name }}
            </a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>
