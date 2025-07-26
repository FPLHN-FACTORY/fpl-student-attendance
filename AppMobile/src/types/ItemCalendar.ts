export type ItemCalendar = {
  id: string
  type: 0 | 1
  shift: string
  staffName: string
  factoryName: string
  attendanceDayStart: number
  attendanceDayEnd: number
  indexs: number
  projectName: string
  subjectName: string
  description?: string | null
  location?: string | null
  link?: string | null
  subjectCode?: string | null
}
