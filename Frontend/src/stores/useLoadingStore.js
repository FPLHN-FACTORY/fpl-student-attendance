import { defineStore } from 'pinia'
import { ref } from 'vue'

const useLoadingStore = defineStore('loadingPage', () => {
  const isLoading = ref(false)

  const show = () => (isLoading.value = true)
  const hide = () => (isLoading.value = false)

  return { isLoading, show, hide }
})

export default useLoadingStore
