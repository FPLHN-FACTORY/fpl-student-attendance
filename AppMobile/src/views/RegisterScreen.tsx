import { RootStackParamList } from '@/types/RootStackParamList'
import {
  base64ToFile,
  generateSignature,
  logout,
  unlinkBase64ToFile,
  UPPER_HEADER_HEIGHT,
  UPPER_HEADER_PADDING_TOP,
} from '@/utils'
import { NativeStackScreenProps } from '@react-navigation/native-stack'
import { View, StyleSheet, StatusBar, AppState, TouchableOpacity, Image } from 'react-native'
import { SafeAreaView, useSafeAreaInsets } from 'react-native-safe-area-context'
import useDoubleBackPressExit from '@/components/useDoubleBackPressExit'
import { Button, Icon, IconButton, Text } from 'react-native-paper'
import { Colors } from '@/constants/Colors'
import ModalConfirm from '@/components/ModalConfirm'
import { useEffect, useRef, useState } from 'react'
import type { WebView as WebViewType } from 'react-native-webview'
import requestAPI from '@/services/requestApiService'
import { API_ROUTES } from '@/constants/ApiRoutes'
import { useLoading } from '@/components/loading/LoadingContext'
import { useGlobalSnackbar } from '@/components/GlobalSnackbarProvider'
import { useGlobalStore } from '@/utils/GlobalStore'
import Select from '@/components/form/Select'
import { KeyboardAwareScrollView } from 'react-native-keyboard-aware-scroll-view'
import CustomInputText from '@/components/form/CustomInputText'
import { saveToken } from '@/utils/secureStorageUtils'
import { SECURE_CONSTANT } from '@/constants'

type Props = NativeStackScreenProps<RootStackParamList, 'Register'>

const RegisterScreen: React.FC<Props> = ({ route, navigation }) => {
  useDoubleBackPressExit()

  const { code: userCode, name: userName } = route.params

  const { showLoading, hideLoading } = useLoading()

  const { showSuccess, showError } = useGlobalSnackbar()

  const webviewRef = useRef<WebViewType>(null)

  const studentInfo = useGlobalStore((state) => state.studentInfo)
  const setStudentInfo = useGlobalStore((state) => state.setStudentInfo)

  const [visible, setVisible] = useState(false)

  const lstFacilities = useGlobalStore((state) => state.lstFacilities)
  const [facility, setFacility] = useState(lstFacilities?.[0]?.id)
  const [code, setCode] = useState<string>(userCode)
  const [name, setName] = useState<string>(userName)

  const dataWebcam = useGlobalStore((state) => state.dataWebcam)
  const setDataWebcam = useGlobalStore((state) => state.setDataWebcam)

  const handleLogout = async () => {
    await logout()
    navigation.replace('Login')
  }

  const insets = useSafeAreaInsets()

  const fetchDatUser = async () => {
    showLoading()
    requestAPI
      .get(API_ROUTES.FETCH_DATA_STUDENT_INFO)
      .then(async ({ data: response }) => {
        const userData = response?.data
        setStudentInfo(userData)
        navigation.replace('Dashboard')
      })
      .catch(async () => {
        showError('Vui lòng đăng nhập lại', 2000)
        handleLogout()
      })
      .finally(() => {
        hideLoading()
      })
  }

  const handleSubmit = async () => {
    setVisible(false)
    showLoading()

    const file = await base64ToFile(dataWebcam.image)
    const data = new FormData()
    data.append('image', file as any)
    data.append('idFacility', facility)
    data.append('code', code)
    data.append('name', name)

    requestAPI
      .put(`${API_ROUTES.FETCH_DATA_REGISTER}`, data, {
        headers: {
          'Content-Type': 'multipart/form-data',
          'X-Signature': generateSignature(studentInfo.id, file.size),
        },
      })
      .then(async ({ data: response }) => {
        showSuccess(response.message, 2000)
        await saveToken(SECURE_CONSTANT.ACCESS_TOKEN, response.data.accessToken)
        await saveToken(SECURE_CONSTANT.REFRESH_TOKEN, response.data.refreshToken)

        await fetchDatUser()
      })
      .catch((error) => {
        showError(error.response?.data?.message || 'Có lỗi xảy ra. Vui lòng thử lại', 2000)
      })
      .finally(() => {
        hideLoading()
        clearDataWebcam()
        unlinkBase64ToFile(file.uri)
      })
  }

  const handleShowCamera = () => {
    navigation.navigate('Webcam')
  }

  const clearDataWebcam = () => {
    setDataWebcam({
      image: '',
    })
  }

  useEffect(() => {
    clearDataWebcam()
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
              Đăng ký tài khoản sinh viên
            </Text>
          </View>
        </View>
      </SafeAreaView>

      <ModalConfirm
        isShow={visible}
        title="Xác nhận đăng ký mới"
        onOk={handleSubmit}
        onCancel={() => {
          setVisible(false)
        }}
      >
        <Text>Không thể hoàn tác. Bạn thực sự muốn đăng ký với thông tin này?</Text>
      </ModalConfirm>

      <KeyboardAwareScrollView
        contentContainerStyle={{ flexGrow: 1 }}
        enableOnAndroid={true}
        extraScrollHeight={120}
        style={styles.containerForm}
      >
        <View style={styles.imageContainer}>
          <TouchableOpacity onPress={handleShowCamera}>
            {!dataWebcam.image ? (
              <View style={styles.image}>
                <Icon source="face-recognition" size={128} color="#d0cdda" />
              </View>
            ) : (
              <Image
                source={{
                  uri: dataWebcam.image,
                }}
                style={styles.imageWebcam}
              />
            )}
          </TouchableOpacity>
        </View>

        <Select
          value={facility}
          onChange={setFacility}
          options={lstFacilities.map((o) => ({ label: o.name, value: o.id }))}
          placeholder="-- Chọn 1 cơ sở --"
          label="Cơ sở"
        />

        <CustomInputText
          label="MSSV"
          placeholder="Nhập mã số sinh viên"
          value={code}
          onChange={setCode}
          required={true}
        />

        <CustomInputText
          label="Họ và tên"
          placeholder="Nhập họ và tên"
          value={name}
          onChange={setName}
          required={true}
        />
        <Button
          mode="contained"
          disabled={!facility || !code || !name || !dataWebcam.image || dataWebcam.image.length < 1}
          style={styles.button}
          onPress={() => setVisible(true)}
        >
          Đăng ký tài khoản
        </Button>
      </KeyboardAwareScrollView>
    </View>
  )
}

export default RegisterScreen

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
  containerForm: {
    flex: 1,
    padding: 20,
  },
  imageContainer: {
    alignItems: 'center',
    justifyContent: 'center',
    width: '100%',
    marginBottom: 30,
  },
  image: {
    padding: 60,
    borderRadius: 999,
    backgroundColor: '#f6f8fa',
    borderColor: '#c7c2da',
    borderWidth: 2,
    borderStyle: 'dotted',
  },
  button: {
    borderRadius: 10,
    marginTop: 20,
  },
  imageWebcam: {
    height: 200,
    width: 200,
    borderRadius: 999,
    backgroundColor: '#f6f8fa',
    borderColor: '#c7c2da',
    borderWidth: 2,
    borderStyle: 'dotted',
  },
})
