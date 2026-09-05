import { useState } from "react";
import Modal from "../common/Modal";
import { userManagementApi } from "../../api/userManagementApi";

const ROLES = ["ROLE_ADMIN", "ROLE_PHARMACIST", "ROLE_TECHNICIAN", "ROLE_PROCUREMENT_OFFICER", "ROLE_AUDITOR", "ROLE_DOCTOR"];

export default function UserFormModal({ onClose, onSaved }) {
  const [form, setForm] = useState({ fullName: "", email: "", phoneNumber: "", staffId: "", temporaryPassword: "", licenseNumber: "" });
  const [role, setRole] = useState("");
  const [error, setError] = useState("");
  const [saving, setSaving] = useState(false);

  function change(event) {
    setForm({ ...form, [event.target.name]: event.target.value });
  }

  async function submit(event) {
    event.preventDefault();
    if (!role) { setError("Select a role."); return; }
    setSaving(true);
    setError("");
    const payload = { ...form, role };
    if (!payload.staffId) delete payload.staffId;
    if (!payload.licenseNumber) delete payload.licenseNumber;
    try {
      await userManagementApi.create(payload);
      onSaved();
    } catch (err) {
      setError(err.response?.data?.message || "Could not create user.");
    } finally {
      setSaving(false);
    }
  }

  return (
    <Modal title="Create Staff User" onClose={onClose}>
      <form className="modal-form" onSubmit={submit}>
        <label>Full name<input name="fullName" value={form.fullName} onChange={change} required /></label>
        <label>Email<input name="email" type="email" value={form.email} onChange={change} required /></label>
        <label>Phone<input name="phoneNumber" value={form.phoneNumber} onChange={change} /></label>
        <label>Staff ID (optional)<input name="staffId" value={form.staffId} onChange={change} /></label>
        <label>
          Temporary password
          <input name="temporaryPassword" type="password" value={form.temporaryPassword} onChange={change} required />
        </label>
        <p className="form-hint">At least 8 characters, with an uppercase letter, lowercase letter, number, and special character (e.g. Passw0rd!).</p>
        <label>Role
          <select value={role} onChange={(event) => setRole(event.target.value)} required>
            <option value="">Select a role...</option>
            {ROLES.map((item) => <option key={item} value={item}>{item.replace("ROLE_", "")}</option>)}
          </select>
        </label>
        {(role === "ROLE_PHARMACIST" || role === "ROLE_DOCTOR") && (
          <label>License number<input name="licenseNumber" value={form.licenseNumber} onChange={change} /></label>
        )}
        {error && <p className="form-error">{error}</p>}
        <div className="modal-form__actions">
          <button type="button" className="button--outline" onClick={onClose}>Cancel</button>
          <button type="submit" disabled={saving}>{saving ? "Creating…" : "Create user"}</button>
        </div>
      </form>
    </Modal>
  );
}
