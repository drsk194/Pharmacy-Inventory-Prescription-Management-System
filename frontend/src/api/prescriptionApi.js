import apiClient from "./client";

export const prescriptionApi = {
  create: (payload) => apiClient.post("/api/prescriptions", payload),
  getQueue: (params) => apiClient.get("/api/prescriptions/queue", { params }),
  getMy: (params) => apiClient.get("/api/prescriptions/my", { params }),
  getById: (id) => apiClient.get(`/api/prescriptions/${id}`),
  getHistory: (id) => apiClient.get(`/api/prescriptions/${id}/history`),
  process: (id, payload) => apiClient.put(`/api/prescriptions/${id}/process`, payload),
  verify: (id, payload) => apiClient.put(`/api/prescriptions/${id}/verify`, payload),
  reject: (id, payload) => apiClient.put(`/api/prescriptions/${id}/reject`, payload),
};
