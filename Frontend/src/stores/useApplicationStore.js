import { defineStore } from 'pinia'
import { ref } from 'vue'

const useApplicationStore = defineStore('Application', () => {
  const selectedKeys = ref([Number(sessionStorage.getItem('selectedKeys'))])

  const setSelectedKeys = (key) => {
    selectedKeys.value = [key]
    setSessionSelectedKeys(key)
  }

  const setSessionSelectedKeys = (key) => {
    sessionStorage.setItem('selectedKeys', key)
  }

  return { selectedKeys, setSelectedKeys }
})

export default useApplicationStore
