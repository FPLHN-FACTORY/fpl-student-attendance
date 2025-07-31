import { useEffect, useRef, useState } from 'react'
import { Alert, AppState, Linking } from 'react-native'
import * as Location from 'expo-location'

const useCheckPermissionLocation = (callback: () => void) => {
  const hasShownAlertRef = useRef(false)
  const [status, setStatus] = useState<Location.PermissionStatus>(
    Location.PermissionStatus.UNDETERMINED,
  )
  const [location, setLocation] = useState<Location.LocationObject | null>(null)

  const checkPermission = async () => {
    const { status: currentStatus, canAskAgain } = await Location.getForegroundPermissionsAsync()
    setStatus(currentStatus)

    if (currentStatus === 'granted') {
      hasShownAlertRef.current = false
      const loc = await Location.getCurrentPositionAsync({})
      setLocation(loc)
      return
    }

    if (canAskAgain) {
      const { status: newStatus } = await Location.requestForegroundPermissionsAsync()
      setStatus(newStatus)
      if (newStatus === 'granted') {
        const loc = await Location.getCurrentPositionAsync({})
        setLocation(loc)
      }
      return
    }

    if (!hasShownAlertRef.current) {
      hasShownAlertRef.current = true
      Alert.alert('Cần quyền vị trí', 'Vui lòng bật quyền vị trí trong Cài đặt.', [
        { text: 'Huỷ', style: 'cancel', onPress: () => callback() },
        { text: 'Mở Cài đặt', onPress: () => Linking.openSettings() },
      ])
    }
  }

  useEffect(() => {
    checkPermission()

    const subscription = AppState.addEventListener('change', (state) => {
      if (state === 'active') {
        hasShownAlertRef.current = false
        checkPermission()
      }
    })

    return () => subscription.remove()
  }, [])

  return { status, location }
}

export default useCheckPermissionLocation
