<script setup>
import useBreadcrumbStore from '@/stores/useBreadCrumbStore'
import useLoadingStore from '@/stores/useLoadingStore'
import { ref, reactive, onMounted, watch } from 'vue'
import { DEFAULT_PAGINATION, DEFAULT_DATE_FORMAT } from '@/constants'
import { autoAddColumnWidth, formatDate, debounce } from '@/utils/utils'
import { GLOBAL_ROUTE_NAMES } from '@/constants/routesConstant'
import { ROUTE_NAMES } from '@/router/staffRoute'
import requestAPI from '@/services/requestApiService'
import { API_ROUTES_ADMIN } from '@/constants/adminConstant'
import { message } from 'ant-design-vue'
import { FilterFilled, SearchOutlined, UnorderedListOutlined } from '@ant-design/icons-vue'

const breadcrumbStore = useBreadcrumbStore()
const loadingStore = useLoadingStore()
const isLoading = ref(false)

const breadcrumb = ref([
  {
    name: GLOBAL_ROUTE_NAMES.ADMIN_PAGE,
    breadcrumbName: 'Admin',
  },
  {
    name: ROUTE_NAMES.MANAGEMENT_USER_LOG_ACTIVITY,
    breadcrumbName: 'Lịch sử hoạt động',
  },
])

// FIX 1: Loại bỏ autoAddColumnWidth wrapper và đảm bảo columns được định nghĩa đúng
const columns = ref(
  autoAddColumnWidth([
    { title: '#', dataIndex: 'rowNumber', key: 'rowNumber', width: 80 },
    {
      title: 'Mã admin / giảng viên',
      dataIndex: 'userCode',
      key: 'userCode',
      width: 150,
    },
    {
      title: 'Tên admin / giảng viên',
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
      title: 'Cơ sở',
      dataIndex: 'facilityName',
      key: 'facilityName',
      width: 150,
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
  facilityId: '',
  userId: '',
})

const list = ref([])

const getList = async () => {
  loadingStore.show()
  try {
    const response = await requestAPI.get(API_ROUTES_ADMIN.FETCH_DATA_USER_LOG_ACTIVITY, {
      params: {
        page: pagination.value.current,
        size: pagination.value.pageSize,
        searchQuery: dataFilter.searchQuery,
        role: dataFilter.role,
        facilityId: dataFilter.facilityId,
        userId: dataFilter.userId,
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
  } catch (error) {
    console.log(error)
    message.error(error?.response?.data?.message || 'Lỗi khi tải danh sách')
  } finally {
    loadingStore.hide()
  }
}

const staff = ref([])
const fetchStaff = async () => {
  try {
    const response = await requestAPI.get(
      API_ROUTES_ADMIN.FETCH_DATA_USER_LOG_ACTIVITY + '/user-staff',
    )
    staff.value = response.data.data
  } catch (error) {
    console.log(error)
    message.error(error?.response?.data?.message || 'Lỗi khi tải danh sách')
  }
}

const facility = ref([])
const fetchFacility = async () => {
  try {
    const response = await requestAPI.get(
      API_ROUTES_ADMIN.FETCH_DATA_USER_LOG_ACTIVITY + '/facility',
    )
    facility.value = response.data.data
  } catch (error) {
    console.log(error)
    message.error(error?.response?.data?.message || 'Lỗi khi tải danh sách')
  }
}

const admin = ref([])
const fetchAdmin = async () => {
  try {
    const response = await requestAPI.get(
      API_ROUTES_ADMIN.FETCH_DATA_USER_LOG_ACTIVITY + '/user-admin',
    )
    admin.value = response.data.data
  } catch (error) {
    console.log(error)
    message.error(error?.response?.data?.message || 'Lỗi khi tải danh sách')
  }
}

const handleSubmitFilter = () => {
  pagination.value.current = 1
  getList()
}

const handleTableChange = (page) => {
  pagination.value.current = page.current || 1
  pagination.value.pageSize = page.pageSize
  getList()
}

const handleClearFilter = () => {
  Object.assign(dataFilter, {
    searchQuery: '',
    role: '',
    facilityId: '',
    userId: '',
  })
  pagination.value.current = 1
  getList()
}

onMounted(() => {
  breadcrumbStore.setRoutes(breadcrumb.value)
  fetchStaff()
  fetchAdmin()
  fetchFacility()
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
              <template #header><FilterFilled /> Bộ lọc</template>
              <div class="row g-3">
                <div class="col-lg-3 col-md-12 col-sm-12">
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
               
                <div class="col-lg-3 col-md-6 col-sm-6">
                  <div class="label-title">Cơ sở:</div>
                  <a-select
                    v-model:value="dataFilter.facilityId"
                    placeholder="Chọn cơ sở"
                    allowClear
                    class="w-100"
                  >
                    <a-select-option :value="''">--Tất cả cơ sở--</a-select-option>
                    <a-select-option v-for="item in facility" :key="item.id" :value="item.id">
                      {{ item.name }}
                    </a-select-option>
                  </a-select>
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
                  <a-tag v-if="record.role === 1" color="geekblue"> Phụ trách xưởng </a-tag>
                  <a-tag v-else-if="record.role === 3" color="blue"> Giảng viên </a-tag>
                  <a-tag v-else color="purple"> Admin </a-tag>
                </template>
                <template v-else-if="column.dataIndex === 'facilityName'">
                  <a-tag color="default">
                    {{ record.facilityName || 'Tất cả cơ sở' }}
                  </a-tag>
                </template>
              </template>
            </a-table>
          </div>
        </a-card>
      </div>
    </div>
  </div>
</template>
