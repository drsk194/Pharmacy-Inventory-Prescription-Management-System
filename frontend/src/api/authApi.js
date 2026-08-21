import apiClient from "./client";

export const authApi = {
  login: (identifier, password) => apiClient.post("/api/auth/login", { identifier, password }),
  register: (payload) => apiClient.post("/api/auth/register", { ...payload, role: "ROLE_PATIENT" }),
  me: () => apiClient.get("/api/auth/me"),
  logout: () => apiClient.post("/api/auth/logout"),
  logoutAll: () => apiClient.post("/api/auth/logout-all"),
  forgotPassword: (identifier) => apiClient.post("/api/auth/forgot-password", { identifier }),
  resetPassword: (payload) => apiClient.post("/api/auth/reset-password", payload),
  changePassword: (payload) => apiClient.put("/api/auth/change-password", payload),
};
