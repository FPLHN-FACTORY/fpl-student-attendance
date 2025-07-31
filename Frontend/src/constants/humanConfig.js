import { backend } from '@tensorflow/tfjs'

export default {
  async: true,
  modelBasePath: '/models/human',
  cacheModels: true,
  cacheSensitivity: 0.01,
  debug: false,
  face: {
    enabled: true,
    detector: {
      rotation: true,
      inputSize: 448,
      maxDetected: 1,
      iouThreshold: 0.3,
      minConfidence: 0.3,
      square: true,
    },
    iris: { enabled: false },
    description: { enabled: true },
    embedding: { enabled: true },
    emotion: { enabled: true },
    mesh: { enabled: true },
    antispoof: { enabled: true },
    liveness: { enabled: true },
  },
  body: { enabled: false },
  hand: { enabled: false },
  object: { enabled: false },
  gesture: { enabled: true },
}
