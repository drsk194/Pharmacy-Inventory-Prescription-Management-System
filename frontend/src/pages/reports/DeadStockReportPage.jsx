import { useCallback, useEffect, useState } from "react";
import BackButton from "../../components/common/BackButton";
import {
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip,
  ResponsiveContainer, Cell, ReferenceLine
} from "recharts";
import { reportApi } from "../../api/reportApi";
import StatCard from "../../components/dashboard/StatCard";

const AXIS = "#8890a8";
const GRID = "#262f47";
const TT = { background: "#1b2338", border: "1px solid #262f47", borderRadius: 8, color: "#edf0f7", fontSize: 12 };

export default function DeadStockReportPage() {
  const [rows, setRows] = useState([]);
  const [error, setError] = useState("");

  const load = useCallback(async () => {
    try {
      const res = await reportApi.getDeadStock();
      setRows(res.data.data || []);
    } catch (err) {
      setError(err.response?.data?.message || "Could not load dead stock.");
    }
  }, []);

  useEffect(() => { load(); }, [load]);

  const top10 = [...rows]
    .sort((a, b) => Number(b.totalQuantityDispensed) - Number(a.totalQuantityDispensed))
    .slice(0, 10)
    .map((r) => ({ name: r.drugGenericName, qty: Number(r.totalQuantityDispensed) }));

  return (
    <main className="list-page report-page">
      <BackButton to="/reports" />
      <div className="list-page__header">
        <div>
          <span className="report-page__eyebrow">Inventory report</span>
          <h1>Dead Stock</h1>
        </div>
      </div>
      <p style={{ color: "var(--color-text-muted)", fontSize: "0.85rem", marginBottom: "1rem" }}>
        Drugs with zero or near-zero movement in the last 30 days.
      </p>

      {error && <p className="form-error">{error}</p>}

      <div className="stat-grid" style={{ marginBottom: "1.5rem" }}>
        <StatCard tone="amber" label="Dead-stock drugs" value={rows.length} />
        <StatCard
          tone="violet"
          label="Total qty on hand"
          value={rows.reduce((s, r) => s + Number(r.totalQuantityDispensed), 0).toLocaleString()}
        />
      </div>

      {top10.length > 0 && (
        <div className="chart-card" style={{ marginBottom: "1.1rem" }}>
          <div className="chart-card__header">
            <div>
              <p className="chart-card__title">Dead-stock quantity (top 10)</p>
              <p className="chart-card__subtitle">Total quantity on hand with no movement</p>
            </div>
          </div>
          <ResponsiveContainer width="100%" height={280}>
            <BarChart data={top10} layout="vertical" margin={{ top: 4, right: 24, left: 8, bottom: 0 }}>
              <CartesianGrid stroke={GRID} horizontal={false} />
              <XAxis type="number" stroke={AXIS} tick={{ fontSize: 11, fill: AXIS }} axisLine={false} tickLine={false} />
              <YAxis type="category" dataKey="name" stroke={AXIS} tick={{ fontSize: 11, fill: AXIS }} axisLine={false} tickLine={false} width={140} />
              <ReferenceLine x={0} stroke={GRID} />
              <Tooltip contentStyle={TT} />
              <Bar dataKey="qty" name="Qty on hand" radius={[0, 6, 6, 0]} maxBarSize={22}>
                {top10.map((_, i) => <Cell key={i} fill={i % 2 === 0 ? "#f59e0b" : "#fb7185"} />)}
              </Bar>
            </BarChart>
          </ResponsiveContainer>
        </div>
      )}

      <table className="data-table">
        <thead>
          <tr><th>Drug</th><th>Qty dispensed (period)</th></tr>
        </thead>
        <tbody>
          {rows.map((r) => (
            <tr key={r.drugId}>
              <td>{r.drugGenericName}</td>
              <td>{Number(r.totalQuantityDispensed).toLocaleString()}</td>
            </tr>
          ))}
          {!rows.length && <tr><td colSpan="2">No dead-stock items found.</td></tr>}
        </tbody>
      </table>
    </main>
  );
}
