import { useCallback, useEffect, useRef, useState } from "react";
import { Link } from "react-router-dom";
import { drugApi } from "../api/drugApi";

const SCHEDULE_LABELS = {
  OTC:         { label: "OTC",         color: "#34d399" },
  SCHEDULE_H:  { label: "Schedule H",  color: "#f59e0b" },
  SCHEDULE_H1: { label: "Schedule H1", color: "#fb923c" },
  SCHEDULE_X:  { label: "Schedule X",  color: "#fb7185" },
};

const DRUG_CLASSES = [
  "All classes",
  "Antibiotics", "Analgesics", "Antihypertensives", "Antidiabetics",
  "Antihistamines", "Antacids", "Vitamins & Supplements", "Vaccines",
  "Cardiovascular", "Respiratory", "Neurological", "Gastrointestinal",
];

export default function DrugCatalogPage() {
  const [drugs, setDrugs]           = useState([]);
  const [search, setSearch]         = useState("");
  const [drugClass, setDrugClass]   = useState("");
  const [schedule, setSchedule]     = useState("");
  const [page, setPage]             = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [loading, setLoading]       = useState(false);
  const [error, setError]           = useState("");
  const searchRef                   = useRef(null);

  const load = useCallback(async () => {
    setLoading(true);
    setError("");
    try {
      const params = {
        page,
        size: 24,
        search: search || undefined,
        drugClass: drugClass || undefined,
        schedule: schedule || undefined,
      };
      const res = await drugApi.getCatalog(params);
      const data = res.data.data;
      setDrugs(data.content || data || []);
      setTotalPages(data.totalPages || 0);
      setTotalElements(data.totalElements || (data.content?.length ?? data.length ?? 0));
    } catch (err) {
      setError(err.response?.data?.message || "Could not load drug catalog.");
    } finally {
      setLoading(false);
    }
  }, [page, search, drugClass, schedule]);

  useEffect(() => {
    const timer = setTimeout(load, search ? 350 : 0);
    return () => clearTimeout(timer);
  }, [load, search]);

  function handleSearch(event) {
    setSearch(event.target.value);
    setPage(0);
  }

  function handleFilter(setter) {
    return (event) => {
      setter(event.target.value);
      setPage(0);
    };
  }

  const scheduleInfo = (s) => SCHEDULE_LABELS[s] || { label: s || "OTC", color: "#34d399" };

  return (
    <div className="catalog-page">
      {/* ── Hero header ─────────────────────────────────────────────────── */}
      <header className="catalog-page__hero">
        <div className="catalog-page__hero-inner">
          <p className="catalog-page__eyebrow">Public Drug Catalog</p>
          <h1 className="catalog-page__title">Find your medication</h1>
          <p className="catalog-page__subtitle">
            Browse our complete formulary. Prescription drugs require a valid doctor's prescription.
          </p>

          <div className="catalog-page__search-wrap">
            <label htmlFor="catalog-search" className="sr-only">Search drugs</label>
            <span className="catalog-page__search-icon" aria-hidden="true">🔍</span>
            <input
              id="catalog-search"
              ref={searchRef}
              type="search"
              className="catalog-page__search"
              placeholder="Search by drug name, generic name, or brand…"
              value={search}
              onChange={handleSearch}
              autoComplete="off"
            />
          </div>

          <div className="catalog-page__filters">
            <label htmlFor="filter-class" className="sr-only">Drug class</label>
            <select
              id="filter-class"
              className="catalog-page__filter-select"
              value={drugClass}
              onChange={handleFilter(setDrugClass)}
            >
              {DRUG_CLASSES.map((c) => (
                <option key={c} value={c === "All classes" ? "" : c}>{c}</option>
              ))}
            </select>

            <label htmlFor="filter-schedule" className="sr-only">Schedule</label>
            <select
              id="filter-schedule"
              className="catalog-page__filter-select"
              value={schedule}
              onChange={handleFilter(setSchedule)}
            >
              <option value="">All schedules</option>
              <option value="OTC">OTC</option>
              <option value="SCHEDULE_H">Schedule H</option>
              <option value="SCHEDULE_H1">Schedule H1</option>
              <option value="SCHEDULE_X">Schedule X</option>
            </select>
          </div>

          {!loading && totalElements > 0 && (
            <p className="catalog-page__result-count">
              {totalElements} drug{totalElements !== 1 ? "s" : ""} found
            </p>
          )}
        </div>
      </header>

      {/* ── Catalog actions bar ─────────────────────────────────────────── */}
      <div className="catalog-page__actions-bar">
        <Link to="/login" className="catalog-page__action-btn">
          Staff login →
        </Link>
        <Link to="/login" className="catalog-page__action-btn catalog-page__action-btn--secondary">
          Patient portal →
        </Link>
      </div>

      {/* ── Drug grid ───────────────────────────────────────────────────── */}
      <main id="main-content" className="catalog-page__content">
        {error && <p className="form-error catalog-page__error" role="alert">{error}</p>}

        {loading && (
          <div className="catalog-page__loading" aria-live="polite" aria-busy="true">
            <div className="catalog-page__spinner" aria-hidden="true" />
            <p>Loading catalog…</p>
          </div>
        )}

        {!loading && drugs.length === 0 && !error && (
          <div className="catalog-page__empty">
            <p className="catalog-page__empty-icon" aria-hidden="true">💊</p>
            <h2>No drugs found</h2>
            <p>Try adjusting your search or filters.</p>
          </div>
        )}

        {!loading && drugs.length > 0 && (
          <ul className="catalog-grid" role="list" aria-label="Drug catalog">
            {drugs.map((drug) => {
              const sched = scheduleInfo(drug.schedule);
              return (
                <li key={drug.id} className="catalog-card">
                  <div className="catalog-card__top">
                    <span
                      className="catalog-card__schedule-badge"
                      style={{ "--badge-color": sched.color }}
                      aria-label={`Schedule: ${sched.label}`}
                    >
                      {sched.label}
                    </span>
                    <span className="catalog-card__class">
                      {drug.drugClass || drug.category || "General"}
                    </span>
                  </div>

                  <h2 className="catalog-card__generic">{drug.genericName || drug.name}</h2>
                  {drug.brandName && drug.brandName !== drug.genericName && (
                    <p className="catalog-card__brand">{drug.brandName}</p>
                  )}

                  {drug.strength && (
                    <p className="catalog-card__strength">{drug.strength}</p>
                  )}
                  {drug.form && (
                    <p className="catalog-card__form">{drug.form}</p>
                  )}

                  {drug.description && (
                    <p className="catalog-card__description">{drug.description}</p>
                  )}

                  <div className="catalog-card__footer">
                    {drug.schedule !== "OTC" && (
                      <span className="catalog-card__rx-notice" aria-label="Requires prescription">
                        Rx — Prescription required
                      </span>
                    )}
                    {drug.schedule === "OTC" && (
                      <span className="catalog-card__otc-notice">Available over the counter</span>
                    )}
                  </div>
                </li>
              );
            })}
          </ul>
        )}

        {/* Pagination */}
        {totalPages > 1 && (
          <nav className="catalog-page__pagination" aria-label="Drug catalog pagination">
            <button
              type="button"
              className="catalog-page__page-btn"
              disabled={page === 0}
              onClick={() => setPage((p) => p - 1)}
              aria-label="Previous page"
            >
              ← Previous
            </button>
            <span className="catalog-page__page-info">
              Page {page + 1} of {totalPages}
            </span>
            <button
              type="button"
              className="catalog-page__page-btn"
              disabled={page >= totalPages - 1}
              onClick={() => setPage((p) => p + 1)}
              aria-label="Next page"
            >
              Next →
            </button>
          </nav>
        )}
      </main>

      {/* ── Footer note ─────────────────────────────────────────────────── */}
      <footer className="catalog-page__footer-note">
        <p>
          This catalog is for informational purposes only. Always consult a licensed pharmacist or
          doctor before starting, stopping, or changing any medication.{" "}
          <Link to="/login">Log in</Link> for pricing and stock availability.
        </p>
      </footer>
    </div>
  );
}
