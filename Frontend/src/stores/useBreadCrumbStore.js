import { defineStore } from 'pinia'
import { ref } from 'vue'

const useBreadcrumbStore = defineStore('breadcrumb', () => {
  const routes = ref([])

  const setRoutes = (data) => (routes.value = data)
  const push = (data) => routes.value.push(data)
  const clear = () => (routes.value = [])

  return { routes, setRoutes, push, clear }
})

export default useBreadcrumbStore
