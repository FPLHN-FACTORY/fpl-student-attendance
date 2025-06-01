<script setup>
import {
  ATTENDANCE_STATUS,
  DEFAULT_EARLY_MINUTE_CHECKIN,
  DEFAULT_PAGINATION,
  STATUS_REQUIRED_ATTENDANCE,
  TYPE_SHIFT,
} from '@/constants'
import { GLOBAL_ROUTE_NAMES } from '@/constants/routesConstant'
import { API_ROUTES_STUDENT } from '@/constants/studentConstant'
import { ROUTE_NAMES } from '@/router/studentRoute'
import requestAPI from '@/services/requestApiService'
import useBreadcrumbStore from '@/stores/useBreadCrumbStore'
import { debounce, formatDate, autoAddColumnWidth } from '@/utils/utils'
import {
  CheckOutlined,
  ExclamationCircleOutlined,
  FilterFilled,
  SearchOutlined,
  UnorderedListOutlined,
} from '@ant-design/icons-vue'
import { message, Modal } from 'ant-design-vue'
import { createVNode, nextTick, onMounted, reactive, ref, watch } from 'vue'
import useLoadingStore from '@/stores/useLoadingStore'
import useFaceIDStore from '@/stores/useFaceIDStore'
import { ROUTE_NAMES_API } from '@/router/authenticationRoute'
import useApplicationStore from '@/stores/useApplicationStore'

const applicationStore = useApplicationStore()
const faceIDStore = useFaceIDStore()
const loadingPage = useLoadingStore()

const isLoading = ref(false)

const lstData = ref([])

const isShowCamera = ref(false)

const video = ref(null)
const canvas = ref(null)

const formData = reactive({
  idPlanDate: null,
  latitude: null,
  longitude: null,
  faceEmbedding: null,
})

const breadcrumbStore = useBreadcrumbStore()

const breadcrumb = ref([
  {
    name: GLOBAL_ROUTE_NAMES.STUDENT_PAGE,
    breadcrumbName: 'Sinh viên',
  },
  {
    name: ROUTE_NAMES.ATTENDANCE,
    breadcrumbName: 'Điểm danh',
  },
])

const columns = ref(
  autoAddColumnWidth([
    { title: '#', dataIndex: 'orderNumber', key: 'orderNumber' },
    { title: 'Thời gian', dataIndex: 'startDate', key: 'startDate' },
    { title: 'Ca học', dataIndex: 'shift', key: 'shift' },
    { title: 'Điểm danh muộn', dataIndex: 'lateArrival', key: 'lateArrival' },
    { title: 'Nhóm xưởng', dataIndex: 'factoryName', key: 'factoryName' },
    { title: 'Giảng viên', dataIndex: 'teacherName', key: 'teacherName' },
    {
      title: 'Checkin/checkout bù',
      dataIndex: 'lateAttendance',
      key: 'lateAttendance',
      align: 'center',
    },
    { title: 'Trạng thái', dataIndex: 'status', key: 'status' },
    { title: '', key: 'actions' },
  ]),
)

const pagination = ref({ ...DEFAULT_PAGINATION })

const dataFilter = reactive({
  keyword: null,
  status: null,
  type: null,
})

const fetchDataList = () => {
  if (isLoading.value === true) {
    return
  }

  isLoading.value = true
  requestAPI
    .get(`${API_ROUTES_STUDENT.FETCH_DATA_ATTENDANCE}`, {
      params: {
        ...dataFilter,
        page: pagination.value.current,
        size: pagination.value.pageSize,
      },
    })
    .then(({ data: response }) => {
      lstData.value = response.data.data
      pagination.value.total = response.data.totalPages * pagination.value.pageSize
    })
    .catch((error) => {
      message.error(error.response?.data?.message || 'Lỗi khi lấy dữ liệu')
    })
    .finally(() => {
      isLoading.value = false
    })
}

const fetchDataStudentInfo = () => {
  requestAPI
    .get(`${ROUTE_NAMES_API.FETCH_DATA_STUDENT_INFO}`)
    .then(({ data: response }) => {
      if (response?.data?.faceEmbedding !== 'OK') {
        Modal.confirm({
          title: 'Yêu cầu cập nhật khuôn mặt',
          icon: createVNode(ExclamationCircleOutlined),
          content: 'Vui lòng cập nhật khuôn mặt để sử dụng hệ thống!!!',
          okText: 'Cập nhật ngay',
          cancelText: 'Huỷ bỏ',
          onOk: () => {
            handleUpdateInfo()
          },
        })
      }
    })
    .catch((error) => {
      message.error(error.response?.data?.message || 'Lỗi khi lấy thông tin sinh viên')
    })
}

const handleClearFilter = () => {
  Object.assign(dataFilter, {
    keyword: null,
    status: null,
    type: null,
  })
  fetchDataList()
}

const handleTableChange = (page) => {
  pagination.value.current = page.current
  pagination.value.pageSize = page.pageSize
  fetchDataList()
}

const handleSubmitFilter = () => {
  pagination.value.current = 1
  fetchDataList()
}

const handleSubmitAttendance = () => {
  loadingPage.show()
  requestAPI
    .post(`${API_ROUTES_STUDENT.FETCH_DATA_ATTENDANCE}/checkin`, formData)
    .then(({ data: response }) => {
      message.success(response.message)
      fetchDataList()
    })
    .catch((error) => {
      message.error(error.response?.data?.message || 'Lỗi khi điểm danh ca học này')
    })
    .finally(() => {
      loadingPage.hide()
      isShowCamera.value = false
    })
}

const handleSubmitUpdateInfo = () => {
  loadingPage.show()
  requestAPI
    .put(`${ROUTE_NAMES_API.FETCH_DATA_UPDATE_FACEID}`, formData)
    .then(({ data: response }) => {
      message.success(response.message)
      applicationStore.loadNotification()
    })
    .catch((error) => {
      message.error(error.response?.data?.message || 'Lỗi khi cập nhật khuôn mặt')
    })
    .finally(() => {
      loadingPage.hide()
    })
}

const handleUpdateInfo = async () => {
  isShowCamera.value = true
  faceIDStore.init(video, canvas, false, (descriptor) => {
    isShowCamera.value = false
    formData.faceEmbedding = JSON.stringify(descriptor)
    Modal.confirm({
      title: 'Xác nhận cập nhật dữ liệu khuôn mặt',
      icon: createVNode(ExclamationCircleOutlined),
      content: 'Không thể hoàn tác. Bạn thực sự muốn cập nhật khuôn mặt này?',
      okText: 'Tiếp tục',
      cancelText: 'Huỷ bỏ',
      onOk: () => {
        handleSubmitUpdateInfo()
      },
    })
  })
  await nextTick()
  await faceIDStore.startVideo()
}

const handleCheckin = async (item) => {
  formData.idPlanDate = item.idPlanDate

  isShowCamera.value = true
  faceIDStore.init(video, canvas, true, (descriptor) => {
    formData.faceEmbedding = JSON.stringify(descriptor)
    handleSubmitAttendance()
  })
  await nextTick()
  await faceIDStore.startVideo()
}

const getCurrentLocation = async () => {
  navigator.geolocation.getCurrentPosition(
    (position) => {
      const { latitude, longitude } = position.coords || {}
      formData.latitude = latitude
      formData.longitude = longitude
    },
    () => {
      Modal.confirm({
        title: 'Không thể lấy vị trí',
        icon: createVNode(ExclamationCircleOutlined),
        content: 'Vui lòng bật quyền truy cập vị trí để tiếp tục!!!',
        okText: 'Tải lại trang',
        cancelText: 'Huỷ bỏ',
        onOk: () => {
          window.location.reload()
        },
      })
    },
  )
}

onMounted(async () => {
  breadcrumbStore.setRoutes(breadcrumb.value)
  loadingPage.show()
  await getCurrentLocation()
  fetchDataStudentInfo()
  fetchDataList()
  await faceIDStore.loadModels()
  loadingPage.hide()
})

const debounceFilter = debounce(handleSubmitFilter, 100)
watch(
  dataFilter,
  () => {
    debounceFilter()
  },
  { deep: true },
)
</script>

<template>
  <a-modal
    v-model:open="isShowCamera"
    title="Xác nhận khuôn mặt"
    @cancel="faceIDStore.stopVideo()"
    :footer="null"
  >
    <div class="video-container">
      <canvas ref="canvas"></canvas>
      <video ref="video" autoplay muted></video>
      <div class="face-id-step" :class="faceIDStore.renderStyle()">
        <div></div>
        <div></div>
        <div></div>
        <div></div>
        <div></div>
        <div></div>
        <div></div>
        <div></div>
        <div></div>
        <div></div>
        <div></div>
        <div></div>
        <div></div>
        <div></div>
        <div></div>
        <div></div>
        <div></div>
        <div></div>
        <div></div>
        <div></div>
        <div></div>
        <div></div>
        <div></div>
        <div></div>
        <div></div>
        <div></div>
        <div></div>
        <div></div>
        <div></div>
        <div></div>
        <div></div>
        <div></div>
        <div></div>
        <div></div>
        <div></div>
        <div></div>
      </div>
      <div class="face-id-loading" v-show="faceIDStore.isLoading">
        <div class="bg-loading">
          <div></div>
          <div></div>
          <div></div>
        </div>
      </div>
    </div>
    <div class="face-id-text" v-show="faceIDStore.textStep != null">
      {{ faceIDStore.textStep }}
    </div>
  </a-modal>

  <div class="container-fluid">
    <div class="row g-3">
      <div class="col-12">
        <!-- Bộ lọc tìm kiếm -->
        <a-card :bordered="false" class="cart">
          <template #title> <FilterFilled /> Bộ lọc </template>
          <div class="row g-2">
            <div class="col-md-6 col-sm-12">
              <div class="label-title">Từ khoá:</div>
              <a-input
                v-model:value="dataFilter.keyword"
                placeholder="Tìm theo nhóm xưởng, giảng viên..."
                allowClear
              >
                <template #prefix>
                  <SearchOutlined />
                </template>
              </a-input>
            </div>
            <div class="col-md-3 col-sm-6">
              <div class="label-title">Trạng thái:</div>
              <a-select
                v-model:value="dataFilter.status"
                class="w-100"
                :dropdownMatchSelectWidth="false"
                placeholder="-- Tất cả trạng thái --"
                allowClear
              >
                <a-select-option :value="null">-- Tất cả trạng thái --</a-select-option>
                <a-select-option v-for="o in ATTENDANCE_STATUS" :key="o.id" :value="o.id">{{
                  o.name
                }}</a-select-option>
              </a-select>
            </div>
            <div class="col-md-3 col-sm-6">
              <div class="label-title">Hình thức học:</div>
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
            <div class="col-12">
              <div class="d-flex justify-content-center flex-wrap gap-2 mt-2">
                <a-button class="btn-light" @click="handleSubmitFilter">
                  <FilterFilled /> Lọc
                </a-button>
                <a-button class="btn-gray" @click="handleClearFilter"> Huỷ lọc </a-button>
              </div>
            </div>
          </div>
        </a-card>
      </div>

      <div class="col-12">
        <a-card :bordered="false" class="cart">
          <template #title> <UnorderedListOutlined /> Danh sách ca học hôm nay</template>

          <a-table
            rowKey="id"
            class="nowrap"
            :dataSource="lstData"
            :columns="columns"
            :loading="isLoading"
            :pagination="pagination"
            :scroll="{ x: 'auto' }"
            @change="handleTableChange"
          >
            <template #bodyCell="{ column, record }">
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
              <template v-if="column.dataIndex === 'startDate'">
                {{
                  `${formatDate(record.startDate, 'HH:mm')} - ${formatDate(record.endDate, 'HH:mm')}`
                }}
              </template>

              <template v-if="column.dataIndex === 'status'">
                <span
                  v-if="
                    record.requiredCheckin === STATUS_REQUIRED_ATTENDANCE.ENABLE &&
                    record.requiredCheckout === STATUS_REQUIRED_ATTENDANCE.ENABLE
                  "
                >
                  <template v-if="record.status === ATTENDANCE_STATUS.CHECKIN.id">
                    <template v-if="Date.now() <= record.startDate">
                      <a-badge status="processing" /> {{ ATTENDANCE_STATUS.CHECKIN.name }}
                    </template>
                    <template v-else>
                      <a-badge status="error" /> {{ ATTENDANCE_STATUS.NOTCHECKOUT.name }}
                    </template>
                  </template>
                  <template v-else-if="record.status === ATTENDANCE_STATUS.ABSENT.id"
                    ><a-badge status="error" /> {{ ATTENDANCE_STATUS.ABSENT.name }}</template
                  >
                  <template v-else-if="record.status === ATTENDANCE_STATUS.PRESENT.id"
                    ><a-badge status="success" /> {{ ATTENDANCE_STATUS.PRESENT.name }}</template
                  >
                  <template v-else
                    ><a-badge status="warning" /> {{ ATTENDANCE_STATUS.NOTCHECKIN.name }}</template
                  >
                </span>
                <span v-else-if="record.requiredCheckin === STATUS_REQUIRED_ATTENDANCE.ENABLE">
                  <a-tag color="green" v-if="record.status === ATTENDANCE_STATUS.PRESENT.id">{{
                    ATTENDANCE_STATUS.PRESENT.name
                  }}</a-tag>
                  <a-tag color="orange" v-else>{{ ATTENDANCE_STATUS.NOTCHECKIN.name }}</a-tag>
                </span>
                <span v-else>
                  <a-tag color="green" v-if="record.status === ATTENDANCE_STATUS.PRESENT.id">{{
                    ATTENDANCE_STATUS.PRESENT.name
                  }}</a-tag>
                  <a-tag color="orange" v-else>{{ ATTENDANCE_STATUS.NOTCHECKOUT.name }}</a-tag>
                </span>
              </template>

              <template v-if="column.dataIndex === 'lateArrival'">
                <a-tag :color="record.lateArrival > 0 ? 'gold' : 'green'">
                  <ExclamationCircleOutlined /> {{ record.lateArrival + ' phút' }}
                </a-tag>
              </template>

              <template v-if="column.dataIndex === 'lateAttendance'">
                <a-tag
                  :color="
                    record.totalLateAttendance > 0
                      ? record.currentLateAttendance >= record.totalLateAttendance
                        ? 'red'
                        : 'green'
                      : 'default'
                  "
                  >{{ record.currentLateAttendance || 0 }} /
                  {{ record.totalLateAttendance }} lần</a-tag
                >
              </template>

              <template v-if="column.key === 'actions'">
                <template
                  v-if="
                    record.requiredCheckin === STATUS_REQUIRED_ATTENDANCE.ENABLE &&
                    record.requiredCheckout === STATUS_REQUIRED_ATTENDANCE.ENABLE
                  "
                >
                  <span v-if="record.status == ATTENDANCE_STATUS.NOTCHECKIN.id">
                    <span
                      v-if="
                        Date.now() < record.startDate - DEFAULT_EARLY_MINUTE_CHECKIN * 60 * 1000
                      "
                    >
                      <a-badge status="warning" />
                      Chưa đến giờ checkin
                    </span>
                    <span
                      v-else-if="Date.now() > record.startDate + record.lateArrival * 60 * 1000"
                    >
                      <template v-if="record.totalLateAttendance > record.currentLateAttendance">
                        <a-tooltip title="Checkin bù">
                          <a-button
                            type="primary"
                            class="btn-primary ms-2 border-0"
                            @click="handleCheckin(record)"
                          >
                            <CheckOutlined />
                            Checkin bù
                          </a-button>
                        </a-tooltip>
                      </template>
                      <template v-else> <a-badge status="error" /> Đã quá giờ checkin</template>
                    </span>
                    <a-tooltip title="Checkin đầu giờ" v-else>
                      <a-button
                        type="primary"
                        class="btn-info ms-2 border-0"
                        @click="handleCheckin(record)"
                      >
                        <CheckOutlined />
                        Checkin đầu giờ
                      </a-button>
                    </a-tooltip>
                  </span>
                  <span v-else-if="record.status == ATTENDANCE_STATUS.CHECKIN.id">
                    <span v-if="Date.now() > record.endDate + record.lateArrival * 60 * 1000">
                      <template v-if="record.totalLateAttendance > record.currentLateAttendance">
                        <a-tooltip title="Checkout bù">
                          <a-button
                            type="primary"
                            class="btn-primary ms-2 border-0"
                            @click="handleCheckin(record)"
                          >
                            <CheckOutlined /> Checkout bù
                          </a-button>
                        </a-tooltip>
                      </template>
                      <template v-else> <a-badge status="error" /> Đã quá giờ checkout </template>
                    </span>
                    <a-tooltip
                      title="Checkout cuối giờ"
                      v-else-if="
                        Date.now() >= record.endDate &&
                        Date.now() <= record.endDate + record.lateArrival * 60 * 1000
                      "
                    >
                      <a-button
                        type="primary"
                        class="btn-success ms-2 border-0"
                        @click="handleCheckin(record)"
                      >
                        <CheckOutlined /> Checkout cuối giờ
                      </a-button>
                    </a-tooltip>
                    <span v-else>
                      <a-badge status="warning" /> Checkin đầu giờ
                      {{ formatDate(record.timeCheckin, 'dd/MM/yyyy HH:mm') }}</span
                    >
                  </span>
                  <span v-else-if="record.status == ATTENDANCE_STATUS.PRESENT.id">
                    <a-badge status="success" />
                    <span class="text-success">Đã điểm danh</span>
                  </span>
                  <span v-else>
                    <a-badge status="error" />
                    <span class="text-danger">Chưa điểm danh</span>
                  </span>
                </template>
                <template v-else-if="record.requiredCheckout != STATUS_REQUIRED_ATTENDANCE.DISABLE">
                  <span v-if="record.status === ATTENDANCE_STATUS.PRESENT.id">
                    <a-badge status="success" />
                    <span class="text-success">Đã điểm danh</span>
                  </span>
                  <span v-else-if="Date.now() > record.endDate + record.lateArrival * 60 * 1000">
                    <template v-if="record.totalLateAttendance > record.currentLateAttendance">
                      <a-tooltip title="Checkout bù">
                        <a-button
                          type="primary"
                          class="btn-primary ms-2 border-0"
                          @click="handleCheckin(record)"
                        >
                          <CheckOutlined /> Checkout bù
                        </a-button>
                      </a-tooltip>
                    </template>
                    <template v-else> <a-badge status="error" /> Đã quá giờ checkout </template>
                  </span>
                  <a-tooltip
                    title="Checkout cuối giờ"
                    v-else-if="
                      Date.now() >= record.endDate &&
                      Date.now() <= record.endDate + record.lateArrival * 60 * 1000
                    "
                  >
                    <a-button
                      type="primary"
                      class="btn-success ms-2 border-0"
                      @click="handleCheckin(record)"
                    >
                      <CheckOutlined /> Checkout cuối giờ
                    </a-button>
                  </a-tooltip>
                  <span v-else> <a-badge status="warning" /> Chưa đến giờ checkout</span>
                </template>
                <template v-else-if="record.requiredCheckin !== STATUS_REQUIRED_ATTENDANCE.DISABLE">
                  <span v-if="record.status === ATTENDANCE_STATUS.PRESENT.id">
                    <a-badge status="success" />
                    <span class="text-success">Đã điểm danh</span>
                  </span>
                  <span
                    v-else-if="
                      Date.now() < record.startDate - DEFAULT_EARLY_MINUTE_CHECKIN * 60 * 1000
                    "
                  >
                    <a-badge status="warning" />
                    Chưa đến giờ checkin
                  </span>
                  <span v-else-if="Date.now() > record.startDate + record.lateArrival * 60 * 1000">
                    <template v-if="record.totalLateAttendance > record.currentLateAttendance">
                      <a-tooltip title="Checkin bù">
                        <a-button
                          type="primary"
                          class="btn-primary ms-2 border-0"
                          @click="handleCheckin(record)"
                        >
                          <CheckOutlined />
                          Checkin bù
                        </a-button>
                      </a-tooltip>
                    </template>
                    <template v-else> <a-badge status="error" /> Đã quá giờ checkin</template>
                  </span>
                  <a-tooltip title="Checkin đầu giờ" v-else>
                    <a-button
                      type="primary"
                      class="btn-info ms-2 border-0"
                      @click="handleCheckin(record)"
                    >
                      <CheckOutlined />
                      Checkin đầu giờ
                    </a-button>
                  </a-tooltip>
                </template>
                <template v-else>
                  <span
                    v-if="Date.now() < record.startDate - DEFAULT_EARLY_MINUTE_CHECKIN * 60 * 1000"
                  >
                    <a-badge status="warning" />
                    Chưa đến giờ điểm danh
                  </span>
                  <span v-else-if="Date.now() > record.startDate + record.lateArrival * 60 * 1000">
                    <a-badge status="error" />
                    Đã quá giờ điểm danh
                  </span>
                  <a-tooltip title="Điểm danh" v-else>
                    <a-button
                      type="primary"
                      class="btn-info ms-2 border-0"
                      @click="handleCheckin(record)"
                    >
                      <CheckOutlined />
                      Điểm danh
                    </a-button>
                  </a-tooltip>
                </template>
              </template>
            </template>
          </a-table>
        </a-card>
      </div>
    </div>
  </div>
</template>
