import { create } from 'zustand'
import { UserStudent } from '../types/UserStudent'
import { Facility } from '../types/Facility'
import { Semester } from '@/types/Semester'

type StoreState = {
  lstFacilities: Facility[]
  lstSemester: Semester[]
  studentInfo: UserStudent
  setLstFacilities: (val: Facility[]) => void
  setLstSemester: (val: Semester[]) => void
  setStudentInfo: (val: UserStudent) => void
}

export const useGlobalStore = create<StoreState>((set) => ({
  lstFacilities: [],
  lstSemester: [],
  studentInfo: {},
  setLstFacilities: (val) => set({ lstFacilities: val }),
  setLstSemester: (val) => set({ lstSemester: val }),
  setStudentInfo: (val) => set({ studentInfo: val }),
}))
