<script setup>
import useBreadcrumbStore from '@/stores/useBreadCrumbStore'
import useLoadingStore from '@/stores/useLoadingStore'
import { ref, reactive, onMounted, watch } from 'vue'
import { DEFAULT_PAGINATION, DEFAULT_DATE_FORMAT } from '@/constants'
import { autoAddColumnWidth, formatDate, debounce } from '@/utils/utils'
import { GLOBAL_ROUTE_NAMES } from '@/constants/routesConstant'
import { ROUTE_NAMES } from '@/router/staffRoute'
import requestAPI from '@/services/requestApiService'
import { API_ROUTES_STAFF } from '@/constants/staffConstant'
import { message } from 'ant-design-vue'
import { FilterFilled, SearchOutlined, UnorderedListOutlined } from '@ant-design/icons-vue'

const breadcrumbStore = useBreadcrumbStore()
const loadingStore = useLoadingStore()
const isLoading = ref(false)

const breadcrumb = ref([
  {
    name: GLOBAL_ROUTE_NAMES.STAFF_PAGE,
    breadcrumbName: 'Phụ trách xưởng',
  },
  {
    name: ROUTE_NAMES.MANAGEMENT_USER_LOG_ACTIVITY,
    breadcrumbName: 'Lịch sử hoạt động',
  },
])

const countFilter = ref(0)

const columns = ref(
  autoAddColumnWidth([
    { title: '#', dataIndex: 'rowNumber', key: 'rowNumber', width: 80 },
    {
      title: 'Mã người dùng',
      dataIndex: 'userCode',
      key: 'userCode',
      width: 150,
    },
    {
      title: 'Tên người dùng',
      dataIndex: 'userName',
      key: 'userName',
      width: 200,
    },
    {
      title: 'Vai trò',
      dataIndex: 'role',
      key: 'role',
      width: 120,
    },
    {
      title: 'Thời gian',
      dataIndex: 'createdAt',
      key: 'createdAt',
      width: 180,
      customRender: ({ text }) => formatDate(text, DEFAULT_DATE_FORMAT.DATE_TIME_FORMAT),
    },
    {
      title: 'Hành động',
      dataIndex: 'message',
      key: 'message',
      width: 300,
    },
  ]),
)

const pagination = ref({
  ...DEFAULT_PAGINATION,
})

const dataFilter = reactive({
  searchQuery: '',
  role: '',
  userId: '',
  dateRange: null, // Add date range field for the date picker component
})

const list = ref([])

const getList = async () => {
  loadingStore.show()
  try {
    const response = await requestAPI.get(API_ROUTES_STAFF.FETCH_DATA_USER_LOG_ACTIVITY, {
      params: {
        page: pagination.value.current,
        size: pagination.value.pageSize,
        searchQuery: dataFilter.searchQuery,
        userId: dataFilter.userId,
        fromDate: dataFilter.fromDate, // Add fromDate parameter
        toDate: dataFilter.toDate, // Add toDate parameter
      },
    })

    // FIX 3: Đảm bảo dữ liệu được gán đúng cách và thêm key cho mỗi row
    const responseData = response.data.data.data || []
    list.value = responseData.map((item, index) => ({
      ...item,
      key: item.id || index, // Đảm bảo mỗi row có key unique
    }))

    // FIX 4: Tính toán pagination đúng cách
    pagination.value.total = response.data.data.totalPages * pagination.value.pageSize
    countFilter.value = response.data.data.totalItems
  } catch (error) {
    message.error(error?.response?.data?.message || 'Lỗi khi tải danh sách')
  } finally {
    loadingStore.hide()
  }
}

const staff = ref([])
const fetchStaff = async () => {
  try {
    const response = await requestAPI.get(
      API_ROUTES_STAFF.FETCH_DATA_USER_LOG_ACTIVITY + '/user-staff',
    )
    staff.value = response.data.data
  } catch (error) {
    message.error(error?.response?.data?.message || 'Lỗi khi tải danh sách')
  }
}

const handleSubmitFilter = () => {
  pagination.value.current = 1
  getList()
}

const handleTableChange = (page) => {
  pagination.value.current = page.current
  pagination.value.pageSize = page.pageSize
  dataFilter.pageSize = page.pageSize
  getList()
}

const handleClearFilter = () => {
  Object.assign(dataFilter, {
    searchQuery: '',
    role: '',
    userId: '',
    dateRange: null, // Clear dateRange
    fromDate: null, // Clear fromDate
    toDate: null, // Clear toDate
  })
  handleSubmitFilter()
}

// Hàm xử lý khi thay đổi khoảng ngày
const handleDateRangeChange = (range) => {
  if (range && range.length === 2) {
    dataFilter.dateRange = range
    dataFilter.fromDate = range[0].valueOf()
    dataFilter.toDate = range[1].valueOf()
  } else {
    dataFilter.dateRange = null
    dataFilter.fromDate = null
    dataFilter.toDate = null
  }
  handleSubmitFilter()
}

onMounted(() => {
  breadcrumbStore.setRoutes(breadcrumb.value)
  fetchStaff()
  getList()
})

const debounceFilter = debounce(handleSubmitFilter, 300) // Tăng debounce time
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
        <a-card :bordered="false" class="cart no-body-padding">
          <a-collapse ghost>
            <a-collapse-panel>
              <template #header><FilterFilled /> Bộ lọc ({{ countFilter }})</template>
              <div class="row g-3">
                <div class="col-lg-8 col-md-12 col-sm-12">
                  <div class="label-title">Từ khoá:</div>
                  <a-input
                    v-model:value="dataFilter.searchQuery"
                    placeholder="Nhập mã, tên hoặc hành động"
                    allowClear
                  >
                    <template #prefix>
                      <SearchOutlined />
                    </template>
                  </a-input>
                </div>
                <div class="col-lg-4 col-md-12 col-sm-12">
                  <div class="label-title">Khoảng ngày:</div>
                  <a-range-picker
                    v-model:value="dataFilter.dateRange"
                    class="w-100"
                    format="DD/MM/YYYY"
                    @change="handleDateRangeChange"
                  />
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
          <template #title><UnorderedListOutlined /> Danh sách Lịch sử hoạt động</template>
          <div>
            <a-table
              rowKey="id"
              class="nowrap"
              :dataSource="list"
              :columns="columns"
              :loading="isLoading"
              :pagination="pagination"
              :scroll="{ x: 1000 }"
              @change="handleTableChange"
            >
              <template #bodyCell="{ column, record, index }">
                <template v-if="column.dataIndex === 'rowNumber'">
                  {{ (pagination.current - 1) * pagination.pageSize + index + 1 }}
                </template>
                <template v-else-if="column.dataIndex === 'role'">
                  <a-tag v-if="record.role === 1" color="blue"> Phụ trách xưởng </a-tag>
                </template>
              </template>
            </a-table>
          </div>
        </a-card>
      </div>
    </div>
  </div>
</template>
