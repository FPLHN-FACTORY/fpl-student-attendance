import { create } from 'zustand'
import { persist, createJSONStorage } from 'zustand/middleware'
import AsyncStorage from '@react-native-async-storage/async-storage'

import { UserStudent } from '../types/UserStudent'
import { Facility } from '../types/Facility'
import { Semester } from '@/types/Semester'
import { DataWebcam } from '@/types/DataWebcam'

type StoreState = {
  lstFacilities: Facility[]
  lstSemester: Semester[]
  studentInfo: UserStudent
  dataWebcam: DataWebcam
  totalNotification: number
  onCallbackAttendance: () => void
  setLstFacilities: (val: Facility[]) => void
  setLstSemester: (val: Semester[]) => void
  setStudentInfo: (val: UserStudent) => void
  setDataWebcam: (val: DataWebcam) => void
  setOnCallbackAttendance: (val: () => void) => void
  setTotalNotification: (val: number) => void
}

export const useGlobalStore = create<StoreState>()(
  persist(
    (set) => ({
      lstFacilities: [],
      lstSemester: [],
      studentInfo: {} as UserStudent,
      dataWebcam: {
        image: '',
      },
      totalNotification: 0,
      onCallbackAttendance: () => {},
      setLstFacilities: (val) => set({ lstFacilities: val }),
      setLstSemester: (val) => set({ lstSemester: val }),
      setStudentInfo: (val) => set({ studentInfo: val }),
      setDataWebcam: (val) => set({ dataWebcam: val }),
      setOnCallbackAttendance: (val) => set({ onCallbackAttendance: val }),
      setTotalNotification: (val) => set({ totalNotification: val }),
    }),
    {
      name: 'global-store',
      storage: createJSONStorage(() => AsyncStorage),
    },
  ),
)
