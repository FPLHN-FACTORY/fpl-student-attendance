<script setup>
import useBreadcrumbStore from '@/stores/useBreadCrumbStore';
import useLoadingStore from '@/stores/useLoadingStore';
import { ref, reactive, onMounted } from 'vue';
import { DEFAULT_PAGINATION, DEFAULT_DATE_FORMAT } from '@/constants';
import { autoAddColumnWidth, formatDate } from '@/utils/utils';
import { GLOBAL_ROUTE_NAMES } from '@/constants/routesConstant'
import { ROUTE_NAMES } from '@/router/staffRoute';
import requestAPI from '@/services/requestApiService';
import { API_ROUTES_STAFF } from '@/constants/staffConstant';
import { message } from 'ant-design-vue';
import { EditFilled, FilterFilled, PlusOutlined, SearchOutlined, UnorderedListOutlined } from '@ant-design/icons-vue';

const breadcrumbStore = useBreadcrumbStore();
const loadingStore = useLoadingStore();
const isLoading = ref(false);

const breadcrumb = ref([
  {
    name: GLOBAL_ROUTE_NAMES.STAFF_PAGE,
    breadcrumbName: 'Phụ trách xưởng',
  },
  {
    name: ROUTE_NAMES.MANAGEMENT_ATTENDANCE_RECOVERY,
    breadcrumbName: 'Khôi phục điểm danh',
  },
])

const filter = reactive({
  searchQuery: '',
  fromDate: null,
  toDate: null,
  dateRange: null,
  semesterId: null,
});

const pagination = reactive({
  ...DEFAULT_PAGINATION,
});

const columns = ref(
  autoAddColumnWidth([
    { title: '#', dataIndex: 'rowNumber', key: 'rowNumber' },
    { title: 'Tên hoạt động', dataIndex: 'name', key: 'name' },
    { title: 'Mô tả', dataIndex: 'description', key: 'description' },
    { title: 'Ngày', dataIndex: 'dayHappen', key: 'dayHappen' },
    { title: 'Chức năng', key: 'action' },
  ])
)

const attendanceRecovery = ref([]);
const fetchAttendanceRecovery = () => {
  loadingStore.show()
  requestAPI
    .get(API_ROUTES_STAFF.FETCH_DATA_ATTENDANCE_RECOVERY, {
      params: {
        searchQuery: filter.searchQuery,
        fromDate: filter.fromDate,
        toDate: filter.toDate,
        // semesterId: filter.semesterId,
        page: pagination.current,
        size: pagination.pageSize,
      },
    })
    .then((response) => {
      const result = response.data.data
      attendanceRecovery.value = result.data
      if (result.totalRecords !== undefined) {
        pagination.total = result.totalRecords
      } else {
        pagination.total = result.totalPages * pagination.pageSize
      }
    })
    .catch((error) => {
      message.error(error.response?.data?.message || 'Lỗi khi lấy danh sách khôi phục điểm danh')
    })
    .finally(() => {
      loadingStore.hide()
    })
}

const handleTableChange = (pageInfo) => {
  pagination.current = pageInfo.current
  pagination.pageSize = pageInfo.pageSize
  fetchAttendanceRecovery()
}

const semester = ref([]);
const fetchSemester = () => {
  loadingStore.show()
  requestAPI
  .get(API_ROUTES_STAFF.FETCH_DATA_ATTENDANCE_RECOVERY + '/semesters')
  .then((response) => {
    semester.value = response.data.data
  })
  .catch((error) => {
    message.error(error.response?.data?.message || 'Lỗi khi lấy danh sách kỳ học')
  })
  .finally(() => {
    loadingStore.hide()
  })
}

const handleDateRangeChange = (range) => {
  if (range && range.length === 2) {
    filter.dateRange = range
    filter.fromDate = range[0].valueOf()
    filter.toDate = range[1].valueOf()
  } else {
    filter.dateRange = null
    filter.fromDate = null
    filter.toDate = null
  }
  pagination.current = 1
  fetchAttendanceRecovery()
}

const handleClearFilter = () => {
  Object.keys(filter).forEach((key) => {
    filter[key] = ''
  })
  pagination.current = 1
  fetchAttendanceRecovery()
}

const modalAddEvent = ref(false);
const modalAddLoading = ref(false)

const newEvent = reactive({
  name: '',
  description: '',
  dayHappen: null,
  semesterId: null,
})
const clearData = () => {
  newEvent.name = ''
  newEvent.description = ''
  newEvent.dayHappen = null
  newEvent.semesterId = null
  modalAddEvent.value = false
}
const handleShowModalAdd = () => {
  newEvent.name = ''
  newEvent.description = ''
  newEvent.dayHappen = null
  newEvent.semesterId = null
  modalAddEvent.value = true
}

const handleAddEvent = () => {
  if(!newEvent.name || !newEvent.dayHappen) {
    message.error('Vui lòng nhập đầy đủ thông tin')
    return
  }
  modalAddLoading.value = true
  loadingStore.show()
  const payload = {
    name: newEvent.name,
    description: newEvent.description,
    day: newEvent.dayHappen.valueOf(),
  }
  requestAPI.post(API_ROUTES_STAFF.FETCH_DATA_ATTENDANCE_RECOVERY, payload)
    .then(() => {
      message.success('Thêm sự kiện khôi phục điểm danh thành công')
      clearData()
      fetchAttendanceRecovery()
    })
    .catch((error) => {
      message.error(error.response?.data?.message || 'Lỗi khi thêm sự kiện khôi phục điểm danh')
    })
    .finally(() => {
      modalAddLoading.value = false
      loadingStore.hide()
    })
}

const modalEditEvent = ref(false)
const modalUpdateLoading = ref(false)
const editEvent = reactive({
  id: null,
  name: '',
  description: '',
  dayHappen: null,
})
const handleShowModalEdit = (record) => {

}
const handleEditEvent = () => {
  console.log('handleEditEvent')
}

onMounted(() => {
  breadcrumbStore.setRoutes(breadcrumb.value)
  fetchAttendanceRecovery()
  fetchSemester()
})
</script>
<template>

  <!-- Danh sách sự kiện khôi phục điểm danh -->
  <div class="container-fluid">
    <div class="row g-3">
      <div class="col-12">
        <a-card :bordered="false" class="cart mb-3">
          <template #title> <FilterFilled /> Bộ lọc </template>
          <div class="row g-2">
            <div class="col-md-6 col-sm-12">
              <div class="label-title">Từ khoá:</div>
              <a-input
                v-model:value="filter.searchQuery"
                placeholder="Tìm theo tên hoặc mô tả"
                allowClear
                @change="fetchAttendanceRecovery"
                class="w-100"
              >
                <template #suffix> <SearchOutlined /> </template>
              </a-input>
            </div>
            <!-- <div class="col-md-6 col-sm-12">
              <div class="label-title">Kỳ học:</div>
              <a-select
                v-model:value="filter.semesterId"
                placeholder="Chọn kỳ học"
                allowClear
                @change="fetchAttendanceRecovery"
                :dropdownMatchSelectWidth="false"
                class="w-100"
              >
                <a-select-option v-for="item in semester" :key="item.id" :value="item.id">
                  {{ item.code }}
                </a-select-option>
              </a-select>
            </div> -->
            <div class="col-md-6 col-sm-12">
              <div class="label-title">Khoảng ngày:</div>
              <a-range-picker
                v-model:value="filter.dateRange"
                class="w-100"
                :format="DEFAULT_DATE_FORMAT"
                @change="handleDateRangeChange"
              />
            </div>
          </div>
          <div class="row">
            <div class="col-12">
              <div class="d-flex justify-content-center flex-wrap gap-2 mt-3">
                <a-button class="btn-light" @click="fetchAttendanceRecovery">
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
          <template #title> <UnorderedListOutlined /> Danh sách sự kiện khôi phục điểm danh </template>
          <div class="d-flex justify-content-end mb-3 flex-wrap gap-3">
              <a-tooltip title="Thêm Sự kiện khôi phục điểm danh">
                <a-button type="primary" @click="handleShowModalAdd">
                  <PlusOutlined /> Thêm
                </a-button>
              </a-tooltip>
          </div>
          <a-table
            class="nowrap"
            rowKey="id"
            :dataSource="attendanceRecovery"
            :columns="columns"
            :pagination="pagination"
            @change="handleTableChange"
            :loading="isLoading"
            :scroll="{ x: 'auto' }"
          >
            <template #bodyCell="{ column, record, index }">
              <template v-if="column.dataIndex">
                <template v-if="column.dataIndex === 'rowNumber'">
                  {{ (pagination.current - 1) * pagination.pageSize + index + 1 }}
                </template>
                <template v-else-if="column.dataIndex === 'dayHappen'">
                  {{ formatDate(record.dayHappen, DEFAULT_DATE_FORMAT) }}
                </template>
                <template v-else>
                  {{ record[column.dataIndex] }}
                </template>
              </template>
              <template v-else-if="column.key === 'action'">
                <a-tooltip title="Sửa thông tin khôi phục điểm danh">
                  <a-button type="text" class="btn-outline-info me-2" @click="handleEdit(record)">
                    <EditFilled />
                  </a-button>
                </a-tooltip>
              </template>
            </template>
          </a-table>
        </a-card>
      </div>
    </div>
  </div>

  <!-- Modal Thêm sự kiện khôi phục điểm danh -->
  <a-modal
  v-model:open="modalAddEvent"
   title="Thêm sự kiện khôi phục điểm danh"
   :okButtonProps="{ loading: modalAddLoading }"
   @ok="handleAddEvent"
   @cancel="clearData"
   @close="clearData">

    <a-form :model="newEvent" layout="vertical">
      <a-form-item label="Tên sự kiện" required>
        <a-input v-model:value="newEvent.name" placeholder="Nhập tên sự kiện" />
      </a-form-item>
      <a-form-item label="Mô tả">
        <a-textarea v-model:value="newEvent.description" placeholder="Nhập mô tả" />
      </a-form-item>
      <a-form-item label="Ngày" required>
        <a-date-picker v-model:value="newEvent.dayHappen" placeholder="Chọn ngày" :format="DEFAULT_DATE_FORMAT" class="w-100" />
      </a-form-item>
    </a-form>

  </a-modal>

  <!-- Modal Sửa sự kiện khôi phục điểm danh -->
  <a-modal v-model:open="modalEditEvent"
  title="Sửa sự kiện khôi phục điểm danh"
  :okButtonProps="{ loading: modalUpdateLoading }"
  @ok="handleEditEvent"
  @cancel="clearData"
  @close="clearData">

    <a-form :model="newEvent" layout="vertical">
      <a-form-item label="Tên sự kiện" required></a-form-item>
    </a-form>

  </a-modal>

</template>

