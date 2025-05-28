export const localStorageUtils = {
  get: (key, defaultValue) => {
    const value = localStorage.getItem(key)
    return value ? JSON.parse(value) : defaultValue
  },

  set: (key, value) => localStorage.setItem(key, JSON.stringify(value)),

  remove: (key) => localStorage.removeItem(key),

  clear: () => localStorage.clear(),
}
