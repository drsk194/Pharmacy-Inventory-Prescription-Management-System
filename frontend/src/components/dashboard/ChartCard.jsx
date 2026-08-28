export default function ChartCard({ title, subtitle, isEmpty, emptyMessage = "No data yet.", children }) {
  return <div className="chart-card"><div className="chart-card__header"><span className="chart-card__title">{title}</span>{subtitle && <span className="chart-card__subtitle">{subtitle}</span>}</div>{isEmpty ? <p className="chart-card__empty">{emptyMessage}</p> : children}</div>;
}
