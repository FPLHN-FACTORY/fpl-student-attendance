<script setup>
import { ref, onMounted, watch, reactive, h, computed } from 'vue'
import {
  PlusOutlined,
  FilterFilled,
  UnorderedListOutlined,
  SearchOutlined,
  EditFilled,
  DeleteFilled,
  EyeFilled,
  ExclamationCircleOutlined,
  LinkOutlined,
  InfoCircleFilled,
  MailOutlined,
} from '@ant-design/icons-vue'
import { message, Modal } from 'ant-design-vue'
import requestAPI from '@/services/requestApiService'
import { DEFAULT_PAGINATION, STATUS_TYPE, TYPE_SHIFT } from '@/constants'
import { API_ROUTES_STAFF } from '@/constants/staffConstant'
import useBreadcrumbStore from '@/stores/useBreadCrumbStore'
import { API_ROUTES_EXCEL, GLOBAL_ROUTE_NAMES } from '@/constants/routesConstant'
import { ROUTE_NAMES } from '@/router/staffRoute'
import useLoadingStore from '@/stores/useLoadingStore'
import { useRoute, useRouter } from 'vue-router'
import {
  DEFAULT_DATE_FORMAT,
  DEFAULT_LATE_ARRIVAL,
  SHIFT,
  STATUS_PLAN_DATE_DETAIL,
} from '@/constants'
import {
  autoAddColumnWidth,
  colorDayOfWeek,
  dayOfWeek,
  debounce,
  formatDate,
  getShiftTimeEnd,
  getShiftTimeStart,
  rowSelectTable,
} from '@/utils/utils'
import dayjs from 'dayjs'
import ExcelUploadButton from '@/components/excel/ExcelUploadButton.vue'
import 'vue-multiselect/dist/vue-multiselect.css'

const route = useRoute()
const router = useRouter()

const breadcrumbStore = useBreadcrumbStore()
const loadingStore = useLoadingStore()

const isLoading = ref(false)
const isShowListStudentExists = ref(false)
const lstStudentExists = ref([])

const _detail = ref(null)
const lstData = ref([])
const lstShift = ref([])
const lstTeacher = ref([])
const teacher = ref(null)
const loadingTeacher = ref(false)

const isActive = computed(() => _detail.value?.status === STATUS_TYPE.ENABLE)

const configImportExcel = reactive({
  fetchUrl: API_ROUTES_EXCEL.FETCH_IMPORT_PLAN_DATE,
  onSuccess: () => {
    fetchDataList()
  },
  onError: () => {
    message.error('Không thể xử lý file excel')
  },
  data: { idPlanFactory: route.params?.id },
  showDownloadTemplate: isActive,
  showHistoryLog: true,
  showImport: isActive,
  showExport: true,
  btnImport: 'Import ca',
  btnExport: 'Export điểm danh',
})

const modalAddOrUpdate = reactive({
  isShow: false,
  isLoading: false,
  title: null,
  cancelText: 'Hủy bỏ',
  okText: 'Xác nhận',
  onOk: null,
  width: 800,
})

const modalUpdateLink = reactive({
  isShow: false,
  isLoading: false,
  title: null,
  cancelText: 'Hủy bỏ',
  okText: 'Xác nhận',
  onOk: null,
  width: 800,
})

const columns = ref(
  autoAddColumnWidth([
    { title: 'Buổi', dataIndex: 'orderNumber', key: 'orderNumber', align: 'center' },
    { title: '', dataIndex: 'day', align: 'center' },
    { title: 'Ngày diễn ra', dataIndex: 'startDate', key: 'startDate' },
    { title: 'Số ca', dataIndex: 'totalShift', key: 'totalShift', align: 'center' },
    { title: 'Ca diễn ra', dataIndex: 'shift' },
    { title: 'Hình thức', dataIndex: 'type' },
    { title: 'Trạng thái', dataIndex: 'status' },
  ]),
)

const columns_inner = ref(
  autoAddColumnWidth([
    { title: 'Ca', dataIndex: 'shift', key: 'shift' },
    { title: 'Thời gian', key: 'time' },
    { title: 'Phòng', dataIndex: 'room', key: 'room' },
    { title: 'Link Online', dataIndex: 'link', key: 'link' },
    {
      title: 'Điểm danh trễ',
      dataIndex: 'lateArrival',
      key: 'lateArrival',
      align: 'center',
    },
    { title: 'Nội dung', dataIndex: 'description', key: 'description' },
    { title: 'Giảng viên thay thế', dataIndex: 'nameTeacher', key: 'nameTeacher' },
    { title: 'Trạng thái', dataIndex: 'status', key: 'status' },
    { title: '', key: 'actions' },
  ]),
)

const columns_student = ref(
  autoAddColumnWidth([
    { title: 'Mã sinh viên', dataIndex: 'code', key: 'code' },
    { title: 'Tên sinh viên', dataIndex: 'name', key: 'name' },
    { title: 'Email', dataIndex: 'email', key: 'email' },
    { title: 'Nhóm xưởng', dataIndex: 'factoryName', key: 'factoryName' },
    { title: 'Kế hoạch', dataIndex: 'planName', key: 'planName' },
  ]),
)

const breadcrumb = ref([
  {
    name: GLOBAL_ROUTE_NAMES.STAFF_PAGE,
    breadcrumbName: 'Phụ trách xưởng',
  },
  {
    name: ROUTE_NAMES.MANAGEMENT_PLAN,
    breadcrumbName: 'Danh sách kế hoạch',
  },
])

const countFilter = ref(0)

const pagination = ref({ ...DEFAULT_PAGINATION })

const dataFilter = reactive({
  keyword: null,
  status: null,
  shift: null,
  type: null,
  startDate: null,
})

const formRefAddOrUpdate = ref(null)
const formRefUpdateLink = ref(null)

const formData = reactive({
  id: null,
  idPlan: null,
  description: null,
  shift: [],
  link: null,
  type: null,
  room: null,
  requiredLocation: STATUS_TYPE.ENABLE,
  requiredIp: STATUS_TYPE.ENABLE,
  requiredCheckin: STATUS_TYPE.ENABLE,
  requiredCheckout: STATUS_TYPE.ENABLE,
  startDate: null,
  endDate: null,
  lateArrival: DEFAULT_LATE_ARRIVAL,
  timeRange: [],
  idTeacher: null,
})

const formDataUpdateLink = reactive({
  idPlanFactory: null,
  link: null,
})

const formRules = reactive({
  startDate: [{ required: true, message: 'Vui lòng chọn ngày diễn ra!' }],
  shift: [{ required: true, message: 'Vui lòng chọn ca!' }],
  type: [{ required: true, message: 'Vui lòng chọn hình thức !' }],
  link: [{ required: true, message: 'Vui lòng nhập link online!' }],
  room: [{ required: true, message: 'Vui lòng nhập địa điểm!' }],
  lateArrival: [{ required: true, message: 'Vui lòng nhập thời gian điểm danh muộn tối đa!' }],
})

const disabledDate = (current) => {
  return current.isBefore(dayjs(), 'day') || current.isAfter(dayjs(_detail.value?.toDate), 'day')
}

const fetchDataDetail = () => {
  loadingStore.show()
  requestAPI
    .get(`${API_ROUTES_STAFF.FETCH_DATA_PLAN_DATE}/${route.params.id}`)
    .then(({ data: response }) => {
      _detail.value = response.data
      formData.idPlan = _detail.value.planId
      breadcrumbStore.push({
        name: ROUTE_NAMES.MANAGEMENT_PLAN_FACTORY,
        params: { id: _detail.value.planId },
        breadcrumbName: _detail.value.planName,
      })
      breadcrumbStore.push({
        breadcrumbName: _detail.value.factoryName,
      })
      fetchDataList()
    })
    .catch((error) => {
      message.error(error?.response?.data?.message || 'Không thể tải thông tin kế hoạch')
      router.push({ name: ROUTE_NAMES.MANAGEMENT_PLAN_FACTORY, params: { id: route.params?.id } })
    })
    .finally(() => {
      loadingStore.hide()
    })
}

const fetchDataList = () => {
  if (isLoading.value === true) {
    return
  }

  isLoading.value = true
  requestAPI
    .get(`${API_ROUTES_STAFF.FETCH_DATA_PLAN_DATE}/${_detail.value.id}/list`, {
      params: {
        page: pagination.value.current,
        size: pagination.value.pageSize,
        ...dataFilter,
        startDate: (dataFilter.startDate && Date.parse(dataFilter.startDate)) || null,
      },
    })
    .then(({ data: response }) => {
      lstData.value = response.data.data
      pagination.value.total = response.data.totalPages * pagination.value.pageSize
      countFilter.value = response.data.totalItems
    })
    .catch((error) => {
      message.error(error?.response?.data?.message || 'Không thể tải danh sách dữ liệu')
    })
    .finally(() => {
      isLoading.value = false
    })
}

const fetchDataShift = () => {
  requestAPI
    .get(`${API_ROUTES_STAFF.FETCH_DATA_PLAN_FACTORY}/list/shift`)
    .then(({ data: response }) => {
      lstShift.value = response.data
    })
    .catch((error) => {
      message.error(error?.response?.data?.message || 'Lỗi khi lấy dữ liệu ca')
    })
}

const fetchDeleteItem = (id) => {
  loadingStore.show()

  requestAPI
    .delete(`${API_ROUTES_STAFF.FETCH_DATA_PLAN_DATE}/${_detail.value.id}/delete/${id}`)
    .then(({ data: response }) => {
      message.success(response.message)
      fetchDataList()
    })
    .catch((error) => {
      message.error(error?.response?.data?.message || 'Không thể xoá mục này')
    })
    .finally(() => {
      loadingStore.hide()
    })
}

const fetchDeleteMultipleItem = (ids) => {
  loadingStore.show()

  requestAPI
    .delete(`${API_ROUTES_STAFF.FETCH_DATA_PLAN_DATE}/${_detail.value.id}/delete`, {
      data: {
        days: ids,
      },
    })
    .then(({ data: response }) => {
      message.success(response.message)
      selectedRowKeys.value = []
      fetchDataList()
    })
    .catch((error) => {
      message.error(error?.response?.data?.message || 'Không thể xoá mục đã chọn')
    })
    .finally(() => {
      loadingStore.hide()
    })
}

const fetchAddItem = () => {
  modalAddOrUpdate.isLoading = true
  requestAPI
    .post(`${API_ROUTES_STAFF.FETCH_DATA_PLAN_DATE}/${_detail.value.id}/add`, {
      ...formData,
      startDate: Date.parse(formData.startDate),
      customTime:
        formData.timeRange?.length > 0
          ? [
              getShiftTimeStart(
                Date.parse(formData.startDate),
                formData.timeRange[0].hour(),
                formData.timeRange[0].minute(),
              ),
              getShiftTimeEnd(
                Date.parse(formData.startDate),
                formData.timeRange[1].hour(),
                formData.timeRange[1].minute(),
              ),
            ]
          : null,
    })
    .then(({ data: response }) => {
      message.success(response.message)
      modalAddOrUpdate.isShow = false
      fetchDataList()
    })
    .catch((error) => {
      const data = error?.response?.data?.data
      if (data) {
        handleShowListStudentExists(data)
        return message.error(error?.response?.data?.message || 'Không thể cập nhật mục này')
      }
      message.error(error?.response?.data?.message || 'Không thể thêm mới mục này')
    })
    .finally(() => {
      modalAddOrUpdate.isLoading = false
    })
}

const fetchUpdateItem = () => {
  modalAddOrUpdate.isLoading = true
  requestAPI
    .put(`${API_ROUTES_STAFF.FETCH_DATA_PLAN_DATE}/${_detail.value.id}/update`, {
      ...formData,
      startDate: Date.parse(formData.startDate),
      customTime:
        formData.timeRange?.length > 0
          ? [
              getShiftTimeStart(
                Date.parse(formData.startDate),
                formData.timeRange[0].hour(),
                formData.timeRange[0].minute(),
              ),
              getShiftTimeEnd(
                Date.parse(formData.startDate),
                formData.timeRange[1].hour(),
                formData.timeRange[1].minute(),
              ),
            ]
          : null,
    })
    .then(({ data: response }) => {
      message.success(response.message)
      modalAddOrUpdate.isShow = false
      fetchDataList()
    })
    .catch((error) => {
      const data = error?.response?.data?.data
      if (data) {
        handleShowListStudentExists(data)
        return message.error(error?.response?.data?.message || 'Không thể cập nhật mục này')
      }
      message.error(error?.response?.data?.message || 'Không thể cập nhật mục này')
    })
    .finally(() => {
      modalAddOrUpdate.isLoading = false
    })
}

const fetchUpdateLink = () => {
  modalUpdateLink.isLoading = true
  requestAPI
    .put(
      `${API_ROUTES_STAFF.FETCH_DATA_PLAN_DATE}/${_detail.value.id}/update-link`,
      formDataUpdateLink,
    )
    .then(({ data: response }) => {
      message.success(response.message)
      modalUpdateLink.isShow = false
      fetchDataList()
    })
    .catch((error) => {
      message.error(error?.response?.data?.message || 'Không thể cập nhật mục này')
    })
    .finally(() => {
      modalUpdateLink.isLoading = false
    })
}

const fetchTeacherOptions = async (keyword) => {
  if (!keyword) {
    return
  }

  loadingTeacher.value = true
  requestAPI
    .get(`${API_ROUTES_STAFF.FETCH_DATA_PLAN_DATE}/search-teacher`, {
      params: {
        keyword,
      },
    })
    .then(({ data: response }) => {
      lstTeacher.value = response.data.map((o) => ({
        label: `${o.code} - ${o.name}`,
        value: o.id,
      }))
    })
    .catch((error) => {
      message.error(error?.response?.data?.message || 'Không thể tải danh sách dữ liệu giảng viên')
    })
    .finally(() => {
      loadingTeacher.value = false
    })
}

const fetchSendMail = () => {
  loadingStore.show()
  requestAPI
    .post(`${API_ROUTES_STAFF.FETCH_DATA_PLAN_DATE}/${_detail.value.id}/send-mail`)
    .then(({ data: response }) => {
      message.success(response.message)
    })
    .catch((error) => {
      message.error(error?.response?.data?.message || 'Không thể gửi thông báo lịch')
    })
    .finally(() => {
      loadingStore.hide()
    })
}

const handleClearFilter = () => {
  Object.assign(dataFilter, {
    keyword: null,
    status: null,
    shift: null,
    type: null,
    startDate: null,
  })
  fetchDataList()
}

const handleSubmitFilter = () => {
  pagination.value.current = 1
  fetchDataList()
}

const handleTableChange = (page) => {
  pagination.value.current = page.current
  pagination.value.pageSize = page.pageSize
  fetchDataList()
}

const handleShowAdd = () => {
  if (formRefAddOrUpdate.value) {
    formRefAddOrUpdate.value.clearValidate()
  }
  modalAddOrUpdate.isShow = true
  modalAddOrUpdate.isLoading = false
  modalAddOrUpdate.title = h('span', [
    h(PlusOutlined, { class: 'me-2 text-primary' }),
    'Thêm ca mới',
  ])
  modalAddOrUpdate.okText = 'Thêm ngay'
  modalAddOrUpdate.onOk = () => handleSubmitAdd()

  lstTeacher.value = []

  formData.id = null
  formData.startDate = dayjs()
  formData.shift = []
  formData.link = null
  formData.type = Object.keys(TYPE_SHIFT)[0]
  formData.room = null
  formData.requiredLocation = STATUS_TYPE.ENABLE
  formData.requiredIp = STATUS_TYPE.ENABLE
  formData.requiredCheckin = STATUS_TYPE.ENABLE
  formData.requiredCheckout = STATUS_TYPE.ENABLE
  formData.lateArrival = DEFAULT_LATE_ARRIVAL
  formData.description = null
  formData.timeRange = []
  formData.idTeacher = null
}

const handleShowUpdate = (item) => {
  if (formRefAddOrUpdate.value) {
    formRefAddOrUpdate.value.clearValidate()
  }
  modalAddOrUpdate.isShow = true
  modalAddOrUpdate.isLoading = false
  modalAddOrUpdate.title = h('span', [
    h(EditFilled, { class: 'me-2 text-primary' }),
    'Chỉnh sửa ca',
  ])
  modalAddOrUpdate.okText = 'Lưu lại'
  modalAddOrUpdate.onOk = () => handleSubmitUpdate()

  lstTeacher.value = item.idTeacher ? [{ label: item.nameTeacher, value: item.idTeacher }] : []

  formData.id = item.id
  formData.startDate = dayjs(item.startDate)
  formData.endDate = dayjs(item.endDate)
  formData.shift = item.shift.split(',').map((o) => Number(o))
  formData.link = item.link
  formData.room = item.room
  formData.type = String(item.type)
  formData.requiredLocation = item.requiredLocation || STATUS_TYPE.DISABLE
  formData.requiredIp = item.requiredIp || STATUS_TYPE.DISABLE
  formData.requiredCheckin = item.requiredCheckin || STATUS_TYPE.DISABLE
  formData.requiredCheckout = item.requiredCheckout || STATUS_TYPE.DISABLE
  formData.lateArrival = item.lateArrival
  formData.description = item.description
  formData.idTeacher = item.idTeacher

  handleUpdateTimeRange()
}

const handleSubmitAdd = async () => {
  try {
    await formRefAddOrUpdate.value.validate()
    Modal.confirm({
      title: `Xác nhận thêm mới`,
      type: 'info',
      content: `Bạn có chắc muốn thêm mới ca này?`,
      okText: 'Tiếp tục',
      cancelText: 'Hủy bỏ',
      onOk() {
        fetchAddItem()
      },
    })
  } catch (error) {}
}

const handleSubmitUpdate = async () => {
  try {
    await formRefAddOrUpdate.value.validate()
    Modal.confirm({
      title: `Xác nhận cập nhật`,
      type: 'info',
      content: `Bạn có chắc muốn lưu lại thay đổi?`,
      okText: 'Tiếp tục',
      cancelText: 'Hủy bỏ',
      onOk() {
        fetchUpdateItem()
      },
    })
  } catch (error) {}
}

const handleSubmitUpdateLink = async () => {
  try {
    await formRefUpdateLink.value.validate()
    Modal.confirm({
      title: `Xác nhận cập nhật link`,
      type: 'info',
      content: `Tất cả ca online sẽ bị ảnh hưởng. Bạn có chắc muốn tiếp tục?`,
      okText: 'Tiếp tục',
      cancelText: 'Hủy bỏ',
      onOk() {
        fetchUpdateLink()
      },
    })
  } catch (error) {}
}

const handleShowAlertDelete = (item) => {
  Modal.confirm({
    title: `Xoá ca: ${dayOfWeek(item.startDate)} - ${formatDate(item.startDate)}`,
    type: 'error',
    content: `Bạn có chắc muốn xoá ca này?`,
    okText: 'Tiếp tục',
    cancelText: 'Hủy bỏ',
    okButtonProps: {
      class: 'btn-danger',
    },
    cancelButtonProps: {
      class: 'btn-gray',
    },
    onOk() {
      fetchDeleteItem(item.id)
    },
  })
}

const handleShowAlertMultipleDelete = () => {
  Modal.confirm({
    title: `Xoá ca đã chọn`,
    type: 'error',
    content: `Bạn có chắc muốn xoá ${selectedRowKeys.value.length} ca đã chọn?`,
    okText: 'Tiếp tục',
    cancelText: 'Hủy bỏ',
    okButtonProps: {
      class: 'btn-danger',
    },
    cancelButtonProps: {
      class: 'btn-gray',
    },
    onOk() {
      fetchDeleteMultipleItem(selectedRowKeys.value)
    },
  })
}

const handleShowDescription = (text) => {
  Modal.info({
    title: 'Nội dung chi tiết',
    type: 'info',
    content: text || 'Không có mô tả',
    okText: 'Đóng',
    okButtonProps: {
      class: 'btn-gray',
    },
  })
}

const handleShowAttendance = (id) => {
  router.push({
    name: ROUTE_NAMES.MANAGEMENT_PLAN_DATE_ATTENDANCE,
    params: { id: id },
  })
}

const handleShowUpdateLink = () => {
  if (formRefUpdateLink.value) {
    formRefUpdateLink.value.clearValidate()
  }
  modalUpdateLink.isShow = true
  modalUpdateLink.isLoading = false
  modalUpdateLink.title = h('span', [
    h(LinkOutlined, { class: 'me-2 text-primary' }),
    'Cập nhật link online',
  ])
  modalUpdateLink.okText = 'Lưu lại'
  modalUpdateLink.onOk = () => handleSubmitUpdateLink()

  formDataUpdateLink.idPlanFactory = _detail.value.id
  formDataUpdateLink.link = null
}

const handleChangeShift = (newValues) => {
  formData.shift = [newValues]

  // const updated = new Set(newValues)

  // const sorted = [...updated].sort((a, b) => a - b)

  // for (let i = 0; i < sorted.length - 1; i++) {
  //   const start = sorted[i]
  //   const end = sorted[i + 1]

  //   if (end - start > 1) {
  //     for (let j = start + 1; j < end; j++) {
  //       updated.add(j)
  //     }
  //   }
  // }

  // formData.shift = Array.from(updated).sort((a, b) => a - b)
}

const handleShowListStudentExists = (data) => {
  lstStudentExists.value = data || []
  isShowListStudentExists.value = true
}

const handleUpdateTimeRange = () => {
  if (formData.shift.length < 1) {
    return (formData.timeRange = [])
  }

  const startTime = formData.startDate
  const endTime = formData.endDate

  const firstShift = lstShift.value.find((o) => o.shift == formData.shift[0])
  const lastShift = lstShift.value.find((o) => o.shift == formData.shift[formData.shift.length - 1])

  if (
    startTime.hour() != firstShift.fromHour ||
    startTime.minute() != firstShift.fromMinute ||
    endTime.hour() != lastShift.toHour ||
    endTime.minute() != lastShift.toMinute
  ) {
    formData.timeRange = [
      dayjs(startTime.hour() + ':' + startTime.minute(), 'HH:mm'),
      dayjs(endTime.hour() + ':' + endTime.minute(), 'HH:mm'),
    ]
  } else {
    formData.timeRange = []
  }
}

const handleSendMail = () => {
  Modal.confirm({
    title: 'Xác nhận gửi mail thông báo lịch',
    type: 'info',
    content: `Một mail chứa tệp Excel lịch sẽ được gửi tới giảng viên và sinh viên trong nhóm.`,
    okText: 'Tiếp tục',
    cancelText: 'Hủy bỏ',
    onOk() {
      fetchSendMail()
    },
  })
}

const selectedRowKeys = ref([])

const isDisabledSelectTable = (key) => {
  const record = lstData.value.find((item) => item.day === key)
  return record?.status === 'DA_DIEN_RA'
}

const rowSelection = computed(() => rowSelectTable(selectedRowKeys, isDisabledSelectTable, 'day'))

onMounted(() => {
  breadcrumbStore.setRoutes(breadcrumb.value)
  fetchDataDetail()
  fetchDataShift()
})

const debounceFilter = debounce(handleSubmitFilter, 100)
watch(
  dataFilter,
  () => {
    debounceFilter()
  },
  { deep: true },
)
watch(
  () => teacher.value,
  (val) => {
    formData.idTeacher = val?.value || null
  },
)
</script>

<template>
  <a-modal
    v-model:open="modalAddOrUpdate.isShow"
    v-bind="modalAddOrUpdate"
    :okButtonProps="{ loading: modalAddOrUpdate.isLoading }"
  >
    <a-form
      ref="formRefAddOrUpdate"
      class="row mt-3"
      layout="vertical"
      autocomplete="off"
      :model="formData"
    >
      <a-form-item
        class="col-sm-4"
        label="Ngày diễn ra"
        name="startDate"
        :rules="formRules.startDate"
      >
        <a-date-picker
          class="w-100"
          :placeholder="DEFAULT_DATE_FORMAT"
          v-model:value="formData.startDate"
          :format="DEFAULT_DATE_FORMAT"
          :disabledDate="disabledDate"
          :disabled="modalAddOrUpdate.isLoading"
        />
      </a-form-item>
      <a-form-item
        class="col-sm-8"
        label="Phòng"
        name="room"
        :rules="formData.type == '1' ? false : formRules.room"
      >
        <a-input
          class="w-100"
          v-model:value="formData.room"
          placeholder="Địa điểm chi tiết"
          :disabled="modalAddOrUpdate.isLoading || formData.type == '1'"
          allowClear
          @keyup.enter="modalAddOrUpdate.onOk"
        />
      </a-form-item>
      <a-form-item class="col-sm-12" label="Ca" name="shift" :rules="formRules.shift">
        <a-select
          class="w-100"
          placeholder="Chọn 1 ca"
          v-model:value="formData.shift"
          :disabled="modalAddOrUpdate.isLoading"
          @change="handleChangeShift"
          allow-clear
        >
          <a-select-option v-for="o in lstShift" :key="o.id" :value="o.shift">
            {{ SHIFT[o.shift] }}
            ({{
              `${String(o.fromHour).padStart(2, 0)}:${String(o.fromMinute).padStart(2, 0)} - ${String(o.toHour).padStart(2, 0)}:${String(o.toMinute).padStart(2, 0)}`
            }})
          </a-select-option>
        </a-select>
      </a-form-item>

      <!-- <a-form-item class="col-sm-4" label="Tuỳ chỉnh thời gian ca" name="timeRange">
        <a-range-picker
          class="w-100"
          v-model:value="formData.timeRange"
          :show-time="{ format: 'HH:mm' }"
          format="HH:mm"
          picker="time"
          :placeholder="['Bắt đầu', 'Kết thúc']"
        />
      </a-form-item> -->

      <a-form-item class="col-sm-5" label="Hình thức " name="type" :rules="formRules.type">
        <a-select
          v-model:value="formData.type"
          class="w-100"
          :dropdownMatchSelectWidth="false"
          placeholder="-- Hình thức --"
          allowClear
        >
          <a-select-option v-for="(name, id) in TYPE_SHIFT" :key="id" :value="id">
            {{ name }}
          </a-select-option>
        </a-select>
      </a-form-item>

      <a-form-item
        class="col-sm-7"
        label="Điểm danh muộn tối đa (phút)"
        name="lateArrival"
        :rules="formRules.lateArrival"
      >
        <a-input-number
          class="w-100"
          placeholder="0"
          v-model:value="formData.lateArrival"
          :min="0"
          :step="1"
          :disabled="modalAddOrUpdate.isLoading"
          allowClear
          @keyup.enter="modalAddOrUpdate.onOk"
        />
      </a-form-item>

      <a-form-item class="col-sm-12" label="Nội dung buổi" name="description">
        <a-textarea
          :rows="4"
          class="w-100"
          placeholder="Mô tả nội dung buổi"
          v-model:value="formData.description"
          :disabled="modalAddOrUpdate.isLoading"
          allowClear
        />
      </a-form-item>
      <a-form-item
        class="col-sm-12"
        label="Link online"
        name="link"
        :rules="formData.type == '1' ? formRules.link : false"
      >
        <a-input
          class="w-100"
          v-model:value="formData.link"
          placeholder="https://"
          :disabled="modalAddOrUpdate.isLoading"
          allowClear
          @keyup.enter="modalAddOrUpdate.onOk"
        />
      </a-form-item>

      <a-form-item class="col-sm-12" label="Giảng viên thay thế" v-if="formData.id">
        <VueMultiselect
          v-model="teacher"
          :options="lstTeacher"
          :searchable="true"
          :loading="loadingTeacher"
          :internal-search="false"
          label="label"
          track-by="value"
          placeholder="Nhập tên, email hoặc mã giảng viên..."
          @search-change="fetchTeacherOptions"
        >
          <template #noOptions> Không có giảng viên nào </template>
          <template #noResult> Không tìm thấy giảng viên nào </template>
        </VueMultiselect>
      </a-form-item>

      <a-form-item class="col-sm-12" label="Điều kiện điểm danh">
        <div class="row g-3">
          <div class="col-sm-6">
            <a-switch
              class="me-2"
              :checked="formData.requiredCheckin === STATUS_TYPE.ENABLE"
              @change="
                formData.requiredCheckin =
                  formData.requiredCheckin === STATUS_TYPE.ENABLE
                    ? STATUS_TYPE.DISABLE
                    : STATUS_TYPE.ENABLE
              "
            />
            <span :class="{ disabled: formData.requiredCheckin !== STATUS_TYPE.ENABLE }"
              >Yêu cầu checkin</span
            >
          </div>
          <div class="col-sm-6">
            <a-switch
              class="me-2"
              :checked="formData.requiredCheckout === STATUS_TYPE.ENABLE"
              @change="
                formData.requiredCheckout =
                  formData.requiredCheckout === STATUS_TYPE.ENABLE
                    ? STATUS_TYPE.DISABLE
                    : STATUS_TYPE.ENABLE
              "
            />
            <span :class="{ disabled: formData.requiredCheckout !== STATUS_TYPE.ENABLE }"
              >Yêu cầu checkout</span
            >
          </div>
          <div class="col-sm-6">
            <a-switch
              class="me-2"
              :checked="
                formData.requiredIp === STATUS_TYPE.ENABLE &&
                formData.type === Object.keys(TYPE_SHIFT)[0]
              "
              :disabled="formData.type !== Object.keys(TYPE_SHIFT)[0]"
              @change="
                formData.requiredIp =
                  formData.requiredIp === STATUS_TYPE.ENABLE
                    ? STATUS_TYPE.DISABLE
                    : STATUS_TYPE.ENABLE
              "
            />
            <span :class="{ disabled: formData.requiredIp !== STATUS_TYPE.ENABLE }"
              >Phải kết nối mạng trường</span
            >
          </div>
          <div class="col-sm-6">
            <a-switch
              class="me-2"
              :checked="
                formData.requiredLocation === STATUS_TYPE.ENABLE &&
                formData.type === Object.keys(TYPE_SHIFT)[0]
              "
              :disabled="formData.type !== Object.keys(TYPE_SHIFT)[0]"
              @change="
                formData.requiredLocation =
                  formData.requiredLocation === STATUS_TYPE.ENABLE
                    ? STATUS_TYPE.DISABLE
                    : STATUS_TYPE.ENABLE
              "
            />
            <span :class="{ disabled: formData.requiredLocation !== STATUS_TYPE.ENABLE }"
              >Phải ở trong địa điểm cơ sở</span
            >
          </div>
        </div>
      </a-form-item>
    </a-form>
  </a-modal>

  <a-modal
    v-model:open="modalUpdateLink.isShow"
    v-bind="modalUpdateLink"
    :okButtonProps="{ loading: modalUpdateLink.isLoading }"
  >
    <a-form
      ref="formRefUpdateLink"
      class="row mt-3"
      layout="vertical"
      autocomplete="off"
      :model="formDataUpdateLink"
    >
      <a-form-item class="col-sm-12" label="Link online:" name="link" :rules="formRules.link">
        <a-input
          class="w-100"
          v-model:value="formDataUpdateLink.link"
          placeholder="https://"
          :disabled="modalUpdateLink.isLoading"
          allowClear
          @keyup.enter="modalUpdateLink.onOk"
        />
      </a-form-item>
    </a-form>
  </a-modal>

  <a-modal v-model:open="isShowListStudentExists" :width="1000" :footer="null">
    <template #title
      ><InfoCircleFilled class="text-primary" /> Danh sách sinh viên bị trùng ca
    </template>
    <div class="row g-2">
      <div class="col-12">
        <a-table
          rowKey="id"
          class="nowrap"
          :dataSource="lstStudentExists"
          :columns="columns_student"
          :pagination="false"
          :scroll="{ x: 'auto' }"
        >
        </a-table>
      </div>
    </div>
  </a-modal>

  <div class="container-fluid">
    <div class="row g-3">
      <div class="col-12">
        <a-card :bordered="false" class="cart no-body-padding">
          <a-collapse ghost>
            <a-collapse-panel>
              <template #header><FilterFilled /> Bộ lọc ({{ countFilter }})</template>
              <div class="row g-3">
                <div class="col-xxl-4 col-lg-8 col-md-8 col-sm-12">
                  <div class="label-title">Từ khoá:</div>
                  <a-input
                    v-model:value="dataFilter.keyword"
                    placeholder="Tìm theo nội dung..."
                    allowClear
                  >
                    <template #prefix>
                      <SearchOutlined />
                    </template>
                  </a-input>
                </div>
                <div class="col-xxl-2 col-lg-4 col-md-4 col-sm-6">
                  <div class="label-title">Trạng thái:</div>
                  <a-select
                    v-model:value="dataFilter.status"
                    class="w-100"
                    :dropdownMatchSelectWidth="false"
                    placeholder="-- Tất cả trạng thái --"
                  >
                    <a-select-option :value="null">-- Tất cả trạng thái --</a-select-option>
                    <a-select-option
                      v-for="(name, id) in STATUS_PLAN_DATE_DETAIL"
                      :key="id"
                      :value="id"
                    >
                      {{ name }}
                    </a-select-option>
                  </a-select>
                </div>
                <div class="col-xxl-2 col-lg-4 col-md-4 col-sm-6">
                  <div class="label-title">Hình thức:</div>
                  <a-select
                    v-model:value="dataFilter.type"
                    class="w-100"
                    :dropdownMatchSelectWidth="false"
                    placeholder="-- Tất cả hình thức --"
                    allowClear
                  >
                    <a-select-option :value="null">-- Tất cả hình thức --</a-select-option>
                    <a-select-option v-for="(name, id) in TYPE_SHIFT" :key="id" :value="id">
                      {{ name }}
                    </a-select-option>
                  </a-select>
                </div>
                <div class="col-xxl-2 col-lg-4 col-md-4 col-sm-6">
                  <div class="label-title">Ca:</div>
                  <a-select
                    v-model:value="dataFilter.shift"
                    class="w-100"
                    :dropdownMatchSelectWidth="false"
                    placeholder="-- Tất cả ca --"
                    allowClear
                  >
                    <a-select-option :value="null">-- Tất cả ca --</a-select-option>
                    <a-select-option v-for="o in lstShift" :key="o.id" :value="o.shift">
                      {{ SHIFT[o.shift] }}
                    </a-select-option>
                  </a-select>
                </div>
                <div class="col-xxl-2 col-lg-4 col-md-4 col-sm-6">
                  <div class="label-title">Ngày diễn ra:</div>
                  <a-date-picker
                    class="w-100"
                    placeholder="-- Tất cả các ngày --"
                    v-model:value="dataFilter.startDate"
                    :format="DEFAULT_DATE_FORMAT"
                  />
                </div>
                <div class="col-12">
                  <div class="d-flex justify-content-center flex-wrap gap-2">
                    <a-button class="btn-light" @click="handleSubmitFilter">
                      <FilterFilled /> Lọc
                    </a-button>
                    <a-button class="btn-gray" @click="handleClearFilter"> Huỷ lọc </a-button>
                  </div>
                </div>
              </div>
            </a-collapse-panel>
          </a-collapse>
        </a-card>
      </div>

      <div class="col-12">
        <a-card :bordered="false" class="cart">
          <template #title>
            <UnorderedListOutlined /> Danh sách ca
            {{ `(${formatDate(_detail?.fromDate)} - ${formatDate(_detail?.toDate)})` }}
          </template>

          <div class="d-flex justify-content-end flex-wrap gap-3 mb-2">
            <a-button
              v-show="selectedRowKeys.length > 0"
              class="btn-outline-danger"
              @click="handleShowAlertMultipleDelete"
              ><DeleteFilled /> Xoá mục đã chọn</a-button
            >
            <ExcelUploadButton v-bind="configImportExcel" />
            <template v-if="isActive">
              <a-button class="btn btn-gray" @click="handleShowUpdateLink">
                <LinkOutlined /> Update link online
              </a-button>
              <a-button class="btn btn-outline-warning" @click="handleSendMail">
                <MailOutlined /> Gửi mail thông báo
              </a-button>
              <a-button type="primary" @click="handleShowAdd"> <PlusOutlined /> Thêm mới </a-button>
            </template>
          </div>

          <div>
            <a-table
              rowKey="day"
              class="nowrap"
              :dataSource="lstData"
              :columns="columns"
              :loading="isLoading"
              :pagination="pagination"
              :scroll="{ x: 'auto' }"
              :expand-row-by-click="true"
              :row-selection="rowSelection"
              @change="handleTableChange"
            >
              <template #bodyCell="{ column, record }">
                <template v-if="column.dataIndex === 'day'">
                  <a-tag :color="colorDayOfWeek(record.startDate)">
                    {{ dayOfWeek(record.startDate) }}
                  </a-tag>
                </template>
                <template v-if="column.dataIndex === 'startDate'">
                  {{ formatDate(record.startDate, DEFAULT_DATE_FORMAT) }}
                </template>
                <template v-if="column.dataIndex === 'totalShift'">
                  <a-tag color="#41395b">
                    {{ record.totalShift }}
                  </a-tag>
                </template>
                <template v-if="column.dataIndex === 'shift'">
                  <a-tag color="purple">
                    {{
                      `Ca ${record.planDates
                        .map((o) =>
                          o.shift
                            .split(',')
                            .map((o) => Number(o))
                            .join(', '),
                        )
                        .join(', ')}`
                    }}
                  </a-tag>
                </template>
                <template v-if="column.dataIndex === 'type'">
                  <span v-for="o in record.types" :key="o">
                    <a-tag :color="o === 1 ? 'blue' : 'purple'">
                      {{ TYPE_SHIFT[o] }}
                    </a-tag>
                  </span>
                </template>
                <template v-if="column.dataIndex === 'status'">
                  <a-badge :status="record.status === 'DA_DIEN_RA' ? 'success' : 'default'" />
                  {{ STATUS_PLAN_DATE_DETAIL[record.status] }}
                </template>
              </template>

              <template #expandedRowRender="{ record }">
                <a-table
                  rowKey="id"
                  class="nowrap expanded"
                  :columns="columns_inner"
                  :data-source="record.planDates"
                  :pagination="false"
                  :loading="isLoading"
                  :scroll="{ x: 'auto' }"
                >
                  <template #bodyCell="{ column, record }">
                    <template v-if="column.dataIndex === 'description'">
                      <a-typography-link
                        v-if="record.description"
                        @click="handleShowDescription(record.description)"
                        >Chi tiết</a-typography-link
                      >
                      <span v-else>Không có mô tả</span>
                    </template>
                    <template v-if="column.dataIndex === 'link'">
                      <a v-if="record.link" target="_blank" :href="record.link">Link</a>
                      <span v-else>--</span>
                    </template>
                    <template v-if="column.dataIndex === 'room'">
                      <span v-if="record.room">{{ record.room }}</span>
                      <span v-else>--</span>
                    </template>
                    <template v-if="column.dataIndex === 'nameTeacher'">
                      <span v-if="record.nameTeacher">{{ record.nameTeacher }}</span>
                      <span v-else>--</span>
                    </template>
                    <template v-if="column.dataIndex === 'lateArrival'">
                      <a-tag color="gold">
                        <ExclamationCircleOutlined /> {{ `${record.lateArrival} phút` }}
                      </a-tag>
                    </template>

                    <template v-if="column.key === 'time'">
                      {{
                        `${formatDate(record.startDate, 'HH:mm')} - ${formatDate(
                          record.endDate,
                          'HH:mm',
                        )}`
                      }}
                    </template>
                    <template v-if="column.dataIndex === 'shift'">
                      <a-tag :color="record.type === 1 ? 'blue' : 'purple'">
                        {{
                          `Ca ${record.shift
                            .split(',')
                            .map((o) => Number(o))
                            .join(', ')} - ${TYPE_SHIFT[record.type]}`
                        }}
                      </a-tag>
                    </template>
                    <template v-if="column.dataIndex === 'status'">
                      <a-badge :status="record.status === 'DA_DIEN_RA' ? 'success' : 'default'" />
                      {{ STATUS_PLAN_DATE_DETAIL[record.status] }}
                    </template>
                    <template v-if="column.dataIndex === 'totalShift'">
                      <a-tag>
                        {{ record.totalShift }}
                      </a-tag>
                    </template>
                    <template v-if="column.key === 'actions'">
                      <template v-if="record.status !== 'DA_DIEN_RA'">
                        <a-tooltip title="Chỉnh sửa ca">
                          <a-button
                            class="btn-outline-info border-0"
                            @click="handleShowUpdate(record)"
                          >
                            <EditFilled />
                          </a-button>
                        </a-tooltip>
                        <a-tooltip title="Xoá ca">
                          <a-button
                            class="btn-outline-danger border-0 ms-2"
                            @click="handleShowAlertDelete(record)"
                          >
                            <DeleteFilled />
                          </a-button>
                        </a-tooltip>
                      </template>
                      <a-tooltip title="Chi tiết điểm danh" v-else>
                        <a-button
                          class="btn-outline-primary border-0 me-2"
                          @click="handleShowAttendance(record.id)"
                        >
                          <EyeFilled />
                        </a-button>
                      </a-tooltip>
                    </template>
                  </template>
                </a-table>
              </template>
            </a-table>
          </div>
        </a-card>
      </div>
    </div>
  </div>
</template>
