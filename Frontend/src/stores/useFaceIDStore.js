import { Modal } from 'ant-design-vue'
import { defineStore } from 'pinia'
import { ref } from 'vue'
import * as faceapi from 'face-api.js'

const DEEP_CHECK = true
const TIME_LOOP_RECHECK = 400
const THRESHOLD_X = 5
const THRESHOLD_Y = 25
const MIN_BRIGHTNESS = 100
const MAX_BRIGHTNESS = 180
const THRESHOLD_LIGHT = 80
const REGION_WIDTH_LIGHT = 10

const useFaceIDStore = defineStore('faceID', () => {
  let isFullStep = false
  let video = null
  let canvas = null
  let axis = null
  let onSuccess = null

  const isRunScan = ref(false)
  const isLoading = ref(true)

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
        case -1:
          return (textStep.value = 'Camera quá tối, vui lòng tăng độ sáng')
        case -2:
          return (textStep.value = 'Camera quá sáng, vui lòng giảm độ sáng')
        case -3:
          return (textStep.value = 'Ánh sáng không đều. Vui lòng thử lại')
        case -4:
          return (textStep.value = 'Không được biểu cảm. Vui lòng thử lại')
        case 0:
          return (textStep.value = 'Vui lòng nhìn thẳng')
        case 1:
          return (textStep.value = 'Xác minh hoàn tất')
        default:
          return (textStep.value = 'Vui lòng nhìn vào camera')
      }
    }

    switch (step.value) {
      case -1:
        return (textStep.value = 'Camera quá tối, vui lòng tăng độ sáng')
      case -2:
        return (textStep.value = 'Camera quá sáng, vui lòng giảm độ sáng')
      case -3:
        return (textStep.value = 'Ánh sáng không đều. Vui lòng thử lại')
      case -4:
        return (textStep.value = 'Không được biểu cảm. Vui lòng thử lại')
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

  const init = (v, c, a, fullStep, callback) => {
    isFullStep = fullStep
    video = v
    canvas = c
    axis = a
    onSuccess = callback
  }

  const startVideo = async () => {
    step.value = 0
    dataImage.value = null
    lstDescriptor.value = []
    isRunScan.value = true
    isLoading.value = true
    try {
      const constraints = {
        video: { facingMode: 'user' },
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

  const loadModels = async () => {
    DEEP_CHECK && (await faceapi.nets.ssdMobilenetv1.loadFromUri('/models'))
    !DEEP_CHECK && (await faceapi.nets.tinyFaceDetector.loadFromUri('/models'))
    await faceapi.nets.faceLandmark68Net.loadFromUri('/models')
    await faceapi.nets.faceRecognitionNet.loadFromUri('/models')
    await faceapi.nets.faceExpressionNet.loadFromUri('/models')
  }

  const detectFace = async () => {
    const options = DEEP_CHECK
      ? new faceapi.SsdMobilenetv1Options({ minConfidence: 0.5 })
      : new faceapi.TinyFaceDetectorOptions()
    const displaySize = { width: video.value.videoWidth, height: video.value.videoHeight }

    faceapi.matchDimensions(canvas.value, displaySize)

    const aX = axis.value.querySelector('.a-x > div')
    const aY = axis.value.querySelector('.a-y > div')

    let faceDescriptor = null

    const getAverageBrightness = async (faceBox) => {
      const cvs = document.createElement('canvas')
      const context = cvs.getContext('2d')
      cvs.width = faceBox.width
      cvs.height = faceBox.height

      context.drawImage(
        video.value,
        faceBox.x,
        faceBox.y,
        cvs.width,
        cvs.height,
        0,
        0,
        faceBox.width,
        faceBox.height,
      )
      const frame = context.getImageData(0, 0, cvs.width, cvs.height)
      const data = frame.data

      let totalLuminance = 0

      for (let i = 0; i < data.length; i += 4) {
        const r = data[i]
        const g = data[i + 1]
        const b = data[i + 2]
        const brightness = 0.299 * r + 0.587 * g + 0.114 * b
        totalLuminance += brightness
      }

      const averageBrightness = totalLuminance / (data.length / 4)
      return averageBrightness
    }

    const isLightBalance = async (landmarks, faceBox) => {
      const cvs = document.createElement('canvas')
      const context = cvs.getContext('2d')
      cvs.width = faceBox.width
      cvs.height = faceBox.height

      context.drawImage(
        video.value,
        faceBox.x,
        faceBox.y,
        cvs.width,
        cvs.height,
        0,
        0,
        faceBox.width,
        faceBox.height,
      )

      const frame = context.getImageData(0, 0, cvs.width, cvs.height)
      const frameData = frame.data

      const positions = landmarks.positions
      const leftEyebrow = positions.slice(17, 22)
      const rightEyebrow = positions.slice(22, 27)
      const leftCheek = [positions[2], positions[3], positions[4]]
      const rightCheek = [positions[12], positions[13], positions[14]]

      const leftSidePoints = [...landmarks.getLeftEye(), ...leftEyebrow, ...leftCheek]
      const rightSidePoints = [...landmarks.getRightEye(), ...rightEyebrow, ...rightCheek]

      const getRegionBrightness = (sidePoints) => {
        let totalLuminance = 0
        let count = 0

        sidePoints.forEach((point) => {
          const x = Math.round(point.x - faceBox.x)
          const y = Math.round(point.y - faceBox.y)

          const regionWidth = REGION_WIDTH_LIGHT

          for (let i = -regionWidth; i < regionWidth; i++) {
            for (let j = -regionWidth; j < regionWidth; j++) {
              const px = x + i
              const py = y + j

              if (px < 0 || px >= frame.width || py < 0 || py >= frame.height) continue

              const pixelIndex = (py * frame.width + px) * 4

              const r = frameData[pixelIndex]
              const g = frameData[pixelIndex + 1]
              const b = frameData[pixelIndex + 2]

              const brightness = 0.299 * r + 0.587 * g + 0.114 * b
              totalLuminance += brightness
              count++
            }
          }
        })

        return totalLuminance / count
      }

      const leftBrightness = getRegionBrightness(leftSidePoints)
      const rightBrightness = getRegionBrightness(rightSidePoints)

      const brightnessDifference = Math.abs(leftBrightness - rightBrightness)

      if (brightnessDifference > THRESHOLD_LIGHT) {
        return false
      }

      return true
    }

    let BASELINE_VERTICAL_OFFSET = null
    const getFaceAngle = async (landmarks) => {
      const leftEye = landmarks.getLeftEye()
      const rightEye = landmarks.getRightEye()
      const nose = landmarks.getNose()

      const deltaX = rightEye[0].x - leftEye[0].x
      const deltaY = rightEye[0].y - leftEye[0].y
      const eyeAngle = Math.atan2(deltaY, deltaX) * (180 / Math.PI)

      const noseX = nose[3].x
      const noseY = nose[3].y
      const eyeCenterX = (leftEye[0].x + rightEye[3].x) / 2
      const eyeCenterY = (leftEye[0].y + rightEye[3].y) / 2

      const noseOffset = noseX - eyeCenterX
      const rawVerticalOffset = noseY - eyeCenterY

      if (BASELINE_VERTICAL_OFFSET === null) {
        BASELINE_VERTICAL_OFFSET = rawVerticalOffset
      }

      const eyeDistance = Math.hypot(rightEye[3].x - leftEye[0].x, rightEye[3].y - leftEye[0].y)
      const offsetFromCenter = rawVerticalOffset - BASELINE_VERTICAL_OFFSET
      const verticalOffset = (offsetFromCenter / eyeDistance) * 50

      updateAxis(verticalOffset, noseOffset)

      if (
        Math.abs(eyeAngle) < THRESHOLD_X &&
        Math.abs(noseOffset) < THRESHOLD_Y &&
        Math.abs(verticalOffset) < THRESHOLD_X
      ) {
        return 0
      }

      if (noseOffset < -THRESHOLD_Y) {
        return -1
      } else if (noseOffset > THRESHOLD_Y) {
        return 1
      }

      return -2
    }

    const checkObstruction = (landmarks, video, faceBox) => {
      const cvs = document.createElement('canvas')
      const ctx = cvs.getContext('2d')
      cvs.width = faceBox.width
      cvs.height = faceBox.height

      ctx.drawImage(
        video,
        faceBox.x,
        faceBox.y,
        faceBox.width,
        faceBox.height,
        0,
        0,
        faceBox.width,
        faceBox.height,
      )

      const getStdDev = (points) => {
        const luminances = []

        points.forEach((point) => {
          const x = Math.round(point.x - faceBox.x)
          const y = Math.round(point.y - faceBox.y)

          for (let i = -3; i <= 3; i++) {
            for (let j = -3; j <= 3; j++) {
              const px = x + i
              const py = y + j
              if (px < 0 || px >= cvs.width || py < 0 || py >= cvs.height) continue

              const [r, g, b] = ctx.getImageData(px, py, 1, 1).data
              const lum = 0.299 * r + 0.587 * g + 0.114 * b
              luminances.push(lum)
            }
          }
        })

        const mean = luminances.reduce((a, b) => a + b, 0) / luminances.length
        const variance =
          luminances.reduce((sum, val) => sum + (val - mean) ** 2, 0) / luminances.length
        return Math.sqrt(variance)
      }

      const mouth = landmarks.getMouth()
      const nose = landmarks.getNose()
      const leftEye = landmarks.getLeftEye()
      const rightEye = landmarks.getRightEye()
      const cheeks = [
        landmarks.positions[2],
        landmarks.positions[3],
        landmarks.positions[13],
        landmarks.positions[14],
      ]

      const stdMouth = getStdDev(mouth)
      const stdNose = getStdDev(nose)
      const stdEyeL = getStdDev(leftEye)
      const stdEyeR = getStdDev(rightEye)
      const stdCheek = getStdDev(cheeks)

      const isObstructed =
        stdMouth < stdCheek * 0.6 ||
        stdNose < stdCheek * 0.6 ||
        stdEyeL < stdCheek * 0.6 ||
        stdEyeR < stdCheek * 0.6

      return isObstructed
    }

    const captureFace = () => {
      const context = canvas.value.getContext('2d')
      canvas.value.width = video.value.videoWidth
      canvas.value.height = video.value.videoHeight

      context.drawImage(video.value, 0, 0, canvas.value.width, canvas.value.height)
      dataImage.value = canvas.value.toDataURL('image/png')
    }

    const updateAxis = (x, y) => {
      axis.value.classList.add('active')
      aX.style.transform = `rotateY(90deg)`
      aY.style.transform = `rotateX(90deg)`

      aX.style.transform = `rotateY(${90 + x}deg)`
      aY.style.transform = `rotateX(${90 + y}deg)`
    }

    const runTask = async () => {
      const detections = await faceapi
        .detectAllFaces(video.value, options)
        .withFaceLandmarks()
        .withFaceDescriptors()
        .withFaceExpressions()

      if (!aX.length || !aY.length || step.value < 0) {
        if (axis.value) {
          axis.value.classList.remove('active')
        }
      }

      if (detections.length === 1) {
        const detection = detections[0]
        const landmarks = detection.landmarks
        const descriptor = detection.descriptor
        const expressions = detection.expressions

        const angle = await getFaceAngle(landmarks)

        if (!faceDescriptor) {
          faceDescriptor = descriptor
          if (step.value >= 0) {
            step.value = 0
          }
          return renderTextStep()
        }

        const faceBox = detections[0].detection.box

        const avgBrightness = await getAverageBrightness(faceBox)

        if (avgBrightness < MIN_BRIGHTNESS) {
          step.value = -1
          faceDescriptor = null
          return renderTextStep()
        }

        if (avgBrightness > MAX_BRIGHTNESS) {
          step.value = -2
          faceDescriptor = null
          return renderTextStep()
        }

        if (!(await isLightBalance(landmarks, faceBox)) && angle === 0) {
          step.value = -3
          faceDescriptor = null
          return renderTextStep()
        }

        if (
          expressions.happy > 0.7 ||
          expressions.surprised > 0.7 ||
          expressions.sad > 0.7 ||
          expressions.surprised > 0.7 ||
          expressions.disgusted > 0.7 ||
          expressions.fearful > 0.7
        ) {
          step.value = -4
          faceDescriptor = null
          return renderTextStep()
        }

        // if (checkObstruction(landmarks, video.value, faceBox)) {
        //   step.value = -5
        //   faceDescriptor = null
        //   return renderTextStep()
        // }

        if (step.value < 0) {
          step.value = 0
          renderTextStep()
        }

        if (faceDescriptor) {
          const distance = faceapi.euclideanDistance(faceDescriptor, descriptor)
          if (distance > 0.5) {
            return
          }

          renderTextStep()

          if (isFullStep) {
            if (step.value === 0 && angle === 0) {
              step.value = 1
              lstDescriptor.value.push(Array.from(descriptor))
            } else if (step.value === 1) {
              step.value = 2
              lstDescriptor.value.push(Array.from(descriptor))
              captureFace()
              stopVideo()
              typeof onSuccess == 'function' && onSuccess(lstDescriptor.value)
              isRunScan.value = false
            } else {
              faceDescriptor = null
            }
          } else {
            if (step.value === 0 && angle === 0) {
              step.value = 1
              lstDescriptor.value.push(Array.from(descriptor))
            } else if (step.value === 1 && angle === -1) {
              step.value = 2
              lstDescriptor.value.push(Array.from(descriptor))
            } else if (step.value === 2 && angle === 1) {
              step.value = 3
              lstDescriptor.value.push(Array.from(descriptor))
            } else if (step.value === 3 && angle === 0) {
              step.value = 4
              lstDescriptor.value.push(Array.from(descriptor))
              captureFace()
              stopVideo()
              typeof onSuccess == 'function' && onSuccess(lstDescriptor.value)
              isRunScan.value = false
            } else {
              // faceDescriptor = null
            }
          }
        }
      } else if (detections.length > 1) {
        renderTextStep('Có quá nhiều khuôn mặt!')
      } else {
        renderTextStep('Vui lòng nhìn vào camera')
        step.value = Math.max(0, step.value - 1)
        step.value === 0 && (faceDescriptor = null)
      }
    }

    while (isRunScan.value) {
      const model = DEEP_CHECK
        ? faceapi.nets.ssdMobilenetv1.isLoaded
        : faceapi.nets.tinyFaceDetector.isLoaded
      if (
        model &&
        faceapi.nets.faceLandmark68Net.isLoaded &&
        faceapi.nets.faceRecognitionNet.isLoaded &&
        faceapi.nets.faceExpressionNet.isLoaded &&
        video.value?.readyState === 4
      ) {
        await runTask()
        isLoading.value && (isLoading.value = false)
      }
      await new Promise((resolve) => setTimeout(resolve, TIME_LOOP_RECHECK))
    }
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
