import { useState } from "react";

export default function PasswordInput({ name, value, onChange, ...props }) {
  const [visible, setVisible] = useState(false);

  return (
    <span className="password-input">
      <input
        {...props}
        name={name}
        type={visible ? "text" : "password"}
        value={value}
        onChange={onChange}
      />
      <button
        type="button"
        className="password-input__toggle"
        onClick={() => setVisible((current) => !current)}
        aria-label={visible ? "Hide password" : "Show password"}
      >
        {visible ? "Hide" : "Show"}
      </button>
    </span>
  );
}
