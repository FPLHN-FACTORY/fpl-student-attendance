import { RootStackParamList } from '@/types/RootStackParamList'
import { base64ToBlob, logout, UPPER_HEADER_HEIGHT, UPPER_HEADER_PADDING_TOP } from '@/utils'
import { NativeStackScreenProps } from '@react-navigation/native-stack'
import { View, StyleSheet, StatusBar, AppState } from 'react-native'
import { SafeAreaView, useSafeAreaInsets } from 'react-native-safe-area-context'
import useDoubleBackPressExit from '@/components/useDoubleBackPressExit'
import { IconButton, Text } from 'react-native-paper'
import { Colors } from '@/constants/Colors'
import { WebView } from 'react-native-webview'
import { CLIENT_DOMAIN, SECRET_KEY } from '@/constants'
import ModalConfirm from '@/components/ModalConfirm'
import { useEffect, useRef, useState } from 'react'
import type { WebView as WebViewType } from 'react-native-webview'
import { PermissionStatus } from 'expo-camera'
import useCheckPermissionCamera from '@/components/useCheckPermissionCamera'
import requestAPI from '@/services/requestApiService'
import { API_ROUTES } from '@/constants/ApiRoutes'
import { useLoading } from '@/components/loading/LoadingContext'
import { useGlobalSnackbar } from '@/components/GlobalSnackbarProvider'

type Props = NativeStackScreenProps<RootStackParamList, 'UpdateFace'>

const UpdateFaceScreen: React.FC<Props> = ({ navigation }) => {
  useDoubleBackPressExit()

  const { showLoading, hideLoading } = useLoading()

  const { showSuccess, showError } = useGlobalSnackbar()

  const webviewRef = useRef<WebViewType>(null)

  const [visible, setVisible] = useState(false)
  const [image, setImage] = useState('')

  const handleLogout = async () => {
    await logout()
    navigation.replace('Login')
  }

  const insets = useSafeAreaInsets()

  const handleSubmit = () => {
    setVisible(false)
    showLoading()
    const data = new FormData()
    data.append('image', base64ToBlob(image))

    requestAPI
      .put(`${API_ROUTES.FETCH_DATA_STUDENT_UPDATE_FACEID}`, data, {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      })
      .then(({ data: response }) => {
        showSuccess(response.message, 2000)
        navigation.replace('Dashboard')
      })
      .catch((error) => {
        showError(error.response?.data?.message, 2000)
      })
      .finally(() => {
        hideLoading()
      })
  }

  const status = useCheckPermissionCamera(handleLogout)

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
              onPress={handleLogout}
              style={{ backgroundColor: '#ffffff21' }}
            />
            <Text variant="titleMedium" style={styles.title}>
              Vui lòng cập nhật khuôn mặt
            </Text>
          </View>
        </View>
      </SafeAreaView>

      <ModalConfirm
        isShow={visible}
        title="Xác nhận đăng ký khuôn mặt"
        onOk={handleSubmit}
        onCancel={() => {
          setVisible(false)
        }}
      >
        <Text>Bạn có chắc muốn đăng ký dữ liệu khuôn mặt này?</Text>
      </ModalConfirm>

      <View style={styles.containerWebcam}>
        {status === PermissionStatus.GRANTED && (
          <WebView
            ref={webviewRef}
            source={{ uri: `${CLIENT_DOMAIN}/${SECRET_KEY}/false` }}
            onMessage={({ nativeEvent }) => {
              try {
                const data = JSON.parse(nativeEvent?.data)
                if (data?.descriptors) {
                  setImage(data.image)
                  setVisible(true)
                }
              } catch (error) {}
            }}
          />
        )}
      </View>
    </View>
  )
}

export default UpdateFaceScreen

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
