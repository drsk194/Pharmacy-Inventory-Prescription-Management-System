import { useCallback, useEffect, useState } from "react";
import { userManagementApi } from "../../api/userManagementApi";
import Pagination from "../../components/common/Pagination";
import StatusBadge from "../../components/common/StatusBadge";
import UserFormModal from "../../components/admin/UserFormModal";
import Modal from "../../components/common/Modal";

const ROLES = ["ROLE_ADMIN", "ROLE_PHARMACIST", "ROLE_TECHNICIAN", "ROLE_PROCUREMENT_OFFICER", "ROLE_AUDITOR", "ROLE_DOCTOR"];

export default function UserManagementPage() {
  const [rows, setRows] = useState([]);
  const [search, setSearch] = useState("");
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [open, setOpen] = useState(false);
  const [editing, setEditing] = useState(null);
  const [draft, setDraft] = useState([]);
  const [error, setError] = useState("");
  const [pinTarget, setPinTarget] = useState(null);
  const [pin, setPin] = useState("");

  const load = useCallback(async () => {
    setError("");
    try {
      const response = await userManagementApi.list({ page, size: 20, search: search || undefined });
      const data = response.data.data;
      setRows(data.content || data);
      setTotalPages(data.totalPages || 0);
    } catch (err) {
      setError(err.response?.data?.message || "Could not load users.");
    }
  }, [page, search]);

  useEffect(() => {
    const timer = setTimeout(load, 300);
    return () => clearTimeout(timer);
  }, [load]);

  async function status(row) {
    try {
      await userManagementApi.setStatus(row.id, !row.active);
      setError("");
      load();
    } catch (err) {
      setError(err.response?.data?.message || "Could not update status.");
    }
  }

  async function saveRoles(id) {
    try {
      await userManagementApi.setRoles(id, draft);
      setEditing(null);
      setError("");
      load();
    } catch (err) {
      setError(err.response?.data?.message || "Could not update roles.");
    }
  }

  async function provisionPin(event) {
    event.preventDefault();
    if (!/^\d{4,6}$/.test(pin)) {
      setError("PIN must be 4-6 digits.");
      return;
    }
    try {
      await userManagementApi.setControlledSubstancePin(pinTarget.id, pin);
      setPinTarget(null);
      setPin("");
      setError("");
    } catch (err) {
      setError(err.response?.data?.message || "Could not provision controlled-substance PIN.");
    }
  }

  return (
    <main className="list-page">
      <div className="list-page__header">
        <h1>User management</h1>
        <button type="button" onClick={() => { setError(""); setOpen(true); }}>Create Staff User</button>
      </div>
      <input className="list-page__search" placeholder="Search users..." value={search} onChange={(event) => { setSearch(event.target.value); setPage(0); }} />
      {error && <p className="form-error">{error}</p>}
      <table className="data-table">
        <thead><tr><th>Name</th><th>Staff ID</th><th>Roles</th><th>Status</th><th /></tr></thead>
        <tbody>
          {rows.map((row) => <tr key={row.id}>
            <td>{row.fullName}</td>
            <td>{row.staffId}</td>
            <td>{editing === row.id ? <div className="filter-row">{ROLES.map((role) => <label className="checkbox-label" key={role}><input type="checkbox" checked={draft.includes(role)} onChange={() => setDraft(draft.includes(role) ? draft.filter((item) => item !== role) : [...draft, role])} />{role.replace("ROLE_", "")}</label>)}<button type="button" onClick={() => saveRoles(row.id)}>Save</button></div> : (row.roles || []).join(", ")}</td>
            <td><StatusBadge label={row.active ? "Active" : "Inactive"} tone={row.active ? "success" : "neutral"} /></td>
            <td><button type="button" onClick={() => status(row)}>{row.active ? "Deactivate" : "Activate"}</button><button type="button" onClick={() => { setEditing(row.id); setDraft(row.roles || []); }}>Edit roles</button>{(row.roles || []).includes("ROLE_TECHNICIAN") && <button type="button" onClick={() => { setPinTarget(row); setPin(""); setError(""); }}>Set CS PIN</button>}</td>
          </tr>)}
          {!rows.length && <tr><td colSpan="5">No users found.</td></tr>}
        </tbody>
      </table>
      <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />
      {open && <UserFormModal onClose={() => setOpen(false)} onSaved={() => { setOpen(false); load(); }} />}
      {pinTarget && <Modal title={`Set CS PIN for ${pinTarget.fullName}`} onClose={() => { setPinTarget(null); setPin(""); }}><form className="modal-form" onSubmit={provisionPin}><label>New PIN (4-6 digits)<input type="password" inputMode="numeric" pattern="\d{4,6}" minLength="4" maxLength="6" value={pin} onChange={(event) => setPin(event.target.value)} required /></label><button type="submit">Save PIN</button></form></Modal>}
    </main>
  );
}
