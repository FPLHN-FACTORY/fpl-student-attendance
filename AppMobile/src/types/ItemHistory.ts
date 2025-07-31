export interface ItemHistory {
  requiredCheckIn: number
  checkOut: number | null
  planDateId: string
  checkIn: number | null
  factoryName: string
  lateArrival: number
  factoryId: string
  planDateEndDate: number
  shift: string
  rowNumber: number
  planDateDescription: string | null
  planDateStartDate: number
  statusAttendance: string
  requiredCheckOut: number
  type: 0 | 1
}
