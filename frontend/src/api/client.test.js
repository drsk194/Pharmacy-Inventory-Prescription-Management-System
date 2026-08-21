import { beforeEach, describe, expect, it } from "vitest";
import { http, HttpResponse } from "msw";
import { server } from "../test/mocks/server";
import apiClient from "./client";
import { clearTokens, setTokens } from "../context/authStore";

const BASE_URL = "http://localhost:8080";

describe("Axios refresh interceptor", () => {
  beforeEach(() => clearTokens());

  it("retries once after a successful refresh", async () => {
    setTokens("expired", "refresh");
    let calls = 0;
    server.use(http.get(`${BASE_URL}/api/protected`, () => { calls += 1; return calls === 1 ? HttpResponse.json({}, { status: 401 }) : HttpResponse.json({ data: { ok: true } }); }), http.post(`${BASE_URL}/api/auth/refresh`, () => HttpResponse.json({ data: { accessToken: "new", refreshToken: "new-refresh" } })));
    const response = await apiClient.get("/api/protected");
    expect(response.data.data.ok).toBe(true);
    expect(calls).toBe(2);
  });
});
