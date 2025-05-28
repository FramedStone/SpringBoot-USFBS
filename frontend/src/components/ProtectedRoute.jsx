import { useEffect, useState } from "react";
import { Navigate } from "react-router-dom";
import { authFetch } from "@utils/authFetch";
import Spinner from "@components/Spinner";  
import BookingManagement from "@pages/BookingManagement";

/**
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
        console.error("Access denied in ProtectedRoute:", err);
      }
    }
    checkAuth();
  }, [allowedRoles, setToast]);

  if (isAuth === null) return <Spinner />; 
  if (!isAuth) return <Navigate to="/login" replace />;
  return children;
}

export default ProtectedRoute;