import { message, Modal } from 'ant-design-vue'
import { defineStore } from 'pinia'
import { ref, toRaw } from 'vue'
import * as tf from '@tensorflow/tfjs'
import Human from '@vladmandic/human'
import Config from '@/constants/humanConfig'
import { isProbablyMobile } from '@/utils/utils'
import * as ort from 'onnxruntime-web'

const isMobile = isProbablyMobile()

const THRESHOLD_P = 0.2
const THRESHOLD_X = 0.05
const THRESHOLD_Y = 0.12
const THRESHOLD_R = 0.07
const THRESHOLD_EMOTIONS = 0.8
const MIN_BRIGHTNESS = isMobile ? 30 : 35
const MAX_BRIGHTNESS = isMobile ? 90 : 100
const THRESHOLD_LIGHT = 40
const SIZE_CAMERA = 480
const CHECK_DISTANCE = true
const DELAY_SUCCESS = 5000

const useFaceIDStore = defineStore('faceID', () => {
  let isFullStep = false
  let video = null
  let canvas = null
  let axis = null
  let onSuccess = null
  let isShowError = true

  let human = null

  let isAllowGlasses = false
  let isAllowMask = false
  let isAllowReaction = false

  const isRunScan = ref(false)
  const isLoading = ref(true)
  const isLoadingModels = ref(false)
  const isReady = ref(false)
  const isSuccess = ref(false)
  const isShowLookAhead = ref(false)
  const isShowActionTurnLeft = ref(false)
  const isShowActionTurnRight = ref(false)

  const step = ref(0)
  const textStep = ref('Vui lòng đợi camera...')
  const dataImage = ref(null)
  const dataCanvas = ref(null)
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

  const setAllowGlasses = (type) => {
    isAllowGlasses = type
  }

  const setAllowMask = (type) => {
    isAllowMask = type
  }

  const setAllowReaction = (type) => {
    isAllowReaction = type
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
  let livenessModel
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

    if (!isAllowMask) {
      const loadMaskModel = async () => {
        try {
          maskModel = await tf.loadLayersModel('indexeddb://maskes-model')
        } catch (err) {
          maskModel = await tf.loadLayersModel('/models/maskes/model.json')
          await maskModel.save('indexeddb://maskes-model')
        }
      }
      if (!maskModel) {
        models.push(loadMaskModel())
      }
    }

    if (!isAllowGlasses) {
      const loadGlassModel = async () => {
        try {
          glassesModel = await tf.loadLayersModel('indexeddb://glasses-model')
        } catch (err) {
          glassesModel = await tf.loadLayersModel('/models/glasses/model.json')
          await glassesModel.save('indexeddb://glasses-model')
        }
      }

      if (!glassesModel) {
        models.push(loadGlassModel())
      }
    }

    ort.env.wasm.wasmPaths = {
      'ort-wasm-simd.wasm': '/models/wasm/ort-wasm-simd.wasm',
      'ort-wasm-simd-threaded.wasm': '/models/wasm/ort-wasm-simd-threaded.wasm',
    }
    const loadLivenessModel = async () => {
      await ort.InferenceSession.create('/models/liveness.onnx', {
        executionProviders: ['wasm'],
      }).then((session) => {
        livenessModel = session
        const input_tensor = new ort.Tensor(
          'float32',
          new Float32Array(128 * 128 * 3),
          [1, 3, 128, 128],
        )
        for (let i = 0; i < 128 * 128 * 3; i++) {
          input_tensor.data[i] = Math.random() * 2.0 - 1.0
        }
        const feeds = { input: input_tensor }
        livenessModel.run(feeds)
      })
    }

    models.push(loadLivenessModel())

    models.push(human.load())
    models.push(human.warmup())

    await Promise.all(models)
    isLoadingModels.value = true
  }

  const detectFace = async () => {
    let faceDescriptor = null

    const ctx = canvas.value.getContext('2d', { willReadFrequently: true })

    const aX = axis.value.querySelectorAll('.a-x > div')
    const aY = axis.value.querySelectorAll('.a-y > div')

    const drawRegion = (ctx, points) => {
      ctx.beginPath()
      ctx.moveTo(points[0][0], points[0][1])
      for (let i = 1; i < points.length; i++) ctx.lineTo(points[i][0], points[i][1])
      ctx.closePath()
      ctx.fill()
    }

    const expandRegion = (points, factor = 1.2) => {
      const cx = points.reduce((s, p) => s + p[0], 0) / points.length
      const cy = points.reduce((s, p) => s + p[1], 0) / points.length
      return points.map(([x, y]) => [cx + (x - cx) * factor, cy + (y - cy) * factor])
    }

    const drawOvalMask = (ctx, points) => {
      const xs = points.map((p) => p[0])
      const ys = points.map((p) => p[1])
      const cx = (Math.min(...xs) + Math.max(...xs)) / 2
      const cy = (Math.min(...ys) + Math.max(...ys)) / 2
      let rx = (Math.max(...xs) - Math.min(...xs)) / 2
      let ry = (Math.max(...ys) - Math.min(...ys)) / 2

      const scaleX = 0.85
      rx *= scaleX

      ctx.beginPath()
      ctx.ellipse(cx, cy, rx, ry, 0, 0, 2 * Math.PI)
      ctx.fill()
    }

    const computeSkinBrightness = async (canvas) => {
      const res = await human.detect(canvas)
      if (!res.face.length) return null
      const ann = res.face[0].annotations

      const width = canvas.width,
        height = canvas.height

      const maskCanvas = document.createElement('canvas')
      maskCanvas.width = width
      maskCanvas.height = height
      const mctx = maskCanvas.getContext('2d')

      mctx.fillStyle = 'white'
      drawOvalMask(mctx, ann.silhouette)

      mctx.globalCompositeOperation = 'destination-out'
      drawRegion(mctx, expandRegion([...ann.leftEyeUpper0, ...ann.leftEyeLower0]))
      drawRegion(mctx, expandRegion([...ann.rightEyeUpper0, ...ann.rightEyeLower0]))
      drawRegion(mctx, expandRegion([...ann.lipsUpperOuter, ...ann.lipsLowerOuter]))
      drawRegion(mctx, expandRegion(ann.leftEyebrowUpper, 1.3))
      drawRegion(mctx, expandRegion(ann.rightEyebrowUpper, 1.3))
      mctx.globalCompositeOperation = 'source-over'

      const octx = canvas.getContext('2d')
      const src = octx.getImageData(0, 0, width, height).data
      const msk = mctx.getImageData(0, 0, width, height).data

      const outCanvas = document.createElement('canvas')
      outCanvas.width = width
      outCanvas.height = height
      const outCtx = outCanvas.getContext('2d')
      const out = outCtx.createImageData(width, height)

      for (let i = 0; i < src.length; i += 4) {
        if (msk[i + 3] > 0) {
          out.data[i] = src[i]
          out.data[i + 1] = src[i + 1]
          out.data[i + 2] = src[i + 2]
          out.data[i + 3] = 255
        } else out.data[i + 3] = 0
      }
      outCtx.putImageData(out, 0, 0)
      return out
    }

    const getHalfImageData = async () => {
      const image = await computeSkinBrightness(canvas.value)
      if (!image) {
        return {
          brightnessLeft: 0,
          brightnessRight: 0,
        }
      }
      const width = image.width
      const height = image.height
      const halfWidth = Math.floor(width / 2)

      const tmpCanvas = document.createElement('canvas')
      tmpCanvas.width = width
      tmpCanvas.height = height
      const tmpCtx = tmpCanvas.getContext('2d')
      tmpCtx.putImageData(image, 0, 0)

      const leftImageData = tmpCtx.getImageData(0, 0, halfWidth, height)
      const rightImageData = tmpCtx.getImageData(halfWidth, 0, width - halfWidth, height)

      const brightnessLeft = await getAverageBrightness(leftImageData)
      const brightnessRight = await getAverageBrightness(rightImageData)

      return {
        brightnessLeft,
        brightnessRight,
      }
    }

    const getAverageBrightness = async (image) => {
      const data = image.data
      let sum = 0

      for (let i = 0; i < data.length; i += 4) {
        const avg = (data[i] + data[i + 1] + data[i + 2]) / 3
        sum += avg
      }

      return sum / (image.width * image.height)
    }

    const isLightBalance = async ({ brightnessLeft, brightnessRight }) => {
      const brightnessDiff = Math.abs(brightnessLeft - brightnessRight)
      return brightnessDiff <= THRESHOLD_LIGHT
    }

    const isLightTooDark = async ({ brightnessLeft, brightnessRight }) => {
      return brightnessLeft < MIN_BRIGHTNESS || brightnessRight < MIN_BRIGHTNESS
    }

    const isLightTooBright = async ({ brightnessLeft, brightnessRight }) => {
      return brightnessLeft > MAX_BRIGHTNESS || brightnessRight > MAX_BRIGHTNESS
    }

    const getFaceAngle = async () => {
      const face = human.result?.face?.[0]
      if (!face) {
        return
      }

      const { yaw, pitch, roll } = face.rotation?.angle || {}

      const yawDeg = toDegrees(yaw)
      const pitchDeg = toDegrees(pitch)

      updateAxis(pitchDeg, yawDeg)

      if (
        Math.abs(yaw) < THRESHOLD_X &&
        Math.abs(pitch) < THRESHOLD_P &&
        Math.abs(roll) < THRESHOLD_R &&
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
      if (isAllowMask) {
        return false
      }
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
      if (isAllowGlasses) {
        return false
      }
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

    const rollback = (text) => {
      if (step.value !== 3) {
        lstDescriptor.value.pop()
        step.value = Math.max(0, step.value - 1)
      }
      renderTextStep(text)
    }

    const pushDescriptor = async () => {
      count.value = 0
      const descriptor = normalize(Array.from(embedding.value))
      if (!descriptor.length) {
        return rollback()
      }

      lstDescriptor.value.push(descriptor)
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

      if (similarity < 0.6) {
        return rollback('Vui lòng xác nhận lại')
      }

      captureFace()
      isSuccess.value = true
      renderTextStep('Xác minh hoàn tất')
      try {
        typeof onSuccess == 'function' && onSuccess(toRaw(lstDescriptor.value))
      } catch (error) {
        console.error(error)
      }

      await delay(DELAY_SUCCESS)
      lstDescriptor.value = []
      faceDescriptor = null
      step.value = 0
      isSuccess.value = false
      renderTextStep()
    }

    const delay = (ms) => new Promise((res) => setTimeout(res, ms))

    const isReaction = async () => {
      if (isAllowReaction) {
        return false
      }
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
        (await isInvalidSize(face))
      ) {
        return []
      }

      const halfImageData = await getHalfImageData()
      if (
        !(await isLightBalance(halfImageData)) ||
        (await isLightTooBright(halfImageData)) ||
        (await isLightTooDark(halfImageData)) ||
        (await isWithGlasses()) ||
        (await isWithMask()) ||
        (await isReaction())
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
          Math.abs(pitch) > THRESHOLD_P) ||
        Math.abs(roll) > THRESHOLD_R ||
        Math.abs(yaw) > THRESHOLD_X
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

        const cvs = document.createElement('canvas')
        cvs.width = video.value.videoWidth
        cvs.height = video.value.videoHeight
        const ctx = cvs.getContext('2d')
        ctx.drawImage(video.value, 0, 0, cvs.width, cvs.height)
        dataCanvas.value = cvs.toDataURL('image/png')
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

      const min = 0.5
      const max = 0.7

      if (ratio < min) {
        return 'Vui lòng đưa mặt lại gần hơn'
      }

      if (ratio > max) {
        return 'Vui lòng đưa mặt ra xa hơn'
      }

      return null
    }

    const preprocessOnnx = (imageData) => {
      const { width, height, data } = imageData
      const channels = 3
      const preprocessed = new Float32Array(1 * channels * height * width)

      for (let y = 0; y < height; y++) {
        for (let x = 0; x < width; x++) {
          const idx = (y * width + x) * 4
          preprocessed[y * width + x] = data[idx]
          preprocessed[height * width + y * width + x] = data[idx + 1]
          preprocessed[2 * height * width + y * width + x] = data[idx + 2]
        }
      }

      return {
        data: preprocessed,
        shape: [1, 3, height, width],
      }
    }

    const softmax = (arr) => {
      return arr.map(function (value, index) {
        return (
          Math.exp(value) /
          arr
            .map(function (y) {
              return Math.exp(y)
            })
            .reduce(function (a, b) {
              return a + b
            })
        )
      })
    }

    const createFeeds = async (size) => {
      const tmpCanvas = document.createElement('canvas')
      tmpCanvas.width = size
      tmpCanvas.height = size
      const tmpCtx = tmpCanvas.getContext('2d')
      tmpCtx.drawImage(canvas.value, 0, 0, size, size)
      const faceImageData = tmpCtx.getImageData(0, 0, size, size)
      const input_image = preprocessOnnx(faceImageData)

      const input_tensor = new ort.Tensor('float32', new Float32Array(size * size * 3), [
        1,
        3,
        size,
        size,
      ])
      input_tensor.data.set(input_image.data)
      return { input: input_tensor }
    }

    const predictLiveness = async () => {
      const feeds = await createFeeds(128)
      const output_tensor = await livenessModel.run(feeds)
      return softmax(output_tensor['output'].data)
    }

    const isLiveness = async () => {
      const score_arr = await predictLiveness()
      const classes = ['fake', 'real', 'unknown']
      const maxIndex = score_arr.indexOf(Math.max(...score_arr))
      const predictedClass = classes[maxIndex]
      return predictedClass === 'real'
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
        isShowLookAhead.value = true
        return renderTextStep('Vui lòng nhìn vào camera')
      }
      error = 0
      const detection = detections.face?.[0]
      const faceBoxRaw = detection.boxRaw

      const angle = await getFaceAngle()
      if (!(await cropFace(detection))) {
        return
      }

      isShowLookAhead.value = step.value === 3 || step.value === 0

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

      if (!isFullStep && (step.value === 0 || step.value === 3)) {
        const halfImageData = await getHalfImageData()
        if (!(await isLightBalance(halfImageData))) {
          return renderTextStep('Ánh sáng không đều. Vui lòng thử lại')
        }
        if (await isLightTooDark(halfImageData)) {
          return renderTextStep('Camera quá tối, Vui lòng tăng độ sáng')
        }
        if (await isLightTooBright(halfImageData)) {
          return renderTextStep('Camera quá sáng, Vui lòng giảm độ sáng')
        }

        const { pitch, roll } = detection.rotation?.angle || {}

        if (
          human.result.gesture.some((o) => o.gesture.includes('head up')) &&
          Math.abs(pitch) > THRESHOLD_P
        ) {
          return renderTextStep('Vui lòng không ngẩng mặt')
        }

        if (
          human.result.gesture.some((o) => o.gesture.includes('head down')) &&
          Math.abs(pitch) > THRESHOLD_P
        ) {
          return renderTextStep('Vui lòng không cúi mặt')
        }

        if (Math.abs(roll) > THRESHOLD_R) {
          return renderTextStep('Vui lòng không nghiêng đầu')
        }
        if (await isReaction()) {
          return renderTextStep('Vui lòng không biểu cảm')
        }
      }

      if (await isWithGlasses()) {
        step.value = 0
        return renderTextStep('Vui lòng không nhắm mắt hoặc đeo kính')
      }

      if (await isWithMask()) {
        step.value = 0
        return renderTextStep('Vui lòng không đeo khẩu trang')
      }

      if (faceDescriptor) {
        isReady.value = true
        count.value = 0

        renderTextStep()

        if (step.value === 0) {
          isShowActionTurnRight.value = false
          isShowActionTurnLeft.value = false
        }

        if (isFullStep) {
          if (step.value === 0 && angle === 0) {
            clearDescriptor()
            prevEmbedding.value = null
            await getBestEmbedding(3, 1)
            if (!embedding.value || embedding.length < 1) {
              return
            }
            await pushDescriptor()
            step.value = 1
            return renderTextStep()
          }
          if (step.value === 1) {
            await delay(500)
            await getBestEmbedding(1, 0, () => {
              step.value = 0
              renderTextStep()
            })
            if (!embedding.value || embedding.length < 1) {
              return
            }
            await pushDescriptor()
            step.value = 2
            return await submit()
          }
        } else {
          const isReal = await isLiveness()
          if (step.value === 0 && angle === 0) {
            clearDescriptor()
            prevEmbedding.value = null
            await getBestEmbedding(3, 0)
            if (!embedding.value || embedding.length < 1) {
              return
            }
            await pushDescriptor()
            step.value = 1
            isShowActionTurnRight.value = true
            isShowActionTurnLeft.value = false
            return renderTextStep()
          }
          if (step.value === 1 && angle === -1 && isReal) {
            await delay(500)
            step.value = 2
            isShowActionTurnRight.value = false
            isShowActionTurnLeft.value = true
            return renderTextStep()
          }
          if (step.value === 2 && angle === 1 && isReal) {
            await delay(500)
            step.value = 3
            isShowActionTurnRight.value = false
            isShowActionTurnLeft.value = false
            renderTextStep()
            return await delay(2000)
          }
          if (step.value === 3 && angle === 0) {
            prevEmbedding.value = null
            await getBestEmbedding(3, 0)
            if (!embedding.value || embedding.length < 1) {
              return
            }
            await pushDescriptor()
            step.value = 4
            return await submit()
          }
        }
      }
    }

    let frameCount = 0
    let detectTimeoutId = null
    let detectAxiesTimeoutId = null
    const detectLoop = async () => {
      if (!isRunScan.value) return clearTimeout(detectTimeoutId)
      if (isLoadingModels.value && video.value?.readyState === 4 && !isSuccess.value) {
        await runTask()
        if (isLoading.value) {
          isLoading.value = false
        }
      }

      frameCount++
      detectTimeoutId = setTimeout(detectLoop, 200)
    }
    const detectAxies = async () => {
      if (!isRunScan.value) return clearTimeout(detectAxiesTimeoutId)

      if (isLoadingModels.value && video.value?.readyState === 4 && !isSuccess.value) {
        await getFaceAngle()
        if (isLoading.value) {
          isLoading.value = false
        }
      }
      detectAxiesTimeoutId = setTimeout(detectAxies, 30)
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

  const renderAction = () => {
    return {
      'turn-left': isShowActionTurnLeft.value && !isFullStep && !isSuccess.value && !count.value,
      'turn-right': isShowActionTurnRight.value && !isFullStep && !isSuccess.value && !count.value,
      'look-ahead': isShowLookAhead.value && !isSuccess.value && !count.value,
    }
  }

  return {
    init,
    setup,
    setFullStep,
    setCallback,
    setShowError,
    setAllowGlasses,
    setAllowMask,
    setAllowReaction,
    loadModels,
    startVideo,
    stopVideo,
    detectFace,
    renderStyle,
    renderAction,
    dataImage,
    dataCanvas,
    step,
    textStep,
    isLoading,
    isReady,
    isSuccess,
    count,
  }
})

export default useFaceIDStore
