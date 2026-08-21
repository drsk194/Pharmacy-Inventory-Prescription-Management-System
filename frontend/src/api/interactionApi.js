import apiClient from "./client";
export const interactionApi = {
  list: (params) => apiClient.get("/api/drug-interactions", { params }),
  create: (payload) => apiClient.post("/api/drug-interactions", payload),
};
