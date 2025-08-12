import { RootStackParamList } from '@/types/RootStackParamList'
import {
  base64ToFile,
  generateSignature,
  unlinkBase64ToFile,
  UPPER_HEADER_HEIGHT,
  UPPER_HEADER_PADDING_TOP,
} from '@/utils'
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
import requestAPI from '@/services/requestApiService'
import { API_ROUTES } from '@/constants/ApiRoutes'
import { useLoading } from '@/components/loading/LoadingContext'
import { useGlobalSnackbar } from '@/components/GlobalSnackbarProvider'
import useBackPressGoBack from '@/components/useBackPressGoBack'
import useCheckPermissionLocation from '@/components/useCheckPermissionLocation'
import { useGlobalStore } from '@/utils/GlobalStore'

type Props = NativeStackScreenProps<RootStackParamList, 'Attendance'>

const AttendanceScreen: React.FC<Props> = ({ route, navigation }) => {
  useBackPressGoBack(navigation)

  const { idPlanDate } = route.params

  const { showLoading, hideLoading } = useLoading()

  const { showSuccess, showError } = useGlobalSnackbar()

  const webviewRef = useRef<WebViewType>(null)

  const onCallbackAttendance = useGlobalStore((state) => state.onCallbackAttendance)

  const handleBack = () => {
    navigation.goBack()
  }

  const insets = useSafeAreaInsets()

  const handleAttendance = async (image: string) => {
    showLoading()

    const file = await base64ToFile(image)
    const data = new FormData()
    data.append('image', file as any)
    data.append('idPlanDate', idPlanDate)
    data.append('latitude', location?.coords.latitude?.toString() ?? '')
    data.append('longitude', location?.coords.longitude?.toString() ?? '')

    requestAPI
      .post(`${API_ROUTES.FETCH_DATA_STUDENT_ATTENDANCE}/checkin`, data, {
        headers: {
          'Content-Type': 'multipart/form-data',
          'X-Signature': generateSignature(idPlanDate, file.size),
        },
      })
      .then(({ data: response }) => {
        showSuccess(response.message, 2000)
        onCallbackAttendance()
        handleBack()
      })
      .catch((error) => {
        showError(error.response?.data?.message || 'Có lỗi xảy ra. Vui lòng thử lại', 2000)
      })
      .finally(() => {
        hideLoading()
        unlinkBase64ToFile(file.uri)
      })
  }

  const statusWebcam = useCheckPermissionCamera(handleBack)

  const { location } = useCheckPermissionLocation(handleBack)

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
      <StatusBar barStyle="light-content" translucent backgroundColor="transparent" />

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
              Xác nhận khuôn mặt
            </Text>
          </View>
        </View>
      </SafeAreaView>

      <View style={styles.containerWebcam}>
        {statusWebcam === PermissionStatus.GRANTED && (
          <WebView
            ref={webviewRef}
            source={{ uri: `${CLIENT_DOMAIN}/${SECRET_KEY}/true` }}
            onMessage={({ nativeEvent }) => {
              try {
                const data = JSON.parse(nativeEvent?.data)
                if (data?.image) {
                  handleAttendance(data?.image)
                }
              } catch (error) {}
            }}
          />
        )}
      </View>
    </View>
  )
}

export default AttendanceScreen

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
