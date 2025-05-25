import { BrowserRouter as Router, Routes, Route, Navigate, useLocation } from "react-router-dom";
import { useEffect, useState } from "react";
import { JsonRpcProvider } from "@ethersproject/providers";
import Login from "@components/Login";
import AdminDashboard from "@pages/AdminDashboard"; 
import Toast from "@components/Toast";
import ProtectedRoute from "@components/ProtectedRoute";
import "./App.css";

function AppRoutes() {
  const location = useLocation();
  const [web3Auth, setWeb3Auth] = useState(null);
  const [user, setUser] = useState(null);
  const [toast, setToast] = useState({ msg: "", type: "error" });

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
            />
          }
        />
        <Route
          path="/admin/dashboard"
          element={
            <ProtectedRoute>
              <AdminDashboard />
            </ProtectedRoute>
          }
        />
        <Route path="/" element={<Navigate to="/login" replace />} />
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
