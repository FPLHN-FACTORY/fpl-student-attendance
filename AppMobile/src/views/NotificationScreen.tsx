import { Colors } from '@/constants/Colors'
import { RootStackParamList } from '@/types/RootStackParamList'
import {
  countNotification,
  lightenColor,
  UPPER_HEADER_HEIGHT,
  UPPER_HEADER_PADDING_TOP,
} from '@/utils'
import { NativeStackScreenProps } from '@react-navigation/native-stack'
import { LinearGradient } from 'expo-linear-gradient'
import {
  View,
  StyleSheet,
  StatusBar,
  ScrollView,
  InteractionManager,
  NativeSyntheticEvent,
  NativeScrollEvent,
  RefreshControl,
  TouchableOpacity,
} from 'react-native'
import { SafeAreaView, useSafeAreaInsets } from 'react-native-safe-area-context'
import useBackPressGoBack from '@/components/useBackPressGoBack'
import { ActivityIndicator, Button, IconButton, Text } from 'react-native-paper'
import requestAPI from '@/services/requestApiService'
import { API_ROUTES_NOTIFICATION } from '@/constants/ApiRoutes'
import { useEffect, useRef, useState } from 'react'
import FilterModal from '@/components/FilterModal'
import Select from '@/components/form/Select'
import { useLoading } from '@/components/loading/LoadingContext'
import { ItemNotification } from '@/types/ItemNotification'
import { PAGIONATION_SIZE } from '@/constants'
import { useGlobalSnackbar } from '@/components/GlobalSnackbarProvider'
import EmptyData from '@/components/EmptyData'
import { CollapseItemNotification } from './notication/item/CollapseItemNotification'
import DraggableBottom, { DraggableBottomRef } from '@/components/DraggableBottom'
import ModalConfirm from '@/components/ModalConfirm'
import { useGlobalStore } from '@/utils/GlobalStore'

type Props = NativeStackScreenProps<RootStackParamList, 'Notification'>

const NotificationScreen: React.FC<Props> = ({ navigation }) => {
  useBackPressGoBack(navigation)
  const insets = useSafeAreaInsets()
  const { showLoading, hideLoading } = useLoading()
  const { showError } = useGlobalSnackbar()

  const [status, setStatus] = useState(null)
  const [currentPage, setCurrentPage] = useState(1)
  const [totalPage, setTotalPage] = useState(0)

  const [lstData, setLstData] = useState<ItemNotification[]>([])
  const [loadMorePage, setLoadMorePage] = useState(false)
  const [totalItems, setTotalItems] = useState(0)

  const [refreshing, setRefreshing] = useState(false)
  const [refreshTrigger, setRefreshTrigger] = useState(0)

  const [isShow, setIsShow] = useState(false)
  const [visibleModal, setVisibleModal] = useState(false)
  const [currentItem, setCurrentItem] = useState<ItemNotification>()
  const bottomSheetRef = useRef<DraggableBottomRef>(null)

  const lstStatus = {
    1: 'Thông báo mới',
    0: 'Đã đọc',
  }
  const totalNotification = useGlobalStore((state) => state.totalNotification)
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
      .get(`${API_ROUTES_NOTIFICATION.FETCH_LIST}`, {
        params: {
          status,
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

  const markReadAll = () => {
    setVisibleModal(false)
    showLoading()
    requestAPI
      .put(API_ROUTES_NOTIFICATION.FETCH_MARK_READ_ALL)
      .then(({ data: response }) => {
        setLstData((prev) =>
          prev.map((item) => ({
            ...item,
            status: 0,
          })),
        )
        getTotalNotification()
      })
      .catch((error) => {
        showError(error?.response?.data?.message || 'Không thể đánh dấu đã đọc tất cả', 2000)
      })
      .finally(() => {
        hideLoading()
      })
  }

  const markRead = () => {
    showLoading()
    requestAPI
      .put(API_ROUTES_NOTIFICATION.FETCH_MARK_READ, {
        ids: [currentItem?.id],
      })
      .then(({ data: response }) => {
        const item = lstData.find((o) => o.id === currentItem?.id)
        if (item) {
          item.status = 0
        }
        setTotalNotification(totalNotification - 1)
      })
      .catch((error) => {
        showError(
          error?.response?.data?.message || 'Không thể đánh dấu chưa đọc thông báo này',
          2000,
        )
      })
      .finally(() => {
        hideLoading()
        bottomSheetRef?.current?.close()
      })
  }

  const markUnread = () => {
    showLoading()
    requestAPI
      .put(API_ROUTES_NOTIFICATION.FETCH_MARK_UNREAD, {
        ids: [currentItem?.id],
      })
      .then(({ data: response }) => {
        const item = lstData.find((o) => o.id === currentItem?.id)
        if (item) {
          item.status = 1
        }
        setTotalNotification(totalNotification + 1)
      })
      .catch((error) => {
        showError(
          error?.response?.data?.message || 'Không thể đánh dấu chưa đọc thông báo này',
          2000,
        )
      })
      .finally(() => {
        hideLoading()
        bottomSheetRef?.current?.close()
      })
  }

  useEffect(() => {
    handleRefresh()
  }, [status])

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

  const onRefresh = async () => {
    setRefreshing(true)
    handleRefresh()
    setRefreshing(false)
  }

  const handleRefresh = () => {
    if (loadMorePage) return
    getTotalNotification()
    setCurrentPage(1)
    setRefreshTrigger((prev) => prev + 1)
  }

  const handleClearFilter = () => {
    setStatus(null)
  }

  const handleShowOption = (item: ItemNotification) => {
    setCurrentItem(item)
    setIsShow(true)
  }

  useEffect(() => {
    getTotalNotification()
  }, [])

  return (
    <View style={styles.container}>
      <LinearGradient
        colors={[Colors.primary, lightenColor(Colors.primary, 0.8)]}
        style={{ position: 'absolute', top: 0, left: 0, right: 0, bottom: 0 }}
      />
      <StatusBar barStyle="light-content" translucent backgroundColor="transparent" />

      <View style={{ height: UPPER_HEADER_HEIGHT + insets.top, paddingTop: insets.top }} />

      <SafeAreaView style={styles.header}>
        <View style={styles.upperHeader}>
          <View style={{ flex: 1, flexDirection: 'row', alignItems: 'center' }}>
            <IconButton
              icon="chevron-left"
              size={24}
              iconColor="#ffffff"
              onPress={() => navigation.goBack()}
              style={{ backgroundColor: '#ffffff21' }}
            />
            <Text variant="titleMedium" style={{ color: '#ffffff', marginLeft: 10 }}>
              Thông báo {totalNotification > 0 ? `(${totalNotification})` : null}
            </Text>
          </View>

          {totalNotification > 0 && (
            <IconButton
              icon="check-all"
              size={24}
              iconColor="#ffffff"
              onPress={() => setVisibleModal(true)}
            />
          )}
        </View>
      </SafeAreaView>

      <ModalConfirm
        isShow={visibleModal}
        title="Đã đọc tất cả"
        onOk={markReadAll}
        onCancel={() => setVisibleModal(false)}
      >
        <Text>Bạn có muốn đánh dấu đã đọc tất cả thông báo không?</Text>
      </ModalConfirm>

      <DraggableBottom
        ref={bottomSheetRef}
        title="Tuỳ chọn"
        isShow={isShow}
        onClose={() => setIsShow(false)}
      >
        <View
          style={{
            flex: 1,
            height: 120,
          }}
        >
          {currentItem?.status === 1 ? (
            <TouchableOpacity
              onPress={markRead}
              style={{
                flex: 1,
                height: 50,
              }}
            >
              <Text style={styles.itemOption}> Đánh dấu đã đọc</Text>
            </TouchableOpacity>
          ) : (
            <TouchableOpacity
              onPress={markUnread}
              style={{
                flex: 1,
                height: 50,
              }}
            >
              <Text style={styles.itemOption}> Đánh dấu chưa đọc</Text>
            </TouchableOpacity>
          )}
        </View>
      </DraggableBottom>

      <View style={styles.containerBody}>
        <View style={[styles.titleCard]}>
          <Text variant="titleMedium">
            {status === null ? 'Tất cả thông báo' : lstStatus[status]}
          </Text>
          <FilterModal totalItems={totalItems} isFilter={!!(status !== null)}>
            {(closeModal) => (
              <>
                <Select
                  value={status}
                  onChange={setStatus}
                  options={[
                    { label: '-- Tất cả thông báo --', value: null },
                    ...Object.entries(lstStatus).map(([key, value]) => ({
                      label: value,
                      value: key,
                    })),
                  ]}
                  placeholder="-- Tất cả thông báo --"
                  label="Trạng thái"
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
        </View>
        <ScrollView
          showsVerticalScrollIndicator={false}
          onMomentumScrollEnd={handleMomentumScrollEnd}
          refreshControl={<RefreshControl refreshing={refreshing} onRefresh={onRefresh} />}
          scrollEventThrottle={16}
          contentContainerStyle={{ flexGrow: 1 }}
          style={{ flex: 1 }}
        >
          <View style={styles.scrollViewContent}>
            {lstData.length > 0 &&
              lstData.map((item, i) => (
                <CollapseItemNotification
                  key={item.id}
                  index={i}
                  item={item}
                  onOption={() => handleShowOption(item)}
                />
              ))}
          </View>
          {loadMorePage && (
            <View style={styles.loading}>
              <ActivityIndicator size="small" color={Colors.primary} />
            </View>
          )}
        </ScrollView>
        {lstData.length < 1 && <EmptyData text="Không có thông báo nào!!!" />}
      </View>
    </View>
  )
}

export default NotificationScreen

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: lightenColor(Colors.primary, 0.8),
  },
  containerBody: {
    flex: 1,
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
    backgroundColor: '#ffffff',
  },
  header: {
    position: 'absolute',
    width: '100%',
    backgroundColor: 'transparent',
  },
  upperHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 16,
    height: UPPER_HEADER_HEIGHT + UPPER_HEADER_PADDING_TOP,
  },
  bell: {
    backgroundColor: 'rgba(255, 255, 255, 0.2)',
    padding: 5,
    borderRadius: 999,
    marginLeft: 'auto',
    marginHorizontal: 18,
  },
  avatar: {
    width: 40,
    height: 40,
    borderRadius: 999,
    borderWidth: 2,
    borderColor: 'rgba(255, 255, 255, 0.2)',
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
  scrollViewContent: {
    flex: 1,
    paddingBottom: 30,
  },
  loading: {
    paddingBottom: 40,
  },
  itemOption: {
    flex: 1,
    paddingHorizontal: 20,
    paddingVertical: 10,
  },
})
