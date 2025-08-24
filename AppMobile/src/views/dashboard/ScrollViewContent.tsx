import { Colors } from '@/constants/Colors'
import { WINDOW_HEIGHT } from '@/utils'
import { useRef, useState } from 'react'
import {
  Animated,
  NativeScrollEvent,
  NativeSyntheticEvent,
  RefreshControl,
  ScrollView,
  StyleSheet,
  View,
} from 'react-native'
import { ActivityIndicator } from 'react-native-paper'

interface Props {
  animatedValue: Animated.Value
  showMenuRef: React.RefObject<boolean>
  isLoading: boolean
  onRefresh: () => void
  onMomentumScrollEnd: (event: NativeSyntheticEvent<NativeScrollEvent>) => void
  children: React.ReactNode
}

const ScrollViewContent: React.FC<Props> = ({
  children,
  animatedValue,
  isLoading,
  onRefresh,
  showMenuRef,
  onMomentumScrollEnd,
}) => {
  const scrollViewRef = useRef<ScrollView>(null)
  const lastOffsetY = useRef(0)
  const scrollDirection = useRef<'up' | 'down'>('down')
  const [refreshing, setRefreshing] = useState(false)

  const handleScroll = (e: NativeSyntheticEvent<NativeScrollEvent>) => {
    const offsetY = e.nativeEvent.contentOffset.y
    const direction = offsetY - lastOffsetY.current > 0 ? 'down' : 'up'
    const totalHeight = e.nativeEvent.contentSize.height

    scrollDirection.current = direction
    lastOffsetY.current = offsetY

    if (totalHeight < WINDOW_HEIGHT * 1.5) {
      return
    }

    const value = Math.min(100, offsetY)

    if (!showMenuRef.current && value <= 100 && direction === 'down') return

    Animated.timing(animatedValue, {
      toValue: value,
      duration: 20,
      useNativeDriver: false,
    }).start()
  }

  const handleScrollEnd = (e: NativeSyntheticEvent<NativeScrollEvent>) => {
    const totalHeight = e.nativeEvent.contentSize.height
    if (!showMenuRef.current || totalHeight < WINDOW_HEIGHT * 1.5) return

    scrollViewRef.current?.scrollTo({
      y: scrollDirection.current === 'down' ? 100 : 0,
      animated: true,
    })
  }

  const handleRefresh = async () => {
    setRefreshing(true)
    onRefresh()
    setRefreshing(false)
  }

  return (
    <ScrollView
      ref={scrollViewRef}
      showsVerticalScrollIndicator={false}
      onScroll={handleScroll}
      onScrollEndDrag={handleScrollEnd}
      onMomentumScrollEnd={onMomentumScrollEnd}
      refreshControl={<RefreshControl refreshing={refreshing} onRefresh={handleRefresh} />}
      scrollEventThrottle={16}
      style={styles.content}
      contentContainerStyle={{ flexGrow: 1 }}
    >
      <View style={styles.scrollViewContent}>{children}</View>
      {isLoading && (
        <View style={styles.loading}>
          <ActivityIndicator size="small" color={Colors.primary} />
        </View>
      )}
    </ScrollView>
  )
}

export default ScrollViewContent

const styles = StyleSheet.create({
  content: {
    flex: 1,
  },
  scrollViewContent: {
    flex: 1,
    paddingBottom: 30,
  },
  loading: {
    paddingBottom: 40,
  },
})
