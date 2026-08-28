import { Link } from "react-router-dom";

const GROUPS = [
  {
    label: "Inventory",
    color: "teal",
    icon: (
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
        <rect x="2" y="7" width="20" height="14" rx="2" />
        <path d="M16 7V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v2" />
        <line x1="12" y1="12" x2="12" y2="16" />
        <line x1="10" y1="14" x2="14" y2="14" />
      </svg>
    ),
    reports: [
      { label: "Inventory summary", path: "inventory-summary", desc: "Stock value, batch counts & status breakdown" },
      { label: "Dead stock", path: "dead-stock", desc: "Drugs with zero movement in the last 30 days" },
      { label: "Slow moving", path: "slow-moving", desc: "Low-turnover drugs over a date range" },
      { label: "Stock turnover", path: "stock-turnover", desc: "Turnover ratios per drug — colour-coded health" },
    ],
  },
  {
    label: "Dispensing",
    color: "violet",
    icon: (
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
        <path d="M9 3H5a2 2 0 0 0-2 2v4m6-6h10a2 2 0 0 1 2 2v4M9 3v18m0 0h10a2 2 0 0 0 2-2v-4M9 21H5a2 2 0 0 1-2-2v-4m0 0h18" />
      </svg>
    ),
    reports: [
      { label: "Prescription volume", path: "prescription-volume", desc: "Total count split by status and source" },
      { label: "Dispensing turnaround", path: "dispensing-turnaround", desc: "Average minutes from receipt to dispense" },
      { label: "Technician activity", path: "technician-activity", desc: "Prescriptions processed per technician" },
      { label: "Pharmacist activity", path: "pharmacist-activity", desc: "Prescriptions verified per pharmacist" },
      { label: "Drug utilization", path: "drug-utilization", desc: "Dispense events and quantities by drug" },
    ],
  },
  {
    label: "Procurement",
    color: "amber",
    icon: (
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
        <path d="M6 2 3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z" />
        <line x1="3" y1="6" x2="21" y2="6" />
        <path d="M16 10a4 4 0 0 1-8 0" />
      </svg>
    ),
    reports: [
      { label: "Procurement spending", path: "procurement-spending", desc: "Supplier-wise spending with share breakdown" },
    ],
  },
  {
    label: "Financial",
    color: "emerald",
    icon: (
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
        <line x1="12" y1="1" x2="12" y2="23" />
        <path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6" />
      </svg>
    ),
    reports: [
      { label: "Revenue", path: "revenue", desc: "Daily revenue trend with total and averages" },
      { label: "Outstanding bills", path: "outstanding", desc: "Unpaid and partial bills with risk gauge" },
    ],
  },
];

const GRADIENT = {
  teal: "var(--gradient-teal)",
  violet: "var(--gradient-violet)",
  amber: "var(--gradient-amber)",
  emerald: "var(--gradient-emerald)",
};

const ACCENT = {
  teal: "#14b8a6",
  violet: "#8b5cf6",
  amber: "#f59e0b",
  emerald: "#10b981",
};

export default function ReportsHubPage() {
  const total = GROUPS.reduce((s, g) => s + g.reports.length, 0);

  return (
    <main className="dashboard-page reports-hub">
      {/* Header */}
      <div className="reports-hub__header">
        <div>
          <span className="report-page__eyebrow">Analytics &amp; Insights</span>
          <h1 style={{ margin: "0.4rem 0 0.3rem" }}>Reports</h1>
          <p style={{ color: "var(--color-text-muted)", margin: 0 }}>
            {total} reports across {GROUPS.length} categories
          </p>
        </div>
      </div>

      {/* Groups */}
      {GROUPS.map((group) => (
        <section key={group.label} className="reports-hub__section">
          {/* Section heading */}
          <div className="reports-hub__section-header">
            <span
              className="reports-hub__section-icon"
              style={{ background: GRADIENT[group.color] }}
            >
              {group.icon}
            </span>
            <h2 className="reports-hub__section-title">{group.label}</h2>
            <span className="reports-hub__section-count">
              {group.reports.length} report{group.reports.length !== 1 ? "s" : ""}
            </span>
          </div>

          {/* Cards grid */}
          <div className="reports-hub__grid">
            {group.reports.map((report) => (
              <Link
                key={report.path}
                to={`/reports/${report.path}`}
                className="reports-hub__card"
                style={{ "--card-accent": ACCENT[group.color] }}
              >
                <div className="reports-hub__card-body">
                  <p className="reports-hub__card-title">{report.label}</p>
                  <p className="reports-hub__card-desc">{report.desc}</p>
                </div>
                <span className="reports-hub__card-arrow" aria-hidden="true">→</span>
              </Link>
            ))}
          </div>
        </section>
      ))}
    </main>
  );
}
