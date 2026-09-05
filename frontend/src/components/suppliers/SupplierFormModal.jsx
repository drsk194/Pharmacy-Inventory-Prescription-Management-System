import { useState } from "react";
import Modal from "../common/Modal";
import { supplierApi } from "../../api/supplierApi";

export default function SupplierFormModal({ supplier, onClose, onSaved }) {
  const [form, setForm] = useState({
    supplierName: supplier?.supplierName || "",
    contactPerson: supplier?.contactPerson || "",
    phone: supplier?.phone || "",
    email: supplier?.email || "",
    address: supplier?.address || "",
    drugLicenseNumber: supplier?.drugLicenseNumber || "",
    creditTerms: supplier?.creditTerms || "",
    rating: supplier?.rating ?? "",
  });
  const [error, setError] = useState("");
  const [saving, setSaving] = useState(false);

  function change(event) {
    setForm({ ...form, [event.target.name]: event.target.value });
  }

  async function submit(event) {
    event.preventDefault();
    setSaving(true);
    setError("");
    const payload = { ...form, rating: form.rating === "" ? undefined : Number(form.rating) };
    try {
      if (supplier) await supplierApi.update(supplier.id, payload);
      else await supplierApi.create(payload);
      onSaved();
    } catch (err) {
      setError(err.response?.data?.message || "Couldn't save supplier.");
    } finally {
      setSaving(false);
    }
  }

  return (
    <Modal title={supplier ? "Edit Supplier" : "Create Supplier"} onClose={onClose}>
      <form className="modal-form" onSubmit={submit}>
        <label>Supplier name<input name="supplierName" value={form.supplierName} onChange={change} required /></label>
        <label>Contact person<input name="contactPerson" value={form.contactPerson} onChange={change} /></label>
        <label>Phone<input name="phone" value={form.phone} onChange={change} placeholder="10 digit phone number" required /></label>
        <label>Email<input name="email" type="email" value={form.email} onChange={change} /></label>
        <label>Address<input name="address" value={form.address} onChange={change} /></label>
        <label>Drug license number<input name="drugLicenseNumber" value={form.drugLicenseNumber} onChange={change} required /></label>
        <label>Credit terms<input name="creditTerms" value={form.creditTerms} onChange={change} placeholder="e.g. Net 30" /></label>
        <label>Rating<input name="rating" type="number" min="0" max="5" step="0.1" value={form.rating} onChange={change} /></label>
        {error && <p className="form-error">{error}</p>}
        <div className="modal-form__actions">
          <button type="button" className="button--outline" onClick={onClose}>Cancel</button>
          <button type="submit" disabled={saving}>{saving ? "Saving…" : "Save"}</button>
        </div>
      </form>
    </Modal>
  );
}
