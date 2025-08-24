import { Facility } from './Facility'

export interface UserStudent {
  id?: string
  email?: string
  name?: string
  code?: string
  image?: string
  faceEmbedding?: string
  facility?: Facility
}
