import { useCallback, useEffect, useState } from "react";
import BackButton from "../../components/common/BackButton";
import {
  BarChart, Bar, Cell, XAxis, YAxis, CartesianGrid, Tooltip,
  ResponsiveContainer, PieChart, Pie
} from "recharts";
import { reportApi } from "../../api/reportApi";
import StatCard from "../../components/dashboard/StatCard";

const PIE_COLORS = ["#14b8a6", "#f59e0b", "#fb7185", "#8b5cf6", "#34d399", "#3b82f6"];
const AXIS = "#8890a8";
const GRID = "#262f47";

export default function InventorySummaryReportPage() {
  const [data, setData] = useState(null);
  const [error, setError] = useState("");

  const load = useCallback(async () => {
    try {
      const res = await reportApi.getInventorySummary();
      setData(res.data.data);
    } catch (err) {
      setError(err.response?.data?.message || "Could not load inventory summary.");
    }
  }, []);

  useEffect(() => { load(); }, [load]);

  const batchBreakdown = data
    ? [
        { name: "Active", value: Number(data.activeBatchCount) },
        { name: "Near expiry", value: Number(data.nearExpiryBatchCount) },
        { name: "Expired", value: Number(data.expiredBatchCount) },
        { name: "Quarantined", value: Number(data.quarantinedBatchCount) },
        { name: "Exhausted", value: Number(data.exhaustedBatchCount) },
      ].filter((d) => d.value > 0)
    : [];

  const total = batchBreakdown.reduce((s, d) => s + d.value, 0);

  return (
    <main className="list-page report-page">
      <BackButton to="/reports" />
      <div className="list-page__header">
        <div>
          <span className="report-page__eyebrow">Inventory report</span>
          <h1>Inventory Summary</h1>
        </div>
      </div>

      {error && <p className="form-error">{error}</p>}

      {data && (
        <>
          <div className="stat-grid" style={{ marginBottom: "1.5rem" }}>
            <StatCard label="Total drugs" value={data.totalDrugs} />
            <StatCard tone="violet" label="Active batches" value={data.activeBatchCount} />
            <StatCard tone="amber" label="Near expiry" value={data.nearExpiryBatchCount} />
            <StatCard tone="danger" label="Low-stock drugs" value={data.lowStockDrugCount} />
          </div>

          <div className="chart-grid" style={{ gap: "1.1rem", display: "grid", gridTemplateColumns: "1.5fr 1fr" }}>
            {/* Bar chart — batch counts by status */}
            <div className="chart-card">
              <div className="chart-card__header">
                <div>
                  <p className="chart-card__title">Batch status breakdown</p>
                  <p className="chart-card__subtitle">Count per status category</p>
                </div>
              </div>
              <ResponsiveContainer width="100%" height={240}>
                <BarChart data={batchBreakdown} margin={{ top: 4, right: 8, left: -18, bottom: 0 }}>
                  <CartesianGrid stroke={GRID} vertical={false} />
                  <XAxis dataKey="name" stroke={AXIS} tick={{ fontSize: 11, fill: AXIS }} axisLine={false} tickLine={false} />
                  <YAxis stroke={AXIS} tick={{ fontSize: 11, fill: AXIS }} axisLine={false} tickLine={false} width={36} />
                  <Tooltip contentStyle={{ background: "#1b2338", border: "1px solid #262f47", borderRadius: 8, color: "#edf0f7", fontSize: 12 }} />
                  <Bar dataKey="value" radius={[6, 6, 0, 0]} maxBarSize={44}>
                    {batchBreakdown.map((_, i) => <Cell key={i} fill={PIE_COLORS[i % PIE_COLORS.length]} />)}
                  </Bar>
                </BarChart>
              </ResponsiveContainer>
            </div>

            {/* Donut — proportion */}
            <div className="chart-card">
              <div className="chart-card__header">
                <p className="chart-card__title">Batch proportion</p>
              </div>
              {batchBreakdown.length > 0 ? (
                <div className="donut-card__body" style={{ marginTop: "0.5rem" }}>
                  <ResponsiveContainer width={130} height={130}>
                    <PieChart>
                      <Pie data={batchBreakdown} dataKey="value" nameKey="name" innerRadius={38} outerRadius={58} paddingAngle={3} strokeWidth={0}>
                        {batchBreakdown.map((_, i) => <Cell key={i} fill={PIE_COLORS[i % PIE_COLORS.length]} />)}
                      </Pie>
                    </PieChart>
                  </ResponsiveContainer>
                  <ul className="donut-card__legend">
                    {batchBreakdown.map((d, i) => (
                      <li key={d.name} className="donut-card__legend-item">
                        <span className="donut-card__legend-dot" style={{ background: PIE_COLORS[i % PIE_COLORS.length] }} />
                        {d.name} — {d.value}{total > 0 ? ` (${Math.round((d.value / total) * 100)}%)` : ""}
                      </li>
                    ))}
                  </ul>
                </div>
              ) : (
                <p className="chart-card__empty">No batch data.</p>
              )}
            </div>
          </div>

          <div className="chart-card" style={{ marginTop: "1.1rem" }}>
            <p className="chart-card__title">Total stock value</p>
            <p style={{ fontSize: "2rem", fontWeight: 700, color: "var(--color-primary)", margin: "0.5rem 0 0" }}>
              ₹{Number(data.totalStockValue ?? 0).toLocaleString("en-IN", { minimumFractionDigits: 2 })}
            </p>
          </div>
        </>
      )}
    </main>
  );
}
