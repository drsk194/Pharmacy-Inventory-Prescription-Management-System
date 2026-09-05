import axios from "axios";
import { getAccessToken, getRefreshToken, setTokens, clearTokens, notifySessionExpired } from "../context/authStore";

const BASE_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";
const apiClient = axios.create({ baseURL: BASE_URL });

apiClient.interceptors.request.use((config) => {
  const token = getAccessToken();
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

let isRefreshing = false;
let pendingQueue = [];
function resolvePendingQueue(error, token) {
  pendingQueue.forEach(({ resolve, reject }) => error ? reject(error) : resolve(token));
  pendingQueue = [];
}

apiClient.interceptors.response.use((response) => response, async (error) => {
  const originalRequest = error.config;
  const status = error.response?.status;
  const fieldErrors = error.response?.data?.fieldErrors;
  if (fieldErrors && Object.keys(fieldErrors).length > 0 && error.response?.data) {
    const detail = Object.entries(fieldErrors).map(([field, msg]) => `${field}: ${msg}`).join("; ");
    error.response.data.message = `${error.response.data.message || "Validation failed"} — ${detail}`;
  }
  const isAuthEndpoint = originalRequest?.url?.includes("/api/auth/login") || originalRequest?.url?.includes("/api/auth/refresh");
  if (status !== 401 || isAuthEndpoint || originalRequest?._retry) return Promise.reject(error);

  if (isRefreshing) {
    return new Promise((resolve, reject) => pendingQueue.push({ resolve, reject })).then((newToken) => {
      originalRequest.headers.Authorization = `Bearer ${newToken}`;
      return apiClient(originalRequest);
    });
  }

  originalRequest._retry = true;
  isRefreshing = true;
  try {
    const response = await axios.post(`${BASE_URL}/api/auth/refresh`, { refreshToken: getRefreshToken() });
    const { accessToken, refreshToken } = response.data.data;
    setTokens(accessToken, refreshToken);
    resolvePendingQueue(null, accessToken);
    originalRequest.headers.Authorization = `Bearer ${accessToken}`;
    return apiClient(originalRequest);
  } catch (refreshError) {
    resolvePendingQueue(refreshError, null);
    clearTokens();
    notifySessionExpired();
    return Promise.reject(refreshError);
  } finally {
    isRefreshing = false;
  }
});

export default apiClient;
