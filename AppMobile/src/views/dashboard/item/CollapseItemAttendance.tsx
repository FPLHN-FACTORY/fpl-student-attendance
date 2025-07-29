import { Colors } from '@/constants/Colors'
import { ItemAttendance } from '@/types/ItemAttendance'
import { RootStackParamList } from '@/types/RootStackParamList'
import { formatDate, getCheckinAction, getShift, getStatus } from '@/utils'
import { useGlobalStore } from '@/utils/GlobalStore'
import { useNavigation } from '@react-navigation/native'
import { NativeStackNavigationProp } from '@react-navigation/native-stack'
import { useState } from 'react'
import { StyleSheet, TouchableOpacity, View } from 'react-native'
import { Button, Icon, Text } from 'react-native-paper'

type Props = {
  item: ItemAttendance
  onSuccess: () => void
  earlyMinuteCheckin: number
}

export const CollapseItemAttendance = ({ item, earlyMinuteCheckin, onSuccess }: Props) => {
  const navigation = useNavigation<NativeStackNavigationProp<RootStackParamList>>()

  const [collapsed, setCollapsed] = useState(false)

  const { textStatus, colorStatus } = getStatus(item)
  const { shiftName, shiftType, shiftColor } = getShift(item)
  const { text: textButton, disabled: disableButton } = getCheckinAction(item, earlyMinuteCheckin)
  const setOnCallbackAttendance = useGlobalStore((state) => state.setOnCallbackAttendance)

  const handleCheckin = (item: ItemAttendance) => {
    setOnCallbackAttendance(onSuccess)
    navigation.navigate('Attendance', {
      idPlanDate: item.idPlanDate,
    })
  }

  return (
    <View style={styles.container}>
      <Text variant="bodySmall" style={styles.orderNumber}>
        #{item.orderNumber}
      </Text>
      <TouchableOpacity onPress={() => setCollapsed(!collapsed)} style={styles.item}>
        <View style={[styles.itemShiftInfo]}>
          <Text variant="labelLarge" style={{ textAlign: 'center', color: shiftColor }}>
            {shiftName}
          </Text>
          <Text variant="labelSmall" style={{ textAlign: 'center', color: shiftColor }}>
            {shiftType}
          </Text>
        </View>
        <View style={styles.itemBody}>
          <Text variant="labelLarge" style={styles.name}>
            {item.factoryName}
          </Text>
          <Text
            variant="labelMedium"
            style={styles.time}
          >{`${formatDate(item.startDate, 'HH:mm')} - ${formatDate(item.endDate, 'HH:mm')}`}</Text>
          <Text variant="labelSmall" style={[styles.textStatus, { color: colorStatus }]}>
            {textStatus}
          </Text>
        </View>
        <Icon source={collapsed ? 'chevron-right' : 'chevron-down'} size={20} />
      </TouchableOpacity>

      {!collapsed && (
        <View style={styles.itemCollapse}>
          <View style={styles.line}>
            <Text variant="labelSmall" style={styles.label}>
              Giảng viên:
            </Text>
            <Text variant="bodySmall" style={styles.labelValue}>
              {item.teacherName}
            </Text>
          </View>
          <View style={styles.line}>
            <Text variant="labelSmall" style={styles.label}>
              Điểm danh muộn tối đa:
            </Text>
            <Text variant="bodySmall" style={[styles.labelValue, { color: Colors.warning }]}>
              {item.lateArrival} phút
            </Text>
          </View>
          <View style={styles.line}>
            <Text variant="labelSmall" style={styles.label}>
              Checkin / Checkout bù:
            </Text>
            <Text
              variant="bodySmall"
              style={[
                styles.labelValue,
                {
                  color:
                    item.totalLateAttendance > 0
                      ? item.currentLateAttendance >= item.totalLateAttendance
                        ? Colors.error
                        : Colors.success
                      : Colors.text,
                },
              ]}
            >
              {item.currentLateAttendance || 0} / {item.totalLateAttendance} lần
            </Text>
          </View>
          <Button
            icon="check"
            mode="contained"
            disabled={disableButton}
            onPress={() => handleCheckin(item)}
          >
            {textButton}
          </Button>
        </View>
      )}
      <View style={styles.shadow1} />
      <View style={styles.shadow2} />
    </View>
  )
}

const styles = StyleSheet.create({
  container: {
    justifyContent: 'center',
    backgroundColor: '#ffffff',
    borderRadius: 10,
    padding: 10,
    paddingVertical: 16,
    marginHorizontal: 15,
    marginVertical: 10,
  },
  shadow1: {
    position: 'absolute',
    bottom: -4,
    left: 10,
    backgroundColor: Colors.primary,
    width: '100%',
    height: 4,
    borderBottomStartRadius: 999,
    borderBottomEndRadius: 999,
    opacity: 0.2,
  },
  shadow2: {
    position: 'absolute',
    bottom: -8,
    left: 20,
    backgroundColor: Colors.primary,
    width: '94%',
    height: 4,
    borderBottomStartRadius: 999,
    borderBottomEndRadius: 999,
    opacity: 0.3,
  },
  item: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' },
  line: { flexDirection: 'row', alignItems: 'flex-start', marginBottom: 10 },
  label: {
    fontWeight: 'bold',
    marginRight: 10,
  },
  labelValue: {
    flex: 1,
    flexShrink: 1,
  },
  itemShiftInfo: {
    justifyContent: 'center',
    alignItems: 'center',
    textAlign: 'center',
    backgroundColor: Colors.background,
    borderRadius: 10,
    padding: 20,
    width: 100,
    height: 80,
  },
  itemBody: {},
  itemCollapse: {
    borderTopWidth: 1,
    borderColor: '#eee',
    marginTop: 10,
    padding: 10,
  },
  name: {
    color: Colors.primary,
    flexShrink: 1,
  },
  time: {
    textAlign: 'center',
    justifyContent: 'center',
    alignItems: 'center',
    borderRadius: 10,
    marginTop: 10,
    paddingHorizontal: 10,
    paddingVertical: 2,
    color: '#ffffff',
    alignSelf: 'flex-start',
    backgroundColor: Colors.primary,
    flexShrink: 1,
    width: 110,
  },
  textStatus: {
    borderRadius: 10,
    marginTop: 5,
    paddingHorizontal: 5,
    paddingVertical: 2,
  },
  orderNumber: {
    position: 'absolute',
    top: 5,
    right: 10,
    opacity: 0.8,
    fontSize: 10,
  },
})
