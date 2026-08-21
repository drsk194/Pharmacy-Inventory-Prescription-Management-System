import { useState, useEffect, useCallback } from "react";
import { AuthContext } from "./contextValue";
import apiClient from "../api/client";
import { authApi } from "../api/authApi";
import { setTokens, clearTokens, registerSessionExpiredHandler } from "./authStore";

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [accessToken, setAccessToken] = useState(null);
  const [isInitializing, setIsInitializing] = useState(true);

  const clearSession = useCallback(() => {
    clearTokens();
    setAccessToken(null);
    setUser(null);
  }, []);

  const login = useCallback(async (identifier, password) => {
    const response = await authApi.login(identifier, password);
    const { accessToken: token, refreshToken } = response.data.data;
    setTokens(token, refreshToken);
    setAccessToken(token);
    const meResponse = await authApi.me();
    setUser(meResponse.data.data);
    return meResponse.data.data;
  }, []);

  const logout = useCallback(async () => {
    try { await authApi.logout(); } catch { /* Local session still needs clearing. */ }
    clearSession();
  }, [clearSession]);

  const logoutAll = useCallback(async () => {
    try { await authApi.logoutAll(); } finally { clearSession(); }
  }, [clearSession]);

  useEffect(() => {
    async function restoreSession() {
      try {
        const response = await apiClient.post("/api/auth/refresh");
        const { accessToken: token, refreshToken } = response.data.data;
        setTokens(token, refreshToken);
        setAccessToken(token);
        const meResponse = await authApi.me();
        setUser(meResponse.data.data);
      } catch { clearSession(); }
      finally { setIsInitializing(false); }
    }
    restoreSession();
  }, [clearSession]);

  useEffect(() => {
    registerSessionExpiredHandler(() => {
      clearSession();
      window.location.href = "/login";
    });
  }, [clearSession]);

  return <AuthContext.Provider value={{ user, accessToken, isAuthenticated: Boolean(user), isInitializing, login, logout, logoutAll }}>{children}</AuthContext.Provider>;
}
