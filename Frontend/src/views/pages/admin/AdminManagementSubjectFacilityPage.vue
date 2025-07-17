<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { message, Modal } from 'ant-design-vue'
import {
  PlusOutlined,
  UnorderedListOutlined,
  FilterFilled,
  EyeFilled,
  EditFilled,
  SearchOutlined,
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

const idSubject = route.query.subjectId
const loadingStore = useLoadingStore()
const breadcrumbStore = useBreadcrumbStore()

const countFilter = ref(0)

const breadcrumb = ref([
  {
    name: GLOBAL_ROUTE_NAMES.ADMIN_PAGE,
    breadcrumbName: 'Admin',
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
  name: null,
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
    { title: '#', key: 'rowNumber' },
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
    .post(`${API_ROUTES_ADMIN.FETCH_DATA_SUBJECT_FACILITY}/list`, {
      ...filter,
      page: pagination.current,
      size: pagination.pageSize,
    })
    .then((response) => {
      const result = response.data.data
      subjectFacility.value = result.data
      pagination.total = result.totalPages * pagination.pageSize
      countFilter.value = result.totalItems
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

const fetchFacilitySubjectCombobox = () => {
  loadingStore.show()
  requestAPI
    .get(`${API_ROUTES_ADMIN.FETCH_DATA_SUBJECT_FACILITY}/facility-combobox/${idSubject}`, {
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

const fetchFacility = () => {
  loadingStore.show()
  requestAPI
    .get(`${API_ROUTES_ADMIN.FETCH_DATA_SUBJECT_FACILITY}/facilities`)
    .then((response) => {
      facility.value = response.data.data
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
  newSubjectFacility.facilityId = []
}

const handleAddSubjectFacility = () => {
  if (!newSubjectFacility.subjectId) {
    message.error('Vui lòng chọn bộ môn')
    return
  }
  if (!newSubjectFacility.facilityId || newSubjectFacility.facilityId.length === 0) {
    message.error('Vui lòng chọn cơ sở')
    return
  }

  Modal.confirm({
    title: 'Xác nhận thêm mới',
    content: `Bạn có chắc chắn muốn thêm bộ môn này vào ${newSubjectFacility.facilityId.length} cơ sở?`,
    okText: 'Tiếp tục',
    cancelText: 'Hủy bỏ',
    onOk() {
      loadingStore.show()

      // Tạo mảng promises để gọi API từng cơ sở một
      const addPromises = newSubjectFacility.facilityId.map((facilityId) => {
        const requestData = {
          subjectId: newSubjectFacility.subjectId,
          name: newSubjectFacility.name,
          facilityId: facilityId,
        }
        return requestAPI.post(API_ROUTES_ADMIN.FETCH_DATA_SUBJECT_FACILITY, requestData)
      })

      // Thực hiện tất cả các API call
      Promise.allSettled(addPromises)
        .then((results) => {
          const successful = results.filter((result) => result.status === 'fulfilled').length
          const failed = results.filter((result) => result.status === 'rejected').length

          if (successful > 0) {
            message.success(`Thêm thành công ${successful} bộ môn cơ sở`)
          }
          if (failed > 0) {
            message.warning(`${failed} bộ môn cơ sở thêm thất bại`)
          }

          modalAdd.value = false
          fetchSubjectFacility()
          clearFormAdd()
        })
        .catch((error) => {
          message.error('Có lỗi xảy ra khi thêm bộ môn cơ sở')
        })
        .finally(() => {
          loadingStore.hide()
        })
    },
  })
}

const handleUpdateSubjectFacility = () => {
  if (!detailSubjectFacility.subjectId) {
    message.error('Vui lòng chọn bộ môn')
    return
  }
  if (!detailSubjectFacility.facilityId) {
    message.error('Vui lòng chọn cơ sở')
    return
  }
  Modal.confirm({
    title: 'Xác nhận cập nhật',
    content: 'Bạn có chắc chắn muốn cập nhật thông tin bộ môn cơ sở này?',
    okText: 'Tiếp tục',
    cancelText: 'Hủy bỏ',
    onOk() {
      loadingStore.show()
      const requestData = {
        id: detailSubjectFacility.id,
        subjectId: detailSubjectFacility.subjectId,
        facilityId: detailSubjectFacility.facilityId,
      }

      requestAPI
        .put(
          `${API_ROUTES_ADMIN.FETCH_DATA_SUBJECT_FACILITY}/${detailSubjectFacility.id}`,
          requestData,
        )
        .then((response) => {
          message.success(response.data.message || 'Cập nhật bộ môn cơ sở thành công')
          modalUpdate.value = false
          fetchSubjectFacility()
        })
        .catch((error) => {
          message.error(error.response?.data?.message || 'Lỗi khi cập nhật bộ môn cơ sở')
        })
        .finally(() => {
          loadingStore.hide()
        })
    },
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

const clearFormAdd = () => {
  newSubjectFacility.facilityId = []
}

const handleFacilityChange = (selectedValues) => {
  // Nếu vừa chọn "Tất cả", thì chọn tất cả các cơ sở (không hiển thị "all" trong combobox)
  if (selectedValues.includes('all')) {
    const allFacilityIds = facilitySubject.value.map((f) => f.id)
    newSubjectFacility.facilityId = allFacilityIds
  } else {
    // Nếu không chọn "Tất cả", chỉ giữ lại các cơ sở được chọn
    newSubjectFacility.facilityId = selectedValues
  }
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
  fetchFacilitySubjectCombobox()
  fetchFacility()
})
</script>

<template>
  <div class="container-fluid">
    <!-- Card Bộ lọc tìm kiếm -->
    <div class="row g-3">
      <div class="col-12">
        <a-card :bordered="false" class="cart no-body-padding">
          <a-collapse ghost>
            <a-collapse-panel>
              <template #header><FilterFilled /> Bộ lọc ({{ countFilter }})</template>
              <div class="row g-3 filter-container">
                <div class="col-xl-6 col-md-6 col-sm-12">
                  <div class="label-title">Cơ sở:</div>
                  <a-select
                    v-model:value="filter.facilityId"
                    placeholder="Chọn cơ sở"
                    allowClear
                    class="w-100"
                    @change="fetchSubjectFacility"
                  >
                    <a-select-option :value="null">-- Tất cả cơ sở --</a-select-option>
                    <a-select-option v-for="item in facility" :key="item.id" :value="item.id">
                      {{ item.name }}
                    </a-select-option>
                  </a-select>
                </div>
                <div class="col-xl-6 col-md-6 col-sm-12">
                  <div class="label-title">Trạng thái:</div>
                  <a-select
                    v-model:value="filter.status"
                    placeholder="-- Tất cả trạng thái --"
                    class="w-100"
                    @change="fetchSubjectFacility"
                  >
                    <a-select-option :value="null">-- Tất cả trạng thái --</a-select-option>
                    <a-select-option value="1">Hoạt động</a-select-option>
                    <a-select-option value="0">Không hoạt động</a-select-option>
                  </a-select>
                </div>

                <div class="col-12">
                  <div class="d-flex justify-content-center flex-wrap gap-2">
                    <a-button class="btn-light" @click="fetchSubjectFacility">
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

      <!-- Card Danh sách bộ môn cơ sở -->
      <div class="col-12">
        <a-card :bordered="false" class="cart">
          <template #title> <UnorderedListOutlined /> Danh sách bộ môn cơ sở </template>

          <div class="d-flex justify-content-end mb-2">
            <a-tooltip title="Thêm bộ môn cơ sở">
              <a-button type="primary" @click="showAddModal">
                <PlusOutlined /> Thêm bộ môn cơ sở
              </a-button>
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
            :scroll="{ x: 'auto' }"
          >
            <template #bodyCell="{ column, record, index }">
              <template v-if="column.key === 'rowNumber'">
                {{ (pagination.current - 1) * pagination.pageSize + index + 1 }}
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
                  <!-- <a-tooltip title="Sửa">
                    <a-button
                      @click="handleUpdateProject(record)"
                      type="text"
                      class="btn-outline-info me-2"
                    >
                      <EditFilled />
                    </a-button>
                  </a-tooltip> -->
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
            mode="multiple"
            placeholder="Chọn cơ sở"
            allowClear
            @change="handleFacilityChange"
          >
            <a-select-option value="all">-- Tất cả --</a-select-option>
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
