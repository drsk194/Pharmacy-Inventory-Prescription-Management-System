import { useCallback, useEffect, useState } from "react";
import BackButton from "../../components/common/BackButton";
import {
  BarChart, Bar, Cell, XAxis, YAxis, CartesianGrid, Tooltip,
  ResponsiveContainer, PieChart, Pie
} from "recharts";
import { reportApi } from "../../api/reportApi";
import DateRangeFilter from "../../components/reports/DateRangeFilter";
import StatCard from "../../components/dashboard/StatCard";

const STATUS_COLORS = ["#14b8a6", "#8b5cf6", "#f59e0b", "#34d399", "#fb7185", "#3b82f6"];
const SOURCE_COLORS = ["#3b82f6", "#f59e0b", "#34d399", "#8b5cf6"];
const AXIS = "#8890a8";
const GRID = "#262f47";
const TT = { background: "#1b2338", border: "1px solid #262f47", borderRadius: 8, color: "#edf0f7", fontSize: 12 };

function exportCsv(data, dates) {
  const rows = [
    ["Dimension", "Label", "Count"],
    ...data.byStatus.map((r) => ["Status", r.label, r.count]),
    ...data.bySource.map((r) => ["Source", r.label, r.count]),
  ];
  const csv = rows.map((r) => r.join(",")).join("\n");
  const url = URL.createObjectURL(new Blob([csv], { type: "text/csv" }));
  const a = document.createElement("a");
  a.href = url; a.download = "prescription-volume.csv"; a.click();
  URL.revokeObjectURL(url);
}

export default function PrescriptionVolumeReportPage() {
  const [data, setData] = useState(null);
  const [dates, setDates] = useState({ startDate: "", endDate: "" });
  const [error, setError] = useState("");

  const load = useCallback(async () => {
    try {
      const res = await reportApi.getPrescriptionVolume({
        startDate: dates.startDate || undefined,
        endDate: dates.endDate || undefined,
      });
      setData(res.data.data);
    } catch (err) {
      setError(err.response?.data?.message || "Could not load prescription volume.");
    }
  }, [dates]);

  useEffect(() => { const t = setTimeout(load, 0); return () => clearTimeout(t); }, [load]);

  const byStatus = data?.byStatus?.map((r) => ({ name: r.label, value: Number(r.count) })) ?? [];
  const bySource = data?.bySource?.map((r) => ({ name: r.label, value: Number(r.count) })) ?? [];
  const statusTotal = byStatus.reduce((s, d) => s + d.value, 0);
  const sourceTotal = bySource.reduce((s, d) => s + d.value, 0);

  return (
    <main className="list-page report-page">
      <BackButton to="/reports" />
      <div className="list-page__header">
        <div>
          <span className="report-page__eyebrow">Dispensing report</span>
          <h1>Prescription Volume</h1>
        </div>
        {data && (
          <button className="report-page__download" type="button" onClick={() => exportCsv(data, dates)}>
            ↓ Download CSV
          </button>
        )}
      </div>

      <DateRangeFilter {...dates} onChange={setDates} />
      {error && <p className="form-error">{error}</p>}

      {data && (
        <>
          <div className="stat-grid" style={{ margin: "1rem 0 1.5rem" }}>
            <StatCard label="Total prescriptions" value={data.totalInPeriod} />
          </div>

          <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: "1.1rem" }}>
            {/* By status bar */}
            <div className="chart-card">
              <div className="chart-card__header">
                <div>
                  <p className="chart-card__title">By status</p>
                  <p className="chart-card__subtitle">Prescription count per status</p>
                </div>
              </div>
              {byStatus.length > 0 ? (
                <ResponsiveContainer width="100%" height={240}>
                  <BarChart data={byStatus} margin={{ top: 4, right: 8, left: -18, bottom: 0 }}>
                    <CartesianGrid stroke={GRID} vertical={false} />
                    <XAxis dataKey="name" stroke={AXIS} tick={{ fontSize: 11, fill: AXIS }} axisLine={false} tickLine={false} />
                    <YAxis stroke={AXIS} tick={{ fontSize: 11, fill: AXIS }} axisLine={false} tickLine={false} width={36} />
                    <Tooltip contentStyle={TT} />
                    <Bar dataKey="value" radius={[6, 6, 0, 0]} maxBarSize={44}>
                      {byStatus.map((_, i) => <Cell key={i} fill={STATUS_COLORS[i % STATUS_COLORS.length]} />)}
                    </Bar>
                  </BarChart>
                </ResponsiveContainer>
              ) : <p className="chart-card__empty">No data for this range.</p>}
            </div>

            {/* By source donut */}
            <div className="chart-card">
              <div className="chart-card__header">
                <p className="chart-card__title">By source</p>
              </div>
              {bySource.length > 0 ? (
                <div className="donut-card__body" style={{ marginTop: "0.5rem" }}>
                  <ResponsiveContainer width={130} height={130}>
                    <PieChart>
                      <Pie data={bySource} dataKey="value" nameKey="name" innerRadius={38} outerRadius={58} paddingAngle={3} strokeWidth={0}>
                        {bySource.map((_, i) => <Cell key={i} fill={SOURCE_COLORS[i % SOURCE_COLORS.length]} />)}
                      </Pie>
                    </PieChart>
                  </ResponsiveContainer>
                  <ul className="donut-card__legend">
                    {bySource.map((d, i) => (
                      <li key={d.name} className="donut-card__legend-item">
                        <span className="donut-card__legend-dot" style={{ background: SOURCE_COLORS[i % SOURCE_COLORS.length] }} />
                        {d.name} — {d.value}{sourceTotal > 0 ? ` (${Math.round((d.value / sourceTotal) * 100)}%)` : ""}
                      </li>
                    ))}
                  </ul>
                </div>
              ) : <p className="chart-card__empty">No source data.</p>}
            </div>
          </div>

          {/* Combined table */}
          <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: "1.1rem", marginTop: "1.1rem" }}>
            <table className="data-table">
              <thead><tr><th>Status</th><th>Count</th><th>Share</th></tr></thead>
              <tbody>
                {byStatus.map((r) => (
                  <tr key={r.name}>
                    <td>{r.name}</td>
                    <td>{r.value}</td>
                    <td>{statusTotal > 0 ? `${Math.round((r.value / statusTotal) * 100)}%` : "—"}</td>
                  </tr>
                ))}
                {!byStatus.length && <tr><td colSpan="3">No data.</td></tr>}
              </tbody>
            </table>
            <table className="data-table">
              <thead><tr><th>Source</th><th>Count</th><th>Share</th></tr></thead>
              <tbody>
                {bySource.map((r) => (
                  <tr key={r.name}>
                    <td>{r.name}</td>
                    <td>{r.value}</td>
                    <td>{sourceTotal > 0 ? `${Math.round((r.value / sourceTotal) * 100)}%` : "—"}</td>
                  </tr>
                ))}
                {!bySource.length && <tr><td colSpan="3">No data.</td></tr>}
              </tbody>
            </table>
          </div>
        </>
      )}
    </main>
  );
}
