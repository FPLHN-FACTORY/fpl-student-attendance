import axios, { AxiosError, AxiosHeaders, AxiosInstance, AxiosResponse } from 'axios'
import { API_URL, SECURE_CONSTANT } from '../constants'
import { CustomAxiosRequestConfig } from '../types/CustomAxiosRequestConfig'
import { getToken, saveToken } from '../utils/secureStorageUtils'
import { RefreshTokenResponse } from '../types/RefreshTokenResponse'
import { API_ROUTES } from '../constants/ApiRoutes'
import { Alert } from 'react-native'
import { navigate } from '@/types/navigationRef'
import { logout } from '@/utils'

const requestAPI: AxiosInstance = axios.create({
  baseURL: API_URL,
  timeout: 10000,
})

requestAPI.interceptors.request.use(async (config: CustomAxiosRequestConfig) => {
  const token = await getToken(SECURE_CONSTANT.ACCESS_TOKEN)
  if (token && config.headers) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

requestAPI.interceptors.response.use(
  (response: AxiosResponse) => response,
  async (error: AxiosError) => {
    const originalRequest = error.config as CustomAxiosRequestConfig
    const refreshToken = await getToken(SECURE_CONSTANT.REFRESH_TOKEN)

    if (error.response && error.response.status === 401 && !originalRequest.isRetry) {
      originalRequest.isRetry = true

      try {
        const { data }: AxiosResponse<RefreshTokenResponse> = await axios.post(
          API_ROUTES.FETCH_DATA_REFRESH_TOKEN,
          {
            refreshToken: refreshToken,
          },
          {
            headers: {
              Authorization: `Bearer ${refreshToken}`,
            },
          },
        )

        await saveToken(SECURE_CONSTANT.ACCESS_TOKEN, data.data.accessToken)
        await saveToken(SECURE_CONSTANT.REFRESH_TOKEN, data.data.refreshToken)
        if (originalRequest.headers) {
          ;(originalRequest.headers as AxiosHeaders).set(
            'Authorization',
            `Bearer ${data.data.accessToken}`,
          )
        }

        return requestAPI(originalRequest)
      } catch (e) {
        Alert.alert('Lỗi', 'Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại', [
          {
            text: 'OK',
            onPress: async () => {
              await logout()
              setTimeout(() => navigate('Login'), 100)
            },
          },
        ])
      }
    }

    return Promise.reject(error)
  },
)

export default requestAPI
