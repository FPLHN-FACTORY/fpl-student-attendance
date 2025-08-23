export const SECRET_KEY = import.meta.env.VITE_SECRET_KEY

export const DEFAULT_DATE_FORMAT = 'DD/MM/YYYY'

export const DEFAULT_LATE_ARRIVAL = 15

export const PAGINATION_SIZE = 10

export const DEFAULT_PAGINATION = {
  current: 1,
  pageSize: PAGINATION_SIZE,
  total: 0,
  showSizeChanger: true,
  pageSizeOptions: ['5', '10', '20', '50', '100', '500'],
}

export const DAY_OF_WEEK = {
  1: 'Thứ 2',
  2: 'Thứ 3',
  3: 'Thứ 4',
  4: 'Thứ 5',
  5: 'Thứ 6',
  6: 'Thứ 7',
  7: 'Chủ nhật',
}

export const STATUS_PLAN_DATE_DETAIL = {
  DA_DIEN_RA: 'Đã diễn ra',
  DANG_DIEN_RA: 'Đang diễn ra',
  CHUA_DIEN_RA: 'Chưa diễn ra',
}

export const STATUS_FACILITY_IP = {
  0: 'Không áp dụng',
  1: 'Đang áp dụng',
}

export const TYPE_FACILITY_IP = {
  0: 'IPv4',
  1: 'IPv6',
  2: 'DNS Suffix',
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

export const ROLE = {
  ADMIN: 'ADMIN',
  STAFF: 'STAFF',
  TEACHER: 'TEACHER',
  STUDENT: 'STUDENT',
}

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
