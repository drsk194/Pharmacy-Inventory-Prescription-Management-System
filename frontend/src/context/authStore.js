const REFRESH_TOKEN_KEY = "pipms_refresh_token";

let accessToken = null;
let refreshToken = typeof window !== "undefined" ? localStorage.getItem(REFRESH_TOKEN_KEY) : null;
let onSessionExpired = () => {};

export function getAccessToken() { return accessToken; }
export function getRefreshToken() {
  if (!refreshToken && typeof window !== "undefined") {
    refreshToken = localStorage.getItem(REFRESH_TOKEN_KEY);
  }
  return refreshToken;
}
export function setTokens(newAccessToken, newRefreshToken) {
  accessToken = newAccessToken;
  if (newRefreshToken !== undefined) {
    refreshToken = newRefreshToken;
    if (typeof window !== "undefined") {
      if (newRefreshToken) {
        localStorage.setItem(REFRESH_TOKEN_KEY, newRefreshToken);
      } else {
        localStorage.removeItem(REFRESH_TOKEN_KEY);
      }
    }
  }
}
export function clearTokens() {
  accessToken = null;
  refreshToken = null;
  if (typeof window !== "undefined") {
    localStorage.removeItem(REFRESH_TOKEN_KEY);
  }
}
export function registerSessionExpiredHandler(handler) { onSessionExpired = handler; }
export function notifySessionExpired() { onSessionExpired(); }
