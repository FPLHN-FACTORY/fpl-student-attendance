import { jwtDecode } from 'jwt-decode'
import { Animated, Dimensions } from 'react-native'
import { deleteToken } from './secureStorageUtils'
import {
  ATTENDANCE_STATUS,
  DEFAULT_DATE_FORMAT,
  SECURE_CONSTANT,
  STATUS_REQUIRED_ATTENDANCE,
  TYPE_SHIFT,
} from '@/constants'
import NetInfo from '@react-native-community/netinfo'
import { ItemAttendance } from '@/types/ItemAttendance'
import { Colors } from '@/constants/Colors'
import { ItemCalendar } from '@/types/ItemCalendar'
import { ItemHistory } from '@/types/ItemHistory'
import { Semester } from '@/types/Semester'
import requestAPI from '@/services/requestApiService'
import { API_ROUTES_NOTIFICATION } from '@/constants/ApiRoutes'
import * as FileSystem from 'expo-file-system'
import cryptoJS from 'crypto-js'

export const UPPER_HEADER_HEIGHT = 64
export const UPPER_HEADER_PADDING_TOP = 4
export const LOWER_HEADER_HEIGHT = 100

export const { width: WINDOW_WIDTH, height: WINDOW_HEIGHT } = Dimensions.get('window')

export const getFeatureViewAnimation = (animatedValue: Animated.Value, outputX: number) => {
  const TRANSLATE_X_INPUT_RANGE = [0, 60]
  const translateY = {
    translateY: animatedValue.interpolate({
      inputRange: [0, 100],
      outputRange: [0, -50],
      extrapolate: 'clamp',
    }),
  }
  return {
    transform: [
      {
        translateX: animatedValue.interpolate({
          inputRange: TRANSLATE_X_INPUT_RANGE,
          outputRange: [0, outputX],
          extrapolate: 'clamp',
        }),
      },
      translateY,
    ],
  }
}

export const decodeBase64Utf8 = (base64: string): string => {
  const fixedBase64 = base64.replace(/ /g, '+')
  return decodeURIComponent(
    Array.prototype.map
      .call(atob(fixedBase64), (c: string) => '%' + c.charCodeAt(0).toString(16).padStart(2, '0'))
      .join(''),
  )
}

export const decodeToken = (tokenData: string) => {
  try {
    return jwtDecode(tokenData)
  } catch (error) {
    return null
  }
}

export const dayOfWeek = (timestamp: number): string => {
  const inputDate = new Date(timestamp)
  const now = new Date()

  const inputDateOnly = new Date(inputDate.getFullYear(), inputDate.getMonth(), inputDate.getDate())
  const nowDateOnly = new Date(now.getFullYear(), now.getMonth(), now.getDate())

  const diffInDays = (inputDateOnly.getTime() - nowDateOnly.getTime()) / (1000 * 60 * 60 * 24)

  const daysOfWeek = ['Chủ Nhật', 'Thứ Hai', 'Thứ Ba', 'Thứ Tư', 'Thứ Năm', 'Thứ Sáu', 'Thứ Bảy']
  const dayName = daysOfWeek[inputDate.getDay()]

  if (diffInDays === 0) return 'Hôm nay'
  if (diffInDays === -1) return 'Hôm qua'
  if (diffInDays === 1) return 'Ngày mai'

  return dayName
}

export const formatDate = (timestamp: number | null, format?: string): string => {
  if (!timestamp) return ''

  const date = new Date(timestamp)
  const fmt = format || DEFAULT_DATE_FORMAT

  const map: Record<string, string | number> = {
    YYYY: date.getFullYear(),
    yyyy: date.getFullYear(),
    MM: String(date.getMonth() + 1).padStart(2, '0'),
    dd: String(date.getDate()).padStart(2, '0'),
    DD: String(date.getDate()).padStart(2, '0'),
    HH: String(date.getHours()).padStart(2, '0'),
    mm: String(date.getMinutes()).padStart(2, '0'),
    ss: String(date.getSeconds()).padStart(2, '0'),
  }

  return fmt.replace(/YYYY|yyyy|MM|dd|DD|HH|mm|ss/g, (matched) => String(map[matched]))
}

export const hasInternet = async () => {
  const state = await NetInfo.fetch()
  return state.isConnected
}

export const logout = async () => {
  await deleteToken(SECURE_CONSTANT.ACCESS_TOKEN)
  await deleteToken(SECURE_CONSTANT.REFRESH_TOKEN)
}

export const lightenColor = (hex: string, percent: number): string => {
  const cleanHex = hex.replace('#', '')
  if (cleanHex.length !== 6) throw new Error('Invalid HEX')

  const num = parseInt(cleanHex, 16)
  const r = Math.min(255, (num >> 16) + Math.round(255 * percent))
  const g = Math.min(255, ((num >> 8) & 0xff) + Math.round(255 * percent))
  const b = Math.min(255, (num & 0xff) + Math.round(255 * percent))

  return `#${r.toString(16).padStart(2, '0')}${g
    .toString(16)
    .padStart(2, '0')}${b.toString(16).padStart(2, '0')}`
}

export const darkenColor = (hex: string, percent: number): string => {
  const cleanHex = hex.replace('#', '')
  if (cleanHex.length !== 6) throw new Error('Invalid HEX')

  const num = parseInt(cleanHex, 16)
  const r = Math.max(0, (num >> 16) - Math.round(255 * percent))
  const g = Math.max(0, ((num >> 8) & 0xff) - Math.round(255 * percent))
  const b = Math.max(0, (num & 0xff) - Math.round(255 * percent))

  return `#${r.toString(16).padStart(2, '0')}${g
    .toString(16)
    .padStart(2, '0')}${b.toString(16).padStart(2, '0')}`
}

export const getStatus = (item: ItemAttendance) => {
  const { requiredCheckin, requiredCheckout, status, startDate } = item

  const isEnableCheckin = requiredCheckin === STATUS_REQUIRED_ATTENDANCE.ENABLE
  const isEnableCheckout = requiredCheckout === STATUS_REQUIRED_ATTENDANCE.ENABLE

  let text = ''
  let color = Colors.warning

  if (isEnableCheckin && isEnableCheckout) {
    if (status === ATTENDANCE_STATUS.CHECKIN.id) {
      text =
        Date.now() <= startDate
          ? ATTENDANCE_STATUS.CHECKIN.name
          : ATTENDANCE_STATUS.NOTCHECKOUT.name
      color = Date.now() <= startDate ? Colors.primary : Colors.error
    } else if (status === ATTENDANCE_STATUS.ABSENT.id) {
      text = ATTENDANCE_STATUS.ABSENT.name
      color = Colors.error
    } else if (status === ATTENDANCE_STATUS.PRESENT.id) {
      text = ATTENDANCE_STATUS.PRESENT.name
      color = Colors.success
    } else {
      text = ATTENDANCE_STATUS.NOTCHECKIN.name
    }
  } else if (isEnableCheckin) {
    if (status === ATTENDANCE_STATUS.PRESENT.id) {
      text = ATTENDANCE_STATUS.PRESENT.name
      color = Colors.success
    } else {
      text = ATTENDANCE_STATUS.NOTCHECKIN.name
    }
  } else {
    if (status === ATTENDANCE_STATUS.PRESENT.id) {
      text = ATTENDANCE_STATUS.PRESENT.name
      color = Colors.success
    } else {
      text = ATTENDANCE_STATUS.NOTCHECKOUT.name
    }
  }

  return {
    textStatus: text,
    colorStatus: color,
  }
}

export const getShift = (item: ItemAttendance | ItemCalendar | ItemHistory) => {
  const { shift, type } = item
  const shiftList = shift
    .split(',')
    .map((o) => Number(o))
    .join(', ')

  return {
    shiftName: `Ca ${shiftList}`,
    shiftType: TYPE_SHIFT[type],
    shiftColor: type === 1 ? Colors.info : Colors.primary,
  }
}

export const getCheckinAction = (record: ItemAttendance, DEFAULT_EARLY_MINUTE_CHECKIN: number) => {
  const make = (text: string, disabled: boolean, color: string) => ({ text, disabled, color })
  const now = Date.now()
  const canLate = record.totalLateAttendance > record.currentLateAttendance
  const isTooEarly = now < record.startDate - DEFAULT_EARLY_MINUTE_CHECKIN * 60 * 1000
  const isTooLateCheckin = now > record.startDate + record.lateArrival * 60 * 1000
  const isTooLateCheckout = now > record.endDate + record.lateArrival * 60 * 1000
  const isValidCheckout =
    now >= record.endDate && now <= record.endDate + record.lateArrival * 60 * 1000

  const reqCheckin = record.requiredCheckin !== STATUS_REQUIRED_ATTENDANCE.DISABLE
  const reqCheckout = record.requiredCheckout !== STATUS_REQUIRED_ATTENDANCE.DISABLE

  const isCanCompensateCheckin = canLate && now <= record.endDate
  const isCanCompensateCheckout =
    canLate && now <= record.endDate + record.lateArrival * 60 * 1000 * 2

  if (
    record.requiredCheckin === STATUS_REQUIRED_ATTENDANCE.ENABLE &&
    record.requiredCheckout === STATUS_REQUIRED_ATTENDANCE.ENABLE
  ) {
    if (record.status === ATTENDANCE_STATUS.NOTCHECKIN.id) {
      if (isTooEarly) {
        return make('Chưa đến giờ checkin', true, Colors.primary)
      }
      if (isTooLateCheckin) {
        if (isCanCompensateCheckin) {
          return make('Checkin bù', false, Colors.warning)
        } else {
          return make('Đã quá giờ checkin', true, Colors.error)
        }
      }
      return make('Checkin đầu giờ', false, Colors.primary)
    }

    if (record.status === ATTENDANCE_STATUS.CHECKIN.id) {
      if (isTooLateCheckout) {
        if (isCanCompensateCheckout) {
          return make('Checkout bù', false, Colors.warning)
        } else {
          return make('Đã quá giờ checkout', true, Colors.error)
        }
      }
      if (isValidCheckout) {
        return make('Checkout cuối giờ', false, Colors.success)
      }
      return make(
        `Checkin đầu giờ ${formatDate(record.timeCheckin, 'dd/MM/yyyy HH:mm')}`,
        true,
        Colors.warning,
      )
    }

    if (record.status === ATTENDANCE_STATUS.PRESENT.id) {
      return make('Đã điểm danh', true, Colors.success)
    }

    return make('Chưa điểm danh', true, Colors.error)
  }

  if (!reqCheckin && reqCheckout) {
    if (record.status === ATTENDANCE_STATUS.PRESENT.id) {
      return make('Đã điểm danh', true, Colors.success)
    }
    if (isTooLateCheckout) {
      if (isCanCompensateCheckout) {
        return make('Checkout bù', false, Colors.warning)
      } else {
        return make('Đã quá giờ checkout', true, Colors.error)
      }
    }
    if (isValidCheckout) {
      return make('Checkout cuối giờ', false, Colors.success)
    }
    return make('Chưa đến giờ checkout', true, Colors.warning)
  }

  if (reqCheckin && !reqCheckout) {
    if (record.status === ATTENDANCE_STATUS.PRESENT.id) {
      return make('Đã điểm danh', true, Colors.success)
    }
    if (isTooEarly) {
      return make('Chưa đến giờ checkin', true, Colors.warning)
    }
    if (isTooLateCheckin) {
      if (isCanCompensateCheckin) {
        return make('Checkin bù', false, Colors.warning)
      } else {
        return make('Đã quá giờ checkin', true, Colors.error)
      }
    }
    return make('Checkin đầu giờ', false, Colors.info)
  }

  if (isTooEarly) {
    return make('Chưa đến giờ điểm danh', true, Colors.primary)
  }
  if (isTooLateCheckin) {
    return make('Đã quá giờ điểm danh', true, Colors.error)
  }
  return make('Điểm danh', false, Colors.primary)
}

export const getTimeRange = (day: number) => {
  const now = Date.now()
  const offset = (day || 0) * 24 * 60 * 60 * 1000
  if (day >= 0) {
    return { now, max: now + offset }
  } else {
    return { now: now + offset, max: now }
  }
}

export const getCurrentSemester = (semesters: Semester[]) => {
  const now = Date.now()

  let currentSemester = semesters.find((item) => now >= item.fromDate && now <= item.toDate)

  if (!currentSemester) {
    currentSemester = semesters.reduce((closest, item) => {
      const diffCurrent = Math.abs(item.fromDate - now)
      const diffClosest = Math.abs(closest.fromDate - now)
      return diffCurrent < diffClosest ? item : closest
    })
  }
  return currentSemester
}

export const getCheckinCheckout = (item: ItemHistory) => {
  let txtCheckin = undefined
  let colorCheckin = undefined
  let txtCheckout = undefined
  let colorCheckout = undefined

  if (item.requiredCheckIn === STATUS_REQUIRED_ATTENDANCE.ENABLE) {
    if (!item.checkIn) {
      colorCheckin = Colors.error
      txtCheckin = 'Chưa checkin'
    } else {
      colorCheckin = Colors.success
      txtCheckin = formatDate(item.checkIn, 'dd/MM/yyyy HH:mm')
    }
  } else {
    colorCheckin = Colors.textSecondary
    txtCheckin = 'Không yêu cầu'
  }

  if (item.requiredCheckOut === STATUS_REQUIRED_ATTENDANCE.ENABLE) {
    if (!item.checkOut) {
      colorCheckout = Colors.error
      txtCheckout = 'Chưa checkout'
    } else {
      colorCheckout = Colors.success
      txtCheckout = formatDate(item.checkOut, 'dd/MM/yyyy HH:mm')
    }
  } else {
    colorCheckout = Colors.textSecondary
    txtCheckout = 'Không yêu cầu'
  }
  return { txtCheckin, colorCheckin, txtCheckout, colorCheckout }
}

export const getStatusAttendance = (item: ItemHistory) => {
  let txtStatus = undefined
  let colorStatus = undefined
  if (item.statusAttendance === 'CHUA_DIEN_RA') {
    colorStatus = Colors.warning
    txtStatus = 'Chưa diễn ra'
  } else if (item.statusAttendance === 'DANG_DIEN_RA') {
    colorStatus = Colors.primary
    txtStatus = 'Đang diễn ra'
  } else {
    const isPresent = item.statusAttendance === 'CO_MAT'
    txtStatus = isPresent ? 'Có mặt' : 'Vắng mặt'
    colorStatus = isPresent ? Colors.success : Colors.error
  }
  return { txtStatus, colorStatus }
}

export const capitalizeFirstLetter = (str: string | undefined) => {
  if (!str) return ''
  return str.charAt(0).toUpperCase() + str.slice(1)
}

export const capitalizeWords = (str: string | undefined) => {
  if (!str) return ''
  return str
    .toLowerCase()
    .split(' ')
    .map((word) => word.charAt(0).toUpperCase() + word.slice(1))
    .join(' ')
}

export const countNotification = (callback: (response: number) => void) => {
  requestAPI.get(API_ROUTES_NOTIFICATION.FETCH_COUNT).then(({ data: response }) => {
    callback(response?.data || 0)
  })
}

export const base64ToFile = async (base64: string) => {
  const [header, data] = base64.split(',')
  const contentType = header.split(':')[1].split(';')[0]
  const ext = contentType.split('/')[1] || 'jpg'
  const filename = `upload_${Date.now()}.${ext}`
  const path = FileSystem.cacheDirectory + filename

  await FileSystem.writeAsStringAsync(path, data, { encoding: FileSystem.EncodingType.Base64 })
  const sizeInBytes =
    Math.ceil((data.length * 3) / 4) - (data.endsWith('==') ? 2 : data.endsWith('=') ? 1 : 0)

  return {
    uri: path,
    type: contentType,
    name: filename,
    size: sizeInBytes,
  }
}

export const unlinkBase64ToFile = async (uri: string) => {
  try {
    await FileSystem.deleteAsync(uri, { idempotent: true })
  } catch {}
}

export const generateSignature = (key: string, data: any) => {
  const timestamp = Math.floor(Date.now() / 1000)
  const toSign = data + '|' + timestamp
  return cryptoJS.HmacSHA256(toSign, key).toString(cryptoJS.enc.Hex)
}
