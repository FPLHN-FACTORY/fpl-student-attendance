<template>
  <div>
    <h1>Quản lý sinh viên nhóm xưởng {{ factoryName }}</h1>

    <!-- Bộ lọc tìm kiếm -->
    <a-card title="Bộ lọc" :bordered="false" class="cart">
      <a-row :gutter="16" class="filter-container">
        <a-col :span="12">
          <a-input
            v-model:value="filter.searchQuery"
            placeholder="Mã, tên hoặc email sinh viên"
            allowClear
            @change="fetchStudentFactories"
          />
        </a-col>
        <a-col :span="12">
          <a-select
            v-model:value="filter.status"
            placeholder="Chọn trạng thái"
            allowClear
            style="width: 100%"
            @change="fetchStudentFactories"
          >
            <a-select-option :value="''">Tất cả trạng thái</a-select-option>
            <a-select-option value="1">Hoạt động</a-select-option>
            <a-select-option value="0">Không hoạt động</a-select-option>
          </a-select>
        </a-col>
      </a-row>
    </a-card>

    <!-- Danh sách sinh viên trong nhóm xưởng -->
    <a-card title="Danh sách sinh viên" :bordered="false" class="cart">
      <div style="display: flex; justify-content: flex-end; margin-bottom: 10px">
        <a-tooltip title="Thêm học sinh vào nhóm xưởng">
          <a-button
            style="background-color: #fff7e6; color: black; border: 1px solid #ffa940"
            @click="isAddStudentModalVisible = true"
          >
            <PlusOutlined /> Thêm học sinh
          </a-button>
        </a-tooltip>
      </div>
      <a-table
        :dataSource="studentFactories"
        :columns="columns"
        rowKey="studentFactoryId"
        bordered
        :pagination="pagination"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record, index }">
          <template v-if="column.dataIndex">
            <!-- STT -->
            <template v-if="column.dataIndex === 'rowNumber'">
              {{ index + 1 }}
            </template>
            <!-- Hiển thị trạng thái với tag -->
            <template v-else-if="column.dataIndex === 'statusStudentFactory'">
              <a-tag
                :color="
                  record.statusStudentFactory === 'ACTIVE' || record.statusStudentFactory === 1
                    ? 'green'
                    : 'red'
                "
              >
                {{
                  record.statusStudentFactory === 'ACTIVE' || record.statusStudentFactory === 1
                    ? 'Hoạt động'
                    : 'Không hoạt động'
                }}
              </a-tag>
            </template>
            <!-- Các cột khác hiển thị giá trị -->
            <template v-else>
              {{ record[column.dataIndex] }}
            </template>
          </template>
          <!-- Cột chức năng -->
          <template v-else-if="column.key === 'actions'">
            <a-space>
              <a-tooltip title="Xóa sinh viên khỏi nhóm xưởng">
                <a-button type="text" class="action-button" @click="confirmDeleteStudent(record)">
                  <DeleteOutlined />
                </a-button>
              </a-tooltip>
              <a-tooltip title="Đổi trạng thái sinh viên">
                <a-button type="text" class="action-button" @click="confirmChangeStatus(record)">
                  <SyncOutlined />
                </a-button>
              </a-tooltip>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- Modal "Thêm học sinh vào nhóm xưởng" -->
    <a-modal
      v-model:open="isAddStudentModalVisible"
      title="Thêm học sinh vào nhóm xưởng"
      width="80%"
      @cancel="resetStudentModal"
      @ok="handleAddStudents"
    >
      <!-- Bộ lọc cho modal danh sách tất cả sinh viên -->
      <a-row :gutter="16" class="filter-container" style="margin-bottom: 16px">
        <a-col :span="21">
          <a-input
            v-model:value="studentFilter.searchQuery"
            placeholder="Mã, tên hoặc email sinh viên"
            allowClear
            @change="fetchAllStudents"
          />
        </a-col>
      </a-row>
      <!-- Bảng danh sách tất cả sinh viên -->
      <a-table
        :dataSource="allStudents"
        :columns="studentColumns"
        rowKey="id"
        bordered
        :pagination="studentPagination"
        @change="handleStudentTableChange"
      >
        <template #bodyCell="{ column, record, index }">
          <template v-if="column.dataIndex">
            <!-- STT -->
            <template v-if="column.dataIndex === 'rowNumber'">
              {{ index + 1 }}
            </template>
            <!-- Các cột khác -->
            <template v-else>
              {{ record[column.dataIndex] }}
            </template>
          </template>
          <!-- Cột checkbox -->
          <template v-else-if="column.key === 'select'">
            <a-checkbox
              :checked="
                selectedStudents[record.id] !== undefined
                  ? selectedStudents[record.id]
                  : record.checked
              "
              @change="(e) => handleStudentCheckboxChange(record, e.target.checked)"
            />
          </template>
        </template>
      </a-table>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { message, Modal } from 'ant-design-vue'
import requestAPI from '@/services/requestApiService'
import { API_ROUTES_STAFF } from '@/constants/staffConstant'
import { PlusOutlined, DeleteOutlined, SyncOutlined } from '@ant-design/icons-vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const factoryId = route.query.factoryId
const factoryName = route.query.factoryName

if (!factoryId) {
  message.error('Không tìm thấy factoryId')
}

// -------------------- Danh sách sinh viên đã có trong nhóm xưởng --------------------
const studentFactories = ref([])

const filter = reactive({
  searchQuery: '',
  status: '',
  page: 1,
  pageSize: 5,
})
const pagination = reactive({
  current: 1,
  pageSize: 5,
  total: 0,
  showSizeChanger: false,
})
const columns = ref([
  { title: 'STT', dataIndex: 'rowNumber', key: 'rowNumber' },
  { title: 'Mã sinh viên', dataIndex: 'studentCode', key: 'studentCode' },
  { title: 'Tên sinh viên', dataIndex: 'studentName', key: 'studentName' },
  { title: 'Email sinh viên', dataIndex: 'studentEmail', key: 'studentEmail' },
  { title: 'Trạng thái', dataIndex: 'statusStudentFactory', key: 'statusStudentFactory' },
  { title: 'Chức năng', key: 'actions' },
])

const fetchStudentFactories = () => {
  requestAPI
    .get(API_ROUTES_STAFF.FETCH_DATA_STUDENT_FACTORY + '/' + factoryId, {
      params: {
        ...filter,
        factoryId,
        'userStudentRequest.searchQuery': filter.searchQuery,
      },
    })
    .then((response) => {
      const result = response.data.data
      studentFactories.value = result.data
      pagination.total = result.totalPages * filter.pageSize
      pagination.current = filter.page
    })
    .catch((error) => {
      message.error(
        error.response?.data?.message || 'Lỗi khi lấy danh sách sinh viên trong nhóm xưởng'
      )
    })
}

// -------------------- Lấy danh sách sinh viên đã có trong nhóm (để tích checkbox) --------------------
const existingStudents = ref([])

const fetchExistingStudents = () => {
  // Gọi API getStudentFactoryExist: GET /exist-student/{factoryId}
  requestAPI
    .get(API_ROUTES_STAFF.FETCH_DATA_STUDENT_FACTORY + '/exist-student/' + factoryId)
    .then((response) => {
      // response.data.data là danh sách sinh viên đã có trong nhóm, chứa studentId
      existingStudents.value = response.data.data || []
      // Cập nhật lại danh sách allStudents nếu đã fetch
      if (allStudents.value.length) {
        updateAllStudentsCheckStatus()
      }
    })
    .catch((error) => {
      message.error(
        error.response?.data?.message || 'Lỗi khi lấy danh sách sinh viên đã có trong nhóm xưởng'
      )
    })
}

// -------------------- Danh sách tất cả sinh viên (modal thêm học sinh) --------------------
const studentFilter = reactive({
  searchQuery: '',
  page: 1,
  pageSize: 5,
})
const studentPagination = reactive({
  current: 1,
  pageSize: 5,
  total: 0,
  showSizeChanger: false,
})
const allStudents = ref([])
const studentColumns = ref([
  { title: 'STT', dataIndex: 'rowNumber', key: 'rowNumber' },
  { title: 'Mã sinh viên', dataIndex: 'code', key: 'code' },
  { title: 'Tên sinh viên', dataIndex: 'name', key: 'name' },
  { title: 'Email', dataIndex: 'email', key: 'email' },
  { title: 'Chọn', key: 'select' },
])
const selectedStudents = reactive({})

// Hàm cập nhật lại thuộc tính checked dựa trên danh sách sinh viên đã có trong nhóm
const updateAllStudentsCheckStatus = () => {
  const existIds = existingStudents.value.map((item) => item.studentId)
  allStudents.value = allStudents.value.map((item) => ({
    ...item,
    // So sánh dựa trên item.id vì API getAllUserStudent trả về field id
    checked: existIds.includes(item.id),
  }))
}

const fetchAllStudents = () => {
  requestAPI
    .get(API_ROUTES_STAFF.FETCH_DATA_STUDENT_FACTORY + '/students', {
      params: {
        ...studentFilter,
        factoryId,
        'userStudentRequest.searchQuery': studentFilter.searchQuery,
      },
    })
    .then((response) => {
      const result = response.data.data
      // Map dữ liệu ban đầu
      allStudents.value = result.data.map((item) => ({
        ...item,
        // Ban đầu checked là false, sẽ cập nhật sau
        checked: false,
      }))
      studentPagination.total = result.totalPages * studentFilter.pageSize
      studentPagination.current = studentFilter.page
      // Sau khi fetch allStudents, cập nhật trạng thái checked dựa trên existingStudents
      updateAllStudentsCheckStatus()
    })
    .catch((error) => {
      message.error(error.response?.data?.message || 'Lỗi khi lấy danh sách sinh viên')
    })
}

const handleStudentTableChange = (paginationData) => {
  studentFilter.page = paginationData.current
  fetchAllStudents()
}

const handleStudentCheckboxChange = (student, checked) => {
  selectedStudents[student.id] = checked
  if (checked) {
    // Nếu checkbox được tích, gọi API thêm sinh viên vào nhóm
    const payload = {
      studentId: student.id,
      factoryId: factoryId,
    }
    requestAPI
      .post(API_ROUTES_STAFF.FETCH_DATA_STUDENT_FACTORY, payload)
      .then((response) => {
        message.success(response.data.message || 'Thêm sinh viên vào nhóm xưởng thành công')
        fetchStudentFactories() // Reload danh sách sinh viên trong nhóm
        fetchExistingStudents() // Cập nhật lại danh sách sinh viên đã có (để checkbox tích sẵn)
      })
      .catch((error) => {
        message.error(error.response?.data?.message || 'Lỗi khi thêm sinh viên vào nhóm xưởng')
      })
  } else {
    // Nếu checkbox bị bỏ tích, gọi API xoá sinh viên khỏi nhóm
    // Lấy thông tin từ danh sách existingStudents để biết được studentFactoryId
    const existing = existingStudents.value.find((item) => item.studentId === student.id)
    if (existing && existing.studentFactoryId) {
      requestAPI
        .delete(API_ROUTES_STAFF.FETCH_DATA_STUDENT_FACTORY + '/' + existing.studentFactoryId)
        .then((response) => {
          message.success(response.data.message || 'Xóa sinh viên khỏi nhóm xưởng thành công')
          fetchStudentFactories() // Reload danh sách sinh viên trong nhóm
          fetchExistingStudents() // Cập nhật lại danh sách sinh viên đã có
        })
        .catch((error) => {
          message.error(error.response?.data?.message || 'Lỗi khi xóa sinh viên khỏi nhóm xưởng')
        })
    } else {
      message.warning('Sinh viên này chưa có trong nhóm xưởng')
    }
  }
}

// Khi nhấn OK trong modal "Thêm học sinh", chỉ đóng modal
const handleAddStudents = () => {
  isAddStudentModalVisible.value = false
}

// Reset lại modal khi đóng
const resetStudentModal = () => {
  isAddStudentModalVisible.value = false
  studentFilter.searchQuery = ''
  studentFilter.page = 1
  for (const key in selectedStudents) {
    selectedStudents[key] = false
  }
}

const handleTableChange = (paginationData) => {
  filter.page = paginationData.current
  fetchStudentFactories()
}

const confirmDeleteStudent = (record) => {
  Modal.confirm({
    title: 'Xác nhận xóa',
    content: `Bạn có chắc muốn xóa sinh viên ${record.studentName} khỏi nhóm xưởng?`,
    onOk() {
      deleteStudentFactory(record.studentFactoryId)
    },
  })
}

const deleteStudentFactory = (studentFactoryId) => {
  requestAPI
    .delete(API_ROUTES_STAFF.FETCH_DATA_STUDENT_FACTORY + '/' + studentFactoryId)
    .then((response) => {
      message.success(response.data.message || 'Xóa sinh viên thành công')
      fetchStudentFactories()
      fetchExistingStudents() // Cập nhật lại danh sách sinh viên đã có
    })
    .catch((error) => {
      message.error(error.response?.data?.message || 'Lỗi khi xóa sinh viên khỏi nhóm xưởng')
    })
}

const confirmChangeStatus = (record) => {
  Modal.confirm({
    title: 'Xác nhận đổi trạng thái',
    content: `Bạn có chắc muốn đổi trạng thái cho sinh viên ${record.studentName}?`,
    onOk() {
      changeStatusStudentFactory(record.studentFactoryId)
    },
  })
}

const changeStatusStudentFactory = (studentFactoryId) => {
  requestAPI
    .put(API_ROUTES_STAFF.FETCH_DATA_STUDENT_FACTORY + '/' + studentFactoryId)
    .then((response) => {
      message.success(response.data.message || 'Đổi trạng thái thành công')
      fetchStudentFactories()
      fetchExistingStudents() // Cập nhật lại checkbox nếu cần
    })
    .catch((error) => {
      message.error(error.response?.data?.message || 'Lỗi khi đổi trạng thái sinh viên')
    })
}

const isAddStudentModalVisible = ref(false)

onMounted(() => {
  fetchStudentFactories()
  fetchExistingStudents()
  fetchAllStudents()
})
</script>

<style scoped>
.cart {
  margin-top: 10px;
}
.filter-container {
  margin-bottom: 10px;
}
.action-button {
  background-color: #fff7e6;
  color: black;
  border: 1px solid #ffa940;
  margin-right: 8px;
}
</style>
