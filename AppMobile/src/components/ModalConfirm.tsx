import React from 'react'
import { StyleSheet, View } from 'react-native'
import { Button, Modal, Portal, Text } from 'react-native-paper'

type Props = {
  children: React.ReactNode
  isShow: boolean
  title?: string
  onOk?: () => void
  onCancel?: () => void
}

const ModalConfirm: React.FC<Props> = ({ children, title, isShow, onOk, onCancel }) => {
  return (
    <Portal>
      <Modal visible={isShow} contentContainerStyle={styles.container}>
        {title && <Text variant="titleMedium">{title}</Text>}
        <View style={styles.content}>{children}</View>

        <View style={styles.buttonWrapper}>
          <Button mode="text" onPress={onCancel} style={styles.button}>
            Đóng
          </Button>
          <Button mode="contained" onPress={onOk} style={styles.button}>
            Tiếp tục
          </Button>
        </View>
      </Modal>
    </Portal>
  )
}

const styles = StyleSheet.create({
  container: {
    padding: 20,
    backgroundColor: '#ffffff',
    marginHorizontal: 20,
    borderRadius: 10,
  },
  content: {
    marginVertical: 10,
  },
  buttonWrapper: {
    marginTop: 10,
    flexDirection: 'row',
    justifyContent: 'space-around',
    alignItems: 'center',
  },
  button: {
    width: '50%',
    borderRadius: 10,
  },
})

export default ModalConfirm
