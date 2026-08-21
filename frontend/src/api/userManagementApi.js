import apiClient from "./client";
export const userManagementApi = { list: (params) => apiClient.get("/api/admin/users", { params }), create: (payload) => apiClient.post("/api/admin/users", payload), setStatus: (id, active) => apiClient.patch(`/api/admin/users/${id}/status`, { active }), setRoles: (id, roles) => apiClient.patch(`/api/admin/users/${id}/roles`, { roles }) };
