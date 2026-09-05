import apiClient from "./client";
export const drugApi = {
  getCatalog: (params) => apiClient.get("/api/drugs/catalog", { params }),
  list: (params) => apiClient.get("/api/drugs", { params }),
  getById: (id) => apiClient.get(`/api/drugs/${id}`),
  create: (payload) => apiClient.post("/api/drugs", payload),
  update: (id, payload) => apiClient.put(`/api/drugs/${id}`, payload),
  setStatus: (id, active) => apiClient.patch(`/api/drugs/${id}/status`, null, { params: { active } }),
  getLowStock: (params) => apiClient.get("/api/drugs/low-stock", { params }),
  getNearExpiry: (params) => apiClient.get("/api/drugs/near-expiry", { params }),
};
