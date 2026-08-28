import { Link, useLocation, useNavigate } from "react-router-dom";
import { useAuth } from "../../hooks/useAuth";
import NotificationBell from "../notifications/NotificationBell";
import ThemeToggle from "../common/ThemeToggle";

function prettify(segment) { return segment.replace(/-/g, " ").replace(/\b\w/g, (c) => c.toUpperCase()); }
function deriveTitle(pathname) { if (pathname === "/") return "Home"; const segments = pathname.split("/").filter(Boolean); if (segments[0] === "dashboard") return "Dashboard"; const last = segments.at(-1); return prettify(/^\d+$/.test(last) || ["me", "new"].includes(last) ? segments.at(-2) || last : last); }

export default function TopBar({ onMenuToggle }) {
  const { user, isAuthenticated, logout } = useAuth(); const navigate = useNavigate(); const location = useLocation();
  async function handleLogout() { await logout(); navigate("/login"); }
  return <header className="topbar"><div className="topbar__heading"><button type="button" className="topbar__menu-toggle" onClick={onMenuToggle} aria-label="Toggle navigation">☰</button><span className="topbar__title">{deriveTitle(location.pathname)}</span></div>
    {isAuthenticated ? <div className="topbar__actions"><NotificationBell /><ThemeToggle /><span className="topbar__user">{user?.fullName || user?.name || user?.staffId || user?.email}</span><button type="button" className="topbar__logout" onClick={handleLogout}>Log out</button></div> : <div className="topbar__actions"><ThemeToggle /><Link to="/login">Log in</Link></div>}
  </header>;
}
