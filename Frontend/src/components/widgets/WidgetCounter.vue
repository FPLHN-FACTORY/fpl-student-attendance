<script setup>
import { defineProps } from 'vue'

const props = defineProps({
  title: {
    type: String,
    default: '',
  },
  value: {
    type: Number,
    default: 0,
  },
  prefix: {
    type: String,
    default: '',
  },
  suffix: {
    type: String,
    default: '',
  },
  icon: {
    type: String,
    default: '',
  },
  status: {
    type: String,
    default: 'success',
  },
})
</script>
<template>
  <a-card :bordered="false" class="widget-1">
    <a-statistic
      :title="title"
      :value="value"
      :prefix="prefix"
      :suffix="suffix"
      :precision="0"
      class="text-success"
      :class="'text-' + status"
    />
    <component class="icon" :is="icon" />
  </a-card>
</template>
