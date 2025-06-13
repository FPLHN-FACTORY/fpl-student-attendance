export default {
  backend: 'webgl',
  modelBasePath: '/models/human',
  cacheModels: true,
  debug: false,
  face: {
    enabled: true,
    detector: {
      rotation: true, // giúp phát hiện mặt nghiêng
      inputSize: 256,
      maxDetected: 1, // nếu chỉ cần 1 người
      minConfidence: 0.6, // giảm nếu muốn bắt được mặt khó
      scoreThreshold: 0.6,
    },
    iris: { enabled: false }, // tắt nếu không cần tracking mắt
    description: { enabled: true }, // tắt nếu không cần thông tin tuổi/giới tính
    embedding: { enabled: true }, // giữ lại để lấy vector đặc trưng khuôn mặt
    emotion: { enabled: true }, // tắt nếu không cần nhận diện biểu cảm
    mesh: { enabled: true }, // tắt nếu không cần vẽ điểm mặt
    liveness: { enabled: true },
  },
  body: { enabled: false },
  hand: { enabled: false },
  object: { enabled: false },
  gesture: { enabled: false },
}
