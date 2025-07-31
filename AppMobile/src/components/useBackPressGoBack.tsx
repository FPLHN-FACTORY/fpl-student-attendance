import { useEffect } from 'react'
import { BackHandler } from 'react-native'
import { NavigationProp } from '@react-navigation/native'

const useBackPressGoBack = (navigation: NavigationProp<any>) => {
  useEffect(() => {
    const backAction = () => {
      if (navigation.canGoBack()) {
        navigation.goBack()
        return true
      }
      return false
    }

    const backHandler = BackHandler.addEventListener('hardwareBackPress', backAction)

    return () => backHandler.remove()
  }, [navigation])
}

export default useBackPressGoBack
