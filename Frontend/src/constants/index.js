export const DEFAULT_DATE_FORMAT = 'DD/MM/YYYY'

export const DEFAULT_LATE_ARRIVAL = 15

export const DEFAULT_MAX_LATE_ARRIVAL = 60

export const PAGINATION_SIZE = 5

export const DEFAULT_PAGINATION = {
  current: 1,
  pageSize: PAGINATION_SIZE,
  total: 0,
  showSizeChanger: true,
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
  CHUA_DIEN_RA: 'Chưa diễn ra',
}

export const STATUS_FACILITY_IP = {
  0: 'Không áp dụng',
  1: 'Đang áp dụng',
}

export const TYPE_FACILITY_IP = {
  0: 'IPv4',
  1: 'IPv6',
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
  NOTCHECKIN: { id: 0, name: 'Chưa điểm danh' },
  ABSENT: { id: 1, name: 'Vắng mặt' },
  CHECKIN: { id: 2, name: 'Đã checkin' },
  PRESENT: { id: 3, name: 'Có mặt' },
}
