export const decodeBase64 = (base64String) => {
  const fixedBase64 = base64String
  const byteArray = Uint8Array.from(atob(fixedBase64), (c) => c.charCodeAt(0))
  return new TextDecoder('utf-8').decode(byteArray)
}
