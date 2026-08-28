import { useCallback, useEffect, useState } from "react";
import BackButton from "../../components/common/BackButton";
import {
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip,
  ResponsiveContainer, Cell
} from "recharts";
import { reportApi } from "../../api/reportApi";
import DateRangeFilter from "../../components/reports/DateRangeFilter";
import StatCard from "../../components/dashboard/StatCard";

const AXIS = "#8890a8";
const GRID = "#262f47";
const TT = { background: "#1b2338", border: "1px solid #262f47", borderRadius: 8, color: "#edf0f7", fontSize: 12 };

export default function SlowMovingReportPage() {
  const [rows, setRows] = useState([]);
  const [dates, setDates] = useState({ startDate: "", endDate: "" });
  const [error, setError] = useState("");

  const load = useCallback(async () => {
    try {
      const res = await reportApi.getSlowMoving({
        startDate: dates.startDate || undefined,
        endDate: dates.endDate || undefined,
      });
      setRows(res.data.data || []);
    } catch (err) {
      setError(err.response?.data?.message || "Could not load slow-moving stock.");
    }
  }, [dates]);

  useEffect(() => { const t = setTimeout(load, 0); return () => clearTimeout(t); }, [load]);

  // Sort ascending (slowest first) for the chart
  const chartData = [...rows]
    .sort((a, b) => Number(a.totalQuantityDispensed) - Number(b.totalQuantityDispensed))
    .slice(0, 12)
    .map((r) => ({ name: r.drugGenericName, qty: Number(r.totalQuantityDispensed) }));

  const totalQty = rows.reduce((s, r) => s + Number(r.totalQuantityDispensed), 0);

  return (
    <main className="list-page report-page">
      <BackButton to="/reports" />
      <div className="list-page__header">
        <div>
          <span className="report-page__eyebrow">Inventory report</span>
          <h1>Slow Moving Stock</h1>
        </div>
      </div>

      <DateRangeFilter {...dates} onChange={setDates} />
      {error && <p className="form-error">{error}</p>}

      <div className="stat-grid" style={{ margin: "1rem 0 1.5rem" }}>
        <StatCard tone="amber" label="Slow-moving drugs" value={rows.length} />
        <StatCard tone="violet" label="Total qty dispensed" value={totalQty.toLocaleString()} />
      </div>

      {chartData.length > 0 && (
        <div className="chart-card" style={{ marginBottom: "1.1rem" }}>
          <div className="chart-card__header">
            <div>
              <p className="chart-card__title">Lowest-movement drugs (up to 12)</p>
              <p className="chart-card__subtitle">Quantity dispensed ascending — slowest at left</p>
            </div>
          </div>
          <ResponsiveContainer width="100%" height={260}>
            <BarChart data={chartData} margin={{ top: 4, right: 8, left: -18, bottom: 0 }}>
              <CartesianGrid stroke={GRID} vertical={false} />
              <XAxis dataKey="name" stroke={AXIS} tick={{ fontSize: 10, fill: AXIS }} axisLine={false} tickLine={false} interval={0} angle={-30} textAnchor="end" height={55} />
              <YAxis stroke={AXIS} tick={{ fontSize: 11, fill: AXIS }} axisLine={false} tickLine={false} width={36} />
              <Tooltip contentStyle={TT} />
              <Bar dataKey="qty" name="Qty dispensed" radius={[6, 6, 0, 0]} maxBarSize={40}>
                {chartData.map((_, i) => <Cell key={i} fill={i < 3 ? "#fb7185" : "#f59e0b"} />)}
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
          {!rows.length && <tr><td colSpan="2">No slow-moving items found.</td></tr>}
        </tbody>
      </table>
    </main>
  );
}
