<script setup>
import { FilterFilled, UnorderedListOutlined } from '@ant-design/icons-vue'
import { ref, reactive, onMounted } from 'vue'
import { message, Modal } from 'ant-design-vue'
import { useRoute } from 'vue-router'
import requestAPI from '@/services/requestApiService'
import { API_ROUTES_TEACHER } from '@/constants/teacherConstant'
import { ROUTE_NAMES } from '@/router/teacherRoute'
import { GLOBAL_ROUTE_NAMES } from '@/constants/routesConstant'
import useBreadcrumbStore from '@/stores/useBreadCrumbStore'
import useLoadingStore from '@/stores/useLoadingStore'
import { DEFAULT_PAGINATION } from '@/constants'

const route = useRoute()
const factoryId = route.query.factoryId
const factoryName = route.query.factoryName

const breadcrumbStore = useBreadcrumbStore()
const isLoading = ref(false)

const breadcrumb = ref([
  {
    name: GLOBAL_ROUTE_NAMES.TEACHER_PAGE,
    breadcrumbName: 'Giảng viên',
  },
  {
    name: ROUTE_NAMES.MANAGEMENT_STUDENT,
    breadcrumbName: 'Nhóm xưởng',
  },
  {
    name: ROUTE_NAMES.MANAGEMENT_STUDENT_FACTORY,
    breadcrumbName: 'Sinh viên nhóm: ' + factoryName,
  },
])

// Danh sách học sinh thuộc nhóm xưởng
const students = ref([])
// Filter & phân trang
const filter = reactive({
  searchQuery: '',
  status: '',
  factoryId: factoryId, // từ query string
  page: 1,
  pageSize: 5,
})
const pagination = reactive({
  ...DEFAULT_PAGINATION,
})

// Cấu hình cột cho bảng
const columns = ref([
  { title: '#', dataIndex: 'rowNumber', key: 'rowNumber', width: 50 },
  {
    title: 'Mã học sinh',
    dataIndex: 'studentCode',
    key: 'studentCode',
    width: 150,
    ellipsis: true,
  },
  {
    title: 'Tên học sinh',
    dataIndex: 'studentName',
    key: 'studentName',
    width: 200,
    ellipsis: true,
  },
  { title: 'Email', dataIndex: 'studentEmail', key: 'studentEmail', width: 250, ellipsis: true },
  {
    title: 'Trạng thái',
    dataIndex: 'statusStudentFactory',
    key: 'statusStudentFactory',
    width: 120,
    ellipsis: true,
  },
])

const loadingStore = useLoadingStore()

// Hàm lấy danh sách học sinh trong nhóm xưởng
const fetchStudentFactory = () => {
  loadingStore.show()
  requestAPI
    .get(API_ROUTES_TEACHER.FETCH_DATA_STUDENT_FACTORY, {
      params: {
        ...filter,
        page: pagination.current,
        size: pagination.pageSize,
      },
    })
    .then((response) => {
      const result = response.data.data
      students.value = result.data
      // Nếu API trả về tổng số trang, sử dụng:
      pagination.total = result.totalPages * filter.pageSize
      // Nếu trả về tổng số bản ghi, thay thế bằng: pagination.total = result.totalRecords
      pagination.current = filter.page
    })
    .catch((error) => {
      message.error(error.data?.message || 'Lỗi khi lấy dữ liệu')
    })
    .finally(() => {
      loadingStore.hide()
    })
}

// Xử lý thay đổi trang bảng
const handleTableChange = (pageInfo) => {
  pagination.current = pageInfo.current
  pagination.pageSize = pageInfo.pageSize
  filter.page = pageInfo.current
  filter.pageSize = pageInfo.pageSize // Đồng bộ pageSize cho filter
  fetchStudentFactory()
}

// Hàm xoá học sinh khỏi nhóm xưởng


const toggleStatusStudentFactory = (record) => {
  Modal.confirm({
    title: 'Xác nhận đổi trạng thái',
    content: `Bạn có chắc muốn thay đổi trạng thái của học sinh ${record.studentName}?`,
    onOk() {
      loadingStore.show()
      requestAPI
        .put(API_ROUTES_TEACHER.FETCH_DATA_STUDENT_FACTORY + '/' + record.studentFactoryId)
        .then((response) => {
          message.success(response.data.message || 'Trạng thái đã được cập nhật thành công')
          fetchStudentFactory() // Làm mới danh sách sau khi đổi trạng thái
        })
        .catch((error) => {
          message.error(error.data?.message || 'Lỗi khi đổi trạng thái sinh viên')
        })
        .finally(() => {
          loadingStore.hide()
        })
    },
  })
}

// <-- Thêm hàm changeFaceStudent
// const changeFaceStudent = (record) => {
//   Modal.confirm({
//     title: 'Xác nhận đổi mặt',
//     content: `Bạn có chắc muốn đổi mặt của học sinh ${record.studentName}?`,
//     onOk() {
//       loadingStore.show()
//       // Giả sử record chứa studentId, nếu không hãy thay đổi cho phù hợp
//       requestAPI
//         .put(API_ROUTES_TEACHER.FETCH_DATA_STUDENT_FACTORY + '/change-face/' + record.studentId)
//         .then((response) => {
//           message.success(response.data.message || 'Đổi mặt học sinh thành công')
//           fetchStudentFactory() // Làm mới danh sách sau khi đổi mặt
//         })
//         .catch((error) => {
//           message.error(error.response?.data?.message || 'Lỗi khi đổi mặt học sinh')
//         })
//         .finally(() => {
//           loadingStore.hide()
//         })
//     },
//   })
// }

const handleClearFilter = () => {
  // Clear all filter values
  Object.keys(filter).forEach(key => {
    if (key !== 'factoryId') { // Keep factoryId as it's from route
      filter[key] = ''
    }
  })
  pagination.current = 1
  fetchStudentFactory()
}

onMounted(() => {
  breadcrumbStore.setRoutes(breadcrumb.value)
  fetchStudentFactory()
})
</script>

<template>
  <div class="container-fluid">
    <!-- Bộ lọc tìm kiếm -->
    <div class="row g-3">
      <div class="col-12">
        <a-card :bordered="false" class="cart mb-3">
          <template #title> <FilterFilled /> Bộ lọc tìm kiếm </template>
          <div class="row g-3 filter-container">
            <div class="col-md-6 col-sm-6">
              <label class="label-title">Từ khoá:</label>
              <a-input
                v-model:value="filter.searchQuery"
                placeholder="Nhập mã, tên hoặc email học sinh"
                allowClear
                @change="fetchStudentFactory"
              />
            </div>
            <div class="col-md-6 col-sm-6">
              <label class="label-title">Trạng thái:</label>
              <a-select
                v-model:value="filter.status"
                placeholder="Chọn trạng thái"
                allowClear
                style="width: 100%"
                @change="fetchStudentFactory"
              >
                <a-select-option :value="''">Tất cả trạng thái</a-select-option>
                <a-select-option value="1">Đang học</a-select-option>
                <a-select-option value="0">Ngưng học</a-select-option>
              </a-select>
            </div>
          </div>
          <div class="row">
            <div class="col-12">
              <div class="d-flex justify-content-center flex-wrap gap-2 mt-3">
                <a-button class="btn-light" @click="fetchStudentFactory">
                  <FilterFilled /> Lọc
                </a-button>
                <a-button class="btn-gray" @click="handleClearFilter"> Huỷ lọc </a-button>
              </div>
            </div>
          </div>
        </a-card>
      </div>
    </div>

    <!-- Danh sách học sinh trong nhóm xưởng -->
    <div class="row g-3">
      <div class="col-12">
        <a-card :bordered="false" class="cart">
          <template #title> <UnorderedListOutlined /> Danh sách học sinh </template>
          <a-table
            class="nowrap"
            :loading="isLoading"
            :dataSource="students"
            :columns="columns"
            rowKey="studentFactoryId"
            :pagination="pagination"
            @change="handleTableChange"
            :scroll="{ y: 500, x: 'auto' }"
          >
            <template #bodyCell="{ column, record, index }">
              <template v-if="column.dataIndex">
                <!-- STT -->
                <template v-if="column.dataIndex === 'rowNumber'">
                  {{ index + 1 }}
                </template>
                <!-- Hiển thị trạng thái -->
                <template v-else-if="column.dataIndex === 'statusStudentFactory'">
                  <span class="nowrap">
                    <a-switch
                      class="me-2"
                      :checked="
                        record.statusStudentFactory === 'ACTIVE' ||
                        record.statusStudentFactory === 1
                      "
                      @change="toggleStatusStudentFactory(record)"
                    />
                  </span>
                  <a-tag
                    :color="
                      record.statusStudentFactory === 'ACTIVE' || record.statusStudentFactory === 1
                        ? 'green'
                        : 'red'
                    "
                  >
                    {{
                      record.statusStudentFactory === 'ACTIVE' || record.statusStudentFactory === 1
                        ? 'Đang học'
                        : 'Ngưng học'
                    }}
                  </a-tag>
                </template>
                <!-- Các cột khác -->
                <template v-else>
                  {{ record[column.dataIndex] }}
                </template>
              </template>
              <!-- Các nút hành động -->
            </template>
          </a-table>
        </a-card>
      </div>
    </div>
  </div>
</template>
