<template>
  <h1>Quản lý cấp dự án</h1>

  <!-- Bộ lọc tìm kiếm -->
  <a-card title="Bộ lọc" :bordered="false" class="cart">
    <a-row :gutter="16" class="filter-container">
      <!-- Ô nhập tên để tìm kiếm -->
      <a-col :span="8">
        <a-input
          v-model:value="filter.name"
          placeholder="Tìm kiếm theo tên"
          allowClear
          @change="fetchLevels"
        />
      </a-col>

      <!-- Dropdown chọn trạng thái -->
      <a-col :span="8">
        <a-select
          v-model:value="filter.status"
          placeholder="Chọn trạng thái"
          allowClear
          @change="fetchLevels"
        >
          <a-select-option :value="''">Tất cả trạng thái</a-select-option>
          <a-select-option :value="1">Hoạt động</a-select-option>
          <a-select-option :value="0">Không hoạt động</a-select-option>
        </a-select>
      </a-col>

      <!-- Nút lọc -->
      <a-col :span="8">
        <a-button
          @click="fetchLevels"
          style="background-color: #fff7e6; color: black; border: 1px solid #ffa940"
        >
          <SearchOutlined />
          Lọc
        </a-button>
      </a-col>
    </a-row>
  </a-card>

  <!-- Danh sách cấp dự án -->
  <a-card title="Danh sách cấp dự án" :bordered="false" class="cart">
    <!-- Nút thêm cấp dự án -->
    <div style="display: flex; justify-content: flex-end; margin-bottom: 10px">
      <a-button
        style="background-color: #fff7e6; color: black; border: 1px solid #ffa940"
        @click="ShowAddModal(true)"
      >
        <PlusOutlined />
        Thêm
      </a-button>
    </div>

    <!-- Bảng hiển thị danh sách -->
    <a-table
      :dataSource="levels"
      :columns="columns"
      :rowKey="'id'"
      bordered
      :pagination="pagination"
      @change="handleTableChange"
    >
      <!-- Custom cell cho cột trạng thái -->
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'status'">
          <a-tag :color="record.status === 1 ? 'green' : 'red'">
            {{ record.status === 1 ? 'Hoạt động' : 'Không hoạt động' }}
          </a-tag>
        </template>

        <!-- Custom cell cho cột chức năng (actions) -->
        <template v-if="column.key === 'actions'">
          <!-- Nút xem chi tiết -->
          <a-button
            @click="HandelDetailLevel(record)"
            type="text"
            :style="{ backgroundColor: '#FFF7E6', marginRight: '8px', border: '1px solid #ffa940' }"
          >
            <EyeOutlined />
          </a-button>

          <!-- Nút sửa -->
          <a-button
            @click="HandelUpdateLevel(record)"
            type="text"
            :style="{ backgroundColor: '#FFF7E6', marginRight: '8px', border: '1px solid #ffa940' }"
          >
            <EditOutlined />
          </a-button>

          <!-- Nút xóa -->
          <a-button
            @click="HandelDeletLevel(record)"
            type="text"
            :style="{ backgroundColor: '#FFF7E6', border: '1px solid #ffa940' }"
          >
            <DeleteOutlined />
          </a-button>
        </template>
      </template>
    </a-table>
  </a-card>

  <!-- Modal xem chi tiết cấp dự án -->
  <a-modal v-model:open="ModalDetail" title="Chi tiết cấp dự án" footer="">
    <p><strong>Tên:</strong> {{ detailLevel.name }}</p>
    <p><strong>Mã:</strong> {{ detailLevel.code }}</p>
    <p><strong>Mô tả:</strong> {{ detailLevel.description }}</p>
    <p>
      <strong>Trạng thái:</strong>
      <span :style="{ color: detailLevel.status === 'ACTIVE' ? 'green' : 'red' }">
        {{ detailLevel.status === 'ACTIVE' ? ' Hoạt động' : ' Không hoạt động' }}
      </span>
    </p>
    <p><strong>Ngày tạo:</strong> {{ formatDate(detailLevel.createdAt) }}</p>
    <p><strong>Ngày cập nhật:</strong> {{ formatDate(detailLevel.updatedAt) }}</p>
  </a-modal>

  <!-- Modal thêm cấp dự án -->
  <a-modal v-model:visible="ModalAdd" title="Thêm cấp dự án" @ok="HandelAddLevel">
    <a-form layout="vertical">
      <a-form-item label="Tên" required>
        <a-input v-model:value="newLevel.name" />
      </a-form-item>
      <a-form-item label="Mã">
        <a-input v-model:value="newLevel.code" />
      </a-form-item>
      <a-form-item label="Mô tả">
        <a-textarea v-model:value="newLevel.description" />
      </a-form-item>
    </a-form>
  </a-modal>

  <!-- Modal sửa cấp dự án -->
  <a-modal v-model:visible="ModalUpdate" title="Sửa cấp dự án" @ok="updateLevel">
    <a-form layout="vertical">
      <a-form-item label="Tên" required>
        <a-input v-model:value="detailLevel.name" />
      </a-form-item>
      <a-form-item label="Mô tả">
        <a-textarea v-model:value="detailLevel.description" />
      </a-form-item>
      <a-form-item label="Mã">
        <a-input v-model:value="detailLevel.code" />
      </a-form-item>
      <a-form-item label="Trạng thái">
        <a-select v-model:value="detailLevel.status" placeholder="Chọn trạng thái">
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
} from '@ant-design/icons-vue'
import { message, Modal } from 'ant-design-vue'
import requestAPI from '@/services/requestApiService'

export default {
  components: { SearchOutlined, PlusOutlined, EyeOutlined, EditOutlined, DeleteOutlined },
  data() {
    return {
      levels: [], // Danh sách cấp dự án
      filter: { name: '', status: '', page: 1, pageSize: 5 }, // Bộ lọc tìm kiếm và phân trang
      ModalAdd: false, // Trạng thái modal thêm cấp dự án
      ModalDetail: false, // Trạng thái modal xem chi tiết
      ModalUpdate: false, // Trạng thái modal sửa cấp dự án
      newLevel: { name: '', code: '', description: '' }, // Dữ liệu cấp dự án mới
      detailLevel: {}, // Cấp dự án đang được xem
      columns: [
        { title: '#', dataIndex: 'indexs', key: 'indexs' },
        { title: 'Tên', dataIndex: 'name', key: 'name' },
        { title: 'Mã', dataIndex: 'code', key: 'code' },
        { title: 'Mô tả', dataIndex: 'description', key: 'description' },
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
    this.fetchLevels()
  },

  methods: {
    //Load table
    fetchLevels() {
      requestAPI
        .post(
          'http://localhost:8765/api/v1/admin-management/level-project-management/list',
          this.filter,
        )
        .then((response) => {
          this.levels = response.data.data.data
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
      this.fetchLevels()
    },

    //Status Open Modal
    ShowAddModal(req) {
      this.ModalAdd = req
    },
    ShowDetailModal(req) {
      this.ModalDetail = req
    },
    ShowUpdateModal(req) {
      this.ModalUpdate = req
    },

    //Add
    HandelAddLevel() {
      if (!this.newLevel.name) {
        message.error('Tên không được bỏ trống')
        return
      }
      requestAPI
        .post(
          'http://localhost:8765/api/v1/admin-management/level-project-management',
          this.newLevel,
        )
        .then(() => {
          message.success('Thêm cấp dự án thành công')
          this.ShowAddModal(false)
          this.fetchLevels()
          this.clearFormAdd()
        })
        .catch(() => {
          message.error('Lỗi khi thêm cấp dự án')
        })
    },

    //Detail
    HandelDetailLevel(record) {
      requestAPI
        .get(`http://localhost:8765/api/v1/admin-management/level-project-management/${record.id}`)
        .then((response) => {
          this.ShowDetailModal(true)
          this.detailLevel = response.data.data
        })
        .catch(() => {
          message.error('Lỗi khi lấy detail')
        })
    },

    //Update
    HandelUpdateLevel(record) {
      requestAPI
        .get(`http://localhost:8765/api/v1/admin-management/level-project-management/${record.id}`)
        .then((response) => {
          this.detailLevel = response.data.data
          this.ShowUpdateModal(true)
        })
        .catch(() => {
          message.error('Lỗi khi lấy detail')
        })
    },

    //Submit update
    updateLevel() {
      if (!this.detailLevel.name) {
        message.error('Tên không được bỏ trống')
        return
      }
      requestAPI
        .put(
          `http://localhost:8765/api/v1/admin-management/level-project-management/${this.detailLevel.id}`,
          this.detailLevel,
        )
        .then(() => {
          message.success('Cập nhật cấp dự án thành công')
          this.ShowUpdateModal(false)
          this.fetchLevels()
        })
        .catch((error) => {
          message.error('Lỗi khi cập nhật cấp dự án')
          console.error('Error:', error)
        })
    },

    //Delete
    HandelDeletLevel(record) {
      Modal.confirm({
        title: 'Xác nhận xóa',
        content: `Bạn có chắc chắn muốn xóa cấp dự án ${record.name} không?`,
        onOk: () => {
          requestAPI
            .delete(
              `http://localhost:8765/api/v1/admin-management/level-project-management/${record.id}`,
            )
            .then(() => {
              message.success('Xóa cấp dự án thành công')
              this.fetchLevels()
            })
            .catch(() => {
              message.error('Lỗi khi xóa cấp dự án')
            })
        },
      })
    },

    //Convert date
    formatDate(timestamp) {
      if (!timestamp) return 'Không xác định'
      return new Date(timestamp).toLocaleString()
    },

    clearFormAdd() {
      this.newLevel.name = ''
      this.newLevel.code = ''
      this.newLevel.description = ''
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
