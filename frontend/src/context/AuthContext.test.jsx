import { describe, expect, it } from "vitest";
import { screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { renderWithProviders } from "../test/test-utils";
import { useAuth } from "../hooks/useAuth";

function Probe() { const { isAuthenticated, user, login } = useAuth(); return <div><span>{isAuthenticated ? `Logged in as ${user?.staffId}` : "Logged out"}</span><button type="button" onClick={() => login("admin@pipms.test", "correct-password")}>Login</button></div>; }

describe("AuthContext", () => {
  it("populates the user after login and /me", async () => {
    const user = userEvent.setup();
    renderWithProviders(<Probe />);
    await waitFor(() => expect(screen.getByText("Logged out")).toBeInTheDocument());
    await user.click(screen.getByRole("button", { name: "Login" }));
    await waitFor(() => expect(screen.getByText("Logged in as ADM0001")).toBeInTheDocument());
  });
});
