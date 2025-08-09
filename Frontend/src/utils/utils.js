import { DEFAULT_DATE_FORMAT } from '@/constants'
import dayjs from 'dayjs'
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

export const autoAddColumnWidth = (columns, ellipsis = false, charWidth = 10, padding = 20) => {
  return columns.map((col) => {
    if (!col.width) {
      const titleLength = (col.title || '').length
      const estimatedWidth = titleLength * charWidth + padding
      return { ...col, width: estimatedWidth, minWidth: estimatedWidth, ellipsis }
    }
    return col
  })
}

export const getCurrentSemester = (semesters) => {
  const now = Date.now()

  let currentSemester = semesters.find((item) => now >= item.fromDate && now <= item.toDate)

  if (!currentSemester) {
    currentSemester = semesters.reduce((closest, item) => {
      const diffCurrent = Math.abs(item.fromDate - now)
      const diffClosest = Math.abs(closest.fromDate - now)
      return diffCurrent < diffClosest ? item : closest
    })
  }
  return currentSemester
}

export const dayOfWeek = (timestamp) => {
  const inputDate = new Date(timestamp)
  const now = new Date()

  const inputDateOnly = new Date(inputDate.getFullYear(), inputDate.getMonth(), inputDate.getDate())
  const nowDateOnly = new Date(now.getFullYear(), now.getMonth(), now.getDate())

  const diffInDays = (inputDateOnly - nowDateOnly) / (1000 * 60 * 60 * 24)

  const daysOfWeek = ['Chủ Nhật', 'Thứ Hai', 'Thứ Ba', 'Thứ Tư', 'Thứ Năm', 'Thứ Sáu', 'Thứ Bảy']
  const dayName = daysOfWeek[inputDate.getDay()]

  if (diffInDays === 0) {
    return 'Hôm nay'
  } else if (diffInDays === -1) {
    return 'Hôm qua'
  } else if (diffInDays === 1) {
    return 'Ngày mai'
  } else {
    return dayName
  }
}

export const colorDayOfWeek = (timestamp) => {
  const inputDate = new Date(timestamp)
  const daysOfWeek = ['red', 'green', 'cyan', 'blue', 'purple', 'orange', 'pink']
  const color = daysOfWeek[inputDate.getDay()]
  return color
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

export const getShiftTimeStart = (timestamp, startHour, startMinute) => {
  return dayjs(timestamp)
    .set('hour', startHour)
    .set('minute', startMinute)
    .set('second', 0)
    .set('millisecond', 0)
    .valueOf()
}

export const getShiftTimeEnd = (timestamp, endHour, endMinute) => {
  return dayjs(timestamp)
    .set('hour', endHour)
    .set('minute', endMinute)
    .set('second', 0)
    .set('millisecond', 0)
    .valueOf()
}

export const rowSelectTable = (selectedRowKeys, isDisabled = () => false, rowKey = 'id') => {
  return {
    selectedRowKeys: unref(selectedRowKeys),
    onChange: (changableRowKeys) => {
      selectedRowKeys.value = changableRowKeys
    },
    hideDefaultSelections: true,
    getCheckboxProps: (record) => ({
      disabled: isDisabled(record[rowKey]),
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

export const isProbablyMobile = () => {
  return (
    window.innerWidth <= 768 &&
    navigator.maxTouchPoints > 1 &&
    window.matchMedia('(orientation: portrait)').matches
  )
}

export const base64ToBlob = (base64, contentType = 'image/jpeg') => {
  const byteCharacters = atob(base64.split(',')[1])
  const byteArrays = []

  for (let offset = 0; offset < byteCharacters.length; offset += 512) {
    const slice = byteCharacters.slice(offset, offset + 512)

    const byteNumbers = new Array(slice.length)
    for (let i = 0; i < slice.length; i++) {
      byteNumbers[i] = slice.charCodeAt(i)
    }

    const byteArray = new Uint8Array(byteNumbers)
    byteArrays.push(byteArray)
  }

  return new Blob(byteArrays, { type: contentType })
}
