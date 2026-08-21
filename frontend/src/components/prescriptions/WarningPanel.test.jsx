import { describe, expect, it } from "vitest";
import { render, screen } from "@testing-library/react";
import WarningPanel from "./WarningPanel";
import { hasBlockingWarning } from "./warningUtils";

describe("warning handling", () => {
  it("detects a blocking warning", () => {
    expect(hasBlockingWarning([{ severity: "WARNING" }, { severity: "BLOCKING" }])).toBe(true);
  });

  it("renders an empty state and grouped messages", () => {
    const { rerender } = render(<WarningPanel warnings={[]} />);
    expect(screen.getByText(/no warnings/i)).toBeInTheDocument();
    rerender(<WarningPanel warnings={[{ type: "Allergy", severity: "BLOCKING", message: "Severe allergy" }]} />);
    expect(screen.getByText("Severe allergy")).toBeInTheDocument();
  });
});
