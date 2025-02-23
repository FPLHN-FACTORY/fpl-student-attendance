export const sessionStorageUtils = {
  get: (key, defaultValue) => {
    const value = sessionStorage.getItem(key)
    return value ? JSON.parse(value) : defaultValue
  },

  set: (key, value) => sessionStorage.setItem(key, JSON.stringify(value)),

  remove: (key) => sessionStorage.removeItem(key),

  clear: () => sessionStorage.clear(),
}
