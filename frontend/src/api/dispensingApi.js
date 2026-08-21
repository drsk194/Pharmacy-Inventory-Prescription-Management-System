import apiClient from "./client";

export const dispensingApi = {
  prepare: (payload) => apiClient.post("/api/dispensing/prepare", payload),
  authorize: (id) => apiClient.put(`/api/dispensing/${id}/authorize`),
  getLabel: (id) => apiClient.get(`/api/dispensing/${id}/label`),
  acknowledge: (id, payload) => apiClient.put(`/api/dispensing/${id}/acknowledge`, payload),
  submitCounselling: (payload) => apiClient.post("/api/dispensing/counselling", payload),
  submitReturn: (payload) => apiClient.post("/api/dispensing/returns", payload),
  submitError: (payload) => apiClient.post("/api/dispensing/errors", payload),
  getBalanceOrders: (params) => apiClient.get("/api/dispensing/balance-orders", { params }),
};
