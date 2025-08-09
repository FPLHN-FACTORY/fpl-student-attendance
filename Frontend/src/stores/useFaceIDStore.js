import { message, Modal } from 'ant-design-vue'
import { defineStore } from 'pinia'
import { ref, toRaw } from 'vue'
import * as tf from '@tensorflow/tfjs'
import Human from '@vladmandic/human'
import Config from '@/constants/humanConfig'
import { isProbablyMobile } from '@/utils/utils'

const THRESHOLD_P = 0.2
const THRESHOLD_X = 0.1
const THRESHOLD_Y = 0.12
const THRESHOLD_EMOTIONS = 0.8
const MIN_BRIGHTNESS = 80
const MAX_BRIGHTNESS = 160
const THRESHOLD_LIGHT = 60
const SIZE_CAMERA = 480
const SKIP_FRAME = 5
const ANTISPOOF = false
const CHECK_DISTANCE = true

const useFaceIDStore = defineStore('faceID', () => {
  let isFullStep = false
  let video = null
  let canvas = null
  let axis = null
  let onSuccess = null
  let isShowError = true

  let human = null

  const isRunScan = ref(false)
  const isLoading = ref(true)
  const isLoadingModels = ref(false)
  const isReady = ref(false)
  const isSuccess = ref(false)

  const step = ref(0)
  const textStep = ref('Vui lòng đợi camera...')
  const dataImage = ref(null)
  const count = ref(0)
  const embedding = ref(null)
  const prevEmbedding = ref(null)
  const lstDescriptor = ref([])

  const renderTextStep = async (text) => {
    if (text) {
      return (textStep.value = text)
    }

    if (isFullStep) {
      switch (step.value) {
        case 0:
          return (textStep.value = 'Vui lòng nhìn thẳng')
        case 1:
          return (textStep.value = 'Vui lòng giữ nguyên...')
        default:
          return (textStep.value = 'Vui lòng nhìn vào camera')
      }
    }

    switch (step.value) {
      case 0:
        return (textStep.value = 'Vui lòng nhìn thẳng')
      case 1:
        return (textStep.value = 'Vui lòng quay mặt sang phải 15°')
      case 2:
        return (textStep.value = 'Vui lòng quay mặt sang trái 15°')
      case 3:
        return (textStep.value = 'Vui lòng nhìn thẳng')
      case 4:
        return (textStep.value = 'Vui lòng giữ nguyên...')
      default:
        return (textStep.value = 'Vui lòng nhìn vào camera')
    }
  }

  const init = async (v, c, a, fullStep, callback) => {
    setup(v, c, a, fullStep)
    setFullStep(fullStep)
    setCallback(callback)
  }

  const setup = async (v, c, a) => {
    video = v
    canvas = c
    axis = a
  }

  const setShowError = (type) => {
    isShowError = type
  }

  const setFullStep = (type) => {
    isFullStep = type
  }

  const setCallback = (callback) => {
    onSuccess = callback
  }

  const toDegrees = (rad) => rad * (180 / Math.PI)

  const startVideo = async () => {
    step.value = 0
    dataImage.value = null
    lstDescriptor.value = []
    isRunScan.value = true
    isLoading.value = true
    try {
      const constraints = {
        video: {
          facingMode: 'user',
          width: { ideal: SIZE_CAMERA },
          height: { ideal: SIZE_CAMERA },
        },
      }
      const stream = await navigator.mediaDevices.getUserMedia(constraints)
      if (video.value) {
        video.value.srcObject = stream
        detectFace()
      }
    } catch (err) {
      if (isShowError) {
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
  }

  const stopVideo = () => {
    isRunScan.value = false
    isLoading.value = false
    const stream = video.value.srcObject
    if (stream) {
      const tracks = stream.getTracks()
      tracks.forEach((track) => track.stop())
      video.value.srcObject = null
    }
  }

  let maskModel
  let glassesModel
  let antispoofModel
  const loadModels = async () => {
    const preferBackends = ['webgpu', 'webgl', 'cpu']
    for (const backend of preferBackends) {
      try {
        if (backend === 'webgpu') {
          if (typeof navigator === 'undefined' || typeof navigator.gpu === 'undefined') {
            continue
          }
          const adapter = await navigator.gpu.requestAdapter()
          if (!adapter) {
            continue
          }
        }

        await tf.setBackend(backend)
        await tf.ready()
        human = new Human({ ...Config, backend })
        break
      } catch {
        if (isShowError) {
          message.error('Không thể khởi chạy camera')
        }
      }
    }

    const models = []

    if (!isFullStep) {
      const loadMaskModel = async () => {
        try {
          maskModel = await tf.loadLayersModel('indexeddb://maskes-model')
        } catch (err) {
          maskModel = await tf.loadLayersModel('/models/maskes/model.json')
          await maskModel.save('indexeddb://maskes-model')
        }
      }

      const loadGlassModel = async () => {
        try {
          glassesModel = await tf.loadLayersModel('indexeddb://glasses-model')
        } catch (err) {
          glassesModel = await tf.loadLayersModel('/models/glasses/model.json')
          await glassesModel.save('indexeddb://glasses-model')
        }
      }

      if (!maskModel) {
        models.push(loadMaskModel())
      }

      if (!glassesModel) {
        models.push(loadGlassModel())
      }
    }

    if (ANTISPOOF) {
      const loadAntispoofModel = async () => {
        try {
          antispoofModel = await tf.loadLayersModel('indexeddb://antispoof-model')
        } catch (err) {
          antispoofModel = await tf.loadLayersModel('/models/antispoof/model.json')
          await antispoofModel.save('indexeddb://antispoof-model')
        }
      }

      if (!antispoofModel) {
        models.push(loadAntispoofModel())
      }
    }

    models.push(human.load())
    models.push(human.warmup())

    await Promise.all(models)
    isLoadingModels.value = true
  }

  const detectFace = async () => {
    let faceDescriptor = null

    const ctx = canvas.value.getContext('2d')

    const aX = axis.value.querySelectorAll('.a-x > div')
    const aY = axis.value.querySelectorAll('.a-y > div')

    const getAverageBrightness = async (imageData) => {
      const data = imageData.data
      let sum = 0

      for (let i = 0; i < data.length; i += 4) {
        const avg = (data[i] + data[i + 1] + data[i + 2]) / 3
        sum += avg
      }

      return sum / (imageData.width * imageData.height)
    }

    const isLightBalance = async () => {
      const width = canvas.value.width
      const height = canvas.value.height
      const halfWidth = Math.floor(width / 2)

      const leftData = ctx.getImageData(0, 0, halfWidth, height)
      const rightData = ctx.getImageData(halfWidth, 0, width - halfWidth, height)

      const brightnessLeft = await getAverageBrightness(leftData)
      const brightnessRight = await getAverageBrightness(rightData)

      const brightnessDiff = Math.abs(brightnessLeft - brightnessRight)
      return brightnessDiff <= THRESHOLD_LIGHT
    }

    const getFaceAngle = async () => {
      const face = human.result?.face?.[0]
      if (!face) {
        return
      }

      const { yaw, pitch } = face.rotation?.angle || {}

      const yawDeg = toDegrees(yaw)
      const pitchDeg = toDegrees(pitch)

      updateAxis(pitchDeg, yawDeg)

      if (
        Math.abs(yaw) < THRESHOLD_X &&
        Math.abs(pitch) < THRESHOLD_P &&
        human.result.gesture.some((o) => o.gesture.includes('facing center'))
      ) {
        return 0
      }

      if (
        human.result.gesture.some((o) => o.gesture.includes('facing left')) &&
        Math.abs(yaw) > THRESHOLD_Y
      ) {
        return 1
      }

      if (
        human.result.gesture.some((o) => o.gesture.includes('facing right')) &&
        Math.abs(yaw) > THRESHOLD_Y
      ) {
        return -1
      }

      return -2
    }

    const isWithMask = async () => {
      const input = tf.browser
        .fromPixels(canvas.value)
        .resizeNearestNeighbor([224, 224])
        .toFloat()
        .div(255)
        .expandDims()
      const result = await maskModel.predict(input).data()
      input.dispose()
      return result[0] > 0.9
    }

    const isWithGlasses = async () => {
      const input = tf.browser
        .fromPixels(canvas.value)
        .resizeNearestNeighbor([224, 224])
        .toFloat()
        .div(255)
        .expandDims()
      const result = await glassesModel.predict(input).data()
      input.dispose()
      return !(result[0] > 0.6)
    }

    const isMobile = isProbablyMobile()
    const isSpoofing = async () => {
      const input = tf.browser
        .fromPixels(canvas.value)
        .resizeNearestNeighbor([224, 224])
        .toFloat()
        .div(255)
        .expandDims()
      const result = await antispoofModel.predict(input).data()
      input.dispose()
      return result[0] < (isMobile ? 0 : 0.99999999)
    }

    const captureFace = () => {
      dataImage.value = canvas.value.toDataURL('image/png')
    }

    const updateAxis = (x, y) => {
      axis.value.classList.add('active')
      aX.forEach((o) => {
        o.style.transform = `rotateY(${90 + x}deg)`
      })
      aY.forEach((o) => {
        o.style.transform = `rotateX(${90 - y}deg)`
      })
    }

    const normalize = (v) => {
      const norm = Math.sqrt(v.reduce((sum, val) => sum + val * val, 0))
      return v.map((val) => val / norm)
    }

    const clearDescriptor = () => {
      lstDescriptor.value = []
    }

    const rollback = (text) => {
      lstDescriptor.value.pop()
      step.value = Math.max(0, step.value - 1)
      renderTextStep(text)
    }

    const pushDescriptor = async (max) => {
      count.value = 0
      const descriptor = normalize(Array.from(embedding.value))
      if (!descriptor.length) {
        return rollback()
      }

      lstDescriptor.value.push(descriptor)

      if (max !== lstDescriptor.value.length) {
        step.value = 0
        return clearDescriptor()
      }
    }

    const submit = async () => {
      if (!lstDescriptor.value.length) {
        step.value = 0
        return renderTextStep()
      }

      const similarity = human.match.similarity(
        lstDescriptor.value[0],
        lstDescriptor.value[lstDescriptor.value.length - 1],
      )

      if (similarity < 0.7) {
        return rollback('Vui lòng xác nhận lại')
      }

      isSuccess.value = true
      renderTextStep('Xác minh hoàn tất')
      captureFace()
      try {
        typeof onSuccess == 'function' && onSuccess(toRaw(lstDescriptor.value))
      } catch (error) {
        console.error(error)
      }

      await delay(3000)
      lstDescriptor.value = []
      faceDescriptor = null
      step.value = 0
      isSuccess.value = false
      renderTextStep()
    }

    const delay = (ms) => new Promise((res) => setTimeout(res, ms))

    const isLiveness = async () => {
      let lst_spoofing = []

      while (lst_spoofing.length < 4) {
        const result = await human.detect(video.value)
        const face = result?.face?.[0]

        if (!face) {
          return false
        }

        const checking = await isSpoofing()
        lst_spoofing.push(checking)
      }
      return lst_spoofing.filter((o) => !o).length > 1
    }

    const isReaction = async () => {
      let progress = []
      let i = 0
      while (i < 4) {
        i++
        const result = await human.detect(video.value)
        const face = result?.face?.[0]

        if (!face) {
          return false
        }

        const checking = face.emotion.some(
          (e) =>
            ['happy', 'angry', 'surprise', 'disgust'].includes(e.emotion) &&
            e.score > THRESHOLD_EMOTIONS,
        )
        progress.push(checking)
      }
      return progress.filter((o) => o).length > 2
    }

    const getEmbedding = async () => {
      const result = await human.detect(video.value)
      const face = result.face?.[0]

      if (
        !face ||
        !(await cropFace(face)) ||
        !isInsideCenter(face.boxRaw) ||
        (!isFullStep && (await isInvalidSize(face)))
      ) {
        return []
      }

      const { pitch, roll, yaw } = face.rotation?.angle || {}

      if (
        (human.result.gesture.some(
          (o) =>
            o.gesture.includes('head up') ||
            o.gesture.includes('head down') ||
            o.gesture.includes('blink left eye') ||
            o.gesture.includes('blink right eye'),
        ) &&
          Math.abs(pitch) > 0.2) ||
        Math.abs(roll) > 0.05 ||
        Math.abs(yaw) > 0.05
      ) {
        return []
      }

      const result2 = await human.detect(canvas.value)
      const face2 = result2.face?.[0]

      if (!face2) {
        return []
      }
      return face2.embedding
    }

    const getBestEmbedding = async (startCount, endCount, callbackError) => {
      await delay(1000)
      embedding.value = null
      count.value = startCount
      while (count.value > endCount) {
        renderTextStep('Vui lòng giữ nguyên...')

        const [_, currentEmbedding] = await Promise.all([delay(1000), getEmbedding()])

        if (!currentEmbedding || currentEmbedding.length === 0) {
          embedding.value = null
          callbackError && callbackError()
          return (count.value = 0)
        }

        if (prevEmbedding.value && !isSameEmbedding(prevEmbedding.value, currentEmbedding)) {
          embedding.value = null
          renderTextStep('Vui lòng không cử động')
          callbackError && callbackError()
          return (count.value = 0)
        }

        prevEmbedding.value = currentEmbedding
        embedding.value = currentEmbedding
        count.value -= 1
      }
    }

    const isSameEmbedding = (prevEmbedding, currentEmbedding) => {
      const similarity = human.match.similarity(prevEmbedding, currentEmbedding)
      return similarity >= isMobile ? 0.8 : 0.9
    }

    const getFaceBox = (face) => {
      const mesh = face.mesh
      if (!mesh || mesh.length < 468) return false

      const leftCheek = mesh[234]
      const rightCheek = mesh[454]

      const yValues = mesh.map((p) => p[1])
      const minY = Math.min(...yValues)

      const bottomLip = Math.max(mesh[14][1], mesh[17][1])

      let sx = leftCheek[0]
      let ex = rightCheek[0]
      let sy = minY
      let ey = bottomLip

      const padding = 0
      const paddingTop = 0
      const paddingBottom = 80
      const paddingLeft = 35
      const paddingRight = 35
      sx -= padding + paddingLeft
      ex += padding + paddingRight
      sy -= padding + paddingTop
      ey += padding + paddingBottom

      const vw = video.value.videoWidth
      const vh = video.value.videoHeight

      sx = Math.max(0, Math.min(sx, vw))
      ex = Math.max(0, Math.min(ex, vw))
      sy = Math.max(0, Math.min(sy, vh))
      ey = Math.max(0, Math.min(ey, vh))

      const width = ex - sx
      const height = ey - sy
      const size = Math.max(width, height)

      const centerX = (sx + ex) / 2
      const centerY = (sy + ey) / 2

      return {
        sx,
        sy,
        ex,
        ey,
        width,
        height,
        size,
        centerX,
        centerY,
      }
    }

    const cropFace = async (face) => {
      if (face.score < 1) {
        return false
      }

      const { size, sx, sy } = getFaceBox(face)

      canvas.value.width = video.value.videoWidth
      canvas.value.height = video.value.videoHeight
      ctx.drawImage(video.value, sx, sy, size, size, 0, 0, canvas.value.width, canvas.value.height)
      return true
    }

    const isInsideCenter = (faceBoxRaw) => {
      const [xRaw, yRaw, wRaw, hRaw] = faceBoxRaw

      const centerX = xRaw + wRaw / 2
      const centerY = yRaw + hRaw / 2

      const margin = 0.1

      const insideCenter =
        centerX > 0.5 - margin &&
        centerX < 0.5 + margin &&
        centerY > 0.5 - margin &&
        centerY < 0.5 + margin
      return insideCenter
    }

    const isInvalidSize = async (face) => {
      if (!CHECK_DISTANCE) {
        return null
      }
      const { width, height } = getFaceBox(face)
      const faceArea = width * height
      const videoArea = video.value.videoWidth * video.value.videoHeight
      const ratio = faceArea / videoArea

      if (ratio < 0.8) {
        return 'Vui lòng đưa mặt lại gần hơn'
      }

      if (ratio > 1) {
        return 'Vui lòng đưa mặt ra xa hơn'
      }

      return null
    }

    let error = 0
    const runTask = async () => {
      if (!faceDescriptor) {
        if (axis.value) {
          axis.value.classList.remove('active')
          aX.forEach((o) => {
            o.style.transform = `rotateY(90deg)`
          })
          aY.forEach((o) => {
            o.style.transform = `rotateX(90deg)`
          })
        }
      }

      const detections = await human.detect(video.value)

      if (detections.face?.length > 1) {
        return renderTextStep('Có quá nhiều khuôn mặt!')
      }

      if (detections.face?.length < 1) {
        error++
        if (error > 10) {
          step.value = Math.max(0, step.value - 1)
        }
        if (step.value === 0) {
          faceDescriptor = null
        }
        isReady.value = false
        return renderTextStep('Vui lòng nhìn vào camera')
      }

      error = 0
      const detection = detections.face?.[0]
      const faceBox = detection.box
      const faceBoxRaw = detection.boxRaw

      const angle = await getFaceAngle()

      if (!(await cropFace(detection))) {
        return
      }

      if (isFullStep || (!isFullStep && (step.value === 0 || step.value === 3))) {
        if (!isInsideCenter(faceBoxRaw)) {
          return rollback('Vui lòng căn chỉnh khuôn mặt vào giữa')
        }
        const txtValidSize = await isInvalidSize(detection)
        if (txtValidSize !== null) {
          return rollback(txtValidSize)
        }
      }

      if (!faceDescriptor) {
        faceDescriptor = [...detection.embedding]
        if (step.value >= 0) {
          step.value = 0
          clearDescriptor()
        }
        return
      }

      const faceImageData = ctx.getImageData(0, 0, canvas.value.width, canvas.value.height)
      const avgBrightness = await getAverageBrightness(faceImageData)
      if (avgBrightness === 0) {
        return
      }

      if (ANTISPOOF && !(!isFullStep && (step.value === 1 || step.value === 2))) {
        if (!(await isLiveness())) {
          return
        }
      }

      if (isFullStep || (!isFullStep && (step.value === 0 || step.value === 3))) {
        if (avgBrightness < MIN_BRIGHTNESS) {
          return renderTextStep('Camera quá tối, Vui lòng tăng độ sáng')
        }

        if (avgBrightness > MAX_BRIGHTNESS) {
          return renderTextStep('Camera quá sáng, Vui lòng giảm độ sáng')
        }

        if (!(await isLightBalance(faceBox))) {
          return renderTextStep('Ánh sáng không đều. Vui lòng thử lại')
        }

        const { pitch, roll } = detection.rotation?.angle || {}

        if (
          human.result.gesture.some((o) => o.gesture.includes('head up')) &&
          Math.abs(pitch) > 0.2
        ) {
          return renderTextStep('Vui lòng không ngẩng mặt')
        }

        if (
          human.result.gesture.some((o) => o.gesture.includes('head down')) &&
          Math.abs(pitch) > 0.2
        ) {
          return renderTextStep('Vui lòng không cúi mặt')
        }

        if (Math.abs(roll) > 0.05) {
          return renderTextStep('Vui lòng không nghiêng đầu')
        }

        if (await isReaction()) {
          return renderTextStep('Vui lòng không biểu cảm')
        }
      }

      if (!isFullStep) {
        if (await isWithGlasses()) {
          step.value = 0
          return renderTextStep('Vui lòng không nhắm mắt hoặc đeo kính')
        }

        if (await isWithMask()) {
          step.value = 0
          return renderTextStep('Vui lòng không đeo khẩu trang')
        }
      }

      if (faceDescriptor) {
        isReady.value = true
        count.value = 0

        renderTextStep()

        if (isFullStep) {
          if (step.value === 0 && angle === 0) {
            clearDescriptor()
            prevEmbedding.value = null
            await getBestEmbedding(3, 1)
            if (!embedding.value || embedding.length < 1) {
              return
            }
            await pushDescriptor(1)
            step.value = 1
            return renderTextStep()
          }
          if (step.value === 1) {
            await getBestEmbedding(1, 0, () => {
              step.value = 0
              renderTextStep()
            })
            if (!embedding.value || embedding.length < 1) {
              return
            }
            await pushDescriptor(2)
            step.value = 2
            return await submit()
          }
        } else {
          if (step.value === 0 && angle === 0) {
            clearDescriptor()
            prevEmbedding.value = null
            await getBestEmbedding(3, 0)
            if (!embedding.value || embedding.length < 1) {
              return
            }
            await pushDescriptor(1)
            step.value = 1
            return renderTextStep()
          }
          if (step.value === 1 && angle === -1) {
            step.value = 2
            return renderTextStep()
          }
          if (step.value === 2 && angle === 1) {
            step.value = 3
            renderTextStep()
            return delay(1000)
          }
          if (step.value === 3 && angle === 0) {
            prevEmbedding.value = null
            await getBestEmbedding(3, 0)
            if (!embedding.value || embedding.length < 1) {
              return
            }
            await pushDescriptor(2)
            step.value = 4
            return await submit()
          }
        }
      }
    }

    let frameCount = 0
    let isProgress = false
    let detectTimeoutId = null
    let detectAxiesTimeoutId = null
    const detectLoop = async () => {
      if (!isRunScan.value) return clearTimeout(detectTimeoutId)

      if (isLoadingModels.value && video.value?.readyState === 4 && frameCount % SKIP_FRAME === 0) {
        if (!isProgress) {
          isProgress = true
          try {
            await runTask()
          } finally {
            isProgress = false
          }
        }
        if (isLoading.value) {
          isLoading.value = false
        }
      }

      frameCount++
      detectTimeoutId = setTimeout(detectLoop, 10)
    }
    const detectAxies = async () => {
      if (!isRunScan.value) return clearTimeout(detectAxiesTimeoutId)

      if (isLoadingModels.value && video.value?.readyState === 4) {
        await getFaceAngle()
        if (isLoading.value) {
          isLoading.value = false
        }
      }
      detectAxiesTimeoutId = setTimeout(detectAxies, 10)
    }
    detectAxies()
    detectLoop()
  }

  const renderStyle = () => {
    if (isFullStep) {
      return {
        checking: step.value > 0,
        fullStep: isSuccess.value,
      }
    }
    return {
      checking: step.value > 0,
      step1: step.value > 0,
      step2: step.value > 1,
      step3: step.value > 2,
      step4: isSuccess.value,
    }
  }

  return {
    init,
    setup,
    setFullStep,
    setCallback,
    setShowError,
    loadModels,
    startVideo,
    stopVideo,
    detectFace,
    renderStyle,
    dataImage,
    step,
    textStep,
    isLoading,
    isReady,
    isSuccess,
    count,
  }
})

export default useFaceIDStore
