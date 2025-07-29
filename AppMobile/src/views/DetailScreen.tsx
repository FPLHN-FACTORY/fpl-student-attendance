import { Colors } from '@/constants/Colors'
import { RootStackParamList } from '@/types/RootStackParamList'
import { capitalizeWords, getCurrentSemester, logout, WINDOW_WIDTH } from '@/utils'
import { NativeStackScreenProps } from '@react-navigation/native-stack'
import { LinearGradient } from 'expo-linear-gradient'
import { View, StyleSheet, StatusBar, ScrollView, Image } from 'react-native'
import { useSafeAreaInsets } from 'react-native-safe-area-context'
import { useGlobalStore } from '@/utils/GlobalStore'
import { Button, Icon, IconButton, Text } from 'react-native-paper'
import { useEffect, useRef, useState } from 'react'
import ModalConfirm from '@/components/ModalConfirm'
import Select from '@/components/form/Select'
import { PieChart } from 'react-native-chart-kit'
import requestAPI from '@/services/requestApiService'
import { API_ROUTES } from '@/constants/ApiRoutes'
import { useGlobalSnackbar } from '@/components/GlobalSnackbarProvider'
import useBackPressGoBack from '@/components/useBackPressGoBack'

type Props = NativeStackScreenProps<RootStackParamList, 'Detail'>

const DetailScreen: React.FC<Props> = ({ navigation }) => {
  useBackPressGoBack(navigation)

  const { showError } = useGlobalSnackbar()

  const [visible, setVisible] = useState(false)
  const lstSemester = useRef(useGlobalStore((state) => state.lstSemester)).current
  const [semester, setSemester] = useState(getCurrentSemester(lstSemester)?.id)

  const [statistic, setStatistic] = useState({
    factory: 0,
    complete: 0,
    cancel: 0,
    process: 0,
    totalAbsent: 0,
    totalPresent: 0,
  })

  const handleLogout = async () => {
    await logout()
    navigation.replace('Login')
  }

  const handleBack = () => {
    navigation.goBack()
  }

  useEffect(() => {
    requestAPI
      .get(`${API_ROUTES.FETCH_DATA_STUDENT_STATISTICS}`, {
        params: {
          idSemester: semester,
        },
      })
      .then(({ data: response }) => {
        const attendanceChartResponses = response?.data?.attendanceChartResponses || {}
        const stdStatisticsStatResponse = response?.data?.stdStatisticsStatResponse || {}
        setStatistic((prev) => ({
          ...prev,
          totalAbsent: attendanceChartResponses?.totalAbsent || 0,
          totalPresent:
            attendanceChartResponses?.totalPresent ||
            attendanceChartResponses?.totalShift - attendanceChartResponses?.totalAbsent ||
            0,
          factory: stdStatisticsStatResponse?.factory || 0,
          complete: stdStatisticsStatResponse?.pass || 0,
          cancel: stdStatisticsStatResponse?.fail || 0,
          process: stdStatisticsStatResponse?.process || 0,
        }))
      })
      .catch((error) => {
        showError(error.response?.data?.message || 'Không thể tải dữ liệu thống kê', 2000)
      })
  }, [semester])

  const studentInfo = useGlobalStore((state) => state.studentInfo)

  const insets = useSafeAreaInsets()

  const data = [
    {
      name: 'Vắng mặt',
      population: statistic.totalAbsent,
      color: Colors.error,
      legendFontColor: '#7F7F7F',
      legendFontSize: 14,
    },
    {
      name: 'Có mặt',
      population: statistic.totalPresent,
      color: Colors.primary,
      legendFontColor: '#7F7F7F',
      legendFontSize: 14,
    },
  ]

  return (
    <View style={styles.container}>
      <LinearGradient
        colors={[Colors.primary, '#ffffff', '#ffffff', '#ffffff']}
        style={{ position: 'absolute', top: 0, left: 0, right: 0, bottom: 0 }}
      />
      <StatusBar barStyle="light-content" translucent backgroundColor="transparent" />

      <View style={{ paddingTop: insets.top }} />

      <ModalConfirm
        isShow={visible}
        title="Đăng xuất"
        onOk={handleLogout}
        onCancel={() => setVisible(false)}
      >
        <Text>Bạn có muốn kết thúc phiên đăng nhập này không?</Text>
      </ModalConfirm>

      <ScrollView style={styles.container}>
        <IconButton
          icon="chevron-left"
          size={30}
          iconColor="#ffffff"
          onPress={handleBack}
          style={styles.buttonBack}
        />
        <View style={styles.header}>
          <View style={styles.avatarWrapper}>
            <Image source={{ uri: studentInfo.image }} style={styles.avatar} />
          </View>
          <View style={styles.info}>
            <Text variant="titleMedium">{capitalizeWords(studentInfo.name)}</Text>
            <Text variant="titleSmall" style={styles.email}>
              {studentInfo.email}
            </Text>
          </View>
          <Button mode="contained" onPress={() => setVisible(true)} style={{ marginTop: 20 }}>
            Đăng xuất
          </Button>
        </View>
        <View style={styles.containerStatistic}>
          <Select
            value={semester}
            onChange={setSemester}
            options={lstSemester.map((o) => ({
              label: o.code,
              value: o.id,
            }))}
            placeholder="-- Chọn 1 học kỳ --"
            label="Thống kê theo học kỳ"
          />

          <View style={styles.statisticWrapper}>
            <View style={styles.statistic}>
              <View>
                <Text variant="labelSmall" style={{ opacity: 0.5 }}>
                  Nhóm xưởng
                </Text>
                <Text variant="labelLarge">{statistic.factory}</Text>
              </View>
              <View style={styles.buttonIconWrapper}>
                <Icon source="account-group" color="#ffffff" size={20} />
              </View>
            </View>
            <View style={styles.statistic}>
              <View>
                <Text variant="labelSmall" style={{ opacity: 0.5 }}>
                  Hoàn thành
                </Text>
                <Text variant="labelLarge">{statistic.complete}</Text>
              </View>
              <View style={styles.buttonIconWrapper}>
                <Icon source="check-all" color="#ffffff" size={20} />
              </View>
            </View>
          </View>

          <View style={styles.statisticWrapper}>
            <View style={styles.statistic}>
              <View>
                <Text variant="labelSmall" style={{ opacity: 0.5 }}>
                  Không đạt
                </Text>
                <Text variant="labelLarge">{statistic.cancel}</Text>
              </View>
              <View style={styles.buttonIconWrapper}>
                <Icon source="close-thick" color="#ffffff" size={20} />
              </View>
            </View>
            <View style={styles.statistic}>
              <View>
                <Text variant="labelSmall" style={{ opacity: 0.5 }}>
                  Đang diễn ra
                </Text>
                <Text variant="labelLarge">{statistic.process}</Text>
              </View>
              <View style={styles.buttonIconWrapper}>
                <Icon source="sync" color="#ffffff" size={20} />
              </View>
            </View>
          </View>

          <View style={styles.chartContainer}>
            <PieChart
              data={data}
              width={WINDOW_WIDTH * 0.7}
              height={120}
              accessor="population"
              backgroundColor="transparent"
              paddingLeft="15"
              absolute
              chartConfig={{
                color: () => `black`,
              }}
            />
          </View>
        </View>
      </ScrollView>
    </View>
  )
}

export default DetailScreen

const styles = StyleSheet.create({
  container: {
    flex: 1,
    paddingTop: 10,
    paddingHorizontal: 5,
  },
  containerStatistic: {
    marginTop: 20,
    borderRadius: 10,
    backgroundColor: '#ffffff',
    paddingHorizontal: 15,
    paddingVertical: 10,
  },
  buttonBack: {},
  header: {
    borderRadius: 20,
    backgroundColor: '#ffffff',
    padding: 10,
  },
  avatarWrapper: {
    flex: 1,
    flexDirection: 'row',
    justifyContent: 'center',
  },
  avatar: {
    marginTop: -40,
    height: 80,
    width: 80,
    borderColor: '#ffffff',
    borderWidth: 4,
    borderRadius: 80 / 2,
  },
  info: {
    justifyContent: 'center',
    alignItems: 'center',
  },
  email: {
    opacity: 0.5,
    fontSize: 11,
  },
  title: {
    marginVertical: 10,
    color: Colors.primary,
  },
  statisticWrapper: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-around',
    gap: 10,
    marginTop: 10,
  },
  statistic: {
    flex: 1,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    backgroundColor: Colors.background,
    padding: 10,
    borderRadius: 10,
  },
  buttonIconWrapper: {
    backgroundColor: Colors.primary,
    borderRadius: 10,
    padding: 10,
  },
  chartContainer: {
    flex: 1,
    backgroundColor: Colors.background,
    padding: 10,
    borderRadius: 10,
    marginTop: 20,
  },
})
