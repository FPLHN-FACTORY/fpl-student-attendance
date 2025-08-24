import { BlurView } from 'expo-blur'
import React from 'react'
import { View, StyleSheet } from 'react-native'

const LoadingOverlay = () => {
  return (
    <View style={styles.overlay}>
      <BlurView intensity={50} tint="light" style={StyleSheet.absoluteFill} />
    </View>
  )
}

const styles = StyleSheet.create({
  overlay: {
    ...StyleSheet.absoluteFillObject,
    justifyContent: 'center',
    alignItems: 'center',
    zIndex: 999,
  },
})

export default LoadingOverlay
