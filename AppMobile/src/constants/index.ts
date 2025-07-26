import Constants from 'expo-constants'

export const DEFAULT_DATE_FORMAT = 'DD/MM/YYYY'

export const SERVER_DOMAIN = Constants.expoConfig?.extra?.SERVER_DOMAIN

export const API_URL = SERVER_DOMAIN + '/api/v1'

export const SECURE_CONSTANT = {
  ACCESS_TOKEN: 'a_t',
  REFRESH_TOKEN: 'r_t',
}

export const PAGIONATION_SIZE = 10

export const FEATURE_ATTENDANCE = 'attendance'
export const FEATURE_CALENDAR = 'calendar'
export const FEATURE_HISTORY = 'history'

export const ATTENDANCE_STATUS = {
  NOTCHECKIN: { id: 0, name: 'Chưa checkin' },
  ABSENT: { id: 1, name: 'Vắng mặt' },
  CHECKIN: { id: 2, name: 'Đang diễn ra' },
  PRESENT: { id: 3, name: 'Có mặt' },
  NOTCHECKOUT: { id: 4, name: 'Chưa checkout' },
}

export const STATUS_REQUIRED_ATTENDANCE = {
  DISABLE: 0,
  ENABLE: 1,
}
export const STATUS_TYPE = {
  DISABLE: 0,
  ENABLE: 1,
}

export const SHIFT = {
  1: 'Ca 1',
  2: 'Ca 2',
  3: 'Ca 3',
  4: 'Ca 4',
  5: 'Ca 5',
  6: 'Ca 6',
}

export const TYPE_SHIFT = {
  0: 'Offline',
  1: 'Online',
}

export const SCHEDULE_TIME = {
  '-90': '90 ngày trước',
  '-30': '30 ngày trước',
  '-14': '14 ngày trước',
  '-7': '7 ngày trước',
  '7': '7 ngày tới',
  '14': '14 ngày tới',
  '30': '30 ngày tới',
  '90': '90 ngày tới',
}
