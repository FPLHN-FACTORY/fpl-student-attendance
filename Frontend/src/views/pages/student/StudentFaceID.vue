<script setup>
import { onMounted } from 'vue'
import useFaceIDStore from '@/stores/useFaceIDStore'
import { useRoute } from 'vue-router'
import WebcamFaceID from '@/components/faceid/WebcamFaceID.vue'

const faceIDStore = useFaceIDStore()
const route = useRoute()

const isFullStep = route.params?.fullstep == 'true'

const handleSubmit = (descriptors) => {
  console.log(faceIDStore.dataImage)
  window.ReactNativeWebView.postMessage(
    JSON.stringify({
      descriptors,
      image: faceIDStore.dataImage,
    }),
  )
}

onMounted(() => {
  faceIDStore.setFullStep(isFullStep)
  faceIDStore.setShowError(false)
  faceIDStore.setCallback((descriptor) => {
    handleSubmit(descriptor)
  })
  faceIDStore.startVideo()
})
</script>

<template>
  <WebcamFaceID />
</template>
