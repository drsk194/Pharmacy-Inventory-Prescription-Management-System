import apiClient from "./client";
export const adminApi = { getReportsDirectory: () => apiClient.get("/api/admin/reports"), getAnalytics: () => apiClient.get("/api/admin/analytics") };
