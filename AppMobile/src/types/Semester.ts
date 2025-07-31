export interface Semester {
  createdAt?: number
  updatedAt?: number
  id: string
  status: 'ACTIVE' | 'INACTIVE'
  code: string
  semesterName: string
  fromDate: number
  toDate: number
  year: number
}
