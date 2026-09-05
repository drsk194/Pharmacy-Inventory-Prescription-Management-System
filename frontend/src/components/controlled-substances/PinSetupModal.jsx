import { useState } from "react";
import Modal from "../common/Modal";
import { controlledSubstanceApi } from "../../api/controlledSubstanceApi";

export default function PinSetupModal({ onClose, onSaved }) {
  const [currentPassword, setCurrentPassword] = useState("");
  const [pin, setPin] = useState("");
  const [confirm, setConfirm] = useState("");
  const [error, setError] = useState("");
  const [saving, setSaving] = useState(false);

  async function submit(event) {
    event.preventDefault();
    if (pin !== confirm) {
      setError("PINs do not match.");
      return;
    }
    setSaving(true);
    setError("");
    try {
      await controlledSubstanceApi.setPin({ currentPassword, newPin: pin });
      onSaved();
    } catch (err) {
      setError(err.response?.data?.message || "Could not set PIN.");
    } finally {
      setSaving(false);
    }
  }

  return (
    <Modal title="Set Controlled Substance PIN" onClose={onClose}>
      <form className="modal-form" onSubmit={submit} autoComplete="off">
        <label>
          Current account password
          <input type="password" autoComplete="current-password" value={currentPassword} onChange={(event) => setCurrentPassword(event.target.value)} required />
        </label>
        <label>
          New PIN (4-6 digits)
          <input type="password" autoComplete="off" inputMode="numeric" pattern="\d{4,6}" minLength="4" maxLength="6" value={pin} onChange={(event) => setPin(event.target.value)} required />
        </label>
        <label>
          Confirm PIN
          <input type="password" autoComplete="off" inputMode="numeric" pattern="\d{4,6}" minLength="4" maxLength="6" value={confirm} onChange={(event) => setConfirm(event.target.value)} required />
        </label>
        {error && <p className="form-error">{error}</p>}
        <button type="submit" disabled={saving}>{saving ? "Saving…" : "Set PIN"}</button>
      </form>
    </Modal>
  );
}
