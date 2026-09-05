import { Link, useLocation } from "react-router-dom";
import { useAuth } from "../../hooks/useAuth";
import { ROLE_NAV_GROUPS } from "./navConfig";

function buildGroupsForRoles(roles = []) {
  const order = [];
  const groups = new Map();
  roles.forEach((role) => (ROLE_NAV_GROUPS[role] || []).forEach((group) => {
    if (!groups.has(group.label)) { groups.set(group.label, new Map()); order.push(group.label); }
    group.items.forEach((item) => groups.get(group.label).set(item.to, item));
  }));
  return order.map((label) => ({ label, items: [...groups.get(label).values()] }));
}

export default function Sidebar({ isOpen, onNavigate }) {
  const { user } = useAuth();
  const location = useLocation();
  return <aside className={isOpen ? "sidebar sidebar--open" : "sidebar"}>
    <Link to="/" className="sidebar__brand" onClick={onNavigate}><span className="sidebar__brand-mark" aria-hidden="true">P</span><span className="sidebar__brand-text">PIPMS</span></Link>
    <nav className="sidebar__nav" aria-label="Main navigation">
      {buildGroupsForRoles(user?.roles).map((group) => <div className="sidebar__group" key={group.label}><span className="sidebar__group-label">{group.label}</span>
        {group.items.map((item) => { const active = location.pathname === item.to || location.pathname.startsWith(`${item.to}/`); return <Link key={item.to} to={item.to} onClick={onNavigate} className={active ? "sidebar__link sidebar__link--active" : "sidebar__link"} aria-current={active ? "page" : undefined}><span className="sidebar__link-dot" aria-hidden="true" />{item.label}</Link>; })}
      </div>)}
    </nav>
  </aside>;
}
