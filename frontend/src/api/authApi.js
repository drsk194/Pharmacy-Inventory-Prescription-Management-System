import apiClient from "./client";
import { getRefreshToken } from "../context/authStore";

export const authApi = {
  login: (identifier, password) => apiClient.post("/api/auth/login", { identifier, password }),
  register: (payload) => apiClient.post("/api/auth/register", { ...payload, role: "ROLE_PATIENT" }),
  me: () => apiClient.get("/api/auth/me"),
  logout: (refreshToken) => apiClient.post("/api/auth/logout", { refreshToken: refreshToken || getRefreshToken() || "" }),
  logoutAll: () => apiClient.post("/api/auth/logout-all"),
  forgotPassword: (email) => apiClient.post("/api/auth/forgot-password", { email }),
  resetPassword: (payload) => apiClient.post("/api/auth/reset-password", payload),
  changePassword: (payload) => apiClient.put("/api/auth/change-password", payload),
};
