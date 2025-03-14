import { defineStore } from 'pinia'
import { reactive, ref } from 'vue'

const useImportExcelStore = defineStore('importExcel', () => {
  const STATUS_PROCESS = {
    PENDING: 0,
    UPLOADING: 1,
    COMPLETE: 2,
    ERROR: 3,
  }

  const queue = ref([])

  const status = reactive({
    isProcessing: false,
    isStopped: false,
  })

  async function fakeUpload(file) {
    console.log(`🔄 Đang upload: ${file.name}...`)
    return new Promise(
      (resolve) => setTimeout(() => resolve(`Uploaded ${file.name}`), 2000), // Giả lập delay 2s
    )
  }

  const generateTimeUUID = () => {
    return Date.now().toString(36) + '-' + Math.random().toString(36).substr(2, 9)
  }

  const wrapperDataFile = (file) => {
    return {
      id: generateTimeUUID(),
      file,
      data: [],
      error: 0,
      success: 0,
      status: STATUS_PROCESS.PENDING,
    }
  }

  const enqueue = (file) => {
    status.isStopped = false

    queue.value.push(wrapperDataFile(file))
    console.log('Đã thêm ' + file.name + ' vào hàng đợi')

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
        return console.log('🛑 Hàng đợi bị dừng hẳn')
      }

      const task = queue.value.find((o) => o.status === STATUS_PROCESS.PENDING)

      if (!task) {
        status.isProcessing = false
        return console.log('upload hoàn thành')
      }

      task.status = STATUS_PROCESS.UPLOADING

      try {
        await fakeUpload(task.file)
        task.status = STATUS_PROCESS.COMPLETE
        console.log(`✅ Upload thành công: ${task.file}`)
      } catch (error) {
        task.status = STATUS_PROCESS.ERROR
        console.error(`❌ Upload thất bại: ${task.file}`, error)
      }
    }
  }

  const size = () => {
    return queue.value.length
  }

  const stop = () => {
    status.isStopped = true
    if (!status.isProcessing) {
      queue.value = []
    }
    console.log('🛑 Hàng đợi đã bị dừng hẳn.')
  }

  const isProcessing = () => {
    return status.isProcessing
  }

  const isStoped = () => {
    return status.isStopped
  }

  return { queue, enqueue, stop, size, isProcessing, isStoped }
})

export default useImportExcelStore
