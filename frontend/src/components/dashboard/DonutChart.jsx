import { PieChart, Pie, Cell, ResponsiveContainer } from "recharts";
import ChartCard from "./ChartCard";
const DEFAULT_COLORS = ["#14b8a6", "#8b5cf6", "#f59e0b", "#34d399", "#fb7185", "#3b82f6"];
export default function DonutChart({ title, subtitle, data, colors = DEFAULT_COLORS }) {
  const isEmpty = !data || data.length === 0; const total = isEmpty ? 0 : data.reduce((sum, entry) => sum + entry.value, 0);
  return <ChartCard title={title} subtitle={subtitle} isEmpty={isEmpty}><div className="donut-card__body"><ResponsiveContainer width={140} height={140}><PieChart><Pie data={data} dataKey="value" nameKey="name" innerRadius={44} outerRadius={62} paddingAngle={3} strokeWidth={0}>{data?.map((entry, index) => <Cell key={entry.name} fill={colors[index % colors.length]} />)}</Pie></PieChart></ResponsiveContainer><ul className="donut-card__legend">{data?.map((entry, index) => <li key={entry.name} className="donut-card__legend-item"><span className="donut-card__legend-dot" style={{ background: colors[index % colors.length] }} />{entry.name} - {entry.value}{total > 0 ? ` (${Math.round((entry.value / total) * 100)}%)` : ""}</li>)}</ul></div></ChartCard>;
}
