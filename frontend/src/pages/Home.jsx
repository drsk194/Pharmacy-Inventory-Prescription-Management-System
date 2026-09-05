import { Link, Navigate } from "react-router-dom";
import { useAuth } from "../hooks/useAuth";

const ROLE_HOME = {
  ROLE_ADMIN: "/dashboard/admin",
  ROLE_PHARMACIST: "/dashboard/pharmacist",
  ROLE_TECHNICIAN: "/dashboard/technician",
  ROLE_PROCUREMENT_OFFICER: "/dashboard/procurement",
  ROLE_AUDITOR: "/dashboard/auditor",
  ROLE_DOCTOR: "/dashboard/doctor",
  ROLE_PATIENT: "/dashboard/patient",
};
const ROLE_PRIORITY = Object.keys(ROLE_HOME);

export default function Home() {
  const { user, isAuthenticated } = useAuth();

  if (isAuthenticated) {
    const role = ROLE_PRIORITY.find((r) => user?.roles?.includes(r));
    return <Navigate to={ROLE_HOME[role] || "/login"} replace />;
  }

  return (
    <div className="landing-page">
      <nav className="landing-nav">
        <Link to="/" className="landing-nav__brand">
          <span className="landing-nav__brand-mark" aria-hidden="true">P</span>
          <span className="landing-nav__brand-text"><strong>PIPMS</strong><span>Pharmacy Management</span></span>
        </Link>
        <div className="landing-nav__links">
          <a href="#about">About</a>
        </div>
        <div className="landing-nav__actions">
          <Link to="/register" className="link">Register</Link>
          <Link to="/login" className="button--primary" style={{ textDecoration: "none", padding: ".55rem 1.1rem" }}>Staff Login</Link>
        </div>
      </nav>

      <section className="landing-hero">
        <div className="landing-hero__content">
          <span className="landing-hero__eyebrow">Healthcare &amp; Medical · Pharmacy Operations</span>
          <h1>Welcome to the Pharmacy Management System</h1>
          <p className="lead">Accurate dispensing, optimized inventory, regulatory compliance.</p>
          <div className="landing-hero__actions">
            <Link to="/login" className="button--primary" style={{ textDecoration: "none" }}>Staff Login</Link>
            <Link to="/register" className="button--outline">Patient Portal</Link>
            <Link to="/catalog" className="button--outline">Browse Catalog</Link>
          </div>
          <div className="landing-hero__meta">
            <span>JWT-secured</span>
            <span>Role-based access · 8 roles</span>
            <span>Batch &amp; expiry tracking</span>
            <span>Audit-logged</span>
          </div>
        </div>
      </section>

      <section className="landing-about" id="about">
        <div className="landing-about__image" role="img" aria-label="Modern healthcare facility" />
        <div className="landing-about__copy">
          <span className="landing-about__eyebrow">About PIPMS</span>
          <h2>One clear view of pharmacy care.</h2>
          <p>
            PIPMS connects pharmacy teams, clinicians, and patients with accurate dispensing,
            inventory control, prescription tracking, and compliance tools.
          </p>
        </div>
      </section>

      <footer className="landing-footer">
        <span>© {new Date().getFullYear()} Pharmacy Inventory and Prescription Management System. All rights reserved.</span>
        <span>Spring Boot · Spring Security · React · JPA/MySQL · JWT</span>
      </footer>
    </div>
  );
}
