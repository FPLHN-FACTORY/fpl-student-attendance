<script setup>
import { GLOBAL_ROUTE_NAMES } from '@/constants/routesConstant'
import { ROUTE_NAMES } from '@/router/studentRoute'
import { API_ROUTES_STUDENT } from '@/constants/studentConstant'
import requestAPI from '@/services/requestApiService'
import useBreadcrumbStore from '@/stores/useBreadCrumbStore'
import { DEFAULT_DATE_FORMAT, DEFAULT_PAGINATION, TYPE_SHIFT } from '@/constants'
import { onMounted, ref, reactive } from 'vue'
import useLoadingStore from '@/stores/useLoadingStore'
import { Modal, message } from 'ant-design-vue'
import ExcelJS from 'exceljs'
import { saveAs } from 'file-saver'
import { autoAddColumnWidth, dayOfWeek, formatDate } from '@/utils/utils'
import { FilterFilled, UnorderedListOutlined } from '@ant-design/icons-vue'

const breadcrumbStore = useBreadcrumbStore()

const breadcrumb = ref([
  { name: GLOBAL_ROUTE_NAMES.STAFF_PAGE, breadcrumbName: 'Sinh viên' },
  { name: ROUTE_NAMES.SCHEDULE, breadcrumbName: 'Lịch học' },
])
const loadingStore = useLoadingStore()
const isLoading = ref(false)
const isLoadingExport = ref(false)

const attendanceList = ref([])
const filter = reactive({
  page: 1,
  pageSize: 5,
  plan: 7, // Mặc định 7 ngày tới
})
const pagination = ref({ ...DEFAULT_PAGINATION })

const columns = autoAddColumnWidth([
  { title: '#', dataIndex: 'indexs', key: 'indexs' },
  { title: 'Ngày học', key: 'time' },
  { title: 'Ca', dataIndex: 'shift', key: 'shift' },
  { title: 'Nhóm xưởng', dataIndex: 'factoryName', key: 'factoryName' },
  { title: 'Dự án', dataIndex: 'projectName', key: 'projectName' },
  { title: 'Link học', dataIndex: 'link', key: 'link' },
  { title: 'Địa điểm', dataIndex: 'location', key: 'location' },
  { title: 'Tên giảng viên', dataIndex: 'staffName', key: 'staffName' },
  { title: 'Mô tả', dataIndex: 'description', key: 'description' },
])

const getTimeRange = () => {
  const now = Date.now()
  const offset = (filter.plan || 0) * 24 * 60 * 60 * 1000
  if (filter.plan >= 0) {
    return { now, max: now + offset }
  } else {
    return { now: now + offset, max: now }
  }
}

const fetchAttendanceList = () => {
  const { now, max } = getTimeRange()
  loadingStore.show()
  requestAPI
    .get(API_ROUTES_STUDENT.FETCH_DATA_STUDENT_PLAN + '/list', {
      params: {
        now,
        max,
        page: pagination.value.current,
        size: pagination.value.pageSize,
      },
    })
    .then(({ data }) => {
      attendanceList.value = data.data.data
      pagination.value.total = data.data.totalPages * pagination.value.pageSize
      pagination.value.current = filter.page
    })
    .catch((error) => {
      message.error(error.response?.data?.message || 'Lỗi khi lấy dữ liệu')
    })
    .finally(() => {
      loadingStore.hide()
    })
}

const handleTableChange = (pageInfo) => {
  pagination.value.current = pageInfo.current
  pagination.value.pageSize = pageInfo.pageSize
  filter.page = pageInfo.current
  filter.pageSize = pageInfo.pageSize
  fetchAttendanceList()
}

const handleShowDescription = (text) => {
  Modal.info({
    title: 'Nội dung buổi học',
    type: 'info',
    content: text || 'Buổi học chưa có nội dung',
    okText: 'Đóng',
    okButtonProps: { class: 'btn-gray' },
  })
}

const exportToExcel = async () => {
  isLoadingExport.value = true
  try {
    const wb = new ExcelJS.Workbook()
    const ws = wb.addWorksheet('DanhSach')

    // 1. Định nghĩa cột + độ rộng
    ws.columns = [
      { header: 'STT', key: 'stt', width: 6 },
      { header: 'Ngày điểm danh', key: 'day', width: 45 },
      { header: 'Ca', key: 'shift', width: 35 },
      { header: 'Nhóm xưởng', key: 'factoryName', width: 30 },
      { header: 'Dự án', key: 'projectName', width: 30 },
      { header: 'Tên môn học', key: 'subjectName', width: 30 },
      { header: 'Tên giảng viên', key: 'staffName', width: 30 },
      { header: 'Mô tả', key: 'description', width: 40 },
    ]

    // 2. Thêm dữ liệu (giữ nguyên logic cũ của bạn)
    attendanceList.value.forEach((item, idx) => {
      const dayString = `${dayOfWeek(item.attendanceDayStart)}, ${formatDate(
        item.attendanceDayStart,
        DEFAULT_DATE_FORMAT,
      )}`
      const timeRange = `${formatDate(item.attendanceDayStart, 'HH:mm')} - ${formatDate(
        item.attendanceDayEnd,
        'HH:mm',
      )}`

      ws.addRow({
        stt: idx + 1,
        day: `${dayString} ${timeRange}`,
        shift: `Ca ${item.shift}`,
        factoryName: item.factoryName,
        projectName: item.projectName,
        subjectName: item.subjectName,
        staffName: item.staffName,
        description: item.description || '',
      })
    })

    // 3. Style header
    const headerRow = ws.getRow(1)
    headerRow.font = { bold: true, size: 12 }
    headerRow.alignment = { vertical: 'middle', horizontal: 'center' }
    headerRow.eachCell((cell) => {
      cell.fill = {
        type: 'pattern',
        pattern: 'solid',
        fgColor: { argb: 'FFADD8E6' },
      }
    })

    // 4. Wrap text cho cột description
    ws.getColumn('description').alignment = { wrapText: true }

    // —— BẮT ĐẦU CHO READ-ONLY —— //

    // 5. Đánh dấu tất cả các ô là locked (mặc định locked=true, nhưng làm rõ lại)
    ws.eachRow((row) =>
      row.eachCell((cell) => {
        cell.protection = { locked: true }
      }),
    )


    await ws.protect('', {
      selectLockedCells: true,
      selectUnlockedCells: true,
      formatCells: false,
      formatRows: false,
      formatColumns: false,
      insertRows: false,
      deleteRows: false,
      // ... bạn có thể tắt thêm các quyền khác nếu cần
    })

    // —— KẾT THÚC READ-ONLY —— //

    // 7. Xuất file
    const buf = await wb.xlsx.writeBuffer()
    saveAs(new Blob([buf]), 'DiemDanh.xlsx')
  } catch (error) {
    message.error('Lỗi khi xuất Excel: ' + error.message)
  } finally {
    isLoadingExport.value = false
  }
}

const exportToPDF = () => {
  isLoadingExport.value = true
  const { now, max } = getTimeRange()

  loadingStore.show()
  requestAPI
    .get(API_ROUTES_STUDENT.FETCH_DATA_STUDENT_PLAN + '/export-pdf', {
      params: {
        now,
        max,
        page: pagination.value.current,
        size: pagination.value.pageSize,
      },
      responseType: 'blob',
    })
    .then((res) => {
      const blob = new Blob([res.data], { type: 'application/pdf' })
      const fileName = 'DiemDanh.pdf'
      saveAs(blob, fileName)
      message.success('Xuất file PDF thành công')
    })
    .catch((err) => {
      message.error(err.response?.data?.message || 'Lỗi khi xuất file PDF')
    })
    .finally(() => {
      loadingStore.hide()
      isLoadingExport.value = false
    })
}

const handleClearFilter = () => {
  filter.plan = 7
  filter.page = 1
  pagination.value.current = 1
  fetchAttendanceList()
}

onMounted(() => {
  breadcrumbStore.setRoutes(breadcrumb.value)
  fetchAttendanceList()
})
</script>

<template>
  <div class="container-fluid">
    <div class="row g-3">
      <div class="col-12">
        <a-card :bordered="false" class="card mb-3">
          <template #title><FilterFilled /> Bộ lọc</template>
          <div class="row g-3 filter-container">
            <div class="col-md-12">
              <div class="label-title">Lịch học:</div>
              <a-select
                v-model:value="filter.plan"
                placeholder="Chọn khoảng thời gian"
                allowClear
                class="w-100"
                @change="fetchAttendanceList"
              >
                <a-select-option :value="-90">90 ngày trước</a-select-option>
                <a-select-option :value="-30">30 ngày trước</a-select-option>
                <a-select-option :value="-14">14 ngày trước</a-select-option>
                <a-select-option :value="-7">7 ngày trước</a-select-option>
                <a-select-option :value="7">7 ngày tới</a-select-option>
                <a-select-option :value="14">14 ngày tới</a-select-option>
                <a-select-option :value="30">30 ngày tới</a-select-option>
                <a-select-option :value="90">90 ngày tới</a-select-option>
              </a-select>
            </div>
          </div>
          <div class="row">
            <div class="col-12">
              <div class="d-flex justify-content-center flex-wrap gap-2 mt-3">
                <a-button class="btn-light" @click="fetchAttendanceList">
                  <FilterFilled /> Lọc
                </a-button>
                <a-button class="btn-gray" @click="handleClearFilter"> Huỷ lọc </a-button>
              </div>
            </div>
          </div>
        </a-card>

        <a-card :bordered="false" class="cart">
          <template #title>
            <UnorderedListOutlined />
            Danh sách điểm danh
          </template>
          <div class="d-flex justify-content-end mb-3">
            <a-button type="primary" @click="exportToExcel" :loading="isLoadingExport" class="me-3">Tải xuống Excel</a-button>
            <a-button type="default" @click="exportToPDF" :loading="isLoadingExport">Tải xuống PDF</a-button>
          </div>

          <a-table
            class="nowrap"
            :dataSource="attendanceList"
            :columns="columns"
            :rowKey="'id'"
            :loading="isLoading"
            :scroll="{ x: 'auto' }"
            :pagination="pagination"
            @change="handleTableChange"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'time'">
                {{
                  `${dayOfWeek(record.attendanceDayStart)}, ${formatDate(
                    record.attendanceDayStart,
                    DEFAULT_DATE_FORMAT,
                  )}`
                }}
                {{
                  `${formatDate(record.attendanceDayStart, 'HH:mm')} - ${formatDate(
                    record.attendanceDayEnd,
                    'HH:mm',
                  )}`
                }}
              </template>
              <template v-else-if="column.dataIndex === 'shift'">
                <a-tag :color="record.type === 1 ? 'blue' : 'purple'">
                  {{
                    `Ca ${record.shift
                      .split(',')
                      .map((o) => Number(o))
                      .join(', ')} - ${TYPE_SHIFT[record.type]}`
                  }}
                </a-tag>
              </template>
              <template v-if="column.dataIndex === 'description'">
                <a-typography-link @click="handleShowDescription(record.description)"
                  >Chi tiết</a-typography-link
                >
              </template>
              <template v-else-if="column.dataIndex === 'link'">
                <a v-if="record.link" :href="record.link" target="_blank">{{ record.link }}</a>
              </template>
              <template v-if="column.dataIndex === 'factoryName'">
                <a-badge status="processing" :text="record.factoryName" />
              </template>
            </template>
          </a-table>
        </a-card>
      </div>
    </div>
  </div>
</template>
