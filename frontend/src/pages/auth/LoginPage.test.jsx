import { describe, expect, it } from "vitest";
import userEvent from "@testing-library/user-event";
import { screen, waitFor } from "@testing-library/react";
import { renderWithProviders } from "../../test/test-utils";
import LoginPage from "./LoginPage";

describe("LoginPage", () => {
  it("shows an API error for invalid credentials", async () => {
    const user = userEvent.setup();
    renderWithProviders(<LoginPage />, { route: "/login" });
    await user.type(screen.getByLabelText(/email or staff id/i), "wrong@pipms.test");
    await user.type(screen.getByLabelText(/password/i), "wrong-password");
    await user.click(screen.getByRole("button", { name: /sign in/i }));
    await waitFor(() => expect(screen.getByRole("alert")).toHaveTextContent(/invalid credentials/i));
  });

  it("allows switching between login and register", async () => {
    const user = userEvent.setup();
    renderWithProviders(<LoginPage />, { route: "/login" });

    await user.click(screen.getByRole("button", { name: /^register$/i }));

    expect(screen.getByLabelText(/full name/i)).toBeInTheDocument();
    expect(screen.getByRole("button", { name: /create account/i })).toBeInTheDocument();
  });
});
