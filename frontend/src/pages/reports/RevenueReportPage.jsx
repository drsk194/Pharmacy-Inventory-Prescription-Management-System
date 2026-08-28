import { useCallback, useEffect, useState } from "react";
import BackButton from "../../components/common/BackButton";
import {
  AreaChart, Area, XAxis, YAxis, CartesianGrid, Tooltip,
  ResponsiveContainer
} from "recharts";
import { reportApi } from "../../api/reportApi";
import DateRangeFilter from "../../components/reports/DateRangeFilter";
import StatCard from "../../components/dashboard/StatCard";

const AXIS = "#8890a8";
const GRID = "#262f47";
const TT = { background: "#1b2338", border: "1px solid #262f47", borderRadius: 8, color: "#edf0f7", fontSize: 12 };

function exportCsv(data) {
  const rows = [["Date", "Revenue"], ...(data.dailyBreakdown ?? []).map((r) => [r.label, r.amount])];
  const csv = rows.map((r) => r.join(",")).join("\n");
  const url = URL.createObjectURL(new Blob([csv], { type: "text/csv" }));
  const a = document.createElement("a"); a.href = url; a.download = "revenue.csv"; a.click();
  URL.revokeObjectURL(url);
}

export default function RevenueReportPage() {
  const [data, setData] = useState(null);
  const [dates, setDates] = useState({ startDate: "", endDate: "" });
  const [error, setError] = useState("");

  const load = useCallback(async () => {
    try {
      const res = await reportApi.getRevenue({
        startDate: dates.startDate || undefined,
        endDate: dates.endDate || undefined,
      });
      setData(res.data.data);
    } catch (err) {
      setError(err.response?.data?.message || "Could not load revenue data.");
    }
  }, [dates]);

  useEffect(() => { const t = setTimeout(load, 0); return () => clearTimeout(t); }, [load]);

  const daily = data?.dailyBreakdown?.map((r) => ({ date: r.label, revenue: Number(r.amount) })) ?? [];
  const total = Number(data?.totalRevenue ?? 0);
  const avg = daily.length > 0 ? total / daily.length : 0;
  const peak = daily.length > 0 ? Math.max(...daily.map((d) => d.revenue)) : 0;

  return (
    <main className="list-page report-page">
      <BackButton to="/reports" />
      <div className="list-page__header">
        <div>
          <span className="report-page__eyebrow">Financial report</span>
          <h1>Revenue</h1>
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
            <StatCard tone="emerald" label="Total revenue" value={`₹${total.toLocaleString("en-IN", { minimumFractionDigits: 2 })}`} />
            <StatCard label="Days in period" value={daily.length} />
            <StatCard tone="violet" label="Daily average" value={`₹${avg.toLocaleString("en-IN", { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`} />
            <StatCard tone="amber" label="Peak day" value={`₹${peak.toLocaleString("en-IN", { minimumFractionDigits: 2 })}`} />
          </div>

          {daily.length > 0 && (
            <div className="chart-card" style={{ marginBottom: "1.1rem" }}>
              <div className="chart-card__header">
                <div>
                  <p className="chart-card__title">Daily revenue trend</p>
                  <p className="chart-card__subtitle">Revenue collected per day in selected period</p>
                </div>
              </div>
              <ResponsiveContainer width="100%" height={280}>
                <AreaChart data={daily} margin={{ top: 4, right: 16, left: 4, bottom: 0 }}>
                  <defs>
                    <linearGradient id="rev-gradient" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="0%" stopColor="#34d399" stopOpacity={0.45} />
                      <stop offset="100%" stopColor="#34d399" stopOpacity={0} />
                    </linearGradient>
                  </defs>
                  <CartesianGrid stroke={GRID} vertical={false} />
                  <XAxis dataKey="date" stroke={AXIS} tick={{ fontSize: 10, fill: AXIS }} axisLine={false} tickLine={false} interval="preserveStartEnd" />
                  <YAxis stroke={AXIS} tick={{ fontSize: 11, fill: AXIS }} axisLine={false} tickLine={false} width={64} tickFormatter={(v) => `₹${(v / 1000).toFixed(0)}k`} />
                  <Tooltip contentStyle={TT} formatter={(v) => [`₹${Number(v).toLocaleString("en-IN", { minimumFractionDigits: 2 })}`, "Revenue"]} />
                  <Area type="monotone" dataKey="revenue" stroke="#34d399" strokeWidth={2.5} fill="url(#rev-gradient)" />
                </AreaChart>
              </ResponsiveContainer>
            </div>
          )}

          <table className="data-table">
            <thead>
              <tr><th>Date</th><th>Revenue</th></tr>
            </thead>
            <tbody>
              {daily.map((r) => (
                <tr key={r.date}>
                  <td>{r.date}</td>
                  <td>₹{r.revenue.toLocaleString("en-IN", { minimumFractionDigits: 2 })}</td>
                </tr>
              ))}
              {!daily.length && <tr><td colSpan="2">No revenue data for this range.</td></tr>}
            </tbody>
          </table>
        </>
      )}
    </main>
  );
}
