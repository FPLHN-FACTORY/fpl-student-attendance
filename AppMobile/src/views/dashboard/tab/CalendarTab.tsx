import { Button, Text } from 'react-native-paper'
import {
  Animated,
  InteractionManager,
  NativeScrollEvent,
  NativeSyntheticEvent,
  StyleSheet,
  View,
} from 'react-native'
import React, { useEffect, useState } from 'react'
import requestAPI from '@/services/requestApiService'
import { API_ROUTES } from '@/constants/ApiRoutes'
import { useGlobalSnackbar } from '@/components/GlobalSnackbarProvider'
import { Colors } from '@/constants/Colors'
import { PAGIONATION_SIZE, SCHEDULE_TIME } from '@/constants'
import ScrollViewContent from '../ScrollViewContent'
import FilterModal from '@/components/FilterModal'
import Select from '@/components/form/Select'
import EmptyData from '@/components/EmptyData'
import { CollapseItemCalendar } from '../item/CollapseItemCalendar'
import { ItemCalendar } from '@/types/ItemCalendar'
import { getTimeRange } from '@/utils'
import { useLoading } from '@/components/loading/LoadingContext'

interface Props {
  animatedValue: Animated.Value
  showMenuRef: React.RefObject<boolean>
}

const CalendarTab: React.FC<Props> = ({ animatedValue, showMenuRef }) => {
  const { showLoading, hideLoading } = useLoading()

  const { showError } = useGlobalSnackbar()

  const [currentPage, setCurrentPage] = useState(1)
  const [totalPage, setTotalPage] = useState(0)

  const [lstData, setLstData] = useState<ItemCalendar[]>([])
  const [loadMorePage, setLoadMorePage] = useState(false)
  const [totalItems, setTotalItems] = useState(0)

  const [refreshTrigger, setRefreshTrigger] = useState(0)

  const DEFAULT_PLAN = '7'

  const [dataFilter, setDataFilter] = useState<{
    plan: keyof typeof SCHEDULE_TIME
  }>({
    plan: DEFAULT_PLAN,
  })

  const headerAnimation = {
    backgroundColor: animatedValue.interpolate({
      inputRange: [0, 100],
      outputRange: [Colors.background, '#ffffff'],
      extrapolate: 'clamp',
    }),
  }

  const fetchDataList = async () => {
    if (currentPage === 1) {
      showLoading()
    }

    if (loadMorePage) {
      return
    }

    setLoadMorePage(true)
    const { now, max } = getTimeRange(Number(dataFilter.plan))

    requestAPI
      .get(`${API_ROUTES.FETCH_DATA_STUDENT_CALENDAR}`, {
        params: {
          now,
          max,
          page: currentPage,
          size: PAGIONATION_SIZE,
        },
      })
      .then(({ data: response }) => {
        setLstData((prev) => {
          const combined =
            currentPage === 1 ? [...response.data.data] : [...prev, ...response.data.data]
          const uniqueData = combined.filter(
            (item, index, self) => index === self.findIndex((t) => t.id === item.id),
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
    setCurrentPage(1)
    setRefreshTrigger((prev) => prev + 1)
  }

  const handleClearFilter = () => {
    setDataFilter({
      plan: DEFAULT_PLAN,
    })
  }

  return (
    <View style={styles.container}>
      <Animated.View style={[styles.titleCard, headerAnimation]}>
        <Text variant="titleMedium"> {SCHEDULE_TIME[dataFilter.plan]}</Text>
        <FilterModal totalItems={totalItems} isFilter={!!(dataFilter.plan !== DEFAULT_PLAN)}>
          {(closeModal) => (
            <>
              <Select
                value={dataFilter.plan}
                onChange={(val) => setDataFilter((prev) => ({ ...prev, plan: val }))}
                options={Object.entries(SCHEDULE_TIME).map(([key, value]) => ({
                  label: value,
                  value: key,
                }))}
                placeholder="-- Khoảng thời gian --"
                label="Khoảng thời gian"
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
          lstData.map((item) => <CollapseItemCalendar key={item.id} item={item} />)}
      </ScrollViewContent>
      {lstData.length < 1 && <EmptyData text="Không có lịch diễn ra nào" />}
    </View>
  )
}
export default CalendarTab

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
