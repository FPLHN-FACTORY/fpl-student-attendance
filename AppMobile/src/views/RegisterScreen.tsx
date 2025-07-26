import { RootStackParamList } from '@/types/RootStackParamList'
import { logout } from '@/utils'
import { NativeStackScreenProps } from '@react-navigation/native-stack'
import { View, StyleSheet, Text } from 'react-native'
import { Button } from 'react-native-paper'

type Props = NativeStackScreenProps<RootStackParamList, 'Register'>

const RegisterScreen: React.FC<Props> = ({ navigation }) => {
  const handleLogout = async () => {
    await logout()
    navigation.replace('Login')
  }
  return (
    <View style={styles.container}>
      <Text>Đây là đăng ký</Text>
      <Button mode="contained" onPress={() => handleLogout()}>
        Đăng xuất
      </Button>
    </View>
  )
}

export default RegisterScreen

const styles = StyleSheet.create({
  container: { flex: 1, justifyContent: 'center', alignItems: 'center' },
  text: { fontSize: 18, fontWeight: 'bold' },
})
