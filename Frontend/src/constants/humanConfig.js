export default {
  async: true,
  backend: 'webgl',
  modelBasePath: '/models/human',
  cacheModels: true,
  cacheSensitivity: 0.01,
  debug: false,
  face: {
    enabled: true,
    detector: {
      rotation: true, // giúp phát hiện mặt nghiêng
      inputSize: 256,
      maxDetected: 1, // nếu chỉ cần 1 người
      iouThreshold: 0.5,
      minConfidence: 0.5, // giảm nếu muốn bắt được mặt khó
    },
    iris: { enabled: false }, // tắt nếu không cần tracking mắt
    description: { enabled: true }, // tắt nếu không cần thông tin tuổi/giới tính
    embedding: { enabled: true }, // giữ lại để lấy vector đặc trưng khuôn mặt
    emotion: { enabled: true }, // tắt nếu không cần nhận diện biểu cảm
    mesh: { enabled: true }, // tắt nếu không cần vẽ điểm mặt
    antispoof: { enabled: true },
    liveness: { enabled: false },
  },
  body: { enabled: false },
  hand: { enabled: false },
  object: { enabled: false },
  gesture: { enabled: false },
}
