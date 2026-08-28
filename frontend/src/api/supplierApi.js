import apiClient from "./client";
export const supplierApi = {
  list: (params) => apiClient.get("/api/suppliers", { params }),
  getById: (id) => apiClient.get(`/api/suppliers/${id}`),
  create: (payload) => apiClient.post("/api/suppliers", payload),
  update: (id, payload) => apiClient.put(`/api/suppliers/${id}`, payload),
  setApproval: (id, approved) => apiClient.patch(`/api/suppliers/${id}/approval`, null, { params: { approved } }),
};
