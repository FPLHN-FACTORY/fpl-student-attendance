<template>
  <h1>Quản lý bộ môn</h1>

  <!-- Bộ lọc tìm kiếm -->
  <a-card title="Bộ lọc" :bordered="false" class="cart">
    <a-row :gutter="16" class="filter-container">
      <!-- Ô nhập tên để tìm kiếm -->
      <a-col :span="8">
        <a-input
          v-model:value="filter.name"
          placeholder="Tìm kiếm theo tên"
          allowClear
          @change="fetchSubjects"
        />
      </a-col>

      <!-- Dropdown chọn trạng thái -->
      <a-col :span="8">
        <a-select
          v-model:value="filter.status"
          placeholder="Chọn trạng thái"
          allowClear
          @change="fetchSubjects"
        >
          <a-select-option :value="''">Tất cả trạng thái</a-select-option>
          <a-select-option :value="1">Hoạt động</a-select-option>
          <a-select-option :value="0">Không hoạt động</a-select-option>
        </a-select>
      </a-col>

      <!-- Nút lọc -->
      <a-col :span="8">
        <a-tooltip title="Lọc">
          <a-button
            @click="fetchSubjects"
            style="background-color: #fff7e6; color: black; border: 1px solid #ffa940"
          >
            <SearchOutlined />
            Lọc
          </a-button>
        </a-tooltip>
      </a-col>
    </a-row>
  </a-card>

  <!-- Danh sách bộ môn -->
  <a-card title="Danh sách bộ môn" :bordered="false" class="cart">
    <!-- Nút thêm bộ môn -->
    <div style="display: flex; justify-content: flex-end; margin-bottom: 10px">
      <a-tooltip title="Thêm bộ môn">
        <a-button
          style="background-color: #fff7e6; color: black; border: 1px solid #ffa940"
          @click="ShowAddModal(true)"
        >
          <PlusOutlined />
          Thêm
        </a-button>
      </a-tooltip>
    </div>

    <!-- Bảng hiển thị danh sách -->
    <a-table
      :dataSource="subjects"
      :columns="columns"
      :rowKey="'id'"
      bordered
      :pagination="pagination"
      @change="handleTableChange"
    >
      <!-- Custom cell cho cột trạng thái và chức năng -->
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'status'">
          <a-tag :color="record.status === 1 ? 'green' : 'red'">
            {{ record.status === 1 ? 'Hoạt động' : 'Không hoạt động' }}
          </a-tag>
        </template>

        <!-- Custom cell cho cột chức năng (actions) -->
        <template v-if="column.key === 'actions'">
          <!-- Nút chuyển hướng bộ môn cơ sở -->
          <a-tooltip title="Bộ môn cơ sở">
            <a-button
              @click="handleAddSubjectFacility(record)"
              type="text"
              :style="{
                backgroundColor: '#FFF7E6',
                marginRight: '8px',
                border: '1px solid #ffa940',
              }"
            >
              <ClusterOutlined />
            </a-button>
          </a-tooltip>

          <!-- Nút xem chi tiết -->
          <a-tooltip title="Xem chi tiết">
            <a-button
              @click="handleDetailSubject(record)"
              type="text"
              :style="{
                backgroundColor: '#FFF7E6',
                marginRight: '8px',
                border: '1px solid #ffa940',
              }"
            >
              <EyeOutlined />
            </a-button>
          </a-tooltip>

          <!-- Nút sửa -->
          <a-tooltip title="Sửa bộ môn">
            <a-button
              @click="handleUpdateSubject(record)"
              type="text"
              :style="{
                backgroundColor: '#FFF7E6',
                marginRight: '8px',
                border: '1px solid #ffa940',
              }"
            >
              <EditOutlined />
            </a-button>
          </a-tooltip>

          <!-- Nút xóa -->
          <a-tooltip title="Xóa bộ môn">
            <a-button
              @click="handleDeleteSubject(record)"
              type="text"
              :style="{ backgroundColor: '#FFF7E6', border: '1px solid #ffa940' }"
            >
              <DeleteOutlined />
            </a-button>
          </a-tooltip>
        </template>
      </template>
    </a-table>
  </a-card>

  <!-- Modal thêm bộ môn -->
  <a-modal v-model:visible="ModalAdd" title="Thêm bộ môn" @ok="handleAddSubject">
    <a-form layout="vertical">
      <a-form-item label="Tên" required>
        <a-input v-model:value="newSubject.name" />
      </a-form-item>
      <a-form-item label="Mã">
        <a-input v-model:value="newSubject.code" />
      </a-form-item>
    </a-form>
  </a-modal>

  <!-- Modal xem chi tiết bộ môn -->
  <a-modal v-model:visible="ModalDetail" title="Chi tiết bộ môn" footer="">
    <p><strong>Tên:</strong> {{ detailSubject.name }}</p>
    <p><strong>Mã:</strong> {{ detailSubject.code }}</p>
    <p>
      <strong>Trạng thái:</strong>
      <a-tag :color="detailSubject.status === 'ACTIVE' ? 'green' : 'red'">
        {{ detailSubject.status === 'ACTIVE' ? 'Hoạt động' : 'Không hoạt động' }}
      </a-tag>
    </p>
    <p><strong>Ngày tạo:</strong> {{ formatDate(detailSubject.createdAt) }}</p>
    <p><strong>Ngày cập nhật:</strong> {{ formatDate(detailSubject.updatedAt) }}</p>
  </a-modal>

  <!-- Modal sửa bộ môn -->
  <a-modal v-model:visible="ModalUpdate" title="Sửa bộ môn" @ok="updateSubject">
    <a-form layout="vertical">
      <a-form-item label="Tên" required>
        <a-input v-model:value="detailSubject.name" />
      </a-form-item>
      <a-form-item label="Mã">
        <a-input v-model:value="detailSubject.code" />
      </a-form-item>
      <a-form-item label="Trạng thái">
        <a-select v-model:value="detailSubject.status" placeholder="Chọn trạng thái">
          <a-select-option value="ACTIVE">Hoạt động</a-select-option>
          <a-select-option value="INACTIVE">Không hoạt động</a-select-option>
        </a-select>
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script>
import {
  SearchOutlined,
  PlusOutlined,
  EyeOutlined,
  EditOutlined,
  DeleteOutlined,
  ClusterOutlined,
} from '@ant-design/icons-vue'
import { message, Modal } from 'ant-design-vue'
import requestAPI from '@/services/requestApiService'
import { ROUTE_NAMES } from '@/router/adminRoute'
import { API_ROUTES_ADMIN } from '@/constants/adminConstant'

export default {
  components: {
    SearchOutlined,
    PlusOutlined,
    EyeOutlined,
    EditOutlined,
    DeleteOutlined,
    ClusterOutlined,
  },
  data() {
    return {
      subjects: [], // Danh sách bộ môn
      filter: { name: '', status: '', page: 1, pageSize: 5 }, // Bộ lọc tìm kiếm và phân trang
      ModalAdd: false, // Trạng thái modal thêm bộ môn
      ModalDetail: false, // Trạng thái modal xem chi tiết
      ModalUpdate: false, // Trạng thái modal sửa bộ môn
      newSubject: { name: '', code: '' }, // Dữ liệu bộ môn mới
      detailSubject: {}, // Bộ môn đang được xem
      columns: [
        { title: '#', dataIndex: 'indexs', key: 'indexs' },
        { title: 'Tên', dataIndex: 'name', key: 'name' },
        { title: 'Mã', dataIndex: 'code', key: 'code' },
        {
          title: 'Số lượng bộ môn cơ sở',
          dataIndex: 'sizeSubjectSemester',
          key: 'sizeSubjectSemester',
        },
        { title: 'Trạng thái', dataIndex: 'status', key: 'status' },
        { title: 'Chức năng', key: 'actions' },
      ],
      pagination: {
        current: 1, // Trang hiện tại
        pageSize: 5, // Số bản ghi mỗi trang (cố định là 5)
        total: 0, // Tổng số bản ghi
        showSizeChanger: false, // Không cho phép thay đổi số bản ghi mỗi trang
      },
    }
  },

  mounted() {
    this.fetchSubjects()
  },

  methods: {
    // Load table
    fetchSubjects() {
      requestAPI
        .post(`${API_ROUTES_ADMIN.FETCH_DATA_SUBJECT}/list`, this.filter)
        .then((response) => {
          this.subjects = response.data.data.data
          this.pagination.total = response.data.data.totalPages * this.filter.pageSize
          this.pagination.current = this.filter.page
        })
        .catch(() => {
          message.error('Lỗi khi lấy dữ liệu')
        })
    },

    // Paging
    handleTableChange(pagination) {
      this.filter.page = pagination.current
      this.fetchSubjects()
    },

    // Mở modal thêm bộ môn
    ShowAddModal(isOpen) {
      this.ModalAdd = isOpen
    },

    // Xử lý thêm bộ môn
    handleAddSubject() {
      if (!this.newSubject.name) {
        message.error('Tên không được bỏ trống')
        return
      }
      requestAPI
        .post(API_ROUTES_ADMIN.FETCH_DATA_SUBJECT, this.newSubject)
        .then(() => {
          message.success('Thêm bộ môn thành công')
          this.ShowAddModal(false)
          this.fetchSubjects()
          this.newSubject = { name: '', code: '' } // Reset form
        })
        .catch(() => {
          message.error('Lỗi khi thêm bộ môn')
        })
    },

    // Xem chi tiết bộ môn
    handleDetailSubject(record) {
      requestAPI
        .get(`${API_ROUTES_ADMIN.FETCH_DATA_SUBJECT}/${record.id}`)
        .then((response) => {
          this.ModalDetail = true
          this.detailSubject = response.data.data
        })
        .catch(() => {
          message.error('Lỗi khi lấy detail')
        })
    },

    // Mở modal sửa bộ môn
    handleUpdateSubject(record) {
      requestAPI
        .get(`${API_ROUTES_ADMIN.FETCH_DATA_SUBJECT}/${record.id}`)
        .then((response) => {
          this.detailSubject = response.data.data
          this.ModalUpdate = true
        })
        .catch(() => {
          message.error('Lỗi khi lấy detail')
        })
    },

    // Xử lý sửa bộ môn
    updateSubject(record) {
      if (!this.detailSubject.name) {
        message.error('Tên không được bỏ trống')
        return
      }
      let req = {
        id: this.detailSubject.id,
        name: this.detailSubject.name,
        code: this.detailSubject.code,
        status: this.detailSubject.status,
      }
      console.log(req.id)

      requestAPI
        .put(`${API_ROUTES_ADMIN.FETCH_DATA_SUBJECT}/${req.id}`, req)
        .then(() => {
          message.success('Cập nhật bộ môn thành công')
          this.ModalUpdate = false
          this.fetchSubjects()
        })
        .catch((error) => {
          message.error('Lỗi khi cập nhật bộ môn')
        })
    },

    // Xóa bộ môn
    handleDeleteSubject(record) {
      Modal.confirm({
        title: 'Xác nhận xóa',
        content: `Bạn có chắc chắn muốn xóa bộ môn ${record.name} không?`,
        onOk: () => {
          requestAPI
            .delete(`${API_ROUTES_ADMIN.FETCH_DATA_SUBJECT}/${record.id}`)
            .then(() => {
              message.success('Xóa bộ môn thành công')
              this.fetchSubjects()
            })
            .catch(() => {
              message.error('Lỗi khi xóa bộ môn')
            })
        },
      })
    },

    // Xử lý chuyển hướng
    handleAddSubjectFacility(record) {
      this.$router.push({
        name: ROUTE_NAMES.MANAGEMENT_SUBJECT_FACILITY,
        query: { subjectId: record.id }, // Truyền ID của bộ môn
      })
    },

    //Convert date
    formatDate(timestamp) {
      if (!timestamp) return 'Không xác định'
      return new Date(timestamp).toLocaleString()
    },
  },
}
</script>

<style>
.cart {
  margin-top: 5px;
}

.filter-container {
  margin-bottom: 5px;
}
</style>
