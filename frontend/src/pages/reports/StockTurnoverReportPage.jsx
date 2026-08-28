import { useCallback, useEffect, useState } from "react";
import BackButton from "../../components/common/BackButton";
import {
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip,
  ResponsiveContainer, Cell, ReferenceLine
} from "recharts";
import { reportApi } from "../../api/reportApi";
import DateRangeFilter from "../../components/reports/DateRangeFilter";

const AXIS = "#8890a8";
const GRID = "#262f47";
const TT = { background: "#1b2338", border: "1px solid #262f47", borderRadius: 8, color: "#edf0f7", fontSize: 12 };

function ratioColor(ratio) {
  if (ratio >= 2) return "#34d399";
  if (ratio >= 0.5) return "#f59e0b";
  return "#fb7185";
}

export default function StockTurnoverReportPage() {
  const [rows, setRows] = useState([]);
  const [dates, setDates] = useState({ startDate: "", endDate: "" });
  const [error, setError] = useState("");

  const load = useCallback(async () => {
    try {
      const res = await reportApi.getStockTurnover({
        startDate: dates.startDate || undefined,
        endDate: dates.endDate || undefined,
      });
      setRows(res.data.data || []);
    } catch (err) {
      setError(err.response?.data?.message || "Could not load stock turnover.");
    }
  }, [dates]);

  useEffect(() => { const t = setTimeout(load, 0); return () => clearTimeout(t); }, [load]);

  const chartData = [...rows]
    .sort((a, b) => Number(b.turnoverRatio) - Number(a.turnoverRatio))
    .slice(0, 12)
    .map((r) => ({
      name: r.drugGenericName,
      ratio: Number(r.turnoverRatio ?? 0),
    }));

  const avgRatio = rows.length
    ? rows.reduce((s, r) => s + Number(r.turnoverRatio ?? 0), 0) / rows.length
    : 0;

  return (
    <main className="list-page report-page">
      <BackButton to="/reports" />
      <div className="list-page__header">
        <div>
          <span className="report-page__eyebrow">Inventory report</span>
          <h1>Stock Turnover</h1>
        </div>
      </div>

      <DateRangeFilter {...dates} onChange={setDates} />
      {error && <p className="form-error">{error}</p>}

      <div className="stat-grid" style={{ margin: "1rem 0 1.5rem" }}>
        <div className="stat-card stat-card--teal">
          <span className="stat-card__label">Drugs analysed</span>
          <span className="stat-card__value">{rows.length}</span>
        </div>
        <div className="stat-card stat-card--flat">
          <span className="stat-card__label">Avg turnover ratio</span>
          <span className="stat-card__value">{avgRatio.toFixed(2)}</span>
        </div>
        <div className="stat-card stat-card--emerald">
          <span className="stat-card__label">High turnover (≥2)</span>
          <span className="stat-card__value">{rows.filter((r) => Number(r.turnoverRatio) >= 2).length}</span>
        </div>
        <div className="stat-card stat-card--amber">
          <span className="stat-card__label">Low turnover (&lt;0.5)</span>
          <span className="stat-card__value">{rows.filter((r) => Number(r.turnoverRatio) < 0.5).length}</span>
        </div>
      </div>

      {chartData.length > 0 && (
        <div className="chart-card" style={{ marginBottom: "1.1rem" }}>
          <div className="chart-card__header">
            <div>
              <p className="chart-card__title">Turnover ratio (top 12)</p>
              <p className="chart-card__subtitle">Green ≥ 2 · Amber 0.5–2 · Red &lt; 0.5</p>
            </div>
          </div>
          <ResponsiveContainer width="100%" height={280}>
            <BarChart data={chartData} margin={{ top: 4, right: 8, left: -18, bottom: 0 }}>
              <CartesianGrid stroke={GRID} vertical={false} />
              <XAxis dataKey="name" stroke={AXIS} tick={{ fontSize: 10, fill: AXIS }} axisLine={false} tickLine={false} interval={0} angle={-30} textAnchor="end" height={60} />
              <YAxis stroke={AXIS} tick={{ fontSize: 11, fill: AXIS }} axisLine={false} tickLine={false} width={40} />
              <ReferenceLine y={2} stroke="#34d399" strokeDasharray="4 4" label={{ value: "Good (2)", fill: "#34d399", fontSize: 10 }} />
              <ReferenceLine y={0.5} stroke="#fb7185" strokeDasharray="4 4" label={{ value: "Low (0.5)", fill: "#fb7185", fontSize: 10 }} />
              <Tooltip contentStyle={TT} formatter={(v) => [v.toFixed(3), "Turnover ratio"]} />
              <Bar dataKey="ratio" name="Turnover ratio" radius={[6, 6, 0, 0]} maxBarSize={40}>
                {chartData.map((d, i) => <Cell key={i} fill={ratioColor(d.ratio)} />)}
              </Bar>
            </BarChart>
          </ResponsiveContainer>
        </div>
      )}

      <table className="data-table">
        <thead>
          <tr><th>Drug</th><th>Qty dispensed</th><th>Current stock</th><th>Turnover ratio</th></tr>
        </thead>
        <tbody>
          {rows.map((r) => (
            <tr key={r.drugId}>
              <td>{r.drugGenericName}</td>
              <td>{Number(r.quantityDispensedInPeriod).toLocaleString()}</td>
              <td>{Number(r.currentStockAsAverageProxy).toLocaleString()}</td>
              <td style={{ fontWeight: 700, color: ratioColor(Number(r.turnoverRatio)) }}>
                {Number(r.turnoverRatio ?? 0).toFixed(3)}
              </td>
            </tr>
          ))}
          {!rows.length && <tr><td colSpan="4">No data for this range.</td></tr>}
        </tbody>
      </table>
    </main>
  );
}
