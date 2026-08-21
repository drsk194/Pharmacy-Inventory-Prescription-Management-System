import { useAuth } from "../../hooks/useAuth";
export default function DashboardStub({ roleLabel }) { const { user } = useAuth(); return <main className="dashboard-stub"><h1>{roleLabel} dashboard</h1><p>Welcome, {user?.name || user?.staffId}. Widgets for this role arrive in a later sprint.</p></main>; }
