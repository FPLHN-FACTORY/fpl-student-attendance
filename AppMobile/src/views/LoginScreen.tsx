import * as AuthSession from 'expo-auth-session'
import { useAuthRequest } from 'expo-auth-session'
import React, { useEffect, useRef, useState } from 'react'
import { View, StyleSheet, Image } from 'react-native'
import { SECURE_CONSTANT, SERVER_DOMAIN } from '../constants'
import { useGlobalStore } from '../utils/GlobalStore'
import logoFPT from '../../assets/png/logo-fpt.png'
import logoUDPM from '../../assets/png/logo-udpm.png'
import { Button, Text } from 'react-native-paper'
import { Colors } from '../constants/Colors'
import useDoubleBackPressExit from '../components/useDoubleBackPressExit'
import { useGlobalSnackbar } from '@/components/GlobalSnackbarProvider'
import { decodeBase64Utf8 } from '@/utils'
import { NativeStackScreenProps } from '@react-navigation/native-stack'
import { RootStackParamList } from '@/types/RootStackParamList'
import requestAPI from '@/services/requestApiService'
import { API_ROUTES } from '@/constants/ApiRoutes'
import { saveToken } from '@/utils/secureStorageUtils'
import Select from '@/components/form/Select'

type Props = NativeStackScreenProps<RootStackParamList, 'Login'>

const LoginScreen: React.FC<Props> = ({ navigation }) => {
  useDoubleBackPressExit()

  const { showError, showSuccess, showWarning } = useGlobalSnackbar()

  const lstFacilities = useRef(useGlobalStore((state) => state.lstFacilities)).current
  const [facility, setFacility] = useState('')
  const setStudentInfo = useGlobalStore((state) => state.setStudentInfo)

  const redirectUri = AuthSession.makeRedirectUri({
    scheme: 'fplstudentattendance',
    path: 'redirect',
  })

  const [request, response, promptAsync] = useAuthRequest(
    {
      clientId: '',
      redirectUri,
      scopes: [],
      responseType: AuthSession.ResponseType.Token,
      extraParams: {
        role: 'STUDENT',
        facility_id: facility,
        redirect_uri: redirectUri,
      },
    },
    {
      authorizationEndpoint: SERVER_DOMAIN + '/authorization',
    },
  )

  useEffect(() => {
    if (response === null) {
      return
    }

    const params = (response as { params: Record<string, string> }).params

    if (!params?.authencation_token) {
      let message
      try {
        const decoded = JSON.parse(decodeBase64Utf8(params.authencation_error))
        message = decoded.message || 'Đăng nhập thất bại'
      } catch (e) {}
      return message && showError(message)
    }

    try {
      const authencation_token = JSON.parse(decodeBase64Utf8(params.authencation_token))
      const access_token = authencation_token?.accessToken
      const refresh_token = authencation_token?.refreshToken

      requestAPI
        .get(API_ROUTES.FETCH_DATA_STUDENT_INFO, {
          headers: {
            Authorization: `Bearer ${access_token}`,
            'Content-Type': 'application/json',
          },
        })
        .then(async ({ data: response }) => {
          const userData = response?.data
          setStudentInfo(userData)

          await saveToken(SECURE_CONSTANT.ACCESS_TOKEN, access_token)
          await saveToken(SECURE_CONSTANT.REFRESH_TOKEN, refresh_token)

          if (userData.faceEmbedding !== 'OK') {
            showWarning('Vui lòng cập nhật sinh trắc học', 2000)
            return navigation.replace('UpdateFace')
          }

          showSuccess('Đăng nhập thành công', 2000)
          navigation.replace('Dashboard')
        })
        .catch(() => {
          showWarning('Vui lòng đăng ký thông tin sinh viên', 2000)
          navigation.replace('Register')
        })
    } catch (e) {
      showError('Có lỗi xảy ra. Vui lòng thử lại sau ít phút')
    }
  }, [response])

  return (
    <View style={styles.container}>
      <View style={styles.logoContainer}>
        <Image source={logoFPT} resizeMode="contain" style={styles.logoImage} />
        <Image source={logoUDPM} resizeMode="contain" style={styles.logoImage} />
      </View>
      <Text variant="headlineLarge" style={styles.title}>
        Đăng nhập
      </Text>
      <Select
        value={facility}
        onChange={(val) => setFacility(val)}
        options={lstFacilities.map((o) => ({ label: o.name, value: o.id }))}
        placeholder="-- Vui lòng chọn 1 cơ sở --"
        label="Cơ sở"
      />
      <View style={styles.line} />
      <Text style={styles.lineText}>SOCIAL</Text>
      <Button
        icon="google"
        mode="contained"
        disabled={!request || !facility}
        onPress={() => promptAsync()}
      >
        Login with Google
      </Button>
    </View>
  )
}

export default LoginScreen

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    paddingLeft: 40,
    paddingRight: 40,
    backgroundColor: '#fff',
  },
  logoContainer: {
    display: 'flex',
    justifyContent: 'center',
    alignItems: 'center',
  },
  logoImage: {
    width: 224,
    height: 100,
    resizeMode: 'contain',
  },
  title: {
    marginTop: 40,
    marginBottom: 20,
    alignSelf: 'center',
    textTransform: 'uppercase',
    fontWeight: 'bold',
    color: Colors.primary,
  },
  line: {
    width: '100%',
    height: 2,
    backgroundColor: '#f3f3f3',
    borderRadius: 10,
    margin: 20,
  },
  lineText: {
    backgroundColor: '#fff',
    padding: 10,
    textTransform: 'uppercase',
    marginTop: -40,
  },
  button: {},
})
