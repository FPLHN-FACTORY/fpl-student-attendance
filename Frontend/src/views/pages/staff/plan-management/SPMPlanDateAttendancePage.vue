<script setup>
import { ref, onMounted, watch, reactive } from 'vue'
import { FilterFilled, SearchOutlined, UnorderedListOutlined } from '@ant-design/icons-vue'
import { message, Modal } from 'ant-design-vue'
import requestAPI from '@/services/requestApiService'
import { ATTENDANCE_STATUS, DEFAULT_PAGINATION } from '@/constants'
import { API_ROUTES_STAFF } from '@/constants/staffConstant'
import useBreadcrumbStore from '@/stores/useBreadCrumbStore'
import { GLOBAL_ROUTE_NAMES } from '@/constants/routesConstant'
import { ROUTE_NAMES } from '@/router/staffRoute'
import { useRoute, useRouter } from 'vue-router'
import { autoAddColumnWidth, debounce } from '@/utils/utils'
import useLoadingStore from '@/stores/useLoadingStore'

const route = useRoute()
const router = useRouter()

const breadcrumbStore = useBreadcrumbStore()
const loadingStore = useLoadingStore()

const isLoading = ref(false)

const _detail = ref(null)
const lstData = ref([])

const columns = ref(
  autoAddColumnWidth([
    { title: '#', dataIndex: 'orderNumber', key: 'orderNumber' },
    { title: 'Mã sinh viên', dataIndex: 'code', key: 'code' },
    { title: 'Họ và tên', dataIndex: 'name', key: 'name' },
    { title: 'Trạng thái', dataIndex: 'status', key: 'status' },
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
})

const fetchDataDetail = () => {
  loadingStore.show()
  requestAPI
    .get(`${API_ROUTES_STAFF.FETCH_DATA_PLAN_DATE_ATTENDANCE}/${route.params.id}`)
    .then(({ data: response }) => {
      _detail.value = response.data
      breadcrumbStore.push({
        name: ROUTE_NAMES.MANAGEMENT_PLAN_FACTORY,
        params: { id: _detail.value.planId },
        breadcrumbName: _detail.value.planName,
      })
      breadcrumbStore.push({
        name: ROUTE_NAMES.MANAGEMENT_PLAN_DATE,
        params: { id: _detail.value.factoryId },
        breadcrumbName: _detail.value.factoryName,
      })
      breadcrumbStore.push({
        breadcrumbName: 'Chi tiết điểm danh',
      })
      fetchDataList()
    })
    .catch((error) => {
      message.error(error?.response?.data?.message || 'Không thể tải thông tin kế hoạch')
      router.push({ name: ROUTE_NAMES.MANAGEMENT_PLAN_FACTORY, params: { id: route.params?.id } })
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
    .get(`${API_ROUTES_STAFF.FETCH_DATA_PLAN_DATE_ATTENDANCE}/${_detail.value.id}/list`, {
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

const fetchAttendance = (id) => {
  requestAPI
    .put(`${API_ROUTES_STAFF.FETCH_DATA_PLAN_DATE_ATTENDANCE}/change-status`, {
      idUserStudent: id,
      idPlanDate: _detail.value.id,
    })
    .then(({ data: response }) => {
      message.success(response.message)
      fetchDataList()
    })
    .catch((error) => {
      message.error(error?.response?.data?.message || 'Không thể thay đổi trạng thái kế hoạch')
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

const handleSubmitAttendance = async (item) => {
  Modal.confirm({
    title: `${item.code} - ${item.name}`,
    type: 'info',
    content: `Không thể hoàn tác. Bạn thực sự muốn đánh dấu đã điểm danh sinh viên này?`,
    okText: 'Tiếp tục',
    cancelText: 'Hủy bỏ',
    onOk() {
      fetchAttendance(item.id)
    },
  })
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
</script>

<template>
  <div class="container-fluid">
    <div class="row g-3">
      <div class="col-12">
        <!-- Bộ lọc tìm kiếm -->
        <a-card :bordered="false" class="cart">
          <template #title> <FilterFilled /> Bộ lọc </template>
          <div class="row g-2">
            <div class="col-md-6 col-sm-12">
              <div class="label-title">Từ khoá:</div>
              <a-input
                v-model:value="dataFilter.keyword"
                placeholder="Tìm theo tên, mã sinh viên..."
                allowClear
              >
                <template #prefix>
                  <SearchOutlined />
                </template>
              </a-input>
            </div>
            <div class="col-md-6 col-sm-6">
              <div class="label-title">Trạng thái:</div>
              <a-select
                v-model:value="dataFilter.status"
                class="w-100"
                :dropdownMatchSelectWidth="false"
                placeholder="-- Tất cả trạng thái --"
                allowClear
              >
                <a-select-option :value="null">-- Tất cả trạng thái --</a-select-option>
                <a-select-option :value="1">Có mặt</a-select-option>
                <a-select-option :value="0">Vắng mặt</a-select-option>
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
        <a-card :bordered="false" class="cart">
          <template #title> <UnorderedListOutlined /> Chi tiết điểm danh sinh viên </template>
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
              <template #bodyCell="{ column, record }">
                <template v-if="column.dataIndex === 'studentCode'">
                  <a-tag color="purple">
                    {{ record.studentCode }}
                  </a-tag>
                </template>
                <template v-if="column.dataIndex === 'status'">
                  <a-switch
                    class="me-2"
                    :checked="record.status === ATTENDANCE_STATUS.PRESENT.id"
                    :disabled="record.status === ATTENDANCE_STATUS.PRESENT.id"
                    @change="handleSubmitAttendance(record)"
                  />
                  <a-tag color="green" v-if="record.status === ATTENDANCE_STATUS.PRESENT.id">
                    Có mặt
                  </a-tag>
                  <a-tag color="red" v-else> Vắng mặt </a-tag>
                </template>
              </template>
            </a-table>
          </div>
        </a-card>
      </div>
    </div>
  </div>
</template>
