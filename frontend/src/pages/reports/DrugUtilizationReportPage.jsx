import { useCallback, useEffect, useState } from "react";
import BackButton from "../../components/common/BackButton";
import {
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip,
  ResponsiveContainer, Cell
} from "recharts";
import { reportApi } from "../../api/reportApi";
import DateRangeFilter from "../../components/reports/DateRangeFilter";

const COLORS = ["#14b8a6", "#8b5cf6", "#f59e0b", "#34d399", "#fb7185", "#3b82f6", "#ec4899", "#10b981"];
const AXIS = "#8890a8";
const GRID = "#262f47";
const TT = { background: "#1b2338", border: "1px solid #262f47", borderRadius: 8, color: "#edf0f7", fontSize: 12 };

export default function DrugUtilizationReportPage() {
  const [rows, setRows] = useState([]);
  const [dates, setDates] = useState({ startDate: "", endDate: "" });
  const [error, setError] = useState("");

  const load = useCallback(async () => {
    try {
      const res = await reportApi.getDrugUtilization({
        startDate: dates.startDate || undefined,
        endDate: dates.endDate || undefined,
      });
      setRows(res.data.data || []);
    } catch (err) {
      setError(err.response?.data?.message || "Could not load drug utilization.");
    }
  }, [dates]);

  useEffect(() => { const t = setTimeout(load, 0); return () => clearTimeout(t); }, [load]);

  // Top 10 by qty dispensed for the chart
  const top10 = [...rows]
    .sort((a, b) => Number(b.totalQuantityDispensed) - Number(a.totalQuantityDispensed))
    .slice(0, 10)
    .map((r) => ({ name: r.drugGenericName, qty: Number(r.totalQuantityDispensed), events: Number(r.dispensingEventCount) }));

  return (
    <main className="list-page report-page">
      <BackButton to="/reports" />
      <div className="list-page__header">
        <div>
          <span className="report-page__eyebrow">Dispensing report</span>
          <h1>Drug Utilization</h1>
        </div>
      </div>

      <DateRangeFilter {...dates} onChange={setDates} />
      {error && <p className="form-error">{error}</p>}

      {top10.length > 0 && (
        <>
          <div className="chart-card" style={{ marginBottom: "1.1rem" }}>
            <div className="chart-card__header">
              <div>
                <p className="chart-card__title">Top 10 drugs by quantity dispensed</p>
                <p className="chart-card__subtitle">Total quantity dispensed in period</p>
              </div>
            </div>
            <ResponsiveContainer width="100%" height={280}>
              <BarChart data={top10} layout="vertical" margin={{ top: 4, right: 24, left: 8, bottom: 0 }}>
                <CartesianGrid stroke={GRID} horizontal={false} />
                <XAxis type="number" stroke={AXIS} tick={{ fontSize: 11, fill: AXIS }} axisLine={false} tickLine={false} />
                <YAxis type="category" dataKey="name" stroke={AXIS} tick={{ fontSize: 11, fill: AXIS }} axisLine={false} tickLine={false} width={130} />
                <Tooltip contentStyle={TT} />
                <Bar dataKey="qty" name="Qty dispensed" radius={[0, 6, 6, 0]} maxBarSize={22}>
                  {top10.map((_, i) => <Cell key={i} fill={COLORS[i % COLORS.length]} />)}
                </Bar>
              </BarChart>
            </ResponsiveContainer>
          </div>

          <div className="chart-card" style={{ marginBottom: "1.1rem" }}>
            <div className="chart-card__header">
              <div>
                <p className="chart-card__title">Dispensing events (top 10)</p>
                <p className="chart-card__subtitle">Number of dispense transactions</p>
              </div>
            </div>
            <ResponsiveContainer width="100%" height={280}>
              <BarChart data={top10} layout="vertical" margin={{ top: 4, right: 24, left: 8, bottom: 0 }}>
                <CartesianGrid stroke={GRID} horizontal={false} />
                <XAxis type="number" stroke={AXIS} tick={{ fontSize: 11, fill: AXIS }} axisLine={false} tickLine={false} />
                <YAxis type="category" dataKey="name" stroke={AXIS} tick={{ fontSize: 11, fill: AXIS }} axisLine={false} tickLine={false} width={130} />
                <Tooltip contentStyle={TT} />
                <Bar dataKey="events" name="Dispense events" fill="#8b5cf6" radius={[0, 6, 6, 0]} maxBarSize={22} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </>
      )}

      <table className="data-table">
        <thead>
          <tr>
            <th>Drug</th>
            <th>Dispense events</th>
            <th>Total qty dispensed</th>
          </tr>
        </thead>
        <tbody>
          {rows.map((r) => (
            <tr key={r.drugId}>
              <td>{r.drugGenericName}</td>
              <td>{r.dispensingEventCount}</td>
              <td>{Number(r.totalQuantityDispensed).toLocaleString()}</td>
            </tr>
          ))}
          {!rows.length && <tr><td colSpan="3">No data for this range.</td></tr>}
        </tbody>
      </table>
    </main>
  );
}
