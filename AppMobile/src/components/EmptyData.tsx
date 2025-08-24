import React from 'react'
import { View, Text, StyleSheet } from 'react-native'
import ImageEmpty from '../../assets/svg/empty_data.svg'
import { WINDOW_HEIGHT } from '@/utils'
import { Colors } from '@/constants/Colors'

interface Props {
  text?: string
}

const EmptyData: React.FC<Props> = ({ text = 'Không có dữ liệu' }) => {
  return (
    <View style={styles.container}>
      <View style={styles.imageWrapper}>
        <ImageEmpty width="100%" height="100%" preserveAspectRatio="xMidYMid meet" />
      </View>
      <Text style={styles.text}>{text}</Text>
    </View>
  )
}

export default EmptyData

const styles = StyleSheet.create({
  container: {
    alignItems: 'center',
    justifyContent: 'center',
    paddingVertical: 20,
    marginBottom: WINDOW_HEIGHT * 0.3,
  },
  imageWrapper: {
    width: 224,
    height: 164,
    marginBottom: 10,
  },
  text: {
    color: Colors.primary,
    opacity: 0.6,
  },
})
