import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { authApi } from "../../api/authApi";
export default function RegisterPage() {
  const [form, setForm] = useState({ fullName: "", email: "", phoneNumber: "", password: "", dateOfBirth: "", gender: "" });
  const [error, setError] = useState("");
  const navigate = useNavigate();

  function change(event) {
    setForm({ ...form, [event.target.name]: event.target.value });
  }

  async function submit(event) {
    event.preventDefault();
    try {
      await authApi.register(form);
      navigate("/login", { state: { message: "Registration successful. Please log in." } });
    } catch (err) {
      setError(err.response?.data?.message || "Registration failed. Please try again.");
    }
  }

  return (
    <main className="auth-page">
      <form className="auth-form" onSubmit={submit}>
        <h1>Patient registration</h1>
        {[
          ["fullName", "Full name", "text"],
          ["email", "Email", "email"],
          ["phoneNumber", "Phone (10 digits)", "tel"],
          ["dateOfBirth", "Date of birth", "date"],
          ["password", "Password", "password"],
        ].map(([name, label, type]) => (
          <label key={name}>
            {label}
            <input
              name={name}
              type={type}
              value={form[name]}
              onChange={change}
              required
              {...(name === "phoneNumber" ? { pattern: "\\d{10}", title: "Phone number must be exactly 10 digits" } : {})}
            />
          </label>
        ))}
        <label>
          Gender
          <select name="gender" value={form.gender} onChange={change} required>
            <option value="">Select...</option>
            <option>MALE</option>
            <option>FEMALE</option>
            <option>OTHER</option>
          </select>
        </label>
        {error && <p className="form-error">{error}</p>}
        <button type="submit">Create account</button>
        <Link to="/login">Already have an account? Log in</Link>
      </form>
    </main>
  );
}
