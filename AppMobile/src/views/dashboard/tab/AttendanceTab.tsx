import { Button, Text } from 'react-native-paper'
import { useLoading } from '../../../components/loading/LoadingContext'
import {
  Animated,
  InteractionManager,
  NativeScrollEvent,
  NativeSyntheticEvent,
  StyleSheet,
  View,
} from 'react-native'
import React, { useEffect, useRef, useState } from 'react'
import requestAPI from '@/services/requestApiService'
import { API_ROUTES } from '@/constants/ApiRoutes'
import { useGlobalSnackbar } from '@/components/GlobalSnackbarProvider'
import { ItemAttendance } from '@/types/ItemAttendance'
import { CollapseItemAttendance } from '@/views/dashboard/item/CollapseItemAttendance'
import { Colors } from '@/constants/Colors'
import { ATTENDANCE_STATUS, PAGIONATION_SIZE, TYPE_SHIFT } from '@/constants'
import ScrollViewContent from '../ScrollViewContent'
import FilterModal from '@/components/FilterModal'
import Select from '@/components/form/Select'
import CustomInputText from '@/components/form/CustomInputText'
import EmptyData from '@/components/EmptyData'
import { useGlobalStore } from '@/utils/GlobalStore'
import { countNotification } from '@/utils'

interface Props {
  animatedValue: Animated.Value
  showMenuRef: React.RefObject<boolean>
}

const AttendanceTab: React.FC<Props> = ({ animatedValue, showMenuRef }) => {
  const { showLoading, hideLoading } = useLoading()
  const { showError } = useGlobalSnackbar()

  const [currentPage, setCurrentPage] = useState(1)
  const [totalPage, setTotalPage] = useState(0)

  const DEFAULT_EARLY_MINUTE_CHECKIN = useRef(0)

  const [lstData, setLstData] = useState<ItemAttendance[]>([])
  const [loadMorePage, setLoadMorePage] = useState(false)
  const [totalItems, setTotalItems] = useState(0)

  const [refreshTrigger, setRefreshTrigger] = useState(0)

  const [dataFilter, setDataFilter] = useState<{
    keyword: string
    status: number | null
    type: string | null
  }>({
    keyword: '',
    status: null,
    type: null,
  })

  const headerAnimation = {
    backgroundColor: animatedValue.interpolate({
      inputRange: [0, 100],
      outputRange: [Colors.background, '#ffffff'],
      extrapolate: 'clamp',
    }),
  }

  const setTotalNotification = useGlobalStore((state) => state.setTotalNotification)
  const getTotalNotification = () => {
    countNotification((data) => setTotalNotification(data || 0))
  }

  const fetchDataList = async () => {
    if (currentPage === 1) {
      showLoading()
    }

    if (loadMorePage) {
      return
    }

    setLoadMorePage(true)
    requestAPI
      .get(`${API_ROUTES.FETCH_DATA_STUDENT_ATTENDANCE}`, {
        params: {
          ...dataFilter,
          page: currentPage,
          size: PAGIONATION_SIZE,
        },
      })
      .then(({ data: response }) => {
        setLstData((prev) => {
          const combined =
            currentPage === 1 ? [...response.data.data] : [...prev, ...response.data.data]
          const uniqueData = combined.filter(
            (item, index, self) =>
              index === self.findIndex((t) => t.idPlanDate === item.idPlanDate),
          )
          return uniqueData
        })
        setTotalItems(response.data.totalItems)
        setTotalPage(response.data.totalPages)
      })
      .catch((error) => {
        showError(error.response?.data?.message || 'Không thể tải dữ liệu. Vui lòng thử lại!', 2000)
      })
      .finally(() => {
        InteractionManager.runAfterInteractions(() => {
          setLoadMorePage(false)
          hideLoading()
        })
      })
  }

  useEffect(() => {
    showLoading()
    requestAPI
      .get(`${API_ROUTES.FETCH_DATA_SETTINGS}`)
      .then(({ data: response }) => {
        DEFAULT_EARLY_MINUTE_CHECKIN.current = response.data?.['ATTENDANCE_EARLY_CHECKIN'] || 0
      })
      .catch((error) => {
        showError(error?.response?.data?.message || 'Không thể tải dữ liệu cài đặt', 2000)
      })
      .finally(() => {
        InteractionManager.runAfterInteractions(() => {
          hideLoading()
        })
      })
  }, [])

  useEffect(() => {
    handleRefresh()
  }, [dataFilter])

  useEffect(() => {
    fetchDataList()
  }, [currentPage, refreshTrigger])

  const handleMomentumScrollEnd = (event: NativeSyntheticEvent<NativeScrollEvent>) => {
    const { layoutMeasurement, contentOffset, contentSize } = event.nativeEvent
    const isCloseToBottom = layoutMeasurement.height + contentOffset.y >= contentSize.height - 50

    if (
      isCloseToBottom &&
      lstData.length < totalItems &&
      totalItems > PAGIONATION_SIZE &&
      currentPage < totalPage &&
      !loadMorePage
    ) {
      setCurrentPage((prev) => prev + 1)
    }
  }

  const handleRefresh = () => {
    if (loadMorePage) return
    getTotalNotification()
    setCurrentPage(1)
    setRefreshTrigger((prev) => prev + 1)
  }

  const handleClearFilter = () => {
    setDataFilter({
      keyword: '',
      status: null,
      type: null,
    })
  }

  return (
    <View style={styles.container}>
      <Animated.View style={[styles.titleCard, headerAnimation]}>
        <Text variant="titleMedium">Danh sách ca hôm nay</Text>
        <FilterModal
          totalItems={totalItems}
          isFilter={
            !!(dataFilter.keyword || dataFilter.status !== null || dataFilter.type !== null)
          }
        >
          {(closeModal) => (
            <>
              <CustomInputText
                label="Từ khoá"
                placeholder="Tên nhóm xưởng, giảng viên..."
                value={dataFilter.keyword}
                onChange={(val) => setDataFilter((prev) => ({ ...prev, keyword: val }))}
                leftIcon="magnify"
              />
              <Select
                value={dataFilter.status}
                onChange={(val) => setDataFilter((prev) => ({ ...prev, status: val }))}
                options={[
                  {
                    label: '-- Tất cả trạng thái --',
                    value: null,
                  },
                  ...Object.values(ATTENDANCE_STATUS).map((o) => ({
                    label: o.name,
                    value: o.id,
                  })),
                ]}
                placeholder="-- Tất cả trạng thái --"
                label="Trạng thái"
              />
              <Select
                value={dataFilter.type}
                onChange={(val) => setDataFilter((prev) => ({ ...prev, type: val }))}
                options={[
                  {
                    label: '-- Tất cả hình thức --',
                    value: null,
                  },
                  ...Object.entries(TYPE_SHIFT).map(([key, value]) => ({
                    label: value,
                    value: key,
                  })),
                ]}
                placeholder="-- Tất cả hình thức --"
                label="Hình thức"
              />
              <View style={styles.buttonWrap}>
                <Button
                  icon="filter"
                  mode="contained"
                  style={styles.button}
                  onPress={() => {
                    closeModal()
                  }}
                >
                  Lọc
                </Button>
                <Button
                  mode="contained"
                  buttonColor={Colors.gray}
                  textColor={Colors.primary}
                  style={styles.button}
                  onPress={() => {
                    closeModal()
                    handleClearFilter()
                  }}
                >
                  Huỷ lọc
                </Button>
              </View>
            </>
          )}
        </FilterModal>
      </Animated.View>
      <ScrollViewContent
        animatedValue={animatedValue}
        showMenuRef={showMenuRef}
        isLoading={loadMorePage}
        onRefresh={handleRefresh}
        onMomentumScrollEnd={handleMomentumScrollEnd}
      >
        {lstData.length > 0 &&
          lstData.map((item) => (
            <CollapseItemAttendance
              key={item.idPlanDate}
              item={item}
              onSuccess={handleRefresh}
              earlyMinuteCheckin={DEFAULT_EARLY_MINUTE_CHECKIN.current}
            />
          ))}
      </ScrollViewContent>
      {lstData.length < 1 && <EmptyData text="Không có ca nào" />}
    </View>
  )
}
export default AttendanceTab

const styles = StyleSheet.create({
  container: {
    flex: 1,
    height: '100%',
    backgroundColor: Colors.background,
  },
  titleCard: {
    paddingTop: 5,
    paddingBottom: 5,
    paddingLeft: 15,
    paddingRight: 5,
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    backgroundColor: Colors.background,
  },
  buttonWrap: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    marginVertical: 20,
    gap: 10,
  },
  button: {
    flex: 1,
  },
})
