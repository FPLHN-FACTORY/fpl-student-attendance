import { defineStore } from 'pinia'
import { ref } from 'vue'

const useLoadingStore = defineStore('loadingPage', () => {
  const isLoading = ref(false)
  const classNoScroll = 'no-scroll'

  const show = () => {
    isLoading.value = true
    document.body.classList.add(classNoScroll)
  }
  const hide = () => {
    isLoading.value = false
    document.body.classList.remove(classNoScroll)
  }

  return { isLoading, show, hide }
})

export default useLoadingStore
