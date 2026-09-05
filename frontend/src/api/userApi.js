import apiClient from "./client";
export const userApi = {
  controlledSubstanceStaff: () => apiClient.get("/api/users/controlled-substance-staff"),
};
