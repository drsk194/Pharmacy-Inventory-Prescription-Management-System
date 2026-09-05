import { useCallback, useEffect, useState } from "react";
import { patientApi } from "../../api/patientApi";

function today() {
  return new Date().toISOString().slice(0, 10);
}

export default function MedicationsPanel({ patientId }) {
  const [rows, setRows] = useState([]);
  const [form, setForm] = useState({ drugName: "", dosage: "", startDate: today() });
  const [error, setError] = useState("");
  const load = useCallback(async () => {
    try {
      const response = await patientApi.getMedications(patientId);
      setRows(response.data.data);
    } catch (err) {
      setError(err.response?.data?.message || "Couldn't load medications.");
    }
  }, [patientId]);

  useEffect(() => {
    const timer = setTimeout(load, 0);
    return () => clearTimeout(timer);
  }, [load]);

  async function submit(event) {
    event.preventDefault();
    try {
      await patientApi.addMedication(patientId, form);
      setForm({ drugName: "", dosage: "", startDate: today() });
      load();
    } catch (err) {
      setError(err.response?.data?.message || "Couldn't add medication.");
    }
  }

  function change(event) {
    setForm({ ...form, [event.target.name]: event.target.value });
  }

  return (
    <section>
      <h2>Medications</h2>
      {error && <p className="form-error">{error}</p>}
      <ul className="record-list">
        {rows.map((row) => <li key={row.id}>{row.drugName} {row.dosage && <span>{row.dosage}</span>}</li>)}
        {!rows.length && <li>No current medications recorded.</li>}
      </ul>
      <form className="inline-form" onSubmit={submit}>
        <input name="drugName" placeholder="Medication name" value={form.drugName} onChange={change} required />
        <input name="dosage" placeholder="Dosage" value={form.dosage} onChange={change} />
        <input name="startDate" type="date" aria-label="Start date" value={form.startDate} onChange={change} required />
        <button type="submit">Add</button>
      </form>
    </section>
  );
}
