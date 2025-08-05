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
import { PAGIONATION_SIZE } from '@/constants'
import ScrollViewContent from '../ScrollViewContent'
import FilterModal from '@/components/FilterModal'
import Select from '@/components/form/Select'
import EmptyData from '@/components/EmptyData'
import { countNotification, getCurrentSemester } from '@/utils'
import { useLoading } from '@/components/loading/LoadingContext'
import { useGlobalStore } from '@/utils/GlobalStore'
import { CollapseItemHistory } from '../item/CollapseItemHistory'
import { ItemHistory } from '@/types/ItemHistory'
import { Factory } from '@/types/Factory'

interface Props {
  animatedValue: Animated.Value
  showMenuRef: React.RefObject<boolean>
}

const HistoryTab: React.FC<Props> = ({ animatedValue, showMenuRef }) => {
  const { showLoading, hideLoading } = useLoading()
  const { showError } = useGlobalSnackbar()

  const lstSemester = useGlobalStore((state) => state.lstSemester)

  const [currentPage, setCurrentPage] = useState(1)
  const [totalPage, setTotalPage] = useState(0)

  const [lstData, setLstData] = useState<ItemHistory[]>([])
  const [lstFactory, setLstFactory] = useState<Factory[]>([])
  const [loadMorePage, setLoadMorePage] = useState(false)
  const [totalItems, setTotalItems] = useState(0)

  const [refreshTrigger, setRefreshTrigger] = useState(0)

  const [totalStats, setTotalStats] = useState<{
    totalShift: number
    totalPresent: number
    totalAbsent: number
  }>({
    totalShift: 0,
    totalPresent: 0,
    totalAbsent: 0,
  })

  const DEFAULT_TITLE = 'Lịch sử'

  const semester = getCurrentSemester(lstSemester)
  const [factoryName, setFactoryName] = useState<string>(DEFAULT_TITLE)
  const [currentIdFactory, setCurrentIdFactory] = useState<string | undefined>()

  const [dataFilter, setDataFilter] = useState<{
    semesterId: string | undefined
    factoryId: string | undefined
  }>({
    semesterId: semester?.id,
    factoryId: currentIdFactory,
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
    if (!dataFilter.factoryId || !dataFilter.semesterId) {
      setLstData([])
      setTotalItems(0)
      setFactoryName(DEFAULT_TITLE)
      return
    }

    if (currentPage === 1) {
      showLoading()
      const currentFactory = lstFactory.find((o) => o.id === dataFilter.factoryId)
      setFactoryName(currentFactory ? currentFactory.name : DEFAULT_TITLE)
    }

    if (loadMorePage) {
      return
    }

    setLoadMorePage(true)

    requestAPI
      .get(`${API_ROUTES.FETCH_DATA_STUDENT_HISTORY}`, {
        params: {
          ...dataFilter,
          page: currentPage,
          size: PAGIONATION_SIZE,
        },
      })
      .then(({ data: response }) => {
        setLstData((prev) => {
          const combined =
            currentPage === 1 ? [...response.data.page.data] : [...prev, ...response.data.page.data]
          const uniqueData = combined.filter(
            (item, index, self) =>
              index === self.findIndex((t) => t.planDateId === item.planDateId),
          )
          return uniqueData
        })
        setTotalStats({
          totalShift: response.data?.totalShift || 0,
          totalPresent: response.data?.totalPresent || 0,
          totalAbsent: response.data?.totalAbsent || 0,
        })
        setTotalItems(response.data.page.totalItems)
        setTotalPage(response.data.page.totalPages)
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
      .get(`${API_ROUTES.FETCH_DATA_STUDENT_HISTORY_FACTORIES}/${dataFilter.semesterId}`)
      .then(({ data: response }) => {
        setLstFactory(response.data)
        const idFactory = response.data?.[0]?.id
        dataFilter.factoryId = idFactory
        setCurrentIdFactory(idFactory)
      })
      .catch((error) => {
        showError(error?.response?.data?.message || 'Không thể tải dữ liệu cài đặt', 2000)
      })
      .finally(() => {
        InteractionManager.runAfterInteractions(() => {
          hideLoading()
        })
      })
  }, [dataFilter.semesterId])

  useEffect(() => {
    handleRefresh()
  }, [dataFilter.factoryId])

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
      semesterId: semester?.id,
      factoryId: currentIdFactory,
    })
  }

  return (
    <View style={styles.container}>
      <Animated.View style={[styles.titleCard, headerAnimation]}>
        <Text variant="titleMedium"> {factoryName} </Text>
        <FilterModal
          totalItems={totalItems}
          isFilter={
            !!(dataFilter.semesterId !== semester?.id || dataFilter.factoryId !== currentIdFactory)
          }
        >
          {(closeModal) => (
            <>
              <Select
                value={dataFilter.semesterId}
                onChange={(val) => setDataFilter((prev) => ({ ...prev, semesterId: val }))}
                options={lstSemester.map((o) => ({
                  label: o.code,
                  value: o.id,
                }))}
                placeholder="-- Học kỳ --"
                label="Học kỳ"
              />
              <Select
                value={dataFilter.factoryId}
                onChange={(val) => setDataFilter((prev) => ({ ...prev, factoryId: val }))}
                options={lstFactory.map((o) => ({
                  label: o.name,
                  value: o.id,
                }))}
                placeholder="-- Nhóm xưởng --"
                label="Nhóm xưởng"
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
        {lstData.length > 0 && (
          <>
            <View
              style={{
                marginBottom: 10,
                paddingHorizontal: 15,
                flexDirection: 'row',
                flexWrap: 'wrap',
              }}
            >
              <Text>Đã vắng </Text>
              <Text style={[styles.grayText, totalStats.totalAbsent > 0 && styles.textDanger]}>
                {totalStats.totalAbsent}
              </Text>
              <Text>, có mặt </Text>
              <Text style={[styles.grayText, totalStats.totalPresent > 0 && styles.textSuccess]}>
                {totalStats.totalPresent}
              </Text>
              <Text> trong tổng số </Text>
              <Text style={styles.grayText}>{totalStats.totalShift}</Text>
              <Text>.</Text>
            </View>
            {lstData.map((item) => (
              <CollapseItemHistory key={item.planDateId} item={item} />
            ))}
          </>
        )}
      </ScrollViewContent>
      {lstData.length < 1 && <EmptyData text="Không có ca nào" />}
    </View>
  )
}
export default HistoryTab

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
  grayText: {
    color: Colors.primary,
    fontWeight: 'bold',
  },
  textDanger: {
    color: 'red',
  },
  textSuccess: {
    color: 'green',
  },
})
