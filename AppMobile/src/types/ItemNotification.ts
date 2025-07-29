export interface ItemNotification {
  id: string
  type: number
  status: number
  createdAt: number
  data: Record<string, any>
  message: string
}
