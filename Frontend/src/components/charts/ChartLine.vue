<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { Chart, registerables } from 'chart.js'

Chart.register(...registerables)

const props = defineProps({
  data: Object,
  height: {
    type: [Number, String],
    default: 300,
  },
})

const chart = ref(null)
const canvas = ref(null)

onMounted(() => {
  const ctx = canvas.value.getContext('2d')

  chart.value = new Chart(ctx, {
    type: 'line',
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
      interaction: {
        mode: 'index',
        intersect: false,
      },
      plugins: {
        legend: {
          display: false,
        },
        tooltip: {
          enabled: true,
        },
      },
      scales: {
        y: {
          grid: {
            display: true,
            color: 'rgba(0, 0, 0, .2)',
            borderDash: [6],
            borderDashOffset: [6],
          },
          ticks: {
            suggestedMin: 0,
            suggestedMax: 1000,
            display: true,
            color: '#8C8C8C',
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
            color: '#8C8C8C',
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
    <canvas ref="canvas" :style="{ height: height + 'px' }"></canvas>
  </div>
</template>
