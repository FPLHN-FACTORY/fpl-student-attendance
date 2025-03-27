import {
  AUTHENCATION_STORAGE_TOKEN,
  AUTHENCATION_STORAGE_USER_DATA,
} from '@/constants/authenticationConstant'
import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { jwtDecode } from 'jwt-decode'
import { localStorageUtils } from '@/utils/localStorageUtils'

const useAuthStore = defineStore('authUser', () => {
  const user = ref(localStorageUtils.get(AUTHENCATION_STORAGE_USER_DATA) || null)

  const token = ref(localStorageUtils.get(AUTHENCATION_STORAGE_TOKEN) || null)

  const isLogin = computed(() => user.value !== null)

  const decodeToken = (tokenData) => {
    try {
      return jwtDecode(tokenData)
    } catch (error) {
      return null
    }
  }

  const setUser = (data) => {
    user.value = data
    localStorageUtils.set(AUTHENCATION_STORAGE_USER_DATA, user.value)
  }

  const setToken = (data) => {
    token.value = data
    localStorageUtils.set(AUTHENCATION_STORAGE_TOKEN, token.value)
  }

  const login = (tokenData) => {
    if (tokenData == '') {
      return false
    }

    setUser(decodeToken(tokenData))
    setToken(tokenData)

    if (user.value == null) {
      logout()
      return false
    }

    return true
  }

  const logout = () => {
    user.value = null
    token.value = null

    localStorageUtils.remove(AUTHENCATION_STORAGE_TOKEN)
    localStorageUtils.remove(AUTHENCATION_STORAGE_USER_DATA)
  }

  const updateUser = (data = {}) => {
    setUser({
      ...user.value,
      ...data,
      picture: user.value.picture,
      role: user.value.role,
    })
  }

  return { user, token, isLogin, login, logout, updateUser, setToken }
})

export default useAuthStore
