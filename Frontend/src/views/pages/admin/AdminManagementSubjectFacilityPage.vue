<template>
  <h1>Quản lý bộ môn cơ sở - Bộ môn {{ subject.name }}</h1>

  <a-card title="Bộ lọc" :bordered="false" class="filter-card">
    <a-row :gutter="16" class="filter-container">
      <a-col :span="9">
        <a-input
          v-model:value="filter.name"
          placeholder="Tìm kiếm theo tên"
          allowClear
          class="filter-input"
          @change="fetchSubjectFacility"
        />
      </a-col>

      <a-col :span="3">
        <a-select
          v-model:value="filter.facilityId"
          placeholder="Cơ sở"
          allowClear
          class="filter-select"
          :dropdownMatchSelectWidth="false"
          @change="fetchSubjectFacility"
        >
          <a-select-option :value="null">Tất cả cơ sở</a-select-option>
          <a-select-option v-for="f in facility" :key="f.id" :value="f.id">
            {{ f.name }}
          </a-select-option>
        </a-select>
      </a-col>

      <a-col :span="3">
        <a-select
          v-model:value="filter.status"
          placeholder="Trạng thái"
          allowClear
          class="filter-select"
          :dropdownMatchSelectWidth="false"
          @change="fetchSubjectFacility"
        >
          <a-select-option :value="null">Tất cả trạng thái</a-select-option>
          <a-select-option :value="1">Hoạt động</a-select-option>
          <a-select-option :value="0">Không hoạt động</a-select-option>
        </a-select>
      </a-col>

      <a-col>
        <a-button
          @click="fetchSubjectFacility"
          class="filter-button"
          :style="{ backgroundColor: '#fff7e6', color: 'black', border: '1px solid #ffa940' }"
        >
          <SearchOutlined />
          Lọc
        </a-button>
      </a-col>
    </a-row>
  </a-card>

  <a-card title="Danh sách bộ môn cơ sở" :bordered="false" class="cart">
    <div style="display: flex; justify-content: flex-end; margin-bottom: 10px">
      <a-button
        style="background-color: #fff7e6; color: black; border: '1px solid #ffa940'"
        @click="showAddModal()"
      >
        <PlusOutlined />
        Thêm
      </a-button>
    </div>

    <a-table
      :dataSource="subjectFacility"
      :columns="columns"
      :rowKey="'id'"
      bordered
      :pagination="pagination"
      @change="handleTableChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'status'">
          <a-tag :color="record.status == 1 ? 'green' : 'red'">
            {{ record.status == 1 ? 'Hoạt động' : 'Không hoạt động' }}
          </a-tag>
        </template>

        <template v-if="column.key === 'actions'">
          <a-button
            @click="handleDetailSubjectFacility(record)"
            type="text"
            :style="{ backgroundColor: '#FFF7E6', marginRight: '8px', border: '1px solid #ffa940' }"
          >
            <EyeOutlined />
          </a-button>

          <a-button
            @click="handleUpdateProject(record)"
            type="text"
            :style="{ backgroundColor: '#FFF7E6', marginRight: '8px', border: '1px solid #ffa940' }"
          >
            <EditOutlined />
          </a-button>

          <a-button
            @click="handleDeleteSubjectFacility(record)"
            type="text"
            :style="{ backgroundColor: '#FFF7E6', border: '1px solid #ffa940' }"
          >
            <DeleteOutlined />
          </a-button>
        </template>
      </template>
    </a-table>
  </a-card>

  <a-modal
    title="Thêm Bộ Môn Cơ Sở"
    v-model:visible="ModalAdd"
    @ok="handleAddSubjectFacility"
    @cancel="ModalAdd = false"
  >
    <a-form :model="newSubjectFacility">
      <a-form-item label="Tên Bộ Môn">
        <a-input v-model:value="newSubjectFacility.name" disabled />
      </a-form-item>

      <a-form-item label="Cơ Sở">
        <a-select
          v-model:value="newSubjectFacility.facilityId"
          placeholder="Chọn cơ sở"
          allowClear
          mode="multiple"
        >
          <a-select-option :value="null">Tất cả cở sở</a-select-option>

          <a-select-option v-for="f in facilitySubject" :key="f.id" :value="f.id">
            {{ f.name }}
          </a-select-option>
        </a-select>
      </a-form-item>
    </a-form>
  </a-modal>

  <!-- Modal xem chi tiết bộ môn -->
  <a-modal v-model:visible="ModalDetail" title="Chi tiết bộ môn cơ sở" footer="">
    <p><strong>Tên bộ môn:</strong> {{ detailSubjectFacility.subject.name }}</p>
    <p><strong>Tên cơ sở:</strong> {{ detailSubjectFacility.facility.name }}</p>
    <p>
      <strong>Trạng thái:</strong>
      <a-tag :color="detailSubjectFacility.status === 'ACTIVE' ? 'green' : 'red'">
        {{ detailSubjectFacility.status === 'ACTIVE' ? 'Hoạt động' : 'Không hoạt động' }}
      </a-tag>
    </p>
    <p><strong>Ngày tạo:</strong> {{ formatDate(detailSubjectFacility.createdAt) }}</p>
    <p><strong>Ngày cập nhật:</strong> {{ formatDate(detailSubjectFacility.updatedAt) }}</p> 
  </a-modal>


  <!-- Modal sửa bộ môn -->
  <a-modal v-model:visible="ModalUpdate" title="Sửa bộ môn cơ sở" @ok="updateSubjectFacility">
    <p>Chưa nghĩ ra cần sửa cái gì</p>
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
      filter: {
        name: null,
        status: null,
        page: 1,
        pageSize: 5,
        facilityId: null,
        subjectId: null,
      },
      subject: {},
      detailSubjectFacility: {},
      facility: [],
      facilitySubject: [],
      subjectFacility: [],
      pagination: {
        current: 1,
        pageSize: 5,
        total: 0,
        showSizeChanger: false,
      },
      columns: [
        { title: '#', dataIndex: 'indexs', key: 'indexs' },
        { title: 'Tên bộ môn', dataIndex: 'subjectName', key: 'subjectName' },
        { title: 'Tên cơ sở', dataIndex: 'facilityName', key: 'facilityName' },
        { title: 'Trạng thái', dataIndex: 'status', key: 'status' },
        { title: 'Chức năng', key: 'actions' },
      ],
      ModalAdd: false,
      ModalDetail: false,
      ModalUpdate: false,
      newSubjectFacility: {
        name: '',
        facilityId: null,
      },
    }
  },

  mounted() {
    this.filter.subjectId = this.$route.query.subjectId
    this.fetchSubjectFacility()
    this.fetchSubject()
    this.fetchFacilityCombobox()
  },

  methods: {
    fetchSubjectFacility() {
      requestAPI
        .post(
          'http://localhost:8765/api/v1/admin-management/subject-facility-management/list',
          this.filter,
        )
        .then((response) => {
          this.subjectFacility = response.data.data.data
          this.pagination.total = response.data.data.totalPages * this.filter.pageSize
          this.pagination.current = this.filter.page
        })
        .catch(() => {
          message.error('Lỗi khi lấy thông tin bộ môn cơ sở')
        })
    },

    fetchSubject() {
      requestAPI
        .get(
          `http://localhost:8765/api/v1/admin-management/subject-management/${this.filter.subjectId}`,
        )
        .then((response) => {
          this.subject = response.data.data
        })
        .catch(() => {
          message.error('Lỗi khi lấy detail')
        })
    },

    fetchFacilityCombobox() {
      requestAPI
        .get(
          'http://localhost:8765/api/v1/admin-management/subject-facility-management/facility-combobox',
        )
        .then((response) => {
          this.facility = response.data
        })
        .catch(() => {
          message.error('Lỗi khi lấy dữ liệu combobox cơ sở')
        })
    },

    fetchFacilitySubjectCombobox() {
      requestAPI
        .post(
          'http://localhost:8765/api/v1/admin-management/subject-facility-management/facility-combobox',
          this.filter,
        )
        .then((response) => {
          this.facilitySubject = response.data
        })
        .catch(() => {
          message.error('Lỗi khi lấy dữ liệu cơ sở chưa có')
        })
    },

    handleTableChange(pagination) {
      this.filter.page = pagination.current
      this.fetchSubjectFacility()
    },

    showAddModal() {
      this.ModalAdd = true
      this.newSubjectFacility.name = this.subject.name
      this.fetchFacilitySubjectCombobox()
      this.newSubjectFacility.facilityId = null
    },

    handleAddSubjectFacility() {
      if (this.newSubjectFacility.facilityId === null) {
        this.facilitySubject.forEach((f) => {
          let req = {
            facilityId: f.id,
            subjectId: this.subject.id,
          }
          requestAPI
            .post('http://localhost:8765/api/v1/admin-management/subject-facility-management', req)
            .then(() => {
              this.fetchSubjectFacility()
            })
        })
        message.success('Thêm bộ môn cơ sở thành công')
        this.ModalAdd = false
      } else {
        this.newSubjectFacility.facilityId.forEach((f) => {
          let req = {
            facilityId: f,
            subjectId: this.subject.id,
          }
          requestAPI
            .post('http://localhost:8765/api/v1/admin-management/subject-facility-management', req)
            .then(() => {
              this.fetchSubjectFacility()
            })
        })
        message.success('Thêm bộ môn cơ sở thành công')
        this.ModalAdd = false
      }
    },

    handleUpdateProject(record){
      this.ModalUpdate = true
    },

    handleDeleteSubjectFacility(record) {
      Modal.confirm({
        title: 'Xác nhận xóa',
        content: `Bạn có chắc chắn muốn xóa bộ môn ${record.subjectName} cơ sở ${record.facilityName}  không?`,
        onOk: () => {
          requestAPI
            .delete(`http://localhost:8765/api/v1/admin-management/subject-facility-management/${record.id}`)
            .then(() => {
              message.success('Xóa bộ môn thành công')
              this.fetchSubjectFacility()
            })
            .catch(() => {
            })
        },
      })
    },

    handleDetailSubjectFacility(record){
      this.ModalDetail = true
      requestAPI
        .get(
          `http://localhost:8765/api/v1/admin-management/subject-facility-management/${record.id}`,
        )
        .then((response) => {
          this.detailSubjectFacility = response.data.data
          console.log(this.detailSubjectFacility);
          
        })
        .catch(() => {
          message.error('Lỗi khi lấy dữ liệu combobox cơ sở')
        })
    },

    formatDate(timestamp) {
      if (!timestamp) return 'Không xác định'
      return new Date(timestamp).toLocaleString()
    },
  },
}
</script>

<style>
.filter-card {
  margin-bottom: 16px;
}

.filter-container {
  margin-bottom: 16px;
}

.filter-input {
  width: 100%;
}

.filter-select {
  width: 100%;
}

.filter-button {
  margin-left: 8px;
}

.cart {
  margin-top: 16px;
}
</style>
