import { useEffect, useState } from "react";
import { patientApi } from "../../api/patientApi";

const FIELD_LABELS = {
  phone: "Phone",
  email: "Email",
  emergencyContactName: "Emergency Contact Name",
  emergencyContactPhone: "Emergency Contact Phone",
};

export default function MyProfilePage() {
  const [form, setForm] = useState(null);
  const [savedForm, setSavedForm] = useState(null);
  const [editing, setEditing] = useState(false);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");

  useEffect(() => {
    patientApi
      .me()
      .then((response) => {
        const data = response.data.data;
        const loaded = {
          phone: data.phone || "",
          email: data.email || "",
          emergencyContactName: data.emergencyContactName || "",
          emergencyContactPhone: data.emergencyContactPhone || "",
        };
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
      await patientApi.updateMe(form);
      setSavedForm(form);
      setMessage("Profile updated.");
      setEditing(false);
    } catch (err) {
      setError(err.response?.data?.message || "Couldn't save profile.");
    } finally {
      setSaving(false);
    }
  }

  if (!form) return <main className="detail-page">{error || "Loading..."}</main>;

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
      <form className="modal-form modal-form--standalone" onSubmit={submit}>
        {Object.keys(form).map((name) => (
          <label key={name}>
            {FIELD_LABELS[name] || name}
            <input
              name={name}
              type={name === "email" ? "email" : "text"}
              value={form[name]}
              onChange={(event) => setForm({ ...form, [name]: event.target.value })}
              readOnly={!editing}
              disabled={!editing}
            />
          </label>
        ))}
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
