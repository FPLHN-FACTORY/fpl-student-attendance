<script setup>
import { ATTENDANCE_STATUS, DEFAULT_DATE_FORMAT, DEFAULT_PAGINATION } from '@/constants'
import { GLOBAL_ROUTE_NAMES } from '@/constants/routesConstant'
import { API_ROUTES_STUDENT } from '@/constants/studentConstant'
import { ROUTE_NAMES } from '@/router/studentRoute'
import requestAPI from '@/services/requestApiService'
import useBreadcrumbStore from '@/stores/useBreadCrumbStore'
import { dayOfWeek, formatDate } from '@/utils/utils'
import {
  CheckOutlined,
  FilterFilled,
  SearchOutlined,
  UnorderedListOutlined,
} from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import { nextTick, onMounted, reactive, ref, watch } from 'vue'
import * as faceapi from 'face-api.js'
import useAuthStore from '@/stores/useAuthStore'
import useLoadingStore from '@/stores/useLoadingStore'

const authStore = useAuthStore()
const loadingPage = useLoadingStore()

const isLoading = ref(false)

const lstData = ref([])

const isShowCamera = ref(false)
const isRunScan = ref(false)

const video = ref(null)
const canvas = ref(null)
const step = ref(0)
const textStep = ref('Vui lòng đợi camera...')

const formData = reactive({
  idPlanDate: null,
  faceEmbedding: null,
})

const breadcrumbStore = useBreadcrumbStore()

const breadcrumb = ref([
  {
    name: GLOBAL_ROUTE_NAMES.STAFF_PAGE,
    breadcrumbName: 'Sinh viên',
  },
  {
    name: ROUTE_NAMES.ATTENDANCE,
    breadcrumbName: 'Điểm danh',
  },
])

const columns = ref([
  { title: '#', dataIndex: 'orderNumber', key: 'orderNumber', width: 50 },
  { title: 'Thời gian', dataIndex: 'date', key: 'date', width: 100 },
  { title: 'Ca học', dataIndex: 'shift', key: 'shift', width: 200 },
  { title: 'Lớp học', dataIndex: 'factoryName', key: 'factoryName' },
  { title: 'Giảng viên', dataIndex: 'teacherName', key: 'teacherName' },
  { title: 'Trạng thái', dataIndex: 'status', key: 'status' },
  { title: '', key: 'actions' },
])

const pagination = ref({ ...DEFAULT_PAGINATION })

const dataFilter = reactive({
  keyword: null,
  status: null,
})

const renderTextStep = (text) => {
  if (text) {
    return (textStep.value = text)
  }
  switch (step.value) {
    case 0:
      return (textStep.value = 'Vui lòng nhìn thẳng')
    case 1:
      return (textStep.value = 'Vui lòng quay mặt sang phải')
    case 2:
      return (textStep.value = 'Vui lòng quay mặt sang trái')
    case 3:
      return (textStep.value = 'Vui lòng nhìn thẳng')
    case 4:
      return (textStep.value = 'Xác minh hoàn tất')
    default:
      return (textStep.value = 'Vui lòng nhìn vào camera')
  }
}

const fetchDataList = () => {
  if (isLoading.value === true) {
    return
  }

  isLoading.value = true
  requestAPI
    .get(`${API_ROUTES_STUDENT.FETCH_DATA_ATTENDANCE}`, {
      params: {
        page: pagination.value.current,
        size: pagination.value.pageSize,
        ...dataFilter,
      },
    })
    .then(({ data: response }) => {
      lstData.value = response.data.data
      pagination.value.total = response.data.totalPages * pagination.value.pageSize
    })
    .catch((error) => {
      message.error(error?.response?.data?.message || 'Không thể tải danh sách dữ liệu')
    })
    .finally(() => {
      isLoading.value = false
    })
}

const handleClearFilter = () => {
  Object.assign(dataFilter, {
    keyword: null,
    status: null,
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
      message.error(error?.response?.data?.message || 'Không thể điểm danh ca học này')
    })
    .finally(() => {
      loadingPage.hide()
    })
}

const startVideo = async () => {
  try {
    const constraints = {
      video: { facingMode: 'environment' },
    }
    const stream = await navigator.mediaDevices.getUserMedia(constraints)
    if (video.value) video.value.srcObject = stream
  } catch (err) {
    Modal.confirm({
      title: `Cảnh báo`,
      type: 'error',
      content: `Không thể kết nối tới Webcam. Vui lòng tải lại trang.`,
      okText: 'Thử lại',
      cancelText: 'Hủy bỏ',
      onOk() {
        window.location.reload()
      },
    })
  }
}

const stopVideo = () => {
  isRunScan.value = false
  const stream = video.value.srcObject

  if (stream) {
    const tracks = stream.getTracks()
    tracks.forEach((track) => track.stop())
    video.value.srcObject = null
  }
}

const loadModels = async () => {
  await faceapi.nets.tinyFaceDetector.loadFromUri('/models')
  await faceapi.nets.faceLandmark68Net.loadFromUri('/models')
  await faceapi.nets.faceRecognitionNet.loadFromUri('/models')
  await faceapi.nets.faceExpressionNet.loadFromUri('/models')
}

const detectFace = async () => {
  const options = new faceapi.TinyFaceDetectorOptions({ minConfidence: 0.5 })
  const displaySize = { width: video.value.videoWidth, height: video.value.videoHeight }

  faceapi.matchDimensions(canvas.value, displaySize)

  let faceDescriptor = null

  const getFaceAngle = (landmarks) => {
    const leftEye = landmarks.getLeftEye()
    const rightEye = landmarks.getRightEye()
    const nose = landmarks.getNose()

    const deltaX = rightEye[0].x - leftEye[0].x
    const deltaY = rightEye[0].y - leftEye[0].y
    const eyeAngle = Math.atan2(deltaY, deltaX) * (180 / Math.PI)

    const noseX = nose[0].x
    const faceCenterX = (leftEye[0].x + rightEye[3].x) / 2

    if (noseX < faceCenterX - 8) {
      return -1
    } else if (noseX > faceCenterX + 8) {
      return 1
    }
    return Math.abs(eyeAngle) < 5 ? 0 : -2
  }

  const runTask = async () => {
    const detections = await faceapi
      .detectAllFaces(video.value, options)
      .withFaceLandmarks()
      .withFaceDescriptors()

    if (detections.length === 1) {
      const detection = detections[0]
      const landmarks = detection.landmarks
      const descriptor = detection.descriptor

      const angle = getFaceAngle(landmarks)

      if (!faceDescriptor && angle === 0) {
        faceDescriptor = descriptor
        return renderTextStep()
      }

      if (faceDescriptor) {
        const distance = faceapi.euclideanDistance(faceDescriptor, descriptor)
        if (distance > 0.5) {
          return
        }

        renderTextStep()
        if (step.value === 0 && angle === 0) {
          step.value = 1
        } else if (step.value === 1 && angle === -1) {
          step.value = 2
        } else if (step.value === 2 && angle === 1) {
          step.value = 3
        } else if (step.value === 3 && angle === 0) {
          step.value = 4
          formData.faceEmbedding = JSON.stringify(Array.from(descriptor))
          handleSubmitAttendance()
          stopVideo()
          isShowCamera.value = false
          isRunScan.value = false
        } else {
          faceDescriptor = null
        }
      }
    } else {
      if (detections.length > 1) {
        renderTextStep('Có quá nhiều khuôn mặt!')
      } else {
        renderTextStep('Vui lòng nhìn vào camera')
        faceDescriptor = null
        step.value = 0
      }
    }
  }

  while (isRunScan.value) {
    if (
      faceapi.nets.tinyFaceDetector.isLoaded &&
      faceapi.nets.faceLandmark68Net.isLoaded &&
      faceapi.nets.faceRecognitionNet.isLoaded &&
      faceapi.nets.faceExpressionNet.isLoaded &&
      video.value.readyState === 4
    ) {
      await runTask()
    }
    await new Promise((resolve) => setTimeout(resolve, 200))
  }
}

const handleCheckin = async (item) => {
  formData.idPlanDate = item.idPlanDate

  isShowCamera.value = true
  isRunScan.value = true
  step.value = 0
  await nextTick()
  await startVideo().then(detectFace)
}

onMounted(async () => {
  breadcrumbStore.setRoutes(breadcrumb.value)
  fetchDataList()
  await loadModels()
})

watch(
  dataFilter,
  () => {
    handleSubmitFilter()
  },
  { deep: true },
)
</script>

<template>
  <a-modal v-model:open="isShowCamera" title="Đăng ký khuôn mặt" @cancel="stopVideo" :footer="null">
    <div class="video-container">
      <canvas ref="canvas"></canvas>
      <video ref="video" autoplay muted></video>
      <div
        class="face-id-loading"
        :class="{
          checking: step > 0,
          step1: step > 0,
          step2: step > 1,
          step3: step > 2,
          step4: step > 3,
        }"
      >
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
    </div>
    <div class="face-id-text" v-show="textStep != null">
      {{ textStep }}
    </div>
  </a-modal>

  <div class="container-fluid">
    <div class="row g-3">
      <div class="col-12">
        <!-- Bộ lọc tìm kiếm -->
        <a-card :bordered="false" class="cart">
          <template #title> <FilterFilled /> Bộ lọc </template>
          <div class="row g-2">
            <div class="col-md-8 col-sm-12">
              <div class="label-title">Từ khoá:</div>
              <a-input
                v-model:value="dataFilter.keyword"
                placeholder="Tìm theo tên lớp học..."
                allowClear
              >
                <template #prefix>
                  <SearchOutlined />
                </template>
              </a-input>
            </div>
            <div class="col-md-4 col-sm-12">
              <div class="label-title">Trạng thái:</div>
              <a-select
                v-model:value="dataFilter.status"
                class="w-100"
                :dropdownMatchSelectWidth="false"
                placeholder="-- Tất cả trạng thái --"
                allowClear
              >
                <a-select-option :value="null">-- Tất cả trạng thái --</a-select-option>
                <a-select-option v-for="o in ATTENDANCE_STATUS" :value="o.id">{{
                  o.name
                }}</a-select-option>
              </a-select>
            </div>
            <div class="col-12">
              <div class="d-flex justify-content-center flex-wrap gap-2 mt-3">
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
            :scroll="{ y: 500, x: 'auto' }"
            @change="handleTableChange"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.dataIndex === 'date'">
                {{
                  `${dayOfWeek(record.date)} - ${formatDate(record.date, DEFAULT_DATE_FORMAT + ' HH:mm')}`
                }}
              </template>
              <template v-if="column.dataIndex === 'status'">
                <a-tag :color="blue" v-if="record.status === ATTENDANCE_STATUS.CHECKIN.id">{{
                  ATTENDANCE_STATUS.CHECKIN.name
                }}</a-tag>
                <a-tag :color="red" v-else-if="record.status === ATTENDANCE_STATUS.ABSENT.id">{{
                  ATTENDANCE_STATUS.ABSENT.name
                }}</a-tag>
                <a-tag :color="green" v-else-if="record.status === ATTENDANCE_STATUS.PRESENT.id">{{
                  ATTENDANCE_STATUS.PRESENT.name
                }}</a-tag>
                <a-tag color="orange" v-else>{{ ATTENDANCE_STATUS.NOTCHECKIN.name }}</a-tag>
              </template>

              <template v-if="column.key === 'actions'">
                <span
                  v-if="
                    record.status == ATTENDANCE_STATUS.NOTCHECKIN.id &&
                    Date.now() <= record.date - 10 * 60 * 1000
                  "
                  ><a-badge status="warning"></a-badge>Chưa đến giờ checkin</span
                >
                <span
                  v-else-if="
                    record.status == ATTENDANCE_STATUS.NOTCHECKIN.id &&
                    Date.now() > record.date + record.lateArrival * 60 * 1000
                  "
                  ><a-badge status="error"></a-badge>Đã quá giờ checkin</span
                >
                <a-tooltip
                  title="Checkin điểm danh"
                  v-else-if="record.status == ATTENDANCE_STATUS.NOTCHECKIN.id"
                >
                  <a-button
                    type="primary"
                    class="btn-info ms-2 border-0"
                    @click="handleCheckin(record)"
                  >
                    <CheckOutlined /> Checkin
                  </a-button>
                </a-tooltip>
                <span v-else
                  ><a-badge status="success"></a-badge>Checkin lúc
                  {{ formatDate(record.timeCheckin, 'dd/MM/yyyy HH:mm') }}</span
                >
              </template>
            </template>
          </a-table>
        </a-card>
      </div>
    </div>
  </div>
</template>
