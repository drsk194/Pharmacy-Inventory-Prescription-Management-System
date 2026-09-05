import { useState } from "react";
import Modal from "../common/Modal";
import { drugApi } from "../../api/drugApi";

const SCHEDULES = ["OTC", "H", "H1", "X", "NOT_SCHEDULED"];
const STORAGE = ["ROOM_TEMP", "REFRIGERATED", "FROZEN", "CONTROLLED_TEMP"];

export default function DrugFormModal({ drug, onClose, onSaved }) {
  const [form, setForm] = useState({
    genericName: drug?.genericName || "",
    brandName: drug?.brandName || "",
    ndcCode: drug?.ndcCode || "",
    drugClass: drug?.drugClass || "",
    schedule: drug?.schedule || "OTC",
    storageCondition: drug?.storageCondition || "ROOM_TEMP",
    unitOfMeasure: drug?.unitOfMeasure || "",
    reorderLevel: drug?.reorderLevel ?? "",
    minStockLevel: drug?.minStockLevel ?? "",
    maxStockLevel: drug?.maxStockLevel ?? "",
    barcode: drug?.barcode || "",
    maxPrescriptionQtyPerFill: drug?.maxPrescriptionQtyPerFill || "",
    maxRefillsAllowed: drug?.maxRefillsAllowed || "",
  });
  const [error, setError] = useState("");
  const [saving, setSaving] = useState(false);
  const scheduled = ["H", "H1", "X"].includes(form.schedule);

  function change(event) {
    setForm({ ...form, [event.target.name]: event.target.value });
  }

  async function submit(event) {
    event.preventDefault();
    setSaving(true);
    setError("");
    const payload = {
      ...form,
      reorderLevel: form.reorderLevel === "" ? null : Number(form.reorderLevel),
      minStockLevel: form.minStockLevel === "" ? null : Number(form.minStockLevel),
      maxStockLevel: form.maxStockLevel === "" ? null : Number(form.maxStockLevel),
    };
    if (!scheduled) {
      delete payload.maxPrescriptionQtyPerFill;
      delete payload.maxRefillsAllowed;
    } else {
      payload.maxPrescriptionQtyPerFill = Number(payload.maxPrescriptionQtyPerFill);
      payload.maxRefillsAllowed = Number(payload.maxRefillsAllowed);
    }
    if (!payload.ndcCode) delete payload.ndcCode;
    if (!payload.brandName) delete payload.brandName;
    if (!payload.barcode) delete payload.barcode;
    try {
      if (drug) await drugApi.update(drug.id, payload);
      else await drugApi.create(payload);
      onSaved();
    } catch (err) {
      const data = err.response?.data;
      const fieldErrors = data?.fieldErrors
        ? Object.entries(data.fieldErrors).map(([field, message]) => `${field}: ${message}`).join(" | ")
        : "";
      setError(fieldErrors || data?.message || data?.error || err.message || "Couldn't save drug.");
    } finally {
      setSaving(false);
    }
  }

  return (
    <Modal title={drug ? "Edit Drug" : "Create Drug"} onClose={onClose}>
      <form className="modal-form" onSubmit={submit}>
        <label>
          Generic name
          <input name="genericName" value={form.genericName} onChange={change} placeholder="e.g. Paracetamol" required />
        </label>
        <label>
          Brand name
          <input name="brandName" value={form.brandName} onChange={change} placeholder="e.g. Calpol" />
        </label>
        <label>
          NDC code
          <input name="ndcCode" value={form.ndcCode} onChange={change} placeholder="e.g. 12345-6789-01" />
        </label>
        <label>
          Drug class
          <input name="drugClass" value={form.drugClass} onChange={change} required />
        </label>
        <label>
          Schedule
          <select name="schedule" value={form.schedule} onChange={change}>
            {SCHEDULES.map((item) => <option key={item}>{item}</option>)}
          </select>
        </label>
        <label>
          Storage condition
          <select name="storageCondition" value={form.storageCondition} onChange={change}>
            {STORAGE.map((item) => <option key={item}>{item}</option>)}
          </select>
        </label>
        <label>
          Unit of measure
          <input name="unitOfMeasure" value={form.unitOfMeasure} onChange={change} placeholder="e.g. Tablet, Vial, Capsule" required />
        </label>
        <label>
          Minimum stock level
          <input name="minStockLevel" type="number" min="0" value={form.minStockLevel} onChange={change} required />
        </label>
        <label>
          Reorder level
          <input name="reorderLevel" type="number" min="0" value={form.reorderLevel} onChange={change} required />
        </label>
        <label>
          Maximum stock level
          <input name="maxStockLevel" type="number" min="0" value={form.maxStockLevel} onChange={change} />
        </label>
        <label>
          Barcode
          <input name="barcode" value={form.barcode} onChange={change} />
        </label>
        {scheduled && (
          <>
            <label>
              Max quantity per fill
              <input name="maxPrescriptionQtyPerFill" type="number" min="1" value={form.maxPrescriptionQtyPerFill} onChange={change} required />
            </label>
            <label>
              Max refills allowed
              <input name="maxRefillsAllowed" type="number" min="0" value={form.maxRefillsAllowed} onChange={change} required />
            </label>
          </>
        )}
        {error && <p className="form-error">{error}</p>}
        <div className="modal-form__actions">
          <button type="button" className="button--outline" onClick={onClose}>Cancel</button>
          <button type="submit" disabled={saving}>{saving ? "Saving…" : "Save"}</button>
        </div>
      </form>
    </Modal>
  );
}
