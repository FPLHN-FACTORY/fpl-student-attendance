<template>
  <div :class="computedClass">
    <slot></slot>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  direction: { type: String, default: 'row' },
  justify: { type: String, default: 'start' },
  align: { type: String, default: 'stretch' },
})

const computedClass = computed(() => [
  'd-flex',
  `flex-${props.direction}`,
  `justify-${props.justify}`,
  `items-${props.align}`,
])
</script>
