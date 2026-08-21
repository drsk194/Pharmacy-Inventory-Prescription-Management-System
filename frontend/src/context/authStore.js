let accessToken = null;
let refreshToken = null;
let onSessionExpired = () => {};

export function getAccessToken() { return accessToken; }
export function getRefreshToken() { return refreshToken; }
export function setTokens(newAccessToken, newRefreshToken) {
  accessToken = newAccessToken;
  if (newRefreshToken !== undefined) refreshToken = newRefreshToken;
}
export function clearTokens() { accessToken = null; refreshToken = null; }
export function registerSessionExpiredHandler(handler) { onSessionExpired = handler; }
export function notifySessionExpired() { onSessionExpired(); }
