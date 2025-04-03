<script>
import {
  SearchOutlined,
  PlusOutlined,
  EyeOutlined,
  EditOutlined,
  DeleteOutlined,
  FilterFilled,
  UnorderedListOutlined,
  EyeFilled,
  EditFilled,
  DeleteFilled,
} from '@ant-design/icons-vue'
import { message, Modal } from 'ant-design-vue'
import requestAPI from '@/services/requestApiService'
import { API_ROUTES_STAFF } from '@/constants/staffConstant'

export default {
  components: {
    SearchOutlined,
    PlusOutlined,
    EyeOutlined,
    EditOutlined,
    DeleteOutlined,
    FilterFilled,
    UnorderedListOutlined,
    EyeFilled,
    EditFilled,
    DeleteFilled,
  },
  data() {
    return {
      //Danh sách cấp dự án
      levels: [],
      //Danh sách dự án
      projects: [],
      //Danh sách học kỳ
      semesters: [],
      //Danh sách môn học cơ sở
      subjects: [],
      //Trạng thái modal
      modalAdd: false,
      modalDetail: false,
      modalEdit: false,
      //Dữ liệu dự án mới
      newProject: {
        name: '',
        description: '',
        levelProjectId: null,
        semesterId: null,
        subjectFacilityId: null,
      },
      //Dữ liệu dự án để xem và sửa
      detailProject: {},
      //Dữ liệu bộ lọc
      filter: {
        name: null,
        status: null,
        page: 1,
        pageSize: 5,
        semesterId: null,
        subjectId: null,
        levelProjectId: null,
      },
      //Cấu hình cột trong bảng
      columns: [
        { title: '#', dataIndex: 'indexs', key: 'indexs' },
        { title: 'Tên', dataIndex: 'name', key: 'name' },
        { title: 'Cấp dự án', dataIndex: 'nameLevelProject', key: 'nameLevelProject' },
        { title: 'Học kỳ', dataIndex: 'nameSemester', key: 'nameSemester' },
        { title: 'Môn học', dataIndex: 'nameSubject', key: 'nameSubject' },
        { title: 'Mô tả', dataIndex: 'description', key: 'description' },
        { title: 'Trạng thái', dataIndex: 'status', key: 'status' },
        { title: 'Chức năng', key: 'actions' },
      ],
      //Cấu hình phân trang
      pagination: {
        current: 1,
        pageSize: 5,
        total: 0,
      },
    }
  },

  mounted() {
    this.fetchProjects()
    this.fetchLevelCombobox()
    this.fetchSemesters()
    this.fetchSubjects()
  },

  methods: {
    //Chuyển đổi thời gian
    formatDate(timestamp) {
      if (!timestamp) return 'Không xác định'
      return new Date(timestamp).toLocaleString()
    },

    //Hiển thị dữ liệu dự án
    fetchProjects() {
      requestAPI
        .post(`${API_ROUTES_STAFF.FETCH_DATA_PROJECT}/list`, this.filter)
        .then((response) => {
          this.projects = response.data.data.data
          this.pagination.total = response.data.data.totalPages * this.filter.pageSize
          this.pagination.current = response.data.data.currentPage + 1
        })
        .catch(() => {
          message.error('Lỗi khi lấy dữ liệu')
        })
    },

    //Hiển thị dữ liệu combobox
    fetchLevelCombobox() {
      requestAPI
        .get(`${API_ROUTES_STAFF.FETCH_DATA_PROJECT}/level-combobox`)
        .then((response) => {
          this.levels = response.data
        })
        .catch(() => {
          message.error('Lỗi khi lấy dữ liệu combobox cấp dự án')
        })
    },

    //Hiển thị dữ liệu combobox
    fetchSemesters() {
      requestAPI
        .get(`${API_ROUTES_STAFF.FETCH_DATA_PROJECT}/semester-combobox`)
        .then((response) => {
          this.semesters = response.data
        })
        .catch(() => {
          message.error('Lỗi khi lấy dữ liệu combobox học kỳ')
        })
    },

    //Hiển thị dữ liệu combobox
    fetchSubjects() {
      requestAPI
        .get(`${API_ROUTES_STAFF.FETCH_DATA_PROJECT}/subject-facility-combobox`)
        .then((response) => {
          this.subjects = response.data
        })
        .catch(() => {
          message.error('Lỗi khi lấy dữ liệu combobox môn học')
        })
    },

    //Phân trang
    handleTableChange(pagination) {
      this.filter.page = pagination.current
      this.filter.pageSize = pagination.pageSize
      this.fetchProjects()
    },

    //Mở modal thêm
    showAddModal() {
      this.modalAdd = true
    },

    //Chức năng thêm
    handleAddProject() {
      if (!this.newProject.name) {
        message.error('Tên dự án không được bỏ trống')
        return
      }
      if (!this.newProject.levelProjectId) {
        message.error('Phải chọn cấp dự án')
        return
      }
      if (!this.newProject.semesterId) {
        message.error('Phải chọn học kỳ')
        return
      }
      if (!this.newProject.subjectFacilityId) {
        message.error('Phải chọn môn học')
        return
      }
      requestAPI
        .post(API_ROUTES_STAFF.FETCH_DATA_PROJECT, this.newProject)
        .then(() => {
          message.success('Thêm dự án thành công')
          this.fetchProjects()
          this.resetForm()
          this.modalAdd = false
        })
        .catch(() => {
          message.error('Lỗi khi thêm dự án')
        })
    },

    //Chức năng xem
    handleDetailProject(record) {
      requestAPI
        .get(`${API_ROUTES_STAFF.FETCH_DATA_PROJECT}/${record.id}`)
        .then((response) => {
          this.detailProject = response.data.data
          this.modalDetail = true
        })
        .catch(() => {
          message.error('Lỗi khi lấy thông tin')
        })
    },

    //Mở modal sửa
    handleEditProject(record) {
      requestAPI
        .get(`${API_ROUTES_STAFF.FETCH_DATA_PROJECT}/${record.id}`)
        .then((response) => {
          this.detailProject = response.data.data
          this.modalEdit = true
        })
        .catch(() => {
          message.error('Lỗi khi lấy thông tin')
        })
    },

    //Chức năng sửa
    handleUpdateProject() {
      if (!this.detailProject.name) {
        message.error('Tên dự án không được bỏ trống')
        return
      }
      if (!this.detailProject.levelProjectId) {
        message.error('Phải chọn cấp dự án')
        return
      }
      if (!this.detailProject.semesterId) {
        message.error('Phải chọn học kỳ')
        return
      }
      if (!this.detailProject.subjectFacilityId) {
        message.error('Phải chọn môn học')
        return
      }
      let req = {
        name: this.detailProject.name,
        description: this.detailProject.description,
        idLevelProject: this.detailProject.levelProjectId,
        idSemester: this.detailProject.semesterId,
        idSubjectFacility: this.detailProject.subjectFacilityId,
      }
      requestAPI
        .put(`${API_ROUTES_STAFF.FETCH_DATA_PROJECT}/${this.detailProject.id}`, req)
        .then(() => {
          message.success('Cập nhật dự án thành công')
          this.modalEdit = false
          this.fetchProjects()
        })
        .catch(() => {
          message.error('Lỗi khi cập nhật dự án')
        })
    },

    //Chức năng xóa
    handleDeleteProject(record) {
      Modal.confirm({
        title: 'Xác nhận',
        content: 'Bạn có chắc chắn muốn thay đổi trạng thái của dự án này?',
        onOk: () => {
          requestAPI
            .delete(`${API_ROUTES_STAFF.FETCH_DATA_PROJECT}/${record.id}`)
            .then(() => {
              message.success('Thay đổi trạng thái dự án thành công')
              this.fetchProjects()
            })
            .catch(() => {
              message.error('Lỗi khi xóa dự án')
            })
        },
      })
    },

    //Clear
    resetForm() {
      this.newProject = {
        name: '',
        description: '',
        levelProjectId: null,
        semesterId: null,
        subjectFacilityId: null,
      }
      this.detailProject = {}
      this.modalAdd = false
      this.modalEdit = false
      this.modalDetail = false
    },
  },
}
</script>

<template>
  <div class="container-fluid">
    <!-- Header -->
    <div class="row g-3">
      <div class="col-12">
        <h1>Quản lý dự án</h1>
      </div>
    </div>

    <!-- Card lọc dự án -->
    <div class="row g-3">
      <div class="col-12">
        <a-card :bordered="false" class="filter-card">
          <template #title> <FilterFilled /> Bộ lọc </template>
          <a-row :gutter="16" class="filter-container">
            <a-col :span="8" class="col">
              <a-input
                v-model:value="filter.name"
                placeholder="Tìm kiếm theo tên"
                allowClear
                class="filter-input"
                @change="fetchProjects"
              />
            </a-col>
            <a-col :span="4" class="col">
              <a-select
                v-model:value="filter.levelProjectId"
                placeholder="Cấp dự án"
                allowClear
                class="filter-select"
                :dropdownMatchSelectWidth="false"
                @change="fetchProjects"
              >
                <a-select-option :value="null">Tất cả cấp dự án</a-select-option>
                <a-select-option v-for="level in levels" :key="level.id" :value="level.id">
                  {{ level.name }}
                </a-select-option>
              </a-select>
            </a-col>
            <a-col :span="4" class="col">
              <a-select
                v-model:value="filter.semesterId"
                placeholder="Học kỳ"
                allowClear
                class="filter-select"
                :dropdownMatchSelectWidth="false"
                @change="fetchProjects"
              >
                <a-select-option :value="null">Tất cả học kỳ</a-select-option>
                <a-select-option
                  v-for="semester in semesters"
                  :key="semester.id"
                  :value="semester.id"
                >
                  {{ semester.code }}
                </a-select-option>
              </a-select>
            </a-col>
            <a-col :span="4" class="col">
              <a-select
                v-model:value="filter.subjectId"
                placeholder="Môn học"
                allowClear
                class="filter-select"
                :dropdownMatchSelectWidth="false"
                @change="fetchProjects"
              >
                <a-select-option :value="null">Tất cả môn học</a-select-option>
                <a-select-option v-for="subject in subjects" :key="subject.id" :value="subject.id">
                  {{ subject.name }}
                </a-select-option>
              </a-select>
            </a-col>
            <a-col :span="4" class="col">
              <a-select
                v-model:value="filter.status"
                placeholder="Trạng thái"
                allowClear
                class="filter-select"
                :dropdownMatchSelectWidth="false"
                @change="fetchProjects"
              >
                <a-select-option :value="null">Tất cả trạng thái</a-select-option>
                <a-select-option :value="1">Đang triển khai</a-select-option>
                <a-select-option :value="0">Không hoạt động</a-select-option>
              </a-select>
            </a-col>
          </a-row>
        </a-card>
      </div>
    </div>

    <!-- Card danh sách dự án -->
    <div class="row g-3">
      <div class="col-12">
        <a-card :bordered="false" class="cart">
          <template #title> <UnorderedListOutlined /> Danh sách dự án </template>
          <div class="d-flex justify-content-end mb-3">
            <a-button type="primary" @click="showAddModal(true)"> <PlusOutlined /> Thêm </a-button>
          </div>
          <a-table
            :dataSource="projects"
            :columns="columns"
            rowKey="id"
            :pagination="pagination"
            @change="handleTableChange"
          >
            <template #bodyCell="{ column, record }">
              <!-- Hiển thị trạng thái -->
              <template v-if="column.dataIndex === 'status'">
                <span class="nowrap">
                  <a-switch
                    class="me-2"
                    :checked="record.status == 'ACTIVE' || record.status == 1"
                    @change="handleDeleteProject(record)"
                  />
                  <a-tag :color="record.status == 1 ? 'green' : 'red'">
                    {{ record.status == 1 ? 'Đang triển khai' : 'Không hoạt động' }}
                  </a-tag>
                </span>
              </template>
              <!-- Các nút chức năng -->
              <template v-else-if="column.key === 'actions'">
                <a-space>
                  <a-button
                    @click="handleDetailProject(record)"
                    type="text"
                    class="btn-outline-primary"
                  >
                    <EyeFilled />
                  </a-button>
                  <a-button @click="handleEditProject(record)" type="text" class="btn-outline-info">
                    <EditFilled />
                  </a-button>
                </a-space>
              </template>
              <template v-else>
                {{ record[column.dataIndex] }}
              </template>
            </template>
          </a-table>
        </a-card>
      </div>
    </div>

    <!-- Modal thêm dự án -->
    <a-modal v-model:open="modalAdd" title="Thêm dự án" @ok="handleAddProject" @cancel="resetForm">
      <a-form layout="vertical">
        <a-form-item label="Tên dự án" required>
          <a-input v-model:value="newProject.name" placeholder="Nhập tên dự án" />
        </a-form-item>
        <a-form-item label="Mô tả">
          <a-textarea v-model:value="newProject.description" placeholder="Nhập mô tả" />
        </a-form-item>
        <a-form-item label="Cấp dự án" required>
          <a-select
            v-model:value="newProject.levelProjectId"
            placeholder="Chọn cấp dự án"
            allowClear
          >
            <a-select-option v-for="level in levels" :key="level.id" :value="level.id">
              {{ level.name }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="Học kỳ" required>
          <a-select v-model:value="newProject.semesterId" placeholder="Chọn học kỳ" allowClear>
            <a-select-option v-for="semester in semesters" :key="semester.id" :value="semester.id">
              {{ semester.name }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="Môn học" required>
          <a-select
            v-model:value="newProject.subjectFacilityId"
            placeholder="Chọn môn học"
            allowClear
          >
            <a-select-option v-for="subject in subjects" :key="subject.id" :value="subject.id">
              {{ subject.name }}
            </a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- Modal xem chi tiết dự án -->
    <a-modal v-model:open="modalDetail" title="Chi tiết dự án" footer="">
      <p><strong>Tên:</strong> {{ detailProject.name }}</p>
      <p><strong>Cấp dự án:</strong> {{ detailProject.nameLevelProject }}</p>
      <p><strong>Học kỳ:</strong> {{ detailProject.nameSemester }}</p>
      <p><strong>Môn học:</strong> {{ detailProject.nameSubject }}</p>
      <p><strong>Mô tả:</strong> {{ detailProject.description }}</p>
      <p v-if="detailProject.createdAt">
        <strong>Ngày tạo:</strong> {{ formatDate(detailProject.createdAt) }}
      </p>
      <p v-if="detailProject.updatedAt">
        <strong>Ngày sửa:</strong> {{ formatDate(detailProject.updatedAt) }}
      </p>
      <p><strong>Số nhóm xưởng: COMING SOON</strong></p>
      <p><strong>Giảng viên: COMING SOON</strong></p>
      <p>
        <strong>Trạng thái:</strong>
        <a-tag :color="detailProject.status === 'ACTIVE' ? 'green' : 'red'">
          {{ detailProject.status === 'ACTIVE' ? 'Hoạt động' : 'Không hoạt động' }}
        </a-tag>
      </p>
    </a-modal>

    <!-- Modal sửa dự án -->
    <a-modal v-model:open="modalEdit" title="Sửa dự án" @ok="handleUpdateProject">
      <a-form layout="vertical">
        <a-form-item label="Tên dự án" required>
          <a-input v-model:value="detailProject.name" placeholder="Nhập tên dự án" />
        </a-form-item>
        <a-form-item label="Mô tả">
          <a-textarea v-model:value="detailProject.description" placeholder="Nhập mô tả" />
        </a-form-item>
        <a-form-item label="Cấp dự án" required>
          <a-select
            v-model:value="detailProject.levelProjectId"
            placeholder="Chọn cấp dự án"
            allowClear
          >
            <a-select-option v-for="level in levels" :key="level.id" :value="level.id">
              {{ level.name }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="Học kỳ" required>
          <a-select v-model:value="detailProject.semesterId" placeholder="Chọn học kỳ" allowClear>
            <a-select-option v-for="semester in semesters" :key="semester.id" :value="semester.id">
              {{ semester.name }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="Môn học" required>
          <!-- Sửa ở đây: sử dụng subjectFacilityId thay vì subjectId -->
          <a-select
            v-model:value="detailProject.subjectFacilityId"
            placeholder="Chọn môn học"
            allowClear
          >
            <a-select-option v-for="subject in subjects" :key="subject.id" :value="subject.id">
              {{ subject.name }}
            </a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>
