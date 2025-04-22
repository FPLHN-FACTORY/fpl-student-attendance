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
const subject = ref({})
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
  name: '',
  facilityId: [],
})

const detailSubjectFacility = reactive({
  id: '',
  subject: { name: '' },
  facility: { name: '' },
  status: 1,
  createdAt: '',
  updatedAt: '',
})

const columns = ref([
  { title: '#', dataIndex: 'indexs', key: 'indexs', width: 50 },
  { title: 'Tên bộ môn', dataIndex: 'subjectName', key: 'subjectName', width: 200 },
  { title: 'Tên cơ sở', dataIndex: 'facilityName', key: 'facilityName', width: 200 },
  { title: 'Trạng thái', dataIndex: 'status', key: 'status', width: 100 },
  { title: 'Chức năng', key: 'actions', width: 200 },
])

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
      }
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
      subject.value = response.data.data
      newSubjectFacility.name = subject.value.name
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
      facilitySubject.value = response.data
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
  fetchFacilitySubjectCombobox()
  newSubjectFacility.facilityId = null
}

const handleAddSubjectFacility = () => {
  loadingStore.show()
  const requests = []

  if (newSubjectFacility.facilityId === null || newSubjectFacility.facilityId.includes(null)) {
    // Thêm tất cả cơ sở chưa có
    facilitySubject.value.forEach((f) => {
      requests.push(
        requestAPI.post(API_ROUTES_ADMIN.FETCH_DATA_SUBJECT_FACILITY, {
          facilityId: f.id,
          subjectId: subject.value.id,
        })
      )
    })
  } else {
    // Thêm các cơ sở được chọn
    newSubjectFacility.facilityId.forEach((f) => {
      requests.push(
        requestAPI.post(API_ROUTES_ADMIN.FETCH_DATA_SUBJECT_FACILITY, {
          facilityId: f,
          subjectId: subject.value.id,
        })
      )
    })
  }

  Promise.all(requests)
    .then(() => {
      message.success('Thêm bộ môn cơ sở thành công')
      modalAdd.value = false
      fetchSubjectFacility()
    })
    .catch((error) => {
      message.error(error.response?.data?.message || 'Lỗi khi thêm bộ môn cơ sở')
    })
    .finally(() => {
      loadingStore.hide()
    })
}

const handleUpdateProject = (record) => {
  modalUpdate.value = true
  // TODO: Implement update logic
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
  return status === '1' ? 'Hoạt động' : 'Không hoạt động'
}

const getStatusColor = (status) => {
  return status === '1' ? 'green' : 'red'
}

/* ----------------- Lifecycle Hooks ----------------- */
onMounted(() => {
  breadcrumbStore.setRoutes(breadcrumb.value)
  fetchSubjectFacility()
  fetchSubject()
  fetchFacilityCombobox()
})
</script>

<template>
  <div class="container-fluid">
    <!-- Card Bộ lọc tìm kiếm -->
    <div class="row g-3">
      <div class="col-12">
        <a-card :bordered="false" class="cart mb-3">
          <template #title> <FilterFilled /> Bộ lọc </template>
          <a-row :gutter="16" class="filter-container">
            <a-col :span="8" class="col">
              <div class="label-title">Tìm kiếm theo tên:</div>
              <a-input
                v-model:value="filter.name"
                placeholder="Tìm kiếm theo tên"
                allowClear
                @change="fetchSubjectFacility"
              />
            </a-col>

            <a-col :span="8" class="col">
              <div class="label-title">Cơ sở:</div>
              <a-select
                v-model:value="filter.facilityId"
                placeholder="Chọn cơ sở"
                allowClear
                style="width: 100%"
                @change="fetchSubjectFacility"
              >
                <a-select-option :value="null">Tất cả cơ sở</a-select-option>
                <a-select-option v-for="f in facility" :key="f.id" :value="f.id">
                  {{ f.name }}
                </a-select-option>
              </a-select>
            </a-col>

            <a-col :span="8" class="col">
              <div class="label-title">Trạng thái:</div>
              <a-select
                v-model:value="filter.status"
                placeholder="Chọn trạng thái"
                allowClear
                style="width: 100%"
                @change="fetchSubjectFacility"
              >
                <a-select-option :value="null">Tất cả trạng thái</a-select-option>
                <a-select-option :value="1">Hoạt động</a-select-option>
                <a-select-option :value="0">Không hoạt động</a-select-option>
              </a-select>
            </a-col>
          </a-row>
        </a-card>
      </div>
    </div>

    <!-- Card Danh sách bộ môn cơ sở -->
    <div class="row g-3">
      <div class="col-12">
        <a-card :bordered="false" class="cart">
          <template #title> <UnorderedListOutlined /> Danh sách bộ môn cơ sở </template>
          <div class="d-flex justify-content-end mb-3">
            <a-tooltip title="Thêm bộ môn cơ sở">
              <a-button type="primary" @click="showAddModal"> <PlusOutlined /> Thêm </a-button>
            </a-tooltip>
          </div>
          <a-table
            :dataSource="subjectFacility"
            :columns="columns"
            rowKey="id"
            :pagination="pagination"
            @change="handleTableChange"
            :loading="loadingStore.isLoading"
            :scroll="{ y: 500, x: 'auto' }"
          >
            <template #bodyCell="{ column, record, index }">
              <template v-if="column.dataIndex">
                <template v-if="column.dataIndex === 'indexs'">
                  {{ index + 1 }}
                </template>
                <template v-else-if="column.dataIndex === 'status'">
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
              </template>
              <template v-else-if="column.key === 'actions'">
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
                  <a-tooltip title="Chuyển trạng thái">
                    <a-button
                      @click="handleDeleteSubjectFacility(record)"
                      type="text"
                      class="btn-outline-warning"
                    >
                      <SyncOutlined />
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
        <a-form-item label="Cơ Sở">
          <a-select
            v-model:value="newSubjectFacility.facilityId"
            placeholder="Chọn cơ sở"
            allowClear
            mode="multiple"
          >
            <a-select-option
              :value="null"
              :disabled="
                Array.isArray(newSubjectFacility.facilityId) &&
                newSubjectFacility.facilityId.length > 0 &&
                newSubjectFacility.facilityId.some((v) => v !== null)
              "
            >
              Tất cả cơ sở
            </a-select-option>

            <a-select-option
              v-for="f in facilitySubject"
              :key="f.id"
              :value="f.id"
              :disabled="
                Array.isArray(newSubjectFacility.facilityId) &&
                newSubjectFacility.facilityId.includes(null)
              "
            >
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
          {{ detailSubjectFacility.subject?.name }}
        </a-descriptions-item>
        <a-descriptions-item label="Tên cơ sở">
          {{ detailSubjectFacility.facility?.name }}
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
      @ok="modalUpdate = false"
      :okButtonProps="{ loading: loadingStore.isLoading }"
    >
      <p>Chức năng đang được phát triển</p>
    </a-modal>
  </div>
</template>
