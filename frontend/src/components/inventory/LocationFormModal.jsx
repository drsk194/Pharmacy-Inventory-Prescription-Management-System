import { useState } from "react";
import Modal from "../common/Modal";
import { locationApi } from "../../api/locationApi";
export default function LocationFormModal({ onClose, onSaved }) { const [form, setForm] = useState({
    name: "",
    type: "",
    description: ""
}); const [error, setError] = useState(""); async function submit(event) { event.preventDefault(); try { await locationApi.create(form); onSaved(); } catch (err) { setError(err.response?.data?.message || "Couldn't create location."); } } return <Modal title="Create Location" onClose={onClose}><form className="modal-form" onSubmit={submit}><label>Name<input value={form.name} onChange={(event) => setForm({ ...form, name: event.target.value })} required /></label><label>Type<select value={form.type} onChange={(event) => setForm({ ...form, type: event.target.value })} required><option value="">Select location type</option><option value="MAIN_PHARMACY">MAIN_PHARMACY</option><option value="SATELLITE_PHARMACY">SATELLITE_PHARMACY</option><option value="WARD_STOCK">WARD_STOCK</option></select></label><label>Description<input value={form.description} onChange={(event) => setForm({ ...form, description: event.target.value })} /></label>{error && <p className="form-error">{error}</p>}<button type="submit">Save</button></form></Modal>; }
