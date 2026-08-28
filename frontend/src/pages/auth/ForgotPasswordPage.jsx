import { useState } from "react";
import { Link } from "react-router-dom";
import { authApi } from "../../api/authApi";
export default function ForgotPasswordPage() {
  const [email, setEmail] = useState("");
  const [submitted, setSubmitted] = useState(false);

  async function submit(event) {
    event.preventDefault();
    try {
      await authApi.forgotPassword(email);
    } catch {
      /* Use the same result for unknown identifiers. */
    } finally {
      setSubmitted(true);
    }
  }

  if (submitted) {
    return (
      <main className="auth-page">
        <div className="auth-form">
          <h1>Check your email</h1>
          <p>If an account matches that email, a reset code was sent.</p>
          <Link to="/reset-password">I have a code</Link>
        </div>
      </main>
    );
  }

  return (
    <main className="auth-page">
      <form className="auth-form" onSubmit={submit}>
        <h1>Forgot password</h1>
        <label>
          Email address
          <input
            type="email"
            value={email}
            onChange={(event) => setEmail(event.target.value)}
            required
            autoComplete="email"
          />
        </label>
        <button type="submit">Send reset code</button>
        <Link to="/login">Back to login</Link>
      </form>
    </main>
  );
}
