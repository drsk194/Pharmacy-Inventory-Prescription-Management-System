import { useEffect, useState } from "react";
import { getCsAuthorizedUntil, isCsAuthorized } from "../../context/csAuthStore";
export default function CsAuthBadge() { const [, tick] = useState(0); useEffect(() => { const timer = setInterval(() => tick((value) => value + 1), 1000); return () => clearInterval(timer); }, []); if (!isCsAuthorized()) return null; const remaining = Math.max(0, new Date(getCsAuthorizedUntil()) - new Date()); return <span className="cs-auth-badge">CS access active - {Math.floor(remaining / 60000)}:{String(Math.floor(remaining / 1000) % 60).padStart(2, "0")}</span>; }
