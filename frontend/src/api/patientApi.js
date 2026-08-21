import apiClient from "./client";
export const patientApi = {
  list: (params) => apiClient.get("/api/patients", { params }),
  getById: (id) => apiClient.get(`/api/patients/${id}`),
  create: (payload) => apiClient.post("/api/patients", payload),
  update: (id, payload) => apiClient.put(`/api/patients/${id}`, payload),
  me: () => apiClient.get("/api/patients/me"),
  updateMe: (payload) => apiClient.put("/api/patients/me", payload),
  getAllergies: (id) => apiClient.get(`/api/patients/${id}/allergies`),
  addAllergy: (id, payload) => apiClient.post(`/api/patients/${id}/allergies`, payload),
  getConditions: (id) => apiClient.get(`/api/patients/${id}/conditions`),
  addCondition: (id, payload) => apiClient.post(`/api/patients/${id}/conditions`, payload),
  getMedications: (id) => apiClient.get(`/api/patients/${id}/medications`),
  addMedication: (id, payload) => apiClient.post(`/api/patients/${id}/medications`, payload),
};
