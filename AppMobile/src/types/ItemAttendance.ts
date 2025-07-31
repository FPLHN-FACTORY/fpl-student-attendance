export type ItemAttendance = {
  idPlanDate: string
  factoryName: string
  currentLateAttendance: number
  endDate: number
  id: string | null
  lateArrival: number
  link: string | null
  orderNumber: number
  requiredCheckin: 0 | 1
  requiredCheckout: 0 | 1
  shift: string
  startDate: number
  status: number
  teacherName: string
  timeCheckin: number | null
  totalLateAttendance: number
  type: 0 | 1
}
