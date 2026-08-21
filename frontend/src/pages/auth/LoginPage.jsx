import { useState } from "react";
import { useNavigate, useLocation, Link } from "react-router-dom";
import { useAuth } from "../../hooks/useAuth";

const ROLE_HOME = { ROLE_ADMIN: "/dashboard/admin", ROLE_PHARMACIST: "/dashboard/pharmacist", ROLE_TECHNICIAN: "/dashboard/technician", ROLE_PROCUREMENT: "/dashboard/procurement", ROLE_AUDITOR: "/dashboard/auditor", ROLE_DOCTOR: "/dashboard/doctor", ROLE_PATIENT: "/dashboard/patient" };
const ROLE_PRIORITY = ["ROLE_ADMIN", "ROLE_PHARMACIST", "ROLE_TECHNICIAN", "ROLE_PROCUREMENT", "ROLE_AUDITOR", "ROLE_DOCTOR", "ROLE_PATIENT"];
function pickHomeRoute(roles = []) { return ROLE_HOME[ROLE_PRIORITY.find((role) => roles.includes(role))] || "/"; }

export default function LoginPage() {
  const [identifier, setIdentifier] = useState(""); const [password, setPassword] = useState(""); const [error, setError] = useState(""); const [isSubmitting, setIsSubmitting] = useState(false);
  const { login } = useAuth(); const navigate = useNavigate(); const location = useLocation();
  async function submit(event) { event.preventDefault(); setError(""); setIsSubmitting(true); try { const user = await login(identifier, password); navigate(location.state?.from?.pathname || pickHomeRoute(user.roles), { replace: true }); } catch (err) { setError(err.response?.data?.message || "Login failed. Check your credentials and try again."); } finally { setIsSubmitting(false); } }
  return <main className="auth-page"><form className="auth-form" onSubmit={submit}><h1>Log in</h1><label htmlFor="identifier">Email or Staff ID</label><input id="identifier" value={identifier} onChange={(event) => setIdentifier(event.target.value)} required autoComplete="username" /><label htmlFor="password">Password</label><input id="password" type="password" value={password} onChange={(event) => setPassword(event.target.value)} required autoComplete="current-password" />{error && <p className="form-error" role="alert">{error}</p>}<button type="submit" disabled={isSubmitting}>{isSubmitting ? "Logging in..." : "Log in"}</button><div className="auth-form__links"><Link to="/forgot-password">Forgot password?</Link><Link to="/register">New patient? Register</Link></div></form></main>;
}
