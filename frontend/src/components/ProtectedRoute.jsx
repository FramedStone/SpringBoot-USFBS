import { useEffect, useState } from "react";
import { Navigate } from "react-router-dom";
import { authFetch } from "@utils/authFetch";
import Spinner from "@components/Spinner"; // 🅿️ PascalCase for component

/**
 * 🅿️ ProtectedRoute
 * Restricts access by role and banned status.
 * Shows spinner while loading, toast on error, and redirects to /login.
 */
function ProtectedRoute({ children, setToast, allowedRoles }) {
  const [isAuth, setIsAuth] = useState(null);

  useEffect(() => {
    async function checkAuth() {
      try {
        const res = await authFetch(
          `${import.meta.env.VITE_BACKEND_URL}/api/auth/me`
        );
        if (!res.ok) throw new Error("Access denied");
        const data = await res.json();

        // 🐫 camelCase: Check for banned user
        if (data.isBanned) {
          setIsAuth(false);
          if (setToast)
            setToast({
              msg: "You have been banned from this website, contact admin for help",
              type: "error",
              autoDismiss: true,
            });
          return;
        }

        // 🐫 camelCase: Role-based access
        if (!allowedRoles || allowedRoles.includes(data.role)) {
          setIsAuth(true);
        } else {
          setIsAuth(false);
          if (setToast)
            setToast({
              msg: "Access denied",
              type: "error",
              autoDismiss: true,
            });
        }
      } catch (err) {
        setIsAuth(false);
        if (setToast)
          setToast({
            msg: "Access denied",
            type: "error",
            autoDismiss: true,
          });
        // 🐫 camelCase: Log error with context
        console.error("Access denied in ProtectedRoute:", err);
      }
    }
    checkAuth();
  }, [allowedRoles, setToast]);

  if (isAuth === null) return <Spinner />; // 🅿️ PascalCase spinner component
  if (!isAuth) return <Navigate to="/login" replace />;
  return children;
}

export default ProtectedRoute;