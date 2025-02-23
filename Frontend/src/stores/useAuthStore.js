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

  const login = (tokenData) => {
    if (tokenData == '') {
      return false
    }

    user.value = decodeToken(tokenData)
    token.value = tokenData

    localStorageUtils.set(AUTHENCATION_STORAGE_TOKEN, token.value)
    localStorageUtils.set(AUTHENCATION_STORAGE_USER_DATA, user.value)

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

  return { user, token, isLogin, login, logout }
})

export default useAuthStore
