import { http, HttpResponse } from "msw";

const BASE_URL = "http://localhost:8080";

export const handlers = [
  http.post(`${BASE_URL}/api/auth/login`, async ({ request }) => {
    const body = await request.json();
    if (body.identifier === "admin@pipms.test" && body.password === "correct-password") {
      return HttpResponse.json({ success: true, data: { accessToken: "mock-access-token", refreshToken: "mock-refresh-token" } });
    }
    return HttpResponse.json({ success: false, message: "Invalid credentials" }, { status: 401 });
  }),
  http.get(`${BASE_URL}/api/auth/me`, () => HttpResponse.json({ success: true, data: { id: 1, name: "Admin User", staffId: "ADM0001", roles: ["ROLE_ADMIN"] } })),
  http.post(`${BASE_URL}/api/auth/refresh`, () => HttpResponse.json({ success: false, message: "No session" }, { status: 401 })),
  http.get(`${BASE_URL}/api/patients`, () => HttpResponse.json({ success: true, data: { content: [{ id: 1, fullName: "Jane Doe", dateOfBirth: "1990-01-01", phone: "1234567890" }], totalPages: 1 } })),
];
