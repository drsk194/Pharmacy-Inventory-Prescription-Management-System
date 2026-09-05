import { Fragment, useCallback, useEffect, useState } from "react";
import { scheduleApi } from "../../api/scheduleApi";
import { userManagementApi } from "../../api/userManagementApi";
import ShiftFormModal from "../../components/admin/ShiftFormModal";

export default function ShiftManagementPage() {
  const [rows, setRows] = useState([]);
  const [users, setUsers] = useState([]);
  const [open, setOpen] = useState(false);
  const [membersId, setMembersId] = useState(null);
  const [selectedMembers, setSelectedMembers] = useState([]);
  const [error, setError] = useState("");

  const load = useCallback(async () => {
    try {
      const [shifts, usersResponse] = await Promise.all([
        scheduleApi.listShifts(),
        userManagementApi.list({ page: 0, size: 500 })
      ]);
      const shiftData = shifts.data.data;
      const userData = usersResponse.data.data;
      setRows(shiftData.content || shiftData);
      setUsers(userData.content || userData);
    } catch (err) {
      setError(err.response?.data?.message || "Could not load shifts.");
    }
  }, []);

  useEffect(() => {
    const timer = setTimeout(load, 0);
    return () => clearTimeout(timer);
  }, [load]);

  function beginMembers(row) {
    setError("");
    setMembersId(row.id);
    setSelectedMembers(row.assignedUserIds || []);
  }

  async function submitMembers(shiftId) {
    try {
      setError("");
      const row = rows.find((item) => item.id === shiftId);
      const currentIds = row?.assignedUserIds || [];
      await Promise.all([
        ...selectedMembers.filter((id) => !currentIds.includes(id)).map((id) => scheduleApi.assignShift(shiftId, id)),
        ...currentIds.filter((id) => !selectedMembers.includes(id)).map((id) => scheduleApi.unassignShift(shiftId, id))
      ]);
      setMembersId(null);
      await load();
    } catch (err) {
      setError(err.response?.data?.message || "Could not update shift members.");
    }
  }

  return <main className="list-page">
    <div className="list-page__header"><h1>Shift management</h1><button type="button" onClick={() => setOpen(true)}>Create Shift</button></div>
    {error && <p className="form-error">{error}</p>}
    <table className="data-table"><thead><tr><th>Name</th><th>Time</th><th>Assigned to</th><th /></tr></thead><tbody>
      {rows.map((row) => <Fragment key={row.id}><tr>
        <><td>{row.name}</td><td>{row.startTime} - {row.endTime}</td><td>{row.assignedUserNames?.length ? row.assignedUserNames.join(", ") : "Unassigned"}</td><td><button type="button" onClick={() => beginMembers(row)}>Assign members</button></td></>
      </tr>{membersId === row.id && <tr><td colSpan="4"><fieldset><legend>Assign members</legend>{users.map((user) => <label key={user.id}><input type="checkbox" checked={selectedMembers.includes(user.id)} onChange={(event) => setSelectedMembers(event.target.checked ? [...selectedMembers, user.id] : selectedMembers.filter((id) => id !== user.id))} /> {user.fullName}</label>)}<button type="button" onClick={() => submitMembers(row.id)}>Submit assignments</button><button type="button" onClick={() => setMembersId(null)}>Cancel</button></fieldset></td></tr>}</Fragment>)}
      {!rows.length && <tr><td colSpan="4">No shifts created.</td></tr>}
    </tbody></table>
    {open && <ShiftFormModal onClose={() => setOpen(false)} onSaved={() => { setOpen(false); load(); }} />}
  </main>;
}
