/* eslint-disable react-hooks/set-state-in-effect -- network data is loaded asynchronously after mount. */
import { useCallback, useEffect, useState } from "react";
import BackButton from "../../components/common/BackButton";
import { RadialBarChart, RadialBar, ResponsiveContainer, PolarAngleAxis } from "recharts";
import { reportApi } from "../../api/reportApi";
import StatCard from "../../components/dashboard/StatCard";

export default function OutstandingReportPage() {
  const [data, setData] = useState(null);
  const [error, setError] = useState("");

  const load = useCallback(async () => {
    try {
      const res = await reportApi.getOutstanding();
      setData(res.data.data);
    } catch (err) {
      setError(err.response?.data?.message || "Could not load outstanding data.");
    }
  }, []);

  useEffect(() => { load(); }, [load]);

  const total = Number(data?.totalOutstanding ?? 0);
  const count = Number(data?.unpaidOrPartialBillCount ?? 0);

  // Gauge: scale the fill based on arbitrary "concerning" threshold of ₹50k
  const gaugeMax = Math.max(total, 50000);
  const gaugeData = [{ name: "Outstanding", value: total, fill: total > 50000 ? "#fb7185" : total > 10000 ? "#f59e0b" : "#34d399" }];

  return (
    <main className="list-page report-page">
      <BackButton to="/reports" />
      <div className="list-page__header">
        <div>
          <span className="report-page__eyebrow">Financial report</span>
          <h1>Outstanding Bills</h1>
        </div>
      </div>

      {error && <p className="form-error">{error}</p>}

      {data && (
        <>
          <div className="stat-grid" style={{ margin: "1rem 0 1.5rem" }}>
            <StatCard
              tone={total > 50000 ? "danger" : total > 10000 ? "amber" : "emerald"}
              label="Total outstanding"
              value={`₹${total.toLocaleString("en-IN", { minimumFractionDigits: 2 })}`}
            />
            <StatCard tone="violet" label="Unpaid / partial bills" value={count} />
            <StatCard
              label="Avg per bill"
              value={count > 0 ? `₹${(total / count).toLocaleString("en-IN", { minimumFractionDigits: 2, maximumFractionDigits: 2 })}` : "—"}
            />
          </div>

          {/* Gauge */}
          <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: "1.1rem", marginBottom: "1.1rem" }}>
            <div className="chart-card">
              <div className="chart-card__header">
                <p className="chart-card__title">Outstanding amount gauge</p>
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
                  <PolarAngleAxis type="number" domain={[0, gaugeMax]} angleAxisId={0} tick={false} />
                  <RadialBar
                    background={{ fill: "#1b2338" }}
                    dataKey="value"
                    angleAxisId={0}
                    cornerRadius={8}
                  />
                </RadialBarChart>
              </ResponsiveContainer>
              <p style={{ textAlign: "center", marginTop: "-1rem", fontSize: "1.8rem", fontWeight: 700, color: gaugeData[0].fill }}>
                ₹{total.toLocaleString("en-IN", { minimumFractionDigits: 2 })}
              </p>
            </div>

            <div className="chart-card">
              <p className="chart-card__title" style={{ marginBottom: "0.75rem" }}>Collection status</p>
              <table className="data-table">
                <thead><tr><th>Metric</th><th>Value</th></tr></thead>
                <tbody>
                  <tr><td>Total outstanding</td><td>₹{total.toLocaleString("en-IN", { minimumFractionDigits: 2 })}</td></tr>
                  <tr><td>Unpaid / partial bills</td><td>{count}</td></tr>
                  <tr>
                    <td>Average per bill</td>
                    <td>{count > 0 ? `₹${(total / count).toLocaleString("en-IN", { minimumFractionDigits: 2, maximumFractionDigits: 2 })}` : "—"}</td>
                  </tr>
                  <tr>
                    <td>Risk level</td>
                    <td style={{ fontWeight: 700, color: total > 50000 ? "#fb7185" : total > 10000 ? "#f59e0b" : "#34d399" }}>
                      {total > 50000 ? "High" : total > 10000 ? "Medium" : "Low"}
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </>
      )}
    </main>
  );
}
