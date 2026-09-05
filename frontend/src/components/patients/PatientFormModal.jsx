import { useState } from "react";
import Modal from "../common/Modal";
import { patientApi } from "../../api/patientApi";

export default function PatientFormModal({ patient, onClose, onSaved }) {
  const [form, setForm] = useState({
    fullName: patient?.fullName || "",
    dateOfBirth: patient?.dateOfBirth || "",
    gender: patient?.gender || "",
    phoneNumber: patient?.phoneNumber || "",
    email: patient?.email || "",
    emergencyContactName: patient?.emergencyContactName || "",
    emergencyContactPhone: patient?.emergencyContactPhone || "",
  });
  const [error, setError] = useState("");

  async function submit(event) {
    event.preventDefault();
    const payload = Object.fromEntries(
      Object.entries(form).filter(([, value]) => value !== ""),
    );
    try {
      if (patient) await patientApi.update(patient.id, payload);
      else await patientApi.create(payload);
      onSaved();
    } catch (err) {
      setError(err.response?.data?.message || "Couldn't save patient.");
    }
  }

  function change(event) {
    setForm({ ...form, [event.target.name]: event.target.value });
  }

  return (
    <Modal title={patient ? "Edit Patient" : "Create Patient"} onClose={onClose}>
      <form className="modal-form" onSubmit={submit}>
        {[["fullName", "Full name", "text"], ["dateOfBirth", "Date of birth", "date"], ["phoneNumber", "Phone", "tel"], ["email", "Email", "email"], ["emergencyContactName", "Emergency contact name", "text"], ["emergencyContactPhone", "Emergency contact phone", "tel"]].map(([name, label, type]) => <label key={name}>{label}<input name={name} type={type} value={form[name]} onChange={change} required={name === "fullName" || name === "dateOfBirth" || name === "phoneNumber"} /></label>)}
        <label>Gender<select name="gender" value={form.gender} onChange={change} required><option value="">Select...</option><option>MALE</option><option>FEMALE</option><option>OTHER</option></select></label>
        {error && <p className="form-error">{error}</p>}
        <div className="modal-form__actions"><button type="button" onClick={onClose}>Cancel</button><button type="submit">Save</button></div>
      </form>
    </Modal>
  );
}
