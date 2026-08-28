export default function StatCard({ label, value, unit, tone = "teal" }) {
  return <div className={`stat-card stat-card--${tone}`}><span className="stat-card__label">{label}</span><span className="stat-card__value">{value ?? "-"}{unit && <span className="stat-card__unit">{unit}</span>}</span></div>;
}
