import apiClient from "./client";
export const auditLogApi = { search: (params) => apiClient.get("/api/admin/audit-logs", { params }), getById: (id) => apiClient.get(`/api/admin/audit-logs/${id}`), export: (params) => apiClient.get("/api/admin/audit-logs/export", { params, responseType: "blob" }) };
