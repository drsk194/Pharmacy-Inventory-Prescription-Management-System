export function hasBlockingWarning(warnings = []) {
  return warnings.some((warning) => warning.severity === "BLOCKING");
}
