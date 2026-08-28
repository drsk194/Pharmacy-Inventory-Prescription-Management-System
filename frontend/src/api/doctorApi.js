import apiClient from "./client";
export const doctorApi = {
  list: (params) => apiClient.get("/api/doctors", { params }),
  getById: (id) => apiClient.get(`/api/doctors/${id}`),
  create: (payload) => apiClient.post("/api/doctors", payload),
  update: (id, payload) => apiClient.put(`/api/doctors/${id}`, payload),
  me: () => apiClient.get("/api/doctors/me"),
  updateMe: (payload) => apiClient.put("/api/doctors/me", payload),
  verify: (id, verified) => apiClient.patch(`/api/doctors/${id}/verify`, null, { params: { verified } }),
  setCsAuthorization: (id, payload) => apiClient.patch(`/api/doctors/${id}/controlled-substance-authorization`, payload),
  setLicenseExpiry: (id, expiryDate) => apiClient.patch(`/api/doctors/${id}/license-expiry`, null, { params: { expiryDate } }),
};
