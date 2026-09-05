import { Link, useLocation, useNavigate } from "react-router-dom";
import { useAuth } from "../../hooks/useAuth";
import NotificationBell from "../notifications/NotificationBell";
import ThemeToggle from "../common/ThemeToggle";

function prettify(segment) { return segment.replace(/-/g, " ").replace(/\b\w/g, (c) => c.toUpperCase()); }
function deriveTitle(pathname) { if (pathname === "/") return "Home"; const segments = pathname.split("/").filter(Boolean); if (segments[0] === "dashboard") return "Dashboard"; const last = segments.at(-1); return prettify(/^\d+$/.test(last) || ["me", "new"].includes(last) ? segments.at(-2) || last : last); }

const ROLE_LABELS = {
  ROLE_ADMIN: "Admin",
  ROLE_PHARMACIST: "Pharmacist",
  ROLE_TECHNICIAN: "Technician",
  ROLE_PROCUREMENT_OFFICER: "Procurement",
  ROLE_AUDITOR: "Auditor",
  ROLE_DOCTOR: "Doctor",
  ROLE_PATIENT: "Patient",
};

function primaryRoleLabel(roles = []) {
  const match = Object.keys(ROLE_LABELS).find((role) => roles.includes(role));
  return match ? ROLE_LABELS[match] : null;
}

function initials(name) {
  if (!name) return "?";
  const parts = name.trim().split(/\s+/);
  return ((parts[0]?.[0] || "") + (parts[1]?.[0] || "")).toUpperCase() || name[0].toUpperCase();
}

export default function TopBar({ onMenuToggle }) {
  const { user, isAuthenticated, logout } = useAuth(); const navigate = useNavigate(); const location = useLocation();
  async function handleLogout() { await logout(); navigate("/login"); }
  const displayName = user?.fullName || user?.name || user?.staffId || user?.email;
  return <header className="topbar">
    <div className="topbar__heading">
      <button type="button" className="topbar__menu-toggle" onClick={onMenuToggle} aria-label="Toggle navigation">☰</button>
      <span className="topbar__title">{deriveTitle(location.pathname)}</span>
    </div>
    {isAuthenticated && (
      <label className="topbar__search" aria-label="Search">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><circle cx="11" cy="11" r="7" /><path d="m21 21-4.3-4.3" /></svg>
        <input type="search" placeholder="Search drugs, prescriptions, patients, POs…" disabled title="Search coming soon" />
      </label>
    )}
    {isAuthenticated ? (
      <div className="topbar__actions">
        <NotificationBell />
        <ThemeToggle />
        <div className="topbar__user">
          <span>{displayName}{primaryRoleLabel(user?.roles) && <span className="topbar__role" style={{ marginLeft: ".4rem" }}>{primaryRoleLabel(user?.roles)}</span>}</span>
        </div>
        <span className="topbar__avatar" aria-hidden="true">{initials(displayName)}</span>
        <button type="button" className="topbar__logout" onClick={handleLogout} aria-label="Log out" title="Log out">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4" /><path d="M16 17l5-5-5-5" /><path d="M21 12H9" /></svg>
        </button>
      </div>
    ) : <div className="topbar__actions"><ThemeToggle /><Link to="/login">Log in</Link></div>}
  </header>;
}
