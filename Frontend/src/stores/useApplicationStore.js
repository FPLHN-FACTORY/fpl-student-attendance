import { API_ROUTES_NOTIFICATION } from '@/constants/routesConstant'
import requestAPI from '@/services/requestApiService'
import { ExclamationCircleOutlined } from '@ant-design/icons-vue'
import { message, Modal } from 'ant-design-vue'
import { defineStore } from 'pinia'
import { createVNode, ref } from 'vue'

const useApplicationStore = defineStore('Application', () => {
  const selectedKeys = ref([Number(sessionStorage.getItem('selectedKeys'))])
  const totalNotification = ref(0)
  const lstNotification = ref([])
  const isLoadingNotification = ref(false)

  const SHOW_MAX_ITEM = 10

  const setSelectedKeys = (key) => {
    selectedKeys.value = [key]
    setSessionSelectedKeys(key)
  }

  const setSessionSelectedKeys = (key) => {
    sessionStorage.setItem('selectedKeys', key)
  }

  const loadNotification = () => {
    const fetchDataCount = () => {
      requestAPI
        .get(API_ROUTES_NOTIFICATION.FETCH_COUNT)
        .then(({ data: response }) => {
          totalNotification.value = response.data
          fetchDataList()
        })
        .catch((error) => {
          message.error(error?.response?.data?.message || 'Không thể tải dữ liệu thông báo')
        })
    }
    const fetchDataList = () => {
      requestAPI
        .get(API_ROUTES_NOTIFICATION.FETCH_LIST, {
          params: {
            size: SHOW_MAX_ITEM,
            status: 1,
          },
        })
        .then(({ data: response }) => {
          lstNotification.value = response.data.data
        })
        .catch((error) => {
          message.error(error?.response?.data?.message || 'Không thể tải dữ liệu thông báo')
        })
    }
    fetchDataCount()
  }

  const markReadAll = (callback) => {
    Modal.confirm({
      title: 'Đánh dấu đã đọc tất cả',
      icon: createVNode(ExclamationCircleOutlined),
      content: 'Đánh dấu đã đọc tất cả thông báo?',
      okText: 'Tiếp tục',
      cancelText: 'Huỷ bỏ',
      onOk: () => {
        isLoadingNotification.value = true
        requestAPI
          .put(API_ROUTES_NOTIFICATION.FETCH_MARK_READ_ALL)
          .then(({ data: response }) => {
            totalNotification.value = 0
            lstNotification.value = []
            message.success(response.message)
            typeof callback == 'function' && callback()
          })
          .catch((error) => {
            message.error(error?.response?.data?.message || 'Không thể đánh dấu đã đọc tất cả')
          })
          .finally(() => {
            isLoadingNotification.value = false
          })
      },
    })
  }

  const markRead = (item, callback) => {
    Modal.confirm({
      title: 'Đánh dấu đã đọc',
      icon: createVNode(ExclamationCircleOutlined),
      content: 'Đánh dấu đã đọc thông báo này?',
      okText: 'Tiếp tục',
      cancelText: 'Huỷ bỏ',
      onOk: () => {
        requestAPI
          .put(API_ROUTES_NOTIFICATION.FETCH_MARK_READ, {
            ids: [item.id],
          })
          .then(({ data: response }) => {
            if (lstNotification.value.find((o) => o.id === item.id)) {
              totalNotification.value -= 1
              lstNotification.value = lstNotification.value.filter((o) => o.id !== item.id)
            }
            message.success(response.message)
            typeof callback == 'function' && callback()
          })
          .catch((error) => {
            message.error(error?.response?.data?.message || 'Không thể đánh dấu đã đọc thông báo')
          })
      },
    })
  }

  const markUnread = (item, callback) => {
    Modal.confirm({
      title: 'Đánh dấu chưa đọc',
      icon: createVNode(ExclamationCircleOutlined),
      content: 'Đánh dấu chưa đọc thông báo này?',
      okText: 'Tiếp tục',
      cancelText: 'Huỷ bỏ',
      onOk: () => {
        requestAPI
          .put(API_ROUTES_NOTIFICATION.FETCH_MARK_UNREAD, {
            ids: [item.id],
          })
          .then(({ data: response }) => {
            if (lstNotification.value.find((o) => o.id !== item.id)) {
              totalNotification.value += 1
              lstNotification.value.put(item)
            }
            message.success(response.message)
            typeof callback == 'function' && callback()
          })
          .catch((error) => {
            message.error(error?.response?.data?.message || 'Không thể đánh dấu chưa đọc thông báo')
          })
      },
    })
  }

  const remove = (item, callback) => {
    Modal.confirm({
      title: 'Xoá thông báo',
      icon: createVNode(ExclamationCircleOutlined),
      content: 'Bạn thực sự muốn xoá thông báo này?',
      okText: 'Tiếp tục',
      cancelText: 'Huỷ bỏ',
      onOk: () => {
        requestAPI
          .delete(API_ROUTES_NOTIFICATION.FETCH_DELETE, {
            data: {
              ids: [item.id],
            },
          })
          .then(({ data: response }) => {
            if (lstNotification.value.find((o) => o.id === item.id)) {
              totalNotification.value -= 1
              lstNotification.value = lstNotification.value.filter((o) => o.id !== item.id)
            }
            message.success(response.message)
            typeof callback == 'function' && callback()
          })
          .catch((error) => {
            message.error(error?.response?.data?.message || 'Không thể xoá thông báo')
          })
      },
    })
  }

  return {
    selectedKeys,
    setSelectedKeys,
    isLoadingNotification,
    totalNotification,
    lstNotification,
    loadNotification,
    markReadAll,
    markRead,
    markUnread,
    remove,
  }
})

export default useApplicationStore
