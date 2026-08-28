import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { authApi } from "../../api/authApi";
export default function ResetPasswordPage() {
  const [form, setForm] = useState({ email: "", otpCode: "", newPassword: "" });
  const [error, setError] = useState("");
  const navigate = useNavigate();

  function change(event) {
    setForm({ ...form, [event.target.name]: event.target.value });
  }

  async function submit(event) {
    event.preventDefault();
    try {
      await authApi.resetPassword(form);
      navigate("/login", { state: { message: "Password reset successfully. Please log in." } });
    } catch (err) {
      setError(err.response?.data?.message || "Reset failed. Check your code and try again.");
    }
  }

  return (
    <main className="auth-page">
      <form className="auth-form" onSubmit={submit}>
        <h1>Reset password</h1>
        <label>
          Email address
          <input
            name="email"
            type="email"
            value={form.email}
            onChange={change}
            required
            autoComplete="email"
          />
        </label>
        <label>
          Reset code (6 digits)
          <input
            name="otpCode"
            value={form.otpCode}
            onChange={change}
            pattern="\d{6}"
            title="6-digit reset code"
            required
          />
        </label>
        <label>
          New password
          <input
            name="newPassword"
            type="password"
            value={form.newPassword}
            onChange={change}
            required
            autoComplete="new-password"
          />
        </label>
        {error && <p className="form-error">{error}</p>}
        <button type="submit">Reset password</button>
        <Link to="/login">Back to login</Link>
      </form>
    </main>
  );
}
