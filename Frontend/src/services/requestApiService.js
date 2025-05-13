import { AUTHENCATION_STORAGE_TOKEN } from '@/constants/authenticationConstant'
import { API_URL, GLOBAL_ROUTE_NAMES } from '@/constants/routesConstant'
import router from '@/router'
import { ROUTE_NAMES_API } from '@/router/authenticationRoute'
import useAuthStore from '@/stores/useAuthStore'
import { localStorageUtils } from '@/utils/localStorageUtils'
import { message } from 'ant-design-vue'
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
  async (error) => {
    const originalRequest = error.config
    if (error.response && error.response.status === 401 && !originalRequest.isRetry) {
      originalRequest.isRetry = true
      const authStore = useAuthStore()
      try {
        const { data: response } = await axios.post(
          ROUTE_NAMES_API.FETCH_DATA_REFRESH_TOKEN,
          {
            refreshToken: authStore.refreshToken,
          },
          {
            headers: {
              Authorization: `Bearer ${authStore.refreshToken}`,
            },
          },
        )
        authStore.setToken(response.data.accessToken, response.data.refreshToken)
        originalRequest.headers.Authorization = `Bearer ${response.data.accessToken}`
        return requestAPI(originalRequest)
      } catch (e) {
        message.destroy()
        message.error('Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại')
        authStore.logout()
        router.push({ name: GLOBAL_ROUTE_NAMES.SWITCH_ROLE })
      }
    }
    return Promise.reject(error)
  },
)

export default requestAPI
