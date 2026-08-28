import { useCallback, useEffect, useState } from "react";
import BackButton from "../../components/common/BackButton";
import {
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip,
  ResponsiveContainer, Cell, LabelList
} from "recharts";
import { reportApi } from "../../api/reportApi";
import DateRangeFilter from "../../components/reports/DateRangeFilter";
import StatCard from "../../components/dashboard/StatCard";

const COLORS = ["#8b5cf6", "#14b8a6", "#f59e0b", "#34d399", "#fb7185", "#3b82f6", "#ec4899", "#10b981"];
const AXIS = "#8890a8";
const GRID = "#262f47";
const TT = { background: "#1b2338", border: "1px solid #262f47", borderRadius: 8, color: "#edf0f7", fontSize: 12 };

export default function PharmacistActivityReportPage() {
  const [rows, setRows] = useState([]);
  const [dates, setDates] = useState({ startDate: "", endDate: "" });
  const [error, setError] = useState("");

  const load = useCallback(async () => {
    try {
      const res = await reportApi.getPharmacistActivity({
        startDate: dates.startDate || undefined,
        endDate: dates.endDate || undefined,
      });
      setRows(res.data.data || []);
    } catch (err) {
      setError(err.response?.data?.message || "Could not load pharmacist activity.");
    }
  }, [dates]);

  useEffect(() => { const t = setTimeout(load, 0); return () => clearTimeout(t); }, [load]);

  const chartData = [...rows]
    .sort((a, b) => Number(b.count) - Number(a.count))
    .map((r) => ({ name: r.label, count: Number(r.count) }));

  const total = rows.reduce((s, r) => s + Number(r.count), 0);
  const top = chartData[0];

  return (
    <main className="list-page report-page">
      <BackButton to="/reports" />
      <div className="list-page__header">
        <div>
          <span className="report-page__eyebrow">Dispensing report</span>
          <h1>Pharmacist Activity</h1>
        </div>
      </div>

      <DateRangeFilter {...dates} onChange={setDates} />
      {error && <p className="form-error">{error}</p>}

      <div className="stat-grid" style={{ margin: "1rem 0 1.5rem" }}>
        <StatCard label="Total verified" value={total} />
        <StatCard tone="emerald" label="Pharmacists active" value={rows.length} />
        {top && <StatCard tone="violet" label={`Top: ${top.name}`} value={top.count} />}
      </div>

      {chartData.length > 0 && (
        <div className="chart-card" style={{ marginBottom: "1.1rem" }}>
          <div className="chart-card__header">
            <div>
              <p className="chart-card__title">Prescriptions verified per pharmacist</p>
              <p className="chart-card__subtitle">Sorted by activity — highest first</p>
            </div>
          </div>
          <ResponsiveContainer width="100%" height={Math.max(200, chartData.length * 42 + 40)}>
            <BarChart data={chartData} layout="vertical" margin={{ top: 4, right: 56, left: 8, bottom: 0 }}>
              <CartesianGrid stroke={GRID} horizontal={false} />
              <XAxis type="number" stroke={AXIS} tick={{ fontSize: 11, fill: AXIS }} axisLine={false} tickLine={false} />
              <YAxis type="category" dataKey="name" stroke={AXIS} tick={{ fontSize: 12, fill: AXIS }} axisLine={false} tickLine={false} width={140} />
              <Tooltip contentStyle={TT} />
              <Bar dataKey="count" name="Prescriptions verified" radius={[0, 6, 6, 0]} maxBarSize={26}>
                <LabelList dataKey="count" position="right" style={{ fill: "#edf0f7", fontSize: 11 }} />
                {chartData.map((_, i) => <Cell key={i} fill={COLORS[i % COLORS.length]} />)}
              </Bar>
            </BarChart>
          </ResponsiveContainer>
        </div>
      )}

      <table className="data-table">
        <thead>
          <tr><th>Pharmacist</th><th>Prescriptions verified</th><th>Share</th></tr>
        </thead>
        <tbody>
          {rows.map((r) => (
            <tr key={r.label}>
              <td>{r.label}</td>
              <td>{Number(r.count).toLocaleString()}</td>
              <td>{total > 0 ? `${Math.round((Number(r.count) / total) * 100)}%` : "—"}</td>
            </tr>
          ))}
          {!rows.length && <tr><td colSpan="3">No data for this range.</td></tr>}
        </tbody>
      </table>
    </main>
  );
}
