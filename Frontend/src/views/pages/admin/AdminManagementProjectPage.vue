<template>
  <h1>Quản lý dự án</h1>

  <!-- Card lọc dự án -->
  <a-card title="Bộ lọc" :bordered="false" class="filter-card">
    <a-row :gutter="16" class="filter-container">
      <a-col :span="8">
        <a-input
          v-model:value="filter.name"
          placeholder="Tìm kiếm theo tên"
          allowClear
          class="filter-input"
          @change="fetchProjects"
        />
      </a-col>

      <a-col :span="4">
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

      <a-col :span="4">
        <a-select
          v-model:value="filter.semesterId"
          placeholder="Học kỳ"
          allowClear
          class="filter-select"
          :dropdownMatchSelectWidth="false"
          @change="fetchProjects"
        >
          <a-select-option :value="null">Tất cả học kỳ</a-select-option>
          <a-select-option v-for="semester in semesters" :key="semester.id" :value="semester.id">
            {{ semester.name }}
          </a-select-option>
        </a-select>
      </a-col>

      <a-col :span="4">
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

      <a-col :span="4">
        <a-select
          v-model:value="filter.status"
          placeholder="Trạng thái"
          allowClear
          class="filter-select"
          :dropdownMatchSelectWidth="false"
          @change="fetchProjects"
        >
          <a-select-option :value="null">Tất cả trạng thái</a-select-option>
          <a-select-option :value="1">Hoạt động</a-select-option>
          <a-select-option :value="0">Không hoạt động</a-select-option>
        </a-select>
      </a-col>
    </a-row>
  </a-card>

  <!-- Card danh sách dự án -->
  <a-card title="Danh sách dự án" :bordered="false" class="cart">
    <div style="display: flex; justify-content: flex-end; margin-bottom: 10px">
      <a-button
        style="
          background-color: #fff7e6;
          color: black;
          border: 1px solid #ffa940;
          margin-right: 10px;
        "
        @click="showAddModal(true)"
      >
        <PlusOutlined />
        Thêm
      </a-button>

      <a-button
        style="
          background-color: #fff7e6;
          color: black;
          border: 1px solid #ffa940;
          margin-right: 10px;
        "
        @click="downloadTemplate()"
      >
        <DownloadOutlined />
        Download Template
      </a-button>

      <a-button
        style="background-color: #fff7e6; color: black; border: 1px solid #ffa940"
        @click="importExcel"
      >
        <ToTopOutlined />
        Import Excel
      </a-button>
    </div>

    <a-table
      :dataSource="projects"
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
            @click="handleDetailProject(record)"
            type="text"
            :style="{ backgroundColor: '#FFF7E6', marginRight: '8px', border: '1px solid #ffa940' }"
          >
            <EyeOutlined />
          </a-button>

          <a-button
            @click="handleEditProject(record)"
            type="text"
            :style="{ backgroundColor: '#FFF7E6', marginRight: '8px', border: '1px solid #ffa940' }"
          >
            <EditOutlined />
          </a-button>

          <a-button
            @click="handleDeleteProject(record)"
            type="text"
            :style="{ backgroundColor: '#FFF7E6', border: '1px solid #ffa940' }"
          >
            <DeleteOutlined />
          </a-button>
        </template>
      </template>
    </a-table>
  </a-card>

  <!-- Modal thêm dự án -->
  <a-modal v-model:visible="modalAdd" title="Thêm dự án" @ok="handleAddProject" @cancel="resetForm">
    <a-form layout="vertical">
      <a-form-item label="Tên dự án" required>
        <a-input v-model:value="newProject.name" placeholder="Nhập tên dự án" />
      </a-form-item>

      <a-form-item label="Mô tả">
        <a-textarea v-model:value="newProject.description" placeholder="Nhập mô tả" />
      </a-form-item>

      <a-form-item label="Cấp dự án" required>
        <a-select v-model:value="newProject.levelProjectId" placeholder="Chọn cấp dự án" allowClear>
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
          v-model:value="newProject.subjectId"
          placeholder="Chọn môn học"
          allowClear
          @change="onSubjectChange"
        >
          <a-select-option v-for="subject in subjects" :key="subject.id" :value="subject.id">
            {{ subject.name }}
          </a-select-option>
        </a-select>
      </a-form-item>

      <a-form-item label="Cơ sở" required>
        <a-select v-model:value="newProject.facilityId" placeholder="Chọn cơ sở" allowClear>
          <a-select-option v-for="f in facilities" :key="f.id" :value="f.id">
            {{ f.name }}
          </a-select-option>
        </a-select>
      </a-form-item>
    </a-form>
  </a-modal>

  <!-- Modal xem chi tiết dự án -->
  <a-modal v-model:visible="modalDetail" title="Chi tiết dự án" footer="">
    <p><strong>Tên:</strong> {{ detailProject.name }}</p>
    <p><strong>Cấp dự án:</strong> {{ detailProject.nameLevelProject }}</p>
    <p><strong>Học kỳ:</strong> {{ detailProject.nameSemester }}</p>
    <p><strong>Môn học:</strong> {{ detailProject.nameSubject }}</p>
    <p><strong>Mô tả:</strong> {{ detailProject.description }}</p>
    <!-- Nếu createdAt và updatedAt không được trả về, bạn có thể loại bỏ hoặc xử lý kiểm tra tồn tại -->
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
  <a-modal v-model:visible="modalEdit" title="Sửa dự án" @ok="handleUpdateProject">
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
        <a-select v-model:value="detailProject.subjectId" placeholder="Chọn môn học" allowClear>
          <a-select-option v-for="subject in subjects" :key="subject.id" :value="subject.id">
            {{ subject.name }}
          </a-select-option>
        </a-select>
      </a-form-item>

      <a-form-item label="Trạng thái">
        <a-select v-model:value="detailProject.status" placeholder="Chọn trạng thái">
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
  DownloadOutlined,
  ToTopOutlined,
} from '@ant-design/icons-vue'
import { message, Modal } from 'ant-design-vue'
import requestAPI from '@/services/requestApiService'
import { API_ROUTES_ADMIN } from '@/constants/adminConstant'

export default {
  components: {
    SearchOutlined,
    PlusOutlined,
    EyeOutlined,
    EditOutlined,
    DeleteOutlined,
    DownloadOutlined,
    ToTopOutlined,
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
      //
      facilities: [],
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
        subjectId: null,
        facilityId: null,
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
        facilityId: null,
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
        .post(`${API_ROUTES_ADMIN.FETCH_DATA_PROJECT}/list`, this.filter)
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
        .get(`${API_ROUTES_ADMIN.FETCH_DATA_PROJECT}/level-combobox`)
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
        .get(`${API_ROUTES_ADMIN.FETCH_DATA_PROJECT}/semester-combobox`)
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
        .get(`${API_ROUTES_ADMIN.FETCH_DATA_PROJECT}/subject-combobox-add`)
        .then((response) => {
          this.subjects = response.data
        })
        .catch(() => {
          message.error('Lỗi khi lấy dữ liệu combobox môn học')
        })
    },

    fetchFacilities(subjectId) {
      requestAPI
        .get(`${API_ROUTES_ADMIN.FETCH_DATA_PROJECT}/facility-combobox/${subjectId}`)
        .then((response) => {
          this.facilities = response.data
        })
        .catch(() => {
          message.error('Lỗi khi lấy dữ liệu combobox cơ sở')
        })
    },

    onSubjectChange() {
      // Gọi hàm fetchFacilities khi subjectId thay đổi
      if (this.newProject.subjectId) {
        this.fetchFacilities(this.newProject.subjectId)
      } else {
        this.facilities = [] // Reset facilities if no subject is selected
      }
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
      if (!this.newProject.subjectId) {
        message.error('Phải chọn môn học')
        return
      }
      requestAPI
        .post(API_ROUTES_ADMIN.FETCH_DATA_PROJECT, this.newProject)
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
        .get(`${API_ROUTES_ADMIN.FETCH_DATA_PROJECT}/${record.id}`)
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
        .get(`${API_ROUTES_ADMIN.FETCH_DATA_PROJECT}/${record.id}`)
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
      if (!this.detailProject.subjectId) {
        message.error('Phải chọn môn học')
        return
      }
      if (!this.detailProject.status) {
        message.error('Phải chọn trạng thái')
        return
      }
      let req = {
        name: this.detailProject.name,
        description: this.detailProject.description,
        idLevelProject: this.detailProject.levelProjectId,
        idSemester: this.detailProject.semesterId,
        idSubject: this.detailProject.subjectId,
        status: this.detailProject.status,
      }
      requestAPI
        .put(`${API_ROUTES_ADMIN.FETCH_DATA_PROJECT}/${this.detailProject.id}`, req)
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
        content: 'Bạn có chắc chắn muốn xóa dự án này?',
        onOk: () => {
          requestAPI
            .delete(`${API_ROUTES_ADMIN.FETCH_DATA_PROJECT}/${record.id}`)
            .then(() => {
              message.success('Xóa dự án thành công')
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

    //Download template
    downloadTemplate() {
      requestAPI({
        url: `${API_ROUTES_ADMIN.FETCH_DATA_PROJECT}/download-template`,
        method: 'GET',
        responseType: 'blob',
      })
        .then((response) => {
          const url = window.URL.createObjectURL(new Blob([response.data]))
          const link = document.createElement('a')
          link.href = url
          link.setAttribute('download', 'Mẫu import.xlsx')
          document.body.appendChild(link)
          link.click()
          message.success('Tải xuống mẫu thành công')
        })
        .catch(() => {
          message.error('Lỗi khi tải xuống mẫu')
        })
    },

    importExcel() {
      // Tạo một input để chọn tệp Excel
      const input = document.createElement('input')
      input.type = 'file'
      input.accept = '.xlsx, .xls' // Chỉ cho phép tệp Excel

      input.onchange = (event) => {
        const file = event.target.files[0]
        if (file) {
          const formData = new FormData()
          formData.append('file', file)

          requestAPI
            .post(`${API_ROUTES_ADMIN.FETCH_DATA_PROJECT}/import-excel`, formData)
            .then(() => {
              message.success('Nhập Excel thành công')
              this.fetchProjects()
            })
            .catch(() => {
              message.error('Lỗi khi nhập Excel')
            })
        }
      }

      input.click() // Kích hoạt input file
    },
  },
}
</script>

<style scoped>
.filter-card {
  margin-bottom: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.filter-container {
  display: flex;
  align-items: center;
}

.filter-input,
.filter-select {
  width: 100%;
}

.filter-select .ant-select-selector {
  height: 32px !important;
}

.filter-button {
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 500;
}
</style>
