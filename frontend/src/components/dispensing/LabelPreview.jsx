export default function LabelPreview({ label }) {
  if (!label) return null;
  return <div className="label-preview"><div id="printable-label"><h3>{label.drugName}</h3><p>{label.dosageInstructions}</p><p>Qty: {label.quantity}</p><p>Patient: {label.patientName}</p><p>Batch: {label.batchNumber} · Exp {label.expiryDate}</p></div><button type="button" onClick={() => window.print()}>Print label</button></div>;
}
