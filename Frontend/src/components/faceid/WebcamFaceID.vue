<script setup>
import { onMounted, ref } from 'vue'
import useFaceIDStore from '@/stores/useFaceIDStore'

const faceIDStore = useFaceIDStore()
const video = ref(null)
const canvas = ref(null)
const axis = ref(null)

onMounted(async () => {
  faceIDStore.setup(video, canvas, axis)
  await faceIDStore.loadModels()
})
</script>

<template>
  <div class="video-container">
    <canvas ref="canvas"></canvas>
    <video ref="video" :class="faceIDStore.isFaceChecking()" autoplay muted></video>
    <div class="counter" v-show="faceIDStore.count">
      <span class="n">3</span>
      <span class="n">2</span>
      <span class="n">1</span>
    </div>
    <div class="face-id-step" :class="faceIDStore.renderStyle()">
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
