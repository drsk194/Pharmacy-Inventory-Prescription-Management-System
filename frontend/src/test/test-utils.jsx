import { render } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import { AuthProvider } from "../context/AuthContext";

export function renderWithProviders(ui, { route = "/" } = {}) {
  return render(<MemoryRouter initialEntries={[route]}><AuthProvider>{ui}</AuthProvider></MemoryRouter>);
}

// eslint-disable-next-line react-refresh/only-export-components
export * from "@testing-library/react";
