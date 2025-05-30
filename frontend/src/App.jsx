import { BrowserRouter as Router, Routes, Route, Navigate, useLocation } from "react-router-dom";
import { useEffect, useState } from "react";
import { JsonRpcProvider } from "@ethersproject/providers";
import Login from "@pages/Login";
import AdminDashboard from "@pages/AdminDashboard";
import SystemLogs from "@pages/SystemLogs";
import BookingManagement from "@pages/BookingManagement";
import UserManagement from "@pages/UserManagement";
import SportAndCourtManagement from "@pages/SportAndCourtManagement";
import Toast from "@components/Toast";
import ProtectedRoute from "@components/ProtectedRoute";
import "./App.css";

function AppRoutes() {
  const location = useLocation();
  const [web3Auth, setWeb3Auth] = useState(null);
  const [user, setUser] = useState(null);
  const [toast, setToast] = useState({ msg: "", type: "error" });
  const [activeTab, setActiveTab] = useState("dashboard");

  useEffect(() => {
    const checkBlockchain = async () => {
      try {
        const provider = new JsonRpcProvider(import.meta.env.VITE_QUORUM_RPC_URL);
        await provider.getBlockNumber();
      } catch (err) {
        setToast({ msg: "Blockchain node is offline or unreachable.", type: "error" });
        console.error("Blockchain health check failed:", err);
      }
    };
    checkBlockchain();
  }, [location]);

  // Reset all app state and storage on logout or token expiry
  const resetAppState = () => {
    setActiveTab("dashboard");
    setUser(null);
    setToast({ msg: "", type: "error" });
    localStorage.clear();
    sessionStorage.clear();
  };

  return (
    <>
      <Routes>
        <Route
          path="/login"
          element={
            <Login
              web3Auth={web3Auth}
              setWeb3Auth={setWeb3Auth}
              setUser={setUser}
              setToast={setToast}
              resetAppState={resetAppState}
            />
          }
        />
        <Route
          path="/admin/dashboard"
          element={
            <ProtectedRoute setToast={setToast} resetAppState={resetAppState} allowedRoles={["Admin"]}>
              <AdminDashboard activeTab={activeTab} setActiveTab={setActiveTab} />
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin/user-management"
          element={
            <ProtectedRoute setToast={setToast} allowedRoles={["Admin"]}>
              <UserManagement />
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin/sportfacility&court-management"
          element={
            <ProtectedRoute setToast={setToast} allowedRoles={["Admin"]}>
              <SportAndCourtManagement />
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin/booking-management"
          element={
            <ProtectedRoute setToast={setToast} allowedRoles={["Admin"]}>
              <BookingManagement />
            </ProtectedRoute>
          }
        />
        <Route
          path="/logs"
          element={
            <ProtectedRoute setToast={setToast} allowedRoles={["Admin", "Moderator"]}>
              <SystemLogs />
            </ProtectedRoute>
          }
        />
        <Route path="*" element={<Navigate to="/login" replace />} />
      </Routes>
      <Toast
        message={toast.msg}
        type={toast.type}
        onClose={() => setToast({ msg: "", type: "error" })}
      />
    </>
  );
}

function App() {
  return (
    <Router>
      <AppRoutes />
    </Router>
  );
}

export default App;
