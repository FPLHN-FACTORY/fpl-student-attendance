import { AUTHENCATION_STORAGE_TOKEN } from '@/constants/authenticationConstant'
import { API_URL, BASE_URL } from '@/constants/routesConstant'
import useAuthStore from '@/stores/useAuthStore'
import { localStorageUtils } from '@/utils/localStorageUtils'
import axios from 'axios'
import { toast } from 'vue3-toastify'

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
      toast.error('Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại')
      authStore.logout()
      window.location.href = BASE_URL
    }
    return Promise.reject(error)
  },
)

export default requestAPI
