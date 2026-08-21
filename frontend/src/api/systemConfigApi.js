import apiClient from "./client";
export const systemConfigApi = { list: (params) => apiClient.get("/api/admin/config", { params }), create: (payload) => apiClient.post("/api/admin/config", payload), update: (key, payload) => apiClient.put(`/api/admin/config/${key}`, payload) };
