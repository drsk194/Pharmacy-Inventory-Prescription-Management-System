import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../../hooks/useAuth";
import "./NavBar.css";
import NotificationBell from "../notifications/NotificationBell";

const ROLE_LINKS = {
  ROLE_ADMIN: [{ label: "Dashboard", to: "/dashboard/admin" }, { label: "Patients", to: "/patients" }, { label: "Doctors", to: "/doctors" }, { label: "Drugs", to: "/drugs" }, { label: "Interactions", to: "/drugs/interactions" }, { label: "Suppliers", to: "/suppliers" }, { label: "Inventory", to: "/inventory" }, { label: "CS Register", to: "/controlled-substances/register" }, { label: "Purchase Orders", to: "/purchase-orders" }, { label: "GRN", to: "/grn" }, { label: "Generate Bill", to: "/bills/new" }, { label: "Outstanding Bills", to: "/bills/outstanding" }, { label: "Reports", to: "/reports" }, { label: "Audit Logs", to: "/admin/audit-logs" }, { label: "Users", to: "/admin/users" }, { label: "Config", to: "/admin/config" }, { label: "Shifts", to: "/admin/shifts" }, { label: "Holidays", to: "/admin/holidays" }, { label: "Operating Hours", to: "/admin/operating-hours" }, { label: "System Health", to: "/admin/system-health" }, { label: "Compliance", to: "/admin/compliance" }],
  ROLE_PHARMACIST: [{ label: "Dashboard", to: "/dashboard/pharmacist" }, { label: "Patients", to: "/patients" }, { label: "Doctors", to: "/doctors" }, { label: "Drugs", to: "/drugs" }, { label: "Prescription queue", to: "/prescriptions/queue" }, { label: "Dispensing", to: "/dispensing" }, { label: "Inventory", to: "/inventory" }, { label: "CS Register", to: "/controlled-substances/register" }, { label: "GRN", to: "/grn" }, { label: "Generate Bill", to: "/bills/new" }, { label: "Outstanding Bills", to: "/bills/outstanding" }],
  ROLE_TECHNICIAN: [{ label: "Dashboard", to: "/dashboard/technician" }, { label: "Patients", to: "/patients" }, { label: "Prescription queue", to: "/prescriptions/queue" }, { label: "Inventory", to: "/inventory" }],
  ROLE_PROCUREMENT: [{ label: "Dashboard", to: "/dashboard/procurement" }, { label: "Suppliers", to: "/suppliers" }, { label: "Inventory", to: "/inventory" }, { label: "Purchase Orders", to: "/purchase-orders" }, { label: "GRN", to: "/grn" }],
  ROLE_AUDITOR: [{ label: "Dashboard", to: "/dashboard/auditor" }, { label: "Inventory", to: "/inventory" }, { label: "Reports", to: "/reports" }, { label: "Audit Logs", to: "/admin/audit-logs" }, { label: "Compliance", to: "/admin/compliance" }],
  ROLE_DOCTOR: [{ label: "Dashboard", to: "/dashboard/doctor" }, { label: "My profile", to: "/doctors/me" }, { label: "New prescription", to: "/prescriptions/new" }, { label: "My prescriptions", to: "/prescriptions/my" }],
  ROLE_PATIENT: [{ label: "Dashboard", to: "/dashboard/patient" }, { label: "My profile", to: "/patients/me" }, { label: "My prescriptions", to: "/prescriptions/my" }, { label: "My bills", to: "/bills/my" }],
};

export default function NavBar() {
  const { user, isAuthenticated, logout } = useAuth();
  const navigate = useNavigate();
  const links = user?.roles?.flatMap((role) => ROLE_LINKS[role] || []) || [];
  async function handleLogout() { await logout(); navigate("/login"); }
  return <header className="navbar"><Link to="/" className="navbar__brand">PIPMS</Link><nav className="navbar__links">{links.map((link) => <Link key={link.to} to={link.to}>{link.label}</Link>)}</nav><div className="navbar__actions">{isAuthenticated ? <><NotificationBell /><span className="navbar__user">{user?.name || user?.staffId}</span><button type="button" onClick={handleLogout}>Log out</button></> : <Link to="/login">Log in</Link>}</div></header>;
}
