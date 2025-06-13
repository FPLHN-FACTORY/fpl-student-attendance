import { Modal } from 'ant-design-vue'
import { defineStore } from 'pinia'
import { ref, toRaw } from 'vue'
import * as tf from '@tensorflow/tfjs'
import Human from '@vladmandic/human'
import Config from '@/constants/humanConfig'

const TIME_LOOP_RECHECK = 300
const THRESHOLD_P = 0.1
const THRESHOLD_X = 0.1
const THRESHOLD_Y = 0.25
const THRESHOLD_EMOTIONS = 0.8
const MIN_BRIGHTNESS = 80
const MAX_BRIGHTNESS = 180
const THRESHOLD_LIGHT = 80
const DETECTION_LIVE = 0.7
const SKIP_FRAME = 6

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
        video: { facingMode: 'user', width: { ideal: 224 }, height: { ideal: 224 } },
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

    const getFaceAngle = async ({ roll, yaw, pitch }) => {
      const yawDeg = toDegrees(yaw)
      const pitchDeg = toDegrees(pitch)

      updateAxis(pitchDeg, yawDeg)

      if (Math.abs(yaw) < THRESHOLD_X && Math.abs(pitch) < THRESHOLD_P) {
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

    const clearDescriptor = () => {
      if (lstDescriptor.length > 0) {
        lstDescriptor.value = []
      }
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

      error = 0
      const detection = detections.face?.[0]
      const descriptor = Array.from(detection.embedding)
      const emotions = detection.emotion || []
      const faceBox = detection.box
      const faceBoxRaw = detection.boxRaw

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

      if (detection.live < DETECTION_LIVE) {
        return
      }

      if (!insideCenter) {
        step.value = Math.max(0, step.value - 1)
        return renderTextStep()
      }

      const angle = await getFaceAngle(detection.rotation?.angle || {})

      if (!faceDescriptor) {
        faceDescriptor = descriptor
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

      if (!isFullStep) {
        if (await isWithGlasses()) {
          return renderTextStep('Vui lòng không đeo kính')
        }

        if (await isWithMask()) {
          return renderTextStep('Vui lòng không đeo khẩu trang')
        }
      }

      for (const obj of detections?.object) {
        if (['hand', 'cell phone', 'book'].includes(obj.label)) {
          if (checkOverlap(obj.box, face.box)) {
            return renderTextStep('Vui lòng không che khuất mặt')
          }
        }
      }

      if (
        emotions.some(
          (e) =>
            ['happy', 'surprised', 'disgusted'].includes(e.emotion) && e.score > THRESHOLD_EMOTIONS,
        )
      ) {
        return renderTextStep('Vui lòng không biểu cảm')
      }

      if (faceDescriptor) {
        const distance = human.match.similarity(faceDescriptor, descriptor)
        if (distance < 0.5) {
          return
        }

        renderTextStep()

        if (isFullStep) {
          if (step.value === 0 && angle === 0) {
            step.value = 1
            clearDescriptor()
            return lstDescriptor.value.push(descriptor)
          }
          if (step.value === 1) {
            step.value = 2
            lstDescriptor.value.push(descriptor)
            await new Promise((resolve) => setTimeout(resolve, TIME_LOOP_RECHECK))
            captureFace()
            stopVideo()
            typeof onSuccess == 'function' && onSuccess(toRaw(lstDescriptor.value))
            return (isRunScan.value = false)
          }
        } else {
          if (step.value === 0 && angle === 0) {
            step.value = 1
            clearDescriptor()
            return lstDescriptor.value.push(descriptor)
          }
          if (step.value === 1 && angle === -1) {
            step.value = 2
            return lstDescriptor.value.push(descriptor)
          }
          if (step.value === 2 && angle === 1) {
            step.value = 3
            return lstDescriptor.value.push(descriptor)
          }
          if (step.value === 3 && angle === 0) {
            step.value = 4
            lstDescriptor.value.push(descriptor)
            await new Promise((resolve) => setTimeout(resolve, TIME_LOOP_RECHECK))
            captureFace()
            stopVideo()
            typeof onSuccess == 'function' && onSuccess(toRaw(lstDescriptor.value))
            return (isRunScan.value = false)
          }
        }
      }
    }

    let frameCount = 0
    let isProgress = false
    const detectLoop = async () => {
      if (!isRunScan.value) return

      if (isLoadingModels.value && video.value?.readyState === 4 && frameCount % SKIP_FRAME === 0) {
        canvas.value.width = video.value.videoWidth
        canvas.value.height = video.value.videoHeight
        ctx.drawImage(video.value, 0, 0, canvas.value.width, canvas.value.height)
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
