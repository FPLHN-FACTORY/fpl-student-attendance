import { AUTHENCATION_STORAGE_TOKEN } from '@/constants/authenticationConstant'
import { API_URL } from '@/constants/routesConstant'
import useAuthStore from '@/stores/useAuthStore'
import { localStorageUtils } from '@/utils/localStorageUtils'
import axios from 'axios'

const requestAPI = axios.create({
  baseURL: `${API_URL}`,
})

requestAPI.interceptors.request.use((config) => {
  const token = localStorageUtils.get(AUTHENCATION_STORAGE_TOKEN)
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

requestAPI.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401) {
      const authStore = useAuthStore()
      authStore.logout()
      window.location.reload()
    }
    return Promise.reject(error)
  },
)

export default requestAPI
