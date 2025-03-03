<template>
  <span>Đây là quản lý cơ sở</span>
  <a-table :columns="columns" :data-source="data">
    <template #headerCell="{ column }">
        <span>
          <smile-outlined />
          {{ column.name }}
        </span>
    </template>

    <template #bodyCell="{ column, record }">
      <template v-if="column.key === 'name'">
        <a>
          {{ record.name }}
        </a>
      </template>
      <template v-else-if="column.key === 'tags'">
        <span>
          <a-tag
            v-for="tag in record.tags"
            :key="tag"
            :color="tag === 'loser' ? 'volcano' : tag.length > 5 ? 'geekblue' : 'green'"
          >
            {{ tag.toUpperCase() }}
          </a-tag>
        </span>
      </template>
      <template v-else-if="column.key === 'action'">
        <span>
          <a>Invite 一 {{ record.name }}</a>
          <a-divider type="vertical" />
          <a>Delete</a>
          <a-divider type="vertical" />
          <a class="ant-dropdown-link">
            More actions
            <down-outlined />
          </a>
        </span>
      </template>
    </template>
  </a-table>
</template>

<script setup>
import requestAPI from '@/services/requestApiService'
import { API_ROUTES_ADMIN } from '@/constants/adminConstant'
import { computed, ref } from 'vue'



const columns = ref([
  {
    name: 'Tencoso',
    dataIndex: 'facilityName',
    key: 'facilityName',
  },
    {
    name: 'Macoso',
    dataIndex: 'facilityCode',
    key: 'facilityCode',
  },
])

const data = ref([])

const fetchData = async () => {
  try {
    const response = await requestAPI.get(API_ROUTES_ADMIN.FETCH_DATA_FACILITY)
    data.value = response.data.data.data
    } catch (error) {
    console.log('Không thể tải danh sách cơ sở')
  }
}

</script>
