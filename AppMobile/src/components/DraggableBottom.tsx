import { WINDOW_HEIGHT, WINDOW_WIDTH } from '@/utils'
import React, { forwardRef, useEffect, useImperativeHandle, useRef, useState } from 'react'
import {
  View,
  TouchableOpacity,
  StyleSheet,
  Animated,
  PanResponder,
  TouchableWithoutFeedback,
  SafeAreaView,
  BackHandler,
} from 'react-native'
import { Portal, Text } from 'react-native-paper'
import Ionicons from 'react-native-vector-icons/Ionicons'

const BOTTOM_SHEET_MAX_HEIGHT = WINDOW_HEIGHT * 0.94
const BOTTOM_SHEET_MIN_HEIGHT = WINDOW_HEIGHT * 0
const MAX_UPWARD_TRANSLATE_Y = BOTTOM_SHEET_MIN_HEIGHT - BOTTOM_SHEET_MAX_HEIGHT
const MAX_DOWNWARD_TRANSLATE_Y = 0
const DRAG_THRESHOLD = 50

export type DraggableBottomRef = {
  close: () => void
}

type Props = {
  children: React.ReactNode
  isShow: boolean
  title?: string
  onClose: () => void
}

const DraggableBottom = forwardRef<DraggableBottomRef, Props>(
  ({ children, isShow, title, onClose }, ref) => {
    const animatedValue = useRef(new Animated.Value(0)).current
    const opacityValue = useRef(new Animated.Value(1)).current
    const lastGestureDy = useRef(0)

    useImperativeHandle(ref, () => ({
      close: () => {
        springAnimation('down')
      },
    }))

    useEffect(() => {
      springAnimation(isShow ? 'up' : 'down')
      const onBackPress = () => {
        if (isShow) {
          springAnimation('down')
          return true
        }
        return false
      }

      const backHandler = BackHandler.addEventListener('hardwareBackPress', onBackPress)

      return () => backHandler.remove()
    }, [isShow])

    const panResponder = useRef(
      PanResponder.create({
        onStartShouldSetPanResponder: () => true,
        onPanResponderGrant: () => {
          animatedValue.setOffset(lastGestureDy.current)
        },
        onPanResponderMove: (e, gesture) => {
          animatedValue.setValue(gesture.dy)
        },
        onPanResponderRelease: (e, gesture) => {
          animatedValue.flattenOffset()
          lastGestureDy.current += gesture.dy

          const dy = gesture.dy

          if (Math.abs(dy) < DRAG_THRESHOLD) {
            return
          }

          if (dy > 0) {
            springAnimation('down')
          } else {
            springAnimation('up')
          }
        },
      }),
    ).current

    const springAnimation = (direction: 'up' | 'down') => {
      lastGestureDy.current =
        direction === 'down' ? MAX_DOWNWARD_TRANSLATE_Y : MAX_UPWARD_TRANSLATE_Y
      Animated.spring(animatedValue, {
        toValue: lastGestureDy.current,
        useNativeDriver: true,
      }).start()
      if (direction === 'down') {
        Animated.timing(opacityValue, {
          toValue: 0,
          duration: 200,
          useNativeDriver: true,
        }).start()
        setTimeout(() => {
          opacityValue.setValue(1)
          onClose()
        }, 200)
      }
    }

    const bottomSheetAnimation = {
      transform: [
        {
          translateY: animatedValue.interpolate({
            inputRange: [MAX_UPWARD_TRANSLATE_Y, MAX_DOWNWARD_TRANSLATE_Y],
            outputRange: [MAX_UPWARD_TRANSLATE_Y, MAX_DOWNWARD_TRANSLATE_Y],
            extrapolate: 'clamp',
          }),
        },
      ],
    }

    return (
      <Portal>
        <SafeAreaView style={[styles.container, { display: isShow ? 'flex' : 'none' }]}>
          <TouchableWithoutFeedback onPress={() => springAnimation('down')}>
            <View style={styles.backdrop} />
          </TouchableWithoutFeedback>

          <Animated.View
            style={[styles.bottomSheet, bottomSheetAnimation, { opacity: opacityValue }]}
          >
            <View style={styles.draggableArea} {...panResponder.panHandlers}>
              <View style={styles.dragHandle} />
              <View style={{ display: 'flex', alignItems: 'center', width: '100%' }}>
                <Text style={styles.title}>{title}</Text>
                <TouchableOpacity onPress={() => springAnimation('down')} style={styles.closeIcon}>
                  <Ionicons name="close" size={24} color="#333" />
                </TouchableOpacity>
              </View>
            </View>
            {children}
          </Animated.View>
        </SafeAreaView>
      </Portal>
    )
  },
)

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignSelf: 'stretch',
    position: 'absolute',
    width: WINDOW_WIDTH,
    height: WINDOW_HEIGHT + 50,
    top: 0,
    left: 0,
    bottom: 0,
    zIndex: 9999,
  },
  backdrop: {
    flex: 1,
    height: WINDOW_HEIGHT + 50,
    position: 'absolute',
    top: 0,
    left: 0,
    right: 0,
    bottom: 0,
    backgroundColor: 'rgba(0, 0, 0, 0.4)',
  },
  title: {
    color: '#333',
    marginTop: 15,
    marginBottom: 15,
    fontWeight: 'bold',
    fontSize: 14,
  },
  closeIcon: {
    position: 'absolute',
    top: 10,
    right: 10,
    zIndex: 1,
  },
  bottomSheet: {
    position: 'absolute',
    width: '100%',
    maxHeight: BOTTOM_SHEET_MAX_HEIGHT,
    bottom: BOTTOM_SHEET_MIN_HEIGHT - BOTTOM_SHEET_MAX_HEIGHT,
    backgroundColor: 'white',
    borderTopLeftRadius: 15,
    borderTopRightRadius: 15,
    overflow: 'hidden',
  },
  draggableArea: {
    width: '100%',
    minHeight: 40,
    alignSelf: 'center',
    alignItems: 'center',
    backgroundColor: '#eee',
  },
  dragHandle: {
    width: 60,
    height: 4,
    backgroundColor: '#d3d3d3',
    borderRadius: 10,
    marginTop: 5,
  },
})

export default DraggableBottom
