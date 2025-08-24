import requestAPI from '@/services/requestApiService'
import { message } from 'ant-design-vue'
import { defineStore } from 'pinia'
import { reactive, ref } from 'vue'
import { toast } from 'vue3-toastify'
import * as XLSX from 'xlsx'
import jsPDF from 'jspdf'
import autoTable from 'jspdf-autotable'
import Roboto from '@/assets/js/fonts/Roboto-Regular-normal.js'
import RobotoBold from '@/assets/js/fonts/Roboto-Bold-normal'

const useImportExcelStore = defineStore('importExcel', () => {
  const STATUS_PROCESS = {
    PENDING: 0,
    UPLOADING: 1,
    COMPLETE: 2,
    ERROR: 3,
    CANCEL: 4,
  }

  const STATUS_LOG = {
    SUCCESS: 0,
    FAILED: 1,
  }
  const urlFetch = ref(null)
  const dataFetch = ref(null)
  const queue = ref([])

  const status = reactive({
    isProcessing: false,
    isStopped: false,
  })

  const generateTimeUUID = () => {
    return Date.now().toString(36) + '-' + Math.random().toString(36).substr(2, 9)
  }

  const wrapperDataFile = (file, { fetchUrl, onSuccess, onError, data }) => {
    return {
      id: generateTimeUUID(),
      options: {
        data,
        fetchUrl,
        onSuccess,
        onError,
      },
      file,
      items: [],
      percent: 0,
      status: STATUS_PROCESS.PENDING,
    }
  }

  const wrapperDataItems = (index, data) => {
    return {
      index,
      data,
      status: null,
      message: null,
      isComplete: false,
    }
  }

  const enqueue = (file, options) => {
    status.isStopped = false

    queue.value.push(wrapperDataFile(file, options))

    if (!status.isProcessing) {
      processQueue()
    }
  }

  const processQueue = async () => {
    if (status.isProcessing || queue.value.length === 0) {
      return
    }

    status.isProcessing = true
    status.isStopped = false

    while (true) {
      if (status.isStopped) {
        queue.value = []
        status.isProcessing = false
        return
      }

      const task = queue.value.find((o) => o.status === STATUS_PROCESS.PENDING)

      if (!task) {
        status.isProcessing = false
        return
      }

      task.status = STATUS_PROCESS.UPLOADING

      const formData = new FormData()
      formData.append('id', task.id)
      formData.append('name', task.file.name)
      formData.append('file', task.file)

      try {
        const { data: response } = await requestAPI.post(
          `${task.options.fetchUrl}/upload`,
          formData,
          {
            headers: {
              'Content-Type': 'multipart/form-data',
            },
          },
        )

        if (response.data.length < 1) {
          task.status = STATUS_PROCESS.ERROR
          toast.warning('Template không hợp lệ: ' + task.file.name)
          continue
        }

        response.data.forEach((o, i) => {
          task.items.push(wrapperDataItems(Number(o['_LINE']), o))
        })

        while (true) {
          if (status.isStopped) {
            queue.value = []
            status.isProcessing = false
            return
          }

          if (task.status !== STATUS_PROCESS.UPLOADING) {
            break
          }

          const item = task.items.find((o) => !o.isComplete)
          if (!item) {
            task.status = STATUS_PROCESS.COMPLETE
            break
          }
          task.percent = (task.items.filter((o) => o.isComplete).length / task.items.length) * 100

          await requestAPI
            .post(`${task.options.fetchUrl}/import`, {
              code: task.id,
              fileName: task.file.name,
              line: item.index,
              data: task.options.data,
              item: item.data,
            })
            .then(async ({ data: response }) => {
              item.message = response.message || 'Import thành công'
              item.status = STATUS_LOG.SUCCESS
            })
            .catch((error) => {
              item.message = error?.response?.data?.message || 'Import thất bại'
              item.status = STATUS_LOG.FAILED
            })
            .finally(() => {
              item.isComplete = true
            })
        }
        typeof task.options.onSuccess == 'function' && task.options.onSuccess(task)
      } catch (error) {
        task.status = STATUS_PROCESS.ERROR
        typeof task.options.onError == 'function' && task.options.onError(error?.response || error)
      }
    }
  }

  const size = () => {
    return queue.value.length
  }

  const stop = () => {
    if (!status.isProcessing) {
      queue.value = []
    }
    status.isStopped = true
    status.isProcessing = false
  }

  const cancelTask = (id) => {
    const task = queue.value.find(
      (o) =>
        o.id === id &&
        (o.status === STATUS_PROCESS.UPLOADING || o.status === STATUS_PROCESS.PENDING),
    )
    if (task) {
      task.status = STATUS_PROCESS.CANCEL
    }
  }

  const retryTask = (id) => {
    const task = queue.value.find((o) => o.id === id && o.status === STATUS_PROCESS.ERROR)
    if (task) {
      task.status = STATUS_PROCESS.PENDING
      if (!status.isProcessing) {
        processQueue()
      }
    }
  }

  const isProcessing = () => {
    return status.isProcessing
  }

  const isStoped = () => {
    return status.isStopped
  }

  const countProcessing = () => {
    return queue.value.filter(
      (o) => o.status === STATUS_PROCESS.PENDING || o.status === STATUS_PROCESS.UPLOADING,
    ).length
  }

  const countComplete = () => {
    return queue.value.filter((o) => o.status === STATUS_PROCESS.COMPLETE).length
  }

  const setUrlFetch = (url) => (urlFetch.value = url)
  const setData = (data) => {
    dataFetch.value = data
  }

  const downloadTemplate = async () => {
    if (urlFetch.value) {
      await requestAPI
        .post(
          `${urlFetch.value}/download-template`,
          { data: dataFetch.value },
          {
            responseType: 'blob',
          },
        )
        .then((response) => {
          const blob = new Blob([response.data], {
            type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
          })
          const contentDisposition = response.headers['content-disposition']
          let filename = 'template-download'
          if (contentDisposition) {
            const match = contentDisposition.match(/filename="(.+)"/)
            if (match) {
              filename = match[1]
            }
          }
          const url = window.URL.createObjectURL(blob)
          const a = document.createElement('a')
          a.href = url
          a.download = filename
          document.body.appendChild(a)
          a.click()
          document.body.removeChild(a)

          window.URL.revokeObjectURL(url)
        })
        .catch((error) => {
          message.error(error?.response?.data?.message || 'Không thể tải xuống template')
        })
    }
  }

  const exportExcel = async () => {
    if (urlFetch.value) {
      await requestAPI
        .post(
          `${urlFetch.value}/export`,
          { data: dataFetch.value },
          {
            responseType: 'blob',
          },
        )
        .then((response) => {
          const blob = new Blob([response.data], {
            type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
          })
          const contentDisposition = response.headers['content-disposition']
          let filename = 'export-download'
          if (contentDisposition) {
            const match = contentDisposition.match(/filename="(.+)"/)
            if (match) {
              filename = match[1]
            }
          }
          const url = window.URL.createObjectURL(blob)
          const a = document.createElement('a')
          a.href = url
          a.download = filename
          document.body.appendChild(a)
          a.click()
          document.body.removeChild(a)

          window.URL.revokeObjectURL(url)
        })
        .catch((error) => {
          message.error(error?.response?.data?.message || 'Không thể xuất excel')
        })
    }
  }

  const exportPDF = async (didParseCell) => {
    if (urlFetch.value) {
      await requestAPI
        .post(
          `${urlFetch.value}/export`,
          { data: dataFetch.value },
          {
            responseType: 'blob',
          },
        )
        .then(async (response) => {
          const blob = new Blob([response.data], {
            type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
          })

          const contentDisposition = response.headers['content-disposition']
          let filename = 'export-download'
          if (contentDisposition) {
            const match = contentDisposition.match(/filename="(.+)"/)
            if (match) {
              filename = match[1].replace('.xlsx', '.pdf')
            }
          }

          const arrayBuffer = await blob.arrayBuffer()
          const workbook = XLSX.read(arrayBuffer, { type: 'array' })

          const sheetName = workbook.SheetNames[0]
          const worksheet = workbook.Sheets[sheetName]
          const data = XLSX.utils.sheet_to_json(worksheet, { header: 1 })

          const doc = new jsPDF()
          doc.addFileToVFS('Roboto-Regular.ttf', Roboto)
          doc.addFileToVFS('Roboto-Bold.ttf', RobotoBold)
          doc.addFont('Roboto-Regular.ttf', 'Roboto', 'normal')
          doc.addFont('Roboto-Bold.ttf', 'Roboto', 'bold')
          doc.setFont('Roboto')

          if (data.length > 0) {
            autoTable(doc, {
              head: [data[0]],
              body: data.slice(1),
              styles: {
                font: 'Roboto',
                cellWidth: 'wrap',
                fontSize: 10,
              },
              didParseCell,
            })
            doc.save(filename)
          } else {
            message.warning('Không có dữ liệu cần export')
          }
        })
        .catch((error) => {
          message.error(error?.response?.data?.message || 'Không thể xuất excel')
        })
    }
  }

  const getHistoryLogs = (pagination) => {
    return requestAPI.post(`${urlFetch.value}/history-log`, {
      ...dataFetch.value,
      page: pagination.value.current,
      size: pagination.value.pageSize,
    })
  }

  const getHistoryLogsDetail = (id) => {
    return requestAPI.post(`${urlFetch.value}/history-log/${id}`, dataFetch.value)
  }

  return {
    queue,
    enqueue,
    stop,
    size,
    isProcessing,
    isStoped,
    cancelTask,
    retryTask,
    STATUS_PROCESS,
    STATUS_LOG,
    countProcessing,
    countComplete,
    downloadTemplate,
    exportExcel,
    exportPDF,
    getHistoryLogs,
    getHistoryLogsDetail,
    setUrlFetch,
    setData,
  }
})

export default useImportExcelStore
