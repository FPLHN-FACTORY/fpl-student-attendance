import { useEffect, useRef, useState } from 'react'
import { View, StyleSheet, BackHandler, Platform } from 'react-native'
import { NativeStackScreenProps } from '@react-navigation/native-stack'
import { RootStackParamList } from '../types/RootStackParamList'
import { Colors } from '../constants/Colors'
import requestAPI from '../services/requestApiService'
import { API_ROUTES } from '../constants/ApiRoutes'
import { useGlobalSnackbar } from '../components/GlobalSnackbarProvider'
import { useGlobalStore } from '../utils/GlobalStore'
import Logo from '../../assets/svg/logo.svg'
import { getToken } from '../utils/secureStorageUtils'
import { SECURE_CONSTANT } from '../constants'
import useDoubleBackPressExit from '../components/useDoubleBackPressExit'
import { hasInternet, logout } from '@/utils'
import { Button, Dialog, Portal, Text } from 'react-native-paper'

type Props = NativeStackScreenProps<RootStackParamList, 'Loading'>

const LoadingScreen: React.FC<Props> = ({ navigation }) => {
  const { showError } = useGlobalSnackbar()
  const setFacilities = useGlobalStore((state) => state.setLstFacilities)
  const setSemester = useGlobalStore((state) => state.setLstSemester)
  const setStudentInfo = useGlobalStore((state) => state.setStudentInfo)
  const isLogin = useRef(false)
  const isUpdateFace = useRef(false)
  const [visible, setVisible] = useState(false)

  const showDialog = () => setVisible(true)
  const hideDialog = () => {
    setVisible(false)
    exitApp()
  }

  useDoubleBackPressExit()

  const exitApp = () => {
    if (Platform.OS === 'android') {
      BackHandler.exitApp()
    }
  }

  useEffect(() => {
    let retryCount = 0
    const maxRetry = 5

    const fetchData = async () => {
      const accessToken = await getToken(SECURE_CONSTANT.ACCESS_TOKEN)

      const retry = (msg: string) => {
        showError(msg, 2000)
        if (retryCount < maxRetry) {
          retryCount++
          setTimeout(fetchData, 2000)
        } else {
          showDialog()
        }
      }

      const isConnected = await hasInternet()
      if (!isConnected) {
        retry('Không có kết nối Internet')
        return
      }

      try {
        const loadFaclities = async () => {
          const response = await requestAPI.get(API_ROUTES.FETCH_DATA_FACILITY)
          const data = response.data.data
          setFacilities(data)
        }

        const loadSemester = async () => {
          const response = await requestAPI.get(API_ROUTES.FETCH_DATA_SEMESTER)
          const data = response.data.data
          setSemester(data)
        }

        const loadStudentInfo = async () => {
          return requestAPI
            .get(API_ROUTES.FETCH_DATA_STUDENT_INFO)
            .then(({ data: response }) => {
              const userData = response?.data
              setStudentInfo(userData)
              isUpdateFace.current = userData.faceEmbedding !== 'OK'
              isLogin.current = true
            })
            .catch(async () => {
              await logout()
              isLogin.current = false
            })
        }

        const promises: Promise<any>[] = [
          loadFaclities(),
          loadSemester(),
          new Promise((r) => setTimeout(r, 2000)),
        ]

        if (accessToken) {
          promises.push(loadStudentInfo())
        }

        await Promise.all(promises)

        if (!isLogin.current) {
          navigation.replace('Login')
        } else {
          navigation.replace(isUpdateFace.current ? 'UpdateFace' : 'Dashboard')
        }
      } catch (error) {
        retry('Lỗi tải dữ liệu, đang thử lại...')
      }
    }

    fetchData()
  }, [])

  return (
    <View style={styles.container}>
      <View style={{ width: 224, height: 224 }}>
        <Logo width="100%" height="100%" preserveAspectRatio="xMidYMid meet" />
      </View>
      <Portal>
        <Dialog visible={visible} onDismiss={hideDialog}>
          <Dialog.Title>Có lỗi xảy ra</Dialog.Title>
          <Dialog.Content>
            <Text variant="bodyMedium">Không thể kết nối tới máy chủ</Text>
          </Dialog.Content>
          <Dialog.Actions>
            <Button onPress={hideDialog}>Xác nhận</Button>
          </Dialog.Actions>
        </Dialog>
      </Portal>
    </View>
  )
}

export default LoadingScreen

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: Colors.primary,
  },
  text: {
    marginTop: 10,
    color: '#ffffff',
  },
})
