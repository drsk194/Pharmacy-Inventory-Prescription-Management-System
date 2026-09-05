import { useEffect, useState } from "react";
import Modal from "../common/Modal";
import { controlledSubstanceApi } from "../../api/controlledSubstanceApi";
import { drugApi } from "../../api/drugApi";
import { batchApi } from "../../api/batchApi";
import { userApi } from "../../api/userApi";

const TYPES = ["RECEIPT", "DISPENSING", "RETURN", "DISPOSAL", "ADJUSTMENT"];

export default function CsTransactionForm({ onClose, onSaved }) {
  const [drugs, setDrugs] = useState([]);
  const [batches, setBatches] = useState([]);
  const [staff, setStaff] = useState([]);
  const [form, setForm] = useState({
    drugId: "",
    batchId: "",
    transactionType: "RECEIPT",
    quantity: "",
    technicianId: "",
    technicianPin: "",
    witnessId: "",
  });
  const [error, setError] = useState("");

  useEffect(() => {
    Promise.all([
      drugApi.list({ page: 0, size: 200, activeOnly: true }),
      batchApi.list({ page: 0, size: 500 }),
      userApi.controlledSubstanceStaff(),
    ])
      .then(([drugsResponse, batchesResponse, staffResponse]) => {
        const drugsData = drugsResponse.data.data;
        const batchesData = batchesResponse.data.data;
        setDrugs(drugsData.content || drugsData);
        setBatches(batchesData.content || batchesData);
        setStaff(staffResponse.data.data || []);
      })
      .catch((err) => setError(err.response?.data?.message || "Could not load transaction options."));
  }, []);

  function change(event) {
    setForm((prev) => ({ ...prev, [event.target.name]: event.target.value }));
  }

  async function submit(event) {
    event.preventDefault();
    setError("");
    try {
      await controlledSubstanceApi.createTransaction({
        drugId: Number(form.drugId),
        batchId: form.batchId ? Number(form.batchId) : undefined,
        transactionType: form.transactionType,
        quantity: Number(form.quantity),
        technicianId: Number(form.technicianId),
        technicianPin: form.technicianPin,
        witnessId: form.transactionType === "DISPOSAL" ? Number(form.witnessId) : undefined,
      });
      onSaved();
    } catch (err) {
      setError(err.response?.data?.message || "Could not record transaction.");
    }
  }

  const technicians = staff.filter(
    (person) => person.roles?.includes("ROLE_TECHNICIAN") || person.roles?.includes("ROLE_ADMIN"),
  );

  return (
    <Modal title="New Controlled Substance Transaction" onClose={onClose}>
      <form className="modal-form" onSubmit={submit}>
        <label>Drug<select name="drugId" value={form.drugId} onChange={change} required><option value="">Select...</option>{drugs.map((drug) => <option key={drug.id} value={drug.id}>{drug.brandName ? `${drug.genericName} (${drug.brandName})` : drug.genericName}</option>)}</select></label>
        <label>Batch<select name="batchId" value={form.batchId} onChange={change} required={form.transactionType !== "DISPENSING"}><option value="">Select...</option>{batches.map((batch) => <option key={batch.id} value={batch.id}>{batch.drugGenericName ? `${batch.drugGenericName} - ` : ""}{batch.batchNumber} ({batch.currentQuantity ?? 0})</option>)}</select></label>
        <label>Type<select name="transactionType" value={form.transactionType} onChange={change}>{TYPES.map((type) => <option key={type} value={type}>{type}</option>)}</select></label>
        <label>Quantity<input name="quantity" type="number" min="0.01" step="0.01" value={form.quantity} onChange={change} required /></label>
        {form.transactionType === "DISPOSAL" && <label>Witness<select name="witnessId" value={form.witnessId} onChange={change} required><option value="">Select witness...</option>{staff.map((person) => <option key={person.id} value={person.id}>{person.fullName}{person.staffId ? ` (${person.staffId})` : ""}</option>)}</select></label>}
        <label>Co-signing technician<select name="technicianId" value={form.technicianId} onChange={change} required><option value="">Select technician...</option>{technicians.map((person) => <option key={person.id} value={person.id}>{person.fullName}{person.staffId ? ` (${person.staffId})` : ""}</option>)}</select></label>
        <label>Technician PIN<input name="technicianPin" type="password" inputMode="numeric" autoComplete="off" value={form.technicianPin} onChange={change} required /></label>
        {error && <p className="form-error">{error}</p>}
        <button type="submit">Record transaction</button>
      </form>
    </Modal>
  );
}
