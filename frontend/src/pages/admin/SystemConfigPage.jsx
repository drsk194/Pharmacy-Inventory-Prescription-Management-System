import { useCallback, useEffect, useState } from "react";
import { systemConfigApi } from "../../api/systemConfigApi";

export default function SystemConfigPage() {
  const [rows, setRows] = useState([]);
  const [category, setCategory] = useState("");
  const [editing, setEditing] = useState(null);
  const [value, setValue] = useState("");
  const [error, setError] = useState("");

  const load = useCallback(async () => {
    try {
      const response = await systemConfigApi.list({ category: category || undefined });
      const data = response.data.data;
      setRows(data.content || data);
    } catch (err) {
      setError(err.response?.data?.message || "Could not load configuration.");
    }
  }, [category]);

  useEffect(() => {
    const timer = setTimeout(load, 0);
    return () => clearTimeout(timer);
  }, [load]);

  async function save(row) {
    try {
      await systemConfigApi.update(row.id, {
        configKey: row.configKey,
        configValue: value,
        dataType: row.dataType,
        category: row.category,
        description: row.description,
      });
      setEditing(null);
      setError("");
      load();
    } catch (err) {
      setError(err.response?.data?.message || "Could not save setting.");
    }
  }

  return (
    <main className="list-page">
      <h1>System configuration</h1>
      <select value={category} onChange={(event) => setCategory(event.target.value)}>
        <option value="">All categories</option>
        {[...new Set(rows.map((row) => row.category).filter(Boolean))].map((item) => (
          <option key={item}>{item}</option>
        ))}
      </select>
      {error && <p className="form-error">{error}</p>}
      <table className="data-table">
        <thead>
          <tr>
            <th>Key</th>
            <th>Value</th>
            <th>Category</th>
            <th />
          </tr>
        </thead>
        <tbody>
          {rows.map((row) => (
            <tr key={row.id}>
              <td>{row.configKey}</td>
              <td>{editing === row.id ? <input value={value} onChange={(event) => setValue(event.target.value)} /> : row.configValue}</td>
              <td>{row.category}</td>
              <td>
                {editing === row.id ? (
                  <button type="button" onClick={() => save(row)}>Save</button>
                ) : (
                  <button type="button" onClick={() => { setEditing(row.id); setValue(row.configValue); }}>Edit</button>
                )}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </main>
  );
}
