import { create } from 'zustand'
import { UserStudent } from '../types/UserStudent'
import { Facility } from '../types/Facility'
import { Semester } from '@/types/Semester'
import { DataWebcam } from '@/types/DataWebcam'

type StoreState = {
  lstFacilities: Facility[]
  lstSemester: Semester[]
  studentInfo: UserStudent
  dataWebcam: DataWebcam
  onCallbackAttendance: () => void
  setLstFacilities: (val: Facility[]) => void
  setLstSemester: (val: Semester[]) => void
  setStudentInfo: (val: UserStudent) => void
  setDataWebcam: (val: DataWebcam) => void
  setOnCallbackAttendance: (val: () => void) => void
}

export const useGlobalStore = create<StoreState>((set) => ({
  lstFacilities: [],
  lstSemester: [],
  studentInfo: {},
  dataWebcam: {
    descriptors: [],
    image: '',
  },
  onCallbackAttendance: () => {},
  setLstFacilities: (val) => set({ lstFacilities: val }),
  setLstSemester: (val) => set({ lstSemester: val }),
  setStudentInfo: (val) => set({ studentInfo: val }),
  setDataWebcam: (val) => set({ dataWebcam: val }),
  setOnCallbackAttendance: (val) => set({ onCallbackAttendance: val }),
}))
