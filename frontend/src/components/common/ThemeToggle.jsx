import { useEffect, useState } from "react";

const STORAGE_KEY = "pipms-theme";

export default function ThemeToggle() {
  const [isLight, setIsLight] = useState(() => localStorage.getItem(STORAGE_KEY) !== "dark");

  useEffect(() => {
    document.documentElement.dataset.theme = isLight ? "light" : "dark";
    localStorage.setItem(STORAGE_KEY, isLight ? "light" : "dark");
  }, [isLight]);

  return (
    <button
      type="button"
      className="theme-toggle"
      onClick={() => setIsLight((current) => !current)}
      aria-label={`Switch to ${isLight ? "dark" : "light"} theme`}
      title={`Switch to ${isLight ? "dark" : "light"} theme`}
    >
      <span aria-hidden="true">{isLight ? "☾" : "☀"}</span>
      <span>{isLight ? "Dark" : "Light"}</span>
    </button>
  );
}