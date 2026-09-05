import { useRef } from "react";

export default function LabelPreview({ label }) {
  const printRef = useRef(null);

  if (!label) return null;

  function handlePrint() {
    const style = document.createElement("style");
    style.textContent = `
      @media print {
        body > *:not(#label-print-root) { display: none !important; }
        #label-print-root { display: block !important; }
        .label-print-card { border: 2px solid #000 !important; box-shadow: none !important; }
      }
    `;
    document.head.appendChild(style);
    const prev = document.getElementById("label-print-root");
    if (prev) prev.remove();
    const root = document.createElement("div");
    root.id = "label-print-root";
    root.innerHTML = printRef.current.innerHTML;
    document.body.appendChild(root);
    window.print();
    setTimeout(() => {
      root.remove();
      style.remove();
    }, 1000);
  }

  const issued = label.dispensedAt
    ? new Date(label.dispensedAt).toLocaleDateString("en-IN")
    : new Date().toLocaleDateString("en-IN");

  return (
    <div className="label-preview">
      {/* Printable label card */}
      <div ref={printRef} className="label-print-card" id="printable-label" aria-label="Dispensing label">
        <div className="label-print-card__header">
          <span className="label-print-card__pharmacy">PIPMS Pharmacy</span>
          <span className="label-print-card__date">{issued}</span>
        </div>

        <div className="label-print-card__drug">
          <strong>{label.drugGenericName || "—"}</strong>
        </div>

        {label.instructions && (
          <p className="label-print-card__dosage">{label.instructions}</p>
        )}

        <div className="label-print-card__row">
          <span>Qty:</span>
          <strong>{label.quantityDispensed ?? "—"}</strong>
        </div>

        {label.dosage && (
          <div className="label-print-card__row">
            <span>Dosage:</span>
            <strong>{label.dosage}</strong>
          </div>
        )}

        {label.frequency && (
          <div className="label-print-card__row">
            <span>Frequency:</span>
            <strong>{label.frequency}</strong>
          </div>
        )}

        {label.duration && (
          <div className="label-print-card__row">
            <span>Duration:</span>
            <strong>{label.duration}</strong>
          </div>
        )}

        <div className="label-print-card__divider" />

        <div className="label-print-card__row">
          <span>Patient:</span>
          <strong>{label.patientName || "—"}</strong>
        </div>

        {label.prescriberName && (
          <div className="label-print-card__row">
            <span>Prescriber:</span>
            <strong>{label.prescriberName}</strong>
          </div>
        )}

        {label.dispensingPharmacistName && (
          <div className="label-print-card__row">
            <span>Dispensed by:</span>
            <strong>{label.dispensingPharmacistName}</strong>
          </div>
        )}
      </div>

      <button
        type="button"
        className="button--primary label-preview__print-btn"
        onClick={handlePrint}
        aria-label="Print dispensing label"
      >
        🖨 Print label
      </button>
    </div>
  );
}
