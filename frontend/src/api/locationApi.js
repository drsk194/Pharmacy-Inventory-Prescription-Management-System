import apiClient from "./client";
export const locationApi = { list: () => apiClient.get("/api/inventory/locations"), create: (payload) => apiClient.post("/api/inventory/locations", payload) };
