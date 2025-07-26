import { SERVER_DOMAIN } from '@/constants'
import { RootStackParamList } from '@/types/RootStackParamList'
import { logout, UPPER_HEADER_HEIGHT, UPPER_HEADER_PADDING_TOP } from '@/utils'
import { NativeStackScreenProps } from '@react-navigation/native-stack'
import { useEffect, useState } from 'react'
import { View, StyleSheet, Text, StatusBar, SafeAreaView } from 'react-native'
import { Button } from 'react-native-paper'
import WebView from 'react-native-webview'
import { Camera } from 'expo-camera'

type Props = NativeStackScreenProps<RootStackParamList, 'UpdateFace'>

const UpdateFaceScreen: React.FC<Props> = ({ navigation }) => {
  const handleLogout = async () => {
    await logout()
    navigation.replace('Login')
  }

  const [hasPermission, setHasPermission] = useState<boolean | null>(null)

  useEffect(() => {
    ;(async () => {
      const { status } = await Camera.requestCameraPermissionsAsync()
      setHasPermission(status === 'granted')
    })()
  }, [])

  return (
    <View style={styles.container}>
      <StatusBar barStyle="light-content" />

      <SafeAreaView>
        <View style={styles.upperHeaderPlaceholder} />
      </SafeAreaView>

      <SafeAreaView style={styles.header}>
        <View style={styles.upperHeader}>
          <Text>Đây à header</Text>
        </View>
      </SafeAreaView>

      <WebView
        source={{ uri: SERVER_DOMAIN }}
        mediaPlaybackRequiresUserAction={false}
        allowsInlineMediaPlayback={true}
        javaScriptEnabled={true}
        domStorageEnabled={true}
        style={{ height: 100 }}
      />
      <Button mode="contained" onPress={() => handleLogout()}>
        Đăng xuất
      </Button>
    </View>
  )
}

export default UpdateFaceScreen

const styles = StyleSheet.create({
  container: { flex: 1 },
  upperHeaderPlaceholder: {
    height: UPPER_HEADER_HEIGHT + UPPER_HEADER_PADDING_TOP,
    paddingTop: UPPER_HEADER_PADDING_TOP,
  },
  header: {
    position: 'absolute',
    width: '100%',
    backgroundColor: '#AF0C6E',
  },
  upperHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 16,
    height: UPPER_HEADER_HEIGHT + UPPER_HEADER_PADDING_TOP,
    paddingTop: UPPER_HEADER_PADDING_TOP,
  },
  text: { fontSize: 18, fontWeight: 'bold' },
})
