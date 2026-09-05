/* eslint-disable react-hooks/set-state-in-effect -- network data is loaded asynchronously after mount. */
import { useEffect, useState } from "react";
import { prescriptionApi } from "../../api/prescriptionApi";
import { dispensingApi } from "../../api/dispensingApi";
import BarcodeScanInput from "../../components/dispensing/BarcodeScanInput";
import LabelPreview from "../../components/dispensing/LabelPreview";
import SignatureCapture from "../../components/dispensing/SignatureCapture";
import CounsellingModal from "../../components/dispensing/CounsellingModal";
import ReturnModal from "../../components/dispensing/ReturnModal";
import ErrorReportModal from "../../components/dispensing/ErrorReportModal";
import WarningPanel from "../../components/prescriptions/WarningPanel";

/* Dispensing lifecycle stages for the active record */
const STAGE = {
  IDLE:          "IDLE",          // nothing selected
  SELECT:        "SELECT",        // prescription selected, awaiting barcode
  PREPARED:      "PREPARED",      // server returned dispensing record (allocation shown)
  AUTHORIZING:   "AUTHORIZING",   // authorize in-flight
  AUTHORIZED:    "AUTHORIZED",    // dispensing deducted from stock
  ACKNOWLEDGING: "ACKNOWLEDGING", // awaiting patient signature
  DONE:          "DONE",          // fully complete
};

export default function DispensingWorkbench() {
  const [queue, setQueue]         = useState([]);
  const [selected, setSelected]   = useState(null);
  const [barcode, setBarcode]     = useState("");
  const [prepared, setPrepared]   = useState(null);   // { dispensingId, allocations, interactionWarnings }
  const [, setDispensing] = useState(null); // authorized record
  const [label, setLabel]         = useState(null);
  const [stage, setStage]         = useState(STAGE.IDLE);
  const [error, setError]         = useState("");
  const [success, setSuccess]     = useState("");
  const [modal, setModal]         = useState(null);   // "counselling" | "return" | "error-report"
  const [signature, setSignature] = useState(null);

  /* ── load verified prescription queue ───────────────────────────────── */
  useEffect(() => {
    prescriptionApi.getQueue({ status: "VERIFIED" })
      .then((res) => {
        const data = res.data.data;
        setQueue(data.content || data);
      })
      .catch((err) => setError(err.response?.data?.message || "Couldn't load dispensing queue."));
  }, []);

  function selectPrescription(row) {
    setSelected(row);
    setPrepared(null);
    setDispensing(null);
    setLabel(null);
    setBarcode("");
    setStage(STAGE.SELECT);
    setError("");
    setSuccess("");
    setSignature(null);
  }

  /* ── STEP 1: prepare ─────────────────────────────────────────────────── */
  async function handlePrepare() {
    setError("");
    if (!selected.items?.length) {
      setError("This prescription has no dispensable items.");
      return;
    }
    try {
      const res = await dispensingApi.prepare({
        prescriptionItemId: selected.items?.[0]?.id,
        scannedBarcode: barcode || undefined,
      });
      setPrepared(res.data.data);
      setStage(STAGE.PREPARED);
    } catch (err) {
      setError(err.response?.data?.message || "Couldn't prepare dispensing.");
    }
  }

  /* ── STEP 2: authorize (point of no return) ──────────────────────────── */
  async function handleAuthorize() {
    setError("");
    setStage(STAGE.AUTHORIZING);
    try {
      const res = await dispensingApi.authorize(prepared.dispensingId);
      setDispensing(res.data.data);
      const labelRes = await dispensingApi.getLabel(prepared.dispensingId);
      setLabel(labelRes.data.data);
      setStage(STAGE.AUTHORIZED);
    } catch (err) {
      setError(err.response?.data?.message || "Authorization failed.");
      setStage(STAGE.PREPARED);  // allow retry
    }
  }

  /* ── STEP 3: patient acknowledgement ─────────────────────────────────── */
  const [ackForm, setAckForm] = useState({ collectedBy: "", relation: "SELF" });

  async function handleAcknowledge() {
    if (!signature) { setError("Please capture a signature first."); return; }
    setError("");
    try {
      await dispensingApi.acknowledge(prepared.dispensingId, {
        ...ackForm,
        signatureData: signature,
      });
      setStage(STAGE.DONE);
      setSuccess("Dispensing complete. Prescription fully processed.");
      // Refresh queue
      const qRes = await prescriptionApi.getQueue({ status: "VERIFIED" });
      const data = qRes.data.data;
      setQueue(data.content || data);
    } catch (err) {
      setError(err.response?.data?.message || "Acknowledgement failed.");
    }
  }

  /* ── sub-modal save callbacks ────────────────────────────────────────── */
  function onCounsellingSaved() { setModal(null); setSuccess("Counselling recorded."); }
  function onReturnSaved()      { setModal(null); setSuccess("Return recorded."); }
  function onErrorSaved()       { setModal(null); setSuccess("Error report submitted."); }

  const blockingWarnings = (prepared?.interactionWarnings || []).filter(
    (w) => w.severity === "BLOCKING"
  );
  const hasBlocking = blockingWarnings.length > 0;

  return (
    <main className="workbench">
      {/* ── Left panel: prescription queue ─────────────────────────────── */}
      <aside className="workbench__queue">
        <h2>Ready to dispense</h2>
        <ul className="record-list">
          {queue.map((row) => (
            <li
              key={row.id}
              className={`workbench__queue-item${selected?.id === row.id ? " workbench__queue-item--active" : ""}`}
              onClick={() => selectPrescription(row)}
              role="button"
              tabIndex={0}
              onKeyDown={(e) => e.key === "Enter" && selectPrescription(row)}
              aria-current={selected?.id === row.id ? "true" : undefined}
            >
              <span className="workbench__queue-patient">{row.patientName}</span>
              <span className="workbench__queue-meta">
                {row.doctorName} · {row.totalItems ?? row.items?.length ?? 0} item{(row.totalItems ?? row.items?.length ?? 0) !== 1 ? "s" : ""}
              </span>
              {row.priority && (
                <span className={`workbench__queue-priority workbench__queue-priority--${row.priority.toLowerCase()}`}>
                  {row.priority}
                </span>
              )}
            </li>
          ))}
          {!queue.length && <li className="workbench__queue-empty">Nothing waiting to be dispensed.</li>}
        </ul>
      </aside>

      {/* ── Main panel ─────────────────────────────────────────────────── */}
      <section className="workbench__main">
        {/* Feedback */}
        {error   && <p className="form-error"  role="alert"  aria-live="assertive">{error}</p>}
        {success && <p className="form-success" role="status" aria-live="polite">{success}</p>}

        {/* ── IDLE ── */}
        {stage === STAGE.IDLE && (
          <p className="workbench__placeholder">Select a prescription from the queue to begin.</p>
        )}

        {/* ── SELECT: barcode input ── */}
        {stage === STAGE.SELECT && selected && (
          <div className="workbench__step">
            <h2>Dispensing: {selected.patientName}</h2>
            <p className="detail-page__meta">
              Prescribed by {selected.doctorName} · Rx #{selected.id}
            </p>

            <BarcodeScanInput value={barcode} onChange={setBarcode} />

            <div className="workbench__step-actions">
              <button
                type="button"
                className="button--primary"
                onClick={handlePrepare}
              >
                Prepare →
              </button>
            </div>
          </div>
        )}

        {/* ── PREPARED: allocations + warnings + authorize ── */}
        {stage === STAGE.PREPARED && prepared && (
          <div className="workbench__step">
            <h2>Review & Authorize</h2>

            {/* FEFO allocation breakdown */}
            {(prepared.allocations || prepared.items || []).length > 0 && (
              <div className="workbench__allocations">
                <h3>FEFO batch allocation</h3>
                <table className="data-table">
                  <thead>
                    <tr>
                      <th>Drug</th>
                      <th>Batch</th>
                      <th>Qty allocated</th>
                      <th>Expiry</th>
                    </tr>
                  </thead>
                  <tbody>
                    {(prepared.allocations || prepared.items || []).map((a, idx) => (
                      <tr key={idx}>
                        <td>{a.drugName || a.drug?.genericName || "—"}</td>
                        <td>{a.batchNumber || "—"}</td>
                        <td>{a.allocatedQuantity ?? a.quantity ?? "—"}</td>
                        <td>{a.expiryDate || "—"}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}

            {/* Drug interaction / allergy warnings */}
            {(prepared.interactionWarnings || []).length > 0 && (
              <div className="workbench__warnings">
                <h3>Clinical warnings</h3>
                <WarningPanel warnings={prepared.interactionWarnings} />
              </div>
            )}

            {hasBlocking && (
              <p className="form-error workbench__blocking-note" role="alert">
                ⛔ There is at least one BLOCKING warning. Pharmacist override and justification required before authorizing.
              </p>
            )}

            <div className="workbench__step-actions">
              <button type="button" onClick={() => setStage(STAGE.SELECT)}>← Back</button>
              <button
                type="button"
                className="button--primary"
                onClick={handleAuthorize}
                disabled={stage === STAGE.AUTHORIZING}
                aria-busy={stage === STAGE.AUTHORIZING}
              >
                {stage === STAGE.AUTHORIZING ? "Authorizing — do not close…" : "Authorize Dispensing"}
              </button>
            </div>
          </div>
        )}

        {/* ── AUTHORIZED: label + acknowledgement ── */}
        {(stage === STAGE.AUTHORIZED || stage === STAGE.ACKNOWLEDGING) && (
          <div className="workbench__step">
            <h2>Dispensing authorized</h2>
            <p className="form-success">Stock deducted. Complete acknowledgement below.</p>

            {/* Label */}
            <LabelPreview label={label} />

            {/* Patient acknowledgement */}
            <div className="workbench__ack">
              <h3>Patient / caregiver acknowledgement</h3>
              <label>
                Collected by (name)
                <input
                  type="text"
                  value={ackForm.collectedBy}
                  onChange={(e) => setAckForm((p) => ({ ...p, collectedBy: e.target.value }))}
                  placeholder="Full name of person collecting"
                  required
                />
              </label>
              <label>
                Relation to patient
                <select
                  value={ackForm.relation}
                  onChange={(e) => setAckForm((p) => ({ ...p, relation: e.target.value }))}
                >
                  <option value="SELF">Self (Patient)</option>
                  <option value="CAREGIVER">Caregiver / Family</option>
                  <option value="AUTHORIZED_REPRESENTATIVE">Authorized Representative</option>
                </select>
              </label>

              <div className="workbench__signature-area">
                <h4>Signature</h4>
                <SignatureCapture onCapture={setSignature} />
                {signature && <p className="form-success" aria-live="polite">✓ Signature captured</p>}
              </div>

              <button
                type="button"
                className="button--primary"
                onClick={handleAcknowledge}
                disabled={!ackForm.collectedBy || !signature}
              >
                Confirm acknowledgement
              </button>
            </div>

            {/* Post-dispense actions */}
            <div className="workbench__post-actions">
              <button type="button" onClick={() => setModal("counselling")}>+ Counselling notes</button>
              <button type="button" onClick={() => setModal("return")}>Record return</button>
              <button type="button" onClick={() => setModal("error-report")}>Report error</button>
            </div>
          </div>
        )}

        {/* ── DONE ── */}
        {stage === STAGE.DONE && (
          <div className="workbench__step workbench__step--done">
            <div className="workbench__done-icon" aria-hidden="true">✔</div>
            <h2>Complete</h2>
            <p>This prescription has been fully dispensed and acknowledged.</p>

            <div className="workbench__post-actions">
              <button type="button" onClick={() => setModal("counselling")}>+ Counselling notes</button>
              <button type="button" onClick={() => setModal("return")}>Record a return</button>
              <button type="button" onClick={() => setModal("error-report")}>Report dispensing error</button>
            </div>

            <button
              type="button"
              className="button--primary"
              onClick={() => {
                setSelected(null);
                setStage(STAGE.IDLE);
                setSuccess("");
                setError("");
              }}
            >
              Next prescription
            </button>
          </div>
        )}
      </section>

      {/* ── Sub-modals ─────────────────────────────────────────────────── */}
      {modal === "counselling" && (
        <CounsellingModal
          dispensingId={prepared?.dispensingId}
          onClose={() => setModal(null)}
          onSaved={onCounsellingSaved}
        />
      )}
      {modal === "return" && (
        <ReturnModal
          onClose={() => setModal(null)}
          onSaved={onReturnSaved}
        />
      )}
      {modal === "error-report" && (
        <ErrorReportModal
          onClose={() => setModal(null)}
          onSaved={onErrorSaved}
        />
      )}
    </main>
  );
}
