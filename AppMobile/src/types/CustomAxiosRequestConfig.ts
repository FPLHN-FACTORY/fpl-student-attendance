import { InternalAxiosRequestConfig } from 'axios'

export interface CustomAxiosRequestConfig extends InternalAxiosRequestConfig {
  isRetry?: boolean
}
