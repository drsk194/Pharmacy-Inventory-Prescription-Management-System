import { useState } from "react";
import { useNavigate, useLocation, Link } from "react-router-dom";
import { authApi } from "../../api/authApi";
import { useAuth } from "../../hooks/useAuth";

const ROLE_HOME = {
  ROLE_ADMIN: "/dashboard/admin",
  ROLE_PHARMACIST: "/dashboard/pharmacist",
  ROLE_TECHNICIAN: "/dashboard/technician",
  ROLE_PROCUREMENT_OFFICER: "/dashboard/procurement",
  ROLE_AUDITOR: "/dashboard/auditor",
  ROLE_DOCTOR: "/dashboard/doctor",
  ROLE_PATIENT: "/dashboard/patient",
};
const ROLE_PRIORITY = [
  "ROLE_ADMIN",
  "ROLE_PHARMACIST",
  "ROLE_TECHNICIAN",
  "ROLE_PROCUREMENT_OFFICER",
  "ROLE_AUDITOR",
  "ROLE_DOCTOR",
  "ROLE_PATIENT",
];

function pickHomeRoute(roles = []) {
  return ROLE_HOME[ROLE_PRIORITY.find((role) => roles.includes(role))] || "/";
}

export default function LoginPage() {
  const [mode, setMode] = useState("login");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [registerForm, setRegisterForm] = useState({
    fullName: "",
    email: "",
    phoneNumber: "",
    password: "",
    dateOfBirth: "",
    gender: "",
  });
  const [error, setError] = useState("");
  const [successMessage, setSuccessMessage] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  async function submit(event) {
    event.preventDefault();
    setError("");
    setSuccessMessage("");
    setIsSubmitting(true);
    try {
      const user = await login(email, password);
      navigate(location.state?.from?.pathname || pickHomeRoute(user.roles), { replace: true });
    } catch (err) {
      setError(err.response?.data?.message || "Login failed. Check your credentials and try again.");
    } finally {
      setIsSubmitting(false);
    }
  }

  async function submitRegister(event) {
    event.preventDefault();
    setError("");
    setSuccessMessage("");
    setIsSubmitting(true);
    try {
      await authApi.register(registerForm);
      setSuccessMessage(
        "Registration submitted. Your account is pending admin approval — you will be notified once access is granted."
      );
      setRegisterForm({ fullName: "", email: "", phoneNumber: "", password: "", dateOfBirth: "", gender: "" });
    } catch (err) {
      setError(err.response?.data?.message || "Registration failed. Please try again.");
    } finally {
      setIsSubmitting(false);
    }
  }

  function handleRegisterChange(event) {
    const { name, value } = event.target;
    setRegisterForm((current) => ({ ...current, [name]: value }));
  }

  return (
    <main className="auth-page auth-page--centered">
      <div className="auth-page__shell">
        <h1 className="auth-page__title">PIPMS</h1>
        <p className="auth-page__subtitle">Pharmacy Inventory and Prescription Management System</p>

        <div className="auth-card">
          <div className="auth-mode-toggle" role="tablist" aria-label="Authentication mode">
            <button
              type="button"
              role="tab"
              className={mode === "login" ? "auth-mode-button auth-mode-button--active" : "auth-mode-button"}
              aria-selected={mode === "login"}
              onClick={() => { setMode("login"); setError(""); setSuccessMessage(""); }}
            >
              Login
            </button>
            <button
              type="button"
              role="tab"
              className={mode === "register" ? "auth-mode-button auth-mode-button--active" : "auth-mode-button"}
              aria-selected={mode === "register"}
              onClick={() => { setMode("register"); setError(""); setSuccessMessage(""); }}
            >
              Register
            </button>
          </div>

          {mode === "login" ? (
            <form className="auth-form" onSubmit={submit} noValidate>
              <label htmlFor="login-email">Email address</label>
              <input
                id="login-email"
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
                autoComplete="email"
                placeholder="you@example.com"
              />

              <label htmlFor="login-password">Password</label>
              <input
                id="login-password"
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
                autoComplete="current-password"
                placeholder="Enter your password"
              />

              <div className="auth-form__meta">
                <label className="auth-remember" htmlFor="remember-me">
                  <input id="remember-me" type="checkbox" />
                  <span>Remember me</span>
                </label>
                <Link to="/forgot-password">Forgot password?</Link>
              </div>

              {error && <p className="form-error" role="alert">{error}</p>}
              {successMessage && <p className="form-success" role="status">{successMessage}</p>}

              <button className="auth-form__submit" aria-label="Sign in" type="submit" disabled={isSubmitting}>
                {isSubmitting ? "Signing in…" : "Sign in"}
              </button>

              <p className="auth-form__switch">
                New patient?{" "}
                <button type="button" className="auth-link" onClick={() => { setMode("register"); setError(""); setSuccessMessage(""); }}>
                  Create account
                </button>
              </p>
            </form>
          ) : (
            <form className="auth-form auth-form--register" onSubmit={submitRegister} noValidate>
              <div className="auth-staff-notice" role="note">
                <span className="auth-staff-notice__icon" aria-hidden="true">ℹ</span>
                <span>
                  <strong>Staff accounts</strong> are created by the system administrator only. This form is for{" "}
                  <strong>patient self-registration</strong>. After submitting, your account will be reviewed and
                  activated by an admin before you can log in.
                </span>
              </div>

              <label htmlFor="reg-fullName">Full name</label>
              <input
                id="reg-fullName"
                name="fullName"
                type="text"
                value={registerForm.fullName}
                onChange={handleRegisterChange}
                required
                placeholder="Your full name"
              />

              <label htmlFor="reg-email">Email address</label>
              <input
                id="reg-email"
                name="email"
                type="email"
                value={registerForm.email}
                onChange={handleRegisterChange}
                required
                placeholder="you@example.com"
              />

              <label htmlFor="reg-phone">Phone number</label>
              <input
                id="reg-phone"
                name="phoneNumber"
                type="tel"
                value={registerForm.phoneNumber}
                onChange={handleRegisterChange}
                pattern="\d{10}"
                title="Phone number must be exactly 10 digits"
                required
                placeholder="10-digit number"
              />

              <label htmlFor="reg-dob">Date of birth</label>
              <input
                id="reg-dob"
                name="dateOfBirth"
                type="date"
                value={registerForm.dateOfBirth}
                onChange={handleRegisterChange}
                required
              />

              <label htmlFor="reg-gender">Gender</label>
              <select id="reg-gender" name="gender" value={registerForm.gender} onChange={handleRegisterChange} required>
                <option value="">Select…</option>
                <option value="MALE">Male</option>
                <option value="FEMALE">Female</option>
                <option value="OTHER">Other</option>
              </select>

              <label htmlFor="reg-password">Password</label>
              <input
                id="reg-password"
                name="password"
                type="password"
                value={registerForm.password}
                onChange={handleRegisterChange}
                required
                placeholder="Min 8 chars, upper, lower, digit &amp; symbol"
              />

              {error && <p className="form-error" role="alert">{error}</p>}
              {successMessage && <p className="form-success" role="status">{successMessage}</p>}

              {!successMessage && (
                <button className="auth-form__submit" aria-label="Create account" type="submit" disabled={isSubmitting}>
                  {isSubmitting ? "Submitting…" : "Submit registration"}
                </button>
              )}

              <p className="auth-form__switch">
                Already have an account?{" "}
                <button type="button" className="auth-link" onClick={() => { setMode("login"); setError(""); setSuccessMessage(""); }}>
                  Log in
                </button>
              </p>
            </form>
          )}
        </div>
      </div>
    </main>
  );
}
