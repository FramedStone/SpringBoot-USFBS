import { useEffect, useState } from "react";
import { Navigate } from "react-router-dom";
import { authFetch } from "@utils/authFetch";

function ProtectedRoute({ children, setToast }) {
  const [isAuth, setIsAuth] = useState(null);

  useEffect(() => {
    async function checkAuth() {
      try {
        const res = await authFetch(
          `${import.meta.env.VITE_BACKEND_URL}/api/auth/me`
        );
        if (res.ok) setIsAuth(true);
        else throw new Error("Access denied");
      } catch (err) {
        setIsAuth(false);
        if (setToast) setToast({ msg: "Access denied", type: "error" });
        console.error("Access denied:", err);
      }
    }
    checkAuth();
  }, []);

  if (isAuth === null) return null; // or loading spinner
  if (!isAuth) return <Navigate to="/login" replace />;
  return children;
}

export default ProtectedRoute;