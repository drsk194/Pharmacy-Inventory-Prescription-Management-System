import { Link } from "react-router-dom";
export default function NotAuthorizedPage() { return <main className="auth-page"><div className="auth-form"><h1>Not authorized</h1><p>Your account does not have access to this page.</p><Link to="/">Return home</Link></div></main>; }
