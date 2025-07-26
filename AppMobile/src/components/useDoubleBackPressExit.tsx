import { useEffect, useRef } from 'react'
import { BackHandler } from 'react-native'
import { useGlobalSnackbar } from './GlobalSnackbarProvider'

const useDoubleBackPressExit = (): void => {
  const backPressTimestamp = useRef<number>(0)

  const { showMessage } = useGlobalSnackbar()

  useEffect(() => {
    const onBackPress = (): boolean => {
      const now = Date.now()
      if (backPressTimestamp.current && now - backPressTimestamp.current < 2000) {
        BackHandler.exitApp()
        return true
      }

      backPressTimestamp.current = now
      showMessage('Nhấn lần nữa để thoát')
      return true
    }

    const subscription = BackHandler.addEventListener('hardwareBackPress', onBackPress)

    return () => subscription.remove()
  }, [])
}

export default useDoubleBackPressExit
