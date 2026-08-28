import { useId } from "react";
import { AreaChart, Area, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from "recharts";
import ChartCard from "./ChartCard";
const AXIS_COLOR = "#8890a8"; const GRID_COLOR = "#262f47";
export default function TrendAreaChart({ title, subtitle, data, dataKey = "value", xKey = "label", color = "#14b8a6" }) {
  const gradientId = `trend-fill-${useId()}`; const isEmpty = !data || data.length === 0;
  return <ChartCard title={title} subtitle={subtitle} isEmpty={isEmpty} emptyMessage="No activity in this range yet."><ResponsiveContainer width="100%" height={220}><AreaChart data={data} margin={{ top: 4, right: 8, left: -18, bottom: 0 }}><defs><linearGradient id={gradientId} x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stopColor={color} stopOpacity={0.45} /><stop offset="100%" stopColor={color} stopOpacity={0} /></linearGradient></defs><CartesianGrid stroke={GRID_COLOR} vertical={false} /><XAxis dataKey={xKey} stroke={AXIS_COLOR} tick={{ fontSize: 11, fill: AXIS_COLOR }} axisLine={false} tickLine={false} /><YAxis stroke={AXIS_COLOR} tick={{ fontSize: 11, fill: AXIS_COLOR }} axisLine={false} tickLine={false} width={36} /><Tooltip contentStyle={{ background: "#1b2338", border: "1px solid #262f47", borderRadius: 8, color: "#edf0f7", fontSize: 12 }} /><Area type="monotone" dataKey={dataKey} stroke={color} strokeWidth={2.5} fill={`url(#${gradientId})`} /></AreaChart></ResponsiveContainer></ChartCard>;
}
