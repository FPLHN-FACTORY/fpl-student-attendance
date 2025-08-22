import { DEFAULT_DATE_FORMAT } from '@/constants'
import { Colors } from '@/constants/Colors'
import { ItemCalendar } from '@/types/ItemCalendar'
import { dayOfWeek, formatDate, getShift } from '@/utils'
import { useState } from 'react'
import { Linking, StyleSheet, TouchableOpacity, View } from 'react-native'
import { Icon, Text } from 'react-native-paper'

type Props = {
  item: ItemCalendar
}

export const CollapseItemCalendar = ({ item }: Props) => {
  const [collapsed, setCollapsed] = useState(false)

  const { shiftName, shiftType, shiftColor } = getShift(item)

  return (
    <View style={styles.container}>
      <Text variant="bodySmall" style={styles.orderNumber}>
        #{item.indexs}
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
            {`${dayOfWeek(item.attendanceDayStart)}, ${formatDate(
              item.attendanceDayStart,
              DEFAULT_DATE_FORMAT,
            )}`}
          </Text>
          <Text
            variant="labelMedium"
            style={styles.time}
            numberOfLines={1}
          >{`${formatDate(item.attendanceDayStart, 'HH:mm')} - ${formatDate(item.attendanceDayEnd, 'HH:mm')}`}</Text>
        </View>
        <Icon source={collapsed ? 'chevron-right' : 'chevron-down'} size={20} />
      </TouchableOpacity>

      {!collapsed && (
        <View style={styles.itemCollapse}>
          {item.type === 0 && (
            <View style={styles.line}>
              <Text variant="labelSmall" style={styles.label}>
                Địa điểm:
              </Text>

              <Text variant="bodySmall" style={[styles.labelValue, { color: Colors.warning }]}>
                {item.location}
              </Text>
            </View>
          )}
          {item.link && (
            <View style={styles.line}>
              <Text variant="labelSmall" style={styles.label}>
                Link online:
              </Text>
              <TouchableOpacity onPress={() => item.link && Linking.openURL(item.link)}>
                <Text variant="bodySmall" style={[styles.labelValue, { color: Colors.info }]}>
                  {item.link}
                </Text>
              </TouchableOpacity>
            </View>
          )}
          <View style={styles.line}>
            <Text variant="labelSmall" style={styles.label}>
              Dự án:
            </Text>
            <Text variant="bodySmall" style={styles.labelValue}>
              {item.projectName}
            </Text>
          </View>
          <View style={styles.line}>
            <Text variant="labelSmall" style={styles.label}>
              Nhóm xưởng:
            </Text>
            <Text variant="bodySmall" style={styles.labelValue}>
              {item.factoryName}
            </Text>
          </View>
          <View style={styles.line}>
            <Text variant="labelSmall" style={styles.label}>
              Giảng viên:
            </Text>
            <Text variant="bodySmall" style={styles.labelValue}>
              {item.staffName}
            </Text>
          </View>
          {item.description != '' && (
            <View style={styles.line}>
              <Text variant="labelSmall" style={styles.label}>
                Nội dung:
              </Text>
              <Text variant="bodySmall" style={styles.labelValue}>
                {item.description}
              </Text>
            </View>
          )}
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
    flexShrink: 1,
    flex: 1,
  },
  itemShiftInfo: {
    justifyContent: 'center',
    alignItems: 'center',
    textAlign: 'center',
    backgroundColor: Colors.background,
    borderRadius: 10,
    padding: 20,
    width: 90,
    height: 60,
  },
  itemBody: {
    flex: 1,
    height: '100%',
    flexGrow: 1,
    padding: 15,
  },
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
  orderNumber: {
    position: 'absolute',
    top: 5,
    right: 10,
    opacity: 0.8,
    fontSize: 10,
  },
})
