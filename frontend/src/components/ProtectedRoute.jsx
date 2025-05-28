import { useEffect, useState } from "react";
import { Navigate } from "react-router-dom";
import Spinner from "@components/Spinner";
import { authFetch } from "@utils/authFetch";

/**
 * Restricts access by role and banned status.
 * Shows spinner while loading, toast on error, and redirects to /login.
 */
function ProtectedRoute({ children, setToast, allowedRoles, resetAppState }) {
  const [isAuth, setIsAuth] = useState(null);

  useEffect(() => {
    async function checkAuth() {
      try {
        const res = await authFetch(
          `${import.meta.env.VITE_BACKEND_URL}/api/auth/me`
        );
        if (!res.ok) throw new Error("Access denied");
        const data = await res.json();

        if (data.isBanned) {
          setIsAuth(false);
          if (setToast)
            setToast({
              msg: "You have been banned from this website, contact admin for help",
              type: "error",
              autoDismiss: true,
            });
          if (resetAppState) resetAppState();
          return;
        }

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
          if (resetAppState) resetAppState();
        }
      } catch (err) {
        setIsAuth(false);
        if (setToast)
          setToast({
            msg: "Session expired. Please login again.",
            type: "error",
            autoDismiss: true,
          });
        if (resetAppState) resetAppState();
      }
    }
    checkAuth();
  }, [allowedRoles, setToast, resetAppState]);

  if (isAuth === null) return <Spinner />;
  if (!isAuth) return <Navigate to="/login" replace />;
  return children;
}

export default ProtectedRoute;