import { useCallback, useEffect, useState } from "react";
import BackButton from "../../components/common/BackButton";
import {
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip,
  ResponsiveContainer, Cell, PieChart, Pie, LabelList
} from "recharts";
import { reportApi } from "../../api/reportApi";
import DateRangeFilter from "../../components/reports/DateRangeFilter";
import StatCard from "../../components/dashboard/StatCard";

const COLORS = ["#8b5cf6", "#14b8a6", "#f59e0b", "#34d399", "#fb7185", "#3b82f6", "#ec4899", "#10b981"];
const AXIS = "#8890a8";
const GRID = "#262f47";
const TT = { background: "#1b2338", border: "1px solid #262f47", borderRadius: 8, color: "#edf0f7", fontSize: 12 };

function exportCsv(data) {
  const rows = [["Supplier", "Total Spend"], ...data.bySupplier.map((r) => [r.label, r.amount])];
  const csv = rows.map((r) => r.join(",")).join("\n");
  const url = URL.createObjectURL(new Blob([csv], { type: "text/csv" }));
  const a = document.createElement("a"); a.href = url; a.download = "procurement-spending.csv"; a.click();
  URL.revokeObjectURL(url);
}

export default function ProcurementSpendingReportPage() {
  const [data, setData] = useState(null);
  const [dates, setDates] = useState({ startDate: "", endDate: "" });
  const [error, setError] = useState("");

  const load = useCallback(async () => {
    try {
      const res = await reportApi.getProcurementSpending({
        startDate: dates.startDate || undefined,
        endDate: dates.endDate || undefined,
      });
      setData(res.data.data);
    } catch (err) {
      setError(err.response?.data?.message || "Could not load procurement spending.");
    }
  }, [dates]);

  useEffect(() => { const t = setTimeout(load, 0); return () => clearTimeout(t); }, [load]);

  const bySupplier = data?.bySupplier?.map((r) => ({ name: r.label, amount: Number(r.amount) })) ?? [];
  const total = Number(data?.totalSpending ?? 0);

  return (
    <main className="list-page report-page">
      <BackButton to="/reports" />
      <div className="list-page__header">
        <div>
          <span className="report-page__eyebrow">Procurement report</span>
          <h1>Procurement Spending</h1>
        </div>
        {data && (
          <button className="report-page__download" type="button" onClick={() => exportCsv(data)}>
            ↓ Download CSV
          </button>
        )}
      </div>

      <DateRangeFilter {...dates} onChange={setDates} />
      {error && <p className="form-error">{error}</p>}

      {data && (
        <>
          <div className="stat-grid" style={{ margin: "1rem 0 1.5rem" }}>
            <StatCard tone="violet" label="Total spending" value={`₹${total.toLocaleString("en-IN", { minimumFractionDigits: 2 })}`} />
            <StatCard label="Suppliers" value={bySupplier.length} />
          </div>

          {bySupplier.length > 0 && (
            <div style={{ display: "grid", gridTemplateColumns: "1.5fr 1fr", gap: "1.1rem", marginBottom: "1.1rem" }}>
              {/* Bar chart */}
              <div className="chart-card">
                <div className="chart-card__header">
                  <div>
                    <p className="chart-card__title">Spending by supplier</p>
                    <p className="chart-card__subtitle">Total value of approved purchase orders</p>
                  </div>
                </div>
                <ResponsiveContainer width="100%" height={Math.max(200, bySupplier.length * 44 + 40)}>
                  <BarChart data={bySupplier} layout="vertical" margin={{ top: 4, right: 80, left: 8, bottom: 0 }}>
                    <CartesianGrid stroke={GRID} horizontal={false} />
                    <XAxis type="number" stroke={AXIS} tick={{ fontSize: 11, fill: AXIS }} axisLine={false} tickLine={false} tickFormatter={(v) => `₹${(v / 1000).toFixed(0)}k`} />
                    <YAxis type="category" dataKey="name" stroke={AXIS} tick={{ fontSize: 11, fill: AXIS }} axisLine={false} tickLine={false} width={130} />
                    <Tooltip contentStyle={TT} formatter={(v) => [`₹${Number(v).toLocaleString("en-IN", { minimumFractionDigits: 2 })}`, "Total spend"]} />
                    <Bar dataKey="amount" name="Total spend" radius={[0, 6, 6, 0]} maxBarSize={26}>
                      <LabelList dataKey="amount" position="right" formatter={(v) => `₹${(v / 1000).toFixed(1)}k`} style={{ fill: "#edf0f7", fontSize: 10 }} />
                      {bySupplier.map((_, i) => <Cell key={i} fill={COLORS[i % COLORS.length]} />)}
                    </Bar>
                  </BarChart>
                </ResponsiveContainer>
              </div>

              {/* Donut share */}
              <div className="chart-card">
                <div className="chart-card__header">
                  <p className="chart-card__title">Supplier share</p>
                </div>
                <div className="donut-card__body" style={{ marginTop: "0.5rem" }}>
                  <ResponsiveContainer width={130} height={130}>
                    <PieChart>
                      <Pie data={bySupplier} dataKey="amount" nameKey="name" innerRadius={38} outerRadius={58} paddingAngle={3} strokeWidth={0}>
                        {bySupplier.map((_, i) => <Cell key={i} fill={COLORS[i % COLORS.length]} />)}
                      </Pie>
                    </PieChart>
                  </ResponsiveContainer>
                  <ul className="donut-card__legend">
                    {bySupplier.map((d, i) => (
                      <li key={d.name} className="donut-card__legend-item">
                        <span className="donut-card__legend-dot" style={{ background: COLORS[i % COLORS.length] }} />
                        {d.name} — {total > 0 ? `${Math.round((d.amount / total) * 100)}%` : "—"}
                      </li>
                    ))}
                  </ul>
                </div>
              </div>
            </div>
          )}

          <table className="data-table">
            <thead>
              <tr><th>Supplier</th><th>Total spend</th><th>Share</th></tr>
            </thead>
            <tbody>
              {bySupplier.map((r) => (
                <tr key={r.name}>
                  <td>{r.name}</td>
                  <td>₹{r.amount.toLocaleString("en-IN", { minimumFractionDigits: 2 })}</td>
                  <td>{total > 0 ? `${Math.round((r.amount / total) * 100)}%` : "—"}</td>
                </tr>
              ))}
              {!bySupplier.length && <tr><td colSpan="3">No data for this range.</td></tr>}
            </tbody>
          </table>
        </>
      )}
    </main>
  );
}
