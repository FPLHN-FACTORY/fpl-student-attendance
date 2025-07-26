import React, { createContext, useContext, useState } from 'react'
import { Icon, Snackbar } from 'react-native-paper'
import { Text, View, ViewStyle } from 'react-native'
import { lightenColor } from '@/utils'
import { Colors } from '@/constants/Colors'

type SnackbarType = 'success' | 'error' | 'warning' | 'default'

interface SnackbarContextType {
  showMessage: (msg: string, duration?: number) => void
  showSuccess: (msg: string, duration?: number) => void
  showError: (msg: string, duration?: number) => void
  showWarning: (msg: string, duration?: number) => void
}

const SnackbarContext = createContext<SnackbarContextType | undefined>(undefined)

export const GlobalSnackbarProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [visible, setVisible] = useState(false)
  const [message, setMessage] = useState('')
  const [type, setType] = useState<SnackbarType>('default')
  const [duration, setDuration] = useState(0)

  const show = (msg: string, type: SnackbarType = 'default', customDuration?: number) => {
    setVisible(false)
    setTimeout(() => {
      setMessage(msg)
      setType(type)
      setDuration(customDuration ?? Number.POSITIVE_INFINITY)
      setVisible(true)
    }, 100)
  }

  const iconMap: Record<SnackbarType, React.ReactNode> = {
    success: <Icon source="check-circle" color="#a5ca00" size={20} />,
    error: <Icon source="close-circle" color="#f0005c" size={20} />,
    warning: <Icon source="alert-circle" color="#ff9800" size={20} />,
    default: null,
  }

  const snackbarStyle: ViewStyle = {
    backgroundColor: Colors.primary,
  }

  const action =
    duration === Number.POSITIVE_INFINITY
      ? {
          label: 'Đóng',
          onPress: () => setVisible(false),
        }
      : undefined

  return (
    <SnackbarContext.Provider
      value={{
        showMessage: (msg, d) => show(msg, 'default', d),
        showSuccess: (msg, d) => show(msg, 'success', d),
        showError: (msg, d) => show(msg, 'error', d),
        showWarning: (msg, d) => show(msg, 'warning', d),
      }}
    >
      {children}
      <Snackbar
        style={snackbarStyle}
        theme={{ colors: { onSurface: '#ffffff' } }}
        visible={visible}
        onDismiss={() => setVisible(false)}
        duration={duration}
        action={action}
      >
        <View style={{ flexDirection: 'row', alignItems: 'center' }}>
          {iconMap[type]}
          <Text style={{ color: 'white', marginLeft: 8, marginRight: 5 }}>{message}</Text>
        </View>
      </Snackbar>
    </SnackbarContext.Provider>
  )
}

export const useGlobalSnackbar = () => {
  const ctx = useContext(SnackbarContext)
  if (!ctx) {
    throw new Error('useGlobalSnackbar phải được sử dụng trong GlobalSnackbarProvider')
  }
  return ctx
}
