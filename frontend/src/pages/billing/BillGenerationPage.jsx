import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { billingApi } from "../../api/billingApi";
import { patientApi } from "../../api/patientApi";

export default function BillGenerationPage() {
  const [patients, setPatients] = useState([]);
  const [patientId, setPatientId] = useState("");
  const [billableRecords, setBillableRecords] = useState([]);
  const [selectedIds, setSelectedIds] = useState([]);
  const [discountPercent, setDiscountPercent] = useState("");
  const [discountReason, setDiscountReason] = useState("");
  const [error, setError] = useState("");
  const [loadingRecords, setLoadingRecords] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    patientApi.list({ page: 0, size: 500 })
      .then((response) => {
        const data = response.data.data;
        setPatients(data.content || data);
      })
      .catch((err) => setError(err.response?.data?.message || "Could not load patients."));
  }, []);

  useEffect(() => {
    setSelectedIds([]);
    setBillableRecords([]);
    if (!patientId) return undefined;

    setLoadingRecords(true);
    setError("");
    billingApi.getBillableDispensing(patientId)
      .then((response) => setBillableRecords(response.data.data || []))
      .catch((err) => setError(err.response?.data?.message || "Could not load billable dispensing records."))
      .finally(() => setLoadingRecords(false));

    return undefined;
  }, [patientId]);

  async function submit(event) {
    event.preventDefault();
    setError("");
    try {
      const response = await billingApi.create({
        patientId: Number(patientId),
        dispensingRecordIds: selectedIds,
        discountPercent: discountPercent || undefined,
        discountReason: discountPercent ? discountReason : undefined,
      });
      navigate(`/bills/${response.data.data.id}`);
    } catch (err) {
      setError(err.response?.data?.message || "Could not generate bill.");
    }
  }

  function toggleRecord(id) {
    setSelectedIds((current) => current.includes(id)
      ? current.filter((item) => item !== id)
      : [...current, id]);
  }

  return (
    <main className="detail-page">
      <h1>Generate bill</h1>
      <form className="modal-form modal-form--standalone" onSubmit={submit}>
        <label>Patient
          <select value={patientId} onChange={(event) => setPatientId(event.target.value)} required>
            <option value="">Select...</option>
            {patients.map((patient) => <option key={patient.id} value={patient.id}>{patient.fullName}</option>)}
          </select>
        </label>

        <fieldset>
          <legend>Dispensing records</legend>
          {!patientId && <p>Select a patient to see unbilled authorized dispensing records.</p>}
          {patientId && loadingRecords && <p>Loading eligible dispensing records...</p>}
          {patientId && !loadingRecords && !billableRecords.length && <p>No unbilled authorized dispensing records are available for this patient.</p>}
          {billableRecords.map((record) => (
            <label key={record.id}>
              <input
                type="checkbox"
                checked={selectedIds.includes(record.id)}
                onChange={() => toggleRecord(record.id)}
              />
              #{record.id} — {record.drugGenericName} — {record.quantityDispensed} units ({record.status})
            </label>
          ))}
        </fieldset>

        <label>Discount percent
          <input type="number" min="0" max="100" step="0.1" value={discountPercent} onChange={(event) => setDiscountPercent(event.target.value)} />
        </label>
        {discountPercent && <label>Discount reason
          <input value={discountReason} onChange={(event) => setDiscountReason(event.target.value)} required />
        </label>}
        {error && <p className="form-error">{error}</p>}
        <button type="submit" disabled={!patientId || !selectedIds.length || loadingRecords}>Generate bill</button>
      </form>
    </main>
  );
}
