import { useEffect, useState } from "react";
import { doctorApi } from "../../api/doctorApi";
import StatusBadge from "../../components/common/StatusBadge";

export default function MyDoctorProfilePage() {
  const [doctor, setDoctor] = useState(null);
  const [form, setForm] = useState({ phone: "", email: "" });
  const [savedForm, setSavedForm] = useState({ phone: "", email: "" });
  const [editing, setEditing] = useState(false);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");

  useEffect(() => {
    doctorApi
      .me()
      .then((response) => {
        const data = response.data.data;
        setDoctor(data);
        const loaded = { phone: data.phone || "", email: data.email || "" };
        setForm(loaded);
        setSavedForm(loaded);
      })
      .catch((err) => setError(err.response?.data?.message || "Couldn't load profile."));
  }, []);

  function startEditing() {
    setMessage("");
    setError("");
    setEditing(true);
  }

  function cancelEditing() {
    setForm(savedForm);
    setError("");
    setEditing(false);
  }

  async function submit(event) {
    event.preventDefault();
    setSaving(true);
    setError("");
    try {
      await doctorApi.updateMe(form);
      setSavedForm(form);
      setMessage("Profile updated.");
      setEditing(false);
    } catch (err) {
      setError(err.response?.data?.message || "Couldn't save profile.");
    } finally {
      setSaving(false);
    }
  }

  if (!doctor) return <main className="detail-page">{error || "Loading..."}</main>;

  return (
    <main className="detail-page">
      <div className="detail-page__header">
        <h1>My profile</h1>
        {!editing && (
          <button type="button" className="button--outline" onClick={startEditing}>
            Edit
          </button>
        )}
      </div>
      <div className="badge-row">
        <StatusBadge label={doctor.verified ? "Verified" : "Pending verification"} tone={doctor.verified ? "success" : "warning"} />
        <StatusBadge label={doctor.controlledSubstanceAuthorized ? "CS Authorized" : "Not CS Authorized"} />
      </div>
      <form className="modal-form modal-form--standalone" onSubmit={submit}>
        <label>
          Phone
          <input
            value={form.phone}
            onChange={(event) => setForm({ ...form, phone: event.target.value })}
            readOnly={!editing}
            disabled={!editing}
            required
          />
        </label>
        <label>
          Email
          <input
            type="email"
            value={form.email}
            onChange={(event) => setForm({ ...form, email: event.target.value })}
            readOnly={!editing}
            disabled={!editing}
            required
          />
        </label>
        {error && <p className="form-error">{error}</p>}
        {message && <p className="form-success">{message}</p>}
        {editing && (
          <div className="header-actions">
            <button type="submit" disabled={saving}>
              {saving ? "Saving…" : "Save changes"}
            </button>
            <button type="button" className="button--outline" onClick={cancelEditing} disabled={saving}>
              Cancel
            </button>
          </div>
        )}
      </form>
    </main>
  );
}
