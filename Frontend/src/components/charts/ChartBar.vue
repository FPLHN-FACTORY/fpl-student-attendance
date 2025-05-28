<script setup>
import { onMounted, onBeforeUnmount, ref } from 'vue'
import { Chart, registerables } from 'chart.js'

Chart.register(...registerables)

const props = defineProps({
  data: Object,
  height: [String, Number],
})

const chart = ref(null)
const canvasRef = ref(null)

onMounted(() => {
  const ctx = canvasRef.value.getContext('2d')
  chart.value = new Chart(ctx, {
    type: 'bar',
    data: props.data,
    options: {
      layout: {
        padding: {
          top: 30,
          right: 15,
          left: 10,
          bottom: 5,
        },
      },
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: {
          display: false,
        },
      },
      interaction: {
        mode: 'index',
        intersect: false,
      },
      scales: {
        y: {
          grid: {
            display: true,
            color: 'rgba(255, 255, 255, .2)',
            borderDash: [6],
            borderDashOffset: 6,
          },
          ticks: {
            suggestedMin: 0,
            suggestedMax: 1000,
            display: true,
            color: '#fff',
            font: (context) => {
              const labelCount = context.chart.data.labels.length
              return {
                size: labelCount > 4 ? 10 : 14,
                weight: '600',
                family: 'Open Sans',
              }
            },
          },
        },
        x: {
          grid: {
            display: false,
          },
          ticks: {
            display: true,
            color: '#fff',
            font: {
              size: 14,
              lineHeight: 1.5,
              weight: '600',
              family: 'Open Sans',
            },
          },
        },
      },
    },
  })
})

onBeforeUnmount(() => {
  if (chart.value) chart.value.destroy()
})
</script>

<template>
  <div>
    <canvas ref="canvasRef" :style="{ height: height + 'px' }"></canvas>
  </div>
</template>

<style lang="scss" scoped>
canvas {
  background-image: linear-gradient(to right, #666177, #6b667c, #616495);
}
</style>
