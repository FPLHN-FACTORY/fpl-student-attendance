import { Colors } from '@/constants/Colors'
import { RootStackParamList } from '@/types/RootStackParamList'
import {
  countNotification,
  getFeatureViewAnimation,
  lightenColor,
  LOWER_HEADER_HEIGHT,
  UPPER_HEADER_HEIGHT,
  UPPER_HEADER_PADDING_TOP,
} from '@/utils'
import { NativeStackScreenProps } from '@react-navigation/native-stack'
import { LinearGradient } from 'expo-linear-gradient'
import { useEffect, useRef, useState } from 'react'
import { View, StyleSheet, Animated, StatusBar, Image, TouchableOpacity } from 'react-native'
import { SafeAreaView, useSafeAreaInsets } from 'react-native-safe-area-context'
import logoUDPM from '../../assets/png/logo-udpm-light.png'
import { useGlobalStore } from '@/utils/GlobalStore'
import { FEATURE_ATTENDANCE, FEATURE_CALENDAR, FEATURE_HISTORY } from '@/constants'
import HistoryTab from '@/views/dashboard/tab/HistoryTab'
import CalendarTab from '@/views/dashboard/tab/CalendarTab'
import AttendanceTab from '@/views/dashboard/tab/AttendanceTab'
import useDoubleBackPressExit from '@/components/useDoubleBackPressExit'
import { Badge } from 'react-native-paper'

type Props = NativeStackScreenProps<RootStackParamList, 'Dashboard'>

const AnimatedTouchable = Animated.createAnimatedComponent(TouchableOpacity)

type FeatureTab = typeof FEATURE_ATTENDANCE | typeof FEATURE_CALENDAR | typeof FEATURE_HISTORY

const DashboardScreen: React.FC<Props> = ({ navigation }) => {
  useDoubleBackPressExit()

  const totalNotification = useGlobalStore((state) => state.totalNotification)
  const setTotalNotification = useGlobalStore((state) => state.setTotalNotification)

  const handleChangeFeature = (feature: FeatureTab) => {
    setActiveTab(feature)
    getTotalNotification()
  }

  const getTotalNotification = () => {
    countNotification((data) => setTotalNotification(data || 0))
  }

  const studentInfo = useGlobalStore((state) => state.studentInfo)

  const [activeTab, setActiveTab] = useState<FeatureTab>(FEATURE_ATTENDANCE)

  const animatedValue = useRef(new Animated.Value(0)).current

  const attendanceViewAnimation = getFeatureViewAnimation(animatedValue, 55)
  const calendarViewAnimation = getFeatureViewAnimation(animatedValue, -5)
  const historyViewAnimation = getFeatureViewAnimation(animatedValue, -70)

  const insets = useSafeAreaInsets()

  const featureAnimation = {
    height: animatedValue.interpolate({
      inputRange: [0, 30],
      outputRange: [80, 40],
      extrapolate: 'clamp',
    }),
    width: animatedValue.interpolate({
      inputRange: [0, 30],
      outputRange: [100, 50],
      extrapolate: 'clamp',
    }),
  }

  const featureIconCircleAnimation = {
    opacity: animatedValue.interpolate({
      inputRange: [0, 25],
      outputRange: [1, 0],
      extrapolate: 'clamp',
    }),
  }
  const featureNameAnimation = {
    transform: [
      {
        scale: animatedValue.interpolate({
          inputRange: [0, 30],
          outputRange: [1, 0],
          extrapolate: 'clamp',
        }),
      },
    ],
    opacity: animatedValue.interpolate({
      inputRange: [0, 30],
      outputRange: [1, 0],
      extrapolate: 'clamp',
    }),
  }
  const featureIconAnimation = {
    opacity: animatedValue.interpolate({
      inputRange: [0, 50],
      outputRange: [0, 1],
      extrapolate: 'clamp',
    }),
  }

  const contentAnimation = {
    marginTop: animatedValue.interpolate({
      inputRange: [0, 100],
      outputRange: [0, -100],
      extrapolate: 'clamp',
    }),
    borderTopStartRadius: animatedValue.interpolate({
      inputRange: [0, 100],
      outputRange: [20, 0],
      extrapolate: 'clamp',
    }),
    borderTopEndRadius: animatedValue.interpolate({
      inputRange: [0, 100],
      outputRange: [20, 0],
      extrapolate: 'clamp',
    }),
  }

  const showMenuRef = useRef(true)
  useEffect(() => {
    getTotalNotification()
    animatedValue.addListener(({ value }) => {
      showMenuRef.current = !(value === 100)
    })
    return () => {
      animatedValue.removeAllListeners()
    }
  }, [])

  return (
    <View style={styles.container}>
      <LinearGradient
        colors={[Colors.primary, lightenColor(Colors.primary, 0.8)]}
        style={{ position: 'absolute', top: 0, left: 0, right: 0, bottom: 0 }}
      />
      <StatusBar barStyle="light-content" translucent backgroundColor="transparent" />

      <View style={{ height: UPPER_HEADER_HEIGHT + insets.top, paddingTop: insets.top }} />

      <SafeAreaView style={styles.header}>
        <View style={styles.upperHeader}>
          <Image source={logoUDPM} resizeMode="contain" style={styles.logo} />

          <TouchableOpacity style={styles.bell} onPress={() => navigation.navigate('Notification')}>
            <Image source={require('../../assets/dashboard/bell.png')} style={styles.icon24} />
            {totalNotification > 0 && <Badge style={styles.badge}>{totalNotification}</Badge>}
          </TouchableOpacity>
          <TouchableOpacity onPress={() => navigation.navigate('Detail')}>
            <Image source={{ uri: studentInfo.image }} style={styles.avatar} />
          </TouchableOpacity>
        </View>

        <View style={[styles.lowerHeader]}>
          <AnimatedTouchable
            style={[
              styles.feature,
              attendanceViewAnimation,
              featureAnimation,
              activeTab === FEATURE_ATTENDANCE && styles.featureActive,
            ]}
            onPress={() => handleChangeFeature(FEATURE_ATTENDANCE)}
            activeOpacity={0.8}
          >
            <Animated.Image
              source={require('../../assets/dashboard/attendance.png')}
              style={[styles.featureIcon, featureIconAnimation]}
            />
            <Animated.Image
              source={require('../../assets/dashboard/attendance-circle.png')}
              style={[styles.icon32, featureIconCircleAnimation]}
            />
            <Animated.Text style={[styles.featureName, featureNameAnimation]}>
              Điểm danh
            </Animated.Text>
          </AnimatedTouchable>

          <AnimatedTouchable
            style={[
              styles.feature,
              calendarViewAnimation,
              featureAnimation,
              activeTab === FEATURE_CALENDAR && styles.featureActive,
            ]}
            onPress={() => handleChangeFeature(FEATURE_CALENDAR)}
            activeOpacity={0.8}
          >
            <Animated.Image
              source={require('../../assets/dashboard/calendar.png')}
              style={[styles.featureIcon, featureIconAnimation]}
            />
            <Animated.Image
              source={require('../../assets/dashboard/calendar-circle.png')}
              style={[styles.icon32, featureIconCircleAnimation]}
            />
            <Animated.Text style={[styles.featureName, featureNameAnimation]}>
              Lịch diễn ra
            </Animated.Text>
          </AnimatedTouchable>

          <AnimatedTouchable
            style={[
              styles.feature,
              historyViewAnimation,
              featureAnimation,
              activeTab === FEATURE_HISTORY && styles.featureActive,
            ]}
            onPress={() => handleChangeFeature(FEATURE_HISTORY)}
            activeOpacity={0.8}
          >
            <Animated.Image
              source={require('../../assets/dashboard/history.png')}
              style={[styles.featureIcon, featureIconAnimation]}
            />
            <Animated.Image
              source={require('../../assets/dashboard/history-circle.png')}
              style={[styles.icon32, featureIconCircleAnimation]}
            />
            <Animated.Text style={[styles.featureName, featureNameAnimation]}>
              Lịch sử
            </Animated.Text>
          </AnimatedTouchable>
        </View>
      </SafeAreaView>

      <View style={styles.spaceForHeader} />

      <Animated.View style={[styles.scrollViewContent, contentAnimation]}>
        {activeTab === FEATURE_ATTENDANCE && (
          <AttendanceTab animatedValue={animatedValue} showMenuRef={showMenuRef} />
        )}
        {activeTab === FEATURE_CALENDAR && (
          <CalendarTab animatedValue={animatedValue} showMenuRef={showMenuRef} />
        )}
        {activeTab === FEATURE_HISTORY && (
          <HistoryTab animatedValue={animatedValue} showMenuRef={showMenuRef} />
        )}
      </Animated.View>
    </View>
  )
}

export default DashboardScreen

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: lightenColor(Colors.primary, 0.8),
  },
  logo: {
    width: 100,
    height: 40,
  },
  icon16: {
    width: 16,
    height: 16,
  },
  icon24: {
    width: 24,
    height: 24,
  },
  icon32: {
    width: 48,
    height: 48,
  },
  header: {
    position: 'absolute',
    width: '100%',
    backgroundColor: 'transparent',
  },
  upperHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 16,
    height: UPPER_HEADER_HEIGHT + UPPER_HEADER_PADDING_TOP,
  },
  featureIcon: {
    width: 16,
    height: 16,
    position: 'absolute',
    top: 8,
  },
  bell: {
    backgroundColor: 'rgba(255, 255, 255, 0.2)',
    padding: 5,
    borderRadius: 999,
    marginLeft: 'auto',
    marginHorizontal: 18,
  },
  avatar: {
    width: 40,
    height: 40,
    borderRadius: 999,
    borderWidth: 2,
    borderColor: 'rgba(255, 255, 255, 0.2)',
  },
  lowerHeader: {
    flexDirection: 'row',
    justifyContent: 'space-around',
    alignItems: 'flex-start',
    width: '100%',
    height: LOWER_HEADER_HEIGHT,
    paddingHorizontal: 16,
  },
  feature: {
    alignItems: 'center',
    width: 100,
    borderTopLeftRadius: 10,
    borderTopRightRadius: 10,
  },
  featureName: {
    fontWeight: 'bold',
    fontSize: 11,
    color: '#FFFFFF',
    marginTop: 8,
    textAlign: 'center',
    flex: 1,
    width: '100%',
  },
  featureActive: {
    borderWidth: 0,
    borderBottomWidth: 4,
    borderBottomColor: 'white',
  },
  spaceForHeader: {
    flexDirection: 'row',
    justifyContent: 'space-around',
    alignItems: 'center',
    width: '100%',
    height: LOWER_HEADER_HEIGHT,
    paddingHorizontal: 16,
  },
  scrollViewContent: {
    flex: 1,
    backgroundColor: Colors.background,
    borderTopStartRadius: 20,
    borderTopEndRadius: 20,
    overflow: 'hidden',
  },
  badge: {
    position: 'absolute',
    top: -5,
    right: -10,
  },
})
