import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { prescriptionApi } from "../../api/prescriptionApi";
import { doctorApi } from "../../api/doctorApi";
import { patientApi } from "../../api/patientApi";
import { drugApi } from "../../api/drugApi";

function today() {
  return new Date().toISOString().slice(0, 10);
}

const EMPTY_ITEM = { drugId: "", prescribedQuantity: "", dosage: "", frequency: "", duration: "", instructions: "" };

export default function PrescriptionSubmitPage() {
  const navigate = useNavigate();
  const [doctorId, setDoctorId] = useState(null);
  const [patients, setPatients] = useState([]);
  const [drugs, setDrugs] = useState([]);
  const [form, setForm] = useState({
    patientId: "",
    prescriptionDate: today(),
    source: "ELECTRONIC",
    digitalSignatureReference: "",
    notes: "",
    items: [{ ...EMPTY_ITEM }],
  });
  const [error, setError] = useState("");
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    doctorApi.me().then((response) => setDoctorId(response.data.data.linkedUserId)).catch(() => setError("Could not load your doctor profile."));
    patientApi.list({ page: 0, size: 500 }).then((response) => {
      const data = response.data.data;
      setPatients(data.content || data);
    }).catch(() => {});
    drugApi.list({ page: 0, size: 500, activeOnly: true }).then((response) => {
      const data = response.data.data;
      setDrugs(data.content || data);
    }).catch(() => {});
  }, []);

  function updateItem(index, field, value) {
    setForm((prev) => ({ ...prev, items: prev.items.map((item, itemIndex) => (itemIndex === index ? { ...item, [field]: value } : item)) }));
  }

  function addItem() {
    setForm((prev) => ({ ...prev, items: [...prev.items, { ...EMPTY_ITEM }] }));
  }

  function removeItem(index) {
    setForm((prev) => ({ ...prev, items: prev.items.filter((_, itemIndex) => itemIndex !== index) }));
  }

  async function submit(event) {
    event.preventDefault();
    if (!doctorId) {
      setError("Your doctor profile hasn't loaded yet.");
      return;
    }
    setSaving(true);
    setError("");
    try {
      await prescriptionApi.create({
        patientId: Number(form.patientId),
        doctorId,
        prescriptionDate: form.prescriptionDate,
        source: form.source,
        notes: form.notes || undefined,
        digitalSignatureReference: form.source === "ELECTRONIC" ? form.digitalSignatureReference : undefined,
        items: form.items.map((item) => ({
          drugId: Number(item.drugId),
          prescribedQuantity: Number(item.prescribedQuantity),
          dosage: item.dosage,
          frequency: item.frequency,
          duration: item.duration,
          instructions: item.instructions || undefined,
        })),
      });
      navigate("/prescriptions/my");
    } catch (err) {
      setError(err.response?.data?.message || "Couldn't submit prescription.");
    } finally {
      setSaving(false);
    }
  }

  return (
    <main className="detail-page">
      <h1>New prescription</h1>
      <form className="modal-form modal-form--standalone" onSubmit={submit}>
        <label>
          Patient
          <select value={form.patientId} onChange={(event) => setForm({ ...form, patientId: event.target.value })} required>
            <option value="">Select patient...</option>
            {patients.map((patient) => <option key={patient.id} value={patient.id}>{patient.fullName}</option>)}
          </select>
        </label>
        <label>
          Prescription date
          <input type="date" max={today()} value={form.prescriptionDate} onChange={(event) => setForm({ ...form, prescriptionDate: event.target.value })} required />
        </label>
        <label>
          Source
          <select value={form.source} onChange={(event) => setForm({ ...form, source: event.target.value })}>
            <option>ELECTRONIC</option>
            <option>TELEMEDICINE</option>
            <option>PAPER</option>
          </select>
        </label>
        {form.source === "ELECTRONIC" && (
          <label>
            Digital signature
            <input value={form.digitalSignatureReference} onChange={(event) => setForm({ ...form, digitalSignatureReference: event.target.value })} required />
          </label>
        )}
        <label>
          Notes (optional)
          <input value={form.notes} onChange={(event) => setForm({ ...form, notes: event.target.value })} />
        </label>
        <h2>Items</h2>
        {form.items.map((item, index) => (
          <div className="prescription-item-row" key={index}>
            <select value={item.drugId} onChange={(event) => updateItem(index, "drugId", event.target.value)} required>
              <option value="">Select drug...</option>
              {drugs.map((drug) => <option key={drug.id} value={drug.id}>{drug.brandName ? `${drug.genericName} (${drug.brandName})` : drug.genericName}</option>)}
            </select>
            <input type="number" min="1" placeholder="Qty" value={item.prescribedQuantity} onChange={(event) => updateItem(index, "prescribedQuantity", event.target.value)} required />
            <input placeholder="Dosage (e.g. 500mg)" value={item.dosage} onChange={(event) => updateItem(index, "dosage", event.target.value)} required />
            <input placeholder="Frequency (e.g. Twice daily)" value={item.frequency} onChange={(event) => updateItem(index, "frequency", event.target.value)} required />
            <input placeholder="Duration (e.g. 7 days)" value={item.duration} onChange={(event) => updateItem(index, "duration", event.target.value)} required />
            <input placeholder="Instructions (optional)" value={item.instructions} onChange={(event) => updateItem(index, "instructions", event.target.value)} />
            {form.items.length > 1 && <button type="button" className="button--outline" onClick={() => removeItem(index)}>Remove</button>}
          </div>
        ))}
        <button type="button" className="button--outline" onClick={addItem}>Add another item</button>
        {error && <p className="form-error">{error}</p>}
        <button type="submit" disabled={saving}>{saving ? "Submitting…" : "Submit prescription"}</button>
      </form>
    </main>
  );
}
