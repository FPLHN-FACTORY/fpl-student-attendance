<template>
  <div class="flex flex-col items-center justify-center h-screen bg-gray-100">
    <h1 class="text-9xl font-extrabold text-red-600 animate-pulse">403</h1>
    <p class="text-xl text-gray-600 mt-4">Bạn không có quyền truy cập trang này.</p>

    <div class="mt-6">
      <router-link
        to="/"
        class="px-6 py-3 bg-red-500 text-white font-semibold rounded-lg shadow-lg hover:bg-red-700 transition"
      >
        Quay về trang chủ
      </router-link>
    </div>
  </div>
</template>

<style scoped>
h1 {
  text-shadow: 4px 4px 10px rgba(0, 0, 0, 0.2);
}
</style>
