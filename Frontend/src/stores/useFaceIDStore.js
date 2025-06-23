import { message, Modal } from 'ant-design-vue'
import { defineStore } from 'pinia'
import { ref, toRaw } from 'vue'
import * as tf from '@tensorflow/tfjs'
import Human from '@vladmandic/human'
import Config from '@/constants/humanConfig'

const THRESHOLD_P = 0.2
const THRESHOLD_X = 0.1
const THRESHOLD_Y = 0.15
const THRESHOLD_EMOTIONS = 0.8
const MIN_BRIGHTNESS = 80
const MAX_BRIGHTNESS = 180
const THRESHOLD_LIGHT = 80
const MAX_HISTORY = 10
const SKIP_FRAME = 10

const useFaceIDStore = defineStore('faceID', () => {
  let isFullStep = false
  let video = null
  let canvas = null
  let axis = null
  let onSuccess = null

  let human = null

  const isRunScan = ref(false)
  const isLoading = ref(true)
  const isLoadingModels = ref(false)

  const step = ref(0)
  const textStep = ref('Vui lòng đợi camera...')
  const dataImage = ref(null)
  const lstDescriptor = ref([])

  const renderTextStep = (text) => {
    if (text) {
      return (textStep.value = text)
    }

    if (isFullStep) {
      switch (step.value) {
        case 0:
          return (textStep.value = 'Vui lòng nhìn thẳng')
        case 1:
          return (textStep.value = 'Xác minh hoàn tất')
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
        return (textStep.value = 'Xác minh hoàn tất')
      default:
        return (textStep.value = 'Vui lòng nhìn vào camera')
    }
  }

  const init = async (v, c, a, fullStep, callback) => {
    isFullStep = fullStep
    video = v
    canvas = c
    axis = a
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
        video: { facingMode: 'user', width: { ideal: 256 }, height: { ideal: 256 } },
      }
      const stream = await navigator.mediaDevices.getUserMedia(constraints)
      if (video.value) {
        video.value.srcObject = stream
        detectFace()
      }
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
        message.error('Không thể khởi chạy camera')
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

    models.push(human.load())
    models.push(human.warmup())

    await Promise.all(models)
    isLoadingModels.value = true
  }

  const detectFace = async () => {
    let faceDescriptor = null
    let realHistory = []
    let liveHistory = []
    let blinkState = { blinking: false, count: 0 }

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

    const isLightBalance = async (faceBox) => {
      const [x, y, width, height] = faceBox
      const halfWidth = Math.floor(width / 2)
      const leftData = ctx.getImageData(x, y, halfWidth, height)
      const rightData = ctx.getImageData(x + halfWidth, y, width - halfWidth, height)

      const brightnessLeft = await getAverageBrightness(leftData)
      const brightnessRight = await getAverageBrightness(rightData)

      const brightnessDiff = Math.abs(brightnessLeft - brightnessRight)
      return brightnessDiff > THRESHOLD_LIGHT ? false : true
    }

    const getFaceAngle = async (face) => {
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

    const rollback = () => {
      lstDescriptor.value.pop()
      step.value = Math.max(0, step.value - 1)
      renderTextStep()
    }

    const pushDescriptor = async (max) => {
      const descriptor = normalize(Array.from(await getBestEmbedding()))

      if (!descriptor.length) {
        return rollback()
      }

      lstDescriptor.value.push(descriptor)

      if (max !== lstDescriptor.value.length) {
        console.group(max, lstDescriptor.value.length)
        step.value = 0
        return clearDescriptor()
      }
    }

    const submit = async () => {
      if (!lstDescriptor.value.length) {
        return (step.value = 0)
      }

      if (
        cosineSimilarity(
          lstDescriptor.value[0],
          lstDescriptor.value[lstDescriptor.value.length - 1],
        ) < 0.9
      ) {
        return rollback()
      }
      captureFace()
      stopVideo()
      typeof onSuccess == 'function' && onSuccess(toRaw(lstDescriptor.value))
      isRunScan.value = false
    }

    const delay = (ms) => new Promise((res) => setTimeout(res, ms))

    const cosineSimilarity = (vec1, vec2) => {
      let dot = 0,
        normA = 0,
        normB = 0
      for (let i = 0; i < vec1.length; i++) {
        dot += vec1[i] * vec2[i]
        normA += vec1[i] * vec1[i]
        normB += vec2[i] * vec2[i]
      }
      return dot / (Math.sqrt(normA) * Math.sqrt(normB))
    }

    const getBestEmbedding = async () => {
      let prevEmbedding = null

      while (true) {
        const result = await human.detect(video.value)
        const face = result.face?.[0]

        if (!face?.embedding) {
          return []
        }

        if (prevEmbedding) {
          const similarity = cosineSimilarity(face.embedding, prevEmbedding)
          if (similarity > 0.95) {
            return face.embedding
          }
        }
        prevEmbedding = face.embedding
        await delay(500)
      }
    }

    const clearHistory = () => {
      realHistory = []
      liveHistory = []
      blinkState = { blinking: false, count: 0 }
    }

    const updateHistories = (face, gestures) => {
      const real = face.real ?? 0
      const live = face.live ?? 0

      realHistory.push(real)
      liveHistory.push(live)
      if (realHistory.length > MAX_HISTORY) realHistory.shift()
      if (liveHistory.length > MAX_HISTORY) liveHistory.shift()

      const isBlink = gestures.includes('blink left eye') || gestures.includes('blink right eye')
      if (isBlink && !blinkState.blinking) {
        blinkState.count++
        blinkState.blinking = true
      }
      if (!isBlink) {
        blinkState.blinking = false
      }
    }

    const calcDelta = (arr) => {
      let delta = 0
      for (let i = 1; i < arr.length; i++) {
        delta += Math.abs(arr[i] - arr[i - 1])
      }
      return delta
    }

    const isRealHuman = (face, options = { minConfidence: 0.6, minDelta: 0.2 }) => {
      const real = face.real ?? 0
      const live = face.live ?? 0
      const confidence = face.faceScore ?? 0

      const deltaReal = calcDelta(realHistory)
      const deltaLive = calcDelta(liveHistory)

      const dynamicEnough = deltaReal > options.minDelta || deltaLive > options.minDelta

      return (
        real >= options.minConfidence &&
        live >= options.minConfidence &&
        confidence >= options.minConfidence &&
        blinkState.count > 0 &&
        dynamicEnough
      )
    }

    let error = 0
    let antispoof = 0
    let is_fake = false
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
          clearHistory()
        }
        is_fake = false
        antispoof = 0
        return renderTextStep('Vui lòng nhìn vào camera')
      }

      if (is_fake) {
        return renderTextStep('Không thể nhận diện khuôn mặt')
      }

      error = 0
      const detection = detections.face?.[0]
      const emotions = detection.emotion || []
      const faceBox = detection.box
      const faceBoxRaw = detection.boxRaw
      const gestures = Object.values(human.result.gesture).map((g) => g.gesture)

      updateHistories(detection, gestures)

      if (!isRealHuman(detection)) {
        antispoof++
        if (antispoof > 10) {
          return (is_fake = true)
        }
      }

      antispoof = 0

      canvas.value.width = video.value.videoWidth
      canvas.value.height = video.value.videoHeight
      ctx.drawImage(video.value, 0, 0, canvas.value.width, canvas.value.height)

      if (detection?.tensor) {
        human.tf.dispose(detection?.tensor)
      }

      const angle = await getFaceAngle(detection)

      const [xRaw, yRaw, wRaw, hRaw] = faceBoxRaw

      const centerX = xRaw + wRaw / 2
      const centerY = yRaw + hRaw / 2

      const margin_y = 0.1
      const margin_x = 0.2
      const insideCenter =
        centerX > 0.5 - margin_x &&
        centerX < 0.5 + margin_x &&
        centerY > 0.5 - margin_y &&
        centerY < 0.5 + margin_y

      if (angle === 0) {
        if (human.result.gesture.some((o) => o.gesture.includes('head up'))) {
          return renderTextStep('Vui lòng không ngẩng mặt')
        }

        if (human.result.gesture.some((o) => o.gesture.includes('head down'))) {
          return renderTextStep('Vui lòng không cúi mặt')
        }
      }

      if (!insideCenter) {
        return renderTextStep('Vui lòng căn chỉnh khuôn mặt vào giữa')
      }

      if (!faceDescriptor) {
        faceDescriptor = [...detection.embedding]
        if (step.value >= 0) {
          step.value = 0
          clearDescriptor()
          clearHistory()
        }
        return
      }

      const [x, y, width, height] = faceBox

      const canvasFacebox = document.createElement('canvas')
      const ctxFacebox = canvasFacebox.getContext('2d')
      canvasFacebox.width = width
      canvasFacebox.height = height

      ctxFacebox.drawImage(video.value, x, y, width, height, 0, 0, width, height)

      const faceImageData = ctxFacebox.getImageData(0, 0, width, height)

      const avgBrightness = await getAverageBrightness(faceImageData)

      if (avgBrightness < MIN_BRIGHTNESS) {
        return renderTextStep('Camera quá tối, Vui lòng tăng độ sáng')
      }

      if (avgBrightness > MAX_BRIGHTNESS) {
        return renderTextStep('Camera quá sáng, Vui lòng giảm độ sáng')
      }

      if (!(await isLightBalance(faceBox))) {
        return renderTextStep('Ánh sáng không đều. Vui lòng thử lại')
      }

      if (
        emotions.some(
          (e) =>
            ['happy', 'angry', 'surprise', 'disgust'].includes(e.emotion) &&
            e.score > THRESHOLD_EMOTIONS,
        )
      ) {
        return renderTextStep('Vui lòng không biểu cảm')
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
        const similarity = human.match.similarity(faceDescriptor, detection.embedding)
        if (similarity < 0.5) {
          return clearHistory()
        }

        renderTextStep()

        if (isFullStep) {
          if (step.value === 0 && angle === 0) {
            clearDescriptor()
            await pushDescriptor(1)
            return (step.value = 1)
          }
          if (step.value === 1) {
            step.value = 2
            await pushDescriptor(2)
            return await submit()
          }
        } else {
          if (step.value === 0 && angle === 0) {
            clearDescriptor()
            await pushDescriptor(1)
            return (step.value = 1)
          }
          if (step.value === 1 && angle === -1) {
            return (step.value = 2)
          }
          if (step.value === 2 && angle === 1) {
            return (step.value = 3)
          }
          if (step.value === 3 && angle === 0) {
            step.value = 4
            await pushDescriptor(2)
            return await submit()
          }
        }
      }
    }

    let frameCount = 0
    let isProgress = false
    const detectLoop = async () => {
      if (!isRunScan.value) return

      if (isLoadingModels.value && video.value?.readyState === 4 && frameCount % SKIP_FRAME === 0) {
        if (!isProgress) {
          isProgress = true
          await runTask()
          isProgress = false
        }
        isLoading.value && (isLoading.value = false)
      }

      frameCount++
      requestAnimationFrame(detectLoop)
    }

    requestAnimationFrame(detectLoop)
  }

  const renderStyle = () => {
    if (isFullStep) {
      return {
        checking: step.value > 0,
        fullStep: step.value > 0,
      }
    }
    return {
      checking: step.value > 0,
      step1: step.value > 0,
      step2: step.value > 1,
      step3: step.value > 2,
      step4: step.value > 3,
    }
  }

  return {
    init,
    loadModels,
    startVideo,
    stopVideo,
    detectFace,
    renderStyle,
    dataImage,
    step,
    textStep,
    isLoading,
  }
})

export default useFaceIDStore
