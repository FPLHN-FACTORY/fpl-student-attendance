import { RootStackParamList } from '@/types/RootStackParamList'
import { UPPER_HEADER_HEIGHT, UPPER_HEADER_PADDING_TOP } from '@/utils'
import { NativeStackScreenProps } from '@react-navigation/native-stack'
import { View, StyleSheet, StatusBar, AppState } from 'react-native'
import { SafeAreaView, useSafeAreaInsets } from 'react-native-safe-area-context'
import { IconButton, Text } from 'react-native-paper'
import { Colors } from '@/constants/Colors'
import { WebView } from 'react-native-webview'
import { CLIENT_DOMAIN, SECRET_KEY } from '@/constants'
import { useEffect, useRef } from 'react'
import type { WebView as WebViewType } from 'react-native-webview'
import { PermissionStatus } from 'expo-camera'
import useCheckPermissionCamera from '@/components/useCheckPermissionCamera'
import useBackPressGoBack from '@/components/useBackPressGoBack'
import { useGlobalStore } from '@/utils/GlobalStore'

type Props = NativeStackScreenProps<RootStackParamList, 'Webcam'>

const WebcamScreen: React.FC<Props> = ({ navigation }) => {
  useBackPressGoBack(navigation)

  const webviewRef = useRef<WebViewType>(null)
  const setDataWebcam = useGlobalStore((state) => state.setDataWebcam)

  const insets = useSafeAreaInsets()

  const handleBack = () => {
    navigation.goBack()
  }

  const status = useCheckPermissionCamera(handleBack)

  useEffect(() => {
    const subscription = AppState.addEventListener('change', (nextAppState) => {
      if (nextAppState === 'active') {
        webviewRef.current?.reload()
      }
    })

    return () => subscription.remove()
  }, [])

  return (
    <View style={styles.container}>
      <StatusBar barStyle="dark-content" translucent backgroundColor="#ffffff" />

      <View style={{ height: UPPER_HEADER_HEIGHT + insets.top, paddingTop: insets.top }} />

      <SafeAreaView style={styles.header}>
        <View style={styles.upperHeader}>
          <View style={{ flexDirection: 'row', alignItems: 'center' }}>
            <IconButton
              icon="chevron-left"
              size={24}
              iconColor={Colors.primary}
              onPress={handleBack}
              style={{ backgroundColor: '#ffffff21' }}
            />
            <Text variant="titleMedium" style={styles.title}>
              Vui lòng xác nhận khuôn mặt
            </Text>
          </View>
        </View>
      </SafeAreaView>

      <View style={styles.containerWebcam}>
        {status === PermissionStatus.GRANTED && (
          <WebView
            ref={webviewRef}
            source={{ uri: `${CLIENT_DOMAIN}/${SECRET_KEY}/false` }}
            onMessage={({ nativeEvent }) => {
              try {
                const data = JSON.parse(nativeEvent?.data)
                if (data?.image) {
                  setDataWebcam({
                    image: data.image,
                  })
                  handleBack()
                }
              } catch (error) {}
            }}
          />
        )}
      </View>
    </View>
  )
}

export default WebcamScreen

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#ffffff',
  },
  header: {
    position: 'absolute',
    width: '100%',
    backgroundColor: 'transparent',
  },
  upperHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 16,
    height: UPPER_HEADER_HEIGHT + UPPER_HEADER_PADDING_TOP,
  },
  title: {
    flex: 1,
    paddingLeft: 20,
  },
  containerWebcam: {
    flex: 1,
    padding: 10,
  },
})
