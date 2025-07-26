import { NavigationContainer } from '@react-navigation/native'
import { RootStackParamList } from './types/RootStackParamList'
import { createNativeStackNavigator } from '@react-navigation/native-stack'
import LoginScreen from './views/LoginScreen'
import LoadingScreen from './views/LoadingScreen'
import { DefaultTheme, Provider as PaperProvider } from 'react-native-paper'
import { GlobalSnackbarProvider } from './components/GlobalSnackbarProvider'
import RegisterScreen from './views/RegisterScreen'
import DashboardScreen from './views/DashboardScreen'
import { Colors } from './constants/Colors'
import UpdateFaceScreen from './views/UpdateFaceScreen'
import { LoadingProvider } from './components/loading/LoadingContext'

const Stack = createNativeStackNavigator<RootStackParamList>()

const theme = {
  ...DefaultTheme,
  colors: {
    ...DefaultTheme.colors,
    ...Colors,
  },
}

export default function App() {
  return (
    <>
      <PaperProvider theme={theme}>
        <GlobalSnackbarProvider>
          <LoadingProvider>
            <NavigationContainer>
              <Stack.Navigator initialRouteName="Loading" screenOptions={{ headerShown: false }}>
                <Stack.Screen name="Loading" component={LoadingScreen} />
                <Stack.Screen name="Login" component={LoginScreen} />
                <Stack.Screen name="Register" component={RegisterScreen} />
                <Stack.Screen name="UpdateFace" component={UpdateFaceScreen} />
                <Stack.Screen name="Dashboard" component={DashboardScreen} />
              </Stack.Navigator>
            </NavigationContainer>
          </LoadingProvider>
        </GlobalSnackbarProvider>
      </PaperProvider>
    </>
  )
}
