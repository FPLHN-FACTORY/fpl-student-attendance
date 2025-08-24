export interface Factory {
  id: string
  name: string
  description?: string | null
  status: 'ACTIVE' | 'INACTIVE'
  createdAt?: number
  updatedAt?: number
}
