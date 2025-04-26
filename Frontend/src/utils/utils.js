import { DEFAULT_DATE_FORMAT } from '@/constants'
import { unref } from 'vue'

export const decodeBase64 = (base64String) => {
  const fixedBase64 = base64String
  const byteArray = Uint8Array.from(atob(fixedBase64), (c) => c.charCodeAt(0))
  return new TextDecoder('utf-8').decode(byteArray)
}

export const debounce = (func, delay) => {
  let timer
  return function (...args) {
    clearTimeout(timer)
    timer = setTimeout(() => {
      func.apply(this, args)
    }, delay)
  }
}

export const dayOfWeek = (timestamp) => {
  const date = new Date(timestamp)
  const daysOfWeek = ['Chủ Nhật', 'Thứ Hai', 'Thứ Ba', 'Thứ Tư', 'Thứ Năm', 'Thứ Sáu', 'Thứ Bảy']

  return daysOfWeek[date.getDay()]
}

export const formatDate = (timestamp, format) => {
  if (!timestamp) {
    return ''
  }

  if (!format) {
    format = DEFAULT_DATE_FORMAT
  }

  const date = new Date(timestamp)

  const map = {
    YYYY: date.getFullYear(),
    yyyy: date.getFullYear(),
    MM: String(date.getMonth() + 1).padStart(2, '0'),
    dd: String(date.getDate()).padStart(2, '0'),
    DD: String(date.getDate()).padStart(2, '0'),
    HH: String(date.getHours()).padStart(2, '0'),
    mm: String(date.getMinutes()).padStart(2, '0'),
    ss: String(date.getSeconds()).padStart(2, '0'),
  }

  return format.replace(/YYYY|yyyy|MM|dd|DD|HH|mm|ss/g, (matched) => map[matched])
}

export const rowSelectTable = (selectedRowKeys, isDisabled = () => false) => {
  return {
    selectedRowKeys: unref(selectedRowKeys),
    onChange: (changableRowKeys) => {
      selectedRowKeys.value = changableRowKeys
    },
    hideDefaultSelections: true,
    getCheckboxProps: (record) => ({
      disabled: isDisabled(record.id),
    }),
    selections: [
      {
        key: 'all',
        text: 'Chọn tất cả',
        onSelect: (changableRowKeys) => {
          selectedRowKeys.value = changableRowKeys.filter((key) => !isDisabled(key))
        },
        disabled: (changableRowKeys) => changableRowKeys.every((key) => isDisabled(key)),
      },
      {
        key: 'invert',
        text: 'Đảo ngược lựa chọn',
        onSelect: (changableRowKeys) => {
          selectedRowKeys.value = changableRowKeys.filter(
            (key) => !selectedRowKeys.value.includes(key) && !isDisabled(key),
          )
        },
        disabled: (changableRowKeys) => changableRowKeys.every((key) => isDisabled(key)),
      },
      {
        key: 'none',
        text: 'Bỏ chọn tất cả',
        onSelect: () => {
          selectedRowKeys.value = []
        },
      },
      {
        key: 'odd',
        text: 'Chọn hàng lẻ',
        onSelect: (changableRowKeys) => {
          selectedRowKeys.value = changableRowKeys.filter(
            (key, index) => index % 2 === 0 && !isDisabled(key),
          )
        },
        disabled: (changableRowKeys) =>
          changableRowKeys.filter((_, index) => index % 2 === 0).every((key) => isDisabled(key)),
      },
      {
        key: 'even',
        text: 'Chọn hàng chẵn',
        onSelect: (changableRowKeys) => {
          selectedRowKeys.value = changableRowKeys.filter(
            (key, index) => index % 2 !== 0 && !isDisabled(key),
          )
        },
        disabled: (changableRowKeys) =>
          changableRowKeys.filter((_, index) => index % 2 !== 0).every((key) => isDisabled(key)),
      },
    ],
  }
}
