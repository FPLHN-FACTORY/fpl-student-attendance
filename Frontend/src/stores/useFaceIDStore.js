import { Modal } from 'ant-design-vue'
import { defineStore } from 'pinia'
import { ref } from 'vue'
import * as faceapi from 'face-api.js'

const DEEP_CHECK = true
const TIME_LOOP_RECHECK = 400
const THRESHOLDS_IDENTIFY = 8

const useFaceIDStore = defineStore('faceID', () => {
  let isFullStep = false
  let video = null
  let canvas = null
  let onSuccess = null

  const isRunScan = ref(false)
  const isLoading = ref(true)

  const step = ref(0)
  const textStep = ref('Vui lòng đợi camera...')
  const dataImage = ref(null)

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

  const init = (v, c, fullStep, callback) => {
    isFullStep = fullStep
    video = v
    canvas = c
    onSuccess = callback
  }

  const startVideo = async () => {
    step.value = 0
    dataImage.value = null
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

      if (noseX < faceCenterX - THRESHOLDS_IDENTIFY) {
        return -1
      } else if (noseX > faceCenterX + THRESHOLDS_IDENTIFY) {
        return 1
      }
      return Math.abs(eyeAngle) < 5 ? 0 : -2
    }

    const captureFace = () => {
      const context = canvas.value.getContext('2d')
      canvas.value.width = video.value.videoWidth
      canvas.value.height = video.value.videoHeight

      context.drawImage(video.value, 0, 0, canvas.value.width, canvas.value.height)
      dataImage.value = canvas.value.toDataURL('image/png')
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
          if (isFullStep) {
            if (step.value === 0 && angle === 0) {
              step.value = 1
              captureFace()
              stopVideo()
              typeof onSuccess == 'function' && onSuccess(descriptor)
              isRunScan.value = false
            } else {
              faceDescriptor = null
            }
          } else {
            if (step.value === 0 && angle === 0) {
              step.value = 1
            } else if (step.value === 1 && angle === -1) {
              step.value = 2
            } else if (step.value === 2 && angle === 1) {
              step.value = 3
            } else if (step.value === 3 && angle === 0) {
              step.value = 4
              captureFace()
              stopVideo()
              typeof onSuccess == 'function' && onSuccess(descriptor)
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
