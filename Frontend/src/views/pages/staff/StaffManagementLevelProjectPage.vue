<template>
  <h1>Quản lý cấp dự án</h1>

  <!-- Bộ lọc -->
  <a-card title="Bộ lọc" :bordered="false" class="cart">
    <a-row :gutter="16" class="filter-container">
      <a-col :span="8">
        <a-input v-model:value="filter.name" placeholder="Tìm kiếm theo tên" allowClear />
      </a-col>
      <a-col :span="8">
        <a-select v-model:value="filter.status" placeholder="Chọn trạng thái" allowClear>
          <a-select-option :value="''">Tất cả trạng thái</a-select-option>
          <a-select-option :value="1">Hoạt động</a-select-option>
          <a-select-option :value="0">Không hoạt động</a-select-option>
        </a-select>
      </a-col>
      <a-col :span="8">
        <a-button 
          @click="fetchLevels"
          style="background-color: #FFF7E6; color: black; border: 1px solid #FFA940"
        >
          <SearchOutlined /> Lọc
        </a-button>
      </a-col>
    </a-row>
  </a-card>

  <!-- Danh sách cấp dự án -->
  <a-card title="Danh sách cấp dự án" :bordered="false" class="cart">
    <div style="display: flex; justify-content: flex-end; margin-bottom: 10px;">
      <a-button style="background-color: #FFF7E6; color: black; border: 1px solid #FFA940">
        <PlusOutlined /> Thêm
      </a-button>
    </div>

    <a-table :dataSource="levels" :columns="columns" :rowKey="'id'" bordered>
      <template #bodyCell="{ column, record }">
        <!-- Hiển thị trạng thái -->
        <template v-if="column.dataIndex === 'status'">
          <span :style="{ color: record.status === 1 ? 'green' : 'red' }">
            {{ record.status === 1 ? 'Hoạt động' : 'Không hoạt động' }}
          </span>
        </template>

        <!-- Chức năng: Xem, Sửa, Xóa -->
        <template v-if="column.key === 'actions'">
          <a-button 
            type="primary" 
            style="background-color: #FFF7E6; color: black; border: 1px solid #FFA940"
            @click="viewRecord(record)"
          >
            <EyeOutlined /> 
          </a-button>

          <a-button 
            type="primary" 
            style="background-color: #FFF7E6; color: black; border: 1px solid #FFA940; margin-left: 5px"
            @click="editRecord(record)"
          >
            <EditOutlined /> 
          </a-button>

          <a-button 
            type="primary" 
            style="background-color: #FFF7E6; color: red; border: 1px solid #FFA940; margin-left: 5px"
            @click="deleteRecord(record)"
          >
            <DeleteOutlined /> 
          </a-button>

        </template>
      </template>
    </a-table>
  </a-card>
</template>

<script>
import { SearchOutlined, PlusOutlined, EyeOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons-vue';
import axios from "axios";
import { message } from "ant-design-vue";
import requestAPI from "@/services/requestApiService";

export default {
  components: {
    SearchOutlined,
    PlusOutlined,
    EyeOutlined,
    EditOutlined,
    DeleteOutlined
  },
  data() {
    return {
      levels: [],
      filter: {
        name: "",
        status: "",
        page: 1,
      },
      columns: [
        { title: "#", dataIndex: "indexs", key: "indexs" },
        { title: "Tên", dataIndex: "name", key: "name" },
        { title: "Mã", dataIndex: "code", key: "code" },
        { title: "Mô tả", dataIndex: "description", key: "description" },
        { 
          title: "Trạng thái", 
          dataIndex: "status", 
          key: "status"
        },
        { 
          title: "Chức năng", 
          key: "actions"
        }
      ]
    };
  },
  mounted() {
    this.fetchLevels();
  },
  methods: {
    fetchLevels() {
      requestAPI
        .post("http://localhost:8765/api/v1/staff-management/level-project-management/list", this.filter)
        .then(response => {
          this.levels = response.data.data.data;
        })
        .catch(() => {
          message.error("Lỗi khi lấy dữ liệu");
        });
    },
    viewRecord(record) {
      console.log("Xem thông tin:", record);
    },
    editRecord(record) {
      console.log("Chỉnh sửa:", record);
    },
    deleteRecord(record) {
      console.log("Xóa:", record);
    }
  }
};
</script>

<style>
.cart {
  margin-top: 30px;
}

.filter-container {
  margin-bottom: 15px;
}
</style>
