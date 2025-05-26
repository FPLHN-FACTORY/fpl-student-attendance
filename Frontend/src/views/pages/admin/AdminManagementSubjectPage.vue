<script setup>
import { ref, reactive, onMounted } from 'vue'
import { message, Modal } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import requestAPI from '@/services/requestApiService'
import {
  PlusOutlined,
  ClusterOutlined,
  UnorderedListOutlined,
  FilterFilled,
  EditFilled,
  EyeFilled,
  SearchOutlined,
} from '@ant-design/icons-vue'
import { DEFAULT_PAGINATION, STATUS_TYPE } from '@/constants'
import useBreadcrumbStore from '@/stores/useBreadCrumbStore'
import useLoadingStore from '@/stores/useLoadingStore'
import { API_ROUTES_ADMIN } from '@/constants/adminConstant'
import { ROUTE_NAMES } from '@/router/adminRoute'
import { GLOBAL_ROUTE_NAMES } from '@/constants/routesConstant'
import { autoAddColumnWidth } from '@/utils/utils'

const router = useRouter()
const breadcrumbStore = useBreadcrumbStore()
const loadingStore = useLoadingStore()

/* ----------------- Data & Reactive Variables ----------------- */
const subjects = ref([])

const filter = reactive({
  name: '',
  status: null,
})

const pagination = reactive({
  ...DEFAULT_PAGINATION,
})

const modalAdd = ref(false)
const modalUpdate = ref(false)
const modalDetail = ref(false)

const newSubject = reactive({
  name: '',
  code: '',
})

const detailSubject = reactive({
  id: '',
  name: '',
  code: '',
  status: 1,
  createdAt: '',
  updatedAt: '',
  sizeSubjectSemester: 0,
})

const columns = ref(
  autoAddColumnWidth([
    { title: '#', dataIndex: 'orderNumber', key: 'orderNumber' },
    { title: 'Tên bộ môn', dataIndex: 'name', key: 'name' },
    { title: 'Mã bộ môn', dataIndex: 'code', key: 'code' },
    {
      title: 'Cơ sở hoạt động',
      dataIndex: 'sizeSubjectSemester',
      key: 'sizeSubjectSemester',
    },
    { title: 'Trạng thái', dataIndex: 'status', key: 'status' },
    { title: 'Chức năng', key: 'actions' },
  ]),
)

const breadcrumb = ref([
  {
    name: GLOBAL_ROUTE_NAMES.ADMIN_PAGE,
    breadcrumbName: 'Admin',
  },
  {
    name: ROUTE_NAMES.MANAGEMENT_SUBJECT,
    breadcrumbName: 'Quản lý bộ môn',
  },
])

/* ----------------- Methods ----------------- */
const fetchSubjects = () => {
  loadingStore.show()
  requestAPI
    .get(`${API_ROUTES_ADMIN.FETCH_DATA_SUBJECT}/list`, {
      params: {
        ...filter,
        page: pagination.current,
        size: pagination.pageSize,
      },
    })
    .then((response) => {
      const result = response.data.data
      subjects.value = result.data
      pagination.total =
        result.totalElements || result.totalItems || result.totalPages * pagination.pageSize
    })
    .catch((error) => {
      message.error(error.response?.data?.message || 'Lỗi khi lấy danh sách bộ môn')
    })
    .finally(() => {
      loadingStore.hide()
    })
}

const handleTableChange = (pageInfo) => {
  pagination.current = pageInfo.current
  pagination.pageSize = pageInfo.pageSize
  fetchSubjects()
}

const showAddModal = (isOpen) => {
  newSubject.code = null
  newSubject.name = null
  modalAdd.value = isOpen
}

const handleAddSubject = () => {
  if (!newSubject.name || !newSubject.name.trim()) {
    message.error('Vui lòng nhập tên bộ môn')
    return
  }
  if (!newSubject.code || !newSubject.code.trim()) {
    message.error('Vui lòng nhập mã bộ môn')
    return
  }
  loadingStore.show()
  requestAPI
    .post(API_ROUTES_ADMIN.FETCH_DATA_SUBJECT, newSubject)
    .then((response) => {
      message.success(response.data.message || 'Thêm bộ môn thành công')
      modalAdd.value = false
      fetchSubjects()
      clearFormAdd()
    })
    .catch((error) => {
      message.error(error.response?.data?.message || 'Lỗi khi thêm bộ môn')
    })
    .finally(() => {
      loadingStore.hide()
    })
}

const clearFormAdd = () => {
  newSubject.name = ''
  newSubject.code = ''
}

const handleDetailSubject = (record) => {
  loadingStore.show()
  requestAPI
    .get(`${API_ROUTES_ADMIN.FETCH_DATA_SUBJECT}/${record.id}`)
    .then((response) => {
      Object.assign(detailSubject, response.data.data)
      detailSubject.sizeSubjectSemester = record.sizeSubjectSemester
      modalDetail.value = true
    })
    .catch((error) => {
      message.error(error.response?.data?.message || 'Lỗi khi lấy chi tiết bộ môn')
    })
    .finally(() => {
      loadingStore.hide()
    })
}

const handleUpdateSubject = (record) => {
  loadingStore.show()
  requestAPI
    .get(`${API_ROUTES_ADMIN.FETCH_DATA_SUBJECT}/${record.id}`)
    .then((response) => {
      Object.assign(detailSubject, response.data.data)
      modalUpdate.value = true
    })
    .catch((error) => {
      message.error(error.response?.data?.message || 'Lỗi khi lấy thông tin bộ môn')
    })
    .finally(() => {
      loadingStore.hide()
    })
}

const updateSubject = () => {
  if (!detailSubject.name) {
    message.error('Vui lòng nhập tên bộ môn')
    return
  }
  if (!detailSubject.code) {
    message.error('Vui lòng nhập mã bộ môn')
    return
  }
  loadingStore.show()
  const requestData = {
    id: detailSubject.id,
    name: detailSubject.name,
    code: detailSubject.code,
  }

  requestAPI
    .put(`${API_ROUTES_ADMIN.FETCH_DATA_SUBJECT}/${detailSubject.id}`, requestData)
    .then((response) => {
      message.success(response.data.message || 'Cập nhật bộ môn thành công')
      modalUpdate.value = false
      fetchSubjects()
    })
    .catch((error) => {
      message.error(error.response?.data?.message || 'Lỗi khi cập nhật bộ môn')
    })
    .finally(() => {
      loadingStore.hide()
    })
}

const confirmChangeStatus = (record) => {
  Modal.confirm({
    title: 'Xác nhận',
    content: `Bạn có chắc chắn muốn chuyển trạng thái bộ môn ${record.name}?`,
    onOk: () => {
      loadingStore.show()
      requestAPI
        .delete(`${API_ROUTES_ADMIN.FETCH_DATA_SUBJECT}/${record.id}`)
        .then((response) => {
          message.success(response.data.message || ' thành công')
          fetchSubjects()
        })
        .catch((error) => {
          message.error(error.response?.data?.message || 'Lỗi khi chuyển trang thái bộ môn')
        })
        .finally(() => {
          loadingStore.hide()
        })
    },
  })
}

const handleAddSubjectFacility = (record) => {
  router.push({
    name: ROUTE_NAMES.MANAGEMENT_SUBJECT_FACILITY,
    query: { subjectId: record.id, name: record.name },
  })
}

const formatDate = (timestamp) => {
  if (!timestamp) return 'Không xác định'
  return new Date(timestamp).toLocaleString()
}

const getStatusText = (status) => {
  return status === 'ACTIVE' ? 'Hoạt động' : 'Không hoạt động'
}

const getStatusColor = (status) => {
  return status === 'ACTIVE' ? 'green' : 'red'
}

const handleClearFilter = () => {
  // Clear all filter values
  Object.keys(filter).forEach((key) => {
    filter[key] = ''
  })
  pagination.current = 1
  fetchSubjects()
}

onMounted(() => {
  breadcrumbStore.setRoutes(breadcrumb.value)
  fetchSubjects()
})
</script>

<template>
  <div class="container-fluid">
    <!-- Card Bộ lọc tìm kiếm -->
    <div class="row g-3">
      <div class="col-12">
        <a-card :bordered="false" class="cart mb-3">
          <template #title> <FilterFilled /> Bộ lọc</template>
          <div class="row g-3 filter-container">
            <div class="col-md-8 col-sm-6">
              <div class="label-title">Từ khoá:</div>
              <a-input
                v-model:value="filter.name"
                placeholder="Nhập tên hoặc mã bộ môn"
                allowClear
                @change="fetchSubjects"
              >
                <template #prefix>
                  <SearchOutlined />
                </template>
              </a-input>
            </div>
            <div class="col-md-4 col-sm-6">
              <div class="label-title">Trạng thái:</div>
              <a-select
                v-model:value="filter.status"
                placeholder="Chọn trạng thái"
                allowClear
                class="w-100"
                @change="fetchSubjects"
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
                <a-button class="btn-light" @click="fetchSubjects"> <FilterFilled /> Lọc </a-button>
                <a-button class="btn-gray" @click="handleClearFilter"> Huỷ lọc </a-button>
              </div>
            </div>
          </div>
        </a-card>
      </div>

      <div class="col-12">
        <a-card :bordered="false" class="cart">
          <template #title> <UnorderedListOutlined /> Danh sách bộ môn </template>
          <div class="d-flex justify-content-end mb-3">
            <a-tooltip title="Thêm bộ môn">
              <a-button type="primary" @click="showAddModal(true)">
                <PlusOutlined /> Thêm mới
              </a-button>
            </a-tooltip>
          </div>
          <a-table
            class="nowrap"
            :dataSource="subjects"
            :columns="columns"
            rowKey="id"
            :pagination="pagination"
            @change="handleTableChange"
            :loading="loadingStore.isLoading"
            :scroll="{ x: 'auto' }"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.dataIndex === 'name'">
                <a @click="handleAddSubjectFacility(record)">{{ record.name }}</a>
              </template>
              <template v-else-if="column.dataIndex === 'status'">
                <span class="nowrap">
                  <a-switch
                    class="me-2"
                    :checked="record.status === 'ACTIVE' || record.status === 1"
                    @change="confirmChangeStatus(record)"
                  />
                  <a-tag
                    :color="record.status === 'ACTIVE' || record.status === 1 ? 'green' : 'red'"
                  >
                    {{
                      record.status === 'ACTIVE' || record.status === 1
                        ? 'Hoạt động'
                        : 'Không hoạt động'
                    }}
                  </a-tag>
                </span>
              </template>
              <template v-else>
                {{ record[column.dataIndex] }}
              </template>
              <template v-if="column.key === 'actions'">
                <a-space>
                  <a-tooltip title="Bộ môn cơ sở" v-if="record.status === STATUS_TYPE.ENABLE">
                    <a-button
                      @click="handleAddSubjectFacility(record)"
                      type="text"
                      class="btn-outline-default me-2"
                    >
                      <ClusterOutlined />
                    </a-button>
                  </a-tooltip>
                  <a-tooltip title="Xem chi tiết">
                    <a-button
                      @click="handleDetailSubject(record)"
                      type="text"
                      class="btn-outline-primary me-2"
                    >
                      <EyeFilled />
                    </a-button>
                  </a-tooltip>
                  <a-tooltip title="Sửa bộ môn">
                    <a-button
                      @click="handleUpdateSubject(record)"
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

    <!-- Modal Thêm bộ môn -->
    <a-modal
      v-model:open="modalAdd"
      title="Thêm bộ môn"
      @ok="handleAddSubject"
      :okButtonProps="{ loading: loadingStore.isLoading }"
      @cancel="clearFormAdd"
      @close="clearFormAdd"
    >
      <a-form layout="vertical">
        <a-form-item label="Mã bộ môn" required>
          <a-input v-model:value="newSubject.code" placeholder="Nhập mã bộ môn" />
        </a-form-item>
        <a-form-item label="Tên bộ môn" required>
          <a-input v-model:value="newSubject.name" placeholder="Nhập tên bộ môn" />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- Modal Xem chi tiết bộ môn -->
    <a-modal v-model:open="modalDetail" title="Chi tiết bộ môn" :footer="null">
      <a-descriptions bordered :column="1">
        <a-descriptions-item label="Mã">{{ detailSubject.code }}</a-descriptions-item>
        <a-descriptions-item label="Tên">{{ detailSubject.name }}</a-descriptions-item>
        <a-descriptions-item label="Trạng thái">
          <a-tag :color="getStatusColor(detailSubject.status)">
            {{ getStatusText(detailSubject.status) }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="Cơ sở hoạt động">
          {{ detailSubject.sizeSubjectSemester }}
        </a-descriptions-item>
        <a-descriptions-item label="Ngày tạo">
          {{ formatDate(detailSubject.createdAt) }}
        </a-descriptions-item>
        <a-descriptions-item label="Ngày cập nhật">
          {{ formatDate(detailSubject.updatedAt) }}
        </a-descriptions-item>
      </a-descriptions>
    </a-modal>

    <!-- Modal Cập nhật bộ môn -->
    <a-modal
      v-model:open="modalUpdate"
      title="Cập nhật bộ môn"
      @ok="updateSubject"
      :okButtonProps="{ loading: loadingStore.isLoading }"
    >
      <a-form layout="vertical">
        <a-form-item label="Mã bộ môn" required>
          <a-input v-model:value="detailSubject.code" placeholder="Nhập mã bộ môn" />
        </a-form-item>
        <a-form-item label="Tên bộ môn" required>
          <a-input v-model:value="detailSubject.name" placeholder="Nhập tên bộ môn" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>
