<script setup>
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import useFaceIDStore from '@/stores/useFaceIDStore'
import { CheckOutlined } from '@ant-design/icons-vue'

const faceIDStore = useFaceIDStore()
const video = ref(null)
const canvas = ref(null)
const axis = ref(null)

const isShow = ref(false)

const hasAction = computed(() => {
  const actions = faceIDStore.renderAction()
  return Object.values(actions).some((v) => v)
})

let intervalId = null
let timeoutId = null

onMounted(async () => {
  faceIDStore.setup(video, canvas, axis)
  await faceIDStore.loadModels()

  watch(hasAction, (newVal) => {
    clearInterval(intervalId)
    clearTimeout(timeoutId)

    if (newVal) {
      const run = () => {
        isShow.value = true
        timeoutId = setTimeout(() => {
          isShow.value = false
        }, 1500)
      }

      run()
      intervalId = setInterval(run, 4000)
    } else {
      isShow.value = false
    }
  })
})

onUnmounted(() => {
  clearInterval(intervalId)
  clearTimeout(timeoutId)
})
</script>

<template>
  <div class="video-container" :class="{ success: faceIDStore.isSuccess }">
    <canvas ref="canvas"></canvas>
    <video ref="video" :class="{ ready: faceIDStore.isReady }" autoplay muted></video>
    <transition name="fade-scale" mode="out-in">
      <div class="counter" v-if="faceIDStore.count > 0" :key="faceIDStore.count">
        {{ faceIDStore.count }}
      </div>
    </transition>
    <div class="face-id-step" :class="faceIDStore.renderStyle()">
      <div class="bg-success">
        <CheckOutlined />
      </div>
      <div class="dot"></div>
      <div class="dot"></div>
      <div class="dot"></div>
      <div class="dot"></div>
      <div class="dot"></div>
      <div class="dot"></div>
      <div class="dot"></div>
      <div class="dot"></div>
      <div class="dot"></div>
      <div class="dot"></div>
      <div class="dot"></div>
      <div class="dot"></div>
      <div class="dot"></div>
      <div class="dot"></div>
      <div class="dot"></div>
      <div class="dot"></div>
      <div class="dot"></div>
      <div class="dot"></div>
      <div class="dot"></div>
      <div class="dot"></div>
      <div class="dot"></div>
      <div class="dot"></div>
      <div class="dot"></div>
      <div class="dot"></div>
      <div class="dot"></div>
      <div class="dot"></div>
      <div class="dot"></div>
      <div class="dot"></div>
      <div class="dot"></div>
      <div class="dot"></div>
      <div class="dot"></div>
      <div class="dot"></div>
      <div class="dot"></div>
      <div class="dot"></div>
      <div class="dot"></div>
      <div class="dot"></div>
      <div class="axis" ref="axis">
        <div class="a-x">
          <div class="a-x__left"></div>
          <div class="a-x__right"></div>
        </div>
        <div class="a-y">
          <div class="a-y__top"></div>
          <div class="a-y__bottom"></div>
        </div>
      </div>
      <div class="step-action" :class="{ show: isShow && !faceIDStore.count }">
        <div :class="faceIDStore.renderAction()"></div>
      </div>
    </div>
    <div class="face-background"></div>
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
</template>
