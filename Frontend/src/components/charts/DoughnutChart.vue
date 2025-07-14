<script setup>
import { ref, onMounted, onBeforeUnmount, watch, nextTick, toRaw } from 'vue'
import { Chart, registerables } from 'chart.js'

Chart.register(...registerables)

const props = defineProps({
  data: Object,
  height: {
    type: [Number, String],
    default: 300,
  },
  width: {
    type: [Number, String],
    default: '100%',
  },
  cutout: {
    type: [Number, String],
    default: '70%', // Controls the size of the hole in the middle
  },
  legend: {
    type: Boolean,
    default: true,
  }
})

const chart = ref(null)
const canvas = ref(null)

const clearChart = () => {
  toRaw(chart.value).destroy()
  chart.value = null
}

const renderChart = async () => {
  await nextTick()

  if (!canvas.value || !canvas.value.isConnected) {
    return
  }

  const ctx = canvas.value?.getContext('2d')

  if (!ctx) {
    return
  }

  if (chart.value) {
    clearChart()
  }

  chart.value = new Chart(ctx, {
    type: 'doughnut',
    data: props.data,
    options: {
      responsive: true,
      maintainAspectRatio: false,
      cutout: props.cutout,
      plugins: {
        legend: {
          display: props.legend,
          position: 'bottom',
          labels: {
            padding: 20,
            boxWidth: 12,
            font: {
              size: 14,
              weight: '600',
              family: 'Open Sans',
            }
          }
        },
        tooltip: {
          enabled: true,
          callbacks: {
            label: function(context) {
              const label = context.label || '';
              const value = context.raw || 0;
              const total = context.chart.data.datasets[0].data.reduce((a, b) => a + b, 0);
              const percentage = Math.round((value / total) * 100);
              return `${label}: ${value} (${percentage}%)`;
            }
          }
        }
      },
      animation: {
        animateScale: true,
        animateRotate: true
      }
    },
  })
}

onMounted(renderChart)

watch(
  () => props.data,
  async () => {
    renderChart()
  },
  { deep: true },
)

onBeforeUnmount(() => {
  if (chart.value) {
    clearChart()
  }
})
</script>

<template>
  <div>
    <canvas ref="canvas" :style="{ height: height + 'px', width: width }"></canvas>
  </div>
</template>
