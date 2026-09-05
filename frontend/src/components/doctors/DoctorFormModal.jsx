import { useState } from "react";
import Modal from "../common/Modal";
import { doctorApi } from "../../api/doctorApi";

const SPECIALIZATIONS = [
  "General Practice", "Internal Medicine", "Cardiology", "Dermatology",
  "Endocrinology", "Gastroenterology", "Neurology", "Oncology",
  "Ophthalmology", "Orthopaedics", "Paediatrics", "Psychiatry",
  "Pulmonology", "Radiology", "Urology", "Other",
];

export default function DoctorFormModal({ doctor, onClose, onSaved }) {
  const isEdit = Boolean(doctor);
  const [form, setForm] = useState({
    userId: "",
    licenseNumber: doctor?.licenseNumber || "",
    registrationCouncil: doctor?.registrationCouncil || "",
    specialization: doctor?.specialization || "",
    qualification: doctor?.qualification || "",
  });
  const [error, setError] = useState("");
  const [fieldErrors, setFieldErrors] = useState({});
  const [submitting, setSubmitting] = useState(false);

  function change(event) {
    const { name, value } = event.target;
    setForm((prev) => ({ ...prev, [name]: value }));
    setFieldErrors((prev) => ({ ...prev, [name]: undefined }));
  }

  async function submit(event) {
    event.preventDefault();
    setError("");
    setFieldErrors({});
    setSubmitting(true);
    try {
      if (isEdit) {
        await doctorApi.update(doctor.id, {
          licenseNumber: form.licenseNumber,
          registrationCouncil: form.registrationCouncil,
          specialization: form.specialization,
          qualification: form.qualification,
        });
      } else {
        await doctorApi.create({
          userId: Number(form.userId),
          licenseNumber: form.licenseNumber,
          registrationCouncil: form.registrationCouncil,
          specialization: form.specialization,
          qualification: form.qualification,
        });
      }
      onSaved();
    } catch (err) {
      const data = err.response?.data;
      if (data?.fieldErrors) setFieldErrors(data.fieldErrors);
      setError(data?.message || "Couldn't save doctor.");
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <Modal title={isEdit ? "Edit Doctor" : "Create Doctor"} onClose={onClose}>
      <form className="modal-form" onSubmit={submit} noValidate>
        {!isEdit && (
          <>
            <p className="form-hint">
              The doctor must already have a staff account with the Doctor role (created by an admin under User
              Management) before you can attach their license details here.
            </p>
            <label>
              User ID <abbr title="required"> *</abbr>
              <input name="userId" type="number" min="1" value={form.userId} onChange={change} required />
              {fieldErrors.userId && <span className="form-error">{fieldErrors.userId}</span>}
            </label>
          </>
        )}
        <label>
          License number <abbr title="required"> *</abbr>
          <input name="licenseNumber" value={form.licenseNumber} onChange={change} required />
          {fieldErrors.licenseNumber && <span className="form-error">{fieldErrors.licenseNumber}</span>}
        </label>
        <label>
          Registration council
          <input name="registrationCouncil" value={form.registrationCouncil} onChange={change} />
          {fieldErrors.registrationCouncil && <span className="form-error">{fieldErrors.registrationCouncil}</span>}
        </label>
        <label>
          Specialization
          <select name="specialization" value={form.specialization} onChange={change}>
            <option value="">Select…</option>
            {SPECIALIZATIONS.map((item) => <option key={item} value={item}>{item}</option>)}
          </select>
          {fieldErrors.specialization && <span className="form-error">{fieldErrors.specialization}</span>}
        </label>
        <label>
          Qualification
          <input name="qualification" value={form.qualification} onChange={change} />
        </label>
        {error && <p className="form-error" role="alert">{error}</p>}
        <div className="modal-form__actions">
          <button type="button" className="button--outline" onClick={onClose}>Cancel</button>
          <button type="submit" disabled={submitting}>{submitting ? "Saving…" : isEdit ? "Save changes" : "Create doctor"}</button>
        </div>
      </form>
    </Modal>
  );
}
