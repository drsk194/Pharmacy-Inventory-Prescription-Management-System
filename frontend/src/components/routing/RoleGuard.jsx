import { Navigate } from "react-router-dom";
import { useAuth } from "../../hooks/useAuth";
export default function RoleGuard({ allow, children }) {
  const { user } = useAuth();
  return user?.roles?.some((role) => allow.includes(role)) ? children : <Navigate to="/not-authorized" replace />;
}
