import { useEffect, useState } from "react";
import { scheduleApi } from "../../api/scheduleApi";

const DAYS = ["MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"];

export default function OperatingHoursPage() {
  const [hours, setHours] = useState(null);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    scheduleApi.getOperatingHours().then((response) => {
      const data = response.data.data || [];
      const map = {};
      DAYS.forEach((day) => {
        const existing = data.find((item) => item.dayOfWeek === day);
        map[day] = existing
          ? { open: existing.openTime || "09:00", close: existing.closeTime || "18:00", closed: existing.closedAllDay }
          : { open: "09:00", close: "18:00", closed: false };
      });
      setHours(map);
    }).catch((err) => setError(err.response?.data?.message || "Could not load operating hours."));
  }, []);

  function update(day, field, value) {
    setHours({ ...hours, [day]: { ...hours[day], [field]: value } });
  }

  async function save() {
    setSaving(true);
    setError("");
    setMessage("");
    try {
      for (const day of DAYS) {
        await scheduleApi.updateOperatingHours({
          dayOfWeek: day,
          openTime: hours[day].closed ? null : hours[day].open,
          closeTime: hours[day].closed ? null : hours[day].close,
          closedAllDay: hours[day].closed,
        });
      }
      setMessage("Operating hours saved.");
    } catch (err) {
      setError(err.response?.data?.message || "Could not save operating hours.");
    } finally {
      setSaving(false);
    }
  }

  if (!hours) return <main className="detail-page">{error || "Loading..."}</main>;

  return (
    <main className="detail-page">
      <h1>Operating hours</h1>
      <table className="data-table">
        <thead>
          <tr><th>Day</th><th>Open</th><th>Close</th><th>Closed</th></tr>
        </thead>
        <tbody>
          {DAYS.map((day) => (
            <tr key={day}>
              <td>{day}</td>
              <td><input type="time" value={hours[day].open} disabled={hours[day].closed} onChange={(event) => update(day, "open", event.target.value)} /></td>
              <td><input type="time" value={hours[day].close} disabled={hours[day].closed} onChange={(event) => update(day, "close", event.target.value)} /></td>
              <td><input type="checkbox" checked={hours[day].closed} onChange={(event) => update(day, "closed", event.target.checked)} /></td>
            </tr>
          ))}
        </tbody>
      </table>
      {error && <p className="form-error">{error}</p>}
      {message && <p className="form-success">{message}</p>}
      <button type="button" onClick={save} disabled={saving}>{saving ? "Saving…" : "Save operating hours"}</button>
    </main>
  );
}
