import { Modal } from 'ant-design-vue'
import { defineStore } from 'pinia'
import { ref, toRaw } from 'vue'
import * as tf from '@tensorflow/tfjs'
import Human from '@vladmandic/human'
import Config from '@/constants/humanConfig'

const THRESHOLD_P = 0.2
const THRESHOLD_X = 0.1
const THRESHOLD_Y = 0.2
const THRESHOLD_EMOTIONS = 0.8
const MIN_BRIGHTNESS = 80
const MAX_BRIGHTNESS = 180
const THRESHOLD_LIGHT = 80
const TIME_GET_BEST_EMBEDDING = 5
const SKIP_FRAME = 10

const useFaceIDStore = defineStore('faceID', () => {
  let isFullStep = false
  let video = null
  let canvas = null
  let axis = null
  let onSuccess = null

  const human = new Human(Config)

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

  const init = (v, c, a, fullStep, callback) => {
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
    const models = [human.load(), human.warmup()]

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
      const { bearing, strength } = face.rotation?.gaze || {}

      const yawDeg = toDegrees(yaw)
      const pitchDeg = toDegrees(pitch)

      updateAxis(pitchDeg, yawDeg)

      if (
        Math.abs(yaw) < THRESHOLD_X &&
        Math.abs(pitch) < THRESHOLD_P &&
        Math.abs(bearing) < 0.5 &&
        strength < 0.4
      ) {
        return 0
      }

      if (yaw < -THRESHOLD_Y) {
        return 1
      }

      if (yaw > THRESHOLD_Y) {
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

    const checkOverlap = (boxA, boxB) => {
      const [xA, yA, wA, hA] = boxA
      const [xB, yB, wB, hB] = boxB

      return !(xA + wA < xB || xA > xB + wB || yA + hA < yB || yA > yB + hB)
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
      if (lstDescriptor.length > 0) {
        lstDescriptor.value = []
      }
    }

    const pushDescriptor = async () => {
      const descriptor = normalize(Array.from(await getBestEmbedding()))

      const rollback = () => {
        lstDescriptor.value.pop()
        step.value = Math.max(0, step.value - 1)
        renderTextStep()
      }

      if (!descriptor.length) {
        return rollback()
      }

      if (
        (!isFullStep && step.value === 4 && lstDescriptor.value.length < 1) ||
        (isFullStep && step.value === 2 && lstDescriptor.value.length < 1)
      ) {
        return (step.value = 0)
      }

      lstDescriptor.value.push(descriptor)
      if (lstDescriptor.value.length === 2) {
        const a = cosineSimilarity(lstDescriptor.value[0], lstDescriptor.value[1])
        if (a < 0.95) {
          return rollback()
        }
        captureFace()
        stopVideo()
        typeof onSuccess == 'function' && onSuccess(toRaw(lstDescriptor.value))
        isRunScan.value = false
      }
    }

    const averageEmbedding = (vectors) => {
      if (!vectors.length) {
        return []
      }
      const length = vectors[0].length
      const avg = new Array(length).fill(0)

      for (const vec of vectors) {
        for (let i = 0; i < length; i++) {
          avg[i] += vec[i]
        }
      }

      for (let i = 0; i < length; i++) {
        avg[i] /= vectors.length
      }

      return avg
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
      let embeddings = []
      let prevEmbedding = null

      for (let i = 0; i < TIME_GET_BEST_EMBEDDING; i++) {
        const result = await human.detect(video.value)
        const face = result.face?.[0]

        if (face?.embedding) {
          const currentEmbedding = Array.from(face.embedding)

          if (prevEmbedding) {
            const similarity = cosineSimilarity(currentEmbedding, prevEmbedding)

            if (similarity > 0.9) {
              embeddings.push(currentEmbedding)
            }
          } else {
            embeddings.push(currentEmbedding)
          }

          prevEmbedding = currentEmbedding
        }

        await delay(50)
      }

      if (embeddings.length !== TIME_GET_BEST_EMBEDDING) {
        return []
      }

      return averageEmbedding(embeddings)
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
        return renderTextStep('Vui lòng nhìn vào camera')
      }

      canvas.value.width = video.value.videoWidth
      canvas.value.height = video.value.videoHeight
      ctx.drawImage(video.value, 0, 0, canvas.value.width, canvas.value.height)

      error = 0
      const detection = detections.face?.[0]
      const emotions = detection.emotion || []
      const faceBox = detection.box
      const faceBoxRaw = detection.boxRaw

      if (detection?.tensor) {
        human.tf.dispose(detection?.tensor)
      }

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

      if (!insideCenter) {
        step.value = Math.max(0, step.value - 1)
        return renderTextStep()
      }

      const angle = await getFaceAngle(detection)

      if (!faceDescriptor) {
        faceDescriptor = true
        if (step.value >= 0) {
          step.value = 0
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
          step.value = Math.max(0, step.value - 1)
          return renderTextStep('Vui lòng không nhắm mắt hoặc đeo kính')
        }

        if (await isWithMask()) {
          step.value = Math.max(0, step.value - 1)
          return renderTextStep('Vui lòng không đeo khẩu trang')
        }
      }

      if (detections?.object) {
        for (const obj of detections?.object) {
          if (['hand', 'cell phone', 'book'].includes(obj.label)) {
            if (checkOverlap(obj.box, face.box)) {
              step.value = Math.max(0, step.value - 1)
              return renderTextStep('Vui lòng không che khuất mặt')
            }
          }
        }
      }

      if (faceDescriptor) {
        renderTextStep()

        if (isFullStep) {
          if (step.value === 0 && angle === 0) {
            step.value = 1
            clearDescriptor()
            return await pushDescriptor()
          }
          if (step.value === 1) {
            step.value = 2
            return await pushDescriptor()
          }
        } else {
          if (step.value === 0 && angle === 0) {
            step.value = 1
            clearDescriptor()
            return await pushDescriptor()
          }
          if (step.value === 1 && angle === -1) {
            return (step.value = 2)
          }
          if (step.value === 2 && angle === 1) {
            return (step.value = 3)
          }
          if (step.value === 3 && angle === 0) {
            step.value = 4
            return await pushDescriptor()
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
