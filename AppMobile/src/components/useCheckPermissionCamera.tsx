import { useEffect, useRef, useState } from 'react'
import { Alert, AppState, Linking } from 'react-native'
import { Camera, PermissionStatus } from 'expo-camera'

const useCheckPermissionCamera = (callback: () => void) => {
  const hasShownAlertRef = useRef(false)
  const [status, setStatus] = useState<PermissionStatus>(PermissionStatus.UNDETERMINED)

  const checkPermission = async () => {
    const { status: currentStatus, canAskAgain } = await Camera.getCameraPermissionsAsync()

    setStatus(currentStatus)

    if (currentStatus === PermissionStatus.GRANTED) {
      hasShownAlertRef.current = false
      return
    }

    if (canAskAgain) {
      const { status: newStatus } = await Camera.requestCameraPermissionsAsync()
      setStatus(newStatus)
      return
    }

    if (!hasShownAlertRef.current) {
      hasShownAlertRef.current = true
      Alert.alert('Cần quyền camera', 'Vui lòng bật quyền camera trong Cài đặt.', [
        { text: 'Huỷ', style: 'cancel', onPress: () => callback() },
        { text: 'Mở Cài đặt', onPress: () => Linking.openSettings() },
      ])
    }
  }

  useEffect(() => {
    checkPermission()

    const subscription = AppState.addEventListener('change', (nextAppState) => {
      if (nextAppState === 'active') {
        hasShownAlertRef.current = false
        checkPermission()
      }
    })

    return () => subscription.remove()
  }, [])
  return status
}

export default useCheckPermissionCamera
