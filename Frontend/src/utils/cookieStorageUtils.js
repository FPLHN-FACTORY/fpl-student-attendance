export const cookieStorageUtils = {
  get: (key) => {
    const cookieArr = document.cookie.split('; ')
    for (let i = 0, length = cookieArr.length; i < length; i++) {
      const kv = cookieArr[i].split('=')
      if (kv[0] === key) {
        return kv[1]
      }
    }
    return ''
  },

  set: (key, value, expire = 60 * 60 * 0.5) =>
    (document.cookie = `${key}=${value}; Max-Age=${expire}`),

  remove: (key) => (document.cookie = `${key}=${1}; Max-Age=${-1}`),

  clear: () => {
    const keys = document.cookie.match(/[^ =;]+(?==)/g)
    if (keys) {
      for (let i = keys.length; i--; ) {
        document.cookie = `${keys[i]}=0;expire=${new Date(0).toUTCString()}`
      }
    }
  },
}
