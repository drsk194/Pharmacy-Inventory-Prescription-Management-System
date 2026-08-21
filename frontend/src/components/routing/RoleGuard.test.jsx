import { describe, expect, it, vi } from "vitest";
import { render, screen } from "@testing-library/react";
import { MemoryRouter, Route, Routes } from "react-router-dom";
import RoleGuard from "./RoleGuard";

vi.mock("../../hooks/useAuth", () => ({ useAuth: vi.fn() }));
import { useAuth } from "../../hooks/useAuth";

function renderGuard(roles) {
  useAuth.mockReturnValue({ user: { roles } });
  return render(<MemoryRouter initialEntries={["/admin-only"]}><Routes><Route path="/admin-only" element={<RoleGuard allow={["ROLE_ADMIN"]}><div>Admin content</div></RoleGuard>} /><Route path="/not-authorized" element={<div>Not authorized</div>} /></Routes></MemoryRouter>);
}

describe("RoleGuard", () => {
  it("renders allowed content", () => { renderGuard(["ROLE_ADMIN"]); expect(screen.getByText("Admin content")).toBeInTheDocument(); });
  it("redirects disallowed roles", () => { renderGuard(["ROLE_TECHNICIAN"]); expect(screen.getByText("Not authorized")).toBeInTheDocument(); });
});
