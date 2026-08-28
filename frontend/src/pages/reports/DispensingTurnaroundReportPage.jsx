import { useCallback, useEffect, useState } from "react";
import BackButton from "../../components/common/BackButton";
import { RadialBarChart, RadialBar, ResponsiveContainer, Tooltip, PolarAngleAxis } from "recharts";
import { reportApi } from "../../api/reportApi";
import DateRangeFilter from "../../components/reports/DateRangeFilter";
import StatCard from "../../components/dashboard/StatCard";

const TT = { background: "#1b2338", border: "1px solid #262f47", borderRadius: 8, color: "#edf0f7", fontSize: 12 };

function gaugeColor(minutes) {
  if (minutes === null || minutes === undefined) return "#8890a8";
  if (minutes <= 15) return "#34d399";
  if (minutes <= 30) return "#f59e0b";
  return "#fb7185";
}

export default function DispensingTurnaroundReportPage() {
  const [data, setData] = useState(null);
  const [dates, setDates] = useState({ startDate: "", endDate: "" });
  const [error, setError] = useState("");

  const load = useCallback(async () => {
    try {
      const res = await reportApi.getDispensingTurnaround({
        startDate: dates.startDate || undefined,
        endDate: dates.endDate || undefined,
      });
      setData(res.data.data);
    } catch (err) {
      setError(err.response?.data?.message || "Could not load turnaround data.");
    }
  }, [dates]);

  useEffect(() => { const t = setTimeout(load, 0); return () => clearTimeout(t); }, [load]);

  const avg = data?.averageTurnaroundMinutes ?? null;
  const color = gaugeColor(avg);
  // Clamp to 60 min for the gauge (anything ≥ 60 fills the bar)
  const gaugeValue = avg !== null ? Math.min(avg, 60) : 0;
  const gaugeData = [{ name: "Turnaround", value: gaugeValue, fill: color }];

  return (
    <main className="list-page report-page">
      <BackButton to="/reports" />
      <div className="list-page__header">
        <div>
          <span className="report-page__eyebrow">Dispensing report</span>
          <h1>Dispensing Turnaround</h1>
        </div>
      </div>

      <DateRangeFilter {...dates} onChange={setDates} />
      {error && <p className="form-error">{error}</p>}

      {data && (
        <>
          <div className="stat-grid" style={{ margin: "1rem 0 1.5rem" }}>
            <StatCard
              label="Avg turnaround"
              value={avg !== null ? `${avg.toFixed(1)} min` : "No data"}
              tone={avg === null ? "neutral" : avg <= 15 ? "emerald" : avg <= 30 ? "amber" : "danger"}
            />
            <StatCard label="Dispensing records" value={data.totalDispensingRecordsInPeriod} />
          </div>

          {/* Radial gauge */}
          <div className="chart-card" style={{ maxWidth: 420, marginBottom: "1.1rem" }}>
            <div className="chart-card__header">
              <div>
                <p className="chart-card__title">Average turnaround gauge</p>
                <p className="chart-card__subtitle">
                  {avg !== null
                    ? avg <= 15 ? "✓ Excellent (≤ 15 min)" : avg <= 30 ? "⚠ Acceptable (15–30 min)" : "✗ Slow (> 30 min)"
                    : "No dispense records in this range"}
                </p>
              </div>
            </div>
            <ResponsiveContainer width="100%" height={220}>
              <RadialBarChart
                innerRadius="55%"
                outerRadius="85%"
                data={gaugeData}
                startAngle={180}
                endAngle={0}
                barSize={18}
              >
                <PolarAngleAxis type="number" domain={[0, 60]} angleAxisId={0} tick={false} />
                <RadialBar
                  background={{ fill: "#1b2338" }}
                  dataKey="value"
                  angleAxisId={0}
                  cornerRadius={8}
                />
                <Tooltip contentStyle={TT} formatter={(v) => [`${avg?.toFixed(1) ?? "—"} min`, "Avg turnaround"]} />
              </RadialBarChart>
            </ResponsiveContainer>
            <p style={{ textAlign: "center", marginTop: "-1rem", fontSize: "2rem", fontWeight: 700, color }}>
              {avg !== null ? `${avg.toFixed(1)} min` : "—"}
            </p>
            <p style={{ textAlign: "center", color: "#8890a8", fontSize: "0.78rem", marginBottom: "0.5rem" }}>
              Gauge scaled to 60 min max
            </p>
          </div>

          {/* Benchmarks */}
          <div className="chart-card">
            <p className="chart-card__title" style={{ marginBottom: "0.75rem" }}>Performance benchmarks</p>
            <table className="data-table">
              <thead><tr><th>Threshold</th><th>Target</th><th>Status</th></tr></thead>
              <tbody>
                {[
                  { label: "Excellent", target: "≤ 15 min", ok: avg !== null && avg <= 15 },
                  { label: "Acceptable", target: "≤ 30 min", ok: avg !== null && avg <= 30 },
                  { label: "Needs improvement", target: "> 30 min", ok: avg !== null && avg > 30 },
                ].map((row) => (
                  <tr key={row.label}>
                    <td>{row.label}</td>
                    <td>{row.target}</td>
                    <td style={{ color: row.ok ? "#34d399" : "#8890a8", fontWeight: row.ok ? 700 : 400 }}>
                      {avg === null ? "No data" : row.ok ? "✓ Current" : "—"}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </>
      )}
    </main>
  );
}
