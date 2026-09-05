import { useCallback, useEffect, useState } from "react";
import { drugApi } from "../../api/drugApi";
import { useAuth } from "../../hooks/useAuth";
import Pagination from "../../components/common/Pagination";
import StatusBadge from "../../components/common/StatusBadge";
import DrugFormModal from "../../components/drugs/DrugFormModal";

const TONE = { OTC: "neutral", SCHEDULE_H: "warning", SCHEDULE_H1: "danger", SCHEDULE_X: "danger" };

export default function DrugListPage() {
  const { user } = useAuth();
  const canManage = user?.roles?.includes("ROLE_ADMIN");
  const [rows, setRows] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [search, setSearch] = useState("");
  const [schedule, setSchedule] = useState("");
  const [activeOnly, setActiveOnly] = useState(true);
  const [open, setOpen] = useState(false);
  const [editing, setEditing] = useState(null);
  const [error, setError] = useState("");

  const load = useCallback(async () => {
    setError("");
    try {
      const response = await drugApi.list({ page, size: 20, search: search || undefined, schedule: schedule || undefined, activeOnly: activeOnly || undefined });
      const data = response.data.data;
      setRows(data.content || data);
      setTotalPages(data.totalPages || 0);
    } catch (err) {
      setError(err.response?.data?.message || "Couldn't load drugs.");
    }
  }, [page, search, schedule, activeOnly]);

  useEffect(() => {
    const timer = setTimeout(load, 300);
    return () => clearTimeout(timer);
  }, [load]);

  async function deactivate(row, event) {
    event.stopPropagation();
    if (!window.confirm(`Deactivate ${row.genericName}?`)) return;
    try {
      await drugApi.setStatus(row.id, false);
      setError("");
      load();
    } catch (err) {
      setError(err.response?.data?.message || "Couldn't deactivate drug.");
    }
  }

  function saved() {
    setOpen(false);
    setEditing(null);
    load();
  }

  return (
    <main className="list-page">
      <div className="list-page__header">
        <h1>Drugs</h1>
        {canManage && <button type="button" onClick={() => { setEditing(null); setError(""); setOpen(true); }}>Create Drug</button>}
      </div>
      <div className="filter-row">
        <input className="list-page__search" type="search" placeholder="Search drugs..." value={search} onChange={(event) => { setSearch(event.target.value); setPage(0); }} />
        <select value={schedule} onChange={(event) => { setSchedule(event.target.value); setPage(0); }}>
          <option value="">All schedules</option>
          {["OTC", "SCHEDULE_H", "SCHEDULE_H1", "SCHEDULE_X"].map((item) => <option key={item}>{item}</option>)}
        </select>
        <label className="checkbox-label"><input type="checkbox" checked={activeOnly} onChange={(event) => setActiveOnly(event.target.checked)} />Active only</label>
      </div>
      {error && <p className="form-error">{error}</p>}
      <table className="data-table">
        <thead>
          <tr><th>Name</th><th>Class</th><th>Schedule</th><th>Status</th>{canManage && <th />}</tr>
        </thead>
        <tbody>
          {rows.map((row) => (
            <tr key={row.id} onClick={canManage ? () => { setEditing(row); setError(""); setOpen(true); } : undefined} style={canManage ? undefined : { cursor: "default" }}>
              <td>{row.genericName}{row.brandName ? ` (${row.brandName})` : ""}</td>
              <td>{row.drugClass}</td>
              <td><StatusBadge label={row.schedule} tone={TONE[row.schedule]} /></td>
              <td><StatusBadge label={row.active ? "Active" : "Inactive"} tone={row.active ? "success" : "neutral"} /></td>
              {canManage && <td>{row.active && <button type="button" onClick={(event) => deactivate(row, event)}>Deactivate</button>}</td>}
            </tr>
          ))}
          {!rows.length && <tr><td colSpan={canManage ? 5 : 4}>No drugs found.</td></tr>}
        </tbody>
      </table>
      <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />
      {open && canManage && <DrugFormModal drug={editing} onClose={() => setOpen(false)} onSaved={saved} />}
    </main>
  );
}
