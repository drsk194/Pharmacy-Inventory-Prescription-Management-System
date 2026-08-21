const TONE = { BLOCKING: "danger", WARNING: "warning", INFO: "neutral" };

export default function WarningPanel({ warnings = [] }) {
  if (!warnings.length) return <p className="form-hint">No warnings.</p>;
  return <div className="warning-panel">{warnings.map((warning, index) => <div key={warning.id || index} className={`warning-item warning-item--${TONE[warning.severity] || "neutral"}`}><strong>{warning.severity}</strong><span>{warning.message}</span></div>)}</div>;
}
